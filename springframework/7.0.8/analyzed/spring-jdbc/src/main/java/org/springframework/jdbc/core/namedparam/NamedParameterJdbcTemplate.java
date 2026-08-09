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

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

import javax.sql.DataSource;

import org.jspecify.annotations.Nullable;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.PreparedStatementCreatorFactory;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.SqlRowSetResultSetExtractor;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.util.Assert;
import org.springframework.util.ConcurrentLruCache;

/**
 * 具有一组基本 JDBC 操作的模板类，允许使用命名参数而不是传统的“？”占位符。
 * <p> 一旦从命名参数替换为 JDBC 样式“？”，该类就会委托给包装的 {@link #getJdbcOperations()
 * JdbcTemplate}占位符是在执行时完成的。它还允许将 {@link java.util.List} 值扩展为适当数量的占位符。
 * <p>一旦配置，该模板类的实例就是线程安全的。暴露底层 {@link org.springframework.jdbc.core.JdbcTemplate}，以便方便访问传统的
 *  {@link org.springframework.jdbc.core.JdbcTemplate} 方法。
 * <p><b>NOTE：从 6.1 开始，有一个统一的 JDBC 访问外观，以 {@link
 * org.springframework.jdbc.core.simple.JdbcClient} 的形式提供。</b> {@code JdbcClient} 为常见的 JDBC
 * 查询/更新提供了流畅的 API 风格，可以灵活地使用索引或命名参数。它委托给 {@code JdbcTemplate}/{@code
 * NamedParameterJdbcTemplate} 来实际执行。
 * @author Thomas Risberg
 * @author Juergen Hoeller
 * @since 2.0
 * @see NamedParameterJdbcOperations
 * @see SqlParameterSource
 * @see ResultSetExtractor
 * @see RowCallbackHandler
 * @see RowMapper
 * @see org.springframework.jdbc.core.JdbcTemplate
 */
public class NamedParameterJdbcTemplate implements NamedParameterJdbcOperations {

	/** 此模板 SQL 缓存的默认最大条目数：256。 */
	public static final int DEFAULT_CACHE_LIMIT = 256;


	/** 被包装的 {@link JdbcOperations} 委托。 */
	private final JdbcOperations classicJdbcTemplate;

	/** 原始 SQL 字符串到 {@link ParsedSql} 表示的缓存。 */
	private volatile ConcurrentLruCache<String, ParsedSql> parsedSqlCache;


	/**
	 * 为给定的 {@link DataSource} 创建新的 NamedParameterJdbcTemplate。 <p>创建一个经典的Spring {@link
	 * org.springframework.jdbc.core.JdbcTemplate}并包装它。
	 * @param dataSource 要访问的 JDBC 数据源
	 */
	public NamedParameterJdbcTemplate(DataSource dataSource) {
		this(new JdbcTemplate(dataSource));
	}

	/**
	 * 为给定的经典 Spring {@link org.springframework.jdbc.core.JdbcTemplate} 创建一个新的
	 * NamedParameterJdbcTemplate。
	 * @param classicJdbcTemplate 经典的 Spring JdbcTemplate 来包装
	 */
	public NamedParameterJdbcTemplate(JdbcOperations classicJdbcTemplate) {
		Assert.notNull(classicJdbcTemplate, "JdbcTemplate must not be null");
		this.classicJdbcTemplate = classicJdbcTemplate;
		this.parsedSqlCache = new ConcurrentLruCache<>(DEFAULT_CACHE_LIMIT, NamedParameterUtils::parseSqlStatement);
	}

	/**
	 * 派生的 NamedParameterJdbcTemplate 的复制构造函数。
	 * @param original 要从中复制的原始 NamedParameterJdbcTemplate
	 * @param classicJdbcTemplate 实际使用的 JdbcTemplate 委托
	 * @since 7.0
	 */
	public NamedParameterJdbcTemplate(NamedParameterJdbcTemplate original, JdbcTemplate classicJdbcTemplate) {
		Assert.notNull(classicJdbcTemplate, "JdbcTemplate must not be null");
		this.classicJdbcTemplate = classicJdbcTemplate;
		this.parsedSqlCache = original.parsedSqlCache;
	}


	/**
	 * 公开经典的 Spring JdbcTemplate 操作以允许调用不太常用的方法。
	 */
	@Override
	public JdbcOperations getJdbcOperations() {
		return this.classicJdbcTemplate;
	}

	/**
	 * 公开经典的 Spring {@link JdbcTemplate} 本身（如果可用），特别是将其传递给其他 {@code JdbcTemplate} 使用者。 <p>
	 * 如果足以满足当前的目的，则建议使用 {@link #getJdbcOperations()} 来替代此变体。
	 * @since 5.0.3
	 */
	public JdbcTemplate getJdbcTemplate() {
		Assert.state(this.classicJdbcTemplate instanceof JdbcTemplate, "No JdbcTemplate available");
		return (JdbcTemplate) this.classicJdbcTemplate;
	}

