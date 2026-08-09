package com.alibaba.arthas.tunnel.server.app.configuration;

import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.alibaba.arthas.tunnel.server.app.configuration.ArthasProperties.EmbeddedRedis;

import redis.embedded.RedisServer;
import redis.embedded.RedisServerBuilder;

/**
 * 内嵌 Redis 自动配置：在测试或单机场景下按需启动 embedded-redis 实例。
 *
 * @author hengyunabc 2020-11-03
 *
 */
@Configuration
@AutoConfigureBefore(TunnelClusterStoreConfiguration.class)
public class EmbeddedRedisConfiguration {

    /**
     * 创建并启动内嵌 Redis，供 {@link TunnelClusterStoreConfiguration} 中的 Redis 集群存储使用。
     */
    @Bean(initMethod = "start", destroyMethod = "stop")
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "arthas", name = { "embedded-redis.enabled" })
    public RedisServer embeddedRedisServer(ArthasProperties arthasProperties) {
        EmbeddedRedis embeddedRedis = arthasProperties.getEmbeddedRedis();

        RedisServerBuilder builder = RedisServer.builder().port(embeddedRedis.getPort()).bind(embeddedRedis.getHost());

        // 应用用户自定义的 Redis 启动参数
        for (String setting : embeddedRedis.getSettings()) {
            builder.setting(setting);
        }
        RedisServer redisServer = builder.build();
        return redisServer;
    }
}
