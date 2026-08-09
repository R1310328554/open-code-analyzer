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

import org.springframework.dao.DataAccessException;

/**
 * {@link JdbcTemplate} 查询方法使用的回调接口。
 * 本接口的实现负责从 {@link java.sql.ResultSet} 提取结果的实际工作，
 * 但无需关心异常处理——{@link java.sql.SQLException SQLExceptions}
 * 由调用方 JdbcTemplate 捕获并处理。
 *
 * <p>本接口主要在 JDBC 框架内部使用。
 * 处理 ResultSet 时 {@link RowMapper} 通常是更简单的选择，
 * 每行映射一个结果对象，而非整个 ResultSet 映射一个结果对象。
 *
 * <p>注意：与 {@link RowCallbackHandler} 不同，ResultSetExtractor
 * 通常是无状态的，因此可重用——只要不访问有状态资源
 * （如流式 LOB 内容时的输出流）或在对象内保留结果状态。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since April 24, 2003
 * @param <T> 结果类型
 * @see JdbcTemplate
 * @see RowCallbackHandler
 * @see RowMapper
 * @see org.springframework.jdbc.core.support.AbstractLobStreamingResultSetExtractor
 */
@FunctionalInterface
public interface ResultSetExtractor<T extends @Nullable Object> {

	/**
	 * 实现者必须实现本方法以处理整个 ResultSet。
	 * @param rs 要提取数据的 ResultSet。实现者不应关闭它——
	 * 将由调用方 JdbcTemplate 关闭。
	 * @return 任意结果对象，若无则 {@code null}
	 * （后一种情况下提取器通常是有状态的）。
	 * @throws SQLException 若获取列值或导航时遇到 SQLException
	 * （即无需捕获 SQLException）
	 * @throws DataAccessException 自定义异常时
	 */
	T extractData(ResultSet rs) throws SQLException, DataAccessException;

}
