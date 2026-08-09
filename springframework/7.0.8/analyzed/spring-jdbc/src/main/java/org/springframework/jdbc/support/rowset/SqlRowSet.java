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

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import org.springframework.jdbc.InvalidResultSetAccessException;

/**
 * {@link javax.sql.RowSet} 的镜像接口，代表 {@link java.sql.ResultSet} 数据的断开连接变体。
 * <p> 与标准 JDBC RowSet 的主要区别在于，这里永远不会抛出 {@link java.sql.SQLException}。这允许使用 SqlRowSet
 * 而无需处理已检查的异常。 SqlRowSet 将抛出 Spring 的 {@link InvalidResultSetAccessException}（在适当的时候）。
 * <p>注：该接口扩展了{@code java.io.Serializable}标记接口。鼓励通常保存断开连接的数据的实现实际上可序列化（尽可能）。
 * @author Thomas Risberg
 * @author Juergen Hoeller
 * @since 1.2
 * @see javax.sql.RowSet
 * @see java.sql.ResultSet
 * @see org.springframework.jdbc.InvalidResultSetAccessException
 * @see org.springframework.jdbc.core.JdbcTemplate#queryForRowSet
 */
public interface SqlRowSet extends Serializable {

	/**
	 * 检索元数据，即该行集的列的数量、类型和属性。
	 * @return 对应的SqlRowSetMetaData实例
	 * @see java.sql.ResultSet#getMetaData()
	 */
	SqlRowSetMetaData getMetaData();

	/**
	 * 将给定的列标签映射到其列索引。
	 * @param columnLabel 列的名称
	 * @return 给定列标签的列索引
	 * @see java.sql.ResultSet#findColumn(String)
	 */
	int findColumn(String columnLabel) throws InvalidResultSetAccessException;


	// 用于提取数据值的 RowSet 方法

	/**
	 * 以 BigDecimal 对象形式检索当前行中指定列的值。
	 * @param columnIndex 列索引
	 * @return 表示列值的 BigDecimal 对象
	 * @see java.sql.ResultSet#getBigDecimal(int)
	 */
	@Nullable BigDecimal getBigDecimal(int columnIndex) throws InvalidResultSetAccessException;

	/**
	 * 以 BigDecimal 对象形式检索当前行中指定列的值。
	 * @param columnLabel 列标签
	 * @return 表示列值的 BigDecimal 对象
	 * @see java.sql.ResultSet#getBigDecimal(String)
	 */
	@Nullable BigDecimal getBigDecimal(String columnLabel) throws InvalidResultSetAccessException;

	/**
	 * 以布尔值形式检索当前行中指定列的值。
	 * @param columnIndex 列索引
	 * @return 表示列值的布尔值
	 * @see java.sql.ResultSet#getBoolean(int)
	 */
	boolean getBoolean(int columnIndex) throws InvalidResultSetAccessException;

	/**
	 * 以布尔值形式检索当前行中指定列的值。
	 * @param columnLabel 列标签
	 * @return 表示列值的布尔值
	 * @see java.sql.ResultSet#getBoolean(String)
	 */
	boolean getBoolean(String columnLabel) throws InvalidResultSetAccessException;

	/**
	 * 以字节形式检索当前行中指定列的值。
	 * @param columnIndex 列索引
	 * @return 表示列值的字节
	 * @see java.sql.ResultSet#getByte(int)
	 */
	byte getByte(int columnIndex) throws InvalidResultSetAccessException;

	/**
	 * 以字节形式检索当前行中指定列的值。
	 * @param columnLabel 列标签
	 * @return 表示列值的字节
	 * @see java.sql.ResultSet#getByte(String)
	 */
	byte getByte(String columnLabel) throws InvalidResultSetAccessException;

	/**
	 * 以 Date 对象的形式检索当前行中指定列的值。
	 * @param columnIndex 列索引
	 * @return 表示列值的日期对象
	 * @see java.sql.ResultSet#getDate(int)
	 */
	@Nullable Date getDate(int columnIndex) throws InvalidResultSetAccessException;

	/**
	 * 以 Date 对象的形式检索当前行中指定列的值。
	 * @param columnLabel 列标签
	 * @return 表示列值的日期对象
	 * @see java.sql.ResultSet#getDate(String)
	 */
	@Nullable Date getDate(String columnLabel) throws InvalidResultSetAccessException;

	/**
	 * 以 Date 对象的形式检索当前行中指定列的值。
	 * @param columnIndex 列索引
	 * @param cal 用于构造日期的日历
	 * @return 表示列值的日期对象
	 * @see java.sql.ResultSet#getDate(int, Calendar)
	 */
	@Nullable Date getDate(int columnIndex, Calendar cal) throws InvalidResultSetAccessException;

