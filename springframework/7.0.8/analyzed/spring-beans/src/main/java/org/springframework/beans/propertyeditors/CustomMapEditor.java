/*
 * Copyright 2002-present the original author or authors.
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

package org.springframework.beans.propertyeditors;

import java.beans.PropertyEditorSupport;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.jspecify.annotations.Nullable;

import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

/**
 * Map 属性编辑器，将任意源 Map 转换为目标 Map 类型。
 *
 * @author Juergen Hoeller
 * @since 2.0.1
 * @see java.util.Map
 * @see java.util.SortedMap
 */
public class CustomMapEditor extends PropertyEditorSupport {

	/** 目标 Map 类型。 */
	@SuppressWarnings("rawtypes")
	private final Class<? extends Map> mapType;

	/** 是否将传入的 {@code null} 转换为空 Map。 */
	private final boolean nullAsEmptyMap;


	/**
	 * 为给定目标类型创建新的 CustomMapEditor，传入的 {@code null} 保持原样。
	 * @param mapType 目标类型，须为 Map 的子接口或具体实现类
	 * @see java.util.Map
	 * @see java.util.HashMap
	 * @see java.util.TreeMap
	 * @see java.util.LinkedHashMap
	 */
	@SuppressWarnings("rawtypes")
	public CustomMapEditor(Class<? extends Map> mapType) {
		this(mapType, false);
	}

	/**
	 * 为给定目标类型创建新的 CustomMapEditor。
	 * <p>若传入值已是目标类型，则直接使用。
	 * 若为其他 Map 类型，则转换为目标 Map 类型的默认实现。
	 * 若为其他类型，则创建仅含该单个值的目标 Map。
	 * <p>默认实现为：SortedMap 对应 TreeMap，Map 对应 LinkedHashMap。
	 * @param mapType 目标类型，须为 Map 的子接口或具体实现类
	 * @param nullAsEmptyMap 是否将传入的 {@code null} 转换为空 Map（对应类型）
	 * @see java.util.Map
	 * @see java.util.TreeMap
	 * @see java.util.LinkedHashMap
	 */
	@SuppressWarnings("rawtypes")
	public CustomMapEditor(Class<? extends Map> mapType, boolean nullAsEmptyMap) {
		Assert.notNull(mapType, "Map type is required");
		if (!Map.class.isAssignableFrom(mapType)) {
			throw new IllegalArgumentException(
					"Map type [" + mapType.getName() + "] does not implement [java.util.Map]");
		}
		this.mapType = mapType;
		this.nullAsEmptyMap = nullAsEmptyMap;
	}


	/**
	 * 将给定文本值转换为仅含单个元素的 Map。
	 */
	@Override
	public void setAsText(String text) throws IllegalArgumentException {
		setValue(text);
	}

	/**
	 * 将给定值转换为目标类型的 Map。
	 */
	@Override
	public void setValue(@Nullable Object value) {
		if (value == null && this.nullAsEmptyMap) {
			super.setValue(createMap(this.mapType, 0));
		}
		else if (value == null || (this.mapType.isInstance(value) && !alwaysCreateNewMap())) {
			// 源值类型已匹配目标类型，直接使用
			super.setValue(value);
		}
		else if (value instanceof Map<?, ?> source) {
			// 转换 Map 元素
			Map<Object, Object> target = createMap(this.mapType, source.size());
			source.forEach((key, val) -> target.put(convertKey(key), convertValue(val)));
			super.setValue(target);
		}
		else {
			throw new IllegalArgumentException("Value cannot be converted to Map: " + value);
		}
	}

	/**
	 * 创建指定类型的 Map，并设置初始容量（若目标类型支持）。
	 * @param mapType Map 的子接口
	 * @param initialCapacity 初始容量
	 * @return 新的 Map 实例
	 */
	@SuppressWarnings({"rawtypes", "unchecked"})
	protected Map<Object, Object> createMap(Class<? extends Map> mapType, int initialCapacity) {
		if (!mapType.isInterface()) {
			try {
				return ReflectionUtils.accessibleConstructor(mapType).newInstance();
			}
			catch (Throwable ex) {
				throw new IllegalArgumentException(
						"Could not instantiate map class: " + mapType.getName(), ex);
			}
		}
		else if (SortedMap.class == mapType) {
			return new TreeMap<>();
		}
		else {
			return new LinkedHashMap<>(initialCapacity);
		}
	}

	/**
	 * 是否始终创建新的 Map，即使传入的 Map 类型已匹配。
	 * <p>默认为 {@code false}；可覆盖以强制创建新 Map，例如始终转换元素。
	 * @see #convertKey
	 * @see #convertValue
	 */
	protected boolean alwaysCreateNewMap() {
		return false;
	}

	/**
	 * 转换每个遇到的 Map 键的钩子方法。
	 * <p>默认实现直接返回传入键。
	 * <p>可覆盖以转换特定键，例如从 String 转为 Integer。
	 * <p>仅在实际创建新 Map 时调用！
	 * 若传入 Map 类型已匹配，默认不会调用。
	 * 覆盖 {@link #alwaysCreateNewMap()} 可强制每次都创建新 Map。
	 * @param key 源键
	 * @return 目标 Map 中使用的键
	 * @see #alwaysCreateNewMap
	 */
	protected Object convertKey(Object key) {
		return key;
	}

	/**
	 * 转换每个遇到的 Map 值的钩子方法。
	 * <p>默认实现直接返回传入值。
	 * <p>可覆盖以转换特定值，例如从 String 转为 Integer。
	 * <p>仅在实际创建新 Map 时调用！
	 * 若传入 Map 类型已匹配，默认不会调用。
	 * 覆盖 {@link #alwaysCreateNewMap()} 可强制每次都创建新 Map。
	 * @param value 源值
	 * @return 目标 Map 中使用的值
	 * @see #alwaysCreateNewMap
	 */
	protected Object convertValue(Object value) {
		return value;
	}


	/**
	 * 本实现返回 {@code null}，表示没有合适的文本表示。
	 */
	@Override
	public @Nullable String getAsText() {
		return null;
	}

}
