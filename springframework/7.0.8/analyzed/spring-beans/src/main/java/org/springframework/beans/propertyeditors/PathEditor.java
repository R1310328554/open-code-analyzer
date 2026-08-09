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
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceEditor;
import org.springframework.util.Assert;
import org.springframework.util.ResourceUtils;

/**
 * {@code java.nio.file.Path} 的属性编辑器，用于直接填充 Path 属性，
 * 而无需以 String 属性作为桥梁。
 *
 * <p>基于 {@link Paths#get(URI)} 的解析算法，检查已注册的 NIO 文件系统提供程序，
 * 包括对 {@code file:...} 路径使用默认文件系统。
 * 同时支持 Spring 风格 URL 记法：任意完全限定的标准 URL 以及 Spring 特有的
 * {@code classpath:} 伪 URL，还有 Spring 上下文相关的相对文件路径。
 * 若找不到已存在的上下文相对资源，则回退到通过 {@code Paths#get(String)}
 * 在文件系统中解析路径。
 *
 * @author Juergen Hoeller
 * @since 4.3.2
 * @see java.nio.file.Path
 * @see Paths#get(URI)
 * @see ResourceEditor
 * @see org.springframework.core.io.ResourceLoader
 * @see FileEditor
 * @see URLEditor
 */
public class PathEditor extends PropertyEditorSupport {

	/** 底层用于解析 Spring 资源位置的 {@link ResourceEditor}。 */
	private final ResourceEditor resourceEditor;


	/**
	 * 创建新的 PathEditor，使用默认的 ResourceEditor 作为底层实现。
	 */
	public PathEditor() {
		this.resourceEditor = new ResourceEditor();
	}

	/**
	 * 创建新的 PathEditor，使用给定的 ResourceEditor 作为底层实现。
	 * @param resourceEditor 要使用的 ResourceEditor
	 */
	public PathEditor(ResourceEditor resourceEditor) {
		Assert.notNull(resourceEditor, "ResourceEditor must not be null");
		this.resourceEditor = resourceEditor;
	}


	@Override
	public void setAsText(String text) throws IllegalArgumentException {
		// 非 classpath: 前缀的路径可能是 NIO 路径候选
		boolean nioPathCandidate = !text.startsWith(ResourceUtils.CLASSPATH_URL_PREFIX);
		if (nioPathCandidate && !text.startsWith("/")) {
			try {
				URI uri = ResourceUtils.toURI(text);
				String scheme = uri.getScheme();
				if (scheme != null) {
					// 除 "C:" 风格盘符外，不再视为 NIO 候选
					nioPathCandidate = (scheme.length() == 1);
					// 尝试通过 Paths.get(URI) 使用 NIO 文件系统提供程序
					setValue(Paths.get(uri).normalize());
					return;
				}
			}
			catch (URISyntaxException ex) {
				// 非合法 URI；可能是 file 前缀后的 Windows 风格路径（改走 Spring 资源解析）
				nioPathCandidate = !text.startsWith(ResourceUtils.FILE_URL_PREFIX);
			}
			catch (FileSystemNotFoundException | IllegalArgumentException ex) {
				// URI scheme 未在 NIO 中注册或不满足 Paths 要求：改走 Spring 资源机制
			}
		}

		this.resourceEditor.setAsText(text);
		Resource resource = (Resource) this.resourceEditor.getValue();
		if (resource == null) {
			setValue(null);
		}
		else if (nioPathCandidate && (!resource.isFile() || !resource.exists())) {
			// 资源不存在或非文件：按本地路径字符串解析
			setValue(Paths.get(text).normalize());
		}
		else {
			try {
				setValue(resource.getFilePath());
			}
			catch (IOException ex) {
				String msg = "Could not resolve \"" + text + "\" to 'java.nio.file.Path' for " + resource + ": " +
						ex.getMessage();
				if (nioPathCandidate) {
					msg += " - In case of ambiguity, consider adding the 'file:' prefix for an explicit reference " +
							"to a file system resource of the same name: \"file:" + text + "\"";
				}
				throw new IllegalArgumentException(msg);
			}
		}
	}

	@Override
	public String getAsText() {
		Path value = (Path) getValue();
		return (value != null ? value.toString() : "");
	}

}
