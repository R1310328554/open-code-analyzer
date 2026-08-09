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

package org.springframework.beans.factory.annotation;

import org.springframework.beans.factory.config.AutowireCapableBeanFactory;

/**
 * 表示自动装配状态的枚举：Spring 容器是否应通过 setter 注入自动注入 Bean 的依赖。
 * 这是 Spring 依赖注入的核心概念之一。
 *
 * <p>可用于基于注解的配置，例如 AspectJ 的 {@code AnnotationBeanConfigurer} 切面。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 2.0
 * @see org.springframework.beans.factory.annotation.Configurable
 * @see org.springframework.beans.factory.config.AutowireCapableBeanFactory
 */
public enum Autowire {

	/**
	 * 表示完全不进行自动装配。
	 */
	NO(AutowireCapableBeanFactory.AUTOWIRE_NO),

	/**
	 * 表示按名称自动装配 Bean 属性。
	 */
	BY_NAME(AutowireCapableBeanFactory.AUTOWIRE_BY_NAME),

	/**
	 * 表示按类型自动装配 Bean 属性。
	 */
	BY_TYPE(AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE);


	/** 与 {@link AutowireCapableBeanFactory} 常量对应的整型值 */
	private final int value;


	Autowire(int value) {
		this.value = value;
	}

	/**
	 * 返回与 {@link AutowireCapableBeanFactory} 自动装配常量对应的整型值。
	 */
	public int value() {
		return this.value;
	}

	/**
	 * 判断该枚举是否表示实际启用了自动装配。
	 * @return 是否指定了实际的自动装配（{@code BY_NAME} 或 {@code BY_TYPE}）
	 */
	public boolean isAutowire() {
		return (this == BY_NAME || this == BY_TYPE);
	}

}
