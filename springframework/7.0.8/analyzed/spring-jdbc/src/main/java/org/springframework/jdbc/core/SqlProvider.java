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

import org.jspecify.annotations.Nullable;

/**
 * 由可以提供 SQL 字符串的对象实现的接口。
 * <p>通常由PreparedStatementCreators、CallableStatementCreators和StatementCallbacks实现，它们希望公开用于创
 * 建语句的SQL，以便在出现异常时提供更好的上下文信息。
 * @author Juergen Hoeller
 * @since 16.03.2004
 * @see PreparedStatementCreator
 * @see CallableStatementCreator
 * @see StatementCallback
 */
public interface SqlProvider {

	/**
	 * 返回该对象的 SQL 字符串，即通常用于创建语句的 SQL。
	 * @return SQL 字符串，或 {@code null}（如果不可用）
	 */
	@Nullable String getSql();

}
