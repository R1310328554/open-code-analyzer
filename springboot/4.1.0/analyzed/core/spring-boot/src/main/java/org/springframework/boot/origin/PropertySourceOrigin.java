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

package org.springframework.boot.origin;

import org.jspecify.annotations.Nullable;

import org.springframework.core.env.PropertySource;
import org.springframework.util.Assert;

/**
 * 来自 {@link PropertySource} 的 {@link Origin} 实现。
 *
 * @author Phillip Webb
 * @since 2.0.0
 */
public class PropertySourceOrigin implements Origin, OriginProvider {

	private final PropertySource<?> propertySource;

	private final String propertyName;

	private final @Nullable Origin origin;

	/**
	 * 创建新的 {@link PropertySourceOrigin} 实例。
	 *
	 * @param propertySource the property source 属性源
	 * @param propertyName the name from the property source 属性源中的属性名
	 */
	public PropertySourceOrigin(PropertySource<?> propertySource, String propertyName) {
		this(propertySource, propertyName, null);
	}

	/**
	 * 创建新的 {@link PropertySourceOrigin} 实例。
	 *
	 * @param propertySource the property source 属性源
	 * @param propertyName the name from the property source 属性源中的属性名
	 * @param origin the actual origin for the source if known 若已知则为属性源的实际来源
	 * @since 3.2.8
	 */
	public PropertySourceOrigin(PropertySource<?> propertySource, String propertyName, @Nullable Origin origin) {
		Assert.notNull(propertySource, "'propertySource' must not be null");
		Assert.hasLength(propertyName, "'propertyName' must not be empty");
		this.propertySource = propertySource;
		this.propertyName = propertyName;
		this.origin = origin;
	}

	/**
	 * 返回来源 {@link PropertySource}。
	 *
	 * @return the origin property source 来源属性源
	 */
	public PropertySource<?> getPropertySource() {
		return this.propertySource;
	}

	/**
	 * 返回从 {@link #getPropertySource() 属性源} 获取原始值时使用的属性名。
	 *
	 * @return the origin property name 来源属性名
	 */
	public String getPropertyName() {
		return this.propertyName;
	}

	/**
	 * 若已知则返回属性源的实际来源。
	 *
	 * @return the actual source origin 实际来源
	 * @since 3.2.8
	 */
	@Override
	public @Nullable Origin getOrigin() {
		return this.origin;
	}

	@Override
	public @Nullable Origin getParent() {
		return (this.origin != null) ? this.origin.getParent() : null;
	}

	@Override
	public String toString() {
		return (this.origin != null) ? this.origin.toString()
				: "\"" + this.propertyName + "\" from property source \"" + this.propertySource.getName() + "\"";
	}

	/**
	 * 获取给定 {@link PropertySource} 与 {@code propertyName} 对应的 {@link Origin}。
	 * 优先返回 {@link OriginLookup} 的结果，否则返回 {@link PropertySourceOrigin}。
	 *
	 * @param propertySource the origin property source 来源属性源
	 * @param name the property name 属性名
	 * @return the property origin 属性来源
	 */
	public static Origin get(PropertySource<?> propertySource, String name) {
		Origin origin = OriginLookup.getOrigin(propertySource, name);
		return (origin instanceof PropertySourceOrigin) ? origin
				: new PropertySourceOrigin(propertySource, name, origin);
	}

}
