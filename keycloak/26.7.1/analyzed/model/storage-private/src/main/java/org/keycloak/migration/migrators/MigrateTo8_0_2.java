/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates
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

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.keycloak.migration.ModelVersion;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.AuthenticationFlowModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.representations.idm.RealmRepresentation;

import org.jboss.logging.Logger;

import static org.keycloak.models.AuthenticationExecutionModel.Requirement.ALTERNATIVE;
import static org.keycloak.models.AuthenticationExecutionModel.Requirement.CONDITIONAL;
import static org.keycloak.models.AuthenticationExecutionModel.Requirement.REQUIRED;

/**
 * 8.0.2 版本迁移：将同级混用的 ALTERNATIVE 执行项拆分到独立子流，以保持与旧版行为一致。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class MigrateTo8_0_2 implements Migration {

    /** 本迁移器对应的模型版本号。 */
    public static final ModelVersion VERSION = new ModelVersion("8.0.2");

    private static final Logger LOG = Logger.getLogger(MigrateTo8_0_2.class);

    @Override
    public ModelVersion getVersion() {
        return VERSION;
    }

    @Override
    public void migrate(KeycloakSession session) {
        session.realms().getRealmsStream().forEach(this::migrateAuthenticationFlowsWithAlternativeRequirements);
    }

    @Override
    public void migrateImport(KeycloakSession session, RealmModel realm, RealmRepresentation rep, boolean skipUserDependent) {
        migrateAuthenticationFlowsWithAlternativeRequirements(realm);
    }


    /** 扫描 realm 中所有认证流，处理 REQUIRED/CONDITIONAL 与 ALTERNATIVE 同级混用的情况。 */
    protected void migrateAuthenticationFlowsWithAlternativeRequirements(RealmModel realm) {
        for (AuthenticationFlowModel flow : realm.getAuthenticationFlowsStream().collect(Collectors.toList())) {
            List<AuthenticationExecutionModel> executions = realm.getAuthenticationExecutionsStream(flow.getId())
                    .collect(Collectors.toList());

            Set<AuthenticationExecutionModel.Requirement> requirements = executions.stream()
                    .map(AuthenticationExecutionModel::getRequirement)
                    .collect(Collectors.toSet());

            // 同级同时存在 REQUIRED/CONDITIONAL 与 ALTERNATIVE 时，将 ALTERNATIVE 拆分到独立子流
            // 以尽量保留旧版认证流语义
            if (requirements.contains(REQUIRED) || requirements.contains(CONDITIONAL) && requirements.contains(ALTERNATIVE)) {

                // 后缀序号仅用于避免子流别名冲突
                AtomicInteger suffix = new AtomicInteger(0);
                LinkedList<AuthenticationExecutionModel> alternativesToMigrate = new LinkedList<>();
                for (AuthenticationExecutionModel execution: executions) {
                    if (AuthenticationExecutionModel.Requirement.ALTERNATIVE.equals(execution.getRequirement())) {
                        alternativesToMigrate.add(execution);
                    }

                    // 遇到 REQUIRED/CONDITIONAL 时，将此前累积的 ALTERNATIVE 执行项迁移到新子流
                    if (REQUIRED.equals(execution.getRequirement()) ||
                            CONDITIONAL.equals(execution.getRequirement())) {
                        if (!alternativesToMigrate.isEmpty()) {
                            migrateAlternatives(realm, flow, alternativesToMigrate, suffix.get());
                            suffix.addAndGet(1);
                            alternativesToMigrate.clear();
                        }
                    }
                }

                if (!alternativesToMigrate.isEmpty()) {
                    migrateAlternatives(realm, flow, alternativesToMigrate, suffix.get());
                }
            }
        }
    }


    /** 将一组 ALTERNATIVE 执行项移入新建子流，并在父流中以 REQUIRED 子流引用替代。 */
    private void migrateAlternatives(RealmModel realm, AuthenticationFlowModel parentFlow,
                                     LinkedList<AuthenticationExecutionModel> alternativesToMigrate, int suffix) {
        LOG.debugf("Migrating %d ALTERNATIVE executions in the flow '%s' of realm '%s' to separate subflow", alternativesToMigrate.size(),
                parentFlow.getAlias(), realm.getName());

        AuthenticationFlowModel newFlow = new AuthenticationFlowModel();
        newFlow.setTopLevel(false);
        newFlow.setBuiltIn(parentFlow.isBuiltIn());
        newFlow.setAlias(parentFlow.getAlias() + " - Alternatives - " + suffix);
        newFlow.setDescription("Subflow of " + parentFlow.getAlias() + " with alternative executions");
        newFlow.setProviderId("basic-flow");
        newFlow = realm.addAuthenticationFlow(newFlow);

        AuthenticationExecutionModel execution = new AuthenticationExecutionModel();
        execution.setParentFlow(parentFlow.getId());
        execution.setRequirement(REQUIRED);
        execution.setFlowId(newFlow.getId());
        // 使用首个 ALTERNATIVE 的优先级，因新子流执行项在父流中实质替代它
        execution.setPriority(alternativesToMigrate.getFirst().getPriority());
        execution.setAuthenticatorFlow(true);
        realm.addAuthenticatorExecution(execution);

        int priority = 0;
        for (AuthenticationExecutionModel ex : alternativesToMigrate) {
            priority += 10;
            ex.setParentFlow(newFlow.getId());
            ex.setPriority(priority);
            realm.updateAuthenticatorExecution(ex);
        }
    }

}
