/*
 * Copyright 2023 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.authentication.authenticators.client;

import java.util.List;

import jakarta.ws.rs.core.Response;

import org.keycloak.OAuth2Constants;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.ClientAuthenticationFlowContext;
import org.keycloak.models.ClientModel;
import org.keycloak.models.RealmModel;
import org.keycloak.representations.JsonWebToken;

import org.jboss.logging.Logger;

/**
 * Common validation for JWT client authentication with private_key_jwt or with client_secret
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public abstract class AbstractJWTClientValidator extends AbstractBaseJWTValidator {

    private static final Logger logger = Logger.getLogger(AbstractJWTClientValidator.class);

    /** 客户端认证流程上下文。 */
    protected final ClientAuthenticationFlowContext context;
    /** 当前领域模型。 */
    protected final RealmModel realm;
    /** 签名验证策略。 */
    protected final SignatureValidator signatureValidator;
    /** 期望的客户端认证器 Provider ID。 */
    protected final String clientAuthenticatorProviderId;
    /** 期望的 client_assertion_type（默认 JWT）。 */
    protected String expectedClientAssertionType = OAuth2Constants.CLIENT_ASSERTION_TYPE_JWT;

    /**
     * @param context 客户端认证流程上下文
     * @param signatureValidator 签名验证策略
     * @param clientAuthenticatorProviderId 期望的认证器 Provider ID
     */
    public AbstractJWTClientValidator(ClientAuthenticationFlowContext context, SignatureValidator signatureValidator, String clientAuthenticatorProviderId) throws Exception {
        super(context.getSession(), context.getState(ClientAssertionState.class, ClientAssertionState.supplier()));
        this.context = context;
        this.realm = context.getRealm();
        this.signatureValidator = signatureValidator;
        this.clientAuthenticatorProviderId = clientAuthenticatorProviderId;
    }

    /** @return 客户端认证流程上下文 */
    public ClientAuthenticationFlowContext getContext() {
        return context;
    }

    /** @return 已解析的客户端模型 */
    public ClientModel getClient() {
        return clientAssertionState.getClient();
    }

    /** 执行完整的 JWT 客户端断言校验链。 */
    public boolean validate() {
        return validateClientAssertionParameters() &&
                validateClient() &&
                validateSignatureAlgorithm(getExpectedSignatureAlgorithm()) &&
                validateSignature() &&
                validateTokenAudience(getExpectedAudiences(), isMultipleAudienceAllowed()) &&
                validateTokenActive(getAllowedClockSkew(), getMaximumExpirationTime(), isReusePermitted());
    }

    /** 校验 client_assertion_type 与 client_assertion 参数。 */
    protected boolean validateClientAssertionParameters() {
        String clientAssertionType = clientAssertionState.getClientAssertionType();
        String clientAssertion = clientAssertionState.getClientAssertion();

        if (clientAssertionType == null) {
            return failure("Parameter client_assertion_type is missing");
        }

        if (!expectedClientAssertionType.equals(clientAssertionType)) {
            return failure("Parameter client_assertion_type has value '"
                    + clientAssertionType + "' but expected is '" + expectedClientAssertionType + "'");
        }

        if (clientAssertion == null) {
            return failure("client_assertion parameter missing");
        }

        return true;
    }

    /** 校验 sub/iss、客户端存在性、启用状态及认证器配置。 */
    protected boolean validateClient() {
        JsonWebToken token = clientAssertionState.getToken();

        String clientId = token.getSubject();
        if (clientId == null) {
            logger.debug("Can't identify client. Subject missing on JWT token");
            return failure("Token sub claim is required");
        }

        String expectedTokenIssuer = getExpectedTokenIssuer();
        if (expectedTokenIssuer != null && !expectedTokenIssuer.equals(token.getIssuer())) {
            return false;
        }

        ClientModel client = clientAssertionState.getClient();

        if (client == null) {
            return failure(AuthenticationFlowError.CLIENT_NOT_FOUND);
        }

        String clientIdParam = context.getHttpRequest().getDecodedFormParameters().getFirst(OAuth2Constants.CLIENT_ID);
        if (clientIdParam != null && !clientIdParam.equals(client.getClientId())) {
            return failure("client_id parameter does not match authenticated client");
        }

        context.getEvent().client(client.getClientId());
        context.setClient(client);

        if (!client.isEnabled()) {
            return failure(AuthenticationFlowError.CLIENT_DISABLED);
        }

        if (clientAuthenticatorProviderId != null && !clientAuthenticatorProviderId.equals(client.getClientAuthenticatorType())) {
            logger.debug("Not configured authenticator for client, ignoring");
            return false;
        }

        return true;
    }

    /** 委托 {@link SignatureValidator} 验证 JWS 签名。 */
    protected boolean validateSignature() {
        return signatureValidator.verifySignature(this);
    }

    /** 以 400 状态返回 invalid_client 错误。 */
    public boolean failure(String errorDescription) {
        return failure(errorDescription, Response.Status.BAD_REQUEST.getStatusCode());
    }

    public boolean failure(String errorDescription, int statusCode) {
        return failure("invalid_client", errorDescription, statusCode);
    }

    public boolean failure(String error, String errorDescription, int statusCode) {
        Response challengeResponse = ClientAuthUtil.errorResponse(statusCode, error, errorDescription);
        return failure(AuthenticationFlowError.INVALID_CLIENT_CREDENTIALS, challengeResponse);
    }

    protected boolean failure(AuthenticationFlowError error) {
        return failure(error, null);
    }

    protected boolean failure(AuthenticationFlowError error, Response response) {
        context.failure(error, response);
        return false;
    }

    @Override
    protected void failureCallback(String errorDescription) {
        failure(errorDescription);
    }

    /** @return 期望的令牌 iss 声明 */
    protected abstract String getExpectedTokenIssuer();

    /** @return 期望的 aud 受众列表 */
    protected abstract List<String> getExpectedAudiences();

    protected abstract boolean isMultipleAudienceAllowed();

    protected abstract int getAllowedClockSkew();

    protected abstract int getMaximumExpirationTime();

    protected abstract boolean isReusePermitted();

    /** @return 期望的 JWS 签名算法 */
    protected abstract String getExpectedSignatureAlgorithm();

    /** JWT 签名验证策略接口。 */
    public interface SignatureValidator {

        /** 验证客户端断言 JWS 签名。 */
        boolean verifySignature(AbstractJWTClientValidator validator);

    }

}
