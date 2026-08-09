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

import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.jspecify.annotations.Nullable;

import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

/**
 * 日志系统的通用抽象。
 * 定义初始化、级别设置、配置查询及工厂发现等核心能力。
 *
 * @author Phillip Webb
 * @author Dave Syer
 * @author Andy Wilkinson
 * @author Ben Hale
 * @since 1.0.0
 */
public abstract class LoggingSystem {

	/**
	 * 用于指定要使用的 {@link LoggingSystem} 的系统属性。
	 */
	public static final String SYSTEM_PROPERTY = LoggingSystem.class.getName();

	/**
	 * {@link #SYSTEM_PROPERTY} 的值，表示不使用任何 {@link LoggingSystem}。
	 */
	public static final String NONE = "none";

	/**
	 * root Logger 使用的名称。
	 * 各 {@link LoggingSystem} 实现应统一用此名称表示 root Logger。
	 */
	public static final String ROOT_LOGGER_NAME = "ROOT";

	private static final LoggingSystemFactory SYSTEM_FACTORY = LoggingSystemFactory.fromSpringFactories();

	/**
	 * 表示期望在日志中输出关联 ID 的 {@link Environment} 属性名。
	 *
	 * @since 3.2.0
	 */
	public static final String EXPECT_CORRELATION_ID_PROPERTY = "logging.expect-correlation-id";

	/**
	 * 返回应应用的 {@link LoggingSystemProperties}。
	 *
	 * @param environment 用于获取值的 {@link ConfigurableEnvironment}
	 * @return the {@link LoggingSystemProperties} to apply 要应用的属性集
	 * @since 2.4.0
	 */
	public LoggingSystemProperties getSystemProperties(ConfigurableEnvironment environment) {
		return new LoggingSystemProperties(environment);
	}

	/**
	 * 重置日志系统以限制输出。
	 * 可在 {@link #initialize(LoggingInitializationContext, String, LogFile)} 之前调用，
	 * 在系统完全初始化前减少日志噪音。
	 */
	public abstract void beforeInitialize();

	/**
	 * 完全初始化日志系统。
	 *
	 * @param initializationContext 日志初始化上下文
	 * @param configLocation 日志配置位置，默认初始化时为 {@code null}
	 * @param logFile 要写入的日志文件，仅控制台输出时为 {@code null}
	 */
	public void initialize(LoggingInitializationContext initializationContext, @Nullable String configLocation,
			@Nullable LogFile logFile) {
	}

	/**
	 * 清理日志系统。默认实现为空，子类应覆盖以执行特定清理。
	 */
	public void cleanUp() {
	}

	/**
	 * 返回 JVM 退出时关闭此日志系统的 {@link Runnable}。
	 * 默认返回 {@code null}，表示无需关闭处理。
	 *
	 * @return the shutdown handler, or {@code null} 关闭处理器或 {@code null}
	 */
	public @Nullable Runnable getShutdownHandler() {
		return null;
	}

	/**
	 * 返回日志系统实际支持的 {@link LogLevel} 集合。
	 *
	 * @return the supported levels 支持的级别
	 */
	public Set<LogLevel> getSupportedLogLevels() {
		return EnumSet.allOf(LogLevel.class);
	}

	/**
	 * 设置指定 Logger 的日志级别。
	 *
	 * @param loggerName 要设置的 Logger 名称（{@code null} 表示 root Logger）
	 * @param level 日志级别（{@code null} 移除自定义级别并恢复默认配置）
	 */
	public void setLogLevel(@Nullable String loggerName, @Nullable LogLevel level) {
		throw new UnsupportedOperationException("Unable to set log level");
	}

	/**
	 * 返回 {@link LoggingSystem} 所有 Logger 的当前配置。
	 *
	 * @return the current configurations 当前配置集合
	 * @since 1.5.0
	 */
	public List<LoggerConfiguration> getLoggerConfigurations() {
		throw new UnsupportedOperationException("Unable to get logger configurations");
	}

	/**
	 * 返回指定 Logger 的当前配置。
	 *
	 * @param loggerName Logger 名称
	 * @return the current configuration 当前配置
	 * @since 1.5.0
	 */
	public @Nullable LoggerConfiguration getLoggerConfiguration(String loggerName) {
		throw new UnsupportedOperationException("Unable to get logger configuration");
	}

	/**
	 * 检测并返回正在使用的日志系统。
	 * 支持 Logback 与 Java Logging 等实现。
	 *
	 * @param classLoader 类加载器
	 * @return the logging system 日志系统
	 */
	public static LoggingSystem get(ClassLoader classLoader) {
		String loggingSystemClassName = System.getProperty(SYSTEM_PROPERTY);
		if (StringUtils.hasLength(loggingSystemClassName)) {
			if (NONE.equals(loggingSystemClassName)) {
				return new NoOpLoggingSystem();
			}
			return get(classLoader, loggingSystemClassName);
		}
		LoggingSystem loggingSystem = SYSTEM_FACTORY.getLoggingSystem(classLoader);
		Assert.state(loggingSystem != null, "No suitable logging system located");
		return loggingSystem;
	}

	private static LoggingSystem get(ClassLoader classLoader, String loggingSystemClassName) {
		try {
			Class<?> systemClass = ClassUtils.forName(loggingSystemClassName, classLoader);
			Constructor<?> constructor = systemClass.getDeclaredConstructor(ClassLoader.class);
			constructor.setAccessible(true);
			return (LoggingSystem) constructor.newInstance(classLoader);
		}
		catch (Exception ex) {
			throw new IllegalStateException(ex);
		}
	}

	/**
	 * 空操作的 {@link LoggingSystem}。
	 */
	static class NoOpLoggingSystem extends LoggingSystem {

		@Override
		public void beforeInitialize() {

		}

		@Override
		public void setLogLevel(@Nullable String loggerName, @Nullable LogLevel level) {

		}

		@Override
		public List<LoggerConfiguration> getLoggerConfigurations() {
			return Collections.emptyList();
		}

		@Override
		public @Nullable LoggerConfiguration getLoggerConfiguration(String loggerName) {
			return null;
		}

	}

}
