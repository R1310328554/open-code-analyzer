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
 * Spring 的 {@link SqlRowSet} 的元数据接口，类似于 JDBC 的 {@link java.sql.ResultSetMetaData}。
 * <p> 与标准 JDBC ResultSetMetaData 的主要区别在于，这里永远不会抛出 {@link java.sql.SQLException}。这允许使用
 * SqlRowSetMetaData 而无需处理已检查的异常。 SqlRowSetMetaData 将抛出 Spring 的 {@link
 * InvalidResultSetAccessException}（在适当的时候）。
 * @author Thomas Risberg
 * @author Juergen Hoeller
 * @since 1.2
 * @see SqlRowSet#getMetaData()
 * @see java.sql.ResultSetMetaData
 * @see org.springframework.jdbc.InvalidResultSetAccessException
 */
public interface SqlRowSetMetaData {

	/**
	 * 检索用作指定列的源的表的目录名称。
	 * @param columnIndex 列的索引
	 * @return 目录名称
	 * @see java.sql.ResultSetMetaData#getCatalogName(int)
	 */
	String getCatalogName(int columnIndex) throws InvalidResultSetAccessException;

	/**
	 * 检索指定列将映射到的完全限定类。
	 * @param columnIndex 列的索引
	 * @return 类名作为字符串
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
	 * 返回结果集表示的表的列名。
	 * @return 列名
	 */
	String[] getColumnNames() throws InvalidResultSetAccessException;

	/**
	 * 检索指定列的最大宽度。
	 * @param columnIndex 列的索引
	 * @return 列宽
	 * @see java.sql.ResultSetMetaData#getColumnDisplaySize(int)
	 */
	int getColumnDisplaySize(int columnIndex) throws InvalidResultSetAccessException;

	/**
	 * 检索指定列的建议列标题。
	 * @param columnIndex 列的索引
	 * @return 栏目标题
	 * @see java.sql.ResultSetMetaData#getColumnLabel(int)
	 */
	String getColumnLabel(int columnIndex) throws InvalidResultSetAccessException;

	/**
	 * 检索指定列的列名。
	 * @param columnIndex 列的索引
	 * @return 列名
	 * @see java.sql.ResultSetMetaData#getColumnName(int)
	 */
	String getColumnName(int columnIndex) throws InvalidResultSetAccessException;

	/**
	 * 检索指定列的 SQL 类型代码。
	 * @param columnIndex 列的索引
	 * @return SQL 类型代码
	 * @see java.sql.ResultSetMetaData#getColumnType(int)
	 * @see java.sql.Types
	 */
	int getColumnType(int columnIndex) throws InvalidResultSetAccessException;

	/**
	 * 检索指定列的 DBMS 特定类型名称。
	 * @param columnIndex 列的索引
	 * @return 类型名称
	 * @see java.sql.ResultSetMetaData#getColumnTypeName(int)
	 */
	String getColumnTypeName(int columnIndex) throws InvalidResultSetAccessException;

	/**
	 * 检索指定列的精度。
	 * @param columnIndex 列的索引
	 * @return 精确
	 * @see java.sql.ResultSetMetaData#getPrecision(int)
	 */
	int getPrecision(int columnIndex) throws InvalidResultSetAccessException;

	/**
	 * 检索指定列的比例。
	 * @param columnIndex 列的索引
	 * @return 规模
	 * @see java.sql.ResultSetMetaData#getScale(int)
	 */
	int getScale(int columnIndex) throws InvalidResultSetAccessException;

	/**
	 * 检索用作指定列的源的表的架构名称。
	 * @param columnIndex 列的索引
	 * @return 模式名称
	 * @see java.sql.ResultSetMetaData#getSchemaName(int)
	 */
	String getSchemaName(int columnIndex) throws InvalidResultSetAccessException;

	/**
	 * 检索用作指定列的源的表的名称。
	 * @param columnIndex 列的索引
	 * @return 表名
	 * @see java.sql.ResultSetMetaData#getTableName(int)
	 */
	String getTableName(int columnIndex) throws InvalidResultSetAccessException;

	/**
	 * 指示指定列的大小写是否重要。
	 * @param columnIndex 列的索引
	 * @return 如果列区分大小写，则 false 否则
	 * @see java.sql.ResultSetMetaData#isCaseSensitive(int)
	 */
	boolean isCaseSensitive(int columnIndex) throws InvalidResultSetAccessException;

	/**
	 * 指示指定列是否包含货币值。
	 * @param columnIndex 列的索引
	 * @return 如果该值是货币值，则 false 否则
	 * @see java.sql.ResultSetMetaData#isCurrency(int)
	 */
	boolean isCurrency(int columnIndex) throws InvalidResultSetAccessException;

	/**
	 * 指示指定列是否包含有符号数字。
	 * @param columnIndex 列的索引
	 * @return 如果该列包含有符号数字，则为 false 否则
	 * @see java.sql.ResultSetMetaData#isSigned(int)
	 */
	boolean isSigned(int columnIndex) throws InvalidResultSetAccessException;

}
