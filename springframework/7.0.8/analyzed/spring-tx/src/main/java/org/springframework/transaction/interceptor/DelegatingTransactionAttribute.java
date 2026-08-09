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
import java.util.Collection;

import org.jspecify.annotations.Nullable;

import org.springframework.transaction.support.DelegatingTransactionDefinition;

/**
 * 将所有调用委托给给定目标 {@link TransactionAttribute} 实例的
 * {@link TransactionAttribute} 实现。为抽象类，旨在被继承，
 * 子类覆盖不应简单委托给目标实例的特定方法。
 *
 * @author Juergen Hoeller
 * @author Mark Paluch
 * @since 1.2
 */
@SuppressWarnings("serial")
public abstract class DelegatingTransactionAttribute extends DelegatingTransactionDefinition
		implements TransactionAttribute, Serializable {

	private final TransactionAttribute targetAttribute;


	/**
	 * 为给定目标属性创建 DelegatingTransactionAttribute。
	 * @param targetAttribute 要委托的目标 TransactionAttribute
	 */
	public DelegatingTransactionAttribute(TransactionAttribute targetAttribute) {
		super(targetAttribute);
		this.targetAttribute = targetAttribute;
	}


	@Override
	public @Nullable String getQualifier() {
		return this.targetAttribute.getQualifier();
	}

	@Override
	public Collection<String> getLabels() {
		return this.targetAttribute.getLabels();
	}

	@Override
	public boolean rollbackOn(Throwable ex) {
		return this.targetAttribute.rollbackOn(ex);
	}

}
