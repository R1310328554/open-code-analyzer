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
 * 从 Realm 表示完成导入的提供者事件（已弃用）。
 * <p>UI/REST 导入可能仅含 Realm 名称与启用状态的最小 JSON，本事件触发后续完整导入。</p>
 * Event to trigger that will complete the import for a given realm representation.
 * <p />
 * This event was created as the import of a JSON via the UI/REST API can be called using a JSON representation that contains
 * only the name of the realm and if it is enabled.
 * <p />
 * In the future, this might not be needed if this is done when the legacy store migration is complete and the functionality
 * is bundled within the map storage.
 *
 * @author Alexander Schwartz
 */
@Deprecated
public class ImportRealmFromRepresentationEvent implements ProviderEvent {
    /** Keycloak 会话。 */
    private final KeycloakSession session;
    /** 待导入的 Realm 表示。 */
    private final RealmRepresentation realmRepresentation;

    /** 导入完成后设置的 Realm 模型。 */
    private RealmModel realmModel;

    /** @param session Keycloak 会话
     * @param realmRepresentation Realm 表示 */
    public ImportRealmFromRepresentationEvent(KeycloakSession session, RealmRepresentation realmRepresentation) {
        this.session = session;
        this.realmRepresentation = realmRepresentation;
    }

    /** 发布事件并返回导入后的 {@link RealmModel}。 */
    public static RealmModel fire(KeycloakSession session, RealmRepresentation rep) {
        ImportRealmFromRepresentationEvent event = new ImportRealmFromRepresentationEvent(session, rep);
        session.getKeycloakSessionFactory().publish(event);
        return event.getRealmModel();
    }

    /** @return Keycloak 会话 */
    public KeycloakSession getSession() {
        return session;
    }

    /** @return Realm 表示 */
    public RealmRepresentation getRealmRepresentation() {
        return realmRepresentation;
    }

    /** 设置导入完成后的 Realm 模型。 */
    public void setRealmModel(RealmModel realmModel) {
        this.realmModel = realmModel;
    }

    /** @return 导入后的 Realm 模型 */
    public RealmModel getRealmModel() {
        return realmModel;
    }
}
