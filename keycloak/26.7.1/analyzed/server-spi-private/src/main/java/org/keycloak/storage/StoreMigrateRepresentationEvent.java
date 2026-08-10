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
import org.keycloak.representations.idm.RealmRepresentation;

/**
 * 存储迁移表示事件：通知存储层按需迁移 Realm 表示。
 * <p>注意：本事件会直接修改表示对象，属特殊行为。</p>
 * Event for notifying the store, so it can do migrations on the representation as needed.
 *
 * CAUTION: This event is exceptional as it performs any necessary modification of the representation.
 */
public class StoreMigrateRepresentationEvent implements ProviderEvent {

    /** Keycloak 会话。 */
    private final KeycloakSession session;
    /** 目标 Realm。 */
    private final RealmModel realm;
    /** 待迁移的 Realm 表示。 */
    private final RealmRepresentation rep;
    /** 是否跳过与用户相关的迁移。 */
    private final boolean skipUserDependent;

    /** @param session Keycloak 会话
     * @param realm 目标 Realm
     * @param rep Realm 表示
     * @param skipUserDependent 是否跳过用户相关迁移 */
    public StoreMigrateRepresentationEvent(KeycloakSession session, RealmModel realm, RealmRepresentation rep, boolean skipUserDependent) {
        this.session = session;
        this.realm = realm;
        this.rep = rep;
        this.skipUserDependent = skipUserDependent;
    }

    /** 发布存储迁移表示事件。 */
    public static void fire(KeycloakSession session, RealmModel realm, RealmRepresentation rep, boolean skipUserDependent) {
        session.getKeycloakSessionFactory().publish(new StoreMigrateRepresentationEvent(session, realm, rep, skipUserDependent));
    }

    /** @return Keycloak 会话 */
    public KeycloakSession getSession() {
        return session;
    }

    /** @return 目标 Realm */
    public RealmModel getRealm() {
        return realm;
    }

    /** @return Realm 表示 */
    public RealmRepresentation getRep() {
        return rep;
    }

    /** @return 是否跳过与用户相关的迁移 */
    public boolean isSkipUserDependent() {
        return skipUserDependent;
    }
}
