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

import java.util.Map;

import org.jspecify.annotations.Nullable;

import org.springframework.boot.origin.Origin;
import org.springframework.boot.origin.OriginLookup;
import org.springframework.boot.origin.OriginTrackedValue;
import org.springframework.core.env.MapPropertySource;

/**
 * 由包含 {@link OriginTrackedValue OriginTrackedValues} 的 {@link Map} 支持的 {@link OriginLookup}。
 *
 * @author Madhura Bhave
 * @author Phillip Webb
 * @since 2.0.0
 * @see OriginTrackedValue
 */
public final class OriginTrackedMapPropertySource extends MapPropertySource
		implements PropertySourceInfo, OriginLookup<String> {

	private final boolean immutable;

	/**
	 * 创建新的 {@link OriginTrackedMapPropertySource} 实例。
	 *
	 * @param name 属性源名称
	 * @param source 底层 Map 源
	 */
	@SuppressWarnings("rawtypes")
	public OriginTrackedMapPropertySource(String name, Map source) {
		this(name, source, false);
	}

	/**
	 * 创建新的 {@link OriginTrackedMapPropertySource} 实例。
	 *
	 * @param name 属性源名称
	 * @param source 底层 Map 源
	 * @param immutable 底层源是否不可变且保证不会改变
	 * @since 2.2.0
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public OriginTrackedMapPropertySource(String name, Map source, boolean immutable) {
		super(name, source);
		this.immutable = immutable;
	}

	@Override
	public @Nullable Object getProperty(String name) {
		Object value = super.getProperty(name);
		if (value instanceof OriginTrackedValue originTrackedValue) {
			return originTrackedValue.getValue();
		}
		return value;
	}

	@Override
	public @Nullable Origin getOrigin(String name) {
		Object value = super.getProperty(name);
		if (value instanceof OriginTrackedValue originTrackedValue) {
			return originTrackedValue.getOrigin();
		}
		return null;
	}

	@Override
	public boolean isImmutable() {
		return this.immutable;
	}

}
