package com.msw.masla.core.discovery.nacos;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.pojo.ListView;
import com.msw.masla.common.constant.Constants;
import com.msw.masla.common.util.StringUtil;
import com.msw.masla.protocol.http.netty.http.HostInstance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Author: Gavin.peng
 * Date: 2024/7/20
 * Description:
 * discovery service in nacos
 */
public class MaslaNacosServiceDiscovery {

    private MaslaNacosDiscoveryProperties maslaNacosDiscoveryProperties;

    private MaslaNacosServiceManager maslaNacosServiceManager;

    private Map<String, List<HostInstance>> stableInstancesMap ;

    private final Map<String, List<HostInstance>> tagInstancesMap;



    public MaslaNacosServiceDiscovery(MaslaNacosDiscoveryProperties discoveryProperties,
                                      MaslaNacosServiceManager nacosServiceManager) {
        this.maslaNacosDiscoveryProperties = discoveryProperties;
        this.maslaNacosServiceManager = nacosServiceManager;
        this.stableInstancesMap = new ConcurrentHashMap<>();
        this.tagInstancesMap = new ConcurrentHashMap<>();

    }

    /**
     * Return all instances for the given service.
     * @param serviceId id of service
     * @return list of instances
     * @throws NacosException nacosException
     */
    public List<HostInstance> getAvailableInstances(String serviceId, boolean tag) throws NacosException {
        if (!stableInstancesMap.containsKey(serviceId)) {

            initAllInstances(serviceId);
        }
        return !tag ? stableInstancesMap.get(serviceId) : tagInstancesMap.get(serviceId);

    }

    public int getServiceInstanceSize(String serviceApp) {
        return stableInstancesMap.get(serviceApp).size();
    }

    private void initAllInstances(String serviceId) throws NacosException {
        String group = maslaNacosDiscoveryProperties.getGroup();
        List<Instance> instances = namingService().selectInstances(serviceId, group,
                true);
        synchronized (this) {
            initInstanceList(instances, serviceId);
        }
        namingService().subscribe(serviceId, new MaslaNacosEventListener(this));
    }

    /**
     * Return the names of all services.
     * @return list of service names
     * @throws NacosException nacosException
     */
    public List<String> getServices() throws NacosException {
        String group = maslaNacosDiscoveryProperties.getGroup();
        ListView<String> services = namingService().getServicesOfServer(1,
                Integer.MAX_VALUE, group);
        return services.getData();
    }

    public void initInstanceList(
            List<Instance> instances, String serviceId) {

        if (stableInstancesMap.containsKey(serviceId)) {
            return;
        }
        synchronized (this) {
            if (stableInstancesMap.containsKey(serviceId)) {
                return;
            }
            List<HostInstance> result = new ArrayList<>(instances.size());
            List<HostInstance> tagHosts = new ArrayList<>(instances.size());
            for (Instance instance : instances) {
                HostInstance hostInstance = instanceToHostProfile(instance, serviceId);
                String tag = hostInstance.getMetadata().get(Constants.MASLA_ROUTE_TAG);
                if (StringUtil.isEmptyString(tag)) {
                    result.add(hostInstance);
                } else {
                    tagHosts.add(hostInstance);
                }
            }
            stableInstancesMap.put(serviceId, result);
            if (!tagHosts.isEmpty()) {
                tagInstancesMap.put(serviceId, tagHosts);
            }
        }

    }

    public void updateInstanceMap(String serviceId, List<Instance> instances){
        if (instances == null || instances.isEmpty()) {
            return;
        }

        List<HostInstance> results = new ArrayList<>(instances.size());
        List<HostInstance> tagHosts = new ArrayList<>(instances.size());
        for (Instance instance : instances) {
            HostInstance hostInstance = instanceToHostProfile(instance, serviceId);
            String tag = hostInstance.getMetadata().get(Constants.MASLA_ROUTE_TAG);
            if (StringUtil.isEmptyString(tag)) {
                results.add(hostInstance);
            } else {
                tagHosts.add(hostInstance);
            }
        }
        if (!results.isEmpty()) {
            stableInstancesMap.put(serviceId, results);
        }
        if (!tagHosts.isEmpty()) {
            tagInstancesMap.put(serviceId, tagHosts);
        }


    }

    public  HostInstance instanceToHostProfile(Instance instance,
                                                        String serviceId) {
        if (instance == null || !instance.isEnabled() || !instance.isHealthy()) {
            return null;
        }
        HostProfile hostProfile = new HostProfile(instance.getIp(), instance.getPort());
        hostProfile.setServiceId(serviceId);

        Map<String, String> metadata = new HashMap<>();
        metadata.put("nacos.instanceId", instance.getInstanceId());
        metadata.put("nacos.weight", instance.getWeight() + "");
        metadata.put("nacos.healthy", instance.isHealthy() + "");
        metadata.put("nacos.cluster", instance.getClusterName() + "");
        if (instance.getMetadata() != null) {
            metadata.putAll(instance.getMetadata());
        }
        metadata.put("nacos.ephemeral", String.valueOf(instance.isEphemeral()));
        hostProfile.setMetadata(metadata);


        return hostProfile;
    }

    private NamingService namingService() {
        return maslaNacosServiceManager.getNamingService();
    }

    public Map<String, List<HostInstance>> getAllInstances() {
        Map<String, List<HostInstance>> allInstance = new HashMap<>();
        allInstance.putAll(stableInstancesMap);
        allInstance.putAll(tagInstancesMap);
        return allInstance;
    }

}
