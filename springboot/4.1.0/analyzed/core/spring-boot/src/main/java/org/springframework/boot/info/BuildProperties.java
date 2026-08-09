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

package org.springframework.boot.info;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

import org.jspecify.annotations.Nullable;

import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.boot.info.BuildProperties.BuildPropertiesRuntimeHints;
import org.springframework.context.annotation.ImportRuntimeHints;

/**
 * 提供构建相关信息，例如 group 与 artifact。
 *
 * @author Stephane Nicoll
 * @since 1.4.0
 */
@ImportRuntimeHints(BuildPropertiesRuntimeHints.class)
public class BuildProperties extends InfoProperties {

	/**
	 * 使用指定条目创建实例。
	 *
	 * @param entries 要暴露的信息
	 */
	public BuildProperties(Properties entries) {
		super(processEntries(entries));
	}

	/**
	 * 返回项目的 groupId 或 {@code null}。
	 *
	 * @return group
	 */
	public @Nullable String getGroup() {
		return get("group");
	}

	/**
	 * 返回项目的 artifactId 或 {@code null}。
	 *
	 * @return artifact
	 */
	public @Nullable String getArtifact() {
		return get("artifact");
	}

	/**
	 * 返回项目名称或 {@code null}。
	 *
	 * @return 名称
	 */
	public @Nullable String getName() {
		return get("name");
	}

	/**
	 * 返回项目版本或 {@code null}。
	 *
	 * @return 版本
	 */
	public @Nullable String getVersion() {
		return get("version");
	}

	/**
	 * 返回构建时间戳或 {@code null}。
	 * <p>
	 * 若原始值无法正确解析，仍可通过 {@code time} 键获取。
	 *
	 * @return 构建时间
	 * @see #get(String)
	 */
	public @Nullable Instant getTime() {
		return getInstant("time");
	}

	private static Properties processEntries(Properties properties) {
		coerceDate(properties, "time");
		return properties;
	}

	private static void coerceDate(Properties properties, String key) {
		String value = properties.getProperty(key);
		if (value != null) {
			try {
				String updatedValue = String
					.valueOf(DateTimeFormatter.ISO_INSTANT.parse(value, Instant::from).toEpochMilli());
				properties.setProperty(key, updatedValue);
			}
			catch (DateTimeException ex) {
				// Ignore and store the original value
			}
		}
	}

	static class BuildPropertiesRuntimeHints implements RuntimeHintsRegistrar {

		@Override
		public void registerHints(RuntimeHints hints, @Nullable ClassLoader classLoader) {
			hints.resources().registerPattern("META-INF/build-info.properties");
		}

	}

}
