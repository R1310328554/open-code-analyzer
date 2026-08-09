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

package org.springframework.jdbc.core;

import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;

import org.springframework.core.SpringProperties;
import org.springframework.jdbc.support.SqlValue;

/**
 * 用于PreparedStatementSetter/Creator 和CallableStatementCreator 实现的实用方法，提供复杂的参数管理（包括对LOB 值的支
 * 持）。
 * <p> 由PreparedStatementCreatorFactory 和CallableStatementCreatorFactory
 * 使用，但也可直接在自定义setter/creator 实现中使用。
 * @author Thomas Risberg
 * @author Juergen Hoeller
 * @since 1.1
 * @see PreparedStatementSetter
 * @see PreparedStatementCreator
 * @see CallableStatementCreator
 * @see PreparedStatementCreatorFactory
 * @see CallableStatementCreatorFactory
 * @see SqlParameter
 * @see SqlTypeValue
 * @see org.springframework.jdbc.core.support.SqlLobValue
 */
public abstract class StatementCreatorUtils {

	/**
	 * 指示 Spring 完全忽略 {@link java.sql.ParameterMetaData#getParameterType} 的系统属性，即永远不会尝试为
	 * {@link StatementCreatorUtils#setNull} 调用检索 {@link
	 * PreparedStatement#getParameterMetaData()}。 <p>默认值为“false”，首先尝试 {@code getParameterType}
	 * 调用，然后根据常见数据库的众所周知的行为回退到 {@link PreparedStatement#setNull} / {@link
	 * PreparedStatement#setObject} 调用。 <p> 如果您在运行时遇到不当行为，请考虑将此标志切换为“true”，例如，在 {@code
	 * getParameterType} 抛出异常（如 JBoss AS 7 上报告）或出现性能问题（如 PostgreSQL 上报告）时出现连接池问题。
	 */
	public static final String IGNORE_GETPARAMETERTYPE_PROPERTY_NAME = "spring.jdbc.getParameterType.ignore";


	/**
	 * 获取 Log（`Log`）。
	 */
	private static final Log logger = LogFactory.getLog(StatementCreatorUtils.class);

	private static final Map<Class<?>, Integer> javaTypeToSqlTypeMap = new HashMap<>(64);

	static @Nullable Boolean shouldIgnoreGetParameterType = SpringProperties.checkFlag(IGNORE_GETPARAMETERTYPE_PROPERTY_NAME);

	static {
		javaTypeToSqlTypeMap.put(boolean.class, Types.BOOLEAN);
		javaTypeToSqlTypeMap.put(Boolean.class, Types.BOOLEAN);
		javaTypeToSqlTypeMap.put(byte.class, Types.TINYINT);
		javaTypeToSqlTypeMap.put(Byte.class, Types.TINYINT);
		javaTypeToSqlTypeMap.put(short.class, Types.SMALLINT);
		javaTypeToSqlTypeMap.put(Short.class, Types.SMALLINT);
		javaTypeToSqlTypeMap.put(int.class, Types.INTEGER);
		javaTypeToSqlTypeMap.put(Integer.class, Types.INTEGER);
		javaTypeToSqlTypeMap.put(long.class, Types.BIGINT);
		javaTypeToSqlTypeMap.put(Long.class, Types.BIGINT);
		javaTypeToSqlTypeMap.put(BigInteger.class, Types.BIGINT);
		javaTypeToSqlTypeMap.put(float.class, Types.FLOAT);
		javaTypeToSqlTypeMap.put(Float.class, Types.FLOAT);
		javaTypeToSqlTypeMap.put(double.class, Types.DOUBLE);
		javaTypeToSqlTypeMap.put(Double.class, Types.DOUBLE);
		javaTypeToSqlTypeMap.put(BigDecimal.class, Types.DECIMAL);
		javaTypeToSqlTypeMap.put(LocalDate.class, Types.DATE);
		javaTypeToSqlTypeMap.put(LocalTime.class, Types.TIME);
		javaTypeToSqlTypeMap.put(LocalDateTime.class, Types.TIMESTAMP);
		javaTypeToSqlTypeMap.put(OffsetTime.class, Types.TIME_WITH_TIMEZONE);
		javaTypeToSqlTypeMap.put(OffsetDateTime.class, Types.TIMESTAMP_WITH_TIMEZONE);
		javaTypeToSqlTypeMap.put(java.sql.Date.class, Types.DATE);
		javaTypeToSqlTypeMap.put(java.sql.Time.class, Types.TIME);
		javaTypeToSqlTypeMap.put(java.sql.Timestamp.class, Types.TIMESTAMP);
		javaTypeToSqlTypeMap.put(Blob.class, Types.BLOB);
		javaTypeToSqlTypeMap.put(Clob.class, Types.CLOB);
	}


