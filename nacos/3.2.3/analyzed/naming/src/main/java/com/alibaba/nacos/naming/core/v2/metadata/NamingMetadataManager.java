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

package com.alibaba.nacos.naming.core.v2.metadata;

import com.alibaba.nacos.common.notify.Event;
import com.alibaba.nacos.common.notify.NotifyCenter;
import com.alibaba.nacos.common.notify.listener.SmartSubscriber;
import com.alibaba.nacos.common.utils.ConcurrentHashSet;
import com.alibaba.nacos.naming.core.v2.ServiceManager;
import com.alibaba.nacos.naming.core.v2.event.client.ClientEvent;
import com.alibaba.nacos.naming.core.v2.event.metadata.MetadataEvent;
import com.alibaba.nacos.naming.core.v2.event.publisher.NamingEventPublisherFactory;
import com.alibaba.nacos.naming.core.v2.pojo.Service;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 命名元数据内存管理器。
 *
 * <p>维护服务与实例元数据映射、过期元数据集合，监听元数据事件与客户端断连并协调快照加载。</p>
 *
 * @author xiweng.yy
 */
@Component
public class NamingMetadataManager extends SmartSubscriber {
    
    /** 待异步清理的过期元数据集合。 */
    private final Set<ExpiredMetadataInfo> expiredMetadataInfos;
    
    /** 服务 -> 服务元数据。 */
    private ConcurrentMap<Service, ServiceMetadata> serviceMetadataMap;
    
    /** 服务 -> (实例 metadataId -> 实例元数据)。 */
    private ConcurrentMap<Service, ConcurrentMap<String, InstanceMetadata>> instanceMetadataMap;
    
    /** 单服务实例元数据子 Map 的初始容量。 */
    private static final int INITIAL_CAPACITY = 1;
    
    public NamingMetadataManager() {
        serviceMetadataMap = new ConcurrentHashMap<>(1 << 10);
        instanceMetadataMap = new ConcurrentHashMap<>(1 << 10);
        expiredMetadataInfos = new ConcurrentHashSet<>();
        NotifyCenter.registerSubscriber(this, NamingEventPublisherFactory.getInstance());
    }
    
    /**
     * 判断是否已存在指定服务的元数据。
     *
     * @param service 服务
     * @return 存在返回 true
     */
    public boolean containServiceMetadata(Service service) {
        return serviceMetadataMap.containsKey(service);
    }
    
    /**
     * 判断是否已存在指定实例的元数据。
     *
     * @param service    服务
     * @param metadataId 实例元数据 ID
     * @return 存在返回 true
     */
    public boolean containInstanceMetadata(Service service, String metadataId) {
        ConcurrentMap<String, InstanceMetadata> metadataMap = instanceMetadataMap.get(service);
        return metadataMap != null && metadataMap.containsKey(metadataId);
    }
    
    /**
     * 获取服务元数据（只读查询，不可修改返回对象）。
     *
     * @param service 服务
     * @return 服务元数据 Optional
     */
    public Optional<ServiceMetadata> getServiceMetadata(Service service) {
        return Optional.ofNullable(serviceMetadataMap.get(service));
    }
    
    /**
     * 获取实例元数据（只读查询，不可修改返回对象）。
     *
     * @param service    服务
     * @param metadataId 实例元数据 ID
     * @return 实例元数据 Optional
     */
    public Optional<InstanceMetadata> getInstanceMetadata(Service service, String metadataId) {
        ConcurrentMap<String, InstanceMetadata> instanceMetadataMapForService =
            instanceMetadataMap.get(service);
        if (null == instanceMetadataMapForService) {
            return Optional.empty();
        }
        return Optional.ofNullable(instanceMetadataMapForService.get(metadataId));
    }
    
    /**
     * 更新服务元数据并递增服务 revision。
     *
     * @param service         服务
     * @param serviceMetadata 新的服务元数据
     */
    public void updateServiceMetadata(Service service, ServiceMetadata serviceMetadata) {
        service.incrementRevision();
        serviceMetadataMap.put(service, serviceMetadata);
    }
    
    /**
     * 更新指定实例的元数据。
     *
     * @param service          服务
     * @param metadataId       实例元数据 ID
     * @param instanceMetadata 新的实例元数据
     */
    public void updateInstanceMetadata(Service service, String metadataId,
        InstanceMetadata instanceMetadata) {
        instanceMetadataMap.computeIfAbsent(service, k -> new ConcurrentHashMap<>(INITIAL_CAPACITY))
            .put(metadataId, instanceMetadata);
    }
    
    /**
     * 移除服务元数据及对应过期标记。
     *
     * @param service 服务
     */
    public void removeServiceMetadata(Service service) {
        serviceMetadataMap.remove(service);
        expiredMetadataInfos.remove(ExpiredMetadataInfo.newExpiredServiceMetadata(service));
    }
    
