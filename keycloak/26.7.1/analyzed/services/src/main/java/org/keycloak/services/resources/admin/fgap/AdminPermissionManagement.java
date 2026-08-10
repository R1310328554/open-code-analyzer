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
package org.keycloak.services.resources.admin.fgap;

import org.keycloak.authorization.AuthorizationProvider;
import org.keycloak.authorization.model.ResourceServer;
import org.keycloak.models.ClientModel;

/**
 * 管理端细粒度权限管理门面。
 * <p>提供角色/用户/组/客户端/身份提供者等资源的授权策略与资源服务器访问。</p>
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface AdminPermissionManagement {
    /** 管理作用域名称 */
    public static final String MANAGE_SCOPE = "manage";
    /** 查看作用域名称 */
    public static final String VIEW_SCOPE = "view";
    /** 令牌交换作用域名称 */
    public static final String TOKEN_EXCHANGE ="token-exchange";

    /** 获取领域级 admin permissions 客户端 */
    ClientModel getRealmPermissionsClient();

    /** 授权服务提供者 */
    AuthorizationProvider authz();

    /** 角色权限管理 */
    RolePermissionManagement roles();
    /** 用户权限管理 */
    UserPermissionManagement users();
    /** 组权限管理 */
    GroupPermissionManagement groups();
    /** 客户端权限管理 */
    ClientPermissionManagement clients();
    /** 身份提供者权限管理 */
    IdentityProviderPermissionManagement idps();

    /** 领域资源服务器 */
    ResourceServer realmResourceServer();
}
