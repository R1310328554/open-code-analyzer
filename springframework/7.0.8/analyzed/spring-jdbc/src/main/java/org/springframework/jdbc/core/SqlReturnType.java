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

import java.sql.CallableStatement;
import java.sql.SQLException;

import org.jspecify.annotations.Nullable;

/**
 * 用于读取标准 {@code CallableStatement.getObject} 不支持的
 * 复杂数据库特定类型值的接口。
 *
 * <p>实现类负责实际取值，须实现回调方法 {@code getTypeValue}；
 * 该方法可能抛出 SQLException，由调用方捕获并翻译。
 * 若需创建数据库特定对象，可通过给定 CallableStatement 访问底层 Connection。
 *
 * @author Thomas Risberg
 * @since 1.1
 * @see java.sql.Types
 * @see java.sql.CallableStatement#getObject
 * @see org.springframework.jdbc.object.StoredProcedure#execute(java.util.Map)
 */
public interface SqlReturnType {

	/**
	 * 表示未知（或未指定）SQL 类型的常量。
	 * 原始操作方法未指定 SQL 类型时传入 setTypeValue。
	 * @see java.sql.Types
	 * @see JdbcOperations#update(String, Object[])
	 */
	int TYPE_UNKNOWN = Integer.MIN_VALUE;


	/**
	 * 从特定对象获取类型值。
	 * @param cs 要操作的 CallableStatement
	 * @param paramIndex 待设值参数的索引
	 * @param sqlType 参数的 SQL 类型
	 * @param typeName 参数类型名（可选）
	 * @return 目标值
	 * @throws SQLException 设参时遇到 SQLException（无需自行捕获）
	 * @see java.sql.Types
	 * @see java.sql.CallableStatement#getObject
	 */
	Object getTypeValue(CallableStatement cs, int paramIndex, int sqlType, @Nullable String typeName)
			throws SQLException;

}
