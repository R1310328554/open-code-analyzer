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

import java.io.IOException;
import java.util.Properties;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PlaceholderConfigurerSupport;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.ConfigurablePropertyResolver;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.PropertySources;
import org.springframework.core.env.PropertySourcesPropertyResolver;
import org.springframework.util.Assert;
import org.springframework.util.StringValueResolver;

/**
 * {@link PlaceholderConfigurerSupport} 的特化实现，针对当前 Spring
 * {@link Environment} 及其 {@link PropertySources} 集合，
 * 解析 Bean 定义属性值与 {@code @Value} 注解中的 ${...} 占位符。
 *
 * <p>本类设计为 {@code PropertyPlaceholderConfigurer} 的通用替代。
 * 默认用于支持 spring-context 3.1 及以上 XSD 的 {@code property-placeholder} 元素；
 * 而 spring-context &lt;= 3.0 为向后兼容默认使用 {@code PropertyPlaceholderConfigurer}。
 * 完整细节参见 spring-context XSD 文档。
 *
 * <p>通过 {@link #setProperties}、{@link #setLocations} 等添加的本地属性
 * 会作为单个 {@link PropertySource} 加入。
 * 本地属性的搜索优先级由 {@link #setLocalOverride localOverride} 属性决定，
 * 默认为 {@code false}，即本地属性最后搜索，排在所有环境属性源之后。
 *
 * <p>操作环境属性源的细节参见
 * {@link org.springframework.core.env.ConfigurableEnvironment} 及相关 JavaDoc。
 *
 * @author Chris Beams
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 3.1
 * @see org.springframework.core.env.ConfigurableEnvironment
 * @see org.springframework.beans.factory.config.PlaceholderConfigurerSupport
 * @see org.springframework.beans.factory.config.PropertyPlaceholderConfigurer
 */
public class PropertySourcesPlaceholderConfigurer extends PlaceholderConfigurerSupport implements EnvironmentAware {

	/**
	 * 赋予本配置器 {@linkplain #mergeProperties() 合并属性} 的 {@link PropertySource} 的名称：{@value}。
	 */
	public static final String LOCAL_PROPERTIES_PROPERTY_SOURCE_NAME = "localProperties";

	/**
	 * 包装本配置器 {@linkplain #setEnvironment 环境} 的 {@link PropertySource} 的名称：{@value}。
	 */
	public static final String ENVIRONMENT_PROPERTIES_PROPERTY_SOURCE_NAME = "environmentProperties";


	/** 自定义属性源集合；设置后忽略环境与本地属性。 */
	private @Nullable MutablePropertySources propertySources;

	/** postProcessBeanFactory 中实际应用的属性源。 */
	private @Nullable PropertySources appliedPropertySources;

	/** 注入的 Environment。 */
	private @Nullable Environment environment;


	/**
	 * 自定义本配置器使用的 {@link PropertySources} 集合。
	 * <p>设置此属性表示应忽略环境属性源与本地属性。
	 * @see #postProcessBeanFactory
	 */
	public void setPropertySources(PropertySources propertySources) {
		this.propertySources = new MutablePropertySources(propertySources);
	}

	/**
	 * {@inheritDoc}
	 * <p>替换 ${...} 占位符时将搜索给定 {@link Environment} 中的 {@code PropertySources}。
	 * @see #setPropertySources
	 * @see #postProcessBeanFactory
	 */
	@Override
	public void setEnvironment(Environment environment) {
		this.environment = environment;
	}


	/**
	 * 通过针对本配置器的 {@link PropertySources} 集合解析每个 ${...} 占位符来处理 Bean 定义，包括：
	 * <ul>
	 * <li>若 {@linkplain #setEnvironment 存在} {@code Environment}，则包含其全部
	 * {@linkplain org.springframework.core.env.ConfigurableEnvironment#getPropertySources 环境属性源}
	 * <li>若 {@linkplain #setLocation 已} {@linkplain #setLocations 指定}
	 * {@linkplain #setProperties 本地} {@linkplain #setPropertiesArray 属性}，则包含
	 * {@linkplain #mergeProperties 合并后的本地属性}
	 * <li>调用 {@link #setPropertySources} 设置的任意属性源
	 * </ul>
	 * <p>若调用 {@link #setPropertySources}，<strong>将忽略环境与本地属性</strong>。
	 * 此方法旨在让用户精细控制属性源；一旦设置，配置器不再假设会添加其他源。
	 */
	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		if (this.propertySources == null) {
			this.propertySources = new MutablePropertySources();
			if (this.environment != null) {
				PropertySource<?> environmentPropertySource =
						(this.environment instanceof ConfigurableEnvironment configurableEnvironment ?
							new ConfigurableEnvironmentPropertySource(configurableEnvironment) :
							new FallbackEnvironmentPropertySource(this.environment));
				this.propertySources.addLast(environmentPropertySource);
			}
			try {
				PropertySource<?> localPropertySource =
						new PropertiesPropertySource(LOCAL_PROPERTIES_PROPERTY_SOURCE_NAME, mergeProperties());
				if (this.localOverride) {
					this.propertySources.addFirst(localPropertySource);
				}
				else {
					this.propertySources.addLast(localPropertySource);
				}
			}
			catch (IOException ex) {
				throw new BeanInitializationException("Could not load properties", ex);
			}
		}

