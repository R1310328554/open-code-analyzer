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

package org.keycloak.storage.ldap;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.storage.ldap.idm.store.ldap.LDAPIdentityStore;
import org.keycloak.storage.ldap.mappers.LDAPConfigDecorator;

import org.jboss.logging.Logger;

/**
 * 按 LDAP 组件 ID 缓存 {@link LDAPConfig}，并在配置变更时重建 {@link org.keycloak.storage.ldap.idm.store.ldap.LDAPIdentityStore}。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class LDAPIdentityStoreRegistry {

    private static final Logger logger = Logger.getLogger(LDAPIdentityStoreRegistry.class);

    private final Map<String, LDAPConfig> ldapStores = new ConcurrentHashMap<>();

    /**
     * 获取或创建与给定 LDAP 组件对应的 {@link org.keycloak.storage.ldap.idm.store.ldap.LDAPIdentityStore}。
     * 会先应用 mapper 提供的 {@link org.keycloak.storage.ldap.mappers.LDAPConfigDecorator} 再比较缓存。
     */
    public LDAPIdentityStore getLdapStore(KeycloakSession session, ComponentModel ldapModel, Map<ComponentModel, LDAPConfigDecorator> configDecorators) {
        // realm 中 LDAP 配置可能已变更，需重新构建 LDAPConfig
        MultivaluedHashMap<String, String> configModel = ldapModel.getConfig();
        LDAPConfig ldapConfig = new LDAPConfig(configModel);
        for (Map.Entry<ComponentModel, LDAPConfigDecorator> entry : configDecorators.entrySet()) {
            ComponentModel mapperModel = entry.getKey();
            LDAPConfigDecorator decorator = entry.getValue();

            decorator.updateLDAPConfig(ldapConfig, mapperModel);
        }

        LDAPConfig cachedConfig = ldapStores.get(ldapModel.getId());
        if (cachedConfig == null || !ldapConfig.equals(cachedConfig)) {
            logLDAPConfig(session, ldapModel, ldapConfig);
            ldapStores.put(ldapModel.getId(), ldapConfig);
        }

        return new LDAPIdentityStore(session, ldapConfig);
    }

    // 日志中不输出 bind 密码（toString 已剔除 BIND_CREDENTIAL）
    private void logLDAPConfig(KeycloakSession session, ComponentModel ldapModel, LDAPConfig ldapConfig) {
        logger.infof("Creating new LDAP Store for the LDAP storage provider: '%s', LDAP Configuration: %s", ldapModel.getName(), ldapConfig.toString());

        if (logger.isDebugEnabled()) {
            RealmModel realm = session.realms().getRealm(ldapModel.getParentId());
            realm.getComponentsStream(ldapModel.getId()).forEach(c ->
                    logger.debugf("Mapper for provider: %s, Mapper name: %s, Provider: %s, Mapper configuration: %s",
                            ldapModel.getName(), c.getName(), c.getProviderId(), c.getConfig().toString()));
        }
    }
}
