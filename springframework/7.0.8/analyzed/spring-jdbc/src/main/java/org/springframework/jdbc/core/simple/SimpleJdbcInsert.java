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

import java.util.Arrays;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.KeyHolder;

/**
 * {@code SimpleJdbcInsert} 是一个多线程、可重用的对象，为表提供简单（批量）插入功能。它提供元数据处理来简化构建基本插入语句所需的代码。您需要提供的只是表
 * 的名称和包含列名称和列值的 {@code Map}。
 * <p>元数据处理是基于JDBC驱动程序提供的{@code DatabaseMetaData}。只要 JDBC 驱动程序可以提供指定表的列名，那么我们就可以依赖这种自动检测功能。
 * 如果不是这种情况，则必须显式指定列名。
 * <p>实际（批量）插入是使用Spring的{@link JdbcTemplate}来处理的。
 * <p>许多配置方法都会返回 {@code SimpleJdbcInsert} 的当前实例，以提供以“流畅”API 样式将多个实例链接在一起的能力。
 * @author Thomas Risberg
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 2.5
 * @see java.sql.DatabaseMetaData
 * @see org.springframework.jdbc.core.JdbcTemplate
 */
public class SimpleJdbcInsert extends AbstractJdbcInsert implements SimpleJdbcInsertOperations {

	/**
	 * 接受 JDBC {@link DataSource} 以在创建 {@link JdbcTemplate} 时使用的构造函数。
	 * @param dataSource 要使用的 {@code DataSource}
	 * @see org.springframework.jdbc.core.JdbcTemplate#setDataSource
	 */
	public SimpleJdbcInsert(DataSource dataSource) {
		super(dataSource);
	}

	/**
	 * 接受要使用的 {@link JdbcTemplate} 的替代构造函数。
	 * @param jdbcTemplate 要使用的 {@code JdbcTemplate}
	 * @see org.springframework.jdbc.core.JdbcTemplate#setDataSource
	 */
	public SimpleJdbcInsert(JdbcTemplate jdbcTemplate) {
		super(jdbcTemplate);
	}


	/**
	 * 方法 `withTableName`：完成本类中与「with Table Name」相关的职责。
	 */
	@Override
	public SimpleJdbcInsert withTableName(String tableName) {
		setTableName(tableName);
		return this;
	}

	/**
	 * 方法 `withSchemaName`：完成本类中与「with Schema Name」相关的职责。
	 */
	@Override
	public SimpleJdbcInsert withSchemaName(String schemaName) {
		setSchemaName(schemaName);
		return this;
	}

	/**
	 * 方法 `withCatalogName`：完成本类中与「with Catalog Name」相关的职责。
	 */
	@Override
	public SimpleJdbcInsert withCatalogName(String catalogName) {
		setCatalogName(catalogName);
		return this;
	}

	/**
	 * 方法 `usingColumns`：完成本类中与「using Columns」相关的职责。
	 */
	@Override
	public SimpleJdbcInsert usingColumns(String... columnNames) {
		setColumnNames(Arrays.asList(columnNames));
		return this;
	}

	/**
	 * 方法 `usingGeneratedKeyColumns`：完成本类中与「using Generated Key Columns」相关的职责。
	 */
	@Override
	public SimpleJdbcInsert usingGeneratedKeyColumns(String... columnNames) {
		setGeneratedKeyNames(columnNames);
		return this;
	}

	/**
	 * 方法 `usingQuotedIdentifiers`：完成本类中与「using Quoted Identifiers」相关的职责。
	 */
	@Override
	public SimpleJdbcInsert usingQuotedIdentifiers() {
		setQuoteIdentifiers(true);
		return this;
	}

	/**
	 * 方法 `withoutTableColumnMetaDataAccess`：完成本类中与「without Table Column Meta Data Access」相关的职责。
	 */
	@Override
	public SimpleJdbcInsert withoutTableColumnMetaDataAccess() {
		setAccessTableColumnMetaData(false);
		return this;
	}

	/**
	 * 方法 `includeSynonymsForTableColumnMetaData`：完成本类中与「include Synonyms For Table Column Meta Data」相关的职责。
	 */
	@Override
	public SimpleJdbcInsert includeSynonymsForTableColumnMetaData() {
		setOverrideIncludeSynonymsDefault(true);
		return this;
	}

	/**
	 * 执行（方法 `execute`）。
	 */
	@Override
	public int execute(Map<String, ?> args) {
		return doExecute(args);
	}

	/**
	 * 执行（方法 `execute`）。
	 */
	@Override
	public int execute(SqlParameterSource parameterSource) {
		return doExecute(parameterSource);
	}

	/**
	 * 执行：And Return Key（方法 `executeAndReturnKey`）。
	 */
	@Override
	public Number executeAndReturnKey(Map<String, ?> args) {
		return doExecuteAndReturnKey(args);
	}

	/**
	 * 执行：And Return Key（方法 `executeAndReturnKey`）。
	 */
	@Override
	public Number executeAndReturnKey(SqlParameterSource parameterSource) {
		return doExecuteAndReturnKey(parameterSource);
	}

	/**
	 * 执行：And Return Key Holder（方法 `executeAndReturnKeyHolder`）。
	 */
	@Override
	public KeyHolder executeAndReturnKeyHolder(Map<String, ?> args) {
		return doExecuteAndReturnKeyHolder(args);
	}

	/**
	 * 执行：And Return Key Holder（方法 `executeAndReturnKeyHolder`）。
	 */
	@Override
	public KeyHolder executeAndReturnKeyHolder(SqlParameterSource parameterSource) {
		return doExecuteAndReturnKeyHolder(parameterSource);
	}

	/**
	 * 执行：Batch（方法 `executeBatch`）。
	 */
	@SuppressWarnings("unchecked")
	@Override
	public int[] executeBatch(Map<String, ?>... batch) {
		return doExecuteBatch(batch);
	}

	/**
	 * 执行：Batch（方法 `executeBatch`）。
	 */
	@Override
	public int[] executeBatch(SqlParameterSource... batch) {
		return doExecuteBatch(batch);
	}

}
