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

package org.springframework.beans;

import java.beans.PropertyEditor;

import org.jspecify.annotations.Nullable;

/**
 * 封装注册 JavaBeans {@link PropertyEditor PropertyEditor} 的方法。
 * 这是 {@link PropertyEditorRegistrar} 操作所围绕的核心接口。
 *
 * <p>由 {@link BeanWrapper} 扩展；由 {@link BeanWrapperImpl}
 * 与 {@link org.springframework.validation.DataBinder} 实现。
 *
 * @author Juergen Hoeller
 * @since 1.2.6
 * @see java.beans.PropertyEditor
 * @see PropertyEditorRegistrar
 * @see BeanWrapper
 * @see org.springframework.validation.DataBinder
 */
public interface PropertyEditorRegistry {

	/**
	 * 为给定类型的全部属性注册自定义属性编辑器。
	 * @param requiredType 属性类型
	 * @param propertyEditor 要注册的编辑器
	 */
	void registerCustomEditor(Class<?> requiredType, PropertyEditor propertyEditor);

	/**
	 * 为给定类型与属性注册自定义属性编辑器，或为该类型的全部属性注册。
	 * <p>若属性路径表示数组或 Collection 属性，编辑器会作用于整个数组/Collection
	 * （此时 {@link PropertyEditor} 需创建数组或 Collection 值），或作用于每个元素
	 * （此时 {@code PropertyEditor} 需创建元素类型），具体取决于指定的 requiredType。
	 * <p>注意：每个属性路径仅支持注册一个自定义编辑器。
	 * 对于 Collection/数组，请勿在同一属性上同时为集合本身和每个元素注册编辑器。
	 * <p>例如，若要为 "items[n].quantity"（全部 n）注册编辑器，
	 * 应将本方法的 {@code propertyPath} 参数设为 "items.quantity"。
	 * @param requiredType 属性类型。若已给出属性可为 {@code null}，
	 * 但无论如何都应指定，尤其是 Collection 场景——以便明确编辑器作用于
	 * 整个 Collection 还是其各个条目。一般规则：
	 * <b>Collection/数组情况下不要在这里传 {@code null}！</b>
	 * @param propertyPath 属性路径（名称或嵌套路径）；若为给定类型的全部属性注册编辑器则为 {@code null}
	 * @param propertyEditor 要注册的编辑器
	 */
	void registerCustomEditor(@Nullable Class<?> requiredType, @Nullable String propertyPath, PropertyEditor propertyEditor);

	/**
	 * 查找给定类型与属性对应的自定义属性编辑器。
	 * @param requiredType 属性类型（若已给出属性可为 {@code null}，
	 * 但为一致性检查仍应指定）
	 * @param propertyPath 属性路径（名称或嵌套路径）；若查找该类型全部属性的编辑器则为 {@code null}
	 * @return 已注册的编辑器；若无则返回 {@code null}
	 */
	@Nullable PropertyEditor findCustomEditor(@Nullable Class<?> requiredType, @Nullable String propertyPath);

}
