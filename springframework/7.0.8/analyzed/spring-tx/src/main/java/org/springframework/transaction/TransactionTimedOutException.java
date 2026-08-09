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

package org.springframework.transaction;

/**
 * 当事务超时时抛出的异常。
 *
 * <p>若在给定事务指定的超时时间内尝试操作时已达截止时间，
 * Spring 本地事务策略会抛出此异常。
 *
 * <p>除每次事务操作前的此类检查外，Spring 本地事务策略
 * 还会将适当的超时值传递给资源操作（例如 JDBC Statement，
 * 让 JDBC 驱动遵守超时）。若操作超时，此类操作通常会抛出
 * 原生资源异常（例如 JDBC SQLException），
 * 在相应 DAO 中转换为 Spring 的 DataAccessException
 *（例如使用 Spring JdbcTemplate 时）。
 *
 * <p>在 JTA 环境中，由 JTA 事务协调器应用事务超时。
 * 通常相应的 JTA 感知连接池会执行超时检查并抛出
 * 对应的原生资源异常（例如 JDBC SQLException）。
 *
 * @author Juergen Hoeller
 * @since 1.1.5
 * @see org.springframework.transaction.support.ResourceHolderSupport#getTimeToLiveInMillis
 * @see java.sql.Statement#setQueryTimeout
 * @see java.sql.SQLException
 */
@SuppressWarnings("serial")
public class TransactionTimedOutException extends TransactionException {

	/**
	 * TransactionTimedOutException 的构造方法。
	 * @param msg 详细消息
	 */
	public TransactionTimedOutException(String msg) {
		super(msg);
	}

	/**
	 * TransactionTimedOutException 的构造方法。
	 * @param msg 详细消息
	 * @param cause 所用事务 API 的根因
	 */
	public TransactionTimedOutException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
