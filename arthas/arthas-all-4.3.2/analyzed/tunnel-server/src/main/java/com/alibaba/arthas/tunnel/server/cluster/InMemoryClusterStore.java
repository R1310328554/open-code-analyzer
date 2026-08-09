package com.alibaba.arthas.tunnel.server.cluster;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.cache.caffeine.CaffeineCache;

import com.alibaba.arthas.tunnel.server.AgentClusterInfo;

/**
 * 基于 Caffeine 本地缓存的 {@link TunnelClusterStore} 实现，适用于单机或小规模部署。
 *
 * @author hengyunabc 2020-12-02
 *
 */
public class InMemoryClusterStore implements TunnelClusterStore {
    private final static Logger logger = LoggerFactory.getLogger(InMemoryClusterStore.class);

    /** Spring Cache 抽象，底层为 Caffeine */
    private Cache cache;

    @Override
    public AgentClusterInfo findAgent(String agentId) {

        ValueWrapper valueWrapper = cache.get(agentId);
        if (valueWrapper == null) {
            return null;
        }

        AgentClusterInfo info = (AgentClusterInfo) valueWrapper.get();
        return info;
    }

    @Override
    public void removeAgent(String agentId) {
        cache.evict(agentId);
    }

    @Override
    public void addAgent(String agentId, AgentClusterInfo info, long timeout, TimeUnit timeUnit) {
        // Caffeine Cache 的过期策略由 CacheManager 配置，此处直接写入
        cache.put(agentId, info);
    }

    @Override
    public Collection<String> allAgentIds() {
        CaffeineCache caffeineCache = (CaffeineCache) cache;
        com.github.benmanes.caffeine.cache.Cache<Object, Object> nativeCache = caffeineCache.getNativeCache();
        return (Collection<String>) (Collection<?>) nativeCache.asMap().keySet();
    }

    @Override
    public Map<String, AgentClusterInfo> agentInfo(String appName) {
        CaffeineCache caffeineCache = (CaffeineCache) cache;
        com.github.benmanes.caffeine.cache.Cache<Object, Object> nativeCache = caffeineCache.getNativeCache();

        ConcurrentMap<String, AgentClusterInfo> map = (ConcurrentMap<String, AgentClusterInfo>) (ConcurrentMap<?, ?>) nativeCache
                .asMap();

        Map<String, AgentClusterInfo> result = new HashMap<String, AgentClusterInfo>();

        // agentId 格式为 appName_host，按前缀过滤
        String prefix = appName + "_";
        for (Entry<String, AgentClusterInfo> entry : map.entrySet()) {
            String agentId = entry.getKey();
            if (agentId.startsWith(prefix)) {
                result.put(agentId, entry.getValue());
            }
        }

        return result;

    }

    public Cache getCache() {
        return cache;
    }

    public void setCache(Cache cache) {
        this.cache = cache;
    }

}
