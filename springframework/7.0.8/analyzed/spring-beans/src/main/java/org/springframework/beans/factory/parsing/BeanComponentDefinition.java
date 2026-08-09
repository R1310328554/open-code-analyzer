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

package org.springframework.beans.factory.parsing;

import java.util.ArrayList;
import java.util.List;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.PropertyValue;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.BeanReference;

/**
 * 基于标准 {@link BeanDefinition} 的 {@link ComponentDefinition}，
 * 暴露给定 Bean 的定义及其内部 Bean 定义与 Bean 引用。
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 2.0
 */
public class BeanComponentDefinition extends BeanDefinitionHolder implements ComponentDefinition {

	/** 空 BeanDefinition 数组常量。 */
	private static final BeanDefinition[] EMPTY_BEAN_DEFINITION_ARRAY = new BeanDefinition[0];

	/** 空 BeanReference 数组常量。 */
	private static final BeanReference[] EMPTY_BEAN_REFERENCE_ARRAY = new BeanReference[0];

	/** 内部 Bean 定义数组。 */
	private final BeanDefinition[] innerBeanDefinitions;

	/** Bean 引用数组。 */
	private final BeanReference[] beanReferences;


	/**
	 * 为给定 Bean 创建新的 {@link BeanComponentDefinition}。
	 * @param beanDefinition BeanDefinition
	 * @param beanName Bean 名称
	 */
	public BeanComponentDefinition(BeanDefinition beanDefinition, String beanName) {
		this(new BeanDefinitionHolder(beanDefinition, beanName));
	}

	/**
	 * 为给定 Bean 创建新的 {@link BeanComponentDefinition}。
	 * @param beanDefinition BeanDefinition
	 * @param beanName Bean 名称
	 * @param aliases Bean 的别名，若无则为 {@code null}
	 */
	public BeanComponentDefinition(BeanDefinition beanDefinition, String beanName, String @Nullable [] aliases) {
		this(new BeanDefinitionHolder(beanDefinition, beanName, aliases));
	}

	/**
	 * 为给定 Bean 创建新的 {@link BeanComponentDefinition}。
	 * @param beanDefinitionHolder 封装 Bean 定义及名称的 BeanDefinitionHolder
	 */
	public BeanComponentDefinition(BeanDefinitionHolder beanDefinitionHolder) {
		super(beanDefinitionHolder);

		List<BeanDefinition> innerBeans = new ArrayList<>();
		List<BeanReference> references = new ArrayList<>();
		PropertyValues propertyValues = beanDefinitionHolder.getBeanDefinition().getPropertyValues();
		// 遍历属性值，收集内部 Bean 定义与 Bean 引用
		for (PropertyValue propertyValue : propertyValues.getPropertyValues()) {
			Object value = propertyValue.getValue();
			if (value instanceof BeanDefinitionHolder beanDefHolder) {
				innerBeans.add(beanDefHolder.getBeanDefinition());
			}
			else if (value instanceof BeanDefinition beanDef) {
				innerBeans.add(beanDef);
			}
			else if (value instanceof BeanReference beanRef) {
				references.add(beanRef);
			}
		}
		this.innerBeanDefinitions = innerBeans.toArray(EMPTY_BEAN_DEFINITION_ARRAY);
		this.beanReferences = references.toArray(EMPTY_BEAN_REFERENCE_ARRAY);
	}


	@Override
	public String getName() {
		return getBeanName();
	}

	@Override
	public String getDescription() {
		return getShortDescription();
	}

	@Override
	public BeanDefinition[] getBeanDefinitions() {
		return new BeanDefinition[] {getBeanDefinition()};
	}

	@Override
	public BeanDefinition[] getInnerBeanDefinitions() {
		return this.innerBeanDefinitions;
	}

	@Override
	public BeanReference[] getBeanReferences() {
		return this.beanReferences;
	}


	/**
	 * 本实现返回本 ComponentDefinition 的描述。
	 * @see #getDescription()
	 */
	@Override
	public String toString() {
		return getDescription();
	}

	/**
	 * 本实现除满足超类的相等性要求外，还要求另一对象同为 BeanComponentDefinition 类型。
	 */
	@Override
	public boolean equals(@Nullable Object other) {
		return (this == other || (other instanceof BeanComponentDefinition && super.equals(other)));
	}

}
