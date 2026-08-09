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

package org.springframework.beans.factory.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.BeanMetadataElement;
import org.springframework.beans.Mergeable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;

/**
 * 构造器参数值的持有者，通常作为 Bean 定义的一部分。
 *
 * <p>既支持按构造器参数列表中的特定索引存放值，
 * 也支持按类型进行通用参数匹配。
 *
 * @author Juergen Hoeller
 * @since 09.11.2003
 * @see BeanDefinition#getConstructorArgumentValues
 */
public class ConstructorArgumentValues {

	/** 按索引存放的构造器参数值，键为参数索引 */
	private final Map<Integer, ValueHolder> indexedArgumentValues = new LinkedHashMap<>();

	/** 按类型匹配的通用构造器参数值列表 */
	private final List<ValueHolder> genericArgumentValues = new ArrayList<>();


	/**
	 * 创建新的空 ConstructorArgumentValues 对象。
	 */
	public ConstructorArgumentValues() {
	}

	/**
	 * 深拷贝构造器。
	 * @param original 要复制的 ConstructorArgumentValues
	 */
	public ConstructorArgumentValues(ConstructorArgumentValues original) {
		addArgumentValues(original);
	}


	/**
	 * 将所有给定参数值复制到本对象，使用独立的持有者实例以保持与原始对象无关。
	 * <p>注意：相同的 ValueHolder 实例只会注册一次，
	 * 以便合并和重新合并参数值定义。内容相同的不同 ValueHolder 实例当然允许存在。
	 */
	public void addArgumentValues(@Nullable ConstructorArgumentValues other) {
		if (other != null) {
			other.indexedArgumentValues.forEach(
				(index, argValue) -> addOrMergeIndexedArgumentValue(index, argValue.copy())
			);
			other.genericArgumentValues.stream()
					.filter(valueHolder -> !this.genericArgumentValues.contains(valueHolder))
					.forEach(valueHolder -> addOrMergeGenericArgumentValue(valueHolder.copy()));
		}
	}


	/**
	 * 为构造器参数列表中的给定索引添加参数值。
	 * @param index 构造器参数列表中的索引
	 * @param value 参数值
	 */
	public void addIndexedArgumentValue(int index, @Nullable Object value) {
		addIndexedArgumentValue(index, new ValueHolder(value));
	}

	/**
	 * 为构造器参数列表中的给定索引添加参数值。
	 * @param index 构造器参数列表中的索引
	 * @param value 参数值
	 * @param type 构造器参数的类型
	 */
	public void addIndexedArgumentValue(int index, @Nullable Object value, String type) {
		addIndexedArgumentValue(index, new ValueHolder(value, type));
	}

	/**
	 * 为构造器参数列表中的给定索引添加参数值。
	 * @param index 构造器参数列表中的索引
	 * @param newValue 以 ValueHolder 形式表示的参数值
	 */
	public void addIndexedArgumentValue(int index, ValueHolder newValue) {
		Assert.isTrue(index >= 0, "Index must not be negative");
		Assert.notNull(newValue, "ValueHolder must not be null");
		addOrMergeIndexedArgumentValue(index, newValue);
	}

	/**
	 * 为构造器参数列表中的给定索引添加参数值，
	 * 若需要则将新值（通常为集合）与当前值合并：参见 {@link org.springframework.beans.Mergeable}。
	 * @param key 构造器参数列表中的索引
	 * @param newValue 以 ValueHolder 形式表示的参数值
	 */
	private void addOrMergeIndexedArgumentValue(Integer key, ValueHolder newValue) {
		ValueHolder currentValue = this.indexedArgumentValues.get(key);
		if (currentValue != null && newValue.getValue() instanceof Mergeable mergeable) {
			if (mergeable.isMergeEnabled()) {
				newValue.setValue(mergeable.merge(currentValue.getValue()));
			}
		}
		this.indexedArgumentValues.put(key, newValue);
	}

