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

package org.springframework.jdbc.core.namedparam;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.jspecify.annotations.Nullable;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;

/**
 * 指定一组基本 JDBC 操作的接口，允许使用命名参数而不是传统的“？”占位符。
 * <p>这是经典{@link org.springframework.jdbc.core.JdbcOperations}接口的替代方案，由{@link
 * NamedParameterJdbcTemplate}实现。该接口通常不直接使用，但提供了一个增强可测试性的有用选项，因为它可以轻松地被模拟或存根。
 * <p><b>NOTE：从 6.1 开始，有一个统一的 JDBC 访问外观，以 {@link
 * org.springframework.jdbc.core.simple.JdbcClient} 的形式提供。</b> {@code JdbcClient} 为常见的 JDBC
 * 查询/更新提供了流畅的 API 风格，可以灵活地使用索引或命名参数。它委托给 {@code JdbcOperations}/{@code
 * NamedParameterJdbcOperations} 来实际执行。
 * @author Thomas Risberg
 * @author Juergen Hoeller
 * @since 2.0
 * @see NamedParameterJdbcTemplate
 * @see org.springframework.jdbc.core.JdbcOperations
 */
public interface NamedParameterJdbcOperations {

	/**
	 * 公开经典的 Spring JdbcTemplate 以允许调用经典的 JDBC 操作。
	 */
	JdbcOperations getJdbcOperations();


	/**
	 * 执行 JDBC 数据访问操作，作为作用于 JDBC PreparedStatement 的回调操作来实现。这允许在 Spring 的托管 JDBC 环境中在单个语句上实现任意数
	 * 据访问操作：即参与 Spring 管理的事务并将 JDBC SQLException 转换为 Spring 的 DataAccessException 层次结构。 <p>回调操
	 * 作可以返回结果对象，例如域对象或域对象的集合。
	 * @param sql 要执行的 SQL
	 * @param paramSource 绑定到查询的参数容器
	 * @param action 指定操作的回调对象
	 * @return 操作返回的结果对象，或 {@code null}
	 * @throws DataAccessException 如果有任何问题
	 */
	<T extends @Nullable Object> T execute(String sql, SqlParameterSource paramSource, PreparedStatementCallback<T> action)
			throws DataAccessException;

	/**
	 * 执行 JDBC 数据访问操作，作为作用于 JDBC PreparedStatement 的回调操作来实现。这允许在 Spring 的托管 JDBC 环境中在单个语句上实现任意数
	 * 据访问操作：即参与 Spring 管理的事务并将 JDBC SQLException 转换为 Spring 的 DataAccessException 层次结构。 <p>回调操
	 * 作可以返回结果对象，例如域对象或域对象的集合。
	 * @param sql 要执行的 SQL
	 * @param paramMap 绑定到查询的参数映射（让PreparedStatement猜测相应的SQL类型）
	 * @param action 指定操作的回调对象
	 * @return 操作返回的结果对象，或 {@code null}
	 * @throws DataAccessException 如果有任何问题
	 */
	<T extends @Nullable Object> T execute(String sql, Map<String, ?> paramMap, PreparedStatementCallback<T> action)
			throws DataAccessException;

	/**
	 * 执行 JDBC 数据访问操作，作为作用于 JDBC PreparedStatement 的回调操作来实现。这允许在 Spring 的托管 JDBC 环境中在单个语句上实现任意数
	 * 据访问操作：即参与 Spring 管理的事务并将 JDBC SQLException 转换为 Spring 的 DataAccessException 层次结构。 <p>回调操
	 * 作可以返回结果对象，例如域对象或域对象的集合。
	 * @param sql 要执行的 SQL
	 * @param action 指定操作的回调对象
	 * @return 操作返回的结果对象，或 {@code null}
	 * @throws DataAccessException 如果有任何问题
	 */
	<T extends @Nullable Object> T execute(String sql, PreparedStatementCallback<T> action) throws DataAccessException;

