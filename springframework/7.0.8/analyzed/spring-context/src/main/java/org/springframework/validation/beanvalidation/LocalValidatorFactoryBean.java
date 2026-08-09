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

package org.springframework.validation.beanvalidation;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Consumer;

import jakarta.validation.ClockProvider;
import jakarta.validation.Configuration;
import jakarta.validation.ConstraintValidatorFactory;
import jakarta.validation.MessageInterpolator;
import jakarta.validation.ParameterNameProvider;
import jakarta.validation.TraversableResolver;
import jakarta.validation.Validation;
import jakarta.validation.ValidationException;
import jakarta.validation.ValidationProviderResolver;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorContext;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.bootstrap.GenericBootstrap;
import jakarta.validation.bootstrap.ProviderSpecificBootstrap;
import org.hibernate.validator.messageinterpolation.ResourceBundleMessageInterpolator;
import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.MessageSource;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;

/**
 * Spring 应用上下文中 {@code jakarta.validation}（JSR-303）设置的核心类：
 * 引导 {@code jakarta.validation.ValidationFactory}，
 * 并通过 Spring {@link org.springframework.validation.Validator} 接口、
 * JSR-303 {@link jakarta.validation.Validator} 接口以及
 * {@link jakarta.validation.ValidatorFactory} 接口本身暴露。
 *
 * <p>通过 Spring 或 JSR-303 Validator 接口与本 Bean 实例交互时，
 * 实际使用的是底层 ValidatorFactory 的默认 Validator。
 * 这非常便利，无需再对工厂进行额外调用，因为几乎总是使用默认 Validator。
 * 也可直接注入到类型为 {@link org.springframework.validation.Validator} 的任意目标依赖！
 *
 * <p>当存在 {@code jakarta.validation} API 但未显式配置 Validator 时，
 * Spring MVC 配置命名空间也使用本类。
 *
 * @author Juergen Hoeller
 * @author Sebastien Deleuze
 * @since 3.0
 * @see jakarta.validation.ValidatorFactory
 * @see jakarta.validation.Validator
 * @see jakarta.validation.Validation#buildDefaultValidatorFactory()
 * @see jakarta.validation.ValidatorFactory#getValidator()
 */
