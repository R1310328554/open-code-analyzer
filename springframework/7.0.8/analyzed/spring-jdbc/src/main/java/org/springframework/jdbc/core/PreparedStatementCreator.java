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
 * JdbcTemplate 类使用的两个核心回调接口之一。
 * 本接口在给定连接上创建 PreparedStatement，连接由 JdbcTemplate 提供。
 * 实现者负责提供 SQL 及所有必要参数。
 *
 * <p>实现者<i>无需</i>关心其操作可能抛出的 SQLExceptions。
 * JdbcTemplate 会适当捕获并处理 SQLExceptions。
 *
 * <p>若 PreparedStatementCreator 能提供创建 PreparedStatement 所用的 SQL，
 * 还应实现 SqlProvider 接口，以便异常时提供更丰富的上下文信息。
 *
 * @author Rod Johnson
 * @see JdbcTemplate#execute(PreparedStatementCreator, PreparedStatementCallback)
 * @see JdbcTemplate#query(PreparedStatementCreator, RowCallbackHandler)
 * @see JdbcTemplate#update(PreparedStatementCreator)
 * @see SqlProvider
 */
@FunctionalInterface
public interface PreparedStatementCreator {

	/**
	 * 在此连接上创建语句。允许实现者使用 PreparedStatement。
	 * JdbcTemplate 将关闭创建的语句。
	 * @param con 用于创建语句的连接
	 * @return PreparedStatement
	 * @throws SQLException 实现中可能抛出的 SQLException 无需捕获，
	 * JdbcTemplate 会处理。
	 */
	PreparedStatement createPreparedStatement(Connection con) throws SQLException;

}
