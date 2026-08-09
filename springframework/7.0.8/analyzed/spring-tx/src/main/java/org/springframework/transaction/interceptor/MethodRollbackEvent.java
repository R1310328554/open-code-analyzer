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

package org.springframework.transaction.interceptor;

import org.aopalliance.intercept.MethodInvocation;

import org.springframework.context.event.MethodFailureEvent;
import org.springframework.transaction.TransactionExecution;

/**
 * 每当通过代理触发的方法调用或其返回的响应式 Publisher 中
 * 遇到触发事务回滚的异常时发布的事件。
 * 可通过 {@code ApplicationListener<MethodRollbackEvent>} Bean 或
 * {@code @EventListener(MethodRollbackEvent.class)} 方法监听。
 *
 * <p>注意：该事件在实际事务回滚<i>之前</i>发布。
 * 因此暴露的 {@link #getTransaction() 事务}反映回滚前的事务状态。
 *
 * @author Juergen Hoeller
 * @since 7.0.3
 * @see TransactionInterceptor
 * @see org.springframework.transaction.annotation.Transactional
 * @see org.springframework.context.ApplicationListener
 * @see org.springframework.context.event.EventListener
 */
@SuppressWarnings("serial")
public class MethodRollbackEvent extends MethodFailureEvent {

	private final TransactionExecution transaction;


	/**
	 * 为给定已回滚的方法调用创建新事件。
	 * @param invocation 事务方法调用
	 * @param failure 触发回滚的异常
	 * @param transaction 回滚前的事务状态
	 */
	public MethodRollbackEvent(MethodInvocation invocation, Throwable failure, TransactionExecution transaction) {
		super(invocation, failure);
		this.transaction = transaction;
	}


	/**
	 * 返回遇到的异常。
	 * <p>可能是方法抛出的异常，或方法返回的响应式 Publisher 发出的异常。
	 */
	@Override
	public Throwable getFailure() {
		return super.getFailure();
	}

	/**
	 * 返回对应的事务状态。
	 */
	public TransactionExecution getTransaction() {
		return this.transaction;
	}

}
