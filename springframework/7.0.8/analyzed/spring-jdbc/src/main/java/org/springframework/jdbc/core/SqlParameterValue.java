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

import org.jspecify.annotations.Nullable;

/**
 * 表示 SQL 参数值的对象，包含参数元数据，
 * 如 SQL 类型和数值的小数位数。
 *
 * <p>设计用于 {@link JdbcTemplate} 接受参数值数组的操作：
 * 每个参数值可以是 {@code SqlParameterValue}，
 * 显式指定 SQL 类型（及可选的小数位数），
 * 而非让模板猜测默认类型。
 * 注意这仅适用于带普通参数数组的操作，
 * 不适用于带显式类型数组的重载变体。
 *
 * @author Juergen Hoeller
 * @since 2.0.5
 * @see java.sql.Types
 * @see JdbcTemplate#query(String, ResultSetExtractor, Object[])
 * @see JdbcTemplate#query(String, RowCallbackHandler, Object[])
 * @see JdbcTemplate#query(String, RowMapper, Object[])
 * @see JdbcTemplate#update(String, Object[])
 */
public class SqlParameterValue extends SqlParameter {

	private final @Nullable Object value;


	/**
	 * 创建新的 SqlParameterValue，指定 SQL 类型。
	 * @param sqlType 参数的 SQL 类型，对应 {@code java.sql.Types}
	 * @param value 值对象
	 */
	public SqlParameterValue(int sqlType, @Nullable Object value) {
		super(sqlType);
		this.value = value;
	}

	/**
	 * 创建新的 SqlParameterValue，指定 SQL 类型。
	 * @param sqlType 参数的 SQL 类型，对应 {@code java.sql.Types}
	 * @param typeName 参数的类型名（可选）
	 * @param value 值对象
	 */
	public SqlParameterValue(int sqlType, @Nullable String typeName, @Nullable Object value) {
		super(sqlType, typeName);
		this.value = value;
	}

	/**
	 * 创建新的 SqlParameterValue，指定 SQL 类型。
	 * @param sqlType 参数的 SQL 类型，对应 {@code java.sql.Types}
	 * @param scale 小数点后的位数（用于 DECIMAL 和 NUMERIC 类型）
	 * @param value 值对象
	 */
	public SqlParameterValue(int sqlType, int scale, @Nullable Object value) {
		super(sqlType, scale);
		this.value = value;
	}

	/**
	 * 基于给定 SqlParameter 声明创建新的 SqlParameterValue。
	 * @param declaredParam 要定义值的已声明 SqlParameter
	 * @param value 值对象
	 */
	public SqlParameterValue(SqlParameter declaredParam, @Nullable Object value) {
		super(declaredParam);
		this.value = value;
	}


	/**
	 * 返回此参数值持有的值对象。
	 */
	public @Nullable Object getValue() {
		return this.value;
	}

}
