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
import java.sql.Savepoint;

import org.jspecify.annotations.Nullable;

import org.springframework.transaction.support.ResourceHolderSupport;
import org.springframework.util.Assert;

/**
 * 包装 JDBC {@link Connection} 的资源持有者。对于特定的 {@link javax.sql.DataSource}，{@link
 * DataSourceTransactionManager} 将此类的实例绑定到线程。
 * <p>从基类继承了对嵌套 JDBC 事务和引用计数功能的仅回滚支持。
 * <p>注意：这是一个 SPI 类，不适合由应用程序使用。
 * @author Juergen Hoeller
 * @since 06.05.2003
 * @see DataSourceTransactionManager
 * @see DataSourceUtils
 */
public class ConnectionHolder extends ResourceHolderSupport {

	/**
	 * 保存点名称的前缀。
	 */
	public static final String SAVEPOINT_NAME_PREFIX = "SAVEPOINT_";


	/** 连接相关状态（`connectionHandle`）。 */
	private @Nullable ConnectionHandle connectionHandle;

	/** 连接相关状态（`currentConnection`）。 */
	private @Nullable Connection currentConnection;

	/** `false`：该类的成员状态。 */
	private boolean transactionActive = false;

	/** `savepointsSupported`：该类的成员状态。 */
	private @Nullable Boolean savepointsSupported;

	private int savepointCounter = 0;


	/**
	 * 为给定的 ConnectionHandle 创建一个新的 ConnectionHolder。
	 * @param connectionHandle 要保存的 ConnectionHandle
	 */
	public ConnectionHolder(ConnectionHandle connectionHandle) {
		Assert.notNull(connectionHandle, "ConnectionHandle must not be null");
		this.connectionHandle = connectionHandle;
	}

	/**
	 * 为给定的 JDBC 连接创建一个新的 ConnectionHolder，用 {@link SimpleConnectionHandle} 包装它，假设没有正在进行的事务。
	 * @param connection 要保存的 JDBC 连接
	 * @see SimpleConnectionHandle
	 * @see #ConnectionHolder(java.sql.Connection, boolean)
	 */
	public ConnectionHolder(Connection connection) {
		this.connectionHandle = new SimpleConnectionHandle(connection);
	}

	/**
	 * 为给定的 JDBC 连接创建一个新的 ConnectionHolder，并用 {@link SimpleConnectionHandle} 包装它。
	 * @param connection 要保存的 JDBC 连接
	 * @param transactionActive 给定的连接是否参与正在进行的事务
	 * @see SimpleConnectionHandle
	 */
	public ConnectionHolder(Connection connection, boolean transactionActive) {
		this(connection);
		this.transactionActive = transactionActive;
	}


	/**
	 * 返回此 ConnectionHolder 持有的 ConnectionHandle。
	 */
	public @Nullable ConnectionHandle getConnectionHandle() {
		return this.connectionHandle;
	}

	/**
	 * 返回此持有者当前是否有连接。
	 */
	protected boolean hasConnection() {
		return (this.connectionHandle != null);
	}

	/**
	 * 设置此持有者是否代表活动的、由 JDBC 管理的事务。
	 * @see DataSourceTransactionManager
	 */
	protected void setTransactionActive(boolean transactionActive) {
		this.transactionActive = transactionActive;
	}

	/**
	 * 返回此持有者是否代表一个活动的、由 JDBC 管理的事务。
	 */
	protected boolean isTransactionActive() {
		return this.transactionActive;
	}


	/**
	 * 使用给定的连接覆盖现有的连接句柄。如果给出 {@code null}，则重置句柄。 <p>用于在挂起时释放连接（使用 {@code null} 参数）并在恢复时设置新连接。
	 */
	protected void setConnection(@Nullable Connection connection) {
		if (this.currentConnection != null) {
			if (this.connectionHandle != null) {
				this.connectionHandle.releaseConnection(this.currentConnection);
			}
			this.currentConnection = null;
		}
		if (connection != null) {
			this.connectionHandle = new SimpleConnectionHandle(connection);
		}
		else {
			this.connectionHandle = null;
		}
	}

	/**
	 * 返回此 ConnectionHolder 所持有的当前 Connection。 <p>这将是相同的连接，直到 {@code released} 在
	 * ConnectionHolder 上被调用，这将重置所保持的连接，按需获取新的连接。
	 * @see ConnectionHandle#getConnection()
	 * @see #released()
	 */
	public Connection getConnection() {
		Assert.state(this.connectionHandle != null, "Active Connection is required");
		if (this.currentConnection == null) {
			this.currentConnection = this.connectionHandle.getConnection();
		}
		return this.currentConnection;
	}

	/**
	 * 返回是否支持 JDBC 保存点。在此 ConnectionHolder 的生命周期内缓存该标志。
	 * @throws SQLException 如果由 JDBC 驱动程序抛出
	 */
	public boolean supportsSavepoints() throws SQLException {
		if (this.savepointsSupported == null) {
			this.savepointsSupported = getConnection().getMetaData().supportsSavepoints();
		}
		return this.savepointsSupported;
	}

	/**
	 * 使用对于连接来说唯一的生成的保存点名称为当前连接创建一个新的 JDBC 保存点。
	 * @return 新的保存点
	 * @throws SQLException 如果由 JDBC 驱动程序抛出
	 */
	public Savepoint createSavepoint() throws SQLException {
		this.savepointCounter++;
		return getConnection().setSavepoint(SAVEPOINT_NAME_PREFIX + this.savepointCounter);
	}

	/**
	 * 释放此 ConnectionHolder 所持有的当前 Connection。 <p>这对于期望“连接借用”的ConnectionHandles是必需的，其中每个返回的Conn
	 * ection只是临时租用，并且需要在数据操作完成后返回，以使Connection可用于同一事务中的其他操作。
	 */
	@Override
	public void released() {
		super.released();
		if (!isOpen() && this.currentConnection != null) {
			if (this.connectionHandle != null) {
				this.connectionHandle.releaseConnection(this.currentConnection);
			}
			this.currentConnection = null;
		}
	}


	/**
	 * 清空（方法 `clear`）。
	 */
	@Override
	public void clear() {
		super.clear();
		this.transactionActive = false;
		this.savepointsSupported = null;
		this.savepointCounter = 0;
	}

}