	/**
	 * 从给定的 Java 类型派生默认 SQL 类型。
	 * @param javaType 要翻译的 Java 类型
	 * @return 相应的 SQL 类型，如果未找到，则为 {@link SqlTypeValue#TYPE_UNKNOWN}
	 */
	public static int javaTypeToSqlParameterType(@Nullable Class<?> javaType) {
		if (javaType == null) {
			return SqlTypeValue.TYPE_UNKNOWN;
		}
		Integer sqlType = javaTypeToSqlTypeMap.get(javaType);
		if (sqlType != null) {
			return sqlType;
		}
		if (Number.class.isAssignableFrom(javaType)) {
			return Types.NUMERIC;
		}
		if (isStringValue(javaType)) {
			return Types.VARCHAR;
		}
		if (isDateValue(javaType) || Calendar.class.isAssignableFrom(javaType)) {
			return Types.TIMESTAMP;
		}
		return SqlTypeValue.TYPE_UNKNOWN;
	}

	/**
	 * 设置参数的值。使用的方法基于参数的 SQL 类型，我们可以处理数组和 LOB 等复杂类型。
	 * @param ps 准备好的语句或可调用的语句
	 * @param paramIndex 我们正在设置的参数的索引
	 * @param param 声明的参数包括类型
	 * @param inValue 要设置的值
	 * @throws SQLException 如果由PreparedStatement方法抛出
	 */
	public static void setParameterValue(PreparedStatement ps, int paramIndex, SqlParameter param,
			@Nullable Object inValue) throws SQLException {

		setParameterValueInternal(ps, paramIndex, param.getSqlType(), param.getTypeName(), param.getScale(), inValue);
	}

	/**
	 * 设置参数的值。使用的方法基于参数的 SQL 类型，我们可以处理数组和 LOB 等复杂类型。
	 * @param ps 准备好的语句或可调用的语句
	 * @param paramIndex 我们正在设置的参数的索引
	 * @param sqlType 参数的 SQL 类型
	 * @param inValue 要设置的值（普通值或 SqlTypeValue）
	 * @throws SQLException 如果由PreparedStatement方法抛出
	 * @see SqlTypeValue
	 */
	public static void setParameterValue(PreparedStatement ps, int paramIndex, int sqlType,
			@Nullable Object inValue) throws SQLException {

		setParameterValueInternal(ps, paramIndex, sqlType, null, null, inValue);
	}

	/**
	 * 设置参数的值。使用的方法基于参数的 SQL 类型，我们可以处理数组和 LOB 等复杂类型。
	 * @param ps 准备好的语句或可调用的语句
	 * @param paramIndex 我们正在设置的参数的索引
	 * @param sqlType 参数的 SQL 类型
	 * @param typeName 参数的类型名称（可选，仅用于 SQL NULL 和 SqlTypeValue）
	 * @param inValue 要设置的值（普通值或 SqlTypeValue）
	 * @throws SQLException 如果由PreparedStatement方法抛出
	 * @see SqlTypeValue
	 */
	public static void setParameterValue(PreparedStatement ps, int paramIndex, int sqlType, String typeName,
			@Nullable Object inValue) throws SQLException {

		setParameterValueInternal(ps, paramIndex, sqlType, typeName, null, inValue);
	}

