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

Spring JDBC 中央模板类：实现 JdbcOperations，管理 DataSource 连接、PreparedStatement/CallableStatement 生命周期、异常翻译与各类 query/update 回调。
===== [OCA 中文解析结束] ===== */
package org.springframework.jdbc.core;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.BatchUpdateException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.sql.DataSource;

import org.jspecify.annotations.Nullable;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.InvalidResultSetAccessException;
import org.springframework.jdbc.SQLWarningException;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.jdbc.datasource.ConnectionProxy;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.JdbcAccessor;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedCaseInsensitiveMap;

/* ===== [OCA 中文解析] =====
class JdbcTemplate — 意图说明

JDBC 核心模板：封装连接/语句资源管理、SQL 执行与 SQLException 翻译，通过 ConnectionCallback/PreparedStatementCallback 等回调让应用代码专注 SQL 与结果映射。

（本注释由 open-code-analyzer 生成，置于原有文档注释之前）
===== [OCA 中文解析结束] ===== */
/**
 * <b>JDBC 核心包中的中央委托类。</b> 可直接完成各类 JDBC 操作；自 6.1 起也可考虑更简洁的 {@link org.springframework.jdbc.core.simple.JdbcClient}。
 * <p>简化 JDBC 使用、避免常见错误：执行查询/更新、遍历 ResultSet，并将 {@link SQLException} 翻译为 {@code org.springframework.dao} 异常层次。
 * <p>应用代码实现 {@link PreparedStatementCreator}、{@link ResultSetExtractor}、{@link RowMapper} 等回调即可，无需子类化本模板。
 * <p>配置完成后实例线程安全；DataSource 应作为 Bean 注入。
 * <p>所有 SQL 操作在 debug 级别记录，日志类别为 {@code org.springframework.jdbc.core.JdbcTemplate}。
 * <p><b>注意：6.1+ 提供 {@link org.springframework.jdbc.core.simple.JdbcClient} 流式 API，底层仍委托本类与 {@code NamedParameterJdbcTemplate}。</b>
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Thomas Risberg
 * @author Yanming Zhou
 * @since May 3, 2001
 * @see JdbcOperations
 * @see PreparedStatementCreator
 * @see PreparedStatementSetter
 * @see CallableStatementCreator
 * @see PreparedStatementCallback
 * @see CallableStatementCallback
 * @see ResultSetExtractor
 * @see RowCallbackHandler
 * @see RowMapper
 * @see org.springframework.jdbc.support.SQLExceptionTranslator
 * @see org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
 */
public class JdbcTemplate extends JdbcAccessor implements JdbcOperations {

	private static final String RETURN_RESULT_SET_PREFIX = "#result-set-";

	private static final String RETURN_UPDATE_COUNT_PREFIX = "#update-count-";


	/**
	 */
	private boolean ignoreWarnings = true;

	/**
	 * 如果此变量设置为非负值，它将用于设置用于查询处理的语句的 fetchSize 属性。
	 */
	private int fetchSize = -1;

	/**
	 * 如果此变量设置为非负值，它将用于设置用于查询处理的语句的 maxRows 属性。
	 */
	private int maxRows = -1;

	/**
	 * 如果此变量设置为非负值，它将用于设置用于查询处理的语句的 queryTimeout 属性。
	 */
	private int queryTimeout = -1;

	/**
	 * 如果此变量设置为 true，则任何可调用语句处理都将绕过所有结果检查。这可用于避免某些较旧的 Oracle JDBC 驱动程序（如 10.1.0.2）中的错误。
	 */
	private boolean skipResultsProcessing = false;

	/**
	 * 如果此变量设置为 true，则将绕过没有相应 SqlOutParameter 声明的存储过程调用的所有结果。除非将变量 {@code
	 * skipResultsProcessing} 设置为 {@code true}，否则将进行所有其他结果处理。
	 */
	private boolean skipUndeclaredResults = false;

	/**
	 * 如果此变量设置为 true，则 CallableStatement 的执行将返回 Map 中的结果，该 Map 使用不区分大小写的参数名称。
	 */
	private boolean resultsMapCaseInsensitive = false;


	/**
	 * 构造一个新的 JdbcTemplate 以供 bean 使用。 <p>注意：在使用实例之前必须设置数据源。
	 * @see #setDataSource
	 */
	public JdbcTemplate() {
	}

	/**
	 * 构造一个新的 JdbcTemplate，给定一个从中获取连接的 DataSource。 <p>注意：这不会触发异常转换器的初始化。
	 * @param dataSource 从中获取连接的 JDBC 数据源
	 */
	public JdbcTemplate(DataSource dataSource) {
		setDataSource(dataSource);
		afterPropertiesSet();
	}

	/**
	 * 构造一个新的 JdbcTemplate，给定一个从中获取连接的 DataSource。 <p>注意：根据“lazyInit”标志，将触发异常转换器的初始化。
	 * @param dataSource 从中获取连接的 JDBC 数据源
	 * @param lazyInit 是否延迟初始化 SQLExceptionTranslator
	 */
	public JdbcTemplate(DataSource dataSource, boolean lazyInit) {
		setDataSource(dataSource);
		setLazyInit(lazyInit);
		afterPropertiesSet();
	}

	/**
	 * 派生 JdbcTemplate 的复制构造函数。
	 * @param original 要复制的原始模板
	 * @since 7.0
	 */
	public JdbcTemplate(JdbcAccessor original) {
		setDataSource(original.getDataSource());
		setExceptionTranslator(original.getExceptionTranslator());
		setLazyInit(original.isLazyInit());
		if (original instanceof JdbcTemplate originalTemplate) {
			setIgnoreWarnings(originalTemplate.isIgnoreWarnings());
			setFetchSize(originalTemplate.getFetchSize());
			setMaxRows(originalTemplate.getMaxRows());
			setQueryTimeout(originalTemplate.getQueryTimeout());
			setSkipResultsProcessing(originalTemplate.isSkipResultsProcessing());
			setSkipUndeclaredResults(originalTemplate.isSkipUndeclaredResults());
			setResultsMapCaseInsensitive(originalTemplate.isResultsMapCaseInsensitive());
		}
	}


	/**
	 * 设置是否要忽略 JDBC 语句警告 ({@link SQLWarning})。 <p>Default 是 {@code true}，吞并并记录所有警告。将此标志切换为
	 * {@code false} 以使此 JdbcTemplate 改为抛出 {@link SQLWarningException}（或将 {@link SQLWarning}
	 * 链接到主 {@link SQLException}（如果有））。
	 * @see Statement#getWarnings()
	 * @see java.sql.SQLWarning
	 * @see org.springframework.jdbc.SQLWarningException
	 * @see #handleWarnings(Statement)
	 */
	public void setIgnoreWarnings(boolean ignoreWarnings) {
		this.ignoreWarnings = ignoreWarnings;
	}

	/**
	 * 返回我们是否忽略 SQLWarnings。
	 */
	public boolean isIgnoreWarnings() {
		return this.ignoreWarnings;
	}

	/**
	 * 设置此 JdbcTemplate 的获取大小。这对于处理大型结果集很重要：将其设置为高于默认值会以内存消耗为代价提高处理速度；设置较低的值可以避免传输应用程序永远不会读取的行数
	 * 据。 <p>Default 为 -1，表示使用 JDBC 驱动程序的默认配置（即不将特定的获取大小设置传递给驱动程序）。 <p>注意：从 4.3 开始，除 -1 之外的负值将传
	 * 递给驱动程序，因为 MySQL 支持 {@code Integer.MIN_VALUE} 的特殊行为。
	 * @see java.sql.Statement#setFetchSize
	 */
	public void setFetchSize(int fetchSize) {
		this.fetchSize = fetchSize;
	}

	/**
	 * 返回为此 JdbcTemplate 指定的获取大小。
	 */
	public int getFetchSize() {
		return this.fetchSize;
	}

	/**
	 * 设置此 JdbcTemplate 的最大行数。这对于处理大型结果集的子集非常重要，如果我们一开始就对整个结果不感兴趣（例如，在执行可能返回大量匹配项的搜索时），则可以避免读取并
	 * 保存数据库或 JDBC 驱动程序中的整个结果集。 <p>Default 为 -1，表示使用 JDBC 驱动程序的默认配置（即不将特定的最大行数设置传递给驱动程序）。 <p>注意
	 * ：从 4.3 开始，除 -1 之外的负值将传递给驱动程序，与 {@link #setFetchSize} 对特殊 MySQL 值的支持同步。
	 * @see java.sql.Statement#setMaxRows
	 */
	public void setMaxRows(int maxRows) {
		this.maxRows = maxRows;
	}

	/**
	 * 返回为此 JdbcTemplate 指定的最大行数。
	 */
	public int getMaxRows() {
		return this.maxRows;
	}

