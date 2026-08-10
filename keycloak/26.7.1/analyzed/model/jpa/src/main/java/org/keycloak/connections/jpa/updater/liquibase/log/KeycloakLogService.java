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

package org.keycloak.connections.jpa.updater.liquibase.log;

import liquibase.logging.Logger;
import liquibase.logging.core.AbstractLogService;

/**
 * Keycloak 定制的 {@link liquibase.logging.LogService}，为 Liquibase 提供 {@link KeycloakLogger} 实例。
 * <p>将 Liquibase 日志桥接到 JBoss Logging，并与 Keycloak 现有日志级别策略对齐。</p>
 *
 * @author <a href="mailto:sguilhen@redhat.com">Stefan Guilhen</a>
 */
public class KeycloakLogService extends AbstractLogService {

    /** 略高于默认优先级，确保 Keycloak 日志服务被 Liquibase 选中。 */
    @Override
    public int getPriority() {
        return PRIORITY_DEFAULT + 1;
    }

    /** @param clazz 请求日志器的 Liquibase 组件类 */
    @Override
    public Logger getLog(Class clazz) {
        return new KeycloakLogger(clazz);
    }
}
