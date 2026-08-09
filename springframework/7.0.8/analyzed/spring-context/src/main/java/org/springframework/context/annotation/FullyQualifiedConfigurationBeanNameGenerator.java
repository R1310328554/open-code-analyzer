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

import org.jspecify.annotations.Nullable;

import org.springframework.core.type.MethodMetadata;

/**
 * 面向 {@link Configuration @Configuration} 类用途的
 * {@link FullyQualifiedAnnotationBeanNameGenerator} 扩展变体：不仅对组件类和配置类本身
 * 强制使用全限定名称，还对 {@link Bean @Bean} 方法使用全限定默认 Bean 名称
 * （格式为 {@code "className.methodName"}）。默认情况下，这仅影响未显式指定
 * {@link Bean#name() name} 属性的方法。
 *
 * <p>这为 {@code @Bean} 方法的默认 Bean 名称生成（使用纯方法名）提供了替代方案，
 * 主要用于可能存在 Bean 名称重叠的大型应用。若预期 {@code @Bean} 方法会出现此类
 * 命名冲突，且应用不依赖 {@code @Bean} 方法名作为 Bean 名称，应优先选用本策略而非
 * {@code FullyQualifiedAnnotationBeanNameGenerator}。若名称确实重要，即使与方法名重复，
 * 也应显式声明 {@code @Bean("myBeanName")}。
 *
 * @author Juergen Hoeller
 * @since 7.0
 * @see AnnotationBeanNameGenerator
 * @see FullyQualifiedAnnotationBeanNameGenerator
 * @see AnnotationConfigApplicationContext#setBeanNameGenerator
 * @see AnnotationConfigUtils#CONFIGURATION_BEAN_NAME_GENERATOR
 */
public class FullyQualifiedConfigurationBeanNameGenerator extends FullyQualifiedAnnotationBeanNameGenerator
		implements ConfigurationBeanNameGenerator {

	/**
	 * 默认 {@code FullyQualifiedConfigurationBeanNameGenerator} 实例的便捷常量，
	 * 用于配置级导入场景。
	 */
	public static final FullyQualifiedConfigurationBeanNameGenerator INSTANCE =
			new FullyQualifiedConfigurationBeanNameGenerator();


	@Override
	public String deriveBeanName(MethodMetadata beanMethod, @Nullable String beanName) {
		return (beanName != null ? beanName : beanMethod.getDeclaringClassName() + "." + beanMethod.getMethodName());
	}

}
