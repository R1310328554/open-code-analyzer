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

/**
 * 字节数组（{@code byte[]}）属性编辑器。
 *
 * <p>将字符串直接转换为其对应的字节表示。
 *
 * @author Juergen Hoeller
 * @since 1.0.1
 * @see java.lang.String#getBytes
 */
public class ByteArrayPropertyEditor extends PropertyEditorSupport {

	/**
	 * 将文本转换为字节数组；{@code null} 文本对应 {@code null} 值。
	 */
	@Override
	public void setAsText(@Nullable String text) {
		setValue(text != null ? text.getBytes() : null);
	}

	/**
	 * 将字节数组转换回字符串表示。
	 */
	@Override
	public String getAsText() {
		byte[] value = (byte[]) getValue();
		return (value != null ? new String(value) : "");
	}

}