	/**
	 * 检查是否已为给定索引注册参数值。
	 * @param index 构造器参数列表中的索引
	 */
	public boolean hasIndexedArgumentValue(int index) {
		return this.indexedArgumentValues.containsKey(index);
	}

	/**
	 * 获取构造器参数列表中给定索引的参数值。
	 * @param index 构造器参数列表中的索引
	 * @param requiredType 要匹配的类型（可为 {@code null}，仅匹配无类型值）
	 * @return 该参数的 ValueHolder，未设置则为 {@code null}
	 */
	public @Nullable ValueHolder getIndexedArgumentValue(int index, @Nullable Class<?> requiredType) {
		return getIndexedArgumentValue(index, requiredType, null);
	}

	/**
	 * 获取构造器参数列表中给定索引的参数值。
	 * @param index 构造器参数列表中的索引
	 * @param requiredType 要匹配的类型（可为 {@code null}，仅匹配无类型值）
	 * @param requiredName 要匹配的名称（可为 {@code null}，仅匹配未命名值；或空字符串匹配任意名称）
	 * @return 该参数的 ValueHolder，未设置则为 {@code null}
	 */
	public @Nullable ValueHolder getIndexedArgumentValue(int index, @Nullable Class<?> requiredType, @Nullable String requiredName) {
		Assert.isTrue(index >= 0, "Index must not be negative");
		ValueHolder valueHolder = this.indexedArgumentValues.get(index);
		if (valueHolder != null &&
				(valueHolder.getType() == null || (requiredType != null &&
						ClassUtils.matchesTypeName(requiredType, valueHolder.getType()))) &&
				(valueHolder.getName() == null || (requiredName != null &&
						(requiredName.isEmpty() || requiredName.equals(valueHolder.getName()))))) {
			return valueHolder;
		}
		return null;
	}

	/**
	 * 返回按索引存放的参数值映射。
	 * @return 以 Integer 索引为键、ValueHolder 为值的不可修改 Map
	 * @see ValueHolder
	 */
	public Map<Integer, ValueHolder> getIndexedArgumentValues() {
		return Collections.unmodifiableMap(this.indexedArgumentValues);
	}


	/**
	 * 添加按类型匹配的通用参数值。
	 * <p>注意：单个通用参数值只会使用一次，不会多次匹配。
	 * @param value 参数值
	 */
	public void addGenericArgumentValue(@Nullable Object value) {
		this.genericArgumentValues.add(new ValueHolder(value));
	}

	/**
	 * 添加按类型匹配的通用参数值。
	 * <p>注意：单个通用参数值只会使用一次，不会多次匹配。
	 * @param value 参数值
	 * @param type 构造器参数的类型
	 */
	public void addGenericArgumentValue(Object value, String type) {
		this.genericArgumentValues.add(new ValueHolder(value, type));
	}

	/**
	 * 添加按类型或名称（若可用）匹配的通用参数值。
	 * <p>注意：单个通用参数值只会使用一次，不会多次匹配。
	 * @param newValue 以 ValueHolder 形式表示的参数值
	 * <p>注意：相同的 ValueHolder 实例只会注册一次，
	 * 以便合并和重新合并参数值定义。内容相同的不同 ValueHolder 实例当然允许存在。
	 */
	public void addGenericArgumentValue(ValueHolder newValue) {
		Assert.notNull(newValue, "ValueHolder must not be null");
		if (!this.genericArgumentValues.contains(newValue)) {
			addOrMergeGenericArgumentValue(newValue);
		}
	}

	/**
	 * 添加通用参数值，若需要则将新值（通常为集合）与当前值合并：
	 * 参见 {@link org.springframework.beans.Mergeable}。
	 * @param newValue 以 ValueHolder 形式表示的参数值
	 */
	private void addOrMergeGenericArgumentValue(ValueHolder newValue) {
		if (newValue.getName() != null) {
			for (Iterator<ValueHolder> it = this.genericArgumentValues.iterator(); it.hasNext();) {
				ValueHolder currentValue = it.next();
				if (newValue.getName().equals(currentValue.getName())) {
					if (newValue.getValue() instanceof Mergeable mergeable) {
						if (mergeable.isMergeEnabled()) {
							newValue.setValue(mergeable.merge(currentValue.getValue()));
						}
					}
					it.remove();
				}
			}
		}
		this.genericArgumentValues.add(newValue);
	}

