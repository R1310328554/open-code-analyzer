/*
 * Copyright 2025 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.services.resources.admin;

import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.RealmModel;
import org.keycloak.services.resources.admin.ext.AdminRealmResourceProvider;
import org.keycloak.services.resources.admin.ext.AdminRealmResourceProviderFactory;
import org.keycloak.services.resources.admin.fgap.AdminPermissionEvaluator;

/**
 * CRL 缓存清除的 Realm 级管理扩展工厂与 Provider 实现。
 * <p>
 * 在管理控制台注册 {@code clear-crl-cache} 子资源，挂载 {@link ClearCrlCacheResource}。
 */
public class ClearCrlCacheRealmAdminProvider implements AdminRealmResourceProviderFactory, AdminRealmResourceProvider  {

    @Override
    /** 创建 Provider 实例（本实现返回自身）。 */
    public AdminRealmResourceProvider create(KeycloakSession session) {
        return this;
    }

    @Override
    /** 初始化配置（无额外配置项）。 */
    public void init(Config.Scope config) {

    }

    @Override
    /** 会话工厂后置初始化。 */
    public void postInit(KeycloakSessionFactory factory) {

    }

    @Override
    /** 关闭资源。 */
    public void close() {

    }

    @Override
    /** 返回管理扩展标识 {@code clear-crl-cache}。 */
    public String getId() {
        return "clear-crl-cache";
    }

    @Override
    /** 返回 CRL 缓存清除 REST 资源实例。 */
    public Object getResource(KeycloakSession session, RealmModel realm, AdminPermissionEvaluator auth, AdminEventBuilder adminEvent) {
        return new ClearCrlCacheResource(session, auth, adminEvent);
    }
}
