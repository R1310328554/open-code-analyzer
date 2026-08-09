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

import org.jspecify.annotations.Nullable;

import org.springframework.core.NamedThreadLocal;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * 目标 JDBC {@link javax.sql.DataSource} 的适配器，
 * 将指定用户凭据应用于每次标准 {@code getConnection()} 调用，
 * 隐式在目标上调用 {@code getConnection(username, password)}。
 * 其他方法均委托给目标 DataSource 的对应方法。
 *
 * <p>可用于代理未配置用户凭据的目标 JNDI DataSource。
 * 客户端代码可照常使用标准 {@code getConnection()} 调用。
 *
 * <p>下例中，客户端代码可透明地使用预配置的 "myDataSource"，
 * 以指定凭据隐式访问 "myTargetDataSource"。
 *
 * <pre class="code">
 * &lt;bean id="myTargetDataSource" class="org.springframework.jndi.JndiObjectFactoryBean"&gt;
 *   &lt;property name="jndiName" value="java:comp/env/jdbc/myds"/&gt;
 * &lt;/bean&gt;
 *
 * &lt;bean id="myDataSource" class="org.springframework.jdbc.datasource.UserCredentialsDataSourceAdapter"&gt;
 *   &lt;property name="targetDataSource" ref="myTargetDataSource"/&gt;
 *   &lt;property name="username" value="myusername"/&gt;
 *   &lt;property name="password" value="mypassword"/&gt;
 * &lt;/bean&gt;</pre>
 *
 * <p>若 "username" 为空，此代理直接委托目标 DataSource 的标准 {@code getConnection()}。
 * 可保留 UserCredentialsDataSourceAdapter Bean 定义，
 * 以便在特定目标 DataSource 需要时<i>可选</i>隐式传入用户凭据。
 *
 * @author Juergen Hoeller
 * @since 1.0.2
 * @see #getConnection
 */
public class UserCredentialsDataSourceAdapter extends DelegatingDataSource {

	private @Nullable String username;

	private @Nullable String password;

	private @Nullable String catalog;

	private @Nullable String schema;

	private final ThreadLocal<JdbcUserCredentials> threadBoundCredentials =
			new NamedThreadLocal<>("Current JDBC user credentials");


	/**
	 * 设置此适配器获取 Connection 时使用的默认用户名。
	 * <p>默认无特定用户。显式指定的用户名始终覆盖 DataSource 级别的用户名/密码。
	 * @see #setPassword
	 * @see #setCredentialsForCurrentThread(String, String)
	 * @see #getConnection(String, String)
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * 设置此适配器获取 Connection 时使用的默认用户密码。
	 * <p>默认无特定密码。显式指定的用户名始终覆盖 DataSource 级别的用户名/密码。
	 * @see #setUsername
	 * @see #setCredentialsForCurrentThread(String, String)
	 * @see #getConnection(String, String)
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * 指定应用于每个获取 Connection 的数据库 catalog。
	 * @since 4.3.2
	 * @see Connection#setCatalog
	 */
	public void setCatalog(String catalog) {
		this.catalog = catalog;
	}

	/**
	 * 指定应用于每个获取 Connection 的数据库 schema。
	 * @since 4.3.2
	 * @see Connection#setSchema
	 */
	public void setSchema(String schema) {
		this.schema = schema;
	}


	/**
	 * 为此代理及当前线程设置用户凭据。
	 * 给定用户名和密码将应用于此后此 DataSource 代理的所有 {@code getConnection()} 调用。
	 * <p>将覆盖静态指定的用户凭据，即 "username" 和 "password" Bean 属性值。
	 * @param username 要应用的用户名
	 * @param password 要应用的密码
	 * @see #removeCredentialsFromCurrentThread
	 */
	public void setCredentialsForCurrentThread(String username, String password) {
		this.threadBoundCredentials.set(new JdbcUserCredentials(username, password));
	}

	/**
	 * 从当前线程移除此代理的用户凭据。
	 * 之后重新应用静态指定的用户凭据。
	 * @see #setCredentialsForCurrentThread
	 */
	public void removeCredentialsFromCurrentThread() {
		this.threadBoundCredentials.remove();
	}


	/**
	 * 确定当前是否有线程绑定凭据，有则使用，
	 * 否则回退到静态指定的用户名和密码（即 Bean 属性值）。
	 * <p>以确定的凭据为参数委托 {@link #doGetConnection(String, String)}。
	 * @see #doGetConnection
	 */
	@Override
	public Connection getConnection() throws SQLException {
		JdbcUserCredentials threadCredentials = this.threadBoundCredentials.get();
		Connection con = (threadCredentials != null ?
				doGetConnection(threadCredentials.username, threadCredentials.password) :
				doGetConnection(this.username, this.password));

		if (this.catalog != null) {
			con.setCatalog(this.catalog);
		}
		if (this.schema != null) {
			con.setSchema(this.schema);
		}
		return con;
	}

	/**
	 * 直接委托 {@link #doGetConnection(String, String)}，
	 * 保持给定用户凭据不变。
	 */
	@Override
	public Connection getConnection(String username, String password) throws SQLException {
		return doGetConnection(username, password);
	}

	/**
	 * 本实现委托目标 DataSource 的 {@code getConnection(username, password)}，
	 * 传入指定用户凭据。
	 * 若用户名为空，则直接委托目标 DataSource 的标准 {@code getConnection()}。
	 * @param username 要使用的用户名
	 * @param password 要使用的密码
	 * @return Connection
	 * @see javax.sql.DataSource#getConnection(String, String)
	 * @see javax.sql.DataSource#getConnection()
	 */
	protected Connection doGetConnection(@Nullable String username, @Nullable String password) throws SQLException {
		Assert.state(getTargetDataSource() != null, "'targetDataSource' is required");
		if (StringUtils.hasLength(username)) {
			return getTargetDataSource().getConnection(username, password);
		}
		else {
			return getTargetDataSource().getConnection();
		}
	}


	/**
	 * 用作 ThreadLocal 值的内部类。
	 */
	private static final class JdbcUserCredentials {

		public final String username;

		public final String password;

		public JdbcUserCredentials(String username, String password) {
			this.username = username;
			this.password = password;
		}

		@Override
		public String toString() {
			return "JdbcUserCredentials[username='" + this.username + "',password='" + this.password + "']";
		}
	}

}
