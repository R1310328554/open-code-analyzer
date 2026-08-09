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

package org.springframework.context.event;

import java.lang.reflect.Method;

import org.aopalliance.intercept.MethodInvocation;

import org.springframework.context.ApplicationEvent;
import org.springframework.util.ClassUtils;

/**
 * 表示方法调用失败的事件。
 *
 * @author Juergen Hoeller
 * @since 7.0.3
 * @see EventPublicationInterceptor
 */
@SuppressWarnings("serial")
public class MethodFailureEvent extends ApplicationEvent {

	/** 方法调用抛出的异常。 */
	private final Throwable failure;


	/**
	 * 为给定方法调用创建新事件。
	 * @param invocation 方法调用
	 * @param failure 遇到的异常
	 */
	public MethodFailureEvent(MethodInvocation invocation, Throwable failure) {
		super(invocation);
		this.failure = failure;
	}


	/**
	 * 返回触发本事件的方法调用。
	 */
	@Override
	public MethodInvocation getSource() {
		return (MethodInvocation) super.getSource();
	}

	/**
	 * 返回触发本事件的方法。
	 */
	public Method getMethod() {
		return getSource().getMethod();
	}

	/**
	 * 返回遇到的异常。
	 */
	public Throwable getFailure() {
		return this.failure;
	}


	@Override
	public String toString() {
		return getClass().getSimpleName() + ": " + ClassUtils.getQualifiedMethodName(getMethod()) +
				" [" + getFailure() + "]";
	}

}
