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

package org.springframework.beans.factory.support;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

/**
 * Bean 定义读取器的简单接口，指定了以 {@link Resource} 和 {@link String} 位置参数加载 Bean 定义的方法。
 *
 * <p>具体的 Bean 定义读取器当然可以根据其 Bean 定义格式，
 * 添加额外的加载与注册方法。
 *
 * @author Juergen Hoeller
 * @since 1.1
 * @see org.springframework.core.io.Resource
 */
public interface BeanDefinitionReader {

	/**
	 * 返回用于注册 Bean 定义的 Bean 工厂。
	 * <p>工厂通过 {@link BeanDefinitionRegistry} 接口暴露，
	 * 封装了与 Bean 定义处理相关的方法。
	 */
	BeanDefinitionRegistry getRegistry();

	/**
	 * 返回用于解析资源位置的 {@link ResourceLoader}。
	 * <p>可检查是否实现 {@code ResourcePatternResolver} 接口并相应转型，
	 * 以便为给定资源模式加载多个资源。
	 * <p>返回 {@code null} 表示此 Bean 定义读取器不支持绝对资源加载。
	 * <p>主要用于在 Bean 定义资源内部导入更多资源，例如 XML Bean 定义中的
	 * {@code import} 标签。建议相对于定义资源进行此类导入；只有显式的完整
	 * 资源位置才会触发基于绝对路径的资源加载。
	 * <p>也提供 {@code loadBeanDefinitions(String)} 方法，
	 * 用于从资源位置（或位置模式）加载 Bean 定义，可避免显式处理 {@code ResourceLoader}。
	 * @see #loadBeanDefinitions(String)
	 * @see org.springframework.core.io.support.ResourcePatternResolver
	 */
	@Nullable ResourceLoader getResourceLoader();

	/**
	 * 返回用于加载 Bean 类的类加载器。
	 * <p>{@code null} 表示不急切加载 Bean 类，而仅注册带类名的 Bean 定义，
	 * 相应类稍后（或永不）解析。
	 */
	@Nullable ClassLoader getBeanClassLoader();

	/**
	 * 返回用于匿名 Bean（未显式指定 Bean 名称）的 {@link BeanNameGenerator}。
	 */
	BeanNameGenerator getBeanNameGenerator();


	/**
	 * 从指定资源加载 Bean 定义。
	 * @param resource 资源描述符
	 * @return 找到的 Bean 定义数量
	 * @throws BeanDefinitionStoreException 加载或解析出错时
	 */
	int loadBeanDefinitions(Resource resource) throws BeanDefinitionStoreException;

	/**
	 * 从指定资源数组加载 Bean 定义。
	 * @param resources 资源描述符数组
	 * @return 找到的 Bean 定义数量
	 * @throws BeanDefinitionStoreException 加载或解析出错时
	 */
	int loadBeanDefinitions(Resource... resources) throws BeanDefinitionStoreException;

	/**
	 * 从指定资源位置加载 Bean 定义。
	 * <p>位置也可以是位置模式，前提是此 Bean 定义读取器的
	 * {@link ResourceLoader} 为 {@code ResourcePatternResolver}。
	 * @param location 资源位置，将使用此 Bean 定义读取器的
	 * {@code ResourceLoader}（或 {@code ResourcePatternResolver}）加载
	 * @return 找到的 Bean 定义数量
	 * @throws BeanDefinitionStoreException 加载或解析出错时
	 * @see #getResourceLoader()
	 * @see #loadBeanDefinitions(org.springframework.core.io.Resource)
	 * @see #loadBeanDefinitions(org.springframework.core.io.Resource[])
	 */
	int loadBeanDefinitions(String location) throws BeanDefinitionStoreException;

	/**
	 * 从指定资源位置数组加载 Bean 定义。
	 * @param locations 资源位置数组，将使用此 Bean 定义读取器的
	 * {@code ResourceLoader}（或 {@code ResourcePatternResolver}）加载
	 * @return 找到的 Bean 定义数量
	 * @throws BeanDefinitionStoreException 加载或解析出错时
	 */
	int loadBeanDefinitions(String... locations) throws BeanDefinitionStoreException;

}
