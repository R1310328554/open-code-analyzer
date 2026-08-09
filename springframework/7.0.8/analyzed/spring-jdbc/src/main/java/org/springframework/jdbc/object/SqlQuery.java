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

package org.springframework.jdbc.object;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import javax.sql.DataSource;

import org.jspecify.annotations.Nullable;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterUtils;
import org.springframework.jdbc.core.namedparam.ParsedSql;

/**
 * 表示 SQL 查询的可重用操作对象。
 * <p>子类必须实现 {@link #newRowMapper} 方法以提供一个对象，该对象可以提取在查询执行期间创建的 {@code ResultSet} 上迭代的结果。
 * <p> 该类提供了许多公共 {@code execute} 方法，这些方法类似于不同的方便的 JDO 查询执行方法。子类可以依赖这些继承的方法之一，也可以添加自己的自定义执行方
 * 法，并具有有意义的名称和类型参数（绝对是最佳实践）。每个自定义查询方法都将调用此类的非类型化查询方法之一。
 * <p> 与 Spring 框架附带的所有 {@code RdbmsOperation} 类一样，{@code SqlQuery} 实例在初始化完成后是线程安全的。也就是说，在通
 * 过 setter 方法构造和配置它们之后，可以从多个线程安全地使用它们。
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Thomas Risberg
 * @author Yanming Zhou
 * @param <T> 结果类型
 * @see SqlUpdate
 */
public abstract class SqlQuery<T extends @Nullable Object> extends SqlOperation {

	/**
	 * 允许用作 JavaBean 的构造函数。 <p>编译和使用之前必须提供{@code DataSource}和SQL。
	 */
	public SqlQuery() {
	}

	/**
	 * 带有 {@code DataSource} 和 SQL 字符串的便捷构造函数。
	 * @param ds 用于获取连接的 {@code DataSource}
	 * @param sql 要执行的 SQL；还可以通过重写 {@link #getSql()} 方法在运行时提供 SQL。
	 */
	public SqlQuery(DataSource ds, String sql) {
		setDataSource(ds);
		setSql(sql);
	}


	/**
	 * 中央执行方法。所有未命名参数的执行都通过此方法。
	 * @param params 参数，类似于JDO查询参数。原始参数必须由其对象包装类型表示。参数的顺序很重要。
	 * @param context 传递给 {@code mapRow} 回调方法的上下文信息。 JDBC 操作本身不依赖于此参数，但它对于创建结果列表的对象很有用。
	 * @return 对象列表，ResultSet 的每一行一个。通常，所有这些都属于同一类，尽管可以使用不同的类型。
	 */
	public List<T> execute(Object @Nullable [] params, @Nullable Map<?, ?> context) throws DataAccessException {
		validateParameters(params);
		RowMapper<T> rowMapper = newRowMapper(params, context);
		return getJdbcTemplate().query(newPreparedStatementCreator(params), rowMapper);
	}

	/**
	 * 中央流法。所有未命名参数的执行都通过此方法。
	 * @param params 参数，类似于JDO查询参数。原始参数必须由其对象包装类型表示。参数的顺序很重要。
	 * @param context 传递给 {@code mapRow} 回调方法的上下文信息。 JDBC 操作本身不依赖于此参数，但它对于创建结果列表的对象很有用。
	 * @return result 对象流，ResultSet 的每一行一个。通常，所有这些都属于同一类，尽管可以使用不同的类型。
	 * @since 7.0
	 */
	public Stream<T> stream(Object @Nullable [] params, @Nullable Map<?, ?> context) throws DataAccessException {
		validateParameters(params);
		RowMapper<T> rowMapper = newRowMapper(params, context);
		return getJdbcTemplate().queryForStream(newPreparedStatementCreator(params), rowMapper);
	}

	/**
	 * 无需上下文即可执行的便捷方法。
	 * @param params 查询的参数。原始参数必须由其对象包装类型表示。参数的顺序很重要。
	 */
	public List<T> execute(Object... params) throws DataAccessException {
		return execute(params, null);
	}

	/**
	 * 无需上下文即可进行流式传输的便捷方法。
	 * @param params 查询的参数。原始参数必须由其对象包装类型表示。参数的顺序很重要。
	 * @since 7.0
	 */
	public Stream<T> stream(Object... params) throws DataAccessException {
		return stream(params, null);
	}

	/**
	 * 无需参数即可执行的便捷方法。
	 * @param context 对象创建的上下文信息
	 */
	public List<T> execute(Map<?, ?> context) throws DataAccessException {
		return execute((Object[]) null, context);
	}

	/**
	 * 无需参数即可进行流式传输的便捷方法。
	 * @param context 对象创建的上下文信息
	 * @since 7.0
	 */
	public Stream<T> stream(Map<?, ?> context) throws DataAccessException {
		return stream(null, context);
	}

	/**
	 * 无需参数或上下文即可执行的便捷方法。
	 */
	public List<T> execute() throws DataAccessException {
		return execute((Object[]) null, null);
	}

	/**
	 * 无需参数和上下文即可进行流式传输的便捷方法。
	 * @since 7.0
	 */
	public Stream<T> stream() throws DataAccessException {
		return stream(null, null);
	}

