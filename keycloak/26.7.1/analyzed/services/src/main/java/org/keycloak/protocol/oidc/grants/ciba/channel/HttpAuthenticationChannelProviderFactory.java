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
 *
 */
package org.keycloak.protocol.oidc.grants.ciba.channel;

import java.util.List;

import org.keycloak.Config.Scope;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;

/**
 * HTTP 认证通道 Provider 工厂：注册 {@link HttpAuthenticationChannelProvider} 并暴露 URI 配置项。
 *
 * @author <a href="mailto:takashi.norimatsu.ws@hitachi.com">Takashi Norimatsu</a>
 */
public class HttpAuthenticationChannelProviderFactory implements AuthenticationChannelProviderFactory {

    /** Provider 标识符 */
    public static final String PROVIDER_ID = "ciba-http-auth-channel";

    /** 配置的认证通道 URI */
    protected String httpAuthenticationChannelUri;

    /** 创建 HTTP 认证通道 Provider 实例 */
    @Override
    public AuthenticationChannelProvider create(KeycloakSession session) {
        return new HttpAuthenticationChannelProvider(session, httpAuthenticationChannelUri);
    }

    /** 从服务器配置读取 httpAuthenticationChannelUri */
    @Override
    public void init(Scope config) {
        httpAuthenticationChannelUri = config.get("httpAuthenticationChannelUri");
    }

    /** 会话工厂初始化后回调（无额外逻辑） */
    @Override
    public void postInit(KeycloakSessionFactory factory) {
    }

    /** 工厂关闭钩子 */
    @Override
    public void close() {
    }

    /** @return Provider ID */
    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    /** @return 管理控制台可配置的 URI 属性 */
    @Override
    public List<ProviderConfigProperty> getConfigMetadata() {
        return ProviderConfigurationBuilder.create()
                .property()
                .name("httpAuthenticationChannelUri")
                .type("string")
                .helpText("The HTTP(S) URI of the authentication channel.")
                .add()
                .build();
    }
}
