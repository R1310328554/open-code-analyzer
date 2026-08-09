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

import org.springframework.util.StringUtils;

/**
 * {@code java.util.Locale} 的属性编辑器，用于直接填充 Locale 类型属性。
 *
 * <p>语法与 Locale 的 {@code toString()} 相同：语言 + 可选的国家/地区 + 可选的变体，
 * 以 {@code _} 分隔（例如 {@code en}、{@code en_US}）。
 * 也接受空格作为分隔符，作为下划线的替代。
 *
 * @author Juergen Hoeller
 * @since 26.05.2003
 * @see java.util.Locale
 * @see org.springframework.util.StringUtils#parseLocale
 */
public class LocaleEditor extends PropertyEditorSupport {

	@Override
	public void setAsText(String text) {
		setValue(StringUtils.parseLocale(text));
	}

	@Override
	public String getAsText() {
		Object value = getValue();
		return (value != null ? value.toString() : "");
	}

}
