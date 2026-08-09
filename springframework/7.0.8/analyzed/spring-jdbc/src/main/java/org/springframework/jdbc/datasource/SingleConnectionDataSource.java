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
 * {@link SmartDataSource} 的实现，包装单个 JDBC 连接，
 * 使用后不关闭。显然，它不支持多线程。
 *
 * <p>注意关闭时应有人通过 {@code close()} 方法关闭底层连接。
 * 若客户端代码感知 SmartDataSource（例如使用 {@code DataSourceUtils.releaseConnection}），
 * 则不会对 Connection 句柄调用 close。
 *
 * <p>若客户端代码会像使用池化连接一样调用 {@code close()}（如使用持久化工具），
 * 请将 "suppressClose" 设为 "true"，
 * 此时将返回抑制 close 的代理而非物理 Connection。
 *
 * <p>主要用于测试。例如可在应用服务器外轻松测试期望 DataSource 的代码。
 * 与 {@link DriverManagerDataSource} 不同，它始终复用同一连接，
 * 避免频繁创建物理连接。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see #getConnection()
 * @see java.sql.Connection#close()
 * @see DataSourceUtils#releaseConnection
 */
public class SingleConnectionDataSource extends DriverManagerDataSource
		implements SmartDataSource, AutoCloseable, DisposableBean {

	/** 是否创建抑制 close 的代理？ */
	private boolean suppressClose;

	/** 关闭前是否显式回滚？ */
	private boolean rollbackBeforeClose;

	/** 是否覆盖自动提交状态？ */
	private @Nullable Boolean autoCommit;

	/** 被包装的 Connection。 */
	private @Nullable Connection target;

	/** 代理 Connection。 */
	private @Nullable Connection connection;

	/** 共享 Connection 的生命周期锁。 */
	private final Lock connectionLock = new ReentrantLock();


	/**
	 * 用于 Bean 风格配置的构造器。
	 */
	public SingleConnectionDataSource() {
	}

	/**
	 * 使用给定标准 DriverManager 参数创建 SingleConnectionDataSource。
	 * @param url 访问 DriverManager 的 JDBC URL
	 * @param username 访问 DriverManager 的 JDBC 用户名
	 * @param password 访问 DriverManager 的 JDBC 密码
	 * @param suppressClose 返回的 Connection 是抑制 close 的代理还是物理 Connection
	 * @see java.sql.DriverManager#getConnection(String, String, String)
	 */
	public SingleConnectionDataSource(String url, String username, String password, boolean suppressClose) {
		super(url, username, password);
		this.suppressClose = suppressClose;
	}

	/**
	 * 使用给定标准 DriverManager 参数创建 SingleConnectionDataSource。
	 * @param url 访问 DriverManager 的 JDBC URL
	 * @param suppressClose 返回的 Connection 是抑制 close 的代理还是物理 Connection
	 * @see java.sql.DriverManager#getConnection(String, String, String)
	 */
	public SingleConnectionDataSource(String url, boolean suppressClose) {
		super(url);
		this.suppressClose = suppressClose;
	}

	/**
	 * 使用给定 Connection 创建 SingleConnectionDataSource。
	 * @param target 底层目标 Connection
	 * @param suppressClose 是否用抑制 {@code close()} 调用的 Connection 包装
	 * （以便期望池化连接但不了解 SmartDataSource 接口的应用正常调用 {@code close()}）
	 */
	public SingleConnectionDataSource(Connection target, boolean suppressClose) {
		Assert.notNull(target, "Connection must not be null");
		this.target = target;
		this.suppressClose = suppressClose;
		this.connection = (suppressClose ? getCloseSuppressingConnectionProxy(target) : target);
	}


	/**
	 * 指定返回的 Connection 是抑制 close 的代理还是物理 Connection。
	 */
	public void setSuppressClose(boolean suppressClose) {
		this.suppressClose = suppressClose;
	}

	/**
	 * 返回返回的 Connection 是抑制 close 的代理还是物理 Connection。
	 */
	protected boolean isSuppressClose() {
		return this.suppressClose;
	}

	/**
	 * 指定关闭前是否显式回滚共享 Connection（非自动提交模式下）。
	 * <p>测试场景下建议对 Oracle JDBC 驱动启用此选项。
	 * @since 6.1.2
	 */
	public void setRollbackBeforeClose(boolean rollbackBeforeClose) {
		this.rollbackBeforeClose = rollbackBeforeClose;
	}

	/**
	 * 返回关闭前是否显式回滚共享 Connection（非自动提交模式下）。
	 * @since 6.1.2
	 */
	protected boolean isRollbackBeforeClose() {
		return this.rollbackBeforeClose;
	}

	/**
	 * 指定是否覆盖返回 Connection 的 "autoCommit" 设置。
	 */
	public void setAutoCommit(boolean autoCommit) {
		this.autoCommit = autoCommit;
	}

	/**
	 * 返回是否覆盖返回 Connection 的 "autoCommit" 设置。
	 * @return "autoCommit" 值，无需应用时为 {@code null}
	 */
	protected @Nullable Boolean getAutoCommitValue() {
		return this.autoCommit;
	}


	@Override
	@SuppressWarnings("NullAway") // Dataflow analysis limitation
	public Connection getConnection() throws SQLException {
		this.connectionLock.lock();
		try {
			if (this.connection == null) {
				// 无底层 Connection -> 通过 DriverManager 懒初始化。
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
	 * 单连接模式下指定自定义用户名和密码无意义。
	 * 用户名和密码相同时返回该连接，否则抛出 SQLException。
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
	 * 这是单连接：归还到"池"时不关闭。
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
	 * 关闭底层 Connection。
	 * 此 DataSource 的提供者需负责正确关闭。
	 * <p>本类实现 {@link AutoCloseable}，可用于 try-with-resources。
	 * @since 6.1.2
	 */
	@Override
	public void close() {
		destroy();
	}

	/**
	 * 关闭底层 Connection。
	 * 此 DataSource 的提供者需负责正确关闭。
	 * <p>本 Bean 实现 {@link DisposableBean}，Bean 工厂销毁时会自动调用。
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
	 * 通过 DriverManager 初始化底层 Connection。
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
	 * 重置底层共享 Connection，下次访问时重新初始化。
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
	 * 在暴露前准备给定 Connection。
	 * <p>默认实现按需应用 auto-commit 标志。
	 * 子类可覆盖。
	 * @param con 待准备的 Connection
	 * @see #setAutoCommit
	 */
	protected void prepareConnection(Connection con) throws SQLException {
		Boolean autoCommit = getAutoCommitValue();
		if (autoCommit != null && con.getAutoCommit() != autoCommit) {
			con.setAutoCommit(autoCommit);
		}
	}

	/**
	 * 关闭底层共享 Connection。
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
	 * 用代理包装给定 Connection，将所有方法调用委托给它但抑制 close 调用。
	 * @param target 待包装的原始 Connection
	 * @return 包装后的 Connection
	 */
	protected Connection getCloseSuppressingConnectionProxy(Connection target) {
		return (Connection) Proxy.newProxyInstance(
				ConnectionProxy.class.getClassLoader(),
				new Class<?>[] {ConnectionProxy.class},
				new CloseSuppressingInvocationHandler(target));
	}


	/**
	 * 抑制 JDBC Connection 上 close 调用的调用处理器。
	 */
	private static class CloseSuppressingInvocationHandler implements InvocationHandler {

		private final Connection target;

		public CloseSuppressingInvocationHandler(Connection target) {
			this.target = target;
		}

		@Override
		public @Nullable Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			// 来自 ConnectionProxy 接口的调用...

			return switch (method.getName()) {
				// 仅当代理相同时视为相等。
				case "equals" -> (proxy == args[0]);
				// 使用 Connection 代理的 hashCode。
				case "hashCode" -> System.identityHashCode(proxy);
				// 处理 close 方法：不继续传递调用。
				case "close" -> null;
				case "isClosed" -> this.target.isClosed();
				// 处理 getTargetConnection 方法：返回底层 Connection。
				case "getTargetConnection" -> this.target;
				case "unwrap" -> (((Class<?>) args[0]).isInstance(proxy) ? proxy : this.target.unwrap((Class<?>) args[0]));
				case "isWrapperFor" -> (((Class<?>) args[0]).isInstance(proxy) || this.target.isWrapperFor((Class<?>) args[0]));
				default -> {
					try {
						// 在目标 Connection 上调用方法。
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
