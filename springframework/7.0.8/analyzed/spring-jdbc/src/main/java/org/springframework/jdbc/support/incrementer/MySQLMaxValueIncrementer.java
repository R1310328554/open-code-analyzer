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
 * {@link DataFieldMaxValueIncrementer} 增加给定 MySQL 表的最大值，相当于自动增量列。注意：如果您使用此类，您的 MySQL 键列应
 * <i>NOT</i> 自动递增，因为序列表会执行此操作。
 * <p>序列保存在一个表中；每个表应该有一个需要自动生成键的序列表。序列表使用的存储引擎可以是 MYISAM 或 INNODB，因为序列是使用单独的连接进行分配的，而不会受到可能
 * 正在进行的任何其他事务的影响。
 * <p>示例：
 * <pre class="code"> 创建表选项卡（id int unsigned not null 主键，text
 * varchar(100)）；创建表tab_sequence（值int不为空）；插入 tab_sequence 值(0);</pre>
 * <p> 如果设置了 {@code cacheSize}，则无需查询数据库即可提供中间值。如果服务器或您的应用程序停止或崩溃或事务回滚，则永远不会提供未使用的值。因此，编号中的最
 * 大孔尺寸是 {@code cacheSize} 的值。
 * <p>可以通过将“useNewConnection”属性设置为 false 来避免为增量器获取新连接。在这种情况下，您 <i>MUST</i> 在定义增量表时使用非事务性存储引
 * 擎（如 MYISAM）。
 * <p>注意，{@code MySQLMaxValueIncrementer}与<a
 * href="https://dev.mysql.com/doc/refman/8.0/en/mysql-tips.html#safe-updates">MySQL安全更新模式</a>兼容。
 * @author Jean-Pierre Pawlak
 * @author Thomas Risberg
 * @author Juergen Hoeller
 * @author Sam Brannen
 */
public class MySQLMaxValueIncrementer extends AbstractColumnMaxValueIncrementer {

	/**
	 */
	private static final String VALUE_SQL = "select last_insert_id()";

	/**
	 */
	private long nextId = 0;

	/**
	 */
	private long maxId = 0;

	/**
	 */
	private boolean useNewConnection = true;


	/**
	 * bean 属性样式使用的默认构造函数。
	 * @see #setDataSource
	 * @see #setIncrementerName
	 * @see #setColumnName
	 */
	public MySQLMaxValueIncrementer() {
	}

	/**
	 * 方便构造函数。
	 * @param dataSource 要使用的数据源
	 * @param incrementerName 要使用的序列表的名称
	 * @param columnName 序列表中要使用的列的名称
	 */
	public MySQLMaxValueIncrementer(DataSource dataSource, String incrementerName, String columnName) {
		super(dataSource, incrementerName, columnName);
	}


	/**
	 * 设置增量器是否使用新连接。 <p>{@code true} 对于支持事务存储引擎是必要的，使用隔离的单独事务进行增量操作。如果序列表的存储引擎是非事务性的（如 MYISAM），
	 * {@code false} 就足够了，避免了为增量操作获取额外的 {@code Connection} 的工作。 <p>默认为{@code true}。
	 * @since 4.3.6
	 * @see DataSource#getConnection()
	 */
	public void setUseNewConnection(boolean useNewConnection) {
		this.useNewConnection = useNewConnection;
	}


	/**
	 * 获取 Next Key（`NextKey`）。
	 */
	@Override
	protected synchronized long getNextKey() throws DataAccessException {
		if (this.maxId == this.nextId) {
			/*
			* If useNewConnection is true, then we obtain a non-managed connection so our modifications
			* are handled in a separate transaction. If it is false, then we use the current transaction's
			* connection relying on the use of a non-transactional storage engine like MYISAM for the
			* incrementer table. We also use straight JDBC code because we need to make sure that the insert
			* and select are performed on the same connection (otherwise we can't be sure that last_insert_id()
			* returned the correct value).
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
				// 增加序列列...
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