    /**
     * 移除实例元数据及对应过期标记。
     *
     * @param service    服务
     * @param metadataId 实例元数据 ID
     */
    public void removeInstanceMetadata(Service service, String metadataId) {
        ConcurrentMap<String, InstanceMetadata> instanceMetadataMapForService =
            instanceMetadataMap.get(service);
        if (null != instanceMetadataMapForService) {
            instanceMetadataMapForService.remove(metadataId);
            if (instanceMetadataMapForService.isEmpty()) {
                instanceMetadataMap.remove(service);
            }
        }
        expiredMetadataInfos
            .remove(ExpiredMetadataInfo.newExpiredInstanceMetadata(service, metadataId));
    }
    
    /**
     * 导出服务元数据快照副本。
     *
     * @return 服务元数据映射
     */
    public Map<Service, ServiceMetadata> getServiceMetadataSnapshot() {
        ConcurrentMap<Service, ServiceMetadata> result =
            new ConcurrentHashMap<>(serviceMetadataMap.size());
        result.putAll(serviceMetadataMap);
        return result;
    }
    
    /**
     * 导出实例元数据快照副本。
     *
     * @return 实例元数据嵌套映射
     */
    public Map<Service, ConcurrentMap<String, InstanceMetadata>> getInstanceMetadataSnapshot() {
        ConcurrentMap<Service, ConcurrentMap<String, InstanceMetadata>> result =
            new ConcurrentHashMap<>(
                instanceMetadataMap.size());
        result.putAll(instanceMetadataMap);
        return result;
    }
    
    /**
     * 从快照加载服务元数据，并确保服务单例已注册。
     *
     * @param snapshot 快照数据
     */
    public void loadServiceMetadataSnapshot(ConcurrentMap<Service, ServiceMetadata> snapshot) {
        for (Service each : snapshot.keySet()) {
            Service service = Service.newService(each.getNamespace(), each.getGroup(),
                each.getName(), each.isEphemeral());
            ServiceManager.getInstance().getSingleton(service);
        }
        ConcurrentMap<Service, ServiceMetadata> oldSnapshot = serviceMetadataMap;
        serviceMetadataMap = snapshot;
        oldSnapshot.clear();
    }
    
    /**
     * 从快照加载实例元数据并替换内存映射。
     *
     * @param snapshot 快照数据
     */
    public void loadInstanceMetadataSnapshot(
        ConcurrentMap<Service, ConcurrentMap<String, InstanceMetadata>> snapshot) {
        ConcurrentMap<Service, ConcurrentMap<String, InstanceMetadata>> oldSnapshot =
            instanceMetadataMap;
        instanceMetadataMap = snapshot;
        oldSnapshot.clear();
    }
    
    /** 返回待清理过期元数据集合。 */
    public Set<ExpiredMetadataInfo> getExpiredMetadataInfos() {
        return expiredMetadataInfos;
    }
    
    /** 订阅元数据变更与客户端断连事件。 */
    @Override
    public List<Class<? extends Event>> subscribeTypes() {
        List<Class<? extends Event>> result = new LinkedList<>();
        result.add(MetadataEvent.InstanceMetadataEvent.class);
        result.add(MetadataEvent.ServiceMetadataEvent.class);
        result.add(ClientEvent.ClientDisconnectEvent.class);
        return result;
    }
    
    /** 处理元数据过期标记与客户端断连清理。 */
    @Override
    public void onEvent(Event event) {
        if (event instanceof MetadataEvent.InstanceMetadataEvent) {
            handleInstanceMetadataEvent((MetadataEvent.InstanceMetadataEvent) event);
        } else if (event instanceof MetadataEvent.ServiceMetadataEvent) {
            handleServiceMetadataEvent((MetadataEvent.ServiceMetadataEvent) event);
        } else {
            handleClientDisconnectEvent((ClientEvent.ClientDisconnectEvent) event);
        }
    }
    
    private void handleClientDisconnectEvent(ClientEvent.ClientDisconnectEvent event) {
        for (Service each : event.getClient().getAllPublishedService()) {
            String metadataId = event.getClient().getInstancePublishInfo(each).getMetadataId();
            if (containInstanceMetadata(each, metadataId)) {
                updateExpiredInfo(true,
                    ExpiredMetadataInfo.newExpiredInstanceMetadata(each, metadataId));
            }
        }
    }
    
    private void handleServiceMetadataEvent(MetadataEvent.ServiceMetadataEvent event) {
        Service service = event.getService();
        if (containServiceMetadata(service)) {
            updateExpiredInfo(event.isExpired(),
                ExpiredMetadataInfo.newExpiredServiceMetadata(service));
        }
    }
    
    private void handleInstanceMetadataEvent(MetadataEvent.InstanceMetadataEvent event) {
        Service service = event.getService();
        String metadataId = event.getMetadataId();
        if (containInstanceMetadata(service, metadataId)) {
            updateExpiredInfo(event.isExpired(),
                ExpiredMetadataInfo.newExpiredInstanceMetadata(event.getService(),
                    event.getMetadataId()));
        }
    }
    
    private void updateExpiredInfo(boolean expired, ExpiredMetadataInfo expiredMetadataInfo) {
        if (expired) {
            expiredMetadataInfos.add(expiredMetadataInfo);
        } else {
            expiredMetadataInfos.remove(expiredMetadataInfo);
        }
    }
}
