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

package com.alibaba.nacos.client.naming.cache;

import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.pojo.ServiceInfo;
import com.alibaba.nacos.api.naming.utils.NamingUtils;
import com.alibaba.nacos.client.env.NacosClientProperties;
import com.alibaba.nacos.client.monitor.MetricsMonitor;
import com.alibaba.nacos.client.naming.backups.FailoverReactor;
import com.alibaba.nacos.client.naming.event.InstancesChangeEvent;
import com.alibaba.nacos.client.naming.event.InstancesDiff;
import com.alibaba.nacos.client.naming.utils.CacheDirUtil;
import com.alibaba.nacos.common.lifecycle.Closeable;
import com.alibaba.nacos.common.notify.NotifyCenter;
import com.alibaba.nacos.common.utils.ConvertUtils;
import com.alibaba.nacos.common.utils.JacksonUtils;
import com.alibaba.nacos.common.utils.StringUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.alibaba.nacos.client.utils.LogUtils.NAMING_LOGGER;

/**
 * 命名客户端服务信息本地缓存持有者。
 *
 * <p>维护 serviceKey -> {@link ServiceInfo} 映射，处理服务端推送与主动查询结果，计算 {@link InstancesDiff} 并通过 {@link NotifyCenter} 发布变更事件；集成容灾切换与异步磁盘缓存刷新。</p>
 *
 * @author xiweng.yy
 */
public class ServiceInfoHolder implements Closeable {
    
    /** 本地服务实例缓存（group@@service -> ServiceInfo）。 */
    private final ConcurrentMap<String, ServiceInfo> serviceInfoMap;
    
    /** 容灾反应器，管理容灾开关与服务映射切换。 */
    private final FailoverReactor failoverReactor;
    
    /** 是否启用空推送保护（拒绝无效/空实例列表覆盖缓存）。 */
    private final boolean pushEmptyProtection;
    
    /** 实例列表差异计算器。 */
    private final InstancesDiffer instancesDiffer;
    
    /** 磁盘缓存异步刷新器。 */
    private final ServiceInfoDiskCacheRefresher serviceInfoDiskCacheRefresher;
    
    /** 本地磁盘缓存目录路径。 */
    private String cacheDir;
    
    /** 变更事件作用域，供 {@link InstancesChangeNotifier} 过滤。 */
    private String notifierEventScope;
    
    /** 是否上报客户端 Prometheus 指标。 */
    private boolean enableClientMetrics = true;
    
    public ServiceInfoHolder(String namespace, String notifierEventScope,
        NacosClientProperties properties) {
        cacheDir = CacheDirUtil.initCacheDir(namespace, properties);
        instancesDiffer = new InstancesDiffer();
        if (isLoadCacheAtStart(properties)) {
            this.serviceInfoMap = new ConcurrentHashMap<>(DiskCache.read(this.cacheDir));
        } else {
            this.serviceInfoMap = new ConcurrentHashMap<>(16);
        }
        this.failoverReactor = new FailoverReactor(this, notifierEventScope);
        this.serviceInfoDiskCacheRefresher = new ServiceInfoDiskCacheRefresher();
        this.pushEmptyProtection = isPushEmptyProtect(properties);
        this.notifierEventScope = notifierEventScope;
        this.enableClientMetrics = Boolean.parseBoolean(
            properties.getProperty(PropertyKeyConst.ENABLE_CLIENT_METRICS, "true"));
    }
    
    /** 读取配置：启动时是否从磁盘加载缓存。 */
    private boolean isLoadCacheAtStart(NacosClientProperties properties) {
        boolean loadCacheAtStart = false;
        if (properties != null && StringUtils.isNotEmpty(
            properties.getProperty(PropertyKeyConst.NAMING_LOAD_CACHE_AT_START))) {
            loadCacheAtStart = ConvertUtils.toBoolean(
                properties.getProperty(PropertyKeyConst.NAMING_LOAD_CACHE_AT_START));
        }
        return loadCacheAtStart;
    }
    
    /** 读取配置：是否启用空推送保护。 */
    private boolean isPushEmptyProtect(NacosClientProperties properties) {
        boolean pushEmptyProtection = false;
        if (properties != null && StringUtils.isNotEmpty(
            properties.getProperty(PropertyKeyConst.NAMING_PUSH_EMPTY_PROTECTION))) {
            pushEmptyProtection = ConvertUtils.toBoolean(
                properties.getProperty(PropertyKeyConst.NAMING_PUSH_EMPTY_PROTECTION));
        }
        return pushEmptyProtection;
    }
    
    /** 返回内部服务缓存映射（可变，供更新任务读取）。 */
    public Map<String, ServiceInfo> getServiceInfoMap() {
        return serviceInfoMap;
    }
    