	/**
	 * 查找与给定类型匹配的通用参数值。
	 * @param requiredType 要匹配的类型
	 * @return 该参数的 ValueHolder，未设置则为 {@code null}
	 */
	public @Nullable ValueHolder getGenericArgumentValue(Class<?> requiredType) {
		return getGenericArgumentValue(requiredType, null, null);
	}

	/**
	 * 查找与给定类型匹配的通用参数值。
	 * @param requiredType 要匹配的类型
	 * @param requiredName 要匹配的名称
	 * @return 该参数的 ValueHolder，未设置则为 {@code null}
	 */
	public @Nullable ValueHolder getGenericArgumentValue(Class<?> requiredType, String requiredName) {
		return getGenericArgumentValue(requiredType, requiredName, null);
	}

	/**
	 * 查找与给定类型匹配的下一个通用参数值，
	 * 忽略当前解析过程中已使用的参数值。
	 * @param requiredType 要匹配的类型（可为 {@code null}，查找任意下一个通用参数值）
	 * @param requiredName 要匹配的名称（可为 {@code null}，不按名称匹配；或空字符串匹配任意名称）
	 * @param usedValueHolders 当前解析过程中已使用、因此不应再次返回的 ValueHolder 集合
	 * @return 该参数的 ValueHolder，未找到则为 {@code null}
	 */
	public @Nullable ValueHolder getGenericArgumentValue(@Nullable Class<?> requiredType, @Nullable String requiredName,
			@Nullable Set<ValueHolder> usedValueHolders) {

		for (ValueHolder valueHolder : this.genericArgumentValues) {
			if (usedValueHolders != null && usedValueHolders.contains(valueHolder)) {
				continue;
			}
			if (valueHolder.getName() != null && (requiredName == null ||
					(!requiredName.isEmpty() && !requiredName.equals(valueHolder.getName())))) {
				continue;
			}
			if (valueHolder.getType() != null && (requiredType == null ||
					!ClassUtils.matchesTypeName(requiredType, valueHolder.getType()))) {
				continue;
			}
			if (requiredType != null && valueHolder.getType() == null && valueHolder.getName() == null &&
					!ClassUtils.isAssignableValue(requiredType, valueHolder.getValue())) {
				continue;
			}
			return valueHolder;
		}
		return null;
	}

	/**
	 * 返回通用参数值列表。
	 * @return 不可修改的 ValueHolder 列表
	 * @see ValueHolder
	 */
	public List<ValueHolder> getGenericArgumentValues() {
		return Collections.unmodifiableList(this.genericArgumentValues);
	}


	/**
	 * 查找对应于构造器参数列表中给定索引或按类型通用匹配的参数值。
	 * @param index 构造器参数列表中的索引
	 * @param requiredType 要匹配的参数类型
	 * @return 该参数的 ValueHolder，未设置则为 {@code null}
	 */
	public @Nullable ValueHolder getArgumentValue(int index, Class<?> requiredType) {
		return getArgumentValue(index, requiredType, null, null);
	}

	/**
	 * 查找对应于构造器参数列表中给定索引或按类型通用匹配的参数值。
	 * @param index 构造器参数列表中的索引
	 * @param requiredType 要匹配的参数类型
	 * @param requiredName 要匹配的参数名称
	 * @return 该参数的 ValueHolder，未设置则为 {@code null}
	 */
	public @Nullable ValueHolder getArgumentValue(int index, Class<?> requiredType, String requiredName) {
		return getArgumentValue(index, requiredType, requiredName, null);
	}

