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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.BeanMetadataElement;
import org.springframework.beans.Mergeable;

/**
 * 标记集合类，用于承载受 Spring 管理的 {@code List} 元素；
 * 元素中可能包含运行时 Bean 引用（后续会解析为实际 Bean 对象）。
 *
 * @author Rod Johnson
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @author Stephane Nicoll
 * @author Sam Brannen
 * @since 27.05.2003
 * @param <E> the element type
 */
@SuppressWarnings("serial")
public class ManagedList<E> extends ArrayList<E> implements Mergeable, BeanMetadataElement {

	/** 该元数据元素对应的配置来源对象。 */
	private @Nullable Object source;

	/** 列表元素的默认类型名（全限定类名）。 */
	private @Nullable String elementTypeName;

	/** 是否允许与父级集合值合并。 */
	private boolean mergeEnabled;


	/** 构造空列表。 */
	public ManagedList() {
	}

	/**
	 * 构造具有指定初始容量的列表。
	 * @param initialCapacity 初始容量
	 */
	public ManagedList(int initialCapacity) {
		super(initialCapacity);
	}


	/**
	 * 创建包含任意数量元素的新实例。
	 * @param elements 要放入列表的元素
	 * @param <E> the {@code List}'s element type
	 * @return 包含指定元素的 {@code ManagedList}
	 * @since 5.3.16
	 */
	@SafeVarargs
	@SuppressWarnings("varargs")
	public static <E> ManagedList<E> of(E... elements) {
		ManagedList<E> list = new ManagedList<>();
		Collections.addAll(list, elements);
		return list;
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
	 * 设置本列表使用的默认元素类型名（类名）。
	 */
	public void setElementTypeName(String elementTypeName) {
		this.elementTypeName = elementTypeName;
	}

	/**
	 * 返回本列表使用的默认元素类型名（类名）。
	 */
	public @Nullable String getElementTypeName() {
		return this.elementTypeName;
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
	public List<E> merge(@Nullable Object parent) {
		if (!this.mergeEnabled) {
			throw new IllegalStateException("Not allowed to merge when the 'mergeEnabled' property is set to 'false'");
		}
		if (parent == null) {
			return this;
		}
		if (!(parent instanceof List)) {
			throw new IllegalArgumentException("Cannot merge with object of type [" + parent.getClass() + "]");
		}
		// 先放入父级元素，再追加当前列表元素
		List<E> merged = new ManagedList<>();
		merged.addAll((List<E>) parent);
		merged.addAll(this);
		return merged;
	}

}
