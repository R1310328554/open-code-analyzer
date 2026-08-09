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

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.SimpleTypeConverter;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.beans.factory.support.AutowireCandidateQualifier;
import org.springframework.beans.factory.support.AutowireCandidateResolver;
import org.springframework.beans.factory.support.GenericTypeAwareAutowireCandidateResolver;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

/**
 * {@link AutowireCandidateResolver} 实现：将 Bean 定义上的限定符与待自动装配字段/参数上的
 * {@link #addQualifierType(Class) 限定符注解} 进行匹配；同时通过
 * {@link #setValueAnnotationType(Class) value 注解} 支持建议的表达式取值。
 *
 * <p>若可用，也支持 JSR-330 的 {@link jakarta.inject.Qualifier} 注解。
 *
 * @author Mark Fisher
 * @author Juergen Hoeller
 * @author Stephane Nicoll
 * @author Sam Brannen
 * @since 2.5
 * @see AutowireCandidateQualifier
 * @see Qualifier
 * @see Value
 */
public class QualifierAnnotationAutowireCandidateResolver extends GenericTypeAwareAutowireCandidateResolver {

	/** 识别为限定符的注解类型集合 */
	private final Set<Class<? extends Annotation>> qualifierTypes = CollectionUtils.newLinkedHashSet(2);

	/** 表示默认值表达式的注解类型（默认为 {@link Value}） */
	private Class<? extends Annotation> valueAnnotationType = Value.class;


	/**
	 * 创建面向 Spring 标准 {@link Qualifier @Qualifier} 注解的
	 * {@code QualifierAnnotationAutowireCandidateResolver}。
	 * <p>若可用，也支持 JSR-330 的 {@link jakarta.inject.Qualifier} 注解。
	 */
	@SuppressWarnings("unchecked")
	public QualifierAnnotationAutowireCandidateResolver() {
		this.qualifierTypes.add(Qualifier.class);
		try {
			this.qualifierTypes.add((Class<? extends Annotation>) ClassUtils.forName("jakarta.inject.Qualifier",
							QualifierAnnotationAutowireCandidateResolver.class.getClassLoader()));
		}
		catch (ClassNotFoundException ex) {
			// 无 Jakarta EE 中的 JSR-330 API——直接跳过
		}
	}

	/**
	 * 为给定的限定符注解类型创建 {@code QualifierAnnotationAutowireCandidateResolver}。
	 * @param qualifierType 要查找的限定符注解
	 */
	public QualifierAnnotationAutowireCandidateResolver(Class<? extends Annotation> qualifierType) {
		Assert.notNull(qualifierType, "'qualifierType' must not be null");
		this.qualifierTypes.add(qualifierType);
	}

	/**
	 * 为给定的一组限定符注解类型创建 {@code QualifierAnnotationAutowireCandidateResolver}。
	 * @param qualifierTypes 要查找的限定符注解集合
	 */
	public QualifierAnnotationAutowireCandidateResolver(Set<Class<? extends Annotation>> qualifierTypes) {
		Assert.notNull(qualifierTypes, "'qualifierTypes' must not be null");
		this.qualifierTypes.addAll(qualifierTypes);
	}


	/**
	 * 注册在自动装配时用作限定符的注解类型。
	 * <p>既包括可直接使用的限定符注解（字段、方法参数、构造器参数），
	 * 也包括进而标识实际限定符注解的元注解。
	 * <p>本实现仅支持以注解作为限定符类型。
	 * 默认为 Spring 的 {@link Qualifier @Qualifier}，既可直接使用，也可作为元注解。
	 * @param qualifierType 要注册的注解类型
	 */
	public void addQualifierType(Class<? extends Annotation> qualifierType) {
		this.qualifierTypes.add(qualifierType);
	}

