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

表操作元数据上下文：持有表名、catalog/schema、列元数据与 TableMetaDataProvider，为 SimpleJdbcInsert 生成参数化 INSERT 及主键回填逻辑。
===== [OCA 中文解析结束] ===== */
package org.springframework.jdbc.core.metadata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;

import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.jdbc.core.SqlTypeValue;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/* ===== [OCA 中文解析] =====
class TableMetaDataContext — 意图说明

INSERT/UPDATE 等表操作的运行时上下文：解析参与列、引号规则与 generated keys，与 GenericTableMetaDataProvider 协作完成元数据驱动的 SQL 组装。

（本注释由 open-code-analyzer 生成，置于原有文档注释之前）
===== [OCA 中文解析结束] ===== */
/**
 * 管理数据库表操作（如 INSERT）的配置与执行上下文元数据。
 * @author Thomas Risberg
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 2.5
 */
public class TableMetaDataContext {

	// 记录器可用于子类
	/**
	 * 日志记录器，供子类或本类记录诊断信息。
	 */
	protected final Log logger = LogFactory.getLog(getClass());

	// 此上下文的表名称
	/** 名称相关状态（`tableName`）。 */
	private @Nullable String tableName;

	// 此上下文的目录名称
	/** 名称相关状态（`catalogName`）。 */
	private @Nullable String catalogName;

	// 此上下文的架构名称
	/** 名称相关状态（`schemaName`）。 */
	private @Nullable String schemaName;

	// 我们是否应该访问插入参数元数据信息
	/** 布尔配置标志。 */
	private boolean accessTableColumnMetaData = true;

	// 我们是否应该覆盖默认值以包含元数据查找的同义词
	/** 布尔配置标志（默认 false）。 */
	private boolean overrideIncludeSynonymsDefault = false;

	// 我们是否引用标识符？
	/** 布尔配置标志（默认 false）。 */
	private boolean quoteIdentifiers = false;

	// 表元数据提供者
	/** `metaDataProvider`：该类的成员状态。 */
	private @Nullable TableMetaDataProvider metaDataProvider;

	// 要在此上下文中使用的列对象的列表
	private List<String> tableColumns = new ArrayList<>();

	// 我们是否使用生成的键列
	/** 布尔配置标志（默认 false）。 */
	private boolean generatedKeyColumnsUsed = false;


	/**
	 * 设置此上下文的表名称。
	 */
	public void setTableName(@Nullable String tableName) {
		this.tableName = tableName;
	}

	/**
	 * 获取此上下文的表的名称。
	 */
	public @Nullable String getTableName() {
		return this.tableName;
	}

	/**
	 * 设置此上下文的目录名称。
	 */
	public void setCatalogName(@Nullable String catalogName) {
		this.catalogName = catalogName;
	}

	/**
	 * 获取此上下文的目录名称。
	 */
	public @Nullable String getCatalogName() {
		return this.catalogName;
	}

	/**
	 * 设置此上下文的架构名称。
	 */
	public void setSchemaName(@Nullable String schemaName) {
		this.schemaName = schemaName;
	}

	/**
	 * 获取此上下文的架构名称。
	 */
	public @Nullable String getSchemaName() {
		return this.schemaName;
	}

	/**
	 * 指定我们是否应该访问表列元数据。
	 */
	public void setAccessTableColumnMetaData(boolean accessTableColumnMetaData) {
		this.accessTableColumnMetaData = accessTableColumnMetaData;
	}

	/**
	 * 我们正在访问表元数据吗？
	 */
	public boolean isAccessTableColumnMetaData() {
		return this.accessTableColumnMetaData;
	}

	/**
	 * 指定我们是否应该覆盖访问同义词的默认值。
	 */
	public void setOverrideIncludeSynonymsDefault(boolean override) {
		this.overrideIncludeSynonymsDefault = override;
	}

	/**
	 * 我们是否覆盖默认的包含同义词？
	 */
	public boolean isOverrideIncludeSynonymsDefault() {
		return this.overrideIncludeSynonymsDefault;
	}

	/**
	 * 指定我们是否引用 SQL 标识符。 <p>默认为 {@code false}。如果设置为 {@code true}，则底层数据库的标识符引用字符串将用于在生成的 SQL 语句中
	 * 引用 SQL 标识符。
	 * @param quoteIdentifiers 标识符是否应该加引号
	 * @since 6.1
	 * @see java.sql.DatabaseMetaData#getIdentifierQuoteString()
	 */
	public void setQuoteIdentifiers(boolean quoteIdentifiers) {
		this.quoteIdentifiers = quoteIdentifiers;
	}

	/**
	 * 我们是否引用标识符？
	 * @since 6.1
	 * @see #setQuoteIdentifiers(boolean)
	 */
	public boolean isQuoteIdentifiers() {
		return this.quoteIdentifiers;
	}

	/**
	 * 获取表列名称的列表。
	 */
	public List<String> getTableColumns() {
		return this.tableColumns;
	}


