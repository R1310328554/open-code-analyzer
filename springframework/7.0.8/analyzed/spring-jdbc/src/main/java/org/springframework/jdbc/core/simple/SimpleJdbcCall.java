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
import java.util.LinkedHashSet;
import java.util.Map;

import javax.sql.DataSource;

import org.jspecify.annotations.Nullable;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

/**
 * SimpleJdbcCall 是表示对存储过程或存储函数调用的多线程、可复用对象。
 * 它提供元数据处理，简化访问基本存储过程/函数所需的代码。
 * 执行调用时只需提供过程/函数名称和包含参数的 Map。
 * 所供参数名称将与创建存储过程时声明的入参和出参匹配。
 *
 * <p>元数据处理基于 JDBC 驱动提供的 DatabaseMetaData。
 * 由于依赖 JDBC 驱动，此"自动检测"仅适用于已知提供准确元数据的数据库。
 * 目前包括 Derby、MySQL、Microsoft SQL Server、Oracle、DB2、
 * Sybase 和 PostgreSQL。其他数据库须显式声明所有参数。
 * 即使数据库提供必要元数据，也可显式声明所有参数，
 * 此时声明的参数优先。若需使用与存储过程编译时声明不匹配的参数名，
 * 也可关闭所有元数据处理。
 *
 * <p>实际调用通过 Spring 的 {@link JdbcTemplate} 处理。
 *
 * <p>许多配置方法返回 SimpleJdbcCall 当前实例，
 * 以便以"流式"接口风格链式调用。
 *
 * @author Thomas Risberg
 * @author Stephane Nicoll
 * @since 2.5
 * @see java.sql.DatabaseMetaData
 * @see org.springframework.jdbc.core.JdbcTemplate
 */
public class SimpleJdbcCall extends AbstractJdbcCall implements SimpleJdbcCallOperations {

	/**
	 * 接受 JDBC DataSource 参数的构造函数，用于创建底层 JdbcTemplate。
	 * @param dataSource 要使用的 {@code DataSource}
	 * @see org.springframework.jdbc.core.JdbcTemplate#setDataSource
	 */
	public SimpleJdbcCall(DataSource dataSource) {
		super(dataSource);
	}

	/**
	 * 接受 JdbcTemplate 参数的替代构造函数。
	 * @param jdbcTemplate 要使用的 {@code JdbcTemplate}
	 * @see org.springframework.jdbc.core.JdbcTemplate#setDataSource
	 */
	public SimpleJdbcCall(JdbcTemplate jdbcTemplate) {
		super(jdbcTemplate);
	}


	@Override
	public SimpleJdbcCall withProcedureName(String procedureName) {
		setProcedureName(procedureName);
		setFunction(false);
		return this;
	}

	@Override
	public SimpleJdbcCall withFunctionName(String functionName) {
		setProcedureName(functionName);
		setFunction(true);
		return this;
	}

	@Override
	public SimpleJdbcCall withSchemaName(String schemaName) {
		setSchemaName(schemaName);
		return this;
	}

	@Override
	public SimpleJdbcCall withCatalogName(String catalogName) {
		setCatalogName(catalogName);
		return this;
	}

	@Override
	public SimpleJdbcCall withReturnValue() {
		setReturnValueRequired(true);
		return this;
	}

	@Override
	public SimpleJdbcCall declareParameters(SqlParameter... sqlParameters) {
		for (SqlParameter sqlParameter : sqlParameters) {
			if (sqlParameter != null) {
				addDeclaredParameter(sqlParameter);
			}
		}
		return this;
	}

	@Override
	public SimpleJdbcCall useInParameterNames(String... inParameterNames) {
		setInParameterNames(new LinkedHashSet<>(Arrays.asList(inParameterNames)));
		return this;
	}

	@Override
	public SimpleJdbcCall returningResultSet(String parameterName, RowMapper<?> rowMapper) {
		addDeclaredRowMapper(parameterName, rowMapper);
		return this;
	}

	@Override
	public SimpleJdbcCall withoutProcedureColumnMetaDataAccess() {
		setAccessCallParameterMetaData(false);
		return this;
	}

	@Override
	public SimpleJdbcCall withNamedBinding() {
		setNamedBinding(true);
		return this;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> @Nullable T executeFunction(Class<T> returnType, Object... args) {
		return (T) doExecute(args).get(getScalarOutParameterName());
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> @Nullable T executeFunction(Class<T> returnType, Map<String, ?> args) {
		return (T) doExecute(args).get(getScalarOutParameterName());
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> @Nullable T executeFunction(Class<T> returnType, SqlParameterSource args) {
		return (T) doExecute(args).get(getScalarOutParameterName());
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> @Nullable T executeObject(Class<T> returnType, Object... args) {
		return (T) doExecute(args).get(getScalarOutParameterName());
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> @Nullable T executeObject(Class<T> returnType, Map<String, ?> args) {
		return (T) doExecute(args).get(getScalarOutParameterName());
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> @Nullable T executeObject(Class<T> returnType, SqlParameterSource args) {
		return (T) doExecute(args).get(getScalarOutParameterName());
	}

	@Override
	public Map<String, @Nullable Object> execute(Object... args) {
		return doExecute(args);
	}

	@Override
	public Map<String, @Nullable Object> execute(Map<String, ?> args) {
		return doExecute(args);
	}

	@Override
	public Map<String, @Nullable Object> execute(SqlParameterSource parameterSource) {
		return doExecute(parameterSource);
	}

}
