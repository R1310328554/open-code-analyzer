/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.events.admin.v2;


import jakarta.ws.rs.core.UriInfo;

import org.keycloak.common.ClientConnection;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.utils.StripSecretsUtilsV2;
import org.keycloak.services.resources.admin.AdminAuth;
import org.keycloak.services.resources.admin.AdminEventBuilder;

/**
 * Admin API v2 管理事件构建器。
 * <p>
 * 在 v1 {@link AdminEventBuilder} 基础上：
 * <ul>
 *   <li>通过 {@code apiVersion=v2} 详情标记，与 v1 事件区分</li>
 *   <li>序列化表示时使用 {@link StripSecretsUtilsV2} 脱敏（v2 表示层处理方式不同）</li>
 * </ul>
 */
public class AdminEventV2Builder extends AdminEventBuilder {
    /** 事件详情中 API 版本键名。 */
    public static final String API_VERSION_DETAIL_KEY = "apiVersion";
    /** v2 API 版本标识值。 */
    public static final String API_VERSION_V2 = "v2";

    public AdminEventV2Builder(RealmModel realm, AdminAuth auth, KeycloakSession session, ClientConnection clientConnection) {
        super(realm, auth, session, clientConnection);
        // 标记为 v2 API 事件
        detail(API_VERSION_DETAIL_KEY, API_VERSION_V2);
    }

    /** 从请求 URI 提取相对 realm 的管理资源路径。 */
    @Override
    protected String getResourcePath(UriInfo uriInfo) {
        String path = uriInfo.getPath();
        String realmRelative = "/admin/api/%s/".formatted(realm.getName());
        return path.substring(path.indexOf(realmRelative) + realmRelative.length());
    }

    /** 使用 v2 脱敏工具处理表示对象中的密钥字段。 */
    @Override
    protected void stripSecretsFromRepresentation(Object value) {
        StripSecretsUtilsV2.stripSecrets(session, value);
    }
}
