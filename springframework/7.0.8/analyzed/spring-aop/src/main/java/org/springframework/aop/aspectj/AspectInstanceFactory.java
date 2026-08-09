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

package org.springframework.aop.aspectj;

import org.jspecify.annotations.Nullable;

import org.springframework.core.Ordered;

/**
 * 实现接口以提供 AspectJ 方面的实例。与 Spring 的 bean 工厂解耦。
 * <p> 扩展了 {@link org.springframework.core.Ordered} 接口以表达链中底层方面的顺序值。
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 2.0
 * @see org.springframework.beans.factory.BeanFactory#getBean
 */
public interface AspectInstanceFactory extends Ordered {

	/**
	 * 创建该工厂方面的一个实例。
	 * @return 方面实例（绝不是 {@code null}）
	 */
	Object getAspectInstance();

	/**
	 * 公开该工厂使用的方面类加载器。
	 * @return 方面类加载器（或用于引导加载器的 {@code null}）
	 * @see org.springframework.util.ClassUtils#getDefaultClassLoader()
	 */
	@Nullable ClassLoader getAspectClassLoader();

}
