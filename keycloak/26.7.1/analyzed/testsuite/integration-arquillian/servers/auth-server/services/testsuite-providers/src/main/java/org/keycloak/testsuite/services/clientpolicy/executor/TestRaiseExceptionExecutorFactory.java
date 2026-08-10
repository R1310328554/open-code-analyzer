/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.testsuite.services.clientpolicy.executor;

import java.util.Collections;
import java.util.List;

import org.keycloak.Config;
import org.keycloak.Config.Scope;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.services.clientpolicy.executor.ClientPolicyExecutorProvider;
import org.keycloak.services.clientpolicy.executor.ClientPolicyExecutorProviderFactory;

/**
 * {@link TestRaiseExceptionExecutor} 的 SPI 工厂，用于集成测试注册可配置异常执行器。
 */
public class TestRaiseExceptionExecutorFactory implements ClientPolicyExecutorProviderFactory {

    /** 提供者标识符。 */
    public static final String PROVIDER_ID = "test-raise-exception";

    /** {@inheritDoc} 创建执行器实例。 */
    @Override
    public ClientPolicyExecutorProvider create(KeycloakSession session) {
        return new TestRaiseExceptionExecutor(session);
    }

    @Override
    public void init(Scope config) {
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
    }

    @Override
    public void close() {
    }

    /** {@inheritDoc} 返回 {@link #PROVIDER_ID}。 */
    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    /** {@inheritDoc} 无详细帮助文本。 */
    @Override
    public String getHelpText() {
        return "NA";
    }

    /** {@inheritDoc} 无额外配置属性。 */
    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return Collections.emptyList();
    }

    /** {@inheritDoc} 始终受支持。 */
    @Override
    public boolean isSupported(Config.Scope config) {
        return true;
    }
}