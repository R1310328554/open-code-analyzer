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

package org.springframework.beans.factory.annotation;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.MethodMetadata;

/**
 * {@link org.springframework.beans.factory.config.BeanDefinition} 的扩展接口，
 * 可暴露 Bean 类上的 {@link org.springframework.core.type.AnnotationMetadata}，
 * 且不必先加载该类。
 *
 * @author Juergen Hoeller
 * @since 2.5
 * @see AnnotatedGenericBeanDefinition
 * @see org.springframework.core.type.AnnotationMetadata
 */
public interface AnnotatedBeanDefinition extends BeanDefinition {

	/**
	 * 获取本 Bean 定义所对应 Bean 类的注解元数据（以及基础类元数据）。
	 * @return 注解元数据对象（永不为 {@code null}）
	 */
	AnnotationMetadata getMetadata();

	/**
	 * 获取本 Bean 定义的工厂方法元数据（若有）。
	 * @return 工厂方法元数据；没有则为 {@code null}
	 * @since 4.1.1
	 */
	@Nullable MethodMetadata getFactoryMethodMetadata();

}
