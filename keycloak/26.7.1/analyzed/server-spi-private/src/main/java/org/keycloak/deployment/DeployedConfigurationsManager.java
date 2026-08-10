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

package org.keycloak.deployment;

import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;

import org.jboss.logging.Logger;

/**
 * 部署配置管理器：统一访问 realm 存储与运行时部署的认证器配置。
 * <p>典型配置保存在 realm 数据库中，也可由部署的扩展在运行时注册，无需持久化。</p>
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class DeployedConfigurationsManager {

    private static final Logger log = Logger.getLogger(DeployedConfigurationsManager.class);

    private final KeycloakSession session;

    /** @param session 当前 Keycloak 会话，用于获取 {@link DeployedConfigurationsProvider} */
    public DeployedConfigurationsManager(KeycloakSession session) {
        this.session = session;
    }

    /** 注册一条部署型认证器配置（不写入数据库）。 */
    public void registerDeployedAuthenticatorConfig(AuthenticatorConfigModel model) {
        log.debugf("Register deployed authenticator config: %s", model.getId());
        session.getProvider(DeployedConfigurationsProvider.class).registerDeployedAuthenticatorConfig(model);
    }

    /** 按 ID 从已部署配置中查找，未找到时返回 {@code null}。 */
    public AuthenticatorConfigModel getDeployedAuthenticatorConfig(String configId) {
        return session.getProvider(DeployedConfigurationsProvider.class).getDeployedAuthenticatorConfigs()
                .filter(config -> configId.equals(config.getId()))
                .findFirst().orElse(null);
    }

    /** 优先返回部署配置，否则回退到 realm 数据库存储。 */
    public AuthenticatorConfigModel getAuthenticatorConfig(RealmModel realm, String configId) {
        AuthenticatorConfigModel cfgModel = getDeployedAuthenticatorConfig(configId);
        if (cfgModel != null) {
            log.tracef("Found deployed configuration by id: %s", configId);
            return cfgModel;
        } else {
            return realm.getAuthenticatorConfigById(configId);
        }
    }


    /** 按别名查找认证器配置，优先部署配置再查 realm。 */
    public AuthenticatorConfigModel getAuthenticatorConfigByAlias(RealmModel realm, String alias) {
        if (alias == null) return null;
        AuthenticatorConfigModel cfgModel = session.getProvider(DeployedConfigurationsProvider.class).getDeployedAuthenticatorConfigs()
                .filter(config -> alias.equals(config.getAlias()))
                .findFirst().orElse(null);
        if (cfgModel != null) {
            log.debugf("Found deployed configuration by alias: %s", alias);
            return cfgModel;
        } else {
            return realm.getAuthenticatorConfigByAlias(alias);
        }
    }

}
