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

package org.springframework.jdbc.datasource;

import java.sql.Connection;

/**
 * Connection 代理应实现的 {@link java.sql.Connection} 子接口。
 * 允许访问底层目标 Connection。
 *
 * <p>需要转换为原生 JDBC Connection（如 Oracle 的 OracleConnection）时可检查本接口。
 * 或者，此类连接也支持 JDBC 4.0 的 {@link Connection#unwrap}。
 *
 * @author Juergen Hoeller
 * @since 1.1
 * @see TransactionAwareDataSourceProxy
 * @see LazyConnectionDataSourceProxy
 * @see DataSourceUtils#getTargetConnection(java.sql.Connection)
 */
public interface ConnectionProxy extends Connection {

	/**
	 * 返回本代理的目标 Connection。
	 * <p>通常是原生驱动 Connection 或连接池包装器。
	 * @return 底层 Connection（永不为 {@code null}）
	 */
	Connection getTargetConnection();

}
