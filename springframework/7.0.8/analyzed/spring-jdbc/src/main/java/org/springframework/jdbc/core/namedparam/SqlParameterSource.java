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

package org.springframework.jdbc.core.namedparam;

import org.jspecify.annotations.Nullable;

import org.springframework.jdbc.support.JdbcUtils;

/**
 * 定义可为命名 SQL 参数提供参数值的对象的通用功能接口，
 * 作为 {@link NamedParameterJdbcTemplate} 操作的参数。
 *
 * <p>本接口除参数值外还允许指定 SQL 类型。
 * 所有参数值和类型均通过参数名称标识。
 *
 * <p>旨在用一致接口包装 Map、JavaBean 等多种实现。
 *
 * @author Thomas Risberg
 * @author Juergen Hoeller
 * @since 2.0
 * @see NamedParameterJdbcOperations
 * @see NamedParameterJdbcTemplate
 * @see MapSqlParameterSource
 * @see BeanPropertySqlParameterSource
 */
public interface SqlParameterSource {

	/**
	 * 表示未知（或未指定）SQL 类型的常量。
	 * 当无特定 SQL 类型时由 {@code getType} 返回。
	 * @see #getSqlType
	 * @see java.sql.Types
	 */
	int TYPE_UNKNOWN = JdbcUtils.TYPE_UNKNOWN;


	/**
	 * 判断指定命名参数是否有值。
	 * @param paramName 参数名称
	 * @return 是否已定义值
	 */
	boolean hasValue(String paramName);

	/**
	 * 返回请求的命名参数的值。
	 * @param paramName 参数名称
	 * @return 指定参数的值
	 * @throws IllegalArgumentException 若请求的参数无值
	 */
	@Nullable Object getValue(String paramName) throws IllegalArgumentException;

	/**
	 * 确定指定命名参数的 SQL 类型。
	 * @param paramName 参数名称
	 * @return 指定参数的 SQL 类型，未知时返回 {@code TYPE_UNKNOWN}
	 * @see #TYPE_UNKNOWN
	 */
	default int getSqlType(String paramName) {
		return TYPE_UNKNOWN;
	}

	/**
	 * 确定指定命名参数的类型名称。
	 * @param paramName 参数名称
	 * @return 指定参数的类型名称，未知时返回 {@code null}
	 */
	default @Nullable String getTypeName(String paramName) {
		return null;
	}

	/**
	 * 若可能，枚举所有可用参数名称。
	 * <p>此为可选操作，主要用于
	 * {@link org.springframework.jdbc.core.simple.SimpleJdbcInsert}
	 * 和 {@link org.springframework.jdbc.core.simple.SimpleJdbcCall}。
	 * @return 参数名称数组，无法确定时返回 {@code null}
	 * @since 5.0.3
	 * @see SqlParameterSourceUtils#extractCaseInsensitiveParameterNames
	 */
	default String @Nullable [] getParameterNames() {
		return null;
	}

}
