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
 *
 */

package org.keycloak.migration.migrators;

import org.keycloak.common.Profile;
import org.keycloak.migration.MigrationProvider;
import org.keycloak.migration.ModelVersion;
import org.keycloak.models.ClientScopeModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.representations.idm.RealmRepresentation;

import org.jboss.logging.Logger;

/**
 * 18.0.0 版本迁移：在启用 STEP_UP_AUTHENTICATION 特性时为各 realm 创建 acr 客户端作用域。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class MigrateTo18_0_0 implements Migration {

    /** 本迁移器对应的模型版本号。 */
    public static final ModelVersion VERSION = new ModelVersion("18.0.0");

    private static final Logger LOG = Logger.getLogger(MigrateTo18_0_0.class);

    @Override
    public ModelVersion getVersion() {
        return VERSION;
    }

    @Override
    public void migrate(KeycloakSession session) {
        if (Profile.isFeatureEnabled(Profile.Feature.STEP_UP_AUTHENTICATION)) {
            session.realms().getRealmsStream().forEach(realm -> migrateRealm(session, realm));
        }
    }

    @Override
    public void migrateImport(KeycloakSession session, RealmModel realm, RealmRepresentation rep, boolean skipUserDependent) {
        migrateRealm(session, realm);
    }

    /** 对单个 realm 创建 acr 默认客户端作用域并关联到所有 OIDC 客户端。 */
    protected void migrateRealm(KeycloakSession session, RealmModel realm) {
        if (Profile.isFeatureEnabled(Profile.Feature.STEP_UP_AUTHENTICATION)) {
            MigrationProvider migrationProvider = session.getProvider(MigrationProvider.class);

            ClientScopeModel acrScope = KeycloakModelUtils.getClientScopeByName(realm, "acr");
            if (acrScope == null) {
                // 在 realm 中创建默认 acr 客户端作用域
                acrScope = migrationProvider.addOIDCAcrClientScope(realm);

                // 将 acr 作用域添加到所有现有 OIDC 客户端
                session.clients().addClientScopeToAllClients(realm, acrScope, true);
            }
        }
    }
}
