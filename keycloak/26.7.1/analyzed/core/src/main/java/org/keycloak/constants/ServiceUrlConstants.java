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

package org.keycloak.constants;

/**
 * Keycloak 核心 OpenID Connect / 账户 / 集群管理等 HTTP 服务路径模板常量。
 * 路径中的 {@code {realm-name}} 需替换为具体领域名称。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface ServiceUrlConstants {

    /** OpenID Connect 授权端点路径。 */
    public static final String AUTH_PATH = "/realms/{realm-name}/protocol/openid-connect/auth";
    /** 令牌端点路径。 */
    public static final String TOKEN_PATH = "/realms/{realm-name}/protocol/openid-connect/token";
    /** 登出端点路径。 */
    public static final String TOKEN_SERVICE_LOGOUT_PATH = "/realms/{realm-name}/protocol/openid-connect/logout";
    /** 账户管理页面路径。 */
    public static final String ACCOUNT_SERVICE_PATH = "/realms/{realm-name}/account";
    /** 领域公开信息路径。 */
    public static final String REALM_INFO_PATH = "/realms/{realm-name}";
    /** 客户端集群节点注册路径。 */
    public static final String CLIENTS_MANAGEMENT_REGISTER_NODE_PATH = "/realms/{realm-name}/clients-managements/register-node";
    /** 客户端集群节点注销路径。 */
    public static final String CLIENTS_MANAGEMENT_UNREGISTER_NODE_PATH = "/realms/{realm-name}/clients-managements/unregister-node";
    /** JWKS 公钥集路径。 */
    public static final String JWKS_URL = "/realms/{realm-name}/protocol/openid-connect/certs";
    /** OpenID Connect 发现文档路径。 */
    public static final String DISCOVERY_URL = "/realms/{realm-name}/.well-known/openid-configuration";
    /** UMA 2.0 授权服务发现文档路径。 */
    String AUTHZ_DISCOVERY_URL = "/realms/{realm-name}/.well-known/uma2-configuration";

}