	/**
	 * 指定该模板的 SQL 缓存的最大条目数。默认值为 256。0 表示不缓存，始终解析每个语句。
	 */
	public void setCacheLimit(int cacheLimit) {
		this.parsedSqlCache = new ConcurrentLruCache<>(cacheLimit, NamedParameterUtils::parseSqlStatement);
	}

	/**
	 * 返回此模板的 SQL 缓存的最大条目数。
	 */
	public int getCacheLimit() {
		return this.parsedSqlCache.capacity();
	}


	@Override
	public <T extends @Nullable Object> T execute(String sql, SqlParameterSource paramSource, PreparedStatementCallback<T> action)
			throws DataAccessException {

		return getJdbcOperations().execute(getPreparedStatementCreator(sql, paramSource), action);
	}

	@Override
	public <T extends @Nullable Object> T execute(String sql, Map<String, ?> paramMap, PreparedStatementCallback<T> action)
			throws DataAccessException {

		return execute(sql, new MapSqlParameterSource(paramMap), action);
	}

	@Override
	public <T extends @Nullable Object> T execute(String sql, PreparedStatementCallback<T> action) throws DataAccessException {
		return execute(sql, EmptySqlParameterSource.INSTANCE, action);
	}

	@Override
	public <T extends @Nullable Object> T query(String sql, SqlParameterSource paramSource, ResultSetExtractor<T> rse)
			throws DataAccessException {

		return getJdbcOperations().query(getPreparedStatementCreator(sql, paramSource), rse);
	}

	@Override
	public <T extends @Nullable Object> T query(String sql, Map<String, ?> paramMap, ResultSetExtractor<T> rse)
			throws DataAccessException {

		return query(sql, new MapSqlParameterSource(paramMap), rse);
	}

	@Override
	public <T extends @Nullable Object> T query(String sql, ResultSetExtractor<T> rse) throws DataAccessException {
		return query(sql, EmptySqlParameterSource.INSTANCE, rse);
	}

	@Override
	public void query(String sql, SqlParameterSource paramSource, RowCallbackHandler rch)
			throws DataAccessException {

		getJdbcOperations().query(getPreparedStatementCreator(sql, paramSource), rch);
	}

	@Override
	public void query(String sql, Map<String, ?> paramMap, RowCallbackHandler rch)
			throws DataAccessException {

		query(sql, new MapSqlParameterSource(paramMap), rch);
	}

	@Override
	public void query(String sql, RowCallbackHandler rch) throws DataAccessException {
		query(sql, EmptySqlParameterSource.INSTANCE, rch);
	}

	@Override
	public <T extends @Nullable Object> List<T> query(String sql, SqlParameterSource paramSource, RowMapper<T> rowMapper)
			throws DataAccessException {

		return getJdbcOperations().query(getPreparedStatementCreator(sql, paramSource), rowMapper);
	}

	@Override
	public <T extends @Nullable Object> List<T> query(String sql, Map<String, ?> paramMap, RowMapper<T> rowMapper)
			throws DataAccessException {

		return query(sql, new MapSqlParameterSource(paramMap), rowMapper);
	}

	@Override
	public <T> List<T> query(String sql, RowMapper<T> rowMapper) throws DataAccessException {
		return query(sql, EmptySqlParameterSource.INSTANCE, rowMapper);
	}

	@Override
	public <T extends @Nullable Object> Stream<T> queryForStream(String sql, SqlParameterSource paramSource, RowMapper<T> rowMapper)
			throws DataAccessException {

		return getJdbcOperations().queryForStream(getPreparedStatementCreator(sql, paramSource), rowMapper);
	}

	@Override
	public <T extends @Nullable Object> Stream<T> queryForStream(String sql, Map<String, ?> paramMap, RowMapper<T> rowMapper)
			throws DataAccessException {

		return queryForStream(sql, new MapSqlParameterSource(paramMap), rowMapper);
	}

	@Override
	public <T extends @Nullable Object> T queryForObject(String sql, SqlParameterSource paramSource, RowMapper<T> rowMapper)
			throws DataAccessException {

		List<T> results = getJdbcOperations().query(getPreparedStatementCreator(sql, paramSource), rowMapper);
		return DataAccessUtils.nullableSingleResult(results);
	}

	@Override
	public <T extends @Nullable Object> T queryForObject(String sql, Map<String, ?> paramMap, RowMapper<T>rowMapper)
			throws DataAccessException {

		return queryForObject(sql, new MapSqlParameterSource(paramMap), rowMapper);
	}

	@Override
	public <T> @Nullable T queryForObject(String sql, SqlParameterSource paramSource, Class<T> requiredType)
			throws DataAccessException {

		return queryForObject(sql, paramSource, new SingleColumnRowMapper<>(requiredType));
	}

