package com.msw.masla.common.stream;

import com.msw.masla.common.circuit.CircuitRuleDefine;
import com.msw.masla.common.pojo.ServiceApi;
import com.msw.masla.common.pojo.ServiceApp;

/**
 * Created by Gavin.peng on 2017/11/27.
 */
public interface CircuitPolicy {

    void reset(ServiceApi apiDO);

    void configApiCircuit(CircuitRuleDefine apiCircuitDO);

    void reset(ServiceApp apiDO);

    void upgrade(ServiceApi apiDO);

    void down(ServiceApi apiDO);

}
