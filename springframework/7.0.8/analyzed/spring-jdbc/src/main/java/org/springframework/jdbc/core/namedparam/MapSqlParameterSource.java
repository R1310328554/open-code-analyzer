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
 * 持有给定参数 Map 的 {@link SqlParameterSource} 实现。
 *
 * <p>本类用于向 {@link NamedParameterJdbcTemplate} 类的方法
 * 传入简单的参数值 Map。
 *
 * <p>本类的 {@code addValue} 方法便于添加多个值。
 * 方法返回 {@link MapSqlParameterSource} 自身引用，
 * 可在单条语句中链式调用多个方法。
 *
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
	 * 创建空的 MapSqlParameterSource，
	 * 值通过 {@code addValue} 添加。
	 * @see #addValue(String, Object)
	 */
	public MapSqlParameterSource() {
	}

	/**
	 * 创建新的 MapSqlParameterSource，包含一个由给定参数组成的值。
	 * @param paramName 参数名称
	 * @param value 参数值
	 * @see #addValue(String, Object)
	 */
	public MapSqlParameterSource(String paramName, @Nullable Object value) {
		addValue(paramName, value);
	}

	/**
	 * 基于 Map 创建新的 MapSqlParameterSource。
	 * @param values 持有现有参数值的 Map（可为 {@code null}）
	 */
	public MapSqlParameterSource(@Nullable Map<String, ?> values) {
		addValues(values);
	}


	/**
	 * 向此参数源添加参数。
	 * @param paramName 参数名称
	 * @param value 参数值
	 * @return 此参数源的引用，便于链式调用
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
	 * 向此参数源添加参数。
	 * @param paramName 参数名称
	 * @param value 参数值
	 * @param sqlType 参数的 SQL 类型
	 * @return 此参数源的引用，便于链式调用
	 */
	public MapSqlParameterSource addValue(String paramName, @Nullable Object value, int sqlType) {
		Assert.notNull(paramName, "Parameter name must not be null");
		this.values.put(paramName, value);
		registerSqlType(paramName, sqlType);
		return this;
	}

	/**
	 * 向此参数源添加参数。
	 * @param paramName 参数名称
	 * @param value 参数值
	 * @param sqlType 参数的 SQL 类型
	 * @param typeName 参数的类型名称
	 * @return 此参数源的引用，便于链式调用
	 */
	public MapSqlParameterSource addValue(String paramName, @Nullable Object value, int sqlType, String typeName) {
		Assert.notNull(paramName, "Parameter name must not be null");
		this.values.put(paramName, value);
		registerSqlType(paramName, sqlType);
		registerTypeName(paramName, typeName);
		return this;
	}

	/**
	 * 向此参数源添加参数 Map。
	 * @param values 持有现有参数值的 Map（可为 {@code null}）
	 * @return 此参数源的引用，便于链式调用
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
	 * 以只读 Map 形式暴露当前参数值。
	 */
	public Map<String, Object> getValues() {
		return Collections.unmodifiableMap(this.values);
	}


	@Override
	public boolean hasValue(String paramName) {
		return this.values.containsKey(paramName);
	}

	@Override
	public @Nullable Object getValue(String paramName) {
		if (!hasValue(paramName)) {
			throw new IllegalArgumentException("No value registered for key '" + paramName + "'");
		}
		return this.values.get(paramName);
	}

	@Override
	public String[] getParameterNames() {
		return StringUtils.toStringArray(this.values.keySet());
	}

}
