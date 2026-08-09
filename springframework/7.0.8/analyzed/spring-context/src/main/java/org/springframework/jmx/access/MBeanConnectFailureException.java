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

import org.springframework.jmx.JmxException;

/**
 * 因 {@code MBeanServerConnection} 上的 I/O 问题导致调用失败时抛出。
 *
 * @author Juergen Hoeller
 * @since 2.5.6
 * @see MBeanClientInterceptor
 */
@SuppressWarnings("serial")
public class MBeanConnectFailureException extends JmxException {

	/**
	 * 使用指定的错误消息和根因创建新的 {@code MBeanConnectFailureException}。
	 * @param msg 详细消息
	 * @param cause 根因
	 */
	public MBeanConnectFailureException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
