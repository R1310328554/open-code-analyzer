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

package com.alibaba.nacos.naming.core.v2;

import com.alibaba.nacos.common.notify.NotifyCenter;
import com.alibaba.nacos.common.utils.ConcurrentHashSet;
import com.alibaba.nacos.naming.core.v2.event.metadata.MetadataEvent;
import com.alibaba.nacos.naming.core.v2.pojo.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * V2 服务单例管理器。
 *
 * <p>维护全局 {@link Service} 单例仓库及按命名空间索引的服务集合，首次注册时发布 {@link MetadataEvent.ServiceMetadataEvent} 事件。</p>
 *
 * @author xiweng.yy
 */
public class ServiceManager {
    
    /** 全局单例实例。 */
    private static final ServiceManager INSTANCE = new ServiceManager();
    
    /** 服务单例主仓库，键值均为同一 Service 对象。 */
    private final ConcurrentHashMap<Service, Service> singletonRepository;
    
    /** 命名空间到该空间下所有服务单例的索引。 */
    private final ConcurrentHashMap<String, Set<Service>> namespaceSingletonMaps;
    
    /** 私有构造，初始化仓库与命名空间索引。 */
    private ServiceManager() {
        singletonRepository = new ConcurrentHashMap<>(1 << 10);
        namespaceSingletonMaps = new ConcurrentHashMap<>(1 << 2);
    }
    
    /** 获取全局单例管理器。 */
    public static ServiceManager getInstance() {
        return INSTANCE;
    }
    
    /** 返回指定命名空间下的所有服务单例集合。 */
    public Set<Service> getSingletons(String namespace) {
        return namespaceSingletonMaps.getOrDefault(namespace, new HashSet<>(1));
    }
    
    /**
     * 获取或注册服务单例。
     *
     * <p>若仓库中不存在则插入并发布元数据事件，同时更新命名空间索引。</p>
     *
     * @param service 服务模板对象
     * @return 已存在的单例或新注册的单例
     */
    public Service getSingleton(Service service) {
        Service result = singletonRepository.computeIfAbsent(service, key -> {
            NotifyCenter.publishEvent(new MetadataEvent.ServiceMetadataEvent(service, false));
            return service;
        });
        namespaceSingletonMaps
            .computeIfAbsent(result.getNamespace(), namespace -> new ConcurrentHashSet<>())
            .add(result);
        return result;
    }
    
    /**
     * 按命名空间、分组与服务名查询单例（若存在）。
     *
     * @param namespace 命名空间
     * @param group     分组名
     * @param name      服务名
     * @return 存在则返回 Optional 包装的单例，否则 empty
     */
    public Optional<Service> getSingletonIfExist(String namespace, String group, String name) {
        return getSingletonIfExist(Service.newService(namespace, group, name));
    }
    
    /**
     * 按服务模板查询单例（若存在）。
     *
     * @param service 服务模板对象
     * @return 存在则返回 Optional 包装的单例，否则 empty
     */
    public Optional<Service> getSingletonIfExist(Service service) {
        return Optional.ofNullable(singletonRepository.get(service));
    }
    
    /** 返回所有已注册服务的命名空间 ID 集合。 */
    public Set<String> getAllNamespaces() {
        return namespaceSingletonMaps.keySet();
    }
    
    /**
     * 从单例仓库及命名空间索引中移除服务。
     *
     * @param service 待移除的服务
     * @return 被移除的服务对象，不存在则返回 null
     */
    public Service removeSingleton(Service service) {
        Set<Service> services = namespaceSingletonMaps.get(service.getNamespace());
        if (services != null) {
            services.remove(service);
        }
        return singletonRepository.remove(service);
    }
    
    /** 判断服务单例是否已存在于仓库中。 */
    public boolean containSingleton(Service service) {
        return singletonRepository.containsKey(service);
    }
    
    /** 返回当前单例仓库中的服务总数。 */
    public int size() {
        return singletonRepository.size();
    }
}
