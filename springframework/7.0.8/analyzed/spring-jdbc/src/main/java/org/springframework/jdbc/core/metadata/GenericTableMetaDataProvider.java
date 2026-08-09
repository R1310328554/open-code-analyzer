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

package org.springframework.jdbc.core.metadata;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;

import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.support.JdbcUtils;

/**
 * {@link TableMetaDataProvider} 接口的通用实现，应该为所有支持的数据库提供足够的功能。
 * @author Thomas Risberg
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 2.5
 */
public class GenericTableMetaDataProvider implements TableMetaDataProvider {

	/**
	 */
	protected static final Log logger = LogFactory.getLog(TableMetaDataProvider.class);

	/**
	 */
	private final @Nullable String userName;

	/**
	 */
	private @Nullable String databaseVersion;

	/**
	 */
	private boolean tableColumnMetaDataUsed = false;

	/**
	 */
	private boolean getGeneratedKeysSupported = true;

	/**
	 */
	private boolean generatedKeysColumnNameArraySupported = true;

	/**
	 */
	private boolean storesUpperCaseIdentifiers = true;

	/**
	 */
	private boolean storesLowerCaseIdentifiers = false;

	/**
	 */
	private String identifierQuoteString = " ";

	/**
	 */
	private final List<TableParameterMetaData> tableParameterMetaData = new ArrayList<>();


	/**
	 * 用于使用提供的数据库元数据进行初始化的构造函数。
	 * @param databaseMetaData 要使用的元数据
	 */
	protected GenericTableMetaDataProvider(DatabaseMetaData databaseMetaData) throws SQLException {
		this.userName = databaseMetaData.getUserName();
	}


	/**
	 * 初始化：With Meta Data（方法 `initializeWithMetaData`）。
	 */
	@Override
	public void initializeWithMetaData(DatabaseMetaData databaseMetaData) throws SQLException {
		try {
			setGetGeneratedKeysSupported(databaseMetaData.supportsGetGeneratedKeys());
			setGeneratedKeysColumnNameArraySupported(isGetGeneratedKeysSupported());
		}
		catch (SQLException ex) {
			if (logger.isWarnEnabled()) {
				logger.warn("Error retrieving 'DatabaseMetaData.supportsGetGeneratedKeys': " + ex.getMessage());
			}
		}

		try {
			this.databaseVersion = databaseMetaData.getDatabaseProductVersion();
		}
		catch (SQLException ex) {
			if (logger.isWarnEnabled()) {
				logger.warn("Error retrieving 'DatabaseMetaData.getDatabaseProductVersion': " + ex.getMessage());
			}
		}

		try {
			setStoresUpperCaseIdentifiers(databaseMetaData.storesUpperCaseIdentifiers());
		}
		catch (SQLException ex) {
			if (logger.isWarnEnabled()) {
				logger.warn("Error retrieving 'DatabaseMetaData.storesUpperCaseIdentifiers': " + ex.getMessage());
			}
		}

		try {
			setStoresLowerCaseIdentifiers(databaseMetaData.storesLowerCaseIdentifiers());
		}
		catch (SQLException ex) {
			if (logger.isWarnEnabled()) {
				logger.warn("Error retrieving 'DatabaseMetaData.storesLowerCaseIdentifiers': " + ex.getMessage());
			}
		}

		try {
			this.identifierQuoteString = databaseMetaData.getIdentifierQuoteString();
		}
		catch (SQLException ex) {
			if (logger.isWarnEnabled()) {
				logger.warn("Error retrieving 'DatabaseMetaData.getIdentifierQuoteString': " + ex.getMessage());
			}
		}
	}

	/**
	 * 初始化：With Table Column Meta Data（方法 `initializeWithTableColumnMetaData`）。
	 */
	@Override
	public void initializeWithTableColumnMetaData(DatabaseMetaData databaseMetaData, @Nullable String catalogName,
			@Nullable String schemaName, @Nullable String tableName) throws SQLException {

		this.tableColumnMetaDataUsed = true;
		locateTableAndProcessMetaData(databaseMetaData, catalogName, schemaName, tableName);
	}

	/**
	 * 获取 Table Parameter Meta Data（`TableParameterMetaData`）。
	 */
	@Override
	public List<TableParameterMetaData> getTableParameterMetaData() {
		return this.tableParameterMetaData;
	}

	/**
	 * 方法 `tableNameToUse`：完成本类中与「table Name To Use」相关的职责。
	 */
	@Override
	public @Nullable String tableNameToUse(@Nullable String tableName) {
		return identifierNameToUse(tableName);
	}

	/**
	 * 方法 `columnNameToUse`：完成本类中与「column Name To Use」相关的职责。
	 */
	@Override
	public @Nullable String columnNameToUse(@Nullable String columnName) {
		return identifierNameToUse(columnName);
	}

	/**
	 * 方法 `catalogNameToUse`：完成本类中与「catalog Name To Use」相关的职责。
	 */
	@Override
	public @Nullable String catalogNameToUse(@Nullable String catalogName) {
		return identifierNameToUse(catalogName);
	}

