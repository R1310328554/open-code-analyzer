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

import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Savepoint;

import org.jspecify.annotations.Nullable;

import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.NestedTransactionNotSupportedException;
import org.springframework.transaction.SavepointManager;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.TransactionUsageException;
import org.springframework.transaction.support.SmartTransactionObject;
import org.springframework.util.Assert;

/**
 * JDBC 感知事务对象的便捷基类。可以包含带有 JDBC {@code Connection} 的 {@link ConnectionHolder}，并基于该 {@code
 * ConnectionHolder} 实现 {@link SavepointManager} 接口。
 * <p> 允许以编程方式管理 JDBC {@link java.sql.Savepoint Savepoints}。 Spring 的 {@link
 * org.springframework.transaction.support.DefaultTransactionStatus} 自动委托给它，因为它自动检测实现
 * {@link SavepointManager} 接口的事务对象。
 * @author Juergen Hoeller
 * @since 1.1
 * @see DataSourceTransactionManager
 */
public abstract class JdbcTransactionObjectSupport implements SavepointManager, SmartTransactionObject {

	/** 连接相关状态（`connectionHolder`）。 */
	private @Nullable ConnectionHolder connectionHolder;

	/** `previousIsolationLevel`：该类的成员状态。 */
	private @Nullable Integer previousIsolationLevel;

	/** `false`：该类的成员状态。 */
	private boolean readOnly = false;

	/** `false`：该类的成员状态。 */
	private boolean savepointAllowed = false;


	/**
	 * 设置该事务对象的ConnectionHolder。
	 */
	public void setConnectionHolder(@Nullable ConnectionHolder connectionHolder) {
		this.connectionHolder = connectionHolder;
	}

	/**
	 * 返回此事务对象的 ConnectionHolder。
	 */
	public ConnectionHolder getConnectionHolder() {
		Assert.state(this.connectionHolder != null, "No ConnectionHolder available");
		return this.connectionHolder;
	}

	/**
	 * 检查该事务对象是否有ConnectionHolder。
	 */
	public boolean hasConnectionHolder() {
		return (this.connectionHolder != null);
	}

	/**
	 * 设置要保留的先前隔离级别（如果有）。
	 */
	public void setPreviousIsolationLevel(@Nullable Integer previousIsolationLevel) {
		this.previousIsolationLevel = previousIsolationLevel;
	}

	/**
	 * 返回保留的先前隔离级别（如果有）。
	 */
	public @Nullable Integer getPreviousIsolationLevel() {
		return this.previousIsolationLevel;
	}

	/**
	 * 设置该事务的只读状态。默认为 {@code false}。
	 * @since 5.2.1
	 */
	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}

	/**
	 * 返回此事务的只读状态。
	 * @since 5.2.1
	 */
	public boolean isReadOnly() {
		return this.readOnly;
	}

	/**
	 * 设置此事务中是否允许保存点。默认为 {@code false}。
	 */
	public void setSavepointAllowed(boolean savepointAllowed) {
		this.savepointAllowed = savepointAllowed;
	}

	/**
	 * 返回此事务中是否允许保存点。
	 */
	public boolean isSavepointAllowed() {
		return this.savepointAllowed;
	}


	//---------------------------------------------------------------------
	// SavepointManager的实现
	//---------------------------------------------------------------------

	/**
	 * 此实现创建一个 JDBC Savepoint 并返回它。
	 * @see java.sql.Connection#setSavepoint
	 */
	@Override
	public Object createSavepoint() throws TransactionException {
		ConnectionHolder conHolder = getConnectionHolderForSavepoint();
		try {
			if (!conHolder.supportsSavepoints()) {
				throw new NestedTransactionNotSupportedException(
						"Cannot create a nested transaction because savepoints are not supported by your JDBC driver");
			}
			if (conHolder.isRollbackOnly()) {
				throw new CannotCreateTransactionException(
						"Cannot create savepoint for transaction which is already marked as rollback-only");
			}
			return conHolder.createSavepoint();
		}
		catch (SQLException ex) {
			throw new CannotCreateTransactionException("Could not create JDBC savepoint", ex);
		}
	}

	/**
	 * 此实现回滚到给定的 JDBC 保存点。
	 * @see java.sql.Connection#rollback(java.sql.Savepoint)
	 */
	@Override
	public void rollbackToSavepoint(Object savepoint) throws TransactionException {
		ConnectionHolder conHolder = getConnectionHolderForSavepoint();
		try {
			conHolder.getConnection().rollback((Savepoint) savepoint);
			conHolder.resetRollbackOnly();
		}
		catch (Throwable ex) {
			throw new TransactionSystemException("Could not roll back to JDBC savepoint", ex);
		}
	}

	/**
	 * 此实现释放给定的 JDBC 保存点。
	 * @see java.sql.Connection#releaseSavepoint
	 */
	@Override
	public void releaseSavepoint(Object savepoint) throws TransactionException {
		ConnectionHolder conHolder = getConnectionHolderForSavepoint();
		try {
			conHolder.getConnection().releaseSavepoint((Savepoint) savepoint);
		}
		catch (SQLFeatureNotSupportedException ex) {
			// 通常在 Oracle 上 - 忽略
		}
		catch (SQLException ex) {
			if ("3B001".equals(ex.getSQLState())) {
				// Savepoint 已发布（HSQLDB、PostgreSQL、DB2） - 忽略
				return;
			}
			// 忽略 Microsoft SQLServerException：不支持此操作。
			String msg = ex.getMessage();
			if (msg == null || (!msg.contains("not supported") && !msg.contains("3B001"))) {
				throw new TransactionSystemException("Could not explicitly release JDBC savepoint", ex);
			}
		}
		catch (Throwable ex) {
			throw new TransactionSystemException("Could not explicitly release JDBC savepoint", ex);
		}
	}

	/**
	 * 获取 Connection Holder For Savepoint（`ConnectionHolderForSavepoint`）。
	 */
	protected ConnectionHolder getConnectionHolderForSavepoint() throws TransactionException {
		if (!isSavepointAllowed()) {
			throw new NestedTransactionNotSupportedException(
					"Transaction manager does not allow nested transactions");
		}
		if (!hasConnectionHolder()) {
			throw new TransactionUsageException(
					"Cannot create nested transaction when not exposing a JDBC transaction");
		}
		return getConnectionHolder();
	}

}
