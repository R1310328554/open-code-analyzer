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

import java.io.File;
import java.util.Properties;

import org.jspecify.annotations.Nullable;

import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertyResolver;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * 日志输出文件的引用。
 * 通过 {@code logging.file.name} 或 {@code logging.file.path} {@link Environment} 属性指定；
 * 未指定 {@code logging.file.name} 时，在 {@code logging.file.path} 目录写入 {@code spring.log}。
 *
 * @author Phillip Webb
 * @author Christian Carriere-Tisseur
 * @since 1.2.1
 * @see #get(PropertyResolver)
 */
public class LogFile {

	/**
	 * 包含日志文件名的 Spring 属性名。
	 * 可为绝对路径或相对当前目录的路径。
	 *
	 * @since 2.2.0
	 */
	public static final String FILE_NAME_PROPERTY = "logging.file.name";

	/**
	 * 包含日志文件写入目录的 Spring 属性名。
	 *
	 * @since 2.2.0
	 */
	public static final String FILE_PATH_PROPERTY = "logging.file.path";

	private final @Nullable String file;

	private final @Nullable String path;

	/**
	 * 创建新的 {@link LogFile} 实例。
	 *
	 * @param file 要写入的文件引用
	 */
	LogFile(String file) {
		this(file, null);
	}

	/**
	 * 创建新的 {@link LogFile} 实例。
	 *
	 * @param file 要写入的文件引用
	 * @param path 未指定 {@code file} 时使用的日志目录
	 */
	LogFile(@Nullable String file, @Nullable String path) {
		Assert.isTrue(StringUtils.hasLength(file) || StringUtils.hasLength(path), "'file' or 'path' must not be empty");
		this.file = file;
		this.path = path;
	}

	/**
	 * 将日志文件详情应用到 {@code LOG_PATH} 与 {@code LOG_FILE} 系统属性。
	 */
	public void applyToSystemProperties() {
		applyTo(System.getProperties());
	}

	/**
	 * 将日志文件详情应用到 {@code LOG_PATH} 与 {@code LOG_FILE} 映射项。
	 *
	 * @param properties 要应用到的属性
	 */
	public void applyTo(Properties properties) {
		put(properties, LoggingSystemProperty.LOG_PATH, this.path);
		put(properties, LoggingSystemProperty.LOG_FILE, toString());
	}

	private void put(Properties properties, LoggingSystemProperty property, @Nullable String value) {
		if (StringUtils.hasLength(value)) {
			properties.put(property.getEnvironmentVariableName(), value);
		}
	}

	@Override
	public String toString() {
		if (StringUtils.hasLength(this.file)) {
			return this.file;
		}
		return new File(this.path, "spring.log").getPath();
	}

	/**
	 * 从给定 Spring {@link Environment} 获取 {@link LogFile}。
	 *
	 * @param propertyResolver 用于获取日志属性的 {@link PropertyResolver}
	 * @return a {@link LogFile} or {@code null} LogFile 实例，无合适属性时为 {@code null}
	 */
	public static @Nullable LogFile get(PropertyResolver propertyResolver) {
		String file = propertyResolver.getProperty(FILE_NAME_PROPERTY);
		String path = propertyResolver.getProperty(FILE_PATH_PROPERTY);
		if (StringUtils.hasLength(file) || StringUtils.hasLength(path)) {
			return new LogFile(file, path);
		}
		return null;
	}

}
