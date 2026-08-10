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
import com.alibaba.nacos.api.ai.model.a2a.AgentCardDetailInfo;
import com.alibaba.nacos.api.ai.model.a2a.AgentInterface;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.client.ai.event.AgentCardChangedEvent;
import com.alibaba.nacos.client.ai.remote.AiGrpcClient;
import com.alibaba.nacos.client.ai.utils.CacheKeyUtils;
import com.alibaba.nacos.client.env.NacosClientProperties;
import com.alibaba.nacos.common.executor.NameThreadFactory;
import com.alibaba.nacos.common.lifecycle.Closeable;
import com.alibaba.nacos.common.notify.NotifyCenter;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.alibaba.nacos.common.utils.JacksonUtils;
import com.alibaba.nacos.common.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Nacos AI 模块 Agent Card 本地缓存持有者。
 *
 * <p>维护 Agent 卡片详情缓存，定时轮询服务端变更并通过
 * {@link AgentCardChangedEvent} 通知监听器。</p>
 *
 * @author xiweng.yy
 */
public class NacosAgentCardCacheHolder implements Closeable {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(NacosAgentCardCacheHolder.class);
    
    /** gRPC 客户端，用于拉取 Agent Card 详情。 */
    private final AiGrpcClient aiGrpcClient;
    
    /** Agent Card 缓存，键为 {@link CacheKeyUtils} 生成的复合键。 */
    private final Map<String, AgentCardDetailInfo> agentCardCache;
    
    /** 定时轮询更新任务的调度线程池。 */
    private final ScheduledExecutorService updaterExecutor;
    
    /** 轮询更新间隔（毫秒）。 */
    private final long updateIntervalMillis;
    
    /** 活跃更新任务映射，键为 Agent Card 缓存键。 */
    private final Map<String, AgentCardUpdater> updateTaskMap;
    
    /** 构造缓存持有者并读取轮询间隔配置。 */
    public NacosAgentCardCacheHolder(AiGrpcClient aiGrpcClient, NacosClientProperties properties) {
        this.aiGrpcClient = aiGrpcClient;
        this.agentCardCache = new ConcurrentHashMap<>(4);
        this.updateTaskMap = new ConcurrentHashMap<>(4);
        this.updaterExecutor = new ScheduledThreadPoolExecutor(1,
            new NameThreadFactory("com.alibaba.nacos.client.ai.agent.card.updater"));
        this.updateIntervalMillis =
            properties.getLong(AiConstants.AI_AGENT_CARD_CACHE_UPDATE_INTERVAL,
                AiConstants.DEFAULT_AI_CACHE_UPDATE_INTERVAL);
    }
    
    /** 从本地缓存读取 Agent Card 详情，未命中返回 null。 */
    public AgentCardDetailInfo getAgentCard(String agentName, String version) {
        String key = CacheKeyUtils.buildAgentCardKey(agentName, version);
        return agentCardCache.get(key);
    }
    
    /**
     * 处理服务端推送或轮询得到的 Agent Card 详情。
     *
     * @param detailInfo new agent card detail info
     */
    public void processAgentCardDetailInfo(AgentCardDetailInfo detailInfo) {
        String agentName = detailInfo.getName();
        String version = detailInfo.getVersion();
        Boolean isLatest = detailInfo.isLatestVersion();
        String key = CacheKeyUtils.buildAgentCardKey(agentName, version);
        AgentCardDetailInfo oldAgentCard = agentCardCache.get(key);
        agentCardCache.put(key, detailInfo);
        if (null != isLatest && isLatest) {
            String latestVersionKey = CacheKeyUtils.buildAgentCardKey(agentName, null);
            agentCardCache.put(latestVersionKey, detailInfo);
        }
        if (isAgentCardChanged(oldAgentCard, detailInfo)) {
            LOGGER.info("agent card {} changed, from {} -> {}.", detailInfo.getName(),
                JacksonUtils.toJson(oldAgentCard), JacksonUtils.toJson(detailInfo));
            NotifyCenter.publishEvent(new AgentCardChangedEvent(detailInfo));
        }
    }
    
