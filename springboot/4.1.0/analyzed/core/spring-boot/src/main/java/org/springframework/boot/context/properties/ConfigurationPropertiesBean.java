/*
 * Copyright 2012-present the original author or authors.
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

package org.springframework.boot.context.properties;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.boot.context.properties.bind.BindConstructorProvider;
import org.springframework.boot.context.properties.bind.BindMethod;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.core.annotation.MergedAnnotations.SearchStrategy;
import org.springframework.lang.Contract;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;

/**
 * 提供对 {@link ConfigurationProperties @ConfigurationProperties} Bean 详情的访问，
 * 无论注解是直接标注在类上还是 {@link Bean @Bean} 工厂方法上。
 * 可用于访问 ApplicationContext 中 {@link #getAll(ApplicationContext) 全部}
 * 配置属性 Bean，或在 {@link BeanPostProcessor} 等场景下
 * {@link #get(ApplicationContext, Object, String) 逐个} 访问。
 *
 * @author Phillip Webb
 * @since 2.2.0
 * @see #getAll(ApplicationContext)
 * @see #get(ApplicationContext, Object, String)
 */
public final class ConfigurationPropertiesBean {

	private final String name;

	private final @Nullable Object instance;

	private final Bindable<?> bindTarget;

	private ConfigurationPropertiesBean(String name, @Nullable Object instance, Bindable<?> bindTarget) {
		this.name = name;
		this.instance = instance;
		this.bindTarget = bindTarget;
	}

	/**
	 * 返回 Spring Bean 的名称。
	 *
	 * @return Bean 名称
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * 返回实际的 Spring Bean 实例。
	 *
	 * @return Bean 实例
	 */
	public @Nullable Object getInstance() {
		return this.instance;
	}

	/**
	 * 返回 Bean 类型。
	 *
	 * @return Bean 类型
	 */
	Class<?> getType() {
		Class<?> resolved = this.bindTarget.getType().resolve();
		Assert.state(resolved != null, "'resolved' must not be null");
		return resolved;
	}

	/**
	 * 返回 Bean 的 {@link ConfigurationProperties} 注解。
	 * 注解可能定义在 Bean 本身，或创建 Bean 的工厂方法（通常是 {@link Bean @Bean} 方法）上。
	 *
	 * @return 配置属性注解
	 */
	public ConfigurationProperties getAnnotation() {
		ConfigurationProperties annotation = this.bindTarget.getAnnotation(ConfigurationProperties.class);
		Assert.state(annotation != null, "'annotation' must not be null");
		return annotation;
	}

	/**
	 * 返回可用作 {@link Binder} 绑定目标的 {@link Bindable} 实例。
	 *
	 * @return 供 {@link Binder} 使用的绑定目标
	 */
	public Bindable<?> asBindTarget() {
		return this.bindTarget;
	}

	/**
	 * 返回给定应用上下文中所有 {@link ConfigurationProperties @ConfigurationProperties} Bean。
	 * 包括直接标注的 Bean 以及工厂方法上标注 {@link ConfigurationProperties @ConfigurationProperties} 的 Bean。
	 *
	 * @param applicationContext 源应用上下文
	 * @return 以 Bean 名称为键的所有配置属性 Bean 映射
	 */
	public static Map<String, ConfigurationPropertiesBean> getAll(ApplicationContext applicationContext) {
		Assert.notNull(applicationContext, "'applicationContext' must not be null");
		if (applicationContext instanceof ConfigurableApplicationContext configurableContext) {
			return getAll(configurableContext);
		}
		Map<String, ConfigurationPropertiesBean> propertiesBeans = new LinkedHashMap<>();
		applicationContext.getBeansWithAnnotation(ConfigurationProperties.class).forEach((name, instance) -> {
			ConfigurationPropertiesBean propertiesBean = get(applicationContext, instance, name);
			if (propertiesBean != null) {
				propertiesBeans.put(name, propertiesBean);
			}
		});
		return propertiesBeans;
	}

