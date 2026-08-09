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

package org.springframework.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * {@link JdbcTemplate}类使用的批量更新回调接口。
 * <p> 此接口在 JdbcTemplate 类提供的 {@link java.sql.PreparedStatement} 上设置值，为使用相同 SQL 的批量更新中的每一个更
 * 新设置值。实现负责设置任何必要的参数。带有占位符的 SQL 已经提供。
 * <p> 实现 <i> 不需要 </i> 需要关注可能从它们尝试的操作中抛出的 SQLException。 JdbcTemplate 类将捕获并适当地处理
 * SQLException。
 * @author Rod Johnson
 * @since March 2, 2003
 * @see JdbcTemplate#batchUpdate(String, BatchPreparedStatementSetter)
 * @see InterruptibleBatchPreparedStatementSetter
 */
public interface BatchPreparedStatementSetter {

	/**
	 * 在给定的PreparedStatement上设置参数值。
	 * @param ps 调用setter方法的PreparedStatement
	 * @param i 我们在批次中发出的语句的索引，从 0 开始
	 * @throws SQLException 如果遇到 SQLException（即不需要捕获 SQLException）
	 */
	void setValues(PreparedStatement ps, int i) throws SQLException;

	/**
	 * 返回批次的大小。
	 * @return 批次中的语句数
	 */
	int getBatchSize();

}
