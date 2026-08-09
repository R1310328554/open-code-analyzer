/*
 * Copyright 2012-present the original author or authors.
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

package org.springframework.boot.context.properties.bind;

import org.jspecify.annotations.Nullable;

import org.springframework.core.env.PropertyResolver;

/**
 * {@link Binder} 用于解析属性占位符的可选策略。
 *
 * @author Phillip Webb
 * @author Madhura Bhave
 * @since 2.0.0
 * @see PropertySourcesPlaceholdersResolver
 */
@FunctionalInterface
public interface PlaceholdersResolver {

	/**
	 * 空操作的 {@link PropertyResolver}。
	 */
	PlaceholdersResolver NONE = (value) -> value;

	/**
	 * 解析给定值中的占位符。
	 *
	 * @param value 源值
	 * @return 占位符已解析的值
	 */
	@Nullable Object resolvePlaceholders(@Nullable Object value);

}
