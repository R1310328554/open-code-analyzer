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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;

import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.jdbc.core.CallableStatementCreator;
import org.springframework.jdbc.core.CallableStatementCreatorFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.metadata.CallMetaDataContext;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * 抽象类，为基于配置选项和数据库元数据的轻松存储过程调用提供基本功能。
 * <p>该类提供{@link SimpleJdbcCall}的处理安排。
 * @author Thomas Risberg
 * @author Juergen Hoeller
 * @since 2.5
 */
public abstract class AbstractJdbcCall {

	/**
	 */
	protected final Log logger = LogFactory.getLog(getClass());

	/**
	 */
	private final JdbcTemplate jdbcTemplate;

	/**
	 */
	private final CallMetaDataContext callMetaDataContext = new CallMetaDataContext();

	/**
	 */
	private final List<SqlParameter> declaredParameters = new ArrayList<>();

	/**
	 */
	private final Map<String, RowMapper<?>> declaredRowMappers = new LinkedHashMap<>();

	/**
	 */
	private final Lock compilationLock = new ReentrantLock();

	/**
	 * 这个操作编译了吗？编译意味着至少检查是否已提供 DataSource 或 JdbcTemplate。
	 */
	private volatile boolean compiled;

	/**
	 */
	private @Nullable String callString;

	/**
	 * 委托使我们能够根据此类的声明参数高效地创建 CallableStatementCreators。
	 */
	private @Nullable CallableStatementCreatorFactory callableStatementFactory;


