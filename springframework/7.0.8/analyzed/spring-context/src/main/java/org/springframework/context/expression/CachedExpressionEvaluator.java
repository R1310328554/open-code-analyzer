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

package org.springframework.context.expression;

import java.util.Map;

import org.jspecify.annotations.Nullable;

import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.util.Assert;

/**
 * 用于求值并缓存定义在 {@link java.lang.reflect.AnnotatedElement AnnotatedElement}
 * 上的 SpEL 表达式的共享工具类。
 *
 * @author Stephane Nicoll
 * @since 4.2
 * @see AnnotatedElementKey
 */
public abstract class CachedExpressionEvaluator {

	private final SpelExpressionParser parser;


	/**
	 * 使用默认的 {@link SpelExpressionParser} 创建新实例。
	 */
	protected CachedExpressionEvaluator() {
		this(new SpelExpressionParser());
	}

	/**
	 * 使用指定的 {@link SpelExpressionParser} 创建新实例。
	 */
	protected CachedExpressionEvaluator(SpelExpressionParser parser) {
		Assert.notNull(parser, "SpelExpressionParser must not be null");
		this.parser = parser;
	}


	/**
	 * 返回要使用的 {@link SpelExpressionParser}。
	 */
	protected SpelExpressionParser getParser() {
		return this.parser;
	}

	/**
	 * 返回在内部缓存数据的共享参数名发现器。
	 * @since 4.3
	 */
	protected ParameterNameDiscoverer getParameterNameDiscoverer() {
		return DefaultParameterNameDiscoverer.getSharedInstance();
	}

	/**
	 * 返回指定 SpEL 表达式对应的已解析 {@link Expression}。
	 * <p>若表达式尚未解析并缓存，则{@linkplain #parseExpression(String) 解析}之。
	 * @param cache 要使用的缓存
	 * @param elementKey 包含表达式定义元素的 {@code AnnotatedElementKey}
	 * @param expression 要解析的表达式
	 */
	protected Expression getExpression(Map<ExpressionKey, Expression> cache,
			AnnotatedElementKey elementKey, String expression) {

		ExpressionKey expressionKey = createKey(elementKey, expression);
		return cache.computeIfAbsent(expressionKey, key -> parseExpression(expression));
	}

	/**
	 * 解析指定的 {@code expression}。
	 * @param expression 要解析的表达式
	 * @since 5.3.13
	 */
	protected Expression parseExpression(String expression) {
		return getParser().parseExpression(expression);
	}

	private ExpressionKey createKey(AnnotatedElementKey elementKey, String expression) {
		return new ExpressionKey(elementKey, expression);
	}


	/**
	 * 表达式缓存键。
	 */
	protected static class ExpressionKey implements Comparable<ExpressionKey> {

		private final AnnotatedElementKey element;

		private final String expression;

		protected ExpressionKey(AnnotatedElementKey element, String expression) {
			Assert.notNull(element, "AnnotatedElementKey must not be null");
			Assert.notNull(expression, "Expression must not be null");
			this.element = element;
			this.expression = expression;
		}

		@Override
		public boolean equals(@Nullable Object other) {
			return (this == other || (other instanceof ExpressionKey that &&
					this.element.equals(that.element) && this.expression.equals(that.expression)));
		}

		@Override
		public int hashCode() {
			return this.element.hashCode() * 29 + this.expression.hashCode();
		}

		@Override
		public String toString() {
			return this.element + " with expression \"" + this.expression + "\"";
		}

		@Override
		public int compareTo(ExpressionKey other) {
			int result = this.element.toString().compareTo(other.element.toString());
			if (result == 0) {
				result = this.expression.compareTo(other.expression);
			}
			return result;
		}
	}

}
