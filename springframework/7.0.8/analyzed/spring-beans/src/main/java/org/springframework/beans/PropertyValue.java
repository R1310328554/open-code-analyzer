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

import org.jspecify.annotations.Nullable;

import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

/**
 * 保存单个 bean 属性的信息与值的对象。
 * 使用对象而非仅用属性名做键的 Map 来存放全部属性，可获得更大灵活性，
 * 并能以更优化的方式处理索引属性等场景。
 *
 * <p>注意：此处的值不必已是最终所需类型：
 * {@link BeanWrapper} 实现应负责必要的类型转换，
 * 因为本对象并不了解将被应用到的目标对象。
 *
 * @author Rod Johnson
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 13 May 2001
 * @see PropertyValues
 * @see BeanWrapper
 */
@SuppressWarnings("serial")
public class PropertyValue extends BeanMetadataAttributeAccessor implements Serializable {

	/** 属性名。 */
	private final String name;

	/** 属性值（可能尚未完成类型转换）。 */
	private final @Nullable Object value;

	/** 是否为可选属性（目标类无对应属性时可忽略）。 */
	private boolean optional = false;

	/** 是否已完成类型转换。 */
	private boolean converted = false;

	/** 类型转换后的值。 */
	private @Nullable Object convertedValue;

	/** 包可见字段：指示是否需要进行转换。 */
	volatile @Nullable Boolean conversionNecessary;

	/** 包可见字段：缓存已解析的属性路径 token。 */
	transient volatile @Nullable Object resolvedTokens;


	/**
	 * 创建新的 PropertyValue 实例。
	 * @param name 属性名（永不为 {@code null}）
	 * @param value 属性值（可能尚未完成类型转换）
	 */
	public PropertyValue(String name, @Nullable Object value) {
		Assert.notNull(name, "Name must not be null");
		this.name = name;
		this.value = value;
	}

	/**
	 * 拷贝构造函数。
	 * @param original 要复制的 PropertyValue（永不为 {@code null}）
	 */
	public PropertyValue(PropertyValue original) {
		Assert.notNull(original, "Original must not be null");
		this.name = original.getName();
		this.value = original.getValue();
		this.optional = original.isOptional();
		this.converted = original.converted;
		this.convertedValue = original.convertedValue;
		this.conversionNecessary = original.conversionNecessary;
		this.resolvedTokens = original.resolvedTokens;
		setSource(original.getSource());
		copyAttributesFrom(original);
	}

	/**
	 * 为原有值持有者暴露新值的构造函数。
	 * 原持有者将作为新持有者的 source。
	 * @param original 要链接的 PropertyValue（永不为 {@code null}）
	 * @param newValue 要应用的新值
	 */
	public PropertyValue(PropertyValue original, @Nullable Object newValue) {
		Assert.notNull(original, "Original must not be null");
		this.name = original.getName();
		this.value = newValue;
		this.optional = original.isOptional();
		this.conversionNecessary = original.conversionNecessary;
		this.resolvedTokens = original.resolvedTokens;
		setSource(original);
		copyAttributesFrom(original);
	}


	/**
	 * 返回属性名。
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * 返回属性值。
	 * <p>注意：此处<i>尚未</i>发生类型转换。
	 * 类型转换由 BeanWrapper 实现负责。
	 */
	public @Nullable Object getValue() {
		return this.value;
	}

	/**
	 * 返回本值持有者所对应的原始 PropertyValue 实例。
	 * @return 原始 PropertyValue（本值持有者的 source，或本持有者自身）
	 */
	public PropertyValue getOriginalPropertyValue() {
		PropertyValue original = this;
		Object source = getSource();
		while (source instanceof PropertyValue pv && source != original) {
			original = pv;
			source = original.getSource();
		}
		return original;
	}

	/**
	 * 设置是否为可选值：目标类上不存在对应属性时可忽略。
	 * @since 3.0
	 */
	public void setOptional(boolean optional) {
		this.optional = optional;
	}

	/**
	 * 返回是否为可选值：目标类上不存在对应属性时可忽略。
	 * @since 3.0
	 */
	public boolean isOptional() {
		return this.optional;
	}

	/**
	 * 返回本持有者是否已包含转换后的值（{@code true}），
	 * 或值仍需转换（{@code false}）。
	 */
	public synchronized boolean isConverted() {
		return this.converted;
	}

	/**
	 * 在完成类型转换后，设置本属性值的转换结果。
	 */
	public synchronized void setConvertedValue(@Nullable Object value) {
		this.converted = true;
		this.convertedValue = value;
	}

	/**
	 * 返回本属性值在类型转换后的结果。
	 */
	public synchronized @Nullable Object getConvertedValue() {
		return this.convertedValue;
	}


	@Override
	public boolean equals(@Nullable Object other) {
		return (this == other || (other instanceof PropertyValue that &&
				this.name.equals(that.name) &&
				ObjectUtils.nullSafeEquals(this.value, that.value) &&
				ObjectUtils.nullSafeEquals(getSource(), that.getSource())));
	}

	@Override
	public int hashCode() {
		return ObjectUtils.nullSafeHash(this.name, this.value);
	}

	@Override
	public String toString() {
		return "bean property '" + this.name + "'";
	}

}
