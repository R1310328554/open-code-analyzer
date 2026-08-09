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
 * 目标 JDBC {@link javax.sql.DataSource} 的代理，增加了对 Spring 管理的事务的感知。类似于 Jakarta EE 服务器提供的事务性
 * JNDI 数据源。
 * <p> 应该不知道 Spring 数据访问支持的数据访问代码可以与此代理一起工作，以无缝参与 Spring 管理的事务。请注意，事务管理器（例如 {@link DataSour
 * ceTransactionManager}）仍然需要使用此代理来处理底层数据源 <i>not</i>。
 * <p><b>确保 TransactionAwareDataSourceProxy 是数据源代理/适配器链的最外层数据源。</b>
 * TransactionAwareDataSourceProxy 可以直接委托给目标连接池或某些中间代理/适配器，例如 {@link
 * LazyConnectionDataSourceProxy} 或 {@link UserCredentialsDataSourceAdapter}。
 * <p>D委托 {@link DataSourceUtils} 自动参与线程绑定事务，例如由 {@link DataSourceTransactionManager}
 * 管理。返回连接上的 {@code getConnection} 调用和 {@code close}
 * 调用将在事务内正常运行，即始终在事务连接上进行操作。如果不在事务内，则应用正常的数据源行为。
 * <p>此代理允许数据访问代码与普通 JDBC API 一起使用，并且仍然参与 Spring 管理的事务，类似于 Jakarta EE/JTA 环境中的 JDBC 代码。但是，如
 * 果可能的话，即使没有目标数据源的代理，也可以使用 Spring 的 DataSourceUtils、JdbcTemplate 或 JDBC 操作对象来获取事务参与，从而避免首先
 * 定义此类代理。
 * <p>A 的另一个效果是，使用事务感知数据源会将剩余事务超时应用于所有创建的 JDBC（Prepared/Callable）语句。这意味着通过标准 JDBC 执行的所有操作都将
 * 自动参与 Spring 管理的事务超时。
 * <p><b>NOTE:</b> 此数据源代理需要返回包装的连接（实现 {@link ConnectionProxy} 接口），以便正确处理关闭调用。使用 {@link
 * Connection#unwrap} 检索本机 JDBC 连接。
 * @author Juergen Hoeller
 * @since 1.1
 * @see javax.sql.DataSource#getConnection()
 * @see java.sql.Connection#close()
 * @see DataSourceUtils#doGetConnection
 * @see DataSourceUtils#applyTransactionTimeout
 * @see DataSourceUtils#doReleaseConnection
 */
public class TransactionAwareDataSourceProxy extends DelegatingDataSource {

	/** `true`：该类的成员状态。 */
	private boolean lazyTransactionalConnections = true;

	/** `false`：该类的成员状态。 */
	private boolean reobtainTransactionalConnections = false;


	/**
	 * 创建一个新的 TransactionAwareDataSourceProxy。
	 * @see #setTargetDataSource
	 */
	public TransactionAwareDataSourceProxy() {
	}

	/**
	 * 创建一个新的 TransactionAwareDataSourceProxy。
	 * @param targetDataSource 目标数据源
	 */
	public TransactionAwareDataSourceProxy(DataSource targetDataSource) {
		super(targetDataSource);
	}


	/**
	 * 指定是否在实际数据访问时延迟获取事务目标连接。 <p>默认为“true”。指定“false”可在检索事务感知连接句柄时立即获取目标连接。
	 * @since 6.1.2
	 */
	public void setLazyTransactionalConnections(boolean lazyTransactionalConnections) {
		this.lazyTransactionalConnections = lazyTransactionalConnections;
	}

	/**
	 * 指定是否为事务中执行的每个操作重新获取目标连接。 <p>默认为“false”。指定“true”以便为连接代理上的每次调用重新获取事务连接；如果您跨事务边界持有连接句柄，这在 J
	 * Boss 上是可取的。 <p> 这个设置的效果类似于“hibernate.connection.release_mode”值“after_statement”。
	 */
	public void setReobtainTransactionalConnections(boolean reobtainTransactionalConnections) {
		this.reobtainTransactionalConnections = reobtainTransactionalConnections;
	}


	/**
	 * 委托 DataSourceUtils 自动参与 Spring 管理的事务。抛出原始 SQLException（如果有）。
	 * <p>返回的Connection句柄实现了ConnectionProxy接口，允许检索底层目标Connection。
	 * @return 事务连接（如果有），否则是一个新连接
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
	 * 使用代理包装给定的 Connection，该代理将每个方法调用委托给它，但将 {@code close()} 调用委托给 DataSourceUtils。
	 * @param targetDataSource 连接来自的数据源
	 * @return 包裹连接
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
	 * 确定是为代理获取固定的目标Connection，还是为每次操作重新获取目标Connection。 <p> 对于所有标准情况，默认实现都会返回 {@code true}。这可以通
	 * 过 {@link #setReobtainTransactionalConnections "reobtainTransactionalConnections"} 标志覆盖，该
	 * 标志在活动事务中强制使用非固定目标连接。请注意，非事务访问将始终使用固定连接。
	 * @param targetDataSource 目标数据源
	 */
	protected boolean shouldObtainFixedConnection(DataSource targetDataSource) {
		return (!TransactionSynchronizationManager.isSynchronizationActive() ||
				!this.reobtainTransactionalConnections);
	}


	/**
	 * 将 JDBC 连接上的关闭调用委托给 DataSourceUtils 的调用处理程序，以了解线程绑定事务。
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
			// 对 ConnectionProxy 接口的调用即将到来...

			switch (method.getName()) {
				case "equals" -> {
					// 仅当代理相同时才被视为相等。
					return (proxy == args[0]);
				}
				case "hashCode" -> {
					// 使用连接代理的 hashCode。
					return System.identityHashCode(proxy);
				}
				case "toString" -> {
					// 允许区分代理和原始连接。
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
					// 处理关闭方法：仅在不在事务内时关闭。
					if (this.target != null) {
						ConnectionHolder conHolder = (ConnectionHolder)
								TransactionSynchronizationManager.getResource(this.targetDataSource);
						if (conHolder != null && conHolder.hasConnection() && conHolder.getConnection() == this.target) {
							// 这是事务连接：不要关闭它。
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
					// 避免在关闭前清理时创建目标连接（例如，Hibernate Session）
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
				// Handle getTargetConnection方法：返回底层Connection。
				return actualTarget;
			}

			// 调用目标连接上的方法。
			try {
				Object retVal = method.invoke(actualTarget, args);

				// 如果返回值是Statement，则应用事务超时。
				// 适用于createStatement、prepareStatement、prepareCall。
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
