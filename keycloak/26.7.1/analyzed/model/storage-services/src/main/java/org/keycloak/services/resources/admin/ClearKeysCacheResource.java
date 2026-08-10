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
import org.keycloak.models.cache.CachePublicKeyProvider;
import org.keycloak.services.resources.admin.fgap.AdminPermissionEvaluator;

/**
 * 清除外部公钥缓存的管理 REST 资源。
 * <p>
 * 清空客户端或身份提供者（IdP）外部公钥的缓存。
 */
public class ClearKeysCacheResource {

    /** 管理端权限评估器。 */
    protected final AdminPermissionEvaluator auth;
    /** 当前操作的 realm。 */
    protected final RealmModel realm;
    /** 管理事件构建器，用于记录操作审计。 */
    private final AdminEventBuilder adminEvent;

    /** 当前 Keycloak 会话。 */
    protected final KeycloakSession session;

    /** 构造外部公钥缓存清除资源。 */
    public ClearKeysCacheResource(KeycloakSession session, AdminPermissionEvaluator auth, AdminEventBuilder adminEvent) {
        this.session = session;
        this.auth = auth;
        this.realm = session.getContext().getRealm();
        this.adminEvent = adminEvent;
    }

    /**
     * 清除外部公钥缓存（客户端或身份提供者的公钥）。
     * <p>
     * 需要 {@code manage-realm} 权限；若 {@link CachePublicKeyProvider} 已注册则调用 {@link CachePublicKeyProvider#clearCache()}。
     */
    @POST
    public void clearKeysCache() {
        auth.realm().requireManageRealm();

        CachePublicKeyProvider cache = session.getProvider(CachePublicKeyProvider.class);
        if (cache != null) {
            cache.clearCache();
        }

        adminEvent.operation(OperationType.ACTION).resourcePath(session.getContext().getUri()).success();
    }
}
