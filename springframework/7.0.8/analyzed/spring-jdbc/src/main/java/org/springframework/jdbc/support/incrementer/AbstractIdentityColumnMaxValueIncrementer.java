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

import org.jspecify.annotations.Nullable;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.util.Assert;

/**
 * 基于类序列表中 identity 列的 {@link DataFieldMaxValueIncrementer} 实现的抽象基类。
 *
 * @author Juergen Hoeller
 * @author Thomas Risberg
 * @since 4.1.2
 */
public abstract class AbstractIdentityColumnMaxValueIncrementer extends AbstractColumnMaxValueIncrementer {

	private boolean deleteSpecificValues = false;

	/** 当前值缓存。 */
	private long @Nullable [] valueCache;

	/** 从值缓存中提供的下一个 id 索引。 */
	private int nextValueIndex = -1;


	/**
	 * Bean 属性风格使用的默认构造器。
	 * @see #setDataSource
	 * @see #setIncrementerName
	 * @see #setColumnName
	 */
	public AbstractIdentityColumnMaxValueIncrementer() {
	}

	public AbstractIdentityColumnMaxValueIncrementer(DataSource dataSource, String incrementerName, String columnName) {
		super(dataSource, incrementerName, columnName);
	}


	/**
	 * 指定是删除当前最大键值以下的整个范围（{@code false}，默认），
	 * 还是仅删除本次生成的值（{@code true}）。
	 * 前者使用 where 范围子句，后者使用 in 子句（从最小值减 1 起），仅保留最大值。
	 */
	public void setDeleteSpecificValues(boolean deleteSpecificValues) {
		this.deleteSpecificValues = deleteSpecificValues;
	}

	/**
	 * 返回是否删除当前最大键值以下的整个范围（{@code false}，默认），
	 * 还是仅删除本次生成的值（{@code true}）。
	 */
	public boolean isDeleteSpecificValues() {
		return this.deleteSpecificValues;
	}


	@Override
	protected synchronized long getNextKey() throws DataAccessException {
		if (this.nextValueIndex < 0 || this.nextValueIndex >= getCacheSize()) {
			/*
			* 须使用原生 JDBC，确保 insert 与 select 在同一连接上执行
			* （否则无法保证 @@identity 返回正确值）
			*/
			Connection con = DataSourceUtils.getConnection(getDataSource());
			Statement stmt = null;
			try {
				stmt = con.createStatement();
				DataSourceUtils.applyTransactionTimeout(stmt, getDataSource());
				this.valueCache = new long[getCacheSize()];
				this.nextValueIndex = 0;
				for (int i = 0; i < getCacheSize(); i++) {
					stmt.executeUpdate(getIncrementStatement());
					ResultSet rs = stmt.executeQuery(getIdentityStatement());
					try {
						if (!rs.next()) {
							throw new DataAccessResourceFailureException("Identity statement failed after inserting");
						}
						this.valueCache[i] = rs.getLong(1);
					}
					finally {
						JdbcUtils.closeResultSet(rs);
					}
				}
				stmt.executeUpdate(getDeleteStatement(this.valueCache));
			}
			catch (SQLException ex) {
				throw new DataAccessResourceFailureException("Could not increment identity", ex);
			}
			finally {
				JdbcUtils.closeStatement(stmt);
				DataSourceUtils.releaseConnection(con, getDataSource());
			}
		}
		Assert.state(this.valueCache != null, "The cache of values can't be null");
		return this.valueCache[this.nextValueIndex++];
	}


	/**
	 * 用于递增"序列"值的语句。
	 * @return 要使用的 SQL 语句
	 */
	protected abstract String getIncrementStatement();

	/**
	 * 用于获取当前 identity 值的语句。
	 * @return 要使用的 SQL 语句
	 */
	protected abstract String getIdentityStatement();

	/**
	 * 用于清理"序列"值的语句。
	 * <p>默认实现根据 {@link #isDeleteSpecificValues()} 设置，
	 * 删除当前最大值以下的整个范围，或本次生成的值（从最小值减 1 起，保留最大值）。
	 * @param values 当前生成的键值（数量对应 {@link #getCacheSize()}）
	 * @return 要使用的 SQL 语句
	 */
	protected String getDeleteStatement(long[] values) {
		StringBuilder sb = new StringBuilder(64);
		sb.append("delete from ").append(getIncrementerName()).append(" where ").append(getColumnName());
		if (isDeleteSpecificValues()) {
			sb.append(" in (").append(values[0] - 1);
			for (int i = 0; i < values.length - 1; i++) {
				sb.append(", ").append(values[i]);
			}
			sb.append(')');
		}
		else {
			long maxValue = values[values.length - 1];
			sb.append(" < ").append(maxValue);
		}
		return sb.toString();
	}

}
