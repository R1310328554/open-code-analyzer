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

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import org.jspecify.annotations.Nullable;

import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.boot.info.GitProperties.GitPropertiesRuntimeHints;
import org.springframework.context.annotation.ImportRuntimeHints;

/**
 * 提供 Git 相关信息，例如 commit id 与时间。
 *
 * @author Stephane Nicoll
 * @since 1.4.0
 */
@ImportRuntimeHints(GitPropertiesRuntimeHints.class)
public class GitProperties extends InfoProperties {

	static final Set<Coercer> coercers = Set.of(Coercer.milliseconds(),
			Coercer.dateTimePattern("yyyy-MM-dd'T'HH:mm:ssXXX"), Coercer.dateTimePattern("yyyy-MM-dd'T'HH:mm:ssZ"));

	public GitProperties(Properties entries) {
		super(processEntries(entries));
	}

	/**
	 * 返回分支名称或 {@code null}。
	 *
	 * @return 分支
	 */
	public @Nullable String getBranch() {
		return get("branch");
	}

	/**
	 * 返回完整 commit id 或 {@code null}。
	 *
	 * @return 完整 commit id
	 */
	public @Nullable String getCommitId() {
		return get("commit.id");
	}

	/**
	 * 返回缩写 commit id 或 {@code null}。
	 *
	 * @return 短 commit id
	 */
	public @Nullable String getShortCommitId() {
		String shortId = get("commit.id.abbrev");
		if (shortId != null) {
			return shortId;
		}
		String id = getCommitId();
		if (id == null) {
			return null;
		}
		return (id.length() > 7) ? id.substring(0, 7) : id;
	}

	/**
	 * 返回 commit 时间戳或 {@code null}。
	 * <p>
	 * 若原始值无法正确解析，仍可通过 {@code commit.time} 键获取。
	 *
	 * @return commit 时间
	 * @see #get(String)
	 */
	public @Nullable Instant getCommitTime() {
		return getInstant("commit.time");
	}

	private static Properties processEntries(Properties properties) {
		coercePropertyToEpoch(properties, "commit.time");
		coercePropertyToEpoch(properties, "build.time");
		Object commitId = properties.get("commit.id");
		if (commitId != null) {
			// Can get converted into a map, so we copy the entry as a nested key
			properties.put("commit.id.full", commitId);
		}
		return properties;
	}

	private static void coercePropertyToEpoch(Properties properties, String key) {
		String value = properties.getProperty(key);
		if (value != null) {
			properties.setProperty(key,
					coercers.stream()
						.map((coercer) -> coercer.apply(value))
						.filter(Objects::nonNull)
						.findFirst()
						.orElse(value));
		}
	}

	/**
	 * Git 属性的 {@link RuntimeHintsRegistrar}。
	 */
	static class GitPropertiesRuntimeHints implements RuntimeHintsRegistrar {

		@Override
		public void registerHints(RuntimeHints hints, @Nullable ClassLoader classLoader) {
			hints.resources().registerPattern("git.properties");
		}

	}

	/**
	 * 将源值转换为 epoch 时间的 Coercer。
	 */
	private record Coercer(Function<String, @Nullable Long> action, Predicate<RuntimeException> ignoredExceptions) {

		/**
		 * 尝试将指定值转换为 epoch 时间。
		 *
		 * @param value 待转换的值
		 * @return 毫秒 epoch 时间或 {@code null}
		 */
		@Nullable String apply(String value) {
			try {
				Long result = this.action.apply(value);
				return (result != null) ? String.valueOf(result) : null;
			}
			catch (RuntimeException ex) {
				if (this.ignoredExceptions.test(ex)) {
					return null;
				}
				throw ex;
			}
		}

		static Coercer milliseconds() {
			return new Coercer((value) -> Long.parseLong(value) * 1000, NumberFormatException.class::isInstance);
		}

		static Coercer dateTimePattern(String pattern) {
			return new Coercer(
					(value) -> DateTimeFormatter.ofPattern(pattern).parse(value, Instant::from).toEpochMilli(),
					DateTimeParseException.class::isInstance);
		}

	}

}
