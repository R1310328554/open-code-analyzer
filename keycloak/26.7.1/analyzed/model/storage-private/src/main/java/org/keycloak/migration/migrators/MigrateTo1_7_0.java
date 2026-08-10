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

import org.keycloak.migration.MigrationProvider;
import org.keycloak.migration.ModelVersion;
import org.keycloak.models.AuthenticationFlowModel;
import org.keycloak.models.Constants;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.IdentityProviderQuery;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.utils.DefaultAuthenticationFlows;
import org.keycloak.representations.idm.RealmRepresentation;

/**
 * 1.7.0 版本迁移：设置隐式流 access token 超时、创建 admin-cli 客户端、
 * 配置 firstBrokerLogin 流并关联到所有身份提供者。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class MigrateTo1_7_0 implements Migration {

    /** 本迁移器对应的模型版本号。 */
    public static final ModelVersion VERSION = new ModelVersion("1.7.0");

    public ModelVersion getVersion() {
        return VERSION;
    }

    public void migrate(KeycloakSession session) {
        RealmModel sessionRealm = session.getContext().getRealm();
        session.realms().getRealmsStream().forEach(realm -> {
            session.getContext().setRealm(realm);
            migrateRealm(session, realm);
        });
        session.getContext().setRealm(sessionRealm);
    }

    @Override
    public void migrateImport(KeycloakSession session, RealmModel realm, RealmRepresentation rep, boolean skipUserDependent) {
        RealmModel sessionRealm = session.getContext().getRealm();
        session.getContext().setRealm(realm);
        migrateRealm(session, realm);
        session.getContext().setRealm(sessionRealm);
    }

    /** 对单个 realm 设置隐式流超时、admin-cli 及 firstBrokerLogin 流。 */
    protected void migrateRealm(KeycloakSession session, RealmModel realm) {
        // 为隐式流设置默认 access token 超时
        realm.setAccessTokenLifespanForImplicitFlow(Constants.DEFAULT_ACCESS_TOKEN_LIFESPAN_FOR_IMPLICIT_FLOW_TIMEOUT);

        // 添加内置 admin-cli 客户端
        MigrationProvider migrationProvider = session.getProvider(MigrationProvider.class);
        migrationProvider.setupAdminCli(realm);

        // 添加 firstBrokerLogin 流并关联到所有身份提供者
        DefaultAuthenticationFlows.migrateFlows(realm);
        AuthenticationFlowModel firstBrokerLoginFlow = realm.getFlowByAlias(DefaultAuthenticationFlows.FIRST_BROKER_LOGIN_FLOW);

        session.identityProviders().getAllStream(IdentityProviderQuery.userAuthentication().with(IdentityProviderModel.FIRST_BROKER_LOGIN_FLOW_ID, ""), null, null)
                    .forEach(provider -> {
                        provider.setFirstBrokerLoginFlowId(firstBrokerLoginFlow.getId());
                        session.identityProviders().update(provider);
                });
    }
}
