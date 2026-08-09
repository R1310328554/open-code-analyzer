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

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;

import org.springframework.lang.Contract;
import org.springframework.util.Assert;

/**
 * 当配置了多个互斥配置属性时抛出的异常。
 *
 * @author Andy Wilkinson
 * @author Phillip Webb
 * @since 2.6.0
 */
@SuppressWarnings("serial")
public class MutuallyExclusiveConfigurationPropertiesException extends RuntimeException {

	private final Set<String> configuredNames;

	private final Set<String> mutuallyExclusiveNames;

	/**
	 * 当两个或多个互斥配置属性已被配置时，创建新实例。
	 *
	 * @param configuredNames 已配置属性的名称
	 * @param mutuallyExclusiveNames 互斥属性的名称
	 */
	public MutuallyExclusiveConfigurationPropertiesException(Collection<String> configuredNames,
			Collection<String> mutuallyExclusiveNames) {
		this(asSet(configuredNames), asSet(mutuallyExclusiveNames));
	}

	private MutuallyExclusiveConfigurationPropertiesException(Set<String> configuredNames,
			Set<String> mutuallyExclusiveNames) {
		super(buildMessage(mutuallyExclusiveNames, configuredNames));
		this.configuredNames = configuredNames;
		this.mutuallyExclusiveNames = mutuallyExclusiveNames;
	}

	/**
	 * 返回已配置属性的名称。
	 *
	 * @return 已配置属性的名称
	 */
	public Set<String> getConfiguredNames() {
		return this.configuredNames;
	}

	/**
	 * 返回互斥属性的名称。
	 *
	 * @return 互斥属性的名称
	 */
	public Set<String> getMutuallyExclusiveNames() {
		return this.mutuallyExclusiveNames;
	}

	@Contract("null -> null; !null -> !null")
	private static @Nullable Set<String> asSet(@Nullable Collection<String> collection) {
		return (collection != null) ? new LinkedHashSet<>(collection) : null;
	}

	private static String buildMessage(Set<String> mutuallyExclusiveNames, Set<String> configuredNames) {
		Assert.isTrue(configuredNames != null && configuredNames.size() > 1,
				"'configuredNames' must contain 2 or more names");
		Assert.isTrue(mutuallyExclusiveNames != null && mutuallyExclusiveNames.size() > 1,
				"'mutuallyExclusiveNames' must contain 2 or more names");
		return "The configuration properties '" + String.join(", ", mutuallyExclusiveNames)
				+ "' are mutually exclusive and '" + String.join(", ", configuredNames)
				+ "' have been configured together";
	}

	/**
	 * 若一组条目中定义了多个非 null 值，则抛出新的 {@link MutuallyExclusiveConfigurationPropertiesException}。
	 *
	 * @param entries 用于填充待检查条目的 consumer
	 */
	public static void throwIfMultipleNonNullValuesIn(Consumer<Map<String, @Nullable Object>> entries) {
		Predicate<@Nullable Object> isNonNull = Objects::nonNull;
		throwIfMultipleMatchingValuesIn(entries, isNonNull);
	}

	/**
	 * 若一组条目中存在多个匹配给定谓词的值，则抛出新的 {@link MutuallyExclusiveConfigurationPropertiesException}。
	 *
	 * @param <V> 值类型
	 * @param entries 用于填充待检查条目的 consumer
	 * @param predicate 用于检查匹配值的谓词
	 * @since 3.3.7
	 */
	public static <V> void throwIfMultipleMatchingValuesIn(Consumer<Map<String, @Nullable V>> entries,
			Predicate<@Nullable V> predicate) {
		Map<String, V> map = new LinkedHashMap<>();
		entries.accept(map);
		Set<String> configuredNames = map.entrySet()
			.stream()
			.filter((entry) -> predicate.test(entry.getValue()))
			.map(Map.Entry::getKey)
			.collect(Collectors.toCollection(LinkedHashSet::new));
		if (configuredNames.size() > 1) {
			throw new MutuallyExclusiveConfigurationPropertiesException(configuredNames, map.keySet());
		}
	}

}
