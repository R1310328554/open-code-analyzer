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
 * 用于执行 {@link DatabasePopulator} 的实用方法。
 * @author Juergen Hoeller
 * @author Oliver Gierke
 * @author Sam Brannen
 * @since 3.1
 */
public abstract class DatabasePopulatorUtils {

	/**
	 * 针对给定的 {@link DataSource} 执行给定的 {@link DatabasePopulator}。 <p> 如果未针对 {@link
	 * Connection#getAutoCommit() auto-commit} 配置且不是 {@linkplain
	 * DataSourceUtils#isConnectionTransactional transactional}，则提供的 {@code DataSource} 的
	 * {@link Connection} 将是 {@linkplain Connection#commit() committed}。
	 * @param populator 要执行的 {@code DatabasePopulator}
	 * @param dataSource 要执行的 {@code DataSource}
	 * @throws DataAccessException 如果发生错误，具体是 {@link ScriptException}
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
