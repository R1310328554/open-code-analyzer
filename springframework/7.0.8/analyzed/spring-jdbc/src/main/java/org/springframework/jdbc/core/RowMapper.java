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

import org.jspecify.annotations.Nullable;

/**
 * {@link JdbcTemplate} 用于逐行映射 {@link java.sql.ResultSet} 行的接口。
 * 本接口的实现负责将每一行映射到结果对象的实际工作，
 * 但无需关心异常处理——{@link java.sql.SQLException SQLExceptions}
 * 由调用方 {@code JdbcTemplate} 捕获并处理。
 *
 * <p>通常用于 {@code JdbcTemplate} 的查询方法或存储过程的 {@code out} 参数。
 * {@code RowMapper} 对象通常是无状态的，因此可重用；
 * 是在单一位置实现行映射逻辑的理想选择。
 *
 * <p>或者，考虑从 {@code jdbc.object} 包子类化
 * {@link org.springframework.jdbc.object.MappingSqlQuery}：
 * 可以该风格构建包含行映射逻辑的可执行查询对象，
 * 而非分别使用 {@code JdbcTemplate} 和 {@code RowMapper}。
 *
 * @author Thomas Risberg
 * @author Juergen Hoeller
 * @param <T> 结果类型
 * @see JdbcTemplate
 * @see RowCallbackHandler
 * @see ResultSetExtractor
 * @see org.springframework.jdbc.object.MappingSqlQuery
 */
@FunctionalInterface
public interface RowMapper<T extends @Nullable Object> {

	/**
	 * 实现者必须实现本方法以映射 {@code ResultSet} 中的每一行数据。
	 * 此方法不应在 {@code ResultSet} 上调用 {@code next()}；
	 * 只应映射当前行的值。
	 * @param rs 要映射的 {@code ResultSet}（针对当前行预先初始化）
	 * @param rowNum 当前行号
	 * @return 当前行的结果对象（可能为 {@code null}）
	 * @throws SQLException 若获取列值时遇到 SQLException
	 * （即无需捕获 SQLException）
	 */
	T mapRow(ResultSet rs, int rowNum) throws SQLException;

}
