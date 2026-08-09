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

import java.util.Iterator;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.jspecify.annotations.Nullable;

import org.springframework.boot.origin.OriginTrackedValue;
import org.springframework.util.StringUtils;

/**
 * 具有完整 {@link Iterable} 条目集的 {@link ConfigurationPropertySource}。
 * 此接口的实现<strong>必须</strong>能够遍历所有包含的配置属性。
 * {@link #getConfigurationProperty(ConfigurationPropertyName)} 返回的任何 {@code non-null} 结果
 * 也必须在 {@link #iterator() 迭代器} 中有对应条目。
 *
 * @author Phillip Webb
 * @author Madhura Bhave
 * @since 2.0.0
 * @see ConfigurationPropertyName
 * @see OriginTrackedValue
 * @see #getConfigurationProperty(ConfigurationPropertyName)
 * @see #iterator()
 * @see #stream()
 */
public interface IterableConfigurationPropertySource
		extends ConfigurationPropertySource, Iterable<ConfigurationPropertyName> {

	/**
	 * 返回此源所管理的 {@link ConfigurationPropertyName 名称} 的迭代器。
	 *
	 * @return 迭代器（永不为 {@code null}）
	 */
	@Override
	default Iterator<ConfigurationPropertyName> iterator() {
		return stream().iterator();
	}

	/**
	 * 返回此源所管理的 {@link ConfigurationPropertyName 名称} 的顺序 {@code Stream}。
	 *
	 * @return 名称流（永不为 {@code null}）
	 */
	Stream<ConfigurationPropertyName> stream();

	@Override
	default ConfigurationPropertyState containsDescendantOf(ConfigurationPropertyName name) {
		return ConfigurationPropertyState.search(this, name::isAncestorOf);
	}

	@Override
	default IterableConfigurationPropertySource filter(Predicate<ConfigurationPropertyName> filter) {
		return new FilteredIterableConfigurationPropertiesSource(this, filter);
	}

	@Override
	default IterableConfigurationPropertySource withAliases(ConfigurationPropertyNameAliases aliases) {
		return new AliasedIterableConfigurationPropertySource(this, aliases);
	}

	@Override
	default IterableConfigurationPropertySource withPrefix(@Nullable String prefix) {
		return (StringUtils.hasText(prefix)) ? new PrefixedIterableConfigurationPropertySource(this, prefix) : this;
	}

}