	/**
	 * 设置此 JdbcTemplate 执行的语句的查询超时（秒）。 <p>Default 为 -1，表示使用 JDBC 驱动程序的默认值（即不传递驱动程序上的特定查询超时设置）。 
	 * <p>注意：当在事务级别指定了超时的事务中执行时，此处指定的任何超时都将被剩余事务超时覆盖。
	 * @see java.sql.Statement#setQueryTimeout
	 */
	public void setQueryTimeout(int queryTimeout) {
		this.queryTimeout = queryTimeout;
	}

	/**
	 * 返回此 JdbcTemplate 执行的语句的查询超时（秒）。
	 */
	public int getQueryTimeout() {
		return this.queryTimeout;
	}

	/**
	 * 设置是否应跳过结果处理。当我们知道没有结果被传回时，可用于优化可调用语句处理 - 输出参数的处理仍将进行。这可用于避免某些较旧的 Oracle JDBC 驱动程序（如 10.1
	 * .0.2）中的错误。
	 */
	public void setSkipResultsProcessing(boolean skipResultsProcessing) {
		this.skipResultsProcessing = skipResultsProcessing;
	}

	/**
	 * 返回是否应跳过结果处理。
	 */
	public boolean isSkipResultsProcessing() {
		return this.skipResultsProcessing;
	}

	/**
	 * 设置是否应跳过未声明的结果。
	 */
	public void setSkipUndeclaredResults(boolean skipUndeclaredResults) {
		this.skipUndeclaredResults = skipUndeclaredResults;
	}

	/**
	 * 返回是否应跳过未声明的结果。
	 */
	public boolean isSkipUndeclaredResults() {
		return this.skipUndeclaredResults;
	}

	/**
	 * 设置 CallableStatement 的执行是否将在使用不区分大小写的参数名称的 Map 中返回结果。
	 */
	public void setResultsMapCaseInsensitive(boolean resultsMapCaseInsensitive) {
		this.resultsMapCaseInsensitive = resultsMapCaseInsensitive;
	}

	/**
	 * 返回 CallableStatement 的执行是否会在使用不区分大小写的参数名称的 Map 中返回结果。
	 */
	public boolean isResultsMapCaseInsensitive() {
		return this.resultsMapCaseInsensitive;
	}


	//-------------------------------------------------------------------------
	// 处理普通 java.sql.Connection 的方法
	//-------------------------------------------------------------------------

	/**
	 * 执行 execute 回调并处理 JDBC 资源生命周期。
	 */
	@Override
	public <T extends @Nullable Object> T execute(ConnectionCallback<T> action) throws DataAccessException {
		Assert.notNull(action, "Callback object must not be null");

		Connection con = DataSourceUtils.getConnection(obtainDataSource());
		try {
			// 创建关闭抑制连接代理，同时准备返回的语句。
			Connection conToUse = createConnectionProxy(con);
			return action.doInConnection(conToUse);
		}
		catch (SQLException ex) {
			// 尽早释放连接，避免潜在的连接池死锁
			// 在异常转换器尚未初始化的情况下。
			String sql = getSql(action);
			DataSourceUtils.releaseConnection(con, getDataSource());
			con = null;
			throw translateException("ConnectionCallback", sql, ex);
		}
		finally {
			DataSourceUtils.releaseConnection(con, getDataSource());
		}
	}

	/**
	 * 为给定的 JDBC 连接创建关闭抑制代理。由 {@code execute} 方法调用。 <p>代理还准备返回的 JDBC 语句，应用语句设置，例如获取大小、最大行数和查询超时
	 * 。
	 * @param con 用于创建代理的 JDBC 连接
	 * @return 连接代理
	 * @see java.sql.Connection#close()
	 * @see #execute(ConnectionCallback)
	 * @see #applyStatementSettings
	 */
	protected Connection createConnectionProxy(Connection con) {
		return (Connection) Proxy.newProxyInstance(
				ConnectionProxy.class.getClassLoader(),
				new Class<?>[] {ConnectionProxy.class},
				new CloseSuppressingInvocationHandler(con));
	}


	//-------------------------------------------------------------------------
	// 处理静态 SQL 的方法 (java.sql.Statement)
	//-------------------------------------------------------------------------

	/**
	 * 执行 execute 回调并处理 JDBC 资源生命周期。
	 */
	private <T extends @Nullable Object> T execute(StatementCallback<T> action, boolean closeResources) throws DataAccessException {
		Assert.notNull(action, "Callback object must not be null");

		Connection con = DataSourceUtils.getConnection(obtainDataSource());
		Statement stmt = null;
		try {
			stmt = con.createStatement();
			applyStatementSettings(stmt);
			T result = action.doInStatement(stmt);
			handleWarnings(stmt);
			return result;
		}
		catch (SQLException ex) {
			// 尽早释放连接，避免潜在的连接池死锁
			// 在异常转换器尚未初始化的情况下。
			if (stmt != null) {
				handleWarnings(stmt, ex);
			}
			String sql = getSql(action);
			JdbcUtils.closeStatement(stmt);
			stmt = null;
			DataSourceUtils.releaseConnection(con, getDataSource());
			con = null;
			throw translateException("StatementCallback", sql, ex);
		}
		finally {
			if (closeResources) {
				JdbcUtils.closeStatement(stmt);
				DataSourceUtils.releaseConnection(con, getDataSource());
			}
		}
	}

	/**
	 * 执行 execute 回调并处理 JDBC 资源生命周期。
	 */
	@Override
	public <T extends @Nullable Object> T execute(StatementCallback<T> action) throws DataAccessException {
		return execute(action, true);
	}

	/**
	 * 执行 execute 回调并处理 JDBC 资源生命周期。
	 */
	@Override
	public void execute(String sql) throws DataAccessException {
		if (logger.isDebugEnabled()) {
			logger.debug("Executing SQL statement [" + sql + "]");
		}

		// 执行语句的回调。
		class ExecuteStatementCallback implements StatementCallback<@Nullable Object>, SqlProvider {
			@Override
			public @Nullable Object doInStatement(Statement stmt) throws SQLException {
				stmt.execute(sql);
				return null;
			}
			@Override
			public String getSql() {
				return sql;
			}
		}

		execute(new ExecuteStatementCallback(), true);
	}

	/**
	 * 执行 query 方法的核心逻辑。
	 */
	@Override
	public <T extends @Nullable Object> T query(String sql, ResultSetExtractor<T> rse) throws DataAccessException {
		Assert.notNull(sql, "SQL must not be null");
		Assert.notNull(rse, "ResultSetExtractor must not be null");
		if (logger.isDebugEnabled()) {
			logger.debug("Executing SQL query [" + sql + "]");
		}

		// 执行查询的回调。
		class QueryStatementCallback implements StatementCallback<T>, SqlProvider {
			@Override
			public T doInStatement(Statement stmt) throws SQLException {
				ResultSet rs = null;
				try {
					rs = stmt.executeQuery(sql);
					return rse.extractData(rs);
				}
				finally {
					JdbcUtils.closeResultSet(rs);
				}
			}
			@Override
			public String getSql() {
				return sql;
			}
		}

		return execute(new QueryStatementCallback(), true);
	}

	/**
	 * 执行 query 方法的核心逻辑。
	 */
	@Override
	public void query(String sql, RowCallbackHandler rch) throws DataAccessException {
		query(sql, new RowCallbackHandlerResultSetExtractor(rch, this.maxRows));
	}

	/**
	 * 执行 query 方法的核心逻辑。
	 */
	@Override
	public <T extends @Nullable Object> List<T> query(String sql, RowMapper<T> rowMapper) throws DataAccessException {
		return result(query(sql, new RowMapperResultSetExtractor<>(rowMapper, 0, this.maxRows)));
	}

	/**
	 * 执行 queryForStream 方法的核心逻辑。
	 */
	@Override
	public <T> Stream<T> queryForStream(String sql, RowMapper<T> rowMapper) throws DataAccessException {
		class StreamStatementCallback implements StatementCallback<Stream<T>>, SqlProvider {
			@Override
			public Stream<T> doInStatement(Statement stmt) throws SQLException {
				ResultSet rs = stmt.executeQuery(sql);
				Connection con = stmt.getConnection();
				return new ResultSetSpliterator<>(rs, rowMapper, JdbcTemplate.this.maxRows).stream().onClose(() -> {
					JdbcUtils.closeResultSet(rs);
					JdbcUtils.closeStatement(stmt);
					DataSourceUtils.releaseConnection(con, getDataSource());
				});
			}
			@Override
			public String getSql() {
				return sql;
			}
		}

		return result(execute(new StreamStatementCallback(), false));
	}

