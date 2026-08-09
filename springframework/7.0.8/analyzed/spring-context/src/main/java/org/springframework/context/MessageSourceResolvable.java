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

import org.jspecify.annotations.Nullable;

/**
 * 适合在 {@link MessageSource} 中进行消息解析的对象接口。
 *
 * <p>Spring 自身的校验错误类实现了此接口。
 *
 * @author Juergen Hoeller
 * @see MessageSource#getMessage(MessageSourceResolvable, java.util.Locale)
 * @see org.springframework.validation.ObjectError
 * @see org.springframework.validation.FieldError
 */
@FunctionalInterface
public interface MessageSourceResolvable {

	/**
	 * 返回用于解析此消息的代码数组，按尝试顺序排列；最后一个代码即为默认代码。
	 * @return a String array of codes which are associated with this message
	 */
	String @Nullable [] getCodes();

	/**
	 * 返回用于解析此消息的参数数组。
	 * <p>默认实现直接返回 {@code null}。
	 * @return an array of objects to be used as parameters to replace
	 * placeholders within the message text
	 * @see java.text.MessageFormat
	 */
	default Object @Nullable [] getArguments() {
		return null;
	}

	/**
	 * 返回用于解析此消息的默认消息文本。
	 * <p>默认实现直接返回 {@code null}。
	 * 注意，默认消息可能与主消息代码（{@link #getCodes()}）相同，
	 * 这实际上对该消息强制启用了
	 * {@link org.springframework.context.support.AbstractMessageSource#setUseCodeAsDefaultMessage}。
	 * @return the default message, or {@code null} if no default
	 */
	default @Nullable String getDefaultMessage() {
		return null;
	}

}
