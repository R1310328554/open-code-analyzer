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

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.provider.ProviderEvent;

/**
 * 为新导入 Realm 设置默认值的提供者事件。
 * Event to trigger that will add defaults for a realm after it has been imported.
 *
 * @author Alexander Schwartz
 */
public class SetDefaultsForNewRealm implements ProviderEvent {
    /** Keycloak 会话。 */
    private final KeycloakSession session;
    /** 新 Realm 模型。 */
    private final RealmModel realmModel;

    /** @param session Keycloak 会话
     * @param realmModel 新 Realm 模型 */
    public SetDefaultsForNewRealm(KeycloakSession session, RealmModel realmModel) {
        this.session = session;
        this.realmModel = realmModel;
    }

    /** 发布事件，为 Realm 添加默认配置。 */
    public static void fire(KeycloakSession session, RealmModel realm) {
        SetDefaultsForNewRealm event = new SetDefaultsForNewRealm(session, realm);
        session.getKeycloakSessionFactory().publish(event);
    }

    /** @return Keycloak 会话 */
    public KeycloakSession getSession() {
        return session;
    }

    /** @return Realm 模型 */
    public RealmModel getRealmModel() {
        return realmModel;
    }
}
