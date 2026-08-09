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

package org.springframework.jdbc.support.rowset;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Collections;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import org.springframework.jdbc.InvalidResultSetAccessException;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * Spring 的 {@link SqlRowSet} 接口的常见实现，包装 {@link java.sql.ResultSet}，捕获任何 {@link
 * SQLException SQLExceptions} 并将它们转换为相应的 Spring {@link InvalidResultSetAccessException}。
 * <p> 如果 SqlRowSet 应该以断开连接的方式使用，则传入的 ResultSet 应该已经断开连接。这意味着您通常会传入一个实现 ResultSet 接口的
 * {@code javax.sql.rowset.CachedRowSet}。
 * <p>注意：自 JDBC 4.0 起，已经明确任何使用 String 来标识列的方法都应该使用列标签。列标签是使用 SQL 查询字符串中的 ALIAS
 * 关键字分配的。当查询不使用别名时，默认标签是列名。大多数 JDBC ResultSet 实现都遵循此模式，但也有例外，例如 {@code
 * com.sun.rowset.CachedRowSetImpl} 类仅使用列名称，忽略任何列标签。 {@code ResultSetWrappingSqlRowSet}
 * 会将列标签转换为正确的列索引，以便为 {@code com.sun.rowset.CachedRowSetImpl} 提供更好的支持，这是 {@link
 * org.springframework.jdbc.core.JdbcTemplate} 在使用 RowSet 时使用的默认实现。
 * <p>注意：此类通过 SqlRowSet 接口实现 {@code java.io.Serializable} 标记接口，但只有在其中包含的断开连接的 ResultSet/Row
 * Set 是可序列化的情况下才真正可序列化。大多数 CachedRowSet 实现实际上都是可序列化的，因此序列化通常应该有效。
 * @author Thomas Risberg
 * @author Juergen Hoeller
 * @since 1.2
 * @see java.sql.ResultSet
 * @see javax.sql.rowset.CachedRowSet
 * @see org.springframework.jdbc.core.JdbcTemplate#queryForRowSet
 */
public class ResultSetWrappingSqlRowSet implements SqlRowSet {

	/** 序列化版本 UID。 */
	private static final long serialVersionUID = -4688694393146734764L;

	/** 被包装的 {@link ResultSet}（通常为断开连接的 CachedRowSet）。 */
	@SuppressWarnings("serial")
	private final ResultSet resultSet;

	/** 本 RowSet 的元数据。 */
	@SuppressWarnings("serial")
	private final SqlRowSetMetaData rowSetMetaData;

	/** 列标签到列索引的映射。 */
	@SuppressWarnings("serial")
	private final Map<String, Integer> columnLabelMap;


	/**
	 * 为给定的 {@link ResultSet} 创建新的 {@code ResultSetWrappingSqlRowSet}。
	 * @param resultSet 要包装的断开连接的结果集（通常是 {@code javax.sql.rowset.CachedRowSet}）
	 * @throws InvalidResultSetAccessException 如果提取 ResultSetMetaData 失败
	 * @see javax.sql.rowset.CachedRowSet
	 * @see java.sql.ResultSet#getMetaData
	 * @see ResultSetWrappingSqlRowSetMetaData
	 */
	public ResultSetWrappingSqlRowSet(ResultSet resultSet) throws InvalidResultSetAccessException {
		this.resultSet = resultSet;
		try {
			this.rowSetMetaData = new ResultSetWrappingSqlRowSetMetaData(resultSet.getMetaData());
		}
		catch (SQLException se) {
			throw new InvalidResultSetAccessException(se);
		}
		try {
			ResultSetMetaData rsmd = resultSet.getMetaData();
			if (rsmd != null) {
				int columnCount = rsmd.getColumnCount();
				this.columnLabelMap = CollectionUtils.newHashMap(columnCount * 2);
				for (int i = 1; i <= columnCount; i++) {
					String key = rsmd.getColumnLabel(i);
					// 确保保留任何给定名称的第一个匹配列，
					// 如 ResultSet 的类型级 javadoc 中所定义（第 81 至 83 行）。
					if (!this.columnLabelMap.containsKey(key)) {
						this.columnLabelMap.put(key, i);
					}
					// 还支持以表名为前缀的列名
					// 如 {table_name}.{column.name} 中所示。
					String table = rsmd.getTableName(i);
					if (StringUtils.hasLength(table)) {
						key = table + "." + rsmd.getColumnName(i);
						if (!this.columnLabelMap.containsKey(key)) {
							this.columnLabelMap.put(key, i);
						}
					}
				}
			}
			else {
				this.columnLabelMap = Collections.emptyMap();
			}
		}
		catch (SQLException se) {
			throw new InvalidResultSetAccessException(se);
		}

	}


