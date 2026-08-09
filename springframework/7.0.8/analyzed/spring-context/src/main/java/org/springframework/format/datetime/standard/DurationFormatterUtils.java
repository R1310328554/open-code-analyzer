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

import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jspecify.annotations.Nullable;

import org.springframework.format.annotation.DurationFormat;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * 支持按 {@link DurationFormat.Style} 所列多种样式解析与打印 {@code Duration}。
 *
 * <p>部分样式可能不强制要求出现单位，此时默认使用 {@code DurationFormat.Unit#MILLIS}。
 * 本类方法提供接受 {@link DurationFormat.Unit} 的重载，用作回退单位而非最终的 MILLIS 默认值。
 *
 * @author Phillip Webb
 * @author Valentine Wu
 * @author Simon Baslé
 * @since 6.2
 */
public abstract class DurationFormatterUtils {

	private static final Pattern ISO_8601_PATTERN = Pattern.compile("^[+-]?[pP].*$");

	private static final Pattern SIMPLE_PATTERN = Pattern.compile("^([+-]?\\d+)([a-zA-Z]{0,2})$");

	private static final Pattern COMPOSITE_PATTERN = Pattern.compile("^([+-]?)\\(?\\s?(\\d+d)?\\s?(\\d+h)?\\s?(\\d+m)?" +
			"\\s?(\\d+s)?\\s?(\\d+ms)?\\s?(\\d+us)?\\s?(\\d+ns)?\\)?$");


	/**
	 * 以指定样式打印 Duration。
	 * @param value 要打印的值
	 * @param style 打印样式
	 * @return 打印结果
	 */
	public static String print(Duration value, DurationFormat.Style style) {
		return print(value, style, null);
	}

	/**
	 * 以指定样式和单位打印 Duration。
	 * @param value 要打印的值
	 * @param style 打印样式
	 * @param unit 打印时使用的单位（若相关；{@code null} 时默认为毫秒）
	 * @return 打印结果
	 */
	public static String print(Duration value, DurationFormat.Style style, DurationFormat.@Nullable Unit unit) {
		return switch (style) {
			case ISO8601 -> value.toString();
			case SIMPLE -> printSimple(value, unit);
			case COMPOSITE -> printComposite(value);
		};
	}

	/**
	 * 将给定值解析为 Duration。
	 * @param value 要解析的值
	 * @param style 解析样式
	 * @return Duration 实例
	 */
	public static Duration parse(String value, DurationFormat.Style style) {
		return parse(value, style, null);
	}

	/**
	 * 将给定值解析为 Duration。
	 * @param value 要解析的值
	 * @param style 解析样式
	 * @param unit 值未指定单位时使用的时长单位（{@code null} 时默认为毫秒）
	 * @return Duration 实例
	 */
	public static Duration parse(String value, DurationFormat.Style style, DurationFormat.@Nullable Unit unit) {
		Assert.hasText(value, () -> "Value must not be empty");
		return switch (style) {
			case ISO8601 -> parseIso8601(value);
			case SIMPLE -> parseSimple(value, unit);
			case COMPOSITE -> parseComposite(value);
		};
	}

	/**
	 * 根据给定源值检测样式。
	 * @param value 源值
	 * @return Duration 样式
	 * @throws IllegalArgumentException 若值不属于任何已知样式
	 */
	public static DurationFormat.Style detect(String value) {
		Assert.notNull(value, "Value must not be null");
		// warning: the order of parsing starts to matter if multiple patterns accept a plain integer (no unit suffix)
		if (ISO_8601_PATTERN.matcher(value).matches()) {
			return DurationFormat.Style.ISO8601;
		}
		if (SIMPLE_PATTERN.matcher(value).matches()) {
			return DurationFormat.Style.SIMPLE;
		}
		if (COMPOSITE_PATTERN.matcher(value).matches()) {
			return DurationFormat.Style.COMPOSITE;
		}
		throw new IllegalArgumentException("'" + value + "' is not a valid duration, cannot detect any known style");
	}

	/**
	 * 检测样式后将值解析为 Duration。
	 * @param value 要解析的值
	 * @return 解析得到的 Duration
	 * @throws IllegalArgumentException 若值不属于任何已知样式或无法解析
	 */
	public static Duration detectAndParse(String value) {
		return detectAndParse(value, null);
	}

	/**
	 * 检测样式后将值解析为 Duration。
	 * @param value 要解析的值
	 * @param unit 值未指定单位时使用的时长单位（{@code null} 时默认为毫秒）
	 * @return 解析得到的 Duration
	 * @throws IllegalArgumentException 若值不属于任何已知样式或无法解析
	 */
	public static Duration detectAndParse(String value, DurationFormat.@Nullable Unit unit) {
		return parse(value, detect(value), unit);
	}


	private static Duration parseIso8601(String value) {
		try {
			return Duration.parse(value);
		}
		catch (Exception ex) {
			throw new IllegalArgumentException("'" + value + "' is not a valid ISO-8601 duration", ex);
		}
	}

	private static String printSimple(Duration duration, DurationFormat.@Nullable Unit unit) {
		unit = (unit == null ? DurationFormat.Unit.MILLIS : unit);
		return unit.print(duration);
	}

	private static Duration parseSimple(String text, DurationFormat.@Nullable Unit fallbackUnit) {
		try {
			Matcher matcher = SIMPLE_PATTERN.matcher(text);
			Assert.state(matcher.matches(), "Does not match simple duration pattern");
			String suffix = matcher.group(2);
			DurationFormat.Unit parsingUnit = (fallbackUnit == null ? DurationFormat.Unit.MILLIS : fallbackUnit);
			if (StringUtils.hasLength(suffix)) {
				parsingUnit = DurationFormat.Unit.fromSuffix(suffix);
			}
			return parsingUnit.parse(matcher.group(1));
		}
		catch (Exception ex) {
			throw new IllegalArgumentException("'" + text + "' is not a valid simple duration", ex);
		}
	}

	private static String printComposite(Duration duration) {
		if (duration.isZero()) {
			return DurationFormat.Unit.SECONDS.print(duration);
		}
		StringBuilder result = new StringBuilder();
		if (duration.isNegative()) {
			result.append('-');
			duration = duration.negated();
		}
		long days = duration.toDaysPart();
		if (days != 0) {
			result.append(days).append(DurationFormat.Unit.DAYS.asSuffix());
		}
		int hours = duration.toHoursPart();
		if (hours != 0) {
			result.append(hours).append(DurationFormat.Unit.HOURS.asSuffix());
		}
		int minutes = duration.toMinutesPart();
		if (minutes != 0) {
			result.append(minutes).append(DurationFormat.Unit.MINUTES.asSuffix());
		}
		int seconds = duration.toSecondsPart();
		if (seconds != 0) {
			result.append(seconds).append(DurationFormat.Unit.SECONDS.asSuffix());
		}
		int millis = duration.toMillisPart();
		if (millis != 0) {
			result.append(millis).append(DurationFormat.Unit.MILLIS.asSuffix());
		}
		//special handling of nanos: remove the millis part and then divide into microseconds and nanoseconds
		long nanos = duration.toNanosPart() - Duration.ofMillis(millis).toNanos();
		if (nanos != 0) {
			long micros = nanos / 1000;
			long remainder = nanos - (micros * 1000);
			if (micros > 0) {
				result.append(micros).append(DurationFormat.Unit.MICROS.asSuffix());
			}
			if (remainder > 0) {
				result.append(remainder).append(DurationFormat.Unit.NANOS.asSuffix());
			}
		}
		return result.toString();
	}

	private static Duration parseComposite(String text) {
		try {
			Matcher matcher = COMPOSITE_PATTERN.matcher(text);
			Assert.state(matcher.matches() && matcher.groupCount() > 1, "Does not match composite duration pattern");
			String sign = matcher.group(1);
			boolean negative = sign != null && sign.equals("-");

			Duration result = Duration.ZERO;
			DurationFormat.Unit[] units = DurationFormat.Unit.values();
			for (int i = 2; i < matcher.groupCount() + 1; i++) {
				String segment = matcher.group(i);
				if (StringUtils.hasText(segment)) {
					DurationFormat.Unit unit = units[units.length - i + 1];
					result = result.plus(unit.parse(segment.replace(unit.asSuffix(), "")));
				}
			}
			return negative ? result.negated() : result;
		}
		catch (Exception ex) {
			throw new IllegalArgumentException("'" + text + "' is not a valid composite duration", ex);
		}
	}

}
