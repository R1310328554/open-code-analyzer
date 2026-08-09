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

import org.springframework.core.NamedInheritableThreadLocal;
import org.springframework.core.NamedThreadLocal;

/**
 * 将 {@link LocaleContext} 实例与当前线程关联的简单持有类。
 * 若 {@code inheritable} 标志设为 {@code true}，当前线程派生的子线程将继承该 {@code LocaleContext}。
 *
 * <p>在 Spring 中作为当前 Locale 的中央持有器，在需要时使用——
 * 例如 {@link org.springframework.context.support.MessageSourceAccessor}。
 * {@link org.springframework.web.servlet.DispatcherServlet} 会自动在此暴露其当前 Locale。
 * 其他应用也可暴露各自的 Locale，使 {@code MessageSourceAccessor} 等类自动使用该 Locale。
 *
 * @author Juergen Hoeller
 * @author Nicholas Williams
 * @since 1.2
 * @see LocaleContext
 * @see org.springframework.context.support.MessageSourceAccessor
 * @see org.springframework.web.servlet.DispatcherServlet
 */
public final class LocaleContextHolder {

	private static final ThreadLocal<LocaleContext> localeContextHolder =
			new NamedThreadLocal<>("LocaleContext");

	private static final ThreadLocal<LocaleContext> inheritableLocaleContextHolder =
			new NamedInheritableThreadLocal<>("LocaleContext");

	// Shared default locale at the framework level
	private static @Nullable Locale defaultLocale;

	// Shared default time zone at the framework level
	private static @Nullable TimeZone defaultTimeZone;


	private LocaleContextHolder() {
	}


	/**
	 * 重置当前线程的 LocaleContext。
	 */
	public static void resetLocaleContext() {
		localeContextHolder.remove();
		inheritableLocaleContextHolder.remove();
	}

	/**
	 * 将给定 LocaleContext 与当前线程关联，
	 * <i>不</i>向子线程暴露为可继承。
	 * <p>给定的 LocaleContext 可以是 {@link TimeZoneAwareLocaleContext}，
	 * 包含关联了时区信息的 Locale。
	 * @param localeContext 当前 LocaleContext，
	 * 或 {@code null} 以重置线程绑定的上下文
	 * @see SimpleLocaleContext
	 * @see SimpleTimeZoneAwareLocaleContext
	 */
	public static void setLocaleContext(@Nullable LocaleContext localeContext) {
		setLocaleContext(localeContext, false);
	}

	/**
	 * 将给定 LocaleContext 与当前线程关联。
	 * <p>给定的 LocaleContext 可以是 {@link TimeZoneAwareLocaleContext}，
	 * 包含关联了时区信息的 Locale。
	 * @param localeContext 当前 LocaleContext，
	 * 或 {@code null} 以重置线程绑定的上下文
	 * @param inheritable 是否将 LocaleContext 作为可继承项暴露给子线程
	 * （使用 {@link InheritableThreadLocal}）
	 * @see SimpleLocaleContext
	 * @see SimpleTimeZoneAwareLocaleContext
	 */
	public static void setLocaleContext(@Nullable LocaleContext localeContext, boolean inheritable) {
		if (localeContext == null) {
			resetLocaleContext();
		}
		else {
			if (inheritable) {
				inheritableLocaleContextHolder.set(localeContext);
				localeContextHolder.remove();
			}
			else {
				localeContextHolder.set(localeContext);
				inheritableLocaleContextHolder.remove();
			}
		}
	}

	/**
	 * 返回与当前线程关联的 LocaleContext（若有）。
	 * @return 当前 LocaleContext；若无则返回 {@code null}
	 */
	public static @Nullable LocaleContext getLocaleContext() {
		LocaleContext localeContext = localeContextHolder.get();
		if (localeContext == null) {
			localeContext = inheritableLocaleContextHolder.get();
		}
		return localeContext;
	}

	/**
	 * 将给定 Locale 与当前线程关联，
	 * 保留可能已设置的 TimeZone。
	 * <p>将为给定 Locale 隐式创建 LocaleContext，
	 * <i>不</i>向子线程暴露为可继承。
	 * @param locale 当前 Locale，或 {@code null} 以重置线程绑定上下文的 Locale 部分
	 * @see #setTimeZone(TimeZone)
	 * @see SimpleLocaleContext#SimpleLocaleContext(Locale)
	 */
	public static void setLocale(@Nullable Locale locale) {
		setLocale(locale, false);
	}

