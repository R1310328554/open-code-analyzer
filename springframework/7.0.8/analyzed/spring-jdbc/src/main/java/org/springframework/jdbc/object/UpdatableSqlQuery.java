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
 * 可重用的 RDBMS 查询，其中具体子类必须实现抽象 updateRow(ResultSet, int, context) 方法来更新 JDBC ResultSet
 * 的每一行，并可选择将内容映射到对象中。
 * 可以构建 <p> 子类，提供 SQL、参数类型和数据源。 SQL 在子类之间通常会有所不同。
 * @author Thomas Risberg
 * @param <T> 结果类型
 * @see org.springframework.jdbc.object.SqlQuery
 */
public abstract class UpdatableSqlQuery<T> extends SqlQuery<T> {

	/**
	 * 允许用作 JavaBean 的构造函数。
	 */
	public UpdatableSqlQuery() {
		setUpdatableResults(true);
	}

	/**
	 * 带有 DataSource 和 SQL 字符串的便捷构造函数。
	 * @param ds 用于获取连接的数据源
	 * @param sql 要运行的 SQL
	 */
	public UpdatableSqlQuery(DataSource ds, String sql) {
		super(ds, sql);
		setUpdatableResults(true);
	}


	/**
	 * 超类模板方法的实现。这将调用 {@code updateRow()} 方法的子类实现。
	 */
	@Override
	protected RowMapper<T> newRowMapper(@Nullable Object @Nullable [] parameters, @Nullable Map<?, ?> context) {
		return new RowMapperImpl(context);
	}

	/**
	 * 子类必须实现此方法来更新 ResultSet 的每一行，并可以选择创建结果类型的对象。
	 * @param rs 我们正在处理的 ResultSet
	 * @param rowNum 我们要做的行号（从 0 开始）
	 * @param context 传递给 {@code execute()} 方法。如果不需要上下文信息，可以是 {@code null}。如果需要传入每一行的数据，可以传入一个HashMap，以该行的主键作为HashMap的键。这样就可以轻松找到每一行的更新
	 * @return 结果类型的对象
	 * @throws SQLException 如果更新数据时出现错误。子类根本无法捕获 SQLException，只能依靠框架来清理。
	 */
	protected abstract T updateRow(ResultSet rs, int rowNum, @Nullable Map<?, ?> context) throws SQLException;


	/**
	 * RowMapper 的实现，为每一行调用封闭类的 {@code updateRow()} 方法。
	 */
	protected class RowMapperImpl implements RowMapper<T> {

		private final @Nullable Map<?, ?> context;

		public RowMapperImpl(@Nullable Map<?, ?> context) {
			this.context = context;
		}

		@Override
		public T mapRow(ResultSet rs, int rowNum) throws SQLException {
			T result = updateRow(rs, rowNum, this.context);
			rs.updateRow();
			return result;
		}
	}

}
