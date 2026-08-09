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

import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.core.type.MethodMetadata;

/**
 * {@link BeanNameGenerator} 的扩展变体，面向 {@link Configuration @Configuration} 类场景，
 * 不仅覆盖组件类与配置类自身的 Bean 名称生成，也覆盖 {@link Bean @Bean} 方法。
 *
 * @author Juergen Hoeller
 * @author Stephane Nicoll
 * @since 7.0
 * @see AnnotationConfigApplicationContext#setBeanNameGenerator
 * @see AnnotationConfigUtils#CONFIGURATION_BEAN_NAME_GENERATOR
 */
public interface ConfigurationBeanNameGenerator extends BeanNameGenerator {

	/**
	 * 为给定 {@link Bean @Bean} 方法推导默认 Bean 名称，同时考虑指定的 {@link Bean#name() name} 属性。
	 * @param beanMethod {@link Bean @Bean} 方法的方法元数据
	 * @param beanName {@link Bean#name() name} 属性值；若未指定则为 {@code null}
	 * @return 要使用的默认 Bean 名称
	 */
	String deriveBeanName(MethodMetadata beanMethod, @Nullable String beanName);

}