	/**
	 * 设置用于字段、方法参数与构造器参数的「value」注解类型。
	 * <p>默认的 value 注解类型是 Spring 提供的 {@link Value @Value}。
	 * <p>提供此 setter，便于开发者使用自定义（非 Spring 专属）注解
	 * 为特定参数声明默认值表达式。
	 */
	public void setValueAnnotationType(Class<? extends Annotation> valueAnnotationType) {
		this.valueAnnotationType = valueAnnotationType;
	}


	/**
	 * 判断给定的 Bean 定义是否为自动装配候选。
	 * <p>要被视为候选，Bean 的 <em>autowire-candidate</em> 属性不得设为 {@code false}。
	 * 此外，若待装配字段/参数上的注解被本 BeanFactory 识别为<em>限定符</em>，
	 * 则该 Bean 还必须与该注解及其属性匹配。Bean 定义须包含相同限定符，或通过元属性匹配。
	 * 若限定符或属性未能匹配，{@code value} 属性会回退到与 Bean 名称或别名匹配。
	 * @see Qualifier
	 */
	@Override
	public boolean isAutowireCandidate(BeanDefinitionHolder bdHolder, DependencyDescriptor descriptor) {
		if (!super.isAutowireCandidate(bdHolder, descriptor)) {
			return false;
		}
		Boolean checked = checkQualifiers(bdHolder, descriptor.getAnnotations());
		if (checked != Boolean.FALSE) {
			MethodParameter methodParam = descriptor.getMethodParameter();
			if (methodParam != null) {
				Method method = methodParam.getMethod();
				if (method == null || void.class == method.getReturnType()) {
					Boolean methodChecked = checkQualifiers(bdHolder, methodParam.getMethodAnnotations());
					if (methodChecked != null && checked == null) {
						checked = methodChecked;
					}
				}
			}
		}
		return (checked == Boolean.TRUE ||
				(checked == null && ((RootBeanDefinition) bdHolder.getBeanDefinition()).isDefaultCandidate()));
	}

	/**
	 * 将给定的限定符注解与候选 Bean 定义进行匹配。
	 * @return 找到限定符但未匹配则为 {@code false}；
	 * 找到并匹配则为 {@code true}；
	 * 完全未找到限定符则为 {@code null}
	 */
	protected @Nullable Boolean checkQualifiers(BeanDefinitionHolder bdHolder, Annotation[] annotationsToSearch) {
		boolean qualifierFound = false;
		if (!ObjectUtils.isEmpty(annotationsToSearch)) {
			SimpleTypeConverter typeConverter = new SimpleTypeConverter();
			for (Annotation annotation : annotationsToSearch) {
				Class<? extends Annotation> type = annotation.annotationType();
				if (isPlainJavaAnnotation(type)) {
					continue;
				}
				boolean checkMeta = true;
				boolean fallbackToMeta = false;
				if (isQualifier(type)) {
					qualifierFound = true;
					if (!checkQualifier(bdHolder, annotation, typeConverter)) {
						fallbackToMeta = true;
					}
					else {
						checkMeta = false;
					}
				}
				if (checkMeta) {
					boolean foundMeta = false;
					for (Annotation metaAnn : type.getAnnotations()) {
						Class<? extends Annotation> metaType = metaAnn.annotationType();
						if (isPlainJavaAnnotation(metaType)) {
							continue;
						}
						if (isQualifier(metaType)) {
							qualifierFound = true;
							foundMeta = true;
							// 仅当 @Qualifier 注解带有 value 时才接受回退匹配……
							// 否则它只是自定义限定符注解的标记
							if ((fallbackToMeta && ObjectUtils.isEmpty(AnnotationUtils.getValue(metaAnn))) ||
									!checkQualifier(bdHolder, metaAnn, typeConverter)) {
								return false;
							}
						}
					}
					if (fallbackToMeta && !foundMeta) {
						return false;
					}
				}
			}
		}
		return (qualifierFound ? true : null);
	}

