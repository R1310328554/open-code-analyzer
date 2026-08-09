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

package org.springframework.jdbc.object;

import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.util.Assert;

/**
 * “RDBMS 操作”是表示查询、更新或存储过程调用的多线程、可重用对象。一个RDBMS操作是<b>而不是</b>命令，因为命令是不可重用的。但是，执行方法可以将命令作为参数。子
 * 类应该是JavaBeans，以便于配置。
 * <p> 该类和子类抛出 {@code org.springframework.dao} 包中定义的运行时异常（以及由 {@code
 * org.springframework.jdbc.core} 包抛出的异常，该包中的类在后台使用该异常来执行原始 JDBC 操作）。
 * <p>子类应在调用 {@link #compile()} 方法之前设置 SQL 并添加参数。添加参数的顺序很重要。然后可以调用适当的 {@code execute} 或 {@c
 * ode update} 方法。
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see SqlQuery
 * @see SqlUpdate
 * @see StoredProcedure
 * @see org.springframework.jdbc.core.JdbcTemplate
 */
public abstract class RdbmsOperation implements InitializingBean {

	/**
	 */
	protected final Log logger = LogFactory.getLog(getClass());

	/**
	 */
	private JdbcTemplate jdbcTemplate = new JdbcTemplate();

	private int resultSetType = ResultSet.TYPE_FORWARD_ONLY;

	/** `false`：该类的成员状态。 */
	private boolean updatableResults = false;

	/** `false`：该类的成员状态。 */
	private boolean returnGeneratedKeys = false;

	/** 名称相关状态（`generatedKeysColumnNames`）。 */
	private String @Nullable [] generatedKeysColumnNames;

	/** `sql`：该类的成员状态。 */
	private @Nullable String sql;

	private final List<SqlParameter> declaredParameters = new ArrayList<>();

	/**
	 * 这个操作编译了吗？编译意味着至少检查是否已提供 DataSource 和 sql，但子类也可以实现自己的自定义验证。
	 */
	private volatile boolean compiled;


	/**
	 * 当您想在多个 {@code RdbmsOperations} 中使用相同的 {@link JdbcTemplate} 时，可以替代更常用的 {@link
	 * #setDataSource}。如果 {@code JdbcTemplate} 具有特殊配置（例如要重用的 {@link
	 * org.springframework.jdbc.support.SQLExceptionTranslator}），则这是合适的。
	 */
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	/**
	 * 返回此操作对象使用的 {@link JdbcTemplate}。
	 */
	public JdbcTemplate getJdbcTemplate() {
		return this.jdbcTemplate;
	}

	/**
	 * 设置从中获取连接的 JDBC {@link DataSource}。
	 * @see org.springframework.jdbc.core.JdbcTemplate#setDataSource
	 */
	public void setDataSource(DataSource dataSource) {
		this.jdbcTemplate.setDataSource(dataSource);
	}

	/**
	 * 设置此 RDBMS 操作的获取大小。这对于处理大型结果集很重要：将其设置为高于默认值会以内存消耗为代价提高处理速度；设置较低的值可以避免传输应用程序永远不会读取的行数据。 <p
	 * >Default 为-1，表示使用驱动程序的默认值。
	 * @see org.springframework.jdbc.core.JdbcTemplate#setFetchSize
	 */
	public void setFetchSize(int fetchSize) {
		this.jdbcTemplate.setFetchSize(fetchSize);
	}

	/**
	 * 设置此 RDBMS 操作的最大行数。这对于处理大型结果集的子集非常重要，以避免在数据库或 JDBC 驱动程序中读取和保存整个结果集。 <p>Default 为-1，表示使用驱动
	 * 程序的默认值。
	 * @see org.springframework.jdbc.core.JdbcTemplate#setMaxRows
	 */
	public void setMaxRows(int maxRows) {
		this.jdbcTemplate.setMaxRows(maxRows);
	}

