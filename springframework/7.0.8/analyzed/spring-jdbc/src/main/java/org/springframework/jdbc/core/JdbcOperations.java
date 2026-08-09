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

/* ===== [OCA 中文解析] =====
文件意图总览

Spring JDBC 核心操作接口：定义 Connection/Statement/PreparedStatement/CallableStatement 上的查询、更新、批处理与存储过程调用契约；由 JdbcTemplate 实现，便于测试 mock。
===== [OCA 中文解析结束] ===== */
package org.springframework.jdbc.core;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.jspecify.annotations.Nullable;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;

/* ===== [OCA 中文解析] =====
interface JdbcOperations — 意图说明

JDBC 模板层对外契约：封装连接获取、语句执行、异常翻译与回调风格 API，是 JdbcTemplate/NamedParameterJdbcTemplate 的共同抽象。

（本注释由 open-code-analyzer 生成，置于原有文档注释之前）
===== [OCA 中文解析结束] ===== */
/**
 * 定义 Spring JDBC 基本操作的接口。
 * <p>由 {@link JdbcTemplate} 实现。通常注入接口类型以增强可测试性（便于 mock/stub）。
 * <p>相比 mock 整个 JDBC 栈，mock 本接口更轻量；也可考虑 {@code spring-test} 中的集成测试支持。
 * <p><b>注意：自 6.1 起推荐使用 {@link org.springframework.jdbc.core.simple.JdbcClient} 作为统一 JDBC 访问外观。</b> {@code JdbcClient} 提供流式 API，底层仍委托 {@code JdbcOperations}/{@code NamedParameterJdbcOperations}。
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see JdbcTemplate
 * @see org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations
 */
public interface JdbcOperations {

	//-------------------------------------------------------------------------
	// 处理普通 java.sql.Connection 的方法
	//-------------------------------------------------------------------------

	/**
	 * 在 Spring 托管的 {@link Connection} 上执行 {@link ConnectionCallback}。
	 * <p>参与 Spring 事务并将 {@link SQLException} 翻译为 {@link DataAccessException}。
	 * @param action 指定操作的回调
	 * @return 回调返回的结果，无则 {@code null}
	 * @throws DataAccessException 数据访问失败时
	 */
	<T extends @Nullable Object> T execute(ConnectionCallback<T> action) throws DataAccessException;


	//-------------------------------------------------------------------------
	// 处理静态 SQL 的方法 (java.sql.Statement)
	//-------------------------------------------------------------------------

	/**
	 * 执行 JDBC 数据访问操作，作为作用于 JDBC 语句的回调操作来实现。这允许在 Spring 的托管 JDBC 环境中在单个语句上实现任意数据访问操作：即参与 Spring
	 *  管理的事务并将 JDBC SQLException 转换为 Spring 的 DataAccessException 层次结构。 <p>回调操作可以返回结果对象，例如域对象或
	 * 域对象的集合。
	 * @param action 指定操作的回调
	 * @return 操作返回的结果对象，如果没有则返回 {@code null}
	 * @throws DataAccessException 如果有任何问题
	 */
	<T extends @Nullable Object> T execute(StatementCallback<T> action) throws DataAccessException;

	/**
	 * 发出单个 SQL 执行，通常是 DDL 语句。
	 * @param sql 要执行的静态 SQL
	 * @throws DataAccessException 如果有任何问题
	 */
	void execute(String sql) throws DataAccessException;

	/**
	 * 执行给定静态 SQL 的查询，使用 ResultSetExtractor 读取 ResultSet。 <p>U 使用 JDBC
	 * 语句，而不是PreparedStatement。如果要使用PreparedStatement执行静态查询，请使用重载的{@code query}方法并将{@code
	 * null}作为参数数组。
	 * @param sql 要执行的 SQL 查询
	 * @param rse 将提取所有结果行的回调
	 * @return 任意结果对象，由 ResultSetExtractor 返回
	 * @throws DataAccessException 如果执行查询有任何问题
	 * @see #query(String, ResultSetExtractor, Object...)
	 */
	<T extends @Nullable Object> T query(String sql, ResultSetExtractor<T> rse) throws DataAccessException;

	/**
	 * 执行给定静态 SQL 的查询，使用 RowCallbackHandler 按行读取 ResultSet。 <p>U 使用 JDBC
	 * 语句，而不是PreparedStatement。如果要使用PreparedStatement执行静态查询，请使用重载的{@code query}方法并将{@code
	 * null}作为参数数组。
	 * @param sql 要执行的 SQL 查询
	 * @param rch 将提取结果的回调，一次一行
	 * @throws DataAccessException 如果执行查询有任何问题
	 * @see #query(String, RowCallbackHandler, Object...)
	 */
	void query(String sql, RowCallbackHandler rch) throws DataAccessException;

	/**
	 * 执行给定静态 SQL 的查询，通过 RowMapper 将每一行映射到结果对象。 <p>U 使用 JDBC
	 * 语句，而不是PreparedStatement。如果要使用PreparedStatement执行静态查询，请使用重载的{@code query}方法并将{@code
	 * null}作为参数数组。
	 * @param sql 要执行的 SQL 查询
	 * @param rowMapper 将映射每行一个对象的回调
	 * @return 结果列表，包含映射对象
	 * @throws DataAccessException 如果执行查询有任何问题
	 * @see #query(String, RowMapper, Object...)
	 */
	<T extends @Nullable Object> List<T> query(String sql, RowMapper<T> rowMapper) throws DataAccessException;

	/**
	 * 执行给定静态 SQL 的查询，通过 RowMapper 将每一行映射到结果对象，并将其转换为可迭代且可关闭的 Stream。 <p>U 使用 JDBC
	 * 语句，而不是PreparedStatement。如果要使用PreparedStatement执行静态查询，请使用重载的{@code query}方法并将{@code
	 * null}作为参数数组。
	 * @param sql 要执行的 SQL 查询
	 * @param rowMapper 将映射每行一个对象的回调
	 * @return 结果流，包含映射对象，完全处理后需要关闭（例如，通过 try-with-resources 子句）
	 * @throws DataAccessException 如果执行查询有任何问题
	 * @since 5.3
	 * @see #queryForStream(String, RowMapper, Object...)
	 */
	<T extends @Nullable Object> Stream<T> queryForStream(String sql, RowMapper<T> rowMapper) throws DataAccessException;

