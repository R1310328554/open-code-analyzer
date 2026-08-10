/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.migration.migrators;

import org.keycloak.migration.MigrationProvider;
import org.keycloak.migration.ModelVersion;
import org.keycloak.models.ClientScopeModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.representations.idm.RealmRepresentation;

import org.jboss.logging.Logger;


/**
 * 4.6.0 版本迁移：创建 {@code roles} 与 {@code web-origins} 默认客户端作用域并关联到所有 OIDC 客户端。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class MigrateTo4_6_0 implements Migration {

    /** 本迁移器对应的模型版本号。 */
    public static final ModelVersion VERSION = new ModelVersion("4.6.0");

    private static final Logger LOG = Logger.getLogger(MigrateTo4_6_0.class);

    @Override
    public ModelVersion getVersion() {
        return VERSION;
    }

    @Override
    public void migrate(KeycloakSession session) {
        session.realms().getRealmsStream().forEach(realm -> migrateRealm(session, realm, false));
    }

    @Override
    public void migrateImport(KeycloakSession session, RealmModel realm, RealmRepresentation rep, boolean skipUserDependent) {
        migrateRealm(session, realm, true);
    }

    /** 为单个 realm 创建默认 OIDC 客户端作用域并绑定到非 Bearer-only 的 OIDC 客户端。 */
    protected void migrateRealm(KeycloakSession session, RealmModel realm, boolean json) {
        MigrationProvider migrationProvider = session.getProvider(MigrationProvider.class);

        // 创建 roles 与 web-origins 默认客户端作用域
        ClientScopeModel rolesScope = migrationProvider.addOIDCRolesClientScope(realm);
        ClientScopeModel webOriginsScope = migrationProvider.addOIDCWebOriginsClientScope(realm);

        LOG.debugf("Added '%s' and '%s' default client scopes", rolesScope.getName(), webOriginsScope.getName());

        // 将 roles 与 web-origins 作为默认作用域关联到所有 OIDC 客户端
        realm.getClientsStream()
                .filter(MigrationUtils::isOIDCNonBearerOnlyClient)
                .forEach(c -> {
                    c.addClientScope(rolesScope, true);
                    c.addClientScope(webOriginsScope, true);
                });

        LOG.debugf("Client scope '%s' assigned to all the clients", rolesScope.getName());
    }
}