	/**
	 * 以 Date 对象的形式检索当前行中指定列的值。
	 * @param columnLabel 列标签
	 * @param cal 用于构造日期的日历
	 * @return 表示列值的日期对象
	 * @see java.sql.ResultSet#getDate(String, Calendar)
	 */
	@Nullable Date getDate(String columnLabel, Calendar cal) throws InvalidResultSetAccessException;

	/**
	 * 以 Double 对象形式检索当前行中指定列的值。
	 * @param columnIndex 列索引
	 * @return 表示列值的双精度对象
	 * @see java.sql.ResultSet#getDouble(int)
	 */
	double getDouble(int columnIndex) throws InvalidResultSetAccessException;

	/**
	 * 以 Double 对象形式检索当前行中指定列的值。
	 * @param columnLabel 列标签
	 * @return 表示列值的双精度对象
	 * @see java.sql.ResultSet#getDouble(String)
	 */
	double getDouble(String columnLabel) throws InvalidResultSetAccessException;

	/**
	 * 以浮点数形式检索当前行中指定列的值。
	 * @param columnIndex 列索引
	 * @return 表示列值的浮点数
	 * @see java.sql.ResultSet#getFloat(int)
	 */
	float getFloat(int columnIndex) throws InvalidResultSetAccessException;

	/**
	 * 以浮点数形式检索当前行中指定列的值。
	 * @param columnLabel 列标签
	 * @return 表示列值的浮点数
	 * @see java.sql.ResultSet#getFloat(String)
	 */
	float getFloat(String columnLabel) throws InvalidResultSetAccessException;

	/**
	 * 以 int 形式检索当前行中指定列的值。
	 * @param columnIndex 列索引
	 * @return int 表示列值
	 * @see java.sql.ResultSet#getInt(int)
	 */
	int getInt(int columnIndex) throws InvalidResultSetAccessException;

	/**
	 * 以 int 形式检索当前行中指定列的值。
	 * @param columnLabel 列标签
	 * @return int 表示列值
	 * @see java.sql.ResultSet#getInt(String)
	 */
	int getInt(String columnLabel) throws InvalidResultSetAccessException;

	/**
	 * 以 long 形式检索当前行中指定列的值。
	 * @param columnIndex 列索引
	 * @return long 代表列值
	 * @see java.sql.ResultSet#getLong(int)
	 */
	long getLong(int columnIndex) throws InvalidResultSetAccessException;

	/**
	 * 以 long 形式检索当前行中指定列的值。
	 * @param columnLabel 列标签
	 * @return long 代表列值
	 * @see java.sql.ResultSet#getLong(String)
	 */
	long getLong(String columnLabel) throws InvalidResultSetAccessException;

	/**
	 * 以字符串形式检索当前行中指定列的值（对于 NCHAR、NVARCHAR、LONGNVARCHAR 列）。
	 * @param columnIndex 列索引
	 * @return 表示列值的字符串
	 * @since 4.1.3
	 * @see java.sql.ResultSet#getNString(int)
	 */
	@Nullable String getNString(int columnIndex) throws InvalidResultSetAccessException;

	/**
	 * 以字符串形式检索当前行中指定列的值（对于 NCHAR、NVARCHAR、LONGNVARCHAR 列）。
	 * @param columnLabel 列标签
	 * @return 表示列值的字符串
	 * @since 4.1.3
	 * @see java.sql.ResultSet#getNString(String)
	 */
	@Nullable String getNString(String columnLabel) throws InvalidResultSetAccessException;

	/**
	 * 以对象形式检索当前行中指定列的值。
	 * @param columnIndex 列索引
	 * @return 表示列值的对象
	 * @see java.sql.ResultSet#getObject(int)
	 */
	@Nullable Object getObject(int columnIndex) throws InvalidResultSetAccessException;

	/**
	 * 以对象形式检索当前行中指定列的值。
	 * @param columnLabel 列标签
	 * @return 表示列值的对象
	 * @see java.sql.ResultSet#getObject(String)
	 */
	@Nullable Object getObject(String columnLabel) throws InvalidResultSetAccessException;

	/**
	 * 以对象形式检索当前行中指定列的值。
	 * @param columnIndex 列索引
	 * @param map 包含从 SQL 类型到 Java 类型的映射的 Map 对象
	 * @return 表示列值的对象
	 * @see java.sql.ResultSet#getObject(int, Map)
	 */
	@Nullable Object getObject(int columnIndex, Map<String, Class<?>> map) throws InvalidResultSetAccessException;

