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
 * ResultSetExtractor 接口的适配器实现，委托给 RowMapper，该 RowMapper 应该为每行创建一个对象。每个对象都会添加到此
 * ResultSetExtractor 的结果列表中。
 * <p> 对于数据库表中每行一个对象的典型情况很有用。结果列表中的条目数将与行数匹配。
 * <p>请注意，RowMapper 对象通常是无状态的，因此可重用；只有 RowMapperResultSetExtractor 适配器是有状态的。
 * <p>A 与 JdbcTemplate 的使用示例：
 * <pre class="code">JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource); // 可重用对象
 * RowMapper rowMapper = new UserRowMapper(); // 可重用对象
 * List allUsers = (List) jdbcTemplate.query( "从用户中选择 *", new
 * RowMapperResultSetExtractor(rowMapper, 10));
 * 用户 user = (User) jdbcTemplate.queryForObject( "select * from user where id=?", new
 * Object[] {id}, new RowMapperResultSetExtractor(rowMapper, 1));</pre>
 * <p>或者，考虑从 {@code jdbc.object} 包中子类化 MappingSqlQuery：您可以在那里拥有可执行查询对象（包含行映射逻辑），而不是使用单独的
 * JdbcTemplate 和 RowMapper 对象。
 * @author Juergen Hoeller
 * @author Yanming Zhou
 * @since 1.0.2
 * @param <T> 结果元素类型
 * @see RowMapper
 * @see JdbcTemplate
 * @see org.springframework.jdbc.object.MappingSqlQuery
 */
public class RowMapperResultSetExtractor<T> implements ResultSetExtractor<List<T>> {

	/** 映射器相关状态（`rowMapper`）。 */
	private final RowMapper<T> rowMapper;

	/** `rowsExpected`：该类的成员状态。 */
	private final int rowsExpected;

	/** `maxRows`：该类的成员状态。 */
	private final int maxRows;


	/**
	 * 创建一个新的 RowMapperResultSetExtractor。
	 * @param rowMapper RowMapper 为每一行创建一个对象
	 */
	public RowMapperResultSetExtractor(RowMapper<T> rowMapper) {
		this(rowMapper, 0);
	}

	/**
	 * 创建一个新的 RowMapperResultSetExtractor。
	 * @param rowMapper RowMapper 为每一行创建一个对象
	 * @param rowsExpected 预期行数（仅用于优化集合处理）
	 */
	public RowMapperResultSetExtractor(RowMapper<T> rowMapper, int rowsExpected) {
		this(rowMapper, rowsExpected, -1);
	}

	/**
	 * 创建一个新的 RowMapperResultSetExtractor。
	 * @param rowMapper RowMapper 为每一行创建一个对象
	 * @param rowsExpected 预期行数（仅用于优化集合处理）
	 * @param maxRows 最大行数（驱动程序默认为 -1）
	 * @since 7.0
	 */
	public RowMapperResultSetExtractor(RowMapper<T> rowMapper, int rowsExpected, int maxRows) {
		Assert.notNull(rowMapper, "RowMapper must not be null");
		this.rowMapper = rowMapper;
		this.rowsExpected = rowsExpected;
		this.maxRows = maxRows;
	}


	/**
	 * 提取：Data（方法 `extractData`）。
	 */
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
