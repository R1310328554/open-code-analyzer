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

package org.springframework.scheduling.support;

import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;

import org.jspecify.annotations.Nullable;

import org.springframework.util.Assert;

/**
 * 面向 <a href="https://www.quartz-scheduler.org">Quartz</a> 特定字段的
 * {@link CronField} 扩展。通过 {@code parse*} 方法创建，内部使用
 * {@link TemporalAdjuster}。
 *
 * <p>支持带 L/# 表达式的 Quartz 日/周字段；其余方面遵循常见 cron 约定，
 * 包括 SUN-SAT 使用 0-6（SUN 也可用 7）。
 * 注意：Quartz 对星期使用 1-7 表示 SUN-SAT，与 cron 不同；
 * Spring 即使结合可选的 Quartz 专用 L/# 表达式也严格遵循 cron 约定。
 *
 * @author Arjen Poutsma
 * @since 5.3
 */
final class QuartzCronField extends CronField {

	private final Type rollForwardType;

	private final TemporalAdjuster adjuster;

	private final String value;


	private QuartzCronField(Type type, TemporalAdjuster adjuster, String value) {
		this(type, type, adjuster, value);
	}

	/**
	 * 用于需跨越与本字段类型不同的类型向前滚动的字段的构造函数。
	 * 参见 {@link #parseDaysOfWeek(String)}。
	 */
	private QuartzCronField(Type type, Type rollForwardType, TemporalAdjuster adjuster, String value) {
		super(type);
		this.adjuster = adjuster;
		this.value = value;
		this.rollForwardType = rollForwardType;
	}


	/**
	 * 判断给定值是否为 Quartz 的“日”字段。
	 */
	public static boolean isQuartzDaysOfMonthField(String value) {
		return value.contains("L") || value.contains("W");
	}

	/**
	 * 将给定值解析为 cron 表达式第四项——“日”的 {@code QuartzCronField}。
	 * <p>要求值中包含 "L" 或 "W"。
	 */
	public static QuartzCronField parseDaysOfMonth(String value) {
		int idx = value.lastIndexOf('L');
		if (idx != -1) {
			TemporalAdjuster adjuster;
			if (idx != 0) {
				throw new IllegalArgumentException("Unrecognized characters before 'L' in '" + value + "'");
			}
			else if (value.length() == 2 && value.charAt(1) == 'W') {  // "LW"
				adjuster = lastWeekdayOfMonth();
			}
			else {
				if (value.length() == 1) {  // "L"
					adjuster = lastDayOfMonth();
				}
				else {  // "L-[0-9]+"
					int offset = Integer.parseInt(value, idx + 1, value.length(), 10);
					if (offset >= 0) {
						throw new IllegalArgumentException("Offset '" + offset + " should be < 0 '" + value + "'");
					}
					adjuster = lastDayWithOffset(offset);
				}
			}
			return new QuartzCronField(Type.DAY_OF_MONTH, adjuster, value);
		}
		idx = value.lastIndexOf('W');
		if (idx != -1) {
			if (idx == 0) {
				throw new IllegalArgumentException("No day-of-month before 'W' in '" + value + "'");
			}
			else if (idx != value.length() - 1) {
				throw new IllegalArgumentException("Unrecognized characters after 'W' in '" + value + "'");
			}
			else {  // "[0-9]+W"
				int dayOfMonth = Integer.parseInt(value, 0, idx, 10);
				dayOfMonth = Type.DAY_OF_MONTH.checkValidValue(dayOfMonth);
				TemporalAdjuster adjuster = weekdayNearestTo(dayOfMonth);
				return new QuartzCronField(Type.DAY_OF_MONTH, adjuster, value);
			}
		}
		throw new IllegalArgumentException("No 'L' or 'W' found in '" + value + "'");
	}

	/**
	 * 判断给定值是否为 Quartz 的“星期”字段。
	 */
	public static boolean isQuartzDaysOfWeekField(String value) {
		return value.contains("L") || value.contains("#");
	}

