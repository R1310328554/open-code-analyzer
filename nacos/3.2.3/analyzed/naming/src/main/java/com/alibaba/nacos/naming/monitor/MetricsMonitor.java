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

package com.alibaba.nacos.naming.monitor;

import com.alibaba.nacos.core.monitor.NacosMeterRegistryCenter;
import com.alibaba.nacos.naming.core.v2.pojo.BatchInstancePublishInfo;
import com.alibaba.nacos.naming.core.v2.pojo.InstancePublishInfo;
import com.alibaba.nacos.naming.core.v2.pojo.Service;
import com.alibaba.nacos.naming.misc.Loggers;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.ImmutableTag;
import io.micrometer.core.instrument.Tag;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Naming 模块核心指标监控单例。
 *
 * <p>维护健康检查、推送、订阅、Distro 事件队列等运行时计数器，通过 {@link NacosMeterRegistryCenter} 注册 Micrometer Gauge/Counter 供 Prometheus 等采集。</p>
 *
 * @author Nacos
 */
public class MetricsMonitor {
    
    /** Naming 稳定指标注册表名称。 */
    private static final String METER_REGISTRY = NacosMeterRegistryCenter.NAMING_STABLE_REGISTRY;
    
    /** 全局单例实例。 */
    private static final MetricsMonitor INSTANCE = new MetricsMonitor();
    
    /** MySQL 健康检查并发任务数。 */
    private final AtomicInteger mysqlHealthCheck = new AtomicInteger();
    
    /** HTTP 健康检查并发任务数。 */
    private final AtomicInteger httpHealthCheck = new AtomicInteger();
    
    /** TCP 健康检查并发任务数。 */
    private final AtomicInteger tcpHealthCheck = new AtomicInteger();
    
    /** 当前服务（Dom）总数。 */
    private final AtomicInteger serviceCount = new AtomicInteger();
    
    /** 当前实例（IP）总数。 */
    private final AtomicInteger ipCount = new AtomicInteger();
    
    /** 订阅者总数。 */
    private final AtomicInteger subscriberCount = new AtomicInteger();
    
    /** 推送耗时最大值（毫秒），-1 表示暂无数据。 */
    private final AtomicLong maxPushCost = new AtomicLong(-1);
    
    /** 推送平均耗时（毫秒）。 */
    private final AtomicLong avgPushCost = new AtomicLong(-1);
    
    /** 集群 Leader 状态指标。 */
    private final AtomicLong leaderStatus = new AtomicLong();
    
    /** 推送总次数。 */
    private final AtomicInteger totalPush = new AtomicInteger();
    
    private final AtomicInteger totalPushCountForAvg = new AtomicInteger();
    
    private final AtomicLong totalPushCostForAvg = new AtomicLong();
    
    /** 推送失败次数。 */
    private final AtomicInteger failedPush = new AtomicInteger();
    
    /** 空推送次数（无变更内容）。 */
    private final AtomicInteger emptyPush = new AtomicInteger();
    
    /** 服务订阅事件队列当前长度。 */
    private final AtomicInteger serviceSubscribedEventQueueSize = new AtomicInteger();
    
    /** 服务变更事件队列当前长度。 */
    private final AtomicInteger serviceChangedEventQueueSize = new AtomicInteger();
    
    /** 待处理推送任务数。 */
    private final AtomicInteger pushPendingTaskCount = new AtomicInteger();
    
    /**
     * 按协议版本（v1/v2）统计的订阅者数量。
     */
    private final ConcurrentHashMap<String, AtomicInteger> namingSubscriber =
        new ConcurrentHashMap<>();
    
    /**
     * 按协议版本（v1/v2）统计的发布者数量。
     */
    private final ConcurrentHashMap<String, AtomicInteger> namingPublisher =
        new ConcurrentHashMap<>();
    
    /**
     * 服务变更次数 TopN 计数器。
     */
    private final ServiceTopNCounter serviceChangeCount = new ServiceTopNCounter();
    
