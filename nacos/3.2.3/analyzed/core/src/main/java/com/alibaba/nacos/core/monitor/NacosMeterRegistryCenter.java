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

package com.alibaba.nacos.core.monitor;

import com.alibaba.nacos.core.utils.Loggers;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Nacos 指标注册中心，按业务域划分多个 {@link CompositeMeterRegistry}，统一对接 Micrometer 全局注册表。
 * <p>提供 counter、gauge、timer、summary 的便捷创建方法。</p>
 * Metrics unified usage center.
 *
 * @author <a href="mailto:liuyixiao0821@gmail.com">liuyixiao</a>
 * @author xiweng.yy
 */
@SuppressWarnings("all")
public final class NacosMeterRegistryCenter {
    
    // 稳定指标注册表（长期保留的 core/config/naming 等）
    /** Core 模块稳定指标注册表名。 */
    public static final String CORE_STABLE_REGISTRY = "CORE_STABLE_REGISTRY";
    
    /** Config 模块稳定指标注册表名。 */
    public static final String CONFIG_STABLE_REGISTRY = "CONFIG_STABLE_REGISTRY";
    
    /** Naming 模块稳定指标注册表名。 */
    public static final String NAMING_STABLE_REGISTRY = "NAMING_STABLE_REGISTRY";
    
    // 动态指标注册表（TopN 等可清理的临时指标）
    /** 配置变更 TopN 动态注册表名。 */
    public static final String TOPN_CONFIG_CHANGE_REGISTRY = "TOPN_CONFIG_CHANGE_REGISTRY";
    
    /** 服务变更 TopN 动态注册表名。 */
    public static final String TOPN_SERVICE_CHANGE_REGISTRY = "TOPN_SERVICE_CHANGE_REGISTRY";
    
    // 控制插件相关注册表
    /** 流控拒绝计数注册表名。 */
    public static final String CONTROL_DENIED_REGISTRY = "CONTROL_DENIED_REGISTRY";
    
    /** 分布式锁模块稳定指标注册表名。 */
    public static final String LOCK_STABLE_REGISTRY = "LOCK_STABLE_REGISTRY";
    
    /** 注册表名称 → CompositeMeterRegistry 映射。 */
    private static final ConcurrentHashMap<String, CompositeMeterRegistry> METER_REGISTRIES =
        new ConcurrentHashMap<>();
    
    /** Micrometer 全局注册表引用，初始化失败时为 null。 */
    private static CompositeMeterRegistry METER_REGISTRY = null;
    
    static {
        try {
            METER_REGISTRY = Metrics.globalRegistry;
        } catch (Throwable t) {
            Loggers.CORE.warn("Metrics init failed :", t);
        }
        registry(CORE_STABLE_REGISTRY, CONFIG_STABLE_REGISTRY, NAMING_STABLE_REGISTRY,
            TOPN_CONFIG_CHANGE_REGISTRY,
            TOPN_SERVICE_CHANGE_REGISTRY, CONTROL_DENIED_REGISTRY, LOCK_STABLE_REGISTRY);
        
    }
    
    /** 批量创建命名 CompositeMeterRegistry 并挂接到全局注册表。 */
    private static void registry(String... names) {
        for (String name : names) {
            CompositeMeterRegistry compositeMeterRegistry = new CompositeMeterRegistry();
            if (METER_REGISTRY != null) {
                compositeMeterRegistry.add(METER_REGISTRY);
            }
            METER_REGISTRIES.put(name, compositeMeterRegistry);
        }
    }
    
    /** 在指定注册表创建带 Tag 的 Counter。 */
    public static Counter counter(String registry, String name, Iterable<Tag> tags) {
        CompositeMeterRegistry compositeMeterRegistry = METER_REGISTRIES.get(registry);
        if (compositeMeterRegistry != null) {
            return METER_REGISTRIES.get(registry).counter(name, tags);
        }
        return null;
    }
    
    /** 在指定注册表创建 Counter（键值对形式 Tag）。 */
    public static Counter counter(String registry, String name, String... tags) {
        CompositeMeterRegistry compositeMeterRegistry = METER_REGISTRIES.get(registry);
        if (compositeMeterRegistry != null) {
            return METER_REGISTRIES.get(registry).counter(name, tags);
        }
        return null;
    }
    
    /** 在指定注册表注册 Gauge 并绑定可变数值对象。 */
    public static <T extends Number> T gauge(String registry, String name, Iterable<Tag> tags,
        T number) {
        CompositeMeterRegistry compositeMeterRegistry = METER_REGISTRIES.get(registry);
        if (compositeMeterRegistry != null) {
            return METER_REGISTRIES.get(registry).gauge(name, tags, number);
        }
        return null;
    }
    
    /** 在指定注册表创建 Timer 指标。 */
    public static Timer timer(String registry, String name, Iterable<Tag> tags) {
        CompositeMeterRegistry compositeMeterRegistry = METER_REGISTRIES.get(registry);
        if (compositeMeterRegistry != null) {
            return METER_REGISTRIES.get(registry).timer(name, tags);
        }
        return null;
    }
    
    /** 在指定注册表创建 Timer（键值对 Tag）。 */
    public static Timer timer(String registry, String name, String... tags) {
        CompositeMeterRegistry compositeMeterRegistry = METER_REGISTRIES.get(registry);
        if (compositeMeterRegistry != null) {
            return METER_REGISTRIES.get(registry).timer(name, tags);
        }
        return null;
    }
    
    /** 在指定注册表创建 DistributionSummary 指标。 */
    public static DistributionSummary summary(String registry, String name, Iterable<Tag> tags) {
        CompositeMeterRegistry compositeMeterRegistry = METER_REGISTRIES.get(registry);
        if (compositeMeterRegistry != null) {
            return METER_REGISTRIES.get(registry).summary(name, tags);
        }
        return null;
    }
    
    /** 在指定注册表创建 DistributionSummary（键值对 Tag）。 */
    public static DistributionSummary summary(String registry, String name, String... tags) {
        CompositeMeterRegistry compositeMeterRegistry = METER_REGISTRIES.get(registry);
        if (compositeMeterRegistry != null) {
            return METER_REGISTRIES.get(registry).summary(name, tags);
        }
        return null;
    }
    
    /** 清空指定注册表下所有已注册指标（TopN 等动态场景使用）。 */
    public static void clear(String registry) {
        METER_REGISTRIES.get(registry).clear();
    }
    
    /**
     * 仅供测试获取内部注册表，生产代码请勿通过此方法注册新指标。
     *
     * @param registry
     * @return CompositeMeterRegistry in NacosMeterRegistryCenter.
     */
    public static CompositeMeterRegistry getMeterRegistry(String registry) {
        return METER_REGISTRIES.get(registry);
    }
}