	/**
	 * 设置参数的值。使用的方法基于参数的 SQL 类型，我们可以处理数组和 LOB 等复杂类型。
	 * @param ps 准备好的语句或可调用的语句
	 * @param paramIndex 我们正在设置的参数的索引
	 * @param sqlType 参数的 SQL 类型
	 * @param typeName 参数的类型名称（可选，仅用于 SQL NULL 和 SqlTypeValue）
	 * @param scale 小数点后的位数（对于 DECIMAL 和 NUMERIC 类型）
	 * @param inValue 要设置的值（普通值或 SqlTypeValue）
	 * @throws SQLException 如果由PreparedStatement方法抛出
	 * @see SqlTypeValue
	 */
	private static void setParameterValueInternal(PreparedStatement ps, int paramIndex, int sqlType,
			@Nullable String typeName, @Nullable Integer scale, @Nullable Object inValue) throws SQLException {

		String typeNameToUse = typeName;
		int sqlTypeToUse = sqlType;
		Object inValueToUse = inValue;

		// 覆盖类型信息？
		if (inValue instanceof SqlParameterValue parameterValue) {
			if (logger.isDebugEnabled()) {
				logger.debug("Overriding type info with runtime info from SqlParameterValue: column index " + paramIndex +
						", SQL type " + parameterValue.getSqlType() + ", type name " + parameterValue.getTypeName());
			}
			if (parameterValue.getSqlType() != SqlTypeValue.TYPE_UNKNOWN) {
				sqlTypeToUse = parameterValue.getSqlType();
			}
			if (parameterValue.getTypeName() != null) {
				typeNameToUse = parameterValue.getTypeName();
			}
			inValueToUse = parameterValue.getValue();
		}

		if (logger.isTraceEnabled()) {
			logger.trace("Setting SQL statement parameter value: column index " + paramIndex +
					", parameter value [" + inValueToUse +
					"], value class [" + (inValueToUse != null ? inValueToUse.getClass().getName() : "null") +
					"], SQL type " + (sqlTypeToUse == SqlTypeValue.TYPE_UNKNOWN ? "unknown" : Integer.toString(sqlTypeToUse)));
		}

		if (inValueToUse == null) {
			setNull(ps, paramIndex, sqlTypeToUse, typeNameToUse);
		}
		else {
			setValue(ps, paramIndex, sqlTypeToUse, typeNameToUse, scale, inValueToUse);
		}
	}

	/**
	 * 将指定的PreparedStatement 参数设置为null，以尊重特定于数据库的特性。
	 */
	private static void setNull(PreparedStatement ps, int paramIndex, int sqlType, @Nullable String typeName)
			throws SQLException {

		if (sqlType == SqlTypeValue.TYPE_UNKNOWN || (sqlType == Types.OTHER && typeName == null)) {
			boolean callGetParameterType = false;
			boolean useSetObject = false;
			Integer sqlTypeToUse = null;
			if (shouldIgnoreGetParameterType != null) {
				callGetParameterType = !shouldIgnoreGetParameterType;
			}
			else {
				String jdbcDriverName = ps.getConnection().getMetaData().getDriverName();
				if (jdbcDriverName.startsWith("PostgreSQL")) {
					sqlTypeToUse = Types.NULL;
				}
				else if (jdbcDriverName.startsWith("Microsoft") && jdbcDriverName.contains("SQL Server")) {
					sqlTypeToUse = Types.NULL;
					useSetObject = true;
				}
				else {
					callGetParameterType = true;
				}
			}
			if (callGetParameterType) {
				try {
					sqlTypeToUse = ps.getParameterMetaData().getParameterType(paramIndex);
				}
				catch (SQLException ex) {
					if (logger.isDebugEnabled()) {
						logger.debug("JDBC getParameterType call failed - using fallback method instead: " + ex);
					}
				}
			}
			if (sqlTypeToUse == null) {
				// 继续进行特定于数据库的检查
				sqlTypeToUse = Types.NULL;
				DatabaseMetaData dbmd = ps.getConnection().getMetaData();
				String jdbcDriverName = dbmd.getDriverName();
				String databaseProductName = dbmd.getDatabaseProductName();
				if (databaseProductName.startsWith("Informix") ||
						(jdbcDriverName.startsWith("Microsoft") && jdbcDriverName.contains("SQL Server"))) {
						// “Microsoft SQL Server JDBC Driver 3.0”与“Microsoft JDBC Driver 4.0 for SQL Server”
					useSetObject = true;
				}
				else if (databaseProductName.startsWith("DB2") ||
						jdbcDriverName.startsWith("jConnect") ||
						jdbcDriverName.startsWith("SQLServer") ||
						jdbcDriverName.startsWith("Apache Derby")) {
					sqlTypeToUse = Types.VARCHAR;
				}
			}
			if (useSetObject) {
				ps.setObject(paramIndex, null);
			}
			else {
				ps.setNull(paramIndex, sqlTypeToUse);
			}
		}
		else if (typeName != null) {
			ps.setNull(paramIndex, sqlType, typeName);
		}
		else {
			// 回退到通用 setNull 调用。
			try {
				// 尝试使用指定的 SQL 类型进行通用 setNull 调用。
				ps.setNull(paramIndex, sqlType);
			}
			catch (SQLFeatureNotSupportedException ex) {
				if (sqlType == Types.NULL) {
					throw ex;
				}
				// 回退到通用 setNull 调用而不指定 SQL 类型
				// （例如，对于 MySQL TIME_WITH_TIMEZONE / TIMESTAMP_WITH_TIMEZONE）。
				ps.setNull(paramIndex, Types.NULL);
			}
		}
	}