	/**
	 * 将给定值解析为 cron 表达式第六项——“星期”的 {@code QuartzCronField}。
	 * <p>要求值中包含 "L" 或 "#"。
	 */
	public static QuartzCronField parseDaysOfWeek(String value) {
		int idx = value.lastIndexOf('L');
		if (idx != -1) {
			if (idx != value.length() - 1) {
				throw new IllegalArgumentException("Unrecognized characters after 'L' in '" + value + "'");
			}
			else {
				TemporalAdjuster adjuster;
				if (idx == 0) {
					throw new IllegalArgumentException("No day-of-week before 'L' in '" + value + "'");
				}
				else {  // "[0-7]L"
					DayOfWeek dayOfWeek = parseDayOfWeek(value.substring(0, idx));
					adjuster = lastInMonth(dayOfWeek);
				}
				return new QuartzCronField(Type.DAY_OF_WEEK, Type.DAY_OF_MONTH, adjuster, value);
			}
		}
		idx = value.lastIndexOf('#');
		if (idx != -1) {
			if (idx == 0) {
				throw new IllegalArgumentException("No day-of-week before '#' in '" + value + "'");
			}
			else if (idx == value.length() - 1) {
				throw new IllegalArgumentException("No ordinal after '#' in '" + value + "'");
			}
			// "[0-7]#[0-9]+"
			DayOfWeek dayOfWeek = parseDayOfWeek(value.substring(0, idx));
			int ordinal = Integer.parseInt(value, idx + 1, value.length(), 10);
			if (ordinal <= 0) {
				throw new IllegalArgumentException("Ordinal '" + ordinal + "' in '" + value +
						"' must be positive number ");
			}
			TemporalAdjuster adjuster = dayOfWeekInMonth(ordinal, dayOfWeek);
			return new QuartzCronField(Type.DAY_OF_WEEK, Type.DAY_OF_MONTH, adjuster, value);
		}
		throw new IllegalArgumentException("No 'L' or '#' found in '" + value + "'");
	}

	private static DayOfWeek parseDayOfWeek(String value) {
		int dayOfWeek = Integer.parseInt(value);
		if (dayOfWeek == 0) {
			dayOfWeek = 7;  // cron is 0 based; java.time 1 based
		}
		try {
			return DayOfWeek.of(dayOfWeek);
		}
		catch (DateTimeException ex) {
			throw new IllegalArgumentException(ex.getMessage() + " '" + value + "'", ex);
		}
	}

	/**
	 * 返回重置到午夜的调整器。
	 */
	private static TemporalAdjuster atMidnight() {
		return temporal -> {
			if (temporal.isSupported(ChronoField.NANO_OF_DAY)) {
				return temporal.with(ChronoField.NANO_OF_DAY, 0);
			}
			else {
				return temporal;
			}
		};
	}

	/**
	 * 返回将时间调整到当月最后一天午夜的调整器。
	 */
	private static TemporalAdjuster lastDayOfMonth() {
		TemporalAdjuster adjuster = TemporalAdjusters.lastDayOfMonth();
		return temporal -> {
			Temporal result = adjuster.adjustInto(temporal);
			return rollbackToMidnight(temporal, result);
		};
	}

	/**
	 * 返回当月最后一个工作日的调整器。
	 */
	private static TemporalAdjuster lastWeekdayOfMonth() {
		TemporalAdjuster adjuster = TemporalAdjusters.lastDayOfMonth();
		return temporal -> {
			Temporal lastDom = adjuster.adjustInto(temporal);
			Temporal result;
			int dow = lastDom.get(ChronoField.DAY_OF_WEEK);
			if (dow == 6) {  // Saturday
				result = lastDom.minus(1, ChronoUnit.DAYS);
			}
			else if (dow == 7) {  // Sunday
				result = lastDom.minus(2, ChronoUnit.DAYS);
			}
			else {
				result = lastDom;
			}
			return rollbackToMidnight(temporal, result);
		};
	}

	/**
	 * 返回查找当月倒数第 n 天的 TemporalAdjuster。
	 * @param offset 负偏移，例如 -3 表示倒数第三天
	 * @return 倒数第 n 天的“日”调整器
	 */
	private static TemporalAdjuster lastDayWithOffset(int offset) {
		Assert.isTrue(offset < 0, "Offset should be < 0");
		TemporalAdjuster adjuster = TemporalAdjusters.lastDayOfMonth();
		return temporal -> {
			Temporal result = adjuster.adjustInto(temporal).plus(offset, ChronoUnit.DAYS);
			return rollbackToMidnight(temporal, result);
		};
	}

