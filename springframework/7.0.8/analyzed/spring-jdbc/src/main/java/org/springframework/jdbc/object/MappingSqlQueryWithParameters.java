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
 * 可复用的 RDBMS 查询，具体子类必须实现抽象方法 mapRow(ResultSet, int)，
 * 将 JDBC ResultSet 的每一行映射为对象。
 *
 * <p>这种手动映射通常优于基于反射的"自动"映射——后者在非平凡场景下会变得复杂。
 * 例如，本类允许不同行使用不同对象（例如根据指示选择子类）、设置计算字段，
 * 且 ResultSet 列名无需与 Bean 属性同名。
 * 帕累托原则在此体现：为自动化提取过程额外投入会使框架复杂得多，收益却有限。
 *
 * <p>子类构造时可提供 SQL、参数类型和 DataSource，SQL 通常因子类而异。
 *
 * @author Rod Johnson
 * @author Thomas Risberg
 * @author Jean-Pierre Pawlak
 * @param <T> 结果类型
 * @see org.springframework.jdbc.object.MappingSqlQuery
 * @see org.springframework.jdbc.object.SqlQuery
 */
public abstract class MappingSqlQueryWithParameters<T extends @Nullable Object> extends SqlQuery<T> {

	/**
	 * 允许作为 JavaBean 使用的构造器。
	 */
	public MappingSqlQueryWithParameters() {
	}

	/**
	 * 便捷构造器，接收 DataSource 和 SQL 字符串。
	 * @param ds 用于获取连接的 DataSource
	 * @param sql 要执行的 SQL
	 */
	public MappingSqlQueryWithParameters(DataSource ds, String sql) {
		super(ds, sql);
	}


	/**
	 * 受保护抽象方法的实现，调用子类的 mapRow() 方法。
	 */
	@Override
	protected RowMapper<T> newRowMapper(@Nullable Object @Nullable [] parameters, @Nullable Map<?, ?> context) {
		return new RowMapperImpl(parameters, context);
	}

	/**
	 * 子类必须实现本方法，将 ResultSet 的每一行转换为结果类型的对象。
	 * @param rs 正在遍历的 ResultSet
	 * @param rowNum 当前行号（从 0 开始）
	 * @param parameters 查询参数（传入 execute() 方法），子类通常不关心，无参数时可 {@code null}
	 * @param context 传入 execute() 方法的上下文，无上下文信息时可 {@code null}
	 * @return 结果类型的对象
	 * @throws SQLException 提取数据出错时抛出。
	 * 子类通常无需捕获 SQLException，由框架负责清理。
	 */
	protected abstract T mapRow(ResultSet rs, int rowNum, @Nullable Object @Nullable [] parameters, @Nullable Map<?, ?> context)
			throws SQLException;


	/**
	 * RowMapper 实现，对每一行调用外部类的 {@code mapRow} 方法。
	 */
	protected class RowMapperImpl implements RowMapper<T> {

		private final @Nullable Object @Nullable [] params;

		private final @Nullable Map<?, ?> context;

		/**
		 * 构造 RowMapper 实现，若已知结果数量则更高效。
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
