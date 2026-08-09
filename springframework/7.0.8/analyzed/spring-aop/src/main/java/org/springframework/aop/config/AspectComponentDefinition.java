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
import org.springframework.beans.factory.config.BeanReference;
import org.springframework.beans.factory.parsing.CompositeComponentDefinition;

/**
 * {@link org.springframework.beans.factory.parsing.ComponentDefinition} 保存切面定义，包括其嵌套切入点。
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 2.0
 * @see #getNestedComponents()
 * @see PointcutComponentDefinition
 */
public class AspectComponentDefinition extends CompositeComponentDefinition {

	/** Bean相关状态（`beanDefinitions`）。 */
	private final BeanDefinition[] beanDefinitions;

	/** Bean相关状态（`beanReferences`）。 */
	private final BeanReference[] beanReferences;


	/**
	 * 创建 `AspectComponentDefinition` 的新实例。
	 */
	public AspectComponentDefinition(String aspectName, BeanDefinition @Nullable [] beanDefinitions,
			BeanReference @Nullable [] beanReferences, @Nullable Object source) {

		super(aspectName, source);
		this.beanDefinitions = (beanDefinitions != null ? beanDefinitions : new BeanDefinition[0]);
		this.beanReferences = (beanReferences != null ? beanReferences : new BeanReference[0]);
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

}