	/**
	 * 设置此 RDBMS 操作执行的语句的查询超时。 <p>Default 为-1，表示使用 JDBC 驱动程序的默认值。 <p>注意：当在事务级别指定了超时的事务中执行时，此处指定
	 * 的任何超时都将被剩余事务超时覆盖。
	 */
	public void setQueryTimeout(int queryTimeout) {
		this.jdbcTemplate.setQueryTimeout(queryTimeout);
	}

	/**
	 * 设置是否使用返回特定类型ResultSet的语句。
	 * @param resultSetType 结果集类型
	 * @see java.sql.ResultSet#TYPE_FORWARD_ONLY
	 * @see java.sql.ResultSet#TYPE_SCROLL_INSENSITIVE
	 * @see java.sql.ResultSet#TYPE_SCROLL_SENSITIVE
	 * @see java.sql.Connection#prepareStatement(String, int, int)
	 */
	public void setResultSetType(int resultSetType) {
		this.resultSetType = resultSetType;
	}

	/**
	 * Return 语句是否将返回特定类型的 ResultSet。
	 */
	public int getResultSetType() {
		return this.resultSetType;
	}

	/**
	 * 设置是否使用能够返回可更新结果集的语句。
	 * @see java.sql.Connection#prepareStatement(String, int, int)
	 */
	public void setUpdatableResults(boolean updatableResults) {
		if (isCompiled()) {
			throw new InvalidDataAccessApiUsageException(
					"The updatableResults flag must be set before the operation is compiled");
		}
		this.updatableResults = updatableResults;
	}

	/**
	 * 返回语句是否将返回可更新的结果集。
	 */
	public boolean isUpdatableResults() {
		return this.updatableResults;
	}

	/**
	 * 设置准备好的语句是否应该能够返回自动生成的键。
	 * @see java.sql.Connection#prepareStatement(String, int)
	 */
	public void setReturnGeneratedKeys(boolean returnGeneratedKeys) {
		if (isCompiled()) {
			throw new InvalidDataAccessApiUsageException(
					"The returnGeneratedKeys flag must be set before the operation is compiled");
		}
		this.returnGeneratedKeys = returnGeneratedKeys;
	}

	/**
	 * 返回语句是否应该能够返回自动生成的键。
	 */
	public boolean isReturnGeneratedKeys() {
		return this.returnGeneratedKeys;
	}

	/**
	 * 设置自动生成的键的列名称。
	 * @see java.sql.Connection#prepareStatement(String, String[])
	 */
	public void setGeneratedKeysColumnNames(String @Nullable ... names) {
		if (isCompiled()) {
			throw new InvalidDataAccessApiUsageException(
					"The column names for the generated keys must be set before the operation is compiled");
		}
		this.generatedKeysColumnNames = names;
	}

	/**
	 * 返回自动生成的键的列名称。
	 */
	public String @Nullable [] getGeneratedKeysColumnNames() {
		return this.generatedKeysColumnNames;
	}

	/**
	 * 设置该操作执行的SQL。
	 */
	public void setSql(@Nullable String sql) {
		this.sql = sql;
	}

	/**
	 * 如果子类愿意，可以重写它以提供动态 SQL，但 SQL 通常是通过调用 {@link #setSql} 方法或在子类构造函数中设置的。
	 */
	public @Nullable String getSql() {
		return this.sql;
	}

	/**
	 * 解析配置好的SQL以供实际使用。
	 * @return SQL（绝不是 {@code null}）
	 * @since 5.0
	 */
	protected String resolveSql() {
		String sql = getSql();
		Assert.state(sql != null, "No SQL set");
		return sql;
	}

