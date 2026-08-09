package com.alibaba.arthas.tunnel.server.endpoint;

import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.alibaba.arthas.tunnel.server.app.configuration.ArthasProperties;

/**
 * Arthas Actuator 端点自动配置：暴露 {@link ArthasEndpoint} 供运维监控。
 */
@EnableConfigurationProperties(ArthasProperties.class)
@Configuration
public class ArthasEndPointAutoconfiguration {

    /** 在 Actuator 端点可用且未自定义 Bean 时注册 Arthas 监控端点 */
    @ConditionalOnMissingBean
    @Bean
    @ConditionalOnAvailableEndpoint
    public ArthasEndpoint arthasEndPoint() {
        return new ArthasEndpoint();
    }
}
