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
 * 在PreparedStatement上操作的代码的通用回调接口。允许在单个PreparedStatement上执行任意数量的操作，例如单个{@code executeUpdat
 * e}调用或具有不同参数的重复{@code executeUpdate}调用。
 * <p> 由 JdbcTemplate 在内部使用，但对于应用程序代码也很有用。请注意，传入的PreparedStatement 可以由框架或自定义PreparedStateme
 * ntCreator 创建。然而，后者几乎没有必要，因为大多数自定义回调操作都会执行更新，在这种情况下，标准的PreparedStatement就可以了。自定义操作始终会自行设置
 * 参数值，因此也不需要PreparedStatementCreator 功能。
 * @author Juergen Hoeller
 * @since 16.03.2004
 * @param <T> 结果类型
 * @see JdbcTemplate#execute(String, PreparedStatementCallback)
 * @see JdbcTemplate#execute(PreparedStatementCreator, PreparedStatementCallback)
 */
@FunctionalInterface
public interface PreparedStatementCallback<T extends @Nullable Object> {

	/**
	 * 由 {@code JdbcTemplate.execute} 使用活动的 JDBCPreparedStatement 进行调用。不需要关心关闭Statement或Connect
	 * ion，或者处理事务：这一切都将由Spring的JdbcTemplate处理。 <p><b>NOTE:</b> 打开的任何结果集都应在回调实现中的finally 块中关闭。 S
	 * pring 将在回调返回后关闭 Statement 对象，但这并不一定意味着 ResultSet 资源将被关闭：Statement 对象可能会被连接池池化，{@code clo
	 * se} 调用仅将对象返回到池中，但不会物理关闭资源。 <p>如果在没有线程绑定 JDBC 事务（由 DataSourceTransactionManager 启动）的情况下调用
	 * ，则代码将简单地在 JDBC 连接上以其事务语义执行。如果 JdbcTemplate 配置为使用 JTA 感知的数据源，则 JDBC 连接以及回调代码将是事务性的（如果 JTA
	 *  事务处于活动状态）。 <p>A允许返回在回调中创建的结果对象，即域对象或域对象的集合。请注意，对单步操作有特殊支持：请参阅 JdbcTemplate.queryForObje
	 * ct 等。抛出的 RuntimeException 被视为应用程序异常，它会传播到模板的调用者。
	 * @param ps 主动 JDBC 准备语句
	 * @return 结果对象，如果没有则为 {@code null}
	 * @throws SQLException 如果由 JDBC 方法抛出，则由 SQLExceptionTranslator 自动转换为 DataAccessException
	 * @throws DataAccessException 如果出现自定义异常
	 * @see JdbcTemplate#queryForObject(String, Class, Object...)
	 * @see JdbcTemplate#queryForList(String, Object...)
	 */
	T doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException;

}