	/**
	 * 添加匿名参数，仅指定 {@code java.sql.Types} 类中定义的 SQL 类型。 <p>参数排序很重要。此方法是 {@link
	 * #declareParameter} 方法的替代方法，通常应首选 {@link #declareParameter} 方法。
	 * @param types {@code java.sql.Types} 类中定义的 SQL 类型数组
	 * @throws InvalidDataAccessApiUsageException 如果该操作已经编译
	 */
	public void setTypes(int @Nullable [] types) throws InvalidDataAccessApiUsageException {
		if (isCompiled()) {
			throw new InvalidDataAccessApiUsageException("Cannot add parameters once query is compiled");
		}
		if (types != null) {
			for (int type : types) {
				declareParameter(new SqlParameter(type));
			}
		}
	}

	/**
	 * 声明此操作的参数。 <p> 使用位置参数时，调用此方法的顺序很重要。此处使用带有命名 SqlParameter 对象的命名参数并不重要；当将命名参数与未命名的 SqlParam
	 * eter 对象结合使用时，它仍然很重要。
	 * @param param 要添加的 SqlParameter。这将指定 SQL 类型和（可选）参数名称。请注意，您通常在此处使用 {@link SqlParameter} 类本身，而不是其任何子类。
	 * @throws InvalidDataAccessApiUsageException 如果该操作已编译，因此无法进一步配置
	 */
	public void declareParameter(SqlParameter param) throws InvalidDataAccessApiUsageException {
		if (isCompiled()) {
			throw new InvalidDataAccessApiUsageException("Cannot add parameters once the query is compiled");
		}
		this.declaredParameters.add(param);
	}

	/**
	 * 添加一个或多个声明的参数。用于在 bean 工厂中使用时配置此操作。每个参数将指定 SQL 类型和（可选）参数名称。
	 * @param parameters 包含声明的 {@link SqlParameter} 对象的数组
	 * @see #declaredParameters
	 */
	public void setParameters(SqlParameter... parameters) {
		if (isCompiled()) {
			throw new InvalidDataAccessApiUsageException("Cannot add parameters once the query is compiled");
		}
		for (int i = 0; i < parameters.length; i++) {
			if (parameters[i] != null) {
				this.declaredParameters.add(parameters[i]);
			}
			else {
				throw new InvalidDataAccessApiUsageException("Cannot add parameter at index " + i + " from " +
						Arrays.asList(parameters) + " since it is 'null'");
			}
		}
	}

	/**
	 * 返回声明的 {@link SqlParameter} 对象的列表。
	 */
	protected List<SqlParameter> getDeclaredParameters() {
		return this.declaredParameters;
	}


	/**
	 * 如果在 bean 工厂中使用，确保编译。
	 */
	@Override
	public void afterPropertiesSet() {
		compile();
	}

	/**
	 * 编译此查询。忽略后续的编译尝试。
	 * @throws InvalidDataAccessApiUsageException 如果对象尚未正确初始化，例如，如果未提供 DataSource
	 */
	public final void compile() throws InvalidDataAccessApiUsageException {
		if (!isCompiled()) {
			if (getSql() == null) {
				throw new InvalidDataAccessApiUsageException("Property 'sql' is required");
			}

			try {
				this.jdbcTemplate.afterPropertiesSet();
			}
			catch (IllegalArgumentException ex) {
				throw new InvalidDataAccessApiUsageException(ex.getMessage());
			}

			compileInternal();
			this.compiled = true;

			if (logger.isDebugEnabled()) {
				logger.debug("RdbmsOperation with SQL [" + getSql() + "] compiled");
			}
		}
	}

	/**
	 * 这个操作是“编译”的吗？与 JDO 中一样，编译意味着操作已完全配置并可供使用。编译的确切含义因子类而异。
	 * @return 该操作已编译并可以使用
	 */
	public boolean isCompiled() {
		return this.compiled;
	}

	/**
	 * 检查该操作是否已经编译；如果尚未编译，则延迟编译它。 <p>由{@code validateParameters}自动调用。
	 * @see #validateParameters
	 */
	protected void checkCompiled() {
		if (!isCompiled()) {
			logger.debug("SQL operation not compiled before execution - invoking compile");
			compile();
		}
	}

