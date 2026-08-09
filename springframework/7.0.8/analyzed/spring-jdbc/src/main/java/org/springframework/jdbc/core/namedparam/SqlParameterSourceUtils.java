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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import org.springframework.jdbc.core.SqlParameterValue;

/**
 * 为 {@link SqlParameterSource} 的使用提供辅助方法的类，
 * 尤其配合 {@link NamedParameterJdbcTemplate} 使用。
 *
 * @author Thomas Risberg
 * @author Juergen Hoeller
 * @since 2.5
 */
public abstract class SqlParameterSourceUtils {

	/**
	 * 创建 {@link SqlParameterSource} 对象数组，用传入值（{@link Map} 或 bean 对象）填充。
	 * 这将定义批操作中包含的内容。
	 * @param candidates 包含待使用值的对象数组
	 * @return {@link SqlParameterSource} 数组
	 * @see MapSqlParameterSource
	 * @see BeanPropertySqlParameterSource
	 * @see NamedParameterJdbcTemplate#batchUpdate(String, SqlParameterSource[])
	 */
	public static SqlParameterSource[] createBatch(Object... candidates) {
		return createBatch(Arrays.asList(candidates));
	}

	/**
	 * 创建 {@link SqlParameterSource} 对象数组，用传入值（{@link Map} 或 bean 对象）填充。
	 * 这将定义批操作中包含的内容。
	 * @param candidates 包含待使用值的集合
	 * @return {@link SqlParameterSource} 数组
	 * @since 5.0.2
	 * @see MapSqlParameterSource
	 * @see BeanPropertySqlParameterSource
	 * @see NamedParameterJdbcTemplate#batchUpdate(String, SqlParameterSource[])
	 */
	@SuppressWarnings({"rawtypes", "unchecked"})
	public static SqlParameterSource[] createBatch(Collection<?> candidates) {
		SqlParameterSource[] batch = new SqlParameterSource[candidates.size()];
		int i = 0;
		for (Object candidate : candidates) {
			batch[i] = (candidate instanceof Map map ? new MapSqlParameterSource(map) :
					new BeanPropertySqlParameterSource(candidate));
			i++;
		}
		return batch;
	}

	/**
	 * 创建 {@link MapSqlParameterSource} 对象数组，用传入值填充。
	 * 这将定义批操作中包含的内容。
	 * @param valueMaps 包含待使用值的 {@link Map} 实例数组
	 * @return {@link SqlParameterSource} 数组
	 * @see MapSqlParameterSource
	 * @see NamedParameterJdbcTemplate#batchUpdate(String, Map[])
	 */
	public static SqlParameterSource[] createBatch(Map<String, ?>[] valueMaps) {
		SqlParameterSource[] batch = new SqlParameterSource[valueMaps.length];
		for (int i = 0; i < valueMaps.length; i++) {
			batch[i] = new MapSqlParameterSource(valueMaps[i]);
		}
		return batch;
	}

	/**
	 * 若参数有类型信息则创建包装值，否则返回普通对象。
	 * @param source 参数值和类型信息的来源
	 * @param parameterName 参数名称
	 * @return 值对象
	 * @see SqlParameterValue
	 */
	public static @Nullable Object getTypedValue(SqlParameterSource source, String parameterName) {
		int sqlType = source.getSqlType(parameterName);
		if (sqlType != SqlParameterSource.TYPE_UNKNOWN) {
			return new SqlParameterValue(sqlType, source.getTypeName(parameterName), source.getValue(parameterName));
		}
		else {
			return source.getValue(parameterName);
		}
	}

	/**
	 * 创建不区分大小写的参数名称与原始名称的 Map。
	 * @param parameterSource 参数名称来源
	 * @return 可用于不区分大小写匹配参数名称的 Map
	 */
	public static Map<String, String> extractCaseInsensitiveParameterNames(SqlParameterSource parameterSource) {
		Map<String, String> caseInsensitiveParameterNames = new HashMap<>();
		String[] paramNames = parameterSource.getParameterNames();
		if (paramNames != null) {
			for (String name : paramNames) {
				caseInsensitiveParameterNames.put(name.toLowerCase(Locale.ROOT), name);
			}
		}
		return caseInsensitiveParameterNames;
	}

}
