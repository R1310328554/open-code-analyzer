/*
 * Copyright 1999-2024 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.plugin.auth.impl.oidc.config;

import com.alibaba.nacos.core.web.NacosWebBean;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * OIDC 认证插件的 Spring Security 配置。
 *
 * <p>在 Spring Security 层放行所有路径，实际鉴权由 Nacos 自有过滤器完成。</p>
 *
 * @author WangzJi
 */
@Configuration
@NacosWebBean
@EnableWebSecurity
@ConditionalOnProperty(name = "nacos.core.auth.system.type", havingValue = "oidc")
public class OidcWebSecurityConfig {
    
    /**
     * 配置 OIDC 模式下的安全过滤器链。
     *
     * <p>所有路径在 Spring Security 层均 permitAll，鉴权逻辑由 Nacos 认证插件接管。</p>
     *
     * @param http HttpSecurity 构建器
     * @return SecurityFilterChain 实例
     * @throws Exception 配置失败时抛出
     */
    @Bean
    @Order(1)
    public SecurityFilterChain oidcSecurityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(
            (authorizeHttpRequests) -> authorizeHttpRequests.requestMatchers("/**").permitAll());
        http.csrf(AbstractHttpConfigurer::disable);
        return http.build();
    }
}
