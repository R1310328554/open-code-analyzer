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

import java.util.Map;

import org.keycloak.authorization.model.Policy;
import org.keycloak.authorization.model.Resource;
import org.keycloak.models.ClientModel;
import org.keycloak.models.IdentityProviderModel;

/**
 * 身份提供者细粒度管理权限接口。
 * <p>管理 IdP 资源、权限策略及客户端到 IdP 的令牌交换授权。</p>
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface IdentityProviderPermissionManagement {
    /** IdP 是否已启用细粒度管理权限 */
    boolean isPermissionsEnabled(IdentityProviderModel idp);

    /** 启用或禁用 IdP 细粒度管理权限 */
    void setPermissionsEnabled(IdentityProviderModel idp, boolean enable);

    /** 获取 IdP 授权资源 */
    Resource resource(IdentityProviderModel idp);

    /** 获取 IdP 作用域策略 ID 映射 */
    Map<String, String> getPermissions(IdentityProviderModel idp);

    /** 授权客户端是否可向该 IdP 交换令牌 */
    boolean canExchangeTo(ClientModel authorizedClient, IdentityProviderModel to);

    /** 获取 IdP 令牌交换权限策略 */
    Policy exchangeToPermission(IdentityProviderModel idp);
}
