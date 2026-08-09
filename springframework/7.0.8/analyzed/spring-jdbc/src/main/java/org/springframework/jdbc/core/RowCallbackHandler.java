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
 * {@link JdbcTemplate} 和 {@link
 * org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
 * NamedParameterJdbcTemplate} 使用的接口，用于按行处理 {@link java.sql.ResultSet}
 * 的行。此接口的实现执行处理每一行的实际工作，但不需要担心异常处理。 {@link java.sql.SQLException SQLExceptions} 将由调用
 * {@code JdbcTemplate} 或 {@code NamedParameterJdbcTemplate} 捕获并处理。
 * <p> 与 {@link ResultSetExtractor} 相比，{@code RowCallbackHandler}
 * 对象通常是有状态的：它将结果状态保留在对象内，以供以后检查。有关使用示例，请参阅 {@link RowCountCallbackHandler}。
 * <p> 如果您需要精确地映射每行一个结果对象，并将它们组装到一个列表中，请考虑使用 {@link RowMapper}。
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
	 * 实现必须实现此方法来处理 {@link ResultSet} 中的每一行数据。此方法不应在 {@code ResultSet} 上调用 {@code next()}；它只应该提
	 * 取当前行的值。 <p>具体实现选择做什么取决于它：一个简单的实现可能只是计算行数，而另一个实现可能会构建一个 XML 文档。
	 * @param rs 要处理的 {@code ResultSet}（针对当前行预先初始化）
	 * @throws SQLException 如果在获取列值时遇到 {@code SQLException}（即无需捕获 {@code SQLException}）
	 */
	void processRow(ResultSet rs) throws SQLException;

}
