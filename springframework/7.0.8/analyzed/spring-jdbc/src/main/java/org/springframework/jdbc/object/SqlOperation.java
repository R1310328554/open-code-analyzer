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
 * 表示基于 SQL 的操作（如查询或更新）的操作对象，区别于存储过程。
 *
 * <p>根据声明的参数配置 {@link org.springframework.jdbc.core.PreparedStatementCreatorFactory}。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
public abstract class SqlOperation extends RdbmsOperation {

	/**
	 * 基于本类声明的参数高效创建 PreparedStatementCreator 的工厂对象。
	 */
	private @Nullable PreparedStatementCreatorFactory preparedStatementFactory;

	/** SQL 语句的解析表示。 */
	private @Nullable ParsedSql cachedSql;

	/** 用于锁定已解析 SQL 语句缓存表示的监视器。 */
	private final Object parsedSqlMonitor = new Object();


	/**
	 * 重写方法，根据声明的参数配置 PreparedStatementCreatorFactory。
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
	 * 子类可覆盖的后处理编译钩子方法，本实现为空操作。
	 * @see #compileInternal
	 */
	protected void onCompileInternal() {
	}

	/**
	 * 获取本操作 SQL 语句的解析表示。
	 * <p>通常用于命名参数解析。
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
	 * 返回 PreparedStatementSetter，以给定参数执行操作。
	 * @param params 参数数组（可为 {@code null}）
	 */
	protected final PreparedStatementSetter newPreparedStatementSetter(@Nullable Object @Nullable [] params) {
		Assert.state(this.preparedStatementFactory != null, "No PreparedStatementFactory available");
		return this.preparedStatementFactory.newPreparedStatementSetter(params);
	}

	/**
	 * 返回 PreparedStatementCreator，以给定参数执行操作。
	 * @param params 参数数组（可为 {@code null}）
	 */
	protected final PreparedStatementCreator newPreparedStatementCreator(@Nullable Object @Nullable [] params) {
		Assert.state(this.preparedStatementFactory != null, "No PreparedStatementFactory available");
		return this.preparedStatementFactory.newPreparedStatementCreator(params);
	}

	/**
	 * 返回 PreparedStatementCreator，以给定参数执行操作。
	 * @param sqlToUse 实际使用的 SQL 语句（若与工厂不同，例如因命名参数展开）
	 * @param params 参数数组（可为 {@code null}）
	 */
	protected final PreparedStatementCreator newPreparedStatementCreator(String sqlToUse, @Nullable Object @Nullable [] params) {
		Assert.state(this.preparedStatementFactory != null, "No PreparedStatementFactory available");
		return this.preparedStatementFactory.newPreparedStatementCreator(sqlToUse, params);
	}

}
