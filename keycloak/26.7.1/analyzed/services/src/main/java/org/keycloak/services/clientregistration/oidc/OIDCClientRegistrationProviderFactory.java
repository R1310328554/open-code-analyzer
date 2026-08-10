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

package org.keycloak.services.clientregistration.oidc;

import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.services.clientregistration.ClientRegistrationProvider;
import org.keycloak.services.clientregistration.ClientRegistrationProviderFactory;

/**
 * {@link OIDCClientRegistrationProvider} 的 Provider 工厂。
 * <p>注册标识为 {@link #ID}（{@code openid-connect}）的 OIDC 动态客户端注册端点。</p>
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class OIDCClientRegistrationProviderFactory implements ClientRegistrationProviderFactory {

    /** Provider 标识符：OpenID Connect 协议 */
    public static final String ID = "openid-connect";

    /** @param session Keycloak 会话 @return 新的客户端注册 Provider 实例 */
    @Override
    public ClientRegistrationProvider create(KeycloakSession session) {
        return new OIDCClientRegistrationProvider(session);
    }

    /** 工厂初始化（无全局配置） */
    @Override
    public void init(Config.Scope config) {
    }

    /** 会话工厂就绪回调 */
    @Override
    public void postInit(KeycloakSessionFactory factory) {
    }

    /** 工厂关闭钩子 */
    @Override
    public void close() {
    }

    /** @return Provider 标识符 */
    @Override
    public String getId() {
        return ID;
    }

}
