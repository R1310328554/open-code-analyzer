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

import java.io.Flushable;

/**
 * 进行中的 {@link PlatformTransactionManager} 事务的表示。
 * 扩展通用 {@link TransactionExecution} 接口。
 *
 * <p>事务代码可用其获取状态信息，
 * 并以编程方式请求回滚（而非抛出导致隐式回滚的异常）。
 *
 * <p>包含 {@link SavepointManager} 接口以提供保存点管理能力。
 * 注意，仅当底层事务管理器支持时保存点管理才可用。
 *
 * @author Juergen Hoeller
 * @since 27.03.2003
 * @see #setRollbackOnly()
 * @see PlatformTransactionManager#getTransaction
 * @see org.springframework.transaction.support.TransactionCallback#doInTransaction
 * @see org.springframework.transaction.interceptor.TransactionInterceptor#currentTransactionStatus()
 */
public interface TransactionStatus extends TransactionExecution, SavepointManager, Flushable {

	/**
	 * 返回本事务内部是否携带保存点，
	 * 即是否基于保存点创建为嵌套事务。
	 * <p>本方法主要用于诊断，与 {@link #isNewTransaction()} 配合使用。
	 * 编程式处理自定义保存点请使用 {@link SavepointManager} 提供的操作。
	 * <p>默认实现返回 {@code false}。
	 * @see #isNewTransaction()
	 * @see #createSavepoint()
	 * @see #rollbackToSavepoint(Object)
	 * @see #releaseSavepoint(Object)
	 */
	default boolean hasSavepoint() {
		return false;
	}

	/**
	 * 若适用，将底层会话刷新到数据存储：
	 * 例如所有受影响的 Hibernate/JPA 会话。
	 * <p>这实际上只是提示；若底层事务管理器无 flush 概念可能为空操作。
	 * flush 信号可能应用于主资源或事务同步，取决于底层资源。
	 * <p>默认实现为空，将 flush 视为空操作。
	 */
	@Override
	default void flush() {
	}

}