	/**
	 * 使用 {@link DataSource} 初始化时要使用的构造函数。
	 * @param dataSource 要使用的数据源
	 */
	protected AbstractJdbcCall(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	/**
	 * 使用 {@link JdbcTemplate} 初始化时要使用的构造函数。
	 * @param jdbcTemplate 要使用的 JdbcTemplate
	 */
	protected AbstractJdbcCall(JdbcTemplate jdbcTemplate) {
		Assert.notNull(jdbcTemplate, "JdbcTemplate must not be null");
		this.jdbcTemplate = jdbcTemplate;
	}


	/**
	 * 获取配置的{@link JdbcTemplate}。
	 */
	public JdbcTemplate getJdbcTemplate() {
		return this.jdbcTemplate;
	}

	/**
	 * 设置存储过程的名称。
	 */
	public void setProcedureName(@Nullable String procedureName) {
		this.callMetaDataContext.setProcedureName(procedureName);
	}

	/**
	 * 获取存储过程的名称。
	 */
	public @Nullable String getProcedureName() {
		return this.callMetaDataContext.getProcedureName();
	}

	/**
	 * 设置要使用的输入参数的名称。
	 */
	public void setInParameterNames(Set<String> inParameterNames) {
		this.callMetaDataContext.setLimitedInParameterNames(inParameterNames);
	}

	/**
	 * 获取要使用的 in 参数的名称。
	 */
	public Set<String> getInParameterNames() {
		return this.callMetaDataContext.getLimitedInParameterNames();
	}

	/**
	 * 设置要使用的目录名称。
	 */
	public void setCatalogName(@Nullable String catalogName) {
		this.callMetaDataContext.setCatalogName(catalogName);
	}

	/**
	 * 获取使用的目录名称。
	 */
	public @Nullable String getCatalogName() {
		return this.callMetaDataContext.getCatalogName();
	}

	/**
	 * 设置要使用的架构名称。
	 */
	public void setSchemaName(@Nullable String schemaName) {
		this.callMetaDataContext.setSchemaName(schemaName);
	}

	/**
	 * 获取使用的架构名称。
	 */
	public @Nullable String getSchemaName() {
		return this.callMetaDataContext.getSchemaName();
	}

	/**
	 * 指定此调用是否是函数调用。默认为 {@code false}。
	 */
	public void setFunction(boolean function) {
		this.callMetaDataContext.setFunction(function);
	}

	/**
	 * 这个调用是函数调用吗？
	 */
	public boolean isFunction() {
		return this.callMetaDataContext.isFunction();
	}

	/**
	 * 指定调用是否需要返回值。默认为 {@code false}。
	 */
	public void setReturnValueRequired(boolean returnValueRequired) {
		this.callMetaDataContext.setReturnValueRequired(returnValueRequired);
	}

	/**
	 * 调用需要返回值吗？
	 */
	public boolean isReturnValueRequired() {
		return this.callMetaDataContext.isReturnValueRequired();
	}

	/**
	 * 指定参数是否应按名称绑定。默认为 {@code false}。
	 * @since 4.2
	 */
	public void setNamedBinding(boolean namedBinding) {
		this.callMetaDataContext.setNamedBinding(namedBinding);
	}

	/**
	 * 参数应该按名称绑定吗？
	 * @since 4.2
	 */
	public boolean isNamedBinding() {
		return this.callMetaDataContext.isNamedBinding();
	}

	/**
	 * 指定是否应使用调用的参数元数据。默认为 {@code true}。
	 */
	public void setAccessCallParameterMetaData(boolean accessCallParameterMetaData) {
		this.callMetaDataContext.setAccessCallParameterMetaData(accessCallParameterMetaData);
	}

	/**
	 * 根据参数和元数据获取应使用的调用字符串。
	 */
	public @Nullable String getCallString() {
		return this.callString;
	}

	/**
	 * 获取正在使用的 {@link CallableStatementCreatorFactory}。
	 */
	protected CallableStatementCreatorFactory getCallableStatementFactory() {
		Assert.state(this.callableStatementFactory != null, "No CallableStatementCreatorFactory available");
		return this.callableStatementFactory;
	}


	/**
	 * 将声明的参数添加到调用的参数列表中。 <p>仅声明为 {@code SqlParameter} 和 {@code SqlInOutParameter}
	 * 的参数将用于提供输入值。这与 {@code StoredProcedure} 类不同，出于向后兼容性的原因，{@code StoredProcedure} 类允许为声明为
	 * {@code SqlOutParameter} 的参数提供输入值。
	 * @param parameter 要添加的 {@link SqlParameter}
	 */
	public void addDeclaredParameter(SqlParameter parameter) {
		if (isCompiled()) {
			throw new IllegalStateException("SqlCall for " + (isFunction() ? "function" : "procedure") +
					" is already compiled");
		}
		Assert.notNull(parameter, "The supplied parameter must not be null");
		if (!StringUtils.hasText(parameter.getName())) {
			throw new InvalidDataAccessApiUsageException(
					"You must specify a parameter name when declaring parameters for \"" + getProcedureName() + "\"");
		}
		this.declaredParameters.add(parameter);
		if (logger.isDebugEnabled()) {
			logger.debug("Added declared parameter for [" + getProcedureName() + "]: " + parameter.getName());
		}
	}

	/**
	 * 为指定的参数或列添加 {@link org.springframework.jdbc.core.RowMapper}。
	 * @param parameterName 参数或列的名称
	 * @param rowMapper 要使用的 RowMapper 实现
	 */
	public void addDeclaredRowMapper(String parameterName, RowMapper<?> rowMapper) {
		if (isCompiled()) {
			throw new IllegalStateException("SqlCall for " + (isFunction() ? "function" : "procedure") +
					" is already compiled");
		}
		this.declaredRowMappers.put(parameterName, rowMapper);
		if (logger.isDebugEnabled()) {
			logger.debug("Added row mapper for [" + getProcedureName() + "]: " + parameterName);
		}
	}


	//-------------------------------------------------------------------------
	// 处理编译问题的方法
	//-------------------------------------------------------------------------

	/**
	 * 使用提供的参数和元数据以及其他设置编译此 JdbcCall。 <p>这最终确定了该对象的配置，并且随后的编译尝试将被忽略。这将在第一次执行未编译的调用时被隐式调用。
	 * @throws org.springframework.dao.InvalidDataAccessApiUsageException 如果对象尚未正确初始化，例如，如果未提供 DataSource
	 */
	public final void compile() throws InvalidDataAccessApiUsageException {
		this.compilationLock.lock();
		try {
			if (!isCompiled()) {
				if (getProcedureName() == null) {
					throw new InvalidDataAccessApiUsageException("Procedure or Function name is required");
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
					logger.debug("SqlCall for " + (isFunction() ? "function" : "procedure") +
							" [" + getProcedureName() + "] compiled");
				}
			}
		}
		finally {
			this.compilationLock.unlock();
		}
	}

	/**
	 * 执行实际编译的委托方法。 <p>子类可以重写此模板方法来执行自己的编译。该基类编译完成后调用。
	 */
	protected void compileInternal() {
		DataSource dataSource = getJdbcTemplate().getDataSource();
		Assert.state(dataSource != null, "No DataSource set");
		this.callMetaDataContext.initializeMetaData(dataSource);

		// 迭代声明的RowMappers并注册相应的SqlParameter
		this.declaredRowMappers.forEach((key, value) -> this.declaredParameters.add(this.callMetaDataContext.createReturnResultSetParameter(key, value)));
		this.callMetaDataContext.processParameters(this.declaredParameters);

		this.callString = this.callMetaDataContext.createCallString();
		if (logger.isDebugEnabled()) {
			logger.debug("Compiled stored procedure. Call string is [" + this.callString + "]");
		}

		this.callableStatementFactory = new CallableStatementCreatorFactory(
				this.callString, this.callMetaDataContext.getCallParameters());

		onCompileInternal();
	}

	/**
	 * 子类可以重写以对编译做出反应的钩子方法。这个实现什么也不做。
	 */
	protected void onCompileInternal() {
	}

	/**
	 * 这个操作是“编译”的吗？
	 * @return 该操作已编译并可以使用
	 */
	public boolean isCompiled() {
		return this.compiled;
	}

	/**
	 * 检查该操作是否已经编译；如果尚未编译，则延迟编译它。 <p> 由所有 {@code doExecute(...)} 方法自动调用。
	 */
	protected void checkCompiled() {
		if (!isCompiled()) {
			logger.debug("JdbcCall call not compiled before execution - invoking compile");
			compile();
		}
	}


	//-------------------------------------------------------------------------
	// 处理执行的方法
	//-------------------------------------------------------------------------

	/**
	 * 使用传入的 {@link SqlParameterSource} 执行调用的委托方法。
	 * @param parameterSource 调用中使用的参数名称和值
	 * @return 输出参数图
	 */
	protected Map<String, @Nullable Object> doExecute(SqlParameterSource parameterSource) {
		checkCompiled();
		Map<String, Object> params = matchInParameterValuesWithCallParameters(parameterSource);
		return executeCallInternal(params);
	}

	/**
	 * 使用传入的参数数组执行调用的委托方法。
	 * @param args 参数值数组。值的顺序必须与为存储过程声明的顺序匹配。
	 * @return 输出参数图
	 */
	protected Map<String, @Nullable Object> doExecute(Object... args) {
		checkCompiled();
		Map<String, ?> params = matchInParameterValuesWithCallParameters(args);
		return executeCallInternal(params);
	}

	/**
	 * 使用传入的参数映射执行调用的委托方法。
	 * @param args 参数名称和值的映射
	 * @return 输出参数图
	 */
	protected Map<String, @Nullable Object> doExecute(Map<String, ?> args) {
		checkCompiled();
		Map<String, ?> params = matchInParameterValuesWithCallParameters(args);
		return executeCallInternal(params);
	}

	/**
	 * 委托方法执行实际的调用处理。
	 */
	private Map<String, @Nullable Object> executeCallInternal(Map<String, ?> args) {
		CallableStatementCreator csc = getCallableStatementFactory().newCallableStatementCreator(args);
		if (logger.isDebugEnabled()) {
			logger.debug("The following parameters are used for call " + getCallString() + " with " + args);
			int i = 1;
			for (SqlParameter param : getCallParameters()) {
				logger.debug(i + ": " + param.getName() + ", SQL type " + param.getSqlType() + ", type name " +
						param.getTypeName() + ", parameter class [" + param.getClass().getName() + "]");
				i++;
			}
		}
		return getJdbcTemplate().call(csc, getCallParameters());
	}


	/**
	 * 获取单个输出参数或返回值的名称。用于带有一个输出参数的函数或过程。
	 */
	protected @Nullable String getScalarOutParameterName() {
		return this.callMetaDataContext.getScalarOutParameterName();
	}

	/**
	 * 获取用于调用的所有调用参数的列表。这包括基于元数据处理添加的任何参数。
	 */
	protected List<SqlParameter> getCallParameters() {
		return this.callMetaDataContext.getCallParameters();
	}

	/**
	 * 将提供的参数值与注册参数和通过元数据处理定义的参数进行匹配。
	 * @param parameterSource 以 {@link SqlParameterSource} 形式提供的参数值
	 * @return 包含参数名称和值的映射
	 */
	protected Map<String, Object> matchInParameterValuesWithCallParameters(SqlParameterSource parameterSource) {
		return this.callMetaDataContext.matchInParameterValuesWithCallParameters(parameterSource);
	}

	/**
	 * 将提供的参数值与注册参数和通过元数据处理定义的参数进行匹配。
	 * @param args 以数组形式提供的参数值
	 * @return 包含参数名称和值的映射
	 */
	private Map<String, ?> matchInParameterValuesWithCallParameters(Object[] args) {
		return this.callMetaDataContext.matchInParameterValuesWithCallParameters(args);
	}

	/**
	 * 将提供的参数值与注册参数和通过元数据处理定义的参数进行匹配。
	 * @param args 以 Map 形式提供的参数值
	 * @return 包含参数名称和值的映射
	 */
	protected Map<String, ?> matchInParameterValuesWithCallParameters(Map<String, ?> args) {
		return this.callMetaDataContext.matchInParameterValuesWithCallParameters(args);
	}

}
