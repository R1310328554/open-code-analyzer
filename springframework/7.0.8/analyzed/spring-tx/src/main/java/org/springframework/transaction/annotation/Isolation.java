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
 * 表示与 {@link Transactional @Transactional} 注解配合使用的事务隔离级别的枚举，
 * 对应 {@link TransactionDefinition} 接口。
 *
 * @author Colin Sampaleanu
 * @author Juergen Hoeller
 * @since 1.2
 */
public enum Isolation {

	/**
	 * 使用底层数据存储的默认隔离级别。
	 * <p>其他所有级别对应 JDBC 隔离级别。
	 * @see java.sql.Connection
	 */
	DEFAULT(TransactionDefinition.ISOLATION_DEFAULT),

	/**
	 * 表示可能发生脏读、不可重复读和幻读的常量。
	 * <p>此级别允许一个事务修改的行在变更提交前
	 * 被另一事务读取（"脏读"）。若任一变更被回滚，
	 * 第二事务将读到无效行。
	 * @see java.sql.Connection#TRANSACTION_READ_UNCOMMITTED
	 */
	READ_UNCOMMITTED(TransactionDefinition.ISOLATION_READ_UNCOMMITTED),

	/**
	 * 表示防止脏读；不可重复读和幻读仍可能发生。
	 * <p>此级别仅禁止事务读取含未提交变更的行。
	 * @see java.sql.Connection#TRANSACTION_READ_COMMITTED
	 */
	READ_COMMITTED(TransactionDefinition.ISOLATION_READ_COMMITTED),

	/**
	 * 表示防止脏读和不可重复读；幻读仍可能发生。
	 * <p>此级别禁止读取含未提交变更的行，
	 * 也禁止一事务读行、二事务改行、一事务再读却得到不同值
	 *（"不可重复读"）的情况。
	 * @see java.sql.Connection#TRANSACTION_REPEATABLE_READ
	 */
	REPEATABLE_READ(TransactionDefinition.ISOLATION_REPEATABLE_READ),

	/**
	 * 表示防止脏读、不可重复读和幻读的常量。
	 * <p>此级别包含 {@link #REPEATABLE_READ} 的所有限制，
	 * 并进一步禁止：一事务读取满足 {@code WHERE} 条件的所有行，
	 * 二事务插入满足该 {@code WHERE} 条件的行，
	 * 一事务再次按相同条件读取时多出一行（"幻读"）。
	 * @see java.sql.Connection#TRANSACTION_SERIALIZABLE
	 */
	SERIALIZABLE(TransactionDefinition.ISOLATION_SERIALIZABLE);


	private final int value;


	Isolation(int value) {
		this.value = value;
	}

	public int value() {
		return this.value;
	}

}
