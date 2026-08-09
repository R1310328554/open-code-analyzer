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

package org.springframework.beans.propertyeditors;

import java.beans.PropertyEditorSupport;
import java.util.HexFormat;

import org.jspecify.annotations.Nullable;

import org.springframework.util.StringUtils;

/**
 * {@link Character} 属性编辑器，用于从字符串填充 {@code Character} 或 {@code char} 类型属性。
 *
 * <p>注意：JDK 未为 {@code char} 提供默认的
 * {@link java.beans.PropertyEditor 属性编辑器}！
 * {@link org.springframework.beans.BeanWrapperImpl} 会默认注册本编辑器。
 *
 * <p>同时支持 Unicode 字符序列转换，例如 {@code u0041}（'A'）。
 *
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @author Rick Evans
 * @since 1.2
 * @see Character
 * @see org.springframework.beans.BeanWrapperImpl
 */
public class CharacterEditor extends PropertyEditorSupport {

	/** 标识字符串为 Unicode 字符序列的前缀。 */
	private static final String UNICODE_PREFIX = "\\u";

	/** Unicode 字符序列的长度。 */
	private static final int UNICODE_LENGTH = 6;

	/** 是否允许空字符串解析为 {@code null}。 */
	private final boolean allowEmpty;


	/**
	 * 创建新的 CharacterEditor 实例。
	 * <p>{@code allowEmpty} 控制解析时是否允许空字符串，
	 * 即调用 {@link #setAsText(String)} 转换文本时是否将其解释为 {@code null} 值。
	 * 若为 {@code false}，遇到空字符串时将抛出 {@link IllegalArgumentException}。
	 * @param allowEmpty 是否允许空字符串
	 */
	public CharacterEditor(boolean allowEmpty) {
		this.allowEmpty = allowEmpty;
	}


	/**
	 * 将文本解析为单个字符值。
	 * <p>支持空字符串（若允许）、单字符文本及 {@code \uXXXX} 形式的 Unicode 序列。
	 */
	@Override
	public void setAsText(@Nullable String text) throws IllegalArgumentException {
		if (this.allowEmpty && !StringUtils.hasLength(text)) {
			// 将空字符串视为 null 值
			setValue(null);
		}
		else if (text == null) {
			throw new IllegalArgumentException("null String cannot be converted to char type");
		}
		else if (isUnicodeCharacterSequence(text)) {
			setAsUnicode(text);
		}
		else if (text.length() == 1) {
			setValue(text.charAt(0));
		}
		else {
			throw new IllegalArgumentException("String [" + text + "] with length " +
					text.length() + " cannot be converted to char type: neither Unicode nor single character");
		}
	}

	/**
	 * 将字符值格式化为字符串。
	 */
	@Override
	public String getAsText() {
		Object value = getValue();
		return (value != null ? value.toString() : "");
	}

	/** 判断给定序列是否为 {@code \uXXXX} 形式的 Unicode 字符序列。 */
	private static boolean isUnicodeCharacterSequence(String sequence) {
		return (sequence.startsWith(UNICODE_PREFIX) && sequence.length() == UNICODE_LENGTH);
	}

	/** 将 Unicode 字符序列解析为字符并设置值。 */
	private void setAsUnicode(String text) {
		int code = HexFormat.fromHexDigits(text, UNICODE_PREFIX.length(), text.length());
		setValue((char) code);
	}

}
