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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Gavin.peng on 2018/1/15.
 * 熔断器，自动升降级和探测,如果完全熔断，则定时放行一个请求，去探测是否可以关闭熔断
 */
public interface MaslaCircuitBreaker {

    static final Logger LOG = LoggerFactory.getLogger(MaslaCircuitBreaker.class);


    /**
     * Every  requests asks this if it is allowed to proceed or not.  It is idempotent and does
     * not modify any internal state, and takes into account the half-open logic which allows some requests through
     * after the circuit has been opened
     *
     * @return boolean whether a request should be permitted
     */
    boolean allowRequest();

    /**
     * Whether the circuit is currently open (tripped).
     *
     * @return boolean state of circuit breaker
     */
    boolean isOpen();

    /**
     * open the circuit is currently open (tripped).
     *
     * @return boolean state of circuit breaker
     */
    boolean open();

    /**
     * Invoked on successful executions from {} as part of feedback mechanism when in a half-open state.
     */
    boolean markSuccess();

    /**
     * Invoked on unsuccessful executions from {} as part of feedback mechanism when in a half-open state.
     */
    void markNonSuccess();

    /**
     * Invoked at start of command execution to attempt an execution.  This is non-idempotent - it may modify internal
     * state.
     */
    boolean attemptExecution();

    void upgrade();

    void doUpgradOrDown(Throwable cause, int appType, String appName, int httpStatus, int clusterSize);

    boolean supportUpgradOrDown();

    void fastRecovery();

    void down();

    boolean doCircuit();

    void closeCircuit();

    void reset(CircuitRuleDefine circuitRuleDefine);

    String showPercent();

}
