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
import java.util.List;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;

import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.jdbc.core.SqlInOutParameter;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.util.StringUtils;

/**
 * {@link CallMetaDataProvider} 接口的通用实现。
 * <p> 这个类可以扩展以提供数据库特定的行为。
 * @author Thomas Risberg
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @author Stephane Nicoll
 * @since 2.5
 */
public class GenericCallMetaDataProvider implements CallMetaDataProvider {

	/**
	 */
	protected static final Log logger = LogFactory.getLog(CallMetaDataProvider.class);


	/** 名称相关状态（`userName`）。 */
	private final String userName;

	/** `false`：该类的成员状态。 */
	private boolean procedureColumnMetaDataUsed = false;

	/** `true`：该类的成员状态。 */
	private boolean supportsCatalogsInProcedureCalls = true;

	/** `true`：该类的成员状态。 */
	private boolean supportsSchemasInProcedureCalls = true;

	/** `true`：该类的成员状态。 */
	private boolean storesUpperCaseIdentifiers = true;

	/** `false`：该类的成员状态。 */
	private boolean storesLowerCaseIdentifiers = false;

	private final List<CallParameterMetaData> callParameterMetaData = new ArrayList<>();


	/**
	 * 用于使用提供的数据库元数据进行初始化的构造函数。
	 * @param databaseMetaData 要使用的元数据
	 */
	protected GenericCallMetaDataProvider(DatabaseMetaData databaseMetaData) throws SQLException {
		this.userName = databaseMetaData.getUserName();
	}


