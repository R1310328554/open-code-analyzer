/*
 * Copyright 2025 Red Hat, Inc. and/or its affiliates
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

import java.util.Map;

import org.keycloak.OAuth2Constants;
import org.keycloak.OAuthErrorException;
import org.keycloak.jose.jws.JWSInput;
import org.keycloak.jose.jws.JWSInputException;
import org.keycloak.models.KeycloakSession;
import org.keycloak.protocol.oidc.JWTAuthorizationGrantValidationContext;
import org.keycloak.protocol.oidc.TokenExchangeContext;
import org.keycloak.representations.idm.ClientPolicyExecutorConfigurationRepresentation;
import org.keycloak.services.clientpolicy.ClientPolicyContext;
import org.keycloak.services.clientpolicy.ClientPolicyException;
import org.keycloak.services.clientpolicy.context.JWTAuthorizationGrantContext;
import org.keycloak.services.clientpolicy.context.TokenExchangeRequestContext;
import org.keycloak.utils.StringUtil;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;

/**
 * JWT 声明（claim）强制执行器。
 * <p>在 JWT 授权授予（RFC 7523）或标准令牌交换请求中，校验传入 JWT 必须包含指定 claim，且其值（可选）须匹配配置的正则表达式；仅支持 string/number 类型 claim。</p>
 */
public class JWTClaimEnforcerExecutor implements ClientPolicyExecutorProvider<JWTClaimEnforcerExecutor.Configuration> {

    /** Keycloak 会话 */
    private final KeycloakSession session;
    /** 执行器运行时配置 */
    private Configuration configuration;

    /** @param session Keycloak 会话 */
    public JWTClaimEnforcerExecutor(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public void setupConfiguration(JWTClaimEnforcerExecutor.Configuration config) {
        this.configuration = config;
    }

    @Override
    public String getProviderId() {
        return JWTClaimEnforcerExecutorFactory.PROVIDER_ID;
    }

    @Override
    public Class<Configuration> getExecutorConfigurationClass() {
        return Configuration.class;
    }

    /** JWT claim 校验配置项 */
    public static class Configuration extends ClientPolicyExecutorConfigurationRepresentation {

        /** 待校验的 JWT claim 名称 */
        @JsonProperty("claim-name")
        protected String claimName;

        /** claim 允许值正则；为空时仅校验 claim 存在 */
        @JsonProperty("allowed-value")
        protected String allowedValue;

        public String getClaimName() {
            return claimName;
        }

        public void setClaimName(String claimName) {
            this.claimName = claimName;
        }

        public String getAllowedValue() {
            return allowedValue;
        }

        public void setAllowedValue(String allowedValue) {
            this.allowedValue = allowedValue;
        }
    }

    /** 在 JWT 授权授予或令牌交换事件中校验 claim */
    @Override
    public void executeOnEvent(ClientPolicyContext context) throws ClientPolicyException {
        // 按事件类型解析 JWT 并校验 claim
        switch (context.getEvent()) {
            case JWT_AUTHORIZATION_GRANT -> {
                JWTAuthorizationGrantContext jwtAuthnGrantContext = ((JWTAuthorizationGrantContext) context);
                JWTAuthorizationGrantValidationContext jwtContext = jwtAuthnGrantContext.getAuthorizationGrantContext();
                checkClaims(getAccessTokenMapFromJWTString(jwtContext.getAssertion()));
            }
            case TOKEN_EXCHANGE_REQUEST -> {
                TokenExchangeContext tokenExchangeContext = ((TokenExchangeRequestContext) context).getTokenExchangeContext();
                if (!OAuth2Constants.ACCESS_TOKEN_TYPE.equals(tokenExchangeContext.getParams().getSubjectTokenType())) {
                    throw new ClientPolicyException(OAuthErrorException.INVALID_REQUEST, "Parameter 'subject_token' should be access_token for the executor");
                }
                checkClaims(getAccessTokenMapFromJWTString(tokenExchangeContext.getParams().getSubjectToken()));
            }
        }
    }

    /** 将 JWT 字符串解析为 claim 映射 */
    private  Map<String, Object> getAccessTokenMapFromJWTString(String jwt) throws ClientPolicyException {
        try {
            return new  JWSInput(jwt).readJsonContent(new TypeReference<>() {});
        } catch (JWSInputException e) {
            throw new ClientPolicyException(OAuthErrorException.INVALID_REQUEST, "JWT is not valid");
        }
    }

    /** 按配置校验 token 中指定 claim 的存在性与取值 */
    private void checkClaims(Map<String, Object> tokenMap) throws ClientPolicyException {
        String claimName = configuration.getClaimName();
        // 校验 claim-name 已配置
        if (claimName == null) {
            throw new ClientPolicyException(OAuthErrorException.INVALID_REQUEST,  "Invalid configuration");
        }

        String allowedValue = configuration.getAllowedValue();

        // 提取 claim 值
        Object claimValue = tokenMap.get(claimName);
        if (claimValue == null) {
            throw new ClientPolicyException(OAuthErrorException.INVALID_REQUEST, "Required claim '" + claimName + "' is missing from the token");
        }

        // allowedValue 为空时仅要求 claim 存在
        if (StringUtil.isBlank(allowedValue)) {
            return;
        }

        // 仅允许 string 或 number 类型
        if (!isAllowedClaimType(claimValue)) {
            throw new ClientPolicyException(OAuthErrorException.INVALID_REQUEST, "Value type for claim '" + claimName + "' not allowed");
        }

        String stringValue = String.valueOf(claimValue);

        if (!stringValue.matches(allowedValue)) {
            throw new ClientPolicyException(OAuthErrorException.INVALID_REQUEST, "Value for claim '" + claimName + "' not allowed");
        }
    }

    /** 判断 claim 值类型是否为 string 或 number */
    private boolean isAllowedClaimType(Object claimValue) {
        return claimValue instanceof String || claimValue instanceof Number;
    }
}
