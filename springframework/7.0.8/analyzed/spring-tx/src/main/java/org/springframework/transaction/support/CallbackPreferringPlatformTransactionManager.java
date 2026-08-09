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

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;

/**
 * {@link org.springframework.transaction.PlatformTransactionManager} 接口的扩展，
 * 暴露在给定回调内于事务中执行的方法。
 *
 * <p>本接口实现者自动表达对回调而非编程式 {@code getTransaction}、
 * {@code commit} 和 {@code rollback} 调用的偏好。
 * 调用代码可检查给定事务管理器是否实现本接口，
 * 以选择准备回调而非显式事务边界控制。
 *
 * <p>Spring 的 {@link TransactionTemplate} 和
 * {@link org.springframework.transaction.interceptor.TransactionInterceptor}
 * 会自动检测并使用本 PlatformTransactionManager 变体。
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see TransactionTemplate
 * @see org.springframework.transaction.interceptor.TransactionInterceptor
 */
public interface CallbackPreferringPlatformTransactionManager extends PlatformTransactionManager {

	/**
	 * 在事务内执行给定回调对象指定的操作。
	 * <p>允许返回事务内创建的结果对象，即领域对象或领域对象集合。
	 * 回调抛出的 RuntimeException 视为强制回滚的致命异常，
	 * 并传播给模板调用方。
	 * @param definition 包装回调的事务定义
	 * @param callback 指定事务操作的回调对象
	 * @return 回调返回的结果对象，无则为 {@code null}
	 * @throws TransactionException 初始化、回滚或系统错误时
	 * @throws RuntimeException 若 TransactionCallback 抛出
	 */
	<T extends @Nullable Object> T execute(@Nullable TransactionDefinition definition, TransactionCallback<T> callback)
			throws TransactionException;

}
