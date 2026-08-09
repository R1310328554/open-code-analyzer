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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.jspecify.annotations.Nullable;

import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * 目标 JDBC {@link javax.sql.DataSource} 的代理，增加对 Spring 管理事务的感知。
 * 类似于 Jakarta EE 服务器提供的带事务 JNDI DataSource。
 *
 * <p>应保持对 Spring 数据访问支持无感的数据访问代码
 * 可通过此代理无缝参与 Spring 管理的事务。
 * 注意事务管理器（如 {@link DataSourceTransactionManager}）
 * 仍需与底层 DataSource 协作，<i>而非</i>此代理。
 *
 * <p><b>请确保 TransactionAwareDataSourceProxy 是 DataSource 代理/适配器链的最外层。</b>
 * 它可直接委托目标连接池，或委托 {@link LazyConnectionDataSourceProxy}、
 * {@link UserCredentialsDataSourceAdapter} 等中间代理/适配器。
 *
 * <p>委托 {@link DataSourceUtils} 自动参与线程绑定事务
 * （例如由 {@link DataSourceTransactionManager} 管理）。
 * 事务内 {@code getConnection} 及返回 Connection 上的 {@code close}
 * 将正确行为，即始终操作事务 Connection；非事务时使用普通 DataSource 行为。
 *
 * <p>此代理使数据访问代码使用纯 JDBC API 仍可参与 Spring 管理事务，
 * 类似 Jakarta EE/JTA 环境中的 JDBC 代码。但如有可能，应使用 Spring 的
 * DataSourceUtils、JdbcTemplate 或 JDBC 操作对象获取事务参与，
 * 无需为目标 DataSource 定义此类代理。
 *
 * <p>此外，使用事务感知 DataSource 会将剩余事务超时应用于
 * 所有创建的 JDBC (Prepared/Callable)Statement，
 * 即标准 JDBC 操作自动参与 Spring 管理的事务超时。
 *
 * <p><b>注意：</b> 此 DataSource 代理需返回包装 Connection（实现 {@link ConnectionProxy}），
 * 以正确处理 close 调用。使用 {@link Connection#unwrap} 获取原生 JDBC Connection。
 *
 * @author Juergen Hoeller
 * @since 1.1
 * @see javax.sql.DataSource#getConnection()
 * @see java.sql.Connection#close()
 * @see DataSourceUtils#doGetConnection
 * @see DataSourceUtils#applyTransactionTimeout
 * @see DataSourceUtils#doReleaseConnection
 */
public class TransactionAwareDataSourceProxy extends DelegatingDataSource {

	private boolean lazyTransactionalConnections = true;

	private boolean reobtainTransactionalConnections = false;


	/**
	 * 创建新的 TransactionAwareDataSourceProxy。
	 * @see #setTargetDataSource
	 */
	public TransactionAwareDataSourceProxy() {
	}

	/**
	 * 创建新的 TransactionAwareDataSourceProxy。
	 * @param targetDataSource 目标 DataSource
	 */
	public TransactionAwareDataSourceProxy(DataSource targetDataSource) {
		super(targetDataSource);
	}


	/**
	 * 指定是否在实际数据访问时懒获取事务目标 Connection。
	 * <p>默认为 "true"。设为 "false" 则在获取事务感知 Connection 句柄时立即获取目标 Connection。
	 * @since 6.1.2
	 */
	public void setLazyTransactionalConnections(boolean lazyTransactionalConnections) {
		this.lazyTransactionalConnections = lazyTransactionalConnections;
	}

	/**
	 * 指定事务内每次操作是否重新获取目标 Connection。
	 * <p>默认为 "false"。设为 "true" 则 Connection 代理每次调用都重新获取事务 Connection；
	 * 若在 JBoss 上跨事务边界持有 Connection 句柄，建议启用。
	 * <p>此设置效果类似 "hibernate.connection.release_mode" 的 "after_statement"。
	 */
	public void setReobtainTransactionalConnections(boolean reobtainTransactionalConnections) {
		this.reobtainTransactionalConnections = reobtainTransactionalConnections;
	}


	/**
	 * 委托 DataSourceUtils 自动参与 Spring 管理的事务。如有则抛出原始 SQLException。
	 * <p>返回的 Connection 句柄实现 ConnectionProxy 接口，
	 * 可获取底层目标 Connection。
	 * @return 有事务时返回事务 Connection，否则返回新连接
	 * @see DataSourceUtils#doGetConnection
	 * @see ConnectionProxy#getTargetConnection
	 */
	@Override
	public Connection getConnection() throws SQLException {
		DataSource ds = obtainTargetDataSource();
		Connection con = getTransactionAwareConnectionProxy(ds);
		if (!this.lazyTransactionalConnections && shouldObtainFixedConnection(ds)) {
			((ConnectionProxy) con).getTargetConnection();
		}
		return con;
	}

	/**
	 * 用代理包装给定 Connection，将所有方法调用委托给它，
	 * 但将 {@code close()} 委托给 DataSourceUtils。
	 * @param targetDataSource Connection 来源的 DataSource
	 * @return 包装后的 Connection
	 * @see java.sql.Connection#close()
	 * @see DataSourceUtils#doReleaseConnection
	 */
	protected Connection getTransactionAwareConnectionProxy(DataSource targetDataSource) {
		return (Connection) Proxy.newProxyInstance(
				ConnectionProxy.class.getClassLoader(),
				new Class<?>[] {ConnectionProxy.class},
				new TransactionAwareInvocationHandler(targetDataSource));
	}

	/**
	 * 确定代理是获取固定目标 Connection，还是每次操作重新获取。
	 * <p>默认实现对所有标准情况返回 {@code true}。
	 * 可通过 {@link #setReobtainTransactionalConnections "reobtainTransactionalConnections"}
	 * 标志覆盖，在活动事务内强制非固定目标 Connection。
	 * 注意非事务访问始终使用固定 Connection。
	 * @param targetDataSource 目标 DataSource
	 */
	protected boolean shouldObtainFixedConnection(DataSource targetDataSource) {
		return (!TransactionSynchronizationManager.isSynchronizationActive() ||
				!this.reobtainTransactionalConnections);
	}


	/**
	 * 将 JDBC Connection 上的 close 调用委托给 DataSourceUtils，
	 * 以感知线程绑定事务的调用处理器。
	 */
	private class TransactionAwareInvocationHandler implements InvocationHandler {

		private final DataSource targetDataSource;

		private @Nullable Connection target;

		private boolean closed = false;

		public TransactionAwareInvocationHandler(DataSource targetDataSource) {
			this.targetDataSource = targetDataSource;
		}

		@Override
		public @Nullable Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			// 来自 ConnectionProxy 接口的调用...

			switch (method.getName()) {
				case "equals" -> {
					// 仅当代理相同时视为相等。
					return (proxy == args[0]);
				}
				case "hashCode" -> {
					// 使用 Connection 代理的 hashCode。
					return System.identityHashCode(proxy);
				}
				case "toString" -> {
					// 便于区分代理与原始 Connection。
					StringBuilder sb = new StringBuilder("Transaction-aware proxy for target Connection ");
					if (this.target != null) {
						sb.append('[').append(this.target).append(']');
					}
					else {
						sb.append("from DataSource [").append(this.targetDataSource).append(']');
					}
					return sb.toString();
				}
				case "close" -> {
					// 处理 close 方法：仅非事务内才关闭。
					if (this.target != null) {
						ConnectionHolder conHolder = (ConnectionHolder)
								TransactionSynchronizationManager.getResource(this.targetDataSource);
						if (conHolder != null && conHolder.hasConnection() && conHolder.getConnection() == this.target) {
							// 这是事务 Connection：不关闭。
							conHolder.released();
						}
						else {
							DataSourceUtils.doCloseConnection(this.target, this.targetDataSource);
						}
					}
					this.closed = true;
					return null;
				}
				case "isClosed" -> {
					return this.closed;
				}
				case "unwrap" -> {
					if (((Class<?>) args[0]).isInstance(proxy)) {
						return proxy;
					}
				}
				case "isWrapperFor" -> {
					if (((Class<?>) args[0]).isInstance(proxy)) {
						return true;
					}
				}
			}

			if (this.target == null) {
				if (method.getName().equals("getWarnings") || method.getName().equals("clearWarnings")) {
					// 关闭前清理时避免创建目标 Connection（例如 Hibernate Session）
					return null;
				}
				if (this.closed) {
					throw new SQLException("Connection handle already closed");
				}
				if (shouldObtainFixedConnection(this.targetDataSource)) {
					this.target = DataSourceUtils.doGetConnection(this.targetDataSource);
				}
			}
			Connection actualTarget = this.target;
			if (actualTarget == null) {
				actualTarget = DataSourceUtils.doGetConnection(this.targetDataSource);
			}

			if (method.getName().equals("getTargetConnection")) {
				// 处理 getTargetConnection 方法：返回底层 Connection。
				return actualTarget;
			}

			// 在目标 Connection 上调用方法。
			try {
				Object retVal = method.invoke(actualTarget, args);

				// 若返回值为 Statement，应用事务超时。
				// 适用于 createStatement、prepareStatement、prepareCall。
				if (retVal instanceof Statement statement) {
					DataSourceUtils.applyTransactionTimeout(statement, this.targetDataSource);
				}

				return retVal;
			}
			catch (InvocationTargetException ex) {
				throw ex.getTargetException();
			}
			finally {
				if (actualTarget != this.target) {
					DataSourceUtils.doReleaseConnection(actualTarget, this.targetDataSource);
				}
			}
		}
	}

}
