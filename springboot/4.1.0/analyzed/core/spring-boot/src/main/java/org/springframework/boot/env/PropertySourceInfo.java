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

package org.springframework.boot.env;

import org.jspecify.annotations.Nullable;

import org.springframework.core.env.PropertySource;

/**
 * {@link PropertySource} 可选实现的接口，用于提供附加信息。
 *
 * @author Phillip Webb
 * @since 4.0.0
 */
public interface PropertySourceInfo {

	/**
	 * 若此查找不可变且内容永不变更则返回 {@code true}。
	 *
	 * @return if the lookup is immutable 查找是否不可变
	 */
	default boolean isImmutable() {
		return false;
	}

	/**
	 * 返回查找时应用的隐式前缀；未使用前缀时返回 {@code null}。
	 * 前缀可用于消歧可能冲突的键。例如同一机器上运行多个应用时，
	 * 可为每个应用设置不同前缀以确保使用不同环境变量。
	 *
	 * @return the prefix applied by the lookup class or {@code null} 查找类应用的前缀或 {@code null}
	 */
	default @Nullable String getPrefix() {
		return null;
	}

}
