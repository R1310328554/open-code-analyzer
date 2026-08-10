/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates
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

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.keycloak.storage.jpa.JpaHashUtils;

import liquibase.database.core.MySQLDatabase;
import liquibase.exception.CustomChangeException;
import liquibase.statement.core.RawParameterizedSqlStatement;

/**
 * MySQL 联邦用户属性长文本列迁移。
 * <p>仅 MySQL 会将超过 255 字符的列改为 TEXT（最多约 64k），见 {@link org.keycloak.connections.jpa.updater.liquibase.MySQL8VarcharType}。
 * 新代码要求超过 2024 字符的值存入 LONG_VALUE 列并清空 VALUE，本迁移负责复制并写入哈希。</p>
 *
 * @author Alexander Schwartz
 */
public class FederatedUserAttributeTextColumnMigration extends CustomKeycloakTask {

    @Override
    protected void generateStatementsImpl() throws CustomChangeException {

        // 仅 MySQL 需要此列类型迁移
        if (database instanceof MySQLDatabase) {

            try (PreparedStatement ps = connection.prepareStatement("SELECT t.ID, t.VALUE" +
                    "  FROM " + getTableName("FED_USER_ATTRIBUTE") + " t" +
                    "  WHERE LENGTH(t.VALUE) > 2024");
                 ResultSet resultSet = ps.executeQuery()
            ) {
                while (resultSet.next()) {
                    String id = resultSet.getString(1);
                    String value = resultSet.getString(2);
                    // SQL LENGTH() 按字节计，Java length() 按 Unicode 字符计；再用 Java 侧校验避免误迁
                    if (value.length() > 2024) {
                        statements.add(new RawParameterizedSqlStatement("UPDATE " + getTableName("FED_USER_ATTRIBUTE") + " SET VALUE = null, LONG_VALUE_HASH = ?, LONG_VALUE_HASH_LOWER_CASE = ?, LONG_VALUE = ? WHERE ID = ?",
                                JpaHashUtils.hashForAttributeValue(value),
                                JpaHashUtils.hashForAttributeValueLowerCase(value),
                                value,
                                id));
                    }
                }
            } catch (Exception e) {
                throw new CustomChangeException(getTaskId() + ": Exception when updating data from previous version", e);
            }
        }

    }

    @Override
    protected String getTaskId() {
        return "Leave only single offline session per user and client";
    }

}
