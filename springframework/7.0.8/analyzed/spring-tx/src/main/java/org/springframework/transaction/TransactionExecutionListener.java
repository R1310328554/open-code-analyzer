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

package org.springframework.transaction;

import org.jspecify.annotations.Nullable;

/**
 * 无状态监听事务管理器中事务创建/完成步骤的回调接口。
 * 主要用于观测与统计；资源管理请考虑有状态事务同步。
 *
 * <p>与同步机制不同，事务执行监听器契约通常同时支持
 * 线程绑定事务与响应式事务。
 * 回调提供的 {@link TransactionExecution} 对象或为
 * {@link TransactionStatus}（{@link PlatformTransactionManager} 事务），
 * 或为 {@link ReactiveTransaction}（{@link ReactiveTransactionManager} 事务）。
 *
 * @author Juergen Hoeller
 * @since 6.1
 * @see ConfigurableTransactionManager#addListener
 * @see org.springframework.transaction.support.TransactionSynchronizationManager#registerSynchronization
 * @see org.springframework.transaction.reactive.TransactionSynchronizationManager#registerSynchronization
 */
public interface TransactionExecutionListener {

	/**
	 * 事务 begin 步骤之前的回调。
	 * @param transaction 当前事务
	 */
	default void beforeBegin(TransactionExecution transaction) {
	}

	/**
	 * 事务 begin 步骤之后的回调。
	 * @param transaction 当前事务
	 * @param beginFailure begin 期间发生的异常
	 * （成功 begin 后为 {@code null}）
	 */
	default void afterBegin(TransactionExecution transaction, @Nullable Throwable beginFailure) {
	}

	/**
	 * 事务 commit 步骤之前的回调。
	 * @param transaction 当前事务
	 */
	default void beforeCommit(TransactionExecution transaction) {
	}

	/**
	 * 事务 commit 步骤之后的回调。
	 * @param transaction 当前事务
	 * @param commitFailure commit 期间发生的异常
	 * （成功 commit 后为 {@code null}）
	 */
	default void afterCommit(TransactionExecution transaction, @Nullable Throwable commitFailure) {
	}

	/**
	 * 事务 rollback 步骤之前的回调。
	 * @param transaction 当前事务
	 */
	default void beforeRollback(TransactionExecution transaction) {
	}

	/**
	 * 事务 rollback 步骤之后的回调。
	 * @param transaction 当前事务
	 * @param rollbackFailure rollback 期间发生的异常
	 * （成功 rollback 后为 {@code null}）
	 */
	default void afterRollback(TransactionExecution transaction, @Nullable Throwable rollbackFailure) {
	}

}
