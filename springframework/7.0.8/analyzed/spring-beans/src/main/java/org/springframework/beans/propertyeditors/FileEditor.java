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
import java.io.File;
import java.io.IOException;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceEditor;
import org.springframework.util.Assert;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;

/**
 * {@code java.io.File} 属性编辑器，从 Spring 资源位置直接填充 File 属性。
 *
 * <p>支持 Spring 风格的 URL 表示法：任意完全限定的标准 URL
 *（"file:"、"http:" 等）以及 Spring 特有的 "classpath:" 伪 URL。
 *
 * <p><b>注意：</b>本编辑器的行为在 Spring 2.0 中已变更。
 * 此前它直接从文件名创建 File 实例。
 * 自 Spring 2.0 起，它接受标准 Spring 资源位置作为输入；
 * 这与 URLEditor 和 InputStreamEditor 的行为一致。
 *
 * <p><b>注意：</b>Spring 2.5 做了如下修改。
 * 若指定的文件名没有 URL 前缀或不是绝对路径，
 * 则尝试使用标准 ResourceLoader 语义定位文件。
 * 若文件未找到，则创建 File 实例，假定文件名指向相对文件位置。
 *
 * @author Juergen Hoeller
 * @author Thomas Risberg
 * @since 09.12.2003
 * @see java.io.File
 * @see org.springframework.core.io.ResourceEditor
 * @see org.springframework.core.io.ResourceLoader
 * @see URLEditor
 * @see InputStreamEditor
 */
public class FileEditor extends PropertyEditorSupport {

	/** 底层用于解析资源位置的 ResourceEditor。 */
	private final ResourceEditor resourceEditor;


	/**
	 * 创建新的 FileEditor，底层使用默认 ResourceEditor。
	 */
	public FileEditor() {
		this.resourceEditor = new ResourceEditor();
	}

	/**
	 * 创建新的 FileEditor，底层使用给定 ResourceEditor。
	 * @param resourceEditor 要使用的 ResourceEditor
	 */
	public FileEditor(ResourceEditor resourceEditor) {
		Assert.notNull(resourceEditor, "ResourceEditor must not be null");
		this.resourceEditor = resourceEditor;
	}


	/**
	 * 将 Spring 资源位置文本解析为 File 对象。
	 * <p>优先处理无 "file:" 前缀的绝对文件路径（向后兼容），
	 * 否则通过 ResourceEditor 解析资源位置。
	 */
	@Override
	public void setAsText(String text) throws IllegalArgumentException {
		if (!StringUtils.hasText(text)) {
			setValue(null);
			return;
		}

		// 检查是否为无 "file:" 前缀的绝对文件路径（向后兼容，直接作为文件路径处理）
		File file = null;
		if (!ResourceUtils.isUrl(text)) {
			file = new File(text);
			if (file.isAbsolute()) {
				setValue(file);
				return;
			}
		}

		// 按标准资源位置解析流程处理
		this.resourceEditor.setAsText(text);
		Resource resource = (Resource) this.resourceEditor.getValue();

		// 若为 URL 或指向已存在资源的路径，直接获取 File
		if (file == null || resource.exists()) {
			try {
				setValue(resource.getFile());
			}
			catch (IOException ex) {
				throw new IllegalArgumentException(
						"Could not retrieve file for " + resource + ": " + ex.getMessage());
			}
		}
		else {
			// 创建相对 File 引用
			setValue(file);
		}
	}

	/**
	 * 将 File 对象格式化为文件路径字符串。
	 */
	@Override
	public String getAsText() {
		File value = (File) getValue();
		return (value != null ? value.getPath() : "");
	}

}
