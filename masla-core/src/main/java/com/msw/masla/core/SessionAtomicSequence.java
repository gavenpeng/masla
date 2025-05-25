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
package com.msw.masla.core;

import java.net.InetAddress;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SessionAtomicSequence {

	private static final Logger logger = LoggerFactory.getLogger(SessionAtomicSequence.class);
    private static ThreadLocal<String> t    = new ThreadLocal<String>();
    private static AtomicInteger       atom = new AtomicInteger();
    private static String              ip   = null;
    interface Sequence {
        public String getSequence();
    }

    private static Sequence sequence = null;
    static {
        try {
            ip = InetAddress.getLocalHost().getHostAddress();
            if (ip != null && !"127.0.0.1".equals(ip)) {
                ip = Long.toHexString(ipStrToLong(ip));
                sequence = new Sequence() {
                    public String getSequence() {
                        int id = atom.incrementAndGet();
                        id = Math.abs(id);
                        return Integer.toHexString(id) + ip;
                    }
                };
            } else {
                sequence = new Sequence() {
                    public String getSequence() {
                        return java.util.UUID.randomUUID().toString();
                    }
                };
            }
        } catch (Exception e) {
            sequence = new Sequence() {
                public String getSequence() {
                    return java.util.UUID.randomUUID().toString();
                }
            };
        }
    }

    public static String getSequence() {
        return t.get();
    }

    public static void makeSequence() {
        try {
            t.set(sequence.getSequence());
        }catch (Exception e) {
            logger.error(e.toString(), e);
        }

    }

    private static long ipStrToLong(String ipaddress) {
        long[] ip = new long[4];
        int position1 = ipaddress.indexOf(".");
        int position2 = ipaddress.indexOf(".", position1 + 1);
        int position3 = ipaddress.indexOf(".", position2 + 1);
        ip[0] = Long.parseLong(ipaddress.substring(0, position1));
        ip[1] = Long.parseLong(ipaddress.substring(position1 + 1, position2));
        ip[2] = Long.parseLong(ipaddress.substring(position2 + 1, position3));
        ip[3] = Long.parseLong(ipaddress.substring(position3 + 1));
        return (ip[0] << 24) + (ip[1] << 16) + (ip[2] << 8) + ip[3];
    }

    public static void main(String[] args) {
        System.out.println(sequence.getSequence());
        System.out.println(sequence.getSequence());
        System.out.println(sequence.getSequence());
        System.out.println(sequence.getSequence());
        System.out.println(sequence.getSequence());
        System.out.println(Long.toHexString(Math.abs(new Long("-893" + "2342"))));
    }
}