	/**
	 * 执行 queryForMap 方法的核心逻辑。
	 */
	@Override
	public Map<String, @Nullable Object> queryForMap(String sql) throws DataAccessException {
		return result(queryForObject(sql, getColumnMapRowMapper()));
	}

	/**
	 * 执行 queryForObject 方法的核心逻辑。
	 */
	@Override
	public <T extends @Nullable Object> T queryForObject(String sql, RowMapper<T> rowMapper) throws DataAccessException {
		List<T> results = query(sql, rowMapper);
		return DataAccessUtils.nullableSingleResult(results);
	}

	/**
	 * 执行 queryForObject 方法的核心逻辑。
	 */
	@Override
	public <T> @Nullable T queryForObject(String sql, Class<T> requiredType) throws DataAccessException {
		return queryForObject(sql, getSingleColumnRowMapper(requiredType));
	}

	/**
	 * 执行 queryForList 方法的核心逻辑。
	 */
	@Override
	public <T> List<@Nullable T> queryForList(String sql, Class<T> elementType) throws DataAccessException {
		return query(sql, getSingleColumnRowMapper(elementType));
	}

	/**
	 * 执行 queryForList 方法的核心逻辑。
	 */
	@Override
	public List<Map<String, @Nullable Object>> queryForList(String sql) throws DataAccessException {
		return query(sql, getColumnMapRowMapper());
	}

	/**
	 * 执行 queryForRowSet 方法的核心逻辑。
	 */
	@Override
	public SqlRowSet queryForRowSet(String sql) throws DataAccessException {
		return result(query(sql, new SqlRowSetResultSetExtractor()));
	}

	/**
	 * 更新（方法 `update`）。
	 */
	@Override
	public int update(String sql) throws DataAccessException {
		Assert.notNull(sql, "SQL must not be null");
		if (logger.isDebugEnabled()) {
			logger.debug("Executing SQL update [" + sql + "]");
		}

		// 执行更新语句的回调。
		class UpdateStatementCallback implements StatementCallback<Integer>, SqlProvider {
			@Override
			public Integer doInStatement(Statement stmt) throws SQLException {
				int rows = stmt.executeUpdate(sql);
				if (logger.isTraceEnabled()) {
					logger.trace("SQL update affected " + rows + " rows");
				}
				return rows;
			}
			@Override
			public String getSql() {
				return sql;
			}
		}

		return updateCount(execute(new UpdateStatementCallback(), true));
	}

	/**
	 * 执行 batchUpdate 方法的核心逻辑。
	 */
	@Override
	public int[] batchUpdate(String... sql) throws DataAccessException {
		Assert.notEmpty(sql, "SQL array must not be empty");
		if (logger.isDebugEnabled()) {
			logger.debug("Executing SQL batch update of " + sql.length + " statements");
		}

		// 执行批量更新的回调。
		class BatchUpdateStatementCallback implements StatementCallback<int[]>, SqlProvider {

			private final StringBuilder currSql = new StringBuilder();

			@Override
			public int[] doInStatement(Statement stmt) throws SQLException, DataAccessException {
				int[] rowsAffected = new int[sql.length];
				if (JdbcUtils.supportsBatchUpdates(stmt.getConnection())) {
					for (String sqlStmt : sql) {
						appendSql(sqlStmt);
						stmt.addBatch(sqlStmt);
					}
					try {
						rowsAffected = stmt.executeBatch();
					}
					catch (BatchUpdateException ex) {
						this.currSql.setLength(0);
						int[] updateCounts = ex.getUpdateCounts();
						for (int i = 0; i < ex.getUpdateCounts().length; i++) {
							if (updateCounts[i] == Statement.EXECUTE_FAILED) {
								appendSql(sql[i]);
							}
						}
						throw ex;
					}
				}
				else {
					for (int i = 0; i < sql.length; i++) {
						this.currSql.setLength(0);
						this.currSql.append(sql[i]);
						if (!stmt.execute(sql[i])) {
							rowsAffected[i] = stmt.getUpdateCount();
						}
						else {
							throw new InvalidDataAccessApiUsageException("Invalid batch SQL statement: " + sql[i]);
						}
					}
				}
				return rowsAffected;
			}

			private void appendSql(String statement) {
				if (!this.currSql.isEmpty()) {
					this.currSql.append("; ");
				}
				this.currSql.append(statement);
			}

			@Override
			public @Nullable String getSql() {
				return this.currSql.toString();
			}
		}

		int[] result = execute(new BatchUpdateStatementCallback(), true);
		Assert.state(result != null, "No update counts");
		return result;
	}


	//-------------------------------------------------------------------------
	// 处理准备好的语句的方法
	//-------------------------------------------------------------------------

	/**
	 * 执行 execute 回调并处理 JDBC 资源生命周期。
	 */
	private <T extends @Nullable Object> T execute(PreparedStatementCreator psc, PreparedStatementCallback<T> action, boolean closeResources)
			throws DataAccessException {

		Assert.notNull(psc, "PreparedStatementCreator must not be null");
		Assert.notNull(action, "Callback object must not be null");
		if (logger.isDebugEnabled()) {
			String sql = getSql(psc);
			logger.debug("Executing prepared SQL statement" + (sql != null ? " [" + sql + "]" : ""));
		}

		Connection con = DataSourceUtils.getConnection(obtainDataSource());
		PreparedStatement ps = null;
		try {
			ps = psc.createPreparedStatement(con);
			applyStatementSettings(ps);
			T result = action.doInPreparedStatement(ps);
			handleWarnings(ps);
			return result;
		}
		catch (SQLException ex) {
			// 尽早释放连接，避免潜在的连接池死锁
			// 在异常转换器尚未初始化的情况下。
			if (psc instanceof ParameterDisposer parameterDisposer) {
				parameterDisposer.cleanupParameters();
			}
			if (ps != null) {
				handleWarnings(ps, ex);
			}
			String sql = getSql(psc);
			psc = null;
			JdbcUtils.closeStatement(ps);
			ps = null;
			DataSourceUtils.releaseConnection(con, getDataSource());
			con = null;
			throw translateException("PreparedStatementCallback", sql, ex);
		}
		finally {
			if (closeResources) {
				if (psc instanceof ParameterDisposer parameterDisposer) {
					parameterDisposer.cleanupParameters();
				}
				JdbcUtils.closeStatement(ps);
				DataSourceUtils.releaseConnection(con, getDataSource());
			}
		}
	}

	/**
	 * 执行 execute 回调并处理 JDBC 资源生命周期。
	 */
	@Override
	public <T extends @Nullable Object> T execute(PreparedStatementCreator psc, PreparedStatementCallback<T> action)
			throws DataAccessException {

		return execute(psc, action, true);
	}

	/**
	 * 执行 execute 回调并处理 JDBC 资源生命周期。
	 */
	@Override
	public <T extends @Nullable Object> T execute(String sql, PreparedStatementCallback<T> action) throws DataAccessException {
		return execute(new SimplePreparedStatementCreator(sql), action, true);
	}

	/**
	 * 使用准备好的语句进行查询，允许使用PreparedStatementCreator 和PreparedStatementSetter。大多数其他查询方法都使用此方法，但应用程序
	 * 代码将始终与创建者或设置者一起使用。
	 * @param psc 给定连接创建一个PreparedStatement的回调
	 * @param pss 知道如何在准备好的语句上设置值的回调。如果这是 {@code null}，则将假定 SQL 不包含绑定参数。
	 * @param rse 将提取结果的回调
	 * @return 任意结果对象，由 ResultSetExtractor 返回
	 * @throws DataAccessException 如果有任何问题
	 */
	public <T extends @Nullable Object> T query(
			PreparedStatementCreator psc, @Nullable PreparedStatementSetter pss, ResultSetExtractor<T> rse)
			throws DataAccessException {

		Assert.notNull(rse, "ResultSetExtractor must not be null");
		logger.debug("Executing prepared SQL query");

		return execute(psc, (PreparedStatementCallback<T>) ps -> {
			ResultSet rs = null;
			try {
				if (pss != null) {
					pss.setValues(ps);
				}
				rs = ps.executeQuery();
				return rse.extractData(rs);
			}
			finally {
				JdbcUtils.closeResultSet(rs);
				if (pss instanceof ParameterDisposer parameterDisposer) {
					parameterDisposer.cleanupParameters();
				}
			}
		}, true);
	}

	/**
	 * 执行 query 方法的核心逻辑。
	 */
	@Override
	public <T extends @Nullable Object> T query(PreparedStatementCreator psc, ResultSetExtractor<T> rse) throws DataAccessException {
		return query(psc, null, rse);
	}

	/**
	 * 执行 query 方法的核心逻辑。
	 */
	@Override
	public <T extends @Nullable Object> T query(String sql, @Nullable PreparedStatementSetter pss, ResultSetExtractor<T> rse) throws DataAccessException {
		return query(new SimplePreparedStatementCreator(sql), pss, rse);
	}

