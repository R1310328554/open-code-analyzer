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
import org.springframework.boot.origin.OriginProvider;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * 用户指定的位置，可 {@link ConfigDataLocationResolver 解析} 为一个或多个 {@link ConfigDataResource 配置数据资源}。
 * {@link ConfigDataLocation} 是对 {@link String} 值的简单包装；格式取决于底层技术，
 * 通常为前缀加路径的 URL 风格，例如 {@code crypt:somehost/somepath}。
 * <p>
 * 位置可为必选或 {@link #isOptional() 可选}；可选位置以 {@code optional:} 为前缀。
 *
 * @author Phillip Webb
 * @since 2.4.0
 */
public final class ConfigDataLocation implements OriginProvider {

	private static final ConfigDataLocation EMPTY = new ConfigDataLocation(false, "", null);

	/**
	 * 表示 {@link ConfigDataResource} 为可选的前缀。
	 */
	public static final String OPTIONAL_PREFIX = "optional:";

	private final boolean optional;

	private final String value;

	private final @Nullable Origin origin;

	private ConfigDataLocation(boolean optional, String value, @Nullable Origin origin) {
		this.value = value;
		this.optional = optional;
		this.origin = origin;
	}

	/**
	 * 返回位置是否可选（应忽略 {@link ConfigDataNotFoundException}）。
	 *
	 * @return 位置是否可选
	 */
	public boolean isOptional() {
		return this.optional;
	}

	/**
	 * 返回位置值（始终不含用户指定的 {@code optional:} 前缀）。
	 *
	 * @return 位置值
	 */
	public String getValue() {
		return this.value;
	}

	/**
	 * 判断 {@link #getValue()} 是否具有指定前缀。
	 *
	 * @param prefix 要检查的前缀
	 * @return 值是否具有该前缀
	 */
	public boolean hasPrefix(String prefix) {
		return this.value.startsWith(prefix);
	}

	/**
	 * 返回移除指定前缀后的 {@link #getValue()}；若无该前缀则原样返回。
	 *
	 * @param prefix 要检查的前缀
	 * @return 移除前缀后的值
	 */
	public String getNonPrefixedValue(String prefix) {
		return (!hasPrefix(prefix)) ? this.value : this.value.substring(prefix.length());
	}

	@Override
	public @Nullable Origin getOrigin() {
		return this.origin;
	}

	/**
	 * 以 {@code ";"} 为分隔符拆分本 {@link ConfigDataLocation}，返回 {@link ConfigDataLocation} 数组。
	 *
	 * @return 拆分后的位置
	 * @since 2.4.7
	 */
	public ConfigDataLocation[] split() {
		return split(";");
	}

	/**
	 * 以指定分隔符拆分本 {@link ConfigDataLocation}，返回 {@link ConfigDataLocation} 数组。
	 *
	 * @param delimiter 分隔符
	 * @return 拆分后的位置
	 * @since 2.4.7
	 */
	public ConfigDataLocation[] split(String delimiter) {
		Assert.state(!this.value.isEmpty(), "Unable to split empty locations");
		String[] values = StringUtils.delimitedListToStringArray(toString(), delimiter);
		ConfigDataLocation[] result = new ConfigDataLocation[values.length];
		for (int i = 0; i < values.length; i++) {
			int index = i;
			ConfigDataLocation configDataLocation = of(values[index]);
			result[i] = configDataLocation.withOrigin(getOrigin());
		}
		return result;
	}

	/**
	 * 创建带有指定 {@link Origin} 的新 {@link ConfigDataLocation}。
	 *
	 * @param origin 要设置的来源
	 * @return 新的 {@link ConfigDataLocation} 实例
	 */
	ConfigDataLocation withOrigin(@Nullable Origin origin) {
		return new ConfigDataLocation(this.optional, this.value, origin);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		ConfigDataLocation other = (ConfigDataLocation) obj;
		return this.value.equals(other.value);
	}

	@Override
	public int hashCode() {
		return this.value.hashCode();
	}

	@Override
	public String toString() {
		return (!this.optional) ? this.value : OPTIONAL_PREFIX + this.value;
	}

	/**
	 * 从字符串创建新 {@link ConfigDataLocation} 的工厂方法。
	 *
	 * @param location 位置字符串
	 * @return {@link ConfigDataLocation}（可能为空）
	 */
	public static ConfigDataLocation of(@Nullable String location) {
		boolean optional = location != null && location.startsWith(OPTIONAL_PREFIX);
		String value = (location != null && optional) ? location.substring(OPTIONAL_PREFIX.length()) : location;
		return (StringUtils.hasText(value)) ? new ConfigDataLocation(optional, value, null) : EMPTY;
	}

	static boolean isNotEmpty(@Nullable ConfigDataLocation location) {
		return (location != null) && !location.getValue().isEmpty();
	}

}