	/**
	 * 执行给定静态 SQL 的查询，通过 RowMapper 将单个结果行映射到结果对象。 <p>U 使用 JDBC
	 * 语句，而不是PreparedStatement。如果要使用PreparedStatement执行静态查询，请使用重载的{@link
	 * #queryForObject(String, RowMapper, Object...)}方法并将{@code null}作为参数数组。
	 * @param sql 要执行的 SQL 查询
	 * @param rowMapper 将映射每行一个对象的回调
	 * @return 单个映射对象（如果给定的 {@link RowMapper} 返回 {@code null}，则可能是 {@code null}）
	 * @throws org.springframework.dao.IncorrectResultSizeDataAccessException 如果查询没有返回恰好一行
	 * @throws DataAccessException 如果执行查询有任何问题
	 * @see #queryForObject(String, RowMapper, Object...)
	 */
	<T extends @Nullable Object> T queryForObject(String sql, RowMapper<T> rowMapper) throws DataAccessException;

	/**
	 * 给定静态 SQL，对结果对象执行查询。 <p>U 使用 JDBC 语句，而不是PreparedStatement。如果要使用PreparedStatement执行静态查询，请使
	 * 用重载的{@link #queryForObject(String, Class, Object...)}方法并将{@code null}作为参数数组。 <p>此方法对于运行具
	 * 有已知结果的静态 SQL 非常有用。查询预计是单行/单列查询；返回的结果将直接映射到相应的对象类型。
	 * @param sql 要执行的 SQL 查询
	 * @param requiredType 结果对象期望匹配的类型
	 * @return 所需类型的结果对象，或 {@code null}（如果 SQL NULL）
	 * @throws org.springframework.dao.IncorrectResultSizeDataAccessException 如果查询没有返回恰好一行
	 * @throws org.springframework.jdbc.IncorrectResultSetColumnCountException 如果查询不返回包含单个列的行
	 * @throws DataAccessException 如果执行查询有任何问题
	 * @see #queryForObject(String, Class, Object...)
	 */
	<T> @Nullable T queryForObject(String sql, Class<T> requiredType) throws DataAccessException;

	/**
	 * 给定静态 SQL，执行结果映射查询。 <p>U 使用 JDBC
	 * 语句，而不是PreparedStatement。如果要使用PreparedStatement执行静态查询，请使用重载的{@link #queryForMap(String,
	 * Object...)}方法并将{@code null}作为参数数组。 <p>查询预计是单行查询；结果行将映射到一个 Map（每列一个条目，使用列名作为键）。
	 * @param sql 要执行的 SQL 查询
	 * @return 结果映射（每列一个条目，以列名作为键）
	 * @throws org.springframework.dao.IncorrectResultSizeDataAccessException 如果查询没有返回恰好一行
	 * @throws DataAccessException 如果执行查询有任何问题
	 * @see #queryForMap(String, Object...)
	 * @see ColumnMapRowMapper
	 */
	Map<String, @Nullable Object> queryForMap(String sql) throws DataAccessException;

	/**
	 * 给定静态 SQL，执行结果列表的查询。 <p>U 使用 JDBC 语句，而不是PreparedStatement。如果要使用PreparedStatement执行静态查询，请使
	 * 用重载的{@code queryForList}方法并将{@code null}作为参数数组。 <p>的结果将映射到结果对象的列表（每行一个条目），每个结果对象都与指定的元素类
	 * 型匹配。
	 * @param sql 要执行的 SQL 查询
	 * @param elementType 结果列表中所需的元素类型（例如 {@code Integer.class}）
	 * @return 与指定元素类型匹配的对象列表
	 * @throws DataAccessException 如果执行查询有任何问题
	 * @see #queryForList(String, Class, Object...)
	 * @see SingleColumnRowMapper
	 */
	<T> List<@Nullable T> queryForList(String sql, Class<T> elementType) throws DataAccessException;

	/**
	 * 给定静态 SQL，执行结果列表的查询。 <p>U 使用 JDBC 语句，而不是PreparedStatement。如果要使用PreparedStatement执行静态查询，请使
	 * 用重载的{@code queryForList}方法并将{@code null}作为参数数组。 <p>的结果将被映射到一个List（每行一个条目）的Maps（每列一个条目，使用
	 * 列名作为键）。列表中的每个元素都将采用此接口的 {@code queryForMap} 方法返回的形式。
	 * @param sql 要执行的 SQL 查询
	 * @return 每行包含一个 Map 的列表
	 * @throws DataAccessException 如果执行查询有任何问题
	 * @see #queryForList(String, Object...)
	 */
	List<Map<String, @Nullable Object>> queryForList(String sql) throws DataAccessException;

	/**
	 * 在给定静态 SQL 的情况下，执行 SqlRowSet 的查询。 <p>U 使用 JDBC
	 * 语句，而不是PreparedStatement。如果要使用PreparedStatement执行静态查询，请使用重载的{@code
	 * queryForRowSet}方法并将{@code null}作为参数数组。 <p>的结果将被映射到一个SqlRowSet，它以断开连接的方式保存数据。该包装器将转换任何抛出的
	 * SQLException。 <p> 请注意，对于默认实现，需要在运行时提供 JDBC RowSet 支持：默认情况下，使用标准 JDBC {@code
	 * CachedRowSet}。
	 * @param sql 要执行的 SQL 查询
	 * @return SqlRowSet 表示（可能是 {@code javax.sql.rowset.CachedRowSet} 的包装器）
	 * @throws DataAccessException 如果执行查询有任何问题
	 * @see #queryForRowSet(String, Object...)
	 * @see SqlRowSetResultSetExtractor
	 * @see javax.sql.rowset.CachedRowSet
	 */
	SqlRowSet queryForRowSet(String sql) throws DataAccessException;

	/**
	 * 发出单个 SQL 更新操作（例如插入、更新或删除语句）。
	 * @param sql 要执行的静态 SQL
	 * @return 受影响的行数
	 * @throws DataAccessException 如果有任何问题。
	 */
	int update(String sql) throws DataAccessException;

	/**
	 * 使用批处理在单个 JDBC 语句上发出多个 SQL 更新。如果 JDBC 驱动程序不支持批量更新，<p> 将回退到单个语句上的单独更新。
	 * @param sql 定义将要执行的 SQL 语句的数组。
	 * @return 每个语句影响的行数数组
	 * @throws DataAccessException 如果执行批处理有任何问题
	 */
	int[] batchUpdate(String... sql) throws DataAccessException;


	//-------------------------------------------------------------------------
	// 处理准备好的语句的方法
	//-------------------------------------------------------------------------

	/**
	 * 执行 JDBC 数据访问操作，作为作用于 JDBC PreparedStatement 的回调操作来实现。这允许在 Spring 的托管 JDBC 环境中在单个语句上实现任意数
	 * 据访问操作：即参与 Spring 管理的事务并将 JDBC SQLException 转换为 Spring 的 DataAccessException 层次结构。 <p>回调操
	 * 作可以返回结果对象，例如域对象或域对象的集合。
	 * @param psc 给定连接创建一个PreparedStatement的回调
	 * @param action 指定操作的回调
	 * @return 操作返回的结果对象，如果没有则返回 {@code null}
	 * @throws DataAccessException 如果有任何问题
	 */
	<T extends @Nullable Object> T execute(PreparedStatementCreator psc, PreparedStatementCallback<T> action) throws DataAccessException;

