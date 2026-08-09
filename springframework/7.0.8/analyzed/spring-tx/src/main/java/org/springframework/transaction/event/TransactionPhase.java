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

package org.springframework.transaction.event;

import java.util.function.Consumer;

import org.springframework.transaction.support.TransactionSynchronization;

/**
 * 事务事件监听器适用的事务阶段。
 *
 * @author Stephane Nicoll
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 4.2
 * @see TransactionalEventListener#phase()
 * @see TransactionalApplicationListener#getTransactionPhase()
 * @see TransactionalApplicationListener#forPayload(TransactionPhase, Consumer)
 */
public enum TransactionPhase {

	/**
	 * 在事务提交前处理事件。
	 * @see TransactionSynchronization#beforeCommit(boolean)
	 */
	BEFORE_COMMIT,

	/**
	 * 在提交成功完成后处理事件。
	 * <p>注意：这是 {@link #AFTER_COMPLETION} 的特化，因此
	 * 与 {@code AFTER_COMPLETION} 在同一事件序列中执行
	 * （而非在 {@link TransactionSynchronization#afterCommit()} 中）。
	 * <p>此阶段与底层事务资源的交互不会被提交。详见
	 * {@link TransactionSynchronization#afterCompletion(int)}。
	 * @see TransactionSynchronization#afterCompletion(int)
	 * @see TransactionSynchronization#STATUS_COMMITTED
	 */
	AFTER_COMMIT,

	/**
	 * 若事务已回滚则处理事件。
	 * <p>注意：这是 {@link #AFTER_COMPLETION} 的特化，因此
	 * 与 {@code AFTER_COMPLETION} 在同一事件序列中执行。
	 * <p>此阶段与底层事务资源的交互不会被提交。详见
	 * {@link TransactionSynchronization#afterCompletion(int)}。
	 * @see TransactionSynchronization#afterCompletion(int)
	 * @see TransactionSynchronization#STATUS_ROLLED_BACK
	 */
	AFTER_ROLLBACK,

	/**
	 * 在事务完成后处理事件。
	 * <p>若需更细粒度的事件，分别使用 {@link #AFTER_COMMIT} 或
	 * {@link #AFTER_ROLLBACK} 拦截事务提交或回滚。
	 * <p>此阶段与底层事务资源的交互不会被提交。详见
	 * {@link TransactionSynchronization#afterCompletion(int)}。
	 * @see TransactionSynchronization#afterCompletion(int)
	 */
	AFTER_COMPLETION

}
