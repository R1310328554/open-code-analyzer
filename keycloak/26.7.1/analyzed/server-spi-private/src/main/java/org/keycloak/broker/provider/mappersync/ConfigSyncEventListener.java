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

import java.util.Arrays;
import java.util.List;

import org.keycloak.provider.ProviderEvent;
import org.keycloak.provider.ProviderEventListener;

import org.jboss.logging.Logger;

/**
 * 映射器配置同步事件监听器：当组路径、客户端 ID 或角色名变更时更新映射器引用。
 *
 * Event listener which synchronizes mapper configs, when references change.
 *
 * @author <a href="mailto:daniel.fesenmeyer@bosch.io">Daniel Fesenmeyer</a>
 */
public final class ConfigSyncEventListener implements ProviderEventListener {

    private static final Logger LOG = Logger.getLogger(ConfigSyncEventListener.class);

    private static final List<ConfigSynchronizer<? extends ProviderEvent>> SYNCHRONIZERS =
            Arrays.asList(GroupConfigPropertyByPathSynchronizer.INSTANCE,
                    RoleConfigPropertyByClientIdSynchronizer.INSTANCE, RoleConfigPropertyByRoleNameSynchronizer.INSTANCE);

    /** 遍历全部 {@link ConfigSynchronizer}，将匹配事件分派给对应处理器。 */
    @Override
    public void onEvent(ProviderEvent event) {
        for (ConfigSynchronizer<? extends ProviderEvent> s : SYNCHRONIZERS) {
            ConfigSynchronizer<ProviderEvent> configSynchronizer = (ConfigSynchronizer<ProviderEvent>) s;

            if (eventMatchesSynchronizer(event, configSynchronizer)) {
                LOG.debugf("Synchronizer %s matches event: %s", configSynchronizer, event);
                configSynchronizer.handleEvent(event);
            } else {
                LOG.debugf("Synchronizer %s does not match event: %s", configSynchronizer, event);
            }
        }
    }

    /** 判断事件类型是否为同步器声明的处理类型。 */
    private static boolean eventMatchesSynchronizer(ProviderEvent event,
            ConfigSynchronizer<? extends ProviderEvent> synchronizer) {
        Class<? extends ProviderEvent> handledClass = synchronizer.getEventClass();
        return (handledClass.isAssignableFrom(event.getClass()));
    }

}
