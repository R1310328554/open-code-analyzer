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

package org.springframework.ui;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import org.springframework.core.Conventions;
import org.springframework.util.Assert;

/**
 * 用于构建 UI 工具所用模型数据的 {@link java.util.Map} 实现。
 * 支持链式调用和模型属性名生成。
 *
 * <p>本类作为 Servlet MVC 的通用模型持有者，但不与其绑定。
 * 接口变体见 {@link Model} 接口。
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 2.0
 * @see Conventions#getVariableName
 * @see org.springframework.web.servlet.ModelAndView
 */
@SuppressWarnings("serial")
public class ModelMap extends LinkedHashMap<String, Object> {

	/**
	 * 构造新的空 {@code ModelMap}。
	 */
	public ModelMap() {
	}

	/**
	 * 构造包含所提供属性（以所提供名称）的新 {@code ModelMap}。
	 * @see #addAttribute(String, Object)
	 */
	public ModelMap(String attributeName, @Nullable Object attributeValue) {
		addAttribute(attributeName, attributeValue);
	}

	/**
	 * 构造包含所提供属性的新 {@code ModelMap}。
	 * 使用属性名生成器为所提供模型对象生成键。
	 * @see #addAttribute(Object)
	 */
	public ModelMap(Object attributeValue) {
		addAttribute(attributeValue);
	}


	/**
	 * 以给定名称添加所提供的属性。
	 * @param attributeName 模型属性名称（永不为 {@code null}）
	 * @param attributeValue 模型属性值（可为 {@code null}）
	 */
	public ModelMap addAttribute(String attributeName, @Nullable Object attributeValue) {
		Assert.notNull(attributeName, "Model attribute name must not be null");
		put(attributeName, attributeValue);
		return this;
	}

	/**
	 * 使用 {@link org.springframework.core.Conventions#getVariableName 生成的名称}
	 * 将所提供的属性添加到本 {@code Map}。
	 * <p><i>注意：使用本方法时，空的 {@link Collection Collection}
	 * 不会添加到模型，因为我们无法正确确定真正的约定名称。
	 * 视图代码应检查 {@code null} 而非空集合，与 JSTL 标签的做法一致。</i>
	 * @param attributeValue 模型属性值（永不为 {@code null}）
	 */
	public ModelMap addAttribute(Object attributeValue) {
		Assert.notNull(attributeValue, "Model object must not be null");
		if (attributeValue instanceof Collection<?> collection && collection.isEmpty()) {
			return this;
		}
		return addAttribute(Conventions.getVariableName(attributeValue), attributeValue);
	}

	/**
	 * 将所提供 {@code Collection} 中的所有属性复制到本 {@code Map}，
	 * 为每个元素生成属性名。
	 * @see #addAttribute(Object)
	 */
	public ModelMap addAllAttributes(@Nullable Collection<?> attributeValues) {
		if (attributeValues != null) {
			for (Object attributeValue : attributeValues) {
				addAttribute(attributeValue);
			}
		}
		return this;
	}

	/**
	 * 将所提供 {@code Map} 中的所有属性复制到本 {@code Map}。
	 * @see #addAttribute(String, Object)
	 */
	public ModelMap addAllAttributes(@Nullable Map<String, ?> attributes) {
		if (attributes != null) {
			putAll(attributes);
		}
		return this;
	}

	/**
	 * 将所提供 {@code Map} 中的所有属性复制到本 {@code Map}，
	 * 同名已有对象优先（即不会被替换）。
	 */
	public ModelMap mergeAttributes(@Nullable Map<String, ?> attributes) {
		if (attributes != null) {
			attributes.forEach((key, value) -> {
				if (!containsKey(key)) {
					put(key, value);
				}
			});
		}
		return this;
	}

	/**
	 * 本模型是否包含给定名称的属性？
	 * @param attributeName 模型属性名称（永不为 {@code null}）
	 * @return 本模型是否包含对应属性
	 */
	public boolean containsAttribute(String attributeName) {
		return containsKey(attributeName);
	}

	/**
	 * 返回给定名称的属性值（若有）。
	 * @param attributeName 模型属性名称（永不为 {@code null}）
	 * @return 对应的属性值，若无则返回 {@code null}
	 * @since 5.2
	 */
	public @Nullable Object getAttribute(String attributeName) {
		return get(attributeName);
	}

}
