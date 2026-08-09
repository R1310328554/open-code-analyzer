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

package org.springframework.jdbc.support;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;

import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.util.NumberUtils;
import org.springframework.util.StringUtils;

/**
 * 使用 JDBC 的通用实用程序方法。主要供框架内部使用，但也可用于自定义 JDBC 访问代码。
 * @author Thomas Risberg
 * @author Juergen Hoeller
 * @author Ben Blinebury
 */
public abstract class JdbcUtils {

	/**
	 * 指示未知（或未指定）SQL 类型的常量。
	 * @see java.sql.Types
	 */
	public static final int TYPE_UNKNOWN = Integer.MIN_VALUE;

	/**
	 * 获取 Log（`Log`）。
	 */
	private static final Log logger = LogFactory.getLog(JdbcUtils.class);

	private static final Map<Integer, String> typeNames = new HashMap<>();

	static {
		try {
			for (Field field : Types.class.getFields()) {
				typeNames.put((Integer) field.get(null), field.getName());
			}
		}
		catch (Exception ex) {
			throw new IllegalStateException("Failed to resolve JDBC Types constants", ex);
		}
	}


	/**
	 * 关闭给定的 JDBC 连接并忽略任何抛出的异常。这对于手动 JDBC 代码中的典型finally 块很有用。
	 * @param con 要关闭的 JDBC 连接（可能是 {@code null}）
	 */
	public static void closeConnection(@Nullable Connection con) {
		if (con != null) {
			try {
				con.close();
			}
			catch (SQLException ex) {
				logger.debug("Could not close JDBC Connection", ex);
			}
			catch (Throwable ex) {
				// 我们不信任 JDBC 驱动程序：它可能会抛出 RuntimeException 或错误。
				logger.debug("Unexpected exception on closing JDBC Connection", ex);
			}
		}
	}

	/**
	 * 关闭给定的 JDBC 语句并忽略任何抛出的异常。这对于手动 JDBC 代码中的典型finally 块很有用。
	 * @param stmt 要关闭的 JDBC 语句（可能是 {@code null}）
	 */
	public static void closeStatement(@Nullable Statement stmt) {
		if (stmt != null) {
			try {
				stmt.close();
			}
			catch (SQLException ex) {
				logger.trace("Could not close JDBC Statement", ex);
			}
			catch (Throwable ex) {
				// 我们不信任 JDBC 驱动程序：它可能会抛出 RuntimeException 或错误。
				logger.trace("Unexpected exception on closing JDBC Statement", ex);
			}
		}
	}

	/**
	 * 关闭给定的 JDBC ResultSet 并忽略任何抛出的异常。这对于手动 JDBC 代码中的典型finally 块很有用。
	 * @param rs 要关闭的 JDBC ResultSet（可能是 {@code null}）
	 */
	public static void closeResultSet(@Nullable ResultSet rs) {
		if (rs != null) {
			try {
				rs.close();
			}
			catch (SQLException ex) {
				logger.trace("Could not close JDBC ResultSet", ex);
			}
			catch (Throwable ex) {
				// 我们不信任 JDBC 驱动程序：它可能会抛出 RuntimeException 或错误。
				logger.trace("Unexpected exception on closing JDBC ResultSet", ex);
			}
		}
	}