	/**
	 * 查询给定的 SQL，从 SQL 创建准备好的语句和绑定到查询的参数列表，使用 ResultSetExtractor 读取 ResultSet。
	 * @param sql 要执行的 SQL 查询
	 * @param paramSource 绑定到查询的参数容器
	 * @param rse 将提取结果的对象
	 * @return 任意结果对象，由 ResultSetExtractor 返回
	 * @throws DataAccessException 如果查询失败
	 */
	<T extends @Nullable Object> T query(String sql, SqlParameterSource paramSource, ResultSetExtractor<T> rse)
			throws DataAccessException;

	/**
	 * 查询给定的 SQL，从 SQL 创建准备好的语句和绑定到查询的参数列表，使用 ResultSetExtractor 读取 ResultSet。
	 * @param sql 要执行的 SQL 查询
	 * @param paramMap 绑定到查询的参数映射（让PreparedStatement猜测相应的SQL类型）
	 * @param rse 将提取结果的对象
	 * @return 任意结果对象，由 ResultSetExtractor 返回
	 * @throws DataAccessException 如果查询失败
	 */
	<T extends @Nullable Object> T query(String sql, Map<String, ?> paramMap, ResultSetExtractor<T> rse)
			throws DataAccessException;

	/**
	 * 查询给定的 SQL 以从 SQL 创建准备好的语句，并使用 ResultSetExtractor 读取 ResultSet。 <p>注意：与具有相同签名的
	 * JdbcOperations 方法相比，此查询变体始终使用PreparedStatement。它实际上相当于带有空参数 Map 的查询调用。
	 * @param sql 要执行的 SQL 查询
	 * @param rse 将提取结果的对象
	 * @return 任意结果对象，由 ResultSetExtractor 返回
	 * @throws DataAccessException 如果查询失败
	 */
	<T extends @Nullable Object> T query(String sql, ResultSetExtractor<T> rse) throws DataAccessException;

	/**
	 * 查询给定的 SQL，从 SQL 创建准备好的语句和绑定到查询的参数列表，使用 RowCallbackHandler 按行读取 ResultSet。
	 * @param sql 要执行的 SQL 查询
	 * @param paramSource 绑定到查询的参数容器
	 * @param rch 将提取结果的对象，一次一行
	 * @throws DataAccessException 如果查询失败
	 */
	void query(String sql, SqlParameterSource paramSource, RowCallbackHandler rch)
			throws DataAccessException;

	/**
	 * 查询给定的 SQL，从 SQL 创建准备好的语句和绑定到查询的参数列表，使用 RowCallbackHandler 按行读取 ResultSet。
	 * @param sql 要执行的 SQL 查询
	 * @param paramMap 绑定到查询的参数映射（让PreparedStatement猜测相应的SQL类型）
	 * @param rch 将提取结果的对象，一次一行
	 * @throws DataAccessException 如果查询失败
	 */
	void query(String sql, Map<String, ?> paramMap, RowCallbackHandler rch) throws DataAccessException;

	/**
	 * 查询给定的 SQL 以从 SQL 创建准备好的语句，使用 RowCallbackHandler 按行读取 ResultSet。 <p>注意：与具有相同签名的
	 * JdbcOperations 方法相比，此查询变体始终使用PreparedStatement。它实际上相当于带有空参数 Map 的查询调用。
	 * @param sql 要执行的 SQL 查询
	 * @param rch 将提取结果的对象，一次一行
	 * @throws DataAccessException 如果查询失败
	 */
	void query(String sql, RowCallbackHandler rch) throws DataAccessException;

	/**
	 * 查询给定的 SQL，从 SQL 创建准备好的语句和绑定到查询的参数列表，通过 RowMapper 将每一行映射到 Java 对象。
	 * @param sql 要执行的 SQL 查询
	 * @param paramSource 绑定到查询的参数容器
	 * @param rowMapper 每行映射一个对象的对象
	 * @return 结果列表，包含映射对象
	 * @throws DataAccessException 如果查询失败
	 */
	<T extends @Nullable Object> List<T> query(String sql, SqlParameterSource paramSource, RowMapper<T> rowMapper)
			throws DataAccessException;

