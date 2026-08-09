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
import java.nio.charset.Charset;

import org.springframework.util.StringUtils;

/**
 * {@code java.nio.charset.Charset} 属性编辑器，
 * 在字符集字符串表示与 Charset 对象之间双向转换。
 *
 * <p>期望的语法与 Charset 的 {@link java.nio.charset.Charset#name()} 相同，
 * 例如 {@code UTF-8}、{@code ISO-8859-16} 等。
 *
 * @author Arjen Poutsma
 * @author Sam Brannen
 * @since 2.5.4
 * @see Charset
 */
public class CharsetEditor extends PropertyEditorSupport {

	/**
	 * 将字符集名称文本解析为 Charset 对象；空文本对应 {@code null}。
	 */
	@Override
	public void setAsText(String text) throws IllegalArgumentException {
		if (StringUtils.hasText(text)) {
			setValue(Charset.forName(text.trim()));
		}
		else {
			setValue(null);
		}
	}

	/**
	 * 将 Charset 对象格式化为字符集名称字符串。
	 */
	@Override
	public String getAsText() {
		Charset value = (Charset) getValue();
		return (value != null ? value.name() : "");
	}

}
