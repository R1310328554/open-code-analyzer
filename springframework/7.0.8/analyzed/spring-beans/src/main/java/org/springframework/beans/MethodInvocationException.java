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

package org.springframework.beans;

import java.beans.PropertyChangeEvent;

import org.jspecify.annotations.Nullable;

/**
 * 当 bean 属性的 getter 或 setter 方法抛出异常时抛出，
 * 类似于 {@code InvocationTargetException}。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
@SuppressWarnings("serial")
public class MethodInvocationException extends PropertyAccessException {

	/**
	 * 方法调用错误注册时所使用的错误码。
	 */
	public static final String ERROR_CODE = "methodInvocation";


	/**
	 * 创建一个新的 {@code MethodInvocationException}。
	 * @param propertyChangeEvent 导致异常的 {@link PropertyChangeEvent}
	 * @param cause 被调用方法抛出的 {@link Throwable}
	 */
	public MethodInvocationException(PropertyChangeEvent propertyChangeEvent, @Nullable Throwable cause) {
		super(propertyChangeEvent,
				"Property '" + propertyChangeEvent.getPropertyName() + "' threw exception: " + cause,
				cause);
	}

	/**
	 * 返回本异常对应的错误码。
	 */
	@Override
	public String getErrorCode() {
		return ERROR_CODE;
	}

}
