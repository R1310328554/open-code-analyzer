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

package org.springframework.format.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.function.Function;

import org.jspecify.annotations.Nullable;

/**
 * 声明字段或方法参数应按指定的 {@link #style Style} 与 {@link #defaultUnit Unit}
 * 格式化为 {@link java.time.Duration}。
 *
 * @author Simon Baslé
 * @since 6.2
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
public @interface DurationFormat {

	/**
	 * 用于解析与打印 {@link Duration} 的 {@link Style}。
	 * <p>默认为 JDK 样式（{@link Style#ISO8601}）。
	 */
	Style style() default Style.ISO8601;

	/**
	 * 当 {@link #style Style} 在解析或打印时需要单位，而输入中未显式提供单位时的回退 {@link Unit}。
	 * <p>未指定时默认为 {@link Unit#MILLIS}。
	 */
	Unit defaultUnit() default Unit.MILLIS;


	/**
	 * {@link Duration} 格式样式。
	 */
	enum Style {

		/**
		 * ISO-8601 格式化。
		 * <p>这也是 JDK 在 {@link Duration#parse(CharSequence)} 与 {@link Duration#toString()} 中使用的格式。
		 */
		ISO8601,

		/**
		 * 基于短后缀的简单格式化，例如 '1s'。
		 * <p>支持的单位后缀包括：{@code ns, us, ms, s, m, h, d}，
		 * 分别对应纳秒、微秒、毫秒、秒、分钟、小时和天。
		 * <p>注意，打印 {@link Duration} 时，若所选单位大于持续时间的分辨率，此样式可能有精度损失。
		 * 例如，{@code Duration.ofMillis(5).plusNanos(1234)} 在使用 {@code ChronoUnit.MILLIS} 打印时会截断为 {@code "5ms"}。
		 * <p>不支持小数持续时间。
		 */
		SIMPLE,

		/**
		 * 类似 {@link #SIMPLE}，但允许多个按从大到小时间单位排序的段，例如 {@code 1h12m27s}。
		 * <p>允许单个减号（{@code -}）表示整个持续时间为负。
		 * 段之间允许空格，带空格的负持续时间可在减号后可选地用括号包围，例如：{@code -(34m 57s)}。
		 */
		COMPOSITE
	}


	/**
	 * {@link Duration} 格式单位，映射 {@link ChronoUnit} 的子集，
	 * 支持与对应 {@code ChronoUnit} 的双向转换，以及从持续时间到 long 的转换。
	 *
	 * <p>该枚举包含其在 {@link Style#SIMPLE SIMPLE} {@code Duration} 格式样式中对应的后缀。
	 */
	enum Unit {

		/**
		 * 纳秒（{@code "ns"}）。
		 */
		NANOS(ChronoUnit.NANOS, "ns", Duration::toNanos),

		/**
		 * 微秒（{@code "us"}）。
		 */
		MICROS(ChronoUnit.MICROS, "us", duration -> duration.toNanos() / 1000L),

		/**
		 * 毫秒（{@code "ms"}）。
		 */
		MILLIS(ChronoUnit.MILLIS, "ms", Duration::toMillis),

		/**
		 * 秒（{@code "s"}）。
		 */
		SECONDS(ChronoUnit.SECONDS, "s", Duration::toSeconds),

		/**
		 * 分钟（{@code "m"}）。
		 */
		MINUTES(ChronoUnit.MINUTES, "m", Duration::toMinutes),

		/**
		 * 小时（{@code "h"}）。
		 */
		HOURS(ChronoUnit.HOURS, "h", Duration::toHours),

		/**
		 * 天（{@code "d"}）。
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

		/**
		 * 将此 {@code Unit} 转换为其等价的 {@link ChronoUnit}。
		 */
		public ChronoUnit asChronoUnit() {
			return this.chronoUnit;
		}

		/**
		 * 将此 {@code Unit} 转换为适用于 {@link Style#SIMPLE SIMPLE} 样式的简单 {@code String} 后缀。
		 */
		public String asSuffix() {
			return this.suffix;
		}

		/**
		 * 从给定的 {@link String} 解析 {@code long}，并将其解释为当前单位下的 {@link Duration}。
		 * @param value long 的 {@code String} 表示
		 * @return 对应的 {@code Duration}
		 */
		public Duration parse(String value) {
			return Duration.of(Long.parseLong(value), asChronoUnit());
		}

		/**
		 * 将给定的 {@link Duration} 打印为 {@link String}，通过 {@link #longValue(Duration)}
		 * 使用本单位的精度将其转换为 long 值，并附加本单位的简单 {@link #asSuffix() 后缀}。
		 * @param value 要转换为 {@code String} 的 {@code Duration}
		 * @return {@link Style#SIMPLE SIMPLE} 样式下 {@code Duration} 的 {@code String} 表示
		 */
		public String print(Duration value) {
			return longValue(value) + asSuffix();
		}

		/**
		 * 将给定的 {@link Duration} 按本单位的精度转换为 long 值。
		 * <p>注意，若当前单位大于持续时间的实际分辨率，此操作可能有精度损失。
		 * 例如，{@code Duration.ofMillis(5).plusNanos(1234)} 对于单位 {@code MILLIS} 会截断为 {@code 5}。
		 * @param value 要转换为 long 的 {@code Duration}
		 * @return 此 {@code Unit} 下 {@code Duration} 的 long 值
		 */
		public long longValue(Duration value) {
			return this.longValue.apply(value);
		}

		/**
		 * 获取与给定 {@link ChronoUnit} 对应的 {@link Unit}。
		 * @throws IllegalArgumentException 若给定的 {@code ChronoUnit} 不受支持
		 */
		public static Unit fromChronoUnit(@Nullable ChronoUnit chronoUnit) {
			if (chronoUnit == null) {
				return Unit.MILLIS;
			}
			for (Unit candidate : values()) {
				if (candidate.chronoUnit == chronoUnit) {
					return candidate;
				}
			}
			throw new IllegalArgumentException("No matching Unit for ChronoUnit." + chronoUnit.name());
		}

		/**
		 * 获取与给定 {@link String} 后缀对应的 {@link Unit}。
		 * @throws IllegalArgumentException 若给定的后缀不受支持
		 */
		public static Unit fromSuffix(String suffix) {
			for (Unit candidate : values()) {
				if (candidate.suffix.equalsIgnoreCase(suffix)) {
					return candidate;
				}
			}
			throw new IllegalArgumentException("'" + suffix + "' is not a valid simple duration Unit");
		}
	}

}
