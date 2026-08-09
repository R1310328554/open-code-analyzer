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

/**
 * {@link DataObjectBinder} 实现可用于绑定数据对象属性的绑定器。
 *
 * @author Phillip Webb
 * @author Madhura Bhave
 */
interface DataObjectPropertyBinder {

	/**
	 * 绑定给定属性。
	 *
	 * @param propertyName 属性名（小写短横线形式，例如 {@code first-name}）
	 * @param target 目标 bindable
	 * @return 绑定的值或 {@code null}
	 */
	@Nullable Object bindProperty(String propertyName, Bindable<?> target);

}
