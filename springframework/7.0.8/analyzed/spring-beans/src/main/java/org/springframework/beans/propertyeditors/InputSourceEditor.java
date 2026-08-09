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
import java.io.IOException;

import org.xml.sax.InputSource;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceEditor;
import org.springframework.util.Assert;

/**
 * {@code org.xml.sax.InputSource} 属性编辑器，
 * 将 Spring 资源位置字符串转换为 SAX InputSource 对象。
 *
 * <p>支持 Spring 风格的 URL 表示法：任意完全限定的标准 URL
 *（"file:"、"http:" 等）以及 Spring 特有的 "classpath:" 伪 URL。
 *
 * @author Juergen Hoeller
 * @since 3.0.3
 * @see org.xml.sax.InputSource
 * @see org.springframework.core.io.ResourceEditor
 * @see org.springframework.core.io.ResourceLoader
 * @see URLEditor
 * @see FileEditor
 */
public class InputSourceEditor extends PropertyEditorSupport {

	/** 底层用于解析资源位置的 ResourceEditor。 */
	private final ResourceEditor resourceEditor;


	/**
	 * 创建新的 InputSourceEditor，底层使用默认 ResourceEditor。
	 */
	public InputSourceEditor() {
		this.resourceEditor = new ResourceEditor();
	}

	/**
	 * 创建新的 InputSourceEditor，底层使用给定 ResourceEditor。
	 * @param resourceEditor 要使用的 ResourceEditor
	 */
	public InputSourceEditor(ResourceEditor resourceEditor) {
		Assert.notNull(resourceEditor, "ResourceEditor must not be null");
		this.resourceEditor = resourceEditor;
	}


	/**
	 * 将 Spring 资源位置文本解析为 InputSource 对象。
	 */
	@Override
	public void setAsText(String text) throws IllegalArgumentException {
		this.resourceEditor.setAsText(text);
		Resource resource = (Resource) this.resourceEditor.getValue();
		try {
			setValue(resource != null ? new InputSource(resource.getURL().toString()) : null);
		}
		catch (IOException ex) {
			throw new IllegalArgumentException(
					"Could not retrieve URL for " + resource + ": " + ex.getMessage());
		}
	}

	/**
	 * 将 InputSource 对象格式化为系统标识（URL）字符串。
	 */
	@Override
	public String getAsText() {
		InputSource value = (InputSource) getValue();
		return (value != null ? value.getSystemId() : "");
	}

}
