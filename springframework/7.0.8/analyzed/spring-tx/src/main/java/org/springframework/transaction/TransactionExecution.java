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

/**
 * 当前事务状态的通用表示。
 * 作为 {@link TransactionStatus} 与 {@link ReactiveTransaction} 的基接口，
 * 自 6.1 起也作为 {@link TransactionExecutionListener} 的事务表示。
 *
 * @author Juergen Hoeller
 * @since 5.2
 */
public interface TransactionExecution {

	/**
	 * 返回事务的定义名称（可能为空字符串）。
	 * <p>对于 Spring 声明式事务，暴露的名称默认为
	 * {@code 全限定类名 + "." + 方法名}。
	 * <p>默认实现返回空字符串。
	 * @since 6.1
	 * @see TransactionDefinition#getName()
	 */
	default String getTransactionName() {
		return "";
	}

	/**
	 * 返回是否存在实际活动事务：涵盖新事务及参与现有事务，
	 * 仅当完全未运行于实际事务中时返回 {@code false}。
	 * <p>默认实现返回 {@code true}。
	 * @since 6.1
	 * @see #isNewTransaction()
	 * @see #isNested()
	 * @see #isReadOnly()
	 */
	default boolean hasTransaction() {
		return true;
	}

	/**
	 * 返回事务管理器是否将当前事务视为新事务；
	 * 否则为参与现有事务，或可能根本未运行于实际事务中。
	 * <p>主要用于事务管理器状态处理。
	 * 应用层更宜使用 {@link #hasTransaction()}，语义通常更合适。
	 * <p>"新" 状态可能因事务管理器而异，例如实际嵌套事务返回 {@code true}，
	 * 但若显式暴露保存点管理（如 {@link TransactionStatus}），
	 * 基于保存点的嵌套事务范围可能返回 {@code false}。
	 * {@link #isNested()} 提供对任意嵌套执行的联合检查。
	 * <p>默认实现返回 {@code true}。
	 * @see #hasTransaction()
	 * @see #isNested()
	 * @see TransactionStatus#hasSavepoint()
	 */
	default boolean isNewTransaction() {
		return true;
	}

	/**
	 * 返回本事务是否以嵌套方式在另一事务内执行。
	 * <p>默认实现返回 {@code false}。
	 * @since 6.1
	 * @see #hasTransaction()
	 * @see #isNewTransaction()
	 * @see TransactionDefinition#PROPAGATION_NESTED
	 */
	default boolean isNested() {
		return false;
	}

	/**
	 * 返回本事务是否定义为只读事务。
	 * <p>默认实现返回 {@code false}。
	 * @since 6.1
	 * @see TransactionDefinition#isReadOnly()
	 */
	default boolean isReadOnly() {
		return false;
	}

	/**
	 * 将事务设为仅回滚。通知事务管理器事务唯一可能结果为回滚，
	 * 作为抛出异常触发回滚的替代方式。
	 * <p>默认实现抛出 UnsupportedOperationException。
	 * @see #isRollbackOnly()
	 */
	default void setRollbackOnly() {
		throw new UnsupportedOperationException("setRollbackOnly not supported");
	}

	/**
	 * 返回事务是否已被标记为仅回滚
	 * （由应用或事务基础设施标记）。
	 * <p>默认实现返回 {@code false}。
	 * @see #setRollbackOnly()
	 */
	default boolean isRollbackOnly() {
		return false;
	}

	/**
	 * 返回本事务是否已完成，
	 * 即是否已提交或已回滚。
	 * <p>默认实现返回 {@code false}。
	 */
	default boolean isCompleted() {
		return false;
	}

}
