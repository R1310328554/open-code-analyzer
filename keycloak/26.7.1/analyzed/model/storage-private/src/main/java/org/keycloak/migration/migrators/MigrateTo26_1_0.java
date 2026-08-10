/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
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
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;

/**
 * 升级至 26.1.0 的域级迁移器：添加 OIDC {@code service_account} 客户端作用域，并迁移认证会话存储。
 *
 * @author rmartinc
 */
public class MigrateTo26_1_0 extends RealmMigration {
    /** 目标模型版本 26.1.0。 */
    public static final ModelVersion VERSION = new ModelVersion("26.1.0");

    @Override
    public ModelVersion getVersion() {
        return VERSION;
    }

    @Override
    public void migrateRealm(KeycloakSession session, RealmModel realm) {
        // 向域添加新的 service_account 客户端作用域
        MigrationProvider migrationProvider = session.getProvider(MigrationProvider.class);
        migrationProvider.addOIDCServiceAccountClientScope(realm);

        session.authenticationSessions().migrate(VERSION.toString());
    }
}
