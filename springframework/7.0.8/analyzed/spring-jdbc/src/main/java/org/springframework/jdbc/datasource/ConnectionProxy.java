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
 * {@link java.sql.Connection} 的子接口由连接代理实现。允许访问底层目标连接。
 * <p> 当需要转换为原生 JDBC Connection（例如 Oracle 的 OracleConnection）时，可以选中此接口。或者，所有此类连接也支持 JDBC
 * 4.0 的 {@link Connection#unwrap}。
 * @author Juergen Hoeller
 * @since 1.1
 * @see TransactionAwareDataSourceProxy
 * @see LazyConnectionDataSourceProxy
 * @see DataSourceUtils#getTargetConnection(java.sql.Connection)
 */
public interface ConnectionProxy extends Connection {

	/**
	 * 返回此代理的目标连接。 <p>这通常是本机驱动程序连接或连接池中的包装器。
	 * @return 底层连接（绝不是 {@code null}）
	 */
	Connection getTargetConnection();

}
