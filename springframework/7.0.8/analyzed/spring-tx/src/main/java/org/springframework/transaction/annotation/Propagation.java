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

package org.springframework.transaction.annotation;

import org.springframework.transaction.TransactionDefinition;

/**
 * 表示与 {@link Transactional} 注解配合使用的事务传播行为的枚举，
 * 对应 {@link TransactionDefinition} 接口。
 *
 * @author Colin Sampaleanu
 * @author Juergen Hoeller
 * @since 1.2
 */
public enum Propagation {

	/**
	 * 支持当前事务，不存在则创建新事务。
	 * 与同名 EJB 事务属性类似。
	 * <p>这是事务注解的默认设置。
	 */
	REQUIRED(TransactionDefinition.PROPAGATION_REQUIRED),

	/**
	 * 支持当前事务，不存在则以非事务方式执行。
	 * 与同名 EJB 事务属性类似。
	 * <p>注意：对支持事务同步的事务管理器，
	 * {@code SUPPORTS} 与完全无事务略有不同，
	 * 它定义了同步将适用的事务范围。
	 * 因此相同资源（JDBC Connection、Hibernate Session 等）
	 * 将在整个指定范围内共享。这取决于事务管理器的实际同步配置。
	 * @see org.springframework.transaction.support.AbstractPlatformTransactionManager#setTransactionSynchronization
	 */
	SUPPORTS(TransactionDefinition.PROPAGATION_SUPPORTS),

	/**
	 * 支持当前事务，不存在则抛出异常。
	 * 与同名 EJB 事务属性类似。
	 */
	MANDATORY(TransactionDefinition.PROPAGATION_MANDATORY),

	/**
	 * 创建新事务，若存在当前事务则挂起。
	 * 与同名 EJB 事务属性类似。
	 * <p><b>注意：</b>并非所有事务管理器都能开箱即用地挂起事务。
	 * 尤其 {@link org.springframework.transaction.jta.JtaTransactionManager}
	 * 需要向其提供 {@code jakarta.transaction.TransactionManager}
	 *（在标准 Jakarta EE 中因服务器而异）。
	 * @see org.springframework.transaction.jta.JtaTransactionManager#setTransactionManager
	 */
	REQUIRES_NEW(TransactionDefinition.PROPAGATION_REQUIRES_NEW),

	/**
	 * 以非事务方式执行，若存在当前事务则挂起。
	 * 与同名 EJB 事务属性类似。
	 * <p><b>注意：</b>并非所有事务管理器都能开箱即用地挂起事务。
	 * 尤其 {@link org.springframework.transaction.jta.JtaTransactionManager}
	 * 需要向其提供 {@code jakarta.transaction.TransactionManager}
	 *（在标准 Jakarta EE 中因服务器而异）。
	 * @see org.springframework.transaction.jta.JtaTransactionManager#setTransactionManager
	 */
	NOT_SUPPORTED(TransactionDefinition.PROPAGATION_NOT_SUPPORTED),

	/**
	 * 以非事务方式执行，若存在事务则抛出异常。
	 * 与同名 EJB 事务属性类似。
	 */
	NEVER(TransactionDefinition.PROPAGATION_NEVER),

	/**
	 * 若存在当前事务则在嵌套事务中执行，否则行为同 {@code REQUIRED}。
	 * EJB 中无对应特性。
	 * <p>注意：嵌套事务的实际创建仅适用于特定事务管理器。
	 * 开箱即用仅适用于 JDBC DataSourceTransactionManager。
	 * 部分 JTA 提供者也可能支持嵌套事务。
	 * @see org.springframework.jdbc.datasource.DataSourceTransactionManager
	 */
	NESTED(TransactionDefinition.PROPAGATION_NESTED);


	private final int value;


	Propagation(int value) {
		this.value = value;
	}

	public int value() {
		return this.value;
	}

}
