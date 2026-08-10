/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.connections.jpa.updater.liquibase;

import liquibase.database.Database;
import liquibase.database.core.MySQLDatabase;
import liquibase.datatype.DatabaseDataType;
import liquibase.datatype.core.VarcharType;
import liquibase.exception.DatabaseException;

/**
 * MySQL 8+ 专用 VARCHAR 类型映射：长度超过 255 的 VARCHAR 自动降级为 TEXT，
 * 以规避 MySQL 8 对表列数与行大小的限制。
 */
public class MySQL8VarcharType extends VarcharType {

    /** 优先级高于默认 {@link VarcharType}，确保 MySQL 8 场景优先选用本实现。 */
    @Override
    public int getPriority() {
        return super.getPriority() + 1; // Always take precedence over VarcharType
    }

    /** MySQL 8 且 size &gt; 255 时映射为 TEXT，否则沿用父类逻辑。 */
    @Override
    public DatabaseDataType toDatabaseDataType(Database database) {
        if (database instanceof MySQLDatabase) {
            try {
                if (database.getDatabaseMajorVersion() >= 8 && getSize() > 255) {
                    return new DatabaseDataType(database.escapeDataTypeName("TEXT"), getSize());
                }
            } catch (DatabaseException e) {
                throw new RuntimeException(e);
            }
        }
        return super.toDatabaseDataType(database);
    }
}
