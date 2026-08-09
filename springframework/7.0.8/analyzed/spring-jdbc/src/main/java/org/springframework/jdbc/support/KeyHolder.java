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
 * 用于检索键的接口，通常用于自动生成的键（可能由 JDBC 插入语句返回）。
 * <p> 该接口的实现可以容纳任意数量的键。在一般情况下，键以列表的形式返回，其中每行键包含一个映射。
 * <p>大多数应用程序每行仅使用一个键，并且在插入语句中一次仅处理一行。在这些情况下，只需调用 {@link #getKey() getKey} 或 {@link #getKey
 * As(Class) getKeyAs} 即可检索密钥。 {@code getKey} 返回的值是 {@link Number}，这是自动生成键的常用类型。对于任何其他自动生成的
 * 密钥类型，请改用 {@code getKeyAs}。
 * @author Thomas Risberg
 * @author Juergen Hoeller
 * @author Slawomir Dymitrow
 * @since 1.1
 * @see org.springframework.jdbc.core.JdbcTemplate
 * @see org.springframework.jdbc.object.SqlUpdate
 */
public interface KeyHolder {

	/**
	 * 从第一个地图中检索第一个项目，假设只有一个项目和一个地图，并且该项目是一个数字。这是典型的情况：单个数字生成的密钥。 <p>Keys 保存在映射列表中，其中列表中的每个项目代表
	 * 每行的键。如果有多个列，则映射也将有多个条目。如果此方法在映射或列表中遇到多个条目（意味着返回了多个键），则会引发 InvalidDataAccessApiUsageExcep
	 * tion。
	 * @return 生成的密钥为数字
	 * @throws InvalidDataAccessApiUsageException 如果遇到多个键
	 * @see #getKeyAs(Class)
	 */
	@Nullable Number getKey() throws InvalidDataAccessApiUsageException;

	/**
	 * 从第一个映射中检索第一个项目，假设只有一个项目和一个映射，并且该项目是指定类型的实例。这是一种常见情况：指定类型的单个生成密钥。 <p>Keys 保存在映射列表中，其中列表中的
	 * 每个项目代表每行的键。如果有多个列，则映射也将有多个条目。如果此方法在映射或列表中遇到多个条目（意味着返回了多个键），则会引发 InvalidDataAccessApiUsag
	 * eException。
	 * @param keyType 自动生成的密钥的类型
	 * @return 生成的密钥作为指定类型的实例
	 * @throws InvalidDataAccessApiUsageException 如果遇到多个键
	 * @since 5.3
	 * @see #getKey()
	 */
	<T> @Nullable T getKeyAs(Class<T> keyType) throws InvalidDataAccessApiUsageException;

	/**
	 * 检索第一个键映射。 <p>如果列表中有多个条目（意味着多行返回了键），则抛出 InvalidDataAccessApiUsageException。
	 * @return 单行生成的键的映射
	 * @throws InvalidDataAccessApiUsageException 如果遇到多行的键
	 */
	@Nullable Map<String, Object> getKeys() throws InvalidDataAccessApiUsageException;

	/**
	 * 返回对包含键的列表的引用。 <p> 可用于提取多行的键（一种不寻常的情况），也可用于添加新的键映射。
	 * @return 生成的键的列表，每个条目代表通过列名称和键值的映射的单独行
	 */
	List<Map<String, Object>> getKeyList();

}