	@Override
	public <T> @Nullable T queryForObject(String sql, Map<String, ?> paramMap, Class<T> requiredType)
			throws DataAccessException {

		return queryForObject(sql, paramMap, new SingleColumnRowMapper<>(requiredType));
	}

	@Override
	public Map<String, @Nullable Object> queryForMap(String sql, SqlParameterSource paramSource) throws DataAccessException {
		Map<String, @Nullable Object> result = queryForObject(sql, paramSource, new ColumnMapRowMapper());
		Assert.state(result != null, "No result map");
		return result;
	}

	@Override
	public Map<String, @Nullable Object> queryForMap(String sql, Map<String, ?> paramMap) throws DataAccessException {
		Map<String, @Nullable Object> result = queryForObject(sql, paramMap, new ColumnMapRowMapper());
		Assert.state(result != null, "No result map");
		return result;
	}

	@Override
	public <T> List<@Nullable T> queryForList(String sql, SqlParameterSource paramSource, Class<T> elementType)
			throws DataAccessException {

		return query(sql, paramSource, new SingleColumnRowMapper<>(elementType));
	}

	@Override
	public <T> List<@Nullable T> queryForList(String sql, Map<String, ?> paramMap, Class<T> elementType)
			throws DataAccessException {

		return queryForList(sql, new MapSqlParameterSource(paramMap), elementType);
	}

	@Override
	public List<Map<String, @Nullable Object>> queryForList(String sql, SqlParameterSource paramSource)
			throws DataAccessException {

		return query(sql, paramSource, new ColumnMapRowMapper());
	}

	@Override
	public List<Map<String, @Nullable Object>> queryForList(String sql, Map<String, ?> paramMap)
			throws DataAccessException {

		return queryForList(sql, new MapSqlParameterSource(paramMap));
	}

	@Override
	public SqlRowSet queryForRowSet(String sql, SqlParameterSource paramSource) throws DataAccessException {
		SqlRowSet result = getJdbcOperations().query(
				getPreparedStatementCreator(sql, paramSource), new SqlRowSetResultSetExtractor());
		Assert.state(result != null, "No result");
		return result;
	}

	@Override
	public SqlRowSet queryForRowSet(String sql, Map<String, ?> paramMap) throws DataAccessException {
		return queryForRowSet(sql, new MapSqlParameterSource(paramMap));
	}

	@Override
	public int update(String sql, SqlParameterSource paramSource) throws DataAccessException {
		return getJdbcOperations().update(getPreparedStatementCreator(sql, paramSource));
	}

	@Override
	public int update(String sql, Map<String, ?> paramMap) throws DataAccessException {
		return update(sql, new MapSqlParameterSource(paramMap));
	}

	@Override
	public int update(String sql, SqlParameterSource paramSource, KeyHolder generatedKeyHolder)
			throws DataAccessException {

		return update(sql, paramSource, generatedKeyHolder, null);
	}

	@Override
	public int update(
			String sql, SqlParameterSource paramSource, KeyHolder generatedKeyHolder, String @Nullable [] keyColumnNames)
			throws DataAccessException {

		PreparedStatementCreator psc = getPreparedStatementCreator(sql, paramSource, pscf -> {
			if (keyColumnNames != null) {
				pscf.setGeneratedKeysColumnNames(keyColumnNames);
			}
			else {
				pscf.setReturnGeneratedKeys(true);
			}
		});
		return getJdbcOperations().update(psc, generatedKeyHolder);
	}

	@Override
	public int[] batchUpdate(String sql, SqlParameterSource[] batchArgs) {
		if (batchArgs.length == 0) {
			return new int[0];
		}

		ParsedSql parsedSql = getParsedSql(sql);
		PreparedStatementCreatorFactory pscf = getPreparedStatementCreatorFactory(parsedSql, batchArgs[0]);

		return getJdbcOperations().batchUpdate(
				pscf.getSql(),
				new BatchPreparedStatementSetter() {
					@Override
					public void setValues(PreparedStatement ps, int i) throws SQLException {
						@Nullable Object[] values = NamedParameterUtils.buildValueArray(parsedSql, batchArgs[i], null);
						pscf.newPreparedStatementSetter(values).setValues(ps);
					}
					@Override
					public int getBatchSize() {
						return batchArgs.length;
					}
				});
	}

	@Override
	public int[] batchUpdate(String sql, Map<String, ?>[] batchValues) {
		return batchUpdate(sql, SqlParameterSourceUtils.createBatch(batchValues));
	}

	@Override
	public int[] batchUpdate(String sql, SqlParameterSource[] batchArgs, KeyHolder generatedKeyHolder) {
		return batchUpdate(sql, batchArgs, generatedKeyHolder, null);
	}