	/**
	 * 判断给定注解类型是否为普通的 {@code java.} 注解
	 *（通常来自 {@code java.lang.annotation}）。
	 * <p>与
	 * {@code org.springframework.core.annotation.AnnotationsScanner#hasPlainJavaAnnotationsOnly}
	 * 对齐。
	 */
	private boolean isPlainJavaAnnotation(Class<? extends Annotation> annotationType) {
		return annotationType.getName().startsWith("java.");
	}

	/**
	 * 判断给定注解类型是否为已识别的限定符类型。
	 */
	protected boolean isQualifier(Class<? extends Annotation> annotationType) {
		for (Class<? extends Annotation> qualifierType : this.qualifierTypes) {
			if (annotationType.equals(qualifierType) || annotationType.isAnnotationPresent(qualifierType)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 将给定的限定符注解与候选 Bean 定义进行匹配。
	 */
	protected boolean checkQualifier(
			BeanDefinitionHolder bdHolder, Annotation annotation, TypeConverter typeConverter) {

		Class<? extends Annotation> type = annotation.annotationType();
		RootBeanDefinition bd = (RootBeanDefinition) bdHolder.getBeanDefinition();

		AutowireCandidateQualifier qualifier = bd.getQualifier(type.getName());
		if (qualifier == null) {
			qualifier = bd.getQualifier(ClassUtils.getShortName(type));
		}
		if (qualifier == null) {
			// 首先检查「限定元素」上的注解（若有）
			Annotation targetAnnotation = getQualifiedElementAnnotation(bd, type);
			// 然后检查工厂方法上的注解（若适用）
			if (targetAnnotation == null) {
				targetAnnotation = getFactoryMethodAnnotation(bd, type);
			}
			if (targetAnnotation == null) {
				RootBeanDefinition dbd = getResolvedDecoratedDefinition(bd);
				if (dbd != null) {
					targetAnnotation = getFactoryMethodAnnotation(dbd, type);
				}
			}
			if (targetAnnotation == null) {
				BeanFactory beanFactory = getBeanFactory();
				// 在目标类上查找匹配的注解
				if (beanFactory != null) {
					try {
						Class<?> beanType = beanFactory.getType(bdHolder.getBeanName());
						if (beanType != null) {
							targetAnnotation = AnnotationUtils.getAnnotation(ClassUtils.getUserClass(beanType), type);
						}
					}
					catch (NoSuchBeanDefinitionException ex) {
						// 非常规情况——忽略类型检查即可……
					}
				}
				if (targetAnnotation == null && bd.hasBeanClass()) {
					targetAnnotation = AnnotationUtils.getAnnotation(ClassUtils.getUserClass(bd.getBeanClass()), type);
				}
			}
			if (targetAnnotation != null && targetAnnotation.equals(annotation)) {
				return true;
			}
		}

		Map<String, @Nullable Object> attributes = AnnotationUtils.getAnnotationAttributes(annotation);
		if (attributes.isEmpty() && qualifier == null) {
			// 没有属性时，限定符本身必须存在
			return false;
		}
		for (Map.Entry<String, Object> entry : attributes.entrySet()) {
			String attributeName = entry.getKey();
			Object expectedValue = entry.getValue();
			Object actualValue = null;
			// 先检查限定符
			if (qualifier != null) {
				actualValue = qualifier.getAttribute(attributeName);
			}
			if (actualValue == null) {
				// 回退到 Bean 定义属性
				actualValue = bd.getAttribute(attributeName);
			}
			if (actualValue == null && attributeName.equals(AutowireCandidateQualifier.VALUE_KEY) &&
					expectedValue instanceof String name && bdHolder.matchesName(name)) {
				// 最后检查 Bean 名称（或别名）是否匹配
				continue;
			}
			if (actualValue == null && qualifier != null) {
				// 仅在限定符存在时回退到默认值
				actualValue = AnnotationUtils.getDefaultValue(annotation, attributeName);
			}
			if (actualValue != null) {
				actualValue = typeConverter.convertIfNecessary(actualValue, expectedValue.getClass());
			}
			if (!ObjectUtils.nullSafeEquals(expectedValue, actualValue)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 从 Bean 定义的「限定元素」上获取指定类型的注解。
	 */
	protected @Nullable Annotation getQualifiedElementAnnotation(RootBeanDefinition bd, Class<? extends Annotation> type) {
		AnnotatedElement qualifiedElement = bd.getQualifiedElement();
		return (qualifiedElement != null ? AnnotationUtils.getAnnotation(qualifiedElement, type) : null);
	}

	/**
	 * 从已解析的工厂方法上获取指定类型的注解。
	 */
	protected @Nullable Annotation getFactoryMethodAnnotation(RootBeanDefinition bd, Class<? extends Annotation> type) {
		Method resolvedFactoryMethod = bd.getResolvedFactoryMethod();
		return (resolvedFactoryMethod != null ? AnnotationUtils.getAnnotation(resolvedFactoryMethod, type) : null);
	}


	/**
	 * 判断给定依赖是否声明了自动装配注解，并检查其 {@code required} 标志。
	 * @see Autowired#required()
	 */
	@Override
	public boolean isRequired(DependencyDescriptor descriptor) {
		if (!super.isRequired(descriptor)) {
			return false;
		}

		for (Annotation ann : descriptor.getAnnotations()) {
			// 直接标注？
			if (ann instanceof Autowired autowired) {
				return autowired.required();
			}
			// 元标注？
			Autowired autowired = AnnotationUtils.findAnnotation(ann.annotationType(), Autowired.class);
			if (autowired != null) {
				return autowired.required();
			}
		}
		// 未出现 @Autowired：默认视为必需
		return true;
	}

	/**
	 * 判断给定依赖是否声明了限定符注解。
	 * @see #isQualifier(Class)
	 * @see Qualifier
	 */
	@Override
	public boolean hasQualifier(DependencyDescriptor descriptor) {
		for (Annotation annotation : descriptor.getAnnotations()) {
			if (isQualifier(annotation.annotationType())) {
				return true;
			}
		}
		MethodParameter methodParam = descriptor.getMethodParameter();
		if (methodParam != null) {
			Method method = methodParam.getMethod();
			if (method == null || void.class == method.getReturnType()) {
				for (Annotation annotation : methodParam.getMethodAnnotations()) {
					if (isQualifier(annotation.annotationType())) {
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	public @Nullable String getSuggestedName(DependencyDescriptor descriptor) {
		for (Annotation annotation : descriptor.getAnnotations()) {
			if (isQualifier(annotation.annotationType())) {
				Object value = AnnotationUtils.getValue(annotation);
				if (value instanceof String str) {
					return str;
				}
			}
		}
		return null;
	}

	/**
	 * 判断给定依赖是否声明了 value 注解，并返回其建议值。
	 * @see Value
	 */
	@Override
	public @Nullable Object getSuggestedValue(DependencyDescriptor descriptor) {
		Object value = findValue(descriptor.getAnnotations());
		if (value == null) {
			MethodParameter methodParam = descriptor.getMethodParameter();
			if (methodParam != null) {
				value = findValue(methodParam.getMethodAnnotations());
			}
		}
		return value;
	}

	/**
	 * 从给定候选注解中确定建议值。
	 */
	protected @Nullable Object findValue(Annotation[] annotationsToSearch) {
		if (annotationsToSearch.length > 0) {   // 限定符注解必须是本地声明的
			AnnotationAttributes attr = AnnotatedElementUtils.getMergedAnnotationAttributes(
					AnnotatedElementUtils.forAnnotations(annotationsToSearch), this.valueAnnotationType);
			if (attr != null) {
				return extractValue(attr);
			}
		}
		return null;
	}

	/**
	 * 从给定注解属性中提取 {@code value}。
	 * @since 4.3
	 */
	protected Object extractValue(AnnotationAttributes attr) {
		Object value = attr.get(AnnotationUtils.VALUE);
		if (value == null) {
			throw new IllegalStateException("Value annotation must have a value attribute");
		}
		return value;
	}

}
