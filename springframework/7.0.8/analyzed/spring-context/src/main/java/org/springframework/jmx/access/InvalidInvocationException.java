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

package org.springframework.jmx.access;

import javax.management.JMRuntimeException;

import org.jspecify.annotations.Nullable;

/**
 * 尝试在代理上调用未被被代理 MBean 资源管理接口暴露的操作时抛出。
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 1.2
 * @see MBeanClientInterceptor
 */
@SuppressWarnings("serial")
public class InvalidInvocationException extends JMRuntimeException {

	/**
	 * 使用给定错误消息创建新的 {@code InvalidInvocationException}。
	 * @param msg 详细消息
	 */
	public InvalidInvocationException(@Nullable String msg) {
		super(msg);
	}

}