    private MetricsMonitor() {
        for (Field each : MetricsMonitor.class.getDeclaredFields()) {
            if (Number.class.isAssignableFrom(each.getType())) {
                each.setAccessible(true);
                try {
                    registerToMetrics(each.getName(), (Number) each.get(this));
                } catch (IllegalAccessException e) {
                    Loggers.PERFORMANCE_LOG.error("Init metrics for {} failed", each.getName(), e);
                }
            }
        }
        
        namingSubscriber.put("v1", new AtomicInteger(0));
        namingSubscriber.put("v2", new AtomicInteger(0));
        
        List<Tag> tags = new ArrayList<>();
        tags.add(new ImmutableTag("version", "v1"));
        NacosMeterRegistryCenter.gauge(METER_REGISTRY, "nacos_naming_subscriber", tags,
            namingSubscriber.get("v1"));
        
        tags = new ArrayList<>();
        tags.add(new ImmutableTag("version", "v2"));
        NacosMeterRegistryCenter.gauge(METER_REGISTRY, "nacos_naming_subscriber", tags,
            namingSubscriber.get("v2"));
        
        namingPublisher.put("v1", new AtomicInteger(0));
        namingPublisher.put("v2", new AtomicInteger(0));
        
        tags = new ArrayList<>();
        tags.add(new ImmutableTag("version", "v1"));
        NacosMeterRegistryCenter.gauge(METER_REGISTRY, "nacos_naming_publisher", tags,
            namingPublisher.get("v1"));
        
        tags = new ArrayList<>();
        tags.add(new ImmutableTag("version", "v2"));
        NacosMeterRegistryCenter.gauge(METER_REGISTRY, "nacos_naming_publisher", tags,
            namingPublisher.get("v2"));
    }
    
    /** 将数值型字段注册为 Micrometer Gauge（module=naming）。 */
    private <T extends Number> void registerToMetrics(String name, T number) {
        List<Tag> tags = new ArrayList<>();
        tags.add(new ImmutableTag("module", "naming"));
        tags.add(new ImmutableTag("name", name));
        NacosMeterRegistryCenter.gauge(METER_REGISTRY, "nacos_monitor", tags, number);
    }
    
    public static AtomicInteger getMysqlHealthCheckMonitor() {
        return INSTANCE.mysqlHealthCheck;
    }
    
    public static AtomicInteger getHttpHealthCheckMonitor() {
        return INSTANCE.httpHealthCheck;
    }
    
    public static AtomicInteger getTcpHealthCheckMonitor() {
        return INSTANCE.tcpHealthCheck;
    }
    
    public static AtomicInteger getDomCountMonitor() {
        return INSTANCE.serviceCount;
    }
    
    public static AtomicInteger getIpCountMonitor() {
        return INSTANCE.ipCount;
    }
    
    public static AtomicInteger getSubscriberCount() {
        return INSTANCE.subscriberCount;
    }
    
    public static AtomicLong getMaxPushCostMonitor() {
        return INSTANCE.maxPushCost;
    }
    
    public static AtomicLong getAvgPushCostMonitor() {
        return INSTANCE.avgPushCost;
    }
    
    public static AtomicLong getLeaderStatusMonitor() {
        return INSTANCE.leaderStatus;
    }
    
    public static AtomicInteger getTotalPushMonitor() {
        return INSTANCE.totalPush;
    }
    
    public static AtomicInteger getFailedPushMonitor() {
        return INSTANCE.failedPush;
    }
    
    public static AtomicInteger getEmptyPushMonitor() {
        return INSTANCE.emptyPush;
    }
    
    public static AtomicInteger getTotalPushCountForAvg() {
        return INSTANCE.totalPushCountForAvg;
    }
    
    public static AtomicInteger getServiceSubscribedEventQueueSize() {
        return INSTANCE.serviceSubscribedEventQueueSize;
    }
    
    public static AtomicInteger getServiceChangedEventQueueSize() {
        return INSTANCE.serviceChangedEventQueueSize;
    }
    
    public static AtomicInteger getPushPendingTaskCount() {
        return INSTANCE.pushPendingTaskCount;
    }
    
    public static AtomicLong getTotalPushCostForAvg() {
        return INSTANCE.totalPushCostForAvg;
    }
    
    public static AtomicInteger getNamingSubscriber(String version) {
        return INSTANCE.namingSubscriber.get(version);
    }
    
    public static AtomicInteger getNamingPublisher(String version) {
        return INSTANCE.namingPublisher.get(version);
    }
    
    public static ServiceTopNCounter getServiceChangeCount() {
        return INSTANCE.serviceChangeCount;
    }
    
    /** 原子更新推送最大耗时（取更大值）。 */
    public static void compareAndSetMaxPushCost(long newCost) {
        INSTANCE.maxPushCost.getAndUpdate((prev) -> Math.max(newCost, prev));
    }
    
