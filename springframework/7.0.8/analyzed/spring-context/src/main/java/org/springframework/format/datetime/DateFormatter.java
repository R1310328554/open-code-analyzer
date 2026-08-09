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

package org.springframework.format.datetime;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import org.jspecify.annotations.Nullable;

import org.springframework.format.Formatter;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * 用于 {@link java.util.Date} 类型的格式化器。
 *
 * <p>支持配置显式日期时间模式、时区、区域设置，以及用于宽松解析的回退日期时间模式。
 *
 * <p>常用的 UTC 时刻 ISO 模式以毫秒精度应用。
 * 注意，若需灵活地将 UTC 解析为 {@link java.time.Instant}，推荐使用
 * {@link org.springframework.format.datetime.standard.InstantFormatter}。
 *
 * @author Keith Donald
 * @author Juergen Hoeller
 * @author Phillip Webb
 * @author Sam Brannen
 * @since 3.0
 * @see SimpleDateFormat
 */
public class DateFormatter implements Formatter<Date> {

	private static final TimeZone UTC = TimeZone.getTimeZone("UTC");

	private static final Map<ISO, String> ISO_PATTERNS;

	private static final Map<ISO, String> ISO_FALLBACK_PATTERNS;

	static {
		// We use an EnumMap instead of Map.of(...) since the former provides better performance.
		Map<ISO, String> formats = new EnumMap<>(ISO.class);
		formats.put(ISO.DATE, "yyyy-MM-dd");
		formats.put(ISO.TIME, "HH:mm:ss.SSSXXX");
		formats.put(ISO.DATE_TIME, "yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
		ISO_PATTERNS = Collections.unmodifiableMap(formats);

		// Fallback format for the time part without milliseconds.
		Map<ISO, String> fallbackFormats = new EnumMap<>(ISO.class);
		fallbackFormats.put(ISO.TIME, "HH:mm:ssXXX");
		fallbackFormats.put(ISO.DATE_TIME, "yyyy-MM-dd'T'HH:mm:ssXXX");
		ISO_FALLBACK_PATTERNS = Collections.unmodifiableMap(fallbackFormats);
	}


	private @Nullable Object source;

	private @Nullable String pattern;

	private String @Nullable [] fallbackPatterns;

	private int style = DateFormat.DEFAULT;

	private @Nullable String stylePattern;

	private @Nullable ISO iso;

	private @Nullable TimeZone timeZone;

	private boolean lenient = false;


	/**
	 * 创建新的默认 {@code DateFormatter}。
	 */
	public DateFormatter() {
	}

	/**
	 * 为给定日期时间模式创建新的 {@code DateFormatter}。
	 */
	public DateFormatter(String pattern) {
		this.pattern = pattern;
	}


	/**
	 * 设置本 {@code DateFormatter} 的配置来源 &mdash;
	 * 例如，若使用 {@link DateTimeFormat @DateTimeFormat} 注解配置本 {@code DateFormatter}，
	 * 则可传入该注解的实例。
	 * <p>所提供的源对象仅用于描述性目的，通过调用其 {@code toString()} 方法 &mdash;
	 * 例如在生成异常消息以提供更多上下文时。
	 * @param source 配置来源
	 * @since 5.3.5
	 */
	public void setSource(Object source) {
		this.source = source;
	}

	/**
	 * 设置用于格式化日期值的模式。
	 * <p>若未指定，将使用 DateFormat 的默认样式。
	 */
	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	/**
	 * 设置当已配置的 {@linkplain #setPattern 模式}、{@linkplain #setIso ISO 格式}、
	 * {@linkplain #setStyle 样式} 或 {@linkplain #setStylePattern 样式模式} 解析失败时用作回退的附加模式。
	 * @param fallbackPatterns 回退解析模式
	 * @since 5.3.5
	 * @see DateTimeFormat#fallbackPatterns()
	 */
	public void setFallbackPatterns(String... fallbackPatterns) {
		this.fallbackPatterns = fallbackPatterns;
	}

	/**
	 * 设置用于格式化日期值的 ISO 格式。
	 * @param iso {@link ISO} 格式
	 * @since 3.2
	 */
	public void setIso(ISO iso) {
		this.iso = iso;
	}

	/**
	 * 设置用于格式化日期值的 {@link DateFormat} 样式。
	 * <p>若未指定，将使用 DateFormat 的默认样式。
	 * @see DateFormat#DEFAULT
	 * @see DateFormat#SHORT
	 * @see DateFormat#MEDIUM
	 * @see DateFormat#LONG
	 * @see DateFormat#FULL
	 */
	public void setStyle(int style) {
		this.style = style;
	}

	/**
	 * 设置用于格式化日期值的两个字符。
	 * <p>第一个字符用于日期样式；第二个用于时间样式。
	 * <p>支持的字符：
	 * <ul>
	 * <li>'S' = Small（短）</li>
	 * <li>'M' = Medium（中）</li>
	 * <li>'L' = Long（长）</li>
	 * <li>'F' = Full（完整）</li>
	 * <li>'-' = Omitted（省略）</li>
	 * </ul>
	 * @param stylePattern 来自集合 {"S", "M", "L", "F", "-"} 的两个字符
	 * @since 3.2
	 */
	public void setStylePattern(String stylePattern) {
		this.stylePattern = stylePattern;
	}

	/**
	 * 设置用于将日期值规范化的 {@link TimeZone}（若有）。
	 */
	public void setTimeZone(TimeZone timeZone) {
		this.timeZone = timeZone;
	}

	/**
	 * 指定解析是否宽松。默认为 {@code false}。
	 * <p>宽松解析时，解析器可能允许与格式不完全匹配的输入。
	 * 严格解析时，输入必须精确匹配格式。
	 */
	public void setLenient(boolean lenient) {
		this.lenient = lenient;
	}


	@Override
	public String print(Date date, Locale locale) {
		return getDateFormat(locale).format(date);
	}

	@Override
	public Date parse(String text, Locale locale) throws ParseException {
		try {
			return getDateFormat(locale).parse(text);
		}
		catch (ParseException ex) {
			Set<String> fallbackPatterns = new LinkedHashSet<>();
			String isoPattern = ISO_FALLBACK_PATTERNS.get(this.iso);
			if (isoPattern != null) {
				fallbackPatterns.add(isoPattern);
			}
			if (!ObjectUtils.isEmpty(this.fallbackPatterns)) {
				Collections.addAll(fallbackPatterns, this.fallbackPatterns);
			}
			if (!fallbackPatterns.isEmpty()) {
				for (String pattern : fallbackPatterns) {
					try {
						DateFormat dateFormat = configureDateFormat(new SimpleDateFormat(pattern, locale));
						// Align timezone for parsing format with printing format if ISO is set.
						if (this.iso != null && this.iso != ISO.NONE) {
							dateFormat.setTimeZone(UTC);
						}
						return dateFormat.parse(text);
					}
					catch (ParseException ignoredException) {
						// Ignore fallback parsing exceptions since the exception thrown below
						// will include information from the "source" if available -- for example,
						// the toString() of a @DateTimeFormat annotation.
					}
				}
			}
			if (this.source != null) {
				ParseException parseException = new ParseException(
						String.format("Unable to parse date time value \"%s\" using configuration from %s", text, this.source),
						ex.getErrorOffset());
				parseException.initCause(ex);
				throw parseException;
			}
			// else rethrow original exception
			throw ex;
		}
	}


	protected DateFormat getDateFormat(Locale locale) {
		return configureDateFormat(createDateFormat(locale));
	}

	private DateFormat configureDateFormat(DateFormat dateFormat) {
		if (this.timeZone != null) {
			dateFormat.setTimeZone(this.timeZone);
		}
		dateFormat.setLenient(this.lenient);
		return dateFormat;
	}

	private DateFormat createDateFormat(Locale locale) {
		if (StringUtils.hasLength(this.pattern)) {
			return new SimpleDateFormat(this.pattern, locale);
		}
		if (this.iso != null && this.iso != ISO.NONE) {
			String pattern = ISO_PATTERNS.get(this.iso);
			if (pattern == null) {
				throw new IllegalStateException("Unsupported ISO format " + this.iso);
			}
			SimpleDateFormat format = new SimpleDateFormat(pattern);
			format.setTimeZone(UTC);
			return format;
		}
		if (StringUtils.hasLength(this.stylePattern)) {
			int dateStyle = getStylePatternForChar(0);
			int timeStyle = getStylePatternForChar(1);
			if (dateStyle != -1 && timeStyle != -1) {
				return DateFormat.getDateTimeInstance(dateStyle, timeStyle, locale);
			}
			if (dateStyle != -1) {
				return DateFormat.getDateInstance(dateStyle, locale);
			}
			if (timeStyle != -1) {
				return DateFormat.getTimeInstance(timeStyle, locale);
			}
			throw unsupportedStylePatternException();

		}
		return DateFormat.getDateInstance(this.style, locale);
	}

	private int getStylePatternForChar(int index) {
		if (this.stylePattern != null && this.stylePattern.length() > index) {
			char ch = this.stylePattern.charAt(index);
			return switch (ch) {
				case 'S' -> DateFormat.SHORT;
				case 'M' -> DateFormat.MEDIUM;
				case 'L' -> DateFormat.LONG;
				case 'F' -> DateFormat.FULL;
				case '-' -> -1;
				default -> throw unsupportedStylePatternException();
			};
		}
		throw unsupportedStylePatternException();
	}

	private IllegalStateException unsupportedStylePatternException() {
		return new IllegalStateException("Unsupported style pattern '" + this.stylePattern + "'");
	}

}
