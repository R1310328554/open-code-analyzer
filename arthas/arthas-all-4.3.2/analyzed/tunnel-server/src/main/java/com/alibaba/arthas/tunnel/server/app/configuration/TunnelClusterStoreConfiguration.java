package com.alibaba.arthas.tunnel.server.app.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.alibaba.arthas.tunnel.server.app.configuration.TunnelClusterStoreConfiguration.RedisTunnelClusterStoreConfiguration;
import com.alibaba.arthas.tunnel.server.cluster.InMemoryClusterStore;
import com.alibaba.arthas.tunnel.server.cluster.RedisTunnelClusterStore;
import com.alibaba.arthas.tunnel.server.cluster.TunnelClusterStore;

/**
 * Tunnel 集群存储自动配置：按缓存类型或 Redis 可用性选择 {@link TunnelClusterStore} 实现。
 *
 * @author hengyunabc 2020-10-29
 *
 */
@Configuration
@AutoConfigureAfter(value = { RedisAutoConfiguration.class, CacheAutoConfiguration.class })
@Import(RedisTunnelClusterStoreConfiguration.class)
public class TunnelClusterStoreConfiguration {

    /**
     * 使用 Caffeine 本地缓存作为集群存储（单机或小规模部署）。
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "spring.cache.type", havingValue = "caffeine")
    public TunnelClusterStore tunnelClusterStore(@Autowired CacheManager cacheManager) {
        Cache inMemoryClusterCache = cacheManager.getCache("inMemoryClusterCache");
        InMemoryClusterStore inMemoryClusterStore = new InMemoryClusterStore();
        inMemoryClusterStore.setCache(inMemoryClusterCache);
        return inMemoryClusterStore;
    }

    /** Redis 版集群存储的独立配置类，避免与 Caffeine Bean 冲突 */
    static class RedisTunnelClusterStoreConfiguration {
        /**
         * 配置了 Redis 主机时使用 Redis 持久化 agent 路由信息，支持多 Tunnel Server 集群。
         */
        @Bean
        // @ConditionalOnBean(StringRedisTemplate.class)
        @ConditionalOnClass(StringRedisTemplate.class)
        @ConditionalOnProperty("spring.data.redis.host")
        @ConditionalOnMissingBean
        public TunnelClusterStore tunnelClusterStore(@Autowired StringRedisTemplate redisTemplate) {
            RedisTunnelClusterStore redisTunnelClusterStore = new RedisTunnelClusterStore();
            redisTunnelClusterStore.setRedisTemplate(redisTemplate);
            return redisTunnelClusterStore;
        }
    }

}
