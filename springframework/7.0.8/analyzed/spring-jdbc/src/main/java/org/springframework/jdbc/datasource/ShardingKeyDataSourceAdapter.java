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
 * 目标 {@link DataSource} 的适配器，在每次标准 {@code #getConnection} 调用时
 * （若已指定）应用分片键，返回对应分片的直连。
 * 其余方法均委托给目标 {@code DataSource} 的对应方法。
 *
 * <p>目标 {@code DataSource} 必须实现 {@link #createConnectionBuilder}；
 * 否则获取分片连接时将抛出 {@link java.sql.SQLFeatureNotSupportedException}。
 *
 * <p>须配置 {@link ShardingKeyProvider} 回调，
 * 在每次 {@code #getConnection} 调用时获取当前分片键，例如：
 *
 * <pre class="code">
 * ShardingKeyDataSourceAdapter dataSourceAdapter = new ShardingKeyDataSourceAdapter(dataSource);
 * dataSourceAdapter.setShardingKeyProvider(() -&gt; dataSource.createShardingKeyBuilder()
 *     .subkey(SecurityContextHolder.getContext().getAuthentication().getName(), JDBCType.VARCHAR).build());
 * </pre>
 *
 * @author Mohamed Lahyane (Anir)
 * @author Juergen Hoeller
 * @since 6.1.2
 * @see #getConnection
 * @see #createConnectionBuilder()
 * @see UserCredentialsDataSourceAdapter
 */
public class ShardingKeyDataSourceAdapter extends DelegatingDataSource {

	private @Nullable ShardingKeyProvider shardingkeyProvider;


	/**
	 * 创建新的 {@code ShardingKeyDataSourceAdapter} 实例，包装给定 {@link DataSource}。
	 * @param dataSource 要包装的目标 {@code DataSource}
	 */
	public ShardingKeyDataSourceAdapter(DataSource dataSource) {
		super(dataSource);
	}

	/**
	 * 创建新的 {@code ShardingKeyDataSourceAdapter} 实例，包装给定 {@link DataSource}。
	 * @param dataSource 要包装的目标 {@code DataSource}
	 * @param shardingKeyProvider 用于获取分片键的 {@code ShardingKeyProvider}
	 */
	public ShardingKeyDataSourceAdapter(DataSource dataSource, ShardingKeyProvider shardingKeyProvider) {
		super(dataSource);
		this.shardingkeyProvider = shardingKeyProvider;
	}


	/**
	 * 设置本适配器的 {@link ShardingKeyProvider}。
	 */
	public void setShardingKeyProvider(ShardingKeyProvider shardingKeyProvider) {
		this.shardingkeyProvider = shardingKeyProvider;
	}


	/**
	 * 使用提供的分片键和上级分片键（若有）获取数据库分片连接。
	 * <p>分片键来自已配置的 {@link #setShardingKeyProvider ShardingKeyProvider}。
	 * @return 表示分片直连的 {@code Connection} 对象
	 * @throws SQLException 创建连接时出错
	 * @see #createConnectionBuilder()
	 */
	@Override
	public Connection getConnection() throws SQLException {
		return createConnectionBuilder().build();
	}

	/**
	 * 使用给定用户名和密码获取数据库分片连接，
	 * 同时考虑分片键（若有）和给定凭据。
	 * <p>分片键来自已配置的 {@link #setShardingKeyProvider ShardingKeyProvider}。
	 * @param username 代表其建立连接的数据库用户
	 * @param password 用户密码
	 * @return 表示分片直连的 {@code Connection} 对象
	 * @throws SQLException 创建连接时出错
	 */
	@Override
	public Connection getConnection(String username, String password) throws SQLException {
		return createConnectionBuilder().user(username).password(password).build();
	}

	/**
	 * 使用目标 {@code DataSource} 的 {@code createConnectionBuilder()} 方法
	 * 创建新的 {@link ConnectionBuilder} 实例，并设置已配置
	 * {@link #setShardingKeyProvider ShardingKeyProvider} 的相应分片键。
	 * @return 用于构建分片直连的 ConnectionBuilder 对象
	 * @throws SQLException 创建 ConnectionBuilder 时出错
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
