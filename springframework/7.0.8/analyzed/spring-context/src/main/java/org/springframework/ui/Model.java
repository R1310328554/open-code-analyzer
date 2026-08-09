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
import java.util.Map;

import org.jspecify.annotations.Nullable;

/**
 * 定义模型属性持有者的接口。
 *
 * <p>主要用于向模型添加属性。
 *
 * <p>允许以 {@code java.util.Map} 形式访问整体模型。
 *
 * @author Juergen Hoeller
 * @since 2.5.1
 */
public interface Model {

	/**
	 * 以给定名称添加所提供的属性。
	 * @param attributeName 模型属性名称（永不为 {@code null}）
	 * @param attributeValue 模型属性值（可为 {@code null}）
	 */
	Model addAttribute(String attributeName, @Nullable Object attributeValue);

	/**
	 * 使用 {@link org.springframework.core.Conventions#getVariableName 生成的名称}
	 * 将所提供的属性添加到本 {@code Map}。
	 * <p><i>注意：使用本方法时，空的 {@link java.util.Collection Collection}
	 * 不会添加到模型，因为我们无法正确确定真正的约定名称。
	 * 视图代码应检查 {@code null} 而非空集合，与 JSTL 标签的做法一致。</i>
	 * @param attributeValue 模型属性值（永不为 {@code null}）
	 */
	Model addAttribute(Object attributeValue);

	/**
	 * 将所提供 {@code Collection} 中的所有属性复制到本 {@code Map}，
	 * 为每个元素生成属性名。
	 * @see #addAttribute(Object)
	 */
	Model addAllAttributes(Collection<?> attributeValues);

	/**
	 * 将所提供 {@code Map} 中的所有属性复制到本 {@code Map}。
	 * @see #addAttribute(String, Object)
	 */
	Model addAllAttributes(Map<String, ?> attributes);

	/**
	 * 将所提供 {@code Map} 中的所有属性复制到本 {@code Map}，
	 * 同名已有对象优先（即不会被替换）。
	 */
	Model mergeAttributes(Map<String, ?> attributes);

	/**
	 * 本模型是否包含给定名称的属性？
	 * @param attributeName 模型属性名称（永不为 {@code null}）
	 * @return 本模型是否包含对应属性
	 */
	boolean containsAttribute(String attributeName);

	/**
	 * 返回给定名称的属性值（若有）。
	 * @param attributeName 模型属性名称（永不为 {@code null}）
	 * @return 对应的属性值，若无则返回 {@code null}
	 * @since 5.2
	 */
	@Nullable Object getAttribute(String attributeName);

	/**
	 * 以 Map 形式返回当前模型属性集合。
	 */
	Map<String, Object> asMap();

}
