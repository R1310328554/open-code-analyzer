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

package org.springframework.jdbc.support.incrementer;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.JdbcUtils;

/**
 * {@link DataFieldMaxValueIncrementer} 使用 SQLite {@code select max(rowid)}
 * 查询来增加给定表的最大值，相当于自动增量列。
 * @author Luke Taylor
 * @author Juergen Hoeller
 * @since 7.0
 */
public class SqliteMaxValueIncrementer extends AbstractColumnMaxValueIncrementer {

	/**
	 * bean 属性样式使用的默认构造函数。
	 * @see #setDataSource
	 * @see #setIncrementerName
	 * @see #setColumnName
	 */
	public SqliteMaxValueIncrementer() {
	}

	/**
	 * 方便构造函数。
	 * @param dataSource 要使用的数据源
	 * @param incrementerName 要使用的序列/表的名称
	 * @param columnName 序列表中要使用的列的名称
	 */
	public SqliteMaxValueIncrementer(DataSource dataSource, String incrementerName, String columnName) {
		super(dataSource, incrementerName, columnName);
	}


	/**
	 * 获取 Next Key（`NextKey`）。
	 */
	@Override
	protected long getNextKey() {
		Connection con = DataSourceUtils.getConnection(getDataSource());
		Statement stmt = null;
		try {
			stmt = con.createStatement();
			DataSourceUtils.applyTransactionTimeout(stmt, getDataSource());
			stmt.executeUpdate("insert into " + getIncrementerName() + " values(null)");
			ResultSet rs = stmt.executeQuery("select max(rowid) from " + getIncrementerName());
			if (!rs.next()) {
				throw new DataAccessResourceFailureException("rowid query failed after executing an update");
			}
			long nextKey = rs.getLong(1);
			stmt.executeUpdate("delete from " + getIncrementerName() + " where " + getColumnName() + " < " + nextKey);
			return nextKey;
		}
		catch (SQLException ex) {
			throw new DataAccessResourceFailureException("Could not obtain rowid", ex);
		}
		finally {
			JdbcUtils.closeStatement(stmt);
			DataSourceUtils.releaseConnection(con, getDataSource());
		}
	}

}