	/**
	 * 执行 query 方法的核心逻辑。
	 */
	@Override
	public <T extends @Nullable Object> T query(String sql, @Nullable Object @Nullable [] args, int[] argTypes, ResultSetExtractor<T> rse) throws DataAccessException {
		return query(sql, newArgTypePreparedStatementSetter(args, argTypes), rse);
	}

	/**
	 * 执行 query 方法的核心逻辑。
	 */
	@Deprecated(since = "5.3")
	@Override
	public <T extends @Nullable Object> T query(String sql, @Nullable Object @Nullable [] args, ResultSetExtractor<T> rse) throws DataAccessException {
		return query(sql, newArgPreparedStatementSetter(args), rse);
	}

	/**
	 * 执行 query 方法的核心逻辑。
	 */
	@Override
	public <T extends @Nullable Object> T query(String sql, ResultSetExtractor<T> rse, @Nullable Object @Nullable ... args) throws DataAccessException {
		return query(sql, newArgPreparedStatementSetter(args), rse);
	}

	/**
	 * 执行 query 方法的核心逻辑。
	 */
	@Override
	public void query(PreparedStatementCreator psc, RowCallbackHandler rch) throws DataAccessException {
		query(psc, new RowCallbackHandlerResultSetExtractor(rch, this.maxRows));
	}

	/**
	 * 执行 query 方法的核心逻辑。
	 */
	@Override
	public void query(String sql, @Nullable PreparedStatementSetter pss, RowCallbackHandler rch) throws DataAccessException {
		query(sql, pss, new RowCallbackHandlerResultSetExtractor(rch, this.maxRows));
	}

	/**
	 * 执行 query 方法的核心逻辑。
	 */
	@Override
	public void query(String sql, @Nullable Object @Nullable [] args, int[] argTypes, RowCallbackHandler rch) throws DataAccessException {
		query(sql, newArgTypePreparedStatementSetter(args, argTypes), rch);
	}

	/**
	 * 执行 query 方法的核心逻辑。
	 */
	@Deprecated(since = "5.3")
	@Override
	public void query(String sql, @Nullable Object @Nullable [] args, RowCallbackHandler rch) throws DataAccessException {
		query(sql, newArgPreparedStatementSetter(args), rch);
	}

	/**
	 * 执行 query 方法的核心逻辑。
	 */
	@Override
	public void query(String sql, RowCallbackHandler rch, @Nullable Object @Nullable ... args) throws DataAccessException {
		query(sql, newArgPreparedStatementSetter(args), rch);
	}

	/**
	 * 执行 query 方法的核心逻辑。
	 */
	@Override
	public <T extends @Nullable Object> List<T> query(PreparedStatementCreator psc, RowMapper<T> rowMapper) throws DataAccessException {
		return result(query(psc, new RowMapperResultSetExtractor<>(rowMapper, 0, this.maxRows)));
	}

	/**
	 * 执行 query 方法的核心逻辑。
	 */
	@Override
	public <T extends @Nullable Object> List<T> query(String sql, @Nullable PreparedStatementSetter pss, RowMapper<T> rowMapper) throws DataAccessException {
		return result(query(sql, pss, new RowMapperResultSetExtractor<>(rowMapper, 0, this.maxRows)));
	}

	/**
	 * 执行 query 方法的核心逻辑。
	 */
	@Override
	public <T extends @Nullable Object> List<T> query(String sql, @Nullable Object @Nullable [] args, int[] argTypes, RowMapper<T> rowMapper) throws DataAccessException {
		return result(query(sql, args, argTypes, new RowMapperResultSetExtractor<>(rowMapper, 0, this.maxRows)));
	}

	/**
	 * 执行 query 方法的核心逻辑。
	 */
	@Deprecated(since = "5.3")
	@Override
	public <T extends @Nullable Object> List<T> query(String sql, @Nullable Object @Nullable [] args, RowMapper<T> rowMapper) throws DataAccessException {
		return result(query(sql, newArgPreparedStatementSetter(args), new RowMapperResultSetExtractor<>(rowMapper, 0, this.maxRows)));
	}

	/**
	 * 执行 query 方法的核心逻辑。
	 */
	@Override
	public <T extends @Nullable Object> List<T> query(String sql, RowMapper<T> rowMapper, @Nullable Object @Nullable ... args) throws DataAccessException {
		return result(query(sql, newArgPreparedStatementSetter(args), new RowMapperResultSetExtractor<>(rowMapper, 0, this.maxRows)));
	}

	/**
	 * 使用准备好的语句进行查询，允许使用PreparedStatementCreator 和PreparedStatementSetter。大多数其他查询方法都使用此方法，但应用程序
	 * 代码将始终与创建者或设置者一起使用。
	 * @param psc 给定连接创建一个PreparedStatement的回调
	 * @param pss 知道如何在准备好的语句上设置值的回调。如果这是 {@code null}，则将假定 SQL 不包含绑定参数。
	 * @param rowMapper 将映射每行一个对象的回调
	 * @return 结果流，包含映射对象，完全处理后需要关闭（例如，通过 try-with-resources 子句）
	 * @throws DataAccessException 如果查询失败
	 * @since 5.3
	 */
	public <T extends @Nullable Object> Stream<T> queryForStream(PreparedStatementCreator psc, @Nullable PreparedStatementSetter pss,
			RowMapper<T> rowMapper) throws DataAccessException {

		return result(execute(psc, ps -> {
			if (pss != null) {
				pss.setValues(ps);
			}
			ResultSet rs = ps.executeQuery();
			Connection con = ps.getConnection();
			return new ResultSetSpliterator<>(rs, rowMapper, this.maxRows).stream().onClose(() -> {
				JdbcUtils.closeResultSet(rs);
				if (pss instanceof ParameterDisposer parameterDisposer) {
					parameterDisposer.cleanupParameters();
				}
				JdbcUtils.closeStatement(ps);
				DataSourceUtils.releaseConnection(con, getDataSource());
			});
		}, false));
	}

	/**
	 * 执行 queryForStream 方法的核心逻辑。
	 */
	@Override
	public <T extends @Nullable Object> Stream<T> queryForStream(PreparedStatementCreator psc, RowMapper<T> rowMapper) throws DataAccessException {
		return queryForStream(psc, null, rowMapper);
	}

	/**
	 * 执行 queryForStream 方法的核心逻辑。
	 */
	@Override
	public <T extends @Nullable Object> Stream<T> queryForStream(String sql, @Nullable PreparedStatementSetter pss, RowMapper<T> rowMapper) throws DataAccessException {
		return queryForStream(new SimplePreparedStatementCreator(sql), pss, rowMapper);
	}

	/**
	 * 执行 queryForStream 方法的核心逻辑。
	 */
	@Override
	public <T extends @Nullable Object> Stream<T> queryForStream(String sql, RowMapper<T> rowMapper, @Nullable Object @Nullable ... args) throws DataAccessException {
		return queryForStream(new SimplePreparedStatementCreator(sql), newArgPreparedStatementSetter(args), rowMapper);
	}

	/**
	 * 执行 queryForObject 方法的核心逻辑。
	 */
	@Override
	public <T extends @Nullable Object> T queryForObject(String sql, @Nullable Object @Nullable [] args, int[] argTypes, RowMapper<T> rowMapper)
			throws DataAccessException {

		List<T> results = query(sql, args, argTypes, new RowMapperResultSetExtractor<>(rowMapper, 1));
		return DataAccessUtils.nullableSingleResult(results);
	}

	/**
	 * 执行 queryForObject 方法的核心逻辑。
	 */
	@Deprecated(since = "5.3")
	@Override
	public <T extends @Nullable Object> T queryForObject(String sql, @Nullable Object @Nullable [] args, RowMapper<T> rowMapper) throws DataAccessException {
		List<T> results = query(sql, newArgPreparedStatementSetter(args), new RowMapperResultSetExtractor<>(rowMapper, 1));
		return DataAccessUtils.nullableSingleResult(results);
	}

	/**
	 * 执行 queryForObject 方法的核心逻辑。
	 */
	@Override
	public <T extends @Nullable Object> T queryForObject(String sql, RowMapper<T> rowMapper, @Nullable Object @Nullable ... args) throws DataAccessException {
		List<T> results = query(sql, newArgPreparedStatementSetter(args), new RowMapperResultSetExtractor<>(rowMapper, 1));
		return DataAccessUtils.nullableSingleResult(results);
	}

	/**
	 * 执行 queryForObject 方法的核心逻辑。
	 */
	@Override
	public <T> @Nullable T queryForObject(String sql, @Nullable Object @Nullable [] args, int[] argTypes, Class<T> requiredType)
			throws DataAccessException {

		return queryForObject(sql, args, argTypes, getSingleColumnRowMapper(requiredType));
	}