	/**
	 * 将给定 Locale 与当前线程关联，
	 * 保留可能已设置的 TimeZone。
	 * <p>将为给定 Locale 隐式创建 LocaleContext。
	 * @param locale 当前 Locale，或 {@code null} 以重置线程绑定上下文的 Locale 部分
	 * @param inheritable 是否将 LocaleContext 作为可继承项暴露给子线程
	 * （使用 {@link InheritableThreadLocal}）
	 * @see #setTimeZone(TimeZone, boolean)
	 * @see SimpleLocaleContext#SimpleLocaleContext(Locale)
	 */
	public static void setLocale(@Nullable Locale locale, boolean inheritable) {
		LocaleContext localeContext = getLocaleContext();
		TimeZone timeZone = (localeContext instanceof TimeZoneAwareLocaleContext timeZoneAware ?
				timeZoneAware.getTimeZone() : null);
		if (timeZone != null) {
			localeContext = new SimpleTimeZoneAwareLocaleContext(locale, timeZone);
		}
		else if (locale != null) {
			localeContext = new SimpleLocaleContext(locale);
		}
		else {
			localeContext = null;
		}
		setLocaleContext(localeContext, inheritable);
	}

	/**
	 * 在框架级别设置共享默认 Locale，
	 * 作为 JVM 全局默认 Locale 的替代方案。
	 * <p><b>注意：</b> 当应用级默认 Locale 与 JVM 全局默认不同时，此方式很有用。
	 * 但要求各应用针对本地部署的 Spring Framework jar 运行。
	 * 在此场景下请勿在服务器级别将 Spring 作为共享库部署！
	 * @param locale 默认 Locale（或 {@code null} 表示无，
	 * 查找将回退到 {@link Locale#getDefault()}）
	 * @since 4.3.5
	 * @see #getLocale()
	 * @see Locale#getDefault()
	 */
	public static void setDefaultLocale(@Nullable Locale locale) {
		LocaleContextHolder.defaultLocale = locale;
	}

	/**
	 * 返回与当前线程关联的 Locale（若有），
	 * 否则返回系统默认 Locale。这实际上是
	 * {@link java.util.Locale#getDefault()} 的替代方案，
	 * 可选择尊重用户级 Locale 设置。
	 * <p>注意：本方法会回退到共享默认 Locale，
	 * 无论是框架级别还是 JVM 系统级别。
	 * 若要检查原始 LocaleContext 内容
	 * （可能通过 {@code null} 表示无特定 Locale），请使用
	 * {@link #getLocaleContext()} 并调用 {@link LocaleContext#getLocale()}。
	 * @return 当前 Locale；若当前线程未关联特定 Locale 则返回系统默认 Locale
	 * @see #getLocaleContext()
	 * @see LocaleContext#getLocale()
	 * @see #setDefaultLocale(Locale)
	 * @see java.util.Locale#getDefault()
	 */
	public static Locale getLocale() {
		return getLocale(getLocaleContext());
	}

	/**
	 * 返回与给定用户上下文关联的 Locale（若有），
	 * 否则返回系统默认 Locale。这实际上是
	 * {@link java.util.Locale#getDefault()} 的替代方案，
	 * 可选择尊重用户级 Locale 设置。
	 * @param localeContext 要检查的用户级 Locale 上下文
	 * @return 当前 Locale；若当前线程未关联特定 Locale 则返回系统默认 Locale
	 * @since 5.0
	 * @see #getLocale()
	 * @see LocaleContext#getLocale()
	 * @see #setDefaultLocale(Locale)
	 * @see java.util.Locale#getDefault()
	 */
	public static Locale getLocale(@Nullable LocaleContext localeContext) {
		if (localeContext != null) {
			Locale locale = localeContext.getLocale();
			if (locale != null) {
				return locale;
			}
		}
		return (defaultLocale != null ? defaultLocale : Locale.getDefault());
	}

	/**
	 * 将给定 TimeZone 与当前线程关联，
	 * 保留可能已设置的 Locale。
	 * <p>将为给定 Locale 隐式创建 LocaleContext，
	 * <i>不</i>向子线程暴露为可继承。
	 * @param timeZone 当前 TimeZone，或 {@code null} 以重置线程绑定上下文的时区部分
	 * @see #setLocale(Locale)
	 * @see SimpleTimeZoneAwareLocaleContext#SimpleTimeZoneAwareLocaleContext(Locale, TimeZone)
	 */
	public static void setTimeZone(@Nullable TimeZone timeZone) {
		setTimeZone(timeZone, false);
	}

