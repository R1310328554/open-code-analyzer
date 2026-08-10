/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.keycloak.services.managers;

import java.util.List;
import java.util.Optional;

import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;

/**
 * 默认暴力破解保护器工厂。
 * <p>根据 {@code allowConcurrentRequests} 配置创建 {@link DefaultBruteForceProtector} 或 {@link DefaultBlockingBruteForceProtector}。</p>
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class DefaultBruteForceProtectorFactory implements BruteForceProtectorFactory {
    /** 单例暴力破解保护器实例（postInit 后可用） */
    DefaultBruteForceProtector protector;

    /** 是否允许同一用户并发登录请求（默认 false，使用阻塞式保护器） */
    private boolean allowConcurrentRequests;

    /** {@inheritDoc} 返回已初始化的保护器单例 */
    @Override
    public BruteForceProtector create(KeycloakSession session) {
        return protector;
    }

    /** {@inheritDoc} 读取 {@code allowConcurrentRequests} 配置 */
    @Override
    public void init(Config.Scope config) {
        // 是否允许并发请求（暴力破解相关配置）
        this.allowConcurrentRequests = config.getBoolean("allowConcurrentRequests", Boolean.FALSE);
    }

    /** {@inheritDoc} 按配置实例化默认或阻塞式保护器 */
    @Override
    public void postInit(KeycloakSessionFactory factory) {
        protector = allowConcurrentRequests ? new DefaultBruteForceProtector(factory) : new DefaultBlockingBruteForceProtector(factory);
    }

    /** {@inheritDoc} 关闭保护器 executor */
    @Override
    public void close() {
        Optional.ofNullable(protector).ifPresent(DefaultBruteForceProtector::shutdown);
    }

    /** {@inheritDoc} 返回 {@code default-brute-force-detector} */
    @Override
    public String getId() {
        return "default-brute-force-detector";
    }

    /** {@inheritDoc} 返回 {@code allowConcurrentRequests} 配置元数据 */
    @Override
    public List<ProviderConfigProperty> getConfigMetadata() {
        return ProviderConfigurationBuilder.create()
                .property()
                .name("allowConcurrentRequests")
                .type("boolean")
                .helpText("If concurrent logins are allowed by the brute force protection.")
                .defaultValue(false)
                .add()
                .build();
    }
}