	/**
	 * 使用指定的值类型从 ResultSet 检索 JDBC 列值。 <p>U 使用特定类型的 ResultSet 访问器方法，对于未知类型回退到 {@link #getResult
	 * SetValue(java.sql.ResultSet, int)}。 <p>请注意，如果类型未知，则返回值可能无法分配给指定的所需类型。调用代码需要适当处理这种情况，例如抛出
	 * 相应的异常。
	 * @param rs 是保存数据的 ResultSet
	 * @param index 是列索引
	 * @param requiredType 所需的值类型（可能是 {@code null}）
	 * @return 值对象（可能不是指定的所需类型，需要进一步的转换步骤）
	 * @throws SQLException 如果由 JDBC API 抛出
	 * @see #getResultSetValue(ResultSet, int)
	 */
	public static @Nullable Object getResultSetValue(ResultSet rs, int index, @Nullable Class<?> requiredType) throws SQLException {
		if (requiredType == null) {
			return getResultSetValue(rs, index);
		}

		Object value;

		// 尽可能显式提取键入的值。
		if (String.class == requiredType) {
			return rs.getString(index);
		}
		else if (boolean.class == requiredType || Boolean.class == requiredType) {
			value = rs.getBoolean(index);
		}
		else if (byte.class == requiredType || Byte.class == requiredType) {
			value = rs.getByte(index);
		}
		else if (short.class == requiredType || Short.class == requiredType) {
			value = rs.getShort(index);
		}
		else if (int.class == requiredType || Integer.class == requiredType) {
			value = rs.getInt(index);
		}
		else if (long.class == requiredType || Long.class == requiredType) {
			value = rs.getLong(index);
		}
		else if (float.class == requiredType || Float.class == requiredType) {
			value = rs.getFloat(index);
		}
		else if (double.class == requiredType || Double.class == requiredType ||
				Number.class == requiredType) {
			value = rs.getDouble(index);
		}
		else if (BigDecimal.class == requiredType) {
			return rs.getBigDecimal(index);
		}
		else if (java.sql.Date.class == requiredType) {
			return rs.getDate(index);
		}
		else if (java.sql.Time.class == requiredType) {
			return rs.getTime(index);
		}
		else if (java.sql.Timestamp.class == requiredType || java.util.Date.class == requiredType) {
			return rs.getTimestamp(index);
		}
		else if (byte[].class == requiredType) {
			return rs.getBytes(index);
		}
		else if (Blob.class == requiredType) {
			return rs.getBlob(index);
		}
		else if (Clob.class == requiredType) {
			return rs.getClob(index);
		}
		else if (requiredType.isEnum()) {
			// 枚举可以通过字符串或枚举索引值表示：
			// 将枚举类型转换留给调用者（例如 ConversionService）
			// 但请确保我们只返回字符串或整数。
			Object obj = rs.getObject(index);
			if (obj instanceof String) {
				return obj;
			}
			else if (obj instanceof Number number) {
				// 防御性地将任何数字转换为整数（根据我们的需要
				// ConversionService 的 IntegerToEnumConverterFactory）用作索引
				return NumberUtils.convertNumberToTargetClass(number, Integer.class);
			}
			else {
				// 例如，在 Postgres 上： getObject 返回一个 PGObject，但我们需要一个 String
				return rs.getString(index);
			}
		}

		else {
			// 需要一些未知类型 -> 依赖 getObject。
			try {
				return rs.getObject(index, requiredType);
			}
			catch (SQLFeatureNotSupportedException | AbstractMethodError ex) {
				logger.debug("JDBC driver does not support JDBC 4.1 'getObject(int, Class)' method", ex);
			}
			catch (SQLException ex) {
				if (logger.isDebugEnabled()) {
					logger.debug("JDBC driver has limited support for 'getObject(int, Class)' with column type: " +
							requiredType.getName(), ex);
				}
			}

			// JSR-310 对应的 SQL 类型，由调用者进行转换
			// 它们（例如，通过 ConversionService）。
			String typeName = requiredType.getSimpleName();
			return switch (typeName) {
				case "LocalDate" -> rs.getDate(index);
				case "LocalTime" -> rs.getTime(index);
				case "LocalDateTime" -> rs.getTimestamp(index);
				// 再次退回到没有类型规范的 getObject
				// 如有必要，由调用者自行转换该值。
				default -> getResultSetValue(rs, index);
			};
		}

		// 如有必要，执行 was-null 检查（对于 JDBC 驱动程序作为原语返回的结果）。
		return (rs.wasNull() ? null : value);
	}