	/**
	 * 执行 queryForObject 方法的核心逻辑。
	 */
	@Deprecated(since = "5.3")
	@Override
	public <T> @Nullable T queryForObject(String sql, @Nullable Object @Nullable [] args, Class<T> requiredType) throws DataAccessException {
		return queryForObject(sql, getSingleColumnRowMapper(requiredType), args);
	}

	/**
	 * 执行 queryForObject 方法的核心逻辑。
	 */
	@Override
	public <T> @Nullable T queryForObject(String sql, Class<T> requiredType, @Nullable Object @Nullable ... args) throws DataAccessException {
		return queryForObject(sql, getSingleColumnRowMapper(requiredType), args);
	}

	/**
	 * 执行 queryForMap 方法的核心逻辑。
	 */
	@Override
	public Map<String, @Nullable Object> queryForMap(String sql, @Nullable Object @Nullable [] args, int[] argTypes) throws DataAccessException {
		return result(queryForObject(sql, args, argTypes, getColumnMapRowMapper()));
	}

	/**
	 * 执行 queryForMap 方法的核心逻辑。
	 */
	@Override
	public Map<String, @Nullable Object> queryForMap(String sql, @Nullable Object @Nullable ... args) throws DataAccessException {
		return result(queryForObject(sql, getColumnMapRowMapper(), args));
	}

	/**
	 * 执行 queryForList 方法的核心逻辑。
	 */
	@Override
	public <T> List<@Nullable T> queryForList(String sql, @Nullable Object @Nullable [] args, int[] argTypes, Class<T> elementType) throws DataAccessException {
		return query(sql, args, argTypes, getSingleColumnRowMapper(elementType));
	}

	/**
	 * 执行 queryForList 方法的核心逻辑。
	 */
	@Deprecated(since = "5.3")
	@Override
	public <T> List<@Nullable T> queryForList(String sql, @Nullable Object @Nullable [] args, Class<T> elementType) throws DataAccessException {
		return query(sql, newArgPreparedStatementSetter(args), getSingleColumnRowMapper(elementType));
	}

	/**
	 * 执行 queryForList 方法的核心逻辑。
	 */
	@Override
	public <T> List<@Nullable T> queryForList(String sql, Class<T> elementType, @Nullable Object @Nullable ... args) throws DataAccessException {
		return query(sql, newArgPreparedStatementSetter(args), getSingleColumnRowMapper(elementType));
	}

	/**
	 * 执行 queryForList 方法的核心逻辑。
	 */
	@Override
	public List<Map<String, @Nullable Object>> queryForList(String sql, @Nullable Object @Nullable [] args, int[] argTypes) throws DataAccessException {
		return query(sql, args, argTypes, getColumnMapRowMapper());
	}

	/**
	 * 执行 queryForList 方法的核心逻辑。
	 */
	@Override
	public List<Map<String, @Nullable Object>> queryForList(String sql, @Nullable Object @Nullable ... args) throws DataAccessException {
		return query(sql, newArgPreparedStatementSetter(args), getColumnMapRowMapper());
	}

	/**
	 * 执行 queryForRowSet 方法的核心逻辑。
	 */
	@Override
	public SqlRowSet queryForRowSet(String sql, @Nullable Object @Nullable [] args, int[] argTypes) throws DataAccessException {
		return result(query(sql, args, argTypes, new SqlRowSetResultSetExtractor()));
	}

	/**
	 * 执行 queryForRowSet 方法的核心逻辑。
	 */
	@Override
	public SqlRowSet queryForRowSet(String sql, @Nullable Object @Nullable ... args) throws DataAccessException {
		return result(query(sql, newArgPreparedStatementSetter(args), new SqlRowSetResultSetExtractor()));
	}

	/**
	 * 更新（方法 `update`）。
	 */
	protected int update(PreparedStatementCreator psc, @Nullable PreparedStatementSetter pss)
			throws DataAccessException {

		logger.debug("Executing prepared SQL update");

		return updateCount(execute(psc, ps -> {
			try {
				if (pss != null) {
					pss.setValues(ps);
				}
				int rows = ps.executeUpdate();
				if (logger.isTraceEnabled()) {
					logger.trace("SQL update affected " + rows + " rows");
				}
				return rows;
			}
			finally {
				if (pss instanceof ParameterDisposer parameterDisposer) {
					parameterDisposer.cleanupParameters();
				}
			}
		}, true));
	}

	/**
	 * 更新（方法 `update`）。
	 */
	@Override
	public int update(PreparedStatementCreator psc) throws DataAccessException {
		return update(psc, (PreparedStatementSetter) null);
	}

	/**
	 * 更新（方法 `update`）。
	 */
	@Override
	public int update(PreparedStatementCreator psc, KeyHolder generatedKeyHolder)
			throws DataAccessException {

		Assert.notNull(generatedKeyHolder, "KeyHolder must not be null");
		logger.debug("Executing SQL update and returning generated keys");

		return updateCount(execute(psc, ps -> {
			int rows = ps.executeUpdate();
			generatedKeyHolder.getKeyList().clear();
			storeGeneratedKeys(generatedKeyHolder, ps, 1);
			if (logger.isTraceEnabled()) {
				logger.trace("SQL update affected " + rows + " rows and returned " + generatedKeyHolder.getKeyList().size() + " keys");
			}
			return rows;
		}, true));
	}

	/**
	 * 更新（方法 `update`）。
	 */
	@Override
	public int update(String sql, @Nullable PreparedStatementSetter pss) throws DataAccessException {
		return update(new SimplePreparedStatementCreator(sql), pss);
	}

	/**
	 * 更新（方法 `update`）。
	 */
	@Override
	public int update(String sql, @Nullable Object @Nullable [] args, int[] argTypes) throws DataAccessException {
		return update(sql, newArgTypePreparedStatementSetter(args, argTypes));
	}

	/**
	 * 更新（方法 `update`）。
	 */
	@Override
	public int update(String sql, @Nullable Object @Nullable ... args) throws DataAccessException {
		return update(sql, newArgPreparedStatementSetter(args));
	}

	/**
	 * 执行 batchUpdate 方法的核心逻辑。
	 */
	@Override
	public int[] batchUpdate(PreparedStatementCreator psc, BatchPreparedStatementSetter pss,
			KeyHolder generatedKeyHolder) throws DataAccessException {

		int[] result = execute(psc, getPreparedStatementCallback(pss, generatedKeyHolder));

		Assert.state(result != null, "No result array");
		return result;
	}

	/**
	 * 执行 batchUpdate 方法的核心逻辑。
	 */
	@Override
	public int[] batchUpdate(String sql, BatchPreparedStatementSetter pss) throws DataAccessException {
		if (logger.isDebugEnabled()) {
			logger.debug("Executing SQL batch update [" + sql + "]");
		}
		int batchSize = pss.getBatchSize();
		if (batchSize == 0) {
			return new int[0];
		}

		int[] result = execute(sql, getPreparedStatementCallback(pss, null));
		Assert.state(result != null, "No result array");
		return result;
	}

	/**
	 * 执行 batchUpdate 方法的核心逻辑。
	 */
	@Override
	public int[] batchUpdate(String sql, List<Object[]> batchArgs) throws DataAccessException {
		return batchUpdate(sql, batchArgs, new int[0]);
	}

	/**
	 * 执行 batchUpdate 方法的核心逻辑。
	 */
	@Override
	public int[] batchUpdate(String sql, List<Object[]> batchArgs, int[] argTypes) throws DataAccessException {
		if (batchArgs.isEmpty()) {
			return new int[0];
		}

		return batchUpdate(
				sql,
				new BatchPreparedStatementSetter() {
					@Override
					public void setValues(PreparedStatement ps, int i) throws SQLException {
						Object[] values = batchArgs.get(i);
						int colIndex = 0;
						for (Object value : values) {
							colIndex++;
							if (value instanceof SqlParameterValue paramValue) {
								StatementCreatorUtils.setParameterValue(ps, colIndex, paramValue, paramValue.getValue());
							}
							else {
								int colType;
								if (argTypes.length < colIndex) {
									colType = SqlTypeValue.TYPE_UNKNOWN;
								}
								else {
									colType = argTypes[colIndex - 1];
								}
								StatementCreatorUtils.setParameterValue(ps, colIndex, colType, value);
							}
						}
					}
					@Override
					public int getBatchSize() {
						return batchArgs.size();
					}
				});
	}

