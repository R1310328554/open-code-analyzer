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

package org.springframework.jdbc.support;

import java.util.List;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import org.springframework.dao.InvalidDataAccessApiUsageException;

/**
 * 用于检索主键的接口，通常用于 JDBC insert 语句可能返回的自动生成键。
 *
 * <p>本接口的实现可保存任意数量的键。一般情况下，键以 List 形式返回，
 * 其中每个元素是一个 Map，对应一行键值。
 *
 * <p>大多数应用每行仅使用一个键，且 insert 语句一次只处理一行。
 * 此时直接调用 {@link #getKey() getKey} 或 {@link #getKeyAs(Class) getKeyAs} 即可。
 * {@code getKey} 返回 {@link Number}，这是自动生成键的常见类型；
 * 其他类型的自动生成键请使用 {@code getKeyAs}。
 *
 * @author Thomas Risberg
 * @author Juergen Hoeller
 * @author Slawomir Dymitrow
 * @since 1.1
 * @see org.springframework.jdbc.core.JdbcTemplate
 * @see org.springframework.jdbc.object.SqlUpdate
 */
public interface KeyHolder {

	/**
	 * 从第一个 Map 中取第一个元素，假定仅有一个 Map 且其中仅有一个元素，且该元素为数字。
	 * 这是典型场景：单个数值型自动生成键。
	 * <p>键保存在 Map 的 List 中，列表每项代表一行的键。
	 * 若有多列，Map 也会有多个条目。若 Map 或 List 中出现多个条目（即返回了多个键），
	 * 则抛出 InvalidDataAccessApiUsageException。
	 * @return 以数字形式返回的生成键
	 * @throws InvalidDataAccessApiUsageException 遇到多个键时
	 * @see #getKeyAs(Class)
	 */
	@Nullable Number getKey() throws InvalidDataAccessApiUsageException;

	/**
	 * 从第一个 Map 中取第一个元素，假定仅有一个 Map 且其中仅有一个元素，且该元素为指定类型实例。
	 * 这是常见场景：单个指定类型的自动生成键。
	 * <p>键保存在 Map 的 List 中，列表每项代表一行的键。
	 * 若有多列，Map 也会有多个条目。若 Map 或 List 中出现多个条目（即返回了多个键），
	 * 则抛出 InvalidDataAccessApiUsageException。
	 * @param keyType 自动生成键的类型
	 * @return 以指定类型实例形式返回的生成键
	 * @throws InvalidDataAccessApiUsageException 遇到多个键时
	 * @since 5.3
	 * @see #getKey()
	 */
	<T> @Nullable T getKeyAs(Class<T> keyType) throws InvalidDataAccessApiUsageException;

	/**
	 * 获取第一个键 Map。
	 * <p>若 List 中有多个条目（即多行都返回了键），则抛出 InvalidDataAccessApiUsageException。
	 * @return 单行生成键的 Map
	 * @throws InvalidDataAccessApiUsageException 遇到多行键时
	 */
	@Nullable Map<String, Object> getKeys() throws InvalidDataAccessApiUsageException;

	/**
	 * 返回包含键的 List 引用。
	 * <p>可用于提取多行键（少见场景），也可用于添加新的键 Map。
	 * @return 生成键的 List，每项通过列名到键值的 Map 表示一行
	 */
	List<Map<String, Object>> getKeyList();

}