	/**
	 * 执行 JDBC 数据访问操作，作为作用于 JDBC PreparedStatement 的回调操作来实现。这允许在 Spring 的托管 JDBC 环境中在单个语句上实现任意数
	 * 据访问操作：即参与 Spring 管理的事务并将 JDBC SQLException 转换为 Spring 的 DataAccessException 层次结构。 <p>回调操
	 * 作可以返回结果对象，例如域对象或域对象的集合。
	 * @param sql 要执行的 SQL
	 * @param action 指定操作的回调
	 * @return 操作返回的结果对象，如果没有则返回 {@code null}
	 * @throws DataAccessException 如果有任何问题
	 */
	<T extends @Nullable Object> T execute(String sql, PreparedStatementCallback<T> action) throws DataAccessException;

	/**
	 * 使用准备好的语句进行查询，并使用 ResultSetExtractor 读取 ResultSet。 <p>APreparedStatementCreator
	 * 可以直接实现，也可以通过PreparedStatementCreatorFactory 进行配置。
	 * @param psc 给定连接创建一个PreparedStatement的回调
	 * @param rse 将提取结果的回调
	 * @return 任意结果对象，由 ResultSetExtractor 返回
	 * @throws DataAccessException 如果有任何问题
	 * @see PreparedStatementCreatorFactory
	 */
	<T extends @Nullable Object> T query(PreparedStatementCreator psc, ResultSetExtractor<T> rse) throws DataAccessException;

	/**
	 * 使用准备好的语句进行查询，并使用 ResultSetExtractor 读取 ResultSet。
	 * @param sql 要执行的 SQL 查询
	 * @param pss 知道如何在准备好的语句上设置值的回调。如果这是 {@code null}，则将假定 SQL 不包含绑定参数。即使没有绑定参数，此回调也可用于设置获取大小和其他性能选项。
	 * @param rse 将提取结果的回调
	 * @return 任意结果对象，由 ResultSetExtractor 返回
	 * @throws DataAccessException 如果有任何问题
	 */
	<T extends @Nullable Object> T query(String sql, @Nullable PreparedStatementSetter pss, ResultSetExtractor<T> rse)
			throws DataAccessException;

	/**
	 * 查询给定的 SQL，从 SQL 创建准备好的语句和绑定到查询的参数列表，使用 ResultSetExtractor 读取 ResultSet。
	 * @param sql 要执行的 SQL 查询
	 * @param args 绑定到查询的参数
	 * @param argTypes 参数的 SQL 类型（来自 {@code java.sql.Types} 的常量）
	 * @param rse 将提取结果的回调
	 * @return 任意结果对象，由 ResultSetExtractor 返回
	 * @throws DataAccessException 如果查询失败
	 * @see java.sql.Types
	 */
	<T extends @Nullable Object> T query(String sql, @Nullable Object @Nullable [] args, int[] argTypes, ResultSetExtractor<T> rse) throws DataAccessException;

	/**
	 * 查询给定的 SQL，从 SQL 创建准备好的语句和绑定到查询的参数列表，使用 ResultSetExtractor 读取 ResultSet。
	 * @param sql 要执行的 SQL 查询
	 * @param args 绑定到查询的参数（让PreparedStatement猜测相应的SQL类型）；还可能包含 {@link SqlParameterValue} 对象，这些对象不仅指示参数值，还指示 SQL 类型和可选的比例
	 * @param rse 将提取结果的回调
	 * @return 任意结果对象，由 ResultSetExtractor 返回
	 * @throws DataAccessException 如果查询失败
	 * @deprecated {@link #query(String, ResultSetExtractor, Object...)} 的青睐
	 */
	@Deprecated(since = "5.3")
	<T extends @Nullable Object> T query(String sql, @Nullable Object @Nullable [] args, ResultSetExtractor<T> rse) throws DataAccessException;

	/**
	 * 查询给定的 SQL，从 SQL 创建准备好的语句和绑定到查询的参数列表，使用 ResultSetExtractor 读取 ResultSet。
	 * @param sql 要执行的 SQL 查询
	 * @param rse 将提取结果的回调
	 * @param args 绑定到查询的参数（让PreparedStatement猜测相应的SQL类型）；还可能包含 {@link SqlParameterValue} 对象，这些对象不仅指示参数值，还指示 SQL 类型和可选的比例
	 * @return 任意结果对象，由 ResultSetExtractor 返回
	 * @throws DataAccessException 如果查询失败
	 * @since 3.0.1
	 */
	<T extends @Nullable Object> T query(String sql, ResultSetExtractor<T> rse, @Nullable Object @Nullable ... args) throws DataAccessException;

	/**
	 * 使用准备好的语句进行查询，使用 RowCallbackHandler 按行读取 ResultSet。 <p>APreparedStatementCreator
	 * 可以直接实现，也可以通过PreparedStatementCreatorFactory 进行配置。
	 * @param psc 给定连接创建一个PreparedStatement的回调
	 * @param rch 将提取结果的回调，一次一行
	 * @throws DataAccessException 如果有任何问题
	 * @see PreparedStatementCreatorFactory
	 */
	void query(PreparedStatementCreator psc, RowCallbackHandler rch) throws DataAccessException;

	/**
	 * 查询给定的 SQL，从 SQL 和一个知道如何将值绑定到查询的PreparedStatementSetter 实现创建一条准备好的语句，并使用
	 * RowCallbackHandler 按行读取 ResultSet。
	 * @param sql 要执行的 SQL 查询
	 * @param pss 知道如何在准备好的语句上设置值的回调。如果这是 {@code null}，则将假定 SQL 不包含绑定参数。即使没有绑定参数，此回调也可用于设置获取大小和其他性能选项。
	 * @param rch 将提取结果的回调，一次一行
	 * @throws DataAccessException 如果查询失败
	 */
	void query(String sql, @Nullable PreparedStatementSetter pss, RowCallbackHandler rch) throws DataAccessException;

	/**
	 * 查询给定的 SQL，从 SQL 创建准备好的语句和绑定到查询的参数列表，使用 RowCallbackHandler 按行读取 ResultSet。
	 * @param sql 要执行的 SQL 查询
	 * @param args 绑定到查询的参数
	 * @param argTypes 参数的 SQL 类型（来自 {@code java.sql.Types} 的常量）
	 * @param rch 将提取结果的回调，一次一行
	 * @throws DataAccessException 如果查询失败
	 * @see java.sql.Types
	 */
	void query(String sql, @Nullable Object @Nullable [] args, int[] argTypes, RowCallbackHandler rch) throws DataAccessException;

