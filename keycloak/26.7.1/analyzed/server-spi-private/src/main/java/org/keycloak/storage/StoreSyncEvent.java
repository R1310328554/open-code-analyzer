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
package org.keycloak.storage;

import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.provider.ProviderEvent;

/**
 * 存储同步事件：通知存储层重新配置用户提供者同步。
 * Event for notifying the store about the need to reconfigure user providers
 * synchronization.
 */
public class StoreSyncEvent implements ProviderEvent {

    /** Keycloak 会话。 */
    private final KeycloakSession session;
    /** 所属 Realm。 */
    private final RealmModel realm;
    /** 是否为移除操作。 */
    private final boolean removed;
    /** 相关组件模型（可为 null）。 */
    private final ComponentModel model;

    /** @param session Keycloak 会话
     * @param realm 所属 Realm
     * @param removed 是否为移除同步配置 */
    public StoreSyncEvent(KeycloakSession session, RealmModel realm, boolean removed) {
        this(session, realm, null, removed);
    }

    /** @param session Keycloak 会话
     * @param realm 所属 Realm
     * @param model 相关组件
     * @param removed 是否为移除操作 */
    public StoreSyncEvent(KeycloakSession session, RealmModel realm, ComponentModel model, boolean removed) {
        this.session = session;
        this.realm = realm;
        this.model = model;
        this.removed = removed;
    }

    /** 发布 Realm 级同步重配置事件。 */
    public static void fire(KeycloakSession session, RealmModel realm, boolean removed) {
        session.getKeycloakSessionFactory().publish(new StoreSyncEvent(session, realm, removed));
    }

    /** 发布组件级同步重配置事件。 */
    public static void fire(KeycloakSession session, RealmModel realm, ComponentModel model, boolean removed) {
        session.getKeycloakSessionFactory().publish(new StoreSyncEvent(session, realm, model, removed));
    }

    /** @return Keycloak 会话 */
    public KeycloakSession getSession() {
        return session;
    }

    /** @return 所属 Realm */
    public RealmModel getRealm() {
        return realm;
    }

    /** @return 相关组件模型 */
    public ComponentModel getModel() {
        return model;
    }

    /** @return 是否为移除操作 */
    public boolean getRemoved() {
        return removed;
    }
}
