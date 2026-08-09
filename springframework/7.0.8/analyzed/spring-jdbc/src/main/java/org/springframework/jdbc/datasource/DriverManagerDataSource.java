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
 * 标准 JDBC {@link javax.sql.DataSource} 接口的简单实现，通​​过 bean 属性配置普通的旧 JDBC {@link
 * java.sql.DriverManager}，并从每个 {@code getConnection} 调用返回新的 {@link java.sql.Connection}。
 * <p><b>NOTE：该类不是实际的连接池；它实际上并不池化 Connections.</b> 它只是作为成熟连接池的简单替代，实现相同的标准接口，但在每次调用时创建新的连接。
 * <p> 对于 Jakarta EE 容器外部的测试或独立环境很有用，可以作为相应 ApplicationContext 中的 DataSource bean，也可以与简单的 J
 * NDI 环境结合使用。池假设 {@code Connection.close()} 调用将简单地关闭连接，因此任何数据源感知的持久性代码都应该可以工作。
 * <p><b>NOTE：在特殊的类加载环境（例如 OSGi）中，由于 JDBC DriverManager 的一般类加载问题可以通过直接使用驱动程序来解决（这正是 SimpleD
 * riverDataSource 所做的），因此该类实际上被 {@link SimpleDriverDataSource} 取代。</b>
 * <p>在Jakarta EE容器中，建议使用容器提供的JNDI DataSource。这样的 DataSource 可以通过 {@link
 * org.springframework.jndi.JndiObjectFactoryBean} 在 Spring ApplicationContext 中公开为
 * DataSource bean，以便无缝切换到此类本地 DataSource bean 或从本地 DataSource bean
 * 无缝切换。对于测试，您可以通过第三方的完整解决方案（例如 <a
 * href="https://github.com/h-thurow/Simple-JNDI">Simple-JNDI</a>）设置模拟 JNDI 环境，或者将 bean
 * 定义切换到本地 DataSource（这更简单，因此推荐）。
 * <p>此 {@code DriverManagerDataSource} 类最初是与 <a
 * href="https://commons.apache.org/proper/commons-dbcp">Apache Commons DBCP</a> 和 <a
 * href="https://sourceforge.net/projects/c3p0">C3P0</a> 一起设计的，具有 bean 风格的 {@code
 * BasicDataSource}/{@code ComboPooledDataSource} 类以及用于本地资源设置的配置属性。对于现代 JDBC 连接池，请考虑使用 <a
 * href="https://github.com/brettwooldridge/HikariCP">HikariCP</a>，向应用程序公开相应的 {@code
 * HikariDataSource} 实例。
 * @author Juergen Hoeller
 * @since 14.03.2003
 * @see SimpleDriverDataSource
 */
public class DriverManagerDataSource extends AbstractDriverBasedDataSource {

	/**
	 * bean 样式配置的构造函数。
	 */
	public DriverManagerDataSource() {
	}

	/**
	 * 使用给定的 JDBC URL 创建新的 DriverManagerDataSource，而不指定 JDBC 访问的用户名或密码。
	 * @param url 用于访问 DriverManager 的 JDBC URL
	 * @see java.sql.DriverManager#getConnection(String)
	 */
	public DriverManagerDataSource(String url) {
		setUrl(url);
	}

	/**
	 * 使用给定的标准 DriverManager 参数创建一个新的 DriverManagerDataSource。
	 * @param url 用于访问 DriverManager 的 JDBC URL
	 * @param username 用于访问 DriverManager 的 JDBC 用户名
	 * @param password 用于访问 DriverManager 的 JDBC 密码
	 * @see java.sql.DriverManager#getConnection(String, String, String)
	 */
	public DriverManagerDataSource(String url, String username, String password) {
		setUrl(url);
		setUsername(username);
		setPassword(password);
	}

	/**
	 * 使用给定的 JDBC URL 创建新的 DriverManagerDataSource，而不指定 JDBC 访问的用户名或密码。
	 * @param url 用于访问 DriverManager 的 JDBC URL
	 * @param conProps JDBC 连接属性
	 * @see java.sql.DriverManager#getConnection(String)
	 */
	public DriverManagerDataSource(String url, Properties conProps) {
		setUrl(url);
		setConnectionProperties(conProps);
	}


	/**
	 * 设置 JDBC 驱动程序类名。该驱动程序将在启动时进行初始化，并在 JDK 的 DriverManager 中注册自身。 <p><b>NOTE：DriverManagerDat
	 * aSource 主要用于访问 <i> 预注册的 </i> JDBC 驱动程序。</b> 如果您需要注册新驱动程序，请考虑使用 {@link SimpleDriverDataSo
	 * urce}。或者，考虑在实例化此数据源之前自行初始化 JDBC 驱动程序。保留“driverClassName”属性主要是为了向后兼容，以及在 Commons DBCP 和此数
	 * 据源之间进行迁移。
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


	/**
	 * 获取 Connection From Driver（`ConnectionFromDriver`）。
	 */
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
	 * 使用 DriverManager 中令人讨厌的静态获取连接被提取到受保护的方法中，以便于进行简单的单元测试。
	 * @see java.sql.DriverManager#getConnection(String, java.util.Properties)
	 */
	protected Connection getConnectionFromDriverManager(String url, Properties props) throws SQLException {
		return DriverManager.getConnection(url, props);
	}

}