	/**
	 * 查询给定的 SQL，从 SQL 创建准备好的语句和绑定到查询的参数列表，通过 RowMapper 将每一行映射到 Java 对象。
	 * @param sql 要执行的 SQL 查询
	 * @param paramMap 绑定到查询的参数映射（让PreparedStatement猜测相应的SQL类型）
	 * @param rowMapper 每行映射一个对象的对象
	 * @return 结果列表，包含映射对象
	 * @throws DataAccessException 如果查询失败
	 */
	<T extends @Nullable Object> List<T> query(String sql, Map<String, ?> paramMap, RowMapper<T> rowMapper)
			throws DataAccessException;

	/**
	 * 查询给定的 SQL 以从 SQL 创建准备好的语句，通过 RowMapper 将每一行映射到 Java 对象。 <p>注意：与具有相同签名的 JdbcOperations 方法
	 * 相比，此查询变体始终使用PreparedStatement。它实际上相当于带有空参数 Map 的查询调用。
	 * @param sql 要执行的 SQL 查询
	 * @param rowMapper 每行映射一个对象的对象
	 * @return 结果列表，包含映射对象
	 * @throws DataAccessException 如果查询失败
	 */
	<T extends @Nullable Object> List<T> query(String sql, RowMapper<T> rowMapper) throws DataAccessException;

	/**
	 * 查询给定的 SQL，从 SQL 创建准备好的语句和绑定到查询的参数列表，通过 RowMapper 将每一行映射到 Java 对象，并将其转换为可迭代和可关闭的 Stream。
	 * @param sql 要执行的 SQL 查询
	 * @param paramSource 绑定到查询的参数容器
	 * @param rowMapper 每行映射一个对象的对象
	 * @return 结果流，包含映射对象，完全处理后需要关闭（例如，通过 try-with-resources 子句）
	 * @throws DataAccessException 如果查询失败
	 * @since 5.3
	 */
	<T extends @Nullable Object> Stream<T> queryForStream(String sql, SqlParameterSource paramSource, RowMapper<T> rowMapper)
			throws DataAccessException;

	/**
	 * 查询给定的 SQL，从 SQL 创建准备好的语句和绑定到查询的参数列表，通过 RowMapper 将每一行映射到 Java 对象，并将其转换为可迭代和可关闭的 Stream。
	 * @param sql 要执行的 SQL 查询
	 * @param paramMap 绑定到查询的参数映射（让PreparedStatement猜测相应的SQL类型）
	 * @param rowMapper 每行映射一个对象的对象
	 * @return 结果流，包含映射对象，完全处理后需要关闭（例如，通过 try-with-resources 子句）
	 * @throws DataAccessException 如果查询失败
	 * @since 5.3
	 */
	<T extends @Nullable Object> Stream<T> queryForStream(String sql, Map<String, ?> paramMap, RowMapper<T> rowMapper)
			throws DataAccessException;

	/**
	 * 查询给定的 SQL，从 SQL 创建准备好的语句和绑定到查询的参数列表，通过 RowMapper 将单个结果行映射到 Java 对象。
	 * @param sql 要执行的 SQL 查询
	 * @param paramSource 绑定到查询的参数容器
	 * @param rowMapper 每行映射一个对象的对象
	 * @return 单个映射对象（如果给定的 {@link RowMapper} 返回 {@code null}，则可能是 {@code null}）
	 * @throws org.springframework.dao.IncorrectResultSizeDataAccessException 如果查询没有返回恰好一行
	 * @throws DataAccessException 如果查询失败
	 */
	<T extends @Nullable Object> T queryForObject(String sql, SqlParameterSource paramSource, RowMapper<T> rowMapper)
			throws DataAccessException;

	/**
	 * 查询给定的 SQL，从 SQL 创建准备好的语句和绑定到查询的参数列表，通过 RowMapper 将单个结果行映射到 Java 对象。
	 * @param sql 要执行的 SQL 查询
	 * @param paramMap 绑定到查询的参数映射（让PreparedStatement猜测相应的SQL类型）
	 * @param rowMapper 每行映射一个对象的对象
	 * @return 单个映射对象（如果给定的 {@link RowMapper} 返回 {@code null}，则可能是 {@code null}）
	 * @throws org.springframework.dao.IncorrectResultSizeDataAccessException 如果查询没有返回恰好一行
	 * @throws DataAccessException 如果查询失败
	 */
	<T extends @Nullable Object> T queryForObject(String sql, Map<String, ?> paramMap, RowMapper<T> rowMapper)
			throws DataAccessException;

