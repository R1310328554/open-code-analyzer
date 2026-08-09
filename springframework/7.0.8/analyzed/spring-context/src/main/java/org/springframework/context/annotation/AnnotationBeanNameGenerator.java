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
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.core.annotation.AliasFor;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotation.Adapt;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

/**
 * 针对带有 {@link org.springframework.stereotype.Component @Component} 注解
 * 或其元注解带有 {@code @Component} 的 Bean 类（例如 Spring 的
 * {@link org.springframework.stereotype.Repository @Repository} 等构造型注解）
 * 的 {@link BeanNameGenerator} 实现。
 *
 * <p>若可用，也支持 JSR-330 的 {@link jakarta.inject.Named} 注解。
 * 注意 Spring 组件注解始终覆盖此类标准注解。
 *
 * <p>若注解的 value 未指定 Bean 名称，则基于类的短名称构建适当名称
 * （首字母小写），除非前两个字母均为大写。例如：
 *
 * <pre class="code">com.xyz.FooServiceImpl -&gt; fooServiceImpl</pre>
 * <pre class="code">com.xyz.URLFooServiceImpl -&gt; URLFooServiceImpl</pre>
 *
 * @author Juergen Hoeller
 * @author Mark Fisher
 * @author Sam Brannen
 * @since 2.5
 * @see org.springframework.stereotype.Component#value()
 * @see org.springframework.stereotype.Repository#value()
 * @see org.springframework.stereotype.Service#value()
 * @see org.springframework.stereotype.Controller#value()
 * @see jakarta.inject.Named#value()
 * @see FullyQualifiedAnnotationBeanNameGenerator
 */
public class AnnotationBeanNameGenerator implements BeanNameGenerator {

	/**
	 * 默认 {@code AnnotationBeanNameGenerator} 实例的便捷常量，用于组件扫描。
	 * @since 5.2
	 */
	public static final AnnotationBeanNameGenerator INSTANCE = new AnnotationBeanNameGenerator();

	/** {@code @Component} 注解的全限定类名。 */
	private static final String COMPONENT_ANNOTATION_CLASSNAME = "org.springframework.stereotype.Component";

	/** MergedAnnotation 属性适配选项。 */
	private static final Adapt[] ADAPTATIONS = Adapt.values(false, true);


	private static final Log logger = LogFactory.getLog(AnnotationBeanNameGenerator.class);

	/**
	 * 用于跟踪已检查过是否在 {@code @Component} 的 {@code value}
	 * 属性上使用基于约定覆盖的构造型注解。
	 * @since 6.1
	 * @see #determineBeanNameFromAnnotation(AnnotatedBeanDefinition)
	 */
	private static final Set<String> conventionBasedStereotypeCheckCache = ConcurrentHashMap.newKeySet();

	/** 注解类型到其元注解类型集合的缓存。 */
	private final Map<String, Set<String>> metaAnnotationTypesCache = new ConcurrentHashMap<>();


	@Override
	public String generateBeanName(BeanDefinition definition, BeanDefinitionRegistry registry) {
		if (definition instanceof AnnotatedBeanDefinition annotatedBeanDefinition) {
			String beanName = determineBeanNameFromAnnotation(annotatedBeanDefinition);
			if (StringUtils.hasText(beanName)) {
				// 找到显式 Bean 名称
				return beanName;
			}
		}
		// 回退：生成唯一默认 Bean 名称
		return buildDefaultBeanName(definition, registry);
	}

	/**
	 * 从类上的某个注解派生 Bean 名称。
	 * @param annotatedDef the annotation-aware bean definition
	 * @return the bean name, or {@code null} if none is found
	 */
	protected @Nullable String determineBeanNameFromAnnotation(AnnotatedBeanDefinition annotatedDef) {
		AnnotationMetadata metadata = annotatedDef.getMetadata();

		String beanName = getExplicitBeanName(metadata);
		if (beanName != null) {
			return beanName;
		}

		// 直接出现在目标类上的注解列表。
		// MergedAnnotation 未实现 equals()/hashCode()，故使用 List 和下方 visited Set。
		List<MergedAnnotation<Annotation>> mergedAnnotations = metadata.getAnnotations().stream()
				.filter(MergedAnnotation::isDirectlyPresent)
				.toList();

		Set<AnnotationAttributes> visited = new HashSet<>();

		for (MergedAnnotation<Annotation> mergedAnnotation : mergedAnnotations) {
			AnnotationAttributes attributes = mergedAnnotation.asAnnotationAttributes(ADAPTATIONS);
			if (visited.add(attributes)) {
				String annotationType = mergedAnnotation.getType().getName();
				Set<String> metaAnnotationTypes = this.metaAnnotationTypesCache.computeIfAbsent(annotationType,
						key -> getMetaAnnotationTypes(mergedAnnotation));
				if (isStereotypeWithNameValue(annotationType, metaAnnotationTypes, attributes)) {
					Object value = attributes.get(MergedAnnotation.VALUE);
					if (value instanceof String currentName && !currentName.isBlank() &&
							!hasExplicitlyAliasedValueAttribute(mergedAnnotation.getType())) {
						// 基于约定的 @Component 名称已弃用，发出警告
						if (conventionBasedStereotypeCheckCache.add(annotationType) &&
								metaAnnotationTypes.contains(COMPONENT_ANNOTATION_CLASSNAME) && logger.isWarnEnabled()) {
							logger.warn("""
									Support for convention-based @Component names is deprecated and will \
									be removed in a future version of the framework. Please annotate the \
									'value' attribute in @%s with @AliasFor(annotation=Component.class) \
									to declare an explicit alias for @Component's 'value' attribute."""
										.formatted(annotationType));
						}
						if (beanName != null && !currentName.equals(beanName)) {
							throw new IllegalStateException("Stereotype annotations suggest inconsistent " +
									"component names: '" + beanName + "' versus '" + currentName + "'");
						}
						beanName = currentName;
					}
				}
			}
		}
		return beanName;
	}

