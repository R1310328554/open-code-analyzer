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

package org.springframework.context.annotation;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.core.type.AnnotationMetadata;

/**
 * 处理 @{@link Configuration} 类时注册额外 Bean 定义的类型应实现的接口。
 * 适用于希望在 Bean 定义级别（而非 {@code @Bean} 方法/实例级别）操作的场景。
 *
 * <p>与 {@code @Configuration} 和 {@link ImportSelector} 一样，本类型可提供给
 * @{@link Import} 注解（也可由 {@code ImportSelector} 返回）。
 *
 * <p>{@link ImportBeanDefinitionRegistrar} 可实现以下任意
 * {@link org.springframework.beans.factory.Aware Aware} 接口，
 * 其对应方法将在 {@link #registerBeanDefinitions} 之前调用：
 * <ul>
 * <li>{@link org.springframework.context.EnvironmentAware EnvironmentAware}</li>
 * <li>{@link org.springframework.beans.factory.BeanFactoryAware BeanFactoryAware}
 * <li>{@link org.springframework.beans.factory.BeanClassLoaderAware BeanClassLoaderAware}
 * <li>{@link org.springframework.context.ResourceLoaderAware ResourceLoaderAware}
 * </ul>
 *
 * <p>或者，该类可提供接受以下一种或多种受支持参数类型的单参构造函数：
 * <ul>
 * <li>{@link org.springframework.core.env.Environment Environment}</li>
 * <li>{@link org.springframework.beans.factory.BeanFactory BeanFactory}</li>
 * <li>{@link java.lang.ClassLoader ClassLoader}</li>
 * <li>{@link org.springframework.core.io.ResourceLoader ResourceLoader}</li>
 * </ul>
 *
 * <p>用法示例请参阅实现类及相关单元测试。
 *
 * @author Chris Beams
 * @author Juergen Hoeller
 * @since 3.1
 * @see Import
 * @see ImportSelector
 * @see Configuration
 */
public interface ImportBeanDefinitionRegistrar {

	/**
	 * 根据导入方 {@code @Configuration} 类的给定注解元数据，按需注册 Bean 定义。
	 * <p>注意：由于与 {@code @Configuration} 类处理相关的生命周期约束，
	 * 此处<em>不得</em>注册 {@link BeanDefinitionRegistryPostProcessor} 类型。
	 * <p>默认实现委托给
	 * {@link #registerBeanDefinitions(AnnotationMetadata, BeanDefinitionRegistry)}。
	 * @param importingClassMetadata 导入类的注解元数据
	 * @param registry 当前 Bean 定义注册表
	 * @param importBeanNameGenerator 导入 Bean 的名称生成策略：默认使用
	 * {@link ConfigurationClassPostProcessor#IMPORT_BEAN_NAME_GENERATOR}；
	 * 若已设置 {@link ConfigurationClassPostProcessor#setBeanNameGenerator}，
	 * 则使用用户提供的策略。后一种情况下，传入策略与包含应用上下文中组件扫描
	 * 使用的策略相同（否则默认组件扫描命名策略为
	 * {@link AnnotationBeanNameGenerator#INSTANCE}）。
	 * @since 5.2
	 * @see ConfigurationClassPostProcessor#IMPORT_BEAN_NAME_GENERATOR
	 * @see ConfigurationClassPostProcessor#setBeanNameGenerator
	 */
	default void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry,
			BeanNameGenerator importBeanNameGenerator) {

		registerBeanDefinitions(importingClassMetadata, registry);
	}

	/**
	 * 根据导入方 {@code @Configuration} 类的给定注解元数据，按需注册 Bean 定义。
	 * <p>注意：由于与 {@code @Configuration} 类处理相关的生命周期约束，
	 * 此处<em>不得</em>注册 {@link BeanDefinitionRegistryPostProcessor} 类型。
	 * <p>默认实现为空。
	 * @param importingClassMetadata 导入类的注解元数据
	 * @param registry 当前 Bean 定义注册表
	 */
	default void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
	}

}
