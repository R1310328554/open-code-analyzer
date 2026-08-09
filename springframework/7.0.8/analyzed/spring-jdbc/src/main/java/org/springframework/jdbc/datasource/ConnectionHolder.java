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
 * 包装 JDBC {@link Connection} 的资源持有者。
 * {@link DataSourceTransactionManager} 会将本类实例
 * 绑定到特定 {@link javax.sql.DataSource} 的线程上下文。
 *
 * <p>从基类继承嵌套 JDBC 事务的 rollback-only 支持与引用计数功能。
 *
 * <p>注意：这是 SPI 类，不供应用程序直接使用。
 *
 * @author Juergen Hoeller
 * @since 06.05.2003
 * @see DataSourceTransactionManager
 * @see DataSourceUtils
 */
public class ConnectionHolder extends ResourceHolderSupport {

	/**
	 * 保存点名称前缀。
	 */
	public static final String SAVEPOINT_NAME_PREFIX = "SAVEPOINT_";


	private @Nullable ConnectionHandle connectionHandle;

	private @Nullable Connection currentConnection;

	private boolean transactionActive = false;

	private @Nullable Boolean savepointsSupported;

	private int savepointCounter = 0;


	/**
	 * 为给定 ConnectionHandle 创建新的 ConnectionHolder。
	 * @param connectionHandle 要持有的 ConnectionHandle
	 */
	public ConnectionHolder(ConnectionHandle connectionHandle) {
		Assert.notNull(connectionHandle, "ConnectionHandle must not be null");
		this.connectionHandle = connectionHandle;
	}

	/**
	 * 为给定 JDBC Connection 创建新的 ConnectionHolder，
	 * 使用 {@link SimpleConnectionHandle} 包装，假定当前无进行中的事务。
	 * @param connection 要持有的 JDBC Connection
	 * @see SimpleConnectionHandle
	 * @see #ConnectionHolder(java.sql.Connection, boolean)
	 */
	public ConnectionHolder(Connection connection) {
		this.connectionHandle = new SimpleConnectionHandle(connection);
	}

	/**
	 * 为给定 JDBC Connection 创建新的 ConnectionHolder，
	 * 使用 {@link SimpleConnectionHandle} 包装。
	 * @param connection 要持有的 JDBC Connection
	 * @param transactionActive 给定 Connection 是否参与进行中的事务
	 * @see SimpleConnectionHandle
	 */
	public ConnectionHolder(Connection connection, boolean transactionActive) {
		this(connection);
		this.transactionActive = transactionActive;
	}


	/**
	 * 返回本 ConnectionHolder 持有的 ConnectionHandle。
	 */
	public @Nullable ConnectionHandle getConnectionHandle() {
		return this.connectionHandle;
	}

	/**
	 * 返回本持有者当前是否持有 Connection。
	 */
	protected boolean hasConnection() {
		return (this.connectionHandle != null);
	}

	/**
	 * 设置本持有者是否表示活跃的、由 JDBC 管理的事务。
	 * @see DataSourceTransactionManager
	 */
	protected void setTransactionActive(boolean transactionActive) {
		this.transactionActive = transactionActive;
	}

	/**
	 * 返回本持有者是否表示活跃的、由 JDBC 管理的事务。
	 */
	protected boolean isTransactionActive() {
		return this.transactionActive;
	}


	/**
	 * 用给定 Connection 覆盖现有 Connection 句柄。
	 * 若传入 {@code null} 则重置句柄。
	 * <p>用于挂起时释放 Connection（传入 {@code null}）
	 * 以及恢复时设置新 Connection。
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
	 * 返回本 ConnectionHolder 当前持有的 Connection。
	 * <p>在调用 {@code released} 重置之前，将始终是同一 Connection；
	 * 重置后按需获取新 Connection。
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
	 * 返回是否支持 JDBC 保存点。
	 * 在本 ConnectionHolder 生命周期内缓存该标志。
	 * @throws SQLException JDBC 驱动抛出时
	 */
	public boolean supportsSavepoints() throws SQLException {
		if (this.savepointsSupported == null) {
			this.savepointsSupported = getConnection().getMetaData().supportsSavepoints();
		}
		return this.savepointsSupported;
	}

	/**
	 * 为当前 Connection 创建新的 JDBC 保存点，
	 * 使用对该 Connection 唯一的生成名称。
	 * @return 新的 Savepoint
	 * @throws SQLException JDBC 驱动抛出时
	 */
	public Savepoint createSavepoint() throws SQLException {
		this.savepointCounter++;
		return getConnection().setSavepoint(SAVEPOINT_NAME_PREFIX + this.savepointCounter);
	}

	/**
	 * 释放本 ConnectionHolder 当前持有的 Connection。
	 * <p>对于期望“连接借用”的 ConnectionHandle 而言这是必要的：
	 * 每次返回的 Connection 仅临时租借，数据操作完成后须归还，
	 * 以便同一事务内其他操作继续使用。
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


	@Override
	public void clear() {
		super.clear();
		this.transactionActive = false;
		this.savepointsSupported = null;
		this.savepointCounter = 0;
	}

}
