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

package org.springframework.jdbc.datasource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;

/**
 * 目标 {@link javax.sql.DataSource} 的适配器，
 * 在每次 {@code getConnection} 调用时应用当前 Spring 事务的隔离级别
 * （以及可能指定的用户凭据），并在指定时应用只读标志。
 *
 * <p>可用于代理未配置所需隔离级别（及用户凭据）的目标 JNDI DataSource。
 * 客户端代码可照常使用本 DataSource，无需关心这些设置。
 *
 * <p>从超类 {@link UserCredentialsDataSourceAdapter} 继承应用特定用户凭据的能力；
 * 详见后者 javadoc（例如 {@link #setCredentialsForCurrentThread}）。
 *
 * <p><b>警告：</b> 本适配器仅对获取的每个 Connection 调用
 * {@link java.sql.Connection#setTransactionIsolation} 和/或
 * {@link java.sql.Connection#setReadOnly}。
 * 但<i>不会</i>重置这些设置，而是期望目标 DataSource
 * 在连接池处理中清理此类事务状态。
 * <b>请确保目标 DataSource 正确清理这些事务状态。</b>
 *
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 2.0.3
 * @see #setIsolationLevel
 * @see #setIsolationLevelName
 * @see #setUsername
 * @see #setPassword
 */
public class IsolationLevelDataSourceAdapter extends UserCredentialsDataSourceAdapter {

	/**
	 * {@link TransactionDefinition} 中定义的隔离级别常量名到常量值的映射。
	 */
	static final Map<String, Integer> constants = Map.of(
			"ISOLATION_DEFAULT", TransactionDefinition.ISOLATION_DEFAULT,
			"ISOLATION_READ_UNCOMMITTED", TransactionDefinition.ISOLATION_READ_UNCOMMITTED,
			"ISOLATION_READ_COMMITTED", TransactionDefinition.ISOLATION_READ_COMMITTED,
			"ISOLATION_REPEATABLE_READ", TransactionDefinition.ISOLATION_REPEATABLE_READ,
			"ISOLATION_SERIALIZABLE", TransactionDefinition.ISOLATION_SERIALIZABLE
		);


	private @Nullable Integer isolationLevel;


	/**
	 * 通过 {@link org.springframework.transaction.TransactionDefinition} 中对应常量名
	 * 设置默认隔离级别，例如 {@code "ISOLATION_SERIALIZABLE"}。
	 * <p>未指定时使用目标 DataSource 默认值。
	 * 事务级隔离值始终覆盖 DataSource 级设置。
	 * @param constantName 常量名
	 * @see org.springframework.transaction.TransactionDefinition#ISOLATION_READ_UNCOMMITTED
	 * @see org.springframework.transaction.TransactionDefinition#ISOLATION_READ_COMMITTED
	 * @see org.springframework.transaction.TransactionDefinition#ISOLATION_REPEATABLE_READ
	 * @see org.springframework.transaction.TransactionDefinition#ISOLATION_SERIALIZABLE
	 * @see #setIsolationLevel
	 */
	public final void setIsolationLevelName(String constantName) throws IllegalArgumentException {
		Assert.hasText(constantName, "'constantName' must not be null or blank");
		Integer isolationLevel = constants.get(constantName);
		Assert.notNull(isolationLevel, "Only isolation constants allowed");
		setIsolationLevel(isolationLevel);
	}

	/**
	 * 按 JDBC {@link java.sql.Connection} 常量（等价于 Spring
	 * {@link org.springframework.transaction.TransactionDefinition} 常量）
	 * 指定获取 Connection 时使用的默认隔离级别。
	 * <p>未指定时使用目标 DataSource 默认值。
	 * 事务级隔离值始终覆盖 DataSource 级设置。
	 * @see java.sql.Connection#TRANSACTION_READ_UNCOMMITTED
	 * @see java.sql.Connection#TRANSACTION_READ_COMMITTED
	 * @see java.sql.Connection#TRANSACTION_REPEATABLE_READ
	 * @see java.sql.Connection#TRANSACTION_SERIALIZABLE
	 * @see org.springframework.transaction.TransactionDefinition#ISOLATION_READ_UNCOMMITTED
	 * @see org.springframework.transaction.TransactionDefinition#ISOLATION_READ_COMMITTED
	 * @see org.springframework.transaction.TransactionDefinition#ISOLATION_REPEATABLE_READ
	 * @see org.springframework.transaction.TransactionDefinition#ISOLATION_SERIALIZABLE
	 * @see org.springframework.transaction.TransactionDefinition#getIsolationLevel()
	 * @see org.springframework.transaction.support.TransactionSynchronizationManager#getCurrentTransactionIsolationLevel()
	 */
	public void setIsolationLevel(int isolationLevel) {
		Assert.isTrue(constants.containsValue(isolationLevel), "Only values of isolation constants allowed");
		this.isolationLevel = (isolationLevel != TransactionDefinition.ISOLATION_DEFAULT ? isolationLevel : null);
	}

	/**
	 * 返回静态指定的隔离级别，未指定时返回 {@code null}。
	 */
	protected @Nullable Integer getIsolationLevel() {
		return this.isolationLevel;
	}


	/**
	 * 将当前隔离级别值和只读标志应用到返回的 Connection。
	 * @see #getCurrentIsolationLevel()
	 * @see #getCurrentReadOnlyFlag()
	 */
	@Override
	protected Connection doGetConnection(@Nullable String username, @Nullable String password) throws SQLException {
		Connection con = super.doGetConnection(username, password);
		Boolean readOnlyToUse = getCurrentReadOnlyFlag();
		if (readOnlyToUse != null) {
			con.setReadOnly(readOnlyToUse);
		}
		Integer isolationLevelToUse = getCurrentIsolationLevel();
		if (isolationLevelToUse != null) {
			con.setTransactionIsolation(isolationLevelToUse);
		}
		return con;
	}

	/**
	 * 确定当前隔离级别：事务隔离级别或静态定义的隔离级别。
	 * @return 当前隔离级别，无则返回 {@code null}
	 * @see org.springframework.transaction.support.TransactionSynchronizationManager#getCurrentTransactionIsolationLevel()
	 * @see #setIsolationLevel
	 */
	protected @Nullable Integer getCurrentIsolationLevel() {
		Integer isolationLevelToUse = TransactionSynchronizationManager.getCurrentTransactionIsolationLevel();
		if (isolationLevelToUse == null) {
			isolationLevelToUse = getIsolationLevel();
		}
		return isolationLevelToUse;
	}

	/**
	 * 确定当前只读标志：默认取事务的只读提示。
	 * @return 当前作用域是否存在只读提示
	 * @see org.springframework.transaction.support.TransactionSynchronizationManager#isCurrentTransactionReadOnly()
	 */
	protected @Nullable Boolean getCurrentReadOnlyFlag() {
		boolean txReadOnly = TransactionSynchronizationManager.isCurrentTransactionReadOnly();
		return (txReadOnly ? Boolean.TRUE : null);
	}

}