	/**
	 * 查询给定的 SQL，从 SQL 创建准备好的语句和绑定到查询的参数列表，使用 RowCallbackHandler 按行读取 ResultSet。
	 * @param sql 要执行的 SQL 查询
	 * @param args 绑定到查询的参数（让PreparedStatement猜测相应的SQL类型）；还可能包含 {@link SqlParameterValue} 对象，这些对象不仅指示参数值，还指示 SQL 类型和可选的比例
	 * @param rch 将提取结果的回调，一次一行
	 * @throws DataAccessException 如果查询失败
	 * @deprecated {@link #query(String, RowCallbackHandler, Object...)} 的青睐
	 */
	@Deprecated(since = "5.3")
	void query(String sql, @Nullable Object @Nullable [] args, RowCallbackHandler rch) throws DataAccessException;

	/**
	 * 查询给定的 SQL，从 SQL 创建准备好的语句和绑定到查询的参数列表，使用 RowCallbackHandler 按行读取 ResultSet。
	 * @param sql 要执行的 SQL 查询
	 * @param rch 将提取结果的回调，一次一行
	 * @param args 绑定到查询的参数（让PreparedStatement猜测相应的SQL类型）；还可能包含 {@link SqlParameterValue} 对象，这些对象不仅指示参数值，还指示 SQL 类型和可选的比例
	 * @throws DataAccessException 如果查询失败
	 * @since 3.0.1
	 */
	void query(String sql, RowCallbackHandler rch, @Nullable Object @Nullable ... args) throws DataAccessException;

	/**
	 * 使用准备好的语句进行查询，通过 RowMapper 将每一行映射到结果对象。 <p>APreparedStatementCreator
	 * 可以直接实现，也可以通过PreparedStatementCreatorFactory 进行配置。
	 * @param psc 给定连接创建一个PreparedStatement的回调
	 * @param rowMapper 将映射每行一个对象的回调
	 * @return 结果列表，包含映射对象
	 * @throws DataAccessException 如果有任何问题
	 * @see PreparedStatementCreatorFactory
	 */
	<T extends @Nullable Object> List<T> query(PreparedStatementCreator psc, RowMapper<T> rowMapper) throws DataAccessException;

	/**
	 * 查询给定的 SQL，从 SQL 创建一条准备好的语句，以及一个知道如何将值绑定到查询的PreparedStatementSetter 实现，通过 RowMapper 将每一行映
	 * 射到结果对象。
	 * @param sql 要执行的 SQL 查询
	 * @param pss 知道如何在准备好的语句上设置值的回调。如果这是 {@code null}，则将假定 SQL 不包含绑定参数。即使没有绑定参数，此回调也可用于设置获取大小和其他性能选项。
	 * @param rowMapper 将映射每行一个对象的回调
	 * @return 结果列表，包含映射对象
	 * @throws DataAccessException 如果查询失败
	 */
	<T extends @Nullable Object> List<T> query(String sql, @Nullable PreparedStatementSetter pss, RowMapper<T> rowMapper)
			throws DataAccessException;

	/**
	 * 查询给定的 SQL，从 SQL 创建准备好的语句和绑定到查询的参数列表，通过 RowMapper 将每一行映射到结果对象。
	 * @param sql 要执行的 SQL 查询
	 * @param args 绑定到查询的参数
	 * @param argTypes 参数的 SQL 类型（来自 {@code java.sql.Types} 的常量）
	 * @param rowMapper 将映射每行一个对象的回调
	 * @return 结果列表，包含映射对象
	 * @throws DataAccessException 如果查询失败
	 * @see java.sql.Types
	 */
	<T extends @Nullable Object> List<T> query(String sql, @Nullable Object @Nullable [] args, int[] argTypes, RowMapper<T> rowMapper) throws DataAccessException;

	/**
	 * 查询给定的 SQL，从 SQL 创建准备好的语句和绑定到查询的参数列表，通过 RowMapper 将每一行映射到结果对象。
	 * @param sql 要执行的 SQL 查询
	 * @param args 绑定到查询的参数（让PreparedStatement猜测相应的SQL类型）；还可能包含 {@link SqlParameterValue} 对象，这些对象不仅指示参数值，还指示 SQL 类型和可选的比例
	 * @param rowMapper 将映射每行一个对象的回调
	 * @return 结果列表，包含映射对象
	 * @throws DataAccessException 如果查询失败
	 * @deprecated {@link #query(String, RowMapper, Object...)} 的青睐
	 */
	@Deprecated(since = "5.3")
	<T extends @Nullable Object> List<T> query(String sql, @Nullable Object @Nullable [] args, RowMapper<T> rowMapper) throws DataAccessException;

	/**
	 * 查询给定的 SQL，从 SQL 创建准备好的语句和绑定到查询的参数列表，通过 RowMapper 将每一行映射到结果对象。
	 * @param sql 要执行的 SQL 查询
	 * @param rowMapper 将映射每行一个对象的回调
	 * @param args 绑定到查询的参数（让PreparedStatement猜测相应的SQL类型）；还可能包含 {@link SqlParameterValue} 对象，这些对象不仅指示参数值，还指示 SQL 类型和可选的比例
	 * @return 结果列表，包含映射对象
	 * @throws DataAccessException 如果查询失败
	 * @since 3.0.1
	 */
	<T extends @Nullable Object> List<T> query(String sql, RowMapper<T> rowMapper, @Nullable Object @Nullable ... args) throws DataAccessException;

	/**
	 * 使用准备好的语句进行查询，通过 RowMapper 将每一行映射到结果对象，并将其转换为可迭代且可关闭的 Stream。
	 * <p>APreparedStatementCreator 可以直接实现，也可以通过PreparedStatementCreatorFactory 进行配置。
	 * @param psc 给定连接创建一个PreparedStatement的回调
	 * @param rowMapper 将映射每行一个对象的回调
	 * @return 结果流，包含映射对象，完全处理后需要关闭（例如，通过 try-with-resources 子句）
	 * @throws DataAccessException 如果有任何问题
	 * @see PreparedStatementCreatorFactory
	 * @since 5.3
	 */
	<T extends @Nullable Object> Stream<T> queryForStream(PreparedStatementCreator psc, RowMapper<T> rowMapper) throws DataAccessException;

	/**
	 * 查询给定的 SQL，从 SQL 创建一条准备好的语句，并知道如何将值绑定到查询，通过 RowMapper 将每一行映射到结果对象，并将其转换为可迭代和可关闭的 Stream。
	 * @param sql 要执行的 SQL 查询
	 * @param pss 知道如何在准备好的语句上设置值的回调。如果这是 {@code null}，则将假定 SQL 不包含绑定参数。即使没有绑定参数，此回调也可用于设置获取大小和其他性能选项。
	 * @param rowMapper 将映射每行一个对象的回调
	 * @return 结果流，包含映射对象，完全处理后需要关闭（例如，通过 try-with-resources 子句）
	 * @throws DataAccessException 如果查询失败
	 * @since 5.3
	 */
	<T extends @Nullable Object> Stream<T> queryForStream(String sql, @Nullable PreparedStatementSetter pss, RowMapper<T> rowMapper)
			throws DataAccessException;

