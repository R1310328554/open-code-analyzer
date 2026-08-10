/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.migration;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.storage.DatastoreProvider;
import org.keycloak.storage.datastore.DefaultDatastoreProvider;

/**
 * 迁移模型管理器：委托 {@link DefaultDatastoreProvider} 执行数据库模式与 realm 数据迁移。
 * <p>
 * 提供全局迁移与 realm 导入时迁移两种入口。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class MigrationModelManager {

    /** 执行全局数据库迁移，按版本顺序依次运行各 {@link org.keycloak.migration.migrators.Migration} 实现。 */
    public static void migrate(KeycloakSession session) {
        ((DefaultDatastoreProvider) session.getProvider(DatastoreProvider.class)).getMigrationManager().migrate();
    }

    /**
     * 在 realm 导入时执行迁移逻辑。
     *
     * @param skipUserDependent 为 true 时跳过依赖用户数据的迁移步骤
     */
    public static void migrateImport(KeycloakSession session, RealmModel realm, RealmRepresentation rep, boolean skipUserDependent) {
        ((DefaultDatastoreProvider) session.getProvider(DatastoreProvider.class)).getMigrationManager().migrate(realm, rep, skipUserDependent);
    }

}
