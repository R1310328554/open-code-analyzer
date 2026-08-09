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

import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.jspecify.annotations.Nullable;

import org.springframework.jdbc.InvalidResultSetAccessException;

/**
 * Spring 的 {@link SqlRowSetMetaData} 接口的默认实现，包装 {@link java.sql.ResultSetMetaData}
 * 实例，捕获任何 {@link SQLException SQLExceptions} 并将它们转换为相应的 Spring {@link
 * InvalidResultSetAccessException}。
 * <p> 由 {@link ResultSetWrappingSqlRowSet} 使用。
 * @author Thomas Risberg
 * @author Juergen Hoeller
 * @since 1.2
 * @see ResultSetWrappingSqlRowSet#getMetaData()
 */
public class ResultSetWrappingSqlRowSetMetaData implements SqlRowSetMetaData {

	/** 结果相关状态（`resultSetMetaData`）。 */
	private final ResultSetMetaData resultSetMetaData;

	/** 名称相关状态（`columnNames`）。 */
	private String @Nullable [] columnNames;


	/**
	 * 为给定的 ResultSetMetaData 实例创建一个新的 ResultSetWrappingSqlRowSetMetaData 对象。
	 * @param resultSetMetaData 要包装的断开连接的 ResultSetMetaData 实例（通常是 {@code javax.sql.RowSetMetaData} 实例）
	 * @see java.sql.ResultSet#getMetaData
	 * @see javax.sql.RowSetMetaData
	 * @see ResultSetWrappingSqlRowSet#getMetaData
	 */
	public ResultSetWrappingSqlRowSetMetaData(ResultSetMetaData resultSetMetaData) {
		this.resultSetMetaData = resultSetMetaData;
	}


	/**
	 * 获取 Catalog Name（`CatalogName`）。
	 */
	@Override
	public String getCatalogName(int column) throws InvalidResultSetAccessException {
		try {
			return this.resultSetMetaData.getCatalogName(column);
		}
		catch (SQLException se) {
			throw new InvalidResultSetAccessException(se);
		}
	}

	/**
	 * 获取 Column Class Name（`ColumnClassName`）。
	 */
	@Override
	public String getColumnClassName(int column) throws InvalidResultSetAccessException {
		try {
			return this.resultSetMetaData.getColumnClassName(column);
		}
		catch (SQLException se) {
			throw new InvalidResultSetAccessException(se);
		}
	}

	/**
	 * 获取 Column Count（`ColumnCount`）。
	 */
	@Override
	public int getColumnCount() throws InvalidResultSetAccessException {
		try {
			return this.resultSetMetaData.getColumnCount();
		}
		catch (SQLException se) {
			throw new InvalidResultSetAccessException(se);
		}
	}

	/**
	 * 获取 Column Names（`ColumnNames`）。
	 */
	@Override
	public String[] getColumnNames() throws InvalidResultSetAccessException {
		if (this.columnNames == null) {
			this.columnNames = new String[getColumnCount()];
			for (int i = 0; i < getColumnCount(); i++) {
				this.columnNames[i] = getColumnName(i + 1);
			}
		}
		return this.columnNames;
	}

	/**
	 * 获取 Column Display Size（`ColumnDisplaySize`）。
	 */
	@Override
	public int getColumnDisplaySize(int column) throws InvalidResultSetAccessException {
		try {
			return this.resultSetMetaData.getColumnDisplaySize(column);
		}
		catch (SQLException se) {
			throw new InvalidResultSetAccessException(se);
		}
	}

	/**
	 * 获取 Column Label（`ColumnLabel`）。
	 */
	@Override
	public String getColumnLabel(int column) throws InvalidResultSetAccessException {
		try {
			return this.resultSetMetaData.getColumnLabel(column);
		}
		catch (SQLException se) {
			throw new InvalidResultSetAccessException(se);
		}
	}

	/**
	 * 获取 Column Name（`ColumnName`）。
	 */
	@Override
	public String getColumnName(int column) throws InvalidResultSetAccessException {
		try {
			return this.resultSetMetaData.getColumnName(column);
		}
		catch (SQLException se) {
			throw new InvalidResultSetAccessException(se);
		}
	}

	/**
	 * 获取 Column Type（`ColumnType`）。
	 */
	@Override
	public int getColumnType(int column) throws InvalidResultSetAccessException {
		try {
			return this.resultSetMetaData.getColumnType(column);
		}
		catch (SQLException se) {
			throw new InvalidResultSetAccessException(se);
		}
	}

	/**
	 * 获取 Column Type Name（`ColumnTypeName`）。
	 */
	@Override
	public String getColumnTypeName(int column) throws InvalidResultSetAccessException {
		try {
			return this.resultSetMetaData.getColumnTypeName(column);
		}
		catch (SQLException se) {
			throw new InvalidResultSetAccessException(se);
		}
	}

	/**
	 * 获取 Precision（`Precision`）。
	 */
	@Override
	public int getPrecision(int column) throws InvalidResultSetAccessException {
		try {
			return this.resultSetMetaData.getPrecision(column);
		}
		catch (SQLException se) {
			throw new InvalidResultSetAccessException(se);
		}
	}

	/**
	 * 获取 Scale（`Scale`）。
	 */
	@Override
	public int getScale(int column) throws InvalidResultSetAccessException {
		try {
			return this.resultSetMetaData.getScale(column);
		}
		catch (SQLException se) {
			throw new InvalidResultSetAccessException(se);
		}
	}

	/**
	 * 获取 Schema Name（`SchemaName`）。
	 */
	@Override
	public String getSchemaName(int column) throws InvalidResultSetAccessException {
		try {
			return this.resultSetMetaData.getSchemaName(column);
		}
		catch (SQLException se) {
			throw new InvalidResultSetAccessException(se);
		}
	}

	/**
	 * 获取 Table Name（`TableName`）。
	 */
	@Override
	public String getTableName(int column) throws InvalidResultSetAccessException {
		try {
			return this.resultSetMetaData.getTableName(column);
		}
		catch (SQLException se) {
			throw new InvalidResultSetAccessException(se);
		}
	}

	/**
	 * 判断是否 Case Sensitive。
	 */
	@Override
	public boolean isCaseSensitive(int column) throws InvalidResultSetAccessException {
		try {
			return this.resultSetMetaData.isCaseSensitive(column);
		}
		catch (SQLException se) {
			throw new InvalidResultSetAccessException(se);
		}
	}

	/**
	 * 判断是否 Currency。
	 */
	@Override
	public boolean isCurrency(int column) throws InvalidResultSetAccessException {
		try {
			return this.resultSetMetaData.isCurrency(column);
		}
		catch (SQLException se) {
			throw new InvalidResultSetAccessException(se);
		}
	}

	/**
	 * 判断是否 Signed。
	 */
	@Override
	public boolean isSigned(int column) throws InvalidResultSetAccessException {
		try {
			return this.resultSetMetaData.isSigned(column);
		}
		catch (SQLException se) {
			throw new InvalidResultSetAccessException(se);
		}
	}

}
