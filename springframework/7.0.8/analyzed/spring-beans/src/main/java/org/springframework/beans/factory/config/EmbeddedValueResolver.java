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

import org.springframework.util.StringValueResolver;

/**
 * 针对 {@link ConfigurableBeanFactory} 解析占位符与表达式的
 * {@link StringValueResolver} 适配器。
 *
 * <p>注意，与 {@link ConfigurableBeanFactory#resolveEmbeddedValue} 方法不同，
 * 本适配器也会解析表达式。所使用的 {@link BeanExpressionContext} 面向普通 Bean 工厂，
 * 未为任何可访问的上下文对象指定作用域。
 *
 * @author Juergen Hoeller
 * @since 4.3
 * @see ConfigurableBeanFactory#resolveEmbeddedValue(String)
 * @see ConfigurableBeanFactory#getBeanExpressionResolver()
 * @see BeanExpressionContext
 */
public class EmbeddedValueResolver implements StringValueResolver {

	/** Bean 表达式上下文。 */
	private final BeanExpressionContext exprContext;

	/** Bean 表达式解析器，可能为 {@code null}。 */
	private final @Nullable BeanExpressionResolver exprResolver;


	public EmbeddedValueResolver(ConfigurableBeanFactory beanFactory) {
		this.exprContext = new BeanExpressionContext(beanFactory, null);
		this.exprResolver = beanFactory.getBeanExpressionResolver();
	}


	@Override
	public @Nullable String resolveStringValue(String strVal) {
		// 先解析嵌入式占位符
		String value = this.exprContext.getBeanFactory().resolveEmbeddedValue(strVal);
		// 若配置了表达式解析器，则继续求值
		if (this.exprResolver != null && value != null) {
			Object evaluated = this.exprResolver.evaluate(value, this.exprContext);
			value = (evaluated != null ? evaluated.toString() : null);
		}
		return value;
	}

}
