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

package org.springframework.jdbc.core.simple;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlTypeValue;
import org.springframework.jdbc.core.StatementCreatorUtils;
import org.springframework.jdbc.core.metadata.TableMetaDataContext;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.util.Assert;

/**
 * 抽象类，提供基于配置选项和数据库元数据的轻松（批量）插入的基本功能。
 * <p>该类提供{@link SimpleJdbcInsert}的处理安排。
 * @author Thomas Risberg
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 2.5
 */
public abstract class AbstractJdbcInsert {

	/**
	 */
	protected final Log logger = LogFactory.getLog(getClass());

	/**
	 */
	private final JdbcTemplate jdbcTemplate;

	/**
	 */
	private final TableMetaDataContext tableMetaDataContext = new TableMetaDataContext();

	/**
	 */
	private final List<String> declaredColumns = new ArrayList<>();

	/**
	 */
	private String[] generatedKeyNames = new String[0];

	/**
	 * 这个操作编译了吗？编译意味着至少检查是否已提供 DataSource 或 JdbcTemplate。
	 */
	private volatile boolean compiled;

	/**
	 */
	private String insertString = "";

	/**
	 */
	private int[] insertTypes = new int[0];


	/**
	 * 使用 {@link DataSource} 初始化时要使用的构造函数。
	 * @param dataSource 要使用的 {@code DataSource}
	 */
	protected AbstractJdbcInsert(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	/**
	 * 使用 {@link JdbcTemplate} 初始化时要使用的构造函数。
	 * @param jdbcTemplate 要使用的 {@code JdbcTemplate}
	 */
	protected AbstractJdbcInsert(JdbcTemplate jdbcTemplate) {
		Assert.notNull(jdbcTemplate, "JdbcTemplate must not be null");
		this.jdbcTemplate = jdbcTemplate;
	}


	//-------------------------------------------------------------------------
	// 处理配置属性的方法
	//-------------------------------------------------------------------------

	/**
	 * 获取配置的{@link JdbcTemplate}。
	 */
	public JdbcTemplate getJdbcTemplate() {
		return this.jdbcTemplate;
	}

	/**
	 * 设置此插入的表名称。
	 */
	public void setTableName(@Nullable String tableName) {
		checkIfConfigurationModificationIsAllowed();
		this.tableMetaDataContext.setTableName(tableName);
	}

	/**
	 * 获取此插入的表的名称。
	 */
	public @Nullable String getTableName() {
		return this.tableMetaDataContext.getTableName();
	}

	/**
	 * 设置此插入的架构名称。
	 */
	public void setSchemaName(@Nullable String schemaName) {
		checkIfConfigurationModificationIsAllowed();
		this.tableMetaDataContext.setSchemaName(schemaName);
	}

	/**
	 * 获取此插入的架构名称。
	 */
	public @Nullable String getSchemaName() {
		return this.tableMetaDataContext.getSchemaName();
	}

	/**
	 * 设置此插入的目录名称。
	 */
	public void setCatalogName(@Nullable String catalogName) {
		checkIfConfigurationModificationIsAllowed();
		this.tableMetaDataContext.setCatalogName(catalogName);
	}

	/**
	 * 获取此插入的目录名称。
	 */
	public @Nullable String getCatalogName() {
		return this.tableMetaDataContext.getCatalogName();
	}

	/**
	 * 设置要使用的列的名称。
	 */
	public void setColumnNames(List<String> columnNames) {
		checkIfConfigurationModificationIsAllowed();
		this.declaredColumns.clear();
		this.declaredColumns.addAll(columnNames);
	}

	/**
	 * 获取所使用的列的名称。
	 */
	public List<String> getColumnNames() {
		return Collections.unmodifiableList(this.declaredColumns);
	}

	/**
	 * 指定单个生成的键列的名称。
	 */
	public void setGeneratedKeyName(String generatedKeyName) {
		checkIfConfigurationModificationIsAllowed();
		this.generatedKeyNames = new String[] {generatedKeyName};
	}

	/**
	 * 设置任何生成的密钥的名称。
	 */
	public void setGeneratedKeyNames(String... generatedKeyNames) {
		checkIfConfigurationModificationIsAllowed();
		this.generatedKeyNames = generatedKeyNames;
	}

	/**
	 * 获取任何生成的密钥的名称。
	 */
	public String[] getGeneratedKeyNames() {
		return this.generatedKeyNames;
	}

	/**
	 * 指定是否应使用调用的参数元数据。 <p>默认为{@code true}。
	 */
	public void setAccessTableColumnMetaData(boolean accessTableColumnMetaData) {
		this.tableMetaDataContext.setAccessTableColumnMetaData(accessTableColumnMetaData);
	}

	/**
	 * 指定是否应更改包含同义词的默认值。 <p>默认为{@code false}。
	 */
	public void setOverrideIncludeSynonymsDefault(boolean override) {
		this.tableMetaDataContext.setOverrideIncludeSynonymsDefault(override);
	}

	/**
	 * 获取要使用的插入字符串。
	 */
	public String getInsertString() {
		return this.insertString;
	}

	/**
	 * 获取用于插入的 {@link java.sql.Types} 数组。
	 */
	public int[] getInsertTypes() {
		return this.insertTypes;
	}

	/**
	 * 指定是否应将 SQL 标识符加引号。 <p>默认为 {@code false}。如果设置为 {@code true}，则底层数据库的标识符引用字符串将用于在生成的 SQL 语句
	 * 中引用 SQL 标识符。
	 * @param quoteIdentifiers 标识符是否应该加引号
	 * @since 6.1
	 * @see java.sql.DatabaseMetaData#getIdentifierQuoteString()
	 */
	public void setQuoteIdentifiers(boolean quoteIdentifiers) {
		this.tableMetaDataContext.setQuoteIdentifiers(quoteIdentifiers);
	}

	/**
	 * 获取 {@code quoteIdentifiers} 标志。
	 * @since 6.1
	 * @see #setQuoteIdentifiers(boolean)
	 */
	public boolean isQuoteIdentifiers() {
		return this.tableMetaDataContext.isQuoteIdentifiers();
	}


	//-------------------------------------------------------------------------
	// 处理编译问题的方法
	//-------------------------------------------------------------------------

	/**
	 * 使用提供的参数和元数据以及其他设置编译此 JdbcInsert。这最终确定了该对象的配置，并且随后的编译尝试将被忽略。这将在第一次执行未编译插入时隐式调用。
	 * @throws InvalidDataAccessApiUsageException 如果对象尚未正确初始化，例如，如果未提供 DataSource
	 */
	public final synchronized void compile() throws InvalidDataAccessApiUsageException {
		if (!isCompiled()) {
			if (getTableName() == null) {
				throw new InvalidDataAccessApiUsageException("Table name is required");
			}
			if (isQuoteIdentifiers() && this.declaredColumns.isEmpty()) {
				throw new InvalidDataAccessApiUsageException(
						"Explicit column names must be provided when using quoted identifiers");
			}
			try {
				this.jdbcTemplate.afterPropertiesSet();
			}
			catch (IllegalArgumentException ex) {
				throw new InvalidDataAccessApiUsageException(ex.getMessage());
			}
			compileInternal();
			this.compiled = true;
			if (logger.isDebugEnabled()) {
				logger.debug("JdbcInsert for table [" + getTableName() + "] compiled");
			}
		}
	}

	/**
	 * 执行实际编译的委托方法。 <p>子类可以重写此模板方法来执行自己的编译。该基类编译完成后调用。
	 */
	protected void compileInternal() {
		DataSource dataSource = getJdbcTemplate().getDataSource();
		Assert.state(dataSource != null, "No DataSource set");
		this.tableMetaDataContext.processMetaData(dataSource, getColumnNames(), getGeneratedKeyNames());
		this.insertString = this.tableMetaDataContext.createInsertString(getGeneratedKeyNames());
		this.insertTypes = this.tableMetaDataContext.createInsertTypes();
		if (logger.isDebugEnabled()) {
			logger.debug("Compiled insert object: insert string is [" + this.insertString + "]");
		}
		onCompileInternal();
	}

	/**
	 * 子类可以重写以对编译做出反应的钩子方法。 <p>这个实现是空的。
	 */
	protected void onCompileInternal() {
	}

	/**
	 * 这个操作是“编译”的吗？
	 * @return 该操作已编译并可以使用
	 */
	public boolean isCompiled() {
		return this.compiled;
	}

	/**
	 * 检查该操作是否已经编译；如果尚未编译，则延迟编译它。 <p> 由所有 {@code doExecute*(...)} 方法自动调用。
	 */
	protected void checkCompiled() {
		if (!isCompiled()) {
			logger.debug("JdbcInsert not compiled before execution - invoking compile");
			compile();
		}
	}

	/**
	 * 检查此时是否允许我们进行任何配置更改的方法。 <p>如果该类已编译，则不允许进一步更改配置。
	 */
	protected void checkIfConfigurationModificationIsAllowed() {
		if (isCompiled()) {
			throw new InvalidDataAccessApiUsageException(
					"Configuration cannot be altered once the class has been compiled or used");
		}
	}


	//-------------------------------------------------------------------------
	// 处理执行的方法
	//-------------------------------------------------------------------------

	/**
	 * 使用传入的参数映射执行插入的委托方法。
	 * @param args 带有要在插入中使用的参数名称和值的映射
	 * @return 受影响的行数
	 */
	protected int doExecute(Map<String, ?> args) {
		checkCompiled();
		List<Object> values = matchInParameterValuesWithInsertColumns(args);
		return executeInsertInternal(values);
	}

	/**
	 * 使用传入的 {@link SqlParameterSource} 执行插入的委托方法。
	 * @param parameterSource 插入中使用的参数名称和值
	 * @return 受影响的行数
	 */
	protected int doExecute(SqlParameterSource parameterSource) {
		checkCompiled();
		List<Object> values = matchInParameterValuesWithInsertColumns(parameterSource);
		return executeInsertInternal(values);
	}

	/**
	 * 执行插入的委托方法。
	 */
	private int executeInsertInternal(List<?> values) {
		if (logger.isDebugEnabled()) {
			logger.debug("The following parameters are used for insert " + getInsertString() + " with: " + values);
		}
		return getJdbcTemplate().update(getInsertString(), values.toArray(), getInsertTypes());
	}

	/**
	 * 使用传入的参数映射执行插入并返回生成的键的方法。
	 * @param args 带有要在插入中使用的参数名称和值的映射
	 * @return 插入生成的密钥
	 */
	protected Number doExecuteAndReturnKey(Map<String, ?> args) {
		checkCompiled();
		List<Object> values = matchInParameterValuesWithInsertColumns(args);
		return executeInsertAndReturnKeyInternal(values);
	}

	/**
	 * 使用传入的 {@link SqlParameterSource} 执行插入并返回生成的密钥的方法。
	 * @param parameterSource 插入中使用的参数名称和值
	 * @return 插入生成的密钥
	 */
	protected Number doExecuteAndReturnKey(SqlParameterSource parameterSource) {
		checkCompiled();
		List<Object> values = matchInParameterValuesWithInsertColumns(parameterSource);
		return executeInsertAndReturnKeyInternal(values);
	}

	/**
	 * 使用传入的参数映射执行插入并返回所有生成的键的方法。
	 * @param args 带有要在插入中使用的参数名称和值的映射
	 * @return KeyHolder 包含插入生成的密钥
	 */
	protected KeyHolder doExecuteAndReturnKeyHolder(Map<String, ?> args) {
		checkCompiled();
		List<Object> values = matchInParameterValuesWithInsertColumns(args);
		return executeInsertAndReturnKeyHolderInternal(values);
	}

	/**
	 * 使用传入的 {@link SqlParameterSource} 执行插入并返回所有生成的键的方法。
	 * @param parameterSource 插入中使用的参数名称和值
	 * @return KeyHolder 包含插入生成的密钥
	 */
	protected KeyHolder doExecuteAndReturnKeyHolder(SqlParameterSource parameterSource) {
		checkCompiled();
		List<Object> values = matchInParameterValuesWithInsertColumns(parameterSource);
		return executeInsertAndReturnKeyHolderInternal(values);
	}

	/**
	 * 委托方法执行插入，生成单个键。
	 */
	private Number executeInsertAndReturnKeyInternal(List<?> values) {
		KeyHolder kh = executeInsertAndReturnKeyHolderInternal(values);
		if (kh.getKey() != null) {
			return kh.getKey();
		}
		else {
			throw new DataIntegrityViolationException(
					"Unable to retrieve the generated key for the insert: " + getInsertString());
		}
	}

	/**
	 * 委托方法执行插入，生成任意数量的键。
	 */
	private KeyHolder executeInsertAndReturnKeyHolderInternal(List<?> values) {
		if (logger.isDebugEnabled()) {
			logger.debug("The following parameters are used for call " + getInsertString() + " with: " + values);
		}
		KeyHolder keyHolder = new GeneratedKeyHolder();

		if (this.tableMetaDataContext.isGetGeneratedKeysSupported()) {
			getJdbcTemplate().update(
					con -> {
						PreparedStatement ps = prepareStatementForGeneratedKeys(con);
						setParameterValues(ps, values, getInsertTypes());
						return ps;
					},
					keyHolder);
		}

		else {
			if (!this.tableMetaDataContext.isGetGeneratedKeysSimulated()) {
				throw new InvalidDataAccessResourceUsageException(
						"The getGeneratedKeys feature is not supported by this database");
			}
			if (getGeneratedKeyNames().length < 1) {
				throw new InvalidDataAccessApiUsageException("Generated Key Name(s) not specified. " +
						"Using the generated keys features requires specifying the name(s) of the generated column(s)");
			}
			if (getGeneratedKeyNames().length > 1) {
				throw new InvalidDataAccessApiUsageException(
						"Current database only supports retrieving the key for a single column. There are " +
						getGeneratedKeyNames().length + " columns specified: " + Arrays.toString(getGeneratedKeyNames()));
			}

			Assert.state(getTableName() != null, "No table name set");
			String keyQuery = this.tableMetaDataContext.getSimpleQueryForGetGeneratedKey(
					getTableName(), getGeneratedKeyNames()[0]);
			Assert.state(keyQuery != null, "Query for simulating get generated keys must not be null");

			// 这是一种能够从不支持的数据库中获取生成密钥的黑客攻击
			// 获取生成的密钥功能。 HSQL 是其中之一，PostgreSQL 是另一个。 Postgres 使用 RETURNING
			// 子句，而 HSQL 使用必须使用同一连接执行的第二个查询。

			if (keyQuery.toUpperCase(Locale.ROOT).startsWith("RETURNING")) {
				Long key = getJdbcTemplate().queryForObject(
						getInsertString() + " " + keyQuery, Long.class, values.toArray());
				Map<String, Object> keys = new HashMap<>(2);
				keys.put(getGeneratedKeyNames()[0], key);
				keyHolder.getKeyList().add(keys);
			}
			else {
				getJdbcTemplate().execute((ConnectionCallback<@Nullable Object>) con -> {
					// 执行插入操作
					PreparedStatement ps = null;
					try {
						ps = con.prepareStatement(getInsertString());
						setParameterValues(ps, values, getInsertTypes());
						ps.executeUpdate();
					}
					finally {
						JdbcUtils.closeStatement(ps);
					}
					// 拿到钥匙
					Statement keyStmt = null;
					ResultSet rs = null;
					try {
						keyStmt = con.createStatement();
						rs = keyStmt.executeQuery(keyQuery);
						if (rs.next()) {
							long key = rs.getLong(1);
							Map<String, Object> keys = new HashMap<>(2);
							keys.put(getGeneratedKeyNames()[0], key);
							keyHolder.getKeyList().add(keys);
						}
					}
					finally {
						JdbcUtils.closeResultSet(rs);
						JdbcUtils.closeStatement(keyStmt);
					}
					return null;
				});
			}
		}

		return keyHolder;
	}

	/**
	 * 创建一个PreparedStatement，用于使用生成的键进行插入操作。
	 * @param con 要使用的连接
	 * @return 准备好的声明
	 */
	private PreparedStatement prepareStatementForGeneratedKeys(Connection con) throws SQLException {
		if (getGeneratedKeyNames().length < 1) {
			throw new InvalidDataAccessApiUsageException("Generated Key Name(s) not specified. " +
					"Using the generated keys features requires specifying the name(s) of the generated column(s).");
		}
		PreparedStatement ps;
		if (this.tableMetaDataContext.isGeneratedKeysColumnNameArraySupported()) {
			if (logger.isDebugEnabled()) {
				logger.debug("Using generated keys support with array of column names.");
			}
			ps = con.prepareStatement(getInsertString(), getGeneratedKeyNames());
		}
		else {
			if (logger.isDebugEnabled()) {
				logger.debug("Using generated keys support with Statement.RETURN_GENERATED_KEYS.");
			}
			ps = con.prepareStatement(getInsertString(), Statement.RETURN_GENERATED_KEYS);
		}
		return ps;
	}

	/**
	 * 使用传入的参数映射执行批量插入的委托方法。
	 * @param batch 带有要在批量插入中使用的参数名称和值的映射
	 * @return 受影响的行数数组
	 */
	@SuppressWarnings("unchecked")
	protected int[] doExecuteBatch(Map<String, ?>... batch) {
		checkCompiled();
		List<List<Object>> batchValues = new ArrayList<>(batch.length);
		for (Map<String, ?> args : batch) {
			batchValues.add(matchInParameterValuesWithInsertColumns(args));
		}
		return executeBatchInternal(batchValues);
	}

	/**
	 * 使用传入的 {@link SqlParameterSource SqlParameterSources} 执行批量插入的委托方法。
	 * @param batch 带有要在批量插入中使用的名称和值的参数源
	 * @return 受影响的行数数组
	 */
	protected int[] doExecuteBatch(SqlParameterSource... batch) {
		checkCompiled();
		List<List<Object>> batchValues = new ArrayList<>(batch.length);
		for (SqlParameterSource parameterSource : batch) {
			batchValues.add(matchInParameterValuesWithInsertColumns(parameterSource));
		}
		return executeBatchInternal(batchValues);
	}

	/**
	 * 执行批量插入的委托方法。
	 */
	private int[] executeBatchInternal(final List<List<Object>> batchValues) {
		if (logger.isDebugEnabled()) {
			logger.debug("Executing statement " + getInsertString() + " with batch of size: " + batchValues.size());
		}
		return getJdbcTemplate().batchUpdate(getInsertString(),
				new BatchPreparedStatementSetter() {
					@Override
					public void setValues(PreparedStatement ps, int i) throws SQLException {
						setParameterValues(ps, batchValues.get(i), getInsertTypes());
					}
					@Override
					public int getBatchSize() {
						return batchValues.size();
					}
				});
	}

	/**
	 * 设置参数值的内部实现。
	 * @param preparedStatement 准备好的声明
	 * @param values 要设置的值
	 */
	private void setParameterValues(PreparedStatement preparedStatement, List<?> values, int @Nullable ... columnTypes)
			throws SQLException {

		int colIndex = 0;
		for (Object value : values) {
			colIndex++;
			if (columnTypes == null || colIndex > columnTypes.length) {
				StatementCreatorUtils.setParameterValue(preparedStatement, colIndex, SqlTypeValue.TYPE_UNKNOWN, value);
			}
			else {
				StatementCreatorUtils.setParameterValue(preparedStatement, colIndex, columnTypes[colIndex - 1], value);
			}
		}
	}

	/**
	 * 将提供的参数值与注册参数和通过元数据处理定义的参数进行匹配。
	 * @param parameterSource 以 {@link SqlParameterSource} 形式提供的参数值
	 * @return 值列表
	 */
	protected List<Object> matchInParameterValuesWithInsertColumns(SqlParameterSource parameterSource) {
		return this.tableMetaDataContext.matchInParameterValuesWithInsertColumns(parameterSource);
	}

	/**
	 * 将提供的参数值与注册参数和通过元数据处理定义的参数进行匹配。
	 * @param args 以 Map 形式提供的参数值
	 * @return 值列表
	 */
	protected List<Object> matchInParameterValuesWithInsertColumns(Map<String, ?> args) {
		return this.tableMetaDataContext.matchInParameterValuesWithInsertColumns(args);
	}

}
