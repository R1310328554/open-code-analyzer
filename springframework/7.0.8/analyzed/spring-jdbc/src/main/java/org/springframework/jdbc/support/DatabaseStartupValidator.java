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

/* ===== [OCA 中文解析] =====
class DatabaseStartupValidator — 意图说明

class `DatabaseStartupValidator`：请结合所属模块与调用方理解其在整体架构中的职责。；源文件: `spring-jdbc/src/main/java/org/springframework/jdbc/support/DatabaseStartupValidator.java`

（本注释由 open-code-analyzer 生成，置于原有文档注释之前）
===== [OCA 中文解析结束] ===== */
/**
 * Bean that checks if a database has already started up. To be referenced
 * via "depends-on" from beans that depend on database startup, like a Hibernate
 * SessionFactory or custom data access objects that access a DataSource directly.
 *
 * <p>Useful to defer application initialization until a database has started up.
 * Particularly appropriate for waiting on a slowly starting Oracle database.
 *
 * @author Juergen Hoeller
 * @author Marten Deinum
 * @since 18.12.2003
 */
public class DatabaseStartupValidator implements InitializingBean {

	// [OCA] 字段 `DEFAULT_INTERVAL`：类成员状态。
	/**
	 * The default interval.
	 */
	public static final int DEFAULT_INTERVAL = 1;

	// [OCA] 字段 `DEFAULT_TIMEOUT`：类成员状态。
	/**
	 * The default timeout.
	 */
	public static final int DEFAULT_TIMEOUT = 60;


	// [OCA] 字段 `logger`：类成员状态。
	protected final Log logger = LogFactory.getLog(getClass());

	private @Nullable DataSource dataSource;

	private @Nullable String validationQuery;

	// [OCA] 字段 `interval`：类成员状态。
	private int interval = DEFAULT_INTERVAL;

	// [OCA] 字段 `timeout`：类成员状态。
	private int timeout = DEFAULT_TIMEOUT;


	/**
	 * Set the DataSource to validate.
	 */
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * Set the SQL query string to use for validation.
	 * @deprecated in favor of the JDBC 4.0 connection validation
	 */
	@Deprecated(since = "5.3")
	public void setValidationQuery(String validationQuery) {
		this.validationQuery = validationQuery;
	}

	/**
	 * Set the interval between validation runs (in seconds).
	 * Default is {@value #DEFAULT_INTERVAL}.
	 */
	public void setInterval(int interval) {
		this.interval = interval;
	}

	/**
	 * Set the timeout (in seconds) after which a fatal exception
	 * will be thrown. Default is {@value #DEFAULT_TIMEOUT}.
	 */
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}


	/**
	 * Check whether the validation query can be executed on a Connection
	 * from the specified DataSource, with the specified interval between
	 * checks, until the specified timeout.
	 */
	@Override
	/* ===== [OCA 中文解析] =====
方法 afterPropertiesSet — 意图与阅读要点

方法 `afterPropertiesSet` 复杂度较高（CCN≈15, NLOC≈66）。阅读时建议先抓住主路径，再看分支/异常/缓存等旁路逻辑；关注它在调用链中上下游的契约（入参约束、返回值语义、抛出的异常）。
	===== [OCA 中文解析结束] ===== */
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
