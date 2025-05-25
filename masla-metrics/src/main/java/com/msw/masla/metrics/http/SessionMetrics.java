/*
 * Copyright 2025 msw
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package com.msw.masla.metrics.http;

import com.msw.masla.common.config.MaslaConfConfig;
import com.msw.masla.common.monitor.metrics.SessionCount;
import com.msw.masla.common.util.MaslaSpringContextUtil;
import com.msw.masla.metrics.frame.AbstractMetrics;
import com.msw.masla.protocol.http.netty.config.NettyConfig;
import com.msw.masla.protocol.http.netty.ssl.AbstractHttp2SslChannelInitializer;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * 网关各应用连接数据监控
 * <pre>
 *   前端<----->网关session数量
 * </pre>
 */
@Slf4j
public class SessionMetrics extends AbstractMetrics {

  private static final String HOME = "home";

  @Override
  public List<SessionCount> getMetrics() {

    List<SessionCount> sessionCountList = new ArrayList<SessionCount>(1);
    int sessionsNums = NettyConfig.getInstance().getMaxSession();
    int https1SessionNums = AbstractHttp2SslChannelInitializer.getH1Sessions();
    int https2SessionNums = AbstractHttp2SslChannelInitializer.getH2Sessions();
    AbstractHttp2SslChannelInitializer.cleanSessions();

    sessionCountList
            .add(new SessionCount("masla", sessionsNums, https1SessionNums, https2SessionNums,
                    MaslaSpringContextUtil.getMaslaConfConfigBean().getLocalIp(), getTimestamp()));

    //记录机器级别的session个数，用来做为自动选择分组的一个维度
    GroupServerMertics.getInstances().setGroupServerInSessions(sessionsNums);
    //AbstractServerChannelHandler.clearMaxMonitorCount();

    return sessionCountList;
  }
}