	/**
	 * 使用提供的配置选项处理当前元数据。
	 * @param dataSource 正在使用的数据源
	 * @param declaredColumns 声明的任何列
	 * @param generatedKeyNames 生成的密钥的名称
	 */
	public void processMetaData(DataSource dataSource, List<String> declaredColumns, String[] generatedKeyNames) {
		this.metaDataProvider = TableMetaDataProviderFactory.createMetaDataProvider(dataSource, this);
		this.tableColumns = reconcileColumnsToUse(declaredColumns, generatedKeyNames);
	}

	/**
	 * 执行 obtainMetaDataProvider 方法的核心逻辑。
	 */
	private TableMetaDataProvider obtainMetaDataProvider() {
		Assert.state(this.metaDataProvider != null, "No TableMetaDataProvider - call processMetaData first");
		return this.metaDataProvider;
	}

	/**
	 * 将从元数据创建的列与声明的列进行比较，并返回一个协调列表。
	 * @param declaredColumns 声明的列名
	 * @param generatedKeyNames 生成的键列的名称
	 */
	protected List<String> reconcileColumnsToUse(List<String> declaredColumns, String[] generatedKeyNames) {
		if (generatedKeyNames.length > 0) {
			this.generatedKeyColumnsUsed = true;
		}
		if (!declaredColumns.isEmpty()) {
			return new ArrayList<>(declaredColumns);
		}
		Set<String> keys = CollectionUtils.newLinkedHashSet(generatedKeyNames.length);
		for (String key : generatedKeyNames) {
			keys.add(key.toUpperCase(Locale.ROOT));
		}
		List<String> columns = new ArrayList<>();
		for (TableParameterMetaData meta : obtainMetaDataProvider().getTableParameterMetaData()) {
			if (!keys.contains(meta.getParameterName().toUpperCase(Locale.ROOT))) {
				columns.add(meta.getParameterName());
			}
		}
		return columns;
	}

	/**
	 * 将提供的列名称和值与所使用的列列表进行匹配。
	 * @param parameterSource 参数名称和值
	 */
	public List<Object> matchInParameterValuesWithInsertColumns(SqlParameterSource parameterSource) {
		List<Object> values = new ArrayList<>();
		// 对于参数源查找，我们需要提供不区分大小写的查找支持，因为
		// 数据库元数据不一定提供区分大小写的列名
		Map<String, String> caseInsensitiveParameterNames =
				SqlParameterSourceUtils.extractCaseInsensitiveParameterNames(parameterSource);
		for (String column : this.tableColumns) {
			if (parameterSource.hasValue(column)) {
				values.add(SqlParameterSourceUtils.getTypedValue(parameterSource, column));
			}
			else {
				String lowerCaseName = column.toLowerCase(Locale.ROOT);
				if (parameterSource.hasValue(lowerCaseName)) {
					values.add(SqlParameterSourceUtils.getTypedValue(parameterSource, lowerCaseName));
				}
				else {
					String propertyName = JdbcUtils.convertUnderscoreNameToPropertyName(column);
					if (parameterSource.hasValue(propertyName)) {
						values.add(SqlParameterSourceUtils.getTypedValue(parameterSource, propertyName));
					}
					else {
						if (caseInsensitiveParameterNames.containsKey(lowerCaseName)) {
							values.add(SqlParameterSourceUtils.getTypedValue(
									parameterSource, caseInsensitiveParameterNames.get(lowerCaseName)));
						}
						else {
							values.add(null);
						}
					}
				}
			}
		}
		return values;
	}

	/**
	 * 将提供的列名称和值与所使用的列列表进行匹配。
	 * @param inParameters 参数名称和值
	 */
	public List<Object> matchInParameterValuesWithInsertColumns(Map<String, ?> inParameters) {
		List<Object> values = new ArrayList<>(inParameters.size());
		for (String column : this.tableColumns) {
			Object value = inParameters.get(column);
			if (value == null) {
				value = inParameters.get(column.toLowerCase(Locale.ROOT));
				if (value == null) {
					for (Map.Entry<String, ?> entry : inParameters.entrySet()) {
						if (column.equalsIgnoreCase(entry.getKey())) {
							value = entry.getValue();
							break;
						}
					}
				}
			}
			values.add(value);
		}
		return values;
	}

