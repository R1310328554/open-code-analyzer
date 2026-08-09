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
 * <p>Note：从 JDBC 4.0 开始，已经明确任何使用 String 来标识列的方法都应该使用列标签。列标签是使用 SQL 查询字符串中的 ALIAS
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

	/**
	 */
	private static final long serialVersionUID = -4688694393146734764L;

	/** 结果相关状态（`resultSet`）。 */
	@SuppressWarnings("serial")
	private final ResultSet resultSet;

	/** `rowSetMetaData`：该类的成员状态。 */
	@SuppressWarnings("serial")
	private final SqlRowSetMetaData rowSetMetaData;

	/** `columnLabelMap`：该类的成员状态。 */
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
	 * @see java.sql.ResultSetMetaData#getCatalogName(int)
	 */
	@Override
	public final SqlRowSetMetaData getMetaData() {
		return this.rowSetMetaData;
	}

	/**
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
	 * @see java.sql.ResultSet#getBigDecimal(int)
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
	 * @see java.sql.ResultSet#getBigDecimal(String)
	 */
	@Override
	public @Nullable BigDecimal getBigDecimal(String columnLabel) throws InvalidResultSetAccessException {
		return getBigDecimal(findColumn(columnLabel));
	}

	/**
	 * @see java.sql.ResultSet#getBoolean(int)
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
	 * @see java.sql.ResultSet#getBoolean(String)
	 */
	@Override
	public boolean getBoolean(String columnLabel) throws InvalidResultSetAccessException {
		return getBoolean(findColumn(columnLabel));
	}

	/**
	 * @see java.sql.ResultSet#getByte(int)
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
	 * @see java.sql.ResultSet#getByte(String)
	 */
	@Override
	public byte getByte(String columnLabel) throws InvalidResultSetAccessException {
		return getByte(findColumn(columnLabel));
	}

	/**
	 * @see java.sql.ResultSet#getDate(int)
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
	 * @see java.sql.ResultSet#getDate(String)
	 */
	@Override
	public @Nullable Date getDate(String columnLabel) throws InvalidResultSetAccessException {
		return getDate(findColumn(columnLabel));
	}

	/**
	 * @see java.sql.ResultSet#getDate(int, Calendar)
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
	 * @see java.sql.ResultSet#getDate(String, Calendar)
	 */
	@Override
	public @Nullable Date getDate(String columnLabel, Calendar cal) throws InvalidResultSetAccessException {
		return getDate(findColumn(columnLabel), cal);
	}

	/**
	 * @see java.sql.ResultSet#getDouble(int)
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
	 * @see java.sql.ResultSet#getDouble(String)
	 */
	@Override
	public double getDouble(String columnLabel) throws InvalidResultSetAccessException {
		return getDouble(findColumn(columnLabel));
	}

	/**
	 * @see java.sql.ResultSet#getFloat(int)
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
	 * @see java.sql.ResultSet#getFloat(String)
	 */
	@Override
	public float getFloat(String columnLabel) throws InvalidResultSetAccessException {
		return getFloat(findColumn(columnLabel));
	}

	/**
	 * @see java.sql.ResultSet#getInt(int)
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
	 * @see java.sql.ResultSet#getInt(String)
	 */
	@Override
	public int getInt(String columnLabel) throws InvalidResultSetAccessException {
		return getInt(findColumn(columnLabel));
	}

	/**
	 * @see java.sql.ResultSet#getLong(int)
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
	 * @see java.sql.ResultSet#getLong(String)
	 */
	@Override
	public long getLong(String columnLabel) throws InvalidResultSetAccessException {
		return getLong(findColumn(columnLabel));
	}

	/**
	 * @see java.sql.ResultSet#getNString(int)
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
	 * @see java.sql.ResultSet#getNString(String)
	 */
	@Override
	public @Nullable String getNString(String columnLabel) throws InvalidResultSetAccessException {
		return getNString(findColumn(columnLabel));
	}

	/**
	 * @see java.sql.ResultSet#getObject(int)
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
	 * @see java.sql.ResultSet#getObject(String)
	 */
	@Override
	public @Nullable Object getObject(String columnLabel) throws InvalidResultSetAccessException {
		return getObject(findColumn(columnLabel));
	}

	/**
	 * @see java.sql.ResultSet#getObject(int, Map)
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
	 * @see java.sql.ResultSet#getObject(String, Map)
	 */
	@Override
	public @Nullable Object getObject(String columnLabel, Map<String, Class<?>> map) throws InvalidResultSetAccessException {
		return getObject(findColumn(columnLabel), map);
	}

	/**
	 * @see java.sql.ResultSet#getObject(int, Class)
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
	 * @see java.sql.ResultSet#getObject(String, Class)
	 */
	@Override
	public <T> @Nullable T getObject(String columnLabel, Class<T> type) throws InvalidResultSetAccessException {
		return getObject(findColumn(columnLabel), type);
	}

	/**
	 * @see java.sql.ResultSet#getShort(int)
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
	 * @see java.sql.ResultSet#getShort(String)
	 */
	@Override
	public short getShort(String columnLabel) throws InvalidResultSetAccessException {
		return getShort(findColumn(columnLabel));
	}

	/**
	 * @see java.sql.ResultSet#getString(int)
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
	 * @see java.sql.ResultSet#getString(String)
	 */
	@Override
	public @Nullable String getString(String columnLabel) throws InvalidResultSetAccessException {
		return getString(findColumn(columnLabel));
	}

	/**
	 * @see java.sql.ResultSet#getTime(int)
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
	 * @see java.sql.ResultSet#getTime(String)
	 */
	@Override
	public @Nullable Time getTime(String columnLabel) throws InvalidResultSetAccessException {
		return getTime(findColumn(columnLabel));
	}

	/**
	 * @see java.sql.ResultSet#getTime(int, Calendar)
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
	 * @see java.sql.ResultSet#getTime(String, Calendar)
	 */
	@Override
	public @Nullable Time getTime(String columnLabel, Calendar cal) throws InvalidResultSetAccessException {
		return getTime(findColumn(columnLabel), cal);
	}

	/**
	 * @see java.sql.ResultSet#getTimestamp(int)
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
	 * @see java.sql.ResultSet#getTimestamp(String)
	 */
	@Override
	public @Nullable Timestamp getTimestamp(String columnLabel) throws InvalidResultSetAccessException {
		return getTimestamp(findColumn(columnLabel));
	}

	/**
	 * @see java.sql.ResultSet#getTimestamp(int, Calendar)
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
	 * @see java.sql.ResultSet#getTimestamp(String, Calendar)
	 */
	@Override
	public @Nullable Timestamp getTimestamp(String columnLabel, Calendar cal) throws InvalidResultSetAccessException {
		return getTimestamp(findColumn(columnLabel), cal);
	}


	// RowSet 导航方法

	/**
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
