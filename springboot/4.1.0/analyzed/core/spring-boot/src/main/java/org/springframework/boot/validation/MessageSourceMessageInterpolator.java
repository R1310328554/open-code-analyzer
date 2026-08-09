/*
 * Copyright 2012-present the original author or authors.
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

package org.springframework.boot.validation;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

import jakarta.validation.MessageInterpolator;
import org.jspecify.annotations.Nullable;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

/**
 * 通过 {@link MessageSource} 解析消息参数，再使用底层 {@link MessageInterpolator} 插值。
 * 支持嵌套 {@code {}} 参数替换与转义，并检测循环引用。
 *
 * @author Dmytro Nosan
 * @author Scott Frederick
 */
class MessageSourceMessageInterpolator implements MessageInterpolator {

	private static final String DEFAULT_MESSAGE = MessageSourceMessageInterpolator.class.getName();

	private static final char PREFIX = '{';

	private static final char SUFFIX = '}';

	private static final char ESCAPE = '\\';

	private final MessageSource messageSource;

	private final MessageInterpolator messageInterpolator;

	MessageSourceMessageInterpolator(MessageSource messageSource, MessageInterpolator messageInterpolator) {
		this.messageSource = messageSource;
		this.messageInterpolator = messageInterpolator;
	}

	@Override
	public String interpolate(String messageTemplate, Context context) {
		return interpolate(messageTemplate, context, LocaleContextHolder.getLocale());
	}

	@Override
	public String interpolate(String messageTemplate, Context context, Locale locale) {
		String message = replaceParameters(messageTemplate, locale);
		return this.messageInterpolator.interpolate(message, context, locale);
	}

	/**
	 * 递归替换全部消息参数。
	 * <p>
	 * 参数前缀 <code>&#123;</code> 与后缀 <code>&#125;</code> 可用 {@code \} 转义，
	 * 例如 <code>\&#123;escaped\&#125;</code>。
	 *
	 * @param message the message containing the parameters to be replaced 含待替换参数的消息
	 * @param locale the locale to use when resolving replacements 解析替换时使用的区域
	 * @return the message with parameters replaced 参数已替换的消息
	 */
	private String replaceParameters(String message, Locale locale) {
		return replaceParameters(message, locale, new LinkedHashSet<>(4));
	}

	private String replaceParameters(String message, Locale locale, Set<String> visitedParameters) {
		StringBuilder buf = new StringBuilder(message);
		int parentheses = 0;
		int startIndex = -1;
		int endIndex = -1;
		for (int i = 0; i < buf.length(); i++) {
			if (buf.charAt(i) == ESCAPE) {
				i++;
			}
			else if (buf.charAt(i) == PREFIX) {
				if (startIndex == -1) {
					startIndex = i;
				}
				parentheses++;
			}
			else if (buf.charAt(i) == SUFFIX) {
				if (parentheses > 0) {
					parentheses--;
				}
				endIndex = i;
			}
			if (parentheses == 0 && startIndex < endIndex) {
				String parameter = buf.substring(startIndex + 1, endIndex);
				if (!visitedParameters.add(parameter)) {
					throw new IllegalArgumentException("Circular reference '{" + String.join(" -> ", visitedParameters)
							+ " -> " + parameter + "}'");
				}
				String value = replaceParameter(parameter, locale, visitedParameters);
				if (value != null) {
					buf.replace(startIndex, endIndex + 1, value);
					i = startIndex + value.length() - 1;
				}
				visitedParameters.remove(parameter);
				startIndex = -1;
				endIndex = -1;
			}
		}
		return buf.toString();
	}

	private @Nullable String replaceParameter(String parameter, Locale locale, Set<String> visitedParameters) {
		parameter = replaceParameters(parameter, locale, visitedParameters);
		String value = this.messageSource.getMessage(parameter, null, DEFAULT_MESSAGE, locale);
		if (value == null || value.equals(DEFAULT_MESSAGE)) {
			return null;
		}
		return replaceParameters(value, locale, visitedParameters);
	}

}
