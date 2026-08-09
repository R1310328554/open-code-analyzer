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

package org.springframework.aop.config;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanReference;
import org.springframework.beans.factory.parsing.AbstractComponentDefinition;
import org.springframework.util.Assert;

/**
 * {@link org.springframework.beans.factory.parsing.ComponentDefinition} 弥补了 {@code
 * <aop:advisor>} 标签配置的顾问 bean 定义与组件定义基础结构之间的差距。
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 2.0
 */
public class AdvisorComponentDefinition extends AbstractComponentDefinition {

	/** 名称相关状态（`advisorBeanName`）。 */
	private final String advisorBeanName;

	/** 通知器相关状态（`advisorDefinition`）。 */
	private final BeanDefinition advisorDefinition;

	/** `description`：该类的成员状态。 */
	private final String description;

	/** Bean相关状态（`beanReferences`）。 */
	private final BeanReference[] beanReferences;

	/** Bean相关状态（`beanDefinitions`）。 */
	private final BeanDefinition[] beanDefinitions;


	/**
	 * 创建 `AdvisorComponentDefinition` 的新实例。
	 */
	public AdvisorComponentDefinition(String advisorBeanName, BeanDefinition advisorDefinition) {
		this(advisorBeanName, advisorDefinition, null);
	}

	/**
	 * 创建 `AdvisorComponentDefinition` 的新实例。
	 */
	public AdvisorComponentDefinition(
			String advisorBeanName, BeanDefinition advisorDefinition, @Nullable BeanDefinition pointcutDefinition) {

		Assert.notNull(advisorBeanName, "'advisorBeanName' must not be null");
		Assert.notNull(advisorDefinition, "'advisorDefinition' must not be null");
		this.advisorBeanName = advisorBeanName;
		this.advisorDefinition = advisorDefinition;

		MutablePropertyValues pvs = advisorDefinition.getPropertyValues();
		BeanReference adviceReference = (BeanReference) pvs.get("adviceBeanName");
		Assert.state(adviceReference != null, "Missing 'adviceBeanName' property");

		if (pointcutDefinition != null) {
			this.beanReferences = new BeanReference[] {adviceReference};
			this.beanDefinitions = new BeanDefinition[] {advisorDefinition, pointcutDefinition};
			this.description = buildDescription(adviceReference, pointcutDefinition);
		}
		else {
			BeanReference pointcutReference = (BeanReference) pvs.get("pointcut");
			Assert.state(pointcutReference != null, "Missing 'pointcut' property");
			this.beanReferences = new BeanReference[] {adviceReference, pointcutReference};
			this.beanDefinitions = new BeanDefinition[] {advisorDefinition};
			this.description = buildDescription(adviceReference, pointcutReference);
		}
	}

	/**
	 * 构建：Description（方法 `buildDescription`）。
	 */
	private String buildDescription(BeanReference adviceReference, BeanDefinition pointcutDefinition) {
		return "Advisor <advice(ref)='" +
				adviceReference.getBeanName() + "', pointcut(expression)=[" +
				pointcutDefinition.getPropertyValues().get("expression") + "]>";
	}

	/**
	 * 构建：Description（方法 `buildDescription`）。
	 */
	private String buildDescription(BeanReference adviceReference, BeanReference pointcutReference) {
		return "Advisor <advice(ref)='" +
				adviceReference.getBeanName() + "', pointcut(ref)='" +
				pointcutReference.getBeanName() + "'>";
	}


	/**
	 * 获取 Name（`Name`）。
	 */
	@Override
	public String getName() {
		return this.advisorBeanName;
	}

	/**
	 * 获取 Description（`Description`）。
	 */
	@Override
	public String getDescription() {
		return this.description;
	}

	/**
	 * 获取 Bean Definitions（`BeanDefinitions`）。
	 */
	@Override
	public BeanDefinition[] getBeanDefinitions() {
		return this.beanDefinitions;
	}

	/**
	 * 获取 Bean References（`BeanReferences`）。
	 */
	@Override
	public BeanReference[] getBeanReferences() {
		return this.beanReferences;
	}

	/**
	 * 获取 Source（`Source`）。
	 */
	@Override
	public @Nullable Object getSource() {
		return this.advisorDefinition.getSource();
	}

}
