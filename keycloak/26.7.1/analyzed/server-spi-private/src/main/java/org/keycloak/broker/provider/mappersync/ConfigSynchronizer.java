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

import org.keycloak.provider.ProviderEvent;

import org.jboss.logging.Logger;

/**
 * 映射器配置同步器：当组路径、角色名等引用变更时更新 IdP 映射器配置。
 *
 * Interface for updating references in mapper configs, when references (like group path) change.
 *
 * @author <a href="mailto:daniel.fesenmeyer@bosch.io">Daniel Fesenmeyer</a>
 */
public interface ConfigSynchronizer<T extends ProviderEvent> {
    Logger LOG = Logger.getLogger(ConfigSynchronizer.class);

    /** 返回此同步器处理的 {@link ProviderEvent} 类型。 */
    Class<T> getEventClass();

    /** 处理匹配事件并更新相关映射器配置。 */
    void handleEvent(T event);

    /** 记录配置引用从旧值到新值的调整日志。 */
    default void logEventProcessed(String configPropertyName, String previousValue, String newValue, String realmName,
                                     String mapperName, String idpAlias) {
        LOG.infof(
                "Reference of type '%s' changed from '%s' to '%s' in realm '%s'. Adjusting the reference from mapper '%s' of IDP '%s'.",
                configPropertyName, previousValue, newValue, realmName, mapperName, idpAlias);

    }
}
