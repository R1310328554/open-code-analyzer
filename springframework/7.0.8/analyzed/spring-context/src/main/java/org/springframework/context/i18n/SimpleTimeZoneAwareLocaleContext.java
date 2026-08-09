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

package org.springframework.context.i18n;

import java.util.Locale;
import java.util.TimeZone;

import org.jspecify.annotations.Nullable;

/**
 * {@link TimeZoneAwareLocaleContext} 接口的简单实现，
 * 始终返回指定的 {@code Locale} 和 {@code TimeZone}。
 *
 * <p>注意：若只需设置 Locale 而不设置 TimeZone，优先使用 {@link SimpleLocaleContext}。
 *
 * @author Juergen Hoeller
 * @author Nicholas Williams
 * @since 4.0
 * @see LocaleContextHolder#setLocaleContext
 * @see LocaleContextHolder#getTimeZone()
 */
public class SimpleTimeZoneAwareLocaleContext extends SimpleLocaleContext implements TimeZoneAwareLocaleContext {

	/** 要暴露的固定时区。 */
	private final @Nullable TimeZone timeZone;


	/**
	 * 创建新的 SimpleTimeZoneAwareLocaleContext，暴露指定的 Locale 和 TimeZone。
	 * 每次 {@link #getLocale()} 调用返回给定 Locale，每次 {@link #getTimeZone()} 调用返回给定 TimeZone。
	 * @param locale 要暴露的 Locale
	 * @param timeZone 要暴露的 TimeZone
	 */
	public SimpleTimeZoneAwareLocaleContext(@Nullable Locale locale, @Nullable TimeZone timeZone) {
		super(locale);
		this.timeZone = timeZone;
	}


	@Override
	public @Nullable TimeZone getTimeZone() {
		return this.timeZone;
	}

	@Override
	public String toString() {
		return super.toString() + " " + (this.timeZone != null ? this.timeZone : "-");
	}

}