	/**
	 * 使用最合适的值类型从 ResultSet 检索 JDBC 列值。返回的值应该是一个分离的值对象，与活动的 ResultSet 没有任何联系：特别是，它不应该是 Blob 或 C
	 * lob 对象，而应该分别是字节数组或字符串表示形式。 <p>U 使用 {@code getObject(index)} 方法，但包含额外的“技巧”来绕过 Oracle 10g 
	 * 返回其 TIMESTAMP 数据类型的非标准对象和 {@code java.sql.Date} 用于 DATE 列，省略时间部分：这些列将显式提取为标准 {@code java
	 * .sql.Timestamp} 对象。
	 * @param rs 是保存数据的 ResultSet
	 * @param index 是列索引
	 * @return 值对象
	 * @throws SQLException 如果由 JDBC API 抛出
	 * @see java.sql.Blob
	 * @see java.sql.Clob
	 * @see java.sql.Timestamp
	 */
	public static @Nullable Object getResultSetValue(ResultSet rs, int index) throws SQLException {
		Object obj = rs.getObject(index);
		String className = null;
		if (obj != null) {
			className = obj.getClass().getName();
		}
		if (obj instanceof Blob blob) {
			obj = blob.getBytes(1, (int) blob.length());
		}
		else if (obj instanceof Clob clob) {
			obj = clob.getSubString(1, (int) clob.length());
		}
		else if ("oracle.sql.TIMESTAMP".equals(className) || "oracle.sql.TIMESTAMPTZ".equals(className)) {
			obj = rs.getTimestamp(index);
		}
		else if (className != null && className.startsWith("oracle.sql.DATE")) {
			String metaDataClassName = rs.getMetaData().getColumnClassName(index);
			if ("java.sql.Timestamp".equals(metaDataClassName) || "oracle.sql.TIMESTAMP".equals(metaDataClassName)) {
				obj = rs.getTimestamp(index);
			}
			else {
				obj = rs.getDate(index);
			}
		}
		else if (obj instanceof java.sql.Date) {
			if ("java.sql.Timestamp".equals(rs.getMetaData().getColumnClassName(index))) {
				obj = rs.getTimestamp(index);
			}
		}
		return obj;
	}

	/**
	 * 通过给定的 DatabaseMetaDataCallback 提取数据库元数据。 <p>此方法将打开与数据库的连接并检索其元数据。由于该方法是在为数据源配置异常转换功能之前调用
	 * 的，因此该方法不能依赖于 SQLException 转换本身。 <p>A任何异常都将包装在 MetaDataAccessException 中。这是一个已检查的异常，任何调用代
	 * 码都应该捕获并处理此异常。您可以只记录错误并希望得到最好的结果，但是当您尝试再次访问数据库时，可能会再次出现更严重的错误。
	 * @param dataSource 用于提取元数据的 DataSource
	 * @param action 回调将完成实际工作
	 * @return 包含由 DatabaseMetaDataCallback 的 {@code processMetaData} 方法返回的提取信息
	 * @throws MetaDataAccessException 如果元数据访问失败
	 * @see java.sql.DatabaseMetaData
	 */
	public static <T extends @Nullable Object> T extractDatabaseMetaData(DataSource dataSource, DatabaseMetaDataCallback<T> action)
			throws MetaDataAccessException {

		Connection con = null;
		try {
			con = DataSourceUtils.getConnection(dataSource);
			DatabaseMetaData metaData;
			try {
				metaData = con.getMetaData();
			}
			catch (SQLException ex) {
				if (DataSourceUtils.isConnectionTransactional(con, dataSource)) {
					// 可能是一个封闭的线程绑定连接 - 重试新连接
					DataSourceUtils.releaseConnection(con, dataSource);
					con = null;
					logger.debug("Failed to obtain DatabaseMetaData from transactional Connection - " +
							"retrying against fresh Connection", ex);
					con = dataSource.getConnection();
					metaData = con.getMetaData();
				}
				else {
					throw ex;
				}
			}
			if (metaData == null) {
				// 应该只发生在测试环境中
				throw new MetaDataAccessException("DatabaseMetaData returned by Connection [" + con + "] was null");
			}
			return action.processMetaData(metaData);
		}
		catch (CannotGetJdbcConnectionException ex) {
			throw new MetaDataAccessException("Could not get Connection for extracting meta-data", ex);
		}
		catch (SQLException ex) {
			throw new MetaDataAccessException("Error while extracting DatabaseMetaData", ex);
		}
		catch (AbstractMethodError err) {
			throw new MetaDataAccessException(
					"JDBC DatabaseMetaData method not implemented by JDBC driver - upgrade your driver", err);
		}
		finally {
			DataSourceUtils.releaseConnection(con, dataSource);
		}
	}

