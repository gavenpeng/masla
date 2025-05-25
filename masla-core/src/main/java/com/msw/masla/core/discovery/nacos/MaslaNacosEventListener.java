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
package com.msw.masla.core.discovery.nacos;

import com.alibaba.nacos.api.naming.listener.AbstractEventListener;
import com.alibaba.nacos.api.naming.listener.Event;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.client.naming.event.InstancesChangeEvent;

import java.util.List;

public class MaslaNacosEventListener extends AbstractEventListener {

    private final MaslaNacosServiceDiscovery maslaNacosServiceDiscovery;

    public MaslaNacosEventListener(MaslaNacosServiceDiscovery maslaNacosServiceDiscovery) {
        this.maslaNacosServiceDiscovery = maslaNacosServiceDiscovery;
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof NamingEvent) {
            NamingEvent namingEvent = (NamingEvent) event;
            String serviceName = namingEvent.getServiceName();
            List<Instance> instanceList = namingEvent.getInstances();
            maslaNacosServiceDiscovery.updateInstanceMap(serviceName, instanceList);
        }

    }
}
