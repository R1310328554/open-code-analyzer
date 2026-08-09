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
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * 标准 JDBC {@link javax.sql.DataSource} 接口的简单实现，
 * 通过 bean 属性配置传统 {@link java.sql.DriverManager}，
 * 每次 {@code getConnection} 调用返回新的 {@link java.sql.Connection}。
 *
 * <p><b>注意：本类不是真正的连接池，不会复用 Connection。</b>
 * 它只是完整连接池的简单替代，实现相同标准接口，但每次调用都创建新 Connection。
 *
 * <p>适用于 Jakarta EE 容器外的测试或独立环境，
 * 可作为 ApplicationContext 中的 DataSource bean，或配合简单 JNDI 环境使用。
 * 假定连接池的 {@code Connection.close()} 调用会直接关闭连接，
 * 因此任何 DataSource 感知的持久化代码均可正常工作。
 *
 * <p><b>注意：在 OSGi 等特殊类加载环境中，由于 DriverManager 的类加载问题，
 * 本类实质上已被 {@link SimpleDriverDataSource} 取代；
 * 后者通过直接使用 Driver 解决该问题。</b>
 *
 * <p>在 Jakarta EE 容器中，建议使用容器提供的 JNDI DataSource。
 * 可通过 {@link org.springframework.jndi.JndiObjectFactoryBean} 将其暴露为 Spring
 * ApplicationContext 中的 DataSource bean，与本类本地 bean 无缝切换。
 * 测试时可使用第三方完整方案（如 <a href="https://github.com/h-thurow/Simple-JNDI">Simple-JNDI</a>）
 * 搭建模拟 JNDI 环境，或改用本地 DataSource bean（更简单，推荐）。
 *
 * <p>本 {@code DriverManagerDataSource} 最初与
 * <a href="https://commons.apache.org/proper/commons-dbcp">Apache Commons DBCP</a>
 * 和 <a href="https://sourceforge.net/projects/c3p0">C3P0</a> 同期设计，
 * 提供 bean 风格的 {@code BasicDataSource}/{@code ComboPooledDataSource} 配置属性。
 * 现代 JDBC 连接池可考虑 <a href="https://github.com/brettwooldridge/HikariCP">HikariCP</a>，
 * 向应用暴露对应的 {@code HikariDataSource} 实例。
 *
 * @author Juergen Hoeller
 * @since 14.03.2003
 * @see SimpleDriverDataSource
 */
public class DriverManagerDataSource extends AbstractDriverBasedDataSource {

	/**
	 * 用于 bean 风格配置的构造函数。
	 */
	public DriverManagerDataSource() {
	}

	/**
	 * 使用给定 JDBC URL 创建新的 DriverManagerDataSource，
	 * 不指定 JDBC 访问的用户名或密码。
	 * @param url 访问 DriverManager 使用的 JDBC URL
	 * @see java.sql.DriverManager#getConnection(String)
	 */
	public DriverManagerDataSource(String url) {
		setUrl(url);
	}

	/**
	 * 使用给定标准 DriverManager 参数创建新的 DriverManagerDataSource。
	 * @param url 访问 DriverManager 使用的 JDBC URL
	 * @param username 访问 DriverManager 使用的 JDBC 用户名
	 * @param password 访问 DriverManager 使用的 JDBC 密码
	 * @see java.sql.DriverManager#getConnection(String, String, String)
	 */
	public DriverManagerDataSource(String url, String username, String password) {
		setUrl(url);
		setUsername(username);
		setPassword(password);
	}

	/**
	 * 使用给定 JDBC URL 创建新的 DriverManagerDataSource，
	 * 不指定 JDBC 访问的用户名或密码。
	 * @param url 访问 DriverManager 使用的 JDBC URL
	 * @param conProps JDBC 连接属性
	 * @see java.sql.DriverManager#getConnection(String)
	 */
	public DriverManagerDataSource(String url, Properties conProps) {
		setUrl(url);
		setConnectionProperties(conProps);
	}


	/**
	 * 设置 JDBC 驱动类名。启动时将初始化该驱动并向 JDK DriverManager 注册。
	 * <p><b>注意：DriverManagerDataSource 主要用于访问<i>已注册</i>的 JDBC 驱动。</b>
	 * 若需注册新驱动，建议使用 {@link SimpleDriverDataSource}。
	 * 或者在本 DataSource 实例化前自行初始化 JDBC 驱动。
	 * "driverClassName" 属性主要为向后兼容及 Commons DBCP 与本 DataSource 迁移保留。
	 * @see java.sql.DriverManager#registerDriver(java.sql.Driver)
	 * @see SimpleDriverDataSource
	 */
	public void setDriverClassName(String driverClassName) {
		Assert.hasText(driverClassName, "Property 'driverClassName' must not be empty");
		String driverClassNameToUse = driverClassName.trim();
		try {
			Class.forName(driverClassNameToUse, true, ClassUtils.getDefaultClassLoader());
		}
		catch (ClassNotFoundException ex) {
			throw new IllegalStateException("Could not load JDBC driver class [" + driverClassNameToUse + "]", ex);
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Loaded JDBC driver: " + driverClassNameToUse);
		}
	}


	@Override
	protected Connection getConnectionFromDriver(Properties props) throws SQLException {
		String url = getUrl();
		Assert.state(url != null, "'url' not set");
		if (logger.isDebugEnabled()) {
			logger.debug("Creating new JDBC DriverManager Connection to [" + url + "]");
		}
		return getConnectionFromDriverManager(url, props);
	}

	/**
	 * 将通过 DriverManager 静态方法获取 Connection 的逻辑
	 * 提取为 protected 方法，便于单元测试。
	 * @see java.sql.DriverManager#getConnection(String, java.util.Properties)
	 */
	protected Connection getConnectionFromDriverManager(String url, Properties props) throws SQLException {
		return DriverManager.getConnection(url, props);
	}

}