	/**
	 * 查询给定的 SQL，从 SQL 创建一个准备好的语句以及绑定到查询的参数列表，需要一个结果对象。 <p>查询预计是单行/单列查询；返回的结果将直接映射到相应的对象类型。
	 * @param sql 要执行的 SQL 查询
	 * @param paramSource 绑定到查询的参数容器
	 * @param requiredType 结果对象期望匹配的类型
	 * @return 所需类型的结果对象，或 {@code null}（如果 SQL NULL）
	 * @throws org.springframework.dao.IncorrectResultSizeDataAccessException 如果查询没有返回恰好一行
	 * @throws org.springframework.jdbc.IncorrectResultSetColumnCountException 如果查询不返回包含单个列的行
	 * @throws DataAccessException 如果查询失败
	 * @see org.springframework.jdbc.core.JdbcTemplate#queryForObject(String, Class)
	 * @see org.springframework.jdbc.core.SingleColumnRowMapper
	 */
	<T> @Nullable T queryForObject(String sql, SqlParameterSource paramSource, Class<T> requiredType)
			throws DataAccessException;

	/**
	 * 查询给定的 SQL，从 SQL 创建一个准备好的语句以及绑定到查询的参数列表，需要一个结果对象。 <p>查询预计是单行/单列查询；返回的结果将直接映射到相应的对象类型。
	 * @param sql 要执行的 SQL 查询
	 * @param paramMap 绑定到查询的参数映射（让PreparedStatement猜测相应的SQL类型）
	 * @param requiredType 结果对象期望匹配的类型
	 * @return 所需类型的结果对象，或 {@code null}（如果 SQL NULL）
	 * @throws org.springframework.dao.IncorrectResultSizeDataAccessException 如果查询没有返回恰好一行
	 * @throws org.springframework.jdbc.IncorrectResultSetColumnCountException 如果查询不返回包含单个列的行
	 * @throws DataAccessException 如果查询失败
	 * @see org.springframework.jdbc.core.JdbcTemplate#queryForObject(String, Class)
	 */
	<T> @Nullable T queryForObject(String sql, Map<String, ?> paramMap, Class<T> requiredType)
			throws DataAccessException;

	/**
	 * 查询给定的 SQL，从 SQL 创建一个准备好的语句和一个绑定到查询的参数列表，需要一个结果 Map。 <p>查询预计是单行查询；结果行将映射到一个 Map（每列一个条目，使用
	 * 列名作为键）。
	 * @param sql 要执行的 SQL 查询
	 * @param paramSource 绑定到查询的参数容器
	 * @return 结果映射（每列一个条目，使用列名作为键）
	 * @throws org.springframework.dao.IncorrectResultSizeDataAccessException 如果查询没有返回恰好一行
	 * @throws DataAccessException 如果查询失败
	 * @see org.springframework.jdbc.core.JdbcTemplate#queryForMap(String)
	 * @see org.springframework.jdbc.core.ColumnMapRowMapper
	 */
	Map<String, @Nullable Object> queryForMap(String sql, SqlParameterSource paramSource) throws DataAccessException;

	/**
	 * 查询给定的 SQL，从 SQL 创建一个准备好的语句和一个绑定到查询的参数列表，需要一个结果 Map。当您没有域模型时，此接口定义的 queryForMap() 方法适用。否则
	 * ，请考虑使用 queryForObject() 方法之一。 <p>查询预计是单行查询；结果行将映射到一个 Map（每列一个条目，使用列名作为键）。
	 * @param sql 要执行的 SQL 查询
	 * @param paramMap 绑定到查询的参数映射（让PreparedStatement猜测相应的SQL类型）
	 * @return 结果映射（每列一个条目，使用列名作为键）
	 * @throws org.springframework.dao.IncorrectResultSizeDataAccessException 如果查询没有返回恰好一行
	 * @throws DataAccessException 如果查询失败
	 * @see org.springframework.jdbc.core.JdbcTemplate#queryForMap(String)
	 * @see org.springframework.jdbc.core.ColumnMapRowMapper
	 */
	Map<String, @Nullable Object> queryForMap(String sql, Map<String, ?> paramMap) throws DataAccessException;

