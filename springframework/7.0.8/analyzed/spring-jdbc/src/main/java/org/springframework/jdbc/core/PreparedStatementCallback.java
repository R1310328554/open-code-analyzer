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

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.jspecify.annotations.Nullable;

import org.springframework.dao.DataAccessException;

/**
 * 在 PreparedStatement 上执行代码的通用回调接口。
 * 允许在单个 PreparedStatement 上执行任意次操作，
 * 例如单次 {@code executeUpdate} 调用，或带不同参数的多次 {@code executeUpdate} 调用。
 *
 * <p>由 JdbcTemplate 内部使用，应用代码同样适用。
 * 注意传入的 PreparedStatement 可能由框架或自定义 PreparedStatementCreator 创建。
 * 但后者几乎不需要——大多数自定义回调执行更新，标准 PreparedStatement 即可。
 * 自定义操作总会自行设置参数值，因此也无需 PreparedStatementCreator 能力。
 *
 * @author Juergen Hoeller
 * @since 16.03.2004
 * @param <T> 结果类型
 * @see JdbcTemplate#execute(String, PreparedStatementCallback)
 * @see JdbcTemplate#execute(PreparedStatementCreator, PreparedStatementCallback)
 */
@FunctionalInterface
public interface PreparedStatementCallback<T extends @Nullable Object> {

	/**
	 * 由 {@code JdbcTemplate.execute} 调用，传入活动的 JDBC PreparedStatement。
	 * 无需关心关闭 Statement 或 Connection，也无需处理事务——均由 Spring JdbcTemplate 负责。
	 * <p><b>注意：</b>回调实现中打开的 ResultSet 应在 finally 块中关闭。
	 * Spring 在回调返回后关闭 Statement 对象，但这不一定意味着 ResultSet 资源已关闭：
	 * Statement 可能被连接池复用，{@code close} 仅将对象归还池中，
	 * 未必物理关闭底层资源。
	 * <p>若无线程绑定的 JDBC 事务（由 DataSourceTransactionManager 启动），
	 * 代码将直接在 JDBC 连接上按其事务语义执行。
	 * 若 JdbcTemplate 配置为使用 JTA 感知 DataSource，
	 * 且 JTA 事务处于活动状态，则 JDBC 连接及回调代码均在事务中运行。
	 * <p>允许返回回调内创建的结果对象，如领域对象或领域对象集合。
	 * 单步操作有专门支持：参见 JdbcTemplate.queryForObject 等。
	 * 抛出的 RuntimeException 视为应用异常，会传播给模板调用方。
	 * @param ps 活动的 JDBC PreparedStatement
	 * @return 结果对象，若无则 {@code null}
	 * @throws SQLException 若 JDBC 方法抛出，将由 SQLExceptionTranslator 自动转换为 DataAccessException
	 * @throws DataAccessException 自定义异常时
	 * @see JdbcTemplate#queryForObject(String, Class, Object...)
	 * @see JdbcTemplate#queryForList(String, Object...)
	 */
	T doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException;

}
