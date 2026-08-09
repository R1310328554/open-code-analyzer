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
import java.sql.SQLException;

import org.jspecify.annotations.Nullable;

import org.springframework.dao.DataAccessException;

/**
 * 在 CallableStatement 上执行代码的通用回调接口。
 * 可在单个 CallableStatement 上执行任意次操作，
 * 例如单次 execute 或参数不同的多次 execute。
 *
 * <p>JdbcTemplate 内部使用，应用代码亦可用。
 * 传入的 CallableStatement 可由框架或自定义 CallableStatementCreator 创建；
 * 后者通常不必，因多数自定义回调只需标准 CallableStatement。
 * 自定义回调会自行设参，故亦无需 CallableStatementCreator。
 *
 * @author Juergen Hoeller
 * @since 16.03.2004
 * @param <T> 结果类型
 * @see JdbcTemplate#execute(String, CallableStatementCallback)
 * @see JdbcTemplate#execute(CallableStatementCreator, CallableStatementCallback)
 */
@FunctionalInterface
public interface CallableStatementCallback<T extends @Nullable Object> {

	/**
	 * 由 {@code JdbcTemplate.execute} 以活动 JDBC CallableStatement 调用。
	 * 无需关心关闭 Statement 或 Connection，亦无需处理事务；
	 * 均由 Spring JdbcTemplate 负责。
	 *
	 * <p><b>注意：</b>打开的 ResultSet 应在回调实现的 finally 块中关闭。
	 * Spring 在回调返回后关闭 Statement，但不保证 ResultSet 资源已释放：
	 * Statement 可能被连接池复用，{@code close} 仅归还池而非物理关闭。
	 *
	 * <p>若无线程绑定 JDBC 事务（由 DataSourceTransactionManager 启动），
	 * 代码将按 JDBC 连接自身语义执行。若 JdbcTemplate 使用 JTA 感知 DataSource，
	 * 且 JTA 事务活动，则 JDBC 连接及回调代码亦具事务性。
	 *
	 * <p>可返回回调内创建的结果对象，如领域对象或其集合。
	 * 抛出的 RuntimeException 视为应用异常，传播给模板调用方。
	 * @param cs 活动 JDBC CallableStatement
	 * @return 结果对象，无则 {@code null}
	 * @throws SQLException JDBC 方法抛出时，由 SQLExceptionTranslator 转为 DataAccessException
	 * @throws DataAccessException 自定义异常时
	 */
	T doInCallableStatement(CallableStatement cs) throws SQLException, DataAccessException;

}
