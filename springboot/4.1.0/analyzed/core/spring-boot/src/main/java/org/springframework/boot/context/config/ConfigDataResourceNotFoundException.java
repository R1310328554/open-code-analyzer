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

package org.springframework.boot.context.config;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import org.jspecify.annotations.Nullable;

import org.springframework.boot.origin.Origin;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

/**
 * 找不到 {@link ConfigDataResource} 时抛出的 {@link ConfigDataNotFoundException}。
 *
 * @author Phillip Webb
 * @since 2.4.0
 */
public class ConfigDataResourceNotFoundException extends ConfigDataNotFoundException {

	private final ConfigDataResource resource;

	private final @Nullable ConfigDataLocation location;

	/**
	 * 创建新的 {@link ConfigDataResourceNotFoundException} 实例。
	 *
	 * @param resource 找不到的配置资源
	 */
	public ConfigDataResourceNotFoundException(ConfigDataResource resource) {
		this(resource, null);
	}

	/**
	 * 创建新的 {@link ConfigDataResourceNotFoundException} 实例。
	 *
	 * @param resource 找不到的配置资源
	 * @param cause 异常原因
	 */
	public ConfigDataResourceNotFoundException(ConfigDataResource resource, @Nullable Throwable cause) {
		this(resource, null, cause);
	}

	private ConfigDataResourceNotFoundException(ConfigDataResource resource, @Nullable ConfigDataLocation location,
			@Nullable Throwable cause) {
		super(getMessage(resource, location), cause);
		Assert.notNull(resource, "'resource' must not be null");
		this.resource = resource;
		this.location = location;
	}

	/**
	 * 返回找不到的配置资源。
	 *
	 * @return 配置资源
	 */
	public ConfigDataResource getResource() {
		return this.resource;
	}

	/**
	 * 返回解析以确定该资源的原始位置。
	 *
	 * @return 配置位置；若无可用位置则为 {@code null}
	 */
	public @Nullable ConfigDataLocation getLocation() {
		return this.location;
	}

	@Override
	public @Nullable Origin getOrigin() {
		return Origin.from(this.location);
	}

	@Override
	public String getReferenceDescription() {
		return getReferenceDescription(this.resource, this.location);
	}

	/**
	 * 创建带位置信息的新 {@link ConfigDataResourceNotFoundException} 实例。
	 *
	 * @param location 要设置的位置
	 * @return 新的 {@link ConfigDataResourceNotFoundException} 实例
	 */
	ConfigDataResourceNotFoundException withLocation(ConfigDataLocation location) {
		return new ConfigDataResourceNotFoundException(this.resource, location, getCause());
	}

	private static String getMessage(ConfigDataResource resource, @Nullable ConfigDataLocation location) {
		return String.format("Config data %s cannot be found", getReferenceDescription(resource, location));
	}

	private static String getReferenceDescription(ConfigDataResource resource, @Nullable ConfigDataLocation location) {
		String description = String.format("resource '%s'", resource);
		if (location != null) {
			description += String.format(" via location '%s'", location);
		}
		return description;
	}

	/**
	 * 若指定 {@link Path} 不存在则抛出 {@link ConfigDataNotFoundException}。
	 *
	 * @param resource 配置数据资源
	 * @param pathToCheck 待检查的路径
	 */
	public static void throwIfDoesNotExist(ConfigDataResource resource, Path pathToCheck) {
		throwIfNot(resource, Files.exists(pathToCheck));
	}

	/**
	 * 若指定 {@link File} 不存在则抛出 {@link ConfigDataNotFoundException}。
	 *
	 * @param resource 配置数据资源
	 * @param fileToCheck 待检查的文件
	 */
	public static void throwIfDoesNotExist(ConfigDataResource resource, File fileToCheck) {
		throwIfNot(resource, fileToCheck.exists());
	}

	/**
	 * 若指定 {@link Resource} 不存在则抛出 {@link ConfigDataNotFoundException}。
	 *
	 * @param resource 配置数据资源
	 * @param resourceToCheck 待检查的资源
	 */
	public static void throwIfDoesNotExist(ConfigDataResource resource, Resource resourceToCheck) {
		throwIfNot(resource, resourceToCheck.exists());
	}

	private static void throwIfNot(ConfigDataResource resource, boolean check) {
		if (!check) {
			throw new ConfigDataResourceNotFoundException(resource);
		}
	}

}
