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

package org.keycloak.quarkus.runtime.storage.database.liquibase;

import liquibase.logging.Logger;
import liquibase.logging.core.AbstractLogService;

/**
 * Liquibase {@link liquibase.logging.LogService} 实现，创建 {@link KeycloakLogger} 实例并桥接到 JBoss Logging。
 *
 * @author <a href="mailto:sguilhen@redhat.com">Stefan Guilhen</a>
 */
public class KeycloakLogService extends AbstractLogService {

    /** 略高于默认优先级，使 Keycloak 日志服务覆盖 Liquibase 内置实现。 */
    @Override
    public int getPriority() {
        return PRIORITY_DEFAULT + 1;
    }

    @Override
    public Logger getLog(Class clazz) {
        return new KeycloakLogger(clazz);
    }
}
