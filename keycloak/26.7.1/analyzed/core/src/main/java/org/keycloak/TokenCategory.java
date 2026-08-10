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
 * Keycloak 令牌的语义分类枚举，用于标识令牌在协议流程中的角色。
 */
public enum TokenCategory {
    /** 内部用途令牌。 */
    INTERNAL,
    /** OAuth/OIDC 访问令牌。 */
    ACCESS,
    /** OpenID Connect ID 令牌。 */
    ID,
    /** 管理 API 访问令牌。 */
    ADMIN,
    /** UserInfo 端点返回的令牌。 */
    USERINFO,
    /** 登出流程使用的令牌。 */
    LOGOUT,
    /** 授权响应中携带的令牌。 */
    AUTHORIZATION_RESPONSE
}
