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
 * {@link JdbcTemplate} 使用的批量更新回调接口。
 *
 * <p>对同一 SQL 的批量更新中，为 JdbcTemplate 提供的
 * {@link java.sql.PreparedStatement} 逐条设值；实现类负责设置必要参数。
 * 带占位符的 SQL 已由框架提供。
 *
 * <p>实现类<i>无需</i>处理操作中可能抛出的 SQLException；
 * JdbcTemplate 会适当捕获并处理。
 *
 * @author Rod Johnson
 * @since March 2, 2003
 * @see JdbcTemplate#batchUpdate(String, BatchPreparedStatementSetter)
 * @see InterruptibleBatchPreparedStatementSetter
 */
public interface BatchPreparedStatementSetter {

	/**
	 * 为给定 PreparedStatement 设置参数值。
	 * @param ps 要调用 setter 的 PreparedStatement
	 * @param i 批量中当前语句索引，从 0 起
	 * @throws SQLException 若遇到 SQLException（无需自行捕获）
	 */
	void setValues(PreparedStatement ps, int i) throws SQLException;

	/**
	 * 返回批量大小。
	 * @return 批量中语句数量
	 */
	int getBatchSize();

}
