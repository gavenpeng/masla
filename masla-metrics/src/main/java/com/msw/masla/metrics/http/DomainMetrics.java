package com.msw.masla.metrics.http;

import com.msw.masla.common.enums.SessionType;
import com.msw.masla.common.monitor.metrics.DomainCount;
import com.msw.masla.common.pojo.IOTDevice;
import com.msw.masla.common.util.MaslaSpringContextUtil;
import com.msw.masla.common.util.StringUtil;
import com.msw.masla.metrics.frame.AbstractMetrics;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 域名及nginx带宽,QPS统计
 *
 * @author Gavin.peng
 */
@Slf4j
public class DomainMetrics extends AbstractMetrics {

  private static final int EXPIRE_TIME = 30 * 60 * 1000;
  private static final int SIZE_LIMIT = 1000;
  private static final String COM_DOMAIN_SUFFIX = "com";
  private static final String FM_DOMAIN_SUFFIX = "fm";


  private static ConcurrentHashMap<String, DomainCount> domainCountMap = new ConcurrentHashMap<String, DomainCount>();

  @Override
  public List getMetrics() {

    long timestamp = getTimestamp();

    List<String> removedKeyList = new ArrayList<String>();
    List<DomainCount> domainCountList = new ArrayList<DomainCount>(domainCountMap.size());
    for (DomainCount count : domainCountMap.values()) {
      DomainCount domainCount = new DomainCount();
      domainCount.setDomain(count.getDomain());
      domainCount.setNginxIp(count.getNginxIp());
      domainCount.setAppId(count.getAppId());
      domainCount.setApp(count.getApp());
      // 机房
      domainCount.setDc(count.getDc());

      // 全局统计
      DomainTotalMetrics.getInstance().convert2DomainCount(count);

      domainCount.getQueryCount().addAndGet(count.getQueryCount().getAndSet(0));
      domainCount.getFlowControlNums().addAndGet(count.getFlowControlNums().getAndSet(0));
      domainCount.getIpv6Qps().addAndGet(count.getIpv6Qps().getAndSet(0));
      domainCount.getInBandwidth().addAndGet(count.getInBandwidth().getAndSet(0));
      domainCount.getOutBandwidth().addAndGet(count.getOutBandwidth().getAndSet(0));
      domainCount.getIosH2Qps().addAndGet(count.getIosH2Qps().getAndSet(0));
      domainCount.getIosHTTPSQps().addAndGet(count.getIosHTTPSQps().getAndSet(0));
      domainCount.getIosQps().addAndGet(count.getIosQps().getAndSet(0));
      domainCount.getIosReQps().addAndGet(count.getIosReQps().getAndSet(0));
      domainCount.getAndroidQps().addAndGet(count.getAndroidQps().getAndSet(0));
      domainCount.getAndroidH2Qps().addAndGet(count.getAndroidH2Qps().getAndSet(0));
      domainCount.getAndroidHTTPSQps().addAndGet(count.getAndroidHTTPSQps().getAndSet(0));
      domainCount.getAndroidReQps().addAndGet(count.getAndroidReQps().getAndSet(0));
      domainCount.getIosInBandWidth().addAndGet(count.getIosInBandWidth().getAndSet(0));
      domainCount.getAndroidInBandWidth().addAndGet(count.getAndroidInBandWidth().getAndSet(0));
      domainCount.getWpInBandWidth().addAndGet(count.getWpInBandWidth().getAndSet(0));
      domainCount.getWpQps().addAndGet(count.getWpQps().getAndSet(0));
      domainCount.getWpReQps().addAndGet(count.getWpReQps().getAndSet(0));
      domainCount.getIosWatchQps().addAndGet(count.getIosWatchQps().getAndSet(0));
      domainCount.getIosWatchInBandWidth().addAndGet(count.getIosWatchInBandWidth().getAndSet(0));
      domainCount.getIosPadQps().addAndGet(count.getIosPadQps().getAndSet(0));
      domainCount.getIosPadInBandWidth().addAndGet(count.getIosPadInBandWidth().getAndSet(0));
      count.resetExtendKeyMap(domainCount);
      domainCount.setTimestamp(timestamp);
      domainCount.setHost(MaslaSpringContextUtil.getMaslaConfConfigBean().getLocalIp());
      domainCountList.add(domainCount);

      if (System.currentTimeMillis() - count.getLastUpdateTime() > EXPIRE_TIME) {
        removedKeyList.add(generateKey(count.getApp(), count.getDomain(), count.getNginxIp(), count.getDc()));
      }
    }

    for (String key : removedKeyList) {
      domainCountMap.remove(key);
    }
    return domainCountList;
  }

