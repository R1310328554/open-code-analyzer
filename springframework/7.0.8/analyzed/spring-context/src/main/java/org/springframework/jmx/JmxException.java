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

package org.springframework.jmx;

import org.springframework.core.NestedRuntimeException;

/**
 * JMX 错误时抛出的通用基类异常。
 * 为 unchecked 异常，因 JMX 失败通常具有致命性。
 *
 * @author Juergen Hoeller
 * @since 2.0
 */
@SuppressWarnings("serial")
public class JmxException extends NestedRuntimeException {

	/**
	 * JmxException 构造函数。
	 * @param msg 详细消息
	 */
	public JmxException(String msg) {
		super(msg);
	}

	/**
	 * JmxException 构造函数。
	 * @param msg 详细消息
	 * @param cause 根因（通常为原始 JMX API 异常）
	 */
	public JmxException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
