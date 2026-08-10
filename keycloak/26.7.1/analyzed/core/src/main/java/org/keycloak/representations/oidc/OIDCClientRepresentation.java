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

package org.keycloak.representations.oidc;

import java.util.List;

import org.keycloak.jose.jwk.JSONWebKeySet;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

/**
 * OIDC 动态客户端注册（Dynamic Client Registration）的 JSON 表示，字段名遵循 OIDC 规范 snake_case 命名，
 * 用于客户端自注册、Adapter 配置导出及协议层 JSON 序列化。Jackson 通过字段可见性直接读写 JSON。
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
public class OIDCClientRepresentation {

    // OIDC 动态客户端注册属性

    /** 合法的重定向 URI 列表。 */
    private List<String> redirect_uris;

    /** Token 端点客户端认证方式（如 client_secret_basic）。 */
    private String token_endpoint_auth_method;

    /** Token 端点认证请求签名算法。 */
    private String token_endpoint_auth_signing_alg;

    /** 允许的 OAuth 2.0 授权类型列表。 */
    private List<String> grant_types;

    /** 允许的 OIDC 响应类型列表。 */
    private List<String> response_types;

    /** 应用类型（web 或 native）。 */
    private String application_type;

    /** 客户端标识符。 */
    private String client_id;

    /** 客户端密钥。 */
    private String client_secret;

    /** 客户端显示名称。 */
    private String client_name;

    /** 客户端主页 URI。 */
    private String client_uri;

    /** 客户端 Logo URI。 */
    private String logo_uri;

    /** 默认请求的 scope 字符串。 */
    private String scope;

    /** 客户端负责人联系邮箱列表。 */
    private List<String> contacts;

    /** 服务条款 URI。 */
    private String tos_uri;

    /** 隐私政策 URI。 */
    private String policy_uri;

    /** 客户端 JWKS 文档 URI。 */
    private String jwks_uri;

    /** 内嵌的客户端 JSON Web Key Set。 */
    private JSONWebKeySet jwks;

    /** Sector Identifier URI，用于 redirect_uri 校验。 */
    private String sector_identifier_uri;

    /** 主体标识类型（public 或 pairwise）。 */
    private String subject_type;

    /** ID Token 响应签名算法。 */
    private String id_token_signed_response_alg;

    /** ID Token 响应内容加密算法。 */
    private String id_token_encrypted_response_alg;

    /** ID Token 响应加密方法。 */
    private String id_token_encrypted_response_enc;

    /** UserInfo 响应签名算法。 */
    private String userinfo_signed_response_alg;

    /** UserInfo 响应内容加密算法。 */
    private String userinfo_encrypted_response_alg;

    /** UserInfo 响应加密方法。 */
    private String userinfo_encrypted_response_enc;

    /** Request Object 签名算法。 */
    private String request_object_signing_alg;

    /** Request Object 内容加密算法。 */
    private String request_object_encryption_alg;

    /** Request Object 加密方法。 */
    private String request_object_encryption_enc;

    /** 默认最大认证年龄（秒）。 */
    private Integer default_max_age;

    /** 是否要求在 ID Token 中包含 auth_time。 */
    private Boolean require_auth_time;

    /** 默认认证上下文类引用（ACR）值列表。 */
    private List<String> default_acr_values;

    /** 第三方发起登录的 URI。 */
    private String initiate_login_uri;

    /** 预注册 Request URI 列表。 */
    private List<String> request_uris;

    // KEYCLOAK-6771 证书绑定访问令牌
    // https://tools.ietf.org/html/draft-ietf-oauth-mtls-08#section-6.5
    /** 是否启用 mTLS 证书绑定的访问令牌。 */
    private Boolean tls_client_certificate_bound_access_tokens;

    /** mTLS 客户端证书 Subject DN，用于证书认证。 */
    private String tls_client_auth_subject_dn;

    // OAuth 2.0 DPoP 演示证明
    /** 是否启用 DPoP 绑定的访问令牌。 */
    private Boolean dpop_bound_access_tokens;

    // OIDC 会话管理
    /** 登出后允许的重定向 URI 列表。 */
    private List<String> post_logout_redirect_uris;

    // 软件标识（规范来源未明确）
    /** 客户端软件 ID。 */
    private String software_id;

    /** 客户端软件版本。 */
    private String software_version;

    // OIDC 动态客户端注册响应字段
    /** client_id 签发时间（Unix 秒）。 */
    private Integer client_id_issued_at;

    /** client_secret 过期时间（Unix 秒）；0 表示不过期。 */
    private Long client_secret_expires_at;

    /** 客户端配置管理 URI。 */
    private String registration_client_uri;

    /** 用于后续客户端配置更新的注册访问令牌。 */
    private String registration_access_token;

    /** 后台通道登出通知 URI。 */
    private String backchannel_logout_uri;

    /** 后台登出是否须携带 session 信息。 */
    private Boolean backchannel_logout_session_required;
    
    /** 后台登出时是否撤销离线令牌。 */
    private Boolean backchannel_logout_revoke_offline_tokens;

    // OIDC CIBA（跨域身份验证）
    /** 后台令牌投递模式（poll/ping/push）。 */
    private String backchannel_token_delivery_mode;

    /** CIBA 客户端通知端点 URI。 */
    private String backchannel_client_notification_endpoint;

    /** CIBA 认证请求签名算法。 */
    private String backchannel_authentication_request_signing_alg;

    // FAPI JARM（JWT 授权响应模式）
    /** 授权响应 JWT 签名算法。 */
    private String authorization_signed_response_alg;

    /** 授权响应 JWT 内容加密算法。 */
    private String authorization_encrypted_response_alg;

    /** 授权响应 JWT 加密方法。 */
    private String authorization_encrypted_response_enc;

    // 推送授权请求（PAR）
    /** 是否强制使用推送授权请求。 */
    private Boolean require_pushed_authorization_requests;

    /** 前台通道登出 URI。 */
    private String frontchannel_logout_uri;

    /** 前台登出是否须携带 session 信息。 */
    private Boolean frontchannel_logout_session_required;

    public List<String> getRedirectUris() {
        return redirect_uris;
    }

    public void setRedirectUris(List<String> redirectUris) {
        this.redirect_uris = redirectUris;
    }

    public String getTokenEndpointAuthMethod() {
        return token_endpoint_auth_method;
    }

    public void setTokenEndpointAuthMethod(String token_endpoint_auth_method) {
        this.token_endpoint_auth_method = token_endpoint_auth_method;
    }

    public String getTokenEndpointAuthSigningAlg() {
        return token_endpoint_auth_signing_alg;
    }

    public void setTokenEndpointAuthSigningAlg(String token_endpoint_auth_signing_alg) {
        this.token_endpoint_auth_signing_alg = token_endpoint_auth_signing_alg;
    }

    public List<String> getGrantTypes() {
        return grant_types;
    }

    public void setGrantTypes(List<String> grantTypes) {
        this.grant_types = grantTypes;
    }

    public List<String> getResponseTypes() {
        return response_types;
    }

    public void setResponseTypes(List<String> responseTypes) {
        this.response_types = responseTypes;
    }

    public String getApplicationType() {
        return application_type;
    }

    public void setApplicationType(String applicationType) {
        this.application_type = applicationType;
    }

    public String getClientId() {
        return client_id;
    }

    public void setClientId(String clientId) {
        this.client_id = clientId;
    }

    public String getClientSecret() {
        return client_secret;
    }

    public void setClientSecret(String clientSecret) {
        this.client_secret = clientSecret;
    }

    public String getClientName() {
        return client_name;
    }

    public void setClientName(String client_name) {
        this.client_name = client_name;
    }

    public String getClientUri() {
        return client_uri;
    }

    public void setClientUri(String client_uri) {
        this.client_uri = client_uri;
    }

    public String getLogoUri() {
        return logo_uri;
    }

    public void setLogoUri(String logo_uri) {
        this.logo_uri = logo_uri;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public List<String> getContacts() {
        return contacts;
    }

    public void setContacts(List<String> contacts) {
        this.contacts = contacts;
    }

    public String getTosUri() {
        return tos_uri;
    }

    public void setTosUri(String tos_uri) {
        this.tos_uri = tos_uri;
    }

    public String getPolicyUri() {
        return policy_uri;
    }

    public void setPolicyUri(String policy_uri) {
        this.policy_uri = policy_uri;
    }

    public String getJwksUri() {
        return jwks_uri;
    }

    public void setJwksUri(String jwks_uri) {
        this.jwks_uri = jwks_uri;
    }

    public JSONWebKeySet getJwks() {
        return jwks;
    }

    public void setJwks(JSONWebKeySet jwks) {
        this.jwks = jwks;
    }

    public String getSectorIdentifierUri() {
        return sector_identifier_uri;
    }

    public void setSectorIdentifierUri(String sectorIdentifierUri) {
        this.sector_identifier_uri = sectorIdentifierUri;
    }

    public String getSubjectType() {
        return subject_type;
    }

    public void setSubjectType(String subjectType) {
        this.subject_type = subjectType;
    }

    public String getIdTokenSignedResponseAlg() {
        return id_token_signed_response_alg;
    }

    public void setIdTokenSignedResponseAlg(String idTokenSignedResponseAlg) {
        this.id_token_signed_response_alg = idTokenSignedResponseAlg;
    }

    public String getIdTokenEncryptedResponseAlg() {
        return id_token_encrypted_response_alg;
    }

    public void setIdTokenEncryptedResponseAlg(String idTokenEncryptedResponseAlg) {
        this.id_token_encrypted_response_alg = idTokenEncryptedResponseAlg;
    }

    public String getIdTokenEncryptedResponseEnc() {
        return id_token_encrypted_response_enc;
    }

    public void setIdTokenEncryptedResponseEnc(String idTokenEncryptedResponseEnc) {
        this.id_token_encrypted_response_enc = idTokenEncryptedResponseEnc;
    }

    public String getUserinfoSignedResponseAlg() {
        return userinfo_signed_response_alg;
    }

    public void setUserinfoSignedResponseAlg(String userinfo_signed_response_alg) {
        this.userinfo_signed_response_alg = userinfo_signed_response_alg;
    }

    public String getUserinfoEncryptedResponseAlg() {
        return userinfo_encrypted_response_alg;
    }

    public void setUserinfoEncryptedResponseAlg(String userinfo_encrypted_response_alg) {
        this.userinfo_encrypted_response_alg = userinfo_encrypted_response_alg;
    }

    public String getUserinfoEncryptedResponseEnc() {
        return userinfo_encrypted_response_enc;
    }

    public void setUserinfoEncryptedResponseEnc(String userinfo_encrypted_response_enc) {
        this.userinfo_encrypted_response_enc = userinfo_encrypted_response_enc;
    }

    public String getRequestObjectSigningAlg() {
        return request_object_signing_alg;
    }

    public void setRequestObjectSigningAlg(String request_object_signing_alg) {
        this.request_object_signing_alg = request_object_signing_alg;
    }

    public String getRequestObjectEncryptionAlg() {
        return request_object_encryption_alg;
    }

    public void setRequestObjectEncryptionAlg(String request_object_encryption_alg) {
        this.request_object_encryption_alg = request_object_encryption_alg;
    }

    public String getRequestObjectEncryptionEnc() {
        return request_object_encryption_enc;
    }

    public void setRequestObjectEncryptionEnc(String request_object_encryption_enc) {
        this.request_object_encryption_enc = request_object_encryption_enc;
    }

    public Integer getDefaultMaxAge() {
        return default_max_age;
    }

    public void setDefaultMaxAge(Integer default_max_age) {
        this.default_max_age = default_max_age;
    }

    public Boolean getRequireAuthTime() {
        return require_auth_time;
    }

    public void setRequireAuthTime(Boolean require_auth_time) {
        this.require_auth_time = require_auth_time;
    }

    public List<String> getDefaultAcrValues() {
        return default_acr_values;
    }

    public void setDefaultAcrValues(List<String> default_acr_values) {
        this.default_acr_values = default_acr_values;
    }

    public String getInitiateLoginUri() {
        return initiate_login_uri;
    }

    public void setInitiateLoginUri(String initiate_login_uri) {
        this.initiate_login_uri = initiate_login_uri;
    }

    public List<String> getRequestUris() {
        return request_uris;
    }

    public void setRequestUris(List<String> requestUris) {
        this.request_uris = requestUris;
    }

    public List<String> getPostLogoutRedirectUris() {
        return post_logout_redirect_uris;
    }

    public void setPostLogoutRedirectUris(List<String> post_logout_redirect_uris) {
        this.post_logout_redirect_uris = post_logout_redirect_uris;
    }

    public String getSoftwareId() {
        return software_id;
    }

    public void setSoftwareId(String softwareId) {
        this.software_id = softwareId;
    }

    public String getSoftwareVersion() {
        return software_version;
    }

    public void setSoftwareVersion(String softwareVersion) {
        this.software_version = softwareVersion;
    }

    public Integer getClientIdIssuedAt() {
        return client_id_issued_at;
    }

    public void setClientIdIssuedAt(Integer clientIdIssuedAt) {
        this.client_id_issued_at = clientIdIssuedAt;
    }

    public Integer getClientSecretExpiresAt() {
        return client_secret_expires_at == null ? null : client_secret_expires_at.intValue();
    }

    public void setClientSecretExpiresAt(Integer client_secret_expires_at) {
        this.client_secret_expires_at = client_secret_expires_at == null ? null : client_secret_expires_at.longValue();
    }

    // Long 重载以避免 2038 年（Y2K38）后时间戳整型溢出
    // Jackson 序列化时直接使用 Long 字段
    public void setClientSecretExpiresAt(Long client_secret_expires_at) {
        this.client_secret_expires_at = client_secret_expires_at;
    }

    public String getRegistrationClientUri() {
        return registration_client_uri;
    }

    public void setRegistrationClientUri(String registrationClientUri) {
        this.registration_client_uri = registrationClientUri;
    }

    public String getRegistrationAccessToken() {
        return registration_access_token;
    }

    public void setRegistrationAccessToken(String registrationAccessToken) {
        this.registration_access_token = registrationAccessToken;
    }

    // KEYCLOAK-6771 证书绑定访问令牌
    // https://tools.ietf.org/html/draft-ietf-oauth-mtls-08#section-6.5
    public Boolean getTlsClientCertificateBoundAccessTokens() {
        return tls_client_certificate_bound_access_tokens;
    }

    public void setTlsClientCertificateBoundAccessTokens(Boolean tls_client_certificate_bound_access_tokens) {
        this.tls_client_certificate_bound_access_tokens = tls_client_certificate_bound_access_tokens;
    }

    public Boolean getDpopBoundAccessTokens() {
        return dpop_bound_access_tokens;
    }

    public void setDpopBoundAccessTokens(Boolean dpop_bound_access_tokens) {
        this.dpop_bound_access_tokens = dpop_bound_access_tokens;
    }

    public String getBackchannelLogoutUri() {
        return backchannel_logout_uri;
    }

    public void setBackchannelLogoutUri(String backchannel_logout_uri) {
        this.backchannel_logout_uri = backchannel_logout_uri;
    }

    public Boolean getBackchannelLogoutSessionRequired() {
        return backchannel_logout_session_required;
    }

    public void setBackchannelLogoutSessionRequired(Boolean backchannel_logout_session_required) {
        this.backchannel_logout_session_required = backchannel_logout_session_required;
    }

    public Boolean getBackchannelLogoutRevokeOfflineTokens() {
        return backchannel_logout_revoke_offline_tokens;
    }

    public void setBackchannelLogoutRevokeOfflineTokens(Boolean backchannel_logout_revoke_offline_tokens) {
        this.backchannel_logout_revoke_offline_tokens = backchannel_logout_revoke_offline_tokens;
    }

    public String getTlsClientAuthSubjectDn() {
            return tls_client_auth_subject_dn;
        }

    public void setTlsClientAuthSubjectDn(String tls_client_auth_subject_dn) {
        this.tls_client_auth_subject_dn = tls_client_auth_subject_dn;
    }

    public String getBackchannelTokenDeliveryMode() {
        return backchannel_token_delivery_mode;
    }

    public void setBackchannelTokenDeliveryMode(String backchannel_token_delivery_mode) {
        this.backchannel_token_delivery_mode = backchannel_token_delivery_mode;
    }

    public String getBackchannelClientNotificationEndpoint() {
        return backchannel_client_notification_endpoint;
    }

    public void setBackchannelClientNotificationEndpoint(String backchannel_client_notification_endpoint) {
        this.backchannel_client_notification_endpoint = backchannel_client_notification_endpoint;
    }

    public String getBackchannelAuthenticationRequestSigningAlg() {
        return backchannel_authentication_request_signing_alg;
    }

    public void setBackchannelAuthenticationRequestSigningAlg(String backchannel_authentication_request_signing_alg) {
        this.backchannel_authentication_request_signing_alg = backchannel_authentication_request_signing_alg;
    }

    public String getAuthorizationSignedResponseAlg() {
        return authorization_signed_response_alg;
    }

    public void setAuthorizationSignedResponseAlg(String authorization_signed_response_alg) {
        this.authorization_signed_response_alg = authorization_signed_response_alg;
    }

    public String getAuthorizationEncryptedResponseAlg() {
        return authorization_encrypted_response_alg;
    }

    public void setAuthorizationEncryptedResponseAlg(String authorization_encrypted_response_alg) {
        this.authorization_encrypted_response_alg = authorization_encrypted_response_alg;
    }

    public String getAuthorizationEncryptedResponseEnc() {
        return authorization_encrypted_response_enc;
    }

    public void setAuthorizationEncryptedResponseEnc(String authorization_encrypted_response_enc) {
        this.authorization_encrypted_response_enc = authorization_encrypted_response_enc;
    }

    public Boolean getRequirePushedAuthorizationRequests() {
        return require_pushed_authorization_requests;
    }

    public void setRequirePushedAuthorizationRequests(Boolean require_pushed_authorization_requests) {
        this.require_pushed_authorization_requests = require_pushed_authorization_requests;
    }

    public String getFrontChannelLogoutUri() {
        return frontchannel_logout_uri;
    }

    public void setFrontChannelLogoutUri(String frontchannel_logout_uri) {
        this.frontchannel_logout_uri = frontchannel_logout_uri;
    }

    public Boolean getFrontchannelLogoutSessionRequired() {
        return frontchannel_logout_session_required;
    }

    public void setFrontchannelLogoutSessionRequired(Boolean frontchannel_logout_session_required) {
        this.frontchannel_logout_session_required = frontchannel_logout_session_required;
    }
}
