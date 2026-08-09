package com.alibaba.arthas.tunnel.server.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import com.alibaba.arthas.tunnel.server.app.configuration.ArthasProperties;

/**
 * Tunnel Server Web 安全配置：Actuator 需登录，其余放行；可按配置允许 iframe 嵌入。
 *
 * @author hengyunabc 2021-08-11
 *
 */
@Configuration
public class WebSecurityConfig {

    @Autowired
    ArthasProperties arthasProperties;

    /** 配置过滤链：保护监控端点，可选关闭 X-Frame-Options 以支持 iframe */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.authorizeHttpRequests(authorize -> authorize
                .requestMatchers(EndpointRequest.toAnyEndpoint()).authenticated().anyRequest().permitAll())
                .formLogin(Customizer.withDefaults());
        // 允许在 iframe 中嵌入控制台页面
        if (arthasProperties.isEnableIframeSupport()) {
            httpSecurity.headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable()));
        }
        return httpSecurity.build();
    }
}