	/**
	 * 返回底层 ResultSet（通常是 {@code javax.sql.rowset.CachedRowSet}）。
	 * @see javax.sql.rowset.CachedRowSet
	 */
	public final ResultSet getResultSet() {
		return this.resultSet;
	}

	/**
	 * 返回本 RowSet 的元数据。
	 * @see java.sql.ResultSetMetaData#getCatalogName(int)
	 */
	@Override
	public final SqlRowSetMetaData getMetaData() {
		return this.rowSetMetaData;
	}

	/**
	 * 将给定的列标签映射到其列索引。
	 * @param columnLabel 列的名称
	 * @return 给定列标签的列索引
	 * @see java.sql.ResultSet#findColumn(String)
	 */
	@Override
	public int findColumn(String columnLabel) throws InvalidResultSetAccessException {
		Integer columnIndex = this.columnLabelMap.get(columnLabel);
		if (columnIndex != null) {
			return columnIndex;
		}
		else {
			try {
				return this.resultSet.findColumn(columnLabel);
			}
			catch (SQLException se) {
				throw new InvalidResultSetAccessException(se);
			}
		}
	}


	// 用于提取数据值的 RowSet 方法

	/**
	 * 以 BigDecimal 对象形式检索当前行中指定列的值。
	 * @param columnLabel 列标签
	 * @return 表示列值的 BigDecimal 对象
	 * @see java.sql.ResultSet#getBigDecimal(String)
	 */
	@Override
	public @Nullable BigDecimal getBigDecimal(int columnIndex) throws InvalidResultSetAccessException {
		try {
			return this.resultSet.getBigDecimal(columnIndex);
		}
		catch (SQLException se) {
			throw new InvalidResultSetAccessException(se);
		}
	}

	/**
	 * 以 BigDecimal 对象形式检索当前行中指定列的值。
	 * @param columnLabel 列标签
	 * @return 表示列值的 BigDecimal 对象
	 * @see java.sql.ResultSet#getBigDecimal(String)
	 */
	@Override
	public @Nullable BigDecimal getBigDecimal(String columnLabel) throws InvalidResultSetAccessException {
		return getBigDecimal(findColumn(columnLabel));
	}

	/**
	 * 以布尔值形式检索当前行中指定列的值。
	 * @param columnLabel 列标签
	 * @return 表示列值的布尔值
	 * @see java.sql.ResultSet#getBoolean(String)
	 */
	@Override
	public boolean getBoolean(int columnIndex) throws InvalidResultSetAccessException {
		try {
			return this.resultSet.getBoolean(columnIndex);
		}
		catch (SQLException se) {
			throw new InvalidResultSetAccessException(se);
		}
	}

	/**
	 * 以布尔值形式检索当前行中指定列的值。
	 * @param columnLabel 列标签
	 * @return 表示列值的布尔值
	 * @see java.sql.ResultSet#getBoolean(String)
	 */
	@Override
	public boolean getBoolean(String columnLabel) throws InvalidResultSetAccessException {
		return getBoolean(findColumn(columnLabel));
	}

	/**
	 * 以字节形式检索当前行中指定列的值。
	 * @param columnLabel 列标签
	 * @return 表示列值的字节
	 * @see java.sql.ResultSet#getByte(String)
	 */
	@Override
	public byte getByte(int columnIndex) throws InvalidResultSetAccessException {
		try {
			return this.resultSet.getByte(columnIndex);
		}
		catch (SQLException se) {
			throw new InvalidResultSetAccessException(se);
		}
	}

	/**
	 * 以字节形式检索当前行中指定列的值。
	 * @param columnLabel 列标签
	 * @return 表示列值的字节
	 * @see java.sql.ResultSet#getByte(String)
	 */
	@Override
	public byte getByte(String columnLabel) throws InvalidResultSetAccessException {
		return getByte(findColumn(columnLabel));
	}

