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

import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.MethodMetadata;
import org.springframework.core.type.StandardAnnotationMetadata;
import org.springframework.util.Assert;

/**
 * {@link org.springframework.beans.factory.support.GenericBeanDefinition} 的扩展，
 * 通过 {@link AnnotatedBeanDefinition} 接口暴露注解元数据。
 *
 * <p>该 GenericBeanDefinition 变体主要用于测试：测试代码期望操作
 * {@code AnnotatedBeanDefinition}（例如组件扫描相关策略）。扫描侧默认的定义类是
 * {@link org.springframework.context.annotation.ScannedGenericBeanDefinition}，
 * 同样实现了 {@code AnnotatedBeanDefinition}。
 *
 * @author Juergen Hoeller
 * @author Chris Beams
 * @since 2.5
 * @see AnnotatedBeanDefinition#getMetadata()
 * @see org.springframework.core.type.StandardAnnotationMetadata
 */
@SuppressWarnings("serial")
public class AnnotatedGenericBeanDefinition extends GenericBeanDefinition implements AnnotatedBeanDefinition {

	/** Bean 类上的注解元数据 */
	private final AnnotationMetadata metadata;

	/** 工厂方法元数据（若通过工厂方法定义 Bean） */
	private @Nullable MethodMetadata factoryMethodMetadata;


	/**
	 * 根据给定的已加载 Bean 类创建 {@code AnnotatedGenericBeanDefinition}。
	 * @param beanClass 已加载的 Bean 类
	 */
	public AnnotatedGenericBeanDefinition(Class<?> beanClass) {
		setBeanClass(beanClass);
		this.metadata = AnnotationMetadata.introspect(beanClass);
	}

	/**
	 * 根据给定的注解元数据创建 {@code AnnotatedGenericBeanDefinition}，
	 * 支持基于 ASM 的处理，并可避免过早加载 Bean 类。
	 * <p>功能上与 {@link org.springframework.context.annotation.ScannedGenericBeanDefinition
	 * ScannedGenericBeanDefinition} 等价；后者语义上表示 Bean 是通过组件扫描发现的，
	 * 而本类不强调发现途径。
	 * @param metadata 目标 Bean 类的注解元数据
	 * @since 3.1.1
	 */
	public AnnotatedGenericBeanDefinition(AnnotationMetadata metadata) {
		Assert.notNull(metadata, "AnnotationMetadata must not be null");
		if (metadata instanceof StandardAnnotationMetadata sam) {
			setBeanClass(sam.getIntrospectedClass());
		}
		else {
			setBeanClassName(metadata.getClassName());
		}
		this.metadata = metadata;
	}

	/**
	 * 根据给定的注解元数据与工厂方法元数据创建 {@code AnnotatedGenericBeanDefinition}
	 *（基于带注解的类及其上的工厂方法）。
	 * @param metadata 目标 Bean 类的注解元数据
	 * @param factoryMethodMetadata 所选工厂方法的元数据
	 * @since 4.1.1
	 */
	public AnnotatedGenericBeanDefinition(AnnotationMetadata metadata, MethodMetadata factoryMethodMetadata) {
		this(metadata);
		Assert.notNull(factoryMethodMetadata, "MethodMetadata must not be null");
		setFactoryMethodName(factoryMethodMetadata.getMethodName());
		this.factoryMethodMetadata = factoryMethodMetadata;
	}


	@Override
	public final AnnotationMetadata getMetadata() {
		return this.metadata;
	}

	@Override
	public final @Nullable MethodMetadata getFactoryMethodMetadata() {
		return this.factoryMethodMetadata;
	}

}
