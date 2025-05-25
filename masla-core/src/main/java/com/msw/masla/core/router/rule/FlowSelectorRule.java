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
package com.msw.masla.core.router.rule;


import com.msw.masla.common.enums.Status;
import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Date;
import java.util.Map;

/**
 * 流量选择器
 */
@Data
@ToString
@Slf4j
public class FlowSelectorRule {


  /**
   * request path
   */
  private String path;

  /**
   * 请求IP地址，多个以逗号间隔
   */
  private String ip;


  private String queryStringKey;

  private String queryStringValue;

  private String headerKey;

  private String headerKeyValue;

  /**
   * 限流每分钟访问的次数
   */
  private int maxFreq;

  /**
   * 限流的时间间隔，单位位秒
   */
  private int interval;

  /**
   * 应用名称
   */
  private String appName;

  /**
   * 状态 {@link Status}
   */
  private Integer status;

  /**
   * 额外条件转化成Map
   * key -> { operator, value}
   *
   * operator: 额外参数的操作符，暂时支持 equals 和 exists
   * 1是 equals
   * 2是 exists
   */
  private Map<String, Pair<Integer, String>> paramMap;

  private Map<String, Pair<Integer, String>> respParamMap;

  /**
   * 参数类型条件转化成Map
   */
  private Map<String, String> paramTypeMap;

  private Map<String, String> osVersionMap;

  private Map<String, String> appVersionMap;

  private Date createTime;

  private Date updateTime;

  private Integer type;//默认是1，预发布的是2

  /**
   * 暂时只用在 type == FLOW_RULE 的情况
   * defRateLimit>0 才生效
   */
  private Integer defRateLimit = -1;

  /**
   * http请求方式
   */
  private String httpMethod;

  /**
   * 起止时间
   */
  private Date startTime;

  private Date endTime;

}
