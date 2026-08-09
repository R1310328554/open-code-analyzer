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

package org.springframework.aop.interceptor;

import java.lang.reflect.Method;

import org.aopalliance.intercept.MethodInvocation;
import org.jspecify.annotations.Nullable;

/**
 * 用于监视拦截器的基类，例如性能监视器。提供可配置的“前缀”和“后缀”属性，有助于对性能监控结果进行分类/分组。
 * <p> 在其 {@link #invokeUnderTrace} 实现中，子类应调用 {@link #createInvocationTraceName}
 * 方法来为给定跟踪创建名称，包括有关方法调用的信息以及前缀/后缀。
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 1.2.7
 * @see #setPrefix
 * @see #setSuffix
 * @see #createInvocationTraceName
 */
@SuppressWarnings("serial")
public abstract class AbstractMonitoringInterceptor extends AbstractTraceInterceptor {

	private String prefix = "";

	private String suffix = "";

	/** `false`：该类的成员状态。 */
	private boolean logTargetClassInvocation = false;


	/**
	 * 设置将附加到跟踪数据的文本。 <p>默认为无。
	 */
	public void setPrefix(@Nullable String prefix) {
		this.prefix = (prefix != null ? prefix : "");
	}

	/**
	 * 返回将附加到跟踪数据的文本。
	 */
	protected String getPrefix() {
		return this.prefix;
	}

	/**
	 * 设置将添加到跟踪数据前面的文本。 <p>默认为无。
	 */
	public void setSuffix(@Nullable String suffix) {
		this.suffix = (suffix != null ? suffix : "");
	}

	/**
	 * 返回将添加到跟踪数据前面的文本。
	 */
	protected String getSuffix() {
		return this.suffix;
	}

	/**
	 * 设置是否记录目标类上的调用（如果适用）（即，如果该方法实际上委托给目标类）。 <p>Default 为“false”，根据代理接口/类名称记录调用。
	 */
	public void setLogTargetClassInvocation(boolean logTargetClassInvocation) {
		this.logTargetClassInvocation = logTargetClassInvocation;
	}


	/**
	 * 为给定的 {@code MethodInvocation} 创建可用于跟踪/记录目的的 {@code String} 名称。该名称由配置的前缀、所调用方法的完全限定名称和配置的
	 * 后缀组成。
	 * @see #setPrefix
	 * @see #setSuffix
	 */
	protected String createInvocationTraceName(MethodInvocation invocation) {
		Method method = invocation.getMethod();
		Class<?> clazz = method.getDeclaringClass();
		if (this.logTargetClassInvocation && clazz.isInstance(invocation.getThis())) {
			clazz = invocation.getThis().getClass();
		}
		String className = clazz.getName();
		return getPrefix() + className + '.' + method.getName() + getSuffix();
	}

}
