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

import java.util.Set;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.TypeConverter;
import org.springframework.core.ResolvableType;
import org.springframework.util.CollectionUtils;

/**
 * 共享 Set 实例的简单工厂。支持通过 XML bean 定义中的 "set" 元素集中配置 Set。
 *
 * @author Juergen Hoeller
 * @since 09.12.2003
 * @see ListFactoryBean
 * @see MapFactoryBean
 */
public class SetFactoryBean extends AbstractFactoryBean<Set<Object>> {

	/** 源 Set，通常由 XML "set" 元素填充。 */
	private @Nullable Set<?> sourceSet;

	/** 目标 Set 的实现类。 */
	@SuppressWarnings("rawtypes")
	private @Nullable Class<? extends Set> targetSetClass;


	/**
	 * 设置源 Set，通常由 XML "set" 元素填充。
	 */
	public void setSourceSet(Set<?> sourceSet) {
		this.sourceSet = sourceSet;
	}

	/**
	 * 设置目标 Set 使用的类。在 Spring 应用上下文中定义时可填入全限定类名。
	 * <p>默认为 LinkedHashSet，保持注册顺序。
	 * @see java.util.LinkedHashSet
	 */
	@SuppressWarnings("rawtypes")
	public void setTargetSetClass(@Nullable Class<? extends Set> targetSetClass) {
		if (targetSetClass == null) {
			throw new IllegalArgumentException("'targetSetClass' must not be null");
		}
		if (!Set.class.isAssignableFrom(targetSetClass)) {
			throw new IllegalArgumentException("'targetSetClass' must implement [java.util.Set]");
		}
		this.targetSetClass = targetSetClass;
	}


	@Override
	@SuppressWarnings("rawtypes")
	public Class<Set> getObjectType() {
		return Set.class;
	}

	@Override
	@SuppressWarnings("unchecked")
	protected Set<Object> createInstance() {
		if (this.sourceSet == null) {
			throw new IllegalArgumentException("'sourceSet' is required");
		}
		Set<Object> result = null;
		// 实例化目标 Set 类型，或使用默认 LinkedHashSet
		if (this.targetSetClass != null) {
			result = BeanUtils.instantiateClass(this.targetSetClass);
		}
		else {
			result = CollectionUtils.newLinkedHashSet(this.sourceSet.size());
		}
		Class<?> valueType = null;
		if (this.targetSetClass != null) {
			valueType = ResolvableType.forClass(this.targetSetClass).asCollection().resolveGeneric();
		}
		// 若可解析元素类型，则进行类型转换后添加
		if (valueType != null) {
			TypeConverter converter = getBeanTypeConverter();
			for (Object elem : this.sourceSet) {
				result.add(converter.convertIfNecessary(elem, valueType));
			}
		}
		else {
			result.addAll(this.sourceSet);
		}
		return result;
	}

}
