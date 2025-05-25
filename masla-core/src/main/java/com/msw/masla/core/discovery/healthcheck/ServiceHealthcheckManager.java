package com.msw.masla.core.discovery.healthcheck;

import com.msw.masla.common.config.MaslaServerConfig;
import com.msw.masla.common.constant.Constants;
import com.msw.masla.common.enums.HostStatus;
import com.msw.masla.common.pojo.ServiceApp;
import com.msw.masla.common.util.AtomicPositiveInteger;
import com.msw.masla.common.util.MaslaSpringContextUtil;
import com.msw.masla.common.util.StringBuilderHolder;
import com.msw.masla.common.util.StringUtil;
import com.msw.masla.core.discovery.nacos.HostProfile;
import com.msw.masla.core.discovery.nacos.MaslaNacosServiceDiscovery;
import com.msw.masla.core.router.rule.RouteRuleCache;
import com.msw.masla.protocol.http.netty.http.HostInstance;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;


/**
 * Created by Gavin.peng on 2023/8/22.
 */
public class ServiceHealthcheckManager {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceHealthcheckManager.class);

    private static final String METHOD_GET = "GET" ;

    public static final int STATUS_TURN_OFF = 406;

    public static final int STATUS_TURN_ONLINE = 200;


    public static final int STATUS_TURN_OFF_INTERVAL = 1000 * 5;

    public static final int STATUS_TURN_OFF_DELAY = 1000 * 60;

    private Timer timer = new Timer("Masla-host-healthcheck-timer");

    private final Map<HostProfile, HealthCheckCount> healthCheckCountMap = new ConcurrentHashMap<HostProfile, HealthCheckCount>();

    private final Map<HostProfile, HealthCheckCount> healthCheckConnRefusedCountMap = new ConcurrentHashMap<HostProfile, HealthCheckCount>();

    private final Map<HostProfile, HealthCheckCount> healthCheckNoRouteToHostCountMap = new ConcurrentHashMap<HostProfile, HealthCheckCount>();

    private MaslaNacosServiceDiscovery maslaNacosServiceDiscovery;

    private MaslaServerConfig maslaServerConfig;


    static class HealthcheckManagerHolder {
        public static ServiceHealthcheckManager instance = new ServiceHealthcheckManager();
    }

    public static ServiceHealthcheckManager getInstance(){
        return HealthcheckManagerHolder.instance;
    }

    private ServiceHealthcheckManager(){

    }

    public void startDoHealthcheck() {
        if(!isHealthCheckDisabled()){
            timer.schedule(new TurnOffTask(), STATUS_TURN_OFF_DELAY, STATUS_TURN_OFF_INTERVAL);
        }
    }

    public void registerMaslaNacosServiceDiscovery(MaslaNacosServiceDiscovery maslaNacosServiceDiscovery) {
        this.maslaNacosServiceDiscovery = maslaNacosServiceDiscovery;
    }

    public void registerMaslaSeverConfig(MaslaServerConfig maslaServerConfig) {
        this.maslaServerConfig = maslaServerConfig;
    }

    private boolean isHealthCheckDisabled(){
        return maslaServerConfig.isServiceHealthcheckDisabled();
    }

    public void addHostProfileNEW(ServiceApp appDO, HostProfile hostProfile){

    }

    public void removeDisableHost(HostProfile hostProfile){
        this.healthCheckCountMap.remove(hostProfile);
        this.healthCheckConnRefusedCountMap.remove(hostProfile);
        this.healthCheckNoRouteToHostCountMap.remove(hostProfile);

    }


    private boolean doHealthCheck(HostProfile hostProfile) throws Throwable{

        ServiceApp appDO = RouteRuleCache.getRouteAppCache(hostProfile.getServiceId());
        String contextPath = "/"+hostProfile.getServiceId();
        String checkPath = StringBuilderHolder.getGlobal()
                .append(Constants.HTTP_PROTOCOL_1).append(hostProfile.getHost())
                .append(":").append(hostProfile.getPort())
                .append(contextPath).append(Constants.MASLA_HEALTHCHECK_PATH_END).toString();
        int rs = this.doTurnOffCheck(checkPath);
        if (rs == STATUS_TURN_ONLINE) {
            if(hostProfile.getCurStatus() == HostStatus.EXC_DISENABLE
                    || hostProfile.getCurStatus() == HostStatus.EXC_TEMP_DISENABLE){
                hostProfile.setCurStatus(HostStatus.EXCLUDE);
                LOG.warn("Masla found app {} host {} port {} is turn on,status {}", hostProfile.getServiceId(), hostProfile.getHost(), hostProfile.getPort(),hostProfile.getCurStatus().name());
            }else if(hostProfile.getCurStatus() == HostStatus.DISENABLE
                    || hostProfile.getCurStatus() == HostStatus.TEMP_DISENABLE){
                makeHostSlowStream(hostProfile);
                hostProfile.setCurStatus(HostStatus.ENABLE);
                LOG.warn("Masla found app {} host {} port {} is turn on,status {}", appDO.getName(), hostProfile.getHost(), hostProfile.getPort(),hostProfile.getCurStatus().name());
            }
            return true;
        }else{
            disableHostProfile(hostProfile);
            LOG.warn("Masla found app {} host {} port {} is turn off,response code {} status {}", appDO.getName(), hostProfile.getHost(), hostProfile.getPort(),rs,hostProfile.getCurStatus().name());
            if(rs >=400 && rs != STATUS_TURN_OFF){
                doHealthCheckAlert(hostProfile, appDO);
                return false;
            }
            return true;
        }
    }


    private void makeHostSlowStream(HostProfile hostProfile){
        long now = System.currentTimeMillis();
        hostProfile.resetSlowUpgrade(now);
    }


    private String doGet(String httpUrl,String requestParams) throws Exception{

        if(!StringUtil.isEmptyString(requestParams)) {
            if (httpUrl.contains("?")) {
                httpUrl += "&" + requestParams;
            } else {
                httpUrl += "?" + requestParams;
            }
        }
        URL url = new URL(httpUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(METHOD_GET);
        conn.setConnectTimeout(3000);
        conn.setReadTimeout(5000);
        conn.setRequestProperty("Content-Type", "text/html; charset=UTF-8");
//        conn.setRequestProperty(ClientConstants.X_GW_REQ_ID, "-1");
        conn.connect();

        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line;
        StringBuilder result = new StringBuilder();
        while ((line = br.readLine()) != null) {
            result.append(line);
        }

        conn.disconnect();

        return result.toString();
    }

    private int doTurnOffCheck(String checkUrl) throws Exception {
        URL url = new URL(checkUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(METHOD_GET);
        conn.setConnectTimeout(500);
        conn.setReadTimeout(1000);
        conn.setRequestProperty("Content-Type", "text/html; charset=UTF-8");
        conn.connect();
        int code = conn.getResponseCode();
        conn.disconnect();
        return code;

    }


    private final class TurnOffTask
            extends TimerTask
    {
        public void run()
        {
            try{
                Map<String, List<HostInstance>> listMap = maslaNacosServiceDiscovery.getAllInstances();
                Iterator<Map.Entry<String, List<HostInstance>>> iterator = listMap.entrySet().iterator();
                while (iterator.hasNext()){
                    Map.Entry<String, List<HostInstance>> entry = iterator.next();
                    List<HostInstance> hostProfileList = entry.getValue();
                    if(!hostProfileList.isEmpty()){
                        doAppHostListHealthCheck(hostProfileList);
                    }

                }


            }catch (Throwable e){
                LOG.error("Masla do health check task failed:{}", e.getMessage());
            }
        }


    }


    private void doAppHostListHealthCheck(List<HostInstance> hostProfileList){
        for (HostInstance hostInstance : hostProfileList) {
            HostProfile hostProfile = (HostProfile) hostInstance;
            try {
                if(doHealthCheck(hostProfile)) {
                    hostProfile.resetTimeout();
                    healthCheckCountMap.remove(hostProfile);
                    healthCheckConnRefusedCountMap.remove(hostProfile);
                    healthCheckNoRouteToHostCountMap.remove(hostProfile);
                }
            } catch (Throwable e) {
                healthcheckFailed(hostProfile, e);
            }
        }
    }

    private boolean healthcheckFailed(HostProfile hostProfile, Throwable e) {
        try {
            ServiceApp appDO = RouteRuleCache.getRouteAppCache(hostProfile.getServiceId());
            String appName = appDO == null ? "unKnow" : appDO.getName();
            HostStatus prevStatus = hostProfile.getCurStatus();
            long lastUnavailableTime = hostProfile.getLastMarkTimeoutTime();
            if(lastUnavailableTime <=0 && (prevStatus == HostStatus.DISENABLE
                || prevStatus == HostStatus.EXC_DISENABLE)){
                //hostProfile.setUnavailableTime();
                return true;
            }

            if(lastUnavailableTime > 0){
                if(System.currentTimeMillis() - lastUnavailableTime < 10 * STATUS_TURN_OFF_DELAY) {
                    return true;
                }
            }
            if (!isTimeoutException(e) && disableHostProfile(hostProfile,true)) {
                LOG.warn("Masla found app {} host {} port {} health check failed {} so do turn off status {}",
                        appName, hostProfile.getHost(), hostProfile.getPort(),e.getMessage(),
                        hostProfile.getCurStatus().name());
            }
            doHealthCheckExceptionAlert(hostProfile, appDO, e);
            return true;
        } catch (Throwable ee) {
            LOG.error("Masla found ip={} port={} healthcheck handle failed:",
                    hostProfile.getHost(), hostProfile.getPort(), ee);
        }
        return true;
    }

    private boolean isTimeoutException(Throwable e){
        if(e.getMessage() != null && e.getMessage().contains(Constants.TIMED_OUT_EXCEPTION)){
            return true;
        }
        return false;
    }

    private void doHealthCheckExceptionAlert(HostProfile hostProfile, ServiceApp appDO, Throwable e) {
        String errMsg = e.getMessage();
        if (errMsg.contains(Constants.CONN_REFUSED) || errMsg.contains(Constants.CONN_REFUSED_CHINESE)) {
            HealthCheckCount healthCheckCount = healthCheckConnRefusedCountMap.get(hostProfile);
            if (null == healthCheckCount) {
                healthCheckCount = new HealthCheckCount(1);
                healthCheckConnRefusedCountMap.put(hostProfile, healthCheckCount);
            }
            healthCheckCount.count.incrementAndGet();
        } else if (errMsg.contains(Constants.NO_ROUTE_TO_HOST) || errMsg.contains(Constants.NO_ROUTE_TO_HOST_CHINESE)
                    || errMsg.contains(Constants.CONNECT_TIMED_OUT)) {
            HealthCheckCount healthCheckCount = healthCheckNoRouteToHostCountMap.get(hostProfile);
            if (null == healthCheckCount) {
                healthCheckCount = new HealthCheckCount(0);
                healthCheckNoRouteToHostCountMap.put(hostProfile, healthCheckCount);
            }
            int timeoutCount = healthCheckCount.count.incrementAndGet();
            if(timeoutCount >= MaslaSpringContextUtil.getMaslaConfConfigBean().getAppHealthcheckTimeoutFailedThreshold()){
                LOG.warn("Masla found app {} host {} continue {} nums health check failed {} so disabled",appDO.getName(),hostProfile.getHost(),timeoutCount,errMsg);
                disableHostProfile(hostProfile,true);
            }

        }else{

            try {
                doHealthCheckAlert(hostProfile, appDO);
            } catch (Throwable ee) {
                LOG.error("Masla do healthcheck failed alert failed:", ee);
            }
        }
    }

    private void doHealthCheckAlert(HostProfile hostProfile, ServiceApp appDO) {
        HealthCheckCount healthCheckCount = healthCheckCountMap.get(hostProfile);
        if (healthCheckCount == null) {
          healthCheckCount = new HealthCheckCount(1);
          healthCheckCountMap.put(hostProfile, healthCheckCount);
        } else {
          healthCheckCount.getCount().incrementAndGet();
        }
    }

    private boolean disableHostProfile(HostProfile hostProfile){
        return disableHostProfile(hostProfile,false);
    }

    private boolean disableHostProfile(HostProfile hostProfile,boolean tempDisable){
        HostStatus curStatus = hostProfile.getCurStatus();
        if(curStatus == HostStatus.EXC_DISENABLE
                || curStatus == HostStatus.DISENABLE
                || curStatus == HostStatus.TEMP_DISENABLE
                || curStatus == HostStatus.EXC_TEMP_DISENABLE){
            return false;
        }
        if(hostProfile.getCurStatus() == HostStatus.EXCLUDE){
            hostProfile.setCurStatus(tempDisable?HostStatus.EXC_TEMP_DISENABLE:HostStatus.EXC_DISENABLE);
        }else {
            hostProfile.setCurStatus(tempDisable?HostStatus.TEMP_DISENABLE:HostStatus.DISENABLE);
        }
        return true;
    }

    public void destory(){

        try {
            timer.cancel();
            LOG.warn("Masla healthcheck pool executor exit!!!");
        }catch (Throwable e){

        }
    }


    public Map<HostProfile, HealthCheckCount> getHealthCheckCountMap() {
        return healthCheckCountMap;
    }

    public Map<HostProfile, HealthCheckCount> getHealthCheckConnRefusedCountMap() {
        return healthCheckConnRefusedCountMap;
    }

    public Map<HostProfile, HealthCheckCount> getHealthCheckNoRouteToHostCountMap() {
        return healthCheckNoRouteToHostCountMap;
    }

    @Data
  public static class HealthCheckCount {

    public HealthCheckCount(int count) {

      this.count = new AtomicPositiveInteger(count);
      this.startTime = new AtomicLong(System.currentTimeMillis());
      this.lastAlertTime = new AtomicLong(0);
    }


    private AtomicPositiveInteger count;

    private AtomicLong startTime;

    private AtomicLong lastAlertTime;
  }
}
