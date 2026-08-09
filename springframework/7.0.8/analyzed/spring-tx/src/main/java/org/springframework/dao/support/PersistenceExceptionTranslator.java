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

package org.springframework.dao.support;

import org.jspecify.annotations.Nullable;

import org.springframework.dao.DataAccessException;

/**
 * 由与抛出运行时异常的数据访问技术（如 JPA 和 Hibernate）
 * 集成的 Spring 组件实现的接口。
 *
 * <p>允许一致地使用组合异常转换功能，
 * 而无需单个转换器理解所有可能的异常类型。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 2.0
 */
@FunctionalInterface
public interface PersistenceExceptionTranslator {

	/**
	 * 若可能，将持久化框架抛出的给定运行时异常转换为
	 * Spring 通用 {@link org.springframework.dao.DataAccessException} 层次结构中
	 * 对应的异常。
	 * <p>不要转换本转换器无法理解的异常：
	 * 例如来自其他持久化框架、用户代码或与持久化无关的异常。
	 * <p>正确转换为 DataIntegrityViolationException（例如约束违例）尤为重要。
	 * 实现可在根因为 SQLException 时利用 Spring JDBC 的精细异常转换
	 * 提供更多信息。
	 * @param ex 要转换的 RuntimeException
	 * @return 对应的 DataAccessException（若无法转换则返回 {@code null}，
	 * 此时异常可能来自用户代码而非实际持久化问题）
	 * @see org.springframework.dao.DataIntegrityViolationException
	 * @see org.springframework.jdbc.support.SQLExceptionTranslator
	 */
	@Nullable DataAccessException translateExceptionIfPossible(RuntimeException ex);

}
