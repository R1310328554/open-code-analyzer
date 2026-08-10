/*
 * Copyright 2026 Red Hat, Inc. and/or its affiliates
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

import liquibase.database.core.MSSQLDatabase;
import liquibase.database.core.MySQLDatabase;
import liquibase.database.core.OracleDatabase;
import liquibase.database.core.PostgresDatabase;
import liquibase.exception.CustomChangeException;
import liquibase.statement.core.RawParameterizedSqlStatement;

/**
 * 26.6.0 迁移：回填 {@code OFFLINE_CLIENT_SESSION.REALM_ID}。
 * <p>从关联的 {@code OFFLINE_USER_SESSION} 按 {@code user_session_id} 与 {@code offline_flag} 复制 realm_id；按数据库方言选用最优 UPDATE 语法。</p>
 */
public class JpaUpdate26_6_0_OfflineClientSessionRealm extends CustomKeycloakTask {

    /** 按数据库类型分派到各厂商专用或通用 SQL 更新语句。 */
    @Override
    protected void generateStatementsImpl() throws CustomChangeException {
        String clientSessionTable = getTableName("OFFLINE_CLIENT_SESSION");
        String userSessionTable = getTableName("OFFLINE_USER_SESSION");

        if (database instanceof PostgresDatabase) {
            generateUpdateQueryForPostgresSQL(clientSessionTable, userSessionTable);
            return;
        }

        if (database instanceof MySQLDatabase) {
            // and MariaDB
            generateUpdateQueryForMySQL(clientSessionTable, userSessionTable);
            return;
        }

        if (database instanceof MSSQLDatabase) {
            generateUpdateQueryForMSSQL(clientSessionTable, userSessionTable);
            return;
        }

        if (database instanceof OracleDatabase) {
            generateUpdateQueryForOracle(clientSessionTable, userSessionTable);
            return;
        }

        // H2 等：标准相关子查询，复杂度 O(n²) 但兼容性最广
        // It is standard SQL queries, it *must* be compatible with all vendors (fingers crossed)
        generateUpdateQueryUsingStandardSQL(clientSessionTable, userSessionTable);
    }

    @Override
    protected String getTaskId() {
        return "Sets the realm column in offline_client_session";
    }

    /** 标准 SQL：子查询回填 realm_id，仅更新 NULL 行。 */
    private void generateUpdateQueryUsingStandardSQL(String clientSessionTable, String userSessionTable) {
        statements.add(new RawParameterizedSqlStatement("""
                UPDATE %s cs
                SET cs.realm_id = (
                    SELECT us.realm_id
                    FROM %s us
                    WHERE us.user_session_id = cs.user_session_id AND cs.offline_flag = us.offline_flag
                )
                WHERE cs.realm_id IS NULL"""
                .formatted(clientSessionTable, userSessionTable)));
    }

    /** Oracle MERGE 语法批量关联更新。 */
    private void generateUpdateQueryForOracle(String clientSessionTable, String userSessionTable) {
        statements.add(new RawParameterizedSqlStatement("""
                MERGE INTO %s cs
                USING %s us
                ON (cs.user_session_id = us.user_session_id AND cs.offline_flag = us.offline_flag)
                WHEN MATCHED THEN
                UPDATE SET cs.realm_id = us.realm_id"""
                .formatted(clientSessionTable, userSessionTable)));
    }

    /** SQL Server UPDATE … FROM … JOIN 语法。 */
    private void generateUpdateQueryForMSSQL(String clientSessionTable, String userSessionTable) {
        statements.add(new RawParameterizedSqlStatement("""
                UPDATE cs
                SET cs.REALM_ID = us.REALM_ID
                FROM %s cs
                INNER JOIN %s us ON cs.USER_SESSION_ID = us.USER_SESSION_ID AND cs.OFFLINE_FLAG = us.OFFLINE_FLAG AND cs.REALM_ID IS NULL"""
                .formatted(clientSessionTable, userSessionTable)));
    }

    /** MySQL/MariaDB UPDATE … JOIN … SET 语法。 */
    private void generateUpdateQueryForMySQL(String clientSessionTable, String userSessionTable) {
        statements.add(new RawParameterizedSqlStatement("""
                UPDATE %s cs
                INNER JOIN %s us ON cs.user_session_id = us.user_session_id AND cs.offline_flag = us.offline_flag AND cs.realm_id IS NULL
                SET cs.realm_id = us.realm_id"""
                .formatted(clientSessionTable, userSessionTable)));
    }

    /** PostgreSQL UPDATE … FROM … WHERE 语法。 */
    private void generateUpdateQueryForPostgresSQL(String clientSessionTable, String userSessionTable) {
        statements.add(new RawParameterizedSqlStatement("""
                UPDATE %s cs
                SET realm_id = us.realm_id
                FROM %s us
                WHERE cs.user_session_id = us.user_session_id AND cs.offline_flag = us.offline_flag AND cs.realm_id IS NULL"""
                .formatted(clientSessionTable, userSessionTable)));
    }
}
