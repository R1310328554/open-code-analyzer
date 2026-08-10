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

import com.alibaba.nacos.api.common.Constants;
import com.alibaba.nacos.common.notify.Event;
import com.alibaba.nacos.common.notify.NotifyCenter;
import com.alibaba.nacos.common.notify.listener.SmartSubscriber;
import com.alibaba.nacos.common.trace.DeregisterInstanceReason;
import com.alibaba.nacos.common.trace.event.naming.DeregisterInstanceTraceEvent;
import com.alibaba.nacos.common.utils.ConcurrentHashSet;
import com.alibaba.nacos.naming.core.v2.client.Client;
import com.alibaba.nacos.naming.core.v2.event.client.ClientOperationEvent;
import com.alibaba.nacos.naming.core.v2.event.publisher.NamingEventPublisherFactory;
import com.alibaba.nacos.naming.core.v2.event.service.ServiceEvent;
import com.alibaba.nacos.naming.core.v2.pojo.InstancePublishInfo;
import com.alibaba.nacos.naming.core.v2.pojo.Service;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 客户端与服务双向索引管理器。
 *
 * <p>维护服务到注册客户端（发布者）与订阅客户端（订阅者）的映射，监听 {@link ClientOperationEvent} 并同步更新索引、发布 {@link ServiceEvent}。</p>
 *
 * @author xiweng.yy
 */
@Component
public class ClientServiceIndexesManager extends SmartSubscriber {
    
    /** 服务 -> 注册该服务的客户端 ID 集合。 */
    private final ConcurrentMap<Service, Set<String>> publisherIndexes = new ConcurrentHashMap<>();
    
    /** 服务 -> 订阅该服务的客户端 ID 集合。 */
    private final ConcurrentMap<Service, Set<String>> subscriberIndexes = new ConcurrentHashMap<>();
    
    public ClientServiceIndexesManager() {
        NotifyCenter.registerSubscriber(this, NamingEventPublisherFactory.getInstance());
    }
    
    /** 获取向指定服务注册实例的所有客户端 ID。 */
    public Collection<String> getAllClientsRegisteredService(Service service) {
        Set<String> publishers = publisherIndexes.get(service);
        return publishers != null ? publishers : new ConcurrentHashSet<>();
    }
    
    /** 获取订阅指定服务的所有客户端 ID。 */
    public Collection<String> getAllClientsSubscribeService(Service service) {
        Set<String> subscribers = subscriberIndexes.get(service);
        return subscribers != null ? subscribers : new ConcurrentHashSet<>();
    }
    
    /** 返回当前存在订阅者的全部服务。 */
    public Collection<Service> getSubscribedService() {
        return subscriberIndexes.keySet();
    }
    
    /**
     * 清除已无实例注册的服务发布索引。
     *
     * @param service 待清理的 Nacos 服务
     */
    public void removePublisherIndexesByEmptyService(Service service) {
        Set<String> publishers = publisherIndexes.get(service);
        if (publishers != null && publishers.isEmpty()) {
            publisherIndexes.remove(service);
        }
    }
    
    /** 订阅客户端注册/注销/订阅/断连等操作事件。 */
    @Override
    public List<Class<? extends Event>> subscribeTypes() {
        List<Class<? extends Event>> result = new LinkedList<>();
        result.add(ClientOperationEvent.ClientRegisterServiceEvent.class);
        result.add(ClientOperationEvent.ClientDeregisterServiceEvent.class);
        result.add(ClientOperationEvent.ClientSubscribeServiceEvent.class);
        result.add(ClientOperationEvent.ClientUnsubscribeServiceEvent.class);
        result.add(ClientOperationEvent.ClientReleaseEvent.class);
        return result;
    }
    
    /** 分发客户端操作或断连事件到对应处理器。 */
    @Override
    public void onEvent(Event event) {
        if (event instanceof ClientOperationEvent.ClientReleaseEvent) {
            handleClientDisconnect((ClientOperationEvent.ClientReleaseEvent) event);
        } else if (event instanceof ClientOperationEvent) {
            handleClientOperation((ClientOperationEvent) event);
        }
    }
    
    private void handleClientDisconnect(ClientOperationEvent.ClientReleaseEvent event) {
        Client client = event.getClient();
        for (Service each : client.getAllSubscribeService()) {
            removeSubscriberIndexes(each, client.getClientId());
        }
        DeregisterInstanceReason reason =
            event.isNative() ? DeregisterInstanceReason.NATIVE_DISCONNECTED
                : DeregisterInstanceReason.SYNCED_DISCONNECTED;
        long currentTimeMillis = System.currentTimeMillis();
        for (Service each : client.getAllPublishedService()) {
            removePublisherIndexes(each, client.getClientId());
            InstancePublishInfo instance = client.getInstancePublishInfo(each);
            NotifyCenter.publishEvent(
                new DeregisterInstanceTraceEvent(currentTimeMillis, "", false, reason,
                    each.getNamespace(),
                    each.getGroup(), each.getName(), instance.getIp(), instance.getPort()));
        }
    }
    
    private void handleClientOperation(ClientOperationEvent event) {
        Service service = event.getService();
        String clientId = event.getClientId();
        if (event instanceof ClientOperationEvent.ClientRegisterServiceEvent) {
            addPublisherIndexes(service, clientId);
        } else if (event instanceof ClientOperationEvent.ClientDeregisterServiceEvent) {
            removePublisherIndexes(service, clientId);
        } else if (event instanceof ClientOperationEvent.ClientSubscribeServiceEvent) {
            addSubscriberIndexes(service, clientId);
        } else if (event instanceof ClientOperationEvent.ClientUnsubscribeServiceEvent) {
            removeSubscriberIndexes(service, clientId);
        }
    }
    
    private void addPublisherIndexes(Service service, String clientId) {
        String serviceChangedType = Constants.ServiceChangedType.INSTANCE_CHANGED;
        if (!publisherIndexes.containsKey(service)) {
            // 服务首次出现注册客户端时，变更类型为 ADD_SERVICE
            serviceChangedType = Constants.ServiceChangedType.ADD_SERVICE;
        }
        NotifyCenter
            .publishEvent(new ServiceEvent.ServiceChangedEvent(service, serviceChangedType, true));
        publisherIndexes.computeIfAbsent(service, key -> new ConcurrentHashSet<>()).add(clientId);
    }
    
    private void removePublisherIndexes(Service service, String clientId) {
        publisherIndexes.computeIfPresent(service, (s, ids) -> {
            ids.remove(clientId);
            String serviceChangedType = ids.isEmpty() ? Constants.ServiceChangedType.DELETE_SERVICE
                : Constants.ServiceChangedType.INSTANCE_CHANGED;
            NotifyCenter.publishEvent(
                new ServiceEvent.ServiceChangedEvent(service, serviceChangedType, true));
            return ids.isEmpty() ? null : ids;
        });
    }
    
    private void addSubscriberIndexes(Service service, String clientId) {
        Set<String> clientIds =
            subscriberIndexes.computeIfAbsent(service, key -> new ConcurrentHashSet<>());
        // 修复 #5404：仅首次添加订阅者时发布 ServiceSubscribedEvent
        if (clientIds.add(clientId)) {
            NotifyCenter.publishEvent(new ServiceEvent.ServiceSubscribedEvent(service, clientId));
        }
    }
    
    private void removeSubscriberIndexes(Service service, String clientId) {
        subscriberIndexes.computeIfPresent(service, (s, clientIds) -> {
            clientIds.remove(clientId);
            return clientIds.isEmpty() ? null : clientIds;
        });
    }
}
