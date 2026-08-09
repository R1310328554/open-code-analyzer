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

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;

import org.springframework.aop.framework.AopInfrastructureBean;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.context.event.EventListenerFactory;
import org.springframework.core.Conventions;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.Order;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.stereotype.Component;

/**
 * 用于识别与配置 {@link Configuration} 类的工具类。
 *
 * @author Chris Beams
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @author Stephane Nicoll
 * @since 6.0
 */
public abstract class ConfigurationClassUtils {

	/** full 模式：需 CGLIB 增强以代理 {@code @Bean} 方法。 */
	static final String CONFIGURATION_CLASS_FULL = "full";

	/** lite 模式：不代理 {@code @Bean} 方法。 */
	static final String CONFIGURATION_CLASS_LITE = "lite";

	/**
	 * 设为 {@link Boolean#TRUE} 时，表示给定 {@link BeanDefinition} 的 Bean 类
	 * 默认应作为 lite 模式的配置类候选。
	 * <p>例如，直接向 {@code ApplicationContext} 注册的类应始终视为配置类候选。
	 * @since 6.0.10
	 */
	static final String CANDIDATE_ATTRIBUTE =
			Conventions.getQualifiedAttributeName(ConfigurationClassPostProcessor.class, "candidate");

	/** BeanDefinition 上记录 full/lite 配置类模式的属性名。 */
	static final String CONFIGURATION_CLASS_ATTRIBUTE =
			Conventions.getQualifiedAttributeName(ConfigurationClassPostProcessor.class, "configurationClass");

	/** BeanDefinition 上记录 {@code @Order} 值的属性名。 */
	static final String ORDER_ATTRIBUTE =
			Conventions.getQualifiedAttributeName(ConfigurationClassPostProcessor.class, "order");


	private static final Log logger = LogFactory.getLog(ConfigurationClassUtils.class);

	/** 标识配置类候选的典型注解名称集合。 */
	private static final Set<String> candidateIndicators = Set.of(
			Component.class.getName(),
			ComponentScan.class.getName(),
			Import.class.getName(),
			ImportResource.class.getName());


	/**
	 * 为指定类初始化配置类代理（CGLIB 增强）。
	 * @param userClass 要初始化的配置类
	 */
	@SuppressWarnings("unused") // Used by AOT-optimized generated code
	public static Class<?> initializeConfigurationClass(Class<?> userClass) {
		Class<?> configurationClass = new ConfigurationClassEnhancer().enhance(userClass, null);
		Enhancer.registerStaticCallbacks(configurationClass, ConfigurationClassEnhancer.CALLBACKS);
		return configurationClass;
	}


