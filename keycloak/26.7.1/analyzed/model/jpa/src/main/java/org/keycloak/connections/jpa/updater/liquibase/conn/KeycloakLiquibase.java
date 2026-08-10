/*
 * Copyright 2023 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.connections.jpa.updater.liquibase.conn;

import liquibase.Liquibase;
import liquibase.UpdateSummaryOutputEnum;
import liquibase.database.Database;
import liquibase.resource.ResourceAccessor;

/**
 * Keycloak 定制的 {@link Liquibase} 子类，公开受保护的 API 并统一迁移摘要输出。
 */
public class KeycloakLiquibase extends Liquibase {

    /** 创建实例并将更新摘要写入日志。 */
    public KeycloakLiquibase(String changeLogFile, ResourceAccessor resourceAccessor, Database database) {
        super(changeLogFile, resourceAccessor, database);
        this.setShowSummaryOutput(UpdateSummaryOutputEnum.LOG);
    }

    /** 公开父类受保护的 resetServices，避免反射调用。 */
    @Override
    public void resetServices() {
        super.resetServices();
    }
}
