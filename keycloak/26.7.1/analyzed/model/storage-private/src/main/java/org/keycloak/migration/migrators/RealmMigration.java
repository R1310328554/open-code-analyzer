/*
 * Copyright 2025 Red Hat, Inc. and/or its affiliates
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

import org.keycloak.connections.jpa.support.EntityManagers;
import org.keycloak.models.KeycloakContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.representations.idm.RealmRepresentation;

import org.jboss.logging.Logger;

/**
 * 按 realm 逐域执行迁移的抽象基类：切换会话上下文、刷新持久化层后调用 {@link #migrateRealm}。
 */
public abstract class RealmMigration implements Migration {

    /** 迁移过程日志记录器。 */
    protected static final Logger LOG = Logger.getLogger(RealmMigration.class);

    @Override
    public void migrate(KeycloakSession session) {
        session.realms().getRealmsStream().forEach(realm -> {
            // 每个 realm 迁移前清空持久化上下文，避免实体状态污染
            EntityManagers.flush(session, true);
            // 亦可使用 EntityManagers.runInBatch，但会改变查询模式，此处不适用
            KeycloakContext context = session.getContext();
            RealmModel oldRealm = session.getContext().getRealm();
            RealmModel mutableRealm = session.realms().getRealmByName(realm.getName());
            try {
                context.setRealm(mutableRealm);
                migrateRealm(session, mutableRealm);
                LOG.infof("migrated realm %s to %s", realm.getName(), getVersion());
            } finally {
                context.setRealm(oldRealm);
            }
        });
    }

    @Override
    public void migrateImport(KeycloakSession session, RealmModel realm, RealmRepresentation rep,
            boolean skipUserDependent) {
        migrateRealm(session, realm);
    }

    /** 子类实现：对单个 realm 执行版本特定的迁移逻辑。 */
    public abstract void migrateRealm(KeycloakSession session, RealmModel realm);
}
