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
 * 要实现的接口用于检索标准 {@code CallableStatement.getObject} 方法不支持的更复杂的数据库特定类型的值。
 * <p>I实现执行获取实际值的实际工作。它们必须实现回调方法 {@code getTypeValue}，该方法可以抛出 SQLException，这些异常将由调用代码捕获和转换。
 * 如果需要创建任何特定于数据库的对象，则此回调方法可以通过给定的 CallableStatement 对象访问底层 Connection。
 * @author Thomas Risberg
 * @since 1.1
 * @see java.sql.Types
 * @see java.sql.CallableStatement#getObject
 * @see org.springframework.jdbc.object.StoredProcedure#execute(java.util.Map)
 */
public interface SqlReturnType {

	/**
	 * 指示未知（或未指定）SQL 类型的常量。如果原始操作方法没有指定SQL类型，则传入setTypeValue。
	 * @see java.sql.Types
	 * @see JdbcOperations#update(String, Object[])
	 */
	int TYPE_UNKNOWN = Integer.MIN_VALUE;


	/**
	 * 从特定对象获取类型值。
	 * @param cs 要操作的 CallableStatement
	 * @param paramIndex 我们需要为其设置值的参数的索引
	 * @param sqlType 我们正在设置的参数的 SQL 类型
	 * @param typeName 参数的类型名称（可选）
	 * @return 目标值
	 * @throws SQLException 如果设置参数值时遇到 SQLException（即无需捕获 SQLException）
	 * @see java.sql.Types
	 * @see java.sql.CallableStatement#getObject
	 */
	Object getTypeValue(CallableStatement cs, int paramIndex, int sqlType, @Nullable String typeName)
			throws SQLException;

}
