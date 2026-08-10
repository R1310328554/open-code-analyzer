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

package com.alibaba.nacos.client.naming.backups;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.pojo.ServiceInfo;
import com.alibaba.nacos.client.naming.cache.InstancesDiffer;
import com.alibaba.nacos.client.naming.cache.ServiceInfoHolder;
import com.alibaba.nacos.client.naming.event.InstancesChangeEvent;
import com.alibaba.nacos.client.naming.event.InstancesDiff;
import com.alibaba.nacos.common.executor.NameThreadFactory;
import com.alibaba.nacos.common.lifecycle.Closeable;
import com.alibaba.nacos.common.notify.NotifyCenter;
import com.alibaba.nacos.common.spi.NacosServiceLoader;
import com.alibaba.nacos.common.utils.JacksonUtils;
import com.alibaba.nacos.common.utils.ThreadUtils;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.ImmutableTag;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Tag;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.alibaba.nacos.client.utils.LogUtils.NAMING_LOGGER;

/**
 * 命名容灾反应器。
 *
 * <p>定时轮询 {@link FailoverDataSource} 的开关与磁盘容灾数据，切换本地服务映射并在开关变化时发布 {@link InstancesChangeEvent}。与 {@link ServiceInfoHolder} 协同在容灾关闭时恢复服务端缓存。</p>
 *
 * @author nkorange
 */
public class FailoverReactor implements Closeable {
    
    /** 当前生效的容灾服务实例映射（groupKey -> ServiceInfo）。 */
    private Map<String, ServiceInfo> serviceMap = new ConcurrentHashMap<>();
    
    /** 容灾模式是否已开启。 */
    private boolean failoverSwitchEnable;
    
    /** 正常模式下的服务缓存持有者，用于容灾关闭时对账恢复。 */
    private final ServiceInfoHolder serviceInfoHolder;
    
    /** 定时刷新容灾开关与数据的单线程调度器。 */
    private final ScheduledExecutorService executorService;
    
    /** 实例列表差异计算器，用于生成变更事件。 */
    private final InstancesDiffer instancesDiffer;
    
    /** SPI 加载的容灾数据源（通常仅取首个）。 */
    private FailoverDataSource failoverDataSource;
    
    /** 发布实例变更事件的作用域标识。 */
    private String notifierEventScope;
    
    public FailoverReactor(ServiceInfoHolder serviceInfoHolder, String notifierEventScope) {
        this.serviceInfoHolder = serviceInfoHolder;
        this.notifierEventScope = notifierEventScope;
        this.instancesDiffer = new InstancesDiffer();
        Collection<FailoverDataSource> dataSources =
            NacosServiceLoader.load(FailoverDataSource.class);
        for (FailoverDataSource dataSource : dataSources) {
            failoverDataSource = dataSource;
            NAMING_LOGGER.info("FailoverDataSource type is {}", dataSource.getClass());
            break;
        }
        // 初始化容灾刷新线程池
        this.executorService = new ScheduledThreadPoolExecutor(1,
            new NameThreadFactory("com.alibaba.nacos.naming.failover"));
        this.init();
    }
    
    /** 启动定时任务，每 5 秒刷新容灾开关与数据。 */
    /** Init. */
    /** 初始化容灾刷新调度。 */
    public void init() {
        executorService.scheduleWithFixedDelay(new FailoverSwitchRefresher(), 0L, 5000L,
            TimeUnit.MILLISECONDS);
    }
    
    /** 定时任务：读取开关、同步容灾数据并在切换时通知监听器。 */
    class FailoverSwitchRefresher implements Runnable {
        
