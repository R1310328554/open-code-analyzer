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
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;

import org.springframework.util.ObjectUtils;

/**
 * 消息源实现类的基类，提供 {@link java.text.MessageFormat} 处理等支持基础设施，
 * 但不实现 {@link org.springframework.context.MessageSource} 中定义的具体方法。
 *
 * <p>{@link AbstractMessageSource} 派生自本类，提供具体的 {@code getMessage} 实现，
 * 委托给用于消息代码解析的中心模板方法。
 *
 * @author Juergen Hoeller
 * @since 2.5.5
 */
public abstract class MessageSourceSupport {

	/** 表示无效 MessageFormat 的占位实例。 */
	private static final MessageFormat INVALID_MESSAGE_FORMAT = new MessageFormat("");

	/** 子类可用的日志记录器。 */
	protected final Log logger = LogFactory.getLog(getClass());

	/** 是否对无参数消息也强制使用 MessageFormat 规则。 */
	private boolean alwaysUseMessageFormat = false;

	/**
	 * 缓存已生成的 MessageFormat（按消息文本分组）。
	 * 用于传入的默认消息；已解析代码的 MessageFormat 在子类中按代码单独缓存。
	 */
	private final Map<String, Map<Locale, MessageFormat>> messageFormatsPerMessage = new ConcurrentHashMap<>();


	/**
	 * 设置是否始终应用 {@code MessageFormat} 规则，即使消息无参数也解析。
	 * <p>默认为 {@code false}：无参数消息默认原样返回，不经过 {@code MessageFormat} 解析。
	 * 设为 {@code true} 则对所有消息强制使用 {@code MessageFormat}，
	 * 要求所有消息文本均按 {@code MessageFormat} 转义规则编写。
	 * <p>例如，{@code MessageFormat} 要求单引号转义为两个相邻单引号（{@code "''"}）。
	 * 若所有消息文本均如此转义（即使无参数占位符），需将本标志设为 {@code true}。
	 * 否则，仅含实际参数的消息文本才应按 {@code MessageFormat} 转义。
	 * @see java.text.MessageFormat
	 */
	public void setAlwaysUseMessageFormat(boolean alwaysUseMessageFormat) {
		this.alwaysUseMessageFormat = alwaysUseMessageFormat;
	}

	/**
	 * 返回是否始终应用 {@code MessageFormat} 规则，即使消息无参数也解析。
	 */
	protected boolean isAlwaysUseMessageFormat() {
		return this.alwaysUseMessageFormat;
	}


	/**
	 * 渲染给定的默认消息字符串。默认消息由调用方原样传入，
	 * 可渲染为展示给用户的完整格式化默认消息。
	 * <p>默认实现将字符串传给 {@code formatMessage}，解析其中的参数占位符。
	 * 子类可覆盖以插入自定义默认消息处理。
	 * @param defaultMessage 传入的默认消息字符串
	 * @param args 填充消息中占位符的参数数组，无则为 {@code null}
	 * @param locale 格式化使用的区域
	 * @return 渲染后的默认消息（参数已解析）
	 * @see #formatMessage(String, Object[], java.util.Locale)
	 */
	protected String renderDefaultMessage(String defaultMessage, Object @Nullable [] args, @Nullable Locale locale) {
		return formatMessage(defaultMessage, args, locale);
	}

	/**
	 * 使用缓存的 MessageFormat 格式化给定消息字符串。
	 * 默认对传入的默认消息调用，以解析其中的参数占位符。
	 * @param msg 要格式化的消息
	 * @param args 填充消息中占位符的参数数组，无则为 {@code null}
	 * @param locale 格式化使用的区域
	 * @return 格式化后的消息（参数已解析）
	 */
	protected String formatMessage(String msg, Object @Nullable [] args, @Nullable Locale locale) {
		if (!isAlwaysUseMessageFormat() && ObjectUtils.isEmpty(args)) {
			return msg;
		}
		Map<Locale, MessageFormat> messageFormatsPerLocale = this.messageFormatsPerMessage
				.computeIfAbsent(msg, key -> new ConcurrentHashMap<>());
		MessageFormat messageFormat = messageFormatsPerLocale.computeIfAbsent(locale, key -> {
			try {
				return createMessageFormat(msg, locale);
			}
			catch (IllegalArgumentException ex) {
				// 无效消息格式——可能并非用于格式化，而是无参数的消息结构
				if (isAlwaysUseMessageFormat()) {
					throw ex;
				}
				// 未强制格式化时静默使用原始消息
				return INVALID_MESSAGE_FORMAT;
			}
		});
		if (messageFormat == INVALID_MESSAGE_FORMAT) {
			return msg;
		}
		synchronized (messageFormat) {
			return messageFormat.format(resolveArguments(args, locale));
		}
	}

	/**
	 * 为给定消息与区域创建 {@code MessageFormat}。
	 * @param msg 要创建 {@code MessageFormat} 的消息
	 * @param locale 要创建 {@code MessageFormat} 的区域
	 * @return {@code MessageFormat} 实例
	 */
	protected MessageFormat createMessageFormat(String msg, @Nullable Locale locale) {
		return new MessageFormat(msg, locale);
	}

	/**
	 * 解析参数对象的模板方法。
	 * <p>默认实现原样返回给定参数数组。子类可覆盖以解析特殊参数类型。
	 * @param args 原始参数数组
	 * @param locale 解析所依据的区域
	 * @return 解析后的参数数组
	 */
	protected Object[] resolveArguments(Object @Nullable [] args, @Nullable Locale locale) {
		return (args != null ? args : new Object[0]);
	}

}
