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

package org.springframework.boot.info;

import java.time.Instant;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.jspecify.annotations.Nullable;

import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.util.Assert;

/**
 * 暴露非结构化数据的组件基类，为常见键提供专用方法。
 *
 * @author Stephane Nicoll
 * @since 1.4.0
 */
public class InfoProperties implements Iterable<InfoProperties.Entry> {

	private final Properties entries;

	/**
	 * 使用指定条目创建实例。
	 *
	 * @param entries 要暴露的信息
	 */
	public InfoProperties(Properties entries) {
		Assert.notNull(entries, "'entries' must not be null");
		this.entries = copy(entries);
	}

	/**
	 * 返回指定属性的值或 {@code null}。
	 *
	 * @param key 属性键
	 * @return 属性值
	 */
	public @Nullable String get(String key) {
		return this.entries.getProperty(key);
	}

	/**
	 * 将指定属性值作为 {@link Instant} 返回；若值不是有效的 epoch {@link Long} 表示则返回 {@code null}。
	 *
	 * @param key 属性键
	 * @return 属性值
	 */
	public @Nullable Instant getInstant(String key) {
		String s = get(key);
		if (s != null) {
			try {
				return Instant.ofEpochMilli(Long.parseLong(s));
			}
			catch (NumberFormatException ex) {
				// Not valid epoch time
			}
		}
		return null;
	}

	@Override
	public Iterator<Entry> iterator() {
		return new PropertiesIterator(this.entries);
	}

	/**
	 * 返回此实例对应的 {@link PropertySource}。
	 *
	 * @return {@link PropertySource}
	 */
	public PropertySource<?> toPropertySource() {
		return new PropertiesPropertySource(getClass().getSimpleName(), copy(this.entries));
	}

	private Properties copy(Properties properties) {
		Properties copy = new Properties();
		copy.putAll(properties);
		return copy;
	}

	private static final class PropertiesIterator implements Iterator<Entry> {

		private final Iterator<Map.Entry<Object, Object>> iterator;

		private PropertiesIterator(Properties properties) {
			this.iterator = properties.entrySet().iterator();
		}

		@Override
		public boolean hasNext() {
			return this.iterator.hasNext();
		}

		@Override
		public Entry next() {
			Map.Entry<Object, Object> entry = this.iterator.next();
			return new Entry((String) entry.getKey(), (String) entry.getValue());
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("InfoProperties are immutable.");
		}

	}

	/**
	 * 属性条目。
	 */
	public static final class Entry {

		private final String key;

		private final String value;

		private Entry(String key, String value) {
			this.key = key;
			this.value = value;
		}

		public String getKey() {
			return this.key;
		}

		public String getValue() {
			return this.value;
		}

	}

}
