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
 * 表达式切入点的抽象超类，提供位置和表达式属性。
 * @author Rod Johnson
 * @author Rob Harrop
 * @since 2.0
 * @see #setLocation
 * @see #setExpression
 */
@SuppressWarnings("serial")
public abstract class AbstractExpressionPointcut implements ExpressionPointcut, Serializable {

	/** `location`：该类的成员状态。 */
	private @Nullable String location;

	/** `expression`：该类的成员状态。 */
	private @Nullable String expression;


	/**
	 * 设置调试位置。
	 */
	public void setLocation(@Nullable String location) {
		this.location = location;
	}

	/**
	 * 返回有关切入点表达式的位置信息（如果可用）。这在调试时很有用。
	 * @return 作为人类可读字符串的信息，或 {@code null}（如果没有可用的）
	 */
	public @Nullable String getLocation() {
		return this.location;
	}

	/**
	 * 设置 Expression（`Expression`）。
	 */
	public void setExpression(@Nullable String expression) {
		this.expression = expression;
		try {
			onSetExpression(expression);
		}
		catch (IllegalArgumentException ex) {
			// 如果可能，请填写位置信息。
			if (this.location != null) {
				throw new IllegalArgumentException("Invalid expression at location [" + this.location + "]: " + ex);
			}
			else {
				throw ex;
			}
		}
	}

	/**
	 * 当设置新的切入点表达式时调用。如果可能的话，应该在此时解析表达式。 <p>这个实现是空的。
	 * @param expression 要设置的表达式
	 * @throws IllegalArgumentException 如果表达式无效
	 * @see #setExpression
	 */
	protected void onSetExpression(@Nullable String expression) throws IllegalArgumentException {
	}

	/**
	 * 返回此切入点的表达式。
	 */
	@Override
	public @Nullable String getExpression() {
		return this.expression;
	}

}
