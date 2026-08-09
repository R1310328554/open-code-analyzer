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

package org.springframework.jdbc.object;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;

/**
 * SqlUpdate 子类，执行批量更新操作。封装要更新的排队记录，并在调用 {@code flush} 或满足给定批次大小时将它们添加为单个批次。
 * <p> 请注意，该类是 <b> 非线程安全对象 </b>，与此包中的所有其他 JDBC 操作对象相反。您需要为每次使用创建一个新实例，或者在同一线程中重用之前调用 {@code
 *  reset}。
 * @author Keith Donald
 * @author Juergen Hoeller
 * @since 1.1
 * @see #flush
 * @see #reset
 */
public class BatchSqlUpdate extends SqlUpdate {

	/**
	 * 提交批次之前累积的默认插入数量 (5000)。
	 */
	public static final int DEFAULT_BATCH_SIZE = 5000;


	/** `DEFAULT_BATCH_SIZE`：该类的成员状态。 */
	private int batchSize = DEFAULT_BATCH_SIZE;

	/** `true`：该类的成员状态。 */
	private boolean trackRowsAffected = true;

	private final Deque<Object[]> parameterQueue = new ArrayDeque<>();

	private final List<Integer> rowsAffected = new ArrayList<>();


	/**
	 * 允许用作 JavaBean 的构造函数。编译和使用前必须提供DataSource和SQL。
	 * @see #setDataSource
	 * @see #setSql
	 */
	public BatchSqlUpdate() {
		super();
	}

	/**
	 * 使用给定的 DataSource 和 SQL 构造更新对象。
	 * @param ds 用于获取连接的 DataSource
	 * @param sql 要执行的SQL语句
	 */
	public BatchSqlUpdate(DataSource ds, String sql) {
		super(ds, sql);
	}

	/**
	 * 使用给定的 DataSource、SQL 和匿名参数构造更新对象。
	 * @param ds 用于获取连接的 DataSource
	 * @param sql 要执行的SQL语句
	 * @param types 参数的 SQL 类型，如 {@code java.sql.Types} 类中定义
	 * @see java.sql.Types
	 */
	public BatchSqlUpdate(DataSource ds, String sql, int[] types) {
		super(ds, sql, types);
	}

	/**
	 * 使用给定的 DataSource、SQL、匿名参数构造一个更新对象，并指定可能受影响的最大行数。
	 * @param ds 用于获取连接的 DataSource
	 * @param sql 要执行的SQL语句
	 * @param types 参数的 SQL 类型，如 {@code java.sql.Types} 类中定义
	 * @param batchSize 将触发自动中间刷新的语句数
	 * @see java.sql.Types
	 */
	public BatchSqlUpdate(DataSource ds, String sql, int[] types, int batchSize) {
		super(ds, sql, types);
		setBatchSize(batchSize);
	}


	/**
	 * 设置将触发自动中间刷新的语句数。 {@code update} 调用或给定的语句参数将排队直到满足批处理大小，此时它将清空队列并执行批处理。 <p>您还可以使用显式 {@cod
	 * e flush} 调用刷新已排队的语句。请注意，您需要在对所有参数进行排队后执行此操作，以确保所有语句均已刷新。
	 */
	public void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
	}

	/**
	 * 设置是否跟踪受该操作对象执行的批量更新影响的行。 <p>默认为“true”。关闭此选项可以节省行计数列表所需的内存。
	 * @see #getRowsAffected()
	 */
	public void setTrackRowsAffected(boolean trackRowsAffected) {
		this.trackRowsAffected = trackRowsAffected;
	}

	/**
	 * BatchSqlUpdate 不支持 BLOB 或 CLOB 参数。
	 */
	@Override
	protected boolean supportsLobParameters() {
		return false;
	}


	/**
	 * {@code update} 的重写版本，将给定的语句参数添加到队列中，而不是立即执行它们。 SqlUpdate 基类的所有其他 {@code update} 方法都经过此方法
	 * ，因此行为类似。 <p>需要调用{@code flush}来实际执行批处理。如果达到指定的批量大小，则会发生隐式刷新；您仍然需要最终调用 {@code flush} 来刷新所有
	 * 语句。
	 * @param params 参数对象数组
	 * @return 受更新影响的行数（始终为 -1，表示“不适用”，因为此方法实际上并未执行该语句）
	 * @see #flush
	 */
	@Override
	public int update(Object... params) throws DataAccessException {
		validateParameters(params);
		this.parameterQueue.add(params.clone());

		if (this.parameterQueue.size() == this.batchSize) {
			if (logger.isDebugEnabled()) {
				logger.debug("Triggering auto-flush because queue reached batch size of " + this.batchSize);
			}
			flush();
		}

		return -1;
	}

	/**
	 * 触发任何排队的更新操作作为最终批次添加。
	 * @return 每个语句影响的行数数组
	 */
	public int[] flush() {
		if (this.parameterQueue.isEmpty()) {
			return new int[0];
		}

		int[] rowsAffected = getJdbcTemplate().batchUpdate(
				resolveSql(),
				new BatchPreparedStatementSetter() {
					@Override
					public int getBatchSize() {
						return parameterQueue.size();
					}
					@Override
					public void setValues(PreparedStatement ps, int index) throws SQLException {
						Object[] params = parameterQueue.removeFirst();
						newPreparedStatementSetter(params).setValues(ps);
					}
				});

		for (int rowCount : rowsAffected) {
			checkRowsAffected(rowCount);
			if (this.trackRowsAffected) {
				this.rowsAffected.add(rowCount);
			}
		}

		return rowsAffected;
	}

	/**
	 * 返回队列中当前语句或语句参数的数量。
	 */
	public int getQueueCount() {
		return this.parameterQueue.size();
	}

	/**
	 * 返回已执行语句的数量。
	 */
	public int getExecutionCount() {
		return this.rowsAffected.size();
	}

	/**
	 * 返回所有已执行语句的受影响行数。累积 {@code flush} 的所有返回值，直到调用 {@code reset} 为止。
	 * @return 每个语句影响的行数数组
	 * @see #reset
	 */
	public int[] getRowsAffected() {
		int[] result = new int[this.rowsAffected.size()];
		for (int i = 0; i < this.rowsAffected.size(); i++) {
			result[i] = this.rowsAffected.get(i);
		}
		return result;
	}

	/**
	 * 重置语句参数队列、影响缓存的行以及执行计数。
	 */
	public void reset() {
		this.parameterQueue.clear();
		this.rowsAffected.clear();
	}

}
