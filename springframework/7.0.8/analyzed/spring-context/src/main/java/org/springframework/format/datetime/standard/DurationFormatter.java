/*
 * Copyright 2002-present the original author or authors.
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

package org.springframework.format.datetime.standard;

import java.text.ParseException;
import java.time.Duration;
import java.util.Locale;

import org.jspecify.annotations.Nullable;

import org.springframework.format.Formatter;
import org.springframework.format.annotation.DurationFormat;

/**
 * JSR-310 {@link Duration} 的 {@link Formatter} 实现，默认遵循 JSR-310 对 Duration 的解析规则，
 * 并支持额外的 {@code DurationFormat.Style} 样式。
 *
 * @author Juergen Hoeller
 * @since 6.2
 * @see DurationFormatterUtils
 * @see DurationFormat.Style
 */
public class DurationFormatter implements Formatter<Duration> {

	private final DurationFormat.Style style;

	private final DurationFormat.@Nullable Unit defaultUnit;

	/**
	 * 创建遵循 JSR-310 Duration 解析规则（{@link DurationFormat.Style#ISO8601 ISO-8601} 样式）的 {@code DurationFormatter}。
	 */
	DurationFormatter() {
		this(DurationFormat.Style.ISO8601);
	}

	/**
	 * 以指定 {@link DurationFormat.Style} 创建 {@code DurationFormatter}。
	 * <p>当需要单位但无法确定时（例如在 {@code SIMPLE} 样式下打印 Duration），
	 * 将使用 {@code DurationFormat.Unit#MILLIS}。
	 */
	public DurationFormatter(DurationFormat.Style style) {
		this(style, null);
	}

	/**
	 * 以指定 {@link DurationFormat.Style} 和可选的 {@code DurationFormat.Unit} 创建 {@code DurationFormatter}。
	 * <p>若指定了 {@code defaultUnit}，在字符串中未出现单位时可用于解析（前提是样式允许此类情况）。
	 * 在 {@link DurationFormat.Style#SIMPLE} 样式下打印时，也作为表示的分辨率单位。
	 * 否则由样式定义其默认单位。
	 *
	 * @param style 要使用的 {@code DurationStyle}
	 * @param defaultUnit 解析与打印时回退使用的 {@code DurationFormat.Unit}
	 */
	public DurationFormatter(DurationFormat.Style style, DurationFormat.@Nullable Unit defaultUnit) {
		this.style = style;
		this.defaultUnit = defaultUnit;
	}

	@Override
	public Duration parse(String text, Locale locale) throws ParseException {
		if (this.defaultUnit == null) {
			//delegate to the style
			return DurationFormatterUtils.parse(text, this.style);
		}
		return DurationFormatterUtils.parse(text, this.style, this.defaultUnit);
	}

	@Override
	public String print(Duration object, Locale locale) {
		if (this.defaultUnit == null) {
			//delegate the ultimate of the default unit to the style
			return DurationFormatterUtils.print(object, this.style);
		}
		return DurationFormatterUtils.print(object, this.style, this.defaultUnit);
	}

}
