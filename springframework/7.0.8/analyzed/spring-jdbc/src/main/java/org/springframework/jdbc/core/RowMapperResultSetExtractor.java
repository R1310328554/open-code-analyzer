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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.util.Assert;

/**
 * ResultSetExtractor 接口的适配器实现，委托给 RowMapper，
 * 由 RowMapper 为每行创建一个对象。
 * 每个对象添加到本 ResultSetExtractor 的结果 List 中。
 *
 * <p>适用于数据库表每行对应一个对象的典型场景。
 * 结果列表的条目数与行数一致。
 *
 * <p>注意 RowMapper 通常是无状态的，因此可重用；
 * 仅 RowMapperResultSetExtractor 适配器是有状态的。
 *
 * <p>与 JdbcTemplate 配合使用的示例：
 *
 * <pre class="code">JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);  // 可重用对象
 * RowMapper rowMapper = new UserRowMapper();  // 可重用对象
 *
 * List allUsers = (List) jdbcTemplate.query(
 *     "select * from user",
 *     new RowMapperResultSetExtractor(rowMapper, 10));
 *
 * User user = (User) jdbcTemplate.queryForObject(
 *     "select * from user where id=?", new Object[] {id},
 *     new RowMapperResultSetExtractor(rowMapper, 1));</pre>
 *
 * <p>或者，考虑从 {@code jdbc.object} 包子类化 MappingSqlQuery：
 * 可以该方式构建包含行映射逻辑的可执行查询对象，
 * 而非分别使用 JdbcTemplate 和 RowMapper。
 *
 * @author Juergen Hoeller
 * @author Yanming Zhou
 * @since 1.0.2
 * @param <T> 结果元素类型
 * @see RowMapper
 * @see JdbcTemplate
 * @see org.springframework.jdbc.object.MappingSqlQuery
 */
public class RowMapperResultSetExtractor<T> implements ResultSetExtractor<List<T>> {

	private final RowMapper<T> rowMapper;

	private final int rowsExpected;

	private final int maxRows;


	/**
	 * 创建新的 RowMapperResultSetExtractor。
	 * @param rowMapper 为每行创建对象的 RowMapper
	 */
	public RowMapperResultSetExtractor(RowMapper<T> rowMapper) {
		this(rowMapper, 0);
	}

	/**
	 * 创建新的 RowMapperResultSetExtractor。
	 * @param rowMapper 为每行创建对象的 RowMapper
	 * @param rowsExpected 预期行数（仅用于优化集合处理）
	 */
	public RowMapperResultSetExtractor(RowMapper<T> rowMapper, int rowsExpected) {
		this(rowMapper, rowsExpected, -1);
	}

	/**
	 * 创建新的 RowMapperResultSetExtractor。
	 * @param rowMapper 为每行创建对象的 RowMapper
	 * @param rowsExpected 预期行数（仅用于优化集合处理）
	 * @param maxRows 最大行数（或 -1 表示使用驱动默认值）
	 * @since 7.0
	 */
	public RowMapperResultSetExtractor(RowMapper<T> rowMapper, int rowsExpected, int maxRows) {
		Assert.notNull(rowMapper, "RowMapper must not be null");
		this.rowMapper = rowMapper;
		this.rowsExpected = rowsExpected;
		this.maxRows = maxRows;
	}


	@Override
	public List<T> extractData(ResultSet rs) throws SQLException {
		List<T> results = (this.rowsExpected > 0 ? new ArrayList<>(this.rowsExpected) : new ArrayList<>());
		int rowNum = 0;
		while (rs.next() && (this.maxRows == -1 || rowNum < this.maxRows)) {
			results.add(this.rowMapper.mapRow(rs, rowNum++));
		}
		return results;
	}

}
