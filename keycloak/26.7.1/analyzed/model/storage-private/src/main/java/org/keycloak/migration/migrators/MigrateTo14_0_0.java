/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates
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
 *
 */

package org.keycloak.migration.migrators;

import org.keycloak.migration.ModelVersion;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ModelException;
import org.keycloak.models.RealmModel;
import org.keycloak.representations.idm.ClientPoliciesRepresentation;
import org.keycloak.representations.idm.ClientProfilesRepresentation;
import org.keycloak.services.clientpolicy.ClientPolicyException;

/**
 * 14.0.0 版本迁移：初始化各 realm 的客户端配置（Client Profiles）与策略（Client Policies）。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class MigrateTo14_0_0 implements Migration {

    /** 本迁移器对应的模型版本号。 */
    public static final ModelVersion VERSION = new ModelVersion("14.0.0");

    @Override
    public void migrate(KeycloakSession session) {
        session.realms()
                .getRealmsStream()
                .forEach(realm -> migrateRealm(session, realm));
    }

    /** 对单个 realm 写入空的客户端配置与策略表示（触发默认初始化）。 */
    private void migrateRealm(KeycloakSession session, RealmModel realm) {
        try {
            session.clientPolicy().updateClientProfiles(realm, new ClientProfilesRepresentation());
            session.clientPolicy().updateClientPolicies(realm, new ClientPoliciesRepresentation());
        } catch (ClientPolicyException cpe) {
            throw new ModelException("Exception during migration client profiles or client policies", cpe);
        }
    }

    @Override
    public ModelVersion getVersion() {
        return VERSION;
    }
}
