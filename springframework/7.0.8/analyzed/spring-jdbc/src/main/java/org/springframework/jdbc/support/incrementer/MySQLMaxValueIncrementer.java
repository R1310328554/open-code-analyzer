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

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.JdbcUtils;

/**
 * 递增给定 MySQL 表中相当于自增列的最大值的 {@link DataFieldMaxValueIncrementer}。
 * 注意：使用本类时，MySQL 主键列<i>不应</i>设为 auto-increment，序列表负责生成键值。
 *
 * <p>序列保存在一张表中；每个需要自动生成主键的表应有一张对应的序列表。
 * 序列表可使用 MYISAM 或 INNODB 存储引擎，因为序列通过独立连接分配，
 * 不受其他进行中事务的影响。
 *
 * <p>示例：
 *
 * <pre class="code">
 * create table tab (id int unsigned not null primary key, text varchar(100));
 * create table tab_sequence (value int not null);
 * insert into tab_sequence values(0);</pre>
 *
 * <p>若设置了 {@code cacheSize}，中间值将不经查询数据库直接分配。
 * 若服务器或应用停止、崩溃，或事务回滚，未使用的值将永远不会被分配。
 * 因此编号中可能出现的最大空洞大小等于 {@code cacheSize} 的值。
 *
 * <p>可将 "useNewConnection" 属性设为 false 以避免为递增器获取新连接。
 * 此时<i>必须</i>为序列表使用 MYISAM 等非事务性存储引擎。
 *
 * <p>{@code MySQLMaxValueIncrementer} 兼容
 * <a href="https://dev.mysql.com/doc/refman/8.0/en/mysql-tips.html#safe-updates">MySQL 安全更新模式</a>。
 *
 * @author Jean-Pierre Pawlak
 * @author Thomas Risberg
 * @author Juergen Hoeller
 * @author Sam Brannen
 */
public class MySQLMaxValueIncrementer extends AbstractColumnMaxValueIncrementer {

	/** 用于检索新序列值的 SQL 字符串。 */
	private static final String VALUE_SQL = "select last_insert_id()";

	/** 下一个待分配的 id。 */
	private long nextId = 0;

	/** 当前可分配的最大 id。 */
	private long maxId = 0;

	/** 是否为递增器使用新连接。 */
	private boolean useNewConnection = true;


	/**
	 * 允许作为 JavaBean 使用的默认构造器。
	 * @see #setDataSource
	 * @see #setIncrementerName
	 * @see #setColumnName
	 */
	public MySQLMaxValueIncrementer() {
	}

	/**
	 * 便捷构造器。
	 * @param dataSource 要使用的 DataSource
	 * @param incrementerName 要使用的序列表名
	 * @param columnName 序列表中要使用的列名
	 */
	public MySQLMaxValueIncrementer(DataSource dataSource, String incrementerName, String columnName) {
		super(dataSource, incrementerName, columnName);
	}


	/**
	 * 设置是否为递增器使用新连接。
	 * <p>{@code true} 用于支持事务性存储引擎，递增操作在隔离的独立事务中执行。
	 * 若序列表使用 MYISAM 等非事务性存储引擎，{@code false} 即可，
	 * 无需为递增操作额外获取 {@code Connection}。
	 * <p>默认为 {@code true}。
	 * @since 4.3.6
	 * @see DataSource#getConnection()
	 */
	public void setUseNewConnection(boolean useNewConnection) {
		this.useNewConnection = useNewConnection;
	}


	@Override
	protected synchronized long getNextKey() throws DataAccessException {
		if (this.maxId == this.nextId) {
			/*
			* 若 useNewConnection 为 true，则获取非托管连接，使修改在独立事务中处理。
			* 若为 false，则使用当前事务的连接，依赖 MYISAM 等非事务性存储引擎的序列表。
			* 同时使用原生 JDBC 代码，确保 insert 与 select 在同一连接上执行
			* （否则无法保证 last_insert_id() 返回正确值）。
			*/
			Connection con = null;
			Statement stmt = null;
			boolean mustRestoreAutoCommit = false;
			try {
				if (this.useNewConnection) {
					con = getDataSource().getConnection();
					if (con.getAutoCommit()) {
						mustRestoreAutoCommit = true;
						con.setAutoCommit(false);
					}
				}
				else {
					con = DataSourceUtils.getConnection(getDataSource());
				}
				stmt = con.createStatement();
				if (!this.useNewConnection) {
					DataSourceUtils.applyTransactionTimeout(stmt, getDataSource());
				}
				// 递增序列列...
				String columnName = getColumnName();
				try {
					stmt.executeUpdate("update " + getIncrementerName() + " set " + columnName +
							" = last_insert_id(" + columnName + " + " + getCacheSize() + ") limit 1");
				}
				catch (SQLException ex) {
					throw new DataAccessResourceFailureException("Could not increment " + columnName + " for " +
							getIncrementerName() + " sequence table", ex);
				}
				// 检索序列列的新最大值...
				ResultSet rs = stmt.executeQuery(VALUE_SQL);
				try {
					if (!rs.next()) {
						throw new DataAccessResourceFailureException("last_insert_id() failed after executing an update");
					}
					this.maxId = rs.getLong(1);
				}
				finally {
					JdbcUtils.closeResultSet(rs);
				}
				this.nextId = this.maxId - getCacheSize() + 1;
			}
			catch (SQLException ex) {
				throw new DataAccessResourceFailureException("Could not obtain last_insert_id()", ex);
			}
			finally {
				JdbcUtils.closeStatement(stmt);
				if (con != null) {
					if (this.useNewConnection) {
						try {
							con.commit();
							if (mustRestoreAutoCommit) {
								con.setAutoCommit(true);
							}
						}
						catch (SQLException ignore) {
							throw new DataAccessResourceFailureException(
									"Unable to commit new sequence value changes for " + getIncrementerName());
						}
						JdbcUtils.closeConnection(con);
					}
					else {
						DataSourceUtils.releaseConnection(con, getDataSource());
					}
				}
			}
		}
		else {
			this.nextId++;
		}
		return this.nextId;
	}

}
