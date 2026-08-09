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
 * 可重用查询，其中具体子类必须实现抽象 mapRow(ResultSet, int) 方法以将 JDBC ResultSet 的每一行转换为对象。
 * <p>S 通过删除参数和上下文来简化 MappingSqlQueryWithParameters
 * API。大多数子类不会关心参数。如果您不使用上下文信息，请对其进行子类化而不是 MappingSqlQueryWithParameters。
 * @author Rod Johnson
 * @author Thomas Risberg
 * @author Jean-Pierre Pawlak
 * @param <T> 结果类型
 * @see MappingSqlQueryWithParameters
 */
public abstract class MappingSqlQuery<T extends @Nullable Object> extends MappingSqlQueryWithParameters<T> {

	/**
	 * 允许用作 JavaBean 的构造函数。
	 */
	public MappingSqlQuery() {
	}

	/**
	 * 带有 DataSource 和 SQL 字符串的便捷构造函数。
	 * @param ds 用于获取连接的 DataSource
	 * @param sql 要运行的 SQL
	 */
	public MappingSqlQuery(DataSource ds, String sql) {
		super(ds, sql);
	}


	/**
	 * 实现此方法是为了调用更简单的 mapRow 模板方法，忽略参数。
	 * @see #mapRow(ResultSet, int)
	 */
	@Override
	protected final T mapRow(ResultSet rs, int rowNum, @Nullable Object @Nullable [] parameters, @Nullable Map<?, ?> context)
			throws SQLException {

		return mapRow(rs, rowNum);
	}

	/**
	 * 子类必须实现此方法才能将 ResultSet 的每一行转换为结果类型的对象。与 MappingSqlQueryWithParameters 的直接子类相反，此类的 <p> 子类
	 * 不需要关心查询对象的执行方法的参数。
	 * @param rs 我们正在处理的 ResultSet
	 * @param rowNum 我们要做的行号（从 0 开始）
	 * @return 结果类型的对象
	 * @throws SQLException 如果提取数据时出现错误。子类根本无法捕获 SQLException，只能依靠框架来清理。
	 */
	protected abstract T mapRow(ResultSet rs, int rowNum) throws SQLException;

}
