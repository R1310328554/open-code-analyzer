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

package org.springframework.boot.logging.structured;

import org.jspecify.annotations.Nullable;

/**
 * Spring Boot 支持的常见结构化日志格式。
 *
 * @author Moritz Halbritter
 * @author Phillip Webb
 * @since 3.4.0
 */
public enum CommonStructuredLogFormat {

	/**
	 * <a href="https://www.elastic.co/guide/en/ecs/current/ecs-log.html">Elastic Common
	 * Schema</a>（ECS）日志格式。
	 */
	ELASTIC_COMMON_SCHEMA("ecs"),

	/**
	 * <a href="https://go2docs.graylog.org/current/getting_in_log_data/gelf.html">Graylog
	 * Extended Log Format</a>（GELF）日志格式。
	 */
	GRAYLOG_EXTENDED_LOG_FORMAT("gelf"),

	/**
	 * <a href=
	 * "https://github.com/logfellow/logstash-logback-encoder?tab=readme-ov-file#standard-fields">Logstash</a>
	 * 日志格式。
	 */
	LOGSTASH("logstash");

	private final String id;

	CommonStructuredLogFormat(String id) {
		this.id = id;
	}

	/**
	 * 返回此格式的 ID。
	 *
	 * @return the format identifier 格式标识符
	 */
	String getId() {
		return this.id;
	}

	/**
	 * 查找给定 ID 对应的 {@link CommonStructuredLogFormat}。
	 *
	 * @param id the format identifier 格式标识符
	 * @return the associated {@link CommonStructuredLogFormat} or {@code null} 关联格式或 {@code null}
	 */
	static @Nullable CommonStructuredLogFormat forId(String id) {
		for (CommonStructuredLogFormat candidate : values()) {
			if (candidate.getId().equalsIgnoreCase(id)) {
				return candidate;
			}
		}
		return null;
	}

}
