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

package org.keycloak.protocol.oidc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.keycloak.authentication.authenticators.client.X509ClientAuthenticator;
import org.keycloak.models.ClientModel;
import org.keycloak.models.Constants;
import org.keycloak.models.utils.MapperTypeSerializer;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.utils.StringUtil;

import static org.keycloak.protocol.oidc.OIDCConfigAttributes.USE_LOWER_CASE_IN_TOKEN_RESPONSE;
import static org.keycloak.protocol.oidc.OIDCConfigAttributes.USE_RFC9068_ACCESS_TOKEN_HEADER_TYPE;

/**
 * OIDC 客户端高级配置包装器。
 * <p>读写 UserInfo/ID Token/Request Object 加解密、PKCE、DPoP/mTLS HoK、刷新令牌策略、前后端通道登出、令牌自省受众等 {@link OIDCConfigAttributes} 属性。</p>
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class OIDCAdvancedConfigWrapper extends AbstractClientConfigWrapper {

    /** 标准令牌交换是否签发刷新令牌：否 / 同一会话。 */
    public static enum TokenExchangeRefreshTokenEnabled {NO, SAME_SESSION};

    private OIDCAdvancedConfigWrapper(ClientModel client, ClientRepresentation clientRep) {
        super(client,clientRep);
    }

    /** @param client 客户端模型 @return 高级配置包装器 */
    public static OIDCAdvancedConfigWrapper fromClientModel(ClientModel client) {
        return new OIDCAdvancedConfigWrapper(client, null);
    }

    /** @param clientRep 客户端表示 @return 高级配置包装器 */
    public static OIDCAdvancedConfigWrapper fromClientRepresentation(ClientRepresentation clientRep) {
        return new OIDCAdvancedConfigWrapper(null, clientRep);
    }

    /** 获取UserInfoSignedResponseAlg 配置值。 */
    public String getUserInfoSignedResponseAlg() {
        return getAttribute(OIDCConfigAttributes.USER_INFO_RESPONSE_SIGNATURE_ALG);
    }

    /** 设置UserInfoSignedResponseAlg 配置。 */
    public void setUserInfoSignedResponseAlg(String algorithm) {
        setAttribute(OIDCConfigAttributes.USER_INFO_RESPONSE_SIGNATURE_ALG, algorithm);
    }

    /** 是否UserInfoSignatureRequired。 */
    public boolean isUserInfoSignatureRequired() {
        return getUserInfoSignedResponseAlg() != null;
    }

    /** 设置UserInfoEncryptedResponseAlg 配置。 */
    public void setUserInfoEncryptedResponseAlg(String algorithm) {
        setAttribute(OIDCConfigAttributes.USER_INFO_ENCRYPTED_RESPONSE_ALG, algorithm);
    }

    /** 获取UserInfoEncryptedResponseAlg 配置值。 */
    public String getUserInfoEncryptedResponseAlg() {
        return getAttribute(OIDCConfigAttributes.USER_INFO_ENCRYPTED_RESPONSE_ALG);
    }

    /** 获取UserInfoEncryptedResponseEnc 配置值。 */
    public String getUserInfoEncryptedResponseEnc() {
        return getAttribute(OIDCConfigAttributes.USER_INFO_ENCRYPTED_RESPONSE_ENC);
    }

    /** 设置UserInfoEncryptedResponseEnc 配置。 */
    public void setUserInfoEncryptedResponseEnc(String algorithm) {
        setAttribute(OIDCConfigAttributes.USER_INFO_ENCRYPTED_RESPONSE_ENC, algorithm);
    }

    /** 是否UserInfoEncryptionRequired。 */
    public boolean isUserInfoEncryptionRequired() {
        return getUserInfoEncryptedResponseAlg() != null;
    }

    /** 获取RequestObjectSignatureAlg 配置值。 */
    public String getRequestObjectSignatureAlg() {
        return getAttribute(OIDCConfigAttributes.REQUEST_OBJECT_SIGNATURE_ALG);
    }

    /** 设置RequestObjectSignatureAlg 配置。 */
    public void setRequestObjectSignatureAlg(String algorithm) {
        setAttribute(OIDCConfigAttributes.REQUEST_OBJECT_SIGNATURE_ALG, algorithm);
    }

    /** 设置RequestObjectEncryptionAlg 配置。 */
    public void setRequestObjectEncryptionAlg(String algorithm) {
        setAttribute(OIDCConfigAttributes.REQUEST_OBJECT_ENCRYPTION_ALG, algorithm);
    }

    /** 获取RequestObjectEncryptionAlg 配置值。 */
    public String getRequestObjectEncryptionAlg() {
        return getAttribute(OIDCConfigAttributes.REQUEST_OBJECT_ENCRYPTION_ALG);
    }

    /** 获取RequestObjectEncryptionEnc 配置值。 */
    public String getRequestObjectEncryptionEnc() {
        return getAttribute(OIDCConfigAttributes.REQUEST_OBJECT_ENCRYPTION_ENC);
    }

    /** 设置RequestObjectEncryptionEnc 配置。 */
    public void setRequestObjectEncryptionEnc(String algorithm) {
        setAttribute(OIDCConfigAttributes.REQUEST_OBJECT_ENCRYPTION_ENC, algorithm);
    }

    /** 获取RequestObjectRequired 配置值。 */
    public String getRequestObjectRequired() {
        return getAttribute(OIDCConfigAttributes.REQUEST_OBJECT_REQUIRED);
    }

    /** 设置RequestObjectRequired 配置。 */
    public void setRequestObjectRequired(String requestObjectRequired) {
        setAttribute(OIDCConfigAttributes.REQUEST_OBJECT_REQUIRED, requestObjectRequired);
    }

    /** 获取RequestUris 配置值。 */
    public List<String> getRequestUris() {
        return getAttributeMultivalued(OIDCConfigAttributes.REQUEST_URIS);
    }

    /** 设置RequestUris 配置。 */
    public void setRequestUris(List<String> requestUris) {
        setAttributeMultivalued(OIDCConfigAttributes.REQUEST_URIS, requestUris);
    }

    /** 是否UseJwksUrl。 */
    public boolean isUseJwksUrl() {
        String useJwksUrl = getAttribute(OIDCConfigAttributes.USE_JWKS_URL);
        return Boolean.parseBoolean(useJwksUrl);
    }

    /** 设置UseJwksUrl 配置。 */
    public void setUseJwksUrl(boolean useJwksUrl) {
        String val = String.valueOf(useJwksUrl);
        setAttribute(OIDCConfigAttributes.USE_JWKS_URL, val);
    }

    /** 获取JwksUrl 配置值。 */
    public String getJwksUrl() {
        return getAttribute(OIDCConfigAttributes.JWKS_URL);
    }

    /** 设置JwksUrl 配置。 */
    public void setJwksUrl(String jwksUrl) {
        setAttribute(OIDCConfigAttributes.JWKS_URL, jwksUrl);
    }

    /** 是否UseJwksString。 */
    public boolean isUseJwksString() {
        String useJwksString = getAttribute(OIDCConfigAttributes.USE_JWKS_STRING);
        return Boolean.parseBoolean(useJwksString);
    }

    /** 设置UseJwksString 配置。 */
    public void setUseJwksString(boolean useJwksString) {
        String val = String.valueOf(useJwksString);
        setAttribute(OIDCConfigAttributes.USE_JWKS_STRING, val);
    }

    /** 获取JwksString 配置值。 */
    public String getJwksString() {
        return getAttribute(OIDCConfigAttributes.JWKS_STRING);
    }

    /** 设置JwksString 配置。 */
    public void setJwksString(String jwksString) {
        setAttribute(OIDCConfigAttributes.JWKS_STRING, jwksString);
    }

    /** 是否ExcludeSessionStateFromAuthResponse。 */
    public boolean isExcludeSessionStateFromAuthResponse() {
        String excludeSessionStateFromAuthResponse = getAttribute(OIDCConfigAttributes.EXCLUDE_SESSION_STATE_FROM_AUTH_RESPONSE);
        return Boolean.parseBoolean(excludeSessionStateFromAuthResponse);
    }

    /** 设置ExcludeSessionStateFromAuthResponse 配置。 */
    public void setExcludeSessionStateFromAuthResponse(boolean excludeSessionStateFromAuthResponse) {
        String val = String.valueOf(excludeSessionStateFromAuthResponse);
        setAttribute(OIDCConfigAttributes.EXCLUDE_SESSION_STATE_FROM_AUTH_RESPONSE, val);
    }

    /** 是否ExcludeIssuerFromAuthResponse。 */
    public boolean isExcludeIssuerFromAuthResponse() {
        String excludeIssuerFromAuthResponse = getAttribute(OIDCConfigAttributes.EXCLUDE_ISSUER_FROM_AUTH_RESPONSE);
        return Boolean.parseBoolean(excludeIssuerFromAuthResponse);
    }

    /** 设置ExcludeIssuerFromAuthResponse 配置。 */
    public void setExcludeIssuerFromAuthResponse(boolean excludeIssuerFromAuthResponse) {
        String val = String.valueOf(excludeIssuerFromAuthResponse);
        setAttribute(OIDCConfigAttributes.EXCLUDE_ISSUER_FROM_AUTH_RESPONSE, val);
    }

    /** 是否UseDPoP。 */
    public boolean isUseDPoP() {
        String mode = getAttribute(OIDCConfigAttributes.DPOP_BOUND_ACCESS_TOKENS);
        return Boolean.parseBoolean(mode);
    }

    /** 设置UseDPoP 配置。 */
    public void setUseDPoP(boolean useDPoP) {
        String val = String.valueOf(useDPoP);
        setAttribute(OIDCConfigAttributes.DPOP_BOUND_ACCESS_TOKENS, val);
    }

    // KEYCLOAK-6771 证书绑定令牌（mTLS HoK）
    // https://tools.ietf.org/html/draft-ietf-oauth-mtls-08#section-6.5
    /** 是否UseMtlsHokToken。 */
    public boolean isUseMtlsHokToken() {
        String useUtlsHokToken = getAttribute(OIDCConfigAttributes.USE_MTLS_HOK_TOKEN);
        return Boolean.parseBoolean(useUtlsHokToken);
    }

    /** 设置UseMtlsHoKToken 配置。 */
    public void setUseMtlsHoKToken(boolean useUtlsHokToken) {
        String val = String.valueOf(useUtlsHokToken);
        setAttribute(OIDCConfigAttributes.USE_MTLS_HOK_TOKEN, val);
    }

    /** 是否UseRefreshToken。 */
    public boolean isUseRefreshToken() {
        String useRefreshToken = getAttribute(OIDCConfigAttributes.USE_REFRESH_TOKEN, "true");
        return Boolean.parseBoolean(useRefreshToken);
    }

    /** 设置UseRefreshToken 配置。 */
    public void setUseRefreshToken(boolean useRefreshToken) {
        String val = String.valueOf(useRefreshToken);
        setAttribute(OIDCConfigAttributes.USE_REFRESH_TOKEN, val);
    }

    /** 是否UseLowerCaseInTokenResponse。 */
    public boolean isUseLowerCaseInTokenResponse() {
        return Boolean.parseBoolean(getAttribute(USE_LOWER_CASE_IN_TOKEN_RESPONSE, "false"));
    }

    /** 设置UseLowerCaseInTokenResponse 配置。 */
    public void setUseLowerCaseInTokenResponse(boolean useLowerCaseInTokenResponse) {
        setAttribute(USE_LOWER_CASE_IN_TOKEN_RESPONSE, String.valueOf(useLowerCaseInTokenResponse));
    }

    /** 是否UseRfc9068AccessTokenHeaderType。 */
    public boolean isUseRfc9068AccessTokenHeaderType() {
        return Boolean.parseBoolean(getAttribute(USE_RFC9068_ACCESS_TOKEN_HEADER_TYPE, "false"));
    }

    /** 设置UseRfc9068AccessTokenHeaderType 配置。 */
    public void setUseRfc9068AccessTokenHeaderType(boolean useRfc9068AccessTokenHeaderType) {
        setAttribute(USE_RFC9068_ACCESS_TOKEN_HEADER_TYPE, String.valueOf(useRfc9068AccessTokenHeaderType));
    }

    /**
     * 客户端凭证模式是否签发刷新令牌并创建用户会话（非规范默认行为，默认 false）。
     * @see <a href="https://tools.ietf.org/html/rfc6749#section-4.4.3">RFC 6749 §4.4.3</a>
     */
    public boolean isUseRefreshTokenForClientCredentialsGrant() {
        String val = getAttribute(OIDCConfigAttributes.USE_REFRESH_TOKEN_FOR_CLIENT_CREDENTIALS_GRANT, "false");
        return Boolean.parseBoolean(val);
    }

    /** 设置UseRefreshTokenForClientCredentialsGrant 配置。 */
    public void setUseRefreshTokenForClientCredentialsGrant(boolean enable) {
        String val =  String.valueOf(enable);
        setAttribute(OIDCConfigAttributes.USE_REFRESH_TOKEN_FOR_CLIENT_CREDENTIALS_GRANT, val);
    }

    /** 是否StandardTokenExchangeEnabled。 */
    public boolean isStandardTokenExchangeEnabled() {
        String val = getAttribute(OIDCConfigAttributes.STANDARD_TOKEN_EXCHANGE_ENABLED, "false");
        return Boolean.parseBoolean(val);
    }
    
    /** 设置StandardTokenExchangeEnabled 配置。 */
    public void setStandardTokenExchangeEnabled(boolean enable) {
        String val = String.valueOf(enable);
        setAttribute(OIDCConfigAttributes.STANDARD_TOKEN_EXCHANGE_ENABLED, val);
    }

    /** 获取StandardTokenExchangeRefreshEnabled 配置值。 */
    public TokenExchangeRefreshTokenEnabled getStandardTokenExchangeRefreshEnabled() {
        final String value = getAttribute(OIDCConfigAttributes.STANDARD_TOKEN_EXCHANGE_REFRESH_ENABLED);
        try {
            return value == null? TokenExchangeRefreshTokenEnabled.NO : TokenExchangeRefreshTokenEnabled.valueOf(value);
        } catch (IllegalArgumentException e) {
            return TokenExchangeRefreshTokenEnabled.NO;
        }
    }

    /** 设置StandardTokenExchangeRefreshEnabled 配置。 */
    public void setStandardTokenExchangeRefreshEnabled(TokenExchangeRefreshTokenEnabled enable) {
        setAttribute(OIDCConfigAttributes.STANDARD_TOKEN_EXCHANGE_REFRESH_ENABLED,
                enable == null || enable == TokenExchangeRefreshTokenEnabled.NO? null : enable.name());
    }

    /** 获取JWTAuthorizationGrantEnabled 配置值。 */
    public boolean getJWTAuthorizationGrantEnabled() {
        String val = getAttribute(OIDCConfigAttributes.JWT_AUTHORIZATION_GRANT_ENABLED, "false");
        return Boolean.parseBoolean(val);
    }

    /** 设置JWTAuthorizationGrantEnabled 配置。 */
    public void setJWTAuthorizationGrantEnabled(boolean enable) {
        String val = String.valueOf(enable);
        setAttribute(OIDCConfigAttributes.JWT_AUTHORIZATION_GRANT_ENABLED, val);
    }

    /** 获取JWTAuthorizationGrantAllowedIdentityProviders 配置值。 */
    public List<String> getJWTAuthorizationGrantAllowedIdentityProviders() {
        List<String> allowedIDPs = getAttributeMultivalued(OIDCConfigAttributes.JWT_AUTHORIZATION_GRANT_IDP);
        return allowedIDPs == null ? Collections.emptyList() : allowedIDPs;
    }

    /** 获取JWTAuthorizationGrantAudience 配置值。 */
    public Map<String, List<String>> getJWTAuthorizationGrantAudience() {
        String audiences = getAttribute(OIDCConfigAttributes.JWT_AUTHORIZATION_GRANT_AUDIENCE);
        return MapperTypeSerializer.deserialize(audiences);
    }

    /** 获取ExternalTokenEnabled 配置值。 */
    public boolean getExternalTokenEnabled() {
        String val = getAttribute(OIDCConfigAttributes.EXTERNAL_TOKEN_ENABLED, "false");
        return Boolean.parseBoolean(val);
    }

    /** 获取ExternalAllowedIdentityProviders 配置值。 */
    public List<String> getExternalAllowedIdentityProviders() {
        List<String> allowedIDPs = getAttributeMultivalued(OIDCConfigAttributes.EXTERNAL_TOKEN_IDP);
        return allowedIDPs == null ? Collections.emptyList() : allowedIDPs;
    }

    /** 获取TlsClientAuthSubjectDn 配置值。 */
    public String getTlsClientAuthSubjectDn() {
        return getAttribute(X509ClientAuthenticator.ATTR_SUBJECT_DN);
     }

    /** 设置TlsClientAuthSubjectDn 配置。 */
    public void setTlsClientAuthSubjectDn(String tls_client_auth_subject_dn) {
        setAttribute(X509ClientAuthenticator.ATTR_SUBJECT_DN, tls_client_auth_subject_dn);
    }

    /** 获取TlsClientAuthCASubjectDn 配置值。 */
    public String getTlsClientAuthCASubjectDn() {
        return getAttribute(X509ClientAuthenticator.ATTR_CA_SUBJECT_DN);
     }

    /** 设置TlsClientAuthCASubjectDn 配置。 */
    public void setTlsClientAuthCASubjectDn(String caSubjectDn) {
        setAttribute(X509ClientAuthenticator.ATTR_CA_SUBJECT_DN, caSubjectDn);
    }

    /** 获取AllowRegexPatternComparison 配置值。 */
    public boolean getAllowRegexPatternComparison() {
        String attrVal = getAttribute(X509ClientAuthenticator.ATTR_ALLOW_REGEX_PATTERN_COMPARISON);
        // 默认允许正则匹配 Subject DN（向后兼容）
        return attrVal == null || Boolean.parseBoolean(attrVal);
    }

    /** 设置AllowRegexPatternComparison 配置。 */
    public void setAllowRegexPatternComparison(boolean allowRegexPatternComparison) {
        setAttribute(X509ClientAuthenticator.ATTR_ALLOW_REGEX_PATTERN_COMPARISON, String.valueOf(allowRegexPatternComparison));
    }

    /** 获取PkceCodeChallengeMethod 配置值。 */
    public String getPkceCodeChallengeMethod() {
        return getAttribute(OIDCConfigAttributes.PKCE_CODE_CHALLENGE_METHOD);
    }

    /** 设置PkceCodeChallengeMethod 配置。 */
    public void setPkceCodeChallengeMethod(String codeChallengeMethodName) {
        setAttribute(OIDCConfigAttributes.PKCE_CODE_CHALLENGE_METHOD, codeChallengeMethodName);
    }

    /** 获取IdTokenSignedResponseAlg 配置值。 */
    public String getIdTokenSignedResponseAlg() {
        return getAttribute(OIDCConfigAttributes.ID_TOKEN_SIGNED_RESPONSE_ALG);
    }
    /** 设置IdTokenSignedResponseAlg 配置。 */
    public void setIdTokenSignedResponseAlg(String algName) {
        setAttribute(OIDCConfigAttributes.ID_TOKEN_SIGNED_RESPONSE_ALG, algName);
    }

    /** 获取IdTokenEncryptedResponseAlg 配置值。 */
    public String getIdTokenEncryptedResponseAlg() {
        return getAttribute(OIDCConfigAttributes.ID_TOKEN_ENCRYPTED_RESPONSE_ALG);
    }

    /** 设置IdTokenEncryptedResponseAlg 配置。 */
    public void setIdTokenEncryptedResponseAlg(String algName) {
        setAttribute(OIDCConfigAttributes.ID_TOKEN_ENCRYPTED_RESPONSE_ALG, algName);
    }

    /** 获取IdTokenEncryptedResponseEnc 配置值。 */
    public String getIdTokenEncryptedResponseEnc() {
        return getAttribute(OIDCConfigAttributes.ID_TOKEN_ENCRYPTED_RESPONSE_ENC);
    }

    /** 设置IdTokenEncryptedResponseEnc 配置。 */
    public void setIdTokenEncryptedResponseEnc(String encName) {
        setAttribute(OIDCConfigAttributes.ID_TOKEN_ENCRYPTED_RESPONSE_ENC, encName);
    }

    /** 获取AuthorizationSignedResponseAlg 配置值。 */
    public String getAuthorizationSignedResponseAlg() {
        return getAttribute(OIDCConfigAttributes.AUTHORIZATION_SIGNED_RESPONSE_ALG);
    }
    /** 设置AuthorizationSignedResponseAlg 配置。 */
    public void setAuthorizationSignedResponseAlg(String algName) {
        setAttribute(OIDCConfigAttributes.AUTHORIZATION_SIGNED_RESPONSE_ALG, algName);
    }

    /** 获取AuthorizationEncryptedResponseAlg 配置值。 */
    public String getAuthorizationEncryptedResponseAlg() {
        return getAttribute(OIDCConfigAttributes.AUTHORIZATION_ENCRYPTED_RESPONSE_ALG);
    }

    /** 设置AuthorizationEncryptedResponseAlg 配置。 */
    public void setAuthorizationEncryptedResponseAlg(String algName) {
        setAttribute(OIDCConfigAttributes.AUTHORIZATION_ENCRYPTED_RESPONSE_ALG, algName);
    }

    /** 获取AuthorizationEncryptedResponseEnc 配置值。 */
    public String getAuthorizationEncryptedResponseEnc() {
        return getAttribute(OIDCConfigAttributes.AUTHORIZATION_ENCRYPTED_RESPONSE_ENC);
    }

    /** 设置AuthorizationEncryptedResponseEnc 配置。 */
    public void setAuthorizationEncryptedResponseEnc(String encName) {
        setAttribute(OIDCConfigAttributes.AUTHORIZATION_ENCRYPTED_RESPONSE_ENC, encName);
    }

    /** 获取TokenEndpointAuthSigningAlg 配置值。 */
    public String getTokenEndpointAuthSigningAlg() {
        return getAttribute(OIDCConfigAttributes.TOKEN_ENDPOINT_AUTH_SIGNING_ALG);
    }

    /** 设置TokenEndpointAuthSigningAlg 配置。 */
    public void setTokenEndpointAuthSigningAlg(String algName) {
        setAttribute(OIDCConfigAttributes.TOKEN_ENDPOINT_AUTH_SIGNING_ALG, algName);
    }

    /** 获取TokenEndpointAuthSigningMaxExp 配置值。 */
    public int getTokenEndpointAuthSigningMaxExp() {
        final String value = getAttribute(OIDCConfigAttributes.TOKEN_ENDPOINT_AUTH_SIGNING_MAX_EXP);
        try {
            final int maxExp = Integer.parseInt(value);
            if (maxExp > 0) {
                return maxExp;
            }
        } catch (NumberFormatException e) {
            // ignore and return default value
        }
        return 60; // default to 60s
    }

    /** 设置TokenEndpointAuthSigningMaxExp 配置。 */
    public void setTokenEndpointAuthSigningMaxExp(int maxExp) {
        if (maxExp <= 0) {
            throw new IllegalArgumentException("Maximum expiration is a positive number in seconds");
        }
        setAttribute(OIDCConfigAttributes.TOKEN_ENDPOINT_AUTH_SIGNING_MAX_EXP, String.valueOf(maxExp));
    }

    /** 是否LogoutConfirmationEnabled。 */
    public boolean isLogoutConfirmationEnabled() {
        return Boolean.parseBoolean(getAttribute(OIDCConfigAttributes.LOGOUT_CONFIRMATION_ENABLED, "false"));
    }

    /** 设置LogoutConfirmationEnabled 配置。 */
    public void setLogoutConfirmationEnabled(boolean enabled) {
        setAttribute(OIDCConfigAttributes.LOGOUT_CONFIRMATION_ENABLED, String.valueOf(enabled));
    }

    /** 获取BackchannelLogoutUrl 配置值。 */
    public String getBackchannelLogoutUrl() {
        return getAttribute(OIDCConfigAttributes.BACKCHANNEL_LOGOUT_URL);
    }

    /** 设置BackchannelLogoutUrl 配置。 */
    public void setBackchannelLogoutUrl(String backchannelLogoutUrl) {
        setAttribute(OIDCConfigAttributes.BACKCHANNEL_LOGOUT_URL, backchannelLogoutUrl);
    }

    /** 是否BackchannelLogoutSessionRequired。 */
    public boolean isBackchannelLogoutSessionRequired() {
        String backchannelLogoutSessionRequired = getAttribute(OIDCConfigAttributes.BACKCHANNEL_LOGOUT_SESSION_REQUIRED);
        return Boolean.parseBoolean(backchannelLogoutSessionRequired);
    }

    /** 设置BackchannelLogoutSessionRequired 配置。 */
    public void setBackchannelLogoutSessionRequired(boolean backchannelLogoutSessionRequired) {
        String val = String.valueOf(backchannelLogoutSessionRequired);
        setAttribute(OIDCConfigAttributes.BACKCHANNEL_LOGOUT_SESSION_REQUIRED, val);
    }

    /** 获取BackchannelLogoutRevokeOfflineTokens 配置值。 */
    public boolean getBackchannelLogoutRevokeOfflineTokens() {
        String backchannelLogoutRevokeOfflineTokens = getAttribute(OIDCConfigAttributes.BACKCHANNEL_LOGOUT_REVOKE_OFFLINE_TOKENS);
        return Boolean.parseBoolean(backchannelLogoutRevokeOfflineTokens);
    }

    /** 设置BackchannelLogoutRevokeOfflineTokens 配置。 */
    public void setBackchannelLogoutRevokeOfflineTokens(boolean backchannelLogoutRevokeOfflineTokens) {
        String val = String.valueOf(backchannelLogoutRevokeOfflineTokens);
        setAttribute(OIDCConfigAttributes.BACKCHANNEL_LOGOUT_REVOKE_OFFLINE_TOKENS, val);
    }

    /** 设置FrontChannelLogoutUrl 配置。 */
    public void setFrontChannelLogoutUrl(String frontChannelLogoutUrl) {
        if (clientRep != null) {
            clientRep.setFrontchannelLogout(StringUtil.isNotBlank(frontChannelLogoutUrl));
        }
        if (clientModel != null) {
            clientModel.setFrontchannelLogout(StringUtil.isNotBlank(frontChannelLogoutUrl));
        }
        setAttribute(OIDCConfigAttributes.FRONT_CHANNEL_LOGOUT_URI, frontChannelLogoutUrl);
    }

    /** 是否FrontChannelLogoutEnabled。 */
    public boolean isFrontChannelLogoutEnabled() {
        return clientModel != null && clientModel.isFrontchannelLogout() && StringUtil.isNotBlank(getFrontChannelLogoutUrl());
    }

    /** 获取FrontChannelLogoutUrl 配置值。 */
    public String getFrontChannelLogoutUrl() {
        return getAttribute(OIDCConfigAttributes.FRONT_CHANNEL_LOGOUT_URI);
    }

    /** 是否FrontChannelLogoutSessionRequired。 */
    public boolean isFrontChannelLogoutSessionRequired() {
        String frontChannelLogoutSessionRequired = getAttribute(OIDCConfigAttributes.FRONT_CHANNEL_LOGOUT_SESSION_REQUIRED);
        // 默认包含 sid/iss 以兼容旧行为
        return frontChannelLogoutSessionRequired == null ? true : Boolean.parseBoolean(frontChannelLogoutSessionRequired);
    }

    /** 设置FrontChannelLogoutSessionRequired 配置。 */
    public void setFrontChannelLogoutSessionRequired(boolean frontChannelLogoutSessionRequired) {
        String val = String.valueOf(frontChannelLogoutSessionRequired);
        setAttribute(OIDCConfigAttributes.FRONT_CHANNEL_LOGOUT_SESSION_REQUIRED, val);
    }

    /** 设置LogoUri 配置。 */
    public void setLogoUri(String logoUri) {
        setAttribute(ClientModel.LOGO_URI, logoUri);
    }

    /** 设置PolicyUri 配置。 */
    public void setPolicyUri(String policyUri) {
        setAttribute(ClientModel.POLICY_URI, policyUri);
    }

    /** 设置TosUri 配置。 */
    public void setTosUri(String tosUri) {
        setAttribute(ClientModel.TOS_URI, tosUri);
    }

    /** 设置SectorIdentifierUri 配置。 */
    public void setSectorIdentifierUri(String sectorIdentifierUri) {
        setAttribute(OIDCConfigAttributes.SECTOR_IDENTIFIER_URI, sectorIdentifierUri);
    }

    /** 获取PostLogoutRedirectUris 配置值。 */
    public List<String> getPostLogoutRedirectUris() {
        List<String> postLogoutRedirectUris = getAttributeMultivalued(OIDCConfigAttributes.POST_LOGOUT_REDIRECT_URIS);
        if(postLogoutRedirectUris == null || postLogoutRedirectUris.isEmpty()) {
            if(clientModel != null) {
                return new ArrayList(clientModel.getRedirectUris());
            }
            else if(clientRep != null) {
                return clientRep.getRedirectUris();
            }
            return null;
        }
        else if(postLogoutRedirectUris.get(0).equals("-")) {
            return new ArrayList<String>();
        }
        else if (postLogoutRedirectUris.contains("+")) {
            Set<String> returnedPostLogoutRedirectUris = postLogoutRedirectUris.stream()
                    .filter(uri -> !"+".equals(uri)).collect(Collectors.toSet());

            if(clientModel != null) {
                returnedPostLogoutRedirectUris.addAll(clientModel.getRedirectUris());
            }
            else if(clientRep != null) {
                returnedPostLogoutRedirectUris.addAll(clientRep.getRedirectUris());
            }
            return new ArrayList<>(returnedPostLogoutRedirectUris);
        }
        else {
            return postLogoutRedirectUris;
        }
    }

    /** 设置PostLogoutRedirectUris 配置。 */
    public void setPostLogoutRedirectUris(List<String> postLogoutRedirectUris) {
        setAttributeMultivalued(OIDCConfigAttributes.POST_LOGOUT_REDIRECT_URIS, postLogoutRedirectUris);
    }

    /** 获取MinimumAcrValue 配置值。 */
    public String getMinimumAcrValue() {
        return getAttribute(Constants.MINIMUM_ACR_VALUE);
    }

    /** 设置MinimumAcrValue 配置。 */
    public void setMinimumAcrValue(String minimumAcrValue) {
        setAttribute(Constants.MINIMUM_ACR_VALUE, minimumAcrValue);
    }

    /** 是否AllowTokenIntrospectionWithoutAudienceCheck。 */
    public boolean isAllowTokenIntrospectionWithoutAudienceCheck() {
        String val = getAttribute(OIDCConfigAttributes.ALLOW_TOKEN_INTROSPECTION_WITHOUT_AUDIENCE_CHECK, "false");
        return Boolean.parseBoolean(val);
    }

    /** 设置AllowTokenIntrospectionWithoutAudienceCheck 配置。 */
    public void setAllowTokenIntrospectionWithoutAudienceCheck(boolean allow) {
        setAttribute(OIDCConfigAttributes.ALLOW_TOKEN_INTROSPECTION_WITHOUT_AUDIENCE_CHECK, String.valueOf(allow));
    }

    /** 是否AllowUserinfoWithLightweightAccessToken。 */
    public boolean isAllowUserinfoWithLightweightAccessToken() {
        String val = getAttribute(OIDCConfigAttributes.ALLOW_USERINFO_WITH_LIGHTWEIGHT_ACCESS_TOKEN, "false");
        return Boolean.parseBoolean(val);
    }

    /** 设置AllowUserinfoWithLightweightAccessToken 配置。 */
    public void setAllowUserinfoWithLightweightAccessToken(boolean allow) {
        setAttribute(OIDCConfigAttributes.ALLOW_USERINFO_WITH_LIGHTWEIGHT_ACCESS_TOKEN, String.valueOf(allow));
    }
}
