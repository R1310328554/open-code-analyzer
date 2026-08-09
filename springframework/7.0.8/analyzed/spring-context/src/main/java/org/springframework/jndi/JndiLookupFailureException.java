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

package org.springframework.jndi;

import javax.naming.NamingException;

import org.springframework.core.NestedRuntimeException;

/**
 * JNDI 查找失败时抛出的运行时异常，
 * 尤其适用于不声明 JNDI 受检 {@link javax.naming.NamingException} 的代码，
 * 例如 Spring 的 {@link JndiObjectTargetSource}。
 *
 * @author Juergen Hoeller
 * @since 2.0.3
 */
@SuppressWarnings("serial")
public class JndiLookupFailureException extends NestedRuntimeException {

	/**
	 * 构造新的 {@code JndiLookupFailureException}，包装给定 JNDI {@code NamingException}。
	 * @param msg 详细消息
	 * @param cause {@code NamingException} 根因
	 */
	public JndiLookupFailureException(String msg, NamingException cause) {
		super(msg, cause);
	}

}
