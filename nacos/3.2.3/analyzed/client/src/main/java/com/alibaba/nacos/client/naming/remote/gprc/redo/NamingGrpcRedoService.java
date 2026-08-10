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

package com.alibaba.nacos.client.naming.remote.gprc.redo;

import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.common.Constants;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.pojo.ServiceInfo;
import com.alibaba.nacos.api.naming.utils.NamingUtils;
import com.alibaba.nacos.client.env.NacosClientProperties;
import com.alibaba.nacos.client.naming.cache.NamingFuzzyWatchServiceListHolder;
import com.alibaba.nacos.client.naming.remote.gprc.NamingGrpcClientProxy;
import com.alibaba.nacos.client.naming.remote.gprc.redo.data.BatchInstanceRedoData;
import com.alibaba.nacos.client.naming.remote.gprc.redo.data.InstanceRedoData;
import com.alibaba.nacos.client.naming.remote.gprc.redo.data.SubscriberRedoData;
import com.alibaba.nacos.client.utils.LogUtils;
import com.alibaba.nacos.common.executor.NameThreadFactory;
import com.alibaba.nacos.common.remote.client.Connection;
import com.alibaba.nacos.common.remote.client.ConnectionEventListener;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 命名 gRPC 客户端 redo 服务。
 *
 * <p>监听连接事件，断线时将注册/订阅标记为待重做；重连后由 {@link RedoScheduledTask} 定时补偿注册与订阅操作。</p>
 * TODO refactor to extends from {@link com.alibaba.nacos.client.redo.service.AbstractRedoService}
 *
 * @author xiweng.yy
 */
public class NamingGrpcRedoService implements ConnectionEventListener {
    
    /** redo 定时任务线程名前缀。 */
    private static final String REDO_THREAD_NAME = "com.alibaba.nacos.client.naming.grpc.redo";
    
    /** redo 线程池大小。 */
    private int redoThreadCount;
    
    /** redo 任务执行间隔毫秒数。 */
    private long redoDelayTime;
    
    /** 已注册实例 redo 缓存，键为 group@@service。 */
    private final ConcurrentMap<String, InstanceRedoData> registeredInstances =
        new ConcurrentHashMap<>();
    
    /** 订阅 redo 缓存，键为 serviceKey。 */
    private final ConcurrentMap<String, SubscriberRedoData> subscribes = new ConcurrentHashMap<>();
    
    /** 模糊监听持有者，断线时重置一致性状态。 */
    private final NamingFuzzyWatchServiceListHolder namingFuzzyWatchServiceListHolder;
    
    /** 定时执行 redo 任务的线程池。 */
    private final ScheduledExecutorService redoExecutor;
    
    /** gRPC 连接是否处于已连接状态。 */
    private volatile boolean connected = false;
    
    public NamingGrpcRedoService(NamingGrpcClientProxy clientProxy,
        NamingFuzzyWatchServiceListHolder namingFuzzyWatchServiceListHolder,
        NacosClientProperties properties) {
        setProperties(properties);
        this.namingFuzzyWatchServiceListHolder = namingFuzzyWatchServiceListHolder;
        this.redoExecutor = new ScheduledThreadPoolExecutor(redoThreadCount,
            new NameThreadFactory(REDO_THREAD_NAME));
        this.redoExecutor.scheduleWithFixedDelay(new RedoScheduledTask(clientProxy, this),
            redoDelayTime, redoDelayTime,
            TimeUnit.MILLISECONDS);
    }
    
    /** 从客户端属性读取 redo 延迟与线程数配置。 */
    private void setProperties(NacosClientProperties properties) {
        redoDelayTime = properties.getLong(PropertyKeyConst.REDO_DELAY_TIME,
            Constants.DEFAULT_REDO_DELAY_TIME);
        redoThreadCount = properties.getInteger(PropertyKeyConst.REDO_DELAY_THREAD_COUNT,
            Constants.DEFAULT_REDO_THREAD_COUNT);
    }
    
    public ConcurrentMap<String, InstanceRedoData> getRegisteredInstances() {
        return registeredInstances;
    }
    
    public boolean isConnected() {
        return connected;
    }
    
