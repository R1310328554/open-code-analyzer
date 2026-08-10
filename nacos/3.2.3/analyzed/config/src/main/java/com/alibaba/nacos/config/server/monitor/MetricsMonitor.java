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

package com.alibaba.nacos.config.server.monitor;

import com.alibaba.nacos.core.monitor.NacosMeterRegistryCenter;
import com.alibaba.nacos.core.monitor.topn.StringTopNCounter;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.ImmutableTag;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 配置模块核心指标注册中心：维护拉配置、发布、长轮询、通知、dump 等 gauge 与 timer/counter。
 * 静态块向 {@link NacosMeterRegistryCenter#CONFIG_STABLE_REGISTRY} 注册 Micrometer 指标供运维采集。
 * Metrics Monitor.
 *
 * @author Nacos
 */
public class MetricsMonitor {
    
    /** 配置稳定指标注册表名称 */
    private static final String METER_REGISTRY = NacosMeterRegistryCenter.CONFIG_STABLE_REGISTRY;
    
    /** 拉配置并发/进行中的任务计数 */
    private static AtomicInteger getConfig = new AtomicInteger();
    
    /** 发布配置任务计数 */
    private static AtomicInteger publish = new AtomicInteger();
    
    /** HTTP 长轮询订阅客户端的配置变更通知任务计数 */
    private static AtomicInteger longPolling = new AtomicInteger();
    
    /** 当前配置 group 总数（由 PrintMemoryTask 刷新） */
    private static AtomicInteger configCount = new AtomicInteger();
    
    /** 向集群其他节点同步配置变更的通知任务计数 */
    private static AtomicInteger notifyTask = new AtomicInteger();
    
    /** 长连接订阅客户端的配置变更通知任务计数 */
    private static AtomicInteger notifyClientTask = new AtomicInteger();
    
    /** 配置 dump 到磁盘任务计数 */
    private static AtomicInteger dumpTask = new AtomicInteger();
    
    /** 配置模糊搜索请求计数 */
    private static AtomicInteger fuzzySearch = new AtomicInteger();
    
    /** 按协议版本（v1/v2）统计的配置订阅客户端数 */
    private static ConcurrentHashMap<String, AtomicInteger> configSubscriber =
        new ConcurrentHashMap<>();
    
    /** 按 dataId@group@tenant 维度的配置变更 TopN 计数器 */
    private static StringTopNCounter configChangeCount = new StringTopNCounter();
    
    static {
        ImmutableTag immutableTag = new ImmutableTag("module", "config");
        
        List<Tag> tags = new ArrayList<>();
        tags.add(immutableTag);
        tags.add(new ImmutableTag("name", "getConfig"));
        NacosMeterRegistryCenter.gauge(METER_REGISTRY, "nacos_monitor", tags, getConfig);
        
        tags = new ArrayList<>();
        tags.add(immutableTag);
        tags.add(new ImmutableTag("name", "publish"));
        NacosMeterRegistryCenter.gauge(METER_REGISTRY, "nacos_monitor", tags, publish);
        
        tags = new ArrayList<>();
        tags.add(immutableTag);
        tags.add(new ImmutableTag("name", "longPolling"));
        NacosMeterRegistryCenter.gauge(METER_REGISTRY, "nacos_monitor", tags, longPolling);
        
        tags = new ArrayList<>();
        tags.add(immutableTag);
        tags.add(new ImmutableTag("name", "configCount"));
        NacosMeterRegistryCenter.gauge(METER_REGISTRY, "nacos_monitor", tags, configCount);
        
        tags = new ArrayList<>();
        tags.add(immutableTag);
        tags.add(new ImmutableTag("name", "notifyTask"));
        NacosMeterRegistryCenter.gauge(METER_REGISTRY, "nacos_monitor", tags, notifyTask);
        
        tags = new ArrayList<>();
        tags.add(immutableTag);
        tags.add(new ImmutableTag("name", "notifyClientTask"));
        NacosMeterRegistryCenter.gauge(METER_REGISTRY, "nacos_monitor", tags, notifyClientTask);
        
        tags = new ArrayList<>();
        tags.add(immutableTag);
        tags.add(new ImmutableTag("name", "dumpTask"));
        NacosMeterRegistryCenter.gauge(METER_REGISTRY, "nacos_monitor", tags, dumpTask);
        
        tags = new ArrayList<>();
        tags.add(immutableTag);
        tags.add(new ImmutableTag("name", "fuzzySearch"));
        NacosMeterRegistryCenter.gauge(METER_REGISTRY, "nacos_monitor", tags, fuzzySearch);
        
        configSubscriber.put("v1", new AtomicInteger(0));
        configSubscriber.put("v2", new AtomicInteger(0));
        
        tags = new ArrayList<>();
        tags.add(new ImmutableTag("version", "v1"));
        NacosMeterRegistryCenter.gauge(METER_REGISTRY, "nacos_config_subscriber", tags,
            configSubscriber.get("v1"));
        
        tags = new ArrayList<>();
        tags.add(new ImmutableTag("version", "v2"));
        NacosMeterRegistryCenter.gauge(METER_REGISTRY, "nacos_config_subscriber", tags,
            configSubscriber.get("v2"));
    }
    
    /** 获取拉配置任务 gauge 引用 */
    public static AtomicInteger getConfigMonitor() {
        return getConfig;
    }
    
    /** 获取发布任务 gauge 引用 */
    public static AtomicInteger getPublishMonitor() {
        return publish;
    }
    
    /** 获取 HTTP 长轮询通知任务 gauge 引用 */
    public static AtomicInteger getLongPollingMonitor() {
        return longPolling;
    }
    
    /** 获取配置 group 总数 gauge 引用 */
    public static AtomicInteger getConfigCountMonitor() {
        return configCount;
    }
    
    /** 获取集群通知任务 gauge 引用 */
    public static AtomicInteger getNotifyTaskMonitor() {
        return notifyTask;
    }
    
    /** 获取长连接客户端通知任务 gauge 引用 */
    public static AtomicInteger getNotifyClientTaskMonitor() {
        return notifyClientTask;
    }
    
    /** 获取 dump 任务 gauge 引用 */
    public static AtomicInteger getDumpTaskMonitor() {
        return dumpTask;
    }
    
    /** 获取模糊搜索计数 gauge 引用 */
    public static AtomicInteger getFuzzySearchMonitor() {
        return fuzzySearch;
    }
    
    /** 按版本获取订阅客户端数 gauge 引用 */
    public static AtomicInteger getConfigSubscriberMonitor(String version) {
        return configSubscriber.get(version);
    }
    
    /** 获取配置变更 TopN 计数器 */
    public static StringTopNCounter getConfigChangeCount() {
        return configChangeCount;
    }
    
    /** 读配置耗时 Timer */
    public static Timer getReadConfigRtTimer() {
        return NacosMeterRegistryCenter
            .timer(METER_REGISTRY, "nacos_timer", "module", "config", "name", "readConfigRt");
    }
    
    /** 写配置耗时 Timer */
    public static Timer getWriteConfigRtTimer() {
        return NacosMeterRegistryCenter
            .timer(METER_REGISTRY, "nacos_timer", "module", "config", "name", "writeConfigRt");
    }
    
    /** 配置变更通知耗时 Timer */
    public static Timer getNotifyRtTimer() {
        return NacosMeterRegistryCenter.timer(METER_REGISTRY, "nacos_timer", "module", "config",
            "name", "notifyRt");
    }
    
    /** 配置 dump 耗时 Timer */
    public static Timer getDumpRtTimer() {
        return NacosMeterRegistryCenter.timer(METER_REGISTRY, "nacos_timer", "module", "config",
            "name", "dumpRt");
    }
    
    /** 非法参数异常 Counter */
    public static Counter getIllegalArgumentException() {
        return NacosMeterRegistryCenter
            .counter(METER_REGISTRY, "nacos_exception", "module", "config", "name",
                "illegalArgument");
    }
    
    /** Nacos 业务异常 Counter */
    public static Counter getNacosException() {
        return NacosMeterRegistryCenter.counter(METER_REGISTRY, "nacos_exception", "module",
            "config", "name", "nacos");
    }
    
    /** 配置通知失败异常 Counter */
    public static Counter getConfigNotifyException() {
        return NacosMeterRegistryCenter
            .counter(METER_REGISTRY, "nacos_exception", "module", "config", "name", "configNotify");
    }
    
    /** 不健康状态异常 Counter */
    public static Counter getUnhealthException() {
        return NacosMeterRegistryCenter
            .counter(METER_REGISTRY, "nacos_exception", "module", "config", "name", "unhealth");
    }
    
    /**
     * 递增指定 tenant@group@dataId 的配置变更计数。
     *
     * @param tenant 命名空间
     * @param group  配置 group
     * @param dataId 配置 dataId
     */
    public static void incrementConfigChangeCount(String tenant, String group, String dataId) {
        configChangeCount.increment(tenant + "@" + group + "@" + dataId);
    }
}
