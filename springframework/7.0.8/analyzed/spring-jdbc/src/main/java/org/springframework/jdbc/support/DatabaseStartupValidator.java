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

package org.springframework.jdbc.support;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.CannotGetJdbcConnectionException;

/**
 * 检查数据库是否已启动的 Bean。依赖数据库启动的 Bean（如 Hibernate SessionFactory
 * 或直接访问 DataSource 的自定义 DAO）可通过 "depends-on" 引用本 Bean。
 *
 * <p>用于延迟应用初始化直至数据库启动，特别适合等待启动较慢的 Oracle 数据库。
 *
 * @author Juergen Hoeller
 * @author Marten Deinum
 * @since 18.12.2003
 */
public class DatabaseStartupValidator implements InitializingBean {

	/** 默认重试间隔（秒）。 */
	public static final int DEFAULT_INTERVAL = 1;

	/** 默认超时时间（秒）。 */
	public static final int DEFAULT_TIMEOUT = 60;


	protected final Log logger = LogFactory.getLog(getClass());

	private @Nullable DataSource dataSource;

	private @Nullable String validationQuery;

	private int interval = DEFAULT_INTERVAL;

	private int timeout = DEFAULT_TIMEOUT;


	/**
	 * 设置要验证的 DataSource。
	 */
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * 设置用于验证的 SQL 查询字符串。
	 * @deprecated 建议使用 JDBC 4.0 连接验证
	 */
	@Deprecated(since = "5.3")
	public void setValidationQuery(String validationQuery) {
		this.validationQuery = validationQuery;
	}

	/**
	 * 设置验证重试间隔（秒），默认 {@value #DEFAULT_INTERVAL}。
	 */
	public void setInterval(int interval) {
		this.interval = interval;
	}

	/**
	 * 设置超时时间（秒），超时后抛出致命异常，默认 {@value #DEFAULT_TIMEOUT}。
	 */
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}


	/**
	 * 检查指定 DataSource 的连接是否可执行验证查询，
	 * 在指定间隔重试直至超时。
	 */
	@Override
	public void afterPropertiesSet() {
		if (this.dataSource == null) {
			throw new IllegalArgumentException("Property 'dataSource' is required");
		}

		try {
			boolean validated = false;
			long beginTime = System.currentTimeMillis();
			long deadLine = beginTime + TimeUnit.SECONDS.toMillis(this.timeout);
			SQLException latestEx = null;

			while (!validated && System.currentTimeMillis() < deadLine) {
				Connection con = null;
				Statement stmt = null;
				try {
					con = this.dataSource.getConnection();
					if (con == null) {
						throw new CannotGetJdbcConnectionException("Failed to execute validation: " +
								"DataSource returned null from getConnection(): " + this.dataSource);
					}
					if (this.validationQuery == null) {
						validated = con.isValid(this.interval);
					}
					else {
						stmt = con.createStatement();
						stmt.execute(this.validationQuery);
						validated = true;
					}
				}
				catch (SQLException ex) {
					latestEx = ex;
					if (logger.isDebugEnabled()) {
						if (this.validationQuery != null) {
							logger.debug("Validation query [" + this.validationQuery + "] threw exception", ex);
						}
						else {
							logger.debug("Validation check threw exception", ex);
						}
					}
					if (logger.isInfoEnabled()) {
						float rest = ((float) (deadLine - System.currentTimeMillis())) / 1000;
						if (rest > this.interval) {
							logger.info("Database has not started up yet - retrying in " + this.interval +
									" seconds (timeout in " + rest + " seconds)");
						}
					}
				}
				finally {
					JdbcUtils.closeStatement(stmt);
					JdbcUtils.closeConnection(con);
				}

				if (!validated) {
					TimeUnit.SECONDS.sleep(this.interval);
				}
			}

			if (!validated) {
				throw new CannotGetJdbcConnectionException(
						"Database has not started up within " + this.timeout + " seconds", latestEx);
			}

			if (logger.isInfoEnabled()) {
				float duration = ((float) (System.currentTimeMillis() - beginTime)) / 1000;
				logger.info("Database startup detected after " + duration + " seconds");
			}
		}
		catch (InterruptedException ex) {
			// Re-interrupt current thread, to allow other threads to react.
			Thread.currentThread().interrupt();
		}
	}

}
