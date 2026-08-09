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

package org.springframework.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Spliterator;
import java.util.stream.Stream;

import org.jspecify.annotations.Nullable;

import org.springframework.util.StringUtils;

/**
 * {@link PropertyValues} 接口的默认实现。
 * 支持对属性值做简单操作，并提供用于深拷贝以及
 * 从 Map 构造的构造器。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @since 13 May 2001
 */
@SuppressWarnings("serial")
public class MutablePropertyValues implements PropertyValues, Serializable {

	/** 空的 PropertyValue 数组常量，供 {@link #getPropertyValues()} 复用 */
	private static final PropertyValue[] EMPTY_PROPERTY_VALUE_ARRAY = new PropertyValue[0];


	/** 内部持有的 PropertyValue 列表 */
	private final List<PropertyValue> propertyValueList;

	/**
	 * 已登记为“已处理”的属性名集合。
	 * 这些属性可能在 PropertyValue(s) 机制之外通过 setter 等方式处理过。
	 */
	private @Nullable Set<String> processedProperties;

	/** 是否仅包含已完成类型转换的值 */
	private volatile boolean converted;


	/**
	 * 创建一个空的 {@code MutablePropertyValues} 对象。
	 * <p>可通过 {@code add} 方法继续添加属性值。
	 * @see #add(String, Object)
	 */
	public MutablePropertyValues() {
		this.propertyValueList = new ArrayList<>(0);
	}

	/**
	 * 深拷贝构造器。保证 {@link PropertyValue} 引用彼此独立，
	 * 但不会对各个 {@link PropertyValue} 当前引用的对象本身做深拷贝。
	 * @param original 要拷贝的 {@link PropertyValues}
	 * @see #addPropertyValues(PropertyValues)
	 */
	public MutablePropertyValues(@Nullable PropertyValues original) {
		// 这里可以优化：全部是新建条目，不存在替换既有属性值的情况。
		if (original != null) {
			PropertyValue[] pvs = original.getPropertyValues();
			this.propertyValueList = new ArrayList<>(pvs.length);
			for (PropertyValue pv : pvs) {
				this.propertyValueList.add(new PropertyValue(pv));
			}
		}
		else {
			this.propertyValueList = new ArrayList<>(0);
		}
	}

	/**
	 * 从 Map 构造新的 {@code MutablePropertyValues} 对象。
	 * @param original 以属性名字符串为键的属性值 Map
	 * @see #addPropertyValues(Map)
	 */
	public MutablePropertyValues(@Nullable Map<?, ?> original) {
		// 这里可以优化：全部是新建条目，不存在替换既有属性值的情况。
		if (original != null) {
			this.propertyValueList = new ArrayList<>(original.size());
			original.forEach((attrName, attrValue) -> this.propertyValueList.add(
					new PropertyValue(attrName.toString(), attrValue)));
		}
		else {
			this.propertyValueList = new ArrayList<>(0);
		}
	}

	/**
	 * 使用给定的 {@link PropertyValue} 列表原样构造新的 {@code MutablePropertyValues}。
	 * <p>这是面向高级场景的构造器，不适合典型的编程式用法。
	 * @param propertyValueList {@link PropertyValue} 对象列表
	 */
	public MutablePropertyValues(@Nullable List<PropertyValue> propertyValueList) {
		this.propertyValueList =
				(propertyValueList != null ? propertyValueList : new ArrayList<>());
	}


	/**
	 * 以原始形式返回底层的 {@link PropertyValue} 列表。
	 * 返回的列表可直接修改，但不推荐这样做。
	 * <p>这是为优化访问全部 {@link PropertyValue} 对象而提供的访问器，
	 * 不适合典型的编程式用法。
	 */
	public List<PropertyValue> getPropertyValueList() {
		return this.propertyValueList;
	}

	/**
	 * 返回列表中 {@link PropertyValue} 条目的数量。
	 */
	public int size() {
		return this.propertyValueList.size();
	}