	/**
	 * 以对象形式检索当前行中指定列的值。
	 * @param columnLabel 列标签
	 * @param map 包含从 SQL 类型到 Java 类型的映射的 Map 对象
	 * @return 表示列值的对象
	 * @see java.sql.ResultSet#getObject(String, Map)
	 */
	@Nullable Object getObject(String columnLabel, Map<String, Class<?>> map) throws InvalidResultSetAccessException;

	/**
	 * 以对象形式检索当前行中指定列的值。
	 * @param columnIndex 列索引
	 * @param type 将指定列转换为的 Java 类型
	 * @return 表示列值的对象
	 * @since 4.1.3
	 * @see java.sql.ResultSet#getObject(int, Class)
	 */
	<T> @Nullable T getObject(int columnIndex, Class<T> type) throws InvalidResultSetAccessException;

	/**
	 * 以对象形式检索当前行中指定列的值。
	 * @param columnLabel 列标签
	 * @param type 将指定列转换为的 Java 类型
	 * @return 表示列值的对象
	 * @since 4.1.3
	 * @see java.sql.ResultSet#getObject(String, Class)
	 */
	<T> @Nullable T getObject(String columnLabel, Class<T> type) throws InvalidResultSetAccessException;

	/**
	 * 检索当前行中指定列的值作为短值。
	 * @param columnIndex 列索引
	 * @return 短表示列值
	 * @see java.sql.ResultSet#getShort(int)
	 */
	short getShort(int columnIndex) throws InvalidResultSetAccessException;

	/**
	 * 检索当前行中指定列的值作为短值。
	 * @param columnLabel 列标签
	 * @return 短表示列值
	 * @see java.sql.ResultSet#getShort(String)
	 */
	short getShort(String columnLabel) throws InvalidResultSetAccessException;

	/**
	 * 以字符串形式检索当前行中指定列的值。
	 * @param columnIndex 列索引
	 * @return 表示列值的字符串
	 * @see java.sql.ResultSet#getString(int)
	 */
	@Nullable String getString(int columnIndex) throws InvalidResultSetAccessException;

	/**
	 * 以字符串形式检索当前行中指定列的值。
	 * @param columnLabel 列标签
	 * @return 表示列值的字符串
	 * @see java.sql.ResultSet#getString(String)
	 */
	@Nullable String getString(String columnLabel) throws InvalidResultSetAccessException;

	/**
	 * 以 Time 对象的形式检索当前行中指示列的值。
	 * @param columnIndex 列索引
	 * @return 表示列值的时间对象
	 * @see java.sql.ResultSet#getTime(int)
	 */
	@Nullable Time getTime(int columnIndex) throws InvalidResultSetAccessException;

	/**
	 * 以 Time 对象的形式检索当前行中指示列的值。
	 * @param columnLabel 列标签
	 * @return 表示列值的时间对象
	 * @see java.sql.ResultSet#getTime(String)
	 */
	@Nullable Time getTime(String columnLabel) throws InvalidResultSetAccessException;

	/**
	 * 以 Time 对象的形式检索当前行中指示列的值。
	 * @param columnIndex 列索引
	 * @param cal 用于构造日期的日历
	 * @return 表示列值的时间对象
	 * @see java.sql.ResultSet#getTime(int, Calendar)
	 */
	@Nullable Time getTime(int columnIndex, Calendar cal) throws InvalidResultSetAccessException;

	/**
	 * 以 Time 对象的形式检索当前行中指示列的值。
	 * @param columnLabel 列标签
	 * @param cal 用于构造日期的日历
	 * @return 表示列值的时间对象
	 * @see java.sql.ResultSet#getTime(String, Calendar)
	 */
	@Nullable Time getTime(String columnLabel, Calendar cal) throws InvalidResultSetAccessException;

	/**
	 * 以 Timestamp 对象的形式检索当前行中指示列的值。
	 * @param columnIndex 列索引
	 * @return 表示列值的时间戳对象
	 * @see java.sql.ResultSet#getTimestamp(int)
	 */
	@Nullable Timestamp getTimestamp(int columnIndex) throws InvalidResultSetAccessException;

	/**
	 * 以 Timestamp 对象的形式检索当前行中指示列的值。
	 * @param columnLabel 列标签
	 * @return 表示列值的时间戳对象
	 * @see java.sql.ResultSet#getTimestamp(String)
	 */
	@Nullable Timestamp getTimestamp(String columnLabel) throws InvalidResultSetAccessException;

