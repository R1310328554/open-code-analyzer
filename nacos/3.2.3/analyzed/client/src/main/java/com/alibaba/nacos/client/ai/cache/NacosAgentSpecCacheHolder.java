/*
 * Copyright 1999-2025 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.client.ai.cache;

import com.alibaba.nacos.api.ai.constant.AiConstants;
import com.alibaba.nacos.api.ai.model.agentspecs.AgentSpec;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.client.ai.event.AgentSpecChangedEvent;
import com.alibaba.nacos.client.ai.remote.AgentSpecQueryResponse;
import com.alibaba.nacos.client.ai.remote.AiClientProxy;
import com.alibaba.nacos.client.ai.utils.CacheKeyUtils;
import com.alibaba.nacos.client.env.NacosClientProperties;
import com.alibaba.nacos.client.utils.LogUtils;
import com.alibaba.nacos.common.executor.NameThreadFactory;
import com.alibaba.nacos.common.lifecycle.Closeable;
import com.alibaba.nacos.common.notify.NotifyCenter;
import com.alibaba.nacos.common.utils.StringUtils;
import org.slf4j.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Nacos AI 模块 AgentSpec 本地缓存持有者。
 *
 * <p>为每个订阅维护轮询循环，周期性调用
 * {@link AiClientProxy#queryAgentSpec(String, String, String, String)} 并携带本地 MD5
 * 进行条件查询。服务端返回 304（{@link NacosException#NOT_MODIFIED}）时保留本地缓存且不触发回调；
 * 内容变更（MD5 不同）时发布 {@link AgentSpecChangedEvent}，由 {@code AiChangeNotifier}
 * 分发给已注册监听器。</p>
 *
 * @author nacos
 */
public class NacosAgentSpecCacheHolder implements Closeable {
    
    private static final Logger LOGGER = LogUtils.logger(NacosAgentSpecCacheHolder.class);
    
    private final AiClientProxy aiClientProxy;
    
    /**
     * agentSpecName -> 上次发布的 MD5 指纹。
     */
    private final Map<String, String> md5Cache;
    
    /**
     * agentSpecName -> 缓存的 AgentSpec 对象。
     */
    private final Map<String, AgentSpec> agentSpecCache;
    
    private final ScheduledExecutorService updaterExecutor;
    
    private final long updateIntervalMillis;
    
    private final Map<String, AgentSpecUpdater> updateTaskMap;
    
    public NacosAgentSpecCacheHolder(AiClientProxy aiClientProxy,
        NacosClientProperties properties) {
        this.aiClientProxy = aiClientProxy;
        this.md5Cache = new ConcurrentHashMap<>(4);
        this.agentSpecCache = new ConcurrentHashMap<>(4);
        this.updateTaskMap = new ConcurrentHashMap<>(4);
        this.updaterExecutor = new ScheduledThreadPoolExecutor(1,
            new NameThreadFactory("com.alibaba.nacos.client.ai.agentspec.updater"));
        this.updateIntervalMillis = properties.getLong(
            AiConstants.AI_AGENTSPEC_CACHE_UPDATE_INTERVAL,
            AiConstants.DEFAULT_AI_CACHE_UPDATE_INTERVAL);
    }
    
    /**
     * 同步查询 AgentSpec（非订阅模式）。
     *
     * @param agentSpecName name of agent spec
     * @return AgentSpec object, null if not found
     * @throws NacosException if error occurs
     */
    public AgentSpec queryAgentSpec(String agentSpecName) throws NacosException {
        if (StringUtils.isBlank(agentSpecName)) {
            throw new NacosException(NacosException.INVALID_PARAM,
                "Required parameter `agentSpecName` not present");
        }
        try {
            AgentSpecQueryResponse response =
                aiClientProxy.queryAgentSpec(agentSpecName, null, null, null);
            return response.getAgentSpec();
        } catch (NacosException e) {
            if (e.getErrCode() == NacosException.NOT_FOUND) {
                return null;
            }
            throw e;
        }
    }
    