	/**
	 * 根据声明的参数验证传递给执行方法的参数。子类应在每个 {@code executeQuery()} 或 {@code update()} 方法之前调用此方法。
	 * @param parameters 提供的参数（可能是 {@code null}）
	 * @throws InvalidDataAccessApiUsageException 如果参数无效
	 */
	protected void validateParameters(Object @Nullable [] parameters) throws InvalidDataAccessApiUsageException {
		checkCompiled();
		int declaredInParameters = 0;
		for (SqlParameter param : this.declaredParameters) {
			if (param.isInputValueProvided()) {
				if (!supportsLobParameters() &&
						(param.getSqlType() == Types.BLOB || param.getSqlType() == Types.CLOB)) {
					throw new InvalidDataAccessApiUsageException(
							"BLOB or CLOB parameters are not allowed for this kind of operation");
				}
				declaredInParameters++;
			}
		}
		validateParameterCount((parameters != null ? parameters.length : 0), declaredInParameters);
	}

	/**
	 * 根据声明的参数验证传递给执行方法的命名参数。子类应在每个 {@code executeQuery()} 或 {@code update()} 方法之前调用此方法。
	 * @param parameters 提供的参数映射（可能是 {@code null}）
	 * @throws InvalidDataAccessApiUsageException 如果参数无效
	 */
	protected void validateNamedParameters(@Nullable Map<String, ?> parameters) throws InvalidDataAccessApiUsageException {
		checkCompiled();
		Map<String, ?> paramsToUse = (parameters != null ? parameters : Collections.<String, Object> emptyMap());
		int declaredInParameters = 0;
		for (SqlParameter param : this.declaredParameters) {
			if (param.isInputValueProvided()) {
				if (!supportsLobParameters() &&
						(param.getSqlType() == Types.BLOB || param.getSqlType() == Types.CLOB)) {
					throw new InvalidDataAccessApiUsageException(
							"BLOB or CLOB parameters are not allowed for this kind of operation");
				}
				if (param.getName() != null && !paramsToUse.containsKey(param.getName())) {
					throw new InvalidDataAccessApiUsageException("The parameter named '" + param.getName() +
							"' was not among the parameters supplied: " + paramsToUse.keySet());
				}
				declaredInParameters++;
			}
		}
		validateParameterCount(paramsToUse.size(), declaredInParameters);
	}

	/**
	 * 根据给定的声明参数验证给定的参数计数。
	 * @param suppliedParamCount 给出的实际参数的数量
	 * @param declaredInParamCount 声明的输入参数的数量
	 */
	private void validateParameterCount(int suppliedParamCount, int declaredInParamCount) {
		if (suppliedParamCount < declaredInParamCount) {
			throw new InvalidDataAccessApiUsageException(suppliedParamCount + " parameters were supplied, but " +
					declaredInParamCount + " in parameters were declared in class [" + getClass().getName() + "]");
		}
		if (suppliedParamCount > this.declaredParameters.size() && !allowsUnusedParameters()) {
			throw new InvalidDataAccessApiUsageException(suppliedParamCount + " parameters were supplied, but " +
					declaredInParamCount + " parameters were declared in class [" + getClass().getName() + "]");
		}
	}


	/**
	 * 子类必须实现此模板方法才能执行自己的编译。该基类编译完成后调用。 <p>子类可以假定已提供 SQL 和数据源。
	 * @throws InvalidDataAccessApiUsageException 如果子类没有正确配置
	 */
	protected abstract void compileInternal() throws InvalidDataAccessApiUsageException;

	/**
	 * 返回此类操作是否支持 BLOB/CLOB 参数。 <p>默认为{@code true}。
	 */
	protected boolean supportsLobParameters() {
		return true;
	}

	/**
	 * 返回此操作是否接受给定但未实际使用的附加参数。特别适用于参数映射。 <p>默认为{@code false}。
	 * @see StoredProcedure
	 */
	protected boolean allowsUnusedParameters() {
		return false;
	}

}
