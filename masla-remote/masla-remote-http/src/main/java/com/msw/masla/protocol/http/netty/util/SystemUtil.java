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
package com.msw.masla.protocol.http.netty.util;

/**
 * check if can use native epoll
 * <p/>
 * 基于GLIBC2.10编译，而CentOS 5.8就只有GLIBC2.5
 * <p/>
 * check centOS ver >= 6.5
 *
 * @author Gavin.peng
 */
public class SystemUtil {

    public static boolean canUseNative() {

        try {
            if (System.getProperty("os.name").equalsIgnoreCase("linux")) {
                // 2.6.32-431.17.1.el6.x86_64
                String versionstr = System.getProperty("os.version");
                String[] verArray = versionstr.split("-");
                if (verArray.length > 0) {
                    String[] subVerArray = verArray[0].split("\\.");
                    if (subVerArray.length == 3) {
                        if (Integer.parseInt(subVerArray[0]) >= 2) {
                            return true;
                        } else if (Integer.parseInt(subVerArray[1]) >= 6) {
                            return true;
                        } else if (Integer.parseInt(subVerArray[2]) >= 32) {
                            return true;
                        }
                    }
                }
            }
        } catch (Exception e) {
            // pass
        }
        return false;
    }

    public static void main(String[] args) {

    }

}

