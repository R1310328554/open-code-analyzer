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
import java.util.concurrent.ConcurrentHashMap;

import org.jspecify.annotations.Nullable;

import org.springframework.core.Conventions;
import org.springframework.util.Assert;

/**
 * 基于 {@link ConcurrentHashMap} 的 {@link Model} 接口实现，
 * 用于并发场景。
 *
 * <p>由 Spring WebFlux 暴露给处理器方法，通常通过声明 {@link Model} 接口。
 * 用户代码通常无需创建。必要时处理器方法可返回常规 {@code java.util.Map}，
 * 可能是 {@code java.util.ConcurrentMap}，作为预定模型。
 *
 * @author Rossen Stoyanchev
 * @since 5.0
 */
@SuppressWarnings("serial")
public class ConcurrentModel extends ConcurrentHashMap<String, Object> implements Model {

	/**
	 * 构造新的空 {@code ConcurrentModel}。
	 */
	public ConcurrentModel() {
	}

	/**
	 * 构造包含所提供属性（以所提供名称）的新 {@code ConcurrentModel}。
	 * @see #addAttribute(String, Object)
	 */
	public ConcurrentModel(String attributeName, Object attributeValue) {
		addAttribute(attributeName, attributeValue);
	}

	/**
	 * 构造包含所提供属性的新 {@code ConcurrentModel}。
	 * <p>使用属性名生成器为所提供模型对象生成键。
	 * @see #addAttribute(Object)
	 */
	public ConcurrentModel(Object attributeValue) {
		addAttribute(attributeValue);
	}


	@Override
	public @Nullable Object put(String key, @Nullable Object value) {
		if (value != null) {
			return super.put(key, value);
		}
		else {
			return remove(key);
		}
	}

	@Override
	public void putAll(Map<? extends String, ?> map) {
		for (Map.Entry<? extends String, ?> entry : map.entrySet()) {
			put(entry.getKey(), entry.getValue());
		}
	}

	/**
	 * 以给定名称添加所提供的属性。
	 * @param attributeName 模型属性名称（永不为 {@code null}）
	 * @param attributeValue 模型属性值（若为 {@code null} 则忽略，
	 * 仅移除已有条目（若有））
	 */
	@Override
	public ConcurrentModel addAttribute(String attributeName, @Nullable Object attributeValue) {
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
	@Override
	public ConcurrentModel addAttribute(Object attributeValue) {
		Assert.notNull(attributeValue, "Model attribute value must not be null");
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
	@Override
	public ConcurrentModel addAllAttributes(@Nullable Collection<?> attributeValues) {
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
	@Override
	public ConcurrentModel addAllAttributes(@Nullable Map<String, ?> attributes) {
		if (attributes != null) {
			putAll(attributes);
		}
		return this;
	}

	/**
	 * 将所提供 {@code Map} 中的所有属性复制到本 {@code Map}，
	 * 同名已有对象优先（即不会被替换）。
	 */
	@Override
	public ConcurrentModel mergeAttributes(@Nullable Map<String, ?> attributes) {
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
	@Override
	public boolean containsAttribute(String attributeName) {
		return containsKey(attributeName);
	}

	@Override
	public @Nullable Object getAttribute(String attributeName) {
		return get(attributeName);
	}

	@Override
	public Map<String, Object> asMap() {
		return this;
	}

}
