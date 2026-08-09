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

import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jspecify.annotations.Nullable;

import org.springframework.util.Assert;

/**
 * 标准的 {@link Period} 单位集合。
 *
 * @author Eddú Meléndez
 * @author Edson Chávez
 * @author Valentine Wu
 * @since 2.3.0
 * @see Period
 */
public enum PeriodStyle {

	/**
	 * 简单格式，例如 '1d'。
	 */
	SIMPLE("^" + "(?:([-+]?[0-9]+)Y)?" + "(?:([-+]?[0-9]+)M)?" + "(?:([-+]?[0-9]+)W)?" + "(?:([-+]?[0-9]+)D)?" + "$",
			Pattern.CASE_INSENSITIVE) {

		@Override
		public Period parse(String value, @Nullable ChronoUnit unit) {
			try {
				if (NUMERIC.matcher(value).matches()) {
					return Unit.fromChronoUnit(unit).parse(value);
				}
				Matcher matcher = matcher(value);
				Assert.state(matcher.matches(), "Does not match simple period pattern");
				Assert.isTrue(hasAtLeastOneGroupValue(matcher), () -> "'" + value + "' is not a valid simple period");
				int years = parseInt(matcher, 1);
				int months = parseInt(matcher, 2);
				int weeks = parseInt(matcher, 3);
				int days = parseInt(matcher, 4);
				return Period.of(years, months, Math.addExact(Math.multiplyExact(weeks, 7), days));
			}
			catch (Exception ex) {
				throw new IllegalArgumentException("'" + value + "' is not a valid simple period", ex);
			}
		}

		boolean hasAtLeastOneGroupValue(Matcher matcher) {
			for (int i = 0; i < matcher.groupCount(); i++) {
				if (matcher.group(i + 1) != null) {
					return true;
				}
			}
			return false;
		}

		private int parseInt(Matcher matcher, int group) {
			String value = matcher.group(group);
			return (value != null) ? Integer.parseInt(value) : 0;
		}

		@Override
		protected boolean matches(String value) {
			return NUMERIC.matcher(value).matches() || matcher(value).matches();
		}

		@Override
		public String print(Period value, @Nullable ChronoUnit unit) {
			if (value.isZero()) {
				return Unit.fromChronoUnit(unit).print(value);
			}
			StringBuilder result = new StringBuilder();
			append(result, value, Unit.YEARS);
			append(result, value, Unit.MONTHS);
			append(result, value, Unit.DAYS);
			return result.toString();
		}

		private void append(StringBuilder result, Period value, Unit unit) {
			if (!unit.isZero(value)) {
				result.append(unit.print(value));
			}
		}

	},

	/**
	 * ISO-8601 格式。
	 */
	ISO8601("^[+-]?P.*$", Pattern.CASE_INSENSITIVE) {

		@Override
		public Period parse(String value, @Nullable ChronoUnit unit) {
			try {
				return Period.parse(value);
			}
			catch (Exception ex) {
				throw new IllegalArgumentException("'" + value + "' is not a valid ISO-8601 period", ex);
			}
		}

		@Override
		public String print(Period value, @Nullable ChronoUnit unit) {
			return value.toString();
		}

	};

	private static final Pattern NUMERIC = Pattern.compile("^[-+]?[0-9]+$");

	private final Pattern pattern;

	PeriodStyle(String pattern, int flags) {
		this.pattern = Pattern.compile(pattern, flags);
	}

	protected boolean matches(String value) {
		return this.pattern.matcher(value).matches();
	}

	protected final Matcher matcher(String value) {
		return this.pattern.matcher(value);
	}

	/**
	 * 将给定值解析为 Period。
	 *
	 * @param value 待解析的值
	 * @return 解析得到的 period
	 */
	public Period parse(String value) {
		return parse(value, null);
	}

	/**
	 * 将给定值解析为 period。
	 *
	 * @param value 待解析的值
	 * @param unit 值未指定单位时使用的 period 单位（{@code null} 默认为 d）
	 * @return 解析得到的 period
	 */
	public abstract Period parse(String value, @Nullable ChronoUnit unit);

	/**
	 * 打印指定的 period。
	 *
	 * @param value 待打印的值
	 * @return 打印结果
	 */
	public String print(Period value) {
		return print(value, null);
	}

	/**
	 * 使用给定单位打印指定的 period。
	 *
	 * @param value 待打印的值
	 * @param unit 用于打印的单位
	 * @return 打印结果
	 */
	public abstract String print(Period value, @Nullable ChronoUnit unit);

	/**
	 * 检测格式后解析值并返回 period。
	 *
	 * @param value 待解析的值
	 * @return 解析得到的 period
	 * @throws IllegalArgumentException 若值不是已知格式或无法解析
	 */
	public static Period detectAndParse(String value) {
		return detectAndParse(value, null);
	}

	/**
	 * 检测格式后解析值并返回 period。
	 *
	 * @param value 待解析的值
	 * @param unit 值未指定单位时使用的 period 单位（{@code null} 默认为 ms）
	 * @return 解析得到的 period
	 * @throws IllegalArgumentException 若值不是已知格式或无法解析
	 */
	public static Period detectAndParse(String value, @Nullable ChronoUnit unit) {
		return detect(value).parse(value, unit);
	}

	/**
	 * 从给定源值检测 period 格式。
	 *
	 * @param value 源值
	 * @return period 格式
	 * @throws IllegalArgumentException 若值不是已知格式
	 */
	public static PeriodStyle detect(String value) {
		Assert.notNull(value, "'value' must not be null");
		for (PeriodStyle candidate : values()) {
			if (candidate.matches(value)) {
				return candidate;
			}
		}
		throw new IllegalArgumentException("'" + value + "' is not a valid period");
	}

	private enum Unit {

		/**
		 * 天，后缀为 {@code d}。
		 */
		DAYS(ChronoUnit.DAYS, "d", Period::getDays, Period::ofDays),

		/**
		 * 周，后缀为 {@code w}。
		 */
		WEEKS(ChronoUnit.WEEKS, "w", null, Period::ofWeeks),

		/**
		 * 月，后缀为 {@code m}。
		 */
		MONTHS(ChronoUnit.MONTHS, "m", Period::getMonths, Period::ofMonths),

		/**
		 * 年，后缀为 {@code y}。
		 */
		YEARS(ChronoUnit.YEARS, "y", Period::getYears, Period::ofYears);

		private final ChronoUnit chronoUnit;

		private final String suffix;

		private final @Nullable Function<Period, Integer> intValue;

		private final Function<Integer, Period> factory;

		Unit(ChronoUnit chronoUnit, String suffix, @Nullable Function<Period, Integer> intValue,
				Function<Integer, Period> factory) {
			this.chronoUnit = chronoUnit;
			this.suffix = suffix;
			this.intValue = intValue;
			this.factory = factory;
		}

		private Period parse(String value) {
			return this.factory.apply(Integer.parseInt(value));
		}

		private String print(Period value) {
			return intValue(value) + this.suffix;
		}

		private boolean isZero(Period value) {
			return intValue(value) == 0;
		}

		private int intValue(Period value) {
			Assert.state(this.intValue != null, () -> "intValue cannot be extracted from " + name());
			return this.intValue.apply(value);
		}

		private static Unit fromChronoUnit(@Nullable ChronoUnit chronoUnit) {
			if (chronoUnit == null) {
				return Unit.DAYS;
			}
			for (Unit candidate : values()) {
				if (candidate.chronoUnit == chronoUnit) {
					return candidate;
				}
			}
			throw new IllegalArgumentException("Unsupported unit " + chronoUnit);
		}

	}

}
