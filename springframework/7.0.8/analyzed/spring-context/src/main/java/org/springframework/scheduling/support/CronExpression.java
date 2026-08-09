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

import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.util.Arrays;

import org.jspecify.annotations.Nullable;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * 可计算下次匹配时间的
 * <a href="https://www.manpagez.com/man/5/crontab/">crontab 表达式</a>表示。
 *
 * <p>{@code CronExpression} 实例通过 {@link #parse(String)} 创建；
 * 下次匹配时间由 {@link #next(Temporal)} 确定。
 *
 * <p>支持带 L/# 表达式的 Quartz 日/周字段。其余方面遵循常见 cron 约定，
 * 包括 SUN-SAT 使用 0-6（SUN 也可用 7）。
 * 注意 Quartz 在 cron 中对星期几使用 1-7 表示 SUN-SAT，
 * 而 Spring 即使结合可选 Quartz 专有 L/# 表达式也严格遵循 cron 约定。
 *
 * @author Arjen Poutsma
 * @since 5.3
 * @see CronTrigger
 */
public final class CronExpression {

	static final int MAX_ATTEMPTS = 366;

	private static final String[] MACROS = new String[] {
			"@yearly", "0 0 0 1 1 *",
			"@annually", "0 0 0 1 1 *",
			"@monthly", "0 0 0 1 * *",
			"@weekly", "0 0 0 * * 0",
			"@daily", "0 0 0 * * *",
			"@midnight", "0 0 0 * * *",
			"@hourly", "0 0 * * * *"
	};


	private final CronField[] fields;

	private final String expression;


	private CronExpression(CronField seconds, CronField minutes, CronField hours,
			CronField daysOfMonth, CronField months, CronField daysOfWeek, String expression) {

		// Reverse order, to make big changes first.
		// To make sure we end up at 0 nanos, we add an extra field.
		this.fields = new CronField[] {daysOfWeek, months, daysOfMonth, hours, minutes, seconds, CronField.zeroNanos()};
		this.expression = expression;
	}


	/**
	 * 将给定
	 * <a href="https://www.manpagez.com/man/5/crontab/">crontab 表达式</a>
	 * 字符串解析为 {@code CronExpression}。
	 * 字符串包含六个以单个空格分隔的日期时间字段：
	 * <pre>
	 * &#9484;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472; second (0-59)
	 * &#9474; &#9484;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472; minute (0 - 59)
	 * &#9474; &#9474; &#9484;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472; hour (0 - 23)
	 * &#9474; &#9474; &#9474; &#9484;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472; day of the month (1 - 31)
	 * &#9474; &#9474; &#9474; &#9474; &#9484;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472; month (1 - 12) (or JAN-DEC)
	 * &#9474; &#9474; &#9474; &#9474; &#9474; &#9484;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472; day of the week (0 - 7)
	 * &#9474; &#9474; &#9474; &#9474; &#9474; &#9474;          (0 or 7 is Sunday, or MON-SUN)
	 * &#9474; &#9474; &#9474; &#9474; &#9474; &#9474;
	 * &#42; &#42; &#42; &#42; &#42; &#42;
	 * </pre>
	 *
	 * <p>适用以下规则：
	 * <ul>
	 * <li>
	 * 字段可以是星号 ({@code *})，始终表示“首尾范围”。
	 * 对于“日”或“星期几”字段，可用问号 ({@code ?}) 代替星号。
	 * </li>
	 * <li>
	 * 数字范围由连字符 ({@code -}) 分隔的两个数字表示，范围包含两端。
	 * </li>
	 * <li>在范围（或 {@code *}）后接 {@code /n} 表示该范围内数值的步进间隔。
	 * </li>
	 * <li>
	 * “月”和“星期几”字段也可使用英文名称。
	 * 使用对应日或月的前三个字母（大小写不敏感）。
	 * </li>
	 * <li>
	 * “日”和“星期几”字段可包含 {@code L} 字符，表示“最后”，
	 * 在各字段中含义不同：
	 * <ul>
	 * <li>
	 * 在“日”字段中，{@code L} 表示“当月最后一天”。
	 * 若后跟负偏移（即 {@code L-n}），表示“当月倒数第 {@code n} 天”。
	 * 若后跟 {@code W}（即 {@code LW}），表示“当月最后一个工作日”。
	 * </li>
	 * <li>
	 * 在“星期几”字段中，{@code dL} 或 {@code DDDL} 表示
	 * “当月最后一个星期 {@code d}（或 {@code DDD}）”。
	 * </li>
	 * </ul>
	 * </li>
	 * <li>
	 * “日”字段可为 {@code nW}，表示“距当月第 {@code n} 日最近的工作日”。
	 * 若 {@code n} 为周六，则取前一个周五。
	 * 若 {@code n} 为周日，则取后一个周一；
	 * 若 {@code n} 为 {@code 1} 且落在周六，同样取后一个周一
	 * （即 {@code 1W} 表示“当月第一个工作日”）。
	 * </li>
	 * <li>
	 * “星期几”字段可为 {@code d#n}（或 {@code DDD#n}），
	 * 表示“当月第 {@code n} 个星期 {@code d}（或 {@code DDD}）”。
	 * </li>
	 * </ul>
	 *
	 * <p>示例表达式：
	 * <ul>
	 * <li>{@code "0 0 * * * *"} = 每天每小时的整点</li>
	 * <li><code>"*&#47;10 * * * * *"</code> = 每十秒</li>
	 * <li>{@code "0 0 8-10 * * *"} = 每天 8、9、10 点</li>
	 * <li>{@code "0 0 6,19 * * *"} = 每天 6:00 和 19:00</li>
	 * <li>{@code "0 0/30 8-10 * * *"} = 每天 8:00、8:30、9:00、9:30、10:00 和 10:30</li>
	 * <li>{@code "0 0 9-17 * * MON-FRI"} = 工作日上午 9 点至下午 5 点整点</li>
	 * <li>{@code "0 0 0 25 12 ?"} = 每年圣诞节午夜</li>
	 * <li>{@code "0 0 0 L * *"} = 每月最后一天午夜</li>
	 * <li>{@code "0 0 0 L-3 * *"} = 每月倒数第三天午夜</li>
	 * <li>{@code "0 0 0 1W * *"} = 每月第一个工作日午夜</li>
	 * <li>{@code "0 0 0 LW * *"} = 每月最后一个工作日午夜</li>
	 * <li>{@code "0 0 0 * * 5L"} = 每月最后一个周五午夜</li>
	 * <li>{@code "0 0 0 * * THUL"} = 每月最后一个周四午夜</li>
	 * <li>{@code "0 0 0 ? * 5#2"} = 每月第二个周五午夜</li>
	 * <li>{@code "0 0 0 ? * MON#1"} = 每月第一个周一午夜</li>
	 * </ul>
	 *
	 * <p>还支持以下宏。
	 * <ul>
	 * <li>{@code "@yearly"}（或 {@code "@annually"}）每年运行一次，即 {@code "0 0 0 1 1 *"}</li>
	 * <li>{@code "@monthly"} 每月运行一次，即 {@code "0 0 0 1 * *"}</li>
	 * <li>{@code "@weekly"} 每周运行一次，即 {@code "0 0 0 * * 0"}</li>
	 * <li>{@code "@daily"}（或 {@code "@midnight"}）每天运行一次，即 {@code "0 0 0 * * *"}</li>
	 * <li>{@code "@hourly"} 每小时运行一次，即 {@code "0 0 * * * *"}</li>
	 * </ul>
	 * @param expression 待解析的表达式字符串
	 * @return 解析后的 {@code CronExpression} 对象
	 * @throws IllegalArgumentException 表达式不符合 cron 格式时
	 */
	public static CronExpression parse(String expression) {
		Assert.hasLength(expression, "Expression must not be empty");

		expression = resolveMacros(expression);

		String[] fields = StringUtils.tokenizeToStringArray(expression, " ");
		if (fields.length != 6) {
			throw new IllegalArgumentException(String.format(
					"Cron expression must consist of 6 fields (found %d in \"%s\")", fields.length, expression));
		}
		try {
			CronField seconds = CronField.parseSeconds(fields[0]);
			CronField minutes = CronField.parseMinutes(fields[1]);
			CronField hours = CronField.parseHours(fields[2]);
			CronField daysOfMonth = CronField.parseDaysOfMonth(fields[3]);
			CronField months = CronField.parseMonth(fields[4]);
			CronField daysOfWeek = CronField.parseDaysOfWeek(fields[5]);

			return new CronExpression(seconds, minutes, hours, daysOfMonth, months, daysOfWeek, expression);
		}
		catch (IllegalArgumentException ex) {
			String msg = ex.getMessage() + " in cron expression \"" + expression + "\"";
			throw new IllegalArgumentException(msg, ex);
		}
	}

	/**
	 * 判断给定字符串是否为有效的 cron 表达式。
	 * @param expression 待评估的表达式
	 * @return 若给定表达式有效则返回 {@code true}
	 * @since 5.3.8
	 */
	public static boolean isValidExpression(@Nullable String expression) {
		if (expression == null) {
			return false;
		}
		try {
			parse(expression);
			return true;
		}
		catch (IllegalArgumentException ex) {
			return false;
		}
	}


	private static String resolveMacros(String expression) {
		expression = expression.trim();
		for (int i = 0; i < MACROS.length; i = i + 2) {
			if (MACROS[i].equalsIgnoreCase(expression)) {
				return MACROS[i + 1];
			}
		}
		return expression;
	}


	/**
	 * 计算匹配本表达式的下一个 {@link Temporal}。
	 * @param temporal 种子值
	 * @param <T> 时间类型
	 * @return 匹配本表达式的下一个 temporal，找不到则返回 {@code null}
	 */
	public <T extends Temporal & Comparable<? super T>> @Nullable T next(T temporal) {
		return nextOrSame(ChronoUnit.NANOS.addTo(temporal, 1));
	}


	private <T extends Temporal & Comparable<? super T>> @Nullable T nextOrSame(T temporal) {
		for (int i = 0; i < MAX_ATTEMPTS; i++) {
			T result = nextOrSameInternal(temporal);
			if (result == null || result.equals(temporal)) {
				return result;
			}
			temporal = result;
		}
		return null;
	}

	private <T extends Temporal & Comparable<? super T>> @Nullable T nextOrSameInternal(T temporal) {
		for (CronField field : this.fields) {
			temporal = field.nextOrSame(temporal);
			if (temporal == null) {
				return null;
			}
		}
		return temporal;
	}


	@Override
	public boolean equals(@Nullable Object other) {
		return (this == other || (other instanceof CronExpression that &&
				Arrays.equals(this.fields, that.fields)));
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(this.fields);
	}

	/**
	 * 返回用于创建本 {@code CronExpression} 的表达式字符串。
	 */
	@Override
	public String toString() {
		return this.expression;
	}

}
