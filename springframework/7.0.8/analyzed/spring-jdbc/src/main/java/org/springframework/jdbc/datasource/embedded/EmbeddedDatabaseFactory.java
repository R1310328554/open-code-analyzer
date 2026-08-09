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

package org.springframework.jdbc.datasource.embedded;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;

import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.jdbc.datasource.init.DatabasePopulator;
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils;
import org.springframework.util.Assert;

/**
 * 创建 {@link EmbeddedDatabase} 实例的工厂。
 *
 * <p>调用方保证返回的数据库已完全初始化并填充。
 *
 * <p>可按如下方式配置工厂：
 * <ul>
 * <li>调用 {@link #generateUniqueDatabaseName} 设置唯一随机数据库名称。
 * <li>调用 {@link #setDatabaseName} 设置显式数据库名称。
 * <li>调用 {@link #setDatabaseType} 设置数据库类型，以使用预支持类型的默认设置。
 * <li>调用 {@link #setDatabaseConfigurer} 配置自定义嵌入式数据库类型，
 * 或 {@linkplain EmbeddedDatabaseConfigurers#customizeConfigurer 定制} 预支持类型的默认值。
 * <li>调用 {@link #setDatabasePopulator} 更改填充数据库的算法。
 * <li>调用 {@link #setDataSourceFactory} 更改连接数据库的 {@link DataSource} 类型。
 * </ul>
 *
 * <p>配置完成后，调用 {@link #getDatabase()} 获取 {@link EmbeddedDatabase} 实例引用。
 *
 * @author Keith Donald
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @author Stephane Nicoll
 * @since 3.0
 */
public class EmbeddedDatabaseFactory {

	/**
	 * 嵌入式数据库的默认名称：{@value}。
	 */
	public static final String DEFAULT_DATABASE_NAME = "testdb";

	private static final Log logger = LogFactory.getLog(EmbeddedDatabaseFactory.class);

	private boolean generateUniqueDatabaseName = false;

	private String databaseName = DEFAULT_DATABASE_NAME;

	private DataSourceFactory dataSourceFactory = new SimpleDriverDataSourceFactory();

	private @Nullable EmbeddedDatabaseConfigurer databaseConfigurer;

	private @Nullable DatabasePopulator databasePopulator;

	private @Nullable DataSource dataSource;


	/**
	 * 设置 {@code generateUniqueDatabaseName} 标志，启用或禁用
	 * 生成伪随机唯一 ID 作为数据库名称。
	 * <p>设为 {@code true} 将覆盖 {@link #setDatabaseName} 设置的显式名称。
	 * @since 4.2
	 * @see #setDatabaseName
	 */
	public void setGenerateUniqueDatabaseName(boolean generateUniqueDatabaseName) {
		this.generateUniqueDatabaseName = generateUniqueDatabaseName;
	}

	/**
	 * 设置数据库名称。
	 * <p>默认为 {@value #DEFAULT_DATABASE_NAME}。
	 * <p>若 {@code generateUniqueDatabaseName} 标志为 {@code true} 则被覆盖。
	 * @param databaseName 嵌入式数据库名称
	 * @see #setGenerateUniqueDatabaseName
	 */
	public void setDatabaseName(String databaseName) {
		Assert.hasText(databaseName, "Database name is required");
		this.databaseName = databaseName;
	}

	/**
	 * 设置用于创建连接嵌入式数据库的 {@link DataSource} 实例的工厂。
	 * <p>默认为 {@link SimpleDriverDataSourceFactory}。
	 */
	public void setDataSourceFactory(DataSourceFactory dataSourceFactory) {
		Assert.notNull(dataSourceFactory, "DataSourceFactory is required");
		this.dataSourceFactory = dataSourceFactory;
	}

	/**
	 * 设置要使用的嵌入式数据库类型。
	 * <p>若要以默认设置配置预支持类型之一，调用此方法。
	 * <p>默认为 HSQL。
	 * @param type 数据库类型
	 */
	public void setDatabaseType(EmbeddedDatabaseType type) {
		this.databaseConfigurer = EmbeddedDatabaseConfigurers.getConfigurer(type);
	}

	/**
	 * 设置用于配置嵌入式数据库实例的策略。
	 * <p>若需定制预支持类型的设置，
	 * 配合 {@linkplain EmbeddedDatabaseConfigurers#customizeConfigurer customizeConfigurer} 调用。
	 * 也可用于尚未支持的嵌入式数据库类型。
	 * @since 6.2
	 */
	public void setDatabaseConfigurer(EmbeddedDatabaseConfigurer configurer) {
		this.databaseConfigurer = configurer;
	}

	/**
	 * 设置用于初始化或填充嵌入式数据库的策略。
	 * <p>默认为 {@code null}。
	 */
	public void setDatabasePopulator(DatabasePopulator populator) {
		this.databasePopulator = populator;
	}

