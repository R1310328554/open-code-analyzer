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

import java.util.Optional;

import org.keycloak.authentication.AuthenticationFlow;
import org.keycloak.component.ComponentModel;
import org.keycloak.migration.ModelVersion;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.userprofile.UserProfileProvider;

import org.jboss.logging.Logger;

/**
 * 升级至 23.0.0 的域级迁移器：合并分片式用户配置组件为单一 JSON，并移除注册表单中的 profile 执行器。
 */
public class MigrateTo23_0_0 extends RealmMigration {

    private static final Logger LOG = Logger.getLogger(MigrateTo23_0_0.class);

    /** 目标模型版本 23.0.0。 */
    public static final ModelVersion VERSION = new ModelVersion("23.0.0");

    /** 域属性：是否启用声明式用户配置（迁移前）。 */
    private static final String USER_PROFILE_ENABLED_PROP = "userProfileEnabled";
    private static final String UP_PIECES_COUNT_COMPONENT_CONFIG_KEY = "config-pieces-count";
    private static final String UP_PIECE_COMPONENT_CONFIG_KEY_BASE = "config-piece-";
    private static final String UP_COMPONENT_CONFIG_KEY = "kc.user.profile.config";

    @Override
    public void migrateRealm(KeycloakSession session, RealmModel realm) {
        updateUserProfileConfig(realm);
        removeRegistrationProfileFormExecution(realm);
    }

    /**
     * 将旧版分片存储的 {@link UserProfileProvider} 组件配置合并为 {@code kc.user.profile.config} 单键。
     * 若分片数为 0 则移除空组件。
     */
    private void updateUserProfileConfig(RealmModel realm) {
        if (realm.getAttribute(USER_PROFILE_ENABLED_PROP, Boolean.FALSE)) {

            Optional<ComponentModel> component = realm.getComponentsStream(realm.getId(), UserProfileProvider.class.getName()).findAny();
            if (component.isPresent()) {
                ComponentModel userProfileComponent = component.get();
                int count = userProfileComponent.get(UP_PIECES_COUNT_COMPONENT_CONFIG_KEY, 0);
                if (count < 1) {
                    realm.removeComponent(userProfileComponent);
                    return;
                }
                userProfileComponent.getConfig().remove(UP_PIECES_COUNT_COMPONENT_CONFIG_KEY);
                String configuration;
                if (count == 1) {
                    configuration = userProfileComponent.get(UP_PIECE_COMPONENT_CONFIG_KEY_BASE + "0");
                    userProfileComponent.getConfig().remove(UP_PIECE_COMPONENT_CONFIG_KEY_BASE + "0");
                } else {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < count; i++) {
                        String v = userProfileComponent.get(UP_PIECE_COMPONENT_CONFIG_KEY_BASE + i);
                        userProfileComponent.getConfig().remove(UP_PIECE_COMPONENT_CONFIG_KEY_BASE + i);
                        if (v != null) sb.append(v);
                    }
                    configuration = sb.toString();
                }
                userProfileComponent.getConfig().putSingle(UP_COMPONENT_CONFIG_KEY, configuration);
                realm.updateComponent(userProfileComponent);
            }
        }
    }

    /** 从所有表单型注册流中移除 {@code registration-profile-action} 执行器。 */
    private void removeRegistrationProfileFormExecution(RealmModel realm) {
        realm.getAuthenticationFlowsStream()
                .filter(flow -> AuthenticationFlow.FORM_FLOW.equals(flow.getProviderId()))
                .forEach(registrationFlow -> {
                    realm.getAuthenticationExecutionsStream(registrationFlow.getId())
                            .filter(authExecution -> "registration-profile-action".equals(authExecution.getAuthenticator()))
                            .forEach(registrationProfileExecution -> {
                                realm.removeAuthenticatorExecution(registrationProfileExecution);
                                LOG.debugf("Removed 'registration-profile-action' form action from authentication flow '%s' in the realm '%s'.", registrationFlow.getAlias(), realm.getName());
                            });
                });

    }

    @Override
    public ModelVersion getVersion() {
        return VERSION;
    }
}
