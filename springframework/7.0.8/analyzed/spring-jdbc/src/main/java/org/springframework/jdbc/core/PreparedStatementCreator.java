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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * JdbcTemplate 类使用的两个中央回调接口之一。该接口在给定连接的情况下创建一个PreparedStatement，该连接由JdbcTemplate 类提供。实现负责提
 * 供 SQL 和任何必要的参数。
 * <p> 实现 <i> 不需要 </i> 需要关注可能从它们尝试的操作中抛出的 SQLException。 JdbcTemplate 类将捕获并适当地处理
 * SQLException。
 * 如果<p>APreparedStatementCreator 能够提供用于创建PreparedStatement 的SQL，那么它还应该实现SqlProvider 接口。这样可
 * 以在出现异常时提供更好的上下文信息。
 * @author Rod Johnson
 * @see JdbcTemplate#execute(PreparedStatementCreator, PreparedStatementCallback)
 * @see JdbcTemplate#query(PreparedStatementCreator, RowCallbackHandler)
 * @see JdbcTemplate#update(PreparedStatementCreator)
 * @see SqlProvider
 */
@FunctionalInterface
public interface PreparedStatementCreator {

	/**
	 * 就此创建一个声明。允许实现使用PreparedStatements。 JdbcTemplate 将关闭创建的语句。
	 * @param con 用于创建语句的连接
	 * @return 准备好的声明
	 * @throws SQLException 无需捕获在该方法的实现中可能抛出的 SQLException。 JdbcTemplate 类将处理它们。
	 */
	PreparedStatement createPreparedStatement(Connection con) throws SQLException;

}
