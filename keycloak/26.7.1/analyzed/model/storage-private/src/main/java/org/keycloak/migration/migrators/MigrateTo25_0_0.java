/*
 * Copyright 2023 Red Hat, Inc. and/or its affiliates
 *  and other contributors as indicated by the @author tags.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.keycloak.migration.migrators;


import org.keycloak.migration.MigrationProvider;
import org.keycloak.migration.ModelVersion;
import org.keycloak.models.ClientScopeModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.utils.KeycloakModelUtils;

import org.jboss.logging.Logger;

/**
 * 升级至 25.0.0 的域级迁移器：触发持久化用户会话存储格式迁移，并为各域创建 OIDC {@code basic} 客户端作用域。
 *
 * @author <a href="mailto:ggrazian@redhat.com">Giuseppe Graziano</a>
 */
public class MigrateTo25_0_0 extends RealmMigration {

    /** 目标模型版本 25.0.0。 */
    public static final ModelVersion VERSION = new ModelVersion("25.0.0");

    private static final Logger LOG = Logger.getLogger(MigrateTo25_0_0.class);

    @Override
    public ModelVersion getVersion() {
        return VERSION;
    }

    @Override
    public void migrate(KeycloakSession session) {
        // 存储模型测试中可能为 null
        if (session.sessions() != null) {
            // KC25 引入的持久化用户会话格式迁移
            session.sessions().migrate(VERSION.toString());
        }

        super.migrate(session);
    }

    @Override
    public void migrateRealm(KeycloakSession session, RealmModel realm) {
        MigrationProvider migrationProvider = session.getProvider(MigrationProvider.class);

        ClientScopeModel basicScope = KeycloakModelUtils.getClientScopeByName(realm, "basic");
        if (basicScope == null) {
            // 在域中创建 basic 客户端作用域
            basicScope = migrationProvider.addOIDCBasicClientScope(realm);

            // 将 basic 作用域作为默认作用域添加到全部现有 OIDC 客户端
            session.clients().addClientScopeToAllClients(realm, basicScope, true);
        } else {
            LOG.warnf("Client scope '%s' already exists in the realm '%s'. Please migrate this realm manually if you need basic claims in your tokens.", basicScope.getName(), realm.getName());
        }

    }
}