	/**
	 * 方法 `schemaNameToUse`：完成本类中与「schema Name To Use」相关的职责。
	 */
	@Override
	public @Nullable String schemaNameToUse(@Nullable String schemaName) {
		return identifierNameToUse(schemaName);
	}

	/**
	 * 方法 `identifierNameToUse`：完成本类中与「identifier Name To Use」相关的职责。
	 */
	private @Nullable String identifierNameToUse(@Nullable String identifierName) {
		if (identifierName == null) {
			return null;
		}
		else if (isStoresUpperCaseIdentifiers()) {
			return identifierName.toUpperCase(Locale.ROOT);
		}
		else if (isStoresLowerCaseIdentifiers()) {
			return identifierName.toLowerCase(Locale.ROOT);
		}
		else {
			return identifierName;
		}
	}

	/**
	 * 该实现委托给 {@link #catalogNameToUse}。
	 */
	@Override
	public @Nullable String metaDataCatalogNameToUse(@Nullable String catalogName) {
		return catalogNameToUse(catalogName);
	}

	/**
	 * 该实现委托给 {@link #schemaNameToUse}。
	 * @see #getDefaultSchema()
	 */
	@Override
	public @Nullable String metaDataSchemaNameToUse(@Nullable String schemaName) {
		return schemaNameToUse(schemaName != null ? schemaName : getDefaultSchema());
	}

	/**
	 * 提供对子类默认模式的访问。
	 */
	protected @Nullable String getDefaultSchema() {
		return this.userName;
	}

	/**
	 * 提供对子类版本信息的访问。
	 */
	protected @Nullable String getDatabaseVersion() {
		return this.databaseVersion;
	}

	/**
	 * 判断是否 Table Column Meta Data Used。
	 */
	@Override
	public boolean isTableColumnMetaDataUsed() {
		return this.tableColumnMetaDataUsed;
	}

	/**
	 * 设置 Get Generated Keys Supported（`GetGeneratedKeysSupported`）。
	 */
	public void setGetGeneratedKeysSupported(boolean getGeneratedKeysSupported) {
		this.getGeneratedKeysSupported = getGeneratedKeysSupported;
	}

	/**
	 * 判断是否 Get Generated Keys Supported。
	 */
	@Override
	public boolean isGetGeneratedKeysSupported() {
		return this.getGeneratedKeysSupported;
	}

	/**
	 * 判断是否 Get Generated Keys Simulated。
	 */
	@Override
	public boolean isGetGeneratedKeysSimulated(){
		return false;
	}

	/**
	 * 获取 Simple Query For Get Generated Key（`SimpleQueryForGetGeneratedKey`）。
	 */
	@Override
	public @Nullable String getSimpleQueryForGetGeneratedKey(String tableName, String keyColumnName) {
		return null;
	}

	/**
	 * 设置 Generated Keys Column Name Array Supported（`GeneratedKeysColumnNameArraySupported`）。
	 */
	public void setGeneratedKeysColumnNameArraySupported(boolean generatedKeysColumnNameArraySupported) {
		this.generatedKeysColumnNameArraySupported = generatedKeysColumnNameArraySupported;
	}

	/**
	 * 判断是否 Generated Keys Column Name Array Supported。
	 */
	@Override
	public boolean isGeneratedKeysColumnNameArraySupported() {
		return this.generatedKeysColumnNameArraySupported;
	}

	/**
	 * 设置 Stores Upper Case Identifiers（`StoresUpperCaseIdentifiers`）。
	 */
	public void setStoresUpperCaseIdentifiers(boolean storesUpperCaseIdentifiers) {
		this.storesUpperCaseIdentifiers = storesUpperCaseIdentifiers;
	}

	/**
	 * 判断是否 Stores Upper Case Identifiers。
	 */
	public boolean isStoresUpperCaseIdentifiers() {
		return this.storesUpperCaseIdentifiers;
	}

	/**
	 * 设置 Stores Lower Case Identifiers（`StoresLowerCaseIdentifiers`）。
	 */
	public void setStoresLowerCaseIdentifiers(boolean storesLowerCaseIdentifiers) {
		this.storesLowerCaseIdentifiers = storesLowerCaseIdentifiers;
	}

	/**
	 * 判断是否 Stores Lower Case Identifiers。
	 */
	public boolean isStoresLowerCaseIdentifiers() {
		return this.storesLowerCaseIdentifiers;
	}

	/**
	 * 获取 Identifier Quote String（`IdentifierQuoteString`）。
	 */
	@Override
	public String getIdentifierQuoteString() {
		return this.identifierQuoteString;
	}


