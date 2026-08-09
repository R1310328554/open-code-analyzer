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

package org.springframework.jdbc.core;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * JdbcTemplate 类使用的三个中心回调接口之一。该接口在给定连接的情况下创建 CallableStatement，该连接由 JdbcTemplate 类提供。实现负责提
 * 供 SQL 和任何必要的参数。
 * <p> 实现 <i> 不需要 </i> 需要关注可能从它们尝试的操作中抛出的 SQLException。 JdbcTemplate 类将捕获并适当地处理
 * SQLException。
 * 如果<p>APreparedStatementCreator 能够提供用于创建PreparedStatement 的SQL，那么它还应该实现SqlProvider 接口。这样可
 * 以在出现异常时提供更好的上下文信息。
 * @author Rod Johnson
 * @author Thomas Risberg
 * @see JdbcTemplate#execute(CallableStatementCreator, CallableStatementCallback)
 * @see JdbcTemplate#call
 * @see SqlProvider
 */
@FunctionalInterface
public interface CallableStatementCreator {

	/**
	 * 在此连接中创建一个可调用语句。允许实现使用 CallableStatements。
	 * @param con 用于创建语句的连接
	 * @return 可调用语句
	 * @throws SQLException 无需捕获在该方法的实现中可能抛出的 SQLException。 JdbcTemplate 类将处理它们。
	 */
	CallableStatement createCallableStatement(Connection con) throws SQLException;

}