	/**
	 * 查询给定的 SQL，从 SQL 创建准备好的语句和绑定到查询的参数列表，通过 RowMapper 将每一行映射到结果对象，并将其转换为可迭代和可关闭的 Stream。
	 * @param sql 要执行的 SQL 查询
	 * @param rowMapper 将映射每行一个对象的回调
	 * @param args 绑定到查询的参数（让PreparedStatement猜测相应的SQL类型）；还可能包含 {@link SqlParameterValue} 对象，这些对象不仅指示参数值，还指示 SQL 类型和可选的比例
	 * @return 结果流，包含映射对象，完全处理后需要关闭（例如，通过 try-with-resources 子句）
	 * @throws DataAccessException 如果查询失败
	 * @since 5.3
	 */
	<T extends @Nullable Object> Stream<T> queryForStream(String sql, RowMapper<T> rowMapper, @Nullable Object @Nullable ... args)
			throws DataAccessException;

	/**
	 * 查询给定的 SQL，从 SQL 创建准备好的语句和绑定到查询的参数列表，通过 RowMapper 将单个结果行映射到结果对象。
	 * @param sql 要执行的 SQL 查询
	 * @param args 绑定到查询的参数（让PreparedStatement猜测相应的SQL类型）
	 * @param argTypes 参数的 SQL 类型（来自 {@code java.sql.Types} 的常量）
	 * @param rowMapper 将映射每行一个对象的回调
	 * @return 单个映射对象（如果给定的 {@link RowMapper} 返回 {@code null}，则可能是 {@code null}）
	 * @throws org.springframework.dao.IncorrectResultSizeDataAccessException 如果查询没有返回恰好一行
	 * @throws DataAccessException 如果查询失败
	 */
	<T extends @Nullable Object> T queryForObject(String sql, @Nullable Object @Nullable [] args, int[] argTypes, RowMapper<T> rowMapper)
			throws DataAccessException;

	/**
	 * 查询给定的 SQL，从 SQL 创建准备好的语句和绑定到查询的参数列表，通过 RowMapper 将单个结果行映射到结果对象。
	 * @param sql 要执行的 SQL 查询
	 * @param args 绑定到查询的参数（让PreparedStatement猜测相应的SQL类型）；还可能包含 {@link SqlParameterValue} 对象，这些对象不仅指示参数值，还指示 SQL 类型和可选的比例
	 * @param rowMapper 将映射每行一个对象的回调
	 * @return 单个映射对象（如果给定的 {@link RowMapper} 返回 {@code null}，则可能是 {@code null}）
	 * @throws org.springframework.dao.IncorrectResultSizeDataAccessException 如果查询没有返回恰好一行
	 * @throws DataAccessException 如果查询失败
	 * @deprecated {@link #queryForObject(String, RowMapper, Object...)} 的青睐
	 */
	@Deprecated(since = "5.3")
	<T extends @Nullable Object> T queryForObject(String sql, @Nullable Object @Nullable [] args, RowMapper<T> rowMapper) throws DataAccessException;

	/**
	 * 查询给定的 SQL，从 SQL 创建准备好的语句和绑定到查询的参数列表，通过 RowMapper 将单个结果行映射到结果对象。
	 * @param sql 要执行的 SQL 查询
	 * @param rowMapper 将映射每行一个对象的回调
	 * @param args 绑定到查询的参数（让PreparedStatement猜测相应的SQL类型）；还可能包含 {@link SqlParameterValue} 对象，这些对象不仅指示参数值，还指示 SQL 类型和可选的比例
	 * @return 单个映射对象（如果给定的 {@link RowMapper} 返回 {@code null}，则可能是 {@code null}）
	 * @throws org.springframework.dao.IncorrectResultSizeDataAccessException 如果查询没有返回恰好一行
	 * @throws DataAccessException 如果查询失败
	 * @since 3.0.1
	 */
	<T extends @Nullable Object> T queryForObject(String sql, RowMapper<T> rowMapper, @Nullable Object @Nullable ... args) throws DataAccessException;

	/**
	 * 查询给定的 SQL，从 SQL 创建一个准备好的语句以及绑定到查询的参数列表，需要一个结果对象。 <p>查询预计是单行/单列查询；返回的结果将直接映射到相应的对象类型。
	 * @param sql 要执行的 SQL 查询
	 * @param args 绑定到查询的参数
	 * @param argTypes 参数的 SQL 类型（来自 {@code java.sql.Types} 的常量）
	 * @param requiredType 结果对象期望匹配的类型
	 * @return 所需类型的结果对象，或 {@code null}（如果 SQL NULL）
	 * @throws org.springframework.dao.IncorrectResultSizeDataAccessException 如果查询没有返回恰好一行
	 * @throws org.springframework.jdbc.IncorrectResultSetColumnCountException 如果查询不返回包含单个列的行
	 * @throws DataAccessException 如果查询失败
	 * @see #queryForObject(String, Class)
	 * @see java.sql.Types
	 */
	<T> @Nullable T queryForObject(String sql, @Nullable Object @Nullable [] args, int[] argTypes, Class<T> requiredType)
			throws DataAccessException;

	/**
	 * 查询给定的 SQL，从 SQL 创建一个准备好的语句以及绑定到查询的参数列表，需要一个结果对象。 <p>查询预计是单行/单列查询；返回的结果将直接映射到相应的对象类型。
	 * @param sql 要执行的 SQL 查询
	 * @param args 绑定到查询的参数（让PreparedStatement猜测相应的SQL类型）；还可能包含 {@link SqlParameterValue} 对象，这些对象不仅指示参数值，还指示 SQL 类型和可选的比例
	 * @param requiredType 结果对象期望匹配的类型
	 * @return 所需类型的结果对象，或 {@code null}（如果 SQL NULL）
	 * @throws org.springframework.dao.IncorrectResultSizeDataAccessException 如果查询没有返回恰好一行
	 * @throws org.springframework.jdbc.IncorrectResultSetColumnCountException 如果查询不返回包含单个列的行
	 * @throws DataAccessException 如果查询失败
	 * @see #queryForObject(String, Class)
	 * @deprecated {@link #queryForObject(String, Class, Object...)} 的青睐
	 */
	@Deprecated(since = "5.3")
	<T> @Nullable T queryForObject(String sql, @Nullable Object @Nullable [] args, Class<T> requiredType) throws DataAccessException;