	/**
	 * 以 Date 对象的形式检索当前行中指定列的值。
	 * @param columnLabel 列标签
	 * @param cal 用于构造日期的日历
	 * @return 表示列值的日期对象
	 * @see java.sql.ResultSet#getDate(String, Calendar)
	 */
	@Override
	public @Nullable Date getDate(int columnIndex) throws InvalidResultSetAccessException {
		try {
			return this.resultSet.getDate(columnIndex);
		}
		catch (SQLException se) {
			throw new InvalidResultSetAccessException(se);
		}
	}

	/**
	 * 以 Date 对象的形式检索当前行中指定列的值。
	 * @param columnLabel 列标签
	 * @param cal 用于构造日期的日历
	 * @return 表示列值的日期对象
	 * @see java.sql.ResultSet#getDate(String, Calendar)
	 */
	@Override
	public @Nullable Date getDate(String columnLabel) throws InvalidResultSetAccessException {
		return getDate(findColumn(columnLabel));
	}

	/**
	 * 以 Date 对象的形式检索当前行中指定列的值。
	 * @param columnLabel 列标签
	 * @param cal 用于构造日期的日历
	 * @return 表示列值的日期对象
	 * @see java.sql.ResultSet#getDate(String, Calendar)
	 */
	@Override
	public @Nullable Date getDate(int columnIndex, Calendar cal) throws InvalidResultSetAccessException {
		try {
			return this.resultSet.getDate(columnIndex, cal);
		}
		catch (SQLException se) {
			throw new InvalidResultSetAccessException(se);
		}
	}

	/**
	 * 以 Date 对象的形式检索当前行中指定列的值。
	 * @param columnLabel 列标签
	 * @param cal 用于构造日期的日历
	 * @return 表示列值的日期对象
	 * @see java.sql.ResultSet#getDate(String, Calendar)
	 */
	@Override
	public @Nullable Date getDate(String columnLabel, Calendar cal) throws InvalidResultSetAccessException {
		return getDate(findColumn(columnLabel), cal);
	}

	/**
	 * 以 Double 对象形式检索当前行中指定列的值。
	 * @param columnLabel 列标签
	 * @return 表示列值的双精度对象
	 * @see java.sql.ResultSet#getDouble(String)
	 */
	@Override
	public double getDouble(int columnIndex) throws InvalidResultSetAccessException {
		try {
			return this.resultSet.getDouble(columnIndex);
		}
		catch (SQLException se) {
			throw new InvalidResultSetAccessException(se);
		}
	}

	/**
	 * 以 Double 对象形式检索当前行中指定列的值。
	 * @param columnLabel 列标签
	 * @return 表示列值的双精度对象
	 * @see java.sql.ResultSet#getDouble(String)
	 */
	@Override
	public double getDouble(String columnLabel) throws InvalidResultSetAccessException {
		return getDouble(findColumn(columnLabel));
	}

	/**
	 * 以浮点数形式检索当前行中指定列的值。
	 * @param columnLabel 列标签
	 * @return 表示列值的浮点数
	 * @see java.sql.ResultSet#getFloat(String)
	 */
	@Override
	public float getFloat(int columnIndex) throws InvalidResultSetAccessException {
		try {
			return this.resultSet.getFloat(columnIndex);
		}
		catch (SQLException se) {
			throw new InvalidResultSetAccessException(se);
		}
	}

	/**
	 * 以浮点数形式检索当前行中指定列的值。
	 * @param columnLabel 列标签
	 * @return 表示列值的浮点数
	 * @see java.sql.ResultSet#getFloat(String)
	 */
	@Override
	public float getFloat(String columnLabel) throws InvalidResultSetAccessException {
		return getFloat(findColumn(columnLabel));
	}

	/**
	 * 以 int 形式检索当前行中指定列的值。
	 * @param columnLabel 列标签
	 * @return int 表示列值
	 * @see java.sql.ResultSet#getInt(String)
	 */
	@Override
	public int getInt(int columnIndex) throws InvalidResultSetAccessException {
		try {
			return this.resultSet.getInt(columnIndex);
		}
		catch (SQLException se) {
			throw new InvalidResultSetAccessException(se);
		}
	}