	/**
	 * 设置 Value（`Value`）。
	 */
	private static void setValue(PreparedStatement ps, int paramIndex, int sqlType,
			@Nullable String typeName, @Nullable Integer scale, Object inValue) throws SQLException {

		if (inValue instanceof SqlTypeValue sqlTypeValue) {
			sqlTypeValue.setTypeValue(ps, paramIndex, sqlType, typeName);
		}
		else if (inValue instanceof SqlValue sqlValue) {
			sqlValue.setValue(ps, paramIndex);
		}
		else if (sqlType == Types.VARCHAR || sqlType == Types.LONGVARCHAR ) {
			ps.setString(paramIndex, inValue.toString());
		}
		else if (sqlType == Types.NVARCHAR || sqlType == Types.LONGNVARCHAR) {
			ps.setNString(paramIndex, inValue.toString());
		}
		else if ((sqlType == Types.CLOB || sqlType == Types.NCLOB) && isStringValue(inValue.getClass())) {
			String strVal = inValue.toString();
			if (strVal.length() > 4000) {
				// 对于较旧的 Oracle 驱动程序是必需的，特别是在针对 Oracle 10 数据库运行时。
				// 由于它使用标准 JDBC 4.0 API，因此也应该可以与其他驱动程序/数据库一起正常工作。
				if (sqlType == Types.NCLOB) {
					ps.setNClob(paramIndex, new StringReader(strVal), strVal.length());
				}
				else {
					ps.setClob(paramIndex, new StringReader(strVal), strVal.length());
				}
			}
			else {
				// 后备：setString 或 setNString 绑定
				if (sqlType == Types.NCLOB) {
					ps.setNString(paramIndex, strVal);
				}
				else {
					ps.setString(paramIndex, strVal);
				}
			}
		}
		else if (sqlType == Types.DECIMAL || sqlType == Types.NUMERIC) {
			if (inValue instanceof BigDecimal bigDecimal) {
				ps.setBigDecimal(paramIndex, bigDecimal);
			}
			else if (scale != null) {
				ps.setObject(paramIndex, inValue, sqlType, scale);
			}
			else {
				ps.setObject(paramIndex, inValue, sqlType);
			}
		}
		else if (sqlType == Types.BOOLEAN) {
			if (inValue instanceof Boolean flag) {
				ps.setBoolean(paramIndex, flag);
			}
			else {
				ps.setObject(paramIndex, inValue, Types.BOOLEAN);
			}
		}
		else if (sqlType == Types.DATE) {
			if (inValue instanceof java.util.Date date) {
				if (inValue instanceof java.sql.Date sqlDate) {
					ps.setDate(paramIndex, sqlDate);
				}
				else {
					ps.setDate(paramIndex, new java.sql.Date(date.getTime()));
				}
			}
			else if (inValue instanceof Calendar cal) {
				ps.setDate(paramIndex, new java.sql.Date(cal.getTime().getTime()), cal);
			}
			else {
				ps.setObject(paramIndex, inValue, Types.DATE);
			}
		}
		else if (sqlType == Types.TIME) {
			if (inValue instanceof java.util.Date date) {
				if (inValue instanceof java.sql.Time time) {
					ps.setTime(paramIndex, time);
				}
				else {
					ps.setTime(paramIndex, new java.sql.Time(date.getTime()));
				}
			}
			else if (inValue instanceof Calendar cal) {
				ps.setTime(paramIndex, new java.sql.Time(cal.getTime().getTime()), cal);
			}
			else {
				ps.setObject(paramIndex, inValue, Types.TIME);
			}
		}
		else if (sqlType == Types.TIMESTAMP) {
			if (inValue instanceof java.util.Date date) {
				if (inValue instanceof java.sql.Timestamp timestamp) {
					ps.setTimestamp(paramIndex, timestamp);
				}
				else {
					ps.setTimestamp(paramIndex, new java.sql.Timestamp(date.getTime()));
				}
			}
			else if (inValue instanceof Calendar cal) {
				ps.setTimestamp(paramIndex, new java.sql.Timestamp(cal.getTime().getTime()), cal);
			}
			else {
				ps.setObject(paramIndex, inValue, Types.TIMESTAMP);
			}
		}
		else if (sqlType == SqlTypeValue.TYPE_UNKNOWN || (sqlType == Types.OTHER &&
				"Oracle".equals(ps.getConnection().getMetaData().getDatabaseProductName()))) {
			if (inValue instanceof byte[] bytes) {
				ps.setBytes(paramIndex, bytes);
			}
			else if (isStringValue(inValue.getClass())) {
				ps.setString(paramIndex, inValue.toString());
			}
			else if (isDateValue(inValue.getClass())) {
				ps.setTimestamp(paramIndex, new java.sql.Timestamp(((java.util.Date) inValue).getTime()));
			}
			else if (inValue instanceof Calendar cal) {
				ps.setTimestamp(paramIndex, new java.sql.Timestamp(cal.getTime().getTime()), cal);
			}
			else {
				// 回退到不指定 SQL 类型的通用 setObject 调用。
				ps.setObject(paramIndex, inValue);
			}
		}
		else {
			// 回退到通用 setObject 调用。
			try {
				// 尝试使用指定的 SQL 类型进行通用 setObject 调用。
				ps.setObject(paramIndex, inValue, sqlType);
			}
			catch (SQLFeatureNotSupportedException ex) {
				// 回退到通用 setObject 调用而不指定 SQL 类型
				// （例如，对于 MySQL TIME_WITH_TIMEZONE / TIMESTAMP_WITH_TIMEZONE）。
				ps.setObject(paramIndex, inValue);
			}
		}
	}

