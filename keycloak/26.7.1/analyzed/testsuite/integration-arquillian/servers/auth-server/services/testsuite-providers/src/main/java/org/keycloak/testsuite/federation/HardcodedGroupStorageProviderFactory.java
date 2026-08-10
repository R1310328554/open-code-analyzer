/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates
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
import org.keycloak.models.KeycloakSession;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;
import org.keycloak.storage.group.GroupStorageProviderFactory;
import org.keycloak.storage.group.GroupStorageProviderModel;

/**
 * 硬编码组存储提供者工厂，注册 {@code hardcoded-group} 测试组件。
 */
public class HardcodedGroupStorageProviderFactory implements GroupStorageProviderFactory<HardcodedGroupStorageProvider> {
    @Override
    public HardcodedGroupStorageProvider create(KeycloakSession session, ComponentModel model) {
        return new HardcodedGroupStorageProvider(new GroupStorageProviderModel(model));
    }

    /** 提供者标识符。 */
    public static final String PROVIDER_ID = "hardcoded-group";
    /** 配置项：硬编码组名（键名保留历史拼写 gorup_name）。 */
    public static final String GROUP_NAME = "gorup_name";
    /** 配置项：搜索是否延迟 5 秒。 */
    public static final String DELAYED_SEARCH = "delayed_search";

    /** 静态配置属性列表。 */
    protected static final List<ProviderConfigProperty> CONFIG_PROPERTIES;

    // 初始化组存储测试组件配置
    static {
        CONFIG_PROPERTIES = ProviderConfigurationBuilder.create()
                .property().name(GROUP_NAME)
                .type(ProviderConfigProperty.STRING_TYPE)
                .label("Hardcoded Group Name")
                .helpText("Only this group name is available for lookup")
                .defaultValue("hardcoded-group")
                .add()
                .property().name(DELAYED_SEARCH)
                .type(ProviderConfigProperty.BOOLEAN_TYPE)
                .label("Delayes provider by 5s.")
                .helpText("If true it delayes search for clients within the provider by 5s.")
                .defaultValue("false")
                .add()
                .build();
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return CONFIG_PROPERTIES;
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}
