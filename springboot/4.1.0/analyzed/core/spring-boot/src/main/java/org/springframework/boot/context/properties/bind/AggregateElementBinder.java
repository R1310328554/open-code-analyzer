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

import org.springframework.boot.context.properties.source.ConfigurationPropertyName;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;

/**
 * 供 {@link AggregateBinder} 实现递归绑定元素时使用的绑定器。
 *
 * @author Phillip Webb
 * @author Madhura Bhave
 */
@FunctionalInterface
interface AggregateElementBinder {

	/**
	 * 将给定名称绑定到目标 Bindable。
	 *
	 * @param name 要绑定的名称
	 * @param target 目标 Bindable
	 * @return 绑定后的对象，或 {@code null}
	 */
	default @Nullable Object bind(ConfigurationPropertyName name, Bindable<?> target) {
		return bind(name, target, null);
	}

	/**
	 * 将给定名称绑定到目标 Bindable，可限定为单一属性源。
	 *
	 * @param name 要绑定的名称
	 * @param target 目标 Bindable
	 * @param source 元素来源，或 {@code null} 表示使用所有源
	 * @return 绑定后的对象，或 {@code null}
	 */
	@Nullable Object bind(ConfigurationPropertyName name, Bindable<?> target, @Nullable ConfigurationPropertySource source);

}
