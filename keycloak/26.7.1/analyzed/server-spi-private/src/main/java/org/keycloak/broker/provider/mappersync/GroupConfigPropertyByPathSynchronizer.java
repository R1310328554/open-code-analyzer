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
import org.keycloak.models.GroupModel;

import static org.keycloak.models.utils.KeycloakModelUtils.GROUP_PATH_SEPARATOR;

/**
 * 组路径变更同步器：更新映射器配置中与组路径匹配的 {@link ConfigConstants#GROUP} 引用。
 *
 * Updates a group reference in a mapper config, when the path of a group changes.
 *
 * @author <a href="mailto:daniel.fesenmeyer@bosch.io">Daniel Fesenmeyer</a>
 */
public class GroupConfigPropertyByPathSynchronizer implements ConfigSynchronizer<GroupModel.GroupPathChangeEvent> {

    /** 单例实例。 */
    public static final GroupConfigPropertyByPathSynchronizer INSTANCE = new GroupConfigPropertyByPathSynchronizer();

    private GroupConfigPropertyByPathSynchronizer() {
        // noop
    }

    @Override
    public Class<GroupModel.GroupPathChangeEvent> getEventClass() {
        return GroupModel.GroupPathChangeEvent.class;
    }

    /** 处理 {@link GroupModel.GroupPathChangeEvent}，更新精确匹配与子路径映射器。 */
    @Override
    public void handleEvent(GroupModel.GroupPathChangeEvent event) {

        // 查找组配置属性与变更前路径完全匹配的映射器。
        event.getKeycloakSession().identityProviders().getMappersStream(Map.of(ConfigConstants.GROUP, event.getPreviousPath()), null, null)
                .forEach(idpMapper -> {
                    idpMapper.getConfig().put(ConfigConstants.GROUP, event.getNewPath());
                    logEventProcessed(ConfigConstants.GROUP, event.getPreviousPath(), event.getNewPath(), event.getRealm().getName(),
                            idpMapper.getName(), idpMapper.getIdentityProviderAlias());
                    event.getKeycloakSession().identityProviders().updateMapper(idpMapper);
                });

        // 再查找组配置指向变更路径子路径的映射器并替换前缀。
        event.getKeycloakSession().identityProviders().getMappersStream(
                Map.of(ConfigConstants.GROUP, event.getPreviousPath() + GROUP_PATH_SEPARATOR + "*"), null, null)
                .forEach(idpMapper -> {
                    String currentGroupPath = idpMapper.getConfig().get(ConfigConstants.GROUP);
                    String newGroupPath = event.getNewPath() + currentGroupPath.substring(event.getPreviousPath().length());
                    idpMapper.getConfig().put(ConfigConstants.GROUP, newGroupPath);
                    logEventProcessed(ConfigConstants.GROUP, currentGroupPath, newGroupPath, event.getRealm().getName(),
                            idpMapper.getName(), idpMapper.getIdentityProviderAlias());
                    event.getKeycloakSession().identityProviders().updateMapper(idpMapper);
                });
    }
}
