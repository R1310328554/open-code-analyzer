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

package org.keycloak.testsuite.actions;

import java.util.List;
import java.util.Map;

import org.keycloak.Config;
import org.keycloak.authentication.RequiredActionContext;
import org.keycloak.authentication.RequiredActionFactory;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.RequiredActionConfigModel;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;

/**
 * 可配置必需操作工厂，用于集成测试验证 {@link RequiredActionConfigModel} 配置读写。
 */
public class DummyConfigurableRequiredActionFactory implements RequiredActionFactory {

    /** 提供者在 SPI 中的标识符。 */
    public static final String PROVIDER_ID = "configurable-test-action";

    /** 字符串类型配置项 setting1 的键名。 */
    public static final String SETTING_1 = "setting1";

    /** 布尔类型配置项 setting2 的键名。 */
    public static final String SETTING_2 = "setting2";

    /** {@inheritDoc} 管理控制台展示名称。 */
    @Override
    public String getDisplayText() {
        return "Configurable Test Action";
    }

    /** {@inheritDoc} 创建匿名 {@link RequiredActionProvider} 实例。 */
    @Override
    public RequiredActionProvider create(KeycloakSession session) {
        return new RequiredActionProvider() {
            @Override
            public void evaluateTriggers(RequiredActionContext context) {

            }

            @Override
            public void requiredActionChallenge(RequiredActionContext context) {

                // 通过 RequiredActionContext#getConfig() 读取必需操作配置
                RequiredActionConfigModel configModel = context.getConfig();
                Map<String, String> config = configModel.getConfig();

                String setting1Value = configModel.getConfigValue(SETTING_1);

                // 读取配置后直接标记挑战成功
                context.success();
            }

            @Override
            public void processAction(RequiredActionContext context) {
            }

            @Override
            public void close() {

            }
        };
    }

    @Override
    public void init(Config.Scope config) {

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

    /** 管理 UI 可编辑的配置项元数据列表。 */
    private static final List<ProviderConfigProperty> CONFIG_PROPERTIES = ProviderConfigurationBuilder.create() //
            .property() //
            .name(SETTING_1) //
            .label("Setting 1") //
            .helpText("Setting 1 Help Text") //
            .type(ProviderConfigProperty.STRING_TYPE) //
            .defaultValue("setting1Default") //
            .add() //

            .property() //
            .name(SETTING_2) //
            .label("Setting 2") //
            .helpText("Setting 2 Help Text") //
            .type(ProviderConfigProperty.BOOLEAN_TYPE) //
            .defaultValue("true") //
            .add() //

            .build();

    /** {@inheritDoc} 返回 {@link #CONFIG_PROPERTIES}。 */
    @Override
    public List<ProviderConfigProperty> getConfigMetadata() {
        return CONFIG_PROPERTIES;
    }
}
