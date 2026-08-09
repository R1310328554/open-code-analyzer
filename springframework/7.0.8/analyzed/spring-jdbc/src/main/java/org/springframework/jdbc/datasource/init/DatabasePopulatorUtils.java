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

package org.springframework.jdbc.datasource.init;

import java.sql.Connection;

import javax.sql.DataSource;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.util.Assert;

/**
 * 执行 {@link DatabasePopulator} 的工具方法。
 *
 * @author Juergen Hoeller
 * @author Oliver Gierke
 * @author Sam Brannen
 * @since 3.1
 */
public abstract class DatabasePopulatorUtils {

	/**
	 * 针对给定 {@link DataSource} 执行指定的 {@link DatabasePopulator}。
	 * <p>若所供 {@code DataSource} 的 {@link Connection} 未配置
	 * {@link Connection#getAutoCommit() 自动提交}，且
	 * {@linkplain DataSourceUtils#isConnectionTransactional 非事务性}，
	 * 则会 {@linkplain Connection#commit() 提交}。
	 * @param populator 要执行的 {@code DatabasePopulator}
	 * @param dataSource 要执行的目标 {@code DataSource}
	 * @throws DataAccessException 发生错误时，通常为 {@link ScriptException}
	 * @see DataSourceUtils#isConnectionTransactional(Connection, DataSource)
	 */
	public static void execute(DatabasePopulator populator, DataSource dataSource) throws DataAccessException {
		Assert.notNull(populator, "DatabasePopulator must not be null");
		Assert.notNull(dataSource, "DataSource must not be null");
		try {
			Connection connection = DataSourceUtils.getConnection(dataSource);
			try {
				populator.populate(connection);
				if (!connection.getAutoCommit() && !DataSourceUtils.isConnectionTransactional(connection, dataSource)) {
					connection.commit();
				}
			}
			finally {
				DataSourceUtils.releaseConnection(connection, dataSource);
			}
		}
		catch (ScriptException ex) {
			throw ex;
		}
		catch (Throwable ex) {
			throw new UncategorizedScriptException("Failed to execute database script", ex);
		}
	}

}
