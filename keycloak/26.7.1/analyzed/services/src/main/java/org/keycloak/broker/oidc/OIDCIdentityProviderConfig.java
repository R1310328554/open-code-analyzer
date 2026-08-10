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
package org.keycloak.broker.oidc;

import org.keycloak.broker.jwtauthorizationgrant.JWTAuthorizationGrantConfig;
import org.keycloak.common.enums.SslRequired;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.IdentityProviderType;
import org.keycloak.models.RealmModel;

import static org.keycloak.common.util.UriUtils.checkUrl;

/**
 * OIDC 身份代理配置：签名校验、JWKS、登出与 JWT Authorization Grant 选项。
 * <p>实现 {@link JWTAuthorizationGrantConfig} 与 {@link IssuerValidation}。</p>
 * @author Pedro Igor
 */
public class OIDCIdentityProviderConfig extends OAuth2IdentityProviderConfig implements JWTAuthorizationGrantConfig, IssuerValidation {

    /** 配置键：JWKS 端点 URL。 */
    public static final String JWKS_URL = "jwksUrl";

    public static final String USE_JWKS_URL = "useJwksUrl";
    /** 配置键：是否校验 ID/Access Token 签名。 */
    public static final String VALIDATE_SIGNATURE = "validateSignature";
    /** 配置键：access token 是否为 JWT。 */
    public static final String IS_ACCESS_TOKEN_JWT = "isAccessTokenJWT";
    public static final String SUPPORTS_CLIENT_ASSERTIONS = "supportsClientAssertions";
    public static final String SUPPORTS_CLIENT_ASSERTION_REUSE = "supportsClientAssertionReuse";
    public static final String ALLOW_CLIENT_ID_AS_AUDIENCE = "allowClientIdAsAudience";

    public OIDCIdentityProviderConfig(IdentityProviderModel identityProviderModel) {
        super(identityProviderModel);
    }

    public OIDCIdentityProviderConfig() {
        super();
    }

    public void setPrompt(String prompt) {
        getConfig().put("prompt", prompt);
    }

    public String getLogoutUrl() {
        return getConfig().get("logoutUrl");
    }
    public void setLogoutUrl(String url) {
        getConfig().put("logoutUrl", url);
    }

    public boolean isSendClientIdOnLogout() {
        return Boolean.parseBoolean(getConfig().getOrDefault("sendClientIdOnLogout", Boolean.FALSE.toString()));
    }

    public void setSendClientOnLogout(boolean value) {
        getConfig().put("sendClientIdOnLogout", String.valueOf(value));
    }

    public boolean isSendIdTokenOnLogout() {
        return Boolean.parseBoolean(getConfig().getOrDefault("sendIdTokenOnLogout", Boolean.TRUE.toString()));
    }

    public void setSendIdTokenOnLogout(boolean value) {
        getConfig().put("sendIdTokenOnLogout", String.valueOf(value));
    }

    /** @return 是否启用 token 签名校验 */
    public boolean isValidateSignature() {
        return Boolean.parseBoolean(getConfig().get("validateSignature"));
    }

    public void setValidateSignature(boolean validateSignature) {
        getConfig().put(VALIDATE_SIGNATURE, String.valueOf(validateSignature));
    }

    public void setAccessTokenJwt(boolean accessTokenJwt) {
        getConfig().put(IS_ACCESS_TOKEN_JWT, String.valueOf(accessTokenJwt));
    }

    /** @return access token 是否按 JWT 解析 */
    public boolean isAccessTokenJwt() {
        return Boolean.parseBoolean(getConfig().get(IS_ACCESS_TOKEN_JWT));
    }

    public boolean isBackchannelSupported() {
        return Boolean.parseBoolean(getConfig().get("backchannelSupported"));
    }

    public void setBackchannelSupported(boolean backchannel) {
        getConfig().put("backchannelSupported", String.valueOf(backchannel));
    }

    /** @return 是否禁用 UserInfo 端点调用 */
    public boolean isDisableUserInfoService() {
        String disableUserInfo = getConfig().get("disableUserInfo");
        return Boolean.parseBoolean(disableUserInfo);
    }

