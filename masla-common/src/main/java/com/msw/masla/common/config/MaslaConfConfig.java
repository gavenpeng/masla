package com.msw.masla.common.config;

import com.alibaba.nacos.api.config.annotation.NacosValue;
import com.msw.masla.common.constant.Constants;
import com.msw.masla.common.util.StringUtil;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.util.*;



/**
 * Created by gavin.peng 2024/7/19.
 */
@Component
@Data
public class MaslaConfConfig {

    protected final Logger LOG = LoggerFactory.getLogger(MaslaConfConfig.class);

    public String localIp;


    public final boolean reportMaslaMetrics = false;

    protected Set<String> prioritySet = new HashSet<String>(2);
    public Map<String,String> extendHeaderMap = new HashMap<String,String>(5);

    @NacosValue("${masla.app.healthcheck.timeout.exception.threshold:4}")
    private Integer appHealthcheckTimeoutFailedThreshold = 4;

    @NacosValue("${masla.app.channel.pool.pending.threshold:5000}")
    private Integer  appChannelPoolPendingThreshold = 5000;

    @NacosValue("${masla.app.default.conn.refused.threshold:10}")
    private Integer  appConnRefusedDefaultThreshold = 10;

    @NacosValue("${masla.app.default.conn.reset.threshold:10}")
    private Integer  appConnResetDefaultThreshold = 10;

    @NacosValue("${masla.app.default.conn.timeout.threshold:10}")
    private Integer  appConnTimeoutDefaultThreshold = 10;

    @NacosValue("${masla.app.default.conn.pool.full.reject.threshold:10}")
    private Integer  appConnPoolFullRejectDefaultThreshold = 10;

    @NacosValue("${masla.app.default.conn.pool.wait.timeout.threshold:10}")
    private Integer  appConnPoolWaitTimeoutDefaultThreshold = 10;

    @NacosValue("${monitor.alarm.netty.server.max.qps:16000}")
    private Integer nettyServerMaxQqs = 16000;

    @NacosValue("${masla.server.app.max.sessions:5000}")
    private Integer appMaxSessions = 5000;//单机app 最大连接数

    @NacosValue("${masla.server.app.max.session.turn.on:}")
    private String turnOffAppMaxSessionLimit;

    @NacosValue("${masla.server.max.sessions:20000}")
    private Integer serverMaxSessions = 20000;

    @NacosValue("${masla.server.h2.max.sessions:20000}")
    private Integer serverH2MaxSessions = 20000;

    @NacosValue("${masla.work.thread.mode:pool}")
    private String workThreadMode = "pool";//pool/nio

    @NacosValue("${masla.https.work.thread.mode:pool}")
    private String httpsWorkThreadMode = "pool";//pool/nio

    @NacosValue("${masla.push.thread.mode:pool}")
    private String pushThreadMode = "pool";//pool/nio

    @NacosValue("${masla.ssl.http2.disabled:false}")
    private Boolean http2Disabled = false;

    @NacosValue("${masla.app.circuit.expired.minute:300}")
    private String appCircuitExpiredMinuteTime;

    @NacosValue("${masla.circuit.upgrade.threshold:0.5}")
    private String circuitUpgradeThreshold;

    @NacosValue(value = "${masla.circuit.open.min.request.threshold:100}", autoRefreshed = true)
    private Integer circuitOpenMinRequestThreshold;

    @NacosValue(value = "${masla.circuit.open.trigger.second.threshold:10}", autoRefreshed = true)
    private Integer circuitTriggerSecond;


    @NacosValue("${masla.circuit.auto.upgradeordown.switch:on}")
    private String circuitAutoUpgradeOrDownSwitch;

    @NacosValue("${masla.healthcheck.turn.off:}")
    private String turnOffHealthcheck;

    @NacosValue("${masla.auto.circuit.close.app:}")
    private String circuitCloseApp;

    @NacosValue("${masla.filter.request.header.app:}")
    private String filterRequestHeaderApps;

    @NacosValue("${masla.auto.flow.low.water:5000}")
    private Integer autoFlowLowWater = 5000;

    @NacosValue("${masla.backup.dc.auto.flow.low.water:1000}")
    private Integer backupDcAutoFlowLowWater = 1000;

    @NacosValue("${masla.qps.bandwidth.influxdb.query.delay:60}")
    private Integer qpsAndBandwidthInfluxdbQueryDelay = 60;//单位秒(30的整数倍)


    @NacosValue("${masla.total.metrics.url.index:0}")
    public int totalMetricsUrlIndex = 0;

    @NacosValue("${masla.total.domain.metrics.url.index:1}")
    public int totalDomainMetricsUrlIndex = 1;

    @NacosValue("${masla.flow.selector.cache.max.size:500}")
    public int flowSelectorCacheMaxSize = 500;

    @NacosValue("${masla.flow.selector.cache.expire.after.access.seconds:1800}")
    public int flowSelectorCacheExpireAfterAccess = 1800;

    @NacosValue("${masla.server.sessionTimeout:20000}")
    private Integer serverSessionTimeout = 20000;

    @NacosValue("${masla.console.app.local.cache.size:10}")
    private int appLocalCacheSize = 10;

    @NacosValue("${masla.console.domain.local.cache.size:10}")
    private int domainLocalCacheSize = 10;
    public boolean supportUpload = false;

    public long requestSizeLimit = 10 * 1024 * 1024l;

    public long responseSizeLimit = 10 * 1024 * 1024l;

    public boolean OOMTurnOffAllCache = false;


    {
        try{
            localIp = InetAddress.getLocalHost().getHostAddress();
        }catch (Exception e){
            localIp = null;
        }
    }


   public int getApiSlowResponseTimeInterval(){
        return 1000;
   }

    public float getCircuitUpgradeThreshold() {
        return circuitUpgradeThreshold == null?0.5f:Float.valueOf(circuitUpgradeThreshold);
    }

    public Set<String> getPrioritySet() {
        return prioritySet;
    }

    @NacosValue("${Masla.priority.app:}")
    public void setPriorityApps(String priorityApps) {
        if(!StringUtil.isEmptyString(priorityApps)){
            Set setTemp = new HashSet<String>(5);
            String[] priorityAppArray = priorityApps.split(";");
            for(String priorityApp:priorityAppArray){
                setTemp.add(priorityApp);
            }
            prioritySet = setTemp;
        }
    }


    public Map<String,String> getDomainExtendHeaderMap() {
        return extendHeaderMap;
    }

    public boolean supportHealthcheck(String appName){
        if(!StringUtil.isEmptyString(turnOffHealthcheck)){
            String[] filterName = turnOffHealthcheck.split(";");
            for(String filter:filterName){
                if(filter.equals(appName)){
                    return false;
                }
            }
        }

        return true;
    }


}
