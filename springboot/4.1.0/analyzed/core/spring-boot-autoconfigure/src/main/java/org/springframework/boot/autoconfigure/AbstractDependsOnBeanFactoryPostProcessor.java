/*
 * Copyright 2012-present the original author or authors.
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

package org.springframework.boot.autoconfigure;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.Ordered;
import org.springframework.util.StringUtils;

/**
 * {@link BeanFactoryPostProcessor} 的抽象基类，用于动态声明：指定类型的所有 bean
 * 应依赖以名称或类型标识的其他 bean。
 *
 * @author Marcel Overdijk
 * @author Dave Syer
 * @author Phillip Webb
 * @author Andy Wilkinson
 * @author Dmytro Nosan
 * @since 1.3.0
 * @see BeanDefinition#setDependsOn(String[])
 */
public abstract class AbstractDependsOnBeanFactoryPostProcessor implements BeanFactoryPostProcessor, Ordered {

	/** 目标 bean 的类型。 */
	private final Class<?> beanClass;

	/** 目标 {@link FactoryBean} 的类型，可为 {@code null}。 */
	private final @Nullable Class<? extends FactoryBean<?>> factoryBeanClass;

	/** 根据 {@link ListableBeanFactory} 解析出依赖 bean 名称的函数。 */
	private final Function<ListableBeanFactory, Set<String>> dependsOn;

	/**
	 * 使用目标 bean 类型、{@link FactoryBean} 类型及依赖名称创建实例。
	 * @param beanClass 目标 bean 类型
	 * @param factoryBeanClass 目标 {@link FactoryBean} 类型
	 * @param dependsOn 依赖的 bean 名称
	 */
	protected AbstractDependsOnBeanFactoryPostProcessor(Class<?> beanClass,
			@Nullable Class<? extends FactoryBean<?>> factoryBeanClass, String... dependsOn) {
		this.beanClass = beanClass;
		this.factoryBeanClass = factoryBeanClass;
		this.dependsOn = (beanFactory) -> new HashSet<>(Arrays.asList(dependsOn));
	}

	/**
	 * 使用目标 bean 类型、{@link FactoryBean} 类型及依赖类型创建实例。
	 * @param beanClass 目标 bean 类型
	 * @param factoryBeanClass 目标 {@link FactoryBean} 类型
	 * @param dependencyTypes 依赖的 bean 类型
	 * @since 2.1.7
	 */
	protected AbstractDependsOnBeanFactoryPostProcessor(Class<?> beanClass,
			@Nullable Class<? extends FactoryBean<?>> factoryBeanClass, Class<?>... dependencyTypes) {
		this.beanClass = beanClass;
		this.factoryBeanClass = factoryBeanClass;
		this.dependsOn = (beanFactory) -> Arrays.stream(dependencyTypes)
			.flatMap((dependencyType) -> getBeanNames(beanFactory, dependencyType).stream())
			.collect(Collectors.toSet());
	}

	/**
	 * 使用目标 bean 类型及依赖名称创建实例。
	 * @param beanClass 目标 bean 类型
	 * @param dependsOn 依赖的 bean 名称
	 * @since 2.0.4
	 */
	protected AbstractDependsOnBeanFactoryPostProcessor(Class<?> beanClass, String... dependsOn) {
		this(beanClass, null, dependsOn);
	}

	/**
	 * 使用目标 bean 类型及依赖类型创建实例。
	 * @param beanClass 目标 bean 类型
	 * @param dependencyTypes 依赖的 bean 类型
	 * @since 2.1.7
	 */
	protected AbstractDependsOnBeanFactoryPostProcessor(Class<?> beanClass, Class<?>... dependencyTypes) {
		this(beanClass, null, dependencyTypes);
	}

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
		// 遍历目标类型的所有 bean，将解析出的依赖名称追加到 dependsOn 属性
		for (String beanName : getBeanNames(beanFactory)) {
			BeanDefinition definition = getBeanDefinition(beanName, beanFactory);
			String[] dependencies = definition.getDependsOn();
			for (String dependencyName : this.dependsOn.apply(beanFactory)) {
				dependencies = StringUtils.addStringToArray(dependencies, dependencyName);
			}
			definition.setDependsOn(dependencies);
		}
	}

	@Override
	public int getOrder() {
		return 0;
	}

	private Set<String> getBeanNames(ListableBeanFactory beanFactory) {
		Set<String> names = getBeanNames(beanFactory, this.beanClass);
		if (this.factoryBeanClass != null) {
			names.addAll(getBeanNames(beanFactory, this.factoryBeanClass));
		}
		return names;
	}

	private static Set<String> getBeanNames(ListableBeanFactory beanFactory, Class<?> beanClass) {
		String[] names = BeanFactoryUtils.beanNamesForTypeIncludingAncestors(beanFactory, beanClass, true, false);
		return Arrays.stream(names).map(BeanFactoryUtils::transformedBeanName).collect(Collectors.toSet());
	}

	private static BeanDefinition getBeanDefinition(String beanName, ConfigurableListableBeanFactory beanFactory) {
		try {
			return beanFactory.getBeanDefinition(beanName);
		}
		catch (NoSuchBeanDefinitionException ex) {
			BeanFactory parentBeanFactory = beanFactory.getParentBeanFactory();
			if (parentBeanFactory instanceof ConfigurableListableBeanFactory listableBeanFactory) {
				return getBeanDefinition(beanName, listableBeanFactory);
			}
			throw ex;
		}
	}

}
