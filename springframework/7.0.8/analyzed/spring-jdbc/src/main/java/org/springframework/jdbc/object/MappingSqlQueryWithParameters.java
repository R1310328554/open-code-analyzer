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

package org.springframework.jdbc.object;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import javax.sql.DataSource;

import org.jspecify.annotations.Nullable;

import org.springframework.jdbc.core.RowMapper;

/**
 * 可重用的 RDBMS 查询，其中具体子类必须实现抽象 mapRow(ResultSet, int) 方法以将 JDBC ResultSet 的每一行映射到对象中。
 * <p>此类手动映射通常优于使用反射的“自动”映射，后者在重要情况下可能会变得复杂。例如，当前类允许不同的对象用于不同的行（例如，如果指示子类）。它允许设置计算字段。并且 Res
 * ultSet 列不需要与 bean 属性具有相同的名称。帕累托原则的实际应用：加倍努力实现提取过程的自动化，使框架变得更加复杂，并且几乎没有带来真正的好处。
 * 可以构建 <p> 子类，提供 SQL、参数类型和数据源。 SQL 在子类之间通常会有所不同。
 * @author Rod Johnson
 * @author Thomas Risberg
 * @author Jean-Pierre Pawlak
 * @param <T> 结果类型
 * @see org.springframework.jdbc.object.MappingSqlQuery
 * @see org.springframework.jdbc.object.SqlQuery
 */
public abstract class MappingSqlQueryWithParameters<T extends @Nullable Object> extends SqlQuery<T> {

	/**
	 * 允许用作 JavaBean 的构造函数。
	 */
	public MappingSqlQueryWithParameters() {
	}

	/**
	 * 带有 DataSource 和 SQL 字符串的便捷构造函数。
	 * @param ds 用于获取连接的数据源
	 * @param sql 要运行的 SQL
	 */
	public MappingSqlQueryWithParameters(DataSource ds, String sql) {
		super(ds, sql);
	}


	/**
	 * 受保护的抽象方法的实现。这将调用子类的 mapRow() 方法的实现。
	 */
	@Override
	protected RowMapper<T> newRowMapper(@Nullable Object @Nullable [] parameters, @Nullable Map<?, ?> context) {
		return new RowMapperImpl(parameters, context);
	}

	/**
	 * 子类必须实现此方法才能将 ResultSet 的每一行转换为结果类型的对象。
	 * @param rs 我们正在处理的 ResultSet
	 * @param rowNum 我们要做的行号（从 0 开始）
	 * @param parameters 到查询（传递给execute()方法）。子类很少对这些感兴趣。如果没有参数可以是{@code null}。
	 * @param context 传递给execute()方法。如果不需要上下文信息，可以是 {@code null}。
	 * @return 结果类型的对象
	 * @throws SQLException 如果提取数据时出现错误。子类根本无法捕获 SQLException，只能依靠框架来清理。
	 */
	protected abstract T mapRow(ResultSet rs, int rowNum, @Nullable Object @Nullable [] parameters, @Nullable Map<?, ?> context)
			throws SQLException;


	/**
	 * RowMapper 的实现，为每一行调用封闭类的 {@code mapRow} 方法。
	 */
	protected class RowMapperImpl implements RowMapper<T> {

		private final @Nullable Object @Nullable [] params;

		private final @Nullable Map<?, ?> context;

		/**
		 * 使用数组结果。如果我们知道预期有多少结果，效率就会更高。
		 */
		public RowMapperImpl(@Nullable Object @Nullable [] parameters, @Nullable Map<?, ?> context) {
			this.params = parameters;
			this.context = context;
		}

		@Override
		public T mapRow(ResultSet rs, int rowNum) throws SQLException {
			return MappingSqlQueryWithParameters.this.mapRow(rs, rowNum, this.params, this.context);
		}
	}

}
