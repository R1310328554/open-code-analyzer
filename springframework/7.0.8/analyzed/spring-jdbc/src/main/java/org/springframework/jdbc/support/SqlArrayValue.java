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

import java.sql.Array;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.jspecify.annotations.Nullable;

import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.util.Assert;

/**
 * 基于 JDBC 4 {@link java.sql.Connection#createArrayOf} 方法的 JDBC {@link Array} 创建的通用 {@link
 * SqlValue} 实现。
 * <p>A 也可用作具有清理需求的自定义 {@link SqlValue} 实现的模板。
 * @author Juergen Hoeller
 * @author Philippe Marschall
 * @since 6.1
 */
public class SqlArrayValue implements SqlValue {

	/** 类型相关状态（`typeName`）。 */
	private final String typeName;

	/** `elements`：该类的成员状态。 */
	private final Object[] elements;

	/** `array`：该类的成员状态。 */
	private @Nullable Array array;


	/**
	 * 为给定的类型名称和元素创建一个新的 {@code SqlArrayValue}。
	 * @param typeName 数组元素映射到的类型的 SQL 名称
	 * @param elements 用于填充 {@code Array} 对象的元素
	 * @see java.sql.Connection#createArrayOf
	 */
	public SqlArrayValue(String typeName, Object... elements) {
		Assert.notNull(typeName, "Type name must not be null");
		Assert.notNull(elements, "Elements array must not be null");
		this.typeName = typeName;
		this.elements = elements;
	}


	/**
	 * 设置 Value（`Value`）。
	 */
	@Override
	public void setValue(PreparedStatement ps, int paramIndex) throws SQLException {
		this.array = ps.getConnection().createArrayOf(this.typeName, this.elements);
		ps.setArray(paramIndex, this.array);
	}

	/**
	 * 方法 `cleanup`：完成本类中与「cleanup」相关的职责。
	 */
	@Override
	public void cleanup() {
		if (this.array != null) {
			try {
				this.array.free();
			}
			catch (SQLException ex) {
				throw new DataAccessResourceFailureException("Could not free Array object", ex);
			}
		}
	}

}
