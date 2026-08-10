/*
 * Copyright 2023 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.testsuite.broker.oidc;

import java.util.Collections;
import java.util.Set;

import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;

import org.keycloak.OAuth2Constants;
import org.keycloak.authentication.ClientAuthenticationFlowContext;
import org.keycloak.authentication.authenticators.client.ClientAuthUtil;
import org.keycloak.authentication.authenticators.client.JWTClientAuthenticator;

/**
 * 要求提供 {@code client_id} 参数的 {@link JWTClientAuthenticator} 变体，用于测试套件。
 *
 * @author Justin Tay
 */
public class ClientIdRequiredJWTClientAuthenticator extends JWTClientAuthenticator {

    /** 测试套件客户端认证器提供商标识符。 */
    public static final String PROVIDER_ID = "testsuite-client-id-required";

    /**
     * {@inheritDoc}
     * 在调用父类 JWT 认证前校验表单参数中是否包含 {@code client_id}。
     */
    @Override
    public void authenticateClient(ClientAuthenticationFlowContext context) {
        MultivaluedMap<String, String> params = context.getHttpRequest().getDecodedFormParameters();

        String clientId = params.getFirst(OAuth2Constants.CLIENT_ID);
        if (clientId == null) {
            Response challengeResponse = ClientAuthUtil.errorResponse(Response.Status.BAD_REQUEST.getStatusCode(), "invalid_client", "Missing client_id parameter");
            context.challenge(challengeResponse);
            return;
        }

        super.authenticateClient(context);
    }

    /** {@inheritDoc} 返回 {@link #PROVIDER_ID}。 */
    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    /**
     * {@inheritDoc}
     * 返回空集合，避免影响 well-known 端点测试。
     */
    @Override
    public Set<String> getProtocolAuthenticatorMethods(String loginProtocol) {
        // 不注册为协议认证方法，以免影响 well-known 提供者测试
        return Collections.emptySet();
    }
}
