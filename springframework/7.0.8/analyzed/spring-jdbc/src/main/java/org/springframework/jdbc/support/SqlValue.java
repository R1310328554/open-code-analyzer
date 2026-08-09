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

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * 将复杂类型设为语句参数的简单接口。
 *
 * <p>实现负责设置实际值，须实现回调方法 {@code setValue}，
 * 其抛出的 SQLException 由调用方捕获并翻译。
 * 若需创建数据库特定对象，可通过给定 PreparedStatement 访问底层 Connection。
 *
 * @author Juergen Hoeller
 * @since 2.5.6
 * @see org.springframework.jdbc.core.SqlTypeValue
 * @see org.springframework.jdbc.core.DisposableSqlTypeValue
 */
public interface SqlValue {

	/**
	 * 在给定 PreparedStatement 上设置值。
	 * @param ps 要操作的 PreparedStatement
	 * @param paramIndex 需要设置值的参数索引
	 * @throws SQLException 设置参数值时遇到 SQLException
	 */
	void setValue(PreparedStatement ps, int paramIndex) throws SQLException;

	/**
	 * 清理本值对象持有的资源。
	 */
	void cleanup();

}
