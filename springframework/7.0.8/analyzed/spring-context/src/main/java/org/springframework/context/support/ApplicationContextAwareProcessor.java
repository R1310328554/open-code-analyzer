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

package org.springframework.context.support;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.Aware;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.EmbeddedValueResolver;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.ApplicationStartupAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.util.StringValueResolver;

/**
 * 为实现了 {@link EnvironmentAware}、{@link EmbeddedValueResolverAware}、
 * {@link ResourceLoaderAware}、{@link ApplicationEventPublisherAware}、
 * {@link MessageSourceAware}、{@link ApplicationStartupAware} 和/或
 * {@link ApplicationContextAware} 接口的 Bean 注入
 * {@link org.springframework.context.ApplicationContext ApplicationContext}、
 * {@link org.springframework.core.env.Environment Environment}、
 * {@link StringValueResolver} 或
 * {@link org.springframework.core.metrics.ApplicationStartup ApplicationStartup}
 * 的 {@link BeanPostProcessor} 实现。
 *
 * <p>上述接口按所列顺序依次满足。
 *
 * <p>应用上下文会自动将其注册到底层 Bean 工厂。应用程序不应直接使用本类。
 *
 * @author Juergen Hoeller
 * @author Costin Leau
 * @author Chris Beams
 * @author Sam Brannen
 * @since 10.10.2003
 * @see org.springframework.context.EnvironmentAware
 * @see org.springframework.context.EmbeddedValueResolverAware
 * @see org.springframework.context.ResourceLoaderAware
 * @see org.springframework.context.ApplicationEventPublisherAware
 * @see org.springframework.context.MessageSourceAware
 * @see org.springframework.context.ApplicationStartupAware
 * @see org.springframework.context.ApplicationContextAware
 * @see org.springframework.context.support.AbstractApplicationContext#refresh()
 */
class ApplicationContextAwareProcessor implements BeanPostProcessor {

	private final ConfigurableApplicationContext applicationContext;

	private final StringValueResolver embeddedValueResolver;


	/**
	 * 为给定上下文创建新的 ApplicationContextAwareProcessor。
	 */
	public ApplicationContextAwareProcessor(ConfigurableApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
		this.embeddedValueResolver = new EmbeddedValueResolver(applicationContext.getBeanFactory());
	}


	@Override
	public @Nullable Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		if (bean instanceof Aware) {
			invokeAwareInterfaces(bean);
		}
		return bean;
	}

	private void invokeAwareInterfaces(Object bean) {
		if (bean instanceof EnvironmentAware environmentAware) {
			environmentAware.setEnvironment(this.applicationContext.getEnvironment());
		}
		if (bean instanceof EmbeddedValueResolverAware embeddedValueResolverAware) {
			embeddedValueResolverAware.setEmbeddedValueResolver(this.embeddedValueResolver);
		}
		if (bean instanceof ResourceLoaderAware resourceLoaderAware) {
			resourceLoaderAware.setResourceLoader(this.applicationContext);
		}
		if (bean instanceof ApplicationEventPublisherAware applicationEventPublisherAware) {
			applicationEventPublisherAware.setApplicationEventPublisher(this.applicationContext);
		}
		if (bean instanceof MessageSourceAware messageSourceAware) {
			messageSourceAware.setMessageSource(this.applicationContext);
		}
		if (bean instanceof ApplicationStartupAware applicationStartupAware) {
			applicationStartupAware.setApplicationStartup(this.applicationContext.getApplicationStartup());
		}
		if (bean instanceof ApplicationContextAware applicationContextAware) {
			applicationContextAware.setApplicationContext(this.applicationContext);
		}
	}

}
