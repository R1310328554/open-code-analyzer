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
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

import org.jspecify.annotations.Nullable;

import org.springframework.util.StringUtils;

/**
 * {@code java.util.Date} 属性编辑器，支持自定义 {@code java.text.DateFormat}。
 *
 * <p>本类并非用作系统级 PropertyEditor，而是作为自定义控制器代码中的
 * 区域特定日期编辑器，用于将用户输入的日期字符串解析为 Bean 的 Date 属性，
 * 并在 UI 表单中回显。
 *
 * <p>在 Web MVC 代码中，通常通过 {@code binder.registerCustomEditor} 注册。
 *
 * @author Juergen Hoeller
 * @since 28.04.2003
 * @see java.util.Date
 * @see java.text.DateFormat
 * @see org.springframework.validation.DataBinder#registerCustomEditor
 */
public class CustomDateEditor extends PropertyEditorSupport {

	/** 用于解析与格式化的 DateFormat。 */
	private final DateFormat dateFormat;

	/** 是否允许空字符串解析为 {@code null}。 */
	private final boolean allowEmpty;

	/** 日期字符串的精确期望长度；{@code -1} 表示不校验。 */
	private final int exactDateLength;


	/**
	 * 创建新的 CustomDateEditor 实例，使用给定 DateFormat 进行解析与格式化。
	 * <p>{@code allowEmpty} 指定解析时是否允许空字符串，
	 * 即是否将其解释为 {@code null} 值；否则将抛出 IllegalArgumentException。
	 * @param dateFormat 用于解析与格式化的 DateFormat
	 * @param allowEmpty 是否允许空字符串
	 */
	public CustomDateEditor(DateFormat dateFormat, boolean allowEmpty) {
		this.dateFormat = dateFormat;
		this.allowEmpty = allowEmpty;
		this.exactDateLength = -1;
	}

	/**
	 * 创建新的 CustomDateEditor 实例，使用给定 DateFormat 进行解析与格式化。
	 * <p>{@code allowEmpty} 指定解析时是否允许空字符串，
	 * 即是否将其解释为 {@code null} 值；否则将抛出 IllegalArgumentException。
	 * <p>{@code exactDateLength} 指定若字符串长度不完全匹配则抛出 IllegalArgumentException。
	 * 这很有用，因为 SimpleDateFormat 不会严格解析年份部分，
	 * 即使设置了 {@code setLenient(false)}。未指定时，"01/01/05" 会被解析为 "01/01/0005"。
	 * 但即使指定了 exactDateLength，日/月部分的前导零仍可能允许更短的年份，
	 * 因此请将其视为更接近预期日期格式的额外断言。
	 * @param dateFormat 用于解析与格式化的 DateFormat
	 * @param allowEmpty 是否允许空字符串
	 * @param exactDateLength 日期字符串的精确期望长度
	 */
	public CustomDateEditor(DateFormat dateFormat, boolean allowEmpty, int exactDateLength) {
		this.dateFormat = dateFormat;
		this.allowEmpty = allowEmpty;
		this.exactDateLength = exactDateLength;
	}


	/**
	 * 使用指定 DateFormat 从给定文本解析 Date。
	 */
	@Override
	public void setAsText(@Nullable String text) throws IllegalArgumentException {
		if (this.allowEmpty && !StringUtils.hasText(text)) {
			// 将空字符串视为 null 值
			setValue(null);
		}
		else if (text != null && this.exactDateLength >= 0 && text.length() != this.exactDateLength) {
			throw new IllegalArgumentException(
					"Could not parse date: it is not exactly" + this.exactDateLength + "characters long");
		}
		else {
			try {
				setValue(this.dateFormat.parse(text));
			}
			catch (ParseException ex) {
				throw new IllegalArgumentException("Could not parse date: " + ex.getMessage(), ex);
			}
		}
	}

	/**
	 * 使用指定 DateFormat 将 Date 格式化为字符串。
	 */
	@Override
	public String getAsText() {
		Date value = (Date) getValue();
		return (value != null ? this.dateFormat.format(value) : "");
	}

}
