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

/**
 * 表示存储过程调用返回的 {@link java.sql.ResultSet}。
 *
 * <p>必须提供 {@link ResultSetExtractor}、{@link RowCallbackHandler}
 * 或 {@link RowMapper} 以处理返回行。
 *
 * <p>返回的 {@link java.sql.ResultSet ResultSets} 与所有存储过程参数一样
 * 必须具名。
 *
 * @author Thomas Risberg
 * @author Juergen Hoeller
 */
public class SqlReturnResultSet extends ResultSetSupportingSqlParameter {

	/**
	 * 创建 {@link SqlReturnResultSet} 新实例。
	 * @param name 参数名，用于输入/输出映射
	 * @param extractor 解析 {@link java.sql.ResultSet} 的 {@link ResultSetExtractor}
	 */
	public SqlReturnResultSet(String name, ResultSetExtractor<?> extractor) {
		super(name, 0, extractor);
	}

	/**
	 * 创建 {@link SqlReturnResultSet} 新实例。
	 * @param name 参数名，用于输入/输出映射
	 * @param handler 解析 {@link java.sql.ResultSet} 的 {@link RowCallbackHandler}
	 */
	public SqlReturnResultSet(String name, RowCallbackHandler handler) {
		super(name, 0, handler);
	}

	/**
	 * 创建 {@link SqlReturnResultSet} 新实例。
	 * @param name 参数名，用于输入/输出映射
	 * @param mapper 解析 {@link java.sql.ResultSet} 的 {@link RowMapper}
	 */
	public SqlReturnResultSet(String name, RowMapper<?> mapper) {
		super(name, 0, mapper);
	}


	/**
	 * 本实现始终返回 {@code true}。
	 */
	@Override
	public boolean isResultsParameter() {
		return true;
	}

}
