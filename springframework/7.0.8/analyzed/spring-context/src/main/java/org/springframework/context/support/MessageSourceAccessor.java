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

package org.springframework.context.support;

import java.util.Locale;

import org.jspecify.annotations.Nullable;

import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;

/**
 * 便于从 MessageSource 访问消息的辅助类，提供多种重载的 getMessage 方法。
 *
 * <p>可通过 ApplicationObjectSupport 获取，也可作为独立辅助类在应用对象中委托使用。
 *
 * @author Juergen Hoeller
 * @since 23.10.2003
 * @see ApplicationObjectSupport#getMessageSourceAccessor
 */
public class MessageSourceAccessor {

	/** 被包装的 MessageSource。 */
	private final MessageSource messageSource;

	/** 默认区域；为 null 时使用 LocaleContextHolder 的区域。 */
	private final @Nullable Locale defaultLocale;


	/**
	 * 创建新的 MessageSourceAccessor，以 LocaleContextHolder 的区域作为默认区域。
	 * @param messageSource 要包装的 MessageSource
	 * @see org.springframework.context.i18n.LocaleContextHolder#getLocale()
	 */
	public MessageSourceAccessor(MessageSource messageSource) {
		this.messageSource = messageSource;
		this.defaultLocale = null;
	}

	/**
	 * 创建新的 MessageSourceAccessor，使用给定默认区域。
	 * @param messageSource 要包装的 MessageSource
	 * @param defaultLocale 消息访问使用的默认区域
	 */
	public MessageSourceAccessor(MessageSource messageSource, Locale defaultLocale) {
		this.messageSource = messageSource;
		this.defaultLocale = defaultLocale;
	}


	/**
	 * 返回未显式指定区域时使用的默认区域。
	 * <p>默认实现返回传入对应构造函数的默认区域，或回退到 LocaleContextHolder 的区域。
	 * 子类可覆盖。
	 * @see #MessageSourceAccessor(org.springframework.context.MessageSource, java.util.Locale)
	 * @see org.springframework.context.i18n.LocaleContextHolder#getLocale()
	 */
	protected Locale getDefaultLocale() {
		return (this.defaultLocale != null ? this.defaultLocale : LocaleContextHolder.getLocale());
	}

	/**
	 * 使用默认区域检索给定代码的消息。
	 * @param code 消息代码
	 * @param defaultMessage 查找失败时返回的字符串
	 * @return 消息文本
	 */
	public String getMessage(String code, String defaultMessage) {
		String msg = this.messageSource.getMessage(code, null, defaultMessage, getDefaultLocale());
		return (msg != null ? msg : "");
	}

	/**
	 * 使用给定区域检索给定代码的消息。
	 * @param code 消息代码
	 * @param defaultMessage 查找失败时返回的字符串
	 * @param locale 查找使用的区域
	 * @return 消息文本
	 */
	public String getMessage(String code, String defaultMessage, Locale locale) {
		String msg = this.messageSource.getMessage(code, null, defaultMessage, locale);
		return (msg != null ? msg : "");
	}

	/**
	 * 使用默认区域检索给定代码的消息。
	 * @param code 消息代码
	 * @param args 消息参数，无则为 {@code null}
	 * @param defaultMessage 查找失败时返回的字符串
	 * @return 消息文本
	 */
	public String getMessage(String code, Object @Nullable [] args, String defaultMessage) {
		String msg = this.messageSource.getMessage(code, args, defaultMessage, getDefaultLocale());
		return (msg != null ? msg : "");
	}

	/**
	 * 使用给定区域检索给定代码的消息。
	 * @param code 消息代码
	 * @param args 消息参数，无则为 {@code null}
	 * @param defaultMessage 查找失败时返回的字符串
	 * @param locale 查找使用的区域
	 * @return 消息文本
	 */
	public String getMessage(String code, Object @Nullable [] args, String defaultMessage, Locale locale) {
		String msg = this.messageSource.getMessage(code, args, defaultMessage, locale);
		return (msg != null ? msg : "");
	}

	/**
	 * 使用默认区域检索给定代码的消息。
	 * @param code 消息代码
	 * @return 消息文本
	 * @throws org.springframework.context.NoSuchMessageException 若未找到
	 */
	public String getMessage(String code) throws NoSuchMessageException {
		return this.messageSource.getMessage(code, null, getDefaultLocale());
	}

	/**
	 * 使用给定区域检索给定代码的消息。
	 * @param code 消息代码
	 * @param locale 查找使用的区域
	 * @return 消息文本
	 * @throws org.springframework.context.NoSuchMessageException 若未找到
	 */
	public String getMessage(String code, Locale locale) throws NoSuchMessageException {
		return this.messageSource.getMessage(code, null, locale);
	}

	/**
	 * 使用默认区域检索给定代码的消息。
	 * @param code 消息代码
	 * @param args 消息参数，无则为 {@code null}
	 * @return 消息文本
	 * @throws org.springframework.context.NoSuchMessageException 若未找到
	 */
	public String getMessage(String code, Object @Nullable [] args) throws NoSuchMessageException {
		return this.messageSource.getMessage(code, args, getDefaultLocale());
	}

	/**
	 * 使用给定区域检索给定代码的消息。
	 * @param code 消息代码
	 * @param args 消息参数，无则为 {@code null}
	 * @param locale 查找使用的区域
	 * @return 消息文本
	 * @throws org.springframework.context.NoSuchMessageException 若未找到
	 */
	public String getMessage(String code, Object @Nullable [] args, Locale locale) throws NoSuchMessageException {
		return this.messageSource.getMessage(code, args, locale);
	}

	/**
	 * 在默认区域中解析给定 MessageSourceResolvable（例如 ObjectError 实例）。
	 * @param resolvable MessageSourceResolvable
	 * @return 消息文本
	 * @throws org.springframework.context.NoSuchMessageException 若未找到
	 */
	public String getMessage(MessageSourceResolvable resolvable) throws NoSuchMessageException {
		return this.messageSource.getMessage(resolvable, getDefaultLocale());
	}

	/**
	 * 在指定区域中解析给定 MessageSourceResolvable（例如 ObjectError 实例）。
	 * @param resolvable MessageSourceResolvable
	 * @param locale 查找使用的区域
	 * @return 消息文本
	 * @throws org.springframework.context.NoSuchMessageException 若未找到
	 */
	public String getMessage(MessageSourceResolvable resolvable, Locale locale) throws NoSuchMessageException {
		return this.messageSource.getMessage(resolvable, locale);
	}

}
