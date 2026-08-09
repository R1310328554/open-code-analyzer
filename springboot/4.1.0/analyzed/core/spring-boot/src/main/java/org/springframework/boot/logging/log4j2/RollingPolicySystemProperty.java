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

package org.springframework.boot.logging.log4j2;

/**
 * 可供日志配置文件后续使用的 Log4j2 滚动策略 system properties。
 *
 * @author HoJoo Moon
 * @author Stephane Nicoll
 * @since 4.1.0
 * @see Log4j2LoggingSystemProperties
 */
public enum RollingPolicySystemProperty {

	/**
	 * 滚动后日志文件名模式的 logging system property。
	 */
	FILE_NAME_PATTERN("file-name-pattern"),

	/**
	 * 文件日志最大大小的 logging system property。
	 */
	MAX_FILE_SIZE("max-file-size"),

	/**
	 * 文件日志最大保留历史的 logging system property。
	 */
	MAX_HISTORY("max-history"),

	/**
	 * {@linkplain RollingPolicyStrategy 滚动策略} 的 logging system property。
	 */
	STRATEGY("strategy"),

	/**
	 * 滚动策略时间间隔的 logging system property。
	 */
	TIME_INTERVAL("time-interval"),

	/**
	 * 滚动策略 time modulate 标志的 logging system property。
	 */
	TIME_MODULATE("time-modulate"),

	/**
	 * 基于 cron 调度的 logging system property。
	 */
	CRON("cron");

	private final String environmentVariableName;

	private final String applicationPropertyName;

	RollingPolicySystemProperty(String applicationPropertyName) {
		this.environmentVariableName = "LOG4J2_ROLLINGPOLICY_" + name();
		this.applicationPropertyName = "logging.log4j2.rollingpolicy." + applicationPropertyName;
	}

	/**
	 * 返回可用于访问此属性的环境变量名。
	 *
	 * @return 环境变量名
	 */
	public String getEnvironmentVariableName() {
		return this.environmentVariableName;
	}

	String getApplicationPropertyName() {
		return this.applicationPropertyName;
	}

}
