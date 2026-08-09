/*
 * Copyright 2002-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.jdbc.core.metadata;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;

/**
 * {@link TableMetaDataProvider} 的 MySQL/MariaDB 特定实现。
 * MySQL 与 MariaDB 驱动在通过列名数组获取自增主键时行为不一致，
 * 因此本实现将 {@link #setGeneratedKeysColumnNameArraySupported} 设为 {@code false}，
 * 避免 SimpleJdbcInsert 等组件依赖不可靠的列名数组特性。
 *
 * @author Juergen Hoeller
 * @since 6.2.12
 */
public class MySQLTableMetaDataProvider extends GenericTableMetaDataProvider {

	public MySQLTableMetaDataProvider(DatabaseMetaData databaseMetaData) throws SQLException {
		super(databaseMetaData);
		setGeneratedKeysColumnNameArraySupported(false);
	}

}
