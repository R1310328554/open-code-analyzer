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

import org.springframework.aop.ClassFilter;
import org.springframework.aop.Pointcut;

/**
 * 持有 Advice 的方法名匹配切入点的便捷类，
 * 使其成为 Advisor。
 *
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @see NameMatchMethodPointcut
 */
@SuppressWarnings("serial")
public class NameMatchMethodPointcutAdvisor extends AbstractGenericPointcutAdvisor {

	private final NameMatchMethodPointcut pointcut = new NameMatchMethodPointcut();


	public NameMatchMethodPointcutAdvisor() {
	}

	public NameMatchMethodPointcutAdvisor(Advice advice) {
		setAdvice(advice);
	}


	/**
	 * 设置本切入点使用的 {@link ClassFilter}。
	 * 默认为 {@link ClassFilter#TRUE}。
	 * @see NameMatchMethodPointcut#setClassFilter
	 */
	public void setClassFilter(ClassFilter classFilter) {
		this.pointcut.setClassFilter(classFilter);
	}

	/**
	 * 仅匹配单个方法名时的便捷方法。
	 * 使用本方法或 {@code setMappedNames} 之一，不可同时使用。
	 * @see #setMappedNames
	 * @see NameMatchMethodPointcut#setMappedName
	 */
	public void setMappedName(String mappedName) {
		this.pointcut.setMappedName(mappedName);
	}

	/**
	 * 设置定义要匹配方法的方法名。
	 * 匹配为所有名称的并集；任一匹配则切入点匹配。
	 * @see NameMatchMethodPointcut#setMappedNames
	 */
	public void setMappedNames(String... mappedNames) {
		this.pointcut.setMappedNames(mappedNames);
	}

	/**
	 * 在已命名方法之外再添加一个符合条件的方法名。
	 * 与 set 方法类似，本方法用于配置代理、代理使用前。
	 * @param name 将匹配的额外方法名
	 * @return 本切入点，支持一行内多次添加
	 * @see NameMatchMethodPointcut#addMethodName
	 */
	public NameMatchMethodPointcut addMethodName(String name) {
		return this.pointcut.addMethodName(name);
	}


	@Override
	public Pointcut getPointcut() {
		return this.pointcut;
	}

}
