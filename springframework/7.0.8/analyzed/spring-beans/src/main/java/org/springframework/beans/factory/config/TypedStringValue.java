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

import java.util.Comparator;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.BeanMetadataElement;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;

/**
 * 带类型信息的 String 值持有者。可添加到 bean 定义中，
 * 为 String 值（例如集合元素）显式指定目标类型。
 *
 * <p>本持有者仅存储 String 值与目标类型，实际转换由 bean 工厂执行。
 *
 * @author Juergen Hoeller
 * @since 1.2
 * @see BeanDefinition#getPropertyValues
 * @see org.springframework.beans.MutablePropertyValues#addPropertyValue
 */
public class TypedStringValue implements BeanMetadataElement, Comparable<TypedStringValue> {

	/** String 值。 */
	private @Nullable String value;

	/** 目标类型（Class 或类名字符串）。 */
	private volatile @Nullable Object targetType;

	/** 配置元数据来源对象。 */
	private @Nullable Object source;

	/** 为本值实际指定的类型名（若有）。 */
	private @Nullable String specifiedTypeName;

	/** 是否标记为动态值（含表达式，不参与缓存）。 */
	private volatile boolean dynamic;


	/**
	 * 为给定 String 值创建新的 {@link TypedStringValue}。
	 * @param value String 值
	 */
	public TypedStringValue(@Nullable String value) {
		setValue(value);
	}

	/**
	 * 为给定 String 值与目标类型创建新的 {@link TypedStringValue}。
	 * @param value String 值
	 * @param targetType 要转换到的类型
	 */
	public TypedStringValue(@Nullable String value, Class<?> targetType) {
		setValue(value);
		setTargetType(targetType);
	}

	/**
	 * 为给定 String 值与目标类型创建新的 {@link TypedStringValue}。
	 * @param value String 值
	 * @param targetTypeName 要转换到的类型名
	 */
	public TypedStringValue(@Nullable String value, String targetTypeName) {
		setValue(value);
		setTargetTypeName(targetTypeName);
	}


	/**
	 * 设置 String 值。
	 * <p>仅在操作已注册值时需要，例如在 BeanFactoryPostProcessor 中。
	 */
	public void setValue(@Nullable String value) {
		this.value = value;
	}

	/**
	 * 返回 String 值。
	 */
	public @Nullable String getValue() {
		return this.value;
	}

	/**
	 * 设置要转换到的类型。
	 * <p>仅在操作已注册值时需要，例如在 BeanFactoryPostProcessor 中。
	 */
	public void setTargetType(Class<?> targetType) {
		Assert.notNull(targetType, "'targetType' must not be null");
		this.targetType = targetType;
	}

	/**
	 * 返回要转换到的类型。
	 */
	public Class<?> getTargetType() {
		Object targetTypeValue = this.targetType;
		if (!(targetTypeValue instanceof Class<?> clazz)) {
			throw new IllegalStateException("Typed String value does not carry a resolved target type");
		}
		return clazz;
	}

	/**
	 * 指定要转换到的类型名。
	 */
	public void setTargetTypeName(@Nullable String targetTypeName) {
		this.targetType = targetTypeName;
	}

	/**
	 * 返回要转换到的类型名。
	 */
	public @Nullable String getTargetTypeName() {
		Object targetTypeValue = this.targetType;
		if (targetTypeValue instanceof Class<?> clazz) {
			return clazz.getName();
		}
		else {
			return (String) targetTypeValue;
		}
	}

	/**
	 * 返回本带类型 String 值是否携带目标类型。
	 */
	public boolean hasTargetType() {
		return (this.targetType instanceof Class);
	}

	/**
	 * 确定要转换到的类型，必要时从指定类名解析。若目标类型已解析，
	 * 调用时也会从类名重新加载指定 Class。
	 * @param classLoader 用于解析（潜在）类名的 ClassLoader
	 * @return 解析后的目标类型
	 * @throws ClassNotFoundException 类型无法解析时
	 */
	public @Nullable Class<?> resolveTargetType(@Nullable ClassLoader classLoader) throws ClassNotFoundException {
		String typeName = getTargetTypeName();
		if (typeName == null) {
			return null;
		}
		Class<?> resolvedClass = ClassUtils.forName(typeName, classLoader);
		this.targetType = resolvedClass;
		return resolvedClass;
	}


	/**
	 * 设置本元数据元素的配置来源 {@code Object}。
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
	 * 设置为本特定值实际指定的类型名（若有）。
	 */
	public void setSpecifiedTypeName(@Nullable String specifiedTypeName) {
		this.specifiedTypeName = specifiedTypeName;
	}

	/**
	 * 返回为本特定值实际指定的类型名（若有）。
	 */
	public @Nullable String getSpecifiedTypeName() {
		return this.specifiedTypeName;
	}

	/**
	 * 将本值标记为动态，即包含表达式，因此不参与缓存。
	 */
	public void setDynamic() {
		this.dynamic = true;
	}

	/**
	 * 返回本值是否已标记为动态。
	 */
	public boolean isDynamic() {
		return this.dynamic;
	}

	@Override
	public int compareTo(@Nullable TypedStringValue o) {
		return Comparator.comparing(TypedStringValue::getValue).compare(this, o);
	}

	@Override
	public boolean equals(@Nullable Object other) {
		return (this == other || (other instanceof TypedStringValue that &&
				ObjectUtils.nullSafeEquals(this.value, that.value) &&
				ObjectUtils.nullSafeEquals(this.targetType, that.targetType)));
	}

	@Override
	public int hashCode() {
		return ObjectUtils.nullSafeHash(this.value, this.targetType);
	}

	@Override
	public String toString() {
		return "TypedStringValue: value [" + this.value + "], target type [" + this.targetType + "]";
	}

}