    @Override
    public void onConnected(Connection connection) {
        connected = true;
        LogUtils.NAMING_LOGGER.info("Grpc connection connect");
    }
    
    @Override
    public void onDisConnect(Connection connection) {
        connected = false;
        LogUtils.NAMING_LOGGER.warn("Grpc connection disconnect, mark to redo");
        synchronized (registeredInstances) {
            registeredInstances.values()
                .forEach(instanceRedoData -> instanceRedoData.setRegistered(false));
        }
        synchronized (subscribes) {
            subscribes.values()
                .forEach(subscriberRedoData -> subscriberRedoData.setRegistered(false));
        }
        synchronized (namingFuzzyWatchServiceListHolder) {
            namingFuzzyWatchServiceListHolder.resetConsistenceStatus();
        }
        LogUtils.NAMING_LOGGER.warn("mark to redo completed");
    }
    
    /**
     * 缓存单实例注册 redo 数据。
     *
     * @param serviceName service name
     * @param groupName   group name
     * @param instance    registered instance
     */
    public void cacheInstanceForRedo(String serviceName, String groupName, Instance instance) {
        String key = NamingUtils.getGroupedName(serviceName, groupName);
        InstanceRedoData redoData = InstanceRedoData.build(serviceName, groupName, instance);
        synchronized (registeredInstances) {
            registeredInstances.put(key, redoData);
        }
    }
    
    /**
     * 缓存批量实例注册 redo 数据。
     *
     * @param serviceName service name
     * @param groupName   group name
     * @param instances   batch registered instance
     */
    public void cacheInstanceForRedo(String serviceName, String groupName,
        List<Instance> instances) {
        String key = NamingUtils.getGroupedName(serviceName, groupName);
        BatchInstanceRedoData redoData =
            BatchInstanceRedoData.build(serviceName, groupName, instances);
        synchronized (registeredInstances) {
            registeredInstances.put(key, redoData);
        }
    }
    
    /**
     * 实例注册成功后标记 redo 为已注册。
     *
     * @param serviceName service name
     * @param groupName   group name
     */
    public void instanceRegistered(String serviceName, String groupName) {
        String key = NamingUtils.getGroupedName(serviceName, groupName);
        synchronized (registeredInstances) {
            InstanceRedoData redoData = registeredInstances.get(key);
            if (null != redoData) {
                redoData.registered();
            }
        }
    }
    
    /**
     * 实例开始注销，标记 redo 为注销中。
     *
     * @param serviceName service name
     * @param groupName   group name
     */
    public void instanceDeregister(String serviceName, String groupName) {
        String key = NamingUtils.getGroupedName(serviceName, groupName);
        synchronized (registeredInstances) {
            InstanceRedoData redoData = registeredInstances.get(key);
            if (null != redoData) {
                redoData.setUnregistering(true);
                redoData.setExpectedRegistered(false);
            }
        }
    }
    
    /**
     * 实例注销完成，更新 redo 为已注销。
     *
     * @param serviceName service name
     * @param groupName   group name
     */
    public void instanceDeregistered(String serviceName, String groupName) {
        String key = NamingUtils.getGroupedName(serviceName, groupName);
        synchronized (registeredInstances) {
            InstanceRedoData redoData = registeredInstances.get(key);
            if (null != redoData) {
                redoData.unregistered();
            }
        }
    }
    
    /**
     * 从 redo 缓存移除实例（预期不再注册时）。
     *
     * @param serviceName service name
     * @param groupName   group name
     */
    public void removeInstanceForRedo(String serviceName, String groupName) {
        String key = NamingUtils.getGroupedName(serviceName, groupName);
        synchronized (registeredInstances) {
            InstanceRedoData redoData = registeredInstances.get(key);
            if (null != redoData && !redoData.isExpectedRegistered()) {
                registeredInstances.remove(key);
            }
        }
    }
    
    /**
     * 查找所有需要重做注册的实例 redo 数据。
     *
     * @return set of {@code InstanceRedoData} need to do redo.
     */
    public Set<InstanceRedoData> findInstanceRedoData() {
        Set<InstanceRedoData> result = new HashSet<>();
        synchronized (registeredInstances) {
            for (InstanceRedoData each : registeredInstances.values()) {
                if (each.isNeedRedo()) {
                    result.add(each);
                }
            }
        }
        return result;
    }
    
