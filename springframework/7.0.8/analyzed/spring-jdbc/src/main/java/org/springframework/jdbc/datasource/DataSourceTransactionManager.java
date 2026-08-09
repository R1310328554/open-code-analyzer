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

import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.ResourceTransactionManager;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionSynchronizationUtils;
import org.springframework.util.Assert;

/**
 * 用于单个 JDBC {@link javax.sql.DataSource} 的 {@link
 * org.springframework.transaction.PlatformTransactionManager} 实现。只要设置使用 {@code
 * javax.sql.DataSource} 作为其 {@code Connection} 工厂机制，此类就能够在具有任何 JDBC 驱动程序的任何环境中工作。将 JDBC
 * {@code Connection} 从指定的 {@code DataSource} 绑定到当前线程，可能允许每个 {@code DataSource} 有一个线程绑定的
 * {@code Connection}。
 * <p><b>注意：此事务管理器操作的 {@code DataSource} 需要返回独立的 {@code Connection}。</b> {@code
 * Connection} 通常来自连接池，但 {@code DataSource} 不得返回特定范围或受约束的 {@code
 * Connection}。该事务管理器将根据指定的传播行为将 {@code Connection} 与线程绑定事务相关联。它假设即使在正在进行的事务期间也可以获得单独的、独立的
 * {@code Connection}。
 * <p>应用程序代码需要通过 {@link DataSourceUtils#getConnection(DataSource)}（而不是标准 EE 风格的 {@link
 * DataSource#getConnection()} 调用）检索 JDBC {@code Connection}。 Spring 类（例如 {@link
 * org.springframework.jdbc.core.JdbcTemplate}）隐式使用此策略。如果不与此事务管理器结合使用，{@link
 * DataSourceUtils} 查找策略的行为与本机 {@code DataSource} 查找完全相同；因此它可以以便携式方式使用。
 * <p> 或者，您可以允许应用程序代码使用标准 EE 风格的查找模式 {@link DataSource#getConnection()}，例如，对于根本不支持 Spring
 * 的遗留代码。在这种情况下，为您的目标 {@code DataSource} 定义一个 {@link TransactionAwareDataSourceProxy}，并将该代理
 * {@code DataSource} 传递给您的 DAO，DAO 在访问它时将自动参与 Spring 管理的事务。
 * <p>S支持自定义隔离级别以及作为适当的 JDBC 语句超时应用的超时。为了支持后者，应用程序代码必须使用 {@link org.springframework.jdbc.co
 * re.JdbcTemplate}，为每个创建的 JDBC {@code Statement} 调用 {@link DataSourceUtils#applyTransactio
 * nTimeout}，或者通过 {@link TransactionAwareDataSourceProxy}（它将自动创建超时感知的 JDBC {@code Connectio
 * n} 和 {@code Statement}）。
 * <p>考虑为您的目标 {@code DataSource} 定义 {@link LazyConnectionDataSourceProxy}，将此事务管理器和您的 DAO
 * 都指向它。这将导致对“空”事务（即没有执行任何 JDBC 语句的事务）的优化处理。在执行 {@code Statement} 之前，{@code
 * LazyConnectionDataSourceProxy} 不会从目标 {@code DataSource} 获取实际的 JDBC {@code
 * Connection}，从而延迟将指定的事务设置应用于目标 {@code Connection}。
 * <p> 此事务管理器通过 JDBC {@link java.sql.Savepoint} 机制支持嵌套事务。 {@link
 * #setNestedTransactionAllowed "nestedTransactionAllowed"} 标志默认为“true”，因为嵌套事务将在支持保存点的 JDBC
 * 驱动程序（例如 Oracle JDBC 驱动程序）上不受限制地运行。
 * <p> 在单一资源情况下，该事务管理器可以用作 {@link
 * org.springframework.transaction.jta.JtaTransactionManager} 的替代品，因为它不需要支持 JTA
 * 的容器，通常与本地定义的 JDBC {@code DataSource}（例如 Hikari 连接池）结合使用。在本地策略和 JTA 环境之间切换只需配置问题！
 * 在 4.3.4 的 <p>A 中，该事务管理器会在注册的事务同步上触发刷新回调（如果同步通常处于活动状态），假设资源在底层 JDBC {@code Connection} 上运
 * 行。这允许类似于 {@code JtaTransactionManager} 的设置，特别是对于延迟注册的 ORM 资源（例如，Hibernate {@code Session
 * }）。
 * <p><b>NOTE：从 5.3 开始，{@link org.springframework.jdbc.support.JdbcTransactionManager}
 * 可作为扩展子类使用，其中包括提交/回滚异常转换，与 {@link org.springframework.jdbc.core.JdbcTemplate}.</b> 保持一致
 * @author Juergen Hoeller
 * @since 02.05.2003
 * @see #setNestedTransactionAllowed
 * @see java.sql.Savepoint
 * @see DataSourceUtils#getConnection(javax.sql.DataSource)
 * @see DataSourceUtils#applyTransactionTimeout
 * @see DataSourceUtils#releaseConnection
 * @see TransactionAwareDataSourceProxy
 * @see LazyConnectionDataSourceProxy
 * @see org.springframework.jdbc.core.JdbcTemplate
 * @see org.springframework.jdbc.support.JdbcTransactionManager
 */