	private static Map<String, ConfigurationPropertiesBean> getAll(ConfigurableApplicationContext applicationContext) {
		Map<String, ConfigurationPropertiesBean> propertiesBeans = new LinkedHashMap<>();
		ConfigurableListableBeanFactory beanFactory = applicationContext.getBeanFactory();
		Iterator<String> beanNames = beanFactory.getBeanNamesIterator();
		while (beanNames.hasNext()) {
			String beanName = beanNames.next();
			if (isConfigurationPropertiesBean(beanFactory, beanName)) {
				try {
					Object bean = beanFactory.getBean(beanName);
					ConfigurationPropertiesBean propertiesBean = get(applicationContext, bean, beanName);
					if (propertiesBean != null) {
						propertiesBeans.put(beanName, propertiesBean);
					}
				}
				catch (Exception ex) {
					// Ignore
				}
			}
		}
		return propertiesBeans;
	}

	private static boolean isConfigurationPropertiesBean(ConfigurableListableBeanFactory beanFactory, String beanName) {
		try {
			if (beanFactory.getBeanDefinition(beanName).isAbstract()) {
				return false;
			}
			if (beanFactory.findAnnotationOnBean(beanName, ConfigurationProperties.class) != null) {
				return true;
			}
			Method factoryMethod = findFactoryMethod(beanFactory, beanName);
			return findMergedAnnotation(factoryMethod, ConfigurationProperties.class).isPresent();
		}
		catch (NoSuchBeanDefinitionException ex) {
			return false;
		}
	}

	/**
	 * 为给定 Bean 详情返回 {@link ConfigurationPropertiesBean} 实例；
	 * 若 Bean 不是 {@link ConfigurationProperties @ConfigurationProperties} 对象则返回 {@code null}。
	 * 会检查 Bean 本身及工厂方法（如 {@link Bean @Bean} 方法）上的注解。
	 *
	 * @param applicationContext 源应用上下文
	 * @param bean 待考虑的 Bean
	 * @param beanName Bean 名称
	 * @return 配置属性 Bean，若 Bean 与工厂方法均未标注
	 * {@link ConfigurationProperties @ConfigurationProperties} 则为 {@code null}
	 */
	public static @Nullable ConfigurationPropertiesBean get(ApplicationContext applicationContext, Object bean,
			String beanName) {
		Method factoryMethod = findFactoryMethod(applicationContext, beanName);
		Bindable<Object> bindTarget = createBindTarget(bean, bean.getClass(), factoryMethod);
		if (bindTarget == null) {
			return null;
		}
		bindTarget = bindTarget.withBindMethod(BindMethodAttribute.get(applicationContext, beanName));
		if (bindTarget.getBindMethod() == null && factoryMethod != null) {
			bindTarget = bindTarget.withBindMethod(BindMethod.JAVA_BEAN);
		}
		if (bindTarget.getBindMethod() != BindMethod.VALUE_OBJECT) {
			bindTarget = bindTarget.withExistingValue(bean);
		}
		return create(beanName, bean, bindTarget);
	}

	private static @Nullable Method findFactoryMethod(ApplicationContext applicationContext, String beanName) {
		if (applicationContext instanceof ConfigurableApplicationContext configurableContext) {
			return findFactoryMethod(configurableContext, beanName);
		}
		return null;
	}

	private static @Nullable Method findFactoryMethod(ConfigurableApplicationContext applicationContext,
			String beanName) {
		return findFactoryMethod(applicationContext.getBeanFactory(), beanName);
	}

	private static @Nullable Method findFactoryMethod(ConfigurableListableBeanFactory beanFactory, String beanName) {
		if (beanFactory.containsBeanDefinition(beanName)) {
			BeanDefinition beanDefinition = beanFactory.getMergedBeanDefinition(beanName);
			if (beanDefinition instanceof RootBeanDefinition rootBeanDefinition) {
				return rootBeanDefinition.getResolvedFactoryMethod();
			}
		}
		return null;
	}

