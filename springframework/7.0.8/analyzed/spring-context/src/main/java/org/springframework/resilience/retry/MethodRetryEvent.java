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

package org.springframework.resilience.retry;

import org.aopalliance.intercept.MethodInvocation;

import org.springframework.context.event.MethodFailureEvent;
import org.springframework.util.ClassUtils;

/**
 * 可重试方法调用过程中遇到的每个异常都会发布此事件。
 * 可通过 {@code ApplicationListener<MethodRetryEvent>} Bean 或
 * {@code @EventListener(MethodRetryEvent.class)} 方法监听。
 *
 * @author Juergen Hoeller
 * @since 7.0.3
 * @see AbstractRetryInterceptor
 * @see org.springframework.resilience.annotation.Retryable
 * @see org.springframework.context.ApplicationListener
 * @see org.springframework.context.event.EventListener
 */
@SuppressWarnings("serial")
public class MethodRetryEvent extends MethodFailureEvent {

	private final boolean retryAborted;


	/**
	 * 为给定可重试方法调用创建新事件。
	 * @param invocation 可重试方法调用
	 * @param failure 遇到的异常
	 * @param retryAborted 当前失败是否导致重试执行中止
	 */
	public MethodRetryEvent(MethodInvocation invocation, Throwable failure, boolean retryAborted) {
		super(invocation, failure);
		this.retryAborted = retryAborted;
	}


	/**
	 * 返回遇到的异常。
	 * <p>可能是方法抛出的异常、方法返回的响应式 Publisher 发出的异常，
	 * 或重试用尽、中断或超时时的终止异常。
	 * <p>对于 {@link org.springframework.core.retry.RetryTemplate} 执行，
	 * {@code instanceof RetryException} 检查可识别最终异常。
	 * 对于 Reactor 管道，{@code Exceptions.isRetryExhausted} 识别用尽异常，
	 * 而 {@code instanceof TimeoutException} 表示超时场景。
	 * @see #isRetryAborted()
	 * @see org.springframework.core.retry.RetryException
	 * @see reactor.core.Exceptions#isRetryExhausted
	 * @see java.util.concurrent.TimeoutException
	 */
	public Throwable getFailure() {
		return super.getFailure();
	}

	/**
	 * 返回当前失败是否导致重试执行中止，
	 * 通常表示用尽、中断或超时场景。
	 * <p>若返回 {@code true}，{@link #getFailure()} 暴露的是重试基础设施抛出的最终异常
	 *（而非方法本身抛出）。
	 * @see #getFailure()
	 */
	public boolean isRetryAborted() {
		return this.retryAborted;
	}


	@Override
	public String toString() {
		return "MethodRetryEvent: " + ClassUtils.getQualifiedMethodName(getMethod()) + " [" + getFailure() + "]";
	}

}
