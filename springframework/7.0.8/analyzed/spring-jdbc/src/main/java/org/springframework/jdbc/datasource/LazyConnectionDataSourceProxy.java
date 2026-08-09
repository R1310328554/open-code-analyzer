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
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;

import org.springframework.util.Assert;

/**
 * 目标 {@link DataSource} 的代理，延迟获取实际 JDBC Connection，即直到首次创建 Statement 时才获取。
 * 连接初始化属性（自动提交模式、事务隔离、只读模式）将被保留，并在获取实际 Connection 后立即应用。
 * 因此，若未创建任何 Statement，commit 与 rollback 调用将被忽略。
 * 自 6.1.2 起，除常规目标 DataSource 外，还支持在只读事务期间使用 {@link #setReadOnlyDataSource read-only DataSource}。
 *
 * <p>本代理可避免在确实不需要时从连接池获取 JDBC Connection：JDBC 事务控制可在不取连接、不与数据库通信的情况下进行，
 * 首次创建 JDBC Statement 时才真正完成。此外，可在路由 DataSource（如
 * {@link org.springframework.jdbc.datasource.lookup.IsolationLevelDataSourceRouter}）中
 * 考虑事务同步的只读标志和/或隔离级别。
 *
 * <p><b>若同时配置 LazyConnectionDataSourceProxy 与 TransactionAwareDataSourceProxy，
 * 请确保后者为最外层 DataSource。</b> 此时数据访问代码与事务感知 DataSource 交互，
 * 后者再与 LazyConnectionDataSourceProxy 协作。自 6.1.2 起，LazyConnectionDataSourceProxy 在首次 Connection 访问时
 * 初始化默认连接特性；若要在启动时强制执行，请调用 {@link #checkDefaultConnectionProperties()}。
 *
 * <p>在通用事务划分环境中，延迟获取物理 JDBC Connection 尤其有益：可在所有可能执行数据访问的方法上划分事务，
 * 而实际未发生数据访问时不会产生性能损失。
 *
 * <p>本代理提供类似 JTA 与 Jakarta EE 服务器所提供的事务性 JNDI DataSource 的行为，
 * 即使使用 DataSourceTransactionManager 或 HibernateTransactionManager 等本地事务策略也适用。
 * 若使用 Spring 的 JtaTransactionManager 作为事务策略则收益有限。
 *
 * <p>对 Hibernate 只读操作也建议延迟获取 JDBC Connection，尤其当二级缓存命中概率较高时，
 * 此类只读操作完全无需与数据库通信。非事务性读取效果相同，但延迟获取仍允许在事务中执行读取。
 *
 * <p>自 6.2.6 起，若超时期间 Connection 已被关闭，本代理还会抑制 rollback 尝试。
 *
 * <p><b>注意：</b>本代理须返回包装 Connection（实现 {@link ConnectionProxy}），以支持延迟获取实际 JDBC Connection。
 * 使用 {@link Connection#unwrap} 获取原生 JDBC Connection。
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 1.1.4
 * @see DataSourceTransactionManager
 * @see #setTargetDataSource
 * @see #setReadOnlyDataSource
 */
public class LazyConnectionDataSourceProxy extends DelegatingDataSource {

	/**
	 * {@link java.sql.Connection} 中定义的隔离常量的常量名称到常量值的映射。
	 */
	static final Map<String, Integer> constants = Map.of(
			"TRANSACTION_READ_UNCOMMITTED", Connection.TRANSACTION_READ_UNCOMMITTED,
			"TRANSACTION_READ_COMMITTED", Connection.TRANSACTION_READ_COMMITTED,
			"TRANSACTION_REPEATABLE_READ", Connection.TRANSACTION_REPEATABLE_READ,
			"TRANSACTION_SERIALIZABLE", Connection.TRANSACTION_SERIALIZABLE
		);

	/** 日志记录器。 */
	private static final Log logger = LogFactory.getLog(LazyConnectionDataSourceProxy.class);

	/** 只读事务期间使用的目标 DataSource。 */
	private @Nullable DataSource readOnlyDataSource;

	/** 默认自动提交模式（尚未获取目标 Connection 时使用）。 */
	private volatile @Nullable Boolean defaultAutoCommit;

	/** 默认事务隔离级别（尚未获取目标 Connection 时使用）。 */
	private volatile @Nullable Integer defaultTransactionIsolation;


	/**
	 * 创建一个新的 LazyConnectionDataSourceProxy。
	 * @see #setTargetDataSource
	 * @see #setReadOnlyDataSource
	 */
	public LazyConnectionDataSourceProxy() {
	}

