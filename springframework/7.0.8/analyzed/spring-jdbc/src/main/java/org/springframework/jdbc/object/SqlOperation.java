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

import org.jspecify.annotations.Nullable;

import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.PreparedStatementCreatorFactory;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.namedparam.NamedParameterUtils;
import org.springframework.jdbc.core.namedparam.ParsedSql;
import org.springframework.util.Assert;

/**
 * 表示基于 SQL 的操作（例如查询或更新）的操作对象，而不是存储过程。
 * <p>根据声明的参数配置{@link org.springframework.jdbc.core.PreparedStatementCreatorFactory}。
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
public abstract class SqlOperation extends RdbmsOperation {

	/**
	 * 对象使我们能够根据此类的声明参数有效地创建PreparedStatementCreators。
	 */
	private @Nullable PreparedStatementCreatorFactory preparedStatementFactory;

	/**
	 */
	private @Nullable ParsedSql cachedSql;

	/**
	 */
	private final Object parsedSqlMonitor = new Object();


	/**
	 * 重写方法以根据我们声明的参数配置PreparedStatementCreatorFactory。
	 */
	@Override
	protected final void compileInternal() {
		this.preparedStatementFactory = new PreparedStatementCreatorFactory(resolveSql(), getDeclaredParameters());
		this.preparedStatementFactory.setResultSetType(getResultSetType());
		this.preparedStatementFactory.setUpdatableResults(isUpdatableResults());
		this.preparedStatementFactory.setReturnGeneratedKeys(isReturnGeneratedKeys());
		if (getGeneratedKeysColumnNames() != null) {
			this.preparedStatementFactory.setGeneratedKeysColumnNames(getGeneratedKeysColumnNames());
		}

		onCompileInternal();
	}

	/**
	 * 子类可以覆盖后处理编译的钩子方法。这个实现什么也不做。
	 * @see #compileInternal
	 */
	protected void onCompileInternal() {
	}

	/**
	 * 获取此操作的 SQL 语句的解析表示。 <p>通常用于命名参数解析。
	 */
	protected ParsedSql getParsedSql() {
		synchronized (this.parsedSqlMonitor) {
			if (this.cachedSql == null) {
				this.cachedSql = NamedParameterUtils.parseSqlStatement(resolveSql());
			}
			return this.cachedSql;
		}
	}


	/**
	 * 返回一个PreparedStatementSetter以使用给定参数执行操作。
	 * @param params 参数数组（可能是 {@code null}）
	 */
	protected final PreparedStatementSetter newPreparedStatementSetter(@Nullable Object @Nullable [] params) {
		Assert.state(this.preparedStatementFactory != null, "No PreparedStatementFactory available");
		return this.preparedStatementFactory.newPreparedStatementSetter(params);
	}

	/**
	 * 返回一个PreparedStatementCreator以使用给定参数执行操作。
	 * @param params 参数数组（可能是 {@code null}）
	 */
	protected final PreparedStatementCreator newPreparedStatementCreator(@Nullable Object @Nullable [] params) {
		Assert.state(this.preparedStatementFactory != null, "No PreparedStatementFactory available");
		return this.preparedStatementFactory.newPreparedStatementCreator(params);
	}

	/**
	 * 返回一个PreparedStatementCreator以使用给定参数执行操作。
	 * @param sqlToUse 要使用的实际 SQL 语句（如果与工厂的不同，例如由于命名参数扩展）
	 * @param params 参数数组（可能是 {@code null}）
	 */
	protected final PreparedStatementCreator newPreparedStatementCreator(String sqlToUse, @Nullable Object @Nullable [] params) {
		Assert.state(this.preparedStatementFactory != null, "No PreparedStatementFactory available");
		return this.preparedStatementFactory.newPreparedStatementCreator(sqlToUse, params);
	}

}
