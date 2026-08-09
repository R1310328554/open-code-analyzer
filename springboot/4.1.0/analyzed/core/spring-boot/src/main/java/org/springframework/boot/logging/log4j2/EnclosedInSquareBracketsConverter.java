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

package org.springframework.boot.logging.log4j2;

import java.util.List;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.core.pattern.ConverterKeys;
import org.apache.logging.log4j.core.pattern.LogEventPatternConverter;
import org.apache.logging.log4j.core.pattern.PatternConverter;
import org.apache.logging.log4j.core.pattern.PatternFormatter;
import org.apache.logging.log4j.core.pattern.PatternParser;
import org.jspecify.annotations.Nullable;

/**
 * 用于格式化应显示在方括号内的可选值的 Log4j2 {@link LogEventPatternConverter}。
 *
 * @author Phillip Webb
 * @since 3.4.0
 */
@Plugin(name = "enclosedInSquareBrackets", category = PatternConverter.CATEGORY)
@ConverterKeys("esb")
public final class EnclosedInSquareBracketsConverter extends LogEventPatternConverter {

	private final List<PatternFormatter> formatters;

	private EnclosedInSquareBracketsConverter(List<PatternFormatter> formatters) {
		super("enclosedInSquareBrackets", null);
		this.formatters = formatters;
	}

	@Override
	public void format(LogEvent event, StringBuilder toAppendTo) {
		StringBuilder buf = new StringBuilder();
		for (PatternFormatter formatter : this.formatters) {
			formatter.format(event, buf);
		}
		if (buf.isEmpty()) {
			return;
		}
		toAppendTo.append("[");
		toAppendTo.append(buf);
		toAppendTo.append("] ");
	}

	/**
	 * 创建类的新实例。Log4J2 要求提供此方法。
	 *
	 * @param config 配置
	 * @param options 选项
	 * @return 新实例；若选项无效则返回 {@code null}
	 */
	public static @Nullable EnclosedInSquareBracketsConverter newInstance(@Nullable Configuration config,
			String[] options) {
		if (options.length < 1) {
			LOGGER.error("Incorrect number of options on style. Expected at least 1, received {}", options.length);
			return null;
		}
		PatternParser parser = PatternLayout.createPatternParser(config);
		List<PatternFormatter> formatters = parser.parse(options[0]);
		return new EnclosedInSquareBracketsConverter(formatters);
	}

}