	/**
	 * 以 int 形式检索当前行中指定列的值。
	 * @param columnLabel 列标签
	 * @return int 表示列值
	 * @see java.sql.ResultSet#getInt(String)
	 */
	@Override
	public int getInt(String columnLabel) throws InvalidResultSetAccessException {
		return getInt(findColumn(columnLabel));
	}

	/**
	 * 以 long 形式检索当前行中指定列的值。
	 * @param columnLabel 列标签
	 * @return long 代表列值
	 * @see java.sql.ResultSet#getLong(String)
	 */
	@Override
	public long getLong(int columnIndex) throws InvalidResultSetAccessException {
		try {
			return this.resultSet.getLong(columnIndex);
		}
		catch (SQLException se) {
			throw new InvalidResultSetAccessException(se);
		}
	}

	/**
	 * 以 long 形式检索当前行中指定列的值。
	 * @param columnLabel 列标签
	 * @return long 代表列值
	 * @see java.sql.ResultSet#getLong(String)
	 */
	@Override
	public long getLong(String columnLabel) throws InvalidResultSetAccessException {
		return getLong(findColumn(columnLabel));
	}

	/**
	 * 以字符串形式检索当前行中指定列的值（对于 NCHAR、NVARCHAR、LONGNVARCHAR 列）。
	 * @param columnLabel 列标签
	 * @return 表示列值的字符串
	 * @since 4.1.3
	 * @see java.sql.ResultSet#getNString(String)
	 */
	@Override
	public @Nullable String getNString(int columnIndex) throws InvalidResultSetAccessException {
		try {
			return this.resultSet.getNString(columnIndex);
		}
		catch (SQLException se) {
			throw new InvalidResultSetAccessException(se);
		}
	}

	/**
	 * 以字符串形式检索当前行中指定列的值（对于 NCHAR、NVARCHAR、LONGNVARCHAR 列）。
	 * @param columnLabel 列标签
	 * @return 表示列值的字符串
	 * @since 4.1.3
	 * @see java.sql.ResultSet#getNString(String)
	 */
	@Override
	public @Nullable String getNString(String columnLabel) throws InvalidResultSetAccessException {
		return getNString(findColumn(columnLabel));
	}

	/**
	 * 以对象形式检索当前行中指定列的值。
	 * @param columnLabel 列标签
	 * @param type 将指定列转换为的 Java 类型
	 * @return 表示列值的对象
	 * @since 4.1.3
	 * @see java.sql.ResultSet#getObject(String, Class)
	 */
	@Override
	public @Nullable Object getObject(int columnIndex) throws InvalidResultSetAccessException {
		try {
			return this.resultSet.getObject(columnIndex);
		}
		catch (SQLException se) {
			throw new InvalidResultSetAccessException(se);
		}
	}

	/**
	 * 以对象形式检索当前行中指定列的值。
	 * @param columnLabel 列标签
	 * @param type 将指定列转换为的 Java 类型
	 * @return 表示列值的对象
	 * @since 4.1.3
	 * @see java.sql.ResultSet#getObject(String, Class)
	 */
	@Override
	public @Nullable Object getObject(String columnLabel) throws InvalidResultSetAccessException {
		return getObject(findColumn(columnLabel));
	}

	/**
	 * 以对象形式检索当前行中指定列的值。
	 * @param columnLabel 列标签
	 * @param type 将指定列转换为的 Java 类型
	 * @return 表示列值的对象
	 * @since 4.1.3
	 * @see java.sql.ResultSet#getObject(String, Class)
	 */
	@Override
	public @Nullable Object getObject(int columnIndex, Map<String, Class<?>> map) throws InvalidResultSetAccessException {
		try {
			return this.resultSet.getObject(columnIndex, map);
		}
		catch (SQLException se) {
			throw new InvalidResultSetAccessException(se);
		}
	}

	/**
	 * 以对象形式检索当前行中指定列的值。
	 * @param columnLabel 列标签
	 * @param type 将指定列转换为的 Java 类型
	 * @return 表示列值的对象
	 * @since 4.1.3
	 * @see java.sql.ResultSet#getObject(String, Class)
	 */
	@Override
	public @Nullable Object getObject(String columnLabel, Map<String, Class<?>> map) throws InvalidResultSetAccessException {
		return getObject(findColumn(columnLabel), map);
	}