	/**
	 * 执行 batchUpdate 方法的核心逻辑。
	 */
	@Override
	public <T> int[][] batchUpdate(String sql, Collection<T> batchArgs, int batchSize,
			ParameterizedPreparedStatementSetter<T> pss) throws DataAccessException {

		if (logger.isDebugEnabled()) {
			logger.debug("Executing SQL batch update [" + sql + "] with a batch size of " + batchSize);
		}
		int[][] result = execute(sql, (PreparedStatementCallback<int[][]>) ps -> {
			List<int[]> rowsAffected = new ArrayList<>();
			try {
				boolean batchSupported = JdbcUtils.supportsBatchUpdates(ps.getConnection());
				int n = 0;
				for (T obj : batchArgs) {
					pss.setValues(ps, obj);
					n++;
					if (batchSupported) {
						ps.addBatch();
						if (n % batchSize == 0 || n == batchArgs.size()) {
							if (logger.isTraceEnabled()) {
								int batchIdx = (n % batchSize == 0) ? n / batchSize : (n / batchSize) + 1;
								int items = n - ((n % batchSize == 0) ? n / batchSize - 1 : (n / batchSize)) * batchSize;
								logger.trace("Sending SQL batch update #" + batchIdx + " with " + items + " items");
							}
							try {
								int[] updateCounts = ps.executeBatch();
								rowsAffected.add(updateCounts);
							}
							catch (BatchUpdateException ex) {
								throw new AggregatedBatchUpdateException(rowsAffected.toArray(int[][]::new), ex);
							}
						}
					}
					else {
						int i = ps.executeUpdate();
						rowsAffected.add(new int[] {i});
					}
				}
				int[][] result1 = new int[rowsAffected.size()][];
				for (int i = 0; i < result1.length; i++) {
					result1[i] = rowsAffected.get(i);
				}
				return result1;
			}
			finally {
				if (pss instanceof ParameterDisposer parameterDisposer) {
					parameterDisposer.cleanupParameters();
				}
			}
		});

		Assert.state(result != null, "No result array");
		return result;
	}


	//-------------------------------------------------------------------------
	// 处理可调用语句的方法
	//-------------------------------------------------------------------------

	/**
	 * 执行 execute 回调并处理 JDBC 资源生命周期。
	 */
	@Override
	public <T extends @Nullable Object> T execute(CallableStatementCreator csc, CallableStatementCallback<T> action)
			throws DataAccessException {

		Assert.notNull(csc, "CallableStatementCreator must not be null");
		Assert.notNull(action, "Callback object must not be null");
		if (logger.isDebugEnabled()) {
			String sql = getSql(csc);
			logger.debug("Calling stored procedure" + (sql != null ? " [" + sql + "]" : ""));
		}

		Connection con = DataSourceUtils.getConnection(obtainDataSource());
		CallableStatement cs = null;
		try {
			cs = csc.createCallableStatement(con);
			applyStatementSettings(cs);
			T result = action.doInCallableStatement(cs);
			handleWarnings(cs);
			return result;
		}
		catch (SQLException ex) {
			// 尽早释放连接，避免潜在的连接池死锁
			// 在异常转换器尚未初始化的情况下。
			if (csc instanceof ParameterDisposer parameterDisposer) {
				parameterDisposer.cleanupParameters();
			}
			if (cs != null) {
				handleWarnings(cs, ex);
			}
			String sql = getSql(csc);
			csc = null;
			JdbcUtils.closeStatement(cs);
			cs = null;
			DataSourceUtils.releaseConnection(con, getDataSource());
			con = null;
			throw translateException("CallableStatementCallback", sql, ex);
		}
		finally {
			if (csc instanceof ParameterDisposer parameterDisposer) {
				parameterDisposer.cleanupParameters();
			}
			JdbcUtils.closeStatement(cs);
			DataSourceUtils.releaseConnection(con, getDataSource());
		}
	}

	/**
	 * 执行 execute 回调并处理 JDBC 资源生命周期。
	 */
	@Override
	public <T extends @Nullable Object> T execute(String callString, CallableStatementCallback<T> action) throws DataAccessException {
		return execute(new SimpleCallableStatementCreator(callString), action);
	}

	/**
	 * 执行 call 方法的核心逻辑。
	 */
	@Override
	public Map<String, @Nullable Object> call(CallableStatementCreator csc, List<SqlParameter> declaredParameters)
			throws DataAccessException {

		List<SqlParameter> updateCountParameters = new ArrayList<>();
		List<SqlParameter> resultSetParameters = new ArrayList<>();
		List<SqlParameter> callParameters = new ArrayList<>();

		for (SqlParameter parameter : declaredParameters) {
			if (parameter.isResultsParameter()) {
				if (parameter instanceof SqlReturnResultSet) {
					resultSetParameters.add(parameter);
				}
				else {
					updateCountParameters.add(parameter);
				}
			}
			else {
				callParameters.add(parameter);
			}
		}

		Map<String, @Nullable Object> result = execute(csc, cs -> {
			boolean retVal = cs.execute();
			int updateCount = cs.getUpdateCount();
			if (logger.isTraceEnabled()) {
				logger.trace("CallableStatement.execute() returned '" + retVal + "'");
				logger.trace("CallableStatement.getUpdateCount() returned " + updateCount);
			}
			Map<String, @Nullable Object> resultsMap = createResultsMap();
			if (retVal || updateCount != -1) {
				resultsMap.putAll(extractReturnedResults(cs, updateCountParameters, resultSetParameters, updateCount));
			}
			resultsMap.putAll(extractOutputParameters(cs, callParameters));
			return resultsMap;
		});

		Assert.state(result != null, "No result map");
		return result;
	}

	/**
	 * 从已完成的存储过程中提取返回的结果集。
	 * @param cs 存储过程的 JDBC 包装器
	 * @param updateCountParameters 存储过程声明的更新计数参数的参数列表
	 * @param resultSetParameters 存储过程声明的 resultSet 参数的参数列表
	 * @return 包含返回结果的地图
	 */
	protected Map<String, @Nullable Object> extractReturnedResults(CallableStatement cs,
			@Nullable List<SqlParameter> updateCountParameters, @Nullable List<SqlParameter> resultSetParameters,
			int updateCount) throws SQLException {

		Map<String, @Nullable Object> results = new LinkedHashMap<>(4);
		int rsIndex = 0;
		int updateIndex = 0;
		boolean moreResults;
		if (!isSkipResultsProcessing()) {
			do {
				if (updateCount == -1) {
					if (resultSetParameters != null && resultSetParameters.size() > rsIndex) {
						SqlReturnResultSet declaredRsParam = (SqlReturnResultSet) resultSetParameters.get(rsIndex);
						results.putAll(processResultSet(cs.getResultSet(), declaredRsParam));
						rsIndex++;
					}
					else {
						if (!isSkipUndeclaredResults()) {
							String rsName = RETURN_RESULT_SET_PREFIX + (rsIndex + 1);
							SqlReturnResultSet undeclaredRsParam = new SqlReturnResultSet(rsName, getColumnMapRowMapper());
							if (logger.isTraceEnabled()) {
								logger.trace("Added default SqlReturnResultSet parameter named '" + rsName + "'");
							}
							results.putAll(processResultSet(cs.getResultSet(), undeclaredRsParam));
							rsIndex++;
						}
					}
				}
				else {
					if (updateCountParameters != null && updateCountParameters.size() > updateIndex) {
						SqlReturnUpdateCount ucParam = (SqlReturnUpdateCount) updateCountParameters.get(updateIndex);
						String declaredUcName = ucParam.getName();
						results.put(declaredUcName, updateCount);
						updateIndex++;
					}
					else {
						if (!isSkipUndeclaredResults()) {
							String undeclaredName = RETURN_UPDATE_COUNT_PREFIX + (updateIndex + 1);
							if (logger.isTraceEnabled()) {
								logger.trace("Added default SqlReturnUpdateCount parameter named '" + undeclaredName + "'");
							}
							results.put(undeclaredName, updateCount);
							updateIndex++;
						}
					}
				}
				moreResults = cs.getMoreResults();
				updateCount = cs.getUpdateCount();
				if (logger.isTraceEnabled()) {
					logger.trace("CallableStatement.getUpdateCount() returned " + updateCount);
				}
			}
			while (moreResults || updateCount != -1);
		}
		return results;
	}