	/**
	 * 查询给定的 SQL，从 SQL 创建一个准备好的语句以及绑定到查询的参数列表，需要一个结果对象。 <p>查询预计是单行/单列查询；返回的结果将直接映射到相应的对象类型。
	 * @param sql 要执行的 SQL 查询
	 * @param requiredType 结果对象期望匹配的类型
	 * @param args 绑定到查询的参数（让PreparedStatement猜测相应的SQL类型）；还可能包含 {@link SqlParameterValue} 对象，这些对象不仅指示参数值，还指示 SQL 类型和可选的比例
	 * @return 所需类型的结果对象，或 {@code null}（如果 SQL NULL）
	 * @throws org.springframework.dao.IncorrectResultSizeDataAccessException 如果查询没有返回恰好一行
	 * @throws org.springframework.jdbc.IncorrectResultSetColumnCountException 如果查询不返回包含单个列的行
	 * @throws DataAccessException 如果查询失败
	 * @since 3.0.1
	 * @see #queryForObject(String, Class)
	 */
	<T> @Nullable T queryForObject(String sql, Class<T> requiredType, @Nullable Object @Nullable ... args) throws DataAccessException;

	/**
	 * 查询给定的 SQL，从 SQL 创建准备好的语句和绑定到查询的参数列表，需要结果映射。 <p>查询预计是单行查询；结果行将映射到一个 Map（每列一个条目，使用列名作为键）。
	 * @param sql 要执行的 SQL 查询
	 * @param args 绑定到查询的参数
	 * @param argTypes 参数的 SQL 类型（来自 {@code java.sql.Types} 的常量）
	 * @return 结果映射（每列一个条目，以列名作为键）
	 * @throws org.springframework.dao.IncorrectResultSizeDataAccessException 如果查询没有返回恰好一行
	 * @throws DataAccessException 如果查询失败
	 * @see #queryForMap(String)
	 * @see ColumnMapRowMapper
	 * @see java.sql.Types
	 */
	Map<String, @Nullable Object> queryForMap(String sql, @Nullable Object @Nullable [] args, int[] argTypes) throws DataAccessException;

	/**
	 * 查询给定的 SQL，从 SQL 创建准备好的语句和绑定到查询的参数列表，需要结果映射。 <p> 当您没有域模型时，此接口定义的 {@code queryForMap} 方法是合
	 * 适的。否则，请考虑使用 {@code queryForObject} 方法之一。 <p>查询预计是单行查询；结果行将映射到一个 Map（每列一个条目，使用列名作为键）。
	 * @param sql 要执行的 SQL 查询
	 * @param args 绑定到查询的参数（让PreparedStatement猜测相应的SQL类型）；还可能包含 {@link SqlParameterValue} 对象，这些对象不仅指示参数值，还指示 SQL 类型和可选的比例
	 * @return 结果映射（每列一个条目，使用列名作为键）
	 * @throws org.springframework.dao.IncorrectResultSizeDataAccessException 如果查询没有返回恰好一行
	 * @throws DataAccessException 如果查询失败
	 * @see #queryForMap(String)
	 * @see ColumnMapRowMapper
	 */
	Map<String, @Nullable Object> queryForMap(String sql, @Nullable Object @Nullable ... args) throws DataAccessException;

	/**
	 * 查询给定的 SQL，从 SQL 创建准备好的语句和绑定到查询的参数列表，需要结果列表。 <p>结果将映射到结果对象的列表（每行一个条目），每个结果对象都与指定的元素类型匹配。
	 * @param sql 要执行的 SQL 查询
	 * @param args 绑定到查询的参数
	 * @param argTypes 参数的 SQL 类型（来自 {@code java.sql.Types} 的常量）
	 * @param elementType 结果列表中所需的元素类型（例如 {@code Integer.class}）
	 * @return 与指定元素类型匹配的对象列表
	 * @throws DataAccessException 如果查询失败
	 * @see #queryForList(String, Class)
	 * @see SingleColumnRowMapper
	 */
	<T> List<@Nullable T> queryForList(String sql, @Nullable Object @Nullable [] args, int[] argTypes, Class<T> elementType)
			throws DataAccessException;

	/**
	 * 查询给定的 SQL，从 SQL 创建准备好的语句和绑定到查询的参数列表，需要结果列表。 <p>结果将映射到结果对象的列表（每行一个条目），每个结果对象都与指定的元素类型匹配。
	 * @param sql 要执行的 SQL 查询
	 * @param args 绑定到查询的参数（让PreparedStatement猜测相应的SQL类型）；还可能包含 {@link SqlParameterValue} 对象，这些对象不仅指示参数值，还指示 SQL 类型和可选的比例
	 * @param elementType 结果列表中所需的元素类型（例如 {@code Integer.class}）
	 * @return 与指定元素类型匹配的对象列表
	 * @throws DataAccessException 如果查询失败
	 * @see #queryForList(String, Class)
	 * @see SingleColumnRowMapper
	 * @deprecated {@link #queryForList(String, Class, Object...)} 的青睐
	 */
	@Deprecated(since = "5.3")
	<T> List<@Nullable T> queryForList(String sql, @Nullable Object @Nullable [] args, Class<T> elementType) throws DataAccessException;

	/**
	 * 查询给定的 SQL，从 SQL 创建准备好的语句和绑定到查询的参数列表，需要结果列表。 <p>结果将映射到结果对象的列表（每行一个条目），每个结果对象都与指定的元素类型匹配。
	 * @param sql 要执行的 SQL 查询
	 * @param elementType 结果列表中所需的元素类型（例如 {@code Integer.class}）
	 * @param args 绑定到查询的参数（让PreparedStatement猜测相应的SQL类型）；还可能包含 {@link SqlParameterValue} 对象，这些对象不仅指示参数值，还指示 SQL 类型和可选的比例
	 * @return 与指定元素类型匹配的对象列表
	 * @throws DataAccessException 如果查询失败
	 * @since 3.0.1
	 * @see #queryForList(String, Class)
	 * @see SingleColumnRowMapper
	 */
	<T> List<@Nullable T> queryForList(String sql, Class<T> elementType, @Nullable Object @Nullable ... args) throws DataAccessException;

	/**
	 * 查询给定的 SQL，从 SQL 创建准备好的语句和绑定到查询的参数列表，需要结果列表。 <p>的结果会被映射到一个List（每行一个条目）的Maps（每列一个条目，使用列名作为
	 * 键）。列表中的每个元素都将采用此接口的 {@code queryForMap} 方法返回的形式。
	 * @param sql 要执行的 SQL 查询
	 * @param args 绑定到查询的参数
	 * @param argTypes 参数的 SQL 类型（来自 {@code java.sql.Types} 的常量）
	 * @return 每行包含一个 Map 的列表
	 * @throws DataAccessException 如果查询失败
	 * @see #queryForList(String)
	 * @see java.sql.Types
	 */
	List<Map<String, @Nullable Object>> queryForList(String sql, @Nullable Object @Nullable [] args, int[] argTypes) throws DataAccessException;

