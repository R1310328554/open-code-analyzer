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
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * 对 Bean 定义读取器实现有用的工具方法。
 * 主要供内部使用。
 *
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @since 1.1
 * @see org.springframework.beans.factory.xml.DefaultBeanDefinitionDocumentReader
 */
public abstract class BeanDefinitionReaderUtils {

	/**
	 * 生成 Bean 名称的分隔符。若类名或父名称不唯一，将追加 "#1"、"#2" 等，直至名称唯一。
	 */
	public static final String GENERATED_BEAN_NAME_SEPARATOR = BeanFactoryUtils.GENERATED_BEAN_NAME_SEPARATOR;


	/**
	 * 为给定的父名称和类名创建新的 GenericBeanDefinition；
	 * 若已指定 ClassLoader，则急切加载 Bean 类。
	 * @param parentName 父 Bean 名称（若有）
	 * @param className Bean 类名（若有）
	 * @param classLoader 用于加载 Bean 类的 ClassLoader（可为 {@code null}，仅按名称注册类）
	 * @return Bean 定义
	 * @throws ClassNotFoundException 无法加载 Bean 类时
	 */
	public static AbstractBeanDefinition createBeanDefinition(
			@Nullable String parentName, @Nullable String className, @Nullable ClassLoader classLoader) throws ClassNotFoundException {

		GenericBeanDefinition bd = new GenericBeanDefinition();
		bd.setParentName(parentName);
		if (className != null) {
			if (classLoader != null) {
				bd.setBeanClass(ClassUtils.forName(className, classLoader));
			}
			else {
				bd.setBeanClassName(className);
			}
		}
		return bd;
	}

	/**
	 * 为给定的顶层 Bean 定义生成 Bean 名称，在给定 Bean 工厂内唯一。
	 * @param beanDefinition 要生成名称的 Bean 定义
	 * @param registry 定义将注册到的 Bean 工厂（用于检查已有 Bean 名称）
	 * @return 生成的 Bean 名称
	 * @throws BeanDefinitionStoreException 无法为给定 Bean 定义生成唯一名称时
	 * @see #generateBeanName(BeanDefinition, BeanDefinitionRegistry, boolean)
	 */
	public static String generateBeanName(BeanDefinition beanDefinition, BeanDefinitionRegistry registry)
			throws BeanDefinitionStoreException {

		return generateBeanName(beanDefinition, registry, false);
	}

	/**
	 * 为给定 Bean 定义生成 Bean 名称，在给定 Bean 工厂内唯一。
	 * @param definition 要生成名称的 Bean 定义
	 * @param registry 定义将注册到的 Bean 工厂（用于检查已有 Bean 名称）
	 * @param isInnerBean 给定 Bean 定义是否作为内部 Bean 注册（允许内部 Bean 与顶层 Bean 采用不同命名策略）
	 * @return 生成的 Bean 名称
	 * @throws BeanDefinitionStoreException 无法为给定 Bean 定义生成唯一名称时
	 */
	public static String generateBeanName(
			BeanDefinition definition, BeanDefinitionRegistry registry, boolean isInnerBean)
			throws BeanDefinitionStoreException {

		String generatedBeanName = definition.getBeanClassName();
		if (generatedBeanName == null) {
			if (definition.getParentName() != null) {
				generatedBeanName = definition.getParentName() + "$child";
			}
			else if (definition.getFactoryBeanName() != null) {
				generatedBeanName = definition.getFactoryBeanName() + "$created";
			}
		}
		if (!StringUtils.hasText(generatedBeanName)) {
			throw new BeanDefinitionStoreException("Unnamed bean definition specifies neither " +
					"'class' nor 'parent' nor 'factory-bean' - can't generate bean name");
		}

		if (isInnerBean) {
			// 内部 Bean：追加身份哈希码后缀
			return generatedBeanName + GENERATED_BEAN_NAME_SEPARATOR + ObjectUtils.getIdentityHexString(definition);
		}

		// 顶层 Bean：使用纯类名，必要时追加唯一后缀
		return uniqueBeanName(generatedBeanName, registry);
	}

	/**
	 * 将给定 Bean 名称转换为在给定 Bean 工厂内的唯一名称，必要时追加唯一计数器后缀。
	 * @param beanName 原始 Bean 名称
	 * @param registry 定义将注册到的 Bean 工厂（用于检查已有 Bean 名称）
	 * @return 要使用的唯一 Bean 名称
	 * @since 5.1
	 */
	public static String uniqueBeanName(String beanName, BeanDefinitionRegistry registry) {
		String id = beanName;
		int counter = -1;

		// 递增计数器直至 id 唯一
		String prefix = beanName + GENERATED_BEAN_NAME_SEPARATOR;
		while (counter == -1 || registry.containsBeanDefinition(id)) {
			counter++;
			id = prefix + counter;
		}
		return id;
	}

	/**
	 * 将给定 Bean 定义注册到给定 Bean 工厂。
	 * @param definitionHolder 包含名称与别名的 Bean 定义
	 * @param registry 要注册到的 Bean 工厂
	 * @throws BeanDefinitionStoreException 注册失败时
	 */
	public static void registerBeanDefinition(
			BeanDefinitionHolder definitionHolder, BeanDefinitionRegistry registry)
			throws BeanDefinitionStoreException {

		// 以主名称注册 Bean 定义
		String beanName = definitionHolder.getBeanName();
		registry.registerBeanDefinition(beanName, definitionHolder.getBeanDefinition());

		// 注册 Bean 名称的别名（若有）
		String[] aliases = definitionHolder.getAliases();
		if (aliases != null) {
			for (String alias : aliases) {
				registry.registerAlias(beanName, alias);
			}
		}
	}

	/**
	 * 使用在给定 Bean 工厂内唯一的生成名称注册给定 Bean 定义。
	 * @param definition 要生成名称并注册的 Bean 定义
	 * @param registry 要注册到的 Bean 工厂
	 * @return 生成的 Bean 名称
	 * @throws BeanDefinitionStoreException 无法为给定 Bean 定义生成唯一名称，或无法注册定义时
	 */
	public static String registerWithGeneratedName(
			AbstractBeanDefinition definition, BeanDefinitionRegistry registry)
			throws BeanDefinitionStoreException {

		String generatedName = generateBeanName(definition, registry, false);
		registry.registerBeanDefinition(generatedName, definition);
		return generatedName;
	}

}