	/**
	 * 检查给定 Bean 定义是否为配置类候选（或配置/组件类内声明的嵌套组件类，亦会自动注册），
	 * 并相应标记。
	 * @param beanDef 待检查的 Bean 定义
	 * @param metadataReaderFactory 调用方当前使用的元数据读取器工厂
	 * @return 候选是否符合（任意类型的）配置类
	 */
	static boolean checkConfigurationClassCandidate(
			BeanDefinition beanDef, MetadataReaderFactory metadataReaderFactory) {

		String className = beanDef.getBeanClassName();
		if (className == null || beanDef.getFactoryMethodName() != null) {
			return false;
		}

		AnnotationMetadata metadata;
		if (beanDef instanceof AnnotatedBeanDefinition annotatedBd &&
				className.equals(annotatedBd.getMetadata().getClassName())) {
			// 可复用 BeanDefinition 中已解析的元数据
			metadata = annotatedBd.getMetadata();
		}
		else if (beanDef instanceof AbstractBeanDefinition abstractBd && abstractBd.hasBeanClass()) {
			// 若 Class 已加载，直接检查（可能无法加载类文件）
			Class<?> beanClass = abstractBd.getBeanClass();
			if (BeanFactoryPostProcessor.class.isAssignableFrom(beanClass) ||
					BeanPostProcessor.class.isAssignableFrom(beanClass) ||
					AopInfrastructureBean.class.isAssignableFrom(beanClass) ||
					EventListenerFactory.class.isAssignableFrom(beanClass)) {
				return false;
			}
			metadata = AnnotationMetadata.introspect(beanClass);
		}
		else {
			try {
				MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(className);
				metadata = metadataReader.getAnnotationMetadata();
			}
			catch (IOException ex) {
				if (logger.isDebugEnabled()) {
					logger.debug("Could not find class file for introspecting configuration annotations: " +
							className, ex);
				}
				return false;
			}
		}

		Map<String, @Nullable Object> config = metadata.getAnnotationAttributes(Configuration.class.getName());
		if (config != null && !Boolean.FALSE.equals(config.get("proxyBeanMethods"))) {
			beanDef.setAttribute(CONFIGURATION_CLASS_ATTRIBUTE, CONFIGURATION_CLASS_FULL);
		}
		else if (config != null || Boolean.TRUE.equals(beanDef.getAttribute(CANDIDATE_ATTRIBUTE)) ||
				isConfigurationCandidate(metadata)) {
			beanDef.setAttribute(CONFIGURATION_CLASS_ATTRIBUTE, CONFIGURATION_CLASS_LITE);
		}
		else {
			return false;
		}

		// 已是 full/lite 候选：解析并记录 order 值（若有）
		Integer order = getOrder(metadata);
		if (order != null) {
			beanDef.setAttribute(ORDER_ATTRIBUTE, order);
		}

		return true;
	}

	/**
	 * 检查给定元数据是否为配置类候选（或配置/组件类内声明的嵌套组件类）。
	 * @param metadata 带注解类的元数据
	 * @return 若该类应参与配置类处理则为 {@code true}，否则为 {@code false}
	 */
	static boolean isConfigurationCandidate(AnnotationMetadata metadata) {
		// 接口与纯注解类型不考虑
		if (metadata.isInterface()) {
			return false;
		}

		// 是否带有典型指示注解？
		for (String indicator : candidateIndicators) {
			if (metadata.isAnnotated(indicator)) {
				return true;
			}
		}

		// 最后检查是否存在 @Bean 方法
		return hasBeanMethods(metadata);
	}

	/** 元数据是否声明了 @Bean 方法。 */
	static boolean hasBeanMethods(AnnotationMetadata metadata) {
		try {
			return metadata.hasAnnotatedMethods(Bean.class.getName());
		}
		catch (Throwable ex) {
			if (logger.isDebugEnabled()) {
				logger.debug("Failed to introspect @Bean methods on class [" + metadata.getClassName() + "]: " + ex);
			}
			return false;
		}
	}

	/**
	 * 确定给定配置类元数据的 order 值。
	 * @param metadata 带注解类的元数据
	 * @return 配置类上 {@code @Order} 注解的值；未声明则返回 {@code Ordered.LOWEST_PRECEDENCE}
	 * @since 5.0
	 */
	public static @Nullable Integer getOrder(AnnotationMetadata metadata) {
		Map<String, @Nullable Object> orderAttributes = metadata.getAnnotationAttributes(Order.class.getName());
		return (orderAttributes != null ? ((Integer) orderAttributes.get(AnnotationUtils.VALUE)) : null);
	}

	/**
	 * 确定给定配置类 Bean 定义的 order 值（由 {@link #checkConfigurationClassCandidate} 设置）。
	 * @param beanDef 待检查的 Bean 定义
	 * @return 配置类上 {@link Order @Order} 注解的值，未声明则为 {@link Ordered#LOWEST_PRECEDENCE}
	 * @since 4.2
	 */
	public static int getOrder(BeanDefinition beanDef) {
		Integer order = (Integer) beanDef.getAttribute(ORDER_ATTRIBUTE);
		return (order != null ? order : Ordered.LOWEST_PRECEDENCE);
	}

}
