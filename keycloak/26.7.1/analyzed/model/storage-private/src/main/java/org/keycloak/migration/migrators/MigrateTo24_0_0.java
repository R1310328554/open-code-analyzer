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

import org.keycloak.migration.ModelVersion;
import org.keycloak.models.AuthenticationFlowModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.LDAPConstants;
import org.keycloak.models.RealmModel;
import org.keycloak.models.utils.DefaultAuthenticationFlows;
import org.keycloak.models.utils.DefaultKeyProviders;
import org.keycloak.representations.userprofile.config.UPConfig;
import org.keycloak.representations.userprofile.config.UPConfig.UnmanagedAttributePolicy;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.userprofile.UserProfileProvider;

import org.jboss.logging.Logger;

/**
 * 升级至 24.0.0 的域级迁移器：强制启用声明式用户配置、迁移 LDAP 信任库设置、
 * 创建 HS512 密钥组件，并将 First Broker Login 流绑定至域。
 */
public class MigrateTo24_0_0 extends RealmMigration {

    private static final Logger LOG = Logger.getLogger(MigrateTo24_0_0.class);
    /** 目标模型版本 24.0.0。 */
    public static final ModelVersion VERSION = new ModelVersion("24.0.0");
    /** 迁移前标记用户配置是否已启用的域属性名。 */
    public static final String REALM_USER_PROFILE_ENABLED = "userProfileEnabled";


    @Override
    public ModelVersion getVersion() {
        return VERSION;
    }

    @Override
    public void migrateRealm(KeycloakSession session, RealmModel realm) {
        updateUserProfileSettings(session);
        updateLdapProviderConfig(session);
        createHS512ComponentModelKey(session);
        bindFirstBrokerLoginFlow(session);
    }

    /**
     * 自 24.0.0 起声明式用户配置始终启用：移除开关属性；
     * 对未启用过的域默认允许未管理属性以保持行为兼容。
     */
    private void updateUserProfileSettings(KeycloakSession session) {
        RealmModel realm = session.getContext().getRealm();
        boolean isUserProfileEnabled = Boolean.parseBoolean(realm.getAttribute(REALM_USER_PROFILE_ENABLED));

        // 移除属性，因从此版本起用户配置始终启用
        realm.removeAttribute(REALM_USER_PROFILE_ENABLED);

        if (isUserProfileEnabled) {
            // 已启用声明式用户配置的域无需额外迁移
            LOG.debugf("Skipping migration for realm %s. The declarative user profile is already enabled.", realm.getName());
            return;
        }

        // 为保持向后兼容，对未启用声明式用户配置的旧域允许未管理属性
        UserProfileProvider provider = session.getProvider(UserProfileProvider.class);
        UPConfig upConfig = provider.getConfiguration();
        upConfig.setUnmanagedAttributePolicy(UnmanagedAttributePolicy.ENABLED);
        provider.setConfiguration(upConfig);

        LOG.debugf("Enabled the declarative user profile to realm %s with support for unmanaged attributes", realm.getName());
    }

    /** 将 LDAP 组件中 {@code useTruststoreSpi=ldapsOnly} 迁移为 {@code always}。 */
    private void updateLdapProviderConfig(final KeycloakSession session) {
        RealmModel realm = session.getContext().getRealm();
        realm.getComponentsStream(realm.getId(), UserStorageProvider.class.getName())
                .filter(c -> LDAPConstants.USE_TRUSTSTORE_LDAPS_ONLY.equals(c.getConfig().getFirst(LDAPConstants.USE_TRUSTSTORE_SPI)))
                .forEach(c -> {
                    c.getConfig().putSingle(LDAPConstants.USE_TRUSTSTORE_SPI, LDAPConstants.USE_TRUSTSTORE_ALWAYS);
                    realm.updateComponent(c);
                });
    }

    /** 为域创建默认 HS512 对称密钥组件。 */
    private void createHS512ComponentModelKey(KeycloakSession session) {
        RealmModel realm = session.getContext().getRealm();
        DefaultKeyProviders.createSecretProvider(realm);
    }

    /** 将 first broker login 别名流绑定为域的 First Broker Login 流。 */
    private void bindFirstBrokerLoginFlow(KeycloakSession session) {
        RealmModel realm = session.getContext().getRealm();
        String flowAlias = DefaultAuthenticationFlows.FIRST_BROKER_LOGIN_FLOW;
        AuthenticationFlowModel flow = realm.getFlowByAlias(flowAlias);
        if (flow == null) {
           LOG.debugf("No flow found for alias '%s'. Skipping.", flowAlias);
           return;
        }
        realm.setFirstBrokerLoginFlow(flow);
        LOG.debugf("Flow '%s' has been bound to realm %s as 'First broker login' flow", flow.getId(), realm.getName());
    }
}
