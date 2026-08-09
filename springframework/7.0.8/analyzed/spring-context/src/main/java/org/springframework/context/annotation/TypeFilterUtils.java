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
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.AspectJTypeFilter;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.core.type.filter.RegexPatternTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.util.Assert;

/**
 * 处理 {@link ComponentScan @ComponentScan}
 * {@linkplain ComponentScan.Filter 类型过滤器} 的工具集合。
 *
 * @author Chris Beams
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 5.3.13
 * @see ComponentScan.Filter
 * @see org.springframework.core.type.filter.TypeFilter
 */
public abstract class TypeFilterUtils {

	/**
	 * 根据提供的 {@link AnnotationAttributes} 创建 {@linkplain TypeFilter 类型过滤器}，
	 * 例如来自 {@link ComponentScan#includeFilters()} 或 {@link ComponentScan#excludeFilters()}。
	 * <p>每个 {@link TypeFilter} 将通过合适的构造器实例化；若过滤器实现了
	 * {@code BeanClassLoaderAware}、{@code BeanFactoryAware}、{@code EnvironmentAware}
	 * 或 {@code ResourceLoaderAware} 契约，将调用相应回调。
	 * @param filterAttributes {@link ComponentScan.Filter @Filter} 声明的 {@code AnnotationAttributes}
	 * @param environment 提供给过滤器的 {@code Environment}
	 * @param resourceLoader 提供给过滤器的 {@code ResourceLoader}
	 * @param registry 在适用时作为 {@link org.springframework.beans.factory.BeanFactory}
	 * 提供给过滤器的 {@code BeanDefinitionRegistry}
	 * @return 已实例化并配置的类型过滤器列表
	 * @see TypeFilter
	 * @see AnnotationTypeFilter
	 * @see AssignableTypeFilter
	 * @see AspectJTypeFilter
	 * @see RegexPatternTypeFilter
	 * @see org.springframework.beans.factory.BeanClassLoaderAware
	 * @see org.springframework.beans.factory.BeanFactoryAware
	 * @see org.springframework.context.EnvironmentAware
	 * @see org.springframework.context.ResourceLoaderAware
	 */
	public static List<TypeFilter> createTypeFiltersFor(AnnotationAttributes filterAttributes, Environment environment,
			ResourceLoader resourceLoader, BeanDefinitionRegistry registry) {

		List<TypeFilter> typeFilters = new ArrayList<>();
		FilterType filterType = filterAttributes.getEnum("type");

		// 按类名配置的类型过滤器（注解、可赋值类型、自定义实现）
		for (Class<?> filterClass : filterAttributes.getClassArray("classes")) {
			switch (filterType) {
				case ANNOTATION -> {
					Assert.isAssignable(Annotation.class, filterClass,
							"@ComponentScan ANNOTATION type filter requires an annotation type");
					@SuppressWarnings("unchecked")
					Class<Annotation> annotationType = (Class<Annotation>) filterClass;
					typeFilters.add(new AnnotationTypeFilter(annotationType));
				}
				case ASSIGNABLE_TYPE -> typeFilters.add(new AssignableTypeFilter(filterClass));
				case CUSTOM -> {
					Assert.isAssignable(TypeFilter.class, filterClass,
							"@ComponentScan CUSTOM type filter requires a TypeFilter implementation");
					TypeFilter filter = ParserStrategyUtils.instantiateClass(filterClass, TypeFilter.class,
							environment, resourceLoader, registry);
					typeFilters.add(filter);
				}
				default ->
					throw new IllegalArgumentException("Filter type not supported with Class value: " + filterType);
			}
		}

		// 按字符串模式配置的类型过滤器（AspectJ 表达式或正则）
		for (String expression : filterAttributes.getStringArray("pattern")) {
			switch (filterType) {
				case ASPECTJ -> typeFilters.add(new AspectJTypeFilter(expression, resourceLoader.getClassLoader()));
				case REGEX -> typeFilters.add(new RegexPatternTypeFilter(Pattern.compile(expression)));
				default ->
					throw new IllegalArgumentException("Filter type not supported with String pattern: " + filterType);
			}
		}

		return typeFilters;
	}

}
