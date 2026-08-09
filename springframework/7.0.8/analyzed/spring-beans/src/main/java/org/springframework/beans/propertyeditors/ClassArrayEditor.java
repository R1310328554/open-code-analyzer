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
import java.util.StringJoiner;

import org.jspecify.annotations.Nullable;

import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * {@link Class} 数组属性编辑器，用于直接填充 {@code Class[]} 属性，
 * 无需借助字符串类名属性作为桥梁。
 *
 * <p>与标准 {@link Class#forName(String)} 不同，还支持
 * {@code "java.lang.String[]"} 形式的数组类名。
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 2.0
 */
public class ClassArrayEditor extends PropertyEditorSupport {

	/** 用于加载类的 ClassLoader。 */
	private final @Nullable ClassLoader classLoader;


	/**
	 * 创建默认的 {@code ClassArrayEditor}，使用线程上下文 {@code ClassLoader}。
	 */
	public ClassArrayEditor() {
		this(null);
	}

	/**
	 * 创建默认的 {@code ClassArrayEditor}，使用指定的 {@code ClassLoader}。
	 * @param classLoader 要使用的 {@code ClassLoader}
	 *（或传入 {@code null} 以使用线程上下文 {@code ClassLoader}）
	 */
	public ClassArrayEditor(@Nullable ClassLoader classLoader) {
		this.classLoader = (classLoader != null ? classLoader : ClassUtils.getDefaultClassLoader());
	}


	/**
	 * 将逗号分隔的类名文本解析为 Class 数组；空文本对应 {@code null}。
	 */
	@Override
	public void setAsText(String text) throws IllegalArgumentException {
		if (StringUtils.hasText(text)) {
			String[] classNames = StringUtils.commaDelimitedListToStringArray(text);
			Class<?>[] classes = new Class<?>[classNames.length];
			for (int i = 0; i < classNames.length; i++) {
				String className = classNames[i].trim();
				classes[i] = ClassUtils.resolveClassName(className, this.classLoader);
			}
			setValue(classes);
		}
		else {
			setValue(null);
		}
	}

	/**
	 * 将 Class 数组格式化为逗号分隔的完全限定类名字符串。
	 */
	@Override
	public String getAsText() {
		Class<?>[] classes = (Class[]) getValue();
		if (ObjectUtils.isEmpty(classes)) {
			return "";
		}
		StringJoiner sj = new StringJoiner(",");
		for (Class<?> klass : classes) {
			sj.add(ClassUtils.getQualifiedName(klass));
		}
		return sj.toString();
	}

}
