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

package org.springframework.transaction.support;

import org.jspecify.annotations.Nullable;

import org.springframework.transaction.NestedTransactionNotSupportedException;
import org.springframework.transaction.SavepointManager;
import org.springframework.util.Assert;

/**
 * {@link org.springframework.transaction.TransactionStatus} 接口的默认实现，
 * 由 {@link AbstractPlatformTransactionManager} 使用。基于底层 "事务对象" 概念。
 *
 * <p>持有 {@link AbstractPlatformTransactionManager} 内部所需的全部状态信息，
 * 包括由具体事务管理器实现确定的通用事务对象。
 *
 * <p>支持将保存点相关方法委托给实现 {@link SavepointManager} 接口的事务对象。
 *
 * <p><b>注意：</b>本类<i>不</i>供其他 PlatformTransactionManager 实现使用，
 * 尤其不用于测试环境中的 mock 事务管理器。
 * 请改用 {@link SimpleTransactionStatus} 类或普通
 * {@link org.springframework.transaction.TransactionStatus} 接口的 mock。
 *
 * @author Juergen Hoeller
 * @since 19.01.2004
 * @see AbstractPlatformTransactionManager
 * @see org.springframework.transaction.SavepointManager
 * @see #getTransaction
 * @see #createSavepoint
 * @see #rollbackToSavepoint
 * @see #releaseSavepoint
 * @see SimpleTransactionStatus
 */
public class DefaultTransactionStatus extends AbstractTransactionStatus {

	private final @Nullable String transactionName;

	private final @Nullable Object transaction;

	private final boolean newTransaction;

	private final boolean newSynchronization;

	private final boolean nested;

	private final boolean readOnly;

	private final boolean debug;

	private final @Nullable Object suspendedResources;


	/**
	 * 创建新的 {@code DefaultTransactionStatus} 实例。
	 * @param transactionName 定义的事务名称
	 * @param transaction 可为内部事务实现保存状态的底层事务对象
	 * @param newTransaction 若为新事务则为 true，否则为参与现有事务
	 * @param newSynchronization 若为给定事务开启了新事务同步则为 true
	 * @param readOnly 事务是否标记为只读
	 * @param debug 是否为本事务处理启用 debug 日志？
	 * 在此缓存可避免反复查询日志系统是否启用 debug。
	 * @param suspendedResources 为本事务挂起的资源的持有者（若有）
	 * @since 6.1
	 */
	public DefaultTransactionStatus(
			@Nullable String transactionName, @Nullable Object transaction, boolean newTransaction,
			boolean newSynchronization, boolean nested, boolean readOnly, boolean debug,
			@Nullable Object suspendedResources) {

		this.transactionName = transactionName;
		this.transaction = transaction;
		this.newTransaction = newTransaction;
		this.newSynchronization = newSynchronization;
		this.nested = nested;
		this.readOnly = readOnly;
		this.debug = debug;
		this.suspendedResources = suspendedResources;
	}


	@Override
	public String getTransactionName() {
		return (this.transactionName != null ? this.transactionName : "");
	}

	/**
	 * 返回底层事务对象。
	 * @throws IllegalStateException 若无活动事务
	 */
	public Object getTransaction() {
		Assert.state(this.transaction != null, "No transaction active");
		return this.transaction;
	}

	@Override
	public boolean hasTransaction() {
		return (this.transaction != null);
	}

	@Override
	public boolean isNewTransaction() {
		return (hasTransaction() && this.newTransaction);
	}

	/**
	 * 返回是否已为该事务开启新事务同步。
	 */
	public boolean isNewSynchronization() {
		return this.newSynchronization;
	}

	@Override
	public boolean isNested() {
		return this.nested;
	}

	@Override
	public boolean isReadOnly() {
		return this.readOnly;
	}

	/**
	 * 返回是否 debug 本事务进度。{@link AbstractPlatformTransactionManager} 用作优化，
	 * 避免反复调用 {@code logger.isDebugEnabled()}。通常不供客户端代码使用。
	 */
	public boolean isDebug() {
		return this.debug;
	}

	/**
	 * 返回为本事务挂起的资源的持有者（若有）。
	 */
	public @Nullable Object getSuspendedResources() {
		return this.suspendedResources;
	}


	//---------------------------------------------------------------------
	// 通过底层事务对象启用功能
	//---------------------------------------------------------------------

	/**
	 * 通过检查事务对象确定 rollback-only 标志（前提是其 implements {@link SmartTransactionObject}）。
	 * <p>若全局事务本身已被事务协调器标记 rollback-only（例如超时），将返回 {@code true}。
	 * @see SmartTransactionObject#isRollbackOnly()
	 */
	@Override
	public boolean isGlobalRollbackOnly() {
		return (this.transaction instanceof SmartTransactionObject smartTransactionObject &&
				smartTransactionObject.isRollbackOnly());
	}

	/**
	 * 本实现暴露底层事务对象（若有）的 {@link SavepointManager} 接口。
	 * @throws NestedTransactionNotSupportedException 若不支持保存点
	 * @see #isTransactionSavepointManager()
	 */
	@Override
	protected SavepointManager getSavepointManager() {
		Object transaction = this.transaction;
		if (!(transaction instanceof SavepointManager savepointManager)) {
			throw new NestedTransactionNotSupportedException(
					"Transaction object [" + this.transaction + "] does not support savepoints");
		}
		return savepointManager;
	}

	/**
	 * 返回底层事务是否实现 {@link SavepointManager} 接口从而支持保存点。
	 * @see #getTransaction()
	 * @see #getSavepointManager()
	 */
	public boolean isTransactionSavepointManager() {
		return (this.transaction instanceof SavepointManager);
	}

	/**
	 * 将 flush 委托给事务对象（前提是其 implements {@link SmartTransactionObject}）。
	 * @see SmartTransactionObject#flush()
	 */
	@Override
	public void flush() {
		if (this.transaction instanceof SmartTransactionObject smartTransactionObject) {
			smartTransactionObject.flush();
		}
	}

}
