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

/* ===== [OCA 中文解析] =====
class MySQLMaxValueIncrementer — 意图说明

class `MySQLMaxValueIncrementer`：请结合所属模块与调用方理解其在整体架构中的职责。；源文件: `spring-jdbc/src/main/java/org/springframework/jdbc/support/incrementer/MySQLMaxValueIncrementer.java`

（本注释由 open-code-analyzer 生成，置于原有文档注释之前）
===== [OCA 中文解析结束] ===== */
/**
 * {@link DataFieldMaxValueIncrementer} that increments the maximum value of a given MySQL table
 * with the equivalent of an auto-increment column. Note: If you use this class, your MySQL
 * key column should <i>NOT</i> be auto-increment, as the sequence table does the job.
 *
 * <p>The sequence is kept in a table; there should be one sequence table per
 * table that needs an auto-generated key. The storage engine used by the sequence table
 * can be MYISAM or INNODB since the sequences are allocated using a separate connection
 * without being affected by any other transactions that might be in progress.
 *
 * <p>Example:
 *
 * <pre class="code">
 * create table tab (id int unsigned not null primary key, text varchar(100));
 * create table tab_sequence (value int not null);
 * insert into tab_sequence values(0);</pre>
 *
 * <p>If {@code cacheSize} is set, the intermediate values are served without querying the
 * database. If the server or your application is stopped or crashes or a transaction
 * is rolled back, the unused values will never be served. The maximum hole size in
 * numbering is consequently the value of {@code cacheSize}.
 *
 * <p>It is possible to avoid acquiring a new connection for the incrementer by setting the
 * "useNewConnection" property to false. In this case you <i>MUST</i> use a non-transactional
 * storage engine like MYISAM when defining the incrementer table.
 *
 * <p>Note that {@code MySQLMaxValueIncrementer} is compatible with
 * <a href="https://dev.mysql.com/doc/refman/8.0/en/mysql-tips.html#safe-updates">MySQL safe updates mode</a>.
 *
 * @author Jean-Pierre Pawlak
 * @author Thomas Risberg
 * @author Juergen Hoeller
 * @author Sam Brannen
 */
public class MySQLMaxValueIncrementer extends AbstractColumnMaxValueIncrementer {

	// [OCA] 字段 `VALUE_SQL`：类成员状态。
	/** The SQL string for retrieving the new sequence value. */
	private static final String VALUE_SQL = "select last_insert_id()";

	// [OCA] 字段 `nextId`：类成员状态。
	/** The next id to serve. */
	private long nextId = 0;

	// [OCA] 字段 `maxId`：类成员状态。
	/** The max id to serve. */
	private long maxId = 0;

	// [OCA] 字段 `useNewConnection`：类成员状态。
	/** Whether to use a new connection for the incrementer. */
	private boolean useNewConnection = true;


	/**
	 * Default constructor for bean property style usage.
	 * @see #setDataSource
	 * @see #setIncrementerName
	 * @see #setColumnName
	 */
	public MySQLMaxValueIncrementer() {
	}

	/**
	 * Convenience constructor.
	 * @param dataSource the DataSource to use
	 * @param incrementerName the name of the sequence table to use
	 * @param columnName the name of the column in the sequence table to use
	 */
	public MySQLMaxValueIncrementer(DataSource dataSource, String incrementerName, String columnName) {
		super(dataSource, incrementerName, columnName);
	}


	/**
	 * Set whether to use a new connection for the incrementer.
	 * <p>{@code true} is necessary to support transactional storage engines,
	 * using an isolated separate transaction for the increment operation.
	 * {@code false} is sufficient if the storage engine of the sequence table
	 * is non-transactional (like MYISAM), avoiding the effort of acquiring an
	 * extra {@code Connection} for the increment operation.
	 * <p>Default is {@code true}.
	 * @since 4.3.6
	 * @see DataSource#getConnection()
	 */
	public void setUseNewConnection(boolean useNewConnection) {
		this.useNewConnection = useNewConnection;
	}


	@Override
	/* ===== [OCA 中文解析] =====
方法 getNextKey — 意图与阅读要点

方法 `getNextKey` 复杂度较高（CCN≈12, NLOC≈71）。阅读时建议先抓住主路径，再看分支/异常/缓存等旁路逻辑；关注它在调用链中上下游的契约（入参约束、返回值语义、抛出的异常）。
	===== [OCA 中文解析结束] ===== */
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
				// Increment the sequence column...
				String columnName = getColumnName();
				try {
					stmt.executeUpdate("update " + getIncrementerName() + " set " + columnName +
							" = last_insert_id(" + columnName + " + " + getCacheSize() + ") limit 1");
				}
				catch (SQLException ex) {
					throw new DataAccessResourceFailureException("Could not increment " + columnName + " for " +
							getIncrementerName() + " sequence table", ex);
				}
				// Retrieve the new max of the sequence column...
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
