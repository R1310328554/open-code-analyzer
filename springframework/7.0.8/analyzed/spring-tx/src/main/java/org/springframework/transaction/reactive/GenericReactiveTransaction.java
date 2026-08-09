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

package org.springframework.transaction.reactive;

import org.jspecify.annotations.Nullable;

import org.springframework.transaction.ReactiveTransaction;
import org.springframework.util.Assert;

/**
 * {@link ReactiveTransaction} 接口的默认实现，
 * 由 {@link AbstractReactiveTransactionManager} 使用。基于底层 "事务对象" 概念。
 *
 * <p>持有 {@link AbstractReactiveTransactionManager} 内部所需的全部状态信息，
 * 包括由具体事务管理器实现确定的通用事务对象。
 *
 * <p><b>注意：</b>本类<i>不</i>供其他 ReactiveTransactionManager 实现使用，
 * 尤其不用于测试环境中的 mock 事务管理器。
 *
 * @author Mark Paluch
 * @author Juergen Hoeller
 * @since 5.2
 * @see AbstractReactiveTransactionManager
 * @see #getTransaction
 */
public class GenericReactiveTransaction implements ReactiveTransaction {

	private final @Nullable String transactionName;

	private final @Nullable Object transaction;

	private final boolean newTransaction;

	private final boolean newSynchronization;

	private final boolean nested;

	private final boolean readOnly;

	private final boolean debug;

	private final @Nullable Object suspendedResources;

	private boolean rollbackOnly = false;

	private boolean completed = false;


	/**
	 * 创建新的 {@code DefaultReactiveTransactionStatus} 实例。
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
	public GenericReactiveTransaction(
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
	 * 返回是否 debug 本事务进度。{@link AbstractReactiveTransactionManager} 用作优化，
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

	@Override
	public void setRollbackOnly() {
		if (this.completed) {
			throw new IllegalStateException("Transaction completed");
		}
		this.rollbackOnly = true;
	}

	/**
	 * 通过检查本 ReactiveTransactionStatus 确定 rollback-only 标志。
	 * <p>仅当应用在本 TransactionStatus 对象上调用 {@code setRollbackOnly} 时才返回 "true"。
	 */
	@Override
	public boolean isRollbackOnly() {
		return this.rollbackOnly;
	}

	/**
	 * 将本事务标记为已完成，即已提交或已回滚。
	 */
	public void setCompleted() {
		this.completed = true;
	}

	@Override
	public boolean isCompleted() {
		return this.completed;
	}

}