	/**
	 * 返回查找最接近给定“日”的工作日的 TemporalAdjuster。
	 * 若 {@code dayOfMonth} 落在周六则回退到周五；
	 * 若落在周日（或 {@code dayOfMonth} 为 1 且落在周六）则前进到周一。
	 * @param dayOfMonth 目标“日”
	 * @return 最近工作日的调整器
	 */
	private static TemporalAdjuster weekdayNearestTo(int dayOfMonth) {
		return temporal -> {
			int current = Type.DAY_OF_MONTH.get(temporal);
			DayOfWeek dayOfWeek = DayOfWeek.from(temporal);

			if ((current == dayOfMonth && isWeekday(dayOfWeek)) ||  // dayOfMonth is a weekday
					(dayOfWeek == DayOfWeek.FRIDAY && current == dayOfMonth - 1) ||  // dayOfMonth is a Saturday, so Friday before
					(dayOfWeek == DayOfWeek.MONDAY && current == dayOfMonth + 1) ||  // dayOfMonth is a Sunday, so Monday after
					(dayOfWeek == DayOfWeek.MONDAY && dayOfMonth == 1 && current == 3)) {  // dayOfMonth is Saturday 1st, so Monday 3rd
				return temporal;
			}
			int count = 0;
			while (count++ < CronExpression.MAX_ATTEMPTS) {
				if (current == dayOfMonth) {
					dayOfWeek = DayOfWeek.from(temporal);

					if (dayOfWeek == DayOfWeek.SATURDAY) {
						if (dayOfMonth != 1) {
							temporal = temporal.minus(1, ChronoUnit.DAYS);
						}
						else {
							// exception for "1W" fields: execute on next Monday
							temporal = temporal.plus(2, ChronoUnit.DAYS);
						}
					}
					else if (dayOfWeek == DayOfWeek.SUNDAY) {
						temporal = temporal.plus(1, ChronoUnit.DAYS);
					}
					return atMidnight().adjustInto(temporal);
				}
				else {
					temporal = Type.DAY_OF_MONTH.elapseUntil(cast(temporal), dayOfMonth);
					current = Type.DAY_OF_MONTH.get(temporal);
				}
			}
			return null;
		};
	}

	private static boolean isWeekday(DayOfWeek dayOfWeek) {
		return dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY;
	}

	/**
	 * 返回查找某月内给定星期最后出现的 TemporalAdjuster。
	 */
	private static TemporalAdjuster lastInMonth(DayOfWeek dayOfWeek) {
		TemporalAdjuster adjuster = TemporalAdjusters.lastInMonth(dayOfWeek);
		return temporal -> {
			Temporal result = adjuster.adjustInto(temporal);
			return rollbackToMidnight(temporal, result);
		};
	}

	/**
	 * 返回查找某月内给定星期第 {@code ordinal} 次出现的 TemporalAdjuster。
	 */
	private static TemporalAdjuster dayOfWeekInMonth(int ordinal, DayOfWeek dayOfWeek) {
		TemporalAdjuster adjuster = TemporalAdjusters.dayOfWeekInMonth(ordinal, dayOfWeek);
		return temporal -> {
			// TemporalAdjusters can overflow to a different month
			// in this case, attempt the same adjustment with the next/previous month
			for (int i = 0; i < 12; i++) {
				Temporal result = adjuster.adjustInto(temporal);
				if (result.get(ChronoField.MONTH_OF_YEAR) == temporal.get(ChronoField.MONTH_OF_YEAR)) {
					return rollbackToMidnight(temporal, result);
				}
				temporal = result;
			}
			return null;
		};
	}

	/**
	 * 将给定 {@code result} 回退到午夜。
	 * 当 {@code current} 与 {@code result} 的“日”相同时返回前者，
	 * 以确保不会回退到起点之前。
	 */
	private static Temporal rollbackToMidnight(Temporal current, Temporal result) {
		if (result.get(ChronoField.DAY_OF_MONTH) == current.get(ChronoField.DAY_OF_MONTH)) {
			return current;
		}
		else {
			return atMidnight().adjustInto(result);
		}
	}


	@Override
	public <T extends Temporal & Comparable<? super T>> @Nullable T nextOrSame(T temporal) {
		T result = adjust(temporal);
		if (result != null) {
			if (result.compareTo(temporal) < 0) {
				// We ended up before the start, roll forward and try again
				temporal = this.rollForwardType.rollForward(temporal);
				result = adjust(temporal);
				if (result != null) {
					result = type().reset(result);
				}
			}
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	private <T extends Temporal & Comparable<? super T>> @Nullable T adjust(T temporal) {
		return (T) this.adjuster.adjustInto(temporal);
	}


	@Override
	public boolean equals(@Nullable Object other) {
		return (this == other || (other instanceof QuartzCronField that &&
				type() == that.type() && this.value.equals(that.value)));
	}

	@Override
	public int hashCode() {
		return this.value.hashCode();
	}

	@Override
	public String toString() {
		return type() + " '" + this.value + "'";
	}

}