	/**
	 * 从已完成的存储过程中提取输出参数。
	 * @param cs 存储过程的 JDBC 包装器
	 * @param parameters 存储过程的参数列表
	 * @return 包含返回结果的地图
	 */
	protected Map<String, @Nullable Object> extractOutputParameters(CallableStatement cs, List<SqlParameter> parameters)
			throws SQLException {

		Map<String, @Nullable Object> results = CollectionUtils.newLinkedHashMap(parameters.size());
		int sqlColIndex = 1;
		for (SqlParameter param : parameters) {
			if (param instanceof SqlOutParameter outParam) {
				Assert.state(outParam.getName() != null, "Anonymous parameters not allowed");
				SqlReturnType returnType = outParam.getSqlReturnType();
				if (returnType != null) {
					Object out = returnType.getTypeValue(cs, sqlColIndex, outParam.getSqlType(), outParam.getTypeName());
					results.put(outParam.getName(), out);
				}
				else {
					Object out = cs.getObject(sqlColIndex);
					if (out instanceof ResultSet resultSet) {
						if (outParam.isResultSetSupported()) {
							results.putAll(processResultSet(resultSet, outParam));
						}
						else {
							String rsName = outParam.getName();
							SqlReturnResultSet rsParam = new SqlReturnResultSet(rsName, getColumnMapRowMapper());
							results.putAll(processResultSet(resultSet, rsParam));
							if (logger.isTraceEnabled()) {
								logger.trace("Added default SqlReturnResultSet parameter named '" + rsName + "'");
							}
						}
					}
					else {
						results.put(outParam.getName(), out);
					}
				}
			}
			if (!param.isResultsParameter()) {
				sqlColIndex++;
			}
		}
		return results;
	}

	/**
	 * 处理存储过程中给定的 ResultSet。
	 * @param rs 要处理的结果集
	 * @param param 对应的存储过程参数
	 * @return 包含返回结果的地图
	 */
	@SuppressWarnings("NullAway") // See https://github.com/uber/NullAway/issues/950
	protected Map<@Nullable String, @Nullable Object> processResultSet(
			@Nullable ResultSet rs, ResultSetSupportingSqlParameter param) throws SQLException {

		if (rs != null) {
			try {
				if (param.getRowMapper() != null) {
					RowMapper<? extends @Nullable Object> rowMapper = param.getRowMapper();
					Object data = (new RowMapperResultSetExtractor<>(rowMapper)).extractData(rs);
					return Collections.singletonMap(param.getName(), data);
				}
				else if (param.getRowCallbackHandler() != null) {
					RowCallbackHandler rch = param.getRowCallbackHandler();
					(new RowCallbackHandlerResultSetExtractor(rch, -1)).extractData(rs);
					return Collections.singletonMap(param.getName(),
							"ResultSet returned from stored procedure was processed");
				}
				else if (param.getResultSetExtractor() != null) {
					Object data = param.getResultSetExtractor().extractData(rs);
					return Collections.singletonMap(param.getName(), data);
				}
			}
			finally {
				JdbcUtils.closeResultSet(rs);
			}
		}
		return Collections.emptyMap();
	}


	//-------------------------------------------------------------------------
	// 实现挂钩和辅助方法
	//-------------------------------------------------------------------------

	/**
	 * 创建一个新的 RowMapper 以将列读取为键值对。
	 * @return 行映射器的使用
	 * @see ColumnMapRowMapper
	 */
	protected RowMapper<Map<String, @Nullable Object>> getColumnMapRowMapper() {
		return new ColumnMapRowMapper();
	}

	/**
	 * 创建一个新的 RowMapper 用于从单个列读取结果对象。
	 * @param requiredType 每个结果对象期望匹配的类型
	 * @return 行映射器的使用
	 * @see SingleColumnRowMapper
	 */
	protected <T> RowMapper<@Nullable T> getSingleColumnRowMapper(Class<T> requiredType) {
		return new SingleColumnRowMapper<>(requiredType);
	}

	/**
	 * 创建一个用作结果地图的 Map 实例。 <p>如果{@link #resultsMapCaseInsensitive}已设置为true，则会创建{@link
	 * LinkedCaseInsensitiveMap}；否则，将创建 {@link LinkedHashMap}。
	 * @return 结果映射实例
	 * @see #setResultsMapCaseInsensitive
	 * @see #isResultsMapCaseInsensitive
	 */
	protected Map<String, @Nullable Object> createResultsMap() {
		if (isResultsMapCaseInsensitive()) {
			return new LinkedCaseInsensitiveMap<>();
		}
		else {
			return new LinkedHashMap<>();
		}
	}

	/**
	 * 准备给定的 JDBC 语句（或PreparedStatement 或 CallableStatement），应用语句设置，例如获取大小、最大行数和查询超时。
	 * @param stmt 准备的 JDBC 语句
	 * @throws SQLException 如果由 JDBC API 抛出
	 * @see #setFetchSize
	 * @see #setMaxRows
	 * @see #setQueryTimeout
	 * @see org.springframework.jdbc.datasource.DataSourceUtils#applyTransactionTimeout
	 */
	protected void applyStatementSettings(Statement stmt) throws SQLException {
		int fetchSize = getFetchSize();
		if (fetchSize != -1) {
			stmt.setFetchSize(fetchSize);
		}
		int maxRows = getMaxRows();
		if (maxRows != -1) {
			stmt.setMaxRows(maxRows);
		}
		DataSourceUtils.applyTimeout(stmt, getDataSource(), getQueryTimeout());
	}

	/**
	 * 使用传入的参数创建一个新的基于参数的PreparedStatementSetter。 <p> 默认情况下，我们将创建一个 {@link ArgumentPreparedStat
	 * ementSetter}。此方法允许子类覆盖创建。
	 * @param args 带参数的对象数组
	 * @return 使用新的PreparedStatementSetter
	 */
	protected PreparedStatementSetter newArgPreparedStatementSetter(@Nullable Object @Nullable [] args) {
		return new ArgumentPreparedStatementSetter(args);
	}

	/**
	 * 使用传入的参数和类型创建一个新的基于参数类型的PreparedStatementSetter。<p>B默认情况下，我们将创建一个{@link ArgumentTypePrepa
	 * redStatementSetter}。此方法允许子类覆盖创建。
	 * @param args 带参数的对象数组
	 * @param argTypes 关联参数的 SQLType 的 int 数组
	 * @return 使用新的PreparedStatementSetter
	 */
	protected PreparedStatementSetter newArgTypePreparedStatementSetter(@Nullable Object @Nullable [] args, int[] argTypes) {
		return new ArgumentTypePreparedStatementSetter(args, argTypes);
	}

	/**
	 * 在传播主 {@code SQLException} 执行给定语句之前处理警告。 <p> 调用常规 {@link #handleWarnings(Statement)} 但捕获
	 * {@link SQLWarningException}，以便将 {@link SQLWarning} 链接到主要异常中。
	 * @param stmt 当前的 JDBC 语句
	 * @param ex 语句执行失败后的主要异常
	 * @since 5.3.29
	 * @see #handleWarnings(Statement)
	 * @see SQLException#setNextException
	 */
	protected void handleWarnings(Statement stmt, SQLException ex) {
		try {
			handleWarnings(stmt);
		}
		catch (SQLWarningException nonIgnoredWarning) {
			ex.setNextException(nonIgnoredWarning.getSQLWarning());
		}
		catch (SQLException warningsEx) {
			logger.debug("Failed to retrieve warnings", warningsEx);
		}
		catch (Throwable warningsEx) {
			logger.debug("Failed to process warnings", warningsEx);
		}
	}

	/**
	 * 处理给定 JDBC 语句的警告（如果有）。 <p> 如果我们不忽略警告，则抛出 {@link SQLWarningException}，否则在调试级别记录警告。
	 * @param stmt 当前的 JDBC 语句
	 * @throws SQLException 如果警告检索失败
	 * @throws SQLWarningException 提出具体警告（当不忽略警告时）
	 * @see #setIgnoreWarnings
	 * @see #handleWarnings(SQLWarning)
	 */
	protected void handleWarnings(Statement stmt) throws SQLException, SQLWarningException {
		if (isIgnoreWarnings()) {
			if (logger.isDebugEnabled()) {
				SQLWarning warningToLog = stmt.getWarnings();
				while (warningToLog != null) {
					logger.debug("SQLWarning ignored: SQL state '" + warningToLog.getSQLState() + "', error code '" +
							warningToLog.getErrorCode() + "', message [" + warningToLog.getMessage() + "]");
					warningToLog = warningToLog.getNextWarning();
				}
			}
		}
		else {
			handleWarnings(stmt.getWarnings());
		}
	}

	/**
	 * 如果遇到实际警告，则抛出 {@link SQLWarningException}。
	 * @param warning 当前语句中的警告对象。可能是 {@code null}，在这种情况下该方法不执行任何操作。
	 * @throws SQLWarningException 如果实际发出警告
	 */
	protected void handleWarnings(@Nullable SQLWarning warning) throws SQLWarningException {
		if (warning != null) {
			throw new SQLWarningException("Warning not ignored", warning);
		}
	}