	/**
	 * 以对象形式检索当前行中指定列的值。
	 * @param columnLabel 列标签
	 * @param type 将指定列转换为的 Java 类型
	 * @return 表示列值的对象
	 * @since 4.1.3
	 * @see java.sql.ResultSet#getObject(String, Class)
	 */
	@Override
	public <T> @Nullable T getObject(int columnIndex, Class<T> type) throws InvalidResultSetAccessException {
		try {
			return this.resultSet.getObject(columnIndex, type);
		}
		catch (SQLException se) {
			throw new InvalidResultSetAccessException(se);
		}
	}

	/**
	 * 以对象形式检索当前行中指定列的值。
	 * @param columnLabel 列标签
	 * @param type 将指定列转换为的 Java 类型
	 * @return 表示列值的对象
	 * @since 4.1.3
	 * @see java.sql.ResultSet#getObject(String, Class)
	 */
	@Override
	public <T> @Nullable T getObject(String columnLabel, Class<T> type) throws InvalidResultSetAccessException {
		return getObject(findColumn(columnLabel), type);
	}

	/**
	 * 检索当前行中指定列的值作为短值。
	 * @param columnLabel 列标签
	 * @return 短表示列值
	 * @see java.sql.ResultSet#getShort(String)
	 */
	@Override
	public short getShort(int columnIndex) throws InvalidResultSetAccessException {
		try {
			return this.resultSet.getShort(columnIndex);
		}
		catch (SQLException se) {
			throw new InvalidResultSetAccessException(se);
		}
	}

	/**
	 * 检索当前行中指定列的值作为短值。
	 * @param columnLabel 列标签
	 * @return 短表示列值
	 * @see java.sql.ResultSet#getShort(String)
	 */
	@Override
	public short getShort(String columnLabel) throws InvalidResultSetAccessException {
		return getShort(findColumn(columnLabel));
	}

	/**
	 * 以字符串形式检索当前行中指定列的值。
	 * @param columnLabel 列标签
	 * @return 表示列值的字符串
	 * @see java.sql.ResultSet#getString(String)
	 */
	@Override
	public @Nullable String getString(int columnIndex) throws InvalidResultSetAccessException {
		try {
			return this.resultSet.getString(columnIndex);
		}
		catch (SQLException se) {
			throw new InvalidResultSetAccessException(se);
		}
	}

	/**
	 * 以字符串形式检索当前行中指定列的值。
	 * @param columnLabel 列标签
	 * @return 表示列值的字符串
	 * @see java.sql.ResultSet#getString(String)
	 */
	@Override
	public @Nullable String getString(String columnLabel) throws InvalidResultSetAccessException {
		return getString(findColumn(columnLabel));
	}

	/**
	 * 以 Time 对象的形式检索当前行中指示列的值。
	 * @param columnLabel 列标签
	 * @param cal 用于构造日期的日历
	 * @return 表示列值的时间对象
	 * @see java.sql.ResultSet#getTime(String, Calendar)
	 */
	@Override
	public @Nullable Time getTime(int columnIndex) throws InvalidResultSetAccessException {
		try {
			return this.resultSet.getTime(columnIndex);
		}
		catch (SQLException se) {
			throw new InvalidResultSetAccessException(se);
		}
	}

	/**
	 * 以 Time 对象的形式检索当前行中指示列的值。
	 * @param columnLabel 列标签
	 * @param cal 用于构造日期的日历
	 * @return 表示列值的时间对象
	 * @see java.sql.ResultSet#getTime(String, Calendar)
	 */
	@Override
	public @Nullable Time getTime(String columnLabel) throws InvalidResultSetAccessException {
		return getTime(findColumn(columnLabel));
	}

	/**
	 * 以 Time 对象的形式检索当前行中指示列的值。
	 * @param columnLabel 列标签
	 * @param cal 用于构造日期的日历
	 * @return 表示列值的时间对象
	 * @see java.sql.ResultSet#getTime(String, Calendar)
	 */
	@Override
	public @Nullable Time getTime(int columnIndex, Calendar cal) throws InvalidResultSetAccessException {
		try {
			return this.resultSet.getTime(columnIndex, cal);
		}
		catch (SQLException se) {
			throw new InvalidResultSetAccessException(se);
		}
	}

