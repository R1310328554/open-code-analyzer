/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.naming.core.v2.index;

import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.pojo.ServiceInfo;
import com.alibaba.nacos.naming.core.v2.ServiceManager;
import com.alibaba.nacos.naming.core.v2.client.Client;
import com.alibaba.nacos.naming.core.v2.client.manager.ClientManager;
import com.alibaba.nacos.naming.core.v2.client.manager.ClientManagerDelegate;
import com.alibaba.nacos.naming.core.v2.metadata.InstanceMetadata;
import com.alibaba.nacos.naming.core.v2.metadata.NamingMetadataManager;
import com.alibaba.nacos.naming.core.v2.pojo.BatchInstancePublishInfo;
import com.alibaba.nacos.naming.core.v2.pojo.InstancePublishInfo;
import com.alibaba.nacos.naming.core.v2.pojo.Service;
import com.alibaba.nacos.naming.misc.SwitchDomain;
import com.alibaba.nacos.naming.utils.InstanceUtil;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 服务实例数据存储与缓存。
 *
 * <p>基于 {@link ClientServiceIndexesManager} 索引聚合各客户端注册的实例，合并 {@link NamingMetadataManager} 中的实例元数据后构建推送用的 {@link ServiceInfo}。</p>
 *
 * @author xiweng.yy
 */
@Component
public class ServiceStorage {
    
    /** 客户端-服务索引管理器。 */
    private final ClientServiceIndexesManager serviceIndexesManager;
    
    /** 客户端管理器，用于读取实例发布信息。 */
    private final ClientManager clientManager;
    
    /** 命名开关域，提供默认推送缓存毫秒数等配置。 */
    private final SwitchDomain switchDomain;
    
    /** 命名元数据管理器。 */
    private final NamingMetadataManager metadataManager;
    
    /** 服务 -> 已聚合 ServiceInfo 的本地缓存。 */
    private final ConcurrentMap<Service, ServiceInfo> serviceDataIndexes;
    
    /** 服务 -> 所属集群名集合的索引。 */
    private final ConcurrentMap<Service, Set<String>> serviceClusterIndex;
    
    public ServiceStorage(ClientServiceIndexesManager serviceIndexesManager,
        ClientManagerDelegate clientManager,
        SwitchDomain switchDomain, NamingMetadataManager metadataManager) {
        this.serviceIndexesManager = serviceIndexesManager;
        this.clientManager = clientManager;
        this.switchDomain = switchDomain;
        this.metadataManager = metadataManager;
        this.serviceDataIndexes = new ConcurrentHashMap<>();
        this.serviceClusterIndex = new ConcurrentHashMap<>();
    }
    
    /** 返回服务下所有实例涉及的集群名。 */
    public Set<String> getClusters(Service service) {
        return serviceClusterIndex.getOrDefault(service, new HashSet<>());
    }
    
    /** 优先返回缓存数据，未命中时实时聚合。 */
    public ServiceInfo getData(Service service) {
        ServiceInfo data = serviceDataIndexes.get(service);
        return data != null ? data : getPushData(service);
    }
    
    /** 从索引重建推送数据并写入缓存。 */
    public ServiceInfo getPushData(Service service) {
        ServiceInfo result = emptyServiceInfo(service);
        if (!ServiceManager.getInstance().containSingleton(service)) {
            return result;
        }
        Service singleton = ServiceManager.getInstance().getSingleton(service);
        result.setHosts(getAllInstancesFromIndex(singleton));
        serviceDataIndexes.put(singleton, result);
        return result;
    }
    
    /** 移除指定服务的缓存数据与集群索引。 */
    public void removeData(Service service) {
        serviceDataIndexes.remove(service);
        serviceClusterIndex.remove(service);
    }
    
    private ServiceInfo emptyServiceInfo(Service service) {
        ServiceInfo result = new ServiceInfo();
        result.setName(service.getName());
        result.setGroupName(service.getGroup());
        result.setLastRefTime(System.currentTimeMillis());
        result.setCacheMillis(switchDomain.getDefaultPushCacheMillis());
        return result;
    }
    
    private List<Instance> getAllInstancesFromIndex(Service service) {
        Set<Instance> result = new HashSet<>();
        Set<String> clusters = new HashSet<>();
        for (String each : serviceIndexesManager.getAllClientsRegisteredService(service)) {
            Optional<InstancePublishInfo> instancePublishInfo = getInstanceInfo(each, service);
            if (instancePublishInfo.isPresent()) {
                InstancePublishInfo publishInfo = instancePublishInfo.get();
                // BatchInstancePublishInfo 需展开为多条 Instance 后合并
                if (publishInfo instanceof BatchInstancePublishInfo) {
                    BatchInstancePublishInfo batchInstancePublishInfo =
                        (BatchInstancePublishInfo) publishInfo;
                    List<Instance> batchInstance =
                        parseBatchInstance(service, batchInstancePublishInfo, clusters);
                    result.addAll(batchInstance);
                } else {
                    Instance instance = parseInstance(service, instancePublishInfo.get());
                    result.add(instance);
                    clusters.add(instance.getClusterName());
                }
            }
        }
        // 缓存该服务下所有集群名，供 getClusters 使用
        serviceClusterIndex.put(service, clusters);
        return new LinkedList<>(result);
    }
    
    /**
     * 将批量实例发布信息解析为 API Instance 列表。
     *
     * @param service 所属服务
     * @param batchInstancePublishInfo 批量发布信息
     * @param clusters 输出参数，收集集群名
     * @return 解析后的实例列表
     */
    private List<Instance> parseBatchInstance(Service service,
        BatchInstancePublishInfo batchInstancePublishInfo, Set<String> clusters) {
        List<Instance> resultInstanceList = new ArrayList<>();
        List<InstancePublishInfo> instancePublishInfos =
            batchInstancePublishInfo.getInstancePublishInfos();
        for (InstancePublishInfo instancePublishInfo : instancePublishInfos) {
            Instance instance = parseInstance(service, instancePublishInfo);
            resultInstanceList.add(instance);
            clusters.add(instance.getClusterName());
        }
        return resultInstanceList;
    }
    
    private Optional<InstancePublishInfo> getInstanceInfo(String clientId, Service service) {
        Client client = clientManager.getClient(clientId);
        if (null == client) {
            return Optional.empty();
        }
        return Optional.ofNullable(client.getInstancePublishInfo(service));
    }
    
    private Instance parseInstance(Service service, InstancePublishInfo instanceInfo) {
        Instance result = InstanceUtil.parseToApiInstance(service, instanceInfo);
        Optional<InstanceMetadata> metadata = metadataManager
            .getInstanceMetadata(service, instanceInfo.getMetadataId());
        metadata.ifPresent(
            instanceMetadata -> InstanceUtil.updateInstanceMetadata(result, instanceMetadata));
        return result;
    }
}
