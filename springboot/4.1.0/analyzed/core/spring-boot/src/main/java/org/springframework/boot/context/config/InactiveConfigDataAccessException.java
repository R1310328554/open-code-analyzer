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

import org.springframework.boot.context.properties.source.ConfigurationProperty;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;
import org.springframework.boot.origin.Origin;
import org.springframework.core.env.PropertySource;
import org.springframework.util.Assert;

/**
 * 尝试针对非活动的 {@link ConfigData} 属性源解析属性时抛出的异常。
 * 用于防止用户误指定永远无法解析的属性。
 *
 * @author Phillip Webb
 * @author Madhura Bhave
 * @since 2.4.0
 */
public class InactiveConfigDataAccessException extends ConfigDataException {

	private final PropertySource<?> propertySource;

	private final @Nullable ConfigDataResource location;

	private final String propertyName;

	private final @Nullable Origin origin;

	/**
	 * 创建新的 {@link InactiveConfigDataAccessException} 实例。
	 *
	 * @param propertySource 非活动的属性源
	 * @param location 属性源的 {@link ConfigDataResource}；若源非来自 {@link ConfigData} 则为 {@code null}
	 * @param propertyName 属性名
	 * @param origin 属性来源，或 {@code null}
	 */
	InactiveConfigDataAccessException(PropertySource<?> propertySource, @Nullable ConfigDataResource location,
			String propertyName, @Nullable Origin origin) {
		super(getMessage(propertySource, location, propertyName, origin), null);
		this.propertySource = propertySource;
		this.location = location;
		this.propertyName = propertyName;
		this.origin = origin;
	}

	private static String getMessage(PropertySource<?> propertySource, @Nullable ConfigDataResource location,
			String propertyName, @Nullable Origin origin) {
		StringBuilder message = new StringBuilder("Inactive property source '");
		message.append(propertySource.getName());
		if (location != null) {
			message.append("' imported from location '");
			message.append(location);
		}
		message.append("' cannot contain property '");
		message.append(propertyName);
		message.append("'");
		if (origin != null) {
			message.append(" [origin: ");
			message.append(origin);
			message.append("]");
		}
		return message.toString();
	}

	/**
	 * 返回包含该属性的非活动属性源。
	 *
	 * @return 属性源
	 */
	public PropertySource<?> getPropertySource() {
		return this.propertySource;
	}

	/**
	 * 返回属性源的 {@link ConfigDataResource}；若源非来自 {@link ConfigData} 则为 {@code null}。
	 *
	 * @return 配置数据位置，或 {@code null}
	 */
	public @Nullable ConfigDataResource getLocation() {
		return this.location;
	}

	/**
	 * 返回属性名。
	 *
	 * @return 属性名
	 */
	public String getPropertyName() {
		return this.propertyName;
	}

	/**
	 * 返回属性来源，或 {@code null}。
	 *
	 * @return 属性来源
	 */
	public @Nullable Origin getOrigin() {
		return this.origin;
	}

	/**
	 * 若给定 {@link ConfigDataEnvironmentContributor} 包含该属性则抛出
	 * {@link InactiveConfigDataAccessException}。
	 *
	 * @param contributor 待检查的 contributor
	 * @param name 待检查的属性名
	 */
	static void throwIfPropertyFound(ConfigDataEnvironmentContributor contributor, ConfigurationPropertyName name) {
		ConfigurationPropertySource source = contributor.getConfigurationPropertySource();
		ConfigurationProperty property = (source != null) ? source.getConfigurationProperty(name) : null;
		if (property != null) {
			PropertySource<?> propertySource = contributor.getPropertySource();
			ConfigDataResource location = contributor.getResource();
			Assert.state(propertySource != null, "'propertySource' must not be null");
			throw new InactiveConfigDataAccessException(propertySource, location, name.toString(),
					property.getOrigin());
		}
	}

}