	/**
	 * 以 Time 对象的形式检索当前行中指示列的值。
	 * @param columnLabel 列标签
	 * @param cal 用于构造日期的日历
	 * @return 表示列值的时间对象
	 * @see java.sql.ResultSet#getTime(String, Calendar)
	 */
	@Override
	public @Nullable Time getTime(String columnLabel, Calendar cal) throws InvalidResultSetAccessException {
		return getTime(findColumn(columnLabel), cal);
	}

	/**
	 * 以 Timestamp 对象的形式检索当前行中指示列的值。
	 * @param columnLabel 列标签
	 * @param cal 用于构造日期的日历
	 * @return 表示列值的时间戳对象
	 * @see java.sql.ResultSet#getTimestamp(String, Calendar)
	 */
	@Override
	public @Nullable Timestamp getTimestamp(int columnIndex) throws InvalidResultSetAccessException {
		try {
			return this.resultSet.getTimestamp(columnIndex);
		}
		catch (SQLException se) {
			throw new InvalidResultSetAccessException(se);
		}
	}

	/**
	 * 以 Timestamp 对象的形式检索当前行中指示列的值。
	 * @param columnLabel 列标签
	 * @param cal 用于构造日期的日历
	 * @return 表示列值的时间戳对象
	 * @see java.sql.ResultSet#getTimestamp(String, Calendar)
	 */
	@Override
	public @Nullable Timestamp getTimestamp(String columnLabel) throws InvalidResultSetAccessException {
		return getTimestamp(findColumn(columnLabel));
	}

	/**
	 * 以 Timestamp 对象的形式检索当前行中指示列的值。
	 * @param columnLabel 列标签
	 * @param cal 用于构造日期的日历
	 * @return 表示列值的时间戳对象
	 * @see java.sql.ResultSet#getTimestamp(String, Calendar)
	 */
	@Override
	public @Nullable Timestamp getTimestamp(int columnIndex, Calendar cal) throws InvalidResultSetAccessException {
		try {
			return this.resultSet.getTimestamp(columnIndex, cal);
		}
		catch (SQLException se) {
			throw new InvalidResultSetAccessException(se);
		}
	}

	/**
	 * 以 Timestamp 对象的形式检索当前行中指示列的值。
	 * @param columnLabel 列标签
	 * @param cal 用于构造日期的日历
	 * @return 表示列值的时间戳对象
	 * @see java.sql.ResultSet#getTimestamp(String, Calendar)
	 */
	@Override
	public @Nullable Timestamp getTimestamp(String columnLabel, Calendar cal) throws InvalidResultSetAccessException {
		return getTimestamp(findColumn(columnLabel), cal);
	}


	// RowSet 导航方法

	/**
	 * 将光标移动到行集中最后一行之后的给定行号。
	 * @param row 光标应移动的行号
	 * @return true} 如果光标位于行集上，否则为 {@code false}
	 * @see java.sql.ResultSet#absolute(int)
	 */
	@Override
	public boolean absolute(int row) throws InvalidResultSetAccessException {
		try {
			return this.resultSet.absolute(row);
		}
		catch (SQLException se) {
			throw new InvalidResultSetAccessException(se);
		}
	}

	/**
	 * 将光标移至该行集的末尾。
	 * @see java.sql.ResultSet#afterLast()
	 */
	@Override
	public void afterLast() throws InvalidResultSetAccessException {
		try {
			this.resultSet.afterLast();
		}
		catch (SQLException se) {
			throw new InvalidResultSetAccessException(se);
		}
	}

	/**
	 * 将光标移动到该行集的前面，就在第一行之前。
	 * @see java.sql.ResultSet#beforeFirst()
	 */
	@Override
	public void beforeFirst() throws InvalidResultSetAccessException {
		try {
			this.resultSet.beforeFirst();
		}
		catch (SQLException se) {
			throw new InvalidResultSetAccessException(se);
		}
	}

	/**
	 * 将光标移至该行集的第一行。
	 * @return true} 如果光标位于有效行上，否则为 {@code false}
	 * @see java.sql.ResultSet#first()
	 */
	@Override
	public boolean first() throws InvalidResultSetAccessException {
		try {
			return this.resultSet.first();
		}
		catch (SQLException se) {
			throw new InvalidResultSetAccessException(se);
		}
	}

