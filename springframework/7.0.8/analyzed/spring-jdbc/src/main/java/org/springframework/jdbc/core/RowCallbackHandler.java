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

/**
 * {@link JdbcTemplate} 和
 * {@link org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
 * NamedParameterJdbcTemplate} 用于逐行处理 {@link java.sql.ResultSet}
 * 行的接口。本接口的实现负责处理每一行的实际工作，
 * 但无需关心异常处理——{@link java.sql.SQLException SQLExceptions}
 * 由调用方 {@code JdbcTemplate} 或 {@code NamedParameterJdbcTemplate} 捕获并处理。
 *
 * <p>与 {@link ResultSetExtractor} 不同，{@code RowCallbackHandler}
 * 通常是有状态的：在对象内保留结果状态，供后续检查。
 * 用法示例参见 {@link RowCountCallbackHandler}。
 *
 * <p>若需每行映射恰好一个结果对象并组装为 List，
 * 建议使用 {@link RowMapper}。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see JdbcTemplate
 * @see org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate NamedParameterJdbcTemplate
 * @see RowMapper
 * @see ResultSetExtractor
 * @see RowCountCallbackHandler
 */
@FunctionalInterface
public interface RowCallbackHandler {

	/**
	 * 实现者必须实现本方法以处理 {@link ResultSet} 中的每一行数据。
	 * 此方法不应在 {@code ResultSet} 上调用 {@code next()}；
	 * 只应提取当前行的值。
	 * <p>具体做什么由实现者决定：简单实现可能仅计数行数，
	 * 复杂实现可能构建 XML 文档。
	 * @param rs 要处理的 {@code ResultSet}（针对当前行预先初始化）
	 * @throws SQLException 若获取列值时遇到 {@code SQLException}
	 * （即无需捕获 {@code SQLException}）
	 */
	void processRow(ResultSet rs) throws SQLException;

}
