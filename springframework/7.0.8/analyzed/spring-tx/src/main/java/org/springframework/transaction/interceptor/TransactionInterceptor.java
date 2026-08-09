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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Properties;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.jspecify.annotations.Nullable;

import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionExecution;
import org.springframework.transaction.TransactionManager;

/**
 * 使用 Spring 通用事务基础设施
 * （{@link org.springframework.transaction.PlatformTransactionManager}/
 * {@link org.springframework.transaction.ReactiveTransactionManager}）
 * 进行声明式事务管理的 AOP Alliance MethodInterceptor。
 *
 * <p>派生自 {@link TransactionAspectSupport}，其中包含与 Spring 底层事务 API 的集成。
 * TransactionInterceptor 仅按正确顺序调用 {@link #invokeWithinTransaction} 等超类方法。
 *
 * <p>TransactionInterceptor 是线程安全的。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Sebastien Deleuze
 * @see TransactionProxyFactoryBean
 * @see org.springframework.aop.framework.ProxyFactoryBean
 * @see org.springframework.aop.framework.ProxyFactory
 */
@SuppressWarnings("serial")
public class TransactionInterceptor extends TransactionAspectSupport
		implements MethodInterceptor, ApplicationEventPublisherAware, Serializable {

	private @Nullable ApplicationEventPublisher applicationEventPublisher;


	/**
	 * 创建新的 TransactionInterceptor。
	 * <p>仍需设置事务管理器与事务属性。
	 * @see #setTransactionManager
	 * @see #setTransactionAttributes(java.util.Properties)
	 * @see #setTransactionAttributeSource(TransactionAttributeSource)
	 */
	public TransactionInterceptor() {
	}

	/**
	 * 创建新的 TransactionInterceptor。
	 * @param ptm 执行实际事务管理的默认事务管理器
	 * @param tas 用于查找事务属性的属性源
	 * @since 5.2.5
	 * @see #setTransactionManager
	 * @see #setTransactionAttributeSource
	 */
	public TransactionInterceptor(TransactionManager ptm, TransactionAttributeSource tas) {
		setTransactionManager(ptm);
		setTransactionAttributeSource(tas);
	}

	/**
	 * 创建新的 TransactionInterceptor。
	 * @param ptm 执行实际事务管理的默认事务管理器
	 * @param tas 用于查找事务属性的属性源
	 * @see #setTransactionManager
	 * @see #setTransactionAttributeSource
	 * @deprecated 请改用
	 * {@link #TransactionInterceptor(TransactionManager, TransactionAttributeSource)}
	 */
	@Deprecated(since = "5.2.5")
	public TransactionInterceptor(PlatformTransactionManager ptm, TransactionAttributeSource tas) {
		setTransactionManager(ptm);
		setTransactionAttributeSource(tas);
	}

	/**
	 * 创建新的 TransactionInterceptor。
	 * @param ptm 执行实际事务管理的默认事务管理器
	 * @param attributes Properties 格式的事务属性
	 * @see #setTransactionManager
	 * @see #setTransactionAttributes(java.util.Properties)
	 * @deprecated 请改用 {@link #setTransactionAttributes(Properties)}
	 */
	@Deprecated(since = "5.2.5")
	public TransactionInterceptor(PlatformTransactionManager ptm, Properties attributes) {
		setTransactionManager(ptm);
		setTransactionAttributes(attributes);
	}


	@Override
	public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
		this.applicationEventPublisher = applicationEventPublisher;
	}

	@Override
	public @Nullable Object invoke(MethodInvocation invocation) throws Throwable {
		// 确定目标类：可能为 {@code null}。
		// TransactionAttributeSource 应同时传入目标类与方法，
		// 方法可能来自接口。
		Class<?> targetClass = (invocation.getThis() != null ? AopUtils.getTargetClass(invocation.getThis()) : null);

		// 适配 TransactionAspectSupport 的 invokeWithinTransaction...
		return invokeWithinTransaction(invocation.getMethod(), targetClass, new InvocationCallback() {
			@Override
			public @Nullable Object proceedWithInvocation() throws Throwable {
				return invocation.proceed();
			}
			@Override
			public void onRollback(Throwable failure, TransactionExecution execution) {
				MethodRollbackEvent event = new MethodRollbackEvent(invocation, failure, execution);
				logger.trace(event, failure);
				if (applicationEventPublisher != null) {
					try {
						applicationEventPublisher.publishEvent(event);
					}
					catch (Throwable ex) {
						if (logger.isWarnEnabled()) {
							logger.warn("Failed to publish " + event, ex);
						}
					}
				}
			}
		});
	}


	//---------------------------------------------------------------------
	// 序列化支持
	//---------------------------------------------------------------------

	private void writeObject(ObjectOutputStream oos) throws IOException {
		// 依赖默认序列化，尽管本类本身并不携带状态...
		oos.defaultWriteObject();

		// 反序列化超类字段。
		oos.writeObject(getTransactionManagerBeanName());
		oos.writeObject(getTransactionManager());
		oos.writeObject(getTransactionAttributeSource());
		oos.writeObject(getBeanFactory());
	}

	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
		// 依赖默认序列化，尽管本类本身并不携带状态...
		ois.defaultReadObject();

		// 序列化所有相关超类字段。
		// 超类不能实现 Serializable，因为它也作为 AspectJ 切面的基类
		// （AspectJ 切面不允许实现 Serializable）！
		setTransactionManagerBeanName((String) ois.readObject());
		setTransactionManager((PlatformTransactionManager) ois.readObject());
		setTransactionAttributeSource((TransactionAttributeSource) ois.readObject());
		setBeanFactory((BeanFactory) ois.readObject());
	}

}
