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

package org.springframework.transaction.event;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.ApplicationListenerMethodAdapter;
import org.springframework.context.event.EventListener;
import org.springframework.context.event.GenericApplicationListener;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.Assert;

/**
 * 将事件处理委托给 {@link TransactionalEventListener} 注解方法的
 * {@link GenericApplicationListener} 适配器。支持与任何常规
 * {@link EventListener} 注解方法完全相同的功能，但感知事件发布者的事务上下文。
 *
 * <p>启用 Spring 事务管理时，{@link TransactionalEventListener} 的处理会自动启用。
 * 其他情况下需注册 {@link TransactionalEventListenerFactory} 类型的 Bean。
 *
 * @author Stephane Nicoll
 * @author Juergen Hoeller
 * @since 5.3
 * @see TransactionalEventListener
 * @see TransactionalApplicationListener
 * @see TransactionalApplicationListenerAdapter
 */
public class TransactionalApplicationListenerMethodAdapter extends ApplicationListenerMethodAdapter
		implements TransactionalApplicationListener<ApplicationEvent> {

	private final TransactionPhase transactionPhase;

	private final List<SynchronizationCallback> callbacks = new CopyOnWriteArrayList<>();


	/**
	 * 构造新的 TransactionalApplicationListenerMethodAdapter。
	 * @param beanName 要调用监听器方法的 Bean 名称
	 * @param targetClass 声明该方法的目标类
	 * @param method 要调用的监听器方法
	 */
	public TransactionalApplicationListenerMethodAdapter(String beanName, Class<?> targetClass, Method method) {
		super(beanName, targetClass, method);
		TransactionalEventListener eventAnn =
				AnnotatedElementUtils.findMergedAnnotation(getTargetMethod(), TransactionalEventListener.class);
		if (eventAnn == null) {
			throw new IllegalStateException("No TransactionalEventListener annotation found on method: " + method);
		}
		this.transactionPhase = eventAnn.phase();
	}


	@Override
	public TransactionPhase getTransactionPhase() {
		return this.transactionPhase;
	}

	@Override
	public void addCallback(SynchronizationCallback callback) {
		Assert.notNull(callback, "SynchronizationCallback must not be null");
		this.callbacks.add(callback);
	}


	@Override
	public void onApplicationEvent(ApplicationEvent event) {
		if (TransactionalApplicationListenerSynchronization.register(event, this, this.callbacks)) {
			if (logger.isDebugEnabled()) {
				logger.debug("Registered transaction synchronization for " + event);
			}
		}
		else if (isDefaultExecution()) {
			if (getTransactionPhase() == TransactionPhase.AFTER_ROLLBACK && logger.isWarnEnabled()) {
				logger.warn("Processing " + event + " as a fallback execution on AFTER_ROLLBACK phase");
			}
			processEvent(event);
		}
		else {
			// 完全无事务事件执行
			if (logger.isDebugEnabled()) {
				logger.debug("No transaction is active - skipping " + event);
			}
		}
	}

}
