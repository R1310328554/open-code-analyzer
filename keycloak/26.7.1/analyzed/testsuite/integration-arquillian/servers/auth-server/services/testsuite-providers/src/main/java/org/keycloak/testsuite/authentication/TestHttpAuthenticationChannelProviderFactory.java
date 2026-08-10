/*
 *
 *  * Copyright 2021  Red Hat, Inc. and/or its affiliates
 *  * and other contributors as indicated by the @author tags.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.keycloak.testsuite.authentication;

import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.protocol.oidc.grants.ciba.channel.AuthenticationChannelProvider;
import org.keycloak.protocol.oidc.grants.ciba.channel.HttpAuthenticationChannelProvider;
import org.keycloak.protocol.oidc.grants.ciba.channel.HttpAuthenticationChannelProviderFactory;
import org.keycloak.testsuite.util.ServerURLs;

/**
 * 测试用 HTTP 认证通道提供者工厂，将 CIBA 认证通道指向集成测试应用的端点。
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class TestHttpAuthenticationChannelProviderFactory extends HttpAuthenticationChannelProviderFactory {

    /** 指向测试应用 OIDC 客户端端点的认证通道 URL。 */
    private static final String TEST_HTTP_AUTH_CHANNEL =
            String.format("%s://%s:%s/auth/realms/master/app/oidc-client-endpoints/request-authentication-channel",
                    ServerURLs.AUTH_SERVER_SCHEME, ServerURLs.AUTH_SERVER_HOST, ServerURLs.AUTH_SERVER_PORT);

    /** {@inheritDoc} 创建指向 {@link #TEST_HTTP_AUTH_CHANNEL} 的 HTTP 认证通道提供者。 */
    @Override
    public AuthenticationChannelProvider create(KeycloakSession session) {
        return new HttpAuthenticationChannelProvider(session, TEST_HTTP_AUTH_CHANNEL);
    }

    /** {@inheritDoc} 较高优先级，确保测试工厂优先于默认实现。 */
    @Override
    public int order() {
        return 100;
    }

    /** {@inheritDoc} 返回 {@code test-http-auth-channel} 标识符。 */
    @Override
    public String getId() {
        return "test-http-auth-channel";
    }

    /** {@inheritDoc} 测试环境始终启用该提供者。 */
    @Override
    public boolean isSupported(Config.Scope config) {
        return true;
    }
}
