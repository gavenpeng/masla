package com.msw.masla.core.invoker.loadbalance;

import com.msw.masla.protocol.http.netty.http.HostInstance;

import java.util.List;

/**
 * Author: Gavin.peng
 * Date: 2024/7/14
 * Description:
 */
public class ConsistentHashLoadBalance extends AbstractLoadBalance {


    public static final String NAME = "consistenthash";

    @Override
    protected HostInstance doSelect(List<HostInstance> hostInstances) {

        return null;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
