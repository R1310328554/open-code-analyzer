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

import org.aopalliance.aop.Advice;

import org.springframework.aop.Pointcut;
import org.springframework.aop.PointcutAdvisor;
import org.springframework.core.Ordered;
import org.springframework.util.Assert;

/**
 * Advisor 的方便基类，也是静态切入点。如果 Advice 和子类是可序列化的。
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
@SuppressWarnings("serial")
public abstract class StaticMethodMatcherPointcutAdvisor extends StaticMethodMatcherPointcut
		implements PointcutAdvisor, Ordered, Serializable {

	/** 通知相关状态（`EMPTY_ADVICE`）。 */
	private Advice advice = EMPTY_ADVICE;

	private int order = Ordered.LOWEST_PRECEDENCE;


	/**
	 * 创建一个新的 StaticMethodMatcherPointcutAdvisor，需要 bean 样式的配置。
	 * @see #setAdvice
	 */
	public StaticMethodMatcherPointcutAdvisor() {
	}

	/**
	 * 为给定的建议创建一个新的 StaticMethodMatcherPointcutAdvisor。
	 * @param advice 使用建议
	 */
	public StaticMethodMatcherPointcutAdvisor(Advice advice) {
		Assert.notNull(advice, "Advice must not be null");
		this.advice = advice;
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
		return this.order;
	}

	/**
	 * 设置 Advice（`Advice`）。
	 */
	public void setAdvice(Advice advice) {
		this.advice = advice;
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
		return this;
	}

}
