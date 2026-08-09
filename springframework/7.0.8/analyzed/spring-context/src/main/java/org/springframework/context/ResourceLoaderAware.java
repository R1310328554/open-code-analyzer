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

package org.springframework.context;

import org.springframework.beans.factory.Aware;
import org.springframework.core.io.ResourceLoader;

/**
 * 任何希望获知其所运行的 {@link ResourceLoader}（通常是 ApplicationContext）
 * 的对象应实现的接口。这是通过 {@link ApplicationContextAware} 接口
 * 依赖完整 {@link ApplicationContext} 的替代方案。
 *
 * <p>注意，{@link org.springframework.core.io.Resource} 依赖也可作为
 * {@code Resource} 或 {@code Resource[]} 类型的 Bean 属性暴露，
 * 通过字符串自动类型转换由 Bean 工厂填充。这样无需仅为访问特定文件资源
 * 而实现任何回调接口。
 *
 * <p>当应用对象需要访问名称需动态计算的各种文件资源时，通常需要
 * {@link ResourceLoader}。一种良好策略是让对象使用
 * {@link org.springframework.core.io.DefaultResourceLoader}，
 * 同时实现 {@code ResourceLoaderAware}，以便在 {@code ApplicationContext}
 * 中运行时可以覆盖。参见
 * {@link org.springframework.context.support.ReloadableResourceBundleMessageSource} 的示例。
 *
 * <p>传入的 {@code ResourceLoader} 也可检查是否实现
 * {@link org.springframework.core.io.support.ResourcePatternResolver} 接口并相应转型，
 * 从而将资源模式解析为 {@code Resource} 数组。在 ApplicationContext 中运行时
 * 始终有效（因为上下文接口扩展了 ResourcePatternResolver）。
 * 默认使用 {@link org.springframework.core.io.support.PathMatchingResourcePatternResolver}；
 * 另见 {@code ResourcePatternUtils.getResourcePatternResolver} 方法。
 *
 * <p>作为 {@code ResourcePatternResolver} 依赖的替代方案，
 * 可考虑暴露 {@code Resource[]} 类型的 Bean 属性，在绑定时通过模式字符串
 * 由 Bean 工厂自动类型转换填充。
 *
 * @author Juergen Hoeller
 * @author Chris Beams
 * @since 10.03.2004
 * @see ApplicationContextAware
 * @see org.springframework.core.io.Resource
 * @see org.springframework.core.io.ResourceLoader
 * @see org.springframework.core.io.support.ResourcePatternResolver
 */
public interface ResourceLoaderAware extends Aware {

	/**
	 * 设置此对象所使用的 ResourceLoader。
	 * <p>它可能是 ResourcePatternResolver，可通过
	 * {@code instanceof ResourcePatternResolver} 检查。
	 * 另见 {@code ResourcePatternUtils.getResourcePatternResolver} 方法。
	 * <p>在填充普通 Bean 属性之后、初始化回调（如 InitializingBean 的
	 * {@code afterPropertiesSet} 或自定义 init-method）之前调用。
	 * 在 ApplicationContextAware 的 {@code setApplicationContext} 之前调用。
	 * @param resourceLoader the ResourceLoader object to be used by this object
	 * @see org.springframework.core.io.support.ResourcePatternResolver
	 * @see org.springframework.core.io.support.ResourcePatternUtils#getResourcePatternResolver
	 */
	void setResourceLoader(ResourceLoader resourceLoader);

}
