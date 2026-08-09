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

package org.springframework.transaction.jta;

import jakarta.transaction.Status;
import jakarta.transaction.Synchronization;
import jakarta.transaction.TransactionManager;
import jakarta.transaction.UserTransaction;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;

import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;

/**
 * 实现 JTA {@link jakarta.transaction.Synchronization} 接口的适配器，
 * 委托给底层 Spring {@link org.springframework.transaction.support.TransactionSynchronization}。
 *
 * <p>尽管原始代码面向 Spring 事务同步构建，
 * 本适配器仍可用于将 Spring 资源管理代码与纯 JTA / EJB CMT 事务同步。
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see jakarta.transaction.Transaction#registerSynchronization
 * @see org.springframework.transaction.support.TransactionSynchronization
 */
public class SpringJtaSynchronizationAdapter implements Synchronization {

	protected static final Log logger = LogFactory.getLog(SpringJtaSynchronizationAdapter.class);

	private final TransactionSynchronization springSynchronization;

	private @Nullable UserTransaction jtaTransaction;

	private boolean beforeCompletionCalled = false;


	/**
	 * 为给定 Spring TransactionSynchronization 创建新的 SpringJtaSynchronizationAdapter。
	 * @param springSynchronization 要委托的 Spring TransactionSynchronization
	 */
	public SpringJtaSynchronizationAdapter(TransactionSynchronization springSynchronization) {
		Assert.notNull(springSynchronization, "TransactionSynchronization must not be null");
		this.springSynchronization = springSynchronization;
	}

	/**
	 * 为给定 Spring TransactionSynchronization 和 JTA UserTransaction 创建新的 SpringJtaSynchronizationAdapter。
	 * @param springSynchronization 要委托的 Spring TransactionSynchronization
	 * @param jtaUserTransaction 在 {@code beforeCompletion} 抛出异常时用于设置 rollback-only 的 JTA UserTransaction
	 * @deprecated 自 6.0.12 起弃用，因 JTA 1.1+ 要求在 {@code beforeCompletion} 抛出异常时
	 * 隐式设置 rollback-only，常规 {@link #SpringJtaSynchronizationAdapter(TransactionSynchronization)} 构造函数
	 * 已足以覆盖所有场景
	 */
	@Deprecated(since = "6.0.12")
	public SpringJtaSynchronizationAdapter(TransactionSynchronization springSynchronization,
			@Nullable UserTransaction jtaUserTransaction) {

		this(springSynchronization);
		this.jtaTransaction = jtaUserTransaction;
	}

	/**
	 * 为给定 Spring TransactionSynchronization 和 JTA TransactionManager 创建新的 SpringJtaSynchronizationAdapter。
	 * @param springSynchronization 要委托的 Spring TransactionSynchronization
	 * @param jtaTransactionManager 在 {@code beforeCompletion} 抛出异常时用于设置 rollback-only 的 JTA TransactionManager
	 * @deprecated 自 6.0.12 起弃用，因 JTA 1.1+ 要求在 {@code beforeCompletion} 抛出异常时
	 * 隐式设置 rollback-only，常规 {@link #SpringJtaSynchronizationAdapter(TransactionSynchronization)} 构造函数
	 * 已足以覆盖所有场景
	 */
	@Deprecated(since = "6.0.12")
	public SpringJtaSynchronizationAdapter(TransactionSynchronization springSynchronization,
			@Nullable TransactionManager jtaTransactionManager) {

		this(springSynchronization);
		this.jtaTransaction =
				(jtaTransactionManager != null ? new UserTransactionAdapter(jtaTransactionManager) : null);
	}


	/**
	 * JTA {@code beforeCompletion} 回调：在提交前调用。
	 * <p>若发生异常，JTA 事务将被标记为 rollback-only。
	 * @see org.springframework.transaction.support.TransactionSynchronization#beforeCommit
	 */
	@Override
	public void beforeCompletion() {
		try {
			boolean readOnly = TransactionSynchronizationManager.isCurrentTransactionReadOnly();
			this.springSynchronization.beforeCommit(readOnly);
		}
		catch (RuntimeException | Error ex) {
			setRollbackOnlyIfPossible();
			throw ex;
		}
		finally {
			// 提前处理 Spring 的 beforeCompletion，以避免严格 JTA 实现在事务完成后
			// 执行 JDBC 操作（例如 Connection.getWarnings）时发出警告的问题。
			this.beforeCompletionCalled = true;
			this.springSynchronization.beforeCompletion();
		}
	}

	/**
	 * 将底层 JTA 事务设置为 rollback-only。
	 */
	private void setRollbackOnlyIfPossible() {
		if (this.jtaTransaction != null) {
			try {
				this.jtaTransaction.setRollbackOnly();
			}
			catch (UnsupportedOperationException ex) {
				// 可能是 Hibernate 的 WebSphereExtendedJTATransactionLookup 伪 JTA 实现...
				logger.debug("JTA transaction handle does not support setRollbackOnly method - " +
						"relying on JTA provider to mark the transaction as rollback-only based on " +
						"the exception thrown from beforeCompletion", ex);
			}
			catch (Throwable ex) {
				logger.error("Could not set JTA transaction rollback-only", ex);
			}
		}
		else {
			logger.debug("No JTA transaction handle available and/or running on WebLogic - " +
						"relying on JTA provider to mark the transaction as rollback-only based on " +
						"the exception thrown from beforeCompletion");
			}
	}

	/**
	 * JTA {@code afterCompletion} 回调：在提交/回滚后调用。
	 * <p>若发生回滚，需在此阶段调用 Spring 同步的 {@code beforeCompletion}，
	 * 因为 JTA 没有对应的回调。
	 * @see org.springframework.transaction.support.TransactionSynchronization#beforeCompletion
	 * @see org.springframework.transaction.support.TransactionSynchronization#afterCompletion
	 */
	@Override
	public void afterCompletion(int status) {
		if (!this.beforeCompletionCalled) {
			// 之前未调用 beforeCompletion（可能因 JTA 回滚）。
			// 在此执行清理。
			this.springSynchronization.beforeCompletion();
		}
		// 以适当的状态指示调用 afterCompletion。
		switch (status) {
			case Status.STATUS_COMMITTED ->
				this.springSynchronization.afterCompletion(TransactionSynchronization.STATUS_COMMITTED);
			case Status.STATUS_ROLLEDBACK ->
				this.springSynchronization.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK);
			default ->
				this.springSynchronization.afterCompletion(TransactionSynchronization.STATUS_UNKNOWN);
		}
	}

}