	/**
	 * 将给定 TimeZone 与当前线程关联，
	 * 保留可能已设置的 Locale。
	 * <p>将为给定 Locale 隐式创建 LocaleContext。
	 * @param timeZone 当前 TimeZone，或 {@code null} 以重置线程绑定上下文的时区部分
	 * @param inheritable 是否将 LocaleContext 作为可继承项暴露给子线程
	 * （使用 {@link InheritableThreadLocal}）
	 * @see #setLocale(Locale, boolean)
	 * @see SimpleTimeZoneAwareLocaleContext#SimpleTimeZoneAwareLocaleContext(Locale, TimeZone)
	 */
	public static void setTimeZone(@Nullable TimeZone timeZone, boolean inheritable) {
		LocaleContext localeContext = getLocaleContext();
		Locale locale = (localeContext != null ? localeContext.getLocale() : null);
		if (timeZone != null) {
			localeContext = new SimpleTimeZoneAwareLocaleContext(locale, timeZone);
		}
		else if (locale != null) {
			localeContext = new SimpleLocaleContext(locale);
		}
		else {
			localeContext = null;
		}
		setLocaleContext(localeContext, inheritable);
	}

	/**
	 * 在框架级别设置共享默认时区，
	 * 作为 JVM 全局默认时区的替代方案。
	 * <p><b>注意：</b> 当应用级默认时区与 JVM 全局默认不同时，此方式很有用。
	 * 但要求各应用针对本地部署的 Spring Framework jar 运行。
	 * 在此场景下请勿在服务器级别将 Spring 作为共享库部署！
	 * @param timeZone 默认时区（或 {@code null} 表示无，
	 * 查找将回退到 {@link TimeZone#getDefault()}）
	 * @since 4.3.5
	 * @see #getTimeZone()
	 * @see TimeZone#getDefault()
	 */
	public static void setDefaultTimeZone(@Nullable TimeZone timeZone) {
		defaultTimeZone = timeZone;
	}

	/**
	 * 返回与当前线程关联的 TimeZone（若有），
	 * 否则返回系统默认 TimeZone。这实际上是
	 * {@link java.util.TimeZone#getDefault()} 的替代方案，
	 * 可选择尊重用户级 TimeZone 设置。
	 * <p>注意：本方法会回退到共享默认 TimeZone，
	 * 无论是框架级别还是 JVM 系统级别。
	 * 若要检查原始 LocaleContext 内容
	 * （可能通过 {@code null} 表示无特定时区），请使用
	 * {@link #getLocaleContext()}，向下转型为 {@link TimeZoneAwareLocaleContext}
	 * 后调用 {@link TimeZoneAwareLocaleContext#getTimeZone()}。
	 * @return 当前 TimeZone；若当前线程未关联特定 TimeZone 则返回系统默认 TimeZone
	 * @see #getLocaleContext()
	 * @see TimeZoneAwareLocaleContext#getTimeZone()
	 * @see #setDefaultTimeZone(TimeZone)
	 * @see java.util.TimeZone#getDefault()
	 */
	public static TimeZone getTimeZone() {
		return getTimeZone(getLocaleContext());
	}

	/**
	 * 返回与给定用户上下文关联的 TimeZone（若有），
	 * 否则返回系统默认 TimeZone。这实际上是
	 * {@link java.util.TimeZone#getDefault()} 的替代方案，
	 * 可选择尊重用户级 TimeZone 设置。
	 * @param localeContext 要检查的用户级 Locale 上下文
	 * @return 当前 TimeZone；若当前线程未关联特定 TimeZone 则返回系统默认 TimeZone
	 * @since 5.0
	 * @see #getTimeZone()
	 * @see TimeZoneAwareLocaleContext#getTimeZone()
	 * @see #setDefaultTimeZone(TimeZone)
	 * @see java.util.TimeZone#getDefault()
	 */
	public static TimeZone getTimeZone(@Nullable LocaleContext localeContext) {
		if (localeContext instanceof TimeZoneAwareLocaleContext timeZoneAware) {
			TimeZone timeZone = timeZoneAware.getTimeZone();
			if (timeZone != null) {
				return timeZone;
			}
		}
		return (defaultTimeZone != null ? defaultTimeZone : TimeZone.getDefault());
	}

}
