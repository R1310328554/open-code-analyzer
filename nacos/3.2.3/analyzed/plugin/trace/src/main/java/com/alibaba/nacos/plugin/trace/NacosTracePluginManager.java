/*
 * Copyright 1999-2021 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.plugin.trace;

import com.alibaba.nacos.api.plugin.PluginStateChecker;
import com.alibaba.nacos.api.plugin.PluginStateCheckerHolder;
import com.alibaba.nacos.api.plugin.PluginType;
import com.alibaba.nacos.common.spi.NacosServiceLoader;
import com.alibaba.nacos.plugin.trace.spi.NacosTraceSubscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Nacos 链路追踪事件订阅者管理器。
 *
 * <p>通过 SPI 加载 {@link NacosTraceSubscriber} 实现，按插件名索引，
 * 并支持按插件启用状态过滤订阅者。</p>
 *
 * @author xiweng.yy
 */
public class NacosTracePluginManager {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(NacosTracePluginManager.class);
    
    /** 单例实例。 */
    private static final NacosTracePluginManager INSTANCE = new NacosTracePluginManager();
    
    /** 插件名 → 追踪订阅者实例。 */
    private final Map<String, NacosTraceSubscriber> traceSubscribers;
    
    private NacosTracePluginManager() {
        this.traceSubscribers = new ConcurrentHashMap<>();
        Collection<NacosTraceSubscriber> plugins =
            NacosServiceLoader.load(NacosTraceSubscriber.class);
        for (NacosTraceSubscriber each : plugins) {
            this.traceSubscribers.put(each.getName(), each);
            LOGGER.info("[TracePluginManager] Load NacosTraceSubscriber({}) name({}) successfully.",
                each.getClass(),
                each.getName());
        }
    }
    
    /**
     * 获取管理器单例。
     *
     * @return 单例实例
     */
    public static NacosTracePluginManager getInstance() {
        return INSTANCE;
    }
    
    /**
     * 获取所有已启用的追踪订阅者。
     *
     * <p>若存在 {@link PluginStateChecker}，则过滤掉被禁用的插件。</p>
     *
     * @return 已启用的订阅者集合
     */
    public Collection<NacosTraceSubscriber> getAllTraceSubscribers() {
        Optional<PluginStateChecker> checker = PluginStateCheckerHolder.getInstance();
        if (checker.isPresent()) {
            return traceSubscribers.values().stream()
                .filter(subscriber -> {
                    boolean enabled = checker.get().isPluginEnabled(PluginType.TRACE.getType(),
                        subscriber.getName());
                    if (!enabled) {
                        LOGGER.debug("[TracePluginManager] Plugin TRACE:{} is disabled",
                            subscriber.getName());
                    }
                    return enabled;
                })
                .collect(Collectors.toSet());
        }
        return new HashSet<>(traceSubscribers.values());
    }
    
    /**
     * 获取全部追踪订阅者（不做启用状态过滤）。
     *
     * @return unmodifiable map of all trace subscribers
     */
    public Map<String, NacosTraceSubscriber> getAllPlugins() {
        return Collections.unmodifiableMap(traceSubscribers);
    }
}