	/**
	 * 为给定的 DataSource 调用 DatabaseMetaData 上的指定方法，并提取调用结果。
	 * @param dataSource 用于提取元数据的 DataSource
	 * @param metaDataMethodName 要调用的 DatabaseMetaData 方法的名称
	 * @return 指定DatabaseMetaData方法返回的对象
	 * @throws MetaDataAccessException 如果我们无法访问 DatabaseMetaData 或无法调用指定的方法
	 * @see java.sql.DatabaseMetaData
	 * @deprecated 支持带有 lambda 表达式或方法引用和通用类型结果的 {@link #extractDatabaseMetaData(DataSource, DatabaseMetaDataCallback)}
	 */
	@Deprecated(since = "5.2.9")
	@SuppressWarnings("unchecked")
	public static <T> T extractDatabaseMetaData(DataSource dataSource, final String metaDataMethodName)
			throws MetaDataAccessException {

		return (T) extractDatabaseMetaData(dataSource,
				dbmd -> {
					try {
						return DatabaseMetaData.class.getMethod(metaDataMethodName).invoke(dbmd);
					}
					catch (NoSuchMethodException ex) {
						throw new MetaDataAccessException("No method named '" + metaDataMethodName +
								"' found on DatabaseMetaData instance [" + dbmd + "]", ex);
					}
					catch (IllegalAccessException ex) {
						throw new MetaDataAccessException(
								"Could not access DatabaseMetaData method '" + metaDataMethodName + "'", ex);
					}
					catch (InvocationTargetException ex) {
						if (ex.getTargetException() instanceof SQLException sqlException) {
							throw sqlException;
						}
						throw new MetaDataAccessException(
								"Invocation of DatabaseMetaData method '" + metaDataMethodName + "' failed", ex);
					}
				});
	}

	/**
	 * 返回给定的 JDBC 驱动程序是否支持 JDBC 批量更新。 <p> 通常在执行给定的一组语句之前调用：决定是否应通过 JDBC 批处理机制或简单地以传统的一对一方式执行该组 
	 * SQL 语句。如果“supportsBatchUpdates”方法引发异常，<p> 会记录警告并在这种情况下仅返回 {@code false}。
	 * @param con 要检查的连接
	 * @return 支持JDBC批量更新
	 * @see java.sql.DatabaseMetaData#supportsBatchUpdates()
	 */
	public static boolean supportsBatchUpdates(Connection con) {
		try {
			DatabaseMetaData dbmd = con.getMetaData();
			if (dbmd != null) {
				if (dbmd.supportsBatchUpdates()) {
					logger.debug("JDBC driver supports batch updates");
					return true;
				}
				else {
					logger.debug("JDBC driver does not support batch updates");
				}
			}
		}
		catch (SQLException ex) {
			logger.debug("JDBC driver 'supportsBatchUpdates' method threw exception", ex);
		}
		return false;
	}

	/**
	 * 即使各种驱动程序/平台在运行时提供不同的名称，也可以为正在使用的目标数据库提取通用名称。
	 * @param source 数据库元数据中提供的名称
	 * @return 要使用的通用名称（例如“DB2”或“Sybase”）
	 */
	public static @Nullable String commonDatabaseName(@Nullable String source) {
		String name = source;
		if (source != null && source.startsWith("DB2")) {
			name = "DB2";
		}
		else if ("Sybase SQL Server".equals(source) ||
				"Adaptive Server Enterprise".equals(source) ||
				"ASE".equals(source) ||
				"sql server".equalsIgnoreCase(source) ) {
			name = "Sybase";
		}
		return name;
	}

