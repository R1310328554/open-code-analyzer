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

package org.keycloak.authentication;

/**
 * 认证器、表单认证器或 FormAction 可抛出的流程错误码集合。
 *
 * Set of error codes that can be thrown by an Authenticator, FormAuthenticator, or FormAction
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public enum AuthenticationFlowError {
    /** 验证码或链接已过期。 */
    EXPIRED_CODE,
    /** 客户端会话无效。 */
    INVALID_CLIENT_SESSION,
    /** 用户无效或不存在于当前上下文。 */
    INVALID_USER,
    /** 凭证校验失败。 */
    INVALID_CREDENTIALS,
    /** 需要先配置凭证。 */
    CREDENTIAL_SETUP_REQUIRED,
    /** 用户已禁用。 */
    USER_DISABLED,
    /** 用户冲突（如重复绑定）。 */
    USER_CONFLICT,
    /** 用户被临时锁定。 */
    USER_TEMPORARILY_DISABLED,
    /** 内部服务器错误。 */
    INTERNAL_ERROR,
    /** 未知用户。 */
    UNKNOWN_USER,
    /** 流程已分叉到新分支。 */
    FORK_FLOW,
    /** 未知客户端。 */
    UNKNOWN_CLIENT,
    /** 客户端未找到。 */
    CLIENT_NOT_FOUND,
    /** 客户端已禁用。 */
    CLIENT_DISABLED,
    /** 客户端需配置凭证。 */
    CLIENT_CREDENTIALS_SETUP_REQUIRED,
    /** 客户端凭证无效。 */
    INVALID_CLIENT_CREDENTIALS,
    /** 客户端 attestation 无效。 */
    INVALID_CLIENT_ATTESTATION,

    /** 身份提供者未找到。 */
    IDENTITY_PROVIDER_NOT_FOUND,
    /** 身份提供者已禁用。 */
    IDENTITY_PROVIDER_DISABLED,
    /** 身份提供者返回错误。 */
    IDENTITY_PROVIDER_ERROR,
    /** 当前展示方式不受支持。 */
    DISPLAY_NOT_SUPPORTED,

    /** 访问被拒绝。 */
    ACCESS_DENIED,
    /** 通用认证错误。 */
    GENERIC_AUTHENTICATION_ERROR
}
