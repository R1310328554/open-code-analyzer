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

import org.jspecify.annotations.Nullable;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceEditor;
import org.springframework.util.Assert;

/**
 * 单向 {@link java.beans.PropertyEditor}：将文本字符串转换为 {@code java.io.InputStream}，
 * 把给定字符串解释为 Spring 资源位置（例如 URL 字符串）。
 *
 * <p>支持 Spring 风格 URL 记法：任意完全限定的标准 URL（{@code file:}、{@code http:} 等）
 * 以及 Spring 特有的 {@code classpath:} 伪 URL。
 *
 * <p>注意：此类流通常不会由 Spring 自行关闭！
 *
 * @author Juergen Hoeller
 * @since 1.0.1
 * @see java.io.InputStream
 * @see org.springframework.core.io.ResourceEditor
 * @see org.springframework.core.io.ResourceLoader
 * @see URLEditor
 * @see FileEditor
 */
public class InputStreamEditor extends PropertyEditorSupport {

	/** 底层用于解析资源位置的 {@link ResourceEditor}。 */
	private final ResourceEditor resourceEditor;


	/**
	 * 创建新的 InputStreamEditor，使用默认的 ResourceEditor 作为底层实现。
	 */
	public InputStreamEditor() {
		this.resourceEditor = new ResourceEditor();
	}

	/**
	 * 创建新的 InputStreamEditor，使用给定的 ResourceEditor 作为底层实现。
	 * @param resourceEditor 要使用的 ResourceEditor
	 */
	public InputStreamEditor(ResourceEditor resourceEditor) {
		Assert.notNull(resourceEditor, "ResourceEditor must not be null");
		this.resourceEditor = resourceEditor;
	}


	@Override
	public void setAsText(String text) throws IllegalArgumentException {
		// 先通过 ResourceEditor 将文本解析为 Resource
		this.resourceEditor.setAsText(text);
		Resource resource = (Resource) this.resourceEditor.getValue();
		try {
			setValue(resource != null ? resource.getInputStream() : null);
		}
		catch (IOException ex) {
			throw new IllegalArgumentException("Failed to retrieve InputStream for " + resource, ex);
		}
	}

	/**
	 * 本实现返回 {@code null}，表示不存在合适的文本表示形式（单向转换）。
	 */
	@Override
	public @Nullable String getAsText() {
		return null;
	}

}
