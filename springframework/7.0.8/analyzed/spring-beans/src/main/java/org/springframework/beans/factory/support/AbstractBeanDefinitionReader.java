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

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.core.env.Environment;
import org.springframework.core.env.EnvironmentCapable;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.Assert;

/**
 * 实现 {@link BeanDefinitionReader} 接口的 Bean 定义读取器抽象基类。
 *
 * <p>提供通用属性，例如要操作的 Bean 工厂以及用于加载 Bean 类的类加载器。
 *
 * @author Juergen Hoeller
 * @author Chris Beams
 * @since 11.12.2003
 * @see BeanDefinitionReaderUtils
 */
public abstract class AbstractBeanDefinitionReader implements BeanDefinitionReader, EnvironmentCapable {

	/** 子类可用的日志记录器。 */
	protected final Log logger = LogFactory.getLog(getClass());

	/** Bean 定义注册表。 */
	private final BeanDefinitionRegistry registry;

	/** 用于解析资源位置的 ResourceLoader。 */
	private @Nullable ResourceLoader resourceLoader;

	/** 用于加载 Bean 类的类加载器。 */
	private @Nullable ClassLoader beanClassLoader;

	/** 读取 Bean 定义时使用的环境（如 Profile 评估）。 */
	private Environment environment;

	/** 为匿名 Bean 生成名称的生成器。 */
	private BeanNameGenerator beanNameGenerator = DefaultBeanNameGenerator.INSTANCE;


	/**
	 * 为给定的 Bean 工厂创建新的 AbstractBeanDefinitionReader。
	 * <p>若传入的 Bean 工厂不仅实现 BeanDefinitionRegistry 接口，还实现 ResourceLoader 接口，
	 * 则同时将其用作默认 ResourceLoader。这通常适用于
	 * {@link org.springframework.context.ApplicationContext} 实现。
	 * <p>若传入的是普通 BeanDefinitionRegistry，默认 ResourceLoader 为
	 * {@link org.springframework.core.io.support.PathMatchingResourcePatternResolver}。
	 * <p>若传入的 Bean 工厂还实现 {@link EnvironmentCapable}，则本读取器将使用其环境；
	 * 否则将初始化并使用 {@link StandardEnvironment}。所有 ApplicationContext 实现
	 * 均为 EnvironmentCapable，而普通 BeanFactory 实现则不是。
	 * @param registry 用于加载 Bean 定义的 BeanFactory，以 BeanDefinitionRegistry 形式提供
	 * @see #setResourceLoader
	 * @see #setEnvironment
	 */
	protected AbstractBeanDefinitionReader(BeanDefinitionRegistry registry) {
		Assert.notNull(registry, "BeanDefinitionRegistry must not be null");
		this.registry = registry;

		// 确定要使用的 ResourceLoader
		if (this.registry instanceof ResourceLoader _resourceLoader) {
			this.resourceLoader = _resourceLoader;
		}
		else {
			this.resourceLoader = new PathMatchingResourcePatternResolver();
		}

		// 尽可能继承环境
		if (this.registry instanceof EnvironmentCapable environmentCapable) {
			this.environment = environmentCapable.getEnvironment();
		}
		else {
			this.environment = new StandardEnvironment();
		}
	}


	@Override
	public final BeanDefinitionRegistry getRegistry() {
		return this.registry;
	}

