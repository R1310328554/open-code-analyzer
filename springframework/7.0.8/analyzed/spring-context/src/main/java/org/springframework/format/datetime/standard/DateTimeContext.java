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

import java.time.ZoneId;
import java.time.chrono.Chronology;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

import org.jspecify.annotations.Nullable;

import org.springframework.context.i18n.LocaleContext;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.i18n.TimeZoneAwareLocaleContext;

/**
 * 保存用户专属的 <code>java.time</code>（JSR-310）设置，
 * 例如用户的历法（Chronology）和时区。
 * <p>属性值为 {@code null} 表示用户未指定该设置。
 *
 * @author Juergen Hoeller
 * @since 4.0
 * @see DateTimeContextHolder
 */
public class DateTimeContext {

	private @Nullable Chronology chronology;

	private @Nullable ZoneId timeZone;


	/**
	 * 设置用户的历法（日历系统）。
	 */
	public void setChronology(@Nullable Chronology chronology) {
		this.chronology = chronology;
	}

	/**
	 * 返回用户的历法（日历系统），若有则返回。
	 */
	public @Nullable Chronology getChronology() {
		return this.chronology;
	}

	/**
	 * 设置用户的时区。
	 * <p>也可在 {@link LocaleContextHolder} 上设置
	 * {@link TimeZoneAwareLocaleContext}。若此处未提供设置，
	 * 本上下文类会回退检查区域上下文。
	 * @see org.springframework.context.i18n.LocaleContextHolder#getTimeZone()
	 * @see org.springframework.context.i18n.LocaleContextHolder#setLocaleContext
	 */
	public void setTimeZone(@Nullable ZoneId timeZone) {
		this.timeZone = timeZone;
	}

	/**
	 * 返回用户的时区，若有则返回。
	 */
	public @Nullable ZoneId getTimeZone() {
		return this.timeZone;
	}


	/**
	 * 获取已将本上下文设置应用到基础 {@code formatter} 上的
	 * {@code DateTimeFormatter}。
	 * @param formatter 建立默认格式化规则的基础格式化器，通常与上下文无关
	 * @return 带上下文信息的 {@code DateTimeFormatter}
	 */
	public DateTimeFormatter getFormatter(DateTimeFormatter formatter) {
		if (this.chronology != null) {
			formatter = formatter.withChronology(this.chronology);
		}
		if (this.timeZone != null) {
			formatter = formatter.withZone(this.timeZone);
		}
		else {
			LocaleContext localeContext = LocaleContextHolder.getLocaleContext();
			if (localeContext instanceof TimeZoneAwareLocaleContext timeZoneAware) {
				TimeZone timeZone = timeZoneAware.getTimeZone();
				if (timeZone != null) {
					formatter = formatter.withZone(timeZone.toZoneId());
				}
			}
		}
		return formatter;
	}

}