	/**
	 * 创建一个新的 LazyConnectionDataSourceProxy。
	 * @param targetDataSource 目标数据源
	 * @see #setTargetDataSource
	 */
	public LazyConnectionDataSourceProxy(DataSource targetDataSource) {
		setTargetDataSource(targetDataSource);
		afterPropertiesSet();
	}


	/**
	 * 指定用于只读事务的目标数据源的变体。 <p>如果可用，来自此类只读数据源的连接将在已标记为只读的 Spring 管理事务中延迟获取。 {@link Connection#set
	 * ReadOnly} 标志将保持不变，期望将其预先配置为只读数据源上的默认值，从而避免在每个事务开始和结束时切换它的开销。此外，默认的自动提交和隔离级别设置应与主要目标数据源的默
	 * 认连接属性相匹配。
	 * @since 6.1.2
	 * @see #setTargetDataSource
	 * @see #setDefaultAutoCommit
	 * @see #setDefaultTransactionIsolation
	 * @see org.springframework.transaction.TransactionDefinition#isReadOnly()
	 */
	public void setReadOnlyDataSource(@Nullable DataSource readOnlyDataSource) {
		this.readOnlyDataSource = readOnlyDataSource;
		if (getTargetDataSource() == null) {
			setTargetDataSource(readOnlyDataSource);
		}
	}

	/**
	 * 设置默认的自动提交模式，以在尚未获取目标连接时公开（当尚不知道实际的 JDBC 连接默认值时）。 <p>如果未指定，则默认值是通过在第一次访问连接时进行延迟检查来确定的。
	 * @see java.sql.Connection#setAutoCommit
	 */
	public void setDefaultAutoCommit(boolean defaultAutoCommit) {
		this.defaultAutoCommit = defaultAutoCommit;
	}

	/**
	 * 通过{@link java.sql.Connection}中对应常量的名称设置默认的事务隔离级别——例如，{@code
	 * "TRANSACTION_SERIALIZABLE"}。
	 * @param constantName 常量名称
	 * @see #setDefaultTransactionIsolation
	 * @see java.sql.Connection#TRANSACTION_READ_UNCOMMITTED
	 * @see java.sql.Connection#TRANSACTION_READ_COMMITTED
	 * @see java.sql.Connection#TRANSACTION_REPEATABLE_READ
	 * @see java.sql.Connection#TRANSACTION_SERIALIZABLE
	 */
	public void setDefaultTransactionIsolationName(String constantName) {
		Assert.hasText(constantName, "'constantName' must not be null or blank");
		Integer defaultTransactionIsolation = constants.get(constantName);
		Assert.notNull(defaultTransactionIsolation, "Only transaction isolation constants allowed");
		this.defaultTransactionIsolation = defaultTransactionIsolation;
	}

	/**
	 * 设置默认事务隔离级别，以在尚未获取目标连接时公开（当尚不知道实际的 JDBC 连接默认值时）。 <p> 该属性接受 {@link java.sql.Connection} 接口
	 * 中定义的 int 常量值（例如 8）；它主要用于编程用途。考虑使用“defaultTransactionIsolationName”属性按名称设置值（例如 {@code "TR
	 * ANSACTION_SERIALIZABLE"}）。 <p>如果未指定，则通过在第一次访问连接时延迟检查来确定默认值。
	 * @see #setDefaultTransactionIsolationName
	 * @see java.sql.Connection#setTransactionIsolation
	 */
	public void setDefaultTransactionIsolation(int defaultTransactionIsolation) {
		Assert.isTrue(constants.containsValue(defaultTransactionIsolation),
				"Only values of transaction isolation constants allowed");
		this.defaultTransactionIsolation = defaultTransactionIsolation;
	}


	/**
	 * 如果可能，通过目标数据源的连接确定默认自动提交和事务隔离。
	 * @since 6.1.2
	 * @see #checkDefaultConnectionProperties(Connection)
	 */
	public void checkDefaultConnectionProperties() {
		if (this.defaultAutoCommit == null || this.defaultTransactionIsolation == null) {
			try {
				try (Connection con = obtainTargetDataSource().getConnection()) {
					checkDefaultConnectionProperties(con);
				}
			}
			catch (SQLException ex) {
				logger.debug("Could not retrieve default auto-commit and transaction isolation settings", ex);
			}
		}
	}

	/**
	 * 检查默认连接属性（自动提交、事务隔离），使它们能够正确公开它们，而无需稍后从目标数据源获取实际的 JDBC 连接。
	 * @param con 用于检查的连接
	 * @throws SQLException 如果由 Connection 方法抛出
	 */
	protected void checkDefaultConnectionProperties(Connection con) throws SQLException {
		if (this.defaultAutoCommit == null) {
			this.defaultAutoCommit = con.getAutoCommit();
		}
		if (this.defaultTransactionIsolation == null) {
			this.defaultTransactionIsolation = con.getTransactionIsolation();
		}
	}

