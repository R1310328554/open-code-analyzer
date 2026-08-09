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

package org.springframework.jdbc.support;

import java.sql.SQLException;

import org.jspecify.annotations.Nullable;

import org.springframework.dao.DataAccessException;

/**
 * 在 {@link SQLException SQLExceptions} 与 Spring 数据访问策略无关的
 * {@link DataAccessException} 层次结构之间翻译的策略接口。
 *
 * <p>实现可以是通用的（如 JDBC 使用 {@link java.sql.SQLException#getSQLState() SQLState} 码），
 * 或完全专有的（如 Oracle 错误码）以获得更高精度。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see org.springframework.dao.DataAccessException
 */
@FunctionalInterface
public interface SQLExceptionTranslator {

	/**
	 * 将给定 {@link SQLException} 翻译为通用 {@link DataAccessException}。
	 * <p>返回的 DataAccessException 应包含原始 {@code SQLException} 作为根因；
	 * 但客户端代码通常不应依赖这一点，因为 DataAccessException 也可能由其他资源 API 引起。
	 * 在预期发生了基于 JDBC 的访问时，{@code getRootCause() instanceof SQLException} 检查（及后续转型）是可靠的。
	 * @param task 描述正在尝试任务的可读文本
	 * @param sql 导致问题的 SQL 查询或更新（若已知）
	 * @param ex 触发的 {@code SQLException}
	 * @return 包装 {@code SQLException} 的 DataAccessException；无法翻译时 {@code null}
	 * @see org.springframework.dao.DataAccessException#getRootCause()
	 */
	@Nullable DataAccessException translate(String task, @Nullable String sql, SQLException ex);

}