	/**
	 * 查询给定的 SQL，从 SQL 创建准备好的语句和绑定到查询的参数列表，需要结果列表。 <p>结果将映射到结果对象的列表（每行一个条目），每个结果对象都与指定的元素类型匹配。
	 * @param sql 要执行的 SQL 查询
	 * @param paramSource 绑定到查询的参数容器
	 * @param elementType 结果列表中所需的元素类型（例如 {@code Integer.class}）
	 * @return 与指定元素类型匹配的对象列表
	 * @throws DataAccessException 如果查询失败
	 * @see org.springframework.jdbc.core.JdbcTemplate#queryForList(String, Class)
	 * @see org.springframework.jdbc.core.SingleColumnRowMapper
	 */
	<T> List<@Nullable T> queryForList(String sql, SqlParameterSource paramSource, Class<T> elementType)
			throws DataAccessException;

	/**
	 * 查询给定的 SQL，从 SQL 创建准备好的语句和绑定到查询的参数列表，需要结果列表。 <p>结果将映射到结果对象的列表（每行一个条目），每个结果对象都与指定的元素类型匹配。
	 * @param sql 要执行的 SQL 查询
	 * @param paramMap 绑定到查询的参数映射（让PreparedStatement猜测相应的SQL类型）
	 * @param elementType 结果列表中所需的元素类型（例如 {@code Integer.class}）
	 * @return 与指定元素类型匹配的对象列表
	 * @throws DataAccessException 如果查询失败
	 * @see org.springframework.jdbc.core.JdbcTemplate#queryForList(String, Class)
	 * @see org.springframework.jdbc.core.SingleColumnRowMapper
	 */
	<T> List<@Nullable T> queryForList(String sql, Map<String, ?> paramMap, Class<T> elementType)
			throws DataAccessException;

	/**
	 * 查询给定的 SQL，从 SQL 创建准备好的语句和绑定到查询的参数列表，需要结果列表。 <p>的结果会被映射到一个List（每行一个条目）的Maps（每列一个条目，使用列名作为
	 * 键）。列表中的每个元素都将采用此接口的 {@code queryForMap} 方法返回的形式。
	 * @param sql 要执行的 SQL 查询
	 * @param paramSource 绑定到查询的参数容器
	 * @return 每行包含一个 Map 的列表
	 * @throws DataAccessException 如果查询失败
	 * @see org.springframework.jdbc.core.JdbcTemplate#queryForList(String)
	 */
	List<Map<String, @Nullable Object>> queryForList(String sql, SqlParameterSource paramSource) throws DataAccessException;

	/**
	 * 查询给定的 SQL，从 SQL 创建准备好的语句和绑定到查询的参数列表，需要结果列表。 <p>的结果会被映射到一个List（每行一个条目）的Maps（每列一个条目，使用列名作为
	 * 键）。列表中的每个元素都将采用此接口的 {@code queryForMap} 方法返回的形式。
	 * @param sql 要执行的 SQL 查询
	 * @param paramMap 绑定到查询的参数映射（让PreparedStatement猜测相应的SQL类型）
	 * @return 每行包含一个 Map 的列表
	 * @throws DataAccessException 如果查询失败
	 * @see org.springframework.jdbc.core.JdbcTemplate#queryForList(String)
	 */
	List<Map<String, @Nullable Object>> queryForList(String sql, Map<String, ?> paramMap) throws DataAccessException;

