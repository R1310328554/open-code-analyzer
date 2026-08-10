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
import org.keycloak.partialimport.PartialImportResults;
import org.keycloak.provider.ProviderEvent;
import org.keycloak.representations.idm.PartialImportRepresentation;

/**
 * 部分导入 Realm 表示的提供者事件（已弃用）。
 * <p>触发对已有 Realm 的部分导入完成逻辑。</p>
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
public class PartialImportRealmFromRepresentationEvent implements ProviderEvent {
    /** Keycloak 会话。 */
    private final KeycloakSession session;
    /** 部分导入表示。 */
    private final PartialImportRepresentation rep;
    /** 目标 Realm。 */
    private final RealmModel realm;

    /** 部分导入结果。 */
    private PartialImportResults partialImportResults;

    /** @param session Keycloak 会话
     * @param rep 部分导入表示
     * @param realm 目标 Realm */
    public PartialImportRealmFromRepresentationEvent(KeycloakSession session, PartialImportRepresentation rep, RealmModel realm) {
        this.session = session;
        this.rep = rep;
        this.realm = realm;
    }

    /** 发布事件并返回部分导入结果。 */
    public static PartialImportResults fire(KeycloakSession session, PartialImportRepresentation rep, RealmModel realm) {
        PartialImportRealmFromRepresentationEvent event = new PartialImportRealmFromRepresentationEvent(session, rep, realm);
        session.getKeycloakSessionFactory().publish(event);
        return event.getPartialImportResults();
    }

    /** @return Keycloak 会话 */
    public KeycloakSession getSession() {
        return session;
    }

    /** @return 部分导入表示 */
    public PartialImportRepresentation getRep() {
        return rep;
    }

    /** 设置部分导入结果。 */
    public void setPartialImportResults(PartialImportResults partialImportResults) {
        this.partialImportResults = partialImportResults;
    }

    /** @return 部分导入结果 */
    public PartialImportResults getPartialImportResults() {
        return partialImportResults;
    }

    /** @return 目标 Realm */
    public RealmModel getRealm() {
        return realm;
    }
}
