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

package org.springframework.context.annotation;

import org.springframework.beans.factory.config.BeanDefinition;

/**
 * 解析 Bean 定义作用域的策略接口。
 *
 * @author Mark Fisher
 * @since 2.5
 * @see org.springframework.context.annotation.Scope
 */
@FunctionalInterface
public interface ScopeMetadataResolver {

	/**
	 * 为给定的 Bean {@code definition} 解析合适的 {@link ScopeMetadata}。
	 * <p>实现可采用任意策略确定作用域元数据，例如读取
	 * {@link BeanDefinition#getBeanClassName() 类}上的源码级注解，
	 * 或利用 {@link BeanDefinition#attributeNames()} 中的元数据。
	 * @param definition 目标 Bean 定义
	 * @return 相关作用域元数据；永不为 {@code null}
	 */
	ScopeMetadata resolveScopeMetadata(BeanDefinition definition);

}
