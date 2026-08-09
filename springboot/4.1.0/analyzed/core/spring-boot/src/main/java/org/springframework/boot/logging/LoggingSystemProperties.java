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

package org.springframework.boot.logging;

import java.io.Console;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.jspecify.annotations.Nullable;

import org.springframework.boot.system.ApplicationPid;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertyResolver;
import org.springframework.core.env.PropertySourcesPropertyResolver;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * 设置日志配置文件可引用的系统属性的工具类。
 *
 * @author Andy Wilkinson
 * @author Phillip Webb
 * @author Madhura Bhave
 * @author Vedran Pavic
 * @author Robert Thornton
 * @author Eddú Meléndez
 * @author Jonatan Ivanov
 * @since 2.0.0
 * @see LoggingSystemProperty
 */
public class LoggingSystemProperties {

	private static final BiConsumer<String, @Nullable String> systemPropertySetter = (name, value) -> {
		if (System.getProperty(name) == null && value != null) {
			System.setProperty(name, value);
		}
	};

	private final Environment environment;

	private final Function<@Nullable String, @Nullable String> defaultValueResolver;

	private final BiConsumer<String, @Nullable String> setter;

	/**
	 * 创建新的 {@link LoggingSystemProperties} 实例。
	 *
	 * @param environment 源环境
	 */
	public LoggingSystemProperties(Environment environment) {
		this(environment, null);
	}

	/**
	 * 创建新的 {@link LoggingSystemProperties} 实例。
	 *
	 * @param environment 源环境
	 * @param setter 应用属性的 setter，{@code null} 时使用系统属性
	 * @since 2.4.2
	 */
	public LoggingSystemProperties(Environment environment, @Nullable BiConsumer<String, @Nullable String> setter) {
		this(environment, null, setter);
	}

	/**
	 * 创建新的 {@link LoggingSystemProperties} 实例。
	 *
	 * @param environment 源环境
	 * @param defaultValueResolver 解析默认值的函数，可为 {@code null}
	 * @param setter 应用属性的 setter，{@code null} 时使用系统属性
	 * @since 3.2.0
	 */
	public LoggingSystemProperties(Environment environment,
			@Nullable Function<@Nullable String, @Nullable String> defaultValueResolver,
			@Nullable BiConsumer<String, @Nullable String> setter) {
		Assert.notNull(environment, "'environment' must not be null");
		this.environment = environment;
		this.defaultValueResolver = (defaultValueResolver != null) ? defaultValueResolver : (name) -> null;
		this.setter = (setter != null) ? setter : systemPropertySetter;
	}

	/**
	 * 返回要使用的 {@link Console}。
	 *
	 * @return the {@link Console} to use 控制台
	 * @since 3.5.0
	 */
	protected @Nullable Console getConsole() {
		return System.console();
	}

	public final void apply() {
		apply(null);
	}

	public final void apply(@Nullable LogFile logFile) {
		PropertyResolver resolver = getPropertyResolver();
		apply(logFile, resolver);
	}

	private PropertyResolver getPropertyResolver() {
		if (this.environment instanceof ConfigurableEnvironment configurableEnvironment) {
			PropertySourcesPropertyResolver resolver = new PropertySourcesPropertyResolver(
					configurableEnvironment.getPropertySources());
			resolver.setConversionService(configurableEnvironment.getConversionService());
			resolver.setIgnoreUnresolvableNestedPlaceholders(true);
			return resolver;
		}
		return this.environment;
	}

