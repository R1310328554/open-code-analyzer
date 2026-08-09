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

import org.jspecify.annotations.Nullable;

import org.springframework.core.io.ClassPathResource;
import org.springframework.util.ClassUtils;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;

/**
 * {@code java.net.URI} 的属性编辑器，用于直接填充 URI 属性，
 * 而无需以 String 属性作为桥梁。
 *
 * <p>支持 Spring 风格 URI 记法：任意完全限定的标准 URI（{@code file:}、{@code http:} 等）
 * 以及 Spring 特有的 {@code classpath:} 伪 URL，后者会解析为对应的 URI。
 *
 * <p>默认情况下，本编辑器会对字符串进行 URI 编码（例如空格编码为 {@code %20}）。
 * 可通过调用 {@link #URIEditor(boolean)} 构造函数改变此行为。
 *
 * <p>注意：URI 比 URL 更宽松，不要求指定合法协议。
 * 只要符合合法 URI 语法，任何 scheme 均可，即使未注册匹配的协议处理器。
 *
 * @author Juergen Hoeller
 * @since 2.0.2
 * @see java.net.URI
 * @see URLEditor
 */
public class URIEditor extends PropertyEditorSupport {

	/** 用于解析 {@code classpath:} 位置的 ClassLoader；为 null 时不解析 classpath。 */
	private final @Nullable ClassLoader classLoader;

	/** 是否对字符串进行 URI 编码。 */
	private final boolean encode;



	/**
	 * 创建新的、启用编码的 URIEditor，将 {@code classpath:} 位置转换为标准 URI
	 *（不尝试解析为物理资源）。
	 */
	public URIEditor() {
		this(true);
	}

	/**
	 * 创建新的 URIEditor，将 {@code classpath:} 位置转换为标准 URI
	 *（不尝试解析为物理资源）。
	 * @param encode 是否对字符串进行编码
	 * @since 3.0
	 */
	public URIEditor(boolean encode) {
		this.classLoader = null;
		this.encode = encode;
	}

	/**
	 * 创建新的 URIEditor，使用给定 ClassLoader 将 {@code classpath:} 位置
	 * 解析为物理资源 URL。
	 * @param classLoader 用于解析 {@code classpath:} 位置的 ClassLoader
	 *（可为 {@code null} 表示默认 ClassLoader）
	 */
	public URIEditor(@Nullable ClassLoader classLoader) {
		this(classLoader, true);
	}

	/**
	 * 创建新的 URIEditor，使用给定 ClassLoader 将 {@code classpath:} 位置
	 * 解析为物理资源 URL。
	 * @param classLoader 用于解析 {@code classpath:} 位置的 ClassLoader
	 *（可为 {@code null} 表示默认 ClassLoader）
	 * @param encode 是否对字符串进行编码
	 * @since 3.0
	 */
	public URIEditor(@Nullable ClassLoader classLoader, boolean encode) {
		this.classLoader = (classLoader != null ? classLoader : ClassUtils.getDefaultClassLoader());
		this.encode = encode;
	}


	@Override
	public void setAsText(String text) throws IllegalArgumentException {
		if (StringUtils.hasText(text)) {
			String uri = text.trim();
			if (this.classLoader != null && uri.startsWith(ResourceUtils.CLASSPATH_URL_PREFIX)) {
				// classpath: 前缀：通过 ClassPathResource 解析为物理 URI
				ClassPathResource resource = new ClassPathResource(
						uri.substring(ResourceUtils.CLASSPATH_URL_PREFIX.length()), this.classLoader);
				try {
					setValue(resource.getURI());
				}
				catch (IOException ex) {
					throw new IllegalArgumentException("Could not retrieve URI for " + resource + ": " + ex.getMessage());
				}
			}
			else {
				try {
					setValue(createURI(uri));
				}
				catch (URISyntaxException ex) {
					throw new IllegalArgumentException("Invalid URI syntax: " + ex.getMessage());
				}
			}
		}
		else {
			setValue(null);
		}
	}

	/**
	 * 为给定用户输入字符串创建 URI 实例。
	 * <p>默认实现将值编码为符合 RFC-2396 的 URI。
	 * @param value 要转换为 URI 实例的值
	 * @return URI 实例
	 * @throws java.net.URISyntaxException 若 URI 转换失败
	 */
	protected URI createURI(String value) throws URISyntaxException {
		int colonIndex = value.indexOf(':');
		if (this.encode && colonIndex != -1) {
			// 含 scheme：拆分 scheme、scheme-specific part 与 fragment 分别编码
			int fragmentIndex = value.indexOf('#', colonIndex + 1);
			String scheme = value.substring(0, colonIndex);
			String ssp = value.substring(colonIndex + 1, (fragmentIndex > 0 ? fragmentIndex : value.length()));
			String fragment = (fragmentIndex > 0 ? value.substring(fragmentIndex + 1) : null);
			return new URI(scheme, ssp, fragment);
		}
		else {
			// 不编码或值不含 scheme：回退到默认构造
			return new URI(value);
		}
	}


	@Override
	public String getAsText() {
		URI value = (URI) getValue();
		return (value != null ? value.toString() : "");
	}

}
