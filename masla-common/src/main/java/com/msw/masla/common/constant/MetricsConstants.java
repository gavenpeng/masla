package com.msw.masla.common.constant;


import com.msw.masla.common.util.StringUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class MetricsConstants {

  @Value("${masla.metrics.switch.list:}")
  private String reportList = "SessionMetrics,ApiMetrics,ChannelPoolMetrics,AlertMetrics,BandwidthMetrics,RequestBodyMetrics,ResponseBodyMetrics,ResponseTimeMetrics,AppRequestFailedMetrics,DomainMetrics,GlobalRequestFailedMetrics,GroupServerMertics,TotalMetrics,DomainTotalMetrics,DirectMemoryMetrics,AcceptQueueMetrics,TaskQueueMetrics,HeraIDCMetrics";

  public static Set<String> reportSet;

  public static final String SESSION_METRICS = "SessionMetrics";

  public static final String GROUP_SERVER_METRICS = "GroupServerMertics";

  public static final String TOTAL_METRICS = "TotalMetrics";

  public static final String API_METRICS = "ApiMetrics";

  public static final String CHANNEL_POOL_METRICS = "ChannelPoolMetrics";

  public static final String ALERT_METRICS = "AlertMetrics";

  public static final String DIRECT_MEMORY_METRICS = "DirectMemoryMetrics";

  public static final String BANDWIDTH_METRICS = "BandwidthMetrics";

  public static final String REQUEST_BODY_METRICS = "RequestBodyMetrics";

  public static final String RESPONSE_BODY_METRICS = "ResponseBodyMetrics";

  public static final String RESPONSE_TIME_METRICS = "ResponseTimeMetrics";

  public static final String APP_REQUEST_FAILED_METRICS = "AppRequestFailedMetrics";

  public static final String GLOBAL_REQUEST_FAILED_METRICS = "GlobalRequestFailedMetrics";

  public static final String DOMAIN_METRICS = "DomainMetrics";

  public static final String DOMAIN_TOTAL_METRICS = "DomainTotalMetrics";

  public static final String ACCEPT_QUEUE_METRICS = "AcceptQueueMetrics";

  public static final String TASK_QUEUE_METRICS = "TaskQueueMetrics";

  public static final String FLUSH_PENDING_METRICS = "FlushPendingMetrics";

  public static final String HERA_IDC_METRICS = "HeraIDCMetrics";

  public static final String BIZ_ERR_METRICS = "BusinessExceptionMetrics";

  public static final String OPEN_SSL_METRICS = "OpenSslStatsMetrics";

  public String getReportList() {
    return reportList;
  }

  public void setReportList(String reportList) {
    this.reportList = reportList;

    if (StringUtil.isEmptyString(reportList)) {
      reportSet = new HashSet<String>();
      return;
    }

    HashSet<String> temp = new HashSet<String>();
    for (String metricItem : reportList.split(",")) {
      temp.add(metricItem.trim());
    }
    reportSet = temp;
  }
}
