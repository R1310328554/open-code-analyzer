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

import org.keycloak.migration.ModelVersion;
import org.keycloak.models.ClientModel;
import org.keycloak.models.Constants;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.representations.idm.RealmRepresentation;

/**
 * 1.2.0 版本迁移：初始化 broker 服务客户端并为内置客户端设置显示名称。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class MigrateTo1_2_0 implements Migration {
    /** 本迁移器对应的模型版本号。 */
    public static final ModelVersion VERSION = new ModelVersion("1.2.0");

    @Override
    public ModelVersion getVersion() {
        return VERSION;
    }

    /** 创建或补全 broker 服务客户端及其角色。 */
    public void setupBrokerService(RealmModel realm) {
        ClientModel client = realm.getClientByClientId(Constants.BROKER_SERVICE_CLIENT_ID);
        if (client == null) {
            client = KeycloakModelUtils.createManagementClient(realm, Constants.BROKER_SERVICE_CLIENT_ID);
            client.setEnabled(true);
            client.setName("${client_" + Constants.BROKER_SERVICE_CLIENT_ID + "}");
            client.setFullScopeAllowed(false);

            for (String role : Constants.BROKER_SERVICE_ROLES) {
                RoleModel roleModel = client.getRole(role);
                if (roleModel != null) continue;
                roleModel = client.addRole(role);
                roleModel.setDescription("${role_" + role.toLowerCase().replaceAll("_", "-") + "}");
            }
        }
    }

    /** 为 account、admin-console、realm-management 等内置客户端设置 i18n 名称。 */
    private void setupClientNames(RealmModel realm) {
        setupClientName(realm.getClientByClientId(Constants.ACCOUNT_MANAGEMENT_CLIENT_ID));
        setupClientName(realm.getClientByClientId(Constants.ADMIN_CONSOLE_CLIENT_ID));
        setupClientName(realm.getClientByClientId(Constants.REALM_MANAGEMENT_CLIENT_ID));
    }

    private void setupClientName(ClientModel client) {
        if (client != null && client.getName() == null) client.setName("${client_" + client.getClientId() + "}");
    }

    /** 对所有 realm 执行 broker 与客户端名称迁移。 */
    public void migrate(KeycloakSession session) {
        session.realms().getRealmsStream().forEach(realm -> {
            setupBrokerService(realm);
            setupClientNames(realm);
        });
    }

    @Override
    public void migrateImport(KeycloakSession session, RealmModel realm, RealmRepresentation rep, boolean skipUserDependent) {
        setupBrokerService(realm);
        setupClientNames(realm);
    }
}