	/** 收集注解类型上的元注解类型名称。 */
	private Set<String> getMetaAnnotationTypes(MergedAnnotation<Annotation> mergedAnnotation) {
		Set<String> result = MergedAnnotations.from(mergedAnnotation.getType()).stream()
				.map(metaAnnotation -> metaAnnotation.getType().getName())
				.collect(Collectors.toCollection(LinkedHashSet::new));
		return (result.isEmpty() ? Collections.emptySet() : result);
	}

	/**
	 * 获取底层类通过 {@link org.springframework.stereotype.Component @Component}
	 * 配置的显式 Bean 名称，并考虑 {@link org.springframework.core.annotation.AliasFor @AliasFor}
	 * 对 {@code @Component} 的 {@code value} 属性覆盖的语义。
	 * @param metadata the {@link AnnotationMetadata} for the underlying class
	 * @return the explicit bean name, or {@code null} if not found
	 * @since 6.1
	 * @see org.springframework.stereotype.Component#value()
	 */
	private @Nullable String getExplicitBeanName(AnnotationMetadata metadata) {
		List<String> names = metadata.getAnnotations().stream(COMPONENT_ANNOTATION_CLASSNAME)
				.map(annotation -> annotation.getString(MergedAnnotation.VALUE))
				.filter(StringUtils::hasText)
				.map(String::trim)
				.distinct()
				.toList();

		if (names.size() == 1) {
			return names.get(0);
		}
		if (names.size() > 1) {
			throw new IllegalStateException("Stereotype annotations suggest inconsistent component names: " + names);
		}
		return null;
	}

	/**
	 * 检查给定注解是否为允许通过 {@code value()} 属性建议组件名称的构造型。
	 * @param annotationType the name of the annotation class to check
	 * @param metaAnnotationTypes the names of meta-annotations on the given annotation
	 * @param attributes the map of attributes for the given annotation
	 * @return whether the annotation qualifies as a stereotype with component name
	 */
	protected boolean isStereotypeWithNameValue(String annotationType,
			Set<String> metaAnnotationTypes, Map<String, @Nullable Object> attributes) {

		boolean isStereotype = metaAnnotationTypes.contains(COMPONENT_ANNOTATION_CLASSNAME) ||
				annotationType.equals("jakarta.inject.Named");
		return (isStereotype && attributes.containsKey(MergedAnnotation.VALUE));
	}

	/**
	 * 从给定 Bean 定义派生默认 Bean 名称。
	 * <p>默认实现委托给 {@link #buildDefaultBeanName(BeanDefinition)}。
	 * @param definition the bean definition to build a bean name for
	 * @param registry the registry that the given bean definition is being registered with
	 * @return the default bean name (never {@code null})
	 */
	protected String buildDefaultBeanName(BeanDefinition definition, BeanDefinitionRegistry registry) {
		return buildDefaultBeanName(definition);
	}

	/**
	 * 从给定 Bean 定义派生默认 Bean 名称。
	 * <p>默认实现仅构建短类名的首字母小写版本：
	 * 例如 "mypackage.MyJdbcDao" &rarr; "myJdbcDao"。
	 * <p>注意，内部类名称形如 "outerClassName.InnerClassName"，
	 * 由于名称中的句点，按名称自动装配时可能有问题。
	 * @param definition the bean definition to build a bean name for
	 * @return the default bean name (never {@code null})
	 */
	protected String buildDefaultBeanName(BeanDefinition definition) {
		String beanClassName = definition.getBeanClassName();
		Assert.state(beanClassName != null, "No bean class name set");
		String shortClassName = ClassUtils.getShortName(beanClassName);
		return StringUtils.uncapitalizeAsProperty(shortClassName);
	}

	/**
	 * 判断提供的注解类型是否通过 {@link AliasFor @AliasFor} 配置了
	 * 显式别名的 {@code value()} 属性。
	 * @since 6.2.3
	 */
	private static boolean hasExplicitlyAliasedValueAttribute(Class<? extends Annotation> annotationType) {
		Method valueAttribute = ReflectionUtils.findMethod(annotationType, MergedAnnotation.VALUE);
		return (valueAttribute != null && valueAttribute.isAnnotationPresent(AliasFor.class));
	}

}