	/**
	 * 查询给定的 SQL，从 SQL 创建准备好的语句和绑定到查询的参数列表，需要结果列表。 <p>的结果会被映射到一个List（每行一个条目）的Maps（每列一个条目，使用列名作为
	 * 键）。列表中的每个元素都将采用此接口的 {@code queryForMap} 方法返回的形式。
	 * @param sql 要执行的 SQL 查询
	 * @param args 绑定到查询的参数（让PreparedStatement猜测相应的SQL类型）；还可能包含 {@link SqlParameterValue} 对象，这些对象不仅指示参数值，还指示 SQL 类型和可选的比例
	 * @return 每行包含一个 Map 的列表
	 * @throws DataAccessException 如果查询失败
	 * @see #queryForList(String)
	 */
	List<Map<String, @Nullable Object>> queryForList(String sql, @Nullable Object @Nullable ... args) throws DataAccessException;

	/**
	 * 查询给定的 SQL，从 SQL 创建准备好的语句以及绑定到查询的参数列表，需要 SqlRowSet。 <p> 结果将映射到 SqlRowSet，该 SqlRowSet 以断开连
	 * 接的方式保存数据。该包装器将转换任何抛出的 SQLException。 <p> 请注意，对于默认实现，需要在运行时提供 JDBC RowSet 支持：默认情况下，使用标准 JD
	 * BC {@code CachedRowSet}。
	 * @param sql 要执行的 SQL 查询
	 * @param args 绑定到查询的参数
	 * @param argTypes 参数的 SQL 类型（来自 {@code java.sql.Types} 的常量）
	 * @return SqlRowSet 表示（可能是 {@code javax.sql.rowset.CachedRowSet} 的包装器）
	 * @throws DataAccessException 如果执行查询有任何问题
	 * @see #queryForRowSet(String)
	 * @see SqlRowSetResultSetExtractor
	 * @see javax.sql.rowset.CachedRowSet
	 * @see java.sql.Types
	 */
	SqlRowSet queryForRowSet(String sql, @Nullable Object @Nullable [] args, int[] argTypes) throws DataAccessException;

	/**
	 * 查询给定的 SQL，从 SQL 创建准备好的语句以及绑定到查询的参数列表，需要 SqlRowSet。 <p> 结果将映射到 SqlRowSet，该 SqlRowSet 以断开连
	 * 接的方式保存数据。该包装器将转换任何抛出的 SQLException。 <p> 请注意，对于默认实现，需要在运行时提供 JDBC RowSet 支持：默认情况下，使用标准 JD
	 * BC {@code CachedRowSet}。
	 * @param sql 要执行的 SQL 查询
	 * @param args 绑定到查询的参数（让PreparedStatement猜测相应的SQL类型）；还可能包含 {@link SqlParameterValue} 对象，这些对象不仅指示参数值，还指示 SQL 类型和可选的比例
	 * @return SqlRowSet 表示（可能是 {@code javax.sql.rowset.CachedRowSet} 的包装器）
	 * @throws DataAccessException 如果执行查询有任何问题
	 * @see #queryForRowSet(String)
	 * @see SqlRowSetResultSetExtractor
	 * @see javax.sql.rowset.CachedRowSet
	 */
	SqlRowSet queryForRowSet(String sql, @Nullable Object @Nullable ... args) throws DataAccessException;

	/**
	 * 使用PreparedStatementCreator 发出单个SQL 更新操作（例如插入、更新或删除语句）以提供SQL 和任何所需的参数。 <p>APreparedStatem
	 * entCreator 可以直接实现，也可以通过PreparedStatementCreatorFactory 进行配置。
	 * @param psc 提供 SQL 和任何必要参数的回调
	 * @return 受影响的行数
	 * @throws DataAccessException 如果发布更新有任何问题
	 * @see PreparedStatementCreatorFactory
	 */
	int update(PreparedStatementCreator psc) throws DataAccessException;

	/**
	 * 使用PreparedStatementCreator 发出更新语句以提供SQL 和任何所需的参数。生成的密钥将被放入给定的 KeyHolder 中。 <p>请注意，给定的Pre
	 * paredStatementCreator必须创建一个带有生成键的激活提取的语句（JDBC 3.0功能）。这可以直接完成，也可以通过使用PreparedStatementCre
	 * atorFactory 完成。 <p>此方法需要支持 JDBC 驱动程序中生成的键。
	 * @param psc 提供 SQL 和任何必要参数的回调
	 * @param generatedKeyHolder 将保存生成的密钥的 KeyHolder
	 * @return 受影响的行数
	 * @throws DataAccessException 如果发布更新有任何问题
	 * @see PreparedStatementCreatorFactory
	 * @see org.springframework.jdbc.support.GeneratedKeyHolder
	 * @see java.sql.DatabaseMetaData#supportsGetGeneratedKeys()
	 */
	int update(PreparedStatementCreator psc, KeyHolder generatedKeyHolder) throws DataAccessException;

	/**
	 * 使用PreparedStatementSetter 发出更新语句，以通过给定的SQL 设置绑定参数。比使用PreparedStatementCreator 更简单，因为此方法将
	 * 创建PreparedStatement：PreparedStatementSetter 只需要设置参数。
	 * @param sql 包含绑定参数的 SQL
	 * @param pss 设置绑定参数的助手。如果这是 {@code null}，我们使用静态 SQL 运行更新。
	 * @return 受影响的行数
	 * @throws DataAccessException 如果发布更新有任何问题
	 */
	int update(String sql, @Nullable PreparedStatementSetter pss) throws DataAccessException;

	/**
	 * 通过准备好的语句发出单个 SQL 更新操作（例如插入、更新或删除语句），绑定给定的参数。
	 * @param sql 包含绑定参数的 SQL
	 * @param args 绑定到查询的参数
	 * @param argTypes 参数的 SQL 类型（来自 {@code java.sql.Types} 的常量）
	 * @return 受影响的行数
	 * @throws DataAccessException 如果发布更新有任何问题
	 * @see java.sql.Types
	 */
	int update(String sql, @Nullable Object @Nullable [] args, int[] argTypes) throws DataAccessException;

	/**
	 * 通过准备好的语句发出单个 SQL 更新操作（例如插入、更新或删除语句），绑定给定的参数。
	 * @param sql 包含绑定参数的 SQL
	 * @param args 绑定到查询的参数（让PreparedStatement猜测相应的SQL类型）；还可能包含 {@link SqlParameterValue} 对象，这些对象不仅指示参数值，还指示 SQL 类型和可选的比例
	 * @return 受影响的行数
	 * @throws DataAccessException 如果发布更新有任何问题
	 */
	int update(String sql, @Nullable Object @Nullable ... args) throws DataAccessException;