  /**
   * 写入流控次数
   */
  public static void addDomainFlowControllerCount(String app, String domain , String ip ,String dc, long nums) {
    if (StringUtils.isEmpty(domain) || StringUtils.isEmpty(ip) || StringUtils.isEmpty(app)) {
      return;
    }
    String key = generateKey(app, domain, ip, dc);
    DomainCount count = domainCountMap.get(key);
    if (null == count) {
      if (domainCountMap.size() <= SIZE_LIMIT) {
        DomainCount domainCount = new DomainCount();
        domainCount.setApp(app);
        domainCount.setNginxIp(ip);
        domainCount.setDomain(domain);
        domainCount.setDc(dc);
        domainCount.getFlowControlNums().addAndGet(nums);
        DomainCount preCount = domainCountMap.putIfAbsent(key, domainCount);
        if (preCount != null) {
          preCount.getFlowControlNums().addAndGet(nums);
        }
      } else {
        log.info("The domain and nginx relations exceed limit {}, not record it, {} {}", SIZE_LIMIT,
                domain, ip);
      }
    } else {
      count.getFlowControlNums().addAndGet(nums);
    }
  }

  public static void countQueryAndInBandwidth(Long appId, String app, String domain, String ip,
                                              long inBandwidth, long qps, IOTDevice iotDevice, boolean isRetryReq, Integer sessionType, List<String> hitHeaders, long outBandwidth,
                                              String dc, long ipv6Qps) {
    if (StringUtils.isEmpty(domain) || StringUtils.isEmpty(ip) || StringUtils.isEmpty(app)) {
      return;
    }

    if(domain.endsWith(COM_DOMAIN_SUFFIX)){
      String key = generateKey(app, domain, ip, dc);
      DomainCount count = domainCountMap.get(key);
      if (count == null) {
        if (domainCountMap.size() <= SIZE_LIMIT) {
          DomainCount domainCount = new DomainCount();
          domainCount.setAppId(appId);
          domainCount.setApp(app);
          domainCount.setNginxIp(ip);
          domainCount.setDomain(domain);
          // 机房
          domainCount.setDc(dc);
          domainCount.getInBandwidth().addAndGet(inBandwidth);
          domainCount.getOutBandwidth().addAndGet(outBandwidth);
          domainCount.getQueryCount().addAndGet(qps);
          domainCount.getIpv6Qps().addAndGet(ipv6Qps);
          DomainCount preCount = domainCountMap.putIfAbsent(key, domainCount);
          if (preCount != null) {
            preCount.getInBandwidth().addAndGet(inBandwidth);
            preCount.getOutBandwidth().addAndGet(outBandwidth);
            preCount.getQueryCount().addAndGet(qps);
            preCount.getIpv6Qps().addAndGet(ipv6Qps);
            setDeviceTypeQpsAndBW(preCount,iotDevice,qps,inBandwidth,isRetryReq,sessionType,hitHeaders);
          }else{
            setDeviceTypeQpsAndBW(domainCount,iotDevice,qps,inBandwidth,isRetryReq,sessionType,hitHeaders);
          }
        }

      } else {
        count.getInBandwidth().addAndGet(inBandwidth);
        count.getOutBandwidth().addAndGet(outBandwidth);
        count.getQueryCount().addAndGet(qps);
        count.getIpv6Qps().addAndGet(ipv6Qps);
        setDeviceTypeQpsAndBW(count,iotDevice,qps,inBandwidth,isRetryReq,sessionType,hitHeaders);
        count.setLastUpdateTime(System.currentTimeMillis());
      }
    }

  }

