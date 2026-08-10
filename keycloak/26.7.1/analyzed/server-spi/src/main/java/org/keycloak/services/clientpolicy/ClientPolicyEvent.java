/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.services.clientpolicy;

/**
 * 客户端策略监听的事件类型：OAuth/OIDC/SAML 注册、授权、令牌、登出等生命周期节点。
 *
 * Events on which client policies mechanism detects and do its operation
 * 
 * @author <a href="mailto:takashi.norimatsu.ws@hitachi.com">Takashi Norimatsu</a>
 */
public enum ClientPolicyEvent {

    /** 客户端注册前。 */
    REGISTER,
    /** 客户端注册完成。 */
    REGISTERED,
    /** 客户端更新前。 */
    UPDATE,
    /** 客户端更新完成。 */
    UPDATED,
    /** 查看客户端配置。 */
    VIEW,
    /** 客户端注销。 */
    UNREGISTER,
    /** 授权请求前。 */
    PRE_AUTHORIZATION_REQUEST,
    /** 授权请求处理中。 */
    AUTHORIZATION_REQUEST,
    /** 隐式/混合流令牌响应。 */
    IMPLICIT_HYBRID_TOKEN_RESPONSE,
    /** 令牌请求前。 */
    PRE_TOKEN_REQUEST,
    /** 令牌请求处理中。 */
    TOKEN_REQUEST,
    /** 令牌响应返回前。 */
    TOKEN_RESPONSE,
    /** 服务账户令牌请求。 */
    SERVICE_ACCOUNT_TOKEN_REQUEST,
    /** 服务账户令牌响应。 */
    SERVICE_ACCOUNT_TOKEN_RESPONSE,
    /** 刷新令牌请求。 */
    TOKEN_REFRESH,
    /** 刷新令牌响应。 */
    TOKEN_REFRESH_RESPONSE,
    /** 令牌撤销请求。 */
    TOKEN_REVOKE,
    /** 令牌撤销响应。 */
    TOKEN_REVOKE_RESPONSE,
    /** 令牌 introspect 请求。 */
    TOKEN_INTROSPECT,
    /** UserInfo 请求。 */
    USERINFO_REQUEST,
    /** 登出请求。 */
    LOGOUT_REQUEST,
    /** CIBA 后台认证请求。 */
    BACKCHANNEL_AUTHENTICATION_REQUEST,
    /** CIBA 后台令牌请求。 */
    BACKCHANNEL_TOKEN_REQUEST,
    /** CIBA 后台令牌响应。 */
    BACKCHANNEL_TOKEN_RESPONSE,
    /** PAR 推送授权请求。 */
    PUSHED_AUTHORIZATION_REQUEST,
    /** 设备授权请求。 */
    DEVICE_AUTHORIZATION_REQUEST,
    /** 设备流令牌请求。 */
    DEVICE_TOKEN_REQUEST,
    /** 设备流令牌响应。 */
    DEVICE_TOKEN_RESPONSE,
    /** 令牌交换请求。 */
    TOKEN_EXCHANGE_REQUEST,
    /** 资源所有者密码凭据请求。 */
    RESOURCE_OWNER_PASSWORD_CREDENTIALS_REQUEST,
    /** 资源所有者密码凭据响应。 */
    RESOURCE_OWNER_PASSWORD_CREDENTIALS_RESPONSE,
    /** JWT 授权授予。 */
    JWT_AUTHORIZATION_GRANT,
    /** 身份代理 API 调用。 */
    IDENTITY_BROKERING_API,

    /** SAML 认证请求。 */
    SAML_AUTHN_REQUEST,
    /** SAML 登出请求。 */
    SAML_LOGOUT_REQUEST,
}
