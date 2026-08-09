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

import java.sql.SQLException;
import java.sql.ShardingKey;

import org.jspecify.annotations.Nullable;

/**
 * 用于确定分片键的策略接口，分片键用于在分片数据库上下文中建立直接分片连接。这用作在 {@link org.springframework.jdbc.datasource.Sha
 * rdingKeyDataSourceAdapter} 中提供当前分片键（以及可选的超级分片键）的回调。
 * <p>可以用作简单分片键的函数接口（例如，使用 lambda 表达式），或者在包含超级分片键时用作两种方法接口。
 * @author Mohamed Lahyane (Anir)
 * @author Juergen Hoeller
 * @since 6.1.2
 * @see ShardingKeyDataSourceAdapter#setShardingKeyProvider
 */
public interface ShardingKeyProvider {

	/**
	 * 确定分片键。该方法返回与当前上下文相关的分片键，用于获取直接分片连接。
	 * @return 分片键，或 {@code null}（如果不可用或无法确定）
	 * @throws SQLException 如果获取分片key出错
	 */
	@Nullable ShardingKey getShardingKey() throws SQLException;

	/**
	 * 确定超级分片键（如果有）。该方法返回与当前上下文相关的超级分片键，用于获取直接分片连接。
	 * @return 超级分片键，如果不可用或无法确定则为 {@code null}（默认）
	 * @throws SQLException 如果获取超级分片键出错
	 */
	default @Nullable ShardingKey getSuperShardingKey() throws SQLException {
		return null;
	}

}