	/**
	 * 根据配置和元数据信息构建插入字符串。
	 * @return 插入要使用的字符串
	 */
	public String createInsertString(String... generatedKeyNames) {
		Set<String> keys = CollectionUtils.newLinkedHashSet(generatedKeyNames.length);
		for (String key : generatedKeyNames) {
			keys.add(key.toUpperCase(Locale.ROOT));
		}

		String identifierQuoteString = (isQuoteIdentifiers() ?
				obtainMetaDataProvider().getIdentifierQuoteString() : null);
		QuoteHandler quoteHandler = new QuoteHandler(identifierQuoteString);

		StringBuilder insertStatement = new StringBuilder();
		insertStatement.append("INSERT INTO ");

		String catalogName = getCatalogName();
		if (catalogName != null) {
			quoteHandler.appendTo(insertStatement, catalogName);
			insertStatement.append('.');
		}

		String schemaName = getSchemaName();
		if (schemaName != null) {
			quoteHandler.appendTo(insertStatement, schemaName);
			insertStatement.append('.');
		}

		String tableName = getTableName();
		quoteHandler.appendTo(insertStatement, tableName);

		insertStatement.append(" (");
		int columnCount = 0;
		for (String columnName : getTableColumns()) {
			if (!keys.contains(columnName.toUpperCase(Locale.ROOT))) {
				columnCount++;
				if (columnCount > 1) {
					insertStatement.append(", ");
				}
				quoteHandler.appendTo(insertStatement, columnName);
			}
		}
		insertStatement.append(") VALUES(");
		if (columnCount < 1) {
			if (this.generatedKeyColumnsUsed) {
				if (logger.isDebugEnabled()) {
					logger.debug("Unable to locate non-key columns for table '" +
							tableName + "' so an empty insert statement is generated");
				}
			}
			else {
				String message = "Unable to locate columns for table '" + tableName +
						"' so an insert statement can't be generated.";
				if (isAccessTableColumnMetaData()) {
					message += " Consider specifying explicit column names -- for example, via SimpleJdbcInsert#usingColumns().";
				}
				throw new InvalidDataAccessApiUsageException(message);
			}
		}
		String params = String.join(", ", Collections.nCopies(columnCount, "?"));
		insertStatement.append(params);
		insertStatement.append(')');
		return insertStatement.toString();
	}

	/**
	 * 根据配置和元数据信息构建 {@link java.sql.Types} 数组。
	 * @return 要使用的类型数组
	 */
	public int[] createInsertTypes() {
		int[] types = new int[getTableColumns().size()];
		List<TableParameterMetaData> parameters = obtainMetaDataProvider().getTableParameterMetaData();
		Map<String, TableParameterMetaData> parameterMap = CollectionUtils.newLinkedHashMap(parameters.size());
		for (TableParameterMetaData tpmd : parameters) {
			parameterMap.put(tpmd.getParameterName().toUpperCase(Locale.ROOT), tpmd);
		}
		int typeIndx = 0;
		for (String column : getTableColumns()) {
			if (column == null) {
				types[typeIndx] = SqlTypeValue.TYPE_UNKNOWN;
			}
			else {
				TableParameterMetaData tpmd = parameterMap.get(column.toUpperCase(Locale.ROOT));
				if (tpmd != null) {
					types[typeIndx] = tpmd.getSqlType();
				}
				else {
					types[typeIndx] = SqlTypeValue.TYPE_UNKNOWN;
				}
			}
			typeIndx++;
		}
		return types;
	}


	/**
	 * 该数据库是否支持 JDBC 功能来检索生成的密钥？
	 * @see java.sql.DatabaseMetaData#supportsGetGeneratedKeys()
	 */
	public boolean isGetGeneratedKeysSupported() {
		return obtainMetaDataProvider().isGetGeneratedKeysSupported();
	}

	/**
	 * 当不支持检索生成密钥的 JDBC 功能时，此数据库是否支持简单查询来检索生成的密钥？
	 * @see #isGetGeneratedKeysSupported()
	 * @see #getSimpleQueryForGetGeneratedKey(String, String)
	 */
	public boolean isGetGeneratedKeysSimulated() {
		return obtainMetaDataProvider().isGetGeneratedKeysSimulated();
	}

	/**
	 * 当不支持检索生成密钥的 JDBC 功能时，获取简单查询来检索生成的密钥。
	 * @see #isGetGeneratedKeysSimulated()
	 */
	public @Nullable String getSimpleQueryForGetGeneratedKey(String tableName, String keyColumnName) {
		return obtainMetaDataProvider().getSimpleQueryForGetGeneratedKey(tableName, keyColumnName);
	}

	/**
	 * 该数据库是否支持列名字符串数组来检索生成的键？
	 * @see java.sql.Connection#createStruct(String, Object[])
	 */
	public boolean isGeneratedKeysColumnNameArraySupported() {
		return obtainMetaDataProvider().isGeneratedKeysColumnNameArraySupported();
	}


	private static final class QuoteHandler {

		private final @Nullable String identifierQuoteString;

		private final boolean quoting;

		QuoteHandler(@Nullable String identifierQuoteString) {
			this.identifierQuoteString = identifierQuoteString;
			this.quoting = StringUtils.hasText(identifierQuoteString);
		}

		void appendTo(StringBuilder stringBuilder, @Nullable String item) {
			if (this.quoting) {
				stringBuilder.append(this.identifierQuoteString)
						.append(item).append(this.identifierQuoteString);
			}
			else {
				stringBuilder.append(item);
			}
		}
	}

}
