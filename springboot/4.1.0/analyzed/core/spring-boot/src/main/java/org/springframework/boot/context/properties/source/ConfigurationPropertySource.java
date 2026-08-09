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

package org.springframework.boot.context.properties.source;

import java.util.function.Predicate;

import org.jspecify.annotations.Nullable;

import org.springframework.boot.origin.OriginTrackedValue;
import org.springframework.core.env.PropertySource;
import org.springframework.util.StringUtils;

/**
 * {@link ConfigurationProperty 配置属性} 的来源。
 *
 * @author Phillip Webb
 * @author Madhura Bhave
 * @since 2.0.0
 * @see ConfigurationPropertyName
 * @see OriginTrackedValue
 * @see #getConfigurationProperty(ConfigurationPropertyName)
 */
@FunctionalInterface
public interface ConfigurationPropertySource {

	/**
	 * 从属性源返回单个 {@link ConfigurationProperty}；找不到属性时返回 {@code null}。
	 *
	 * @param name 属性名称
	 * @return the associated object or {@code null} 关联对象，或 {@code null}
	 */
	@Nullable ConfigurationProperty getConfigurationProperty(ConfigurationPropertyName name);

	/**
	 * 判断属性源是否包含指定名称的任意后代。若能确定则返回
	 * {@link ConfigurationPropertyState#PRESENT} 或 {@link ConfigurationPropertyState#ABSENT}；
	 * 无法确定时返回 {@link ConfigurationPropertyState#UNKNOWN}。
	 *
	 * @param name 要检查的名称
	 * @return if the source contains any descendants 是否包含后代
	 */
	default ConfigurationPropertyState containsDescendantOf(ConfigurationPropertyName name) {
		return ConfigurationPropertyState.UNKNOWN;
	}

	/**
	 * 返回此属性源的过滤变体，仅包含匹配给定 {@link Predicate} 的名称。
	 *
	 * @param filter 匹配过滤器
	 * @return a filtered {@link ConfigurationPropertySource} instance 过滤后的属性源实例
	 */
	default ConfigurationPropertySource filter(Predicate<ConfigurationPropertyName> filter) {
		return new FilteredConfigurationPropertiesSource(this, filter);
	}

	/**
	 * 返回支持名称别名的属性源变体。
	 *
	 * @param aliases 为给定名称返回别名流的函数
	 * @return a {@link ConfigurationPropertySource} instance supporting name aliases 支持名称别名的属性源实例
	 */
	default ConfigurationPropertySource withAliases(ConfigurationPropertyNameAliases aliases) {
		return new AliasedConfigurationPropertySource(this, aliases);
	}

	/**
	 * 返回支持前缀的属性源变体。
	 *
	 * @param prefix 属性源中属性的前缀
	 * @return a {@link ConfigurationPropertySource} instance supporting a prefix 支持前缀的属性源实例
	 * @since 2.5.0
	 */
	default ConfigurationPropertySource withPrefix(@Nullable String prefix) {
		return (StringUtils.hasText(prefix)) ? new PrefixedConfigurationPropertySource(this, prefix) : this;
	}

	/**
	 * 返回实际提供属性的底层源。
	 *
	 * @return the underlying property source or {@code null} 底层属性源，或 {@code null}
	 */
	default @Nullable Object getUnderlyingSource() {
		return null;
	}

	/**
	 * 从给定 Spring {@link PropertySource} 适配出新的 {@link ConfigurationPropertySource}；
	 * 无法适配时返回 {@code null}。
	 *
	 * @param source 要适配的 Spring 属性源
	 * @return an adapted source or {@code null} {@link SpringConfigurationPropertySource} 适配后的属性源，或 {@code null}
	 * @since 2.4.0
	 */
	static @Nullable ConfigurationPropertySource from(PropertySource<?> source) {
		if (source instanceof ConfigurationPropertySourcesPropertySource) {
			return null;
		}
		return SpringConfigurationPropertySource.from(source);
	}

}
