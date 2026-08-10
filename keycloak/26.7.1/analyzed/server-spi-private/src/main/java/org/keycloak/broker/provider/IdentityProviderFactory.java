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
package org.keycloak.broker.provider;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.provider.ConfiguredProvider;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderFactory;

/**
 * 身份联邦提供者工厂 SPI，扩展 {@link ProviderFactory} 与 {@link ConfiguredProvider}。
 * <p>负责创建 {@link IdentityProvider} 实例、解析导入配置及提供管理控制台配置属性。</p>
 *
 * @author Pedro Igor
 */
public interface IdentityProviderFactory<T extends IdentityProvider> extends ProviderFactory<T>, ConfiguredProvider {

    /**
     * 工厂在管理控制台中的友好显示名称。
     *
     * <p>A friendly name for this factory.</p>
     *
     * @return
     */
    String getName();

    /**
     * 根据 {@link IdentityProviderModel} 创建身份提供者实例。
     *
     * <p>Creates an {@link IdentityProvider} based on the configuration contained in
     * <code>model</code>.</p>
     *
     * @param session
     * @param model The configuration to be used to create the identity provider.
     * @return
     */
    T create(KeycloakSession session, IdentityProviderModel model);

    /**
     * 解析配置字符串为键值映射（用于导入/迁移）。
     *
     * <p>Creates an {@link IdentityProvider} based on the configuration from
     * <code>inputStream</code>.</p>
     *
     * @param session
     * @param config The configuration for the provider
     * @return
     */
    Map<String, String> parseConfig(KeycloakSession session, String config);

    /**
     * 创建提供者专用的 {@link IdentityProviderModel} 子类实例以支持配置校验。
     *
     * <p>Creates a provider specific {@link IdentityProviderModel} instance.
     * 
     * <p>Providers may want to implement their own {@link IdentityProviderModel} type so that validations
     * can be performed when managing the provider configuration
     * 
     * @return the provider specific instance
     */
    IdentityProviderModel createConfig();

    /** 管理控制台可编辑的配置属性列表；默认空。 */
    default List<ProviderConfigProperty> getConfigProperties() {
        return Collections.emptyList();
    }

    /** 配置页帮助文本；默认空字符串。 */
    default String getHelpText() { return ""; }
}
