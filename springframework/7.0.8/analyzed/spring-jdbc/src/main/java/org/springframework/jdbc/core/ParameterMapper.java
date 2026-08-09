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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

/**
 * 当需要根据连接自定义参数时，需要实现该接口。我们可能需要这样做才能利用仅适用于特定连接类型的专有功能。
 * @author Rod Johnson
 * @author Thomas Risberg
 * @see CallableStatementCreatorFactory#newCallableStatementCreator(ParameterMapper)
 * @see org.springframework.jdbc.object.StoredProcedure#execute(ParameterMapper)
 */
@FunctionalInterface
public interface ParameterMapper {

	/**
	 * 创建输入参数的映射，按名称键入。
	 * @param con 一个 JDBC 连接。如果我们需要使用专有的 Connection 实现类执行特定于 RDBMS 的操作，那么这很有用（也是此接口的目的）。此类隐藏了此类专有细节。但是，如果可能的话，最好避免使用此类专有的 RDBMS 功能。
	 * @return 输入参数映射，按名称键入（绝不是 {@code null}）
	 * @throws SQLException 如果设置参数值时遇到 SQLException（即无需捕获 SQLException）
	 */
	Map<String, ?> createMap(Connection con) throws SQLException;

}
