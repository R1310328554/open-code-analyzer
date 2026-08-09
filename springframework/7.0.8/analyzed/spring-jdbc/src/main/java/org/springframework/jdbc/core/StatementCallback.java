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

import java.sql.SQLException;
import java.sql.Statement;

import org.jspecify.annotations.Nullable;

import org.springframework.dao.DataAccessException;

/**
 * 在 JDBC Statement 上执行代码的通用回调接口。
 * 可在单个 Statement 上执行任意次操作，
 * 例如单次 {@code executeUpdate} 或 SQL 不同的多次 {@code executeUpdate}。
 *
 * <p>JdbcTemplate 内部使用，应用代码亦可用。
 *
 * @author Juergen Hoeller
 * @since 16.03.2004
 * @param <T> 结果类型
 * @see JdbcTemplate#execute(StatementCallback)
 */
@FunctionalInterface
public interface StatementCallback<T extends @Nullable Object> {

	/**
	 * 由 {@code JdbcTemplate.execute} 以活动 JDBC Statement 调用。
	 * 无需关心关闭 Statement 或 Connection，亦无需处理事务；
	 * 均由 Spring JdbcTemplate 负责。
	 * <p><b>注意：</b>打开的 ResultSet 应在回调实现的 finally 块中关闭。
	 * Spring 在回调返回后关闭 Statement，但不保证 ResultSet 资源已释放：
	 * Statement 可能被连接池复用，{@code close} 仅归还池而非物理关闭。
	 * <p>若无线程绑定 JDBC 事务（由 DataSourceTransactionManager 启动），
	 * 代码将按 JDBC 连接自身语义执行。若 JdbcTemplate 使用 JTA 感知 DataSource，
	 * 且 JTA 事务活动，则 JDBC 连接及回调代码亦具事务性。
	 * <p>可返回回调内创建的结果对象，如领域对象或其集合。
	 * 单步操作有专门支持：见 JdbcTemplate.queryForObject 等。
	 * 抛出的 RuntimeException 视为应用异常，传播给模板调用方。
	 * @param stmt 活动 JDBC Statement
	 * @return 结果对象，无则 {@code null}
	 * @throws SQLException JDBC 方法抛出时，由 SQLExceptionTranslator 转为 DataAccessException
	 * @throws DataAccessException 自定义异常时
	 * @see JdbcTemplate#queryForObject(String, Class)
	 * @see JdbcTemplate#queryForRowSet(String)
	 */
	T doInStatement(Statement stmt) throws SQLException, DataAccessException;

}
