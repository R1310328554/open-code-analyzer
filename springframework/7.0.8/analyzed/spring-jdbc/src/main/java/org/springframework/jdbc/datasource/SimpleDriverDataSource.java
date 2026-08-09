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
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.BeanUtils;
import org.springframework.util.Assert;

/**
 * 标准 JDBC {@link javax.sql.DataSource} 接口的简单实现，通​​过 bean 属性配置普通的旧 JDBC {@link
 * java.sql.Driver}，并从每个 {@code getConnection} 调用返回新的 {@link java.sql.Connection}。
 * <p><b>NOTE：该类不是实际的连接池；它实际上并不池化 Connections.</b> 它只是作为成熟连接池的简单替代，实现相同的标准接口，但在每次调用时创建新的连接。
 * <p>在Jakarta EE容器中，建议使用容器提供的JNDI DataSource。这样的 DataSource 可以通过 {@link
 * org.springframework.jndi.JndiObjectFactoryBean} 在 Spring ApplicationContext 中公开为
 * DataSource bean，以便无缝切换到此类本地 DataSource bean 或从本地 DataSource bean 无缝切换。
 * <p>此 {@code SimpleDriverDataSource} 类最初是与 <a
 * href="https://commons.apache.org/proper/commons-dbcp">Apache Commons DBCP</a> 和 <a
 * href="https://sourceforge.net/projects/c3p0">C3P0</a> 一起设计的，具有 bean 风格的 {@code
 * BasicDataSource}/{@code ComboPooledDataSource} 类以及用于本地资源设置的配置属性。对于现代 JDBC 连接池，请考虑使用 <a
 * href="https://github.com/brettwooldridge/HikariCP">HikariCP</a>，向应用程序公开相应的 {@code
 * HikariDataSource} 实例。
 * @author Juergen Hoeller
 * @since 2.5.5
 * @see DriverManagerDataSource
 */
public class SimpleDriverDataSource extends AbstractDriverBasedDataSource {

	/** `driver`：该类的成员状态。 */
	private @Nullable Driver driver;


	/**
	 * bean 样式配置的构造函数。
	 */
	public SimpleDriverDataSource() {
	}

	/**
	 * 使用给定的标准驱动程序参数创建一个新的 DriverManagerDataSource。
	 * @param driver JDBC 驱动程序对象
	 * @param url 用于访问 DriverManager 的 JDBC URL
	 * @see java.sql.Driver#connect(String, java.util.Properties)
	 */
	public SimpleDriverDataSource(Driver driver, String url) {
		setDriver(driver);
		setUrl(url);
	}

	/**
	 * 使用给定的标准驱动程序参数创建一个新的 DriverManagerDataSource。
	 * @param driver JDBC 驱动程序对象
	 * @param url 用于访问 DriverManager 的 JDBC URL
	 * @param username 用于访问 DriverManager 的 JDBC 用户名
	 * @param password 用于访问 DriverManager 的 JDBC 密码
	 * @see java.sql.Driver#connect(String, java.util.Properties)
	 */
	public SimpleDriverDataSource(Driver driver, String url, String username, String password) {
		setDriver(driver);
		setUrl(url);
		setUsername(username);
		setPassword(password);
	}

	/**
	 * 使用给定的标准驱动程序参数创建一个新的 DriverManagerDataSource。
	 * @param driver JDBC 驱动程序对象
	 * @param url 用于访问 DriverManager 的 JDBC URL
	 * @param conProps JDBC 连接属性
	 * @see java.sql.Driver#connect(String, java.util.Properties)
	 */
	public SimpleDriverDataSource(Driver driver, String url, Properties conProps) {
		setDriver(driver);
		setUrl(url);
		setConnectionProperties(conProps);
	}


	/**
	 * 指定要使用的 JDBC 驱动程序实现类。 <p> 此 Driver 类的实例将被创建并保存在 SimpleDriverDataSource 中。
	 * @see #setDriver
	 */
	public void setDriverClass(Class<? extends Driver> driverClass) {
		this.driver = BeanUtils.instantiateClass(driverClass);
	}

	/**
	 * 指定要使用的 JDBC 驱动程序实例。 <p>这允许传入共享的、可能是预先配置的驱动程序实例。
	 * @see #setDriverClass
	 */
	public void setDriver(@Nullable Driver driver) {
		this.driver = driver;
	}

	/**
	 * 返回要使用的 JDBC 驱动程序实例。
	 */
	public @Nullable Driver getDriver() {
		return this.driver;
	}


	/**
	 * 获取 Connection From Driver（`ConnectionFromDriver`）。
	 */
	@Override
	protected Connection getConnectionFromDriver(Properties props) throws SQLException {
		Driver driver = getDriver();
		Assert.state(driver != null, "Driver has not been set");
		String url = getUrl();
		if (logger.isDebugEnabled()) {
			logger.debug("Creating new JDBC Driver Connection to [" + url + "]");
		}
		return driver.connect(url, props);
	}

}