	/**
	 * 查找对应于构造器参数列表中给定索引或按类型通用匹配的参数值。
	 * @param index 构造器参数列表中的索引
	 * @param requiredType 要匹配的参数类型（可为 {@code null}，查找无类型参数值）
	 * @param requiredName 要匹配的参数名称（可为 {@code null}，查找未命名参数值；或空字符串匹配任意名称）
	 * @param usedValueHolders 当前解析过程中已使用、因此不应再次返回的 ValueHolder 集合
	 * （允许多个同类型通用参数值时返回下一个匹配）
	 * @return 该参数的 ValueHolder，未设置则为 {@code null}
	 */
	public @Nullable ValueHolder getArgumentValue(int index, @Nullable Class<?> requiredType,
			@Nullable String requiredName, @Nullable Set<ValueHolder> usedValueHolders) {

		Assert.isTrue(index >= 0, "Index must not be negative");
		ValueHolder valueHolder = getIndexedArgumentValue(index, requiredType, requiredName);
		if (valueHolder == null) {
			valueHolder = getGenericArgumentValue(requiredType, requiredName, usedValueHolders);
		}
		return valueHolder;
	}

	/**
	 * 判断是否至少有一个参数值引用了名称。
	 * @since 6.0.3
	 * @see ValueHolder#getName()
	 */
	public boolean containsNamedArgument() {
		for (ValueHolder valueHolder : this.indexedArgumentValues.values()) {
			if (valueHolder.getName() != null) {
				return true;
			}
		}
		for (ValueHolder valueHolder : this.genericArgumentValues) {
			if (valueHolder.getName() != null) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 返回本实例持有的参数值数量，包括索引参数值和通用参数值。
	 */
	public int getArgumentCount() {
		return (this.indexedArgumentValues.size() + this.genericArgumentValues.size());
	}

	/**
	 * 判断本持有者是否不包含任何参数值，包括索引参数值和通用参数值。
	 */
	public boolean isEmpty() {
		return (this.indexedArgumentValues.isEmpty() && this.genericArgumentValues.isEmpty());
	}

	/**
	 * 清空本持有者，移除所有参数值。
	 */
	public void clear() {
		this.indexedArgumentValues.clear();
		this.genericArgumentValues.clear();
	}


	@Override
	public boolean equals(@Nullable Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof ConstructorArgumentValues that)) {
			return false;
		}
		if (this.genericArgumentValues.size() != that.genericArgumentValues.size() ||
				this.indexedArgumentValues.size() != that.indexedArgumentValues.size()) {
			return false;
		}
		Iterator<ValueHolder> it1 = this.genericArgumentValues.iterator();
		Iterator<ValueHolder> it2 = that.genericArgumentValues.iterator();
		while (it1.hasNext() && it2.hasNext()) {
			ValueHolder vh1 = it1.next();
			ValueHolder vh2 = it2.next();
			if (!vh1.contentEquals(vh2)) {
				return false;
			}
		}
		for (Map.Entry<Integer, ValueHolder> entry : this.indexedArgumentValues.entrySet()) {
			ValueHolder vh1 = entry.getValue();
			ValueHolder vh2 = that.indexedArgumentValues.get(entry.getKey());
			if (vh2 == null || !vh1.contentEquals(vh2)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public int hashCode() {
		int hashCode = 7;
		for (ValueHolder valueHolder : this.genericArgumentValues) {
			hashCode = 31 * hashCode + valueHolder.contentHashCode();
		}
		hashCode = 29 * hashCode;
		for (Map.Entry<Integer, ValueHolder> entry : this.indexedArgumentValues.entrySet()) {
			hashCode = 31 * hashCode + (entry.getValue().contentHashCode() ^ entry.getKey().hashCode());
		}
		return hashCode;
	}


	/**
	 * 构造器参数值的持有者，带有可选的类型属性，指示实际构造器参数的目标类型。
	 */
	public static class ValueHolder implements BeanMetadataElement {

		/** 参数值 */
		private @Nullable Object value;

		/** 构造器参数的类型 */
		private @Nullable String type;

		/** 构造器参数的名称 */
		private @Nullable String name;

		/** 配置源对象 */
		private @Nullable Object source;

		/** 是否已完成类型转换 */
		private boolean converted = false;

		/** 类型转换后的值 */
		private @Nullable Object convertedValue;

		/**
		 * 为给定值创建新的 ValueHolder。
		 * @param value 参数值
		 */
		public ValueHolder(@Nullable Object value) {
			this.value = value;
		}

		/**
		 * 为给定值和类型创建新的 ValueHolder。
		 * @param value 参数值
		 * @param type 构造器参数的类型
		 */
		public ValueHolder(@Nullable Object value, @Nullable String type) {
			this.value = value;
			this.type = type;
		}

		/**
		 * 为给定值、类型和名称创建新的 ValueHolder。
		 * @param value 参数值
		 * @param type 构造器参数的类型
		 * @param name 构造器参数的名称
		 */
		public ValueHolder(@Nullable Object value, @Nullable String type, @Nullable String name) {
			this.value = value;
			this.type = type;
			this.name = name;
		}

		/**
		 * 设置构造器参数的值。
		 */
		public void setValue(@Nullable Object value) {
			this.value = value;
		}

		/**
		 * 返回构造器参数的值。
		 */
		public @Nullable Object getValue() {
			return this.value;
		}

		/**
		 * 设置构造器参数的类型。
		 */
		public void setType(@Nullable String type) {
			this.type = type;
		}

		/**
		 * 返回构造器参数的类型。
		 */
		public @Nullable String getType() {
			return this.type;
		}

		/**
		 * 设置构造器参数的名称。
		 */
		public void setName(@Nullable String name) {
			this.name = name;
		}

		/**
		 * 返回构造器参数的名称。
		 */
		public @Nullable String getName() {
			return this.name;
		}

		/**
		 * 设置本元数据元素的配置源 {@code Object}。
		 * <p>对象的确切类型取决于所使用的配置机制。
		 */
		public void setSource(@Nullable Object source) {
			this.source = source;
		}

		@Override
		public @Nullable Object getSource() {
			return this.source;
		}

		/**
		 * 返回本持有者是否已包含转换后的值（{@code true}），
		 * 还是值仍待转换（{@code false}）。
		 */
		public synchronized boolean isConverted() {
			return this.converted;
		}

		/**
		 * 设置构造器参数经类型转换处理后的值。
		 */
		public synchronized void setConvertedValue(@Nullable Object value) {
			this.converted = (value != null);
			this.convertedValue = value;
		}

		/**
		 * 返回构造器参数经类型转换处理后的值。
		 */
		public synchronized @Nullable Object getConvertedValue() {
			return this.convertedValue;
		}

		/**
		 * 判断本 ValueHolder 的内容是否与给定其他 ValueHolder 的内容相等。
		 * <p>注意：ValueHolder 不直接实现 {@code equals}，
		 * 以允许内容相同的多个 ValueHolder 实例共存于同一 Set 中。
		 */
		private boolean contentEquals(ValueHolder other) {
			return (this == other ||
					(ObjectUtils.nullSafeEquals(this.value, other.value) && ObjectUtils.nullSafeEquals(this.type, other.type)));
		}

		/**
		 * 计算本 ValueHolder 内容的哈希码。
		 * <p>注意：ValueHolder 不直接实现 {@code hashCode}，
		 * 以允许内容相同的多个 ValueHolder 实例共存于同一 Set 中。
		 */
		private int contentHashCode() {
			return ObjectUtils.nullSafeHash(this.value, this.type);
		}

		/**
		 * 创建本 ValueHolder 的副本：即内容相同但相互独立的 ValueHolder 实例。
		 */
		public ValueHolder copy() {
			ValueHolder copy = new ValueHolder(this.value, this.type, this.name);
			copy.setSource(this.source);
			return copy;
		}
	}

}
