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

package org.springframework.beans.factory.config;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.BeansException;

/**
 * 通过将值作为表达式求值来解析值的策略接口（在适用时）。
 *
 * <p>原始的 {@link org.springframework.beans.factory.BeanFactory} 不包含
 * 此策略的默认实现。但 {@link org.springframework.context.ApplicationContext}
 * 实现会开箱即用地提供表达式支持。
 *
 * @author Juergen Hoeller
 * @since 3.0
 */
public interface BeanExpressionResolver {

	/**
	 * 在适用时将给定值作为表达式求值；否则原样返回值。
	 * @param value 要求值的值
	 * @param beanExpressionContext 求值表达式时使用的 bean 表达式上下文
	 * @return 解析后的值（可能原样返回给定值）
	 * @throws BeansException 如果求值失败
	 */
	@Nullable Object evaluate(@Nullable String value, BeanExpressionContext beanExpressionContext) throws BeansException;

}
