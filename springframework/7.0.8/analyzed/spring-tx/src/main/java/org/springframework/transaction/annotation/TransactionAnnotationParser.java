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

package org.springframework.transaction.annotation;

import java.lang.reflect.AnnotatedElement;

import org.jspecify.annotations.Nullable;

import org.springframework.transaction.interceptor.TransactionAttribute;

/**
 * 解析已知事务注解类型的策略接口。
 * {@link AnnotationTransactionAttributeSource} 委托此类解析器
 * 以支持特定注解类型，如 Spring 自身的 {@link Transactional}、
 * JTA 1.2 的 {@link jakarta.transaction.Transactional}
 * 或 EJB3 的 {@link jakarta.ejb.TransactionAttribute}。
 *
 * @author Juergen Hoeller
 * @since 2.5
 * @see AnnotationTransactionAttributeSource
 * @see SpringTransactionAnnotationParser
 * @see Ejb3TransactionAnnotationParser
 * @see JtaTransactionAnnotationParser
 */
public interface TransactionAnnotationParser {

	/**
	 * 判断给定类是否为此 {@code TransactionAnnotationParser}
	 * 注解格式下事务属性的候选类。
	 * <p>若返回 {@code false}，则不会遍历该类方法进行
	 * {@code #parseTransactionAnnotation} 内省。
	 * 因此 {@code false} 是对不受影响类的优化，
	 * 而 {@code true} 表示需对该类每个方法逐一完整内省。
	 * @param targetClass 待内省的类
	 * @return 若类在类或方法级别已知无事务注解则 {@code false}，
	 * 否则 {@code true}。默认实现返回 {@code true}，进行常规内省。
	 * @since 5.2
	 */
	default boolean isCandidateClass(Class<?> targetClass) {
		return true;
	}

	/**
	 * 基于本解析器理解的注解类型，解析给定方法或类的事务属性。
	 * <p>本质上是将已知事务注解解析为 Spring 元数据属性类。
	 * 若方法/类非事务性则返回 {@code null}。
	 * <p>返回的属性通常（但不一定）为
	 * {@link org.springframework.transaction.interceptor.RuleBasedTransactionAttribute} 类型。
	 * @param element 带注解的方法或类
	 * @return 配置的事务属性，未找到则 {@code null}
	 * @see AnnotationTransactionAttributeSource#determineTransactionAttribute
	 */
	@Nullable TransactionAttribute parseTransactionAnnotation(AnnotatedElement element);

}