public class LocalValidatorFactoryBean extends SpringValidatorAdapter
		implements ValidatorFactory, ApplicationContextAware, InitializingBean, DisposableBean {

	@SuppressWarnings("rawtypes")
	private @Nullable Class providerClass;

	private @Nullable ValidationProviderResolver validationProviderResolver;

	private @Nullable MessageInterpolator messageInterpolator;

	private @Nullable TraversableResolver traversableResolver;

	private @Nullable ConstraintValidatorFactory constraintValidatorFactory;

	private @Nullable ParameterNameDiscoverer parameterNameDiscoverer;

	private Resource @Nullable [] mappingLocations;

	private final Map<String, String> validationPropertyMap = new HashMap<>();

	private @Nullable Consumer<Configuration<?>> configurationInitializer;

	private @Nullable ApplicationContext applicationContext;

	private @Nullable ValidatorFactory validatorFactory;


	/**
	 * 指定所需的提供者类（若有）。
	 * <p>未指定时使用 JSR-303 的默认搜索机制。
	 * @see jakarta.validation.Validation#byProvider(Class)
	 * @see jakarta.validation.Validation#byDefaultProvider()
	 */
	@SuppressWarnings("rawtypes")
	public void setProviderClass(Class providerClass) {
		this.providerClass = providerClass;
	}

	/**
	 * 指定 JSR-303 {@link ValidationProviderResolver} 以引导所选提供者，
 * 作为 {@code META-INF} 驱动解析的替代方案。
	 * @since 4.3
	 */
	public void setValidationProviderResolver(ValidationProviderResolver validationProviderResolver) {
		this.validationProviderResolver = validationProviderResolver;
	}

	/**
	 * 指定本 ValidatorFactory 及其暴露的默认 Validator 使用的自定义 MessageInterpolator。
	 */
	public void setMessageInterpolator(MessageInterpolator messageInterpolator) {
		this.messageInterpolator = messageInterpolator;
	}

	/**
	 * 指定自定义 Spring MessageSource 解析校验消息，
	 * 而非依赖类路径中 JSR-303 默认的 "ValidationMessages.properties" 资源包。 This may refer to a Spring context's shared "messageSource" bean,
	 * or to some special MessageSource setup for validation purposes only.
	 * <p><b>NOTE:</b> This feature requires Hibernate Validator 4.3 or higher on the classpath.
	 * You may nevertheless use a different validation provider but Hibernate Validator's
	 * {@link ResourceBundleMessageInterpolator} class must be accessible during configuration.
	 * <p>Specify either this property or {@link #setMessageInterpolator "messageInterpolator"},
	 * not both. If you would like to build a custom MessageInterpolator, consider deriving from
	 * Hibernate Validator's {@link ResourceBundleMessageInterpolator} and passing in a
	 * Spring-based {@code ResourceBundleLocator} when constructing your interpolator.
	 * <p>In order for Hibernate's default validation messages to be resolved still, your
	 * {@link MessageSource} must be configured for optional resolution (usually the default).
	 * In particular, the {@code MessageSource} instance specified here should not apply
	 * {@link org.springframework.context.support.AbstractMessageSource#setUseCodeAsDefaultMessage
	 * "useCodeAsDefaultMessage"} behavior. Please double-check your setup accordingly.
	 * @see ResourceBundleMessageInterpolator
	 */
	public void setValidationMessageSource(MessageSource messageSource) {
		this.messageInterpolator = HibernateValidatorDelegate.buildMessageInterpolator(messageSource);
	}

	/**
	 * 指定本 ValidatorFactory 及其暴露的默认 Validator 使用的自定义 TraversableResolver。
	 */
	public void setTraversableResolver(TraversableResolver traversableResolver) {
		this.traversableResolver = traversableResolver;
	}

	/**
	 * 指定本 ValidatorFactory 使用的自定义 ConstraintValidatorFactory。
	 * <p>默认为 {@link SpringConstraintValidatorFactory}，
	 * 委托包含的 ApplicationContext 创建可自动装配的 ConstraintValidator 实例。
	 */
	public void setConstraintValidatorFactory(ConstraintValidatorFactory constraintValidatorFactory) {
		this.constraintValidatorFactory = constraintValidatorFactory;
	}

	/**
	 * 设置 ParameterNameDiscoverer，用于在消息插值需要时解析方法和构造器参数名。
	 * <p>Default is Hibernate Validator's own internal use of standard Java reflection.
	 * This may be overridden with a custom subclass or a Spring-controlled
	 * {@link org.springframework.core.DefaultParameterNameDiscoverer} if necessary.
	 */
	public void setParameterNameDiscoverer(ParameterNameDiscoverer parameterNameDiscoverer) {
		this.parameterNameDiscoverer = parameterNameDiscoverer;
	}

	/**
	 * 指定加载 XML 约束映射文件的资源位置（若有）。
	 */
	public void setMappingLocations(Resource... mappingLocations) {
		this.mappingLocations = mappingLocations;
	}

	/**
	 * 指定传递给校验提供者的 Bean 校验属性。
	 * <p>Can be populated with a String "value" (parsed via PropertiesEditor)
	 * or a "props" element in XML bean definitions.
	 * @see jakarta.validation.Configuration#addProperty(String, String)
	 */
	public void setValidationProperties(Properties jpaProperties) {
		CollectionUtils.mergePropertiesIntoMap(jpaProperties, this.validationPropertyMap);
	}

	/**
	 * 以 Map 形式指定传递给校验提供者的 Bean 校验属性。
	 * <p>Can be populated with a "map" or "props" element in XML bean definitions.
	 * @see jakarta.validation.Configuration#addProperty(String, String)
	 */
	public void setValidationPropertyMap(@Nullable Map<String, String> validationProperties) {
		if (validationProperties != null) {
			this.validationPropertyMap.putAll(validationProperties);
		}
	}

	/**
	 * 允许以 Map 方式访问传递给校验提供者的 Bean 校验属性，
	 * 并可添加或覆盖特定条目。
	 * <p>Useful for specifying entries directly, for example via "validationPropertyMap[myKey]".
	 */
	public Map<String, String> getValidationPropertyMap() {
		return this.validationPropertyMap;
	}

	/**
	 * 指定用于定制 Bean Validation {@code Configuration} 实例的回调，
	 * 作为在自定义 {@code LocalValidatorFactoryBean} 子类中
	 * 覆盖 {@link #postProcessConfiguration(Configuration)} 方法的替代方案。
	 * <p>This enables convenient customizations for application purposes. Infrastructure
	 * extensions may keep overriding the {@link #postProcessConfiguration} template method.
	 * @since 5.3.19
	 */
	public void setConfigurationInitializer(Consumer<Configuration<?>> configurationInitializer) {
		this.configurationInitializer = configurationInitializer;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}


	@Override
	@SuppressWarnings({"rawtypes", "unchecked"})
	public void afterPropertiesSet() {
		Configuration<?> configuration;
		if (this.providerClass != null) {
			ProviderSpecificBootstrap bootstrap = Validation.byProvider(this.providerClass);
			if (this.validationProviderResolver != null) {
				bootstrap = bootstrap.providerResolver(this.validationProviderResolver);
			}
			configuration = bootstrap.configure();
		}
		else {
			GenericBootstrap bootstrap = Validation.byDefaultProvider();
			if (this.validationProviderResolver != null) {
				bootstrap = bootstrap.providerResolver(this.validationProviderResolver);
			}
			configuration = bootstrap.configure();
		}

		// Try Hibernate Validator's externalClassLoader(ClassLoader) method
		if (this.applicationContext != null) {
			try {
				Method eclMethod = configuration.getClass().getMethod("externalClassLoader", ClassLoader.class);
				eclMethod = ClassUtils.getPubliclyAccessibleMethodIfPossible(eclMethod, configuration.getClass());
				ReflectionUtils.invokeMethod(eclMethod, configuration, this.applicationContext.getClassLoader());
			}
			catch (NoSuchMethodException ignored) {
				// no Hibernate Validator or similar provider
			}
		}

		MessageInterpolator targetInterpolator = this.messageInterpolator;
		if (targetInterpolator == null) {
			targetInterpolator = configuration.getDefaultMessageInterpolator();
		}
		configuration.messageInterpolator(new LocaleContextMessageInterpolator(targetInterpolator));

		if (this.traversableResolver != null) {
			configuration.traversableResolver(this.traversableResolver);
		}

		ConstraintValidatorFactory targetConstraintValidatorFactory = this.constraintValidatorFactory;
		if (targetConstraintValidatorFactory == null && this.applicationContext != null) {
			targetConstraintValidatorFactory = new SpringConstraintValidatorFactory(
					this.applicationContext.getAutowireCapableBeanFactory(),
					configuration.getDefaultConstraintValidatorFactory());
		}
		if (targetConstraintValidatorFactory != null) {
			configuration.constraintValidatorFactory(targetConstraintValidatorFactory);
		}

		if (this.parameterNameDiscoverer != null) {
			configureParameterNameProvider(this.parameterNameDiscoverer, configuration);
		}

		List<InputStream> mappingStreams = null;
		if (this.mappingLocations != null) {
			mappingStreams = new ArrayList<>(this.mappingLocations.length);
			for (Resource location : this.mappingLocations) {
				try {
					InputStream stream = location.getInputStream();
					mappingStreams.add(stream);
					configuration.addMapping(stream);
				}
				catch (IOException ex) {
					closeMappingStreams(mappingStreams);
					throw new IllegalStateException("Cannot read mapping resource: " + location);
				}
			}
		}

		this.validationPropertyMap.forEach(configuration::addProperty);

		// Allow for custom post-processing before we actually build the ValidatorFactory.
		if (this.configurationInitializer != null) {
			this.configurationInitializer.accept(configuration);
		}
		postProcessConfiguration(configuration);

		try {
			this.validatorFactory = configuration.buildValidatorFactory();
			setTargetValidator(this.validatorFactory.getValidator());
		}
		finally {
			closeMappingStreams(mappingStreams);
		}
	}

	private void configureParameterNameProvider(ParameterNameDiscoverer discoverer, Configuration<?> configuration) {
		final ParameterNameProvider defaultProvider = configuration.getDefaultParameterNameProvider();
		configuration.parameterNameProvider(new ParameterNameProvider() {
			@Override
			public List<String> getParameterNames(Constructor<?> constructor) {
				@Nullable String[] paramNames = discoverer.getParameterNames(constructor);
				return (paramNames != null ? Arrays.asList(paramNames) :
						defaultProvider.getParameterNames(constructor));
			}
			@Override
			public List<String> getParameterNames(Method method) {
				@Nullable String[] paramNames = discoverer.getParameterNames(method);
				return (paramNames != null ? Arrays.asList(paramNames) :
						defaultProvider.getParameterNames(method));
			}
		});
	}

	private void closeMappingStreams(@Nullable List<InputStream> mappingStreams){
		if (!CollectionUtils.isEmpty(mappingStreams)) {
			for (InputStream stream : mappingStreams) {
				try {
					stream.close();
				}
				catch (IOException ignored) {
				}
			}
		}
	}

	/**
	 * 后处理给定 Bean Validation 配置，添加或覆盖其任何设置。
	 * <p>在构建 {@link ValidatorFactory} 之前调用。
	 * @param configuration Configuration 对象，已预填充 LocalValidatorFactoryBean 属性驱动的设置
	 */
	protected void postProcessConfiguration(Configuration<?> configuration) {
	}

	private ValidatorFactory obtainValidatorFactory() {
		Assert.state(this.validatorFactory != null, "No target ValidatorFactory set");
		return this.validatorFactory;
	}


	@Override
	public Validator getValidator() {
		return obtainValidatorFactory().getValidator();
	}

	@Override
	public ValidatorContext usingContext() {
		return obtainValidatorFactory().usingContext();
	}

	@Override
	public MessageInterpolator getMessageInterpolator() {
		return obtainValidatorFactory().getMessageInterpolator();
	}

	@Override
	public TraversableResolver getTraversableResolver() {
		return obtainValidatorFactory().getTraversableResolver();
	}

	@Override
	public ConstraintValidatorFactory getConstraintValidatorFactory() {
		return obtainValidatorFactory().getConstraintValidatorFactory();
	}

	@Override
	public ParameterNameProvider getParameterNameProvider() {
		return obtainValidatorFactory().getParameterNameProvider();
	}

	@Override
	public ClockProvider getClockProvider() {
		return obtainValidatorFactory().getClockProvider();
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T unwrap(@Nullable Class<T> type) {
		if (type == null || !ValidatorFactory.class.isAssignableFrom(type)) {
			try {
				return super.unwrap(type);
			}
			catch (ValidationException ignored) {
				// Trying ValidatorFactory unwrapping next
			}
		}
		if (this.validatorFactory != null) {
			try {
				return this.validatorFactory.unwrap(type);
			}
			catch (ValidationException ex) {
				// Ignore if just being asked for ValidatorFactory
				if (ValidatorFactory.class == type) {
					return (T) this.validatorFactory;
				}
				throw ex;
			}
		}
		throw new ValidationException("Cannot unwrap to " + type);
	}

	@Override
	public void close() {
		if (this.validatorFactory != null) {
			this.validatorFactory.close();
		}
	}

	@Override
	public void destroy() {
		close();
	}


	/**
	 * 内部类，避免硬编码 Hibernate Validator 依赖。
	 */
	private static class HibernateValidatorDelegate {

		public static MessageInterpolator buildMessageInterpolator(MessageSource messageSource) {
			return new ResourceBundleMessageInterpolator(new MessageSourceResourceBundleLocator(messageSource));
		}
	}

}
