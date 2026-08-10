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

package org.keycloak.services.clientpolicy.condition;

import java.util.LinkedList;
import java.util.List;

import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.protocol.LoginProtocol;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;

/**
 * {@link ClientProtocolCondition} 的 SPI 工厂：从已注册 {@link LoginProtocol} 提供方动态填充协议选项。
 *
 * @author rmartinc
 */
public class ClientProtocolConditionFactory implements ClientPolicyConditionProviderFactory {

    /** 客户端策略条件提供方 ID（历史命名 client-type） */
    public static final String PROVIDER_ID = "client-type";

    /** postInit 时收集的可用登录协议 ID 列表 */
    private List<String> loginProtocols;

    /** @param session Keycloak 会话 @return ClientProtocolCondition 实例 */
    @Override
    public ClientPolicyConditionProvider create(KeycloakSession session) {
        return new ClientProtocolCondition(session);
    }

    /** 扫描并缓存所有 LoginProtocol 提供方 ID @param factory 会话工厂 */
    @Override
    public void postInit(KeycloakSessionFactory factory) {
        try (KeycloakSession session = factory.create()) {
            loginProtocols = new LinkedList<>(session.listProviderIds(LoginProtocol.class));
        }
    }

    /** @return 提供方 ID */
    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    /** @return 条件帮助说明 */
    @Override
    public String getHelpText() {
        return "Condition that uses the client's protocol (OpenID Connect, SAML) to determine whether the policy is applied.";
    }

    /** SPI 初始化（无配置） @param config 配置作用域 */
    @Override
    public void init(Config.Scope config) {
        // 无操作
    }

    /** 关闭工厂（无资源需释放） */
    @Override
    public void close() {
        // 无操作
    }

    /** @return 动态构建的协议下拉配置项 */
    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return ProviderConfigurationBuilder.create()
                .property()
                .name("protocol")
                .type(ProviderConfigProperty.LIST_TYPE)
                .options(loginProtocols)
                .defaultValue(loginProtocols.iterator().next())
                .label("Client protocol")
                .helpText("What client login protocol the condition will apply on.")
                .add()
                .build();
    }
}
