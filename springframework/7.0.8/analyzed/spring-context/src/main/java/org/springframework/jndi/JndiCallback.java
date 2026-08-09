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

package org.springframework.jndi;

import javax.naming.Context;
import javax.naming.NamingException;

import org.jspecify.annotations.Nullable;

/**
 * 回调接口，供需要在 JNDI 上下文中执行操作（如查找）的类实现。
 * 该回调方式简化了错误处理，由 {@link JndiTemplate} 负责，类似 {@code JdbcTemplate} 的做法。
 *
 * <p>几乎无需实现此回调接口，因为 {@link JndiTemplate} 已通过便捷方法提供全部常用 JNDI 操作。
 *
 * @author Rod Johnson
 * @param <T> 结果对象类型
 * @see JndiTemplate
 * @see org.springframework.jdbc.core.JdbcTemplate
 */
@FunctionalInterface
public interface JndiCallback<T> {

	/**
	 * 使用给定 JNDI 上下文执行操作。
	 * <p>实现类无需关心错误处理或清理，{@link JndiTemplate} 会处理这些。
	 * @param ctx 当前 JNDI 上下文
	 * @return 结果对象，或 {@code null}
	 * @throws NamingException 若 JNDI 方法抛出
	 */
	@Nullable T doInContext(Context ctx) throws NamingException;

}

