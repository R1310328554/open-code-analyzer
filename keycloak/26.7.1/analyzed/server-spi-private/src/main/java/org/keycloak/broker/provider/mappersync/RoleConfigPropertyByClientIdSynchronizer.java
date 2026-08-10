/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.broker.provider.mappersync;

import java.util.Map;

import org.keycloak.broker.provider.ConfigConstants;
import org.keycloak.models.ClientModel;
import org.keycloak.models.utils.KeycloakModelUtils;

/**
 * 客户端 ID 变更同步器：更新映射器 {@link ConfigConstants#ROLE} 中与该客户端关联的角色引用。
 *
 * Updates a role reference in a mapper config, when a client ID changes.
 *
 * @author <a href="mailto:daniel.fesenmeyer@bosch.io">Daniel Fesenmeyer</a>
 */
public class RoleConfigPropertyByClientIdSynchronizer implements ConfigSynchronizer<ClientModel.ClientIdChangeEvent> {

    /** 单例实例。 */
    public static final RoleConfigPropertyByClientIdSynchronizer INSTANCE =
            new RoleConfigPropertyByClientIdSynchronizer();

    private RoleConfigPropertyByClientIdSynchronizer() {
        // noop
    }

    @Override
    public Class<ClientModel.ClientIdChangeEvent> getEventClass() {
        return ClientModel.ClientIdChangeEvent.class;
    }

    /** 处理 {@link ClientModel.ClientIdChangeEvent}，重建角色限定符中的客户端 ID 部分。 */
    @Override
    public void handleEvent(ClientModel.ClientIdChangeEvent event) {
        // 查找角色配置指向变更客户端下角色的映射器。
        event.getKeycloakSession().identityProviders().getMappersStream(Map.of(ConfigConstants.ROLE, event.getPreviousClientId() + ".*"), null, null)
                .forEach(idpMapper -> {
                    String currentRoleValue = idpMapper.getConfig().get(ConfigConstants.ROLE);
                    String configuredRoleName = KeycloakModelUtils.parseRole(currentRoleValue)[1];
                    String newRoleValue = KeycloakModelUtils.buildRoleQualifier(event.getNewClientId(), configuredRoleName);
                    idpMapper.getConfig().put(ConfigConstants.ROLE, newRoleValue);
                    logEventProcessed(ConfigConstants.ROLE, currentRoleValue, newRoleValue, event.getUpdatedClient().getRealm().getName(),
                            idpMapper.getName(), idpMapper.getIdentityProviderAlias());
                    event.getKeycloakSession().identityProviders().updateMapper(idpMapper);
                });
    }
}
