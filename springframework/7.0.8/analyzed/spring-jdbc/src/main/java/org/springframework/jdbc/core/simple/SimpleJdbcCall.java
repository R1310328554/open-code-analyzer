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
 * SimpleJdbcCall 是一个多线程、可重用的对象，表示对存储过程或存储函数的调用。它提供元数据处理来简化访问基本存储过程/函数所需的代码。您需要提供的只是过程/函数的名
 * 称以及执行调用时包含参数的 Map。提供的参数的名称将与创建存储过程时声明的输入和输出参数相匹配。
 * <p>的元数据处理是基于JDBC驱动程序提供的DatabaseMetaData。由于我们依赖 JDBC 驱动程序，因此这种“自动检测”只能用于已知可提供准确元数据的数据库。目前
 * 包括 Derby、MySQL、Microsoft SQL Server、Oracle、DB2、Sybase 和 PostgreSQL。对于任何其他数据库，您需要显式声明所有参数
 * 。当然，即使数据库提供了必要的元数据，您也可以显式声明所有参数。在这种情况下，您声明的参数将优先。如果您想要使用与存储过程编译期间声明的名称不匹配的参数名称，您还可以关闭任何元
 * 数据处理。
 * <p>实际调用是使用Spring的{@link JdbcTemplate}来处理的。
 * <p>许多配置方法都会返回 SimpleJdbcCall 的当前实例，以便提供以“流畅”界面风格将多个实例链接在一起的能力。
 * @author Thomas Risberg
 * @author Stephane Nicoll
 * @since 2.5
 * @see java.sql.DatabaseMetaData
 * @see org.springframework.jdbc.core.JdbcTemplate
 */
public class SimpleJdbcCall extends AbstractJdbcCall implements SimpleJdbcCallOperations {

	/**
	 * 一种构造函数，它采用 JDBC 数据源的一个参数，以便在创建底层 JdbcTemplate 时使用。
	 * @param dataSource 要使用的 {@code DataSource}
	 * @see org.springframework.jdbc.core.JdbcTemplate#setDataSource
	 */
	public SimpleJdbcCall(DataSource dataSource) {
		super(dataSource);
	}

	/**
	 * 另一种构造函数，它采用一个参数并使用要使用的 JdbcTemplate。
	 * @param jdbcTemplate 要使用的 {@code JdbcTemplate}
	 * @see org.springframework.jdbc.core.JdbcTemplate#setDataSource
	 */
	public SimpleJdbcCall(JdbcTemplate jdbcTemplate) {
		super(jdbcTemplate);
	}


	/**
	 * 方法 `withProcedureName`：完成本类中与「with Procedure Name」相关的职责。
	 */
	@Override
	public SimpleJdbcCall withProcedureName(String procedureName) {
		setProcedureName(procedureName);
		setFunction(false);
		return this;
	}

	/**
	 * 方法 `withFunctionName`：完成本类中与「with Function Name」相关的职责。
	 */
	@Override
	public SimpleJdbcCall withFunctionName(String functionName) {
		setProcedureName(functionName);
		setFunction(true);
		return this;
	}

	/**
	 * 方法 `withSchemaName`：完成本类中与「with Schema Name」相关的职责。
	 */
	@Override
	public SimpleJdbcCall withSchemaName(String schemaName) {
		setSchemaName(schemaName);
		return this;
	}

	/**
	 * 方法 `withCatalogName`：完成本类中与「with Catalog Name」相关的职责。
	 */
	@Override
	public SimpleJdbcCall withCatalogName(String catalogName) {
		setCatalogName(catalogName);
		return this;
	}

	/**
	 * 方法 `withReturnValue`：完成本类中与「with Return Value」相关的职责。
	 */
	@Override
	public SimpleJdbcCall withReturnValue() {
		setReturnValueRequired(true);
		return this;
	}

	/**
	 * 方法 `declareParameters`：完成本类中与「declare Parameters」相关的职责。
	 */
	@Override
	public SimpleJdbcCall declareParameters(SqlParameter... sqlParameters) {
		for (SqlParameter sqlParameter : sqlParameters) {
			if (sqlParameter != null) {
				addDeclaredParameter(sqlParameter);
			}
		}
		return this;
	}

	/**
	 * 方法 `useInParameterNames`：完成本类中与「use In Parameter Names」相关的职责。
	 */
	@Override
	public SimpleJdbcCall useInParameterNames(String... inParameterNames) {
		setInParameterNames(new LinkedHashSet<>(Arrays.asList(inParameterNames)));
		return this;
	}

	/**
	 * 方法 `returningResultSet`：完成本类中与「returning Result Set」相关的职责。
	 */
	@Override
	public SimpleJdbcCall returningResultSet(String parameterName, RowMapper<?> rowMapper) {
		addDeclaredRowMapper(parameterName, rowMapper);
		return this;
	}

	/**
	 * 方法 `withoutProcedureColumnMetaDataAccess`：完成本类中与「without Procedure Column Meta Data Access」相关的职责。
	 */
	@Override
	public SimpleJdbcCall withoutProcedureColumnMetaDataAccess() {
		setAccessCallParameterMetaData(false);
		return this;
	}

	/**
	 * 方法 `withNamedBinding`：完成本类中与「with Named Binding」相关的职责。
	 */
	@Override
	public SimpleJdbcCall withNamedBinding() {
		setNamedBinding(true);
		return this;
	}

	/**
	 * 执行：Function（方法 `executeFunction`）。
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T> @Nullable T executeFunction(Class<T> returnType, Object... args) {
		return (T) doExecute(args).get(getScalarOutParameterName());
	}

	/**
	 * 执行：Function（方法 `executeFunction`）。
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T> @Nullable T executeFunction(Class<T> returnType, Map<String, ?> args) {
		return (T) doExecute(args).get(getScalarOutParameterName());
	}

	/**
	 * 执行：Function（方法 `executeFunction`）。
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T> @Nullable T executeFunction(Class<T> returnType, SqlParameterSource args) {
		return (T) doExecute(args).get(getScalarOutParameterName());
	}

	/**
	 * 执行：Object（方法 `executeObject`）。
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T> @Nullable T executeObject(Class<T> returnType, Object... args) {
		return (T) doExecute(args).get(getScalarOutParameterName());
	}

	/**
	 * 执行：Object（方法 `executeObject`）。
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T> @Nullable T executeObject(Class<T> returnType, Map<String, ?> args) {
		return (T) doExecute(args).get(getScalarOutParameterName());
	}

	/**
	 * 执行：Object（方法 `executeObject`）。
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T> @Nullable T executeObject(Class<T> returnType, SqlParameterSource args) {
		return (T) doExecute(args).get(getScalarOutParameterName());
	}

	/**
	 * 执行（方法 `execute`）。
	 */
	@Override
	public Map<String, @Nullable Object> execute(Object... args) {
		return doExecute(args);
	}

	/**
	 * 执行（方法 `execute`）。
	 */
	@Override
	public Map<String, @Nullable Object> execute(Map<String, ?> args) {
		return doExecute(args);
	}

	/**
	 * 执行（方法 `execute`）。
	 */
	@Override
	public Map<String, @Nullable Object> execute(SqlParameterSource parameterSource) {
		return doExecute(parameterSource);
	}

}
