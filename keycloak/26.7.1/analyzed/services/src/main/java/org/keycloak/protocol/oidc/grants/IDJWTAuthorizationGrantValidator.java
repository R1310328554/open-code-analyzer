/*
 * Copyright 2025 Red Hat, Inc. and/or its affiliates
 *  and other contributors as indicated by the @author tags.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.keycloak.protocol.oidc.grants;


import org.keycloak.authentication.authenticators.client.ClientAssertionState;
import org.keycloak.models.KeycloakSession;
import org.keycloak.representations.JsonWebToken;

import org.jboss.logging.Logger;

/**
 * 身份断言 JWT 授权模式（ID-JAG）断言校验器。
 * <p>Identity Assertion JWT 是 RFC 7523 框架下可用作授权 grant 的新型 JWT。</p>
 * <p>草案：https://datatracker.ietf.org/doc/draft-ietf-oauth-identity-assertion-authz-grant/</p>
 *
 * @author <a href="mailto:yutaka.obuchi.sd@hitachi.com">Yutaka Obuchi</a>
 */
public class IDJWTAuthorizationGrantValidator extends DefaultJWTAuthorizationGrantValidator {

    private static final Logger logger = Logger.getLogger(IDJWTAuthorizationGrantValidator.class);

    /** 创建 ID-JAG 断言校验器 */
    public static IDJWTAuthorizationGrantValidator createValidator(KeycloakSession session, String scope, ClientAssertionState clientAssertionState) {
        return new IDJWTAuthorizationGrantValidator(session, scope, clientAssertionState);
    }

    private IDJWTAuthorizationGrantValidator(KeycloakSession session, String scope, ClientAssertionState clientAssertionState) {
        super(session, scope, clientAssertionState);
    }

    /** 在默认客户端校验基础上，比对断言与请求中的 client_id */
    public void validateClient() {
        super.validateClient();

        JsonWebToken accessToken = clientAssertionState.getToken();
        String clientIdInToken = (String) accessToken.getOtherClaims().get("client_id");
        String clientIdInRequestHeaderOrBody = session.getContext().getClient().getClientId();
        if (clientIdInToken == null || !clientIdInRequestHeaderOrBody.equals(clientIdInToken)) {
                logger.warn("client id in assertion : " + clientIdInToken + " and client id in request header/body : " + clientIdInRequestHeaderOrBody);
                failureCallback("client id in assertion : " + clientIdInToken + " and client id in request header/body : " + clientIdInRequestHeaderOrBody);
                return;
        }
    }

    /**
     * 校验令牌时效；ID-JAG 不允许断言重用。
     * @param allowedClockSkew 允许的时钟偏差
     * @param maxExp 最大过期时间
     * @param reusePermitted 是否允许重用（本实现忽略为 true 的配置）
     * @return 校验是否通过
     */
    public boolean validateTokenActive(int allowedClockSkew, int maxExp, boolean reusePermitted) {

        JsonWebToken accessToken = clientAssertionState.getToken();
        if (accessToken.getIat() == null) {
            failureCallback("Token iat claim is required");
            return false;
        }

        if (reusePermitted) {
            logger.warn("Token reuse is not permitted. Token reuse permitted setting is ignored.");            
        }

        return super.validateTokenActive(allowedClockSkew, maxExp, false);

    }

}
