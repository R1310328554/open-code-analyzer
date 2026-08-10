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

package com.alibaba.nacos.prometheus.conf;

import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.core.web.NacosWebBean;
import com.alibaba.nacos.plugin.auth.constant.Constants;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import static com.alibaba.nacos.prometheus.api.ApiConstants.PROMETHEUS_CONTROLLER_NAMESPACE_PATH;
import static com.alibaba.nacos.prometheus.api.ApiConstants.PROMETHEUS_CONTROLLER_PATH;
import static com.alibaba.nacos.prometheus.api.ApiConstants.PROMETHEUS_CONTROLLER_SERVICE_PATH;

/**
 * Prometheus 端点 Spring Security 配置。
 *
 * <p>在未启用 Nacos 认证插件时，将 Prometheus SD 路径设为 {@code permitAll}， 避免全局 Security 拦截 metrics 拉取；启用认证时由 {@link com.alibaba.nacos.prometheus.filter.PrometheusAuthFilter} 处理。</p>
 *
 * @author vividfish
 */
@Configuration
@NacosWebBean
@ConditionalOnProperty(name = "nacos.prometheus.metrics.enabled", havingValue = "true")
public class PrometheusSecurityConfiguration {
    
    /** 无认证插件时对 Prometheus 路径放行。 */
    @Bean
    @Conditional(ConditionOnNoAuthPluginType.class)
    public SecurityFilterChain prometheusSecurityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(
            (authorizeHttpRequests) -> authorizeHttpRequests
                .requestMatchers(PROMETHEUS_CONTROLLER_PATH,
                    PROMETHEUS_CONTROLLER_NAMESPACE_PATH, PROMETHEUS_CONTROLLER_SERVICE_PATH)
                .permitAll());
        return http.getOrBuild();
    }
    
    /** 当 {@code nacos.core.auth.system.type} 为空时匹配（未配置认证插件）。 */
    private static class ConditionOnNoAuthPluginType implements Condition {
        
        @Override
        /** 检测环境是否未指定 Nacos 认证系统类型。 */
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            String nacosAuthSystemType = context.getEnvironment()
                .getProperty(Constants.Auth.NACOS_CORE_AUTH_SYSTEM_TYPE, "");
            return StringUtils.isBlank(nacosAuthSystemType);
        }
    }
}
