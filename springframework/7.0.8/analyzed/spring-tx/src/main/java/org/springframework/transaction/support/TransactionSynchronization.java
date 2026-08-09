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

import java.io.Flushable;

import org.springframework.core.Ordered;

/**
 * 事务同步回调接口。
 * 由 AbstractPlatformTransactionManager 支持。
 *
 * <p>TransactionSynchronization 实现可实现 Ordered 接口
 * 以影响执行顺序。未实现 Ordered 的同步会追加到同步链末尾。
 *
 * <p>Spring 自身执行的系统同步使用特定顺序值，
 * 必要时可精细控制其执行顺序。
 *
 * <p>实现 {@link Ordered} 接口，以便声明式控制同步执行顺序。
 * 默认 {@link #getOrder() order} 为 {@link Ordered#LOWEST_PRECEDENCE}，
 * 表示较晚执行；返回更小值可更早执行。
 *
 * @author Juergen Hoeller
 * @since 02.06.2003
 * @see TransactionSynchronizationManager
 * @see AbstractPlatformTransactionManager
 * @see org.springframework.jdbc.datasource.DataSourceUtils#CONNECTION_SYNCHRONIZATION_ORDER
 */
public interface TransactionSynchronization extends Ordered, Flushable {

	/** 正常提交时的完成状态。 */
	int STATUS_COMMITTED = 0;

	/** 正常回滚时的完成状态。 */
	int STATUS_ROLLED_BACK = 1;

	/** 启发式混合完成或系统错误时的完成状态。 */
	int STATUS_UNKNOWN = 2;


	/**
	 * 返回此事务同步的执行顺序。
	 * <p>默认为 {@link Ordered#LOWEST_PRECEDENCE}。
	 */
	@Override
	default int getOrder() {
		return Ordered.LOWEST_PRECEDENCE;
	}

	/**
	 * 挂起此同步。
	 * 若管理资源，应将其从 TransactionSynchronizationManager 解绑。
	 * @see TransactionSynchronizationManager#unbindResource
	 */
	default void suspend() {
	}

	/**
	 * 恢复此同步。
	 * 若管理资源，应将其重新绑定到 TransactionSynchronizationManager。
	 * @see TransactionSynchronizationManager#bindResource
	 */
	default void resume() {
	}

	/**
	 * 若适用，将底层 Session flush 到数据存储：
	 * 例如 Hibernate/JPA Session。
	 * @see org.springframework.transaction.TransactionStatus#flush()
	 */
	@Override
	default void flush() {
	}

	/**
	 * 在创建新保存点时调用，
	 * 既可能是在现有事务上启动嵌套事务，
	 * 也可能是通过 {@link org.springframework.transaction.TransactionStatus} 编程式创建保存点。
	 * <p>此同步回调在资源保存点创建<i>之后</i>立即调用，
	 * 此时给定保存点对象已生效。
	 * @param savepoint 关联的保存点对象（主要用作标识保存点的键，
	 * 也可转型为资源保存点类型）
	 * @since 6.2
	 * @see org.springframework.transaction.SavepointManager#createSavepoint
	 * @see org.springframework.transaction.TransactionDefinition#PROPAGATION_NESTED
	 */
	default void savepoint(Object savepoint) {
	}

	/**
	 * 回滚到先前创建的保存点时调用。
	 * <p>此同步回调在资源保存点回滚<i>之前</i>立即调用，
	 * 此时给定保存点对象仍有效。
	 * @param savepoint 关联的保存点对象（主要用作标识保存点的键，
	 * 也可转型为资源保存点类型）
	 * @since 6.2
	 * @see #savepoint
	 * @see org.springframework.transaction.SavepointManager#rollbackToSavepoint
	 */
	default void savepointRollback(Object savepoint) {
	}

	/**
	 * 在事务提交前调用（在 "beforeCompletion" 之前）。
	 * 例如可将事务性 O/R Mapping Session flush 到数据库。
	 * <p>此回调<i>并不</i>表示事务一定会提交。
	 * 调用此方法后仍可能决定回滚。此回调旨在执行
	 * 仅在仍有可能提交时才有意义的工作，
	 * 例如将 SQL 语句 flush 到数据库。
	 * <p>注意：异常会传播给提交调用方并导致事务回滚。
	 * @param readOnly 事务是否定义为只读事务
	 * @throws RuntimeException 出错时；将<b>传播给调用方</b>
	 * （注意：此处不要抛出 TransactionException 子类！）
	 * @see #beforeCompletion
	 */
	default void beforeCommit(boolean readOnly) {
	}

	/**
	 * 在事务提交/回滚前调用。
	 * 可在事务完成<i>之前</i>执行资源清理。
	 * <p>即使 {@code beforeCommit} 抛出异常，
	 * 此方法仍会在其之后调用。此回调允许在事务完成前
	 * 关闭资源，无论最终结果如何。
	 * @throws RuntimeException 出错时；将<b>记录日志但不传播</b>
	 * （注意：此处不要抛出 TransactionException 子类！）
	 * @see #beforeCommit
	 * @see #afterCompletion
	 */
	default void beforeCompletion() {
	}

	/**
	 * 在事务提交后调用。可在主事务<i>成功</i>提交<i>之后</i>立即执行进一步操作。
	 * <p>例如可提交主事务成功提交后应执行的后续操作，
	 * 如确认消息或邮件。
	 * <p><b>注意：</b>事务已提交，但事务资源可能仍活跃且可访问。
	 * 因此，此时触发的任何数据访问代码仍会「参与」原始事务，
	 * 允许执行一些清理（之后不再有提交！），
	 * 除非它显式声明需要在独立事务中运行。
	 * 因此：<b>从此处调用的任何事务操作请使用 {@code PROPAGATION_REQUIRES_NEW}。</b>
	 * @throws RuntimeException 出错时；将<b>传播给调用方</b>
	 * （注意：此处不要抛出 TransactionException 子类！）
	 */
	default void afterCommit() {
	}

	/**
	 * 在事务提交/回滚后调用。
	 * 可在事务完成<i>之后</i>执行资源清理。
	 * <p><b>注意：</b>事务已提交或回滚，但事务资源可能仍活跃且可访问。
	 * 因此，此时触发的任何数据访问代码仍会「参与」原始事务，
	 * 允许执行一些清理（之后不再有提交！），
	 * 除非它显式声明需要在独立事务中运行。
	 * 因此：<b>从此处调用的任何事务操作请使用 {@code PROPAGATION_REQUIRES_NEW}。</b>
	 * @param status 根据 {@code STATUS_*} 常量表示的完成状态
	 * @throws RuntimeException 出错时；将<b>记录日志但不传播</b>
	 * （注意：此处不要抛出 TransactionException 子类！）
	 * @see #STATUS_COMMITTED
	 * @see #STATUS_ROLLED_BACK
	 * @see #STATUS_UNKNOWN
	 * @see #beforeCompletion
	 */
	default void afterCompletion(int status) {
	}

}
