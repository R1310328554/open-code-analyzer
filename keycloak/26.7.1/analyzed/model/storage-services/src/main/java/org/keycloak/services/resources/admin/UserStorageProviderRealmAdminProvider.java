/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates
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

import org.keycloak.Config.Scope;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.RealmModel;
import org.keycloak.services.resources.admin.ext.AdminRealmResourceProvider;
import org.keycloak.services.resources.admin.ext.AdminRealmResourceProviderFactory;
import org.keycloak.services.resources.admin.fgap.AdminPermissionEvaluator;

/**
 * 用户存储 Provider 的 Realm 级管理扩展工厂与 Provider 实现。
 * <p>
 * 在管理控制台注册 {@code user-storage} 子资源，挂载 {@link UserStorageProviderResource}。
 */
public class UserStorageProviderRealmAdminProvider implements AdminRealmResourceProviderFactory, AdminRealmResourceProvider {

    /** 创建 Provider 实例（本实现返回自身）。 */
    @Override
    public AdminRealmResourceProvider create(KeycloakSession session) {
        return this;
    }

    /** 初始化配置（无额外配置项）。 */
    @Override
    public void init(Scope config) {
    }

    /** 会话工厂后置初始化。 */
    @Override
    public void postInit(KeycloakSessionFactory factory) {
    }

    /** 关闭资源。 */
    @Override
    public void close() {
    }

    /** 返回管理扩展标识 {@code user-storage}。 */
    @Override
    public String getId() {
        return "user-storage";
    }

    /** 返回用户存储 Provider 管理 REST 资源实例。 */
    @Override
    public Object getResource(KeycloakSession session, RealmModel realm, AdminPermissionEvaluator auth, AdminEventBuilder adminEvent) {
        return new UserStorageProviderResource(session, auth, adminEvent);
    }

}
