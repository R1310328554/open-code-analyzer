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

package org.springframework.jndi;

import java.beans.PropertyEditorSupport;
import java.util.Properties;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.propertyeditors.PropertiesEditor;

/**
 * {@code JndiTemplate} 对象的属性编辑器。
 * 允许用 properties 格式字符串填充 {@code JndiTemplate} 类型属性。
 *
 * @author Rod Johnson
 * @since 09.05.2003
 */
public class JndiTemplateEditor extends PropertyEditorSupport {

	private final PropertiesEditor propertiesEditor = new PropertiesEditor();

	@Override
	public void setAsText(@Nullable String text) throws IllegalArgumentException {
		if (text == null) {
			throw new IllegalArgumentException("JndiTemplate cannot be created from null string");
		}
		if (text.isEmpty()) {
			// empty environment
			setValue(new JndiTemplate());
		}
		else {
			// we have a non-empty properties string
			this.propertiesEditor.setAsText(text);
			Properties props = (Properties) this.propertiesEditor.getValue();
			setValue(new JndiTemplate(props));
		}
	}

}