	/**
	 * 初始化：With Meta Data（方法 `initializeWithMetaData`）。
	 */
	@Override
	public void initializeWithMetaData(DatabaseMetaData databaseMetaData) throws SQLException {
		try {
			setSupportsCatalogsInProcedureCalls(databaseMetaData.supportsCatalogsInProcedureCalls());
		}
		catch (SQLException ex) {
			if (logger.isWarnEnabled()) {
				logger.warn("Error retrieving 'DatabaseMetaData.supportsCatalogsInProcedureCalls': " + ex.getMessage());
			}
		}
		try {
			setSupportsSchemasInProcedureCalls(databaseMetaData.supportsSchemasInProcedureCalls());
		}
		catch (SQLException ex) {
			if (logger.isWarnEnabled()) {
				logger.warn("Error retrieving 'DatabaseMetaData.supportsSchemasInProcedureCalls': " + ex.getMessage());
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
	}

	/**
	 * 初始化：With Procedure Column Meta Data（方法 `initializeWithProcedureColumnMetaData`）。
	 */
	@Override
	public void initializeWithProcedureColumnMetaData(DatabaseMetaData databaseMetaData, @Nullable String catalogName,
			@Nullable String schemaName, @Nullable String procedureName) throws SQLException {

		this.procedureColumnMetaDataUsed = true;
		processProcedureColumns(databaseMetaData, catalogName, schemaName, procedureName);
	}

	/**
	 * 获取 Call Parameter Meta Data（`CallParameterMetaData`）。
	 */
	@Override
	public List<CallParameterMetaData> getCallParameterMetaData() {
		return this.callParameterMetaData;
	}

	/**
	 * 方法 `procedureNameToUse`：完成本类中与「procedure Name To Use」相关的职责。
	 */
	@Override
	public @Nullable String procedureNameToUse(@Nullable String procedureName) {
		return identifierNameToUse(procedureName);
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
	 * 方法 `metaDataCatalogNameToUse`：完成本类中与「meta Data Catalog Name To Use」相关的职责。
	 */
	@Override
	public @Nullable String metaDataCatalogNameToUse(@Nullable String catalogName) {
		if (isSupportsCatalogsInProcedureCalls()) {
			return catalogNameToUse(catalogName);
		}
		else {
			return null;
		}
	}

	/**
	 * 方法 `metaDataSchemaNameToUse`：完成本类中与「meta Data Schema Name To Use」相关的职责。
	 */
	@Override
	public @Nullable String metaDataSchemaNameToUse(@Nullable String schemaName) {
		if (isSupportsSchemasInProcedureCalls()) {
			return schemaNameToUse(schemaName);
		}
		else {
			return null;
		}
	}

	/**
	 * 方法 `parameterNameToUse`：完成本类中与「parameter Name To Use」相关的职责。
	 */
	@Override
	public @Nullable String parameterNameToUse(@Nullable String parameterName) {
		return identifierNameToUse(parameterName);
	}

	/**
	 * 方法 `namedParameterBindingToUse`：完成本类中与「named Parameter Binding To Use」相关的职责。
	 */
	@Override
	public String namedParameterBindingToUse(@Nullable String parameterName) {
		return parameterName + " => ?";
	}

	/**
	 * 创建：Default Out Parameter（方法 `createDefaultOutParameter`）。
	 */
	@Override
	public SqlParameter createDefaultOutParameter(String parameterName, CallParameterMetaData meta) {
		return new SqlOutParameter(parameterName, meta.getSqlType());
	}

	/**
	 * 创建：Default In Out Parameter（方法 `createDefaultInOutParameter`）。
	 */
	@Override
	public SqlParameter createDefaultInOutParameter(String parameterName, CallParameterMetaData meta) {
		return new SqlInOutParameter(parameterName, meta.getSqlType());
	}

	/**
	 * 创建：Default In Parameter（方法 `createDefaultInParameter`）。
	 */
	@Override
	public SqlParameter createDefaultInParameter(String parameterName, CallParameterMetaData meta) {
		return new SqlParameter(parameterName, meta.getSqlType());
	}

	/**
	 * 获取 User Name（`UserName`）。
	 */
	@Override
	public String getUserName() {
		return this.userName;
	}

	/**
	 * 判断是否 Procedure Column Meta Data Used。
	 */
	@Override
	public boolean isProcedureColumnMetaDataUsed() {
		return this.procedureColumnMetaDataUsed;
	}

	/**
	 * 判断是否 Return Result Set Supported。
	 */
	@Override
	public boolean isReturnResultSetSupported() {
		return true;
	}

	/**
	 * 判断是否 Ref Cursor Supported。
	 */
	@Override
	public boolean isRefCursorSupported() {
		return false;
	}

	/**
	 * 获取 Ref Cursor Sql Type（`RefCursorSqlType`）。
	 */
	@Override
	public int getRefCursorSqlType() {
		return Types.OTHER;
	}

	/**
	 * 方法 `byPassReturnParameter`：完成本类中与「by Pass Return Parameter」相关的职责。
	 */
	@Override
	public boolean byPassReturnParameter(String parameterName) {
		return false;
	}

	/**
	 * 指定数据库是否支持在过程调用中使用目录名称。
	 */
	protected void setSupportsCatalogsInProcedureCalls(boolean supportsCatalogsInProcedureCalls) {
		this.supportsCatalogsInProcedureCalls = supportsCatalogsInProcedureCalls;
	}

	/**
	 * 数据库是否支持在过程调用中使用目录名称？
	 */
	@Override
	public boolean isSupportsCatalogsInProcedureCalls() {
		return this.supportsCatalogsInProcedureCalls;
	}

	/**
	 * 指定数据库是否支持在过程调用中使用架构名称。
	 */
	protected void setSupportsSchemasInProcedureCalls(boolean supportsSchemasInProcedureCalls) {
		this.supportsSchemasInProcedureCalls = supportsSchemasInProcedureCalls;
	}

	/**
	 * 数据库是否支持在过程调用中使用架构名称？
	 */
	@Override
	public boolean isSupportsSchemasInProcedureCalls() {
		return this.supportsSchemasInProcedureCalls;
	}

	/**
	 * 指定数据库是否使用大写字母作为标识符。
	 */
	protected void setStoresUpperCaseIdentifiers(boolean storesUpperCaseIdentifiers) {
		this.storesUpperCaseIdentifiers = storesUpperCaseIdentifiers;
	}

	/**
	 * 数据库是否使用大写字母作为标识符？
	 */
	protected boolean isStoresUpperCaseIdentifiers() {
		return this.storesUpperCaseIdentifiers;
	}

	/**
	 * 指定数据库是否使用小写字母作为标识符。
	 */
	protected void setStoresLowerCaseIdentifiers(boolean storesLowerCaseIdentifiers) {
		this.storesLowerCaseIdentifiers = storesLowerCaseIdentifiers;
	}

	/**
	 * 数据库是否使用小写字母作为标识符？
	 */
	protected boolean isStoresLowerCaseIdentifiers() {
		return this.storesLowerCaseIdentifiers;
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
	 * 处理过程列元数据。
	 */
	private void processProcedureColumns(DatabaseMetaData databaseMetaData,
			@Nullable String catalogName, @Nullable String schemaName, @Nullable String procedureName) {

		String metaDataCatalogName = metaDataCatalogNameToUse(catalogName);
		String metaDataSchemaName = metaDataSchemaNameToUse(schemaName);
		String metaDataProcedureName = procedureNameToUse(procedureName);
		try {
			ProcedureMetadata procedureMetadata = getProcedureMetadata(databaseMetaData,
					metaDataCatalogName, metaDataSchemaName, metaDataProcedureName);
			if (procedureMetadata.hits() > 1) {
				// 如果有占位符，请重试完全匹配
				String searchStringEscape = databaseMetaData.getSearchStringEscape();
				if (searchStringEscape != null) {
					procedureMetadata = getProcedureMetadata(databaseMetaData, metaDataCatalogName,
							escapeNamePattern(metaDataSchemaName, searchStringEscape),
							escapeNamePattern(metaDataProcedureName, searchStringEscape));
				}
			}
			if (procedureMetadata.hits() == 0) {
				// PostgreSQL 驱动程序 42.2.11 上的函数不再作为过程公开
				procedureMetadata = getProcedureMetadataAsFunction(databaseMetaData,
						metaDataCatalogName, metaDataSchemaName, metaDataProcedureName);
				if (procedureMetadata.hits() > 1) {
					// 如果有占位符，请重试完全匹配
					String searchStringEscape = databaseMetaData.getSearchStringEscape();
					if (searchStringEscape != null) {
						procedureMetadata = getProcedureMetadataAsFunction(
								databaseMetaData, metaDataCatalogName,
								escapeNamePattern(metaDataSchemaName, searchStringEscape),
								escapeNamePattern(metaDataProcedureName, searchStringEscape));
					}
				}
			}
			// 处理比赛

			boolean isFunction = procedureMetadata.function();
			List<String> matches = procedureMetadata.matches;
			if (matches.size() > 1) {
				throw new InvalidDataAccessApiUsageException(
						"Unable to determine the correct call signature - multiple signatures for '" +
						metaDataProcedureName + "': found " + matches + " " + (isFunction ? "functions" : "procedures"));
			}
			else if (matches.isEmpty()) {
				if (metaDataProcedureName != null && metaDataProcedureName.contains(".") &&
						!StringUtils.hasText(metaDataCatalogName)) {
					String packageName = metaDataProcedureName.substring(0, metaDataProcedureName.indexOf('.'));
					throw new InvalidDataAccessApiUsageException(
							"Unable to determine the correct call signature for '" + metaDataProcedureName +
							"' - package name should be specified separately using '.withCatalogName(\"" +
							packageName + "\")'");
				}
				else if ("Oracle".equals(databaseMetaData.getDatabaseProductName())) {
					if (logger.isDebugEnabled()) {
						logger.debug("Oracle JDBC driver did not return procedure/function/signature for '" +
								metaDataProcedureName + "' - assuming a non-exposed synonym");
					}
				}
				else {
					throw new InvalidDataAccessApiUsageException(
							"Unable to determine the correct call signature - no " +
							"procedure/function/signature for '" + metaDataProcedureName + "'");
				}
			}

			if (logger.isDebugEnabled()) {
				logger.debug("Retrieving column meta-data for " + (isFunction ? "function" : "procedure") + ' ' +
						metaDataCatalogName + '/' + procedureMetadata.schemaName + '/' + procedureMetadata.procedureName);
			}
			try (ResultSet columns = isFunction ?
					databaseMetaData.getFunctionColumns(metaDataCatalogName, procedureMetadata.schemaName, procedureMetadata.procedureName, null) :
					databaseMetaData.getProcedureColumns(metaDataCatalogName, procedureMetadata.schemaName, procedureMetadata.procedureName, null)) {
				while (columns.next()) {
					String columnName = columns.getString("COLUMN_NAME");
					int columnType = columns.getInt("COLUMN_TYPE");
					if (columnName == null && isInOrOutColumn(columnType, isFunction)) {
						if (logger.isDebugEnabled()) {
							logger.debug("Skipping meta-data for: " + columnType + " " + columns.getInt("DATA_TYPE") +
									" " + columns.getString("TYPE_NAME") + " " + columns.getInt("NULLABLE") +
									" (probably a member of a collection)");
						}
					}
					else {
						int nullable = (isFunction ? DatabaseMetaData.functionNullable : DatabaseMetaData.procedureNullable);
						CallParameterMetaData meta = new CallParameterMetaData(isFunction, columnName, columnType,
								columns.getInt("DATA_TYPE"), columns.getString("TYPE_NAME"),
								columns.getInt("NULLABLE") == nullable);
						this.callParameterMetaData.add(meta);
						if (logger.isDebugEnabled()) {
							logger.debug("Retrieved meta-data: " + meta.getParameterName() + " " +
									meta.getParameterType() + " " + meta.getSqlType() + " " +
									meta.getTypeName() + " " + meta.isNullable());
						}
					}
				}
			}
		}
		catch (SQLException ex) {
			if (logger.isWarnEnabled()) {
				logger.warn("Error while retrieving meta-data for procedure columns. " +
						"Consider declaring explicit parameters -- for example, via SimpleJdbcCall#addDeclaredParameter().",
						ex);
			}
			// 虽然我们可以调用 `this.callParameterMetaData.clear()` 以便
			// 我们不保留列名的部分列表（就像我们在
			// GenericTableMetaDataProvider.processTableColumns(...))，我们选择
			// 不要在这里这样做，因为调用存储过程将
			// 无论如何，可能会因参数列表不正确而失败。
		}
	}

	/**
	 * 获取 Procedure Metadata（`ProcedureMetadata`）。
	 */
	private ProcedureMetadata getProcedureMetadata(DatabaseMetaData databaseMetaData,
			@Nullable String catalogName, @Nullable String schemaName, @Nullable String procedureName) throws SQLException {
		if (logger.isDebugEnabled()) {
			logger.debug("Retrieving meta-data for " + catalogName + '/' + schemaName + '/' + procedureName);
		}
		List<String> matches = new ArrayList<>();
		try (ResultSet procedures = databaseMetaData.getProcedures(catalogName, schemaName, procedureName)) {
			while (procedures.next()) {
				matches.add(procedures.getString("PROCEDURE_CAT") + '.' + procedures.getString("PROCEDURE_SCHEM") +
						'.' + procedures.getString("PROCEDURE_NAME"));
			}
		}
		return new ProcedureMetadata(schemaName, procedureName, matches, false);
	}

	/**
	 * 获取 Procedure Metadata As Function（`ProcedureMetadataAsFunction`）。
	 */
	private ProcedureMetadata getProcedureMetadataAsFunction(DatabaseMetaData databaseMetaData,
			@Nullable String catalogName, @Nullable String schemaName, @Nullable String procedureName) throws SQLException {
		if (logger.isDebugEnabled()) {
			logger.debug("Fallback on retrieving function meta-data for " + catalogName + '/' + schemaName + '/' + procedureName);
		}
		List<String> matches = new ArrayList<>();
		try (ResultSet functions = databaseMetaData.getFunctions(catalogName, schemaName, procedureName)) {
			while (functions.next()) {
				matches.add(functions.getString("FUNCTION_CAT") + '.' + functions.getString("FUNCTION_SCHEM") +
						'.' + functions.getString("FUNCTION_NAME"));
			}
		}
		return new ProcedureMetadata(schemaName, procedureName, matches, true);
	}

	/**
	 * 方法 `escapeNamePattern`：完成本类中与「escape Name Pattern」相关的职责。
	 */
	private static @Nullable String escapeNamePattern(@Nullable String name, @Nullable String escape) {
		if (name == null || escape == null) {
			return name;
		}
		return name.replace(escape, escape + escape)
					.replace("_", escape + "_")
					.replace("%", escape + "%");
	}

	/**
	 * 判断是否 In Or Out Column。
	 */
	private static boolean isInOrOutColumn(int columnType, boolean function) {
		if (function) {
			return (columnType == DatabaseMetaData.functionColumnIn ||
					columnType == DatabaseMetaData.functionColumnInOut ||
					columnType == DatabaseMetaData.functionColumnOut);
		}
		else {
			return (columnType == DatabaseMetaData.procedureColumnIn ||
					columnType == DatabaseMetaData.procedureColumnInOut ||
					columnType == DatabaseMetaData.procedureColumnOut);
		}
	}

	/**
	 * 方法 `ProcedureMetadata`：完成本类中与「Procedure Metadata」相关的职责。
	 */
	private record ProcedureMetadata(@Nullable String schemaName, @Nullable String procedureName,
			List<String> matches, boolean function) {

		int hits() {
			return this.matches.size();
		}
	}

}
