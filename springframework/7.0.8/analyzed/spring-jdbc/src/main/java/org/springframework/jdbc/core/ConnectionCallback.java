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
import java.sql.SQLException;

import org.jspecify.annotations.Nullable;

import org.springframework.dao.DataAccessException;

/**
 * 在 JDBC 连接上操作的代码的通用回调接口。允许使用任何类型和数量的语句在单个连接上执行任意数量的操作。
 * <p>这对于委托给需要连接工作并抛出 SQLException 的现有数据访问代码特别有用。对于新编写的代码，强烈建议使用 JdbcTemplate 更具体的操作，例如 {@c
 * ode query} 或 {@code update} 变体。
 * @author Juergen Hoeller
 * @since 1.1.3
 * @param <T> 结果类型
 * @see JdbcTemplate#execute(ConnectionCallback)
 * @see JdbcTemplate#query
 * @see JdbcTemplate#update
 */
@FunctionalInterface
public interface ConnectionCallback<T extends @Nullable Object> {

	/**
	 * 由 {@code JdbcTemplate.execute} 使用活动的 JDBC 连接进行调用。不需要关心激活或关闭连接，或处理事务。 <p>如果在没有线程绑定 JDBC 事
	 * 务（由 DataSourceTransactionManager 启动）的情况下调用，则代码将简单地在 JDBC 连接上以其事务语义执行。如果 JdbcTemplate 配置为
	 * 使用 JTA 感知的数据源，则 JDBC 连接以及回调代码将是事务性的（如果 JTA 事务处于活动状态）。 <p>A允许返回在回调中创建的结果对象，即域对象或域对象的集合。请注
	 * 意，对单步操作有特殊支持：请参阅 {@code JdbcTemplate.queryForObject} 等。抛出的 RuntimeException 被视为应用程序异常：它会
	 * 传播到模板的调用者。
	 * @param con 活动 JDBC 连接
	 * @return 结果对象，如果没有则为 {@code null}
	 * @throws SQLException 如果由 JDBC 方法抛出，则由 SQLExceptionTranslator 自动转换为 DataAccessException
	 * @throws DataAccessException 如果出现自定义异常
	 * @see JdbcTemplate#queryForObject(String, Class)
	 * @see JdbcTemplate#queryForRowSet(String)
	 */
	T doInConnection(Connection con) throws SQLException, DataAccessException;

}
