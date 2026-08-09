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

package org.springframework.transaction.interceptor;

import java.lang.reflect.Method;

import org.jspecify.annotations.Nullable;

/**
 * {@link TransactionInterceptor} 用于获取元数据的策略接口。
 *
 * <p>实现类知道如何获取事务属性，无论来自配置、
 * 源码级元数据属性（如注解）或其他来源。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 15.04.2003
 * @see TransactionInterceptor#setTransactionAttributeSource
 * @see TransactionProxyFactoryBean#setTransactionAttributeSource
 * @see org.springframework.transaction.annotation.AnnotationTransactionAttributeSource
 */
public interface TransactionAttributeSource {

	/**
	 * 判断给定类是否为本 {@code TransactionAttributeSource} 元数据格式下
	 * 事务属性的候选类。
	 * <p>若返回 {@code false}，给定类上的方法将不会为
	 * {@link #getTransactionAttribute} 内省而遍历。
	 * 因此对不受影响类返回 {@code false} 是一种优化，
	 * 而 {@code true} 仅表示需要对该类每个方法逐一完整内省。
	 * @param targetClass 要内省的类
	 * @return 若已知类在类或方法级别无事务属性则为 {@code false}，否则为 {@code true}。
	 * 默认实现返回 {@code true}，进行常规内省。
	 * @since 5.2
	 * @see #hasTransactionAttribute
	 */
	default boolean isCandidateClass(Class<?> targetClass) {
		return true;
	}

	/**
	 * 判断给定方法是否存在事务属性。
	 * @param method 要内省的方法
	 * @param targetClass 目标类（可为 {@code null}，此时须使用方法声明类）
	 * @since 6.2
	 * @see #getTransactionAttribute
	 */
	default boolean hasTransactionAttribute(Method method, @Nullable Class<?> targetClass) {
		return (getTransactionAttribute(method, targetClass) != null);
	}

	/**
	 * 返回给定方法的事务属性，若方法非事务性则为 {@code null}。
	 * @param method 要内省的方法
	 * @param targetClass 目标类（可为 {@code null}，此时须使用方法声明类）
	 * @return 匹配的事务属性，未找到则为 {@code null}
	 */
	@Nullable TransactionAttribute getTransactionAttribute(Method method, @Nullable Class<?> targetClass);

}
