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

import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.annotation.AnnotatedGenericBeanDefinition;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.MethodMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.util.Assert;

/**
 * 基于 ASM {@code ClassReader} 的 {@link org.springframework.beans.factory.support.GenericBeanDefinition}
 * 扩展，通过 {@link AnnotatedBeanDefinition} 接口暴露注解元数据。
 *
 * <p>本类<i>不会</i>过早加载 Bean 的 {@code Class}，而是从 {@code .class} 文件本身
 * 解析全部相关元数据。功能上等价于
 * {@link AnnotatedGenericBeanDefinition#AnnotatedGenericBeanDefinition(AnnotationMetadata)}，
 * 但通过类型区分经<em>扫描</em>发现的 Bean 与通过其他方式注册或检测到的 Bean。
 *
 * @author Juergen Hoeller
 * @author Chris Beams
 * @since 2.5
 * @see #getMetadata()
 * @see #getBeanClassName()
 * @see org.springframework.core.type.classreading.MetadataReaderFactory
 * @see AnnotatedGenericBeanDefinition
 */
@SuppressWarnings("serial")
public class ScannedGenericBeanDefinition extends GenericBeanDefinition implements AnnotatedBeanDefinition {

	/** 从扫描目标类解析得到的注解元数据。 */
	private final AnnotationMetadata metadata;


	/**
	 * 为给定 {@link MetadataReader} 所描述的类创建新的 {@code ScannedGenericBeanDefinition}。
	 * @param metadataReader 扫描目标类的 MetadataReader
	 */
	public ScannedGenericBeanDefinition(MetadataReader metadataReader) {
		Assert.notNull(metadataReader, "MetadataReader must not be null");
		this.metadata = metadataReader.getAnnotationMetadata();
		setBeanClassName(this.metadata.getClassName());
		setResource(metadataReader.getResource());
	}


	@Override
	public final AnnotationMetadata getMetadata() {
		return this.metadata;
	}

	@Override
	public @Nullable MethodMetadata getFactoryMethodMetadata() {
		return null;
	}

}
