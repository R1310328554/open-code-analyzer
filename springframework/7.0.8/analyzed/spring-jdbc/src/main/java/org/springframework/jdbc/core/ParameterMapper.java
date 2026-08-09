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
 * 当参数需基于连接进行定制时实现此接口。
 * 可能需要利用特定 Connection 类型才能使用的专有特性。
 *
 * @author Rod Johnson
 * @author Thomas Risberg
 * @see CallableStatementCreatorFactory#newCallableStatementCreator(ParameterMapper)
 * @see org.springframework.jdbc.object.StoredProcedure#execute(ParameterMapper)
 */
@FunctionalInterface
public interface ParameterMapper {

	/**
	 * 创建以名称为键的输入参数 Map。
	 * @param con JDBC 连接。若需对专有 Connection 实现类
	 * 执行 RDBMS 特定操作，此参数很有用（也是本接口的用途）。
	 * 此类封装了这类专有细节；但如有可能，最好避免使用这类专有 RDBMS 特性。
	 * @return 以名称为键的输入参数 Map（永不为 {@code null}）
	 * @throws SQLException 若设置参数值时遇到 SQLException
	 * （即无需捕获 SQLException）
	 */
	Map<String, ?> createMap(Connection con) throws SQLException;

}
