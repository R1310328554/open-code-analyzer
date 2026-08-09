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
 * 目标 {@link javax.sql.DataSource} 的适配器，将当前 Spring 事务的隔离级别（以及可能指定的用户凭据）应用于每个 {@code getConn
 * ection} 调用。如果指定，还应用只读标志。
 * <p> 可用于代理未配置所需隔离级别（和用户凭据）的目标 JNDI 数据源。客户端代码可以像往常一样使用此数据源，而不必担心此类设置。
 * <p>继承了其超类{@link UserCredentialsDataSourceAdapter}应用特定用户凭证的能力；有关该功能的详细信息，请参阅后者的
 * javadoc（例如 {@link #setCredentialsForCurrentThread}）。
 * <p><b>WARNING:</b> 此适配器只是为从其获取的每个连接调用 {@link
 * java.sql.Connection#setTransactionIsolation} 和/或 {@link
 * java.sql.Connection#setReadOnly}。但是，<i>not</i> 会重置这些设置；它更希望目标数据源执行此类重置作为其连接池处理的一部分。
 * <b>确保目标数据源正确清理此类事务状态。</b>
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
	 * {@link TransactionDefinition} 中定义的隔离常量的常量名称到常量值的映射。
	 */
	static final Map<String, Integer> constants = Map.of(
			"ISOLATION_DEFAULT", TransactionDefinition.ISOLATION_DEFAULT,
			"ISOLATION_READ_UNCOMMITTED", TransactionDefinition.ISOLATION_READ_UNCOMMITTED,
			"ISOLATION_READ_COMMITTED", TransactionDefinition.ISOLATION_READ_COMMITTED,
			"ISOLATION_REPEATABLE_READ", TransactionDefinition.ISOLATION_REPEATABLE_READ,
			"ISOLATION_SERIALIZABLE", TransactionDefinition.ISOLATION_SERIALIZABLE
		);


	/** `isolationLevel`：该类的成员状态。 */
	private @Nullable Integer isolationLevel;


	/**
	 * 通过{@link
	 * org.springframework.transaction.TransactionDefinition}中对应常量的名称设置默认隔离级别&mdash;例如，{@code
	 * "ISOLATION_SERIALIZABLE"}。 <p>如果未指定，将使用目标数据源的默认值。请注意，特定于事务的隔离值将始终覆盖在数据源级别指定的任何隔离设置。
	 * @param constantName 常量名称
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
	 * 根据 JDBC {@link java.sql.Connection} 常量（相当于相应的 Spring {@link
	 * org.springframework.transaction.TransactionDefinition} 常量）指定用于连接检索的默认隔离级别。
	 * <p>如果未指定，将使用目标数据源的默认值。请注意，特定于事务的隔离值将始终覆盖在数据源级别指定的任何隔离设置。
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
	 * 返回静态指定的隔离级别，如果没有，则返回 {@code null}。
	 */
	protected @Nullable Integer getIsolationLevel() {
		return this.isolationLevel;
	}


	/**
	 * 将当前隔离级别值和只读标志应用于返回的连接。
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
	 * 确定当前隔离级别：事务的隔离级别或静态定义的隔离级别。
	 * @return 当前隔离级别，如果没有，则为 {@code null}
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
	 * 确定当前只读标志：默认情况下，事务的只读提示。
	 * @return 当前范围有一个只读提示
	 * @see org.springframework.transaction.support.TransactionSynchronizationManager#isCurrentTransactionReadOnly()
	 */
	protected @Nullable Boolean getCurrentReadOnlyFlag() {
		boolean txReadOnly = TransactionSynchronizationManager.isCurrentTransactionReadOnly();
		return (txReadOnly ? Boolean.TRUE : null);
	}

}
