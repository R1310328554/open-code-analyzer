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

package org.springframework.boot.logging.logback;

/**
 * 可供日志配置文件后续使用的 Logback 滚动策略系统属性枚举。
 *
 * @author Phillip Webb
 * @since 3.2.0
 * @see LogbackLoggingSystemProperties
 */
public enum RollingPolicySystemProperty {

	/**
	 * 滚动后日志文件名模式的 logging system property。
	 */
	FILE_NAME_PATTERN("file-name-pattern"),

	/**
	 * 启动时清理历史日志标志的 logging system property。
	 */
	CLEAN_HISTORY_ON_START("clean-history-on-start"),

	/**
	 * 文件日志最大大小的 logging system property。
	 */
	MAX_FILE_SIZE("max-file-size"),

	/**
	 * 文件日志总大小上限的 logging system property。
	 */
	TOTAL_SIZE_CAP("total-size-cap"),

	/**
	 * 文件日志最大保留历史的 logging system property。
	 */
	MAX_HISTORY("max-history");

	private final String environmentVariableName;

	private final String applicationPropertyName;

	RollingPolicySystemProperty(String applicationPropertyName) {
		this.environmentVariableName = "LOGBACK_ROLLINGPOLICY_" + name();
		this.applicationPropertyName = "logging.logback.rollingpolicy." + applicationPropertyName;
	}

	/**
	 * 返回可用于访问此属性的环境变量名。
	 *
	 * @return the environment variable name 环境变量名
	 */
	public String getEnvironmentVariableName() {
		return this.environmentVariableName;
	}

	String getApplicationPropertyName() {
		return this.applicationPropertyName;
	}

}
