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
import java.sql.SQLException;
import java.util.Properties;

import org.jspecify.annotations.Nullable;

/**
 * 基于 JDBC {@link java.sql.Driver} 的 {@link javax.sql.DataSource} 实现抽象基类。
 *
 * @author Juergen Hoeller
 * @since 2.5.5
 * @see SimpleDriverDataSource
 * @see DriverManagerDataSource
 */
public abstract class AbstractDriverBasedDataSource extends AbstractDataSource {

	private @Nullable String url;

	private @Nullable String username;

	private @Nullable String password;

	private @Nullable String catalog;

	private @Nullable String schema;

	private @Nullable Properties connectionProperties;


	/**
	 * 设置通过 Driver 连接时使用的 JDBC URL。
	 * @see java.sql.Driver#connect(String, java.util.Properties)
	 */
	public void setUrl(@Nullable String url) {
		this.url = (url != null ? url.trim() : null);
	}

	/**
	 * 返回通过 Driver 连接时使用的 JDBC URL。
	 */
	public @Nullable String getUrl() {
		return this.url;
	}

	/**
	 * 设置通过 Driver 连接时使用的 JDBC 用户名。
	 * @see java.sql.Driver#connect(String, java.util.Properties)
	 */
	public void setUsername(@Nullable String username) {
		this.username = username;
	}

	/**
	 * 返回通过 Driver 连接时使用的 JDBC 用户名。
	 */
	public @Nullable String getUsername() {
		return this.username;
	}

	/**
	 * 设置通过 Driver 连接时使用的 JDBC 密码。
	 * @see java.sql.Driver#connect(String, java.util.Properties)
	 */
	public void setPassword(@Nullable String password) {
		this.password = password;
	}

	/**
	 * 返回通过 Driver 连接时使用的 JDBC 密码。
	 */
	public @Nullable String getPassword() {
		return this.password;
	}

	/**
	 * 指定要应用到每个 Connection 的数据库 catalog。
	 * @since 4.3.2
	 * @see Connection#setCatalog
	 */
	public void setCatalog(@Nullable String catalog) {
		this.catalog = catalog;
	}

	/**
	 * 返回要应用到每个 Connection 的数据库 catalog（若有）。
	 * @since 4.3.2
	 */
	public @Nullable String getCatalog() {
		return this.catalog;
	}

	/**
	 * 指定要应用到每个 Connection 的数据库 schema。
	 * @since 4.3.2
	 * @see Connection#setSchema
	 */
	public void setSchema(@Nullable String schema) {
		this.schema = schema;
	}

	/**
	 * 返回要应用到每个 Connection 的数据库 schema（若有）。
	 * @since 4.3.2
	 */
	public @Nullable String getSchema() {
		return this.schema;
	}

	/**
	 * 以键值对形式指定任意连接属性，传递给 Driver。
	 * <p>也可包含 "user" 和 "password" 属性。但本 DataSource 上
	 * 指定的 username/password bean 属性会覆盖对应连接属性。
	 * @see java.sql.Driver#connect(String, java.util.Properties)
	 */
	public void setConnectionProperties(@Nullable Properties connectionProperties) {
		this.connectionProperties = connectionProperties;
	}

	/**
	 * 返回要传递给 Driver 的连接属性（若有）。
	 */
	public @Nullable Properties getConnectionProperties() {
		return this.connectionProperties;
	}


	/**
	 * 本实现委托 {@code getConnectionFromDriver}，
	 * 使用本 DataSource 的默认用户名和密码。
	 * @see #getConnectionFromDriver(String, String)
	 * @see #setUsername
	 * @see #setPassword
	 */
	@Override
	public Connection getConnection() throws SQLException {
		return getConnectionFromDriver(getUsername(), getPassword());
	}

	/**
	 * 本实现委托 {@code getConnectionFromDriver}，
	 * 使用给定用户名和密码。
	 * @see #getConnectionFromDriver(String, String)
	 */
	@Override
	public Connection getConnection(String username, String password) throws SQLException {
		return getConnectionFromDriver(username, password);
	}


	/**
	 * 构建 Driver 所需属性（含给定用户名和密码），并获取对应 Connection。
	 * @param username 用户名
	 * @param password 密码
	 * @return 获取到的 Connection
	 * @throws SQLException 失败时
	 * @see java.sql.Driver#connect(String, java.util.Properties)
	 */
	protected Connection getConnectionFromDriver(@Nullable String username, @Nullable String password) throws SQLException {
		Properties mergedProps = new Properties();
		Properties connProps = getConnectionProperties();
		if (connProps != null) {
			mergedProps.putAll(connProps);
		}
		if (username != null) {
			mergedProps.setProperty("user", username);
		}
		if (password != null) {
			mergedProps.setProperty("password", password);
		}

		Connection con = getConnectionFromDriver(mergedProps);
		if (this.catalog != null) {
			con.setCatalog(this.catalog);
		}
		if (this.schema != null) {
			con.setSchema(this.schema);
		}
		return con;
	}

	/**
	 * 使用给定属性获取 Connection。
	 * <p>由子类实现的模板方法。
	 * @param props 合并后的连接属性
	 * @return 获取到的 Connection
	 * @throws SQLException 失败时
	 */
	protected abstract Connection getConnectionFromDriver(Properties props) throws SQLException;

}
