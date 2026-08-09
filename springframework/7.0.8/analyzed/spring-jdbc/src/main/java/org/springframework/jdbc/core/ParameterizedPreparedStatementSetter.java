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

package org.springframework.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * {@link JdbcTemplate} 类用于批量更新的参数化回调接口。
 *
 * <p>本接口在 JdbcTemplate 提供的 {@link java.sql.PreparedStatement} 上，
 * 为使用相同 SQL 的一批更新中的每次更新设置值。
 * 实现者负责设置所有必要参数；带占位符的 SQL 已预先提供。
 *
 * <p>实现者<i>无需</i>关心其操作可能抛出的 SQLExceptions。
 * JdbcTemplate 会适当捕获并处理 SQLExceptions。
 *
 * @author Nicolas Fabre
 * @author Thomas Risberg
 * @since 3.1
 * @param <T> 参数类型
 * @see JdbcTemplate#batchUpdate(String, java.util.Collection, int, ParameterizedPreparedStatementSetter)
 */
@FunctionalInterface
public interface ParameterizedPreparedStatementSetter<T> {

	/**
	 * 在给定 PreparedStatement 上设置参数值。
	 * @param ps 要调用 setter 方法的 PreparedStatement
	 * @param argument 包含待设置值的对象
	 * @throws SQLException 若遇到 SQLException（即无需捕获 SQLException）
	 */
	void setValues(PreparedStatement ps, T argument) throws SQLException;

}
