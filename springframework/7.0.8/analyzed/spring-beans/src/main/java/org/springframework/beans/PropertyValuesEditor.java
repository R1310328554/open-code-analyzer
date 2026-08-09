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

package org.springframework.beans;

import java.beans.PropertyEditorSupport;
import java.util.Properties;

import org.springframework.beans.propertyeditors.PropertiesEditor;

/**
 * 面向 {@link PropertyValues} 对象的 {@link java.beans.PropertyEditor}。
 *
 * <p>所需格式见 {@link java.util.Properties} 文档说明。
 * 每个属性必须独占一行。
 *
 * <p>当前实现在底层依赖
 * {@link org.springframework.beans.propertyeditors.PropertiesEditor}。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
public class PropertyValuesEditor extends PropertyEditorSupport {

	/** 用于将文本解析为 Properties 的底层编辑器。 */
	private final PropertiesEditor propertiesEditor = new PropertiesEditor();

	/**
	 * 将文本解析为 Properties，再包装为 MutablePropertyValues。
	 */
	@Override
	public void setAsText(String text) throws IllegalArgumentException {
		this.propertiesEditor.setAsText(text);
		Properties props = (Properties) this.propertiesEditor.getValue();
		setValue(new MutablePropertyValues(props));
	}

}
