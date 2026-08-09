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

package org.springframework.beans.factory.aot;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.aot.AotServices.Source;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.RegisteredBean;
import org.springframework.core.log.LogMessage;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

/**
 * 用于为 {@link RegisteredBean} 创建 {@link BeanDefinitionMethodGenerator} 实例的工厂。
 *
 * @author Phillip Webb
 * @author Stephane Nicoll
 * @since 6.0
 * @see BeanDefinitionMethodGenerator
 * @see #getBeanDefinitionMethodGenerator(RegisteredBean)
 */
class BeanDefinitionMethodGeneratorFactory {

	private static final Log logger = LogFactory.getLog(BeanDefinitionMethodGeneratorFactory.class);

	/** Bean 注册 AOT 处理器服务。 */
	private final AotServices<BeanRegistrationAotProcessor> aotProcessors;

	/** Bean 注册排除过滤器服务。 */
	private final AotServices<BeanRegistrationExcludeFilter> excludeFilters;


	/**
	 * 使用给定 {@link ConfigurableListableBeanFactory} 创建新的
	 * {@link BeanDefinitionMethodGeneratorFactory}。
	 * @param beanFactory 使用的 bean 工厂
	 */
	BeanDefinitionMethodGeneratorFactory(ConfigurableListableBeanFactory beanFactory) {
		this(AotServices.factoriesAndBeans(beanFactory));
	}

	/**
	 * 使用给定 {@link AotServices.Loader} 创建新的
	 * {@link BeanDefinitionMethodGeneratorFactory}。
	 * @param loader 使用的 AOT 服务加载器
	 */
	BeanDefinitionMethodGeneratorFactory(AotServices.Loader loader) {
		this.aotProcessors = loader.load(BeanRegistrationAotProcessor.class);
		this.excludeFilters = loader.load(BeanRegistrationExcludeFilter.class);
		for (BeanRegistrationExcludeFilter excludeFilter : this.excludeFilters) {
			if (this.excludeFilters.getSource(excludeFilter) == Source.BEAN_FACTORY) {
				Assert.state(excludeFilter instanceof BeanRegistrationAotProcessor ||
						excludeFilter instanceof BeanFactoryInitializationAotProcessor,
						() -> "BeanRegistrationExcludeFilter bean of type %s must also implement an AOT processor interface"
								.formatted(excludeFilter.getClass().getName()));
			}
		}
	}


	/**
	 * 为给定 {@link RegisteredBean}（及其所属属性名）返回
	 * {@link BeanDefinitionMethodGenerator}，若被
	 * {@link BeanRegistrationExcludeFilter} 排除则返回 {@code null}。
	 * 生成的 {@link BeanDefinitionMethodGenerator} 将包含所有
	 * {@link BeanRegistrationAotProcessor} 提供的贡献。
	 * @param registeredBean 已注册 bean
	 * @param currentPropertyName 此 bean 所属的属性名
	 * @return 新的 {@link BeanDefinitionMethodGenerator} 实例，或 {@code null}
	 */
	@Nullable BeanDefinitionMethodGenerator getBeanDefinitionMethodGenerator(
			RegisteredBean registeredBean, @Nullable String currentPropertyName) {

		if (isExcluded(registeredBean)) {
			return null;
		}
		List<BeanRegistrationAotContribution> contributions = getAotContributions(registeredBean);
		return new BeanDefinitionMethodGenerator(this, registeredBean,
				currentPropertyName, contributions);
	}

	/**
	 * 为给定 {@link RegisteredBean} 返回 {@link BeanDefinitionMethodGenerator}，
	 * 若被 {@link BeanRegistrationExcludeFilter} 排除则返回 {@code null}。
	 * 生成的 {@link BeanDefinitionMethodGenerator} 将包含所有
	 * {@link BeanRegistrationAotProcessor} 提供的贡献。
	 * @param registeredBean 已注册 bean
	 * @return 新的 {@link BeanDefinitionMethodGenerator} 实例，或 {@code null}
	 */
	@Nullable BeanDefinitionMethodGenerator getBeanDefinitionMethodGenerator(RegisteredBean registeredBean) {
		return getBeanDefinitionMethodGenerator(registeredBean, null);
	}

	private boolean isExcluded(RegisteredBean registeredBean) {
		if (isImplicitlyExcluded(registeredBean)) {
			return true;
		}
		for (BeanRegistrationExcludeFilter excludeFilter : this.excludeFilters) {
			if (excludeFilter.isExcludedFromAotProcessing(registeredBean)) {
				logger.trace(LogMessage.format(
						"Excluding registered bean '%s' from bean factory %s due to %s",
						registeredBean.getBeanName(),
						ObjectUtils.identityToString(registeredBean.getBeanFactory()),
						excludeFilter.getClass().getName()));
				return true;
			}
		}
		return false;
	}

	private boolean isImplicitlyExcluded(RegisteredBean registeredBean) {
		if (Boolean.TRUE.equals(registeredBean.getMergedBeanDefinition()
				.getAttribute(BeanRegistrationAotProcessor.IGNORE_REGISTRATION_ATTRIBUTE))) {
			return true;
		}
		Class<?> beanClass = registeredBean.getBeanClass();
		if (BeanFactoryInitializationAotProcessor.class.isAssignableFrom(beanClass)) {
			return true;
		}
		if (BeanRegistrationAotProcessor.class.isAssignableFrom(beanClass)) {
			BeanRegistrationAotProcessor processor = this.aotProcessors.findByBeanName(registeredBean.getBeanName());
			return (processor == null || processor.isBeanExcludedFromAotProcessing());
		}
		return false;
	}

	private List<BeanRegistrationAotContribution> getAotContributions(RegisteredBean registeredBean) {
		String beanName = registeredBean.getBeanName();
		List<BeanRegistrationAotContribution> contributions = new ArrayList<>();
		for (BeanRegistrationAotProcessor aotProcessor : this.aotProcessors) {
			BeanRegistrationAotContribution contribution = aotProcessor.processAheadOfTime(registeredBean);
			if (contribution != null) {
				logger.trace(LogMessage.format(
						"Adding bean registration AOT contribution %S from %S to '%S'",
						contribution.getClass().getName(),
						aotProcessor.getClass().getName(), beanName));
				contributions.add(contribution);
			}
		}
		return contributions;
	}

}