	/**
	 * 检查给定的 SQL 类型是否为数字。
	 * @param sqlType 要检查的 SQL 类型
	 * @return 类型是数字
	 */
	public static boolean isNumeric(int sqlType) {
		return (Types.BIT == sqlType || Types.BIGINT == sqlType || Types.DECIMAL == sqlType ||
				Types.DOUBLE == sqlType || Types.FLOAT == sqlType || Types.INTEGER == sqlType ||
				Types.NUMERIC == sqlType || Types.REAL == sqlType || Types.SMALLINT == sqlType ||
				Types.TINYINT == sqlType);
	}

	/**
	 * 如果可能，解析给定 SQL 类型的标准类型名称。
	 * @param sqlType 要解析的 SQL 类型
	 * @return {@link java.sql.Types} 中对应的常量名称（例如“VARCHAR”/“NUMERIC”），如果不可解析则为 {@code null}
	 * @since 5.2
	 */
	public static @Nullable String resolveTypeName(int sqlType) {
		return typeNames.get(sqlType);
	}

	/**
	 * 确定要使用的列名称。列名称是根据使用 ResultSetMetaData 的查找来确定的。 <p>此方法的实现考虑了 JDBC 4.0 规范中表达的说明： <p><i>colu
	 * mnLabel - 使用 SQL AS 子句指定的列的标签。如果未指定 SQL AS 子句，则标签是列 </i> 的名称。
	 * @param resultSetMetaData 当前要使用的元数据
	 * @param columnIndex 用于查找的列的索引
	 * @return 要使用的列名称
	 * @throws SQLException 如果查找失败
	 */
	public static String lookupColumnName(ResultSetMetaData resultSetMetaData, int columnIndex) throws SQLException {
		String name = resultSetMetaData.getColumnLabel(columnIndex);
		if (!StringUtils.hasLength(name)) {
			name = resultSetMetaData.getColumnName(columnIndex);
		}
		return name;
	}

	/**
	 * 使用“camelCase”将属性名称转换为带有下划线的相应列名称。诸如“customerNumber”之类的名称将与“customer_number”列名称匹配。
	 * @param name 要转换的属性名称
	 * @return 使用下划线的列名
	 * @since 6.1
	 * @see #convertUnderscoreNameToPropertyName
	 */
	public static String convertPropertyNameToUnderscoreName(@Nullable String name) {
		if (!StringUtils.hasLength(name)) {
			return "";
		}

		StringBuilder result = new StringBuilder();
		result.append(Character.toLowerCase(name.charAt(0)));
		for (int i = 1; i < name.length(); i++) {
			char c = name.charAt(i);
			if (Character.isUpperCase(c)) {
				result.append('_').append(Character.toLowerCase(c));
			}
			else {
				result.append(c);
			}
		}
		return result.toString();
	}

	/**
	 * 使用“camelCase”将带下划线的列名称转换为相应的属性名称。诸如“customer_number”之类的名称将与“customerNumber”属性名称匹配。
	 * @param name 要转换的可能基于下划线的列名称
	 * @return 使用“camelCase”命名
	 * @see #convertPropertyNameToUnderscoreName
	 */
	public static String convertUnderscoreNameToPropertyName(@Nullable String name) {
		if (!StringUtils.hasLength(name)) {
			return "";
		}

		StringBuilder result = new StringBuilder();
		boolean nextIsUpper = false;
		if (name.length() > 1 && name.charAt(1) == '_') {
			result.append(Character.toUpperCase(name.charAt(0)));
		}
		else {
			result.append(Character.toLowerCase(name.charAt(0)));
		}
		for (int i = 1; i < name.length(); i++) {
			char c = name.charAt(i);
			if (c == '_') {
				nextIsUpper = true;
			}
			else {
				if (nextIsUpper) {
					result.append(Character.toUpperCase(c));
					nextIsUpper = false;
				}
				else {
					result.append(Character.toLowerCase(c));
				}
			}
		}
		return result.toString();
	}

}