	protected void apply(@Nullable LogFile logFile, PropertyResolver resolver) {
		setSystemProperty(LoggingSystemProperty.APPLICATION_NAME, resolver);
		setSystemProperty(LoggingSystemProperty.APPLICATION_GROUP, resolver);
		setSystemProperty(LoggingSystemProperty.PID, new ApplicationPid().toString());
		setSystemProperty(LoggingSystemProperty.CONSOLE_CHARSET, resolver, getDefaultConsoleCharset().name());
		setSystemProperty(LoggingSystemProperty.FILE_CHARSET, resolver, getDefaultFileCharset().name());
		setSystemProperty(LoggingSystemProperty.CONSOLE_THRESHOLD, resolver, this::thresholdMapper);
		setSystemProperty(LoggingSystemProperty.FILE_THRESHOLD, resolver, this::thresholdMapper);
		setSystemProperty(LoggingSystemProperty.EXCEPTION_CONVERSION_WORD, resolver);
		setSystemProperty(LoggingSystemProperty.CONSOLE_PATTERN, resolver);
		setSystemProperty(LoggingSystemProperty.FILE_PATTERN, resolver);
		setSystemProperty(LoggingSystemProperty.CONSOLE_STRUCTURED_FORMAT, resolver);
		setSystemProperty(LoggingSystemProperty.FILE_STRUCTURED_FORMAT, resolver);
		setSystemProperty(LoggingSystemProperty.LEVEL_PATTERN, resolver);
		setSystemProperty(LoggingSystemProperty.DATEFORMAT_PATTERN, resolver);
		setSystemProperty(LoggingSystemProperty.CORRELATION_PATTERN, resolver);
		if (logFile != null) {
			logFile.applyToSystemProperties();
		}
		if (!this.environment.getProperty("logging.console.enabled", Boolean.class, true)) {
			setSystemProperty(LoggingSystemProperty.CONSOLE_THRESHOLD.getEnvironmentVariableName(), "OFF");
		}
	}

	/**
	 * 返回默认控制台字符集。
	 *
	 * @return the default console charset 默认控制台字符集
	 * @since 3.5.0
	 */
	protected Charset getDefaultConsoleCharset() {
		Console console = getConsole();
		return (console != null) ? console.charset() : Charset.defaultCharset();
	}

	/**
	 * 返回默认文件字符集。
	 *
	 * @return the default file charset 默认文件字符集
	 * @since 3.5.0
	 */
	protected Charset getDefaultFileCharset() {
		return StandardCharsets.UTF_8;
	}

	private void setSystemProperty(LoggingSystemProperty property, PropertyResolver resolver) {
		setSystemProperty(property, resolver, (i) -> i);
	}

	private void setSystemProperty(LoggingSystemProperty property, PropertyResolver resolver,
			Function<@Nullable String, @Nullable String> mapper) {
		setSystemProperty(property, resolver, null, mapper);
	}

	private void setSystemProperty(LoggingSystemProperty property, PropertyResolver resolver, String defaultValue) {
		setSystemProperty(property, resolver, defaultValue, (i) -> i);
	}

	private void setSystemProperty(LoggingSystemProperty property, PropertyResolver resolver,
			@Nullable String defaultValue, Function<@Nullable String, @Nullable String> mapper) {
		if (property.getIncludePropertyName() != null) {
			if (!resolver.getProperty(property.getIncludePropertyName(), Boolean.class, Boolean.TRUE)) {
				return;
			}
		}
		String applicationPropertyName = property.getApplicationPropertyName();
		String value = (applicationPropertyName != null) ? resolver.getProperty(applicationPropertyName) : null;
		value = (value != null) ? value : this.defaultValueResolver.apply(applicationPropertyName);
		value = (value != null) ? value : defaultValue;
		value = mapper.apply(value);
		setSystemProperty(property.getEnvironmentVariableName(), value);
		if (property == LoggingSystemProperty.APPLICATION_NAME && StringUtils.hasText(value)) {
			// LOGGED_APPLICATION_NAME is deprecated for removal in 4.0.0
			setSystemProperty("LOGGED_APPLICATION_NAME", "[%s] ".formatted(value));
		}
	}

	private void setSystemProperty(LoggingSystemProperty property, String value) {
		setSystemProperty(property.getEnvironmentVariableName(), value);
	}

	private @Nullable String thresholdMapper(@Nullable String input) {
		// YAML converts an unquoted OFF to false
		if ("false".equals(input)) {
			return "OFF";
		}
		return input;
	}

	/**
	 * 设置系统属性。
	 *
	 * @param name 属性名
	 * @param value 属性值
	 */
	protected final void setSystemProperty(String name, @Nullable String value) {
		this.setter.accept(name, value);
	}

}
