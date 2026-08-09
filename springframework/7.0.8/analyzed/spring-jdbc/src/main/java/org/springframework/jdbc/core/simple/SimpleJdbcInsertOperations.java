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
 * 指定 {@link SimpleJdbcInsert} 实现的简单 JDBC 插入 API 的接口。
 * <p> 该接口通常不直接使用，但提供了增强可测试性的选项，因为它可以轻松地被模拟或存根。
 * @author Thomas Risberg
 * @author Sam Brannen
 * @since 2.5
 */
public interface SimpleJdbcInsertOperations {

	/**
	 * 指定用于插入的表名称。
	 * @param tableName 存储的表的名称
	 * @return {@code SimpleJdbcInsert}（用于方法链接）
	 */
	SimpleJdbcInsertOperations withTableName(String tableName);

	/**
	 * 指定用于插入的架构名称（如果有）。
	 * @param schemaName 模式的名称
	 * @return {@code SimpleJdbcInsert}（用于方法链接）
	 */
	SimpleJdbcInsertOperations withSchemaName(String schemaName);

	/**
	 * 指定用于插入的目录名称（如果有）。
	 * @param catalogName 目录名称
	 * @return {@code SimpleJdbcInsert}（用于方法链接）
	 */
	SimpleJdbcInsertOperations withCatalogName(String catalogName);

	/**
	 * 指定插入语句应限制使用的列名。
	 * @param columnNames 一个或多个列名
	 * @return {@code SimpleJdbcInsert}（用于方法链接）
	 */
	SimpleJdbcInsertOperations usingColumns(String... columnNames);

	/**
	 * 指定具有自动生成键的任何列的名称。
	 * @param columnNames 一个或多个列名
	 * @return {@code SimpleJdbcInsert}（用于方法链接）
	 */
	SimpleJdbcInsertOperations usingGeneratedKeyColumns(String... columnNames);

	/**
	 * 指定 SQL 标识符应加引号。 <p>如果调用此方法，则将使用底层数据库的标识符引用字符串来引用生成的 SQL 语句中的 SQL 标识符。在此上下文中，SQL 标识符指的是架构
	 * 、表和列名称。 <p>当引用标识符时，必须通过 {@link #usingColumns(String...)} 提供显式列名称。此外，模式名称、表名称和列名称的所有标识符必须
	 * 与数据库元数据中有关大小写（混合大小写、大写或小写）的相应标识符相匹配。
	 * @return {@code SimpleJdbcInsert}（用于方法链接）
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
	 * 关闭对通过 JDBC 获取的列元数据信息的任何处理。
	 * @return {@code SimpleJdbcInsert}（用于方法链接）
	 */
	SimpleJdbcInsertOperations withoutTableColumnMetaDataAccess();

	/**
	 * 包括通过 JDBC 进行列元数据查找的同义词。 <p>注意：这仅适用于 Oracle，因为支持同义词的其他数据库似乎自动包含同义词。
	 * @return {@code SimpleJdbcInsert}（用于方法链接）
	 */
	SimpleJdbcInsertOperations includeSynonymsForTableColumnMetaData();

	/**
	 * 使用传入的值执行插入。
	 * @param args 包含列名和相应值的 Map
	 * @return JDBC 驱动程序返回的受影响的行数
	 */
	int execute(Map<String, ?> args);

	/**
	 * 使用传入的值执行插入。
	 * @param parameterSource 包含用于插入的值的 SqlParameterSource
	 * @return JDBC 驱动程序返回的受影响的行数
	 */
	int execute(SqlParameterSource parameterSource);

	/**
	 * 使用传入的值执行插入并返回生成的键。 <p>这要求已指定具有自动生成键的列的名称。此方法将始终返回 KeyHolder，但调用者必须验证它是否确实包含生成的密钥。
	 * @param args 包含列名和相应值的 Map
	 * @return 生成的键值
	 */
	Number executeAndReturnKey(Map<String, ?> args);

	/**
	 * 使用传入的值执行插入并返回生成的键。 <p>这要求已指定具有自动生成键的列的名称。此方法将始终返回 KeyHolder，但调用者必须验证它是否确实包含生成的密钥。
	 * @param parameterSource 包含用于插入的值的 SqlParameterSource
	 * @return 生成的键值。
	 */
	Number executeAndReturnKey(SqlParameterSource parameterSource);

	/**
	 * 使用传入的值执行插入并返回生成的键。 <p>这要求已指定具有自动生成键的列的名称。此方法将始终返回 KeyHolder，但调用者必须验证它是否确实包含生成的密钥。
	 * @param args 包含列名和相应值的 Map
	 * @return KeyHolder 包含所有生成的密钥
	 */
	KeyHolder executeAndReturnKeyHolder(Map<String, ?> args);

	/**
	 * 使用传入的值执行插入并返回生成的键。 <p>这要求已指定具有自动生成键的列的名称。此方法将始终返回 KeyHolder，但调用者必须验证它是否确实包含生成的密钥。
	 * @param parameterSource 包含用于插入的值的 SqlParameterSource
	 * @return KeyHolder 包含所有生成的密钥
	 */
	KeyHolder executeAndReturnKeyHolder(SqlParameterSource parameterSource);

	/**
	 * 使用传入的批量值执行批量插入。
	 * @param batch 包含一批列名和相应值的 Map 数组
	 * @return JDBC 驱动程序返回的受影响的行数数组
	 */
	@SuppressWarnings("unchecked")
	int[] executeBatch(Map<String, ?>... batch);

	/**
	 * 使用传入的批量值执行批量插入。
	 * @param batch 包含批次值的 SqlParameterSource 数组
	 * @return JDBC 驱动程序返回的受影响的行数数组
	 */
	int[] executeBatch(SqlParameterSource... batch);

}
