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

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.jspecify.annotations.Nullable;

import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.util.CollectionUtils;

/**
 * 包含直接贡献给 {@code SpringApplication} 的默认属性的 {@link MapPropertySource}。
 * 按约定，{@link DefaultPropertiesPropertySource} 始终是 {@link Environment} 中最后一个属性源。
 *
 * @author Phillip Webb
 * @since 2.4.0
 */
public class DefaultPropertiesPropertySource extends MapPropertySource {

	/**
	 * {@code defaultProperties} 属性源的名称。
	 */
	public static final String NAME = "defaultProperties";

	/**
	 * 使用给定 {@code Map} 源创建新的 {@link DefaultPropertiesPropertySource}。
	 *
	 * @param source 源 Map
	 */
	public DefaultPropertiesPropertySource(Map<String, Object> source) {
		super(NAME, source);
	}

	/**
	 * 若给定源名为 {@code defaultProperties} 则返回 {@code true}。
	 *
	 * @param propertySource 要检查的属性源
	 * @return {@code true} if the name matches 名称匹配时为 {@code true}
	 */
	public static boolean hasMatchingName(@Nullable PropertySource<?> propertySource) {
		return (propertySource != null) && propertySource.getName().equals(NAME);
	}

	/**
	 * 若提供的源非空，则创建新的 {@link DefaultPropertiesPropertySource} 实例。
	 *
	 * @param source {@code Map} 源
	 * @param action 消费 {@link DefaultPropertiesPropertySource} 的操作
	 */
	public static void ifNotEmpty(@Nullable Map<String, Object> source,
			@Nullable Consumer<DefaultPropertiesPropertySource> action) {
		if (!CollectionUtils.isEmpty(source) && action != null) {
			action.accept(new DefaultPropertiesPropertySource(source));
		}
	}

	/**
	 * 添加新的 {@link DefaultPropertiesPropertySource} 或与现有实例合并。
	 *
	 * @param source {@code Map} 源
	 * @param sources 现有属性源
	 * @since 2.4.4
	 */
	public static void addOrMerge(Map<String, Object> source, MutablePropertySources sources) {
		if (!CollectionUtils.isEmpty(source)) {
			Map<String, Object> resultingSource = new HashMap<>();
			DefaultPropertiesPropertySource propertySource = new DefaultPropertiesPropertySource(resultingSource);
			if (sources.contains(NAME)) {
				mergeIfPossible(source, sources, resultingSource);
				sources.replace(NAME, propertySource);
			}
			else {
				resultingSource.putAll(source);
				sources.addLast(propertySource);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private static void mergeIfPossible(Map<String, Object> source, MutablePropertySources sources,
			Map<String, Object> resultingSource) {
		PropertySource<?> existingSource = sources.get(NAME);
		if (existingSource != null) {
			Object underlyingSource = existingSource.getSource();
			if (underlyingSource instanceof Map) {
				resultingSource.putAll((Map<String, Object>) underlyingSource);
			}
			resultingSource.putAll(source);
		}
	}

	/**
	 * 将 {@code defaultProperties} 属性源移至给定 {@link ConfigurableEnvironment} 的最后。
	 *
	 * @param environment 要更新的环境
	 */
	public static void moveToEnd(ConfigurableEnvironment environment) {
		moveToEnd(environment.getPropertySources());
	}

	/**
	 * 将 {@code defaultProperties} 属性源移至给定 {@link MutablePropertySources} 的最后。
	 *
	 * @param propertySources 要更新的属性源
	 */
	public static void moveToEnd(MutablePropertySources propertySources) {
		PropertySource<?> propertySource = propertySources.remove(NAME);
		if (propertySource != null) {
			propertySources.addLast(propertySource);
		}
	}

}