    /**
     * 缓存订阅 redo 数据。
     *
     * @param serviceName service name
     * @param groupName   group name
     * @param cluster     cluster
     */
    public void cacheSubscriberForRedo(String serviceName, String groupName, String cluster) {
        String key =
            ServiceInfo.getKey(NamingUtils.getGroupedName(serviceName, groupName), cluster);
        SubscriberRedoData redoData = SubscriberRedoData.build(serviceName, groupName, cluster);
        synchronized (subscribes) {
            subscribes.put(key, redoData);
        }
    }
    
    /**
     * 订阅成功后标记 redo 为已注册。
     *
     * @param serviceName service name
     * @param groupName   group name
     * @param cluster     cluster
     */
    public void subscriberRegistered(String serviceName, String groupName, String cluster) {
        String key =
            ServiceInfo.getKey(NamingUtils.getGroupedName(serviceName, groupName), cluster);
        synchronized (subscribes) {
            SubscriberRedoData redoData = subscribes.get(key);
            if (null != redoData) {
                redoData.setRegistered(true);
            }
        }
    }
    
    /**
     * 取消订阅开始，标记 redo 为注销中。
     *
     * @param serviceName service name
     * @param groupName   group name
     * @param cluster     cluster
     */
    public void subscriberDeregister(String serviceName, String groupName, String cluster) {
        String key =
            ServiceInfo.getKey(NamingUtils.getGroupedName(serviceName, groupName), cluster);
        synchronized (subscribes) {
            SubscriberRedoData redoData = subscribes.get(key);
            if (null != redoData) {
                redoData.setUnregistering(true);
                redoData.setExpectedRegistered(false);
            }
        }
    }
    
    /**
     * 判断订阅是否已在服务端注册成功。
     *
     * @param serviceName service name
     * @param groupName   group name
     * @param cluster     cluster
     * @return {@code true} if subscribed, otherwise {@code false}
     */
    public boolean isSubscriberRegistered(String serviceName, String groupName, String cluster) {
        String key =
            ServiceInfo.getKey(NamingUtils.getGroupedName(serviceName, groupName), cluster);
        synchronized (subscribes) {
            SubscriberRedoData redoData = subscribes.get(key);
            return null != redoData && redoData.isRegistered();
        }
    }
    
    /**
     * 从 redo 缓存移除订阅（预期不再订阅时）。
     *
     * @param serviceName service name
     * @param groupName   group name
     * @param cluster     cluster
     */
    public void removeSubscriberForRedo(String serviceName, String groupName, String cluster) {
        String key =
            ServiceInfo.getKey(NamingUtils.getGroupedName(serviceName, groupName), cluster);
        synchronized (subscribes) {
            SubscriberRedoData redoData = subscribes.get(key);
            if (null != redoData && !redoData.isExpectedRegistered()) {
                subscribes.remove(key);
            }
        }
    }
    
    /**
     * 查找所有需要重做订阅的 redo 数据。
     *
     * @return set of {@code SubscriberRedoData} need to do redo.
     */
    public Set<SubscriberRedoData> findSubscriberRedoData() {
        Set<SubscriberRedoData> result = new HashSet<>();
        synchronized (subscribes) {
            for (SubscriberRedoData each : subscribes.values()) {
                if (each.isNeedRedo()) {
                    result.add(each);
                }
            }
        }
        return result;
    }
    
    /**
     * 按组合服务名获取实例 redo 缓存。
     *
     * @return cache service
     */
    public InstanceRedoData getRegisteredInstancesByKey(String combinedServiceName) {
        return registeredInstances.get(combinedServiceName);
    }
    
    /**
     * 关闭 redo 线程池并清空缓存。
     */
    public void shutdown() {
        LogUtils.NAMING_LOGGER.info("Shutdown grpc redo service executor " + redoExecutor);
        registeredInstances.clear();
        subscribes.clear();
        redoExecutor.shutdownNow();
    }
    
}