	/**
	 * 支持表的元数据处理的方法。
	 */
	private void locateTableAndProcessMetaData(DatabaseMetaData databaseMetaData,
			@Nullable String catalogName, @Nullable String schemaName, @Nullable String tableName) {

		Map<String, TableMetaData> tableMeta = new HashMap<>();
		ResultSet tables = null;
		try {
			tables = databaseMetaData.getTables(
					catalogNameToUse(catalogName), schemaNameToUse(schemaName), tableNameToUse(tableName), null);
			while (tables != null && tables.next()) {
				TableMetaData tmd = new TableMetaData(tables.getString("TABLE_CAT"),
						tables.getString("TABLE_SCHEM"), tables.getString("TABLE_NAME"));
				if (tmd.schemaName() == null) {
					tableMeta.put(this.userName != null ? this.userName.toUpperCase(Locale.ROOT) : "", tmd);
				}
				else {
					tableMeta.put(tmd.schemaName().toUpperCase(Locale.ROOT), tmd);
				}
			}
		}
		catch (SQLException ex) {
			if (logger.isWarnEnabled()) {
				logger.warn("Error while accessing table meta-data results: " + ex.getMessage());
			}
		}
		finally {
			JdbcUtils.closeResultSet(tables);
		}

		if (tableMeta.isEmpty()) {
			if (logger.isInfoEnabled()) {
				logger.info("Unable to locate table meta-data for '" + tableName + "': column names must be provided");
			}
		}
		else {
			processTableColumns(databaseMetaData, findTableMetaData(schemaName, tableName, tableMeta));
		}
	}

	/**
	 * 查找：Table Meta Data（方法 `findTableMetaData`）。
	 */
	private TableMetaData findTableMetaData(@Nullable String schemaName, @Nullable String tableName,
			Map<String, TableMetaData> tableMeta) {

		if (schemaName != null) {
			TableMetaData tmd = tableMeta.get(schemaName.toUpperCase(Locale.ROOT));
			if (tmd == null) {
				throw new DataAccessResourceFailureException("Unable to locate table meta-data for '" +
						tableName + "' in the '" + schemaName + "' schema");
			}
			return tmd;
		}
		else if (tableMeta.size() == 1) {
			return tableMeta.values().iterator().next();
		}
		else {
			TableMetaData tmd = tableMeta.get(getDefaultSchema());
			if (tmd == null) {
				tmd = tableMeta.get(this.userName != null ? this.userName.toUpperCase(Locale.ROOT) : "");
			}
			if (tmd == null) {
				tmd = tableMeta.get("PUBLIC");
			}
			if (tmd == null) {
				tmd = tableMeta.get("DBO");
			}
			if (tmd == null) {
				throw new DataAccessResourceFailureException(
						"Unable to locate table meta-data for '" + tableName + "' in the default schema");
			}
			return tmd;
		}
	}

	/**
	 * 支持表列元数据处理的方法。
	 */
	private void processTableColumns(DatabaseMetaData databaseMetaData, TableMetaData tmd) {
		ResultSet tableColumns = null;
		String metaDataCatalogName = metaDataCatalogNameToUse(tmd.catalogName());
		String metaDataSchemaName = metaDataSchemaNameToUse(tmd.schemaName());
		String metaDataTableName = tableNameToUse(tmd.tableName());
		if (logger.isDebugEnabled()) {
			logger.debug("Retrieving meta-data for " + metaDataCatalogName + '/' +
					metaDataSchemaName + '/' + metaDataTableName);
		}
		try {
			tableColumns = databaseMetaData.getColumns(
					metaDataCatalogName, metaDataSchemaName, metaDataTableName, null);
			while (tableColumns != null && tableColumns.next()) {
				String columnName = tableColumns.getString("COLUMN_NAME");
				int dataType = tableColumns.getInt("DATA_TYPE");
				if (dataType == Types.DECIMAL) {
					String typeName = tableColumns.getString("TYPE_NAME");
					int decimalDigits = tableColumns.getInt("DECIMAL_DIGITS");
					// 覆盖非十进制数字的 DECIMAL 数据类型
					// （这是为了在出现问题时提供更好的 Oracle 支持
					// 对某些插入使用 DECIMAL（请参阅 SPR-6912））
					if ("NUMBER".equals(typeName) && decimalDigits == 0) {
						dataType = Types.NUMERIC;
						if (logger.isDebugEnabled()) {
							logger.debug("Overriding meta-data: " + columnName + " now NUMERIC instead of DECIMAL");
						}
					}
				}
				boolean nullable = tableColumns.getBoolean("NULLABLE");
				TableParameterMetaData meta = new TableParameterMetaData(columnName, dataType, nullable);
				this.tableParameterMetaData.add(meta);
				if (logger.isDebugEnabled()) {
					logger.debug("Retrieved meta-data: '" + meta.getParameterName() + "', sqlType=" +
							meta.getSqlType() + ", nullable=" + meta.isNullable());
				}
			}
		}
		catch (SQLException ex) {
			if (logger.isWarnEnabled()) {
				logger.warn("Error while retrieving meta-data for table columns. " +
						"Consider specifying explicit column names -- for example, via SimpleJdbcInsert#usingColumns().",
						ex);
			}
			// 清除元数据，以便我们不保留部分列名列表
			this.tableParameterMetaData.clear();
		}
		finally {
			JdbcUtils.closeResultSet(tableColumns);
		}
	}


	/**
	 * 表示表元数据的记录。
	 */
	private record TableMetaData(@Nullable String catalogName, @Nullable String schemaName,
			@Nullable String tableName) {
	}

}
