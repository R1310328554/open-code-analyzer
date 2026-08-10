/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.connections.jpa.updater.liquibase.custom;

import liquibase.database.core.MySQLDatabase;
import liquibase.exception.CustomChangeException;
import liquibase.statement.core.RawSqlStatement;

/**
 * 26.0.0 迁移：将组织对应的 {@code KEYCLOAK_GROUP} 行标记为组织类型。
 * <p>若组名与 {@code ORG.ID} 匹配，则将 {@code TYPE} 设为 1，以区分普通组与组织组。</p>
 */
public class JpaUpdate26_0_0_OrganizationGroupType extends CustomKeycloakTask {

    /** 按数据库方言生成 UPDATE，MySQL 需显式 UTF-8 转换以避免字符集比较问题。 */
    @Override
    protected void generateStatementsImpl() throws CustomChangeException {
        String groupTable = getTableName("KEYCLOAK_GROUP");
        String orgTable = getTableName("ORG");

        if (database instanceof MySQLDatabase) {
            // MySQL/MariaDB：CONVERT 保证 NAME 与 ORG.ID 在 utf8 下可比
            statements.add(new RawSqlStatement("UPDATE " + groupTable + " SET TYPE = 1 WHERE CONVERT(NAME USING utf8) IN (SELECT CONVERT(ID USING utf8) FROM " +  orgTable + ")"));
        } else {
            statements.add(new RawSqlStatement("UPDATE " + groupTable + " SET TYPE = 1 WHERE NAME IN (SELECT ID FROM " +  orgTable + ")"));
        }
    }

    @Override
    protected String getTaskId() {
        return "Update type and id for organization groups";
    }

}