	/**
	 * 公开默认的自动提交值。
	 */
	protected @Nullable Boolean defaultAutoCommit() {
		return this.defaultAutoCommit;
	}

	/**
	 * 公开默认的事务隔离值。
	 */
	protected @Nullable Integer defaultTransactionIsolation() {
		return this.defaultTransactionIsolation;
	}


	/**
	 * 返回一个连接句柄，当请求语句（或PreparedStatement或CallableStatement）时，该句柄会延迟获取实际的JDBC连接。 <p>返回的Connectio
	 * n句柄实现了ConnectionProxy接口，允许检索底层目标Connection。
	 * @return 惰性连接句柄
	 * @see ConnectionProxy#getTargetConnection()
	 */
	@Override
	public Connection getConnection() throws SQLException {
		checkDefaultConnectionProperties();
		return (Connection) Proxy.newProxyInstance(
				ConnectionProxy.class.getClassLoader(),
				new Class<?>[] {ConnectionProxy.class},
				new LazyConnectionInvocationHandler());
	}

	/**
	 * 返回一个连接句柄，当请求语句（或PreparedStatement或CallableStatement）时，该句柄会延迟获取实际的JDBC连接。 <p>返回的Connectio
	 * n句柄实现了ConnectionProxy接口，允许检索底层目标Connection。
	 * @param username 每个连接的用户名
	 * @param password 每个连接的密码
	 * @return 惰性连接句柄
	 * @see ConnectionProxy#getTargetConnection()
	 */
	@Override
	public Connection getConnection(String username, String password) throws SQLException {
		checkDefaultConnectionProperties();
		return (Connection) Proxy.newProxyInstance(
				ConnectionProxy.class.getClassLoader(),
				new Class<?>[] {ConnectionProxy.class},
				new LazyConnectionInvocationHandler(username, password));
	}


	/**
	 * 延迟获取实际 JDBC 连接直到首次创建语句的调用处理程序。
	 */
	private class LazyConnectionInvocationHandler implements InvocationHandler {

		private @Nullable String username;

		private @Nullable String password;

		private @Nullable String catalog;

		private @Nullable String schema;

		private @Nullable Integer holdability;

		private boolean readOnly = false;

		private @Nullable Integer transactionIsolation;

		private @Nullable Boolean autoCommit;

		private boolean closed = false;

		private @Nullable Connection target;

		public LazyConnectionInvocationHandler() {
			this.autoCommit = defaultAutoCommit();
			this.transactionIsolation = defaultTransactionIsolation();
		}

		public LazyConnectionInvocationHandler(String username, String password) {
			this();
			this.username = username;
			this.password = password;
		}

		@Override
		public @Nullable Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			// 对 ConnectionProxy 接口的调用即将到来...

