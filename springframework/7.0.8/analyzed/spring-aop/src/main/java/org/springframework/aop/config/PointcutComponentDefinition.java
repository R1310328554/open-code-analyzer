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

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.parsing.AbstractComponentDefinition;
import org.springframework.util.Assert;

/**
 * 保存切入点定义的 {@link org.springframework.beans.factory.parsing.ComponentDefinition} 实现。
 * @author Rob Harrop
 * @since 2.0
 */
public class PointcutComponentDefinition extends AbstractComponentDefinition {

	/** 名称相关状态（`pointcutBeanName`）。 */
	private final String pointcutBeanName;

	/** 切点相关状态（`pointcutDefinition`）。 */
	private final BeanDefinition pointcutDefinition;

	/** `description`：该类的成员状态。 */
	private final String description;


	/**
	 * 创建 `PointcutComponentDefinition` 的新实例。
	 */
	public PointcutComponentDefinition(String pointcutBeanName, BeanDefinition pointcutDefinition, String expression) {
		Assert.notNull(pointcutBeanName, "Bean name must not be null");
		Assert.notNull(pointcutDefinition, "Pointcut definition must not be null");
		Assert.notNull(expression, "Expression must not be null");
		this.pointcutBeanName = pointcutBeanName;
		this.pointcutDefinition = pointcutDefinition;
		this.description = "Pointcut <name='" + pointcutBeanName + "', expression=[" + expression + "]>";
	}


	/**
	 * 获取 Name（`Name`）。
	 */
	@Override
	public String getName() {
		return this.pointcutBeanName;
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
		return new BeanDefinition[] {this.pointcutDefinition};
	}

	/**
	 * 获取 Source（`Source`）。
	 */
	@Override
	public @Nullable Object getSource() {
		return this.pointcutDefinition.getSource();
	}

}
