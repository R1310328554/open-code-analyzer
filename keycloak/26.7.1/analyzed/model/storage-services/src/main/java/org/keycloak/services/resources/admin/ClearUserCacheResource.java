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

import jakarta.ws.rs.POST;

import org.keycloak.events.admin.OperationType;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.cache.UserCache;
import org.keycloak.services.resources.admin.fgap.AdminPermissionEvaluator;

/**
 * 清除用户缓存的管理 REST 资源。
 * <p>
 * 通过 {@link UserCache#clear()} 清空集群内全部 Keycloak 实例的用户缓存。
 */
public class ClearUserCacheResource {
    /** 当前操作的 realm。 */
    protected final RealmModel realm;

    /** 管理端权限评估器。 */
    protected final AdminPermissionEvaluator auth;

    /** 管理事件构建器，用于记录操作审计。 */
    protected final AdminEventBuilder adminEvent;

    /** 当前 Keycloak 会话。 */
    protected final KeycloakSession session;

    /** 构造清除用户缓存资源。 */
    public ClearUserCacheResource(KeycloakSession session, AdminPermissionEvaluator auth, AdminEventBuilder adminEvent) {
        this.session = session;
        this.auth = auth;
        this.realm = session.getContext().getRealm();
        this.adminEvent = adminEvent;
    }

    /**
     * 清除用户缓存。
     * <p>
     * 需要 {@code manage-realm} 权限；若 {@link UserCache} 已注册则调用 {@link UserCache#clear()}。
     */
    @POST
    public void clearUserCache() {
        auth.realm().requireManageRealm();

        UserCache cache = session.getProvider(UserCache.class);
        if (cache != null) {
            cache.clear();
        }

        adminEvent.operation(OperationType.ACTION).resourcePath(session.getContext().getUri()).success();
    }

}
