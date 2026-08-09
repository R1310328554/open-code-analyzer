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
 * JdbcTemplate 使用的三个核心回调接口之一。
 * 由 JdbcTemplate 提供 Connection，本接口负责创建 CallableStatement；
 * 实现类需提供 SQL 及必要参数。
 *
 * <p>实现类<i>无需</i>处理操作中可能抛出的 SQLException；
 * JdbcTemplate 会适当捕获并处理。
 *
 * <p>若能为 CallableStatement 创建提供所用 SQL，
 * 还应实现 SqlProvider，以便异常时提供更完整上下文。
 *
 * @author Rod Johnson
 * @author Thomas Risberg
 * @see JdbcTemplate#execute(CallableStatementCreator, CallableStatementCallback)
 * @see JdbcTemplate#call
 * @see SqlProvider
 */
@FunctionalInterface
public interface CallableStatementCreator {

	/**
	 * 在此连接上创建 CallableStatement。
	 * @param con 用于创建语句的 Connection
	 * @return CallableStatement
	 * @throws SQLException 实现中可能抛出，无需自行捕获；JdbcTemplate 会处理
	 */
	CallableStatement createCallableStatement(Connection con) throws SQLException;

}
