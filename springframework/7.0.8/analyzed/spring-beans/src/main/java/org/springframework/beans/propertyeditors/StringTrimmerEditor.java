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
 * 修剪 String 的属性编辑器。
 *
 * <p>可选地将空字符串转换为 {@code null} 值。
 * 须显式注册，例如在命令绑定场景中。
 *
 * @author Juergen Hoeller
 * @see org.springframework.validation.DataBinder#registerCustomEditor
 */
public class StringTrimmerEditor extends PropertyEditorSupport {

	/** 除 trim 外要删除的字符集；可为 null。 */
	private final @Nullable String charsToDelete;

	/** 修剪后空串是否转换为 {@code null}。 */
	private final boolean emptyAsNull;


	/**
	 * 创建新的 StringTrimmerEditor。
	 * @param emptyAsNull 空 String 是否转换为 {@code null}
	 */
	public StringTrimmerEditor(boolean emptyAsNull) {
		this.charsToDelete = null;
		this.emptyAsNull = emptyAsNull;
	}

	/**
	 * 创建新的 StringTrimmerEditor。
	 * @param charsToDelete 除修剪外要删除的字符集；可用于删除多余换行，
	 * 例如 {@code \r\n\f} 会删除所有换行与换页符
	 * @param emptyAsNull 空 String 是否转换为 {@code null}
	 */
	public StringTrimmerEditor(String charsToDelete, boolean emptyAsNull) {
		this.charsToDelete = charsToDelete;
		this.emptyAsNull = emptyAsNull;
	}


	@Override
	public void setAsText(@Nullable String text) {
		if (text == null) {
			setValue(null);
		}
		else {
			String value = text.trim();
			if (this.charsToDelete != null) {
				value = StringUtils.deleteAny(value, this.charsToDelete);
			}
			if (this.emptyAsNull && value.isEmpty()) {
				setValue(null);
			}
			else {
				setValue(value);
			}
		}
	}

	@Override
	public String getAsText() {
		Object value = getValue();
		return (value != null ? value.toString() : "");
	}

}
