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
import java.net.URL;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceEditor;
import org.springframework.util.Assert;

/**
 * {@code java.net.URL} 的属性编辑器，用于直接填充 URL 属性，
 * 而无需以 String 属性作为桥梁。
 *
 * <p>支持 Spring 风格 URL 记法：任意完全限定的标准 URL（{@code file:}、{@code http:} 等）、
 * Spring 特有的 {@code classpath:} 伪 URL，以及 Spring 上下文相关的相对文件路径。
 *
 * <p>注意：URL 必须指定合法协议，否则会在创建时被拒绝。
 * 但目标资源在创建 URL 时不一定必须存在，这取决于具体资源类型。
 *
 * @author Juergen Hoeller
 * @since 15.12.2003
 * @see java.net.URL
 * @see org.springframework.core.io.ResourceEditor
 * @see org.springframework.core.io.ResourceLoader
 * @see FileEditor
 * @see InputStreamEditor
 */
public class URLEditor extends PropertyEditorSupport {

	/** 底层用于解析 Spring 资源位置的 {@link ResourceEditor}。 */
	private final ResourceEditor resourceEditor;


	/**
	 * 创建新的 URLEditor，使用默认 ResourceEditor 作为底层实现。
	 */
	public URLEditor() {
		this.resourceEditor = new ResourceEditor();
	}

	/**
	 * 创建新的 URLEditor，使用给定的 ResourceEditor 作为底层实现。
	 * @param resourceEditor 要使用的 ResourceEditor
	 */
	public URLEditor(ResourceEditor resourceEditor) {
		Assert.notNull(resourceEditor, "ResourceEditor must not be null");
		this.resourceEditor = resourceEditor;
	}


	@Override
	public void setAsText(String text) throws IllegalArgumentException {
		this.resourceEditor.setAsText(text);
		Resource resource = (Resource) this.resourceEditor.getValue();
		try {
			setValue(resource != null ? resource.getURL() : null);
		}
		catch (IOException ex) {
			throw new IllegalArgumentException("Could not retrieve URL for " + resource + ": " + ex.getMessage());
		}
	}

	@Override
	public String getAsText() {
		URL value = (URL) getValue();
		return (value != null ? value.toExternalForm() : "");
	}

}
