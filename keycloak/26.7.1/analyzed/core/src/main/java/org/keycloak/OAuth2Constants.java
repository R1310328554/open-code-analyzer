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

package org.keycloak;

import org.keycloak.jose.jws.Algorithm;

import static org.keycloak.jose.jws.Algorithm.PS256;

/**
 * OAuth 2.0、OpenID Connect 及扩展规范（PKCE、Token Exchange、DPoP、OID4VCI 等）中使用的参数名与常量值。
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public interface OAuth2Constants {

    /** 授权码参数名。 */
    String CODE = "code";

    /** 隐式流 token 响应类型片段。 */
    String TOKEN = "token";

    String CLIENT_ID = "client_id";

    String CLIENT_SECRET = "client_secret";

    String ERROR = "error";

    String ERROR_DESCRIPTION = "error_description";

    String REDIRECT_URI = "redirect_uri";

    String POST_LOGOUT_REDIRECT_URI = "post_logout_redirect_uri";

    String ID_TOKEN_HINT = "id_token_hint";

    String DISPLAY = "display";

    String SCOPE = "scope";

    String STATE = "state";

    String GRANT_TYPE = "grant_type";

    String RESPONSE_TYPE = "response_type";

    String ACCESS_TOKEN = "access_token";

    String TOKEN_TYPE = "token_type";

    String EXPIRES_IN = "expires_in";

    String ID_TOKEN = "id_token";

    String REFRESH_TOKEN = "refresh_token";

    String LOGOUT_TOKEN = "logout_token";

    /** 授权码 grant type 值。 */
    String AUTHORIZATION_CODE = "authorization_code";


    /** 隐式授权 response_type 值。 */
    String IMPLICIT = "implicit";

    String USERNAME="username";

    String PASSWORD = "password";

    /** 客户端凭证 grant type。 */
    String CLIENT_CREDENTIALS = "client_credentials";

    /** JWT Bearer 授权 grant type（RFC 7523）。 */
    String JWT_AUTHORIZATION_GRANT = "urn:ietf:params:oauth:grant-type:jwt-bearer";
    String ASSERTION = "assertion";

    // https://datatracker.ietf.org/doc/draft-ietf-oauth-identity-assertion-authz-grant/
    /** 身份断言 JWT 的 typ 头值。 */
    String IDENTITY_ASSERTION_JWT_HEADER_TYPE = "oauth-id-jag+jwt";


    // https://tools.ietf.org/html/draft-ietf-oauth-assertions-01#page-5
    /** 客户端断言类型与断言体参数名。 */
    String CLIENT_ASSERTION_TYPE = "client_assertion_type";
    String CLIENT_ASSERTION = "client_assertion";

    // https://tools.ietf.org/html/draft-jones-oauth-jwt-bearer-03#section-2.2
    /** JWT Bearer 客户端断言类型 URN。 */
    String CLIENT_ASSERTION_TYPE_JWT = "urn:ietf:params:oauth:client-assertion-type:jwt-bearer";

    // http://openid.net/specs/openid-connect-core-1_0.html#OfflineAccess
    /** 离线访问 scope，用于获取 refresh token。 */
    String OFFLINE_ACCESS = "offline_access";

    // http://openid.net/specs/openid-connect-core-1_0.html#AuthRequest
    /** OpenID Connect 核心 scope。 */
    String SCOPE_OPENID = "openid";

    // http://openid.net/specs/openid-connect-core-1_0.html#ScopeClaims
    String SCOPE_PROFILE = "profile";
    String SCOPE_EMAIL = "email";
    String SCOPE_ADDRESS = "address";
    String SCOPE_PHONE = "phone";

    /** 组织相关 scope 与声明。 */
    String ORGANIZATION = "organization";
    String ORGANIZATION_ID = "id";

    /** UI 语言偏好参数。 */
    String UI_LOCALES_PARAM = "ui_locales";

    String PROMPT = "prompt";
    String ACR_VALUES = "acr_values";

    String MAX_AGE = "max_age";

    // OIDC 会话管理
    String SESSION_STATE = "session_state";

    String JWT = "JWT";

    // https://tools.ietf.org/html/rfc7636#section-6.1 — PKCE
    String CODE_VERIFIER = "code_verifier";
    String CODE_CHALLENGE = "code_challenge";
    String CODE_CHALLENGE_METHOD = "code_challenge_method";

    // https://tools.ietf.org/html/rfc7636#section-6.2.2
    String PKCE_METHOD_PLAIN = "plain";
    String PKCE_METHOD_S256 = "S256";

    // https://tools.ietf.org/html/rfc8693#section-2.1 — Token Exchange
    String TOKEN_EXCHANGE_GRANT_TYPE="urn:ietf:params:oauth:grant-type:token-exchange";
    String AUDIENCE="audience";
    String RESOURCE="resource";
    String REQUESTED_SUBJECT="requested_subject";
    String SUBJECT_TOKEN="subject_token";
    String SUBJECT_TOKEN_TYPE="subject_token_type";
    String ACTOR_TOKEN="actor_token";
    String ACTOR_TOKEN_TYPE="actor_token_type";
    String REQUESTED_TOKEN_TYPE="requested_token_type";
    String ISSUED_TOKEN_TYPE="issued_token_type";
    String REQUESTED_ISSUER="requested_issuer";
    String SUBJECT_ISSUER="subject_issuer";
    String ACCESS_TOKEN_TYPE="urn:ietf:params:oauth:token-type:access_token";
    String REFRESH_TOKEN_TYPE="urn:ietf:params:oauth:token-type:refresh_token";
    String JWT_TOKEN_TYPE="urn:ietf:params:oauth:token-type:jwt";
    String ID_TOKEN_TYPE="urn:ietf:params:oauth:token-type:id_token";
    String SAML2_TOKEN_TYPE = "urn:ietf:params:oauth:token-type:saml2";

    /** UMA ticket grant type。 */
    String UMA_GRANT_TYPE = "urn:ietf:params:oauth:grant-type:uma-ticket";

    // https://tools.ietf.org/html/draft-ietf-oauth-device-flow-15#section-3.4 — 设备授权
    String DEVICE_CODE_GRANT_TYPE = "urn:ietf:params:oauth:grant-type:device_code";
    String DEVICE_CODE = "device_code";

    /** CIBA grant type。 */
    String CIBA_GRANT_TYPE = "urn:openid:params:grant-type:ciba";

    String INTERVAL = "interval";
    String USER_CODE = "user_code";

    // https://openid.net/specs/openid-financial-api-jarm-ID1.html — JARM 响应模式
    String RESPONSE = "response";

    // https://www.rfc-editor.org/rfc/rfc9207.html — 授权响应中的 iss
    String ISSUER = "iss";

    String AUTHENTICATOR_METHOD_REFERENCE = "amr";

    String CNF = "cnf";

    // RAR — https://datatracker.ietf.org/doc/html/rfc9396
    // 在授权请求 URL 参数与令牌响应声明中使用
    String AUTHORIZATION_DETAILS = "authorization_details";

    // DPoP — https://datatracker.ietf.org/doc/html/rfc9449
    String DPOP_HTTP_HEADER = "DPoP";
    String DPOP_NONCE_HEADER = "DPoP-Nonce";
    /** DPoP 证明 JWT 默认签名算法。 */
    Algorithm DPOP_DEFAULT_ALGORITHM = PS256;
    String DPOP_JWT_HEADER_TYPE = "dpop+jwt";
    String ALGS_ATTRIBUTE = "algs";

    // OID4VCI — https://openid.net/specs/openid-4-verifiable-credential-issuance-1_0.html#section-5.1.3
    // 用于将 issuer_state 回传给凭证签发者
    String ISSUER_STATE = "issuer_state";
}