	/**
	 * 将给定的 {@link SQLException} 转换为通用 {@link DataAccessException}。
	 * @param task 描述正在尝试的任务的可读文本
	 * @param sql 导致问题的 SQL 查询或更新（可能是 {@code null}）
	 * @param ex 有问题的 {@code SQLException}
	 * @return DataAccessException 包装 {@code SQLException}（绝不是 {@code null}）
	 * @since 5.0
	 * @see #getExceptionTranslator()
	 */
	protected DataAccessException translateException(String task, @Nullable String sql, SQLException ex) {
		DataAccessException dae = getExceptionTranslator().translate(task, sql, ex);
		return (dae != null ? dae : new UncategorizedSQLException(task, sql, ex));
	}


	/**
	 * 从潜在的提供者对象中确定 SQL。
	 * @param obj 可能是 SqlProvider 的对象
	 * @return SQL 字符串，如果未知则为 {@code null}
	 * @see SqlProvider
	 */
	private static @Nullable String getSql(Object obj) {
		return (obj instanceof SqlProvider sqlProvider ? sqlProvider.getSql() : null);
	}

	/**
	 * 执行 result 方法的核心逻辑。
	 */
	private static <T> T result(@Nullable T result) {
		Assert.state(result != null, "No result");
		return result;
	}

	/**
	 * 更新：Count（方法 `updateCount`）。
	 */
	private static int updateCount(@Nullable Integer result) {
		Assert.state(result != null, "No update count");
		return result;
	}

	/**
	 * 执行 storeGeneratedKeys 方法的核心逻辑。
	 */
	private void storeGeneratedKeys(KeyHolder generatedKeyHolder, PreparedStatement ps, int rowsExpected)
			throws SQLException {

		List<Map<String, Object>> generatedKeys = generatedKeyHolder.getKeyList();
		ResultSet keys = ps.getGeneratedKeys();
		if (keys != null) {
			try {
				RowMapperResultSetExtractor<Map<String, @Nullable Object>> rse =
						new RowMapperResultSetExtractor<>(getColumnMapRowMapper(), rowsExpected);
				generatedKeys.addAll(result(rse.extractData(keys)));
			}
			finally {
				JdbcUtils.closeResultSet(keys);
			}
		}
	}

	/**
	 * 获取 Prepared Statement Callback（`PreparedStatementCallback`）。
	 */
	private PreparedStatementCallback<int[]> getPreparedStatementCallback(BatchPreparedStatementSetter pss,
			@Nullable KeyHolder generatedKeyHolder) {
		return ps -> {
			try {
				int batchSize = pss.getBatchSize();
				InterruptibleBatchPreparedStatementSetter ipss =
						(pss instanceof InterruptibleBatchPreparedStatementSetter ibpss ? ibpss : null);
				if (generatedKeyHolder != null) {
					generatedKeyHolder.getKeyList().clear();
				}
				if (JdbcUtils.supportsBatchUpdates(ps.getConnection())) {
					for (int i = 0; i < batchSize; i++) {
						pss.setValues(ps, i);
						if (ipss != null && ipss.isBatchExhausted(i)) {
							break;
						}
						ps.addBatch();
					}
					int[] results = ps.executeBatch();
					if (generatedKeyHolder != null) {
						storeGeneratedKeys(generatedKeyHolder, ps, batchSize);
					}
					return results;
				}
				else {
					List<Integer> rowsAffected = new ArrayList<>();
					for (int i = 0; i < batchSize; i++) {
						pss.setValues(ps, i);
						if (ipss != null && ipss.isBatchExhausted(i)) {
							break;
						}
						rowsAffected.add(ps.executeUpdate());
						if (generatedKeyHolder != null) {
							storeGeneratedKeys(generatedKeyHolder, ps, 1);
						}
					}
					int[] rowsAffectedArray = new int[rowsAffected.size()];
					for (int i = 0; i < rowsAffectedArray.length; i++) {
						rowsAffectedArray[i] = rowsAffected.get(i);
					}
					return rowsAffectedArray;
				}
			}
			finally {
				if (pss instanceof ParameterDisposer parameterDisposer) {
					parameterDisposer.cleanupParameters();
				}
			}
		};
	}


	/**
	 * 抑制 JDBC 连接上的关闭调用的调用处理程序。还准备返回的 Statement (Prepared/CallbackStatement) 对象。
	 * @see java.sql.Connection#close()
	 */
	private class CloseSuppressingInvocationHandler implements InvocationHandler {

		private final Connection target;

		public CloseSuppressingInvocationHandler(Connection target) {
			this.target = target;
		}

		@Override
		public @Nullable Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			// 对 ConnectionProxy 接口的调用即将到来...

			return switch (method.getName()) {
				// 仅当代理相同时才考虑相等。
				case "equals" -> (proxy == args[0]);
				// 使用连接代理的 hashCode。
				case "hashCode" -> System.identityHashCode(proxy);
				// 处理关闭方法：抑制，无效。
				case "close" -> null;
				case "isClosed" -> false;
				// Handle getTargetConnection方法：返回底层Connection。
				case "getTargetConnection" -> this.target;
				case "unwrap" ->
						(((Class<?>) args[0]).isInstance(proxy) ? proxy : this.target.unwrap((Class<?>) args[0]));
				case "isWrapperFor" ->
						(((Class<?>) args[0]).isInstance(proxy) || this.target.isWrapperFor((Class<?>) args[0]));
				default -> {
					try {
						// 调用目标连接上的方法。
						Object retVal = method.invoke(this.target, args);

						// 如果返回值是 JDBC 语句，则应用语句设置
						// （获取大小、最大行数、事务超时）。
						if (retVal instanceof Statement statement) {
							applyStatementSettings(statement);
						}

						yield retVal;
					}
					catch (InvocationTargetException ex) {
						throw ex.getTargetException();
					}
				}
			};
		}
	}


	/**
	 * PreparedStatementCreator 的简单适配器，允许使用纯 SQL 语句。
	 */
	private static class SimplePreparedStatementCreator implements PreparedStatementCreator, SqlProvider {

		private final String sql;

		public SimplePreparedStatementCreator(String sql) {
			Assert.notNull(sql, "SQL must not be null");
			this.sql = sql;
		}

		@Override
		public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
			return con.prepareStatement(this.sql);
		}

		@Override
		public String getSql() {
			return this.sql;
		}
	}


	/**
	 * CallableStatementCreator 的简单适配器，允许使用纯 SQL 语句。
	 */
	private static class SimpleCallableStatementCreator implements CallableStatementCreator, SqlProvider {

		private final String callString;

		public SimpleCallableStatementCreator(String callString) {
			Assert.notNull(callString, "Call string must not be null");
			this.callString = callString;
		}

		@Override
		public CallableStatement createCallableStatement(Connection con) throws SQLException {
			return con.prepareCall(this.callString);
		}

		@Override
		public String getSql() {
			return this.callString;
		}
	}


	/**
	 * 用于启用在 ResultSetExtractor 内使用 RowCallbackHandler 的适配器。 <p>U 使用常规结果集，因此我们在使用它时必须小心：我们不使用它进
	 * 行导航，因为这可能会导致不可预测的后果。
	 */
	private static class RowCallbackHandlerResultSetExtractor implements ResultSetExtractor<@Nullable Object> {

		private final RowCallbackHandler rch;

		private final int maxRows;

		public RowCallbackHandlerResultSetExtractor(RowCallbackHandler rch, int maxRows) {
			this.rch = rch;
			this.maxRows = maxRows;
		}

		@Override
		public @Nullable Object extractData(ResultSet rs) throws SQLException {
			int processed = 0;
			while (rs.next() && (this.maxRows == -1 || (processed++) < this.maxRows)) {
				this.rch.processRow(rs);
			}
			return null;
		}
	}


	/**
	 * 用于将 ResultSet 适配为 Stream 的 queryForStream 的 Spliterator。
	 * @since 5.3
	 */
	private static class ResultSetSpliterator<T> implements Spliterator<T> {

		private final ResultSet rs;

		private final RowMapper<T> rowMapper;

		private final int maxRows;

		private int rowNum = 0;

		public ResultSetSpliterator(ResultSet rs, RowMapper<T> rowMapper, int maxRows) {
			this.rs = rs;
			this.rowMapper = rowMapper;
			this.maxRows = maxRows;
		}

		@Override
		public boolean tryAdvance(Consumer<? super T> action) {
			try {
				if (this.rs.next() && (this.maxRows == -1 || this.rowNum < this.maxRows)) {
					action.accept(this.rowMapper.mapRow(this.rs, this.rowNum++));
					return true;
				}
				return false;
			}
			catch (SQLException ex) {
				throw new InvalidResultSetAccessException(ex);
			}
		}

		@Override
		public @Nullable Spliterator<T> trySplit() {
			return null;
		}

		@Override
		public long estimateSize() {
			return Long.MAX_VALUE;
		}

		@Override
		public int characteristics() {
			return Spliterator.ORDERED;
		}

		public Stream<T> stream() {
			return StreamSupport.stream(this, false);
		}
	}

}
