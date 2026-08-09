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
 * 用于在 {@link SQLException SQLExceptions} 和 Spring 的数据访问策略无关的 {@link DataAccessException}
 * 层次结构之间进行转换的策略接口。
 * <p>I 实现可以是通用的（例如，使用 JDBC 的 {@link java.sql.SQLException#getSQLState() SQLState}
 * 代码）或完全专有的（例如，使用 Oracle 错误代码）以获得更高的精度。
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see org.springframework.dao.DataAccessException
 */
@FunctionalInterface
public interface SQLExceptionTranslator {

	/**
	 * 将给定的 {@link SQLException} 转换为通用 {@link DataAccessException}。 <p>返回的 DataAccessException
	 * 应该包含原始 {@code SQLException} 作为根本原因。但是，客户端代码通常可能不依赖于此，因为 DataAccessException 也可能由其他资源 API
	 * 引起。也就是说，当期望发生基于 JDBC 的访问时，{@code getRootCause() instanceof SQLException}
	 * 检查（以及后续转换）被认为是可靠的。
	 * @param task 描述正在尝试的任务的可读文本
	 * @param sql 导致问题的 SQL 查询或更新（如果已知）
	 * @param ex 有问题的 {@code SQLException}
	 * @return DataAccessException 包装 {@code SQLException} 或 {@code null}（如果无法应用特定翻译）
	 * @see org.springframework.dao.DataAccessException#getRootCause()
	 */
	@Nullable DataAccessException translate(String task, @Nullable String sql, SQLException ex);

}
