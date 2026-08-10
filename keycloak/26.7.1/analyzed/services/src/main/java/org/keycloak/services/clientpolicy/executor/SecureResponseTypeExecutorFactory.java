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

package org.keycloak.services.clientpolicy.executor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.keycloak.Config.Scope;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;

/**
 * {@link SecureResponseTypeExecutor} 的 Provider 工厂。
 * <p>暴露自动配置与是否允许 token 响应类型等配置项。</p>
 *
 * @author <a href="mailto:takashi.norimatsu.ws@hitachi.com">Takashi Norimatsu</a>
 */
public class SecureResponseTypeExecutorFactory implements ClientPolicyExecutorProviderFactory {

    /** 执行器 Provider 标识符 */
    public static final String PROVIDER_ID = "secure-response-type";

    /** 配置键：是否自动配置 ID Token 分离签名 */
    public static final String AUTO_CONFIGURE = "auto-configure";
    /** 配置键：混合流是否允许返回 access token */
    public static final String ALLOW_TOKEN_RESPONSE_TYPE = "allow-token-response-type";

    private static final ProviderConfigProperty AUTO_CONFIGURE_PROPERTY = new ProviderConfigProperty(
            AUTO_CONFIGURE, "Auto-configure", "If On, then the during client creation or update, the configuration of the client will be auto-configured to use ID token returned from authorization endpoint as detached signature.", ProviderConfigProperty.BOOLEAN_TYPE, false);
    private static final ProviderConfigProperty ALLOW_TOKEN_RESPONSE_TYPE_PROPERTY = new ProviderConfigProperty(
            ALLOW_TOKEN_RESPONSE_TYPE, "Allow-token-response-type", "If On, then it allows an access token returned from authorization endpoint in hybrid flow.", ProviderConfigProperty.BOOLEAN_TYPE, false);

    /** @param session Keycloak 会话 @return 新的执行器实例 */
    @Override
    public ClientPolicyExecutorProvider create(KeycloakSession session) {
        return new SecureResponseTypeExecutor(session);
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
        return "The executor checks whether the client sent its authorization request with code id_token or code id_token token in its response type depending on its setting.";
    }

    /** @return 可配置属性列表 */
    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return new ArrayList<>(Arrays.asList(AUTO_CONFIGURE_PROPERTY, ALLOW_TOKEN_RESPONSE_TYPE_PROPERTY));
    }

}
