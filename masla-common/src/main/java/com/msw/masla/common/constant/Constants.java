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
package com.msw.masla.common.constant;


public class Constants {

	public static final String MASLA_HEALTHCHECK_PATH_END = "/healthcheck";

	public static final String MASLA_API_PATH = "/masla";

	public static final String MASLA_ACTUATOR_ROOT = "/actuator";

	public static final String MASLA_COMMON_MATCH_PATH = "/.*";

	public static final String MASLA_MONITOR_PATH = "/monitor";

	public static final String MASLA_METADATA_FLUSH_THREAD = "masla-metadata-flush";

	public static final String MASLA_META_CONTEXT = MASLA_MONITOR_PATH;

	public static final Long MINUTE_TEN = 1000 * 60 * 10L;

	public static final Long MAX_HISTOGRAM = 3000L;

	public static final String HTTP_PROTOCOL_1 = "http://";

	public static final String HTTP1_PROTOCOL = "HTTP/1.1";

	public static final String HTTP2_PROTOCOL = "HTTP/2";

	public static final String CLIENT_REAL_IP = "REAL_CLIENT_IP";

	public static final String ICO_REQUEST = ".ico";

	public static final String PATTEN_START_CHAR = "^";

	public static final String UNVALID_SERVICE_PATH= "unvalid_serviceid";

	public static final String JSON_CONTENT_TYPE_VALUE = "application/json; charset=UTF-8";

	public static final int HTTP_CODE_APP_DEFINE = 600;

	public final static int CACHE_SIZE = 1024;

	public final static int CONTEXT_CACHE_SIZE_LIMIT = 2048;

	public final static int CACHE_SIZE_LIMIT = 1024;

	public static final String ACQUIRE_CONN_QUEUE_FULL_EXCEPTION = "Too many outstanding";

	public static final String CONN_REFUSED = "Connection refused";

	public static final String CONN_REFUSED_CHINESE = "拒绝连接";

	public static final String CONN_RESET = "Connection reset by peer";

	public static final String WAIT_TIMEOUT_EXCEPTION = "Acquire operation took";

	public static final String CONNECTION_TIMED_OUT = "connection timed out";

	public static final String CONNECT_TIMED_OUT = "connect timed out";

	public static final String TIMED_OUT_EXCEPTION = "timed out";

	public static final String NO_ROUTE_TO_HOST = "No route to host";

	public static final String NO_ROUTE_TO_HOST_CHINESE = "没有到主机的路由";

	public static final String MASLA_RESPONSE_HEADER_KEY = "MState";

	public static final String MASLA_RESPONSE_HEADER_KEY_VALUE = MASLA_RESPONSE_HEADER_KEY;

	public static final String MASLA_RESPONSE_HEADER_QUEUE_FULL = "QFull";

	public static final String MASLA_RESPONSE_HEADER_GLOBAL_BLACK = "GBlack";

	public static final String MASLA_RESPONSE_HEADER_APPLICATION_BLACK = "ABlack";

	public static final String MASLA_RESPONSE_HEADER_UNVALID_PATH = "UNPath";

	public static final String MASLA_RESPONSE_HEADER_PROTOCOL_EXCEPTION = "ProtocolException";

	public static final String MASLA_RESPONSE_HEADER_BODY_TOO_LARGE = "BodyTooLarge";

	public static final String MASLA_RESPONSE_HEADER_APPLICATION_SESSION_TOO_MANY = "ASessionTooMany";

	public static final String MASLA_RESPONSE_HEADER_CIRCUIT = "Circuited";

	public static final String MASLA_RESPONSE_HEADER_FLOW_CONTROLLER = "FlowControlled";

	public static final String MASLA_RESPONSE_HEADER_ROUTER_FAILED = "NOHost";

	public static final String MASLA_RESPONSE_HEADER_FORWARD_FAILED = "ForwardFailed";

	public static final String MASLA_RESPONSE_HEADER_WATI_QUEUE_FULL = "WPoolFull";

	public static final String MASLA_RESPONSE_HEADER_WATI_CONN_TIMEOUT = "WConnTimeout";

	public static final String MASLA_RESPONSE_HEADER_CONN_TIMEOUT = "ConnTimeout";

	public static final String MASLA_RESPONSE_HEADER_CONN_REFUSED = "ConnRefused";

	public static final String MASLA_RESPONSE_HEADER_SERVER_TIMEOUT= "SerTimeout";

	public static final String MASLA_RESPONSE_HEADER_SERVER_CONN_CLOSED = "SerConnClosed";

	public static final String MASLA_RESPONSE_HEADER_SERVER_CONN_RESET = "SerConnReset";

	public static final String NETTY_HEADER_ERROR="HTTP header";

	public static final String HTTPS_PROTOCOL_HEADER_KEY="X-Server-Protocol";

	public static final String HTTPS2_PROTOCOL_HEADER_KEY_VALUE="HTTP/2.0";

	public static final String HTTPS1_PROTOCOL_HEADER_KEY_VALUE="HTTPS/1.0";

	public static final String X_FORWARDED_FOR = "X-Forwarded-For";

	public static final String MASLA_ROOT_CONTEXT = "";

	public static final String HTTP_SCHEMA = "/";

	public static final String MASLA_NIO_THREAD_MODE = "nio";

	public static final String MASLA_ROUTE_TAG = "masla.tag";

	public static final String MASLA_ROUTE_TAG_HEADER = "masla_tag";

	public static final String MASLA_REQUEST_TAG_STRESS_HEADER = "stress_tag";

}
