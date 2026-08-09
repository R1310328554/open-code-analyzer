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

import org.springframework.util.StringUtils;

/**
 * Boolean/boolean 属性编辑器。
 *
 * <p>本类并非用作系统级 PropertyEditor，而是作为自定义控制器代码中的
 * 区域特定布尔编辑器，用于将 UI 提交的布尔字符串解析为 Bean 的布尔属性，
 * 并在 UI 表单中回显。
 *
 * <p>在 Web MVC 代码中，通常通过 {@code binder.registerCustomEditor} 注册本编辑器。
 *
 * @author Juergen Hoeller
 * @since 10.06.2003
 * @see org.springframework.validation.DataBinder#registerCustomEditor
 */
public class CustomBooleanEditor extends PropertyEditorSupport {

	/** {@code "true"} 的值。 */
	public static final String VALUE_TRUE = "true";

	/** {@code "false"} 的值。 */
	public static final String VALUE_FALSE = "false";

	/** {@code "on"} 的值。 */
	public static final String VALUE_ON = "on";

	/** {@code "off"} 的值。 */
	public static final String VALUE_OFF = "off";

	/** {@code "yes"} 的值。 */
	public static final String VALUE_YES = "yes";

	/** {@code "no"} 的值。 */
	public static final String VALUE_NO = "no";

	/** {@code "1"} 的值。 */
	public static final String VALUE_1 = "1";

	/** {@code "0"} 的值。 */
	public static final String VALUE_0 = "0";

	/** 自定义的"真"字符串表示；{@code null} 时使用内置默认值。 */
	private final @Nullable String trueString;

	/** 自定义的"假"字符串表示；{@code null} 时使用内置默认值。 */
	private final @Nullable String falseString;

	/** 是否允许空字符串解析为 {@code null}。 */
	private final boolean allowEmpty;


	/**
	 * 创建新的 CustomBooleanEditor 实例，默认识别
	 * "true"/"on"/"yes" 和 "false"/"off"/"no" 字符串值。
	 * <p>{@code allowEmpty} 指定解析时是否允许空字符串，
	 * 即是否将其解释为 {@code null} 值；否则将抛出 IllegalArgumentException。
	 * @param allowEmpty 是否允许空字符串
	 */
	public CustomBooleanEditor(boolean allowEmpty) {
		this(null, null, allowEmpty);
	}

	/**
	 * 创建新的 CustomBooleanEditor 实例，可配置真/假的字符串表示。
	 * <p>{@code allowEmpty} 指定解析时是否允许空字符串，
	 * 即是否将其解释为 {@code null} 值；否则将抛出 IllegalArgumentException。
	 * @param trueString 表示"真"的字符串值：
	 * 例如 "true"（VALUE_TRUE）、"on"（VALUE_ON）、
	 * "yes"（VALUE_YES）或自定义值
	 * @param falseString 表示"假"的字符串值：
	 * 例如 "false"（VALUE_FALSE）、"off"（VALUE_OFF）、
	 * "no"（VALUE_NO）或自定义值
	 * @param allowEmpty 是否允许空字符串
	 * @see #VALUE_TRUE
	 * @see #VALUE_FALSE
	 * @see #VALUE_ON
	 * @see #VALUE_OFF
	 * @see #VALUE_YES
	 * @see #VALUE_NO
	 */
	public CustomBooleanEditor(@Nullable String trueString, @Nullable String falseString, boolean allowEmpty) {
		this.trueString = trueString;
		this.falseString = falseString;
		this.allowEmpty = allowEmpty;
	}


	/**
	 * 将文本解析为布尔值。
	 * <p>依次匹配自定义真/假字符串，或内置的 true/on/yes/1 与 false/off/no/0。
	 */
	@Override
	public void setAsText(@Nullable String text) throws IllegalArgumentException {
		String input = (text != null ? text.trim() : null);
		if (this.allowEmpty && !StringUtils.hasLength(input)) {
			// 将空字符串视为 null 值
			setValue(null);
		}
		else if (this.trueString != null && this.trueString.equalsIgnoreCase(input)) {
			setValue(Boolean.TRUE);
		}
		else if (this.falseString != null && this.falseString.equalsIgnoreCase(input)) {
			setValue(Boolean.FALSE);
		}
		else if (this.trueString == null &&
				(VALUE_TRUE.equalsIgnoreCase(input) || VALUE_ON.equalsIgnoreCase(input) ||
						VALUE_YES.equalsIgnoreCase(input) || VALUE_1.equals(input))) {
			setValue(Boolean.TRUE);
		}
		else if (this.falseString == null &&
				(VALUE_FALSE.equalsIgnoreCase(input) || VALUE_OFF.equalsIgnoreCase(input) ||
						VALUE_NO.equalsIgnoreCase(input) || VALUE_0.equals(input))) {
			setValue(Boolean.FALSE);
		}
		else {
			throw new IllegalArgumentException("Invalid boolean value [" + text + "]");
		}
	}

	/**
	 * 将布尔值格式化为字符串表示。
	 */
	@Override
	public String getAsText() {
		if (Boolean.TRUE.equals(getValue())) {
			return (this.trueString != null ? this.trueString : VALUE_TRUE);
		}
		else if (Boolean.FALSE.equals(getValue())) {
			return (this.falseString != null ? this.falseString : VALUE_FALSE);
		}
		else {
			return "";
		}
	}

}
