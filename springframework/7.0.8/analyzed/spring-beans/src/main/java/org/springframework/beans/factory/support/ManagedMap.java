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

package org.springframework.beans.factory.support;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.BeanMetadataElement;
import org.springframework.beans.Mergeable;

/**
 * 标记集合类，用于承载受 Spring 管理的 {@code Map} 值；
 * 值中可能包含运行时 Bean 引用（后续会解析为实际 Bean 对象）。
 *
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @since 27.05.2003
 * @param <K> the key type
 * @param <V> the value type
 */
@SuppressWarnings("serial")
public class ManagedMap<K, V> extends LinkedHashMap<K, V> implements Mergeable, BeanMetadataElement {

	/** 该元数据元素对应的配置来源对象。 */
	private @Nullable Object source;

	/** 映射键的默认类型名（全限定类名）。 */
	private @Nullable String keyTypeName;

	/** 映射值的默认类型名（全限定类名）。 */
	private @Nullable String valueTypeName;

	/** 是否允许与父级集合值合并。 */
	private boolean mergeEnabled;


	/** 构造空映射。 */
	public ManagedMap() {
	}

	/**
	 * 构造具有指定初始容量的映射。
	 * @param initialCapacity 初始容量
	 */
	public ManagedMap(int initialCapacity) {
		super(initialCapacity);
	}


	/**
	 * 根据给定条目创建新实例；条目对象本身不会存入映射。
	 * @param entries 用于填充映射的 {@code Map.Entry}，包含键与值
	 * @param <K> the {@code Map}'s key type
	 * @param <V> the {@code Map}'s value type
	 * @return 包含指定映射关系的 {@code Map}
	 * @since 5.3.16
	 */
	@SafeVarargs
	@SuppressWarnings("unchecked")
	public static <K,V> ManagedMap<K,V> ofEntries(Entry<? extends K, ? extends V>... entries) {
		ManagedMap<K,V > map = new ManagedMap<>();
		for (Entry<? extends K, ? extends V> entry : entries) {
			map.put(entry.getKey(), entry.getValue());
		}
		return map;
	}

	/**
	 * 设置该元数据元素的配置来源 {@code Object}。
	 * <p>对象的具体类型取决于所使用的配置机制。
	 */
	public void setSource(@Nullable Object source) {
		this.source = source;
	}

	@Override
	public @Nullable Object getSource() {
		return this.source;
	}

	/**
	 * 设置本映射使用的默认键类型名（类名）。
	 */
	public void setKeyTypeName(@Nullable String keyTypeName) {
		this.keyTypeName = keyTypeName;
	}

	/**
	 * 返回本映射使用的默认键类型名（类名）。
	 */
	public @Nullable String getKeyTypeName() {
		return this.keyTypeName;
	}

	/**
	 * 设置本映射使用的默认值类型名（类名）。
	 */
	public void setValueTypeName(@Nullable String valueTypeName) {
		this.valueTypeName = valueTypeName;
	}

	/**
	 * 返回本映射使用的默认值类型名（类名）。
	 */
	public @Nullable String getValueTypeName() {
		return this.valueTypeName;
	}

	/**
	 * 设置是否允许合并本集合；
	 * 当存在「父级」集合值时可与父级合并。
	 */
	public void setMergeEnabled(boolean mergeEnabled) {
		this.mergeEnabled = mergeEnabled;
	}

	@Override
	public boolean isMergeEnabled() {
		return this.mergeEnabled;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Object merge(@Nullable Object parent) {
		if (!this.mergeEnabled) {
			throw new IllegalStateException("Not allowed to merge when the 'mergeEnabled' property is set to 'false'");
		}
		if (parent == null) {
			return this;
		}
		if (!(parent instanceof Map)) {
			throw new IllegalArgumentException("Cannot merge with object of type [" + parent.getClass() + "]");
		}
		// 先放入父级条目，再用当前映射覆盖/追加
		Map<K, V> merged = new ManagedMap<>();
		merged.putAll((Map<K, V>) parent);
		merged.putAll(this);
		return merged;
	}

}
