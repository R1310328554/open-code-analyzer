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

package com.alibaba.nacos.client.naming.core;

import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.pojo.ServiceInfo;
import com.alibaba.nacos.api.naming.utils.NamingUtils;
import com.alibaba.nacos.client.env.NacosClientProperties;
import com.alibaba.nacos.client.naming.cache.ServiceInfoHolder;
import com.alibaba.nacos.client.naming.event.InstancesChangeNotifier;
import com.alibaba.nacos.client.naming.remote.NamingClientProxy;
import com.alibaba.nacos.common.executor.NameThreadFactory;
import com.alibaba.nacos.common.lifecycle.Closeable;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.alibaba.nacos.common.utils.ConvertUtils;
import com.alibaba.nacos.common.utils.ThreadUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.alibaba.nacos.client.utils.LogUtils.NAMING_LOGGER;

/**
 * 订阅服务的定时主动更新服务。
 *
 * <p>在开启异步查询订阅服务时，为每个 serviceKey 调度 {@link UpdateTask}，按 cacheMillis 倍数间隔向服务端拉取最新实例并更新 {@link ServiceInfoHolder}。</p>
 *
 * @author xiweng.yy
 */
public class ServiceInfoUpdateService implements Closeable {
    
    /** 首次调度延迟与失败退避基准（毫秒）。 */
    private static final long DEFAULT_DELAY = 1000L;
    
    /** 拉取间隔 = 服务端 cacheMillis × 该倍数。 */
    private static final int DEFAULT_UPDATE_CACHE_TIME_MULTIPLE = 6;
    
    /** 轮询线程池最小线程数。 */
    private static final int MIN_THREAD_NUM = 1;
    
    /** serviceKey 到定时任务的映射，防止重复调度。 */
    private final Map<String, ScheduledFuture<?>> futureMap = new HashMap<>();
    
    /** 本地服务缓存持有者。 */
    private final ServiceInfoHolder serviceInfoHolder;
    
    /** 更新任务调度线程池。 */
    private final ScheduledExecutorService executor;
    
    /** 命名远程代理，执行实例查询 RPC。 */
    private final NamingClientProxy namingClientProxy;
    
    /** 变更通知器，用于判断订阅是否仍有效。 */
    private final InstancesChangeNotifier changeNotifier;
    
    /** 是否启用订阅服务的异步主动查询。 */
    private final boolean asyncQuerySubscribeService;
    
    public ServiceInfoUpdateService(NacosClientProperties properties,
        ServiceInfoHolder serviceInfoHolder,
        NamingClientProxy namingClientProxy, InstancesChangeNotifier changeNotifier) {
        this.asyncQuerySubscribeService = isAsyncQueryForSubscribeService(properties);
        this.executor = new ScheduledThreadPoolExecutor(initPollingThreadCount(properties),
            new NameThreadFactory("com.alibaba.nacos.client.naming.updater"));
        this.serviceInfoHolder = serviceInfoHolder;
        this.namingClientProxy = namingClientProxy;
        this.changeNotifier = changeNotifier;
    }
    
    /** 读取 NAMING_ASYNC_QUERY_SUBSCRIBE_SERVICE 配置。 */
    private boolean isAsyncQueryForSubscribeService(NacosClientProperties properties) {
        if (properties == null
            || !properties.containsKey(PropertyKeyConst.NAMING_ASYNC_QUERY_SUBSCRIBE_SERVICE)) {
            return false;
        }
        return ConvertUtils.toBoolean(
            properties.getProperty(PropertyKeyConst.NAMING_ASYNC_QUERY_SUBSCRIBE_SERVICE),
            false);
    }
    
    /** 根据 CPU 与配置初始化轮询线程数。 */
    private int initPollingThreadCount(NacosClientProperties properties) {
        int count = ThreadUtils.getSuitableThreadCount(1) > 1
            ? ThreadUtils.getSuitableThreadCount(1) / 2 : 1;
        if (properties == null) {
            return count;
        }
        count = Math.min(
            properties.getInteger(PropertyKeyConst.NAMING_POLLING_MAX_THREAD_COUNT, count),
            count);
        count = Math.max(count, MIN_THREAD_NUM);
        return properties.getInteger(PropertyKeyConst.NAMING_POLLING_THREAD_COUNT, count);
    }
    
    /**
     * 若尚未调度则为该服务创建定时更新任务。
     *
     * @param serviceName 服务名
     * @param groupName   分组名
     * @param clusters    集群列表
     */
    public void scheduleUpdateIfAbsent(String serviceName, String groupName, String clusters) {
        if (!asyncQuerySubscribeService) {
            return;
        }
        String serviceKey =
            ServiceInfo.getKey(NamingUtils.getGroupedName(serviceName, groupName), clusters);
        if (futureMap.get(serviceKey) != null) {
            return;
        }
        synchronized (futureMap) {
            if (futureMap.get(serviceKey) != null) {
                return;
            }
            
            ScheduledFuture<?> future = addTask(new UpdateTask(serviceName, groupName, clusters));
            futureMap.put(serviceKey, future);
        }
    }
    