        @Override
        public void run() {
            try {
                FailoverSwitch fSwitch = failoverDataSource.getSwitch();
                if (fSwitch == null) {
                    failoverSwitchEnable = false;
                    return;
                }
                if (fSwitch.getEnabled() != failoverSwitchEnable) {
                    NAMING_LOGGER.info("failover switch changed, new: {}", fSwitch.getEnabled());
                }
                if (fSwitch.getEnabled()) {
                    Map<String, ServiceInfo> failoverMap = new ConcurrentHashMap<>(200);
                    Map<String, FailoverData> failoverData = failoverDataSource.getFailoverData();
                    for (Map.Entry<String, FailoverData> entry : failoverData.entrySet()) {
                        ServiceInfo newService = (ServiceInfo) entry.getValue().getData();
                        ServiceInfo oldService = serviceMap.get(entry.getKey());
                        InstancesDiff diff = instancesDiffer.doDiff(oldService, newService);
                        if (diff.hasDifferent()) {
                            NAMING_LOGGER.info(
                                "[NA] failoverdata isChangedServiceInfo. newService:{}",
                                JacksonUtils.toJson(newService));
                            NotifyCenter.publishEvent(new InstancesChangeEvent(notifierEventScope,
                                newService.getName(),
                                newService.getGroupName(), newService.getClusters(),
                                newService.getHosts(), diff));
                        }
                        failoverMap.put(entry.getKey(), (ServiceInfo) entry.getValue().getData());
                    }
                    
                    if (!failoverMap.isEmpty()) {
                        failoverServiceCntMetrics();
                        serviceMap = failoverMap;
                    }
                    
                    failoverSwitchEnable = true;
                    return;
                }
                
                if (failoverSwitchEnable && !fSwitch.getEnabled()) {
                    Map<String, ServiceInfo> serviceInfoMap = serviceInfoHolder.getServiceInfoMap();
                    for (Map.Entry<String, ServiceInfo> entry : serviceMap.entrySet()) {
                        ServiceInfo oldService = entry.getValue();
                        ServiceInfo newService = serviceInfoMap.get(entry.getKey());
                        if (newService != null) {
                            InstancesDiff diff = instancesDiffer.doDiff(oldService, newService);
                            if (diff.hasDifferent()) {
                                NotifyCenter.publishEvent(
                                    new InstancesChangeEvent(notifierEventScope,
                                        newService.getName(),
                                        newService.getGroupName(), newService.getClusters(),
                                        newService.getHosts(), diff));
                            }
                        }
                    }
                    
                    serviceMap.clear();
                    failoverSwitchEnable = false;
                    failoverServiceCntMetricsClear();
                }
            } catch (Exception e) {
                NAMING_LOGGER.error("FailoverSwitchRefresher run err", e);
            }
        }
    }
    
    /** 全局容灾开关是否开启。 */
    public boolean isFailoverSwitch() {
        return failoverSwitchEnable;
    }
    
    /** 指定服务是否在容灾模式且拥有可用实例。 */
    public boolean isFailoverSwitch(String serviceName) {
        ServiceInfo serviceInfo = serviceMap.get(serviceName);
        return failoverSwitchEnable && serviceInfo != null && serviceInfo.ipCount() > 0;
    }
    
    /** 按 groupKey 获取容灾服务信息；不存在时返回空壳 ServiceInfo。 */
    public ServiceInfo getService(String key) {
        ServiceInfo serviceInfo = serviceMap.get(key);
        
        if (serviceInfo == null) {
            serviceInfo = new ServiceInfo();
            serviceInfo.setName(key);
        }
        
        return serviceInfo;
    }
    
    /**
     * 关闭容灾刷新线程池。
     *
     * @throws NacosException 关闭过程中的 Nacos 异常
     */
    @Override
    public void shutdown() throws NacosException {
        String className = this.getClass().getName();
        NAMING_LOGGER.info("{} do shutdown begin", className);
        ThreadUtils.shutdownThreadPool(executorService, NAMING_LOGGER);
        NAMING_LOGGER.info("{} do shutdown stop", className);
    }
    
    /** 为各容灾服务注册 Micrometer 实例数 Gauge。 */
    private void failoverServiceCntMetrics() {
        for (Map.Entry<String, ServiceInfo> entry : serviceMap.entrySet()) {
            String serviceName = entry.getKey();
            List<Tag> tags = new ArrayList<>();
            tags.add(new ImmutableTag("service_name", serviceName));
            if (Metrics.globalRegistry.find("nacos_naming_client_failover_instances").tags(tags)
                .gauge() == null) {
                Gauge.builder("nacos_naming_client_failover_instances",
                    () -> serviceMap.get(serviceName).ipCount())
                    .tags(tags).register(Metrics.globalRegistry);
            }
        }
    }
    
    /** 容灾关闭时移除已注册的实例数 Gauge。 */
    private void failoverServiceCntMetricsClear() {
        for (Map.Entry<String, ServiceInfo> entry : serviceMap.entrySet()) {
            Gauge gauge = Metrics.globalRegistry.find("nacos_naming_client_failover_instances")
                .tag("service_name", entry.getKey()).gauge();
            if (gauge != null) {
                Metrics.globalRegistry.remove(gauge);
            }
        }
    }
}