	/**
	 * 工厂方法，返回 {@linkplain EmbeddedDatabase 嵌入式数据库} 实例，
	 * 同时也是 {@link DataSource}。
	 */
	@SuppressWarnings("NullAway") // Dataflow analysis limitation
	public EmbeddedDatabase getDatabase() {
		if (this.dataSource == null) {
			initDatabase();
		}
		return new EmbeddedDataSourceProxy(this.dataSource);
	}


	/**
	 * 初始化嵌入式数据库的钩子。
	 * <p>若 {@code generateUniqueDatabaseName} 标志为 {@code true}，
	 * 当前 {@linkplain #setDatabaseName 数据库名称} 将被自动生成的名称覆盖。
	 * <p>子类可调用此方法强制初始化，但仅应调用一次。
	 * <p>调用后 {@link #getDataSource()} 返回提供数据库连接的 {@link DataSource}。
	 */
	protected void initDatabase() {
		if (this.generateUniqueDatabaseName) {
			setDatabaseName(UUID.randomUUID().toString());
		}

		// 先创建嵌入式数据库
		if (this.databaseConfigurer == null) {
			this.databaseConfigurer = EmbeddedDatabaseConfigurers.getConfigurer(EmbeddedDatabaseType.HSQL);
		}
		this.databaseConfigurer.configureConnectionProperties(
				this.dataSourceFactory.getConnectionProperties(), this.databaseName);
		this.dataSource = this.dataSourceFactory.getDataSource();

		if (logger.isInfoEnabled()) {
			if (this.dataSource instanceof SimpleDriverDataSource simpleDriverDataSource) {
				logger.info(String.format("Starting embedded database: url='%s', username='%s'",
						simpleDriverDataSource.getUrl(), simpleDriverDataSource.getUsername()));
			}
			else {
				logger.info(String.format("Starting embedded database '%s'", this.databaseName));
			}
		}

		// 再填充数据库
		if (this.databasePopulator != null) {
			try {
				DatabasePopulatorUtils.execute(this.databasePopulator, this.dataSource);
			}
			catch (RuntimeException ex) {
				// 填充失败，保持未初始化状态
				shutdownDatabase();
				throw ex;
			}
		}
	}

	/**
	 * 关闭嵌入式数据库的钩子。子类可调用此方法强制关闭。
	 * <p>调用后 {@link #getDataSource()} 返回 {@code null}。
	 * <p>若未初始化嵌入式数据库则不执行任何操作。
	 */
	protected void shutdownDatabase() {
		if (this.dataSource != null) {
			if (logger.isInfoEnabled()) {
				if (this.dataSource instanceof SimpleDriverDataSource simpleDriverDataSource) {
					logger.info(String.format("Shutting down embedded database: url='%s'",
							simpleDriverDataSource.getUrl()));
				}
				else {
					logger.info(String.format("Shutting down embedded database '%s'", this.databaseName));
				}
			}
			if (this.databaseConfigurer != null) {
				this.databaseConfigurer.shutdown(this.dataSource, this.databaseName);
			}
			this.dataSource = null;
		}
	}

	/**
	 * 获取提供嵌入式数据库连接的 {@link DataSource} 的钩子。
	 * <p>若 {@code DataSource} 未初始化或数据库已关闭则返回 {@code null}。
	 * 子类可调用此方法直接访问 {@code DataSource} 实例。
	 */
	protected final @Nullable DataSource getDataSource() {
		return this.dataSource;
	}


	private class EmbeddedDataSourceProxy implements EmbeddedDatabase {

		private final DataSource dataSource;

		public EmbeddedDataSourceProxy(DataSource dataSource) {
			this.dataSource = dataSource;
		}

		@Override
		public Connection getConnection() throws SQLException {
			return this.dataSource.getConnection();
		}

		@Override
		public Connection getConnection(String username, String password) throws SQLException {
			return this.dataSource.getConnection(username, password);
		}

		@Override
		public PrintWriter getLogWriter() throws SQLException {
			return this.dataSource.getLogWriter();
		}

		@Override
		public void setLogWriter(PrintWriter out) throws SQLException {
			this.dataSource.setLogWriter(out);
		}

		@Override
		public int getLoginTimeout() throws SQLException {
			return this.dataSource.getLoginTimeout();
		}

		@Override
		public void setLoginTimeout(int seconds) throws SQLException {
			this.dataSource.setLoginTimeout(seconds);
		}

		@Override
		public <T> T unwrap(Class<T> iface) throws SQLException {
			return this.dataSource.unwrap(iface);
		}

		@Override
		public boolean isWrapperFor(Class<?> iface) throws SQLException {
			return this.dataSource.isWrapperFor(iface);
		}

		// getParentLogger() 为 JDBC 4.1 兼容性所需
		@Override
		public Logger getParentLogger() {
			return Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
		}

		@Override
		public void shutdown() {
			shutdownDatabase();
		}
	}

}
