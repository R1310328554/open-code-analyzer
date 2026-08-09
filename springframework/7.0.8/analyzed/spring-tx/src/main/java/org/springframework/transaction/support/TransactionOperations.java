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

import java.util.function.Consumer;

import org.jspecify.annotations.Nullable;

import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;

/**
 * 指定基本事务执行操作的接口。
 * 由 {@link TransactionTemplate} 实现。不常直接使用，
 * 但有助于提升可测试性，因为可轻松 mock 或 stub。
 *
 * @author Juergen Hoeller
 * @since 2.0.4
 */
public interface TransactionOperations {

	/**
	 * 在事务中执行给定回调对象指定操作。
	 * <p>允许返回在事务内创建的结果对象，
	 * 即领域对象或领域对象集合。回调抛出的 RuntimeException
	 * 视为强制回滚的致命异常，并传播给模板调用方。
	 * @param action 指定事务操作的回调对象
	 * @return 回调返回的结果对象，若无则 {@code null}
	 * @throws TransactionException 初始化、回滚或系统错误时
	 * @throws RuntimeException 若 TransactionCallback 抛出
	 * @see #executeWithoutResult(Consumer)
	 */
	<T extends @Nullable Object> T execute(TransactionCallback<T> action) throws TransactionException;

	/**
	 * 在事务中执行给定 {@link Runnable} 指定的操作。
	 * <p>若需从回调返回值或在回调内访问
	 * {@link org.springframework.transaction.TransactionStatus}，
	 * 请改用 {@link #execute(TransactionCallback)}。
	 * <p>此变体类似使用 {@link TransactionCallbackWithoutResult}，
	 * 但针对常见场景简化签名，便于配合 lambda 表达式。
	 * @param action 指定事务操作的 Runnable
	 * @throws TransactionException 初始化、回滚或系统错误时
	 * @throws RuntimeException 若 Runnable 抛出
	 * @since 5.2
	 * @see #execute(TransactionCallback)
	 * @see TransactionCallbackWithoutResult
	 */
	default void executeWithoutResult(Consumer<TransactionStatus> action) throws TransactionException {
		this.<@Nullable Object> execute(status -> {
			action.accept(status);
			return null;
		});
	}


	/**
	 * 返回 {@code TransactionOperations} 接口的实现，
	 * 在无实际事务的情况下执行给定 {@link TransactionCallback}。
	 * <p>适用于测试：行为等价于使用无实际事务
	 * （PROPAGATION_SUPPORTS）且无同步（SYNCHRONIZATION_NEVER）的事务管理器。
	 * <p>若需带实际事务处理的 {@link TransactionOperations} 实现，
	 * 请使用 {@link TransactionTemplate} 配合适当的
	 * {@link org.springframework.transaction.PlatformTransactionManager}。
	 * @since 5.2
	 * @see org.springframework.transaction.TransactionDefinition#PROPAGATION_SUPPORTS
	 * @see AbstractPlatformTransactionManager#SYNCHRONIZATION_NEVER
	 * @see TransactionTemplate
	 */
	static TransactionOperations withoutTransaction() {
		return WithoutTransactionOperations.INSTANCE;
	}

}
