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

package org.springframework.aop.aspectj;

import org.aopalliance.aop.Advice;
import org.jspecify.annotations.Nullable;

import org.springframework.aop.Pointcut;
import org.springframework.aop.PointcutAdvisor;
import org.springframework.core.Ordered;
import org.springframework.util.Assert;

/**
 * {@code AspectJPointcutAdvisor} 将 {@link AbstractAspectJAdvice} 适配为 {@link
 * PointcutAdvisor} 接口。
 * @author Adrian Colyer
 * @author Juergen Hoeller
 * @since 2.0
 */
public class AspectJPointcutAdvisor implements PointcutAdvisor, Ordered {

	/** 通知相关状态（`advice`）。 */
	private final AbstractAspectJAdvice advice;

	/** 切点相关状态（`pointcut`）。 */
	private final Pointcut pointcut;

	/** `order`：该类的成员状态。 */
	private @Nullable Integer order;


	/**
	 * 为给定的建议创建一个新的 AspectJPointcutAdvisor。
	 * @param advice 要包装的 AbstractAspectJAdvice
	 */
	public AspectJPointcutAdvisor(AbstractAspectJAdvice advice) {
		Assert.notNull(advice, "Advice must not be null");
		this.advice = advice;
		this.pointcut = advice.buildSafePointcut();
	}


	/**
	 * 设置 Order（`Order`）。
	 */
	public void setOrder(int order) {
		this.order = order;
	}

	/**
	 * 获取 Order（`Order`）。
	 */
	@Override
	public int getOrder() {
		if (this.order != null) {
			return this.order;
		}
		else {
			return this.advice.getOrder();
		}
	}

	/**
	 * 获取 Advice（`Advice`）。
	 */
	@Override
	public Advice getAdvice() {
		return this.advice;
	}

	/**
	 * 获取 Pointcut（`Pointcut`）。
	 */
	@Override
	public Pointcut getPointcut() {
		return this.pointcut;
	}

	/**
	 * 返回声明通知的方面（bean）的名称。
	 * @since 4.3.15
	 * @see AbstractAspectJAdvice#getAspectName()
	 */
	public String getAspectName() {
		return this.advice.getAspectName();
	}


	/**
	 * 比较是否相等。
	 */
	@Override
	public boolean equals(@Nullable Object other) {
		return (this == other || (other instanceof AspectJPointcutAdvisor that &&
				this.advice.equals(that.advice)));
	}

	/**
	 * 判断是否包含/具备 h Code。
	 */
	@Override
	public int hashCode() {
		return AspectJPointcutAdvisor.class.hashCode() * 29 + this.advice.hashCode();
	}

}
