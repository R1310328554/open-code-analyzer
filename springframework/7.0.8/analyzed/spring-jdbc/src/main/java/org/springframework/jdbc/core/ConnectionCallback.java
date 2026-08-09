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
 * 在 JDBC Connection 上执行代码的通用回调接口。
 * 可在单个 Connection 上用任意类型与数量的 Statement 执行任意次操作。
 *
 * <p>特别适用于委托给期望 Connection 并抛出 SQLException 的既有数据访问代码。
 * 新代码强烈建议使用 JdbcTemplate 更具体的操作，如 {@code query} 或 {@code update} 变体。
 *
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
	 * 由 {@code JdbcTemplate.execute} 以活动 JDBC Connection 调用。
	 * 无需关心激活或关闭 Connection，亦无需处理事务。
	 * <p>若无线程绑定 JDBC 事务（由 DataSourceTransactionManager 启动），
	 * 代码将按 JDBC 连接自身语义执行。若 JdbcTemplate 使用 JTA 感知 DataSource，
	 * 且 JTA 事务活动，则 JDBC Connection 及回调代码亦具事务性。
	 * <p>可返回回调内创建的结果对象，如领域对象或其集合。
	 * 单步操作有专门支持：见 {@code JdbcTemplate.queryForObject} 等。
	 * 抛出的 RuntimeException 视为应用异常，传播给模板调用方。
	 * @param con 活动 JDBC Connection
	 * @return 结果对象，无则 {@code null}
	 * @throws SQLException JDBC 方法抛出时，由 SQLExceptionTranslator 转为 DataAccessException
	 * @throws DataAccessException 自定义异常时
	 * @see JdbcTemplate#queryForObject(String, Class)
	 * @see JdbcTemplate#queryForRowSet(String)
	 */
	T doInConnection(Connection con) throws SQLException, DataAccessException;

}