    /** 提交 UpdateTask 并返回 ScheduledFuture。 */
    private synchronized ScheduledFuture<?> addTask(UpdateTask task) {
        return executor.schedule(task, DEFAULT_DELAY, TimeUnit.MILLISECONDS);
    }
    
    /**
     * 取消指定服务的定时更新任务（若存在）。
     *
     * @param serviceName 服务名
     * @param groupName   分组名
     * @param clusters    集群列表
     */
    public void stopUpdateIfContain(String serviceName, String groupName, String clusters) {
        String serviceKey =
            ServiceInfo.getKey(NamingUtils.getGroupedName(serviceName, groupName), clusters);
        if (!futureMap.containsKey(serviceKey)) {
            return;
        }
        synchronized (futureMap) {
            if (!futureMap.containsKey(serviceKey)) {
                return;
            }
            futureMap.remove(serviceKey);
        }
    }
    
    @Override
    public void shutdown() throws NacosException {
        String className = this.getClass().getName();
        NAMING_LOGGER.info("{} do shutdown begin", className);
        ThreadUtils.shutdownThreadPool(executor, NAMING_LOGGER);
        NAMING_LOGGER.info("{} do shutdown stop", className);
    }
    
    /** 单服务定时拉取任务，含失败指数退避。 */
    public class UpdateTask implements Runnable {
        
        /** 上次见到的服务端 lastRefTime，用于判断是否需要重新查询。 */
        long lastRefTime = Long.MAX_VALUE;
        
        /** 任务是否已取消（退订后不再重调度）。 */
        private boolean isCancel;
        
        private final String serviceName;
        
        private final String groupName;
        
        private final String clusters;
        
        private final String groupedServiceName;
        
        private final String serviceKey;
        
        /** 连续失败次数，用于指数退避（如连不上服务端或实例为空）。 */
        /**
         * the fail situation. 1:can't connect to server 2:serviceInfo's hosts is empty
          * <p>订阅服务定时更新；详见类级说明。</p>
         */
        private int failCount = 0;
        
        public UpdateTask(String serviceName, String groupName, String clusters) {
            this.serviceName = serviceName;
            this.groupName = groupName;
            this.clusters = clusters;
            this.groupedServiceName = NamingUtils.getGroupedName(serviceName, groupName);
            this.serviceKey = ServiceInfo.getKey(groupedServiceName, clusters);
        }
        
        @Override
        public void run() {
            long delayTime = DEFAULT_DELAY;
            
            try {
                if (!changeNotifier.isSubscribed(groupName, serviceName) && !futureMap.containsKey(
                    serviceKey)) {
                    NAMING_LOGGER.info("update task is stopped, service:{}, clusters:{}",
                        groupedServiceName, clusters);
                    isCancel = true;
                    return;
                }
                
                ServiceInfo serviceObj = serviceInfoHolder.getServiceInfoMap().get(serviceKey);
                if (serviceObj == null) {
                    serviceObj = namingClientProxy.queryInstancesOfService(serviceName, groupName,
                        clusters, false);
                    serviceInfoHolder.processServiceInfo(serviceObj);
                    // TODO 拉取间隔倍数可配置化
                    delayTime = serviceObj.getCacheMillis() * DEFAULT_UPDATE_CACHE_TIME_MULTIPLE;
                    lastRefTime = serviceObj.getLastRefTime();
                    return;
                }
                
                if (serviceObj.getLastRefTime() <= lastRefTime) {
                    serviceObj = namingClientProxy.queryInstancesOfService(serviceName, groupName,
                        clusters, false);
                    serviceInfoHolder.processServiceInfo(serviceObj);
                }
                lastRefTime = serviceObj.getLastRefTime();
                if (CollectionUtils.isEmpty(serviceObj.getHosts())) {
                    incFailCount();
                    return;
                }
                // TODO 拉取间隔倍数可配置化
                delayTime = serviceObj.getCacheMillis() * DEFAULT_UPDATE_CACHE_TIME_MULTIPLE;
                resetFailCount();
            } catch (NacosException e) {
                handleNacosException(e);
            } catch (Throwable e) {
                handleUnknownException(e);
            } finally {
                if (!isCancel) {
                    executor.schedule(this, Math.min(delayTime << failCount, DEFAULT_DELAY * 60),
                        TimeUnit.MILLISECONDS);
                }
            }
        }
        
        /** 处理 Nacos 异常并递增失败计数。 */
        private void handleNacosException(NacosException e) {
            incFailCount();
            int errorCode = e.getErrCode();
            if (NacosException.SERVER_ERROR == errorCode) {
                handleUnknownException(e);
            }
            NAMING_LOGGER.warn("Can't update serviceName: {}, reason: {}", groupedServiceName,
                e.getErrMsg());
        }
        
        /** 处理未知异常并递增失败计数。 */
        private void handleUnknownException(Throwable throwable) {
            incFailCount();
            NAMING_LOGGER.warn("[NA] failed to update serviceName: {}", groupedServiceName,
                throwable);
        }
        
        /** 递增失败次数，上限为 6。 */
        private void incFailCount() {
            int limit = 6;
            if (failCount == limit) {
                return;
            }
            failCount++;
        }
        
        /** 成功后重置失败计数。 */
        private void resetFailCount() {
            failCount = 0;
        }
    }
}
