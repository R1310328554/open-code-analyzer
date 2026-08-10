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

package org.keycloak.migration.migrators;


import java.util.stream.Collectors;

import org.keycloak.migration.ModelVersion;
import org.keycloak.models.ClientModel;
import org.keycloak.models.Constants;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.representations.idm.RealmRepresentation;

/**
 * 升级至 3.4.2 的模型迁移器：修正 admin-cli 与 admin-console 客户端的作用域映射（补做 3.2.0 遗漏项）。
 *
 * @author <a href="mailto:bruno@abstractj.org">Bruno Oliveira</a>
 */
public class MigrateTo3_4_2 implements Migration {

    /** 目标模型版本 3.4.2。 */
    public static final ModelVersion VERSION = new ModelVersion("3.4.2");

    @Override
    public void migrate(KeycloakSession session) {
        session.realms().getRealmsStream().forEach(this::migrateRealm);
    }

    @Override
    public void migrateImport(KeycloakSession session, RealmModel realm, RealmRepresentation rep, boolean skipUserDependent) {
        migrateRealm(realm);
    }

    /** 清理 admin-cli 与 admin-console 的全作用域许可及角色映射。 */
    protected void migrateRealm(RealmModel realm) {
        // 本应在 3_2_0 完成的迁移修复
        ClientModel cli = realm.getClientByClientId(Constants.ADMIN_CLI_CLIENT_ID);
        clearScope(cli);
        ClientModel console = realm.getClientByClientId(Constants.ADMIN_CONSOLE_CLIENT_ID);
        clearScope(console);

    }

    /** 禁用全作用域并删除所有作用域映射。 */
    private void clearScope(ClientModel cli) {
        if (cli.isFullScopeAllowed()) cli.setFullScopeAllowed(false);
        cli.getScopeMappingsStream().collect(Collectors.toList()).forEach(cli::deleteScopeMapping);
    }

    @Override
    public ModelVersion getVersion() {
        return VERSION;
    }

}
