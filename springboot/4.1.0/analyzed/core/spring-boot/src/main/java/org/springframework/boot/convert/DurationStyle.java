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

package org.springframework.boot.convert;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jspecify.annotations.Nullable;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Duration 格式风格。
 *
 * @author Phillip Webb
 * @author Valentine Wu
 * @since 2.0.0
 */
public enum DurationStyle {

	/**
	 * 简单格式，例如 {@code 1s}。
	 */
	SIMPLE("^([+-]?\\d+)([a-zA-Z]{0,2})$") {

		@Override
		public Duration parse(String value, @Nullable ChronoUnit unit) {
			try {
				Matcher matcher = matcher(value);
				Assert.state(matcher.matches(), "Does not match simple duration pattern");
				String suffix = matcher.group(2);
				return (StringUtils.hasLength(suffix) ? Unit.fromSuffix(suffix) : Unit.fromChronoUnit(unit))
					.parse(matcher.group(1));
			}
			catch (Exception ex) {
				throw new IllegalArgumentException("'" + value + "' is not a valid simple duration", ex);
			}
		}

		@Override
		public String print(Duration value, @Nullable ChronoUnit unit) {
			return Unit.fromChronoUnit(unit).print(value);
		}

	},

	/**
	 * ISO-8601 格式。
	 */
	ISO8601("^[+-]?[pP].*$") {

		@Override
		public Duration parse(String value, @Nullable ChronoUnit unit) {
			try {
				return Duration.parse(value);
			}
			catch (Exception ex) {
				throw new IllegalArgumentException("'" + value + "' is not a valid ISO-8601 duration", ex);
			}
		}

		@Override
		public String print(Duration value, @Nullable ChronoUnit unit) {
			return value.toString();
		}

	};

	private final Pattern pattern;

	DurationStyle(String pattern) {
		this.pattern = Pattern.compile(pattern);
	}

	protected final boolean matches(String value) {
		return this.pattern.matcher(value).matches();
	}

	protected final Matcher matcher(String value) {
		return this.pattern.matcher(value);
	}

	/**
	 * 将给定值解析为 Duration。
	 *
	 * @param value 要解析的值
	 * @return a duration Duration 实例
	 */
	public Duration parse(String value) {
		return parse(value, null);
	}

	/**
	 * 将给定值解析为 Duration。
	 *
	 * @param value 要解析的值
	 * @param unit 值未指定单位时使用的 Duration 单位（{@code null} 默认为毫秒）
	 * @return a duration Duration 实例
	 */
	public abstract Duration parse(String value, @Nullable ChronoUnit unit);

	/**
	 * 打印指定的 Duration。
	 *
	 * @param value 要打印的值
	 * @return the printed result 打印结果
	 */
	public String print(Duration value) {
		return print(value, null);
	}

	/**
	 * 使用给定单位打印指定的 Duration。
	 *
	 * @param value 要打印的值
	 * @param unit 打印时使用的单位
	 * @return the printed result 打印结果
	 */
	public abstract String print(Duration value, @Nullable ChronoUnit unit);

	/**
	 * 检测格式风格并将值解析为 Duration。
	 *
	 * @param value 要解析的值
	 * @return the parsed duration 解析后的 Duration
	 * @throws IllegalArgumentException if the value is not a known style or cannot be parsed 值不是已知风格或无法解析时
	 */
	public static Duration detectAndParse(String value) {
		return detectAndParse(value, null);
	}

	/**
	 * 检测格式风格并将值解析为 Duration。
	 *
	 * @param value 要解析的值
	 * @param unit 值未指定单位时使用的 Duration 单位（{@code null} 默认为毫秒）
	 * @return the parsed duration 解析后的 Duration
	 * @throws IllegalArgumentException if the value is not a known style or cannot be parsed 值不是已知风格或无法解析时
	 */
	public static Duration detectAndParse(String value, @Nullable ChronoUnit unit) {
		return detect(value).parse(value, unit);
	}

	/**
	 * 从给定源值检测 Duration 格式风格。
	 *
	 * @param value 源值
	 * @return the duration style Duration 格式风格
	 * @throws IllegalArgumentException if the value is not a known style 值不是已知风格时
	 */
	public static DurationStyle detect(String value) {
		Assert.notNull(value, "'value' must not be null");
		for (DurationStyle candidate : values()) {
			if (candidate.matches(value)) {
				return candidate;
			}
		}
		throw new IllegalArgumentException("'" + value + "' is not a valid duration");
	}

	/**
	 * 支持的时间单位。
	 */
	enum Unit {

		/**
		 * 纳秒。
		 */
		NANOS(ChronoUnit.NANOS, "ns", Duration::toNanos),

		/**
		 * 微秒。
		 */
		MICROS(ChronoUnit.MICROS, "us", (duration) -> duration.toNanos() / 1000L),

		/**
		 * 毫秒。
		 */
		MILLIS(ChronoUnit.MILLIS, "ms", Duration::toMillis),

		/**
		 * 秒。
		 */
		SECONDS(ChronoUnit.SECONDS, "s", Duration::getSeconds),

		/**
		 * 分钟。
		 */
		MINUTES(ChronoUnit.MINUTES, "m", Duration::toMinutes),

		/**
		 * 小时。
		 */
		HOURS(ChronoUnit.HOURS, "h", Duration::toHours),

		/**
		 * 天。
		 */
		DAYS(ChronoUnit.DAYS, "d", Duration::toDays);

		private final ChronoUnit chronoUnit;

		private final String suffix;

		private final Function<Duration, Long> longValue;

		Unit(ChronoUnit chronoUnit, String suffix, Function<Duration, Long> toUnit) {
			this.chronoUnit = chronoUnit;
			this.suffix = suffix;
			this.longValue = toUnit;
		}

		public Duration parse(String value) {
			return Duration.of(Long.parseLong(value), this.chronoUnit);
		}

		public String print(Duration value) {
			return longValue(value) + this.suffix;
		}

		public long longValue(Duration value) {
			return this.longValue.apply(value);
		}

		public static Unit fromChronoUnit(@Nullable ChronoUnit chronoUnit) {
			if (chronoUnit == null) {
				return Unit.MILLIS;
			}
			for (Unit candidate : values()) {
				if (candidate.chronoUnit == chronoUnit) {
					return candidate;
				}
			}
			throw new IllegalArgumentException("Unknown unit " + chronoUnit);
		}

		public static Unit fromSuffix(String suffix) {
			for (Unit candidate : values()) {
				if (candidate.suffix.equalsIgnoreCase(suffix)) {
					return candidate;
				}
			}
			throw new IllegalArgumentException("Unknown unit '" + suffix + "'");
		}

	}

}
