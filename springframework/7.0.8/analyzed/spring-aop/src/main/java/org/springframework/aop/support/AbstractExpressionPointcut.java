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

package org.springframework.aop.support;

import java.io.Serializable;

import org.jspecify.annotations.Nullable;

/**
 * 表达式切入点的抽象超类，
 * 提供 location 与 expression 属性。
 *
 * @author Rod Johnson
 * @author Rob Harrop
 * @since 2.0
 * @see #setLocation
 * @see #setExpression
 */
@SuppressWarnings("serial")
public abstract class AbstractExpressionPointcut implements ExpressionPointcut, Serializable {

	private @Nullable String location;

	private @Nullable String expression;


	/**
	 * 设置用于调试的 location。
	 */
	public void setLocation(@Nullable String location) {
		this.location = location;
	}

	/**
	 * 返回切入点表达式的 location 信息（若有）。
	 * 便于调试。
	 * @return 可读的 location 字符串，
	 * 若无则返回 {@code null}
	 */
	public @Nullable String getLocation() {
		return this.location;
	}

	public void setExpression(@Nullable String expression) {
		this.expression = expression;
		try {
			onSetExpression(expression);
		}
		catch (IllegalArgumentException ex) {
			// 尽可能补充 location 信息。
			if (this.location != null) {
				throw new IllegalArgumentException("Invalid expression at location [" + this.location + "]: " + ex);
			}
			else {
				throw ex;
			}
		}
	}

	/**
	 * 设置新切入点表达式时调用。
	 * 若可能，应在此解析表达式。
	 * <p>本实现为空。
	 * @param expression 要设置的表达式
	 * @throws IllegalArgumentException 若表达式无效
	 * @see #setExpression
	 */
	protected void onSetExpression(@Nullable String expression) throws IllegalArgumentException {
	}

	/**
	 * 返回本切入点的表达式。
	 */
	@Override
	public @Nullable String getExpression() {
		return this.expression;
	}

}
