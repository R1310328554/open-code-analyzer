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

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.util.Assert;

/**
 * {@code AnnotationBeanNameGenerator} 的扩展：若未通过 {@code @Component} 等受支持的
 * 类型级注解显式指定 Bean 名称，则默认使用全限定类名作为 Bean 名称（受支持注解详见
 * {@link AnnotationBeanNameGenerator}）。
 *
 * <p>若因多个自动检测到的组件具有相同的非限定类名（即类名相同但位于不同包）而导致命名冲突，
 * 应优先选用本命名策略而非 {@code AnnotationBeanNameGenerator}。
 * 若 {@link Bean @Bean} 方法也需要避免此类冲突，请考虑
 * {@link FullyQualifiedConfigurationBeanNameGenerator}。
 *
 * <p>注意：配置级导入场景默认使用本类实例；而组件扫描场景默认使用普通的
 * {@code AnnotationBeanNameGenerator}。
 *
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 5.2.3
 * @see org.springframework.beans.factory.support.DefaultBeanNameGenerator
 * @see AnnotationBeanNameGenerator
 * @see FullyQualifiedConfigurationBeanNameGenerator
 * @see ConfigurationClassPostProcessor#IMPORT_BEAN_NAME_GENERATOR
 */
public class FullyQualifiedAnnotationBeanNameGenerator extends AnnotationBeanNameGenerator {

	/**
	 * 默认 {@code FullyQualifiedAnnotationBeanNameGenerator} 实例的便捷常量，
	 * 用于配置级导入场景。
	 * @since 5.2.11
	 */
	public static final FullyQualifiedAnnotationBeanNameGenerator INSTANCE =
			new FullyQualifiedAnnotationBeanNameGenerator();


	@Override
	protected String buildDefaultBeanName(BeanDefinition definition) {
		String beanClassName = definition.getBeanClassName();
		Assert.state(beanClassName != null, "No bean class name set");
		return beanClassName;
	}

}
