package com.msw.masla.core.discovery.nacos;

import com.alibaba.nacos.api.naming.listener.AbstractEventListener;
import com.alibaba.nacos.api.naming.listener.Event;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.client.naming.event.InstancesChangeEvent;

import java.util.List;

public class MaslaNacosEventListener extends AbstractEventListener {

    private MaslaNacosServiceDiscovery maslaNacosServiceDiscovery;

    public MaslaNacosEventListener(MaslaNacosServiceDiscovery maslaNacosServiceDiscovery) {
        this.maslaNacosServiceDiscovery = maslaNacosServiceDiscovery;
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof InstancesChangeEvent) {
            InstancesChangeEvent instancesChangeEvent = (InstancesChangeEvent) event;
            String serviceName = instancesChangeEvent.getServiceName();
            List<Instance> instanceList = instancesChangeEvent.getHosts();
            maslaNacosServiceDiscovery.updateInstanceMap(serviceName, instanceList);
        }

    }
}
