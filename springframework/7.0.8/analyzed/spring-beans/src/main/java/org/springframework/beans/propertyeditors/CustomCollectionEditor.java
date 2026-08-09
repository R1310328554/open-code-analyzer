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
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.jspecify.annotations.Nullable;

import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

/**
 * Collection 属性编辑器，将任意源 Collection 转换为目标 Collection 类型。
 *
 * <p>默认注册于 Set、SortedSet 和 List，
 * 当类型与目标属性不匹配时自动将给定 Collection 转换为这些目标类型之一。
 *
 * @author Juergen Hoeller
 * @since 1.1.3
 * @see java.util.Collection
 * @see java.util.Set
 * @see java.util.SortedSet
 * @see java.util.List
 */
public class CustomCollectionEditor extends PropertyEditorSupport {

	/** 目标 Collection 类型。 */
	@SuppressWarnings("rawtypes")
	private final Class<? extends Collection> collectionType;

	/** 是否将传入的 {@code null} 转换为空 Collection。 */
	private final boolean nullAsEmptyCollection;


	/**
	 * 为给定目标类型创建新的 CustomCollectionEditor，传入的 {@code null} 保持原样。
	 * @param collectionType 目标类型，须为 Collection 的子接口或具体实现类
	 * @see java.util.Collection
	 * @see java.util.ArrayList
	 * @see java.util.TreeSet
	 * @see java.util.LinkedHashSet
	 */
	@SuppressWarnings("rawtypes")
	public CustomCollectionEditor(Class<? extends Collection> collectionType) {
		this(collectionType, false);
	}

	/**
	 * 为给定目标类型创建新的 CustomCollectionEditor。
	 * <p>若传入值已是目标类型，则直接使用。
	 * 若为其他 Collection 类型或数组，则转换为目标 Collection 类型的默认实现。
	 * 若为其他类型，则创建仅含该单个元素的目标 Collection。
	 * <p>默认实现为：List 对应 ArrayList，SortedSet 对应 TreeSet，Set 对应 LinkedHashSet。
	 * @param collectionType 目标类型，须为 Collection 的子接口或具体实现类
	 * @param nullAsEmptyCollection 是否将传入的 {@code null} 转换为空 Collection（对应类型）
	 * @see java.util.Collection
	 * @see java.util.ArrayList
	 * @see java.util.TreeSet
	 * @see java.util.LinkedHashSet
	 */
	@SuppressWarnings("rawtypes")
	public CustomCollectionEditor(Class<? extends Collection> collectionType, boolean nullAsEmptyCollection) {
		Assert.notNull(collectionType, "Collection type is required");
		if (!Collection.class.isAssignableFrom(collectionType)) {
			throw new IllegalArgumentException(
					"Collection type [" + collectionType.getName() + "] does not implement [java.util.Collection]");
		}
		this.collectionType = collectionType;
		this.nullAsEmptyCollection = nullAsEmptyCollection;
	}


	/**
	 * 将给定文本值转换为仅含单个元素的 Collection。
	 */
	@Override
	public void setAsText(String text) throws IllegalArgumentException {
		setValue(text);
	}

	/**
	 * 将给定值转换为目标类型的 Collection。
	 */
	@Override
	public void setValue(@Nullable Object value) {
		if (value == null && this.nullAsEmptyCollection) {
			super.setValue(createCollection(this.collectionType, 0));
		}
		else if (value == null || (this.collectionType.isInstance(value) && !alwaysCreateNewCollection())) {
			// 源值类型已匹配目标类型，直接使用
			super.setValue(value);
		}
		else if (value instanceof Collection<?> source) {
			// 转换 Collection 元素
			Collection<Object> target = createCollection(this.collectionType, source.size());
			for (Object elem : source) {
				target.add(convertElement(elem));
			}
			super.setValue(target);
		}
		else if (value.getClass().isArray()) {
			// 将数组元素转换为 Collection 元素
			int length = Array.getLength(value);
			Collection<Object> target = createCollection(this.collectionType, length);
			for (int i = 0; i < length; i++) {
				target.add(convertElement(Array.get(value, i)));
			}
			super.setValue(target);
		}
		else {
			// 普通单值：包装为仅含一个元素的 Collection
			Collection<Object> target = createCollection(this.collectionType, 1);
			target.add(convertElement(value));
			super.setValue(target);
		}
	}

	/**
	 * 创建指定类型的 Collection，并设置初始容量（若目标类型支持）。
	 * @param collectionType Collection 的子接口
	 * @param initialCapacity 初始容量
	 * @return 新的 Collection 实例
	 */
	@SuppressWarnings({"rawtypes", "unchecked"})
	protected Collection<Object> createCollection(Class<? extends Collection> collectionType, int initialCapacity) {
		if (!collectionType.isInterface()) {
			try {
				return ReflectionUtils.accessibleConstructor(collectionType).newInstance();
			}
			catch (Throwable ex) {
				throw new IllegalArgumentException(
						"Could not instantiate collection class: " + collectionType.getName(), ex);
			}
		}
		else if (List.class == collectionType) {
			return new ArrayList<>(initialCapacity);
		}
		else if (SortedSet.class == collectionType) {
			return new TreeSet<>();
		}
		else {
			return new LinkedHashSet<>(initialCapacity);
		}
	}

	/**
	 * 是否始终创建新的 Collection，即使传入的 Collection 类型已匹配。
	 * <p>默认为 {@code false}；可覆盖以强制创建新 Collection，例如始终转换元素。
	 * @see #convertElement
	 */
	protected boolean alwaysCreateNewCollection() {
		return false;
	}

	/**
	 * 转换每个遇到的 Collection/数组元素的钩子方法。
	 * <p>默认实现直接返回传入元素。
	 * <p>可覆盖以转换特定元素，例如将字符串数组转换为 Integer 的 Set。
	 * <p>仅在实际创建新 Collection 时调用！
	 * 若传入 Collection 类型已匹配，默认不会调用。
	 * 覆盖 {@link #alwaysCreateNewCollection()} 可强制每次都创建新 Collection。
	 * @param element 源元素
	 * @return 目标 Collection 中使用的元素
	 * @see #alwaysCreateNewCollection()
	 */
	protected Object convertElement(Object element) {
		return element;
	}


	/**
	 * 本实现返回 {@code null}，表示没有合适的文本表示。
	 */
	@Override
	public @Nullable String getAsText() {
		return null;
	}

}