	/**
	 * 使用单个 int 参数和上下文执行的便捷方法。
	 * @param p1 单个整型参数
	 * @param context 对象创建的上下文信息
	 */
	public List<T> execute(int p1, @Nullable Map<?, ?> context) throws DataAccessException {
		return execute(new Object[] {p1}, context);
	}

	/**
	 * 使用单个 int 参数执行的便捷方法。
	 * @param p1 单个整型参数
	 */
	public List<T> execute(int p1) throws DataAccessException {
		return execute(p1, null);
	}

	/**
	 * 使用两个 int 参数和上下文执行的便捷方法。
	 * @param p1 第一个 int 参数
	 * @param p2 第二个 int 参数
	 * @param context 对象创建的上下文信息
	 */
	public List<T> execute(int p1, int p2, @Nullable Map<?, ?> context) throws DataAccessException {
		return execute(new Object[] {p1, p2}, context);
	}

	/**
	 * 使用两个 int 参数执行的便捷方法。
	 * @param p1 第一个 int 参数
	 * @param p2 第二个 int 参数
	 */
	public List<T> execute(int p1, int p2) throws DataAccessException {
		return execute(p1, p2, null);
	}

	/**
	 * 使用单个长参数和上下文执行的便捷方法。
	 * @param p1 单个长参数
	 * @param context 对象创建的上下文信息
	 */
	public List<T> execute(long p1, @Nullable Map<?, ?> context) throws DataAccessException {
		return execute(new Object[] {p1}, context);
	}

	/**
	 * 使用单个长参数执行的便捷方法。
	 * @param p1 单个长参数
	 */
	public List<T> execute(long p1) throws DataAccessException {
		return execute(p1, null);
	}

	/**
	 * 使用单个字符串参数和上下文执行的便捷方法。
	 * @param p1 单个字符串参数
	 * @param context 对象创建的上下文信息
	 */
	public List<T> execute(String p1, @Nullable Map<?, ?> context) throws DataAccessException {
		return execute(new Object[] {p1}, context);
	}

	/**
	 * 使用单个字符串参数执行的便捷方法。
	 * @param p1 单个字符串参数
	 */
	public List<T> execute(String p1) throws DataAccessException {
		return execute(p1, null);
	}

	/**
	 * 中央执行方法。所有命名参数的执行都通过此方法。
	 * @param paramMap 与声明 SqlParameters 时指定的名称关联的参数。原始参数必须由其对象包装类型表示。参数的顺序并不重要，因为它们是在 SqlParameterMap 中提供的，SqlParameterMap 是 Map 接口的实现。
	 * @param context 传递给 {@code mapRow} 回调方法的上下文信息。 JDBC 操作本身不依赖于此参数，但它对于创建结果列表的对象很有用。
	 * @return 对象列表，ResultSet 的每一行一个。通常，所有这些都属于同一类，尽管可以使用不同的类型。
	 */
	public List<T> executeByNamedParam(Map<String, ?> paramMap, @Nullable Map<?, ?> context) throws DataAccessException {
		return queryByNamedParam(paramMap, context, getJdbcTemplate()::query);
	}

	/**
	 * 中央流法。所有命名参数的执行都通过此方法。
	 * @param paramMap 与声明 SqlParameters 时指定的名称关联的参数。原始参数必须由其对象包装类型表示。参数的顺序并不重要，因为它们是在 SqlParameterMap 中提供的，SqlParameterMap 是 Map 接口的实现。
	 * @param context 传递给 {@code mapRow} 回调方法的上下文信息。 JDBC 操作本身不依赖于此参数，但它对于创建结果列表的对象很有用。
	 * @return 对象流，ResultSet 的每一行一个。通常，所有这些都属于同一类，尽管可以使用不同的类型。
	 * @since 7.0
	 */
	public Stream<T> streamByNamedParam(Map<String, ?> paramMap, @Nullable Map<?, ?> context) throws DataAccessException {
		return queryByNamedParam(paramMap, context, getJdbcTemplate()::queryForStream);
	}

	/**
	 * 无需上下文即可执行的便捷方法。
	 * @param paramMap 与声明 SqlParameters 时指定的名称关联的参数。原始参数必须由其对象包装类型表示。参数的顺序并不重要。
	 */
	public List<T> executeByNamedParam(Map<String, ? extends @Nullable Object> paramMap) throws DataAccessException {
		return executeByNamedParam(paramMap, null);
	}

	/**
	 * 无需上下文即可进行流式传输的便捷方法。
	 * @param paramMap 与声明 SqlParameters 时指定的名称关联的参数。原始参数必须由其对象包装类型表示。参数的顺序并不重要。
	 * @since 7.0
	 */
	public Stream<T> streamByNamedParam(Map<String, ? extends @Nullable Object> paramMap) throws DataAccessException {
		return streamByNamedParam(paramMap, null);
	}