	/**
	 * 使用批量更新和 BatchPreparedStatementSetter 设置值，对单个PreparedStatement 发出多个更新语句。如果 JDBC
	 * 驱动程序不支持批量更新，<p> 将回退到单个PreparedStatement 上的单独更新。
	 * @param sql 定义将被重用的PreparedStatement。批处理中的所有语句将使用相同的 SQL。
	 * @param pss 对象来设置由此方法创建的PreparedStatement的参数
	 * @return 每个语句影响的行数数组（还可能包含受影响行的特殊 JDBC 定义负值，例如 {@link java.sql.Statement#SUCCESS_NO_INFO}/{@link java.sql.Statement#EXECUTE_FAILED}）
	 * @throws DataAccessException 如果发布更新有任何问题
	 */
	int[] batchUpdate(String sql, BatchPreparedStatementSetter pss) throws DataAccessException;

	/**
	 * 使用批量更新和 BatchPreparedStatementSetter 设置值，对单个PreparedStatement 发出多个更新语句。生成的密钥将被放入给定的
	 * KeyHolder 中。 <p>请注意，给定的PreparedStatementCreator必须创建一个带有生成键的激活提取的语句（JDBC
	 * 3.0功能）。这可以直接完成，也可以通过使用PreparedStatementCreatorFactory 完成。 <p>此方法需要支持 JDBC 驱动程序中生成的键。如果
	 * JDBC 驱动程序不支持批量更新，它将回退到对单个PreparedStatement 进行单独更新。
	 * @param psc 给定连接创建一个PreparedStatement的回调
	 * @param pss 对象来设置由此方法创建的PreparedStatement的参数
	 * @param generatedKeyHolder 将保存生成的密钥的 KeyHolder
	 * @return 每个语句影响的行数数组（还可能包含受影响行的特殊 JDBC 定义负值，例如 {@link java.sql.Statement#SUCCESS_NO_INFO}/{@link java.sql.Statement#EXECUTE_FAILED}）
	 * @throws DataAccessException 如果发布更新有任何问题
	 * @since 6.1
	 * @see org.springframework.jdbc.support.GeneratedKeyHolder
	 * @see java.sql.DatabaseMetaData#supportsGetGeneratedKeys()
	 */
	int[] batchUpdate(PreparedStatementCreator psc, BatchPreparedStatementSetter pss,
			KeyHolder generatedKeyHolder) throws DataAccessException;

	/**
	 * 使用提供的 SQL 语句和一批提供的参数执行批处理。
	 * @param sql 要执行的SQL语句
	 * @param batchArgs 包含查询参数批次的对象数组列表
	 * @return 包含批次中每次更新影响的行数的数组（还可能包含受影响行的特殊 JDBC 定义负值，例如 {@link java.sql.Statement#SUCCESS_NO_INFO}/{@link java.sql.Statement#EXECUTE_FAILED}）
	 * @throws DataAccessException 如果发布更新有任何问题
	 */
	int[] batchUpdate(String sql, List<Object[]> batchArgs) throws DataAccessException;

	/**
	 * 使用提供的 SQL 语句和一批提供的参数执行批处理。
	 * @param sql 要执行的 SQL 语句。
	 * @param batchArgs 包含查询参数批次的对象数组列表
	 * @param argTypes 参数的 SQL 类型（来自 {@code java.sql.Types} 的常量）
	 * @return 包含批次中每次更新影响的行数的数组（还可能包含受影响行的特殊 JDBC 定义负值，例如 {@link java.sql.Statement#SUCCESS_NO_INFO}/{@link java.sql.Statement#EXECUTE_FAILED}）
	 * @throws DataAccessException 如果发布更新有任何问题
	 */
	int[] batchUpdate(String sql, List<Object[]> batchArgs, int[] argTypes) throws DataAccessException;

	/**
	 * 使用提供的 SQL 语句和提供的参数集合来执行多个批处理。参数的值将使用 ParameterizedPreparedStatementSetter
	 * 设置。每个批次的大小应在“batchSize”中指定。
	 * @param sql 要执行的 SQL 语句。
	 * @param batchArgs 包含查询参数批次的对象数组列表
	 * @param batchSize 批量大小
	 * @param pss 要使用的 ParameterizedPreparedStatementSetter
	 * @return 每个批次包含另一个数组，该数组包含批次中每个更新影响的行数（还可能包含受影响行的特殊 JDBC 定义负值，例如 {@link java.sql.Statement#SUCCESS_NO_INFO}/{@link java.sql.Statement#EXECUTE_FAILED}）
	 * @throws DataAccessException 如果发布更新有任何问题
	 * @since 3.1
	 */
	<T> int[][] batchUpdate(String sql, Collection<T> batchArgs, int batchSize,
			ParameterizedPreparedStatementSetter<T> pss) throws DataAccessException;


	//-------------------------------------------------------------------------
	// 处理可调用语句的方法
	//-------------------------------------------------------------------------

	/**
	 * 执行 JDBC 数据访问操作，作为在 JDBC CallableStatement 上工作的回调操作来实现。这允许在 Spring 的托管 JDBC 环境中在单个语句上实现任意
	 * 数据访问操作：即参与 Spring 管理的事务并将 JDBC SQLException 转换为 Spring 的 DataAccessException 层次结构。 <p>回调
	 * 操作可以返回结果对象，例如域对象或域对象的集合。
	 * @param csc 给定 Connection 创建 CallableStatement 的回调
	 * @param action 指定操作的回调
	 * @return 操作返回的结果对象，如果没有则返回 {@code null}
	 * @throws DataAccessException 如果有任何问题
	 */
	<T extends @Nullable Object> T execute(CallableStatementCreator csc, CallableStatementCallback<T> action) throws DataAccessException;

	/**
	 * 执行 JDBC 数据访问操作，作为在 JDBC CallableStatement 上工作的回调操作来实现。这允许在 Spring 的托管 JDBC 环境中在单个语句上实现任意
	 * 数据访问操作：即参与 Spring 管理的事务并将 JDBC SQLException 转换为 Spring 的 DataAccessException 层次结构。 <p>回调
	 * 操作可以返回结果对象，例如域对象或域对象的集合。
	 * @param callString 要执行的 SQL 调用字符串
	 * @param action 指定操作的回调
	 * @return 操作返回的结果对象，如果没有则返回 {@code null}
	 * @throws DataAccessException 如果有任何问题
	 */
	<T extends @Nullable Object> T execute(String callString, CallableStatementCallback<T> action) throws DataAccessException;

	/**
	 * 使用 CallableStatementCreator 执行 SQL 调用以提供 SQL 和任何所需的参数。
	 * @param csc 提供 SQL 和任何必要参数的回调
	 * @param declaredParameters 声明的 SqlParameter 对象的列表
	 * @return 提取出的参数图
	 * @throws DataAccessException 如果发布更新有任何问题
	 */
	Map<String, @Nullable Object> call(CallableStatementCreator csc, List<SqlParameter> declaredParameters)
			throws DataAccessException;

}
