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
import java.util.Currency;

import org.springframework.util.StringUtils;

/**
 * {@code java.util.Currency} 属性编辑器，
 * 将货币代码转换为 Currency 对象，并以货币代码作为文本表示。
 *
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 3.0
 * @see java.util.Currency
 */
public class CurrencyEditor extends PropertyEditorSupport {

	/**
	 * 将货币代码文本解析为 Currency 对象。
	 */
	@Override
	public void setAsText(String text) throws IllegalArgumentException {
		if (StringUtils.hasText(text)) {
			text = text.trim();
		}
		setValue(Currency.getInstance(text));
	}

	/**
	 * 将 Currency 对象格式化为货币代码字符串。
	 */
	@Override
	public String getAsText() {
		Currency value = (Currency) getValue();
		return (value != null ? value.getCurrencyCode() : "");
	}

}
