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

import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.jspecify.annotations.Nullable;

import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import org.springframework.util.SystemPropertyUtils;

/**
 * {@link LoggingSystem} 实现的抽象基类。
 * 提供基于约定或显式配置位置的初始化流程。
 *
 * @author Phillip Webb
 * @author Dave Syer
 * @since 1.0.0
 */
public abstract class AbstractLoggingSystem extends LoggingSystem {

	protected static final Comparator<LoggerConfiguration> CONFIGURATION_COMPARATOR = new LoggerConfigurationComparator(
			ROOT_LOGGER_NAME);

	private final ClassLoader classLoader;

	public AbstractLoggingSystem(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	@Override
	public void beforeInitialize() {
	}

	@Override
	public void initialize(LoggingInitializationContext initializationContext, @Nullable String configLocation,
			@Nullable LogFile logFile) {
		if (StringUtils.hasLength(configLocation)) {
			initializeWithSpecificConfig(initializationContext, configLocation, logFile);
			return;
		}
		initializeWithConventions(initializationContext, logFile);
	}

	private void initializeWithSpecificConfig(LoggingInitializationContext initializationContext, String configLocation,
			@Nullable LogFile logFile) {
		configLocation = SystemPropertyUtils.resolvePlaceholders(configLocation);
		loadConfiguration(initializationContext, configLocation, logFile);
	}

	private void initializeWithConventions(LoggingInitializationContext initializationContext,
			@Nullable LogFile logFile) {
		String config = getSelfInitializationConfig();
		if (config != null && logFile == null) {
			// self initialization has occurred, reinitialize in case of property changes
			reinitialize(initializationContext);
			return;
		}
		if (config == null) {
			config = getSpringInitializationConfig();
		}
		if (config != null) {
			loadConfiguration(initializationContext, config, logFile);
			return;
		}
		loadDefaults(initializationContext, logFile);
	}

	/**
	 * 返回已应用的自初始化配置。
	 * 默认检查 {@link #getStandardConfigLocations()}，假定存在的文件已被应用。
	 *
	 * @return the self initialization config or {@code null} 自初始化配置或 {@code null}
	 */
	protected @Nullable String getSelfInitializationConfig() {
		return findConfig(getStandardConfigLocations());
	}

	/**
	 * 返回应应用的 Spring 专用初始化配置。
	 * 默认检查 {@link #getSpringConfigLocations()}。
	 *
	 * @return the spring initialization config or {@code null} Spring 初始化配置或 {@code null}
	 */
	protected @Nullable String getSpringInitializationConfig() {
		return findConfig(getSpringConfigLocations());
	}

	private @Nullable String findConfig(String[] locations) {
		for (String location : locations) {
			ClassPathResource resource = new ClassPathResource(location, this.classLoader);
			if (resource.exists()) {
				return "classpath:" + location;
			}
		}
		return null;
	}

	/**
	 * 返回此系统的标准配置位置。
	 *
	 * @return the standard config locations 标准配置位置
	 * @see #getSelfInitializationConfig()
	 */
	protected abstract String[] getStandardConfigLocations();

	/**
	 * 返回此系统的 Spring 配置位置。
	 * 默认基于 {@link #getStandardConfigLocations()} 生成带 {@code -spring} 后缀的位置。
	 *
	 * @return the spring config locations Spring 配置位置
	 * @see #getSpringInitializationConfig()
	 */
	protected String[] getSpringConfigLocations() {
		String[] locations = getStandardConfigLocations();
		for (int i = 0; i < locations.length; i++) {
			String extension = StringUtils.getFilenameExtension(locations[i]);
			int extensionLength = (extension != null) ? (extension.length() + 1) : 0;
			locations[i] = locations[i].substring(0, locations[i].length() - extensionLength) + "-spring." + extension;
		}
		return locations;
	}

	/**
	 * 加载日志系统的合理默认配置。
	 *
	 * @param initializationContext 日志初始化上下文
	 * @param logFile 要写入的日志文件，不写入时为 {@code null}
	 */
	protected abstract void loadDefaults(LoggingInitializationContext initializationContext, @Nullable LogFile logFile);

	/**
	 * 加载指定配置。
	 *
	 * @param initializationContext 日志初始化上下文
	 * @param location 要加载的配置位置（永不为 {@code null}）
	 * @param logFile 要写入的日志文件，不写入时为 {@code null}
	 */
	protected abstract void loadConfiguration(LoggingInitializationContext initializationContext, String location,
			@Nullable LogFile logFile);

	/**
	 * 必要时重新初始化日志系统。
	 * 在已使用 {@link #getSelfInitializationConfig()} 且日志文件未变更时调用，
	 * 可用于重载配置（例如拾取新增系统属性）。
	 *
	 * @param initializationContext 日志初始化上下文
	 */
	protected void reinitialize(LoggingInitializationContext initializationContext) {
	}

	protected final ClassLoader getClassLoader() {
		return this.classLoader;
	}

	protected final String getPackagedConfigFile(String fileName) {
		String defaultPath = ClassUtils.getPackageName(getClass());
		defaultPath = defaultPath.replace('.', '/');
		defaultPath = defaultPath + "/" + fileName;
		defaultPath = "classpath:" + defaultPath;
		return defaultPath;
	}

	protected final void applySystemProperties(Environment environment, @Nullable LogFile logFile) {
		new LoggingSystemProperties(environment, getDefaultValueResolver(environment), null).apply(logFile);
	}

	/**
	 * 解析系统属性时使用的默认值解析器。
	 *
	 * @param environment 环境
	 * @return the default value resolver 默认值解析器
	 * @since 3.2.0
	 */
	protected Function<@Nullable String, @Nullable String> getDefaultValueResolver(Environment environment) {
		String defaultLogCorrelationPattern = getDefaultLogCorrelationPattern();
		return (name) -> {
			String applicationPropertyName = LoggingSystemProperty.CORRELATION_PATTERN.getApplicationPropertyName();
			Assert.state(applicationPropertyName != null, "applicationPropertyName must not be null");
			if (StringUtils.hasLength(defaultLogCorrelationPattern) && applicationPropertyName.equals(name)
					&& environment.getProperty(LoggingSystem.EXPECT_CORRELATION_ID_PROPERTY, Boolean.class, false)) {
				return defaultLogCorrelationPattern;
			}
			return null;
		};
	}

	/**
	 * 返回默认日志关联 ID 模式；不支持时返回 {@code null}。
	 *
	 * @return the default log correlation pattern 默认关联 ID 模式
	 * @since 3.2.0
	 */
	protected @Nullable String getDefaultLogCorrelationPattern() {
		return null;
	}

	/**
	 * 维护原生日志级别与 {@link LogLevel} 之间的映射。
	 *
	 * @param <T> the native level type 原生级别类型
	 */
	protected static class LogLevels<T> {

		private final Map<LogLevel, T> systemToNative;

		private final Map<T, LogLevel> nativeToSystem;

		public LogLevels() {
			this.systemToNative = new EnumMap<>(LogLevel.class);
			this.nativeToSystem = new HashMap<>();
		}

		public void map(LogLevel system, T nativeLevel) {
			this.systemToNative.putIfAbsent(system, nativeLevel);
			this.nativeToSystem.putIfAbsent(nativeLevel, system);
		}

		public @Nullable LogLevel convertNativeToSystem(T level) {
			return this.nativeToSystem.get(level);
		}

		public @Nullable T convertSystemToNative(@Nullable LogLevel level) {
			return this.systemToNative.get(level);
		}

		public Set<LogLevel> getSupported() {
			return new LinkedHashSet<>(this.nativeToSystem.values());
		}

	}

}