	/**
	 * 查询给定的 SQL，从 SQL 创建准备好的语句以及绑定到查询的参数列表，需要 SqlRowSet。 <p> 结果将映射到 SqlRowSet，该 SqlRowSet 以断开连
	 * 接的方式保存数据。该包装器将转换任何抛出的 SQLException。 <p> 请注意，对于默认实现，需要在运行时提供 JDBC RowSet 支持：默认情况下，使用标准 JD
	 * BC {@code CachedRowSet}。
	 * @param sql 要执行的 SQL 查询
	 * @param paramSource 绑定到查询的参数容器
	 * @return SqlRowSet 表示（可能是 {@code javax.sql.rowset.CachedRowSet} 的包装器）
	 * @throws DataAccessException 如果执行查询有任何问题
	 * @see org.springframework.jdbc.core.JdbcTemplate#queryForRowSet(String)
	 * @see org.springframework.jdbc.core.SqlRowSetResultSetExtractor
	 * @see javax.sql.rowset.CachedRowSet
	 */
	SqlRowSet queryForRowSet(String sql, SqlParameterSource paramSource) throws DataAccessException;

	/**
	 * 查询给定的 SQL，从 SQL 创建准备好的语句以及绑定到查询的参数列表，需要 SqlRowSet。 <p> 结果将映射到 SqlRowSet，该 SqlRowSet 以断开连
	 * 接的方式保存数据。该包装器将转换任何抛出的 SQLException。 <p> 请注意，对于默认实现，需要在运行时提供 JDBC RowSet 支持：默认情况下，使用标准 JD
	 * BC {@code CachedRowSet}。
	 * @param sql 要执行的 SQL 查询
	 * @param paramMap 绑定到查询的参数映射（让PreparedStatement猜测相应的SQL类型）
	 * @return SqlRowSet 表示（可能是 {@code javax.sql.rowset.CachedRowSet} 的包装器）
	 * @throws DataAccessException 如果执行查询有任何问题
	 * @see org.springframework.jdbc.core.JdbcTemplate#queryForRowSet(String)
	 * @see org.springframework.jdbc.core.SqlRowSetResultSetExtractor
	 * @see javax.sql.rowset.CachedRowSet
	 */
	SqlRowSet queryForRowSet(String sql, Map<String, ?> paramMap) throws DataAccessException;

	/**
	 * 通过准备好的语句发出更新，绑定给定的参数。
	 * @param sql 包含命名参数的 SQL
	 * @param paramSource 要绑定到查询的参数和 SQL 类型的容器
	 * @return 受影响的行数
	 * @throws DataAccessException 如果发布更新有任何问题
	 */
	int update(String sql, SqlParameterSource paramSource) throws DataAccessException;

	/**
	 * 通过准备好的语句发出更新，绑定给定的参数。
	 * @param sql 包含命名参数的 SQL
	 * @param paramMap 绑定到查询的参数映射（让PreparedStatement猜测相应的SQL类型）
	 * @return 受影响的行数
	 * @throws DataAccessException 如果发布更新有任何问题
	 */
	int update(String sql, Map<String, ?> paramMap) throws DataAccessException;

	/**
	 * 通过准备好的语句发出更新，绑定给定的参数，返回生成的键。 <p>此方法需要支持 JDBC 驱动程序中生成的键。
	 * @param sql 包含命名参数的 SQL
	 * @param paramSource 要绑定到查询的参数和 SQL 类型的容器
	 * @param generatedKeyHolder 将保存生成的密钥的 {@link KeyHolder}
	 * @return 受影响的行数
	 * @throws DataAccessException 如果发布更新有任何问题
	 * @see MapSqlParameterSource
	 * @see org.springframework.jdbc.support.GeneratedKeyHolder
	 * @see java.sql.DatabaseMetaData#supportsGetGeneratedKeys()
	 */
	int update(String sql, SqlParameterSource paramSource, KeyHolder generatedKeyHolder)
			throws DataAccessException;