	/**
	 * 检查给定值是否可以被视为字符串值。
	 */
	private static boolean isStringValue(Class<?> inValueType) {
		// 将任何 CharSequence（包括 StringBuffer 和 StringBuilder）视为字符串。
		return (CharSequence.class.isAssignableFrom(inValueType) ||
				StringWriter.class.isAssignableFrom(inValueType));
	}

	/**
	 * 检查给定值是否是 {@code java.util.Date}（但不是特定于 JDBC 的子类之一）。
	 */
	private static boolean isDateValue(Class<?> inValueType) {
		return (java.util.Date.class.isAssignableFrom(inValueType) &&
				!(java.sql.Date.class.isAssignableFrom(inValueType) ||
						java.sql.Time.class.isAssignableFrom(inValueType) ||
						java.sql.Timestamp.class.isAssignableFrom(inValueType)));
	}

	/**
	 * 清理传递给执行方法的参数值所持有的所有资源。例如，这对于关闭 LOB 值很重要。
	 * @param paramValues 提供的参数值。可能是 {@code null}。
	 * @see DisposableSqlTypeValue#cleanup()
	 * @see org.springframework.jdbc.core.support.SqlLobValue#cleanup()
	 */
	public static void cleanupParameters(@Nullable Object @Nullable ... paramValues) {
		if (paramValues != null) {
			cleanupParameters(Arrays.asList(paramValues));
		}
	}

	/**
	 * 清理传递给执行方法的参数值所持有的所有资源。例如，这对于关闭 LOB 值很重要。
	 * @param paramValues 提供的参数值。可能是 {@code null}。
	 * @see DisposableSqlTypeValue#cleanup()
	 * @see org.springframework.jdbc.core.support.SqlLobValue#cleanup()
	 */
	public static void cleanupParameters(@Nullable Collection<?> paramValues) {
		if (paramValues != null) {
			for (Object inValue : paramValues) {
				// 首先解开 SqlParameterValue...
				if (inValue instanceof SqlParameterValue sqlParameterValue) {
					inValue = sqlParameterValue.getValue();
				}
				// 检查一次性值类型
				if (inValue instanceof SqlValue sqlValue) {
					sqlValue.cleanup();
				}
				else if (inValue instanceof DisposableSqlTypeValue disposableSqlTypeValue) {
					disposableSqlTypeValue.cleanup();
				}
			}
		}
	}

}
