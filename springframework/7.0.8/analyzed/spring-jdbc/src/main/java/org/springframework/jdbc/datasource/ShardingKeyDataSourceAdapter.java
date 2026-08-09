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
import java.sql.ConnectionBuilder;
import java.sql.SQLException;
import java.sql.ShardingKey;

import javax.sql.DataSource;

import org.jspecify.annotations.Nullable;

/**
 * 目标 {@link DataSource} 的适配器，旨在将分片键（如果指定）应用于每个标准 {@code #getConnection} 调用，返回与指定分片键值对应的分片的
 * 直接连接。所有其他方法都简单地委托给目标 {@code DataSource} 的相应方法。
 * <p>目标{@code DataSource}必须实现{@link #createConnectionBuilder}方法；否则，在尝试获取分片连接时将抛出 {@link
 * java.sql.SQLFeatureNotSupportedException}。
 * <p> 该适配器需要配置 {@link ShardingKeyProvider} 回调，该回调用于获取每个 {@code #getConnection}
 * 调用的当前分片键，例如：
 * <pre class="code"> ShardingKeyDataSourceAdapter dataSourceAdapter = new
 * ShardingKeyDataSourceAdapter(dataSource); dataSourceAdapter.setShardingKeyProvider(() ->
 * dataSource.createShardingKeyBuilder()
 * .subkey(SecurityContextHolder.getContext().getAuthentication().getName(),
 * JDBCType.VARCHAR).build()); OCAJAVA1文档
 * @author Mohamed Lahyane (Anir)
 * @author Juergen Hoeller
 * @since 6.1.2
 * @see #getConnection
 * @see #createConnectionBuilder()
 * @see UserCredentialsDataSourceAdapter
 */
public class ShardingKeyDataSourceAdapter extends DelegatingDataSource {

	/** `shardingkeyProvider`：该类的成员状态。 */
	private @Nullable ShardingKeyProvider shardingkeyProvider;


	/**
	 * 创建 {@code ShardingKeyDataSourceAdapter} 的新实例，包装给定的 {@link DataSource}。
	 * @param dataSource 要包装的目标 {@code DataSource}
	 */
	public ShardingKeyDataSourceAdapter(DataSource dataSource) {
		super(dataSource);
	}

	/**
	 * 创建 {@code ShardingKeyDataSourceAdapter} 的新实例，包装给定的 {@link DataSource}。
	 * @param dataSource 要包装的目标 {@code DataSource}
	 * @param shardingKeyProvider {@code ShardingKeyProvider} 用于获取分片键
	 */
	public ShardingKeyDataSourceAdapter(DataSource dataSource, ShardingKeyProvider shardingKeyProvider) {
		super(dataSource);
		this.shardingkeyProvider = shardingKeyProvider;
	}


	/**
	 * 为此适配器设置 {@link ShardingKeyProvider}。
	 */
	public void setShardingKeyProvider(ShardingKeyProvider shardingKeyProvider) {
		this.shardingkeyProvider = shardingKeyProvider;
	}


	/**
	 * 使用提供的分片键和超级分片键（如果可用）获取与数据库分片的连接。 <p>的分片键是从配置的{@link #setShardingKeyProvider ShardingKeyP
	 * rovider}中获取的。
	 * @return 表示直接分片连接的 {@code Connection} 对象
	 * @throws SQLException 如果创建连接时发生错误
	 * @see #createConnectionBuilder()
	 */
	@Override
	public Connection getConnection() throws SQLException {
		return createConnectionBuilder().build();
	}

	/**
	 * 使用提供的用户名和密码获取与数据库分片的连接，并考虑分片密钥（如果可用）和给定的凭据。 <p>的分片键是从配置的{@link #setShardingKeyProvider S
	 * hardingKeyProvider}中获取的。
	 * @param username 代表其建立连接的数据库用户
	 * @param password 用户的密码
	 * @return 表示直接分片连接的 {@code Connection} 对象
	 * @throws SQLException 如果创建连接时发生错误
	 */
	@Override
	public Connection getConnection(String username, String password) throws SQLException {
		return createConnectionBuilder().user(username).password(password).build();
	}

	/**
	 * 使用目标 {@code DataSource} 的 {@code createConnectionBuilder()} 方法创建 {@link
	 * ConnectionBuilder} 的新实例，并从配置的 {@link #setShardingKeyProvider ShardingKeyProvider}
	 * 设置适当的分片键。
	 * @return ConnectionBuilder 对象表示直接分片连接的构建器
	 * @throws SQLException 如果创建 ConnectionBuilder 时发生错误
	 */
	@Override
	public ConnectionBuilder createConnectionBuilder() throws SQLException {
		ConnectionBuilder connectionBuilder = super.createConnectionBuilder();
		if (this.shardingkeyProvider == null) {
			return connectionBuilder;
		}

		ShardingKey shardingKey = this.shardingkeyProvider.getShardingKey();
		ShardingKey superShardingKey = this.shardingkeyProvider.getSuperShardingKey();
		return connectionBuilder.shardingKey(shardingKey).superShardingKey(superShardingKey);
	}

}
