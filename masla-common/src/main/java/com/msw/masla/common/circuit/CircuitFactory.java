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
package com.msw.masla.common.circuit;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.msw.masla.common.constant.Constants;
import com.msw.masla.common.pojo.ServiceApp;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

/**
 * Created by Gavin.peng on 2018/1/15.
 */

@Slf4j
public class CircuitFactory {

    private static ExecutorService executorService = new ThreadPoolExecutor(1,1,0, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(100),
            new ThreadFactoryBuilder()
                    .setNameFormat("CircuitDOCache-%d")
                    .setDaemon(true)
                    .setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                        @Override
                        public void uncaughtException(Thread t, Throwable e) {
                            log.error("something is error in circuitDOCache ExecutorService's thread:{}, ", t, e);
                        }
                    })
                    .build(),
            new ThreadPoolExecutor.AbortPolicy()
    );

    private static ConcurrentHashMap<String, MaslaCircuitBreaker> circuitBreakersByCommand = new ConcurrentHashMap<String, MaslaCircuitBreaker>();

    /**
     * Get the {@link MaslaCircuitBreaker} instance for a given {@link }.
     * <p>
     * This is thread-safe and ensures only 1 {@link MaslaCircuitBreaker} per {@link }.
     *
     * @param key
     *            {@link } of {@link } instance requesting the {@link MaslaCircuitBreaker}
     * @return {@link MaslaCircuitBreaker} for {@link }
     */
    public static MaslaCircuitBreaker getInstance(String key, CircuitRuleDefine apiCircuitDO) {
        // this should find it for all but the first time
        MaslaCircuitBreaker previouslyCached = circuitBreakersByCommand.get(key);
        if (previouslyCached != null) {
            return previouslyCached;
        }

        // if we get here this is the first time so we need to initialize

        // Create and add to the map ... use putIfAbsent to atomically handle the possible race-condition of
        // 2 threads hitting this point at the same time and let ConcurrentHashMap provide us our thread-safety
        // If 2 threads hit here only one will get added and the other will get a non-null response instead.
        MaslaCircuitBreaker cbForCommand = circuitBreakersByCommand.putIfAbsent(key, new MaslaCircuitBreakerImpl(key,apiCircuitDO));
        if (cbForCommand == null) {
            // this means the putIfAbsent step just created a new one so let's retrieve and return it
            return circuitBreakersByCommand.get(key);
        } else {
            // this means a race occurred and while attempting to 'put' another one got there before
            // and we instead retrieved it and will now return it
            return cbForCommand;
        }
    }

    /**
     * 不会创建CircuitBreaker instance
     * @param key
     * @return
     */
    public static MaslaCircuitBreaker getCircuitBreaker(String key) {
        return circuitBreakersByCommand.get(key);
    }

    public static MaslaCircuitBreaker getCircuitBreaker(ServiceApp appDO){
        CircuitRuleDefine apiCircuitDO = appDO.getDefaultCircuit();
        return apiCircuitDO.getCircuitBreaker();
    }

    public static MaslaCircuitBreaker getAppDefaultCircuitBreaker(ServiceApp appDO) {
        String key = appDO.getName();
        return circuitBreakersByCommand.get(key);
    }

    public static void clearCircuitBreaker(String key){
        circuitBreakersByCommand.remove(key);
    }

    /**
     * Clears all circuit breakers. If new requests come in instances will be recreated.
     */
    public static void reset() {
        circuitBreakersByCommand.clear();
    }

    public static void processApiCircuit(final CircuitRuleDefine apiCircuitDO, final boolean doDisalbed) {

    }

}
