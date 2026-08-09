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
 * 执行批量更新操作的 SqlUpdate 子类。封装待更新记录的排队逻辑，
 * 在调用 {@code flush} 或达到给定批量大小时作为单个批次提交。
 *
 * <p>注意：与本包中其他 JDBC 操作对象不同，本类为<b>非线程安全对象</b>。
 * 每次使用需创建新实例，或在同一线程内复用前调用 {@code reset}。
 *
 * @author Keith Donald
 * @author Juergen Hoeller
 * @since 1.1
 * @see #flush
 * @see #reset
 */
public class BatchSqlUpdate extends SqlUpdate {

	/**
	 * 提交批次前累积的默认插入条数（5000）。
	 */
	public static final int DEFAULT_BATCH_SIZE = 5000;


	private int batchSize = DEFAULT_BATCH_SIZE;

	private boolean trackRowsAffected = true;

	private final Deque<Object[]> parameterQueue = new ArrayDeque<>();

	private final List<Integer> rowsAffected = new ArrayList<>();


	/**
	 * 允许作为 JavaBean 使用的构造函数。编译和使用前须设置 DataSource 与 SQL。
	 * @see #setDataSource
	 * @see #setSql
	 */
	public BatchSqlUpdate() {
		super();
	}

	/**
	 * 使用给定 DataSource 与 SQL 构造更新对象。
	 * @param ds 用于获取连接的 DataSource
	 * @param sql 要执行的 SQL 语句
	 */
	public BatchSqlUpdate(DataSource ds, String sql) {
		super(ds, sql);
	}

	/**
	 * 使用给定 DataSource、SQL 与匿名参数构造更新对象。
	 * @param ds 用于获取连接的 DataSource
	 * @param sql 要执行的 SQL 语句
	 * @param types 参数 SQL 类型，定义于 {@code java.sql.Types}
	 * @see java.sql.Types
	 */
	public BatchSqlUpdate(DataSource ds, String sql, int[] types) {
		super(ds, sql, types);
	}

	/**
	 * 使用给定 DataSource、SQL、匿名参数及批量大小构造更新对象。
	 * @param ds 用于获取连接的 DataSource
	 * @param sql 要执行的 SQL 语句
	 * @param types 参数 SQL 类型，定义于 {@code java.sql.Types}
	 * @param batchSize 触发自动中间 flush 的语句数量
	 * @see java.sql.Types
	 */
	public BatchSqlUpdate(DataSource ds, String sql, int[] types, int batchSize) {
		super(ds, sql, types);
		setBatchSize(batchSize);
	}


	/**
	 * 设置触发自动中间 flush 的语句数量。{@code update} 调用或给定语句参数
	 * 将排队直至达到批量大小，届时清空队列并执行批次。
	 * <p>也可通过显式 {@code flush} 调用刷新已排队语句。
	 * 注意：排队所有参数后须调用 flush，以确保所有语句均已刷新。
	 */
	public void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
	}

	/**
	 * 设置是否跟踪本操作对象执行的批量更新所影响的行数。
	 * <p>默认为 "true"。关闭可节省行数列表所需内存。
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
	 * 重写的 {@code update}，将给定语句参数加入队列而非立即执行。
	 * SqlUpdate 基类的其他 {@code update} 方法均经此方法，行为类似。
	 * <p>须调用 {@code flush} 才真正执行批次。
	 * 达到指定批量大小时会隐式 flush；仍须最终调用 {@code flush} 刷新所有语句。
	 * @param params 参数对象数组
	 * @return 更新影响行数（恒为 -1，表示“不适用”，因本方法不实际执行语句）
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
	 * 触发将已排队的更新操作作为最终批次提交。
	 * @return 每条语句影响行数的数组
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
	 * 返回所有已执行语句的影响行数。
	 * 累积 {@code flush} 的返回值直至调用 {@code reset}。
	 * @return 每条语句影响行数的数组
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
	 * 重置语句参数队列、影响行数缓存与执行计数。
	 */
	public void reset() {
		this.parameterQueue.clear();
		this.rowsAffected.clear();
	}

}
