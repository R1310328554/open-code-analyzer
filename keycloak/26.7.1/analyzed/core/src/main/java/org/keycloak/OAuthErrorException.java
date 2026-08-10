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

/**
 * OAuth 2.0 / OpenID Connect 协议错误异常，携带标准 {@code error} 与 {@code error_description} 字段。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class OAuthErrorException extends Exception {
    // OAuth 2.0 核心错误码
    public static final String INVALID_REQUEST = "invalid_request";
    public static final String INVALID_SCOPE = "invalid_scope";
    public static final String UNAUTHORIZED_CLIENT = "unauthorized_client";
    public static final String ACCESS_DENIED = "access_denied";
    public static final String UNSUPPORTED_RESPONSE_TYPE = "unsupported_response_type";
    public static final String SERVER_ERROR = "server_error";
    public static final String TEMPORARILY_UNAVAILABLE = "temporarily_unavailable";
    public static final String INVALID_REQUEST_URI = "invalid_request_uri";
    public static final String INVALID_REQUEST_OBJECT = "invalid_request_object";

    // OpenID Connect 1.0 错误码
    public static final String INTERACTION_REQUIRED = "interaction_required";
    public static final String LOGIN_REQUIRED = "login_required";
    public static final String REQUEST_NOT_SUPPORTED = "request_not_supported";
    public static final String REQUEST_URI_NOT_SUPPORTED = "request_uri_not_supported";

    // OAuth 2.0 Bearer Token 使用规范
    public static final String INVALID_TOKEN = "invalid_token";
    public static final String INSUFFICIENT_SCOPE = "insufficient_scope";

    // OIDC 动态客户端注册
    public static final String INVALID_REDIRECT_URI = "invalid_redirect_uri";
    public static final String INVALID_CLIENT_METADATA = "invalid_client_metadata";

    // OAuth 2.0 设备授权流程
    // https://tools.ietf.org/html/draft-ietf-oauth-device-flow-15#section-3.5
    public static final String AUTHORIZATION_PENDING = "authorization_pending";
    public static final String SLOW_DOWN = "slow_down";
    public static final String EXPIRED_TOKEN = "expired_token";

    // CIBA（客户端发起的后端认证）
    public static final String INVALID_BINDING_MESSAGE = "invalid_binding_message";

    // DPoP（Demonstrating Proof-of-Possession）
    public static final String INVALID_DPOP_PROOF = "invalid_dpop_proof";

    // 资源指示器（Resource Indicators）
    public static final String INVALID_TARGET =  "invalid_target";

    // 其他通用 OAuth 错误码
    public static final String INVALID_CLIENT = "invalid_client";
    public static final String INVALID_CLIENT_ATTESTATION = "invalid_client_attestation";
    public static final String INVALID_GRANT = "invalid_grant";
    public static final String UNSUPPORTED_GRANT_TYPE = "unsupported_grant_type";
    public static final String UNSUPPORTED_TOKEN_TYPE = "unsupported_token_type";

    /**
     * @param error OAuth error 代码
     * @param description 人类可读描述
     * @param message 异常消息
     * @param cause 根因
     */
    public OAuthErrorException(String error, String description, String message, Throwable cause) {
        super(message, cause);
        this.error = error;
        this.description = description;
    }

    /**
     * @param error OAuth error 代码
     * @param description 人类可读描述
     * @param message 异常消息
     */
    public OAuthErrorException(String error, String description, String message) {
        super(message);
        this.error = error;
        this.description = description;
    }

    /**
     * @param error OAuth error 代码
     * @param description 同时作为 error_description 与异常消息
     */
    public OAuthErrorException(String error, String description) {
        super(description);
        this.error = error;
        this.description = description;
    }

    /**
     * @param error OAuth error 代码
     * @param description 人类可读描述
     * @param cause 根因
     */
    public OAuthErrorException(String error, String description, Throwable cause) {
        super(description, cause);
        this.error = error;
        this.description = description;
    }

    /** 仅指定 error 代码，消息与 error 相同。 */
    public OAuthErrorException(String error) {
        super(error);
        this.error = error;
    }

    /**
     * @param error OAuth error 代码
     * @param cause 根因
     */
    public OAuthErrorException(String error, Throwable cause) {
        super(error, cause);
        this.error = error;
    }


    /** OAuth {@code error} 字段值。 */
    protected String error;
    /** OAuth {@code error_description} 字段值。 */
    protected String description;

    /** 返回 OAuth error 代码。 */
    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    /** 返回 OAuth error_description。 */
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
