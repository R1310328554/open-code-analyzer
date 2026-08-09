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
 * 标准 JDBC {@link javax.sql.DataSource} 接口的简单实现，
 * 通过 bean 属性配置传统 {@link java.sql.Driver}，
 * 每次 {@code getConnection} 调用返回新的 {@link java.sql.Connection}。
 *
 * <p><b>注意：本类不是真正的连接池，不会复用 Connection。</b>
 * 它只是完整连接池的简单替代，实现相同标准接口，但每次调用都创建新 Connection。
 *
 * <p>在 Jakarta EE 容器中，建议使用容器提供的 JNDI DataSource。
 * 可通过 {@link org.springframework.jndi.JndiObjectFactoryBean} 将其暴露为 Spring
 * ApplicationContext 中的 DataSource bean，与本类本地 bean 无缝切换。
 *
 * <p>本 {@code SimpleDriverDataSource} 最初与
 * <a href="https://commons.apache.org/proper/commons-dbcp">Apache Commons DBCP</a>
 * 和 <a href="https://sourceforge.net/projects/c3p0">C3P0</a> 同期设计，
 * 提供 bean 风格的 {@code BasicDataSource}/{@code ComboPooledDataSource} 配置属性。
 * 现代 JDBC 连接池可考虑 <a href="https://github.com/brettwooldridge/HikariCP">HikariCP</a>，
 * 向应用暴露对应的 {@code HikariDataSource} 实例。
 *
 * @author Juergen Hoeller
 * @since 2.5.5
 * @see DriverManagerDataSource
 */
public class SimpleDriverDataSource extends AbstractDriverBasedDataSource {

	private @Nullable Driver driver;


	/**
	 * 用于 bean 风格配置的构造函数。
	 */
	public SimpleDriverDataSource() {
	}

	/**
	 * 使用给定标准 Driver 参数创建新的 SimpleDriverDataSource。
	 * @param driver JDBC Driver 对象
	 * @param url 访问 Driver 使用的 JDBC URL
	 * @see java.sql.Driver#connect(String, java.util.Properties)
	 */
	public SimpleDriverDataSource(Driver driver, String url) {
		setDriver(driver);
		setUrl(url);
	}

	/**
	 * 使用给定标准 Driver 参数创建新的 SimpleDriverDataSource。
	 * @param driver JDBC Driver 对象
	 * @param url 访问 Driver 使用的 JDBC URL
	 * @param username 访问 Driver 使用的 JDBC 用户名
	 * @param password 访问 Driver 使用的 JDBC 密码
	 * @see java.sql.Driver#connect(String, java.util.Properties)
	 */
	public SimpleDriverDataSource(Driver driver, String url, String username, String password) {
		setDriver(driver);
		setUrl(url);
		setUsername(username);
		setPassword(password);
	}

	/**
	 * 使用给定标准 Driver 参数创建新的 SimpleDriverDataSource。
	 * @param driver JDBC Driver 对象
	 * @param url 访问 Driver 使用的 JDBC URL
	 * @param conProps JDBC 连接属性
	 * @see java.sql.Driver#connect(String, java.util.Properties)
	 */
	public SimpleDriverDataSource(Driver driver, String url, Properties conProps) {
		setDriver(driver);
		setUrl(url);
		setConnectionProperties(conProps);
	}


	/**
	 * 指定要使用的 JDBC Driver 实现类。
	 * <p>将创建并持有该 Driver 类实例于 SimpleDriverDataSource 内。
	 * @see #setDriver
	 */
	public void setDriverClass(Class<? extends Driver> driverClass) {
		this.driver = BeanUtils.instantiateClass(driverClass);
	}

	/**
	 * 指定要使用的 JDBC Driver 实例。
	 * <p>允许传入共享的、可能已预配置的 Driver 实例。
	 * @see #setDriverClass
	 */
	public void setDriver(@Nullable Driver driver) {
		this.driver = driver;
	}

	/**
	 * 返回要使用的 JDBC Driver 实例。
	 */
	public @Nullable Driver getDriver() {
		return this.driver;
	}


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