    public void setDisableUserInfoService(boolean disable) {
        getConfig().put("disableUserInfo", String.valueOf(disable));
    }

    /** @return 是否禁用 nonce 校验 */
    public boolean isDisableNonce() {
        return Boolean.parseBoolean(getConfig().get("disableNonce"));
    }

    public void setDisableNonce(boolean disableNonce) {
        if (disableNonce) {
            getConfig().put("disableNonce", Boolean.TRUE.toString());
        } else {
            getConfig().remove("disableNonce");
        }
    }

    /** @return token 校验允许的时钟偏差（秒） */
    public int getAllowedClockSkew() {
        String allowedClockSkew = getConfig().get(ALLOWED_CLOCK_SKEW);
        if (allowedClockSkew == null || allowedClockSkew.isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(getConfig().get(ALLOWED_CLOCK_SKEW));
        } catch (NumberFormatException e) {
            // 解析失败时使用默认值 0
            return 0;
        }
    }

    public boolean isDisableTypeClaimCheck() {
        return Boolean.parseBoolean(getConfig().get("disableTypeClaimCheck"));
    }

    public void setDisableTypeClaimCheck(boolean disableTypeClaimCheck) {
        if (disableTypeClaimCheck) {
            getConfig().put("disableTypeClaimCheck", Boolean.TRUE.toString());
        } else {
            getConfig().remove("disableTypeClaimCheck");
        }
    }

    /** @return 是否支持 Federated Client Assertion */
    public boolean isSupportsClientAssertions() {
        return Boolean.parseBoolean(getConfig().get(SUPPORTS_CLIENT_ASSERTIONS));
    }

    public void setSupportsClientAssertions(boolean supportsClientAssertions) {
        getConfig().put(SUPPORTS_CLIENT_ASSERTIONS, String.valueOf(supportsClientAssertions));
    }

    public boolean isSupportsClientAssertionReuse() {
        return Boolean.parseBoolean(getConfig().get(SUPPORTS_CLIENT_ASSERTION_REUSE));
    }

    public boolean isAllowClientIdAsAudience() {
        return Boolean.parseBoolean(getConfig().getOrDefault(ALLOW_CLIENT_ID_AS_AUDIENCE, "false"));
    }

    public void setAllowClientIdAsAudience(boolean allowClientIdAsAudience) {
        getConfig().put(ALLOW_CLIENT_ID_AS_AUDIENCE, String.valueOf(allowClientIdAsAudience));
    }

    /** 校验 JWKS/公钥、logout URL 及 issuer 唯一性。 */
    @Override
    public void validate(RealmModel realm) {
        super.validate(realm);
        SslRequired sslRequired = realm.getSslRequired();
        checkUrl(sslRequired, getJwksUrl(), "jwks_url");
        checkUrl(sslRequired, getLogoutUrl(), "logout_url");

        if (isValidateSignature() || isJWTAuthorizationGrantEnabled() || isSupportsClientAssertions()) {
            String optionText = isValidateSignature() ? "Validate signatures" :
                    (isJWTAuthorizationGrantEnabled() ? "JWT Authorization Grant" : "Supports client assertions");

            if (isUseJwksUrl()) {
                if (getJwksUrl() == null) {
                    throw new IllegalArgumentException(String.format("JWKS URL is required when '%s' enabled and 'Use JWKS URL' enabled", optionText));
                }
            } else if (getPublicKeySignatureVerifier() == null) {
                throw new IllegalArgumentException(String.format("The 'Validating public key' is required when '%s' enabled and 'Use JWKS URL' disabled", optionText));
            }
        }
        if (isJWTAuthorizationGrantEnabled()) {
            validateIssuer(realm, IdentityProviderType.JWT_AUTHORIZATION_GRANT);
        }
        if (isSupportsClientAssertions()) {
            validateIssuer(realm, IdentityProviderType.CLIENT_ASSERTION);
        }
    }
}
