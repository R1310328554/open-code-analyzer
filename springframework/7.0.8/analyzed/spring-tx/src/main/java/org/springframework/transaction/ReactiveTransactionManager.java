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
import reactor.core.publisher.Mono;

/**
 * Spring 响应式事务基础设施的核心接口。
 * 应用可直接使用，但主要并非作为 API 设计：
 * 通常应用通过事务操作符或 AOP 声明式事务划分工作。
 *
 * @author Mark Paluch
 * @author Juergen Hoeller
 * @since 5.2
 * @see org.springframework.transaction.reactive.TransactionalOperator
 * @see org.springframework.transaction.interceptor.TransactionInterceptor
 * @see org.springframework.transaction.PlatformTransactionManager
 * @see ConfigurableTransactionManager
 */
public interface ReactiveTransactionManager extends TransactionManager {

	/**
	 * 根据指定传播行为发出当前活动响应式事务或创建新事务。
	 * <p>注意，隔离级别、超时等参数仅应用于新事务，
	 * 参与现有活动事务时将被忽略。
	 * <p>此外，并非所有事务定义设置都被每个事务管理器支持：
	 * 合适实现遇到不支持的设置时应抛出异常。
	 * <p>上述规则的例外是只读标志：若不支持显式只读模式应被忽略。
	 * 本质上只读标志只是潜在优化的提示。
	 * <p>注意：与 {@link PlatformTransactionManager} 不同，
	 * 异常通过本方法返回的响应式管道传播。
	 * @param definition TransactionDefinition 实例，
	 * 描述传播行为、隔离级别、超时等
	 * @return 表示新事务或当前事务的状态对象
	 * @throws TransactionException 查找、创建或系统错误时
	 * @throws IllegalTransactionStateException 给定事务定义无法执行时
	 * （例如当前活动事务与指定传播行为冲突）
	 * @see TransactionDefinition#getPropagationBehavior
	 * @see TransactionDefinition#getIsolationLevel
	 * @see TransactionDefinition#getTimeout
	 * @see TransactionDefinition#isReadOnly
	 */
	Mono<ReactiveTransaction> getReactiveTransaction(@Nullable TransactionDefinition definition);

	/**
	 * 根据状态提交给定事务。若事务已被编程式标记为仅回滚，则执行回滚。
	 * <p>若非新事务，为正确参与外围事务则省略 commit。
	 * 若先前事务被挂起以创建新事务，
	 * 提交新事务后恢复先前事务。
	 * <p>注意，commit 调用完成时（无论正常还是传播异常），
	 * 事务必须已完全完成并清理。此时不应再期望 rollback 调用。
	 * <p>注意：与 {@link PlatformTransactionManager} 不同，
	 * 异常通过本方法返回的响应式管道传播。
	 * 此外，取决于事务管理器实现，{@code commit} 也可能传播
	 * {@link org.springframework.dao.DataAccessException}。
	 * @param transaction {@code getTransaction} 方法返回的对象
	 * @throws UnexpectedRollbackException 事务协调器发起意外回滚时
	 * @throws HeuristicCompletionException 事务协调器启发式决策导致失败时
	 * @throws TransactionSystemException commit 或系统错误时
	 * （通常由根本性资源失败引起）
	 * @throws IllegalTransactionStateException 给定事务已完成
	 * （已提交或已回滚）时
	 * @see ReactiveTransaction#setRollbackOnly
	 */
	Mono<Void> commit(ReactiveTransaction transaction);

	/**
	 * 对给定事务执行回滚。
	 * <p>若非新事务，为正确参与外围事务仅将其设为仅回滚。
	 * 若先前事务被挂起以创建新事务，
	 * 回滚新事务后恢复先前事务。
	 * <p><b>若 commit 失败，请勿对事务调用 rollback。</b>
	 * commit 返回时事务已完全完成并清理，即使 commit 异常亦然。
	 * 因此 commit 失败后调用 rollback 将导致 IllegalTransactionStateException。
	 * <p>注意：与 {@link PlatformTransactionManager} 不同，
	 * 异常通过本方法返回的响应式管道传播。
	 * 此外，取决于事务管理器实现，{@code rollback} 也可能传播
	 * {@link org.springframework.dao.DataAccessException}。
	 * @param transaction {@code getTransaction} 方法返回的对象
	 * @throws TransactionSystemException rollback 或系统错误时
	 * （通常由根本性资源失败引起）
	 * @throws IllegalTransactionStateException 给定事务已完成
	 * （已提交或已回滚）时
	 */
	Mono<Void> rollback(ReactiveTransaction transaction);

}
