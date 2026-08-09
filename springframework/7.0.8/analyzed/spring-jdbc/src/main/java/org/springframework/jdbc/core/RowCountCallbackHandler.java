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
 * RowCallbackHandler 的实现。回调处理器的便捷超类。
 * 每个实例只能使用一次。
 *
 * <p>可单独使用（例如在测试用例中验证结果集维度），
 * 也可作为实际执行操作的回调处理器的超类，
 * 以利用其提供的维度信息。
 *
 * <p>与 JdbcTemplate 配合使用的示例：
 *
 * <pre class="code">JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);  // 可重用对象
 *
 * RowCountCallbackHandler countCallback = new RowCountCallbackHandler();  // 不可重用
 * jdbcTemplate.query("select * from user", countCallback);
 * int rowCount = countCallback.getRowCount();</pre>
 *
 * @author Rod Johnson
 * @since May 3, 2001
 */
public class RowCountCallbackHandler implements RowCallbackHandler {

	/** 目前已处理的行数。 */
	private int rowCount;

	/** 目前已知的列数。 */
	private int columnCount;

	/**
	 * 从 0 开始索引。ResultSetMetaData 返回的各列类型
	 * （对应 java.sql.Types 常量）。
	 */
	private int @Nullable [] columnTypes;

	/**
	 * 从 0 开始索引。ResultSetMetaData 返回的列名。
	 */
	private String @Nullable [] columnNames;


	/**
	 * ResultSetCallbackHandler 的实现。
	 * 若是第一行则确定列数，否则仅计数行数。
	 * <p>子类可覆盖 {@code processRow(ResultSet, int)} 方法
	 * 执行自定义提取或处理。
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
			// 也可获取列名
		}
		processRow(rs, this.rowCount++);
	}

	/**
	 * 子类可覆盖本方法执行自定义提取或处理。
	 * 本类实现为空操作。
	 * @param rs 要提取数据的 ResultSet。每行调用一次
	 * @param rowNum 当前行号（从 0 开始）
	 */
	protected void processRow(ResultSet rs, int rowNum) throws SQLException {
	}


	/**
	 * 以 java.sql.Types 常量返回各列类型。
	 * 首次调用 processRow 后有效。
	 * @return 各列的 java.sql.Types 常量。
	 * <b>从 0 到 n-1 索引。</b>
	 */
	public final int @Nullable [] getColumnTypes() {
		return this.columnTypes;
	}

	/**
	 * 返回各列名称。
	 * 首次调用 processRow 后有效。
	 * @return 各列名称。
	 * <b>从 0 到 n-1 索引。</b>
	 */
	public final String @Nullable [] getColumnNames() {
		return this.columnNames;
	}

	/**
	 * 返回此 ResultSet 的行数。
	 * 仅在处理完成后有效。
	 * @return 此 ResultSet 中的行数
	 */
	public final int getRowCount() {
		return this.rowCount;
	}

	/**
	 * 返回此结果集的列数。
	 * 看到第一行后有效，子类可在处理过程中使用。
	 * @return 此结果集的列数
	 */
	public final int getColumnCount() {
		return this.columnCount;
	}

}
