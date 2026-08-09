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

import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

/**
 * {@link Class java.lang.Class} 属性编辑器，用于直接填充 {@code Class} 属性，
 * 无需借助字符串类名属性作为桥梁。
 *
 * <p>与标准 {@link Class#forName(String)} 不同，还支持
 * {@code "java.lang.String[]"} 形式的数组类名。
 *
 * @author Juergen Hoeller
 * @author Rick Evans
 * @since 13.05.2003
 * @see Class#forName
 * @see org.springframework.util.ClassUtils#forName(String, ClassLoader)
 */
public class ClassEditor extends PropertyEditorSupport {

	/** 用于加载类的 ClassLoader。 */
	private final @Nullable ClassLoader classLoader;


	/**
	 * 创建默认 ClassEditor，使用线程上下文 ClassLoader。
	 */
	public ClassEditor() {
		this(null);
	}

	/**
	 * 创建默认 ClassEditor，使用指定的 ClassLoader。
	 * @param classLoader 要使用的 ClassLoader
	 *（或 {@code null} 表示使用线程上下文 ClassLoader）
	 */
	public ClassEditor(@Nullable ClassLoader classLoader) {
		this.classLoader = (classLoader != null ? classLoader : ClassUtils.getDefaultClassLoader());
	}


	/**
	 * 将类名文本解析为 Class 对象；空文本对应 {@code null}。
	 */
	@Override
	public void setAsText(String text) throws IllegalArgumentException {
		if (StringUtils.hasText(text)) {
			setValue(ClassUtils.resolveClassName(text.trim(), this.classLoader));
		}
		else {
			setValue(null);
		}
	}

	/**
	 * 将 Class 对象格式化为完全限定类名字符串。
	 */
	@Override
	public String getAsText() {
		Class<?> clazz = (Class<?>) getValue();
		if (clazz != null) {
			return ClassUtils.getQualifiedName(clazz);
		}
		else {
			return "";
		}
	}

}
