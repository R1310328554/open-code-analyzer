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

import org.jspecify.annotations.Nullable;

import org.springframework.aop.Pointcut;

/**
 * 基于具体 BeanFactory 的 PointcutAdvisor，允许将任何建议配置为对 BeanFactory 中的建议 bean 的引用，以及通过 bean
 * 属性配置切入点。
 * <p>指定通知 bean 的名称而不是通知对象本身（如果在 BeanFactory 中运行）会增加初始化时的松散耦合，以便在切入点实际匹配之前不初始化通知对象。
 * @author Juergen Hoeller
 * @since 2.0.2
 * @see #setPointcut
 * @see #setAdviceBeanName
 */
@SuppressWarnings("serial")
public class DefaultBeanFactoryPointcutAdvisor extends AbstractBeanFactoryPointcutAdvisor {

	private Pointcut pointcut = Pointcut.TRUE;


	/**
	 * 指定针对建议的切入点。 <p>默认为 {@code Pointcut.TRUE}。
	 * @see #setAdviceBeanName
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
		return getClass().getName() + ": pointcut [" + getPointcut() + "]; advice bean '" + getAdviceBeanName() + "'";
	}

}