	/**
	 * 检索当前行号。
	 * @return 当前行号
	 * @see java.sql.ResultSet#getRow()
	 */
	@Override
	public int getRow() throws InvalidResultSetAccessException {
		try {
			return this.resultSet.getRow();
		}
		catch (SQLException se) {
			throw new InvalidResultSetAccessException(se);
		}
	}

	/**
	 * 检索光标是否位于该行集的最后一行之后。
	 * @return true} 如果光标位于最后一行之后，否则为 {@code false}
	 * @see java.sql.ResultSet#isAfterLast()
	 */
	@Override
	public boolean isAfterLast() throws InvalidResultSetAccessException {
		try {
			return this.resultSet.isAfterLast();
		}
		catch (SQLException se) {
			throw new InvalidResultSetAccessException(se);
		}
	}

	/**
	 * 检索光标是否位于该行集的第一行之前。
	 * @return true} 如果光标位于第一行之前，否则为 {@code false}
	 * @see java.sql.ResultSet#isBeforeFirst()
	 */
	@Override
	public boolean isBeforeFirst() throws InvalidResultSetAccessException {
		try {
			return this.resultSet.isBeforeFirst();
		}
		catch (SQLException se) {
			throw new InvalidResultSetAccessException(se);
		}
	}

	/**
	 * 检索光标是否位于该行集的第一行。
	 * @return true} 如果光标位于第一行之后，否则为 {@code false}
	 * @see java.sql.ResultSet#isFirst()
	 */
	@Override
	public boolean isFirst() throws InvalidResultSetAccessException {
		try {
			return this.resultSet.isFirst();
		}
		catch (SQLException se) {
			throw new InvalidResultSetAccessException(se);
		}
	}

	/**
	 * 检索光标是否位于该行集的最后一行。
	 * @return true} 如果光标位于最后一行之后，否则为 {@code false}
	 * @see java.sql.ResultSet#isLast()
	 */
	@Override
	public boolean isLast() throws InvalidResultSetAccessException {
		try {
			return this.resultSet.isLast();
		}
		catch (SQLException se) {
			throw new InvalidResultSetAccessException(se);
		}
	}

	/**
	 * 将光标移至该行集的最后一行。
	 * @return true} 如果光标位于有效行上，否则为 {@code false}
	 * @see java.sql.ResultSet#last()
	 */
	@Override
	public boolean last() throws InvalidResultSetAccessException {
		try {
			return this.resultSet.last();
		}
		catch (SQLException se) {
			throw new InvalidResultSetAccessException(se);
		}
	}

	/**
	 * 将光标移至下一行。
	 * @return true} 如果新行有效，则 {@code false} 如果没有更多行
	 * @see java.sql.ResultSet#next()
	 */
	@Override
	public boolean next() throws InvalidResultSetAccessException {
		try {
			return this.resultSet.next();
		}
		catch (SQLException se) {
			throw new InvalidResultSetAccessException(se);
		}
	}

	/**
	 * 将光标移至上一行。
	 * @return true} 如果新行有效，则 {@code false} 如果它不在行集内
	 * @see java.sql.ResultSet#previous()
	 */
	@Override
	public boolean previous() throws InvalidResultSetAccessException {
		try {
			return this.resultSet.previous();
		}
		catch (SQLException se) {
			throw new InvalidResultSetAccessException(se);
		}
	}

	/**
	 * 将光标移动相对行数（正数或负数）。
	 * @return true} 如果光标位于一行，否则为 {@code false}
	 * @see java.sql.ResultSet#relative(int)
	 */
	@Override
	public boolean relative(int rows) throws InvalidResultSetAccessException {
		try {
			return this.resultSet.relative(rows);
		}
		catch (SQLException se) {
			throw new InvalidResultSetAccessException(se);
		}
	}

	/**
	 * 报告最后读取的列是否具有 SQL {@code NULL} 值。 <p>请注意，您必须首先调用其中一个 getter 方法，然后再调用 {@code wasNull()} 方法
	 * 。
	 * @return true} 如果最近检索到的列是 SQL {@code NULL}，否则为 {@code false}
	 * @see java.sql.ResultSet#wasNull()
	 */
	@Override
	public boolean wasNull() throws InvalidResultSetAccessException {
		try {
			return this.resultSet.wasNull();
		}
		catch (SQLException se) {
			throw new InvalidResultSetAccessException(se);
		}
	}

}
