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
 * 为对象定义通用功能的接口，这些对象可以为命名 SQL 参数提供参数值，充当 {@link NamedParameterJdbcTemplate} 操作的参数。
 * <p> 除了参数值之外，该接口还允许指定 SQL 类型。所有参数值和类型均通过指定参数名称来标识。
 * <p> 旨在用一致的接口包装各种实现，例如 Map 或 JavaBean。
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
	 * 指示未知（或未指定）SQL 类型的常量。当没有特定的 SQL 类型已知时从 {@code getType} 返回。
	 * @see #getSqlType
	 * @see java.sql.Types
	 */
	int TYPE_UNKNOWN = JdbcUtils.TYPE_UNKNOWN;


	/**
	 * 确定指定的命名参数是否有值。
	 * @param paramName 参数名称
	 * @return 有一个定义的值
	 */
	boolean hasValue(String paramName);

	/**
	 * 返回请求的命名参数的参数值。
	 * @param paramName 参数名称
	 * @return 指定参数的值
	 * @throws IllegalArgumentException 如果请求的参数没有值
	 */
	@Nullable Object getValue(String paramName) throws IllegalArgumentException;

	/**
	 * 确定指定命名参数的 SQL 类型。
	 * @param paramName 参数名称
	 * @return 指定参数的 SQL 类型，如果未知则为 {@code TYPE_UNKNOWN}
	 * @see #TYPE_UNKNOWN
	 */
	default int getSqlType(String paramName) {
		return TYPE_UNKNOWN;
	}

	/**
	 * 确定指定命名参数的类型名称。
	 * @param paramName 参数名称
	 * @return 输入指定参数的名称，如果未知，则输入 {@code null}
	 */
	default @Nullable String getTypeName(String paramName) {
		return null;
	}

	/**
	 * 如果可能，枚举所有可用的参数名称。 <p>这是一个可选操作，主要与{@link
	 * org.springframework.jdbc.core.simple.SimpleJdbcInsert}和{@link
	 * org.springframework.jdbc.core.simple.SimpleJdbcCall}一起使用。
	 * @return 参数名称数组，如果无法确定则为 {@code null}
	 * @since 5.0.3
	 * @see SqlParameterSourceUtils#extractCaseInsensitiveParameterNames
	 */
	default String @Nullable [] getParameterNames() {
		return null;
	}

}
