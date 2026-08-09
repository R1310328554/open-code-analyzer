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
 * 目标 JDBC {@link javax.sql.DataSource} 的适配器，将指定的用户凭据应用于每个标准 {@code getConnection()}
 * 调用，在目标上隐式调用 {@code getConnection(username, password)}。所有其他方法只是委托给目标数据源的相应方法。
 * <p> 可用于代理未配置用户凭据的目标 JNDI 数据源。客户端代码可以像往常一样使用标准 {@code getConnection()} 调用来使用此数据源。
 * <p>在以下示例中，客户端代码可以简单地透明地使用预配置的“myDataSource”，使用指定的用户凭据隐式访问“myTargetDataSource”。
 * <pre class="code"> <bean id="myTargetDataSource"
 * class="org.springframework.jndi.JndiObjectFactoryBean">
 * &lt;属性名称=“jndiName”值=“java:comp/env/jdbc/myds”/&gt; &lt;/豆&gt;
 * &lt;bean id="myDataSource"
 * class="org.springframework.jdbc.datasource.UserCredentialsDataSourceAdapter"&gt;
 * <属性名称=“targetDataSource”ref=“myTargetDataSource”/> <属性名称=“用户名”值=“我的用户名”/>
 * <属性名称=“密码”值=“我的密码”/> </bean></pre>
 * <p>如果“用户名”为空，则此代理将简单地委托给目标数据源的标准 {@code getConnection()} 方法。这可用于保留 UserCredentialsDataSo
 * urceAdapter bean 定义，以便仅用于 <i>option</i>，以便在特定目标数据源需要时隐式传递用户凭据。
 * @author Juergen Hoeller
 * @since 1.0.2
 * @see #getConnection
 */
public class UserCredentialsDataSourceAdapter extends DelegatingDataSource {

	/** 名称相关状态（`username`）。 */
	private @Nullable String username;

	/** `password`：该类的成员状态。 */
	private @Nullable String password;

	/** `catalog`：该类的成员状态。 */
	private @Nullable String catalog;

	/** `schema`：该类的成员状态。 */
	private @Nullable String schema;

	/** `threadBoundCredentials`：该类的成员状态。 */
	private final ThreadLocal<JdbcUserCredentials> threadBoundCredentials =
			new NamedThreadLocal<>("Current JDBC user credentials");


	/**
	 * 设置此适配器用于检索连接的默认用户名。 <p>Default 没有特定用户。请注意，显式指定的用户名将始终覆盖在数据源级别指定的任何用户名/密码。
	 * @see #setPassword
	 * @see #setCredentialsForCurrentThread(String, String)
	 * @see #getConnection(String, String)
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * 设置此适配器用于检索连接的默认用户密码。 <p>默认是没有特定密码。请注意，显式指定的用户名将始终覆盖在数据源级别指定的任何用户名/密码。
	 * @see #setUsername
	 * @see #setCredentialsForCurrentThread(String, String)
	 * @see #getConnection(String, String)
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * 指定要应用于每个检索到的连接的数据库目录。
	 * @since 4.3.2
	 * @see Connection#setCatalog
	 */
	public void setCatalog(String catalog) {
		this.catalog = catalog;
	}

	/**
	 * 指定要应用于每个检索到的连接的数据库架构。
	 * @since 4.3.2
	 * @see Connection#setSchema
	 */
	public void setSchema(String schema) {
		this.schema = schema;
	}


	/**
	 * 设置此代理和当前线程的用户凭据。给定的用户名和密码将应用于此数据源代理上的所有后续 {@code getConnection()} 调用。 <p>这将覆盖任何静态指定的用户凭据
	 * ，即“用户名”和“密码”bean 属性的值。
	 * @param username 要申请的用户名
	 * @param password 申请密码
	 * @see #removeCredentialsFromCurrentThread
	 */
	public void setCredentialsForCurrentThread(String username, String password) {
		this.threadBoundCredentials.set(new JdbcUserCredentials(username, password));
	}

	/**
	 * 从当前线程中删除此代理的所有用户凭据。之后再次应用静态指定的用户凭据。
	 * @see #setCredentialsForCurrentThread
	 */
	public void removeCredentialsFromCurrentThread() {
		this.threadBoundCredentials.remove();
	}


	/**
	 * 确定当前是否存在线程绑定凭证，如果可用则使用它们，否则回退到静态指定的用户名和密码（即 bean 属性的值）。 <p>D 使用确定的凭据作为参数委托给 {@link #doGe
	 * tConnection(String, String)}。
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
	 * 只需委托给 {@link #doGetConnection(String, String)}，按原样保留给定的用户凭据。
	 */
	@Override
	public Connection getConnection(String username, String password) throws SQLException {
		return doGetConnection(username, password);
	}

	/**
	 * 此实现委托给目标数据源的 {@code getConnection(username, password)} 方法，传入指定的用户凭据。如果指定的用户名为空，它将简单地委托给目
	 * 标数据源的标准 {@code getConnection()} 方法。
	 * @param username 要使用的用户名
	 * @param password 使用的密码
	 * @return 联系
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
