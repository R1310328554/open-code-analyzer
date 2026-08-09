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
 * 在 JDBC {@link java.sql.Driver} 上运行的 JDBC {@link javax.sql.DataSource} 实现的抽象基类。
 * @author Juergen Hoeller
 * @since 2.5.5
 * @see SimpleDriverDataSource
 * @see DriverManagerDataSource
 */
public abstract class AbstractDriverBasedDataSource extends AbstractDataSource {

	/** `url`：该类的成员状态。 */
	private @Nullable String url;

	/** 名称相关状态（`username`）。 */
	private @Nullable String username;

	/** `password`：该类的成员状态。 */
	private @Nullable String password;

	/** `catalog`：该类的成员状态。 */
	private @Nullable String catalog;

	/** `schema`：该类的成员状态。 */
	private @Nullable String schema;

	/** 连接相关状态（`connectionProperties`）。 */
	private @Nullable Properties connectionProperties;


	/**
	 * 设置用于通过驱动程序进行连接的 JDBC URL。
	 * @see java.sql.Driver#connect(String, java.util.Properties)
	 */
	public void setUrl(@Nullable String url) {
		this.url = (url != null ? url.trim() : null);
	}

	/**
	 * 返回用于通过驱动程序进行连接的 JDBC URL。
	 */
	public @Nullable String getUrl() {
		return this.url;
	}

	/**
	 * 设置用于通过驱动程序连接的 JDBC 用户名。
	 * @see java.sql.Driver#connect(String, java.util.Properties)
	 */
	public void setUsername(@Nullable String username) {
		this.username = username;
	}

	/**
	 * 返回用于通过驱动程序进行连接的 JDBC 用户名。
	 */
	public @Nullable String getUsername() {
		return this.username;
	}

	/**
	 * 设置用于通过驱动程序连接的 JDBC 密码。
	 * @see java.sql.Driver#connect(String, java.util.Properties)
	 */
	public void setPassword(@Nullable String password) {
		this.password = password;
	}

	/**
	 * 返回用于通过驱动程序进行连接的 JDBC 密码。
	 */
	public @Nullable String getPassword() {
		return this.password;
	}

	/**
	 * 指定要应用于每个连接的数据库目录。
	 * @since 4.3.2
	 * @see Connection#setCatalog
	 */
	public void setCatalog(@Nullable String catalog) {
		this.catalog = catalog;
	}

	/**
	 * 返回要应用于每个连接的数据库目录（如果有）。
	 * @since 4.3.2
	 */
	public @Nullable String getCatalog() {
		return this.catalog;
	}

	/**
	 * 指定要应用于每个连接的数据库架构。
	 * @since 4.3.2
	 * @see Connection#setSchema
	 */
	public void setSchema(@Nullable String schema) {
		this.schema = schema;
	}

	/**
	 * 返回要应用于每个连接的数据库架构（如果有）。
	 * @since 4.3.2
	 */
	public @Nullable String getSchema() {
		return this.schema;
	}

	/**
	 * 将任意连接属性指定为键/值对，以传递给驱动程序。 <p> 还可以包含“用户”和“密码”属性。但是，在此数据源上指定的任何“用户名”和“密码”bean 属性都将覆盖相应的连接属性
	 * 。
	 * @see java.sql.Driver#connect(String, java.util.Properties)
	 */
	public void setConnectionProperties(@Nullable Properties connectionProperties) {
		this.connectionProperties = connectionProperties;
	}

	/**
	 * 返回要传递给驱动程序的连接属性（如果有）。
	 */
	public @Nullable Properties getConnectionProperties() {
		return this.connectionProperties;
	}


	/**
	 * 此实现委托给 {@code getConnectionFromDriver}，使用此数据源的默认用户名和密码。
	 * @see #getConnectionFromDriver(String, String)
	 * @see #setUsername
	 * @see #setPassword
	 */
	@Override
	public Connection getConnection() throws SQLException {
		return getConnectionFromDriver(getUsername(), getPassword());
	}

	/**
	 * 此实现委托给 {@code getConnectionFromDriver}，使用给定的用户名和密码。
	 * @see #getConnectionFromDriver(String, String)
	 */
	@Override
	public Connection getConnection(String username, String password) throws SQLException {
		return getConnectionFromDriver(username, password);
	}


	/**
	 * 构建Driver的属性，包括给定的用户名和密码（如果有），并获取相应的Connection。
	 * @param username 用户名
	 * @param password 使用的密码
	 * @return 获得连接
	 * @throws SQLException 万一失败
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
	 * 使用给定属性获取连接。 <p>Template 方法由子类实现。
	 * @param props 合并的连接属性
	 * @return 获得连接
	 * @throws SQLException 万一失败
	 */
	protected abstract Connection getConnectionFromDriver(Properties props) throws SQLException;

}
