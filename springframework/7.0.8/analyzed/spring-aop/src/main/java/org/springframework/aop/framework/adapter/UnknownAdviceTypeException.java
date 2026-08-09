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

package org.springframework.aop.framework.adapter;

/**
 * 尝试使用不受支持的 Advisor 或 Advice 类型时抛出的异常。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see org.aopalliance.aop.Advice
 * @see org.springframework.aop.Advisor
 */
@SuppressWarnings("serial")
public class UnknownAdviceTypeException extends IllegalArgumentException {

	/**
	 * 为给定 Advice 对象创建新的 UnknownAdviceTypeException。
	 * 消息文本说明该对象既非 Advice 子接口，也非 Advisor。
	 * @param advice 未知类型的 Advice 对象
	 */
	public UnknownAdviceTypeException(Object advice) {
		super("Advice object [" + advice + "] is neither a supported subinterface of " +
				"[org.aopalliance.aop.Advice] nor an [org.springframework.aop.Advisor]");
	}

	/**
	 * 以给定消息创建新的 UnknownAdviceTypeException。
	 * @param message 消息文本
	 */
	public UnknownAdviceTypeException(String message) {
		super(message);
	}

}