	/**
	 * 设置用于资源位置的 ResourceLoader。
	 * 若指定 ResourcePatternResolver，则 Bean 定义读取器可解析资源模式为 Resource 数组。
	 * <p>默认为 PathMatchingResourcePatternResolver，同样支持通过 ResourcePatternResolver
	 * 接口进行资源模式解析。
	 * <p>设为 {@code null} 表示本 Bean 定义读取器不支持绝对资源加载。
	 * @see org.springframework.core.io.support.ResourcePatternResolver
	 * @see org.springframework.core.io.support.PathMatchingResourcePatternResolver
	 */
	public void setResourceLoader(@Nullable ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	@Override
	public @Nullable ResourceLoader getResourceLoader() {
		return this.resourceLoader;
	}

	/**
	 * 设置用于 Bean 类的 ClassLoader。
	 * <p>默认为 {@code null}，表示不立即加载 Bean 类，而仅注册带类名的 Bean 定义，
	 * 对应 Class 稍后（或永不）解析。
	 * @see Thread#getContextClassLoader()
	 */
	public void setBeanClassLoader(@Nullable ClassLoader beanClassLoader) {
		this.beanClassLoader = beanClassLoader;
	}

	@Override
	public @Nullable ClassLoader getBeanClassLoader() {
		return this.beanClassLoader;
	}

	/**
	 * 设置读取 Bean 定义时使用的 Environment。
	 * 最常用于评估 Profile 信息，以决定哪些 Bean 定义应被读取、哪些应被忽略。
	 */
	public void setEnvironment(Environment environment) {
		Assert.notNull(environment, "Environment must not be null");
		this.environment = environment;
	}

	@Override
	public Environment getEnvironment() {
		return this.environment;
	}

	/**
	 * 设置用于匿名 Bean（未显式指定 Bean 名称）的 BeanNameGenerator。
	 * <p>默认为 {@link DefaultBeanNameGenerator}。
	 */
	public void setBeanNameGenerator(@Nullable BeanNameGenerator beanNameGenerator) {
		this.beanNameGenerator = (beanNameGenerator != null ? beanNameGenerator : DefaultBeanNameGenerator.INSTANCE);
	}

	@Override
	public BeanNameGenerator getBeanNameGenerator() {
		return this.beanNameGenerator;
	}


	@Override
	public int loadBeanDefinitions(Resource... resources) throws BeanDefinitionStoreException {
		Assert.notNull(resources, "Resource array must not be null");
		int count = 0;
		for (Resource resource : resources) {
			count += loadBeanDefinitions(resource);
		}
		return count;
	}

	@Override
	public int loadBeanDefinitions(String location) throws BeanDefinitionStoreException {
		return loadBeanDefinitions(location, null);
	}

	/**
	 * 从指定资源位置加载 Bean 定义。
	 * <p>若本 Bean 定义读取器的 ResourceLoader 为 ResourcePatternResolver，
	 * 则 location 也可以是位置模式。
	 * @param location 资源位置，由本 Bean 定义读取器的 ResourceLoader
	 *（或 ResourcePatternResolver）加载
	 * @param actualResources 用于填充加载过程中实际解析到的 Resource 对象的集合；
	 * 可为 {@code null}，表示调用方不关心这些 Resource 对象
	 * @return 找到的 Bean 定义数量
	 * @throws BeanDefinitionStoreException 加载或解析出错时
	 * @see #getResourceLoader()
	 * @see #loadBeanDefinitions(org.springframework.core.io.Resource)
	 * @see #loadBeanDefinitions(org.springframework.core.io.Resource[])
	 */
	public int loadBeanDefinitions(String location, @Nullable Set<Resource> actualResources) throws BeanDefinitionStoreException {
		ResourceLoader resourceLoader = getResourceLoader();
		if (resourceLoader == null) {
			throw new BeanDefinitionStoreException(
					"Cannot load bean definitions from location [" + location + "]: no ResourceLoader available");
		}

		if (resourceLoader instanceof ResourcePatternResolver resourcePatternResolver) {
			// 支持资源模式匹配
			try {
				Resource[] resources = resourcePatternResolver.getResources(location);
				int count = loadBeanDefinitions(resources);
				if (actualResources != null) {
					Collections.addAll(actualResources, resources);
				}
				if (logger.isTraceEnabled()) {
					logger.trace("Loaded " + count + " bean definitions from location pattern [" + location + "]");
				}
				return count;
			}
			catch (IOException ex) {
				throw new BeanDefinitionStoreException(
						"Could not resolve bean definition resource pattern [" + location + "]", ex);
			}
		}
		else {
			// 仅能通过绝对 URL 加载单个资源
			Resource resource = resourceLoader.getResource(location);
			int count = loadBeanDefinitions(resource);
			if (actualResources != null) {
				actualResources.add(resource);
			}
			if (logger.isTraceEnabled()) {
				logger.trace("Loaded " + count + " bean definitions from location [" + location + "]");
			}
			return count;
		}
	}

	@Override
	public int loadBeanDefinitions(String... locations) throws BeanDefinitionStoreException {
		Assert.notNull(locations, "Location array must not be null");
		int count = 0;
		for (String location : locations) {
			count += loadBeanDefinitions(location);
		}
		return count;
	}

}
