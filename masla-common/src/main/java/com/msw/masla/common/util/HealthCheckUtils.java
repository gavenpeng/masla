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
package com.msw.masla.common.util;

import com.msw.masla.common.constant.Constants;

import java.net.HttpURLConnection;
import java.net.URL;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Slf4j
public class HealthCheckUtils {

    private static final Logger logger = LoggerFactory.getLogger(HealthCheckUtils.class);


    public static int queryHealthCheckCode(String checkUrl) throws Exception {
        logger.info("start do healthcheck url {}",checkUrl);
        URL url = new URL(checkUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("Get");
        conn.setConnectTimeout(1000);
        conn.setReadTimeout(1000);
        conn.setRequestProperty(Constants.CLIENT_REAL_IP, MaslaSpringContextUtil.getMaslaConfConfigBean().getLocalIp());
        conn.setRequestProperty("Content-Type", "text/html; charset=UTF-8");
        conn.connect();
        int code = conn.getResponseCode();
        conn.disconnect();
        return code;
    }

    public static boolean isHealth(String checkPath, int retry) {
        try {
            for (int i = 0; i < retry; i++) {
                int code = queryHealthCheckCode(checkPath);
                if (code == 200) {
                    return true;
                }
            }
        } catch (Throwable e) {
            log.error("do health check failed,url {}",checkPath,e);
            return false;
        }
        return false;
    }


}
