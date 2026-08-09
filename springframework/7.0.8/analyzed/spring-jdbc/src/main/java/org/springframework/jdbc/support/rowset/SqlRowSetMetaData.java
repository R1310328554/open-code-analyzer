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

import org.springframework.jdbc.InvalidResultSetAccessException;

/**
 * Spring {@link SqlRowSet} 的元数据接口，类似于 JDBC 的
 * {@link java.sql.ResultSetMetaData}。
 *
 * <p>与标准 JDBC ResultSetMetaData 的主要区别在于此处从不抛出
 * {@link java.sql.SQLException}，因此使用 SqlRowSetMetaData 时
 * 无需处理受检异常；在适当时机会抛出 Spring 的
 * {@link InvalidResultSetAccessException}。
 *
 * @author Thomas Risberg
 * @author Juergen Hoeller
 * @since 1.2
 * @see SqlRowSet#getMetaData()
 * @see java.sql.ResultSetMetaData
 * @see org.springframework.jdbc.InvalidResultSetAccessException
 */
public interface SqlRowSetMetaData {

	/**
	 * 检索指定列来源表的 catalog 名称。
	 * @param columnIndex 列索引
	 * @return catalog 名称
	 * @see java.sql.ResultSetMetaData#getCatalogName(int)
	 */
	String getCatalogName(int columnIndex) throws InvalidResultSetAccessException;

	/**
	 * 检索指定列将映射到的完全限定类名。
	 * @param columnIndex 列索引
	 * @return 类名字符串
	 * @see java.sql.ResultSetMetaData#getColumnClassName(int)
	 */
	String getColumnClassName(int columnIndex) throws InvalidResultSetAccessException;

	/**
	 * 检索 RowSet 中的列数。
	 * @return 列数
	 * @see java.sql.ResultSetMetaData#getColumnCount()
	 */
	int getColumnCount() throws InvalidResultSetAccessException;

	/**
	 * 返回结果集所代表表的列名。
	 * @return 列名数组
	 */
	String[] getColumnNames() throws InvalidResultSetAccessException;

	/**
	 * 检索指定列的最大显示宽度。
	 * @param columnIndex 列索引
	 * @return 列宽度
	 * @see java.sql.ResultSetMetaData#getColumnDisplaySize(int)
	 */
	int getColumnDisplaySize(int columnIndex) throws InvalidResultSetAccessException;

	/**
	 * 检索指定列的建议列标题。
	 * @param columnIndex 列索引
	 * @return 列标题
	 * @see java.sql.ResultSetMetaData#getColumnLabel(int)
	 */
	String getColumnLabel(int columnIndex) throws InvalidResultSetAccessException;

	/**
	 * 检索指定列的列名。
	 * @param columnIndex 列索引
	 * @return 列名
	 * @see java.sql.ResultSetMetaData#getColumnName(int)
	 */
	String getColumnName(int columnIndex) throws InvalidResultSetAccessException;

	/**
	 * 检索指定列的 SQL 类型代码。
	 * @param columnIndex 列索引
	 * @return SQL 类型代码
	 * @see java.sql.ResultSetMetaData#getColumnType(int)
	 * @see java.sql.Types
	 */
	int getColumnType(int columnIndex) throws InvalidResultSetAccessException;

	/**
	 * 检索指定列的 DBMS 特定类型名。
	 * @param columnIndex 列索引
	 * @return 类型名
	 * @see java.sql.ResultSetMetaData#getColumnTypeName(int)
	 */
	String getColumnTypeName(int columnIndex) throws InvalidResultSetAccessException;

	/**
	 * 检索指定列的精度。
	 * @param columnIndex 列索引
	 * @return 精度
	 * @see java.sql.ResultSetMetaData#getPrecision(int)
	 */
	int getPrecision(int columnIndex) throws InvalidResultSetAccessException;

	/**
	 * 检索指定列的小数位数。
	 * @param columnIndex 列索引
	 * @return 小数位数
	 * @see java.sql.ResultSetMetaData#getScale(int)
	 */
	int getScale(int columnIndex) throws InvalidResultSetAccessException;

	/**
	 * 检索指定列来源表的 schema 名称。
	 * @param columnIndex 列索引
	 * @return schema 名称
	 * @see java.sql.ResultSetMetaData#getSchemaName(int)
	 */
	String getSchemaName(int columnIndex) throws InvalidResultSetAccessException;

	/**
	 * 检索指定列来源表的名称。
	 * @param columnIndex 列索引
	 * @return 表名
	 * @see java.sql.ResultSetMetaData#getTableName(int)
	 */
	String getTableName(int columnIndex) throws InvalidResultSetAccessException;

	/**
	 * 指示指定列的大小写是否敏感。
	 * @param columnIndex 列索引
	 * @return 若列大小写敏感则为 true，否则为 false
	 * @see java.sql.ResultSetMetaData#isCaseSensitive(int)
	 */
	boolean isCaseSensitive(int columnIndex) throws InvalidResultSetAccessException;

	/**
	 * 指示指定列是否包含货币值。
	 * @param columnIndex 列索引
	 * @return 若为货币值则为 true，否则为 false
	 * @see java.sql.ResultSetMetaData#isCurrency(int)
	 */
	boolean isCurrency(int columnIndex) throws InvalidResultSetAccessException;

	/**
	 * 指示指定列是否包含有符号数。
	 * @param columnIndex 列索引
	 * @return 若列包含有符号数则为 true，否则为 false
	 * @see java.sql.ResultSetMetaData#isSigned(int)
	 */
	boolean isSigned(int columnIndex) throws InvalidResultSetAccessException;

}
