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
 * 用于创建 {@link EmbeddedDatabase} 实例的工厂。
 * <p>Callers 保证返回的数据库已完全初始化和填充。
 * <p> 工厂可以配置如下： <ul> <li> 调用 {@link #generateUniqueDatabaseName} 为数据库设置一个唯一的、随机的名称。
 * <li>调用 {@link #setDatabaseName} 为数据库设置显式名称。 <li> 如果您希望使用预先支持的类型之一及其默认设置，请调用 {@link
 * #setDatabaseType} 设置数据库类型。 <li>调用 {@link #setDatabaseConfigurer} 配置对自定义嵌入式数据库类型的支持，或
 * {@linkplain EmbeddedDatabaseConfigurers#customizeConfigurer customize} 配置预先支持的类型之一的默认值。
 * <li>调用 {@link #setDatabasePopulator} 更改用于填充数据库的算法。 <li>调用{@link
 * #setDataSourceFactory}来更改用于连接数据库的{@link DataSource}的类型。 </ul>
 * <p> 配置工厂后，调用 {@link #getDatabase()} 获取 {@link EmbeddedDatabase} 实例的引用。
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

	/**
	 * 获取 Log（`Log`）。
	 */
	private static final Log logger = LogFactory.getLog(EmbeddedDatabaseFactory.class);

	/** `false`：该类的成员状态。 */
	private boolean generateUniqueDatabaseName = false;

	/** 名称相关状态（`DEFAULT_DATABASE_NAME`）。 */
	private String databaseName = DEFAULT_DATABASE_NAME;

	/**
	 * 方法 `SimpleDriverDataSourceFactory`：完成本类中与「Simple Driver Data Source Factory」相关的职责。
	 */
	private DataSourceFactory dataSourceFactory = new SimpleDriverDataSourceFactory();

	/** 配置相关状态（`databaseConfigurer`）。 */
	private @Nullable EmbeddedDatabaseConfigurer databaseConfigurer;

	/** `databasePopulator`：该类的成员状态。 */
	private @Nullable DatabasePopulator databasePopulator;

	/** 来源相关状态（`dataSource`）。 */
	private @Nullable DataSource dataSource;


	/**
	 * 设置 {@code generateUniqueDatabaseName} 标志以启用或禁用生成用作数据库名称的伪随机唯一 ID。 <p> 将此标志设置为 {@code
	 * true} 会覆盖通过 {@link #setDatabaseName} 设置的任何显式名称。
	 * @since 4.2
	 * @see #setDatabaseName
	 */
	public void setGenerateUniqueDatabaseName(boolean generateUniqueDatabaseName) {
		this.generateUniqueDatabaseName = generateUniqueDatabaseName;
	}

	/**
	 * 设置数据库的名称。 <p>默认为 {@value #DEFAULT_DATABASE_NAME}。如果 {@code generateUniqueDatabaseName}
	 * 标志已设置为 {@code true}，则 <p> 将被覆盖。
	 * @param databaseName 嵌入式数据库的名称
	 * @see #setGenerateUniqueDatabaseName
	 */
	public void setDatabaseName(String databaseName) {
		Assert.hasText(databaseName, "Database name is required");
		this.databaseName = databaseName;
	}

	/**
	 * 设置用于创建连接到嵌入式数据库的 {@link DataSource} 实例的工厂。 <p>默认为 {@link
	 * SimpleDriverDataSourceFactory}。
	 */
	public void setDataSourceFactory(DataSourceFactory dataSourceFactory) {
		Assert.notNull(dataSourceFactory, "DataSourceFactory is required");
		this.dataSourceFactory = dataSourceFactory;
	}

	/**
	 * 设置要使用的嵌入式数据库的类型。 <p> 当您希望使用其默认设置配置预先支持的类型之一时，请调用此函数。 <p>默认为 HSQL。
	 * @param type 数据库类型
	 */
	public void setDatabaseType(EmbeddedDatabaseType type) {
		this.databaseConfigurer = EmbeddedDatabaseConfigurers.getConfigurer(type);
	}

	/**
	 * 设置将用于配置嵌入式数据库实例的策略。 <p> 当您希望自定义其中一种预支持类型的设置时，请使用 {@linkplain EmbeddedDatabaseConfigurers
	 * #customizeConfigurer customizeConfigurer} 调用此选项。或者，当您希望使用尚未支持的嵌入式数据库类型时，可以使用此选项。
	 * @since 6.2
	 */
	public void setDatabaseConfigurer(EmbeddedDatabaseConfigurer configurer) {
		this.databaseConfigurer = configurer;
	}

	/**
	 * 设置将用于初始化或填充嵌入式数据库的策略。 <p>默认为 {@code null}。
	 */
	public void setDatabasePopulator(DatabasePopulator populator) {
		this.databasePopulator = populator;
	}

	/**
	 * 返回 {@linkplain EmbeddedDatabase embedded database} 实例的工厂方法，该实例也是 {@link DataSource}。
	 */
	@SuppressWarnings("NullAway") // Dataflow analysis limitation
	public EmbeddedDatabase getDatabase() {
		if (this.dataSource == null) {
			initDatabase();
		}
		return new EmbeddedDataSourceProxy(this.dataSource);
	}


	/**
	 * 用于初始化嵌入式数据库的挂钩。 <p>如果 {@code generateUniqueDatabaseName} 标志已设置为 {@code true}，则
	 * {@linkplain #setDatabaseName database name} 的当前值将被自动生成的名称覆盖。
	 * <p>子类可以调用该方法强制初始化；但是，该方法只能调用一次。 <p> 调用此方法后，{@link #getDataSource()} 返回提供与数据库连接的 {@link
	 * DataSource}。
	 */
	protected void initDatabase() {
		if (this.generateUniqueDatabaseName) {
			setDatabaseName(UUID.randomUUID().toString());
		}

		// 首先创建嵌入式数据库
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

		// 现在填充数据库
		if (this.databasePopulator != null) {
			try {
				DatabasePopulatorUtils.execute(this.databasePopulator, this.dataSource);
			}
			catch (RuntimeException ex) {
				// 填充失败，因此将其保留为未初始化
				shutdownDatabase();
				throw ex;
			}
		}
	}

	/**
	 * 用于关闭嵌入式数据库的挂钩。子类可以调用此方法来强制关闭。 <p>调用后，{@link #getDataSource()}返回{@code null}。如果没有初始化嵌入式数据
	 * 库，<p>不执行任何操作。
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
	 * 获取 {@link DataSource} 的挂钩，该 {@link DataSource} 提供与嵌入式数据库的连接。 <p> 如果 {@code DataSource}
	 * 尚未初始化或数据库已关闭，则返回 {@code null}。子类可以调用此方法直接访问 {@code DataSource} 实例。
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

		// getParentLogger() 是 JDBC 4.1 兼容性所必需的
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
