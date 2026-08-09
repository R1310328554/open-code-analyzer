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

import java.util.Collection;

import org.jspecify.annotations.Nullable;

import org.springframework.transaction.TransactionDefinition;

/**
 * 本接口向 {@link TransactionDefinition} 添加 {@code rollbackOn} 规范。
 * 自定义 {@code rollbackOn} 仅能通过 AOP 实现，故位于 AOP 相关事务子包中。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Mark Paluch
 * @since 16.03.2003
 * @see DefaultTransactionAttribute
 * @see RuleBasedTransactionAttribute
 */
public interface TransactionAttribute extends TransactionDefinition {

	/**
	 * 返回与本事务属性关联的限定符值。
	 * <p>可用于选择相应的事务管理器处理该特定事务。
	 * @since 3.0
	 */
	@Nullable String getQualifier();

	/**
	 * 返回与本事务属性关联的标签。
	 * <p>可用于应用特定事务行为，或仅作描述性用途。
	 * @since 5.3
	 */
	Collection<String> getLabels();

	/**
	 * 遇到给定异常是否应回滚？
	 * @param ex 要评估的异常
	 * @return 是否执行回滚
	 */
	boolean rollbackOn(Throwable ex);

}
