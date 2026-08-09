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

import java.time.Duration;

import org.jspecify.annotations.Nullable;

import org.springframework.core.env.Environment;
import org.springframework.util.Assert;

/**
 * 用于控制配置属性源缓存的接口。
 *
 * @author Phillip Webb
 * @since 2.3.0
 */
public interface ConfigurationPropertyCaching {

	/**
	 * 启用缓存，生存时间不限。
	 */
	void enable();

	/**
	 * 禁用缓存。
	 */
	void disable();

	/**
	 * 设置缓存项的生存时间。调用此方法也会启用缓存。
	 *
	 * @param timeToLive 生存时间
	 */
	void setTimeToLive(Duration timeToLive);

	/**
	 * 清空缓存，并在下次访问时强制重新加载。
	 */
	void clear();

	/**
	 * 临时覆盖并启用缓存。不再需要缓存时应关闭返回的 {@link CacheOverride} 以恢复先前设置。
	 *
	 * @return a {@link CacheOverride} 缓存覆盖句柄
	 * @since 3.5.0
	 */
	CacheOverride override();

	/**
	 * 获取环境中所有配置属性源的缓存控制器。
	 *
	 * @param environment Spring 环境
	 * @return 控制环境中所有属性源的缓存实例
	 */
	static ConfigurationPropertyCaching get(Environment environment) {
		return get(environment, null);
	}

	/**
	 * 获取环境中特定配置属性源的缓存控制器。
	 *
	 * @param environment Spring 环境
	 * @param underlyingSource 必须匹配的
	 * {@link ConfigurationPropertySource#getUnderlyingSource() 底层源}
	 * @return 控制匹配属性源的缓存实例
	 */
	static ConfigurationPropertyCaching get(Environment environment, @Nullable Object underlyingSource) {
		Iterable<ConfigurationPropertySource> sources = ConfigurationPropertySources.get(environment);
		return get(sources, underlyingSource);
	}

	/**
	 * 获取指定配置属性源集合的缓存控制器。
	 *
	 * @param sources 配置属性源
	 * @return 控制这些属性源的缓存实例
	 */
	static ConfigurationPropertyCaching get(Iterable<ConfigurationPropertySource> sources) {
		return get(sources, null);
	}

	/**
	 * 获取指定配置属性源集合中特定属性源的缓存控制器。
	 *
	 * @param sources 配置属性源
	 * @param underlyingSource 必须匹配的
	 * {@link ConfigurationPropertySource#getUnderlyingSource() 底层源}
	 * @return 控制匹配属性源的缓存实例
	 */
	static ConfigurationPropertyCaching get(Iterable<ConfigurationPropertySource> sources,
			@Nullable Object underlyingSource) {
		Assert.notNull(sources, "'sources' must not be null");
		if (underlyingSource == null) {
			return new ConfigurationPropertySourcesCaching(sources);
		}
		for (ConfigurationPropertySource source : sources) {
			if (source.getUnderlyingSource() == underlyingSource) {
				ConfigurationPropertyCaching caching = CachingConfigurationPropertySource.find(source);
				if (caching != null) {
					return caching;
				}
			}
		}
		throw new IllegalStateException("Unable to find cache from configuration property sources");
	}

	/**
	 * 用于控制 {@link ConfigurationPropertyCaching#override() 缓存覆盖} 的 {@link AutoCloseable}。
	 *
	 * @since 3.5.0
	 */
	interface CacheOverride extends AutoCloseable {

		@Override
		void close();

	}

}
