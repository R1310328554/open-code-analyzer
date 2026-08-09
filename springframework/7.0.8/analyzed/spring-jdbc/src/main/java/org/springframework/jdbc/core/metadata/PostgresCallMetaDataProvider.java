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
import java.sql.Types;

import org.jspecify.annotations.Nullable;

import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;

/**
 * {@link CallMetaDataProvider} 接口的 Postgres 特定实现。此类供 Simple JDBC 类内部使用。
 * @author Thomas Risberg
 * @author Juergen Hoeller
 * @since 2.5
 */
public class PostgresCallMetaDataProvider extends GenericCallMetaDataProvider {

	private static final String RETURN_VALUE_NAME = "returnValue";

	/** 名称相关状态（`schemaName`）。 */
	private final String schemaName;


	/**
	 * 创建 `PostgresCallMetaDataProvider` 的新实例。
	 */
	public PostgresCallMetaDataProvider(DatabaseMetaData databaseMetaData) throws SQLException {
		super(databaseMetaData);

		// 如果未指定架构，则使用当前架构（或公共架构）
		String schema = databaseMetaData.getConnection().getSchema();
		this.schemaName = (schema != null ? schema : "public");
	}


	/**
	 * 判断是否 Return Result Set Supported。
	 */
	@Override
	public boolean isReturnResultSetSupported() {
		return false;
	}

	/**
	 * 判断是否 Ref Cursor Supported。
	 */
	@Override
	public boolean isRefCursorSupported() {
		return true;
	}

	/**
	 * 获取 Ref Cursor Sql Type（`RefCursorSqlType`）。
	 */
	@Override
	public int getRefCursorSqlType() {
		return Types.OTHER;
	}

	/**
	 * 方法 `metaDataSchemaNameToUse`：完成本类中与「meta Data Schema Name To Use」相关的职责。
	 */
	@Override
	public @Nullable String metaDataSchemaNameToUse(@Nullable String schemaName) {
		return (schemaName == null ? this.schemaName : super.metaDataSchemaNameToUse(schemaName));
	}

	/**
	 * 创建：Default Out Parameter（方法 `createDefaultOutParameter`）。
	 */
	@Override
	public SqlParameter createDefaultOutParameter(String parameterName, CallParameterMetaData meta) {
		if (meta.getSqlType() == Types.OTHER && "refcursor".equals(meta.getTypeName())) {
			return new SqlOutParameter(parameterName, getRefCursorSqlType(), new ColumnMapRowMapper());
		}
		else {
			return super.createDefaultOutParameter(parameterName, meta);
		}
	}

	/**
	 * 方法 `byPassReturnParameter`：完成本类中与「by Pass Return Parameter」相关的职责。
	 */
	@Override
	public boolean byPassReturnParameter(String parameterName) {
		return RETURN_VALUE_NAME.equals(parameterName);
	}

}