	/**
	 * 将给定 {@link PropertyValues} 中的全部属性值拷贝到本对象。
	 * 保证 {@link PropertyValue} 引用彼此独立，
	 * 但不会对各个 {@link PropertyValue} 当前引用的对象本身做深拷贝。
	 * @param other 要拷贝的 {@link PropertyValues}
	 * @return 返回 this，以便链式添加多个属性值
	 */
	public MutablePropertyValues addPropertyValues(@Nullable PropertyValues other) {
		if (other != null) {
			PropertyValue[] pvs = other.getPropertyValues();
			for (PropertyValue pv : pvs) {
				addPropertyValue(new PropertyValue(pv));
			}
		}
		return this;
	}

	/**
	 * 将给定 Map 中的全部属性值加入本对象。
	 * @param other 以属性名为键的 Map，键必须是 String
	 * @return 返回 this，以便链式添加多个属性值
	 */
	public MutablePropertyValues addPropertyValues(@Nullable Map<?, ?> other) {
		if (other != null) {
			other.forEach((attrName, attrValue) -> addPropertyValue(
					new PropertyValue(attrName.toString(), attrValue)));
		}
		return this;
	}

	/**
	 * 添加一个 {@link PropertyValue}：若已存在同名属性则替换，
	 * 或在适用时与既有值合并。
	 * @param pv 要添加的 {@link PropertyValue}
	 * @return 返回 this，以便链式添加多个属性值
	 */
	public MutablePropertyValues addPropertyValue(PropertyValue pv) {
		for (int i = 0; i < this.propertyValueList.size(); i++) {
			PropertyValue currentPv = this.propertyValueList.get(i);
			if (currentPv.getName().equals(pv.getName())) {
				pv = mergeIfRequired(pv, currentPv);
				setPropertyValueAt(pv, i);
				return this;
			}
		}
		this.propertyValueList.add(pv);
		return this;
	}

	/**
	 * {@code addPropertyValue} 的重载形式，直接接受属性名与属性值。
	 * <p>注意：更推荐使用更简洁且支持链式调用的
	 * {@link #add(String, Object)}。
	 * @param propertyName 属性名
	 * @param propertyValue 属性值
	 * @see #addPropertyValue(PropertyValue)
	 */
	public void addPropertyValue(String propertyName, Object propertyValue) {
		addPropertyValue(new PropertyValue(propertyName, propertyValue));
	}

	/**
	 * 添加一个属性值：若已存在同名属性则替换，
	 * 或在适用时与既有值合并。
	 * @param propertyName 属性名
	 * @param propertyValue 属性值
	 * @return 返回 this，以便链式添加多个属性值
	 */
	public MutablePropertyValues add(String propertyName, @Nullable Object propertyValue) {
		addPropertyValue(new PropertyValue(propertyName, propertyValue));
		return this;
	}

	/**
	 * 修改本对象持有的某个 {@link PropertyValue}。
	 * 索引从 0 开始。
	 * @param pv 新的 {@link PropertyValue}
	 * @param i 列表下标
	 */
	public void setPropertyValueAt(PropertyValue pv, int i) {
		this.propertyValueList.set(i, pv);
	}

	/**
	 * 若新值支持并启用了合并，则将所提供的“新”{@link PropertyValue}
	 * 与当前 {@link PropertyValue} 的值进行合并。
	 * @see Mergeable
	 */
	private PropertyValue mergeIfRequired(PropertyValue newPv, PropertyValue currentPv) {
		Object value = newPv.getValue();
		if (value instanceof Mergeable mergeable) {
			if (mergeable.isMergeEnabled()) {
				Object merged = mergeable.merge(currentPv.getValue());
				return new PropertyValue(newPv.getName(), merged);
			}
		}
		return newPv;
	}

	/**
	 * 若包含给定 {@link PropertyValue}，则将其移除。
	 * @param pv 要移除的 {@link PropertyValue}
	 */
	public void removePropertyValue(PropertyValue pv) {
		this.propertyValueList.remove(pv);
	}

