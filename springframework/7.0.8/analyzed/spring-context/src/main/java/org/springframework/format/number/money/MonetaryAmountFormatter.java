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

package org.springframework.format.number.money;

import java.util.Locale;

import javax.money.MonetaryAmount;
import javax.money.format.MonetaryAmountFormat;
import javax.money.format.MonetaryFormats;

import org.jspecify.annotations.Nullable;

import org.springframework.format.Formatter;

/**
 * 用于 JSR-354 {@link javax.money.MonetaryAmount} 值的格式化器，
 * 委托给 {@link javax.money.format.MonetaryAmountFormat#format}
 * 与 {@link javax.money.format.MonetaryAmountFormat#parse}。
 *
 * @author Juergen Hoeller
 * @since 4.2
 * @see #getMonetaryAmountFormat
 */
public class MonetaryAmountFormatter implements Formatter<MonetaryAmount> {

	private @Nullable String formatName;


	/**
	 * 创建由区域设置驱动的 MonetaryAmountFormatter。
	 */
	public MonetaryAmountFormatter() {
	}

	/**
	 * 为给定格式名称创建新的 MonetaryAmountFormatter。
	 * @param formatName 格式名称，由 JSR-354 提供者在运行时解析
	 */
	public MonetaryAmountFormatter(String formatName) {
		this.formatName = formatName;
	}


	/**
	 * 指定格式名称，由 JSR-354 提供者在运行时解析。
	 * <p>默认不指定，根据当前区域设置获取 {@link MonetaryAmountFormat}。
	 */
	public void setFormatName(String formatName) {
		this.formatName = formatName;
	}


	@Override
	public String print(MonetaryAmount object, Locale locale) {
		return getMonetaryAmountFormat(locale).format(object);
	}

	@Override
	public MonetaryAmount parse(String text, Locale locale) {
		return getMonetaryAmountFormat(locale).parse(text);
	}


	/**
	 * 为给定区域设置获取 MonetaryAmountFormat。
	 * <p>默认实现直接调用
	 * {@link javax.money.format.MonetaryFormats#getAmountFormat}，
	 * 传入已配置的格式名称或给定区域设置。
	 * @param locale 当前区域设置
	 * @return MonetaryAmountFormat（永不为 {@code null}）
	 * @see #setFormatName
	 */
	protected MonetaryAmountFormat getMonetaryAmountFormat(Locale locale) {
		if (this.formatName != null) {
			return MonetaryFormats.getAmountFormat(this.formatName);
		}
		else {
			return MonetaryFormats.getAmountFormat(locale);
		}
	}

}
