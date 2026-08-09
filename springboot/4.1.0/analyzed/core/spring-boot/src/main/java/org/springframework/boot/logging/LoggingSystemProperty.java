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

import org.jspecify.annotations.Nullable;

/**
 * 日志配置文件可引用的日志系统属性枚举。
 *
 * @author Phillip Webb
 * @since 3.2.0
 * @see LoggingSystemProperties
 */
public enum LoggingSystemProperty {

	/**
	 * 应写入日志的应用名称系统属性。
	 */
	APPLICATION_NAME("APPLICATION_NAME", "spring.application.name", "logging.include-application-name"),

	/**
	 * 应写入日志的应用组系统属性。
	 *
	 * @since 3.4.0
	 */
	APPLICATION_GROUP("APPLICATION_GROUP", "spring.application.group", "logging.include-application-group"),

	/**
	 * 进程 ID 的日志系统属性。
	 */
	PID("PID"),

	/**
	 * 日志文件的日志系统属性。
	 */
	LOG_FILE("LOG_FILE"),

	/**
	 * 日志路径的日志系统属性。
	 */
	LOG_PATH("LOG_PATH"),

	/**
	 * 控制台日志字符集的系统属性。
	 */
	CONSOLE_CHARSET("CONSOLE_LOG_CHARSET", "logging.charset.console"),

	/**
	 * 文件日志字符集的系统属性。
	 */
	FILE_CHARSET("FILE_LOG_CHARSET", "logging.charset.file"),

	/**
	 * 控制台日志阈值的系统属性。
	 */
	CONSOLE_THRESHOLD("CONSOLE_LOG_THRESHOLD", "logging.threshold.console"),

	/**
	 * 文件日志阈值的系统属性。
	 */
	FILE_THRESHOLD("FILE_LOG_THRESHOLD", "logging.threshold.file"),

	/**
	 * 异常转换字的日志系统属性。
	 */
	EXCEPTION_CONVERSION_WORD("LOG_EXCEPTION_CONVERSION_WORD", "logging.exception-conversion-word"),

	/**
	 * 控制台日志模式的系统属性。
	 */
	CONSOLE_PATTERN("CONSOLE_LOG_PATTERN", "logging.pattern.console"),

	/**
	 * 文件日志模式的系统属性。
	 */
	FILE_PATTERN("FILE_LOG_PATTERN", "logging.pattern.file"),

	/**
	 * 控制台结构化日志格式的系统属性。
	 *
	 * @since 3.4.0
	 */
	CONSOLE_STRUCTURED_FORMAT("CONSOLE_LOG_STRUCTURED_FORMAT", "logging.structured.format.console"),

	/**
	 * 文件结构化日志格式的系统属性。
	 *
	 * @since 3.4.0
	 */
	FILE_STRUCTURED_FORMAT("FILE_LOG_STRUCTURED_FORMAT", "logging.structured.format.file"),

	/**
	 * 日志级别模式的系统属性。
	 */
	LEVEL_PATTERN("LOG_LEVEL_PATTERN", "logging.pattern.level"),

	/**
	 * 日期格式模式的日志系统属性。
	 */
	DATEFORMAT_PATTERN("LOG_DATEFORMAT_PATTERN", "logging.pattern.dateformat"),

	/**
	 * 关联 ID 模式的日志系统属性。
	 */
	CORRELATION_PATTERN("LOG_CORRELATION_PATTERN", "logging.pattern.correlation");

	private final String environmentVariableName;

	private final @Nullable String applicationPropertyName;

	private final @Nullable String includePropertyName;

	LoggingSystemProperty(String environmentVariableName) {
		this(environmentVariableName, null);
	}

	LoggingSystemProperty(String environmentVariableName, @Nullable String applicationPropertyName) {
		this(environmentVariableName, applicationPropertyName, null);
	}

	LoggingSystemProperty(String environmentVariableName, @Nullable String applicationPropertyName,
			@Nullable String includePropertyName) {
		this.environmentVariableName = environmentVariableName;
		this.applicationPropertyName = applicationPropertyName;
		this.includePropertyName = includePropertyName;
	}

	/**
	 * 返回访问此属性的环境变量名。
	 *
	 * @return the environment variable name 环境变量名
	 */
	public String getEnvironmentVariableName() {
		return this.environmentVariableName;
	}

	/**
	 * 返回设置此属性的应用属性名。
	 *
	 * @return the application property name 应用属性名
	 * @since 3.4.0
	 */
	public @Nullable String getApplicationPropertyName() {
		return this.applicationPropertyName;
	}

	@Nullable String getIncludePropertyName() {
		return this.includePropertyName;
	}

}