    /** 推送总次数加一。 */
    public static void incrementPush() {
        INSTANCE.totalPush.incrementAndGet();
    }
    
    /** 累加推送耗时并增加平均计算样本数。 */
    public static void incrementPushCost(long costTime) {
        INSTANCE.totalPushCountForAvg.incrementAndGet();
        INSTANCE.totalPushCostForAvg.addAndGet(costTime);
    }
    
    /** 推送失败次数加一。 */
    public static void incrementFailPush() {
        INSTANCE.failedPush.incrementAndGet();
    }
    
    /** 空推送次数加一。 */
    public static void incrementEmptyPush() {
        INSTANCE.emptyPush.incrementAndGet();
    }
    
    /** 实例计数加一（单实例注册）。 */
    public static void incrementInstanceCount() {
        INSTANCE.ipCount.incrementAndGet();
    }
    
    /** 实例计数减一（单实例注销）。 */
    public static void decrementInstanceCount() {
        INSTANCE.ipCount.decrementAndGet();
    }
    
    /** 订阅计数加一。 */
    public static void incrementSubscribeCount() {
        INSTANCE.subscriberCount.incrementAndGet();
    }
    
    /** 订阅计数减一。 */
    public static void decrementSubscribeCount() {
        INSTANCE.subscriberCount.decrementAndGet();
    }
    
    /** 记录指定服务的变更次数（TopN 统计）。 */
    public static void incrementServiceChangeCount(Service service) {
        INSTANCE.serviceChangeCount.increment(service);
    }
    
    /** 磁盘异常 Counter 指标。 */
    public static Counter getDiskException() {
        return NacosMeterRegistryCenter.counter(METER_REGISTRY, "nacos_exception", "module",
            "naming", "name", "disk");
    }
    
    /** Leader 发送心跳失败异常 Counter 指标。 */
    public static Counter getLeaderSendBeatFailedException() {
        return NacosMeterRegistryCenter
            .counter(METER_REGISTRY, "nacos_exception", "module", "naming", "name",
                "leaderSendBeatFailed");
    }
    
    /**
     * 批量注册实例时按差异更新 IP 计数。
     *
     * @param old                 旧的实例发布信息
     * @param instancePublishInfo 必须为 {@link BatchInstancePublishInfo}
     */
    public static void incrementIpCountWithBatchRegister(InstancePublishInfo old,
        BatchInstancePublishInfo instancePublishInfo) {
        int newSize = instancePublishInfo.getInstancePublishInfos().size();
        if (null == old) {
            // 首次批量注册，将全部实例数计入指标
            getIpCountMonitor().addAndGet(newSize);
        } else if (old instanceof BatchInstancePublishInfo) {
            // 非首次批量更新，按新旧实例数差值调整指标（差值可能为负）
            int oldSize = ((BatchInstancePublishInfo) old).getInstancePublishInfos().size();
            getIpCountMonitor().addAndGet(newSize - oldSize);
        } else {
            // 旧数据非批量类型时，按新批量大小减 1 计算差值
            getIpCountMonitor().addAndGet(newSize - 1);
        }
    }
    
    /**
     * 批量注销实例时减少 IP 计数。
     *
     * @param instancePublishInfo 必须为 {@link BatchInstancePublishInfo}
     */
    public static void decrementIpCountWithBatchRegister(InstancePublishInfo instancePublishInfo) {
        BatchInstancePublishInfo batchInstancePublishInfo =
            (BatchInstancePublishInfo) instancePublishInfo;
        List<InstancePublishInfo> instancePublishInfos =
            batchInstancePublishInfo.getInstancePublishInfos();
        getIpCountMonitor().addAndGet(-1 * instancePublishInfos.size());
    }
    
    /**
     * 重置全部指标（含健康检查与推送）。
     */
    public static void resetAll() {
        resetPush();
        getHttpHealthCheckMonitor().set(0);
        getMysqlHealthCheckMonitor().set(0);
        getTcpHealthCheckMonitor().set(0);
    }
    
    /**
     * 仅重置推送相关指标。
     */
    public static void resetPush() {
        getTotalPushMonitor().set(0);
        getFailedPushMonitor().set(0);
        getEmptyPushMonitor().set(0);
        getTotalPushCostForAvg().set(0);
        getTotalPushCountForAvg().set(0);
        getMaxPushCostMonitor().set(-1);
        getAvgPushCostMonitor().set(-1);
    }
}
