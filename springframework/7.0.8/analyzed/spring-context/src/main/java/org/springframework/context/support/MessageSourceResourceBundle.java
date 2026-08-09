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

import java.util.Enumeration;
import java.util.Locale;
import java.util.ResourceBundle;

import org.jspecify.annotations.Nullable;

import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.util.Assert;

/**
 * 辅助类，允许将 Spring {@link org.springframework.context.MessageSource}
 * 作为 {@link java.util.ResourceBundle} 访问。
 * 例如用于向 JSTL Web 视图暴露 Spring MessageSource。
 *
 * @author Juergen Hoeller
 * @since 27.02.2003
 * @see org.springframework.context.MessageSource
 * @see java.util.ResourceBundle
 * @see org.springframework.web.servlet.support.JstlUtils#exposeLocalizationContext
 */
public class MessageSourceResourceBundle extends ResourceBundle {

	/** 底层 MessageSource，用于按代码解析消息。 */
	private final MessageSource messageSource;

	/** 本 ResourceBundle 绑定的区域设置。 */
	private final Locale locale;


	/**
	 * 为给定 MessageSource 与 Locale 创建新的 MessageSourceResourceBundle。
	 * @param source 用于检索消息的 MessageSource
	 * @param locale 要检索消息的区域
	 */
	public MessageSourceResourceBundle(MessageSource source, Locale locale) {
		Assert.notNull(source, "MessageSource must not be null");
		this.messageSource = source;
		this.locale = locale;
	}

	/**
	 * 为给定 MessageSource 与 Locale 创建新的 MessageSourceResourceBundle。
	 * @param source 用于检索消息的 MessageSource
	 * @param locale 要检索消息的区域
	 * @param parent 本地未找到消息时委托的父 ResourceBundle
	 */
	public MessageSourceResourceBundle(MessageSource source, Locale locale, ResourceBundle parent) {
		this(source, locale);
		setParent(parent);
	}


	/**
	 * 在 MessageSource 中解析给定代码。
	 * 若无法解析消息则返回 {@code null}。
	 */
	@Override
	protected @Nullable Object handleGetObject(String key) {
		try {
			return this.messageSource.getMessage(key, null, this.locale);
		}
		catch (NoSuchMessageException ex) {
			return null;
		}
	}

	/**
	 * 检查目标 MessageSource 能否为给定键解析消息，
	 * 并相应处理 {@code NoSuchMessageException}。
	 * 与 JDK 1.6 中 ResourceBundle 的默认实现不同，本实现不依赖枚举消息键的能力。
	 */
	@Override
	public boolean containsKey(String key) {
		try {
			this.messageSource.getMessage(key, null, this.locale);
			return true;
		}
		catch (NoSuchMessageException ex) {
			return false;
		}
	}

	/**
	 * 抛出 {@code UnsupportedOperationException}，
	 * 因为 MessageSource 不支持枚举已定义的消息代码。
	 */
	@Override
	public Enumeration<String> getKeys() {
		throw new UnsupportedOperationException("MessageSourceResourceBundle does not support enumerating its keys");
	}

	/**
	 * 通过标准 {@code ResourceBundle.getLocale()} 方法暴露指定的 Locale，供自省使用。
	 */
	@Override
	public Locale getLocale() {
		return this.locale;
	}

}
