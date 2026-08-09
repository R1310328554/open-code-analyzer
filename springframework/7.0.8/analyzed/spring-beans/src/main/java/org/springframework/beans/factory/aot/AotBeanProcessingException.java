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

package org.springframework.beans.factory.aot;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.support.RegisteredBean;
import org.springframework.beans.factory.support.RootBeanDefinition;

/**
 * AOT 处理某个 bean 失败时抛出。
 *
 * @author Stephane Nicoll
 * @since 6.2
 */
@SuppressWarnings("serial")
public class AotBeanProcessingException extends AotProcessingException {

	/** 处理失败的 bean 定义。 */
	private final RootBeanDefinition beanDefinition;


	/**
	 * 使用处理失败的 {@link RegisteredBean}、详细消息和可选根原因创建实例。
	 * @param registeredBean 处理失败的已注册 bean
	 * @param msg 详细消息
	 * @param cause 根原因（若有）
	 */
	public AotBeanProcessingException(RegisteredBean registeredBean, String msg, @Nullable Throwable cause) {
		super(createErrorMessage(registeredBean, msg), cause);
		this.beanDefinition = registeredBean.getMergedBeanDefinition();
	}

	/**
	 * 快捷方式：仅使用处理失败的 {@link RegisteredBean} 和详细消息创建实例。
	 * @param registeredBean 处理失败的已注册 bean
	 * @param msg 详细消息
	 */
	public AotBeanProcessingException(RegisteredBean registeredBean, String msg) {
		this(registeredBean, msg, null);
	}

	private static String createErrorMessage(RegisteredBean registeredBean, String msg) {
		StringBuilder sb = new StringBuilder("Error processing bean with name '");
		sb.append(registeredBean.getBeanName()).append("'");
		String resourceDescription = registeredBean.getMergedBeanDefinition().getResourceDescription();
		if (resourceDescription != null) {
			sb.append(" defined in ").append(resourceDescription);
		}
		sb.append(": ").append(msg);
		return sb.toString();
	}


	/**
	 * 返回处理失败的 bean 的 bean 定义。
	 */
	public RootBeanDefinition getBeanDefinition() {
		return this.beanDefinition;
	}

}
