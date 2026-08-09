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

import java.util.function.Predicate;

import org.jspecify.annotations.Nullable;

import org.springframework.core.type.AnnotationMetadata;

/**
 * 根据给定选择条件（通常是一个或多个注解属性）决定应导入哪些
 * @{@link Configuration} 类的类型应实现的接口。
 *
 * <p>{@link ImportSelector} 可实现以下任意
 * {@link org.springframework.beans.factory.Aware Aware} 接口，
 * 其对应方法将在 {@link #selectImports} 之前调用：
 * <ul>
 * <li>{@link org.springframework.context.EnvironmentAware EnvironmentAware}</li>
 * <li>{@link org.springframework.beans.factory.BeanFactoryAware BeanFactoryAware}</li>
 * <li>{@link org.springframework.beans.factory.BeanClassLoaderAware BeanClassLoaderAware}</li>
 * <li>{@link org.springframework.context.ResourceLoaderAware ResourceLoaderAware}</li>
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
 * <p>{@code ImportSelector} 实现通常与普通 {@code @Import} 注解以相同方式处理，
 * 但也可将导入选择推迟到所有 {@code @Configuration} 类处理完毕之后
 * （详见 {@link DeferredImportSelector}）。
 *
 * @author Chris Beams
 * @author Juergen Hoeller
 * @since 3.1
 * @see DeferredImportSelector
 * @see Import
 * @see ImportBeanDefinitionRegistrar
 * @see Configuration
 */
public interface ImportSelector {

	/**
	 * 根据导入方 @{@link Configuration} 类的 {@link AnnotationMetadata}，
	 * 选择并返回应导入的类名。
	 * @param importingClassMetadata 导入类的注解元数据
	 * @return 类名数组；若无则返回空数组
	 */
	String[] selectImports(AnnotationMetadata importingClassMetadata);

	/**
	 * 返回用于从导入候选中排除类的谓词，将传递应用于通过本选择器导入发现的所有类。
	 * <p>若此谓词对给定全限定类名返回 {@code true}，则该类不会被视为导入的配置类，
	 * 从而跳过类文件加载和元数据内省。
	 * @return 传递导入的配置类候选全限定类名的过滤谓词；若无则返回 {@code null}
	 * @since 5.2.4
	 */
	default @Nullable Predicate<String> getExclusionFilter() {
		return null;
	}

}