  private static void setDeviceTypeQpsAndBW(DomainCount domainCount, IOTDevice iotDevice, long qps, long inBW, boolean isAutoRetry, Integer sessionType, List<String> hitHeaders){
    if(!StringUtil.isEmptyString(iotDevice.getOs())) {

      if (StringUtils.equals(iotDevice.getOs(), DomainCount.IOS_WATCH)) {
        domainCount.getIosWatchQps().addAndGet(qps);
        domainCount.getIosWatchInBandWidth().addAndGet(inBW);
      }else if (StringUtils.equals(iotDevice.getOs(), DomainCount.IOS_PAD)) {
        domainCount.getIosPadQps().addAndGet(qps);
        domainCount.getIosPadInBandWidth().addAndGet(inBW);
      }else if (StringUtils.containsIgnoreCase(iotDevice.getOs(),DomainCount.IOS)) {
        if(!isAutoRetry) {
          if(sessionType != null && sessionType.equals(SessionType.HTTPS1.ordinal())){
            domainCount.getIosHTTPSQps().addAndGet(qps);
          }else if(sessionType != null && sessionType.equals(SessionType.HTTPS2.ordinal())){
            domainCount.getIosH2Qps().addAndGet(qps);
          }else {
            domainCount.getIosQps().addAndGet(qps);
          }
        }else{
          domainCount.getIosReQps().addAndGet(qps);
        }
        domainCount.getIosInBandWidth().addAndGet(inBW);
      } else if (StringUtils.containsIgnoreCase(iotDevice.getOs(),DomainCount.ANDROIDS)) {
        if(!isAutoRetry) {
          if(sessionType != null && sessionType.equals(SessionType.HTTPS1.ordinal())) {
            domainCount.getAndroidHTTPSQps().addAndGet(qps);
          }else if(sessionType != null && sessionType.equals(SessionType.HTTPS2.ordinal())){
            domainCount.getAndroidH2Qps().addAndGet(qps);
          }else {
            domainCount.getAndroidQps().addAndGet(qps);
          }
        }else{
          domainCount.getAndroidReQps().addAndGet(qps);
        }
        domainCount.getAndroidInBandWidth().addAndGet(inBW);
      }else if (StringUtils.containsIgnoreCase(iotDevice.getOs(),DomainCount.WINDOW)) {
        if(!isAutoRetry) {
          domainCount.getWpQps().addAndGet(qps);
        }else{
          domainCount.getWpReQps().addAndGet(qps);
        }
        domainCount.getWpInBandWidth().addAndGet(inBW);
      } else{
        log.warn("Masla found request come from {}",iotDevice.getOs());
      }
    }
    //统计自定义请求头的请求,自定义的响应头只统计命中的，不影响总的次数
    if(hitHeaders != null && hitHeaders.size() >0){
      for(String hitHeader:hitHeaders){
        AtomicLong qpsNums = domainCount.getExtendKeyMap().get(hitHeader);
        if(qpsNums == null){
          qpsNums = new AtomicLong(0);
          qpsNums.addAndGet(qps);
        }
        AtomicLong preQpsNums = domainCount.getExtendKeyMap().putIfAbsent(hitHeader,qpsNums);
        if(preQpsNums != null){
          preQpsNums.addAndGet(qps);
        }
      }
    }

  }

  public static void countOutBandwidth(Long appId,String app, String domain, String ip,
      long outBandwidth) {
    if (StringUtils.isEmpty(domain) || StringUtils.isEmpty(ip) || StringUtils.isEmpty(app)) {
      return;
    }

    //
    if(domain.endsWith(COM_DOMAIN_SUFFIX)||domain.endsWith(FM_DOMAIN_SUFFIX)) {
      String key = app + domain + ip;
      DomainCount count = domainCountMap.get(key);
      if (count == null) {
        if (domainCountMap.size() <= SIZE_LIMIT) {
          DomainCount domainCount = new DomainCount();
          domainCount.setAppId(appId);
          domainCount.setApp(app);
          domainCount.setNginxIp(ip);
          domainCount.setDomain(domain);
          domainCount.getOutBandwidth().addAndGet(outBandwidth);
          DomainCount preCount = domainCountMap.putIfAbsent(key, domainCount);
          if (preCount != null) {
//          preCount.getInBandwidth().addAndGet(preCount.getInBandwidth().get());
            preCount.getOutBandwidth().addAndGet(outBandwidth);
            //preCount.getQueryCount().addAndGet(preCount.getQueryCount().get());
          }
        } else {
          log.warn("The domain and nginx relations exceed limit {}, not record it, {} {}", SIZE_LIMIT,
                  domain, ip);
        }
      } else {
        count.getOutBandwidth().addAndGet(outBandwidth);
      }
    }
  }

  private static String generateKey(String app, String domain, String ip, String dc) {
    return app + domain + ip + dc;
  }
}
