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

package org.springframework.format.number;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import org.jspecify.annotations.Nullable;

/**
 * 使用 NumberFormat 数值样式的通用数字格式化器。
 *
 * <p>委托给 {@link java.text.NumberFormat#getInstance(Locale)}。
 * 配置 BigDecimal 解析以避免精度损失。
 * 允许配置小数数字模式。
 * {@link #parse(String, Locale)} 方法始终返回 BigDecimal。
 *
 * @author Keith Donald
 * @author Juergen Hoeller
 * @since 4.2
 * @see #setPattern
 * @see #setLenient
 */
public class NumberStyleFormatter extends AbstractNumberFormatter {

	private @Nullable String pattern;


	/**
	 * 创建不带模式的新 NumberStyleFormatter。
	 */
	public NumberStyleFormatter() {
	}

	/**
	 * 使用指定模式创建新的 NumberStyleFormatter。
	 * @param pattern 格式模式
	 * @see #setPattern
	 */
	public NumberStyleFormatter(String pattern) {
		this.pattern = pattern;
	}


	/**
	 * 指定用于格式化数值的模式。
	 * 若未指定，则使用默认的 DecimalFormat 模式。
	 * @see java.text.DecimalFormat#applyPattern(String)
	 */
	public void setPattern(String pattern) {
		this.pattern = pattern;
	}


	@Override
	public NumberFormat getNumberFormat(Locale locale) {
		NumberFormat format = NumberFormat.getInstance(locale);
		if (!(format instanceof DecimalFormat decimalFormat)) {
			if (this.pattern != null) {
				throw new IllegalStateException("Cannot support pattern for non-DecimalFormat: " + format);
			}
			return format;
		}
		decimalFormat.setParseBigDecimal(true);
		if (this.pattern != null) {
			decimalFormat.applyPattern(this.pattern);
		}
		return decimalFormat;
	}

}