@SuppressWarnings("serial")
public class DataSourceTransactionManager extends AbstractPlatformTransactionManager
		implements ResourceTransactionManager, InitializingBean {

	/** 来源相关状态（`dataSource`）。 */
	private @Nullable DataSource dataSource;

	/** `false`：该类的成员状态。 */
	private boolean enforceReadOnly = false;

	/** `defaultReadOnly`：该类的成员状态。 */
	private volatile @Nullable Boolean defaultReadOnly;


	/**
	 * 创建一个新的 {@code DataSourceTransactionManager} 实例。必须设置 {@code DataSource} 才能使用它。
	 * @see #setDataSource
	 */
	public DataSourceTransactionManager() {
		setNestedTransactionAllowed(true);
	}

	/**
	 * 创建一个新的 {@code DataSourceTransactionManager} 实例。
	 * @param dataSource 用于管理事务的 JDBC 数据源
	 */
	public DataSourceTransactionManager(DataSource dataSource) {
		this();
		setDataSource(dataSource);
		afterPropertiesSet();
	}


	/**
	 * 设置此实例应管理事务的 JDBC {@code DataSource}。 <p>这通常是本地定义的 {@code DataSource}，例如 Hikari
	 * 连接池。或者，您还可以管理从 JNDI 获取的非 XA {@code DataSource} 的事务。对于 XA {@code DataSource}，请改用 {@link
	 * org.springframework.transaction.jta.JtaTransactionManager}。 <p>此处指定的 {@code DataSource}
	 * 应该是管理事务的目标 {@code DataSource}，而不是 {@link TransactionAwareDataSourceProxy}。只有数据访问代码可以与
	 * {@code TransactionAwareDataSourceProxy} 一起工作，而事务管理器需要在底层目标 {@code DataSource} 上工作。如果仍然传入
	 * {@code TransactionAwareDataSourceProxy}，则会将其解包以提取其目标 {@code DataSource}。 <p><b>此处传入的
	 * {@code DataSource} 需要返回独立的 {@code Connection}。</b> {@code Connection} 通常来自连接池，但 {@code
	 * DataSource} 不得返回特定作用域或受约束的 {@code Connection}，而可能是延迟获取的。
	 * @see LazyConnectionDataSourceProxy
	 */
	public void setDataSource(@Nullable DataSource dataSource) {
		if (dataSource instanceof TransactionAwareDataSourceProxy tadsp) {
			// 如果我们有一个 TransactionAwareDataSourceProxy，我们需要执行事务
			// 对于其底层目标数据源，否则数据访问代码将看不到
			// 正确公开的事务（即目标数据源的事务）。
			this.dataSource = tadsp.getTargetDataSource();
		}
		else {
			this.dataSource = dataSource;
		}
	}

	/**
	 * 返回此实例管理事务的 JDBC {@code DataSource}。
	 */
	public @Nullable DataSource getDataSource() {
		return this.dataSource;
	}

	/**
	 * 获取{@code DataSource}以供实际使用。
	 * @return 数据源（绝不是 {@code null}）
	 * @throws IllegalStateException 如果没有设置数据源
	 * @since 5.0
	 */
	protected DataSource obtainDataSource() {
		DataSource dataSource = getDataSource();
		Assert.state(dataSource != null, "No DataSource set");
		return dataSource;
	}

	/**
	 * 指定是否通过事务连接上的显式语句强制事务的只读性质（如 {@link TransactionDefinition#isReadOnly()} 所示）：Oracle、MySQL 
	 * 和 Postgres 所理解的“SET TRANSACTION READ ONLY”。 <p> 的精确处理，包括在连接上执行的任何 SQL 语句，都可以通过 {@link #p
	 * repareTransactionalConnection} 进行定制。 <p>这种只读处理模式超出了Spring默认应用的{@link Connection#setReadO
	 * nly}提示。与标准 JDBC 提示相反，“SET TRANSACTION READ ONLY”强制执行类似隔离级别的连接模式，其中严格不允许数据操作语句。另外，在 Oracl
	 * e 上，这种只读模式为整个事务提供了读一致性。 <p> 请注意，旧版 Oracle JDBC 驱动程序（9i、10g）用于强制执行此只读模式，即使对于 {@code Conne
	 * ction.setReadOnly(true} 也是如此。然而，对于最近的驱动程序，需要明确应用这种强有力的强制执行，例如通过此标志。
	 * @since 4.3.7
	 * @see #prepareTransactionalConnection
	 */
	public void setEnforceReadOnly(boolean enforceReadOnly) {
		this.enforceReadOnly = enforceReadOnly;
	}

	/**
	 * 返回是否通过事务连接上的显式语句强制事务的只读性质。
	 * @since 4.3.7
	 * @see #setEnforceReadOnly
	 */
	public boolean isEnforceReadOnly() {
		return this.enforceReadOnly;
	}

	/**
	 * 在…之后回调：Properties Set（方法 `afterPropertiesSet`）。
	 */
	@Override
	public void afterPropertiesSet() {
		if (getDataSource() == null) {
			throw new IllegalArgumentException("Property 'dataSource' is required");
		}
	}


	/**
	 * 获取 Resource Factory（`ResourceFactory`）。
	 */
	@Override
	public Object getResourceFactory() {
		return obtainDataSource();
	}

	/**
	 * 执行核心逻辑：Get Transaction（方法 `doGetTransaction`）。
	 */
	@Override
	protected Object doGetTransaction() {
		DataSourceTransactionObject txObject = new DataSourceTransactionObject();
		txObject.setSavepointAllowed(isNestedTransactionAllowed());
		ConnectionHolder conHolder =
				(ConnectionHolder) TransactionSynchronizationManager.getResource(obtainDataSource());
		txObject.setConnectionHolder(conHolder, false);
		return txObject;
	}

	/**
	 * 判断是否 Existing Transaction。
	 */
	@Override
	protected boolean isExistingTransaction(Object transaction) {
		DataSourceTransactionObject txObject = (DataSourceTransactionObject) transaction;
		return (txObject.hasConnectionHolder() && txObject.getConnectionHolder().isTransactionActive());
	}

	/**
	 * 执行核心逻辑：Begin（方法 `doBegin`）。
	 */
	@Override
	protected void doBegin(Object transaction, TransactionDefinition definition) {
		DataSourceTransactionObject txObject = (DataSourceTransactionObject) transaction;
		Connection con = null;

		try {
			if (!txObject.hasConnectionHolder() ||
					txObject.getConnectionHolder().isSynchronizedWithTransaction()) {
				Connection newCon = obtainDataSource().getConnection();
				if (logger.isDebugEnabled()) {
					logger.debug("Acquired Connection [" + newCon + "] for JDBC transaction");
				}
				if (definition.isReadOnly()) {
					checkDefaultReadOnly(newCon);
				}
				txObject.setConnectionHolder(new ConnectionHolder(newCon), true);
			}

			txObject.getConnectionHolder().setSynchronizedWithTransaction(true);
			con = txObject.getConnectionHolder().getConnection();

			Integer previousIsolationLevel = DataSourceUtils.prepareConnectionForTransaction(con,
					definition.getIsolationLevel(),
					(definition.isReadOnly() && !isDefaultReadOnly()));
			txObject.setPreviousIsolationLevel(previousIsolationLevel);
			txObject.setReadOnly(definition.isReadOnly());

			// 如有必要，请切换到手动提交。这在某些 JDBC 驱动程序中非常昂贵，
			// 所以我们不想做不必要的事情（例如，如果我们明确地
			// 配置连接池以进行设置）。
			if (con.getAutoCommit()) {
				txObject.setMustRestoreAutoCommit(true);
				if (logger.isDebugEnabled()) {
					logger.debug("Switching JDBC Connection [" + con + "] to manual commit");
				}
				con.setAutoCommit(false);
			}

			prepareTransactionalConnection(con, definition);
			txObject.getConnectionHolder().setTransactionActive(true);

			int timeout = determineTimeout(definition);
			if (timeout != TransactionDefinition.TIMEOUT_DEFAULT) {
				txObject.getConnectionHolder().setTimeoutInSeconds(timeout);
			}

			// 将连接支架固定到螺纹上。
			if (txObject.isNewConnectionHolder()) {
				TransactionSynchronizationManager.bindResource(obtainDataSource(), txObject.getConnectionHolder());
			}
		}

		catch (Throwable ex) {
			if (txObject.isNewConnectionHolder()) {
				DataSourceUtils.releaseConnection(con, obtainDataSource());
				txObject.setConnectionHolder(null, false);
			}
			throw new CannotCreateTransactionException("Could not open JDBC Connection for transaction", ex);
		}
	}

	/**
	 * 执行核心逻辑：Suspend（方法 `doSuspend`）。
	 */
	@Override
	protected Object doSuspend(Object transaction) {
		DataSourceTransactionObject txObject = (DataSourceTransactionObject) transaction;
		txObject.setConnectionHolder(null);
		return TransactionSynchronizationManager.unbindResource(obtainDataSource());
	}

	/**
	 * 执行核心逻辑：Resume（方法 `doResume`）。
	 */
	@Override
	protected void doResume(@Nullable Object transaction, Object suspendedResources) {
		TransactionSynchronizationManager.bindResource(obtainDataSource(), suspendedResources);
	}

	/**
	 * 执行核心逻辑：Commit（方法 `doCommit`）。
	 */
	@Override
	protected void doCommit(DefaultTransactionStatus status) {
		DataSourceTransactionObject txObject = (DataSourceTransactionObject) status.getTransaction();
		Connection con = txObject.getConnectionHolder().getConnection();
		if (status.isDebug()) {
			logger.debug("Committing JDBC transaction on Connection [" + con + "]");
		}
		try {
			con.commit();
		}
		catch (SQLException ex) {
			throw translateException("JDBC commit", ex);
		}
	}

	/**
	 * 执行核心逻辑：Rollback（方法 `doRollback`）。
	 */
	@Override
	protected void doRollback(DefaultTransactionStatus status) {
		DataSourceTransactionObject txObject = (DataSourceTransactionObject) status.getTransaction();
		Connection con = txObject.getConnectionHolder().getConnection();
		if (status.isDebug()) {
			logger.debug("Rolling back JDBC transaction on Connection [" + con + "]");
		}
		try {
			con.rollback();
		}
		catch (SQLException ex) {
			throw translateException("JDBC rollback", ex);
		}
	}

	/**
	 * 执行核心逻辑：Set Rollback Only（方法 `doSetRollbackOnly`）。
	 */
	@Override
	protected void doSetRollbackOnly(DefaultTransactionStatus status) {
		DataSourceTransactionObject txObject = (DataSourceTransactionObject) status.getTransaction();
		if (status.isDebug()) {
			logger.debug("Setting JDBC transaction [" + txObject.getConnectionHolder().getConnection() +
					"] rollback-only");
		}
		txObject.setRollbackOnly();
	}

	/**
	 * 执行核心逻辑：Cleanup After Completion（方法 `doCleanupAfterCompletion`）。
	 */
	@Override
	protected void doCleanupAfterCompletion(Object transaction) {
		DataSourceTransactionObject txObject = (DataSourceTransactionObject) transaction;

		// 如果连接支架裸露，请将其从螺纹上拆下。
		if (txObject.isNewConnectionHolder()) {
			TransactionSynchronizationManager.unbindResource(obtainDataSource());
		}

		// 重置连接。
		Connection con = txObject.getConnectionHolder().getConnection();
		try {
			if (txObject.isMustRestoreAutoCommit()) {
				con.setAutoCommit(true);
			}
			DataSourceUtils.resetConnectionAfterTransaction(con,
					txObject.getPreviousIsolationLevel(),
					(txObject.isReadOnly() && !isDefaultReadOnly()));
		}
		catch (Throwable ex) {
			logger.debug("Could not reset JDBC Connection after transaction", ex);
		}

		if (txObject.isNewConnectionHolder()) {
			if (logger.isDebugEnabled()) {
				logger.debug("Releasing JDBC Connection [" + con + "] after transaction");
			}
			DataSourceUtils.releaseConnection(con, this.dataSource);
		}

		txObject.getConnectionHolder().clear();
	}


	/**
	 * 检查从 {@code DataSource} 新获取的连接上的默认 {@link Connection#isReadOnly()}
	 * 标志，假设相同的标志适用于从给定设置获取的所有连接。
	 * @param newCon 要检查的连接
	 * @since 6.2.13
	 * @see #isDefaultReadOnly()
	 */
	private void checkDefaultReadOnly(Connection newCon) {
		if (this.defaultReadOnly == null) {
			try {
				this.defaultReadOnly = newCon.isReadOnly();
			}
			catch (Throwable ex) {
				logger.debug("Could not determine default JDBC Connection isReadOnly - assuming false", ex);
				this.defaultReadOnly = false;
			}
		}
	}

	/**
	 * 检查默认只读标志是否已确定为 {@code true}，假设所有遇到的连接默认都是只读的，因此不需要显式 {@link Connection#setReadOnly}（重新）设
	 * 置。
	 * @since 6.2.13
	 * @see #checkDefaultReadOnly(Connection)
	 */
	private boolean isDefaultReadOnly() {
		return (this.defaultReadOnly == Boolean.TRUE);
	}

	/**
	 * 事务开始后立即准备事务 {@code Connection}。 <p> 如果 {@link #setEnforceReadOnly "enforceReadOnly"}
	 * 标志设置为 {@code true} 并且事务定义指示只读事务，则默认实现执行“SET TRANSACTION READ ONLY”语句。 <p>“SET
	 * TRANSACTION READ ONLY”可以被 Oracle、MySQL 和 Postgres
	 * 理解，也可以与其他数据库一起使用。如果您想采用此处理方法，请相应地覆盖此方法。
	 * @param con 事务性 JDBC 连接
	 * @param definition 当前事务定义
	 * @throws SQLException 如果由 JDBC API 抛出
	 * @since 4.3.7
	 * @see #setEnforceReadOnly
	 */
	protected void prepareTransactionalConnection(Connection con, TransactionDefinition definition)
			throws SQLException {

		if (isEnforceReadOnly() && definition.isReadOnly()) {
			try (Statement stmt = con.createStatement()) {
				stmt.executeUpdate("SET TRANSACTION READ ONLY");
			}
		}
	}

	/**
	 * 将给定的 JDBC 提交/回滚异常转换为常见的 Spring 异常，以从 {@link #commit}/{@link #rollback} 调用传播。
	 * <p>默认实现抛出{@link TransactionSystemException}。子类可以专门识别并发失败等。
	 * @param task 任务描述（提交或回滚）
	 * @param ex 提交/回滚抛出的 SQLException
	 * @return 要抛出的翻译异常，{@link org.springframework.dao.DataAccessException} 或 {@link org.springframework.transaction.TransactionException}
	 * @since 5.3
	 */
	protected RuntimeException translateException(String task, SQLException ex) {
		return new TransactionSystemException(task + " failed", ex);
	}


	/**
	 * DataSource事务对象，代表一个ConnectionHolder。由 DataSourceTransactionManager 用作事务对象。
	 */
	private static class DataSourceTransactionObject extends JdbcTransactionObjectSupport {

		private boolean newConnectionHolder;

		private boolean mustRestoreAutoCommit;

		public void setConnectionHolder(@Nullable ConnectionHolder connectionHolder, boolean newConnectionHolder) {
			super.setConnectionHolder(connectionHolder);
			this.newConnectionHolder = newConnectionHolder;
		}

		public boolean isNewConnectionHolder() {
			return this.newConnectionHolder;
		}

		public void setMustRestoreAutoCommit(boolean mustRestoreAutoCommit) {
			this.mustRestoreAutoCommit = mustRestoreAutoCommit;
		}

		public boolean isMustRestoreAutoCommit() {
			return this.mustRestoreAutoCommit;
		}

		public void setRollbackOnly() {
			getConnectionHolder().setRollbackOnly();
		}

		@Override
		public boolean isRollbackOnly() {
			return getConnectionHolder().isRollbackOnly();
		}

		@Override
		public void flush() {
			TransactionSynchronizationUtils.triggerFlush();
		}
	}

}
