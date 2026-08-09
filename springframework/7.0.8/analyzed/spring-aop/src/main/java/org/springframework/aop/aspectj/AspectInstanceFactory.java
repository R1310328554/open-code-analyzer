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
 * 提供 AspectJ 切面实例的接口。
 * 与 Spring Bean 工厂解耦。
 *
 * <p>扩展 {@link org.springframework.core.Ordered} 接口，
 * 为链中的底层切面表达顺序值。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 2.0
 * @see org.springframework.beans.factory.BeanFactory#getBean
 */
public interface AspectInstanceFactory extends Ordered {

	/**
	 * 创建本工厂切面的一个实例。
	 * @return 切面实例（永不为 {@code null}）
	 */
	Object getAspectInstance();

	/**
	 * 暴露本工厂使用的切面类加载器。
	 * @return 切面类加载器（引导类加载器时为 {@code null}）
	 * @see org.springframework.util.ClassUtils#getDefaultClassLoader()
	 */
	@Nullable ClassLoader getAspectClassLoader();

}
