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

import org.jspecify.annotations.Nullable;

import org.springframework.core.convert.ConversionService;

/**
 * 封装 {@link PropertyAccessor} 配置方法的接口。
 * 同时继承 {@link PropertyEditorRegistry}，后者定义了 PropertyEditor 的管理方法。
 *
 * <p>作为 {@link BeanWrapper} 的基础接口。
 *
 * @author Juergen Hoeller
 * @author Stephane Nicoll
 * @since 2.0
 * @see BeanWrapper
 */
public interface ConfigurablePropertyAccessor extends PropertyAccessor, PropertyEditorRegistry, TypeConverter {

	/**
	 * 指定用于转换属性值的 {@link ConversionService}，
	 * 作为 JavaBeans PropertyEditor 的替代方案。
	 */
	void setConversionService(@Nullable ConversionService conversionService);

	/**
	 * 返回关联的 ConversionService（若有）。
	 */
	@Nullable ConversionService getConversionService();

	/**
	 * 设置在将 PropertyEditor 应用于属性的新值时，
	 * 是否先提取该属性的旧值。
	 */
	void setExtractOldValueForEditor(boolean extractOldValueForEditor);

	/**
	 * 返回在将 PropertyEditor 应用于属性的新值时，
	 * 是否会先提取该属性的旧值。
	 */
	boolean isExtractOldValueForEditor();

	/**
	 * 设置当嵌套路径中存在 {@code null} 值时，
	 * 本实例是否应尝试“自动生长”（auto-grow）该路径。
	 * <p>若为 {@code true}，则会用默认对象值填充 {@code null} 路径位置并继续遍历，
	 * 而不是抛出 {@link NullValueInNestedPathException}。
	 * <p>普通 accessor 上的默认值为 {@code false}。
	 * @since 4.1
	 */
	void setAutoGrowNestedPaths(boolean autoGrowNestedPaths);

	/**
	 * 返回嵌套路径的“自动生长”是否已启用。
	 * @since 4.1
	 */
	boolean isAutoGrowNestedPaths();

}