    /**
     * 为指定 Agent Card 添加定时轮询更新任务。
     *
     * @param agentName name of agent card
     * @param version version of agent card
     */
    public void addAgentCardUpdateTask(String agentName, String version) {
        String agentCardKey = CacheKeyUtils.buildAgentCardKey(agentName, version);
        this.updateTaskMap.computeIfAbsent(agentCardKey, s -> {
            AgentCardUpdater updateTask = new AgentCardUpdater(agentName, version);
            updaterExecutor.schedule(updateTask, updateIntervalMillis, TimeUnit.MILLISECONDS);
            return updateTask;
        });
    }
    
    /**
     * 移除 Agent Card 的定时轮询更新任务。
     *
     * @param agentName name of agent card
     * @param version version of agent card
     */
    public void removeAgentCardUpdateTask(String agentName, String version) {
        String agentNameKey = CacheKeyUtils.buildAgentCardKey(agentName, version);
        AgentCardUpdater updateTask = this.updateTaskMap.remove(agentNameKey);
        if (null != updateTask) {
            updateTask.cancel();
        }
    }
    
    /** 比较新旧 Agent Card 是否发生实质变更。 */
    private boolean isAgentCardChanged(AgentCardDetailInfo oldAgentCard,
        AgentCardDetailInfo newAgentCard) {
        if (null == oldAgentCard) {
            LOGGER.info("init new agent card: {} -> {}", newAgentCard.getName(),
                JacksonUtils.toJson(newAgentCard));
            return true;
        }
        if (!Objects.equals(oldAgentCard.getVersion(), newAgentCard.getVersion())) {
            return true;
        }
        return isInterfacesChanged(oldAgentCard, newAgentCard);
    }
    
    /** 比较 supportedInterfaces 或 additionalInterfaces 是否变更。 */
    private boolean isInterfacesChanged(AgentCardDetailInfo oldAgentCard,
        AgentCardDetailInfo newAgentCard) {
        List<AgentInterface> oldSupported = oldAgentCard.getSupportedInterfaces();
        List<AgentInterface> newSupported = newAgentCard.getSupportedInterfaces();
        boolean oldHasSupported = !CollectionUtils.isEmpty(oldSupported);
        boolean newHasSupported = !CollectionUtils.isEmpty(newSupported);
        if (oldHasSupported || newHasSupported) {
            if (oldHasSupported != newHasSupported) {
                return true;
            }
            return !CollectionUtils.isEqualCollection(oldSupported, newSupported);
        }
        List<AgentInterface> oldInterfaces = oldAgentCard.getAdditionalInterfaces();
        List<AgentInterface> newInterfaces = newAgentCard.getAdditionalInterfaces();
        if (Objects.isNull(oldInterfaces) && Objects.isNull(newInterfaces)) {
            return !Objects.equals(oldAgentCard.getUrl(), newAgentCard.getUrl());
        }
        if (Objects.isNull(oldInterfaces) || Objects.isNull(newInterfaces)) {
            return true;
        }
        return !CollectionUtils.isEqualCollection(oldInterfaces, newInterfaces);
    }
    
    @Override
    public void shutdown() throws NacosException {
        this.updaterExecutor.shutdownNow();
    }
    
    /** 定时拉取 Agent Card 并触发变更检测的内部任务。 */
    private class AgentCardUpdater implements Runnable {
        
        private final String agentName;
        
        private final String version;
        
        private final AtomicBoolean cancel;
        
        public AgentCardUpdater(String agentName, String version) {
            this.agentName = agentName;
            this.version = version;
            this.cancel = new AtomicBoolean(false);
        }
        
        @Override
        public void run() {
            if (cancel.get()) {
                return;
            }
            try {
                AgentCardDetailInfo detailInfo =
                    aiGrpcClient.getAgentCard(agentName, version, StringUtils.EMPTY);
                processAgentCardDetailInfo(detailInfo);
            } catch (Exception e) {
                if (e instanceof NacosException) {
                    NacosException nacosException = (NacosException) e;
                    if (nacosException.getErrCode() == NacosException.NOT_FOUND) {
                        return;
                    }
                }
                LOGGER.warn("AgentCard updater execute query failed", e);
            } finally {
                if (!cancel.get()) {
                    updaterExecutor.schedule(this, updateIntervalMillis, TimeUnit.MILLISECONDS);
                }
            }
        }
        
        public void cancel() {
            cancel.set(true);
        }
    }
}
