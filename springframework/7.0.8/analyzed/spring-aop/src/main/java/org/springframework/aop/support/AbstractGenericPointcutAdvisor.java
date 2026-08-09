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

import org.aopalliance.aop.Advice;

/**
 * 抽象通用 {@link org.springframework.aop.PointcutAdvisor}，允许配置任何 {@link Advice}。
 * @author Juergen Hoeller
 * @since 2.0
 * @see #setAdvice
 * @see DefaultPointcutAdvisor
 */
@SuppressWarnings("serial")
public abstract class AbstractGenericPointcutAdvisor extends AbstractPointcutAdvisor {

	/** 通知相关状态（`EMPTY_ADVICE`）。 */
	private Advice advice = EMPTY_ADVICE;


	/**
	 * 指定该顾问应采用的建议。
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
	 * 返回字符串表示。
	 */
	@Override
	public String toString() {
		return getClass().getName() + ": advice [" + getAdvice() + "]";
	}

}