    /**
     * 订阅 AgentSpec 变更并启动轮询。
     *
     * <p>首次同步查询并填充 MD5 缓存；后续轮询携带 MD5 以跳过未变更内容。</p>
     *
     * @param agentSpecName name of agent spec
     * @return current AgentSpec object, null if not found
     * @throws NacosException if error occurs
     */
    public AgentSpec subscribeAgentSpec(String agentSpecName) throws NacosException {
        if (StringUtils.isBlank(agentSpecName)) {
            throw new NacosException(NacosException.INVALID_PARAM,
                "Required parameter `agentSpecName` not present");
        }
        String cacheKey = CacheKeyUtils.buildAgentSpecKey(agentSpecName);
        
        AgentSpec agentSpec = null;
        try {
            AgentSpecQueryResponse response =
                aiClientProxy.queryAgentSpec(agentSpecName, null, null, null);
            agentSpec = response.getAgentSpec();
            // 首次订阅仅更新缓存，不在此处发布事件；
            // 首次监听器通知由调用方 NacosAiService 负责。
            String newMd5 = response.getMd5();
            if (StringUtils.isNotBlank(newMd5)) {
                md5Cache.put(cacheKey, newMd5);
            }
        } catch (NacosException e) {
            if (e.getErrCode() != NacosException.NOT_FOUND) {
                throw e;
            }
            md5Cache.remove(cacheKey);
        }
        
        if (agentSpec != null) {
            agentSpecCache.put(cacheKey, agentSpec);
        }
        addUpdateTask(agentSpecName);
        LOGGER.info("Subscribed agent spec: {}", agentSpecName);
        return agentSpec;
    }
    
    /**
     * 取消 AgentSpec 订阅并清理缓存与轮询任务。
     *
     * @param agentSpecName name of agent spec
     */
    public void unsubscribeAgentSpec(String agentSpecName) {
        if (StringUtils.isBlank(agentSpecName)) {
            return;
        }
        String cacheKey = CacheKeyUtils.buildAgentSpecKey(agentSpecName);
        removeUpdateTask(agentSpecName);
        md5Cache.remove(cacheKey);
        agentSpecCache.remove(cacheKey);
        LOGGER.info("Unsubscribed agent spec: {}", agentSpecName);
    }
    
    @Override
    public void shutdown() throws NacosException {
        this.updaterExecutor.shutdownNow();
    }
    
    /** 为指定 AgentSpec 添加定时轮询任务。 */
    private void addUpdateTask(String agentSpecName) {
        String key = CacheKeyUtils.buildAgentSpecKey(agentSpecName);
        this.updateTaskMap.computeIfAbsent(key, s -> {
            AgentSpecUpdater task = new AgentSpecUpdater(agentSpecName);
            updaterExecutor.schedule(task, updateIntervalMillis, TimeUnit.MILLISECONDS);
            return task;
        });
    }
    
    /** 移除 AgentSpec 轮询任务。 */
    private void removeUpdateTask(String agentSpecName) {
        String key = CacheKeyUtils.buildAgentSpecKey(agentSpecName);
        AgentSpecUpdater task = this.updateTaskMap.remove(key);
        if (task != null) {
            task.cancel();
        }
    }
    
    /** 处理轮询响应，更新 MD5/对象缓存并在变更时发布事件。 */
    private void processAgentSpec(String agentSpecName, String cacheKey,
        AgentSpecQueryResponse response) {
        String oldMd5 = md5Cache.get(cacheKey);
        String newMd5 = response == null ? null : response.getMd5();
        if (response == null) {
            md5Cache.remove(cacheKey);
            agentSpecCache.remove(cacheKey);
        } else if (StringUtils.isNotBlank(newMd5)) {
            md5Cache.put(cacheKey, newMd5);
            agentSpecCache.put(cacheKey, response.getAgentSpec());
        }
        if (response != null && !StringUtils.equals(oldMd5, newMd5)) {
            NotifyCenter.publishEvent(
                new AgentSpecChangedEvent(agentSpecName, response.getAgentSpec()));
        }
    }
    
    /** 定时条件查询 AgentSpec 的内部轮询任务。 */
    private class AgentSpecUpdater implements Runnable {
        
        private final String agentSpecName;
        
        private final String cacheKey;
        
        private final AtomicBoolean cancel = new AtomicBoolean(false);
        
        AgentSpecUpdater(String agentSpecName) {
            this.agentSpecName = agentSpecName;
            this.cacheKey = CacheKeyUtils.buildAgentSpecKey(agentSpecName);
        }
        
        void cancel() {
            cancel.set(true);
        }
        
        @Override
        public void run() {
            if (cancel.get()) {
                return;
            }
            try {
                String currentMd5 = md5Cache.get(cacheKey);
                AgentSpecQueryResponse response =
                    aiClientProxy.queryAgentSpec(agentSpecName, null, null, currentMd5);
                processAgentSpec(agentSpecName, cacheKey, response);
            } catch (NacosException e) {
                if (e.getErrCode() == NacosException.NOT_FOUND) {
                    processAgentSpec(agentSpecName, cacheKey, null);
                } else if (e.getErrCode() == NacosException.NOT_MODIFIED) {
                    // 内容未变更，保留本地缓存并跳过回调。
                } else {
                    LOGGER.warn(
                        "AgentSpec updater query failed: name={}, err={}",
                        agentSpecName, e.getErrMsg());
                }
            } finally {
                if (!cancel.get()) {
                    updaterExecutor.schedule(this, updateIntervalMillis,
                        TimeUnit.MILLISECONDS);
                }
            }
        }
    }
}
