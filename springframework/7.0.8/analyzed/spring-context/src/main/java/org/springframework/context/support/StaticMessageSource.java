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

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import org.springframework.util.Assert;

/**
 * {@link org.springframework.context.MessageSource} 的简单实现，支持以编程方式注册消息。
 * 支持基本的国际化（i18n）能力。
 *
 * <p>主要用于测试场景，不建议在生产系统中使用。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
public class StaticMessageSource extends AbstractMessageSource {

	/** 消息码 →（区域 → 消息持有者）的静态映射表。 */
	private final Map<String, Map<Locale, MessageHolder>> messageMap = new HashMap<>();


	/** 按消息码与区域查找不带占位符的原始消息文本。 */
	@Override
	protected @Nullable String resolveCodeWithoutArguments(String code, Locale locale) {
		Map<Locale, MessageHolder> localeMap = this.messageMap.get(code);
		if (localeMap == null) {
			return null;
		}
		MessageHolder holder = localeMap.get(locale);
		if (holder == null) {
			return null;
		}
		return holder.getMessage();
	}

	/** 按消息码与区域查找可格式化的 {@link MessageFormat}。 */
	@Override
	protected @Nullable MessageFormat resolveCode(String code, Locale locale) {
		Map<Locale, MessageHolder> localeMap = this.messageMap.get(code);
		if (localeMap == null) {
			return null;
		}
		MessageHolder holder = localeMap.get(locale);
		if (holder == null) {
			return null;
		}
		return holder.getMessageFormat();
	}

	/**
	 * 将给定消息与消息码关联注册。
	 * @param code 查找码
	 * @param locale 消息所属的区域
	 * @param msg 与该查找码关联的消息文本
	 */
	public void addMessage(String code, Locale locale, String msg) {
		Assert.notNull(code, "Code must not be null");
		Assert.notNull(locale, "Locale must not be null");
		Assert.notNull(msg, "Message must not be null");
		this.messageMap.computeIfAbsent(code, key -> new HashMap<>(4)).put(locale, new MessageHolder(msg, locale));
		if (logger.isDebugEnabled()) {
			logger.debug("Added message [" + msg + "] for code [" + code + "] and Locale [" + locale + "]");
		}
	}

	/**
	 * 批量注册消息：以键为消息码、值为消息文本。
	 * @param messages 待注册的消息映射（键为消息码，值为消息文本）
	 * @param locale 消息所属的区域
	 */
	public void addMessages(Map<String, String> messages, Locale locale) {
		Assert.notNull(messages, "Messages Map must not be null");
		messages.forEach((code, msg) -> addMessage(code, locale, msg));
	}


	@Override
	public String toString() {
		return getClass().getName() + ": " + this.messageMap;
	}


	/** 缓存单条消息及其对应 {@link MessageFormat} 的内部持有者。 */
	private class MessageHolder {

		/** 原始消息文本。 */
		private final String message;

		/** 消息所属区域。 */
		private final Locale locale;

		/** 懒加载缓存的 {@link MessageFormat}。 */
		private volatile @Nullable MessageFormat cachedFormat;

		public MessageHolder(String message, Locale locale) {
			this.message = message;
			this.locale = locale;
		}

		/** 返回原始消息文本。 */
		public String getMessage() {
			return this.message;
		}

		/** 返回（必要时创建并缓存的）{@link MessageFormat}。 */
		public MessageFormat getMessageFormat() {
			MessageFormat messageFormat = this.cachedFormat;
			if (messageFormat == null) {
				messageFormat = createMessageFormat(this.message, this.locale);
				this.cachedFormat = messageFormat;
			}
			return messageFormat;
		}

		@Override
		public String toString() {
			return this.message;
		}
	}

}