	/**
	 * 以 Timestamp 对象的形式检索当前行中指示列的值。
	 * @param columnIndex 列索引
	 * @param cal 用于构造日期的日历
	 * @return 表示列值的时间戳对象
	 * @see java.sql.ResultSet#getTimestamp(int, Calendar)
	 */
	@Nullable Timestamp getTimestamp(int columnIndex, Calendar cal) throws InvalidResultSetAccessException;

	/**
	 * 以 Timestamp 对象的形式检索当前行中指示列的值。
	 * @param columnLabel 列标签
	 * @param cal 用于构造日期的日历
	 * @return 表示列值的时间戳对象
	 * @see java.sql.ResultSet#getTimestamp(String, Calendar)
	 */
	@Nullable Timestamp getTimestamp(String columnLabel, Calendar cal) throws InvalidResultSetAccessException;


	// RowSet 导航方法

	/**
	 * 将光标移动到行集中最后一行之后的给定行号。
	 * @param row 光标应移动的行号
	 * @return true} 如果光标位于行集上，否则为 {@code false}
	 * @see java.sql.ResultSet#absolute(int)
	 */
	boolean absolute(int row) throws InvalidResultSetAccessException;

	/**
	 * 将光标移至该行集的末尾。
	 * @see java.sql.ResultSet#afterLast()
	 */
	void afterLast() throws InvalidResultSetAccessException;

	/**
	 * 将光标移动到该行集的前面，就在第一行之前。
	 * @see java.sql.ResultSet#beforeFirst()
	 */
	void beforeFirst() throws InvalidResultSetAccessException;

	/**
	 * 将光标移至该行集的第一行。
	 * @return true} 如果光标位于有效行上，否则为 {@code false}
	 * @see java.sql.ResultSet#first()
	 */
	boolean first() throws InvalidResultSetAccessException;

	/**
	 * 检索当前行号。
	 * @return 当前行号
	 * @see java.sql.ResultSet#getRow()
	 */
	int getRow() throws InvalidResultSetAccessException;

	/**
	 * 检索光标是否位于该行集的最后一行之后。
	 * @return true} 如果光标位于最后一行之后，否则为 {@code false}
	 * @see java.sql.ResultSet#isAfterLast()
	 */
	boolean isAfterLast() throws InvalidResultSetAccessException;

	/**
	 * 检索光标是否位于该行集的第一行之前。
	 * @return true} 如果光标位于第一行之前，否则为 {@code false}
	 * @see java.sql.ResultSet#isBeforeFirst()
	 */
	boolean isBeforeFirst() throws InvalidResultSetAccessException;

	/**
	 * 检索光标是否位于该行集的第一行。
	 * @return true} 如果光标位于第一行之后，否则为 {@code false}
	 * @see java.sql.ResultSet#isFirst()
	 */
	boolean isFirst() throws InvalidResultSetAccessException;

	/**
	 * 检索光标是否位于该行集的最后一行。
	 * @return true} 如果光标位于最后一行之后，否则为 {@code false}
	 * @see java.sql.ResultSet#isLast()
	 */
	boolean isLast() throws InvalidResultSetAccessException;

	/**
	 * 将光标移至该行集的最后一行。
	 * @return true} 如果光标位于有效行上，否则为 {@code false}
	 * @see java.sql.ResultSet#last()
	 */
	boolean last() throws InvalidResultSetAccessException;

	/**
	 * 将光标移至下一行。
	 * @return true} 如果新行有效，则 {@code false} 如果没有更多行
	 * @see java.sql.ResultSet#next()
	 */
	boolean next() throws InvalidResultSetAccessException;

	/**
	 * 将光标移至上一行。
	 * @return true} 如果新行有效，则 {@code false} 如果它不在行集内
	 * @see java.sql.ResultSet#previous()
	 */
	boolean previous() throws InvalidResultSetAccessException;

	/**
	 * 将光标移动相对行数（正数或负数）。
	 * @return true} 如果光标位于一行，否则为 {@code false}
	 * @see java.sql.ResultSet#relative(int)
	 */
	boolean relative(int rows) throws InvalidResultSetAccessException;

	/**
	 * 报告最后读取的列是否具有 SQL {@code NULL} 值。 <p>请注意，您必须首先调用其中一个 getter 方法，然后再调用 {@code wasNull()} 方法
	 * 。
	 * @return true} 如果最近检索到的列是 SQL {@code NULL}，否则为 {@code false}
	 * @see java.sql.ResultSet#wasNull()
	 */
	boolean wasNull() throws InvalidResultSetAccessException;

}
