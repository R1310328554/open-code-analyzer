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

import com.alibaba.nacos.plugin.auth.impl.oidc.controller.OidcLoginController;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * OIDC 认证插件 Spring 自动配置。
 *
 * <p>当 {@code nacos.core.auth.system.type=oidc} 时注册登录控制器与安全配置。</p>
 *
 * @author WangzJi
 */
@Configuration
@ConditionalOnProperty(name = "nacos.core.auth.system.type", havingValue = "oidc")
@Import(OidcWebSecurityConfig.class)
@SuppressWarnings("PMD")
public class OidcPluginAutoConfiguration {
    
    /**
     * 注册 {@link OidcLoginController} Bean。
     *
     * @return OidcLoginController 实例
     */
    @Bean
    public OidcLoginController oidcLoginController() {
        return new OidcLoginController();
    }
}
