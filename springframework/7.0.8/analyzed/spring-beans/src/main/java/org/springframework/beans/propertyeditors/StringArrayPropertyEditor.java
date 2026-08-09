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

import org.jspecify.annotations.Nullable;

import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * String 数组的自定义 {@link java.beans.PropertyEditor}。
 *
 * <p>字符串须为 CSV 格式，分隔符可自定义。
 * 默认情况下，解析结果中的各元素会去除首尾空白。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Dave Syer
 * @see org.springframework.util.StringUtils#delimitedListToStringArray
 * @see org.springframework.util.StringUtils#arrayToDelimitedString
 */
public class StringArrayPropertyEditor extends PropertyEditorSupport {

	/**
	 * 拆分 String 的默认分隔符：逗号（{@code ,}）。
	 */
	public static final String DEFAULT_SEPARATOR = ",";


	/** 用于拆分字符串的分隔符。 */
	private final String separator;

	/** 除修剪外还要删除的字符集；可为 null。 */
	private final @Nullable String charsToDelete;

	/** 空数组是否转换为 {@code null}。 */
	private final boolean emptyArrayAsNull;

	/** 是否对解析出的数组元素执行 trim。 */
	private final boolean trimValues;


	/**
	 * 使用默认分隔符（逗号）创建新的 {@code StringArrayPropertyEditor}。
	 * <p>空文本（无元素）将转换为空数组。
	 */
	public StringArrayPropertyEditor() {
		this(DEFAULT_SEPARATOR, null, false);
	}

	/**
	 * 使用给定分隔符创建新的 {@code StringArrayPropertyEditor}。
	 * <p>空文本（无元素）将转换为空数组。
	 * @param separator 用于拆分 {@link String} 的分隔符
	 */
	public StringArrayPropertyEditor(String separator) {
		this(separator, null, false);
	}

	/**
	 * 使用给定分隔符创建新的 {@code StringArrayPropertyEditor}。
	 * @param separator 用于拆分 {@link String} 的分隔符
	 * @param emptyArrayAsNull 空 String 数组是否转换为 {@code null}
	 */
	public StringArrayPropertyEditor(String separator, boolean emptyArrayAsNull) {
		this(separator, null, emptyArrayAsNull);
	}

	/**
	 * 使用给定分隔符创建新的 {@code StringArrayPropertyEditor}。
	 * @param separator 用于拆分 {@link String} 的分隔符
	 * @param emptyArrayAsNull 空 String 数组是否转换为 {@code null}
	 * @param trimValues 是否对解析出的数组元素去除首尾空白（默认为 {@code true}）
	 */
	public StringArrayPropertyEditor(String separator, boolean emptyArrayAsNull, boolean trimValues) {
		this(separator, null, emptyArrayAsNull, trimValues);
	}

	/**
	 * 使用给定分隔符创建新的 {@code StringArrayPropertyEditor}。
	 * @param separator 用于拆分 {@link String} 的分隔符
	 * @param charsToDelete 除修剪外要删除的字符集；可用于删除多余换行，
	 * 例如 {@code \r\n\f} 会删除所有换行与换页符
	 * @param emptyArrayAsNull 空 String 数组是否转换为 {@code null}
	 */
	public StringArrayPropertyEditor(String separator, @Nullable String charsToDelete, boolean emptyArrayAsNull) {
		this(separator, charsToDelete, emptyArrayAsNull, true);
	}

	/**
	 * 使用给定分隔符创建新的 {@code StringArrayPropertyEditor}。
	 * @param separator 用于拆分 {@link String} 的分隔符
	 * @param charsToDelete 除修剪外要删除的字符集；可用于删除多余换行，
	 * 例如 {@code \r\n\f} 会删除所有换行与换页符
	 * @param emptyArrayAsNull 空 String 数组是否转换为 {@code null}
	 * @param trimValues 是否对解析出的数组元素去除首尾空白（默认为 {@code true}）
	 */
	public StringArrayPropertyEditor(
			String separator, @Nullable String charsToDelete, boolean emptyArrayAsNull, boolean trimValues) {

		this.separator = separator;
		this.charsToDelete = charsToDelete;
		this.emptyArrayAsNull = emptyArrayAsNull;
		this.trimValues = trimValues;
	}

	@Override
	public void setAsText(String text) throws IllegalArgumentException {
		@Nullable String[] array = StringUtils.delimitedListToStringArray(text, this.separator, this.charsToDelete);
		if (this.emptyArrayAsNull && array.length == 0) {
			setValue(null);
		}
		else {
			if (this.trimValues) {
				array = StringUtils.trimArrayElements(array);
			}
			setValue(array);
		}
	}

	@Override
	public String getAsText() {
		return StringUtils.arrayToDelimitedString(ObjectUtils.toObjectArray(getValue()), this.separator);
	}

}
