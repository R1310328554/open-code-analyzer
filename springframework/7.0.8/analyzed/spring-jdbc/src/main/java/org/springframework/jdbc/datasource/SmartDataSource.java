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

import javax.sql.DataSource;

/**
 * {@code javax.sql.DataSource} 接口的扩展，
 * 由以未包装方式返回 JDBC 连接的特殊数据源实现。
 *
 * <p>使用此接口的类可查询操作后是否应关闭 Connection。
 * Spring 的 DataSourceUtils 和 JdbcTemplate 会自动执行此类检查。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see SingleConnectionDataSource#shouldClose
 * @see DataSourceUtils#releaseConnection
 * @see org.springframework.jdbc.core.JdbcTemplate
 */
public interface SmartDataSource extends DataSource {

	/**
	 * 是否应关闭从此数据源获取的 Connection？
	 * <p>使用 SmartDataSource 连接的代码在调用 {@code close()} 前
	 * 应始终通过本方法检查。
	 * <p>注意 jdbc.core 包中的 JdbcTemplate 负责释放 JDBC 连接，
	 * 应用代码无需承担此职责。
	 * @param con 待检查的 Connection
	 * @return 给定 Connection 是否应关闭
	 * @see java.sql.Connection#close()
	 */
	boolean shouldClose(Connection con);

}
