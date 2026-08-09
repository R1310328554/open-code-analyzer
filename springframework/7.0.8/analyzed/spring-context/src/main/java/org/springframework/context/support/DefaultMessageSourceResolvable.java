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

import java.io.Serializable;

import org.jspecify.annotations.Nullable;

import org.springframework.context.MessageSourceResolvable;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * Spring 对 {@link MessageSourceResolvable} 接口的默认实现。
 * 提供便捷方式存储通过 {@link org.springframework.context.MessageSource} 解析消息所需的全部值。
 *
 * @author Juergen Hoeller
 * @since 13.02.2004
 * @see org.springframework.context.MessageSource#getMessage(MessageSourceResolvable, java.util.Locale)
 */
@SuppressWarnings("serial")
public class DefaultMessageSourceResolvable implements MessageSourceResolvable, Serializable {

	private final String @Nullable [] codes;

	private final Object @Nullable [] arguments;

	private final @Nullable String defaultMessage;


	/**
	 * 创建新的 DefaultMessageSourceResolvable。
	 * @param code 用于解析本消息的代码
	 */
	public DefaultMessageSourceResolvable(String code) {
		this(new String[] {code}, null, null);
	}

	/**
	 * 创建新的 DefaultMessageSourceResolvable。
	 * @param codes 用于解析本消息的代码数组
	 */
	public DefaultMessageSourceResolvable(String[] codes) {
		this(codes, null, null);
	}

	/**
	 * 创建新的 DefaultMessageSourceResolvable。
	 * @param codes 用于解析本消息的代码数组
	 * @param defaultMessage 用于解析本消息的默认消息
	 */
	public DefaultMessageSourceResolvable(String[] codes, String defaultMessage) {
		this(codes, null, defaultMessage);
	}

	/**
	 * 创建新的 DefaultMessageSourceResolvable。
	 * @param codes 用于解析本消息的代码数组
	 * @param arguments 用于解析本消息的参数数组
	 */
	public DefaultMessageSourceResolvable(String[] codes, Object[] arguments) {
		this(codes, arguments, null);
	}

	/**
	 * 创建新的 DefaultMessageSourceResolvable。
	 * @param codes 用于解析本消息的代码数组
	 * @param arguments 用于解析本消息的参数数组
	 * @param defaultMessage 用于解析本消息的默认消息
	 */
	public DefaultMessageSourceResolvable(
			String @Nullable [] codes, Object @Nullable [] arguments, @Nullable String defaultMessage) {

		this.codes = codes;
		this.arguments = arguments;
		this.defaultMessage = defaultMessage;
	}

	/**
	 * 拷贝构造函数：从另一个可解析对象创建新实例。
	 * @param resolvable 要拷贝的源对象
	 */
	public DefaultMessageSourceResolvable(MessageSourceResolvable resolvable) {
		this(resolvable.getCodes(), resolvable.getArguments(), resolvable.getDefaultMessage());
	}


	/**
	 * 返回本可解析对象的默认代码，即代码数组中的最后一个。
	 */
	public @Nullable String getCode() {
		return (this.codes != null && this.codes.length > 0 ? this.codes[this.codes.length - 1] : null);
	}

	@Override
	public String @Nullable [] getCodes() {
		return this.codes;
	}

	@Override
	public Object @Nullable [] getArguments() {
		return this.arguments;
	}

	@Override
	public @Nullable String getDefaultMessage() {
		return this.defaultMessage;
	}

	/**
	 * 指示指定的默认消息是否需要渲染以替换占位符和/或进行
	 * {@link java.text.MessageFormat} 转义。
	 * @return 若默认消息可能包含参数占位符则为 {@code true}；
	 * 若确定不包含占位符或自定义转义、可直接原样暴露则为 {@code false}
	 * @since 5.1.7
	 * @see #getDefaultMessage()
	 * @see #getArguments()
	 * @see AbstractMessageSource#renderDefaultMessage
	 */
	public boolean shouldRenderDefaultMessage() {
		return true;
	}


	/**
	 * 为本 MessageSourceResolvable 构建默认的字符串表示：包含代码、参数和默认消息。
	 */
	protected final String resolvableToString() {
		StringBuilder result = new StringBuilder(64);
		result.append("codes [").append(StringUtils.arrayToDelimitedString(this.codes, ","));
		result.append("]; arguments [").append(StringUtils.arrayToDelimitedString(this.arguments, ","));
		result.append("]; default message [").append(this.defaultMessage).append(']');
		return result.toString();
	}

	/**
	 * 默认实现暴露本 MessageSourceResolvable 的属性。
	 * <p>更具体的子类应覆盖，可能通过 {@code resolvableToString()} 包含可解析内容。
	 * @see #resolvableToString()
	 */
	@Override
	public String toString() {
		return getClass().getName() + ": " + resolvableToString();
	}


	@Override
	public boolean equals(@Nullable Object other) {
		return (this == other || (other instanceof MessageSourceResolvable that &&
				ObjectUtils.nullSafeEquals(getCodes(), that.getCodes()) &&
				ObjectUtils.nullSafeEquals(getArguments(), that.getArguments()) &&
				ObjectUtils.nullSafeEquals(getDefaultMessage(), that.getDefaultMessage())));
	}

	@Override
	public int hashCode() {
		return ObjectUtils.nullSafeHash(getCode(), getArguments(), getDefaultMessage());
	}

}
