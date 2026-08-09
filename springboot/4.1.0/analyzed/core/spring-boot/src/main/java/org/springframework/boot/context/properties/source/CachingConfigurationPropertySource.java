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

package org.springframework.boot.context.properties.source;

import org.jspecify.annotations.Nullable;

/**
 * 用于标识 {@link ConfigurationPropertySource} 支持 {@link ConfigurationPropertyCaching} 的接口。
 *
 * @author Phillip Webb
 */
interface CachingConfigurationPropertySource {

	/**
	 * 返回此属性源的 {@link ConfigurationPropertyCaching}。
	 *
	 * @return source caching 属性源缓存
	 */
	ConfigurationPropertyCaching getCaching();

	/**
	 * 查找给定属性源对应的 {@link ConfigurationPropertyCaching}。
	 *
	 * @param source 配置属性源
	 * @return {@link ConfigurationPropertyCaching} 实例；若属性源不支持缓存则返回 {@code null}
	 */
	static @Nullable ConfigurationPropertyCaching find(@Nullable ConfigurationPropertySource source) {
		if (source instanceof CachingConfigurationPropertySource cachingSource) {
			return cachingSource.getCaching();
		}
		return null;
	}

}
