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

package org.springframework.boot.autoconfigure;

import java.util.function.Supplier;

import org.springframework.aot.AotDetector;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.aot.BeanRegistrationExcludeFilter;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.RegisteredBean;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.context.annotation.ConfigurationClassPostProcessor;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReaderFactory;

/**
 * {@link ApplicationContextInitializer}，在 {@link ConfigurationClassPostProcessor}
 * 与 Spring Boot 之间创建共享的 {@link CachingMetadataReaderFactory}。
 *
 * @author Phillip Webb
 * @author Dave Syer
 */
class SharedMetadataReaderFactoryContextInitializer implements
		ApplicationContextInitializer<ConfigurableApplicationContext>, Ordered, BeanRegistrationExcludeFilter {

	/** 共享 {@link CachingMetadataReaderFactory} bean 的名称。 */
	public static final String BEAN_NAME = "org.springframework.boot.autoconfigure."
			+ "internalCachingMetadataReaderFactory";

	@Override
	public void initialize(ConfigurableApplicationContext applicationContext) {
		// AOT 生成产物模式下无需注册共享工厂
		if (AotDetector.useGeneratedArtifacts()) {
			return;
		}
		BeanFactoryPostProcessor postProcessor = new CachingMetadataReaderFactoryPostProcessor(applicationContext);
		applicationContext.addBeanFactoryPostProcessor(postProcessor);
	}

	@Override
	public int getOrder() {
		return 0;
	}

	@Override
	public boolean isExcludedFromAotProcessing(RegisteredBean registeredBean) {
		return BEAN_NAME.equals(registeredBean.getBeanName());
	}

	/**
	 * 注册 {@link CachingMetadataReaderFactory} 并配置
	 * {@link ConfigurationClassPostProcessor} 的 {@link BeanDefinitionRegistryPostProcessor}。
	 */
	static class CachingMetadataReaderFactoryPostProcessor
			implements BeanDefinitionRegistryPostProcessor, PriorityOrdered {

		/** 关联的应用上下文。 */
		private final ConfigurableApplicationContext context;

		CachingMetadataReaderFactoryPostProcessor(ConfigurableApplicationContext context) {
			this.context = context;
		}

		@Override
		public int getOrder() {
			// 必须在 ConfigurationClassPostProcessor 创建之前执行
			return Ordered.HIGHEST_PRECEDENCE;
		}

		@Override
		public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		}

		@Override
		public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
			register(registry);
			configureConfigurationClassPostProcessor(registry);
		}

		private void register(BeanDefinitionRegistry registry) {
			if (!registry.containsBeanDefinition(BEAN_NAME)) {
				BeanDefinition definition = BeanDefinitionBuilder
					.rootBeanDefinition(SharedMetadataReaderFactoryBean.class, SharedMetadataReaderFactoryBean::new)
					.getBeanDefinition();
				registry.registerBeanDefinition(BEAN_NAME, definition);
			}
		}

		private void configureConfigurationClassPostProcessor(BeanDefinitionRegistry registry) {
			try {
				configureConfigurationClassPostProcessor(
						registry.getBeanDefinition(AnnotationConfigUtils.CONFIGURATION_ANNOTATION_PROCESSOR_BEAN_NAME));
			}
			catch (NoSuchBeanDefinitionException ex) {
				// 非注解驱动上下文，忽略
			}
		}

		private void configureConfigurationClassPostProcessor(BeanDefinition definition) {
			if (definition instanceof AbstractBeanDefinition abstractBeanDefinition) {
				configureConfigurationClassPostProcessor(abstractBeanDefinition);
				return;
			}
			configureConfigurationClassPostProcessor(definition.getPropertyValues());
		}

		private void configureConfigurationClassPostProcessor(AbstractBeanDefinition definition) {
			Supplier<?> instanceSupplier = definition.getInstanceSupplier();
			if (instanceSupplier != null) {
				definition.setInstanceSupplier(
						new ConfigurationClassPostProcessorCustomizingSupplier(this.context, instanceSupplier));
				return;
			}
			configureConfigurationClassPostProcessor(definition.getPropertyValues());
		}

		private void configureConfigurationClassPostProcessor(MutablePropertyValues propertyValues) {
			propertyValues.add("metadataReaderFactory", new RuntimeBeanReference(BEAN_NAME));
		}

	}

	/**
	 * 在 {@link ConfigurationClassPostProcessor} 首次创建时自定义其行为的 {@link Supplier}。
	 */
	static class ConfigurationClassPostProcessorCustomizingSupplier implements Supplier<Object> {

		private final ConfigurableApplicationContext context;

		private final Supplier<?> instanceSupplier;

		ConfigurationClassPostProcessorCustomizingSupplier(ConfigurableApplicationContext context,
				Supplier<?> instanceSupplier) {
			this.context = context;
			this.instanceSupplier = instanceSupplier;
		}

		@Override
		public Object get() {
			Object instance = this.instanceSupplier.get();
			if (instance instanceof ConfigurationClassPostProcessor postProcessor) {
				configureConfigurationClassPostProcessor(postProcessor);
			}
			return instance;
		}

		private void configureConfigurationClassPostProcessor(ConfigurationClassPostProcessor instance) {
			instance.setMetadataReaderFactory(this.context.getBean(BEAN_NAME, MetadataReaderFactory.class));
		}

	}

	/**
	 * 创建共享 {@link MetadataReaderFactory} 的 {@link FactoryBean}。
	 */
	static class SharedMetadataReaderFactoryBean implements FactoryBean<CachingMetadataReaderFactory>,
			ResourceLoaderAware, ApplicationListener<ContextRefreshedEvent> {

		/** 带缓存的元数据读取工厂实例。 */
		@SuppressWarnings("NullAway.Init")
		private CachingMetadataReaderFactory metadataReaderFactory;

		@Override
		public void setResourceLoader(ResourceLoader resourceLoader) {
			this.metadataReaderFactory = new CachingMetadataReaderFactory(resourceLoader);
		}

		@Override
		public CachingMetadataReaderFactory getObject() throws Exception {
			return this.metadataReaderFactory;
		}

		@Override
		public Class<?> getObjectType() {
			return CachingMetadataReaderFactory.class;
		}

		@Override
		public boolean isSingleton() {
			return true;
		}

		@Override
		public void onApplicationEvent(ContextRefreshedEvent event) {
			this.metadataReaderFactory.clearCache();
		}

	}

}
