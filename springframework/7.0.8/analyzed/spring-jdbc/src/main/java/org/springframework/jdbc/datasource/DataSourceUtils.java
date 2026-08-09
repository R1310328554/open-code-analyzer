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
import java.sql.Statement;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;

import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;

/**
 * 提供用于从 {@link javax.sql.DataSource} 获取 JDBC {@code Connection} 的静态方法的帮助程序类。包括对 Spring
 * 管理的事务性 {@code Connection} 的特殊支持，例如由 {@link DataSourceTransactionManager} 或 {@link
 * org.springframework.transaction.jta.JtaTransactionManager} 管理。
 * <p> 由 Spring 的 {@link org.springframework.jdbc.core.JdbcTemplate}、Spring 的 JDBC 操作对象和
 * JDBC {@link DataSourceTransactionManager} 在内部使用。也可以直接在应用程序代码中使用。
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see #getConnection
 * @see #releaseConnection
 * @see org.springframework.jdbc.core.JdbcTemplate
 * @see org.springframework.jdbc.support.JdbcTransactionManager
 * @see org.springframework.transaction.jta.JtaTransactionManager
 * @see org.springframework.transaction.support.TransactionSynchronizationManager
 */
public abstract class DataSourceUtils {

	/**
	 * 清理 JDBC 连接的 TransactionSynchronization 对象的顺序值。
	 */
	public static final int CONNECTION_SYNCHRONIZATION_ORDER = 1000;

	/**
	 * 获取 Log（`Log`）。
	 */
	private static final Log logger = LogFactory.getLog(DataSourceUtils.class);


	/**
	 * 从给定的数据源获取连接。将 SQLException 转换为未经检查的通用数据访问异常的 Spring 层次结构，从而简化了调用代码并使抛出的任何异常更有意义。 <p>I
	 * 知道绑定到当前线程的相应连接，例如在使用 {@link DataSourceTransactionManager} 时。如果事务同步处于活动状态（例如，在 {@link
	 * org.springframework.transaction.jta.JtaTransactionManager JTA} 事务中运行时），会将连接绑定到线程。
	 * @param dataSource 从中获取连接的数据源
	 * @return 来自给定数据源的 JDBC 连接
	 * @throws org.springframework.jdbc.CannotGetJdbcConnectionException 如果尝试获取连接失败
	 * @see #releaseConnection(Connection, DataSource)
	 * @see #isConnectionTransactional(Connection, DataSource)
	 */
	public static Connection getConnection(DataSource dataSource) throws CannotGetJdbcConnectionException {
		try {
			return doGetConnection(dataSource);
		}
		catch (SQLException ex) {
			throw new CannotGetJdbcConnectionException("Failed to obtain JDBC Connection", ex);
		}
		catch (IllegalStateException ex) {
			throw new CannotGetJdbcConnectionException("Failed to obtain JDBC Connection", ex);
		}
	}

	/**
	 * 实际上从给定的数据源获取 JDBC 连接。与 {@link #getConnection} 相同，但抛出原始 SQLException。 <p>I
	 * 知道绑定到当前线程的相应连接，例如在使用 {@link DataSourceTransactionManager} 时。如果事务同步处于活动状态（例如，如果在 JTA
	 * 事务中），则将连接绑定到线程。 <p>直接由{@link TransactionAwareDataSourceProxy}访问。
	 * @param dataSource 从中获取连接的数据源
	 * @return 来自给定数据源的 JDBC 连接
	 * @throws SQLException 如果由 JDBC 方法抛出
	 * @see #doReleaseConnection
	 */
	public static Connection doGetConnection(DataSource dataSource) throws SQLException {
		Assert.notNull(dataSource, "No DataSource specified");

		ConnectionHolder conHolder = (ConnectionHolder) TransactionSynchronizationManager.getResource(dataSource);
		if (conHolder != null && (conHolder.hasConnection() || conHolder.isSynchronizedWithTransaction())) {
			conHolder.requested();
			if (!conHolder.hasConnection()) {
				logger.debug("Fetching resumed JDBC Connection from DataSource");
				conHolder.setConnection(fetchConnection(dataSource));
			}
			return conHolder.getConnection();
		}
		// 否则，我们要么没有支架，要么有一个空的线装支架。

		logger.debug("Fetching JDBC Connection from DataSource");
		Connection con = fetchConnection(dataSource);

		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			try {
				// 使用相同的连接来执行事务中的进一步 JDBC 操作。
				// 线程绑定对象将在事务完成时通过同步删除。
				ConnectionHolder holderToUse = conHolder;
				if (holderToUse == null) {
					holderToUse = new ConnectionHolder(con);
				}
				else {
					holderToUse.setConnection(con);
				}
				holderToUse.requested();
				TransactionSynchronizationManager.registerSynchronization(
						new ConnectionSynchronization(holderToUse, dataSource));
				holderToUse.setSynchronizedWithTransaction(true);
				if (holderToUse != conHolder) {
					TransactionSynchronizationManager.bindResource(dataSource, holderToUse);
				}
			}
			catch (RuntimeException ex) {
				// 外部委托调用出现意外异常 -> 关闭连接并重新抛出。
				releaseConnection(con, dataSource);
				throw ex;
			}
		}