			switch (method.getName()) {
				case "equals" -> {
					// 我们必须避免获取“等于”的目标连接。
					// 仅当代理相同时才考虑相等。
					return (proxy == args[0]);
				}
				case "hashCode" -> {
					// 我们必须避免获取“hashCode”的目标连接，
					// 即使目标是相同的，我们也必须返回相同的哈希码
					// 连接已获取：使用连接代理的 hashCode。
					return System.identityHashCode(proxy);
				}
				case "getTargetConnection" -> {
					// 处理getTargetConnection方法：返回底层连接。
					return getTargetConnection(method);
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

			if (!hasTargetConnection()) {
				// 尚未保持物理目标连接 ->
				// 解决事务划分方法而不需要获取
				// 物理 JDBC 连接，直到绝对必要为止。

				switch (method.getName()) {
					case "toString" -> {
						return "Lazy Connection proxy for target DataSource [" + getTargetDataSource() + "]";
					}
					case "getCatalog" -> {
						if (this.catalog != null) {
							return this.catalog;
						}
						// 否则获取实际连接并检查那里。
					}
					case "setCatalog" -> {
						this.catalog = (String) args[0];
						return null;
					}
					case "getSchema" -> {
						if (this.schema != null) {
							return this.schema;
						}
						// 否则获取实际连接并检查那里。
					}
					case "setSchema" -> {
						this.schema = (String) args[0];
						return null;
					}
					case "getHoldability" -> {
						if (this.holdability != null) {
							return this.holdability;
						}
						// 否则获取实际连接并检查那里。
					}
					case "setHoldability" -> {
						this.holdability = (Integer) args[0];
						return null;
					}
					case "isReadOnly" -> {
						return (this.readOnly || getTargetDataSource() == readOnlyDataSource);
					}
					case "setReadOnly" -> {
						this.readOnly = (Boolean) args[0];
						return null;
					}
					case "getTransactionIsolation" -> {
						if (this.transactionIsolation != null) {
							return this.transactionIsolation;
						}
						// 否则获取实际连接并检查那里，
						// 因为我们没有指定默认值。
					}
					case "setTransactionIsolation" -> {
						this.transactionIsolation = (Integer) args[0];
						return null;
					}
					case "getAutoCommit" -> {
						if (this.autoCommit != null) {
							return this.autoCommit;
						}
						// 否则获取实际连接并检查那里，
						// 因为我们没有指定默认值。
					}
					case "setAutoCommit" -> {
						this.autoCommit = (Boolean) args[0];
						return null;
					}
					case "commit", "rollback" -> {
						// 忽略：尚未创建任何语句。
						return null;
					}
					case "getWarnings", "clearWarnings" -> {
						// 忽略：尚无要暴露的警告。
						return null;
					}
					case "close" -> {
						// 忽略：还没有目标连接。
						this.closed = true;
						return null;
					}
					case "isClosed" -> {
						return this.closed;
					}
					default -> {
						if (this.closed) {
							// 连接代理已关闭，尚未获取
							// 物理 JDBC 连接：抛出相应的 SQLException。
							throw new SQLException("Illegal operation: connection is closed");
						}
					}
				}
			}

			if (readOnlyDataSource != null && "setReadOnly".equals(method.getName())) {
				// 在专用只读数据源的情况下抑制 setReadOnly 重置调用
				return null;
			}


			// 已获取目标连接，或当前操作所需的目标连接
			// -> 在目标连接上调用方法。
			try {
				Connection conToUse = getTargetConnection(method);

				if ("rollback".equals(method.getName()) && conToUse.isClosed()) {
					// 连接同时关闭，可能是由于资源超时。自从一个
					// 回滚尝试通常发生在关闭之前，我们会宽容地抑制它。
					return null;
				}

				return method.invoke(conToUse, args);
			}
			catch (InvocationTargetException ex) {
				throw ex.getTargetException();
			}
		}

		/**
		 * 返回代理当前是否持有目标连接。
		 */
		private boolean hasTargetConnection() {
			return (this.target != null);
		}

		/**
		 * 返回目标连接，获取它并在必要时初始化它。
		 */
		private Connection getTargetConnection(Method operation) throws Throwable {
			Connection target = this.target;
			if (target != null) {
				// 目标连接已持有 -> 返回它。
				if (logger.isTraceEnabled()) {
					logger.trace("Using existing database connection for operation '" + operation.getName() + "'");
				}
				return target;
			}

			// 没有持有目标连接 -> 获取一个。
			if (logger.isTraceEnabled()) {
				logger.trace("Connecting to database for operation '" + operation.getName() + "'");
			}

			// 从数据源获取物理连接。
			DataSource dataSource = getDataSourceToUse();
			target = (this.username != null ? dataSource.getConnection(this.username, this.password) :
					dataSource.getConnection());
			if (target == null) {
				throw new IllegalStateException("DataSource returned null from getConnection(): " + dataSource);
			}

			// 应用保留的事务设置（如果有）。
			try {
				if (this.catalog != null) {
					target.setCatalog(this.catalog);
				}
				if (this.schema != null) {
					target.setSchema(this.schema);
				}
				if (this.holdability != null) {
					target.setHoldability(this.holdability);
				}
				if (this.readOnly && readOnlyDataSource == null) {
					DataSourceUtils.setReadOnlyIfPossible(target);
				}
				if (this.transactionIsolation != null &&
						!this.transactionIsolation.equals(defaultTransactionIsolation())) {
					target.setTransactionIsolation(this.transactionIsolation);
				}
				if (this.autoCommit != null && this.autoCommit != defaultAutoCommit()) {
					target.setAutoCommit(this.autoCommit);
				}
			}
			catch (Throwable settingsEx) {
				logger.debug("Failed to apply transaction settings to JDBC Connection", settingsEx);
				// 关闭连接并且不将其设置为目标。
				try {
					target.close();
				}
				catch (Throwable closeEx) {
					logger.debug("Could not close JDBC Connection after failed settings", closeEx);
				}
				throw settingsEx;
			}

			this.target = target;
			return target;
		}

		private DataSource getDataSourceToUse() {
			return (this.readOnly && readOnlyDataSource != null ? readOnlyDataSource : obtainTargetDataSource());
		}
	}

}
