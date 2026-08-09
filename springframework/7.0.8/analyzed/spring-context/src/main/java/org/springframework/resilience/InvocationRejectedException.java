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

package org.springframework.resilience;

import java.util.concurrent.RejectedExecutionException;

/**
 * 因弹性策略导致目标不会被调用时抛出的异常，
 * 例如带 {@link org.springframework.resilience.annotation.ConcurrencyLimit @ConcurrencyLimit}
 * 注解的类/方法已达到并发上限。
 *
 * <p>继承 {@link RejectedExecutionException}，与
 * {@link org.springframework.core.task.TaskRejectedException} 共用基类，
 * 便于自定义 catch 块同时覆盖 Spring 场景与
 * {@link java.util.concurrent.ExecutorService} 拒绝异常。
 *
 * @author Juergen Hoeller
 * @since 7.0.3
 * @see org.springframework.resilience.annotation.ConcurrencyLimit.ThrottlePolicy#REJECT
 * @see org.springframework.core.task.TaskRejectedException
 */
@SuppressWarnings("serial")
public class InvocationRejectedException extends RejectedExecutionException {

	private final Object target;


	/**
	 * 使用指定详细消息和目标实例创建新的 {@code InvocationRejectedException}。
	 * @param msg 详细消息
	 * @param target 即将被调用的目标实例
	 */
	public InvocationRejectedException(String msg, Object target) {
		super(msg);
		this.target = target;
	}


	/**
	 * 返回即将被调用的目标实例。
	 */
	public Object getTarget() {
		return this.target;
	}

}
