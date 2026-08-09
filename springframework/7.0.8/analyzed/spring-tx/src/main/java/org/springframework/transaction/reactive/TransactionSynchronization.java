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

import reactor.core.publisher.Mono;

/**
 * 响应式事务同步回调接口。
 * 由 {@link AbstractReactiveTransactionManager} 支持。
 *
 * <p>TransactionSynchronization 实现可实现 {@link org.springframework.core.Ordered} 接口
 * 以影响执行顺序。未实现 {@link org.springframework.core.Ordered} 接口的同步
 * 将追加到同步链末尾。
 *
 * <p>Spring 自身执行的系统同步使用特定顺序值，
 * 必要时可精细控制其执行顺序。
 *
 * @author Mark Paluch
 * @author Juergen Hoeller
 * @since 5.2
 * @see TransactionSynchronizationManager
 * @see AbstractReactiveTransactionManager
 */
public interface TransactionSynchronization {

	/** 正常提交时的完成状态。 */
	int STATUS_COMMITTED = 0;

	/** 正常回滚时的完成状态。 */
	int STATUS_ROLLED_BACK = 1;

	/** 启发式混合完成或系统错误时的完成状态。 */
	int STATUS_UNKNOWN = 2;


	/**
	 * 挂起本同步。
	 * 若管理资源，应从 TransactionSynchronizationManager 解绑资源。
	 * @see TransactionSynchronizationManager#unbindResource
	 */
	default Mono<Void> suspend() {
		return Mono.empty();
	}

	/**
	 * 恢复本同步。
	 * 若管理资源，应重新绑定资源到 TransactionSynchronizationManager。
	 * @see TransactionSynchronizationManager#bindResource
	 */
	default Mono<Void> resume() {
		return Mono.empty();
	}

	/**
	 * 在事务提交前调用（在 "beforeCompletion" 之前）。
	 * <p>本回调<i>不</i>表示事务一定会提交。
	 * 调用本方法后仍可能决定回滚。本回调用于执行仅在仍可能提交时
	 * 才有意义的工作，例如将 SQL 语句 flush 到数据库。
	 * <p>注意，异常将传播给提交调用方并导致事务回滚。
	 * @param readOnly 事务是否定义为只读
	 * @throws RuntimeException 发生错误时；将<b>传播给调用方</b>
	 * （注意：不要在此抛出 TransactionException 子类！）
	 * @see #beforeCompletion
	 */
	default Mono<Void> beforeCommit(boolean readOnly) {
		return Mono.empty();
	}

	/**
	 * 在事务提交/回滚前调用。
	 * 可在事务完成<i>前</i>执行资源清理。
	 * <p>即使 {@code beforeCommit} 抛出异常，本方法也会在 {@code beforeCommit} 之后调用。
	 * 本回调允许在任意结果下于事务完成前关闭资源。
	 * @throws RuntimeException 发生错误时；将<b>记录但不传播</b>
	 * （注意：不要在此抛出 TransactionException 子类！）
	 * @see #beforeCommit
	 * @see #afterCompletion
	 */
	default Mono<Void> beforeCompletion() {
		return Mono.empty();
	}

	/**
	 * 在事务提交后调用。可在主事务<i>成功</i>提交<i>后</i>立即执行进一步操作。
	 * <p>例如，可提交主事务成功提交后应执行的后续操作，如确认消息或邮件。
	 * <p><b>注意：</b>事务已提交，但事务资源可能仍活动且可访问。
	 * 因此，此触发的任何数据访问代码仍将 "参与" 原事务，
	 * 允许执行一些清理（之后不再有提交！），除非显式声明需在独立事务中运行。
	 * 因此：<b>从此处调用的任何事务操作请使用 {@code PROPAGATION_REQUIRES_NEW}。</b>
	 * @throws RuntimeException 发生错误时；将<b>传播给调用方</b>
	 * （注意：不要在此抛出 TransactionException 子类！）
	 */
	default Mono<Void> afterCommit() {
		return Mono.empty();
	}

	/**
	 * 在事务提交/回滚后调用。
	 * 可在事务完成<i>后</i>执行资源清理。
	 * <p><b>注意：</b>事务已提交或回滚，但事务资源可能仍活动且可访问。
	 * 因此，此触发的任何数据访问代码仍将 "参与" 原事务，
	 * 允许执行一些清理（之后不再有提交！），除非显式声明需在独立事务中运行。
	 * 因此：<b>从此处调用的任何事务操作请使用 {@code PROPAGATION_REQUIRES_NEW}。</b>
	 * @param status 根据 {@code STATUS_*} 常量的完成状态
	 * @throws RuntimeException 发生错误时；将<b>记录但不传播</b>
	 * （注意：不要在此抛出 TransactionException 子类！）
	 * @see #STATUS_COMMITTED
	 * @see #STATUS_ROLLED_BACK
	 * @see #STATUS_UNKNOWN
	 * @see #beforeCompletion
	 */
	default Mono<Void> afterCompletion(int status) {
		return Mono.empty();
	}

}
