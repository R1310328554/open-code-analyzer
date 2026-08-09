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
 * 监控拦截器（如性能监控器）的基类。
 * 提供可配置的 "prefix" 和 "suffix" 属性，
 * 用于分类/分组性能监控结果。
 *
 * <p>在 {@link #invokeUnderTrace} 实现中，子类应调用
 * {@link #createInvocationTraceName} 方法为给定跟踪创建名称，
 * 包含方法调用信息及前缀/后缀。
 *
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

	private boolean logTargetClassInvocation = false;


	/**
	 * 设置追加到跟踪数据后的文本。
	 * <p>默认为无。
	 */
	public void setPrefix(@Nullable String prefix) {
		this.prefix = (prefix != null ? prefix : "");
	}

	/**
	 * 返回追加到跟踪数据后的文本。
	 */
	protected String getPrefix() {
		return this.prefix;
	}

	/**
	 * 设置前置到跟踪数据前的文本。
	 * <p>默认为无。
	 */
	public void setSuffix(@Nullable String suffix) {
		this.suffix = (suffix != null ? suffix : "");
	}

	/**
	 * 返回前置到跟踪数据前的文本。
	 */
	protected String getSuffix() {
		return this.suffix;
	}

	/**
	 * 设置是否记录目标类上的调用（若适用，
	 * 即方法实际委托给目标类）。
	 * <p>默认为 "false"，基于代理接口/类名记录调用。
	 */
	public void setLogTargetClassInvocation(boolean logTargetClassInvocation) {
		this.logTargetClassInvocation = logTargetClassInvocation;
	}


	/**
	 * 为给定 {@code MethodInvocation} 创建可用于跟踪/日志的 {@code String} 名称。
	 * 该名称由配置的前缀、被调用方法的全限定名及配置的后缀组成。
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
