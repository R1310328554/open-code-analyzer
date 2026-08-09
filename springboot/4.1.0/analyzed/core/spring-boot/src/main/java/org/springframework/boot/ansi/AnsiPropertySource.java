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

package org.springframework.boot.ansi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.IntFunction;

import org.jspecify.annotations.Nullable;

import org.springframework.core.env.PropertyResolver;
import org.springframework.core.env.PropertySource;
import org.springframework.util.StringUtils;

/**
 * 解析 {@link AnsiStyle}、{@link AnsiColor}、{@link AnsiBackground} 与
 * {@link Ansi8BitColor} 元素的 {@link PropertyResolver}。
 * <p>
 * 支持 {@code AnsiStyle.BOLD}、{@code AnsiColor.RED}、{@code AnsiBackground.GREEN} 等形式；
 * {@code Ansi.} 前缀聚合全部（背景色以 {@code BG_} 为前缀）。
 * <p>
 * {@code AnsiColor} 与 {@code AnsiBackground} 也支持 8 位色码，例如 {@code AnsiColor.208} 渲染橙色文本。
 * 完整 8 位色码列表见
 * <a href="https://en.wikipedia.org/wiki/ANSI_escape_code">Wikipedia</a>。
 *
 * @author Phillip Webb
 * @author Toshiaki Maki
 * @since 1.3.0
 */
public class AnsiPropertySource extends PropertySource<AnsiElement> {

	private static final Iterable<Mapping> MAPPINGS;

	static {
		List<Mapping> mappings = new ArrayList<>();
		mappings.add(new EnumMapping<>("AnsiStyle.", AnsiStyle.class));
		mappings.add(new EnumMapping<>("AnsiColor.", AnsiColor.class));
		mappings.add(new Ansi8BitColorMapping("AnsiColor.", Ansi8BitColor::foreground));
		mappings.add(new EnumMapping<>("AnsiBackground.", AnsiBackground.class));
		mappings.add(new Ansi8BitColorMapping("AnsiBackground.", Ansi8BitColor::background));
		mappings.add(new EnumMapping<>("Ansi.", AnsiStyle.class));
		mappings.add(new EnumMapping<>("Ansi.", AnsiColor.class));
		mappings.add(new EnumMapping<>("Ansi.BG_", AnsiBackground.class));
		MAPPINGS = Collections.unmodifiableList(mappings);
	}

	private final boolean encode;

	/**
	 * 创建新的 {@link AnsiPropertySource} 实例。
	 *
	 * @param name 属性源名称
	 * @param encode 是否对输出进行 ANSI 编码
	 */
	public AnsiPropertySource(String name, boolean encode) {
		super(name);
		this.encode = encode;
	}

	@Override
	public @Nullable Object getProperty(String name) {
		if (StringUtils.hasLength(name)) {
			for (Mapping mapping : MAPPINGS) {
				String prefix = mapping.getPrefix();
				if (name.startsWith(prefix)) {
					String postfix = name.substring(prefix.length());
					AnsiElement element = mapping.getElement(postfix);
					if (element != null) {
						return (this.encode) ? AnsiOutput.encode(element) : element;
					}
				}
			}
		}
		return null;
	}

	/**
	 * 名称与伪属性源之间的映射。
	 */
	private abstract static class Mapping {

		private final String prefix;

		Mapping(String prefix) {
			this.prefix = prefix;
		}

		String getPrefix() {
			return this.prefix;
		}

		abstract @Nullable AnsiElement getElement(String postfix);

	}

	/**
	 * {@link AnsiElement} 枚举的 {@link Mapping}。
	 */
	private static class EnumMapping<E extends Enum<E> & AnsiElement> extends Mapping {

		private final Set<E> enums;

		EnumMapping(String prefix, Class<E> enumType) {
			super(prefix);
			this.enums = EnumSet.allOf(enumType);
		}

		@Override
		@Nullable AnsiElement getElement(String postfix) {
			for (Enum<?> candidate : this.enums) {
				if (candidate.name().equals(postfix)) {
					return (AnsiElement) candidate;
				}
			}
			return null;
		}

	}

	/**
	 * {@link Ansi8BitColor} 的 {@link Mapping}。
	 */
	private static class Ansi8BitColorMapping extends Mapping {

		private final IntFunction<Ansi8BitColor> factory;

		Ansi8BitColorMapping(String prefix, IntFunction<Ansi8BitColor> factory) {
			super(prefix);
			this.factory = factory;
		}

		@Override
		@Nullable AnsiElement getElement(String postfix) {
			if (containsOnlyDigits(postfix)) {
				try {
					return this.factory.apply(Integer.parseInt(postfix));
				}
				catch (IllegalArgumentException ex) {
					// Ignore
				}
			}
			return null;
		}

		private boolean containsOnlyDigits(String postfix) {
			for (int i = 0; i < postfix.length(); i++) {
				if (!Character.isDigit(postfix.charAt(i))) {
					return false;
				}
			}
			return !postfix.isEmpty();
		}

	}

}
