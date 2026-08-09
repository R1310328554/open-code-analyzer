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
 * {@link RowMapper} 实现为每一行创建一个 {@code java.util.Map}，将所有列表示为键值对：每列一个条目，以列名称作为键。
 * <p> 可以通过分别重写 {@link #createColumnMap} 和 {@link #getColumnKey} 来自定义要使用的 Map 实现以及列 Map
 * 中每一列要使用的键。
 * <p><b>注意：</b> 默认情况下，{@code ColumnMapRowMapper} 将尝试使用不区分大小写的键构建链接映射，以保留列顺序并允许对列名称使用任何大小写。
 * @author Juergen Hoeller
 * @since 1.2
 * @see JdbcTemplate#queryForList(String)
 * @see JdbcTemplate#queryForMap(String)
 */
public class ColumnMapRowMapper implements RowMapper<Map<String, @Nullable Object>> {

	/**
	 * 映射：Row（方法 `mapRow`）。
	 */
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
	 * 创建一个用作列图的 Map 实例。 <p> 默认情况下，将创建一个不区分大小写的链接映射。
	 * @param columnCount 列数，用作 Map 的初始容量
	 * @return 新的地图实例
	 * @see org.springframework.util.LinkedCaseInsensitiveMap
	 */
	protected Map<String, @Nullable Object> createColumnMap(int columnCount) {
		return new LinkedCaseInsensitiveMap<>(columnCount);
	}

	/**
	 * 确定用于列映射中给定列的键。 <p> 默认情况下，提供的列名将不加修改地返回。
	 * @param columnName ResultSet 返回的列名
	 * @return 要使用的列键
	 * @see java.sql.ResultSetMetaData#getColumnName
	 */
	protected String getColumnKey(String columnName) {
		return columnName;
	}

	/**
	 * 检索指定列的 JDBC 对象值。 <p>默认实现使用{@code getObject}方法。此外，此实现还包括一个“hack”，用于绕过 Oracle 返回其 TIMESTAM
	 * P 数据类型的非标准对象。
	 * @param rs 保存数据的 ResultSet
	 * @param index 列索引
	 * @return 返回对象
	 * @see org.springframework.jdbc.support.JdbcUtils#getResultSetValue
	 */
	protected @Nullable Object getColumnValue(ResultSet rs, int index) throws SQLException {
		return JdbcUtils.getResultSetValue(rs, index);
	}

}
