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
 * 可复用的 RDBMS 查询，具体子类必须实现抽象方法 updateRow(ResultSet, int, context)，
 * 更新 JDBC ResultSet 的每一行，并可选地将内容映射为对象。
 *
 * <p>子类构造时可提供 SQL、参数类型和 DataSource，SQL 通常因子类而异。
 *
 * @author Thomas Risberg
 * @param <T> 结果类型
 * @see org.springframework.jdbc.object.SqlQuery
 */
public abstract class UpdatableSqlQuery<T> extends SqlQuery<T> {

	/**
	 * 允许作为 JavaBean 使用的构造器。
	 */
	public UpdatableSqlQuery() {
		setUpdatableResults(true);
	}

	/**
	 * 便捷构造器，接收 DataSource 和 SQL 字符串。
	 * @param ds 用于获取连接的 DataSource
	 * @param sql 要执行的 SQL
	 */
	public UpdatableSqlQuery(DataSource ds, String sql) {
		super(ds, sql);
		setUpdatableResults(true);
	}


	/**
	 * 父类模板方法的实现，调用子类的 {@code updateRow()} 方法。
	 */
	@Override
	protected RowMapper<T> newRowMapper(@Nullable Object @Nullable [] parameters, @Nullable Map<?, ?> context) {
		return new RowMapperImpl(context);
	}

	/**
	 * 子类必须实现本方法，更新 ResultSet 的每一行，并可选地创建结果类型对象。
	 * @param rs 正在遍历的 ResultSet
	 * @param rowNum 当前行号（从 0 开始）
	 * @param context 传入 {@code execute()} 方法的上下文，无上下文信息时可 {@code null}；
	 * 若需为每行传入数据，可使用以行主键为键的 HashMap，便于定位每行的更新
	 * @return 结果类型的对象
	 * @throws SQLException 更新数据出错时抛出。
	 * 子类通常无需捕获 SQLException，由框架负责清理。
	 */
	protected abstract T updateRow(ResultSet rs, int rowNum, @Nullable Map<?, ?> context) throws SQLException;


	/**
	 * RowMapper 实现，对每一行调用外部类的 {@code updateRow()} 方法。
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
