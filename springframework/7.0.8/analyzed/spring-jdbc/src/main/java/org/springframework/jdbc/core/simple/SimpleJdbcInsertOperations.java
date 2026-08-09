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

package org.springframework.jdbc.core.simple;

import java.util.Map;

import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.KeyHolder;

/**
 * 定义 {@link SimpleJdbcInsert} 实现的 Simple JDBC Insert API 接口。
 *
 * <p>本接口不常直接使用，但可增强可测试性，
 * 因其易于 mock 或 stub。
 *
 * @author Thomas Risberg
 * @author Sam Brannen
 * @since 2.5
 */
public interface SimpleJdbcInsertOperations {

	/**
	 * 指定 insert 使用的表名。
	 * @param tableName 存储表名称
	 * @return 此 {@code SimpleJdbcInsert}（用于方法链式调用）
	 */
	SimpleJdbcInsertOperations withTableName(String tableName);

	/**
	 * 指定 insert 使用的 schema 名称（若有）。
	 * @param schemaName schema 名称
	 * @return 此 {@code SimpleJdbcInsert}（用于方法链式调用）
	 */
	SimpleJdbcInsertOperations withSchemaName(String schemaName);

	/**
	 * 指定 insert 使用的 catalog 名称（若有）。
	 * @param catalogName catalog 名称
	 * @return 此 {@code SimpleJdbcInsert}（用于方法链式调用）
	 */
	SimpleJdbcInsertOperations withCatalogName(String catalogName);

	/**
	 * 指定 insert 语句应限制使用的列名。
	 * @param columnNames 一个或多个列名
	 * @return 此 {@code SimpleJdbcInsert}（用于方法链式调用）
	 */
	SimpleJdbcInsertOperations usingColumns(String... columnNames);

	/**
	 * 指定具有自动生成键的列名。
	 * @param columnNames 一个或多个列名
	 * @return 此 {@code SimpleJdbcInsert}（用于方法链式调用）
	 */
	SimpleJdbcInsertOperations usingGeneratedKeyColumns(String... columnNames);

	/**
	 * 指定应对 SQL 标识符加引号。
	 * <p>调用此方法后，将使用底层数据库的标识符引用字符串
	 * 为生成 SQL 语句中的 SQL 标识符加引号。
	 * 此处 SQL 标识符指 schema、表和列名。
	 * <p>标识符加引号时，须通过 {@link #usingColumns(String...)} 显式提供列名。
	 * 此外，schema 名、表名和列名的所有标识符
	 * 须与数据库元数据中对应标识符的大小写（混合、大写或小写）一致。
	 * @return 此 {@code SimpleJdbcInsert}（用于方法链式调用）
	 * @since 6.1
	 * @see #withSchemaName(String)
	 * @see #withTableName(String)
	 * @see #usingColumns(String...)
	 * @see java.sql.DatabaseMetaData#getIdentifierQuoteString()
	 * @see java.sql.DatabaseMetaData#storesMixedCaseIdentifiers()
	 * @see java.sql.DatabaseMetaData#storesMixedCaseQuotedIdentifiers()
	 * @see java.sql.DatabaseMetaData#storesUpperCaseIdentifiers()
	 * @see java.sql.DatabaseMetaData#storesUpperCaseQuotedIdentifiers()
	 * @see java.sql.DatabaseMetaData#storesLowerCaseIdentifiers()
	 * @see java.sql.DatabaseMetaData#storesLowerCaseQuotedIdentifiers()
	 */
	SimpleJdbcInsertOperations usingQuotedIdentifiers();

	/**
	 * 关闭通过 JDBC 获取的列元数据信息的任何处理。
	 * @return 此 {@code SimpleJdbcInsert}（用于方法链式调用）
	 */
	SimpleJdbcInsertOperations withoutTableColumnMetaDataAccess();

	/**
	 * 在通过 JDBC 查找列元数据时包含同义词。
	 * <p>注意：仅 Oracle 需要显式包含，其他支持同义词的数据库似乎会自动包含。
	 * @return 此 {@code SimpleJdbcInsert}（用于方法链式调用）
	 */
	SimpleJdbcInsertOperations includeSynonymsForTableColumnMetaData();

	/**
	 * 使用传入的值执行 insert。
	 * @param args 包含列名和对应值的 Map
	 * @return JDBC 驱动返回的影响行数
	 */
	int execute(Map<String, ?> args);

	/**
	 * 使用传入的值执行 insert。
	 * @param parameterSource 包含 insert 所用值的 SqlParameterSource
	 * @return JDBC 驱动返回的影响行数
	 */
	int execute(SqlParameterSource parameterSource);

	/**
	 * 使用传入的值执行 insert 并返回生成的键。
	 * <p>须已指定具有自动生成键的列名。
	 * 本方法始终返回 KeyHolder，但调用方须验证其确实包含生成的键。
	 * @param args 包含列名和对应值的 Map
	 * @return 生成的键值
	 */
	Number executeAndReturnKey(Map<String, ?> args);

	/**
	 * 使用传入的值执行 insert 并返回生成的键。
	 * <p>须已指定具有自动生成键的列名。
	 * 本方法始终返回 KeyHolder，但调用方须验证其确实包含生成的键。
	 * @param parameterSource 包含 insert 所用值的 SqlParameterSource
	 * @return 生成的键值
	 */
	Number executeAndReturnKey(SqlParameterSource parameterSource);

	/**
	 * 使用传入的值执行 insert 并返回生成的键。
	 * <p>须已指定具有自动生成键的列名。
	 * 本方法始终返回 KeyHolder，但调用方须验证其确实包含生成的键。
	 * @param args 包含列名和对应值的 Map
	 * @return 包含所有生成键的 KeyHolder
	 */
	KeyHolder executeAndReturnKeyHolder(Map<String, ?> args);

	/**
	 * 使用传入的值执行 insert 并返回生成的键。
	 * <p>须已指定具有自动生成键的列名。
	 * 本方法始终返回 KeyHolder，但调用方须验证其确实包含生成的键。
	 * @param parameterSource 包含 insert 所用值的 SqlParameterSource
	 * @return 包含所有生成键的 KeyHolder
	 */
	KeyHolder executeAndReturnKeyHolder(SqlParameterSource parameterSource);

	/**
	 * 使用传入的批量值执行批量 insert。
	 * @param batch 包含批量列名和对应值的 Map 数组
	 * @return JDBC 驱动返回的影响行数数组
	 */
	@SuppressWarnings("unchecked")
	int[] executeBatch(Map<String, ?>... batch);

	/**
	 * 使用传入的批量值执行批量 insert。
	 * @param batch 包含批量值的 SqlParameterSource 数组
	 * @return JDBC 驱动返回的影响行数数组
	 */
	int[] executeBatch(SqlParameterSource... batch);

}
