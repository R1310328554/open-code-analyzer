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

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.TimeZone;

import org.jspecify.annotations.Nullable;

import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/* ===== [OCA 中文解析] =====
class DateTimeFormatterFactory — 意图说明

工厂：封装复杂创建逻辑；源文件: `spring-context/src/main/java/org/springframework/format/datetime/standard/DateTimeFormatterFactory.java`

（本注释由 open-code-analyzer 生成，置于原有文档注释之前）
===== [OCA 中文解析结束] ===== */
/**
 * 创建 JSR-310 {@link java.time.format.DateTimeFormatter} 的工厂。
 *
 * <p>格式化器将按 {@link #setPattern pattern}、{@link #setIso ISO}
 * 以及 <code>xxxStyle</code> 方法的定义顺序创建。
 *
 * @author Juergen Hoeller
 * @author Phillip Webb
 * @author Sam Brannen
 * @since 4.0
 * @see #createDateTimeFormatter()
 * @see #createDateTimeFormatter(DateTimeFormatter)
 * @see #setPattern
 * @see #setIso
 * @see #setDateStyle
 * @see #setTimeStyle
 * @see #setDateTimeStyle
 * @see DateTimeFormatterFactoryBean
 */
public class DateTimeFormatterFactory {

	private @Nullable String pattern;

	private @Nullable ISO iso;

	private @Nullable FormatStyle dateStyle;

	private @Nullable FormatStyle timeStyle;

	private @Nullable TimeZone timeZone;


	/**
	 * 创建新的 {@code DateTimeFormatterFactory} 实例。
	 */
	public DateTimeFormatterFactory() {
	}

	/**
	 * Create a new {@code DateTimeFormatterFactory} instance.
	 * @param pattern 用于格式化日期值的图案
	 */
	public DateTimeFormatterFactory(String pattern) {
		this.pattern = pattern;
	}


	/**
	 * 设置用于格式化日期值的图案。
	 * @param pattern 格式图案
	 */
	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	/**
	 * 设置用于格式化日期值的 ISO 格式。
	 * @param iso ISO 格式
	 */
	public void setIso(ISO iso) {
		this.iso = iso;
	}

	/**
	 * 设置用于日期类型的样式。
	 */
	public void setDateStyle(FormatStyle dateStyle) {
		this.dateStyle = dateStyle;
	}

	/**
	 * 设置用于时间类型的样式。
	 */
	public void setTimeStyle(FormatStyle timeStyle) {
		this.timeStyle = timeStyle;
	}

	/**
	 * 设置用于日期时间类型的样式。
	 */
	public void setDateTimeStyle(FormatStyle dateTimeStyle) {
		this.dateStyle = dateTimeStyle;
		this.timeStyle = dateTimeStyle;
	}

	/**
	 * 设置用于格式化日期值的两个字符。
	 * <p>第一个字符用于日期样式，第二个用于时间样式。支持的字符为：
	 * <ul>
	 * <li>'S' = Small（简短）</li>
	 * <li>'M' = Medium（中等）</li>
	 * <li>'L' = Long（长）</li>
	 * <li>'F' = Full（完整）</li>
	 * <li>'-' = Omitted（省略）</li>
	 * </ul>
	 * <p>注意，JSR-310 原生偏好 {@link java.time.format.FormatStyle}，
	 * 与 {@link #setDateStyle}、{@link #setTimeStyle} 和
	 * {@link #setDateTimeStyle} 所用方式一致。
	 * @param style 来自集合 {"S", "M", "L", "F", "-"} 的两个字符
	 */
	public void setStylePattern(String style) {
		Assert.isTrue(style.length() == 2, "Style pattern must consist of two characters");
		this.dateStyle = convertStyleCharacter(style.charAt(0));
		this.timeStyle = convertStyleCharacter(style.charAt(1));
	}

	private @Nullable FormatStyle convertStyleCharacter(char c) {
		return switch (c) {
			case 'S' -> FormatStyle.SHORT;
			case 'M' -> FormatStyle.MEDIUM;
			case 'L' -> FormatStyle.LONG;
			case 'F' -> FormatStyle.FULL;
			case '-' -> null;
			default -> throw new IllegalArgumentException("Invalid style character '" + c + "'");
		};
	}

	/**
	 * 设置用于将日期值规范化的 {@code TimeZone}，若有则设置。
	 * @param timeZone 时区
	 */
	public void setTimeZone(TimeZone timeZone) {
		this.timeZone = timeZone;
	}


	/* ===== [OCA 中文解析] =====
方法 createDateTimeFormatter — 意图与阅读要点

方法 `createDateTimeFormatter` 复杂度较高（CCN≈14, NLOC≈27）。阅读时建议先抓住主路径，再看分支/异常/缓存等旁路逻辑；关注它在调用链中上下游的契约（入参约束、返回值语义、抛出的异常）。
	===== [OCA 中文解析结束] ===== */
	/**
	 * 使用本工厂创建新的 {@code DateTimeFormatter}。
	 * <p>若未定义特定图案或样式，将使用
	 * {@link FormatStyle#MEDIUM 中等日期时间格式}。
	 * @return 新的日期时间格式化器
	 * @see #createDateTimeFormatter(DateTimeFormatter)
	 */
	public DateTimeFormatter createDateTimeFormatter() {
		return createDateTimeFormatter(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM));
	}

	/* ===== [OCA 中文解析] =====
方法 createDateTimeFormatter — 意图与阅读要点

方法 `createDateTimeFormatter` 复杂度较高（CCN≈14, NLOC≈27）。阅读时建议先抓住主路径，再看分支/异常/缓存等旁路逻辑；关注它在调用链中上下游的契约（入参约束、返回值语义、抛出的异常）。
	===== [OCA 中文解析结束] ===== */
	/**
	 * Create a new {@code DateTimeFormatter} using this factory.
	 * <p>If no specific pattern or style has been defined,
	 * 将使用提供的 {@code fallbackFormatter}。
	 * @param fallbackFormatter 在未设置工厂特定属性时使用的回退格式化器
	 * @return a new date time formatter
	 */
	public DateTimeFormatter createDateTimeFormatter(DateTimeFormatter fallbackFormatter) {
		DateTimeFormatter dateTimeFormatter = null;
		if (StringUtils.hasLength(this.pattern)) {
			dateTimeFormatter = DateTimeFormatterUtils.createStrictDateTimeFormatter(this.pattern);
		}
		else if (this.iso != null && this.iso != ISO.NONE) {
			dateTimeFormatter = switch (this.iso) {
				case DATE -> DateTimeFormatter.ISO_DATE;
				case TIME -> DateTimeFormatter.ISO_TIME;
				case DATE_TIME -> DateTimeFormatter.ISO_DATE_TIME;
				default -> throw new IllegalStateException("Unsupported ISO format: " + this.iso);
			};
		}
		else if (this.dateStyle != null && this.timeStyle != null) {
			dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(this.dateStyle, this.timeStyle);
		}
		else if (this.dateStyle != null) {
			dateTimeFormatter = DateTimeFormatter.ofLocalizedDate(this.dateStyle);
		}
		else if (this.timeStyle != null) {
			dateTimeFormatter = DateTimeFormatter.ofLocalizedTime(this.timeStyle);
		}

		if (dateTimeFormatter != null && this.timeZone != null) {
			dateTimeFormatter = dateTimeFormatter.withZone(this.timeZone.toZoneId());
		}
		return (dateTimeFormatter != null ? dateTimeFormatter : fallbackFormatter);
	}

}
