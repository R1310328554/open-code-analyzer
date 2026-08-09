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
 * 确定分片键的策略接口，用于在分片数据库场景下建立分片直连。
 * 作为回调在 {@link org.springframework.jdbc.datasource.ShardingKeyDataSourceAdapter}
 * 中提供当前分片键（以及可选的上级分片键）。
 *
 * <p>简单分片键可作为函数式接口（例如 lambda）使用；
 * 若需上级分片键，则作为双方法接口使用。
 *
 * @author Mohamed Lahyane (Anir)
 * @author Juergen Hoeller
 * @since 6.1.2
 * @see ShardingKeyDataSourceAdapter#setShardingKeyProvider
 */
public interface ShardingKeyProvider {

	/**
	 * 确定分片键。返回与当前上下文相关、用于获取分片直连的分片键。
	 * @return 分片键，不可用或无法确定时返回 {@code null}
	 * @throws SQLException 获取分片键时出错
	 */
	@Nullable ShardingKey getShardingKey() throws SQLException;

	/**
	 * 确定上级分片键（若有）。返回与当前上下文相关、用于获取分片直连的上级分片键。
	 * @return 上级分片键，不可用或无法确定时返回 {@code null}（默认）
	 * @throws SQLException 获取上级分片键时出错
	 */
	default @Nullable ShardingKey getSuperShardingKey() throws SQLException {
		return null;
	}

}
