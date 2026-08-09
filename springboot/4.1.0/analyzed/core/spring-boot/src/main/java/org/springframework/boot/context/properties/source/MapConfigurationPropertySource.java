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

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.jspecify.annotations.Nullable;

import org.springframework.core.env.MapPropertySource;
import org.springframework.util.Assert;

/**
 * 由 {@link Map} 支持并使用标准名称映射规则的 {@link ConfigurationPropertySource}。
 *
 * @author Phillip Webb
 * @author Madhura Bhave
 * @since 2.0.0
 */
public class MapConfigurationPropertySource implements IterableConfigurationPropertySource {

	private static final PropertyMapper[] DEFAULT_MAPPERS = { DefaultPropertyMapper.INSTANCE };

	private final Map<String, Object> source;

	private final IterableConfigurationPropertySource delegate;

	/**
	 * 创建新的空 {@link MapConfigurationPropertySource} 实例。
	 */
	public MapConfigurationPropertySource() {
		this(Collections.emptyMap());
	}

	/**
	 * 从指定 Map 复制条目，创建新的 {@link MapConfigurationPropertySource} 实例。
	 *
	 * @param map 源 Map
	 */
	public MapConfigurationPropertySource(Map<?, ?> map) {
		this.source = new LinkedHashMap<>();
		MapPropertySource mapPropertySource = new MapPropertySource("source", this.source);
		this.delegate = new SpringIterableConfigurationPropertySource(mapPropertySource, false, DEFAULT_MAPPERS);
		putAll(map);
	}

	/**
	 * 添加指定 Map 中的所有条目。
	 *
	 * @param map 源 Map
	 */
	public void putAll(Map<?, ?> map) {
		Assert.notNull(map, "'map' must not be null");
		assertNotReadOnlySystemAttributesMap(map);
		map.forEach(this::put);
	}

	/**
	 * 添加单个条目。
	 *
	 * @param name 名称
	 * @param value 值
	 */
	public void put(Object name, Object value) {
		Assert.notNull(name, "'name' must not be null");
		this.source.put(name.toString(), value);
	}

	@Override
	public Object getUnderlyingSource() {
		return this.source;
	}

	@Override
	public @Nullable ConfigurationProperty getConfigurationProperty(ConfigurationPropertyName name) {
		return this.delegate.getConfigurationProperty(name);
	}

	@Override
	public Iterator<ConfigurationPropertyName> iterator() {
		return this.delegate.iterator();
	}

	@Override
	public Stream<ConfigurationPropertyName> stream() {
		return this.delegate.stream();
	}

	private void assertNotReadOnlySystemAttributesMap(Map<?, ?> map) {
		try {
			map.size();
		}
		catch (UnsupportedOperationException ex) {
			throw new IllegalArgumentException("Security restricted maps are not supported", ex);
		}
	}

}