	/**
	 * 通过准备好的语句发出更新，绑定给定的参数，返回生成的键。 <p>此方法需要支持 JDBC 驱动程序中生成的键。
	 * @param sql 包含命名参数的 SQL
	 * @param paramSource 要绑定到查询的参数和 SQL 类型的容器
	 * @param generatedKeyHolder 将保存生成的密钥的 {@link KeyHolder}
	 * @param keyColumnNames 将为其生成键的列的名称
	 * @return 受影响的行数
	 * @throws DataAccessException 如果发布更新有任何问题
	 * @see MapSqlParameterSource
	 * @see org.springframework.jdbc.support.GeneratedKeyHolder
	 * @see java.sql.DatabaseMetaData#supportsGetGeneratedKeys()
	 */
	int update(String sql, SqlParameterSource paramSource, KeyHolder generatedKeyHolder, String[] keyColumnNames)
			throws DataAccessException;

	/**
	 * 使用提供的 SQL 语句和一批提供的参数执行批处理。
	 * @param sql 要执行的SQL语句
	 * @param batchArgs 包含查询参数批次的 {@link SqlParameterSource} 数组
	 * @return 包含批次中每次更新影响的行数的数组（还可能包含受影响行的特殊 JDBC 定义负值，例如 {@link java.sql.Statement#SUCCESS_NO_INFO}/{@link java.sql.Statement#EXECUTE_FAILED}）
	 * @throws DataAccessException 如果发布更新有任何问题
	 */
	int[] batchUpdate(String sql, SqlParameterSource[] batchArgs);

	/**
	 * 使用提供的 SQL 语句和一批提供的参数来执行批处理。
	 * @param sql 要执行的SQL语句
	 * @param batchValues 包含查询参数批次的 Maps 数组
	 * @return 包含批次中每次更新影响的行数的数组（还可能包含受影响行的特殊 JDBC 定义负值，例如 {@link java.sql.Statement#SUCCESS_NO_INFO}/{@link java.sql.Statement#EXECUTE_FAILED}）
	 * @throws DataAccessException 如果发布更新有任何问题
	 */
	int[] batchUpdate(String sql, Map<String, ?>[] batchValues);

	/**
	 * 使用提供的 SQL 语句和一批提供的参数执行批处理，返回生成的键。 <p>此方法需要支持 JDBC 驱动程序中生成的键。
	 * @param sql 要执行的SQL语句
	 * @param batchArgs 包含查询参数批次的 {@link SqlParameterSource} 数组
	 * @param generatedKeyHolder 将保存生成的密钥的 {@link KeyHolder}
	 * @return 包含批次中每次更新影响的行数的数组（还可能包含受影响行的特殊 JDBC 定义负值，例如 {@link java.sql.Statement#SUCCESS_NO_INFO}/{@link java.sql.Statement#EXECUTE_FAILED}）
	 * @throws DataAccessException 如果发布更新有任何问题
	 * @since 6.1
	 * @see org.springframework.jdbc.support.GeneratedKeyHolder
	 * @see java.sql.DatabaseMetaData#supportsGetGeneratedKeys()
	 */
	int[] batchUpdate(String sql, SqlParameterSource[] batchArgs, KeyHolder generatedKeyHolder);

	/**
	 * 使用提供的 SQL 语句和一批提供的参数执行批处理，返回生成的键。 <p>此方法需要支持 JDBC 驱动程序中生成的键。
	 * @param sql 要执行的SQL语句
	 * @param batchArgs 包含查询参数批次的 {@link SqlParameterSource} 数组
	 * @param generatedKeyHolder 将保存生成的密钥的 {@link KeyHolder}
	 * @param keyColumnNames 将为其生成键的列的名称
	 * @return 包含批次中每次更新影响的行数的数组（还可能包含受影响行的特殊 JDBC 定义负值，例如 {@link java.sql.Statement#SUCCESS_NO_INFO}/{@link java.sql.Statement#EXECUTE_FAILED}）
	 * @throws DataAccessException 如果发布更新有任何问题
	 * @since 6.1
	 * @see org.springframework.jdbc.support.GeneratedKeyHolder
	 * @see java.sql.DatabaseMetaData#supportsGetGeneratedKeys()
	 */
	int[] batchUpdate(String sql, SqlParameterSource[] batchArgs, KeyHolder generatedKeyHolder,
			String[] keyColumnNames);

}
