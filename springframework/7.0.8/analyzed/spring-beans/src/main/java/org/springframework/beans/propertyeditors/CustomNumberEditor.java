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
import java.text.NumberFormat;

import org.jspecify.annotations.Nullable;

import org.springframework.util.NumberUtils;
import org.springframework.util.StringUtils;

/**
 * 任意 Number 子类（如 Short、Integer、Long、BigInteger、Float、Double、BigDecimal）
 * 的属性编辑器。可使用给定 NumberFormat 进行（区域特定的）解析与格式化，
 * 或改用默认的 {@code decode} / {@code valueOf} / {@code toString} 方法。
 *
 * <p>本类并非用作系统级 PropertyEditor，而是作为自定义控制器代码中的
 * 区域特定数字编辑器，用于将用户输入的数字字符串解析为 Bean 的 Number 属性，
 * 并在 UI 表单中回显。
 *
 * <p>在 Web MVC 代码中，通常通过 {@code binder.registerCustomEditor} 注册。
 *
 * @author Juergen Hoeller
 * @since 06.06.2003
 * @see Number
 * @see java.text.NumberFormat
 * @see org.springframework.validation.DataBinder#registerCustomEditor
 */
public class CustomNumberEditor extends PropertyEditorSupport {

	/** 要生成的 Number 子类类型。 */
	private final Class<? extends Number> numberClass;

	/** 用于解析与格式化的 NumberFormat；{@code null} 时使用默认方法。 */
	private final @Nullable NumberFormat numberFormat;

	/** 是否允许空字符串解析为 {@code null}。 */
	private final boolean allowEmpty;


	/**
	 * 创建新的 CustomNumberEditor 实例，使用默认的 {@code valueOf} 方法解析、
	 * {@code toString} 方法格式化。
	 * <p>{@code allowEmpty} 指定解析时是否允许空字符串，
	 * 即是否将其解释为 {@code null} 值；否则将抛出 IllegalArgumentException。
	 * @param numberClass 要生成的 Number 子类
	 * @param allowEmpty 是否允许空字符串
	 * @throws IllegalArgumentException 若指定的 numberClass 无效
	 * @see org.springframework.util.NumberUtils#parseNumber(String, Class)
	 * @see Integer#valueOf
	 * @see Integer#toString
	 */
	public CustomNumberEditor(Class<? extends Number> numberClass, boolean allowEmpty) throws IllegalArgumentException {
		this(numberClass, null, allowEmpty);
	}

	/**
	 * 创建新的 CustomNumberEditor 实例，使用给定 NumberFormat 进行解析与格式化。
	 * <p>{@code allowEmpty} 指定解析时是否允许空字符串，
	 * 即是否将其解释为 {@code null} 值；否则将抛出 IllegalArgumentException。
	 * @param numberClass 要生成的 Number 子类
	 * @param numberFormat 用于解析与格式化的 NumberFormat
	 * @param allowEmpty 是否允许空字符串
	 * @throws IllegalArgumentException 若指定的 numberClass 无效
	 * @see org.springframework.util.NumberUtils#parseNumber(String, Class, java.text.NumberFormat)
	 * @see java.text.NumberFormat#parse
	 * @see java.text.NumberFormat#format
	 */
	public CustomNumberEditor(Class<? extends Number> numberClass,
			@Nullable NumberFormat numberFormat, boolean allowEmpty) throws IllegalArgumentException {

		if (!Number.class.isAssignableFrom(numberClass)) {
			throw new IllegalArgumentException("Property class must be a subclass of Number");
		}
		this.numberClass = numberClass;
		this.numberFormat = numberFormat;
		this.allowEmpty = allowEmpty;
	}


	/**
	 * 使用指定 NumberFormat 从给定文本解析 Number。
	 */
	@Override
	public void setAsText(String text) throws IllegalArgumentException {
		if (this.allowEmpty && !StringUtils.hasText(text)) {
			// 将空字符串视为 null 值
			setValue(null);
		}
		else if (this.numberFormat != null) {
			// 使用给定 NumberFormat 解析文本
			setValue(NumberUtils.parseNumber(text, this.numberClass, this.numberFormat));
		}
		else {
			// 使用默认 valueOf 方法解析文本
			setValue(NumberUtils.parseNumber(text, this.numberClass));
		}
	}

	/**
	 * 必要时将 Number 值强制转换为目标类。
	 */
	@Override
	public void setValue(@Nullable Object value) {
		if (value instanceof Number num) {
			super.setValue(NumberUtils.convertNumberToTargetClass(num, this.numberClass));
		}
		else {
			super.setValue(value);
		}
	}

	/**
	 * 使用指定 NumberFormat 将 Number 格式化为字符串。
	 */
	@Override
	public String getAsText() {
		Object value = getValue();
		if (value == null) {
			return "";
		}
		if (this.numberFormat != null) {
			// 使用 NumberFormat 格式化值
			return this.numberFormat.format(value);
		}
		else {
			// 使用 toString 方法格式化值
			return value.toString();
		}
	}

}
