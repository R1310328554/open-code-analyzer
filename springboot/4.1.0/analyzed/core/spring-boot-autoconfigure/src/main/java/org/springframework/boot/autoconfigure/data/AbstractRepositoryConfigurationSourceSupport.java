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

package org.springframework.boot.autoconfigure.data;

import java.lang.annotation.Annotation;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.data.repository.config.AnnotationRepositoryConfigurationSource;
import org.springframework.data.repository.config.BootstrapMode;
import org.springframework.data.repository.config.RepositoryConfigurationDelegate;
import org.springframework.data.repository.config.RepositoryConfigurationExtension;
import org.springframework.data.util.Streamable;

/**
 * 用于自动配置 Spring Data Repository 的
 * {@link ImportBeanDefinitionRegistrar} 基类。
 * <p>
 * 通过 {@link RepositoryConfigurationDelegate} 扫描并注册 Repository 接口，
 * 基包默认取自 {@link AutoConfigurationPackages}。
 *
 * @author Phillip Webb
 * @author Dave Syer
 * @author Oliver Gierke
 * @since 1.0.0
 */
public abstract class AbstractRepositoryConfigurationSourceSupport
		implements ImportBeanDefinitionRegistrar, BeanFactoryAware, ResourceLoaderAware, EnvironmentAware {

	@SuppressWarnings("NullAway.Init")
	private ResourceLoader resourceLoader;

	@SuppressWarnings("NullAway.Init")
	private BeanFactory beanFactory;

	@SuppressWarnings("NullAway.Init")
	private Environment environment;

	@Override
	public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry,
			@Nullable BeanNameGenerator importBeanNameGenerator) {
		RepositoryConfigurationDelegate delegate = new RepositoryConfigurationDelegate(
				getConfigurationSource(registry, importBeanNameGenerator), this.resourceLoader, this.environment);
		delegate.registerRepositoriesIn(registry, getRepositoryConfigurationExtension());
	}

	@Override
	public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
		registerBeanDefinitions(importingClassMetadata, registry, null);
	}

	private AnnotationRepositoryConfigurationSource getConfigurationSource(BeanDefinitionRegistry registry,
			@Nullable BeanNameGenerator importBeanNameGenerator) {
		AnnotationMetadata metadata = AnnotationMetadata.introspect(getConfiguration());
		return new AutoConfiguredAnnotationRepositoryConfigurationSource(metadata, getAnnotation(), this.resourceLoader,
				this.environment, registry, importBeanNameGenerator) {
		};
	}

	protected Streamable<String> getBasePackages() {
		return Streamable.of(AutoConfigurationPackages.get(this.beanFactory));
	}

	/**
	 * 启用特定 Repository 支持的 Spring Data 注解类型。
	 *
	 * @return 注解类
	 */
	protected abstract Class<? extends Annotation> getAnnotation();

	/**
	 * Spring Boot 用作模板的配置类。
	 *
	 * @return 配置类
	 */
	protected abstract Class<?> getConfiguration();

	/**
	 * 特定 Repository 支持的 {@link RepositoryConfigurationExtension}。
	 *
	 * @return Repository 配置扩展
	 */
	protected abstract RepositoryConfigurationExtension getRepositoryConfigurationExtension();

	/**
	 * 特定 Repository 支持的 {@link BootstrapMode}，默认为 {@link BootstrapMode#DEFAULT}。
	 *
	 * @return 引导模式
	 */
	protected BootstrapMode getBootstrapMode() {
		return BootstrapMode.DEFAULT;
	}

	@Override
	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}

	@Override
	public void setEnvironment(Environment environment) {
		this.environment = environment;
	}

	/**
	 * 自动配置的 {@link AnnotationRepositoryConfigurationSource}。
	 */
	private class AutoConfiguredAnnotationRepositoryConfigurationSource
			extends AnnotationRepositoryConfigurationSource {

		AutoConfiguredAnnotationRepositoryConfigurationSource(AnnotationMetadata metadata,
				Class<? extends Annotation> annotation, ResourceLoader resourceLoader, Environment environment,
				BeanDefinitionRegistry registry, @Nullable BeanNameGenerator generator) {
			super(metadata, annotation, resourceLoader, environment, registry, generator);
		}

		@Override
		public Streamable<String> getBasePackages() {
			return AbstractRepositoryConfigurationSourceSupport.this.getBasePackages();
		}

		@Override
		public BootstrapMode getBootstrapMode() {
			return AbstractRepositoryConfigurationSourceSupport.this.getBootstrapMode();
		}

	}

}
