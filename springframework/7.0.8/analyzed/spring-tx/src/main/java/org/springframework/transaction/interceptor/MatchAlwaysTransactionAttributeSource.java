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

import java.io.Serializable;
import java.lang.reflect.Method;

import org.jspecify.annotations.Nullable;

import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;

/**
 * {@link TransactionAttributeSource} 的极简实现：对传入的所有方法始终返回
 * 同一个 {@link TransactionAttribute}。可显式指定该属性，否则默认为
 * PROPAGATION_REQUIRED。适用于希望事务拦截器处理的所有方法
 * 使用相同事务属性的场景。
 *
 * @author Colin Sampaleanu
 * @author Juergen Hoeller
 * @since 15.10.2003
 * @see org.springframework.transaction.interceptor.TransactionProxyFactoryBean
 * @see org.springframework.aop.framework.autoproxy.BeanNameAutoProxyCreator
 */
@SuppressWarnings("serial")
public class MatchAlwaysTransactionAttributeSource implements TransactionAttributeSource, Serializable {

	private TransactionAttribute transactionAttribute = new DefaultTransactionAttribute();


	/**
	 * 允许指定事务属性，可使用字符串形式，例如 "PROPAGATION_REQUIRED"。
	 * @param transactionAttribute 要使用的事务属性（字符串形式）。
	 * @see org.springframework.transaction.interceptor.TransactionAttributeEditor
	 */
	public void setTransactionAttribute(TransactionAttribute transactionAttribute) {
		if (transactionAttribute instanceof DefaultTransactionAttribute dta) {
			dta.resolveAttributeStrings(null);
		}
		this.transactionAttribute = transactionAttribute;
	}


	@Override
	public @Nullable TransactionAttribute getTransactionAttribute(Method method, @Nullable Class<?> targetClass) {
		return (ClassUtils.isUserLevelMethod(method) ? this.transactionAttribute : null);
	}


	@Override
	public boolean equals(@Nullable Object other) {
		return (this == other || (other instanceof MatchAlwaysTransactionAttributeSource that &&
				ObjectUtils.nullSafeEquals(this.transactionAttribute, that.transactionAttribute)));
	}

	@Override
	public int hashCode() {
		return MatchAlwaysTransactionAttributeSource.class.hashCode();
	}

	@Override
	public String toString() {
		return getClass().getName() + ": " + this.transactionAttribute;
	}

}
