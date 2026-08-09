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
import org.jspecify.annotations.Nullable;

import org.springframework.aop.Pointcut;

/**
 * 方便的切入点驱动的 Advisor 实施。
 * <p>这是最常用的Advisor实现。它可以与任何切入点和建议类型一起使用，除了介绍之外。通常不需要子类化此类，或实现自定义顾问。
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see #setPointcut
 * @see #setAdvice
 */
@SuppressWarnings("serial")
public class DefaultPointcutAdvisor extends AbstractGenericPointcutAdvisor implements Serializable {

	private Pointcut pointcut = Pointcut.TRUE;


	/**
	 * 创建一个空的 DefaultPointcutAdvisor。在使用 setter 方法之前必须设置 <p>Advice。通常也会设置切入点，但默认为 {@code
	 * Pointcut.TRUE}。
	 */
	public DefaultPointcutAdvisor() {
	}

	/**
	 * 创建一个与所有方法匹配的 DefaultPointcutAdvisor。 <p>{@code Pointcut.TRUE} 将用作切入点。
	 * @param advice 使用建议
	 */
	public DefaultPointcutAdvisor(Advice advice) {
		this(Pointcut.TRUE, advice);
	}

	/**
	 * 创建一个 DefaultPointcutAdvisor，指定切入点和建议。
	 * @param pointcut 针对建议的切入点
	 * @param advice 切入点匹配时运行的建议
	 */
	public DefaultPointcutAdvisor(Pointcut pointcut, Advice advice) {
		this.pointcut = pointcut;
		setAdvice(advice);
	}


	/**
	 * 指定针对建议的切入点。 <p>默认为 {@code Pointcut.TRUE}。
	 * @see #setAdvice
	 */
	public void setPointcut(@Nullable Pointcut pointcut) {
		this.pointcut = (pointcut != null ? pointcut : Pointcut.TRUE);
	}

	/**
	 * 获取 Pointcut（`Pointcut`）。
	 */
	@Override
	public Pointcut getPointcut() {
		return this.pointcut;
	}


	/**
	 * 返回字符串表示。
	 */
	@Override
	public String toString() {
		return getClass().getName() + ": pointcut [" + getPointcut() + "]; advice [" + getAdvice() + "]";
	}

}
