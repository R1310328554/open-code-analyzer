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
 * {@code javax.sql.DataSource} 接口的扩展，由以未包装方式返回 JDBC 连接的特殊数据源实现。
 * 使用此接口的<p>类可以查询操作后是否应关闭Connection。 Spring 的 DataSourceUtils 和 JdbcTemplate 类自动执行此类检查。
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see SingleConnectionDataSource#shouldClose
 * @see DataSourceUtils#releaseConnection
 * @see org.springframework.jdbc.core.JdbcTemplate
 */
public interface SmartDataSource extends DataSource {

	/**
	 * 我们应该关闭从该数据源获取的连接吗？使用来自 SmartDataSource 的连接的 <p>Code 在调用 {@code close()} 之前应始终通过此方法执行检查。 
	 * <p>请注意，“jdbc.core”包中的 JdbcTemplate 类负责释放 JDBC 连接，从而使应用程序代码摆脱此责任。
	 * @param con 要检查的连接
	 * @return 应关闭给定的连接
	 * @see java.sql.Connection#close()
	 */
	boolean shouldClose(Connection con);

}
