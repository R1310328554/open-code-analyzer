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

package org.springframework.boot.autoconfigure;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Set;

import org.jspecify.annotations.Nullable;

import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.util.StringUtils;

/**
 * 用于加载 {@link AutoConfigurationMetadata} 的内部工具类。
 *
 * @author Phillip Webb
 */
final class AutoConfigurationMetadataLoader {

	/** 元数据属性文件在类路径上的默认位置。 */
	private static final String PATH = "META-INF/spring-autoconfigure-metadata.properties";

	private AutoConfigurationMetadataLoader() {
	}

	static AutoConfigurationMetadata loadMetadata(ClassLoader classLoader) {
		return loadMetadata(classLoader, PATH);
	}

	static AutoConfigurationMetadata loadMetadata(@Nullable ClassLoader classLoader, String path) {
		try {
			Enumeration<URL> urls = (classLoader != null) ? classLoader.getResources(path)
					: ClassLoader.getSystemResources(path);
			Properties properties = new Properties();
			// 合并类路径上所有匹配位置的属性
			while (urls.hasMoreElements()) {
				properties.putAll(PropertiesLoaderUtils.loadProperties(new UrlResource(urls.nextElement())));
			}
			return loadMetadata(properties);
		}
		catch (IOException ex) {
			throw new IllegalArgumentException("Unable to load @ConditionalOnClass location [" + path + "]", ex);
		}
	}

	static AutoConfigurationMetadata loadMetadata(Properties properties) {
		return new PropertiesAutoConfigurationMetadata(properties);
	}

	/**
	 * 基于属性文件实现的 {@link AutoConfigurationMetadata}。
	 */
	private static class PropertiesAutoConfigurationMetadata implements AutoConfigurationMetadata {

		/** 底层属性存储。 */
		private final Properties properties;

		PropertiesAutoConfigurationMetadata(Properties properties) {
			this.properties = properties;
		}

		@Override
		public boolean wasProcessed(String className) {
			return this.properties.containsKey(className);
		}

		@Override
		public @Nullable Integer getInteger(String className, String key) {
			return getInteger(className, key, null);
		}

		@Override
		public @Nullable Integer getInteger(String className, String key, @Nullable Integer defaultValue) {
			String value = get(className, key);
			return (value != null) ? Integer.valueOf(value) : defaultValue;
		}

		@Override
		public @Nullable Set<String> getSet(String className, String key) {
			return getSet(className, key, null);
		}

		@Override
		public @Nullable Set<String> getSet(String className, String key, @Nullable Set<String> defaultValue) {
			String value = get(className, key);
			return (value != null) ? StringUtils.commaDelimitedListToSet(value) : defaultValue;
		}

		@Override
		public @Nullable String get(String className, String key) {
			return get(className, key, null);
		}

		@Override
		public @Nullable String get(String className, String key, @Nullable String defaultValue) {
			String value = this.properties.getProperty(className + "." + key);
			return (value != null) ? value : defaultValue;
		}

	}

}