	/**
	 * {@code removePropertyValue} 的重载形式，按属性名移除。
	 * @param propertyName 属性名
	 * @see #removePropertyValue(PropertyValue)
	 */
	public void removePropertyValue(String propertyName) {
		this.propertyValueList.remove(getPropertyValue(propertyName));
	}


	@Override
	public Iterator<PropertyValue> iterator() {
		return Collections.unmodifiableList(this.propertyValueList).iterator();
	}

	@Override
	public Spliterator<PropertyValue> spliterator() {
		return this.propertyValueList.spliterator();
	}

	@Override
	public Stream<PropertyValue> stream() {
		return this.propertyValueList.stream();
	}

	@Override
	public PropertyValue[] getPropertyValues() {
		return this.propertyValueList.toArray(EMPTY_PROPERTY_VALUE_ARRAY);
	}

	@Override
	public @Nullable PropertyValue getPropertyValue(String propertyName) {
		for (PropertyValue pv : this.propertyValueList) {
			if (pv.getName().equals(propertyName)) {
				return pv;
			}
		}
		return null;
	}

	/**
	 * 获取原始属性值（若存在）。
	 * @param propertyName 要查找的属性名
	 * @return 原始属性值；未找到时为 {@code null}
	 * @since 4.0
	 * @see #getPropertyValue(String)
	 * @see PropertyValue#getValue()
	 */
	public @Nullable Object get(String propertyName) {
		PropertyValue pv = getPropertyValue(propertyName);
		return (pv != null ? pv.getValue() : null);
	}

	@Override
	public PropertyValues changesSince(PropertyValues old) {
		MutablePropertyValues changes = new MutablePropertyValues();
		if (old == this) {
			return changes;
		}

		// 遍历新集合中的每个属性值
		for (PropertyValue newPv : this.propertyValueList) {
			// 若旧集合中没有同名项，或值不相等，则记为变更
			PropertyValue pvOld = old.getPropertyValue(newPv.getName());
			if (pvOld == null || !pvOld.equals(newPv)) {
				changes.addPropertyValue(newPv);
			}
		}
		return changes;
	}

	@Override
	public boolean contains(String propertyName) {
		return (getPropertyValue(propertyName) != null ||
				(this.processedProperties != null && this.processedProperties.contains(propertyName)));
	}

	@Override
	public boolean isEmpty() {
		return this.propertyValueList.isEmpty();
	}


	/**
	 * 将指定属性登记为“已处理”，含义是某个处理器在
	 * PropertyValue(s) 机制之外调用了对应的 setter。
	 * <p>登记后，对该属性调用 {@link #contains} 将返回 {@code true}。
	 * @param propertyName 属性名
	 */
	public void registerProcessedProperty(String propertyName) {
		if (this.processedProperties == null) {
			this.processedProperties = new HashSet<>(4);
		}
		this.processedProperties.add(propertyName);
	}

	/**
	 * 清除给定属性的“已处理”登记（若有）。
	 * @param propertyName 属性名
	 * @since 3.2.13
	 */
	public void clearProcessedProperty(String propertyName) {
		if (this.processedProperties != null) {
			this.processedProperties.remove(propertyName);
		}
	}

	/**
	 * 将本持有者标记为仅包含已转换的值
	 * （即不再需要运行时解析）。
	 */
	public void setConverted() {
		this.converted = true;
	}

	/**
	 * 返回本持有者是否仅包含已转换的值（{@code true}），
	 * 抑或值仍需转换（{@code false}）。
	 */
	public boolean isConverted() {
		return this.converted;
	}


	@Override
	public boolean equals(@Nullable Object other) {
		return (this == other || (other instanceof MutablePropertyValues that &&
				this.propertyValueList.equals(that.propertyValueList)));
	}

	@Override
	public int hashCode() {
		return this.propertyValueList.hashCode();
	}

	@Override
	public String toString() {
		PropertyValue[] pvs = getPropertyValues();
		if (pvs.length > 0) {
			return "PropertyValues: length=" + pvs.length + "; " + StringUtils.arrayToDelimitedString(pvs, "; ");
		}
		return "PropertyValues: length=0";
	}

}
