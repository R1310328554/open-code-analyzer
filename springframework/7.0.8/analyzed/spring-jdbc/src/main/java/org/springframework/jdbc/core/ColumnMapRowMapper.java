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
import java.util.Map;

import org.jspecify.annotations.Nullable;

import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.util.LinkedCaseInsensitiveMap;

/**
 * 为每行创建 {@code java.util.Map} 的 {@link RowMapper} 实现，
 * 以列名为键、列值为值表示所有列。
 *
 * <p>可通过覆盖 {@link #createColumnMap} 与 {@link #getColumnKey}
 * 分别自定义 Map 实现及列键。
 *
 * <p><b>注意：</b>默认 {@code ColumnMapRowMapper} 构建键不区分大小写的
 * 链接 Map，既保留列顺序又允许任意大小写列名。
 *
 * @author Juergen Hoeller
 * @since 1.2
 * @see JdbcTemplate#queryForList(String)
 * @see JdbcTemplate#queryForMap(String)
 */
public class ColumnMapRowMapper implements RowMapper<Map<String, @Nullable Object>> {

	@Override
	public Map<String, @Nullable Object> mapRow(ResultSet rs, int rowNum) throws SQLException {
		ResultSetMetaData rsmd = rs.getMetaData();
		int columnCount = rsmd.getColumnCount();
		Map<String, @Nullable Object> mapOfColumnValues = createColumnMap(columnCount);
		for (int i = 1; i <= columnCount; i++) {
			String column = JdbcUtils.lookupColumnName(rsmd, i);
			mapOfColumnValues.putIfAbsent(getColumnKey(column), getColumnValue(rs, i));
		}
		return mapOfColumnValues;
	}

	/**
	 * 创建用作列 Map 的 Map 实例。
	 * <p>默认创建链接且不区分大小写的 Map。
	 * @param columnCount 列数，用作 Map 初始容量
	 * @return 新 Map 实例
	 * @see org.springframework.util.LinkedCaseInsensitiveMap
	 */
	protected Map<String, @Nullable Object> createColumnMap(int columnCount) {
		return new LinkedCaseInsensitiveMap<>(columnCount);
	}

	/**
	 * 确定列 Map 中给定列使用的键。
	 * <p>默认原样返回列名。
	 * @param columnName ResultSet 返回的列名
	 * @return 使用的列键
	 * @see java.sql.ResultSetMetaData#getColumnName
	 */
	protected String getColumnKey(String columnName) {
		return columnName;
	}

	/**
	 * 获取指定列的 JDBC 对象值。
	 * <p>默认使用 {@code getObject}；另含针对 Oracle TIMESTAMP
	 * 返回非标准对象的变通处理。
	 * @param rs 持有数据的 ResultSet
	 * @param index 列索引
	 * @return 返回值对象
	 * @see org.springframework.jdbc.support.JdbcUtils#getResultSetValue
	 */
	protected @Nullable Object getColumnValue(ResultSet rs, int index) throws SQLException {
		return JdbcUtils.getResultSetValue(rs, index);
	}

}
