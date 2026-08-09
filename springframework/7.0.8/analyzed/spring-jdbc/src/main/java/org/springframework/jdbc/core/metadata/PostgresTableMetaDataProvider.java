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

import org.jspecify.annotations.Nullable;

/**
 * {@link TableMetaDataProvider} 的 PostgreSQL 特定实现。支持在没有 JDBC 3.0 {@code getGeneratedKeys}
 * 支持的情况下检索生成的密钥的功能。此外，它还会在给定情况下处理来自 {@code DatabaseMetaData} 的 PostgreSQL 返回的目录和模式名称。
 * @author Thomas Risberg
 * @author Juergen Hoeller
 * @since 2.5
 */
public class PostgresTableMetaDataProvider extends GenericTableMetaDataProvider {

	/**
	 * 创建 `PostgresTableMetaDataProvider` 的新实例。
	 */
	public PostgresTableMetaDataProvider(DatabaseMetaData databaseMetaData) throws SQLException {
		super(databaseMetaData);
	}


	/**
	 * 方法 `metaDataCatalogNameToUse`：完成本类中与「meta Data Catalog Name To Use」相关的职责。
	 */
	@Override
	public @Nullable String metaDataCatalogNameToUse(@Nullable String catalogName) {
		return catalogName;
	}

	/**
	 * 方法 `metaDataSchemaNameToUse`：完成本类中与「meta Data Schema Name To Use」相关的职责。
	 */
	@Override
	public @Nullable String metaDataSchemaNameToUse(@Nullable String schemaName) {
		return (schemaName != null ? schemaName : getDefaultSchema());
	}

	/**
	 * 判断是否 Get Generated Keys Simulated。
	 */
	@Override
	public boolean isGetGeneratedKeysSimulated() {
		return true;
	}

	/**
	 * 获取 Simple Query For Get Generated Key（`SimpleQueryForGetGeneratedKey`）。
	 */
	@Override
	public String getSimpleQueryForGetGeneratedKey(String tableName, String keyColumnName) {
		return "RETURNING " + keyColumnName;
	}

}