    /** 按服务名与分组获取缓存副本（防御性克隆）。 */
    public ServiceInfo getServiceInfo(final String serviceName, final String groupName) {
        String key = NamingUtils.getGroupedName(serviceName, groupName);
        ServiceInfo serviceInfo = serviceInfoMap.get(key);
        return serviceInfo == null ? null : serviceInfo.clone();
    }
    
    /**
     * 解析 JSON 并处理服务信息更新。
     *
     * @param json 服务端返回的服务 JSON
     * @return 处理后的服务信息
     */
    public ServiceInfo processServiceInfo(String json) {
        ServiceInfo serviceInfo = JacksonUtils.toObj(json, ServiceInfo.class);
        serviceInfo.setJsonFromServer(json);
        return processServiceInfo(serviceInfo);
    }
    
    /**
     * 处理新服务信息：校验、更新缓存、发布变更与刷盘事件。
     *
     * @param serviceInfo 新服务信息
     * @return 更新后的服务信息，忽略时返回旧值
     */
    public ServiceInfo processServiceInfo(ServiceInfo serviceInfo) {
        String serviceKey = serviceInfo.getKeyWithoutClusters();
        if (serviceKey == null) {
            NAMING_LOGGER.warn("process service info but serviceKey is null, service host: {}",
                JacksonUtils.toJson(serviceInfo.getHosts()));
            return null;
        }
        ServiceInfo oldService = serviceInfoMap.get(serviceKey);
        if (isEmptyOrErrorPush(serviceInfo)) {
            // 空推送或错误推送，直接忽略
            NAMING_LOGGER.warn(
                "process service info but found empty or error push, serviceKey: {}, "
                    + "pushEmptyProtection: {}, hosts: {}",
                serviceKey, pushEmptyProtection, serviceInfo.getHosts());
            return oldService;
        }
        serviceInfoMap.put(serviceKey, serviceInfo);
        InstancesDiff diff = getServiceInfoDiff(oldService, serviceInfo);
        if (StringUtils.isBlank(serviceInfo.getJsonFromServer())) {
            serviceInfo.setJsonFromServer(JacksonUtils.toJson(serviceInfo));
        }
        
        if (enableClientMetrics) {
            try {
                MetricsMonitor.getServiceInfoMapSizeMonitor().set(serviceInfoMap.size());
            } catch (Throwable t) {
                NAMING_LOGGER.error("Failed to update metrics for service info map size", t);
            }
        }
        
        if (diff.hasDifferent()) {
            NAMING_LOGGER.info("current ips:({}) service: {} -> {}", serviceInfo.ipCount(),
                serviceKey,
                JacksonUtils.toJson(serviceInfo.getHosts()));
            
            if (!failoverReactor.isFailoverSwitch(serviceKey)) {
                NotifyCenter.publishEvent(
                    new InstancesChangeEvent(notifierEventScope, serviceInfo.getName(),
                        serviceInfo.getGroupName(),
                        serviceInfo.getClusters(), serviceInfo.getHosts(), diff));
            }
            publishDiskCacheRefreshEvent(serviceKey, serviceInfo);
        }
        return serviceInfo;
    }
    
    /**
     * 发布磁盘缓存异步刷新事件。
     *
     * @param serviceKey 不含集群后缀的服务键
     * @param serviceInfo 最新服务信息快照
     */
    /** 委托刷新器合并并异步落盘。 */
    private void publishDiskCacheRefreshEvent(String serviceKey, ServiceInfo serviceInfo) {
        serviceInfoDiskCacheRefresher.publishEvent(
            new ServiceInfoDiskCacheRefreshEvent(serviceKey, serviceInfo, cacheDir));
    }
    
    /** 判断是否为空实例列表或在校验失败且开启保护时应忽略的推送。 */
    private boolean isEmptyOrErrorPush(ServiceInfo serviceInfo) {
        return null == serviceInfo.getHosts() || (pushEmptyProtection && !serviceInfo.validate());
    }
    
    /** 计算新旧服务信息之间的实例差异。 */
    private InstancesDiff getServiceInfoDiff(ServiceInfo oldService, ServiceInfo newService) {
        return instancesDiffer.doDiff(oldService, newService);
    }
    
    /** 返回本地磁盘缓存目录。 */
    public String getCacheDir() {
        return cacheDir;
    }
    
    /** 判断全局容灾开关是否开启。 */
    public boolean isFailoverSwitch() {
        return failoverReactor.isFailoverSwitch();
    }
    
    /** 从容灾映射获取指定服务的容灾实例信息。 */
    public ServiceInfo getFailoverServiceInfo(final String serviceName, final String groupName) {
        String key = NamingUtils.getGroupedName(serviceName, groupName);
        return failoverReactor.getService(key);
    }
    
    @Override
    public void shutdown() throws NacosException {
        String className = this.getClass().getName();
        NAMING_LOGGER.info("{} do shutdown begin", className);
        failoverReactor.shutdown();
        serviceInfoDiskCacheRefresher.shutdown();
        NAMING_LOGGER.info("{} do shutdown stop", className);
    }
}
