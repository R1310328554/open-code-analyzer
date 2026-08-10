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

import org.keycloak.authorization.model.ResourceServer;

/**
 * 单个领域内管理 REST 资源的权限评估接口。
 * <p>涵盖领域配置、身份提供者、授权服务、事件、Required Actions、认证流等资源的查看与管理权限。</p>
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface RealmPermissionEvaluator {
    /** 是否可列出可访问的领域名称。 */
    boolean canListRealms();

    /** 要求可查看领域名称列表，否则抛出 403。 */
    void requireViewRealmNameList();

    /** 是否可管理领域配置。 */
    boolean canManageRealm();

    /** 要求领域管理权限，否则抛出 403。 */
    void requireManageRealm();

    /** 是否可查看领域配置。 */
    boolean canViewRealm();

    /** 要求领域查看权限，否则抛出 403。 */
    void requireViewRealm();

    /** 是否可管理身份提供者。 */
    boolean canManageIdentityProviders();

    /** 是否可查看身份提供者。 */
    boolean canViewIdentityProviders();

    /** 要求身份提供者查看权限，否则抛出 403。 */
    void requireViewIdentityProviders();

    /** 要求身份提供者管理权限，否则抛出 403。 */
    void requireManageIdentityProviders();

    /** 是否可管理指定 {@link ResourceServer} 的授权配置。 */
    boolean canManageAuthorization(ResourceServer resourceServer);

    /** 是否可查看指定 {@link ResourceServer} 的授权配置。 */
    boolean canViewAuthorization(ResourceServer resourceServer);

    /** 要求授权管理权限，否则抛出 403。 */
    void requireManageAuthorization(ResourceServer resourceServer);

    /** 要求授权查看权限，否则抛出 403。 */
    void requireViewAuthorization(ResourceServer resourceServer);

    /** 是否可管理领域事件。 */
    boolean canManageEvents();

    /** 要求事件管理权限，否则抛出 403。 */
    void requireManageEvents();

    /** 是否可查看领域事件。 */
    boolean canViewEvents();

    /** 要求事件查看权限，否则抛出 403。 */
    void requireViewEvents();

    /** 要求查看 Required Actions 的权限，否则抛出 403。 */
    void requireViewRequiredActions();

    /** 要求查看认证流的权限，否则抛出 403。 */
    void requireViewAuthenticationFlows();

    /** 要求查看客户端认证 Provider 的权限，否则抛出 403。 */
    void requireViewClientAuthenticatorProviders();
}
