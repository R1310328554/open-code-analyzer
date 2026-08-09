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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;

import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * 基于 <a href=
 * "https://www.w3.org/TR/trace-context/#examples-of-http-traceparent-headers">W3C</a>
 * 建议格式化日志关联 ID 的工具类。
 * <p>
 * 可通过逗号分隔的名称列表及期望解析长度进行配置，每项格式为 {@code "<name>(length)"}。
 * 例如 {@code "traceId(32),spanId(16)"} 指定 {@code traceId} 与 {@code spanId}，长度分别为 32 与 16。
 * <p>
 * 关联 ID 格式化为方括号内以短横线分隔的固定宽度字符串，末尾带空格；
 * 若所有命名项均无法解析则省略短横线。
 * <p>
 * 若 {@link #of(String)} 传入空规格，则使用 {@link #DEFAULT} 格式化器。
 *
 * @author Phillip Webb
 * @since 3.2.0
 * @see #of(String)
 * @see #of(Collection)
 */
public final class CorrelationIdFormatter {

	/**
	 * 默认 {@link CorrelationIdFormatter}。
	 */
	public static final CorrelationIdFormatter DEFAULT = CorrelationIdFormatter.of("traceId(32),spanId(16)");

	private final List<Part> parts;

	private final String blank;

	private CorrelationIdFormatter(List<Part> parts) {
		this.parts = parts;
		this.blank = String.format("[%s] ", parts.stream().map(Part::blank).collect(Collectors.joining(" ")));
	}

	/**
	 * 根据解析器中的值格式化关联 ID。
	 *
	 * @param resolver 用于解析命名值的解析器
	 * @return a formatted correlation id 格式化后的关联 ID
	 */
	public String format(UnaryOperator<@Nullable String> resolver) {
		StringBuilder result = new StringBuilder(this.blank.length());
		formatTo(resolver, result);
		return result.toString();
	}

	/**
	 * 格式化关联 ID 并追加到给定 {@link Appendable}。
	 *
	 * @param resolver 用于解析命名值的解析器
	 * @param appendable 接收格式化关联 ID 的可追加对象
	 */
	public void formatTo(UnaryOperator<@Nullable String> resolver, Appendable appendable) {
		Predicate<Part> canResolve = (part) -> StringUtils.hasLength(resolver.apply(part.name()));
		try {
			if (this.parts.stream().anyMatch(canResolve)) {
				appendable.append('[');
				for (Iterator<Part> iterator = this.parts.iterator(); iterator.hasNext();) {
					appendable.append(iterator.next().resolve(resolver));
					if (iterator.hasNext()) {
						appendable.append('-');
					}
				}
				appendable.append("] ");
			}
			else {
				appendable.append(this.blank);
			}
		}
		catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
	}

	@Override
	public String toString() {
		return this.parts.stream().map(Part::toString).collect(Collectors.joining(","));
	}

	/**
	 * 根据给定规格创建新的 {@link CorrelationIdFormatter} 实例。
	 *
	 * @param spec 逗号分隔的规格字符串
	 * @return a new {@link CorrelationIdFormatter} instance 新实例
	 */
	public static CorrelationIdFormatter of(@Nullable String spec) {
		try {
			return (!StringUtils.hasText(spec)) ? DEFAULT : of(List.of(spec.split(",")));
		}
		catch (Exception ex) {
			throw new IllegalStateException("Unable to parse correlation formatter spec '%s'".formatted(spec), ex);
		}
	}

	/**
	 * 根据预分割规格创建新的 {@link CorrelationIdFormatter} 实例。
	 *
	 * @param spec 预分割规格
	 * @return a new {@link CorrelationIdFormatter} instance 新实例
	 */
	public static CorrelationIdFormatter of(String @Nullable [] spec) {
		return of((spec != null) ? List.of(spec) : Collections.emptyList());
	}

	/**
	 * Create a new {@link CorrelationIdFormatter} instance from the given specification.
	 * @param spec a pre-separated specification
	 * @return a new {@link CorrelationIdFormatter} instance
	 */
	public static CorrelationIdFormatter of(Collection<String> spec) {
		if (CollectionUtils.isEmpty(spec)) {
			return DEFAULT;
		}
		List<Part> parts = spec.stream().map(Part::of).toList();
		return new CorrelationIdFormatter(parts);
	}

	/**
	 * 关联 ID 的一个组成部分。
	 *
	 * @param name the name of the correlation part 关联部分名称
	 * @param length the expected length of the correlation part 期望长度
	 */
	record Part(String name, int length) {

		private static final Pattern pattern = Pattern.compile("^(.+?)\\((\\d+)\\)$");

		String resolve(UnaryOperator<@Nullable String> resolver) {
			String resolved = resolver.apply(name());
			if (resolved == null) {
				return blank();
			}
			int padding = length() - resolved.length();
			return (padding <= 0) ? resolved : resolved + " ".repeat(padding);
		}

		String blank() {
			return " ".repeat(this.length);
		}

		@Override
		public String toString() {
			return "%s(%s)".formatted(name(), length());
		}

		static Part of(String part) {
			Matcher matcher = pattern.matcher(part.trim());
			Assert.state(matcher.matches(), () -> "Invalid specification part '%s'".formatted(part));
			String name = matcher.group(1);
			int length = Integer.parseInt(matcher.group(2));
			return new Part(name, length);
		}

	}

}
