/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.testsuite.federation;

import java.util.List;

import org.keycloak.component.ComponentModel;
import org.keycloak.component.ComponentValidationException;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;
import org.keycloak.storage.UserStorageProviderFactory;

/**
 * {@link FailingUserStorageProvider} 的工厂，用于在测试中模拟用户存储失败以验证优雅降级。
 */
public class FailingUserStorageProviderFactory implements UserStorageProviderFactory<FailingUserStorageProvider> {
    
    /** 提供者唯一标识符。 */
    public static final String PROVIDER_ID = "failing-user-storage";
    
    /** {@inheritDoc} 创建可配置失败行为的用户存储提供者。 */
    @Override
    public FailingUserStorageProvider create(KeycloakSession session, ComponentModel model) {
        return new FailingUserStorageProvider(session, model);
    }
    
    /** {@inheritDoc} 返回工厂标识。 */
    @Override
    public String getId() {
        return PROVIDER_ID;
    }
    
    /** {@inheritDoc} 返回提供者说明文本。 */
    @Override
    public String getHelpText() {
        return "Test user storage provider that can be configured to fail for testing graceful degradation";
    }
    
    /** {@inheritDoc} 返回搜索与计数失败开关的配置属性。 */
    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return ProviderConfigurationBuilder.create()
            .property()
                .name(FailingUserStorageProvider.FAIL_ON_SEARCH)
                .label("Fail on Search")
                .helpText("If enabled, this provider will throw exceptions during user search operations")
                .type(ProviderConfigProperty.BOOLEAN_TYPE)
                .defaultValue(false)
                .add()
            .property()
                .name(FailingUserStorageProvider.FAIL_ON_COUNT)
                .label("Fail on Count")
                .helpText("If enabled, this provider will throw exceptions during user count operations")
                .type(ProviderConfigProperty.BOOLEAN_TYPE)
                .defaultValue(false)
                .add()
            .build();
    }
    
    /** {@inheritDoc} 测试提供者无需额外配置校验。 */
    @Override
    public void validateConfiguration(KeycloakSession session, RealmModel realm, ComponentModel config) throws ComponentValidationException {
        // 测试提供者无需校验
    }
}