	static ConfigurationPropertiesBean forValueObject(Class<?> beanType, String beanName) {
		Bindable<Object> bindTarget = createBindTarget(null, beanType, null);
		Assert.state(bindTarget != null && deduceBindMethod(bindTarget) == BindMethod.VALUE_OBJECT,
				() -> "Bean '" + beanName + "' is not a @ConfigurationProperties value object");
		return create(beanName, null, bindTarget.withBindMethod(BindMethod.VALUE_OBJECT));
	}

	private static @Nullable Bindable<Object> createBindTarget(@Nullable Object bean, Class<?> beanType,
			@Nullable Method factoryMethod) {
		ResolvableType type = (factoryMethod != null) ? ResolvableType.forMethodReturnType(factoryMethod)
				: ResolvableType.forClass(beanType);
		Annotation[] annotations = findAnnotations(bean, beanType, factoryMethod);
		return (annotations != null) ? Bindable.of(type).withAnnotations(annotations) : null;
	}

	private static Annotation @Nullable [] findAnnotations(@Nullable Object instance, Class<?> type,
			@Nullable Method factory) {
		ConfigurationProperties annotation = findAnnotation(instance, type, factory, ConfigurationProperties.class);
		if (annotation == null) {
			return null;
		}
		Validated validated = findAnnotation(instance, type, factory, Validated.class);
		return (validated != null) ? new Annotation[] { annotation, validated } : new Annotation[] { annotation };
	}

	private static <A extends Annotation> @Nullable A findAnnotation(@Nullable Object instance, Class<?> type,
			@Nullable Method factory, Class<A> annotationType) {
		MergedAnnotation<A> annotation = MergedAnnotation.missing();
		if (factory != null) {
			annotation = findMergedAnnotation(factory, annotationType);
		}
		if (!annotation.isPresent()) {
			annotation = findMergedAnnotation(type, annotationType);
		}
		if (!annotation.isPresent() && AopUtils.isAopProxy(instance)) {
			annotation = MergedAnnotations.from(AopUtils.getTargetClass(instance), SearchStrategy.TYPE_HIERARCHY)
				.get(annotationType);
		}
		return annotation.isPresent() ? annotation.synthesize() : null;
	}

	private static <A extends Annotation> MergedAnnotation<A> findMergedAnnotation(@Nullable AnnotatedElement element,
			Class<A> annotationType) {
		return (element != null) ? MergedAnnotations.from(element, SearchStrategy.TYPE_HIERARCHY).get(annotationType)
				: MergedAnnotation.missing();
	}

	@Contract("_, _, !null -> !null")
	private static @Nullable ConfigurationPropertiesBean create(String name, @Nullable Object instance,
			@Nullable Bindable<Object> bindTarget) {
		return (bindTarget != null) ? new ConfigurationPropertiesBean(name, instance, bindTarget) : null;
	}

	/**
	 * 推断给定类型应使用的 {@code BindMethod}。
	 *
	 * @param type 源类型
	 * @return 要使用的绑定方法
	 */
	static BindMethod deduceBindMethod(Class<?> type) {
		return deduceBindMethod(BindConstructorProvider.DEFAULT.getBindConstructor(type, false));
	}

	/**
	 * 推断给定 {@link Bindable} 应使用的 {@code BindMethod}。
	 *
	 * @param bindable 源 Bindable
	 * @return 要使用的绑定方法
	 */
	static BindMethod deduceBindMethod(Bindable<Object> bindable) {
		return deduceBindMethod(BindConstructorProvider.DEFAULT.getBindConstructor(bindable, false));
	}

	private static BindMethod deduceBindMethod(@Nullable Constructor<?> bindConstructor) {
		return (bindConstructor != null) ? BindMethod.VALUE_OBJECT : BindMethod.JAVA_BEAN;
	}

}