	/**
	 * 方法 `queryByNamedParam`：完成本类中与「query By Named Param」相关的职责。
	 */
	private <R> R queryByNamedParam(Map<String, ?> paramMap, @Nullable Map<?, ?> context, BiFunction<PreparedStatementCreator, RowMapper<T>, R> queryFunction) {
		validateNamedParameters(paramMap);
		ParsedSql parsedSql = getParsedSql();
		MapSqlParameterSource paramSource = new MapSqlParameterSource(paramMap);
		String sqlToUse = NamedParameterUtils.substituteNamedParameters(parsedSql, paramSource);
		@Nullable Object[] params = NamedParameterUtils.buildValueArray(parsedSql, paramSource, getDeclaredParameters());
		RowMapper<T> rowMapper = newRowMapper(params, context);
		return queryFunction.apply(newPreparedStatementCreator(sqlToUse, params), rowMapper);
	}


	/**
	 * 通用对象查找器方法，由所有其他 {@code findObject} 方法使用。对象查找器方法类似于 EJB 实体 bean 查找器，因为如果它们返回多个结果，则将被视为错误。
	 * @return 结果对象，如果未找到则为 {@code null}。子类可以选择将此视为错误并引发异常。
	 * @see org.springframework.dao.support.DataAccessUtils#singleResult
	 */
	public @Nullable T findObject(Object @Nullable [] params, @Nullable Map<?, ?> context) throws DataAccessException {
		List<T> results = execute(params, context);
		return DataAccessUtils.singleResult(results);
	}

	/**
	 * 在没有上下文的情况下查找单个对象的便捷方法。
	 */
	public @Nullable T findObject(Object... params) throws DataAccessException {
		return findObject(params, null);
	}

	/**
	 * 在给定单个 int 参数和上下文的情况下查找单个对象的便捷方法。
	 */
	public @Nullable T findObject(int p1, @Nullable Map<?, ?> context) throws DataAccessException {
		return findObject(new Object[] {p1}, context);
	}

	/**
	 * 在给定单个 int 参数的情况下查找单个对象的便捷方法。
	 */
	public @Nullable T findObject(int p1) throws DataAccessException {
		return findObject(p1, null);
	}

	/**
	 * 给定两个 int 参数和上下文来查找单个对象的便捷方法。
	 */
	public @Nullable T findObject(int p1, int p2, @Nullable Map<?, ?> context) throws DataAccessException {
		return findObject(new Object[] {p1, p2}, context);
	}

	/**
	 * 在给定两个 int 参数的情况下查找单个对象的便捷方法。
	 */
	public @Nullable T findObject(int p1, int p2) throws DataAccessException {
		return findObject(p1, p2, null);
	}

	/**
	 * 在给定单个长参数和上下文的情况下查找单个对象的便捷方法。
	 */
	public @Nullable T findObject(long p1, @Nullable Map<?, ?> context) throws DataAccessException {
		return findObject(new Object[] {p1}, context);
	}

	/**
	 * 在给定单个长参数的情况下查找单个对象的便捷方法。
	 */
	public @Nullable T findObject(long p1) throws DataAccessException {
		return findObject(p1, null);
	}

	/**
	 * 在给定单个字符串参数和上下文的情况下查找单个对象的便捷方法。
	 */
	public @Nullable T findObject(String p1, @Nullable Map<?, ?> context) throws DataAccessException {
		return findObject(new Object[] {p1}, context);
	}

	/**
	 * 在给定单个字符串参数的情况下查找单个对象的便捷方法。
	 */
	public @Nullable T findObject(String p1) throws DataAccessException {
		return findObject(p1, null);
	}

	/**
	 * 命名参数的通用对象查找方法。
	 * @param paramMap 参数名称到参数对象的映射，与 SQL 语句中指定的命名参数匹配。订购并不重要。
	 * @param context 传递给 {@code mapRow} 回调方法的上下文信息。 JDBC 操作本身不依赖于此参数，但它对于创建结果列表的对象很有用。
	 * @return 对象列表，ResultSet 的每一行一个。通常，所有这些都属于同一类，尽管可以使用不同的类型。
	 */
	public @Nullable T findObjectByNamedParam(Map<String, ?> paramMap, @Nullable Map<?, ?> context) throws DataAccessException {
		List<T> results = executeByNamedParam(paramMap, context);
		return DataAccessUtils.singleResult(results);
	}

	/**
	 * 无需上下文即可执行的便捷方法。
	 * @param paramMap 参数名称到参数对象的映射，与 SQL 语句中指定的命名参数匹配。订购并不重要。
	 */
	public @Nullable T findObjectByNamedParam(Map<String, ?> paramMap) throws DataAccessException {
		return findObjectByNamedParam(paramMap, null);
	}


	/**
	 * 子类必须实现此方法以提取每行一个对象，由 {@code execute} 方法作为聚合的 {@link List} 返回。
	 * @param parameters {@code execute()} 方法的参数，以防子类感兴趣；如果没有参数，可能是{@code null}。
	 * @param context 传递给 {@code mapRow} 回调方法的上下文信息。 JDBC 操作本身不依赖于此参数，但它对于创建结果列表的对象很有用。
	 * @see #execute
	 */
	protected abstract RowMapper<T> newRowMapper(@Nullable Object @Nullable [] parameters, @Nullable Map<?, ?> context);

}
