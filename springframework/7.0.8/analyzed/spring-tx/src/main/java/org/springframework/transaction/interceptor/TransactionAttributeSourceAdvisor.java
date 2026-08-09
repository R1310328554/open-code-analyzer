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

import org.aopalliance.aop.Advice;
import org.jspecify.annotations.Nullable;

import org.springframework.aop.ClassFilter;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractPointcutAdvisor;
import org.springframework.util.Assert;

/**
 * 由 {@link TransactionAttributeSource} 驱动的 Advisor，
 * 仅对事务性方法包含 {@link TransactionInterceptor}。
 *
 * <p>由于 AOP 框架缓存 advice 计算结果，
 * 这通常比让 TransactionInterceptor 自行运行并发现无事可做更快。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see #setTransactionInterceptor
 * @see TransactionProxyFactoryBean
 */
@SuppressWarnings("serial")
public class TransactionAttributeSourceAdvisor extends AbstractPointcutAdvisor {

	private @Nullable TransactionInterceptor transactionInterceptor;

	private final TransactionAttributeSourcePointcut pointcut = new TransactionAttributeSourcePointcut();


	/**
	 * 创建新的 TransactionAttributeSourceAdvisor。
	 */
	public TransactionAttributeSourceAdvisor() {
	}

	/**
	 * 创建新的 TransactionAttributeSourceAdvisor。
	 * @param interceptor 本 Advisor 使用的事务拦截器
	 */
	public TransactionAttributeSourceAdvisor(TransactionInterceptor interceptor) {
		setTransactionInterceptor(interceptor);
	}


	/**
	 * 设置本 Advisor 使用的事务拦截器。
	 */
	public void setTransactionInterceptor(TransactionInterceptor interceptor) {
		Assert.notNull(interceptor, "TransactionInterceptor must not be null");
		this.transactionInterceptor = interceptor;
		this.pointcut.setTransactionAttributeSource(interceptor.getTransactionAttributeSource());
	}

	/**
	 * 设置本切点使用的 {@link ClassFilter}。
	 * 默认为 {@link ClassFilter#TRUE}。
	 */
	public void setClassFilter(ClassFilter classFilter) {
		this.pointcut.setClassFilter(classFilter);
	}


	@Override
	public Advice getAdvice() {
		Assert.state(this.transactionInterceptor != null, "No TransactionInterceptor set");
		return this.transactionInterceptor;
	}

	@Override
	public Pointcut getPointcut() {
		return this.pointcut;
	}

}
