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
 * 用于保存建议的名称匹配方法切入点的便捷类，使它们成为顾问。
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @see NameMatchMethodPointcut
 */
@SuppressWarnings("serial")
public class NameMatchMethodPointcutAdvisor extends AbstractGenericPointcutAdvisor {

	/**
	 * 方法 `NameMatchMethodPointcut`：完成本类中与「Name Match Method Pointcut」相关的职责。
	 */
	private final NameMatchMethodPointcut pointcut = new NameMatchMethodPointcut();


	/**
	 * 创建 `NameMatchMethodPointcutAdvisor` 的新实例。
	 */
	public NameMatchMethodPointcutAdvisor() {
	}

	/**
	 * 创建 `NameMatchMethodPointcutAdvisor` 的新实例。
	 */
	public NameMatchMethodPointcutAdvisor(Advice advice) {
		setAdvice(advice);
	}


	/**
	 * 设置用于此切入点的 {@link ClassFilter}。默认为 {@link ClassFilter#TRUE}。
	 * @see NameMatchMethodPointcut#setClassFilter
	 */
	public void setClassFilter(ClassFilter classFilter) {
		this.pointcut.setClassFilter(classFilter);
	}

	/**
	 * 当我们只有一个方法名称需要匹配时，这是一种方便的方法。使用此方法或 {@code setMappedNames}，而不是同时使用两者。
	 * @see #setMappedNames
	 * @see NameMatchMethodPointcut#setMappedName
	 */
	public void setMappedName(String mappedName) {
		this.pointcut.setMappedName(mappedName);
	}

	/**
	 * 设置定义要匹配的方法的方法名称。匹配将是所有这些的并集；如果有匹配，则切入点匹配。
	 * @see NameMatchMethodPointcut#setMappedNames
	 */
	public void setMappedNames(String... mappedNames) {
		this.pointcut.setMappedNames(mappedNames);
	}

	/**
	 * 除了已命名的方法名称之外，添加另一个符合条件的方法名称。与 set 方法一样，此方法用于在使用代理之前配置代理时使用。
	 * @param name 将匹配的附加方法的名称
	 * @return 切入点以允许在一行中进行多次添加
	 * @see NameMatchMethodPointcut#addMethodName
	 */
	public NameMatchMethodPointcut addMethodName(String name) {
		return this.pointcut.addMethodName(name);
	}


	/**
	 * 获取 Pointcut（`Pointcut`）。
	 */
	@Override
	public Pointcut getPointcut() {
		return this.pointcut;
	}

}
