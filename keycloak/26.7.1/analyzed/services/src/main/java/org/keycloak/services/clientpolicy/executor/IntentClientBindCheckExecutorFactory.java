/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.services.clientpolicy.executor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.keycloak.Config.Scope;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;

/**
 * {@link IntentClientBindCheckExecutor} 的 Provider 工厂。
 * <p>注册 Intent 与客户端绑定校验执行器，并暴露外部校验端点配置项。</p>
 */
public class IntentClientBindCheckExecutorFactory implements ClientPolicyExecutorProviderFactory {

    /** 执行器 Provider 标识符 */
    public static final String PROVIDER_ID = "intent-client-bind-checker";

    /** 配置键：Intent 绑定校验 HTTP 端点 URL */
    public static final String INTENT_CLIENT_BIND_CHECK_ENDPOINT = "intent-client-bind-check-endpoint";

    /** 管理控制台中的端点 URL 配置属性定义 */
    private static final ProviderConfigProperty INTENT_CLIENT_BIND_CHECK_ENDPOINT_PROPERTY = new ProviderConfigProperty(
            INTENT_CLIENT_BIND_CHECK_ENDPOINT, "Intent Client Bind Check Endpoint", "Endpoint for checking if openbanking_intent_id is bound with a client.",
            ProviderConfigProperty.STRING_TYPE, "https://rs.keycloak-fapi.org/check-intent-client-bound");

    /** @param session Keycloak 会话 @return 新的执行器实例 */
    @Override
    public ClientPolicyExecutorProvider create(KeycloakSession session) {
        return new IntentClientBindCheckExecutor(session);
    }

    /** 工厂初始化（无全局配置） */
    @Override
    public void init(Scope config) {
    }

    /** 会话工厂就绪回调 */
    @Override
    public void postInit(KeycloakSessionFactory factory) {
    }

    /** 工厂关闭钩子 */
    @Override
    public void close() {
    }

    /** @return 执行器标识 {@link #PROVIDER_ID} */
    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    /** @return 执行器说明（英文原文保留） */
    @Override
    public String getHelpText() {
        return "The executor checks if openbanking_intent_id is bound with a client.";
    }

    /** @return 可配置属性列表 */
    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return new ArrayList<>(Arrays.asList(INTENT_CLIENT_BIND_CHECK_ENDPOINT_PROPERTY));
    }

}
