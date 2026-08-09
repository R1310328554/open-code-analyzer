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

/**
 * 可复用查询，具体子类必须实现抽象方法 mapRow(ResultSet, int)，
 * 将 JDBC ResultSet 的每一行转换为对象。
 *
 * <p>通过省略参数和上下文简化 MappingSqlQueryWithParameters API。
 * 大多数子类不关心参数；若不需要上下文信息，应继承本类而非 MappingSqlQueryWithParameters。
 *
 * @author Rod Johnson
 * @author Thomas Risberg
 * @author Jean-Pierre Pawlak
 * @param <T> 结果类型
 * @see MappingSqlQueryWithParameters
 */
public abstract class MappingSqlQuery<T extends @Nullable Object> extends MappingSqlQueryWithParameters<T> {

	/**
	 * 允许作为 JavaBean 使用的构造器。
	 */
	public MappingSqlQuery() {
	}

	/**
	 * 便捷构造器，接收 DataSource 和 SQL 字符串。
	 * @param ds 用于获取连接的 DataSource
	 * @param sql 要执行的 SQL
	 */
	public MappingSqlQuery(DataSource ds, String sql) {
		super(ds, sql);
	}


	/**
	 * 本方法调用更简单的 mapRow 模板方法，忽略参数。
	 * @see #mapRow(ResultSet, int)
	 */
	@Override
	protected final T mapRow(ResultSet rs, int rowNum, @Nullable Object @Nullable [] parameters, @Nullable Map<?, ?> context)
			throws SQLException {

		return mapRow(rs, rowNum);
	}

	/**
	 * 子类必须实现本方法，将 ResultSet 的每一行转换为结果类型的对象。
	 * <p>与本类子类不同，MappingSqlQueryWithParameters 的直接子类
	 * 无需关心查询对象 execute 方法的参数。
	 * @param rs 正在遍历的 ResultSet
	 * @param rowNum 当前行号（从 0 开始）
	 * @return 结果类型的对象
	 * @throws SQLException 提取数据出错时抛出。
	 * 子类通常无需捕获 SQLException，由框架负责清理。
	 */
	protected abstract T mapRow(ResultSet rs, int rowNum) throws SQLException;

}
