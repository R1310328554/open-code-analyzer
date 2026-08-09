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
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

/**
 * {@link SmartDataSource} 的实现，包装单个 JDBC 连接，使用后不会关闭。显然，这不具备多线程能力。
 * <p>注意，在关闭时，应该有人通过 {@code close()} 方法关闭底层连接。如果客户端代码支持 SmartDataSource（例如，使用 {@code
 * DataSourceUtils.releaseConnection}），则它永远不会对连接句柄调用 close。
 * <p> 如果客户端代码将在假设池化连接的情况下调用 {@code close()}，就像使用持久性工具时一样，请将“suppressClose”设置为“true”。这将返回一个
 * 关闭抑制代理而不是物理连接。
 * <p>这主要用于测试。例如，它可以在应用程序服务器外部轻松测试希望在数据源上运行的代码。与 {@link DriverManagerDataSource} 相比，它始终重用相同
 * 的连接，避免过多创建物理连接。
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see #getConnection()
 * @see java.sql.Connection#close()
 * @see DataSourceUtils#releaseConnection
 */
public class SingleConnectionDataSource extends DriverManagerDataSource
		implements SmartDataSource, AutoCloseable, DisposableBean {

	/**
	 */
	private boolean suppressClose;

	/**
	 */
	private boolean rollbackBeforeClose;

	/**
	 */
	private @Nullable Boolean autoCommit;

	/**
	 */
	private @Nullable Connection target;

	/**
	 */
	private @Nullable Connection connection;

	/**
	 */
	private final Lock connectionLock = new ReentrantLock();


	/**
	 * bean 样式配置的构造函数。
	 */
	public SingleConnectionDataSource() {
	}

	/**
	 * 使用给定的标准 DriverManager 参数创建一个新的 SingleConnectionDataSource。
	 * @param url 用于访问 DriverManager 的 JDBC URL
	 * @param username 用于访问 DriverManager 的 JDBC 用户名
	 * @param password 用于访问 DriverManager 的 JDBC 密码
	 * @param suppressClose 返回的连接是否应该是关闭抑制代理或物理连接
	 * @see java.sql.DriverManager#getConnection(String, String, String)
	 */
	public SingleConnectionDataSource(String url, String username, String password, boolean suppressClose) {
		super(url, username, password);
		this.suppressClose = suppressClose;
	}

	/**
	 * 使用给定的标准 DriverManager 参数创建一个新的 SingleConnectionDataSource。
	 * @param url 用于访问 DriverManager 的 JDBC URL
	 * @param suppressClose 返回的连接是否应该是关闭抑制代理或物理连接
	 * @see java.sql.DriverManager#getConnection(String, String, String)
	 */
	public SingleConnectionDataSource(String url, boolean suppressClose) {
		super(url);
		this.suppressClose = suppressClose;
	}

	/**
	 * 使用给定的连接创建一个新的 SingleConnectionDataSource。
	 * @param target 底层目标连接
	 * @param suppressClose 是否应使用抑制 {@code close()} 调用的连接来包装连接（以允许在需要池连接但不知道我们的 SmartDataSource 接口的应用程序中正常使用 {@code close()}）
	 */
	public SingleConnectionDataSource(Connection target, boolean suppressClose) {
		Assert.notNull(target, "Connection must not be null");
		this.target = target;
		this.suppressClose = suppressClose;
		this.connection = (suppressClose ? getCloseSuppressingConnectionProxy(target) : target);
	}


	/**
	 * 指定返回的连接应该是关闭抑制代理还是物理连接。
	 */
	public void setSuppressClose(boolean suppressClose) {
		this.suppressClose = suppressClose;
	}

	/**
	 * 返回返回的 Connection 是关闭抑制代理还是物理 Connection。
	 */
	protected boolean isSuppressClose() {
		return this.suppressClose;
	}

	/**
	 * 指定共享连接是否应在关闭前显式回滚（如果不在自动提交模式下）。 <p> 建议在测试场景中用于 Oracle JDBC 驱动程序。
	 * @since 6.1.2
	 */
	public void setRollbackBeforeClose(boolean rollbackBeforeClose) {
		this.rollbackBeforeClose = rollbackBeforeClose;
	}

	/**
	 * 返回共享连接是否应在关闭之前显式回滚（如果不在自动提交模式下）。
	 * @since 6.1.2
	 */
	protected boolean isRollbackBeforeClose() {
		return this.rollbackBeforeClose;
	}

	/**
	 * 指定是否应覆盖返回的连接的“autoCommit”设置。
	 */
	public void setAutoCommit(boolean autoCommit) {
		this.autoCommit = autoCommit;
	}

	/**
	 * 返回是否应覆盖返回的连接的“autoCommit”设置。
	 * @return “autoCommit”值，或 {@code null}（如果没有应用）
	 */
	protected @Nullable Boolean getAutoCommitValue() {
		return this.autoCommit;
	}


	/**
	 * 获取 Connection（`Connection`）。
	 */
	@Override
	@SuppressWarnings("NullAway") // Dataflow analysis limitation
	public Connection getConnection() throws SQLException {
		this.connectionLock.lock();
		try {
			if (this.connection == null) {
				// 没有底层连接 -> 通过 DriverManager 进行延迟初始化。
				initConnection();
			}
			if (this.connection.isClosed()) {
				throw new SQLException(
						"Connection was closed in SingleConnectionDataSource. Check that user code checks " +
						"shouldClose() before closing Connections, or set 'suppressClose' to 'true'");
			}
			return this.connection;
		}
		finally {
			this.connectionLock.unlock();
		}
	}

	/**
	 * 指定自定义用户名和密码对于单个连接没有意义。如果给定相同的用户名和密码，则返回单个连接；否则抛出 SQLException。
	 */
	@Override
	public Connection getConnection(String username, String password) throws SQLException {
		if (ObjectUtils.nullSafeEquals(username, getUsername()) &&
				ObjectUtils.nullSafeEquals(password, getPassword())) {
			return getConnection();
		}
		else {
			throw new SQLException("SingleConnectionDataSource does not support custom username and password");
		}
	}

	/**
	 * 这是单个连接：返回“池”时不要关闭它。
	 */
	@Override
	public boolean shouldClose(Connection con) {
		this.connectionLock.lock();
		try {
			return (con != this.connection && con != this.target);
		}
		finally {
			this.connectionLock.unlock();
		}
	}

	/**
	 * 关闭底层连接。此数据源的提供者需要注意正确关闭。 <p>A 由于此类实现了 {@link AutoCloseable}，因此它可以与 try-with-resource 语句一
	 * 起使用。
	 * @since 6.1.2
	 */
	@Override
	public void close() {
		destroy();
	}

	/**
	 * 关闭底层连接。此数据源的提供者需要注意正确关闭。 <p>A 由于该 bean 实现了 {@link DisposableBean}，因此 bean 工厂将在销毁该 bean 时
	 * 自动调用它。
	 */
	@Override
	public void destroy() {
		this.connectionLock.lock();
		try {
			if (this.target != null) {
				closeConnection(this.target);
			}
		}
		finally {
			this.connectionLock.unlock();
		}
	}


	/**
	 * 通过DriverManager初始化底层Connection。
	 */
	public void initConnection() throws SQLException {
		if (getUrl() == null) {
			throw new IllegalStateException("'url' property is required for lazily initializing a Connection");
		}
		this.connectionLock.lock();
		try {
			if (this.target != null) {
				closeConnection(this.target);
			}
			this.target = getConnectionFromDriver(getUsername(), getPassword());
			prepareConnection(this.target);
			if (logger.isDebugEnabled()) {
				logger.debug("Established shared JDBC Connection: " + this.target);
			}
			this.connection = (isSuppressClose() ? getCloseSuppressingConnectionProxy(this.target) : this.target);
		}
		finally {
			this.connectionLock.unlock();
		}
	}

	/**
	 * 重置底层共享连接，以便在下次访问时重新初始化。
	 */
	public void resetConnection() {
		this.connectionLock.lock();
		try {
			if (this.target != null) {
				closeConnection(this.target);
			}
			this.target = null;
			this.connection = null;
		}
		finally {
			this.connectionLock.unlock();
		}
	}

	/**
	 * 在暴露之前准备好给定的连接。 <p> 如有必要，默认实现会应用自动提交标志。可以在子类中重写。
	 * @param con 连接准备
	 * @see #setAutoCommit
	 */
	protected void prepareConnection(Connection con) throws SQLException {
		Boolean autoCommit = getAutoCommitValue();
		if (autoCommit != null && con.getAutoCommit() != autoCommit) {
			con.setAutoCommit(autoCommit);
		}
	}

	/**
	 * 关闭底层共享连接。
	 * @since 6.1.2
	 */
	protected void closeConnection(Connection con) {
		if (isRollbackBeforeClose()) {
			try {
				if (!con.getAutoCommit()) {
					con.rollback();
				}
			}
			catch (Throwable ex) {
				logger.info("Could not roll back shared JDBC Connection before close", ex);
			}
		}
		try {
			con.close();
		}
		catch (Throwable ex) {
			logger.info("Could not close shared JDBC Connection", ex);
		}
	}

	/**
	 * 使用代理包装给定的 Connection，该代理将每个方法调用委托给它，但抑制关闭调用。
	 * @param target 要包装的原始连接
	 * @return 包裹连接
	 */
	protected Connection getCloseSuppressingConnectionProxy(Connection target) {
		return (Connection) Proxy.newProxyInstance(
				ConnectionProxy.class.getClassLoader(),
				new Class<?>[] {ConnectionProxy.class},
				new CloseSuppressingInvocationHandler(target));
	}


	/**
	 * 抑制 JDBC 连接上的关闭调用的调用处理程序。
	 */
	private static class CloseSuppressingInvocationHandler implements InvocationHandler {

		private final Connection target;

		public CloseSuppressingInvocationHandler(Connection target) {
			this.target = target;
		}

		@Override
		public @Nullable Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			// 对 ConnectionProxy 接口的调用即将到来...

			return switch (method.getName()) {
				// 仅当代理相同时才考虑相等。
				case "equals" -> (proxy == args[0]);
				// 使用连接代理的 hashCode。
				case "hashCode" -> System.identityHashCode(proxy);
				// 处理 close 方法：不传递调用。
				case "close" -> null;
				case "isClosed" -> this.target.isClosed();
				// Handle getTargetConnection方法：返回底层Connection。
				case "getTargetConnection" -> this.target;
				case "unwrap" -> (((Class<?>) args[0]).isInstance(proxy) ? proxy : this.target.unwrap((Class<?>) args[0]));
				case "isWrapperFor" -> (((Class<?>) args[0]).isInstance(proxy) || this.target.isWrapperFor((Class<?>) args[0]));
				default -> {
					try {
						// 调用目标连接上的方法。
						yield method.invoke(this.target, args);
					}
					catch (InvocationTargetException ex) {
						throw ex.getTargetException();
					}
				}
			};
		}
	}

}