	@Override
	public int[] batchUpdate(String sql, SqlParameterSource[] batchArgs, KeyHolder generatedKeyHolder,
			String @Nullable [] keyColumnNames) {

		if (batchArgs.length == 0) {
			return new int[0];
		}

		ParsedSql parsedSql = getParsedSql(sql);
		SqlParameterSource paramSource = batchArgs[0];
		PreparedStatementCreatorFactory pscf = getPreparedStatementCreatorFactory(parsedSql, paramSource);
		if (keyColumnNames != null) {
			pscf.setGeneratedKeysColumnNames(keyColumnNames);
		}
		else {
			pscf.setReturnGeneratedKeys(true);
		}
		@Nullable Object[] params = NamedParameterUtils.buildValueArray(parsedSql, paramSource, null);
		PreparedStatementCreator psc = pscf.newPreparedStatementCreator(params);
		return getJdbcOperations().batchUpdate(psc, new BatchPreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				@Nullable Object[] values = NamedParameterUtils.buildValueArray(parsedSql, batchArgs[i], null);
				pscf.newPreparedStatementSetter(values).setValues(ps);
			}

			@Override
			public int getBatchSize() {
				return batchArgs.length;
			}
		}, generatedKeyHolder);
	}


	/**
	 * 根据给定的 SQL 和命名参数构建 {@link PreparedStatementCreator}。 <p>注意：从所有 {@code query}
	 * 变体直接调用。代表常见的 {@link #getPreparedStatementCreator(String, SqlParameterSource, Consumer)}
	 * 方法。
	 * @param sql 要执行的SQL语句
	 * @param paramSource 要绑定的参数的容器
	 * @return 对应{@link PreparedStatementCreator}
	 * @see #getPreparedStatementCreator(String, SqlParameterSource, Consumer)
	 */
	protected PreparedStatementCreator getPreparedStatementCreator(String sql, SqlParameterSource paramSource) {
		return getPreparedStatementCreator(sql, paramSource, null);
	}

	/**
	 * 根据给定的 SQL 和命名参数构建 {@link PreparedStatementCreator}。 <p>Note：用于具有生成密钥处理的 {@code update}
	 * 变体，也由 {@link #getPreparedStatementCreator(String, SqlParameterSource)} 委托。
	 * @param sql 要执行的SQL语句
	 * @param paramSource 要绑定的参数的容器
	 * @param customizer 用于在正在使用的 {@link PreparedStatementCreatorFactory} 上设置更多属性的回调，在实际 {@code newPreparedStatementCreator} 调用之前应用
	 * @return 对应{@link PreparedStatementCreator}
	 * @since 5.0.5
	 * @see #getParsedSql(String)
	 * @see PreparedStatementCreatorFactory#PreparedStatementCreatorFactory(String, List)
	 * @see PreparedStatementCreatorFactory#newPreparedStatementCreator(Object[])
	 */
	protected PreparedStatementCreator getPreparedStatementCreator(String sql, SqlParameterSource paramSource,
			@Nullable Consumer<PreparedStatementCreatorFactory> customizer) {

		ParsedSql parsedSql = getParsedSql(sql);
		PreparedStatementCreatorFactory pscf = getPreparedStatementCreatorFactory(parsedSql, paramSource);
		if (customizer != null) {
			customizer.accept(pscf);
		}
		@Nullable Object[] params = NamedParameterUtils.buildValueArray(parsedSql, paramSource, null);
		return pscf.newPreparedStatementCreator(params);
	}

	/**
	 * 获取给定 SQL 语句的解析表示。 <p>默认实现使用LRU缓存，上限为256个条目。
	 * @param sql 原始SQL语句
	 * @return 解析后的 SQL 语句的表示
	 */
	protected ParsedSql getParsedSql(String sql) {
		Assert.notNull(sql, "SQL must not be null");
		return this.parsedSqlCache.get(sql);
	}

	/**
	 * 根据给定的 SQL 和命名参数构建 {@link PreparedStatementCreatorFactory}。
	 * @param parsedSql 给定 SQL 语句的解析表示
	 * @param paramSource 要绑定的参数的容器
	 * @return 对应{@link PreparedStatementCreatorFactory}
	 * @since 5.1.3
	 * @see #getPreparedStatementCreator(String, SqlParameterSource, Consumer)
	 * @see #getParsedSql(String)
	 */
	protected PreparedStatementCreatorFactory getPreparedStatementCreatorFactory(
			ParsedSql parsedSql, SqlParameterSource paramSource) {

		String sqlToUse = NamedParameterUtils.substituteNamedParameters(parsedSql, paramSource);
		List<SqlParameter> declaredParameters = NamedParameterUtils.buildSqlParameterList(parsedSql, paramSource);
		return new PreparedStatementCreatorFactory(sqlToUse, declaredParameters);
	}

}
