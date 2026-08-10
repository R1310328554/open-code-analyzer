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

package org.keycloak.models.workflow;


import java.util.function.BiFunction;

import org.keycloak.events.Event;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;

/**
 * 工作流可操作的 realm 资源类型枚举。
 * <p>每种类型提供按 ID 解析资源对象，以及从 {@link org.keycloak.events.Event} 提取资源 ID 的策略。</p>
 */
public enum ResourceType {

    /** 用户资源：通过 {@code userId} 查找 {@link org.keycloak.models.UserModel}。 */
    USERS(
            (session, id) -> session.users().getUserById(session.getContext().getRealm(), id),
            (session, event) -> event.getUserId()
    ),
    /** 客户端资源：通过内部 ID 查找 {@link org.keycloak.models.ClientModel}。 */
    CLIENTS(
            (session, id) -> session.clients().getClientById(session.getContext().getRealm(), id),
            (session, event) -> findClientResourceId(session, event.getClientId())
    );

    private final BiFunction<KeycloakSession, String, ?> resourceResolver;
    private final BiFunction<KeycloakSession, Event, String> resourceIdResolver;

    ResourceType(BiFunction<KeycloakSession, String, ?> resourceResolver,
                 BiFunction<KeycloakSession, Event, String> resourceIdResolver) {
        this.resourceResolver = resourceResolver;
        this.resourceIdResolver = resourceIdResolver;
    }

    /**
     * 按资源 ID 解析为领域对象。
     * @param session Keycloak 会话
     * @param id 资源 ID
     * @return 解析后的资源对象
     */
    public Object resolveResource(KeycloakSession session, String id) {
        return resourceResolver.apply(session, id);
    }

    /**
     * 从用户事件中提取本资源类型对应的资源 ID。
     * @param session Keycloak 会话
     * @param event 用户事件
     * @return 资源 ID，无法解析时返回 {@code null}
     */
    public String resolveResourceId(KeycloakSession session, Event event) {
        return resourceIdResolver.apply(session, event);
    }

    /** 将事件中的 clientId 转换为客户端内部 ID。 */
    private static String findClientResourceId(KeycloakSession session, String clientClientId) {
        RealmModel realm = session.getContext().getRealm();
        if (realm == null) {
            return null;
        }

        ClientModel client = realm.getClientByClientId(clientClientId);
        if (client == null) {
            return null;
        }

        return client.getId();
    }
}
