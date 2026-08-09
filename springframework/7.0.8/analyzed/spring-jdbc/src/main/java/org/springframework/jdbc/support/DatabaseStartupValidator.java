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
 * 检查数据库是否已经启动的 Bean。通过“depends-on”从依赖于数据库启动的 bean 进行引用，例如 Hibernate SessionFactory 或直接访问
 * DataSource 的自定义数据访问对象。
 * <p> 用于推迟应用程序初始化直到数据库启动。特别适合等待缓慢启动的 Oracle 数据库。
 * @author Juergen Hoeller
 * @author Marten Deinum
 * @since 18.12.2003
 */
public class DatabaseStartupValidator implements InitializingBean {

	/**
	 * 默认间隔。
	 */
	public static final int DEFAULT_INTERVAL = 1;

	/**
	 * 默认超时。
	 */
	public static final int DEFAULT_TIMEOUT = 60;


	/**
	 * 获取 Log（`Log`）。
	 */
	protected final Log logger = LogFactory.getLog(getClass());

	/** 来源相关状态（`dataSource`）。 */
	private @Nullable DataSource dataSource;

	/** `validationQuery`：该类的成员状态。 */
	private @Nullable String validationQuery;

	/** `DEFAULT_INTERVAL`：该类的成员状态。 */
	private int interval = DEFAULT_INTERVAL;

	/** `DEFAULT_TIMEOUT`：该类的成员状态。 */
	private int timeout = DEFAULT_TIMEOUT;


	/**
	 * 设置要验证的数据源。
	 */
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * 设置用于验证的 SQL 查询字符串。
	 * @deprecated 支持 JDBC 4.0 连接验证
	 */
	@Deprecated(since = "5.3")
	public void setValidationQuery(String validationQuery) {
		this.validationQuery = validationQuery;
	}

	/**
	 * 设置验证运行之间的间隔（以秒为单位）。默认为 {@value #DEFAULT_INTERVAL}。
	 */
	public void setInterval(int interval) {
		this.interval = interval;
	}

	/**
	 * 设置超时（以秒为单位），超过该超时将引发致命异常。默认为 {@value #DEFAULT_TIMEOUT}。
	 */
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}


	/**
	 * 检查是否可以在来自指定数据源的连接上执行验证查询，检查之间具有指定的间隔，直到指定的超时。
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
			// 重新中断当前线程，以允许其他线程做出反应。
			Thread.currentThread().interrupt();
		}
	}

}
