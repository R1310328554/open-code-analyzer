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

package org.springframework.context;

import java.util.Locale;

import org.jspecify.annotations.Nullable;

/**
 * 解析消息的策略接口，支持消息的参数化与国际化。
 *
 * <p>Spring 在生产环境中提供两个开箱即用的实现：
 * <ul>
 * <li>{@link org.springframework.context.support.ResourceBundleMessageSource}：
 * 基于标准 {@link java.util.ResourceBundle}，共享其限制。
 * <li>{@link org.springframework.context.support.ReloadableResourceBundleMessageSource}：
 * 高度可配置，尤其支持重新加载消息定义。
 * </ul>
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see org.springframework.context.support.ResourceBundleMessageSource
 * @see org.springframework.context.support.ReloadableResourceBundleMessageSource
 */
public interface MessageSource {

	/**
	 * 尝试解析消息；若未找到则返回默认消息。
	 * @param code the message code to look up, for example, 'calculator.noRateSet'.
	 * MessageSource users are encouraged to base message names on qualified class
	 * or package names, avoiding potential conflicts and ensuring maximum clarity.
	 * @param args an array of arguments that will be filled in for params within
	 * the message (params look like "{0}", "{1,date}", "{2,time}" within a message),
	 * or {@code null} if none
	 * @param defaultMessage a default message to return if the lookup fails
	 * @param locale the locale in which to do the lookup
	 * @return the resolved message if the lookup was successful, otherwise
	 * the default message passed as a parameter (which may be {@code null})
	 * @see #getMessage(MessageSourceResolvable, Locale)
	 * @see java.text.MessageFormat
	 */
	@Nullable String getMessage(String code, Object @Nullable [] args, @Nullable String defaultMessage, @Nullable Locale locale);

	/**
	 * 尝试解析消息；若找不到消息则视为错误。
	 * @param code the message code to look up, for example, 'calculator.noRateSet'.
	 * MessageSource users are encouraged to base message names on qualified class
	 * or package names, avoiding potential conflicts and ensuring maximum clarity.
	 * @param args an array of arguments that will be filled in for params within
	 * the message (params look like "{0}", "{1,date}", "{2,time}" within a message),
	 * or {@code null} if none
	 * @param locale the locale in which to do the lookup
	 * @return the resolved message (never {@code null})
	 * @throws NoSuchMessageException if no corresponding message was found
	 * @see #getMessage(MessageSourceResolvable, Locale)
	 * @see java.text.MessageFormat
	 */
	String getMessage(String code, Object @Nullable [] args, @Nullable Locale locale) throws NoSuchMessageException;

	/**
	 * 使用传入的 {@code MessageSourceResolvable} 参数中包含的所有属性来尝试解析消息。
	 * <p>注意：本方法必须抛出 {@code NoSuchMessageException}，因为在调用时
	 * 无法确定 resolvable 的 {@code defaultMessage} 属性是否为 {@code null}。
	 * @param resolvable the value object storing attributes required to resolve a message
	 * (may include a default message)
	 * @param locale the locale in which to do the lookup
	 * @return the resolved message (never {@code null} since even a
	 * {@code MessageSourceResolvable}-provided default message needs to be non-null)
	 * @throws NoSuchMessageException if no corresponding message was found
	 * (and no default message was provided by the {@code MessageSourceResolvable})
	 * @see MessageSourceResolvable#getCodes()
	 * @see MessageSourceResolvable#getArguments()
	 * @see MessageSourceResolvable#getDefaultMessage()
	 * @see java.text.MessageFormat
	 */
	String getMessage(MessageSourceResolvable resolvable, @Nullable Locale locale) throws NoSuchMessageException;

}
