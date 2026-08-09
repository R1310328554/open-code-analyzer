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

import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.jspecify.annotations.Nullable;

import org.springframework.jdbc.core.CallableStatementCreator;
import org.springframework.jdbc.core.CallableStatementCreatorFactory;
import org.springframework.jdbc.core.ParameterMapper;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.util.Assert;

/**
 * RdbmsOperation 使用 JdbcTemplate 并表示基于 SQL 的调用，例如存储过程或存储函数。
 * <p>根据声明的参数配置Call​​ableStatementCreatorFactory。
 * @author Rod Johnson
 * @author Thomas Risberg
 * @see CallableStatementCreatorFactory
 */
public abstract class SqlCall extends RdbmsOperation {

	/**
	 * 标志用于指示此调用是针对函数的，并使用 {? = 调用 get_invoice_count(?)} 语法。
	 */
	private boolean function = false;

	/**
	 * 用于指示此调用的 sql 应完全按照定义使用的标志。无需添加转义语法和参数占位符。
	 */
	private boolean sqlReadyForUse = false;

	/**
	 * 调用字符串如 java.sql.CallableStatement 中定义。形式为 {call add_invoice(?, ?, ?)} 或 {? = 如果
	 * isFunction 设置为 true，则调用 get_invoice_count(?)}。添加每个参数后更新。
	 */
	private @Nullable String callString;

	/**
	 * 对象使我们能够根据此类的声明参数有效地创建 CallableStatementCreators。
	 */
	private @Nullable CallableStatementCreatorFactory callableStatementFactory;


	/**
	 * 允许用作 JavaBean 的构造函数。在调用 {@code compile} 方法和使用此对象之前，必须提供数据源、SQL 和任何参数。
	 * @see #setDataSource
	 * @see #setSql
	 * @see #compile
	 */
	public SqlCall() {
	}

	/**
	 * 使用 SQL 创建一个新的 SqlCall 对象，但不带参数。必须添加参数或不设置任何参数。
	 * @param ds 从中获取连接的数据源
	 * @param sql 要执行的 SQL
	 */
	public SqlCall(DataSource ds, String sql) {
		setDataSource(ds);
		setSql(sql);
	}


	/**
	 * 设置此调用是否针对函数。
	 */
	public void setFunction(boolean function) {
		this.function = function;
	}

	/**
	 * 返回此调用是否针对函数。
	 */
	public boolean isFunction() {
		return this.function;
	}

	/**
	 * 设置SQL是否可以按原样使用。
	 */
	public void setSqlReadyForUse(boolean sqlReadyForUse) {
		this.sqlReadyForUse = sqlReadyForUse;
	}

	/**
	 * 返回SQL是否可以按原样使用。
	 */
	public boolean isSqlReadyForUse() {
		return this.sqlReadyForUse;
	}


	/**
	 * 重写方法以根据我们声明的参数配置 CallableStatementCreatorFactory。
	 * @see RdbmsOperation#compileInternal()
	 */
	@Override
	protected final void compileInternal() {
		if (isSqlReadyForUse()) {
			this.callString = resolveSql();
		}
		else {
			StringBuilder callString = new StringBuilder(32);
			List<SqlParameter> parameters = getDeclaredParameters();
			int parameterCount = 0;
			if (isFunction()) {
				callString.append("{? = call ").append(resolveSql()).append('(');
				parameterCount = -1;
			}
			else {
				callString.append("{call ").append(resolveSql()).append('(');
			}
			for (SqlParameter parameter : parameters) {
				if (!parameter.isResultsParameter()) {
					if (parameterCount > 0) {
						callString.append(", ");
					}
					if (parameterCount >= 0) {
						callString.append('?');
					}
					parameterCount++;
				}
			}
			callString.append(")}");
			this.callString = callString.toString();
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Compiled stored procedure. Call string is [" + this.callString + "]");
		}

		this.callableStatementFactory = new CallableStatementCreatorFactory(this.callString, getDeclaredParameters());
		this.callableStatementFactory.setResultSetType(getResultSetType());
		this.callableStatementFactory.setUpdatableResults(isUpdatableResults());

		onCompileInternal();
	}

	/**
	 * 子类可以重写以对编译做出反应的钩子方法。这个实现什么也不做。
	 */
	protected void onCompileInternal() {
	}

	/**
	 * 获取调用字符串。
	 */
	public @Nullable String getCallString() {
		return this.callString;
	}

	/**
	 * 返回 CallableStatementCreator 以使用这些参数执行操作。
	 * @param inParams 参数。可能是 {@code null}。
	 */
	protected CallableStatementCreator newCallableStatementCreator(@Nullable Map<String, ?> inParams) {
		Assert.state(this.callableStatementFactory != null, "No CallableStatementFactory available");
		return this.callableStatementFactory.newCallableStatementCreator(inParams);
	}

	/**
	 * 返回 CallableStatementCreator 以使用从此 ParameterMapper 返回的参数执行操作。
	 * @param inParamMapper 参数映射器。可能不是 {@code null}。
	 */
	protected CallableStatementCreator newCallableStatementCreator(ParameterMapper inParamMapper) {
		Assert.state(this.callableStatementFactory != null, "No CallableStatementFactory available");
		return this.callableStatementFactory.newCallableStatementCreator(inParamMapper);
	}

}
