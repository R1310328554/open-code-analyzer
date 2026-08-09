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

package org.springframework.jdbc.core;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.jspecify.annotations.Nullable;

import org.springframework.jdbc.support.JdbcUtils;

/**
 * RowCallbackHandler 的实现。回调处理程序的方便超类。一个实例只能使用一次。
 * <p>我们可以单独使用它（例如，在测试用例中，以确保我们的结果集具有有效的维度），或者将其用作实际执行某些操作的回调处理程序的超类，并将受益于它提供的维度信息。
 * <p>A 与 JdbcTemplate 的使用示例：
 * <pre class="code">JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource); // 可重用对象
 * RowCountCallbackHandler countCallback = new RowCountCallbackHandler(); // 不可重用 jdbcTempl
 * ate.query("select * from user", countCallback); int rowCount = countCallback.getRowCount
 * ();</pre>
 * @author Rod Johnson
 * @since May 3, 2001
 */
public class RowCountCallbackHandler implements RowCallbackHandler {

	/**
	 */
	private int rowCount;

	/**
	 */
	private int columnCount;

	/**
	 * 从 0 开始索引。ResultSetMetaData 对象返回的列的类型（如 java.sql.Types 中）。
	 */
	private int @Nullable [] columnTypes;

	/**
	 * 从 0 开始索引。由 ResultSetMetaData 对象返回的列名称。
	 */
	private String @Nullable [] columnNames;


	/**
	 * ResultSetCallbackHandler 的实现。如果这是第一行，则计算出列大小，否则仅计算行数。 <p>子类可以通过重写{@code processRow(Resul
	 * tSet, int)}方法来执行自定义提取或处理。
	 * @see #processRow(java.sql.ResultSet, int)
	 */
	@Override
	public final void processRow(ResultSet rs) throws SQLException {
		if (this.rowCount == 0) {
			ResultSetMetaData rsmd = rs.getMetaData();
			this.columnCount = rsmd.getColumnCount();
			this.columnTypes = new int[this.columnCount];
			this.columnNames = new String[this.columnCount];
			for (int i = 0; i < this.columnCount; i++) {
				this.columnTypes[i] = rsmd.getColumnType(i + 1);
				this.columnNames[i] = JdbcUtils.lookupColumnName(rsmd, i + 1);
			}
			// 还可以获得列名
		}
		processRow(rs, this.rowCount++);
	}

	/**
	 * 子类可以覆盖它以执行自定义提取或处理。这个类的实现什么也不做。
	 * @param rs 要从中提取数据的 ResultSet。为每一行调用此方法
	 * @param rowNum 当前行号（从0开始）
	 */
	protected void processRow(ResultSet rs, int rowNum) throws SQLException {
	}


	/**
	 * 以 java.sql.Types 常量形式返回列的类型 首次调用 processRow 后有效。
	 * @return 列的类型作为 java.sql.Types 常量。 <b>索引从 0 到 n-1.</b>
	 */
	public final int @Nullable [] getColumnTypes() {
		return this.columnTypes;
	}

	/**
	 * 返回列的名称。第一次调用 processRow 后有效。
	 * @return 列的名称。 <b>索引从 0 到 n-1.</b>
	 */
	public final String @Nullable [] getColumnNames() {
		return this.columnNames;
	}

	/**
	 * 返回此 ResultSet 的行数。仅在处理完成后有效
	 * @return 此结果集中的行数
	 */
	public final int getRowCount() {
		return this.rowCount;
	}

	/**
	 * 返回此结果集中的列数。一旦我们看到第一行就有效，因此子类可以在处理过程中使用它
	 * @return 此结果集中的列数
	 */
	public final int getColumnCount() {
		return this.columnCount;
	}

}
