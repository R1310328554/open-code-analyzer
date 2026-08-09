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
 * 属性访问相关异常的超类，例如类型不匹配或调用目标异常。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
@SuppressWarnings("serial")
public abstract class PropertyAccessException extends BeansException {

	/** 导致问题的属性变更事件（可能为 {@code null}）。 */
	private final @Nullable PropertyChangeEvent propertyChangeEvent;


	/**
	 * 创建新的 {@code PropertyAccessException}。
	 * @param propertyChangeEvent 导致问题的 {@link PropertyChangeEvent}
	 * @param msg 详细消息
	 * @param cause 根因
	 */
	public PropertyAccessException(PropertyChangeEvent propertyChangeEvent, String msg, @Nullable Throwable cause) {
		super(msg, cause);
		this.propertyChangeEvent = propertyChangeEvent;
	}

	/**
	 * 创建不带 {@link PropertyChangeEvent} 的 {@code PropertyAccessException}。
	 * @param msg 详细消息
	 * @param cause 根因
	 */
	public PropertyAccessException(String msg, @Nullable Throwable cause) {
		super(msg, cause);
		this.propertyChangeEvent = null;
	}


	/**
	 * 返回导致问题的 {@link PropertyChangeEvent}。
	 * <p>可能为 {@code null}；仅当实际影响到某个 bean 属性时才有值。
	 */
	public @Nullable PropertyChangeEvent getPropertyChangeEvent() {
		return this.propertyChangeEvent;
	}

	/**
	 * 返回受影响属性的名称（若可获得）。
	 */
	public @Nullable String getPropertyName() {
		return (this.propertyChangeEvent != null ? this.propertyChangeEvent.getPropertyName() : null);
	}

	/**
	 * 返回即将被设置的受影响值（若有）。
	 */
	public @Nullable Object getValue() {
		return (this.propertyChangeEvent != null ? this.propertyChangeEvent.getNewValue() : null);
	}

	/**
	 * 返回与此类异常对应的错误码。
	 */
	public abstract String getErrorCode();

}
