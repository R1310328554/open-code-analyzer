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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * {@link SqlParameterSource} 实现保存给定的参数映射。
 * <p> 该类旨在将参数值的简单映射传递给 {@link NamedParameterJdbcTemplate} 类的方法。
 * <p> 此类上的 {@code addValue} 方法将使添加多个值变得更加容易。这些方法返回对 {@link MapSqlParameterSource} 本身的引用，因此
 * 您可以在单个语句中将多个方法调用链接在一起。
 * @author Thomas Risberg
 * @author Juergen Hoeller
 * @since 2.0
 * @see #addValue(String, Object)
 * @see #addValue(String, Object, int)
 * @see #registerSqlType
 * @see NamedParameterJdbcTemplate
 */
public class MapSqlParameterSource extends AbstractSqlParameterSource {

	private final Map<String, Object> values = new LinkedHashMap<>();


	/**
	 * 创建一个空的 MapSqlParameterSource，并通过 {@code addValue} 添加值。
	 * @see #addValue(String, Object)
	 */
	public MapSqlParameterSource() {
	}

	/**
	 * 创建一个新的 MapSqlParameterSource，其中一个值由提供的参数组成。
	 * @param paramName 参数名称
	 * @param value 参数的值
	 * @see #addValue(String, Object)
	 */
	public MapSqlParameterSource(String paramName, @Nullable Object value) {
		addValue(paramName, value);
	}

	/**
	 * 基于Map创建一个新的MapSqlParameterSource。
	 * @param values 保存现有参数值的 Map（可以是 {@code null}）
	 */
	public MapSqlParameterSource(@Nullable Map<String, ?> values) {
		addValues(values);
	}


	/**
	 * 在此参数源中添加一个参数。
	 * @param paramName 参数名称
	 * @param value 参数的值
	 * @return 引用此参数源，因此可以将多个调用链接在一起
	 */
	public MapSqlParameterSource addValue(String paramName, @Nullable Object value) {
		Assert.notNull(paramName, "Parameter name must not be null");
		this.values.put(paramName, value);
		if (value instanceof SqlParameterValue sqlParameterValue) {
			registerSqlType(paramName, sqlParameterValue.getSqlType());
		}
		return this;
	}

	/**
	 * 在此参数源中添加一个参数。
	 * @param paramName 参数名称
	 * @param value 参数的值
	 * @param sqlType 参数的 SQL 类型
	 * @return 引用此参数源，因此可以将多个调用链接在一起
	 */
	public MapSqlParameterSource addValue(String paramName, @Nullable Object value, int sqlType) {
		Assert.notNull(paramName, "Parameter name must not be null");
		this.values.put(paramName, value);
		registerSqlType(paramName, sqlType);
		return this;
	}

	/**
	 * 在此参数源中添加一个参数。
	 * @param paramName 参数名称
	 * @param value 参数的值
	 * @param sqlType 参数的 SQL 类型
	 * @param typeName 参数的类型名称
	 * @return 引用此参数源，因此可以将多个调用链接在一起
	 */
	public MapSqlParameterSource addValue(String paramName, @Nullable Object value, int sqlType, String typeName) {
		Assert.notNull(paramName, "Parameter name must not be null");
		this.values.put(paramName, value);
		registerSqlType(paramName, sqlType);
		registerTypeName(paramName, typeName);
		return this;
	}

	/**
	 * 将参数映射添加到此参数源。
	 * @param values 保存现有参数值的 Map（可以是 {@code null}）
	 * @return 引用此参数源，因此可以将多个调用链接在一起
	 */
	public MapSqlParameterSource addValues(@Nullable Map<String, ?> values) {
		if (values != null) {
			values.forEach((key, value) -> {
				this.values.put(key, value);
				if (value instanceof SqlParameterValue sqlParameterValue) {
					registerSqlType(key, sqlParameterValue.getSqlType());
				}
			});
		}
		return this;
	}

	/**
	 * 返回此参数源是否已配置任何值。
	 * @since 6.1
	 */
	public boolean hasValues() {
		return !this.values.isEmpty();
	}

	/**
	 * 将当前参数值公开为只读映射。
	 */
	public Map<String, Object> getValues() {
		return Collections.unmodifiableMap(this.values);
	}


	/**
	 * 判断是否包含/具备 Value。
	 */
	@Override
	public boolean hasValue(String paramName) {
		return this.values.containsKey(paramName);
	}

	/**
	 * 获取 Value（`Value`）。
	 */
	@Override
	public @Nullable Object getValue(String paramName) {
		if (!hasValue(paramName)) {
			throw new IllegalArgumentException("No value registered for key '" + paramName + "'");
		}
		return this.values.get(paramName);
	}

	/**
	 * 获取 Parameter Names（`ParameterNames`）。
	 */
	@Override
	public String[] getParameterNames() {
		return StringUtils.toStringArray(this.values.keySet());
	}

}