		processProperties(beanFactory, createPropertyResolver(this.propertySources));
		this.appliedPropertySources = this.propertySources;
	}

	/**
	 * 为指定属性源创建 {@link ConfigurablePropertyResolver}。
	 * <p>默认实现创建 {@link PropertySourcesPropertyResolver}。
	 * @param propertySources 要使用的属性源
	 * @since 6.0.12
	 */
	protected ConfigurablePropertyResolver createPropertyResolver(MutablePropertySources propertySources){
		return new PropertySourcesPropertyResolver(propertySources);
	}

	/**
	 * 遍历给定 BeanFactory 中的每个 Bean 定义，尝试用给定属性解析 ${...} 占位符。
	 */
	protected void processProperties(ConfigurableListableBeanFactory beanFactoryToProcess,
			ConfigurablePropertyResolver propertyResolver) throws BeansException {

		propertyResolver.setPlaceholderPrefix(this.placeholderPrefix);
		propertyResolver.setPlaceholderSuffix(this.placeholderSuffix);
		propertyResolver.setValueSeparator(this.valueSeparator);
		propertyResolver.setEscapeCharacter(this.escapeCharacter);

		StringValueResolver valueResolver = strVal -> {
			String resolved = (this.ignoreUnresolvablePlaceholders ?
					propertyResolver.resolvePlaceholders(strVal) :
					propertyResolver.resolveRequiredPlaceholders(strVal));
			if (this.trimValues) {
				resolved = resolved.trim();
			}
			return (resolved.equals(this.nullValue) ? null : resolved);
		};

		doProcessProperties(beanFactoryToProcess, valueResolver);
	}

	/**
	 * 为与 {@link org.springframework.beans.factory.config.PlaceholderConfigurerSupport} 兼容而实现。
	 * @throws UnsupportedOperationException 本实现中始终抛出
	 * @deprecated 请改用
	 * {@link #processProperties(ConfigurableListableBeanFactory, ConfigurablePropertyResolver)}
	 */
	@Override
	@Deprecated(since = "3.1")
	protected void processProperties(ConfigurableListableBeanFactory beanFactory, Properties props) {
		throw new UnsupportedOperationException(
				"Call processProperties(ConfigurableListableBeanFactory, ConfigurablePropertyResolver) instead");
	}

	/**
	 * 返回 {@link #postProcessBeanFactory(ConfigurableListableBeanFactory) 后处理} 中实际应用的属性源。
	 * @return 已应用的属性源
	 * @throws IllegalStateException 若属性源尚未应用
	 * @since 4.0
	 */
	public PropertySources getAppliedPropertySources() throws IllegalStateException {
		Assert.state(this.appliedPropertySources != null, "PropertySources have not yet been applied");
		return this.appliedPropertySources;
	}


	/**
	 * 自定义 {@link PropertySource}，委托给 {@link ConfigurableEnvironment} 中的
	 * {@link ConfigurableEnvironment#getPropertySources() PropertySources}。
	 * @since 6.2.7
	 */
	private static class ConfigurableEnvironmentPropertySource extends PropertySource<ConfigurableEnvironment> {

		ConfigurableEnvironmentPropertySource(ConfigurableEnvironment environment) {
			super(ENVIRONMENT_PROPERTIES_PROPERTY_SOURCE_NAME, environment);
		}

		@Override
		public boolean containsProperty(String name) {
			for (PropertySource<?> propertySource : super.source.getPropertySources()) {
				if (propertySource.containsProperty(name)) {
					return true;
				}
			}
			return false;
		}

		@Override
		// Declare String as covariant return type, since a String is actually required.
		public @Nullable String getProperty(String name) {
			for (PropertySource<?> propertySource : super.source.getPropertySources()) {
				Object candidate = propertySource.getProperty(name);
				if (candidate != null) {
					return convertToString(candidate);
				}
			}
			return null;
		}

		/**
		 * 使用 {@link Environment} 中的 {@link ConversionService} 将给定值转换为 {@link String}。
		 * <p>这是
		 * {@link org.springframework.core.env.AbstractPropertyResolver#convertValueIfNecessary(Object, Class)}
		 * 的修改版本。
		 * @param value 要转换的值
		 * @return 转换后的值，或无需转换时的原值
		 * @since 6.2.8
		 */
		private @Nullable String convertToString(Object value) {
			if (value instanceof String string) {
				return string;
			}
			return super.source.getConversionService().convert(value, String.class);
		}

		@Override
		public String toString() {
			return "ConfigurableEnvironmentPropertySource {propertySources=" + super.source.getPropertySources() + "}";
		}
	}


	/**
	 * 委托给原始 {@link Environment} 的回退 {@link PropertySource}。
	 * <p>常规场景中不应出现，因为 {@code ApplicationContext} 中的 {@code Environment}
	 * 应始终为 {@link ConfigurableEnvironment}。
	 * @since 6.2.7
	 */
	private static class FallbackEnvironmentPropertySource extends PropertySource<Environment> {

		FallbackEnvironmentPropertySource(Environment environment) {
			super(ENVIRONMENT_PROPERTIES_PROPERTY_SOURCE_NAME, environment);
		}

		@Override
		public boolean containsProperty(String name) {
			return super.source.containsProperty(name);
		}

		@Override
		// Declare String as covariant return type, since a String is actually required.
		public @Nullable String getProperty(String name) {
			return super.source.getProperty(name);
		}

		@Override
		public String toString() {
			return "FallbackEnvironmentPropertySource {environment=" + super.source + "}";
		}
	}

}
