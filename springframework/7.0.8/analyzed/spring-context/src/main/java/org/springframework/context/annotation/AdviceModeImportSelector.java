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

import java.lang.annotation.Annotation;

import org.jspecify.annotations.Nullable;

import org.springframework.core.GenericTypeResolver;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.Assert;

/**
 * 基于注解（如 {@code @Enable*} 注解）中的 {@link AdviceMode} 值
 * 选择导入类的 {@link ImportSelector} 实现的便捷基类。
 *
 * @author Chris Beams
 * @since 3.1
 * @param <A> annotation containing {@linkplain #getAdviceModeAttributeName() AdviceMode attribute}
 */
public abstract class AdviceModeImportSelector<A extends Annotation> implements ImportSelector {


	/** 默认的 advice mode 属性名。 */
	public static final String DEFAULT_ADVICE_MODE_ATTRIBUTE_NAME = "mode";


	/**
	 * 泛型 {@code A} 所指定注解的 {@link AdviceMode} 属性名。
	 * 默认为 {@value #DEFAULT_ADVICE_MODE_ATTRIBUTE_NAME}，子类可覆盖以自定义。
	 */
	protected String getAdviceModeAttributeName() {
		return DEFAULT_ADVICE_MODE_ATTRIBUTE_NAME;
	}

	/**
	 * 本实现从泛型元数据解析注解类型，并验证：
	 * (a) 该注解确实存在于导入的 {@code @Configuration} 类上；
	 * (b) 给定注解具有类型为 {@link AdviceMode} 的
	 * {@linkplain #getAdviceModeAttributeName() advice mode 属性}。
	 * <p>随后调用 {@link #selectImports(AdviceMode)}，使具体实现
	 * 能以安全便捷的方式选择导入。
	 * @throws IllegalArgumentException if expected annotation {@code A} is not present
	 * on the importing {@code @Configuration} class or if {@link #selectImports(AdviceMode)}
	 * returns {@code null}
	 */
	@Override
	public final String[] selectImports(AnnotationMetadata importingClassMetadata) {
		// 从泛型参数解析注解类型 A
		Class<?> annType = GenericTypeResolver.resolveTypeArgument(getClass(), AdviceModeImportSelector.class);
		Assert.state(annType != null, "Unresolvable type argument for AdviceModeImportSelector");

		AnnotationAttributes attributes = AnnotationConfigUtils.attributesFor(importingClassMetadata, annType);
		if (attributes == null) {
			throw new IllegalArgumentException(String.format(
					"@%s is not present on importing class '%s' as expected",
					annType.getSimpleName(), importingClassMetadata.getClassName()));
		}

		AdviceMode adviceMode = attributes.getEnum(getAdviceModeAttributeName());
		String[] imports = selectImports(adviceMode);
		if (imports == null) {
			throw new IllegalArgumentException("Unknown AdviceMode: " + adviceMode);
		}
		return imports;
	}

	/**
	 * 根据给定的 {@code AdviceMode} 确定应导入哪些类。
	 * <p>从此方法返回 {@code null} 表示无法处理或未知的 {@code AdviceMode}，
	 * 应抛出 {@code IllegalArgumentException}。
	 * @param adviceMode the value of the {@linkplain #getAdviceModeAttributeName()
	 * advice mode attribute} for the annotation specified via generics.
	 * @return array containing classes to import (empty array if none;
	 * {@code null} if the given {@code AdviceMode} is unknown)
	 */
	protected abstract String @Nullable [] selectImports(AdviceMode adviceMode);

}
