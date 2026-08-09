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

import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

import org.jspecify.annotations.Nullable;

import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.util.Assert;

/**
 * {@link SqlParameterSource} 实现的抽象基类。
 * 提供按参数注册 SQL 类型，并为实现了 {@link #getParameterNames()} 的
 * {@code SqlParameterSource} 提供友好的 {@link #toString() toString} 表示，
 * 枚举所有参数。具体子类必须实现 {@link #hasValue} 和 {@link #getValue}。
 *
 * @author Juergen Hoeller
 * @author Jens Schauder
 * @since 2.0
 * @see #hasValue(String)
 * @see #getValue(String)
 * @see #getParameterNames()
 */
public abstract class AbstractSqlParameterSource implements SqlParameterSource {

	private final Map<String, Integer> sqlTypes = new HashMap<>();

	private final Map<String, String> typeNames = new HashMap<>();


	/**
	 * 为给定参数注册 SQL 类型。
	 * @param paramName 参数名称
	 * @param sqlType 参数的 SQL 类型
	 */
	public void registerSqlType(String paramName, int sqlType) {
		Assert.notNull(paramName, "Parameter name must not be null");
		this.sqlTypes.put(paramName, sqlType);
	}

	/**
	 * 为给定参数注册类型名称。
	 * @param paramName 参数名称
	 * @param typeName 参数的类型名称
	 */
	public void registerTypeName(String paramName, String typeName) {
		Assert.notNull(paramName, "Parameter name must not be null");
		this.typeNames.put(paramName, typeName);
	}

	/**
	 * 返回给定参数已注册的 SQL 类型。
	 * @param paramName 参数名称
	 * @return 参数的 SQL 类型，未注册时返回 {@code TYPE_UNKNOWN}
	 */
	@Override
	public int getSqlType(String paramName) {
		Assert.notNull(paramName, "Parameter name must not be null");
		return this.sqlTypes.getOrDefault(paramName, TYPE_UNKNOWN);
	}

	/**
	 * 返回给定参数已注册的类型名称。
	 * @param paramName 参数名称
	 * @return 参数的类型名称，未注册时返回 {@code null}
	 */
	@Override
	public @Nullable String getTypeName(String paramName) {
		Assert.notNull(paramName, "Parameter name must not be null");
		return this.typeNames.get(paramName);
	}


	/**
	 * 枚举参数名称和值及其对应的 SQL 类型（若有），
	 * 否则仅返回 {@code SqlParameterSource} 实现类的简单类名。
	 * @since 5.2
	 * @see #getParameterNames()
	 */
	@Override
	public String toString() {
		String[] parameterNames = getParameterNames();
		if (parameterNames != null) {
			StringJoiner result = new StringJoiner(", ", getClass().getSimpleName() + " {", "}");
			for (String parameterName : parameterNames) {
				Object value = getValue(parameterName);
				if (value instanceof SqlParameterValue sqlParameterValue) {
					value = sqlParameterValue.getValue();
				}
				String typeName = getTypeName(parameterName);
				if (typeName == null) {
					int sqlType = getSqlType(parameterName);
					if (sqlType != TYPE_UNKNOWN) {
						typeName = JdbcUtils.resolveTypeName(sqlType);
						if (typeName == null) {
							typeName = String.valueOf(sqlType);
						}
					}
				}
				StringBuilder entry = new StringBuilder();
				entry.append(parameterName).append('=').append(value);
				if (typeName != null) {
					entry.append(" (type:").append(typeName).append(')');
				}
				result.add(entry);
			}
			return result.toString();
		}
		else {
			return getClass().getSimpleName();
		}
	}

}
