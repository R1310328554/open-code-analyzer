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

/**
 * OIDC 客户端/作用域配置属性键常量集合。
 * <p>对应 Advanced Settings 与 Fine-grained OpenID Connect configuration 中的选项名。</p>
 */
public final class OIDCConfigAttributes {

    /** UserInfo 响应签名算法。 */
    public static final String USER_INFO_RESPONSE_SIGNATURE_ALG = "user.info.response.signature.alg";
    public static final String USER_INFO_ENCRYPTED_RESPONSE_ALG = "user.info.encrypted.response.alg";
    public static final String USER_INFO_ENCRYPTED_RESPONSE_ENC = "user.info.encrypted.response.enc";

    public static final String REQUEST_OBJECT_SIGNATURE_ALG = "request.object.signature.alg";
    public static final String REQUEST_OBJECT_ENCRYPTION_ALG = "request.object.encryption.alg";
    public static final String REQUEST_OBJECT_ENCRYPTION_ENC = "request.object.encryption.enc";

    public static final String REQUEST_OBJECT_REQUIRED = "request.object.required";
    public static final String REQUEST_OBJECT_REQUIRED_REQUEST_OR_REQUEST_URI = "request or request_uri";
    public static final String REQUEST_OBJECT_REQUIRED_REQUEST = "request only";
    public static final String REQUEST_OBJECT_REQUIRED_REQUEST_URI = "request_uri only";

    public static final String REQUEST_URIS = "request.uris";

    public static final String JWKS_URL = "jwks.url";

    public static final String USE_JWKS_URL = "use.jwks.url";

    public static final String JWKS_STRING = "jwks.string";

    public static final String USE_JWKS_STRING = "use.jwks.string";

    public static final String EXCLUDE_SESSION_STATE_FROM_AUTH_RESPONSE = "exclude.session.state.from.auth.response";
    public static final String EXCLUDE_ISSUER_FROM_AUTH_RESPONSE = "exclude.issuer.from.auth.response";

    public static final String USE_MTLS_HOK_TOKEN = "tls.client.certificate.bound.access.tokens";

    public static final String DPOP_BOUND_ACCESS_TOKENS = "dpop.bound.access.tokens";

    /** ID Token 签名算法。 */
    public static final String ID_TOKEN_SIGNED_RESPONSE_ALG = "id.token.signed.response.alg";

    public static final String ID_TOKEN_ENCRYPTED_RESPONSE_ALG = "id.token.encrypted.response.alg";

    public static final String ID_TOKEN_ENCRYPTED_RESPONSE_ENC = "id.token.encrypted.response.enc";

    /** Access Token 签名算法。 */
    public static final String ACCESS_TOKEN_SIGNED_RESPONSE_ALG = "access.token.signed.response.alg";

    /** Access Token 生命周期（秒）。 */
    public static final String ACCESS_TOKEN_LIFESPAN = "access.token.lifespan";
    public static final String CLIENT_SESSION_IDLE_TIMEOUT = "client.session.idle.timeout";
    public static final String CLIENT_SESSION_MAX_LIFESPAN = "client.session.max.lifespan";
    public static final String CLIENT_OFFLINE_SESSION_IDLE_TIMEOUT = "client.offline.session.idle.timeout";
    public static final String CLIENT_OFFLINE_SESSION_MAX_LIFESPAN = "client.offline.session.max.lifespan";
    /** PKCE code challenge 方法（如 S256）。 */
    public static final String PKCE_CODE_CHALLENGE_METHOD = "pkce.code.challenge.method";

    public static final String TOKEN_ENDPOINT_AUTH_SIGNING_ALG = "token.endpoint.auth.signing.alg";
    public static final String TOKEN_ENDPOINT_AUTH_SIGNING_MAX_EXP = "token.endpoint.auth.signing.max.exp";

    /** 后端通道登出 URL。 */
    public static final String BACKCHANNEL_LOGOUT_URL = "backchannel.logout.url";

    public static final String BACKCHANNEL_LOGOUT_SESSION_REQUIRED = "backchannel.logout.session.required";
    
    public static final String BACKCHANNEL_LOGOUT_REVOKE_OFFLINE_TOKENS = "backchannel.logout.revoke.offline.tokens";

    public static final String LOGOUT_CONFIRMATION_ENABLED = "logout.confirmation.enabled";

    public static final String USE_REFRESH_TOKEN_FOR_CLIENT_CREDENTIALS_GRANT = "client_credentials.use_refresh_token";

    public static final String USE_REFRESH_TOKEN = "use.refresh.tokens";

    public static final String USE_LOWER_CASE_IN_TOKEN_RESPONSE = "token.response.type.bearer.lower-case";

    public static final String USE_RFC9068_ACCESS_TOKEN_HEADER_TYPE = "access.token.header.type.rfc9068";

    public static final String ID_TOKEN_AS_DETACHED_SIGNATURE  = "id.token.as.detached.signature";

    public static final String AUTHORIZATION_SIGNED_RESPONSE_ALG = "authorization.signed.response.alg";
    public static final String AUTHORIZATION_ENCRYPTED_RESPONSE_ALG = "authorization.encrypted.response.alg";
    public static final String AUTHORIZATION_ENCRYPTED_RESPONSE_ENC = "authorization.encrypted.response.enc";
    /** 前端通道登出 URI。 */
    public static final String FRONT_CHANNEL_LOGOUT_URI = "frontchannel.logout.url";
    public static final String FRONT_CHANNEL_LOGOUT_SESSION_REQUIRED = "frontchannel.logout.session.required";

    public static final String POST_LOGOUT_REDIRECT_URIS = "post.logout.redirect.uris";
    
    /** 是否启用标准令牌交换（RFC 8693）。 */
    public static final String STANDARD_TOKEN_EXCHANGE_ENABLED = "standard.token.exchange.enabled";
    public static final String STANDARD_TOKEN_EXCHANGE_REFRESH_ENABLED = "standard.token.exchange.enableRefreshRequestedTokenType";

    /** 是否启用 JWT 授权授予。 */
    public static final String JWT_AUTHORIZATION_GRANT_ENABLED = "oauth2.jwt.authorization.grant.enabled";
    public static final String JWT_AUTHORIZATION_GRANT_IDP = "oauth2.jwt.authorization.grant.idp";
    public static final String JWT_AUTHORIZATION_GRANT_AUDIENCE = "oauth2.jwt.authorization.grant.audience";

    /** 是否启用外部令牌验证。 */
    public static final String EXTERNAL_TOKEN_ENABLED = "external.token.enabled";
    public static final String EXTERNAL_TOKEN_IDP = "external.token.idp";

    public static final String LOGO_URI = "logoUri";
    public static final String TOS_URI = "tosUri";
    public static final String POLICY_URI = "policyUri";
    public static final String SECTOR_IDENTIFIER_URI = "sectorIdentifierUri";

    public static final String ALLOW_TOKEN_INTROSPECTION_WITHOUT_AUDIENCE_CHECK = "allow.token.introspection.without.audience.check";

    public static final String ALLOW_USERINFO_WITH_LIGHTWEIGHT_ACCESS_TOKEN = "allow.userinfo.with.lightweight.access.token";

    /** 工具类，禁止实例化。 */
    private OIDCConfigAttributes() {
    }

}
