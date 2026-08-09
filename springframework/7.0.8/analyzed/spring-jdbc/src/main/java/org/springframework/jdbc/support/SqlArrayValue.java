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
 * 基于 JDBC 4 {@link java.sql.Connection#createArrayOf} 方法创建 JDBC {@link Array} 的通用 {@link SqlValue} 实现。
 *
 * <p>也可作为需要清理的自定义 {@link SqlValue} 实现的模板。
 *
 * @author Juergen Hoeller
 * @author Philippe Marschall
 * @since 6.1
 */
public class SqlArrayValue implements SqlValue {

	private final String typeName;

	private final Object[] elements;

	private @Nullable Array array;


	/**
	 * 为给定类型名和元素创建 {@code SqlArrayValue}。
	 * @param typeName 数组元素映射到的 SQL 类型名
	 * @param elements 用于填充 {@code Array} 对象的元素
	 * @see java.sql.Connection#createArrayOf
	 */
	public SqlArrayValue(String typeName, Object... elements) {
		Assert.notNull(typeName, "Type name must not be null");
		Assert.notNull(elements, "Elements array must not be null");
		this.typeName = typeName;
		this.elements = elements;
	}


	@Override
	public void setValue(PreparedStatement ps, int paramIndex) throws SQLException {
		this.array = ps.getConnection().createArrayOf(this.typeName, this.elements);
		ps.setArray(paramIndex, this.array);
	}

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