		return con;
	}

	/**
	 * 实际上从给定的 {@link DataSource} 中获取 {@link Connection}，防御性地将意外的 {@code null} 返回值从 {@link
	 * DataSource#getConnection()} 转换为 {@link IllegalStateException}。
	 * @param dataSource 从中获取连接的数据源
	 * @return 来自给定数据源的 JDBC 连接（绝不是 {@code null}）
	 * @throws SQLException 如果由 JDBC 方法抛出
	 * @throws IllegalStateException 如果数据源返回空值
	 * @see DataSource#getConnection()
	 */
	private static Connection fetchConnection(DataSource dataSource) throws SQLException {
		Connection con = dataSource.getConnection();
		if (con == null) {
			throw new IllegalStateException("DataSource returned null from getConnection(): " + dataSource);
		}
		return con;
	}

	/**
	 * 使用给定的事务语义准备给定的连接。
	 * @param con 连接准备
	 * @param definition 要应用的事务定义
	 * @return 之前的隔离级别（如果有）
	 * @throws SQLException 如果由 JDBC 方法抛出
	 * @see #prepareConnectionForTransaction(Connection, int, boolean)
	 */
	public static @Nullable Integer prepareConnectionForTransaction(Connection con, @Nullable TransactionDefinition definition)
			throws SQLException {

		return prepareConnectionForTransaction(con,
				(definition != null ? definition.getIsolationLevel() : TransactionDefinition.ISOLATION_DEFAULT),
				(definition != null && definition.isReadOnly()));
	}

	/**
	 * 使用给定的事务语义准备给定的连接。
	 * @param con 连接准备
	 * @param isolationLevel 要应用的隔离级别
	 * @param setReadOnly 是否设置只读标志
	 * @return 之前的隔离级别（如果有）
	 * @throws SQLException 如果由 JDBC 方法抛出
	 * @since 6.2.13
	 * @see #resetConnectionAfterTransaction(Connection, Integer, boolean)
	 * @see Connection#setTransactionIsolation
	 * @see Connection#setReadOnly
	 */
	static @Nullable Integer prepareConnectionForTransaction(Connection con, int isolationLevel, boolean setReadOnly)
			throws SQLException {

		Assert.notNull(con, "No Connection specified");

		boolean debugEnabled = logger.isDebugEnabled();
		// 设置只读标志。
		if (setReadOnly) {
			if (debugEnabled) {
				logger.debug("Setting JDBC Connection [" + con + "] read-only");
			}
			setReadOnlyIfPossible(con);
		}

		// 应用特定的隔离级别（如果有）。
		Integer previousIsolationLevel = null;
		if (isolationLevel != TransactionDefinition.ISOLATION_DEFAULT) {
			if (debugEnabled) {
				logger.debug("Changing isolation level of JDBC Connection [" + con + "] to " + isolationLevel);
			}
			int currentIsolation = con.getTransactionIsolation();
			if (currentIsolation != isolationLevel) {
				previousIsolationLevel = currentIsolation;
				con.setTransactionIsolation(isolationLevel);
			}
		}

		return previousIsolationLevel;
	}

	/**
	 * 将只读提示应用于给定的连接，抑制除超时相关异常之外的异常。
	 * @param con 连接准备
	 * @throws SQLException 如果出现超时异常
	 * @since 6.2.15
	 */
	static void setReadOnlyIfPossible(Connection con) throws SQLException {
		try {
			con.setReadOnly(true);
		}
		catch (SQLException | RuntimeException ex) {
			Throwable exToCheck = ex;
			while (exToCheck != null) {
				if (exToCheck.getClass().getSimpleName().contains("Timeout")) {
					// 假设这是一个连接超时，否则会丢失：例如，从 JDBC 4.0
					throw ex;
				}
				exToCheck = exToCheck.getCause();
			}
			// “只读不支持” SQLException -> 忽略，无论如何这只是一个提示
			logger.debug("Could not set JDBC Connection read-only", ex);
		}
	}

	/**
	 * 事务后重置给定连接，涉及只读标志和隔离级别。
	 * @param con 要重置的连接
	 * @param previousIsolationLevel 要恢复的隔离级别（如果有）
	 * @param resetReadOnly 是否重置连接的只读标志
	 * @since 5.2.1
	 * @see #prepareConnectionForTransaction
	 * @see Connection#setTransactionIsolation
	 * @see Connection#setReadOnly
	 */
	public static void resetConnectionAfterTransaction(
			Connection con, @Nullable Integer previousIsolationLevel, boolean resetReadOnly) {

		Assert.notNull(con, "No Connection specified");
		boolean debugEnabled = logger.isDebugEnabled();
		try {
			// 如果事务发生更改，则将事务隔离重置为之前的值。
			if (previousIsolationLevel != null) {
				if (debugEnabled) {
					logger.debug("Resetting isolation level of JDBC Connection [" +
							con + "] to " + previousIsolationLevel);
				}
				con.setTransactionIsolation(previousIsolationLevel);
			}

			// 如果我们最初在事务开始时将其切换为 true，则重置只读标志。
			if (resetReadOnly) {
				if (debugEnabled) {
					logger.debug("Resetting read-only flag of JDBC Connection [" + con + "]");
				}
				con.setReadOnly(false);
			}
		}
		catch (Throwable ex) {
			logger.debug("Could not reset JDBC Connection after transaction", ex);
		}
	}

	/**
	 * 事务后重置给定连接，涉及只读标志和隔离级别。
	 * @param con 要重置的连接
	 * @param previousIsolationLevel 要恢复的隔离级别（如果有）
	 * @deprecated {@link #resetConnectionAfterTransaction(Connection, Integer, boolean)} 的青睐
	 */
	@Deprecated(since = "5.1.11")
	public static void resetConnectionAfterTransaction(Connection con, @Nullable Integer previousIsolationLevel) {
		Assert.notNull(con, "No Connection specified");
		try {
			// 如果事务发生更改，则将事务隔离重置为之前的值。
			if (previousIsolationLevel != null) {
				if (logger.isDebugEnabled()) {
					logger.debug("Resetting isolation level of JDBC Connection [" +
							con + "] to " + previousIsolationLevel);
				}
				con.setTransactionIsolation(previousIsolationLevel);
			}

			// 重置只读标志。
			if (con.isReadOnly()) {
				if (logger.isDebugEnabled()) {
					logger.debug("Resetting read-only flag of JDBC Connection [" + con + "]");
				}
				con.setReadOnly(false);
			}
		}
		catch (Throwable ex) {
			logger.debug("Could not reset JDBC Connection after transaction", ex);
		}
	}

	/**
	 * 确定给定的 JDBC Connection 是否是事务性的，即通过 Spring 的事务设施绑定到当前线程。
	 * @param con 要检查的连接
	 * @param dataSource 从中获取连接的数据源（可能是 {@code null}）
	 * @return 连接是事务性的
	 * @see #getConnection(DataSource)
	 */
	public static boolean isConnectionTransactional(Connection con, @Nullable DataSource dataSource) {
		if (dataSource == null) {
			return false;
		}
		ConnectionHolder conHolder = (ConnectionHolder) TransactionSynchronizationManager.getResource(dataSource);
		return (conHolder != null && connectionEquals(conHolder, con));
	}

	/**
	 * 将当前事务超时（如果有）应用于给定的 JDBC Statement 对象。
	 * @param stmt JDBC 语句对象
	 * @param dataSource 从中获取连接的数据源
	 * @throws SQLException 如果由 JDBC 方法抛出
	 * @see java.sql.Statement#setQueryTimeout
	 */
	public static void applyTransactionTimeout(Statement stmt, @Nullable DataSource dataSource) throws SQLException {
		applyTimeout(stmt, dataSource, -1);
	}

	/**
	 * 将指定的超时（由当前事务超时覆盖（如果有）覆盖）应用于给定的 JDBC Statement 对象。
	 * @param stmt JDBC 语句对象
	 * @param dataSource 从中获取连接的数据源
	 * @param timeout 应用的超时（或 0 表示事务外没有超时）
	 * @throws SQLException 如果由 JDBC 方法抛出
	 * @see java.sql.Statement#setQueryTimeout
	 */
	public static void applyTimeout(Statement stmt, @Nullable DataSource dataSource, int timeout) throws SQLException {
		Assert.notNull(stmt, "No Statement specified");
		ConnectionHolder holder = null;
		if (dataSource != null) {
			holder = (ConnectionHolder) TransactionSynchronizationManager.getResource(dataSource);
		}
		if (holder != null && holder.hasTimeout()) {
			// 剩余事务超时覆盖指定值。
			stmt.setQueryTimeout(holder.getTimeToLiveInSeconds());
		}
		else if (timeout >= 0) {
			// 当前没有事务超时 -> 应用指定值。
			stmt.setQueryTimeout(timeout);
		}
	}

	/**
	 * 关闭从给定 DataSource 获取的给定 Connection，如果它不是外部管理的（即未绑定到线程）。
	 * @param con 必要时关闭的连接（如果这是 {@code null}，则调用将被忽略）
	 * @param dataSource 从中获取连接的数据源（可能是 {@code null}）
	 * @see #getConnection
	 */
	public static void releaseConnection(@Nullable Connection con, @Nullable DataSource dataSource) {
		try {
			doReleaseConnection(con, dataSource);
		}
		catch (SQLException ex) {
			logger.debug("Could not close JDBC Connection", ex);
		}
		catch (Throwable ex) {
			logger.debug("Unexpected exception on closing JDBC Connection", ex);
		}
	}

	/**
	 * 实际上关闭从给定数据源获取的给定连接。与 {@link #releaseConnection} 相同，但抛出原始 SQLException。 <p>由{@link Transa
	 * ctionAwareDataSourceProxy}直接访问。
	 * @param con 必要时关闭的连接（如果这是 {@code null}，则调用将被忽略）
	 * @param dataSource 从中获取连接的数据源（可能是 {@code null}）
	 * @throws SQLException 如果由 JDBC 方法抛出
	 * @see #doGetConnection
	 */
	public static void doReleaseConnection(@Nullable Connection con, @Nullable DataSource dataSource) throws SQLException {
		if (con == null) {
			return;
		}
		if (dataSource != null) {
			ConnectionHolder conHolder = (ConnectionHolder) TransactionSynchronizationManager.getResource(dataSource);
			if (conHolder != null && connectionEquals(conHolder, con)) {
				// 这是事务连接：不要关闭它。
				conHolder.released();
				return;
			}
		}
		doCloseConnection(con, dataSource);
	}

	/**
	 * 关闭连接，除非 {@link SmartDataSource} 不希望我们这样做。
	 * @param con 必要时关闭连接
	 * @param dataSource 从中获取连接的数据源
	 * @throws SQLException 如果由 JDBC 方法抛出
	 * @see Connection#close()
	 * @see SmartDataSource#shouldClose(Connection)
	 */
	public static void doCloseConnection(Connection con, @Nullable DataSource dataSource) throws SQLException {
		if (!(dataSource instanceof SmartDataSource smartDataSource) || smartDataSource.shouldClose(con)) {
			con.close();
		}
	}

	/**
	 * 确定给定的两个连接是否相等，在代理的情况下询问目标连接。用于检测相等性，即使用户传入原始目标连接而保留的连接是代理也是如此。
	 * @param conHolder 所持有的 Connection 的 ConnectionHolder （可能是一个代理）
	 * @param passedInCon 用户传入的连接（可能是没有代理的目标连接）
	 * @return 给定的连接是相等的
	 * @see #getTargetConnection
	 */
	private static boolean connectionEquals(ConnectionHolder conHolder, Connection passedInCon) {
		if (!conHolder.hasConnection()) {
			return false;
		}
		Connection heldCon = conHolder.getConnection();
		// 也显式检查身份：对于未实现的连接句柄
		// 正确地“等于”，例如 Commons DBCP 公开的）。
		return (heldCon == passedInCon || heldCon.equals(passedInCon) ||
				getTargetConnection(heldCon).equals(passedInCon));
	}

	/**
	 * 返回给定连接的最里面的目标连接。如果给定的连接是代理，它将被解包，直到找到非代理连接。否则，传入的 Connection 将按原样返回。
	 * @param con 要解包的连接代理
	 * @return 最里面的目标连接，如果没有代理则为传入的连接
	 * @see ConnectionProxy#getTargetConnection()
	 */
	public static Connection getTargetConnection(Connection con) {
		Connection conToUse = con;
		while (conToUse instanceof ConnectionProxy connectionProxy) {
			Connection targetCon = connectionProxy.getTargetConnection();
			if (targetCon == conToUse) {
				break;
			}
			conToUse = targetCon;
		}
		return conToUse;
	}

	/**
	 * 确定用于给定数据源的连接同步顺序。通过 DelegatingDataSource 嵌套级别进行检查，数据源具有的每个嵌套级别都会减少。
	 * @param dataSource 要检查的数据源
	 * @return 连接同步使用顺序
	 * @see #CONNECTION_SYNCHRONIZATION_ORDER
	 */
	private static int getConnectionSynchronizationOrder(DataSource dataSource) {
		int order = CONNECTION_SYNCHRONIZATION_ORDER;
		DataSource currDs = dataSource;
		while (currDs instanceof DelegatingDataSource delegatingDataSource) {
			order--;
			currDs = delegatingDataSource.getTargetDataSource();
		}
		return order;
	}


	/**
	 * 非本机 JDBC 事务结束时（例如，参与 JtaTransactionManager 事务时）回调资源清理。
	 * @see org.springframework.transaction.jta.JtaTransactionManager
	 */
	private static class ConnectionSynchronization implements TransactionSynchronization {

		private final ConnectionHolder connectionHolder;

		private final DataSource dataSource;

		private final int order;

		private boolean holderActive = true;

		public ConnectionSynchronization(ConnectionHolder connectionHolder, DataSource dataSource) {
			this.connectionHolder = connectionHolder;
			this.dataSource = dataSource;
			this.order = getConnectionSynchronizationOrder(dataSource);
		}

		@Override
		public int getOrder() {
			return this.order;
		}

		@Override
		public void suspend() {
			if (this.holderActive) {
				TransactionSynchronizationManager.unbindResource(this.dataSource);
				if (this.connectionHolder.hasConnection() && !this.connectionHolder.isOpen()) {
					// 如果应用程序不保留，则在挂起时释放连接
					// 不再有它的句柄了。如果出现以下情况，我们将获取一个新的连接
					// 应用程序恢复后再次访问ConnectionHolder，
					// 假设它将参与同一交易。
					releaseConnection(this.connectionHolder.getConnection(), this.dataSource);
					this.connectionHolder.setConnection(null);
				}
			}
		}

		@Override
		public void resume() {
			if (this.holderActive) {
				TransactionSynchronizationManager.bindResource(this.dataSource, this.connectionHolder);
			}
		}

		@Override
		public void beforeCompletion() {
			// 如果持有者不再打开，请尽早释放连接
			// （也就是说，不被 Hibernate Session 等其他资源使用
			// 通过事务同步有自己的清理），
			// 以避免严格的 JTA 实现的问题
			// 交易完成前的千钧一发。
			if (!this.connectionHolder.isOpen()) {
				TransactionSynchronizationManager.unbindResource(this.dataSource);
				this.holderActive = false;
				if (this.connectionHolder.hasConnection()) {
					releaseConnection(this.connectionHolder.getConnection(), this.dataSource);
				}
			}
		}

		@Override
		public void afterCompletion(int status) {
			// 如果我们没有在beforeCompletion中关闭Connection，
			// 现在关闭它。该支架可能已用于其他用途
			// 同时进行清理，例如通过 Hibernate Session。
			if (this.holderActive) {
				// 线程绑定的 ConnectionHolder 可能不再可用，
				// 因为 afterCompletion 可能会从不同的线程调用。
				TransactionSynchronizationManager.unbindResourceIfPossible(this.dataSource);
				this.holderActive = false;
				if (this.connectionHolder.hasConnection()) {
					releaseConnection(this.connectionHolder.getConnection(), this.dataSource);
					// 重置 ConnectionHolder：它可能仍与线程绑定。
					this.connectionHolder.setConnection(null);
				}
			}
			this.connectionHolder.reset();
		}
	}

}
