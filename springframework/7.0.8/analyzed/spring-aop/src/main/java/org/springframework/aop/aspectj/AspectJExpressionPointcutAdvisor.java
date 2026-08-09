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

import org.jspecify.annotations.Nullable;

import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractGenericPointcutAdvisor;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;

/**
 * Spring AOP Advisor 可用于任何 AspectJ 切入点表达式。
 * @author Rob Harrop
 * @since 2.0
 */
@SuppressWarnings("serial")
public class AspectJExpressionPointcutAdvisor extends AbstractGenericPointcutAdvisor implements BeanFactoryAware {

	/**
	 * 方法 `AspectJExpressionPointcut`：完成本类中与「Aspect J Expression Pointcut」相关的职责。
	 */
	private final AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();


	/**
	 * 设置 Expression（`Expression`）。
	 */
	public void setExpression(@Nullable String expression) {
		this.pointcut.setExpression(expression);
	}

	/**
	 * 获取 Expression（`Expression`）。
	 */
	public @Nullable String getExpression() {
		return this.pointcut.getExpression();
	}

	/**
	 * 设置 Location（`Location`）。
	 */
	public void setLocation(@Nullable String location) {
		this.pointcut.setLocation(location);
	}

	/**
	 * 获取 Location（`Location`）。
	 */
	public @Nullable String getLocation() {
		return this.pointcut.getLocation();
	}

	/**
	 * 设置 Parameter Names（`ParameterNames`）。
	 */
	public void setParameterNames(String... names) {
		this.pointcut.setParameterNames(names);
	}

	/**
	 * 设置 Parameter Types（`ParameterTypes`）。
	 */
	public void setParameterTypes(Class<?>... types) {
		this.pointcut.setParameterTypes(types);
	}

	/**
	 * 设置 Bean Factory（`BeanFactory`）。
	 */
	@Override
	public void setBeanFactory(BeanFactory beanFactory) {
		this.pointcut.setBeanFactory(beanFactory);
	}

	/**
	 * 获取 Pointcut（`Pointcut`）。
	 */
	@Override
	public Pointcut getPointcut() {
		return this.pointcut;
	}

}
