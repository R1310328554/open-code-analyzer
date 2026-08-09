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

import org.jspecify.annotations.Nullable;

import org.springframework.boot.origin.Origin;
import org.springframework.util.Assert;

/**
 * 找不到 {@link ConfigDataLocation} 时抛出的 {@link ConfigDataNotFoundException}。
 *
 * @author Phillip Webb
 * @since 2.4.0
 */
public class ConfigDataLocationNotFoundException extends ConfigDataNotFoundException {

	private final ConfigDataLocation location;

	/**
	 * 创建新的 {@link ConfigDataLocationNotFoundException} 实例。
	 *
	 * @param location 找不到的配置位置
	 */
	public ConfigDataLocationNotFoundException(ConfigDataLocation location) {
		this(location, null);
	}

	/**
	 * 创建新的 {@link ConfigDataLocationNotFoundException} 实例。
	 *
	 * @param location 找不到的配置位置
	 * @param cause 异常原因
	 */
	public ConfigDataLocationNotFoundException(ConfigDataLocation location, @Nullable Throwable cause) {
		this(location, getMessage(location), cause);
	}

	/**
	 * 创建新的 {@link ConfigDataLocationNotFoundException} 实例。
	 *
	 * @param location 找不到的配置位置
	 * @param message 异常消息
	 * @param cause 异常原因
	 * @since 2.4.7
	 */
	public ConfigDataLocationNotFoundException(ConfigDataLocation location, String message, @Nullable Throwable cause) {
		super(message, cause);
		Assert.notNull(location, "'location' must not be null");
		this.location = location;
	}

	/**
	 * 返回找不到的配置位置。
	 *
	 * @return 配置位置
	 */
	public ConfigDataLocation getLocation() {
		return this.location;
	}

	@Override
	public @Nullable Origin getOrigin() {
		return Origin.from(this.location);
	}

	@Override
	public String getReferenceDescription() {
		return getReferenceDescription(this.location);
	}

	private static String getMessage(ConfigDataLocation location) {
		return String.format("Config data %s cannot be found", getReferenceDescription(location));
	}

	private static String getReferenceDescription(ConfigDataLocation location) {
		return String.format("location '%s'", location);
	}

}
