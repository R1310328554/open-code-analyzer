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

import java.util.Map;

import org.jspecify.annotations.Nullable;

import org.springframework.core.convert.TypeDescriptor;

/**
 * 可按名称访问属性的类的通用接口
 * （例如对象的 bean 属性，或对象中的字段）。
 *
 * <p>作为 {@link BeanWrapper} 的基础接口。
 *
 * @author Juergen Hoeller
 * @since 1.1
 * @see BeanWrapper
 * @see PropertyAccessorFactory#forBeanPropertyAccess
 * @see PropertyAccessorFactory#forDirectFieldAccess
 */
public interface PropertyAccessor {

	/**
	 * 嵌套属性的路径分隔符。
	 * 遵循常规 Java 约定：{@code getFoo().getBar()} 对应 {@code "foo.bar"}。
	 */
	String NESTED_PROPERTY_SEPARATOR = ".";

	/**
	 * 嵌套属性的路径分隔符（字符形式）。
	 * 遵循常规 Java 约定：{@code getFoo().getBar()} 对应 {@code "foo.bar"}。
	 */
	char NESTED_PROPERTY_SEPARATOR_CHAR = '.';

	/**
	 * 索引或映射属性键的起始标记，例如 {@code "person.addresses[0]"}。
	 */
	String PROPERTY_KEY_PREFIX = "[";

	/**
	 * 索引或映射属性键的起始标记（字符形式），例如 {@code "person.addresses[0]"}。
	 */
	char PROPERTY_KEY_PREFIX_CHAR = '[';

	/**
	 * 索引或映射属性键的结束标记，例如 {@code "person.addresses[0]"}。
	 */
	String PROPERTY_KEY_SUFFIX = "]";

	/**
	 * 索引或映射属性键的结束标记（字符形式），例如 {@code "person.addresses[0]"}。
	 */
	char PROPERTY_KEY_SUFFIX_CHAR = ']';


	/**
	 * 判断指定属性是否可读。
	 * <p>若属性不存在，返回 {@code false}。
	 * @param propertyName 要检查的属性
	 * （可为嵌套路径和/或索引/映射属性）
	 * @return 属性是否可读
	 */
	boolean isReadableProperty(String propertyName);

	/**
	 * 判断指定属性是否可写。
	 * <p>若属性不存在，返回 {@code false}。
	 * @param propertyName 要检查的属性
	 * （可为嵌套路径和/或索引/映射属性）
	 * @return 属性是否可写
	 */
	boolean isWritableProperty(String propertyName);

	/**
	 * 确定指定属性的类型：
	 * 可检查属性描述符，也可在索引/映射元素情况下检查实际值。
	 * @param propertyName 要检查的属性
	 * （可为嵌套路径和/或索引/映射属性）
	 * @return 该属性的类型；若无法确定则为 {@code null}
	 * @throws PropertyAccessException 属性合法但访问器方法执行失败
	 */
	@Nullable Class<?> getPropertyType(String propertyName) throws BeansException;

	/**
	 * 返回指定属性的类型描述符：
	 * 优先来自读方法，否则回退到写方法。
	 * @param propertyName 要检查的属性
	 * （可为嵌套路径和/或索引/映射属性）
	 * @return 该属性的类型描述符；若无法确定则为 {@code null}
	 * @throws PropertyAccessException 属性合法但访问器方法执行失败
	 */
	@Nullable TypeDescriptor getPropertyTypeDescriptor(String propertyName) throws BeansException;

	/**
	 * 获取指定属性的当前值。
	 * @param propertyName 要读取的属性名
	 * （可为嵌套路径和/或索引/映射属性）
	 * @return 属性值
	 * @throws InvalidPropertyException 不存在该属性或属性不可读
	 * @throws PropertyAccessException 属性合法但访问器方法执行失败
	 */
	@Nullable Object getPropertyValue(String propertyName) throws BeansException;

	/**
	 * 将指定值设置为当前属性值。
	 * @param propertyName 要写入的属性名
	 * （可为嵌套路径和/或索引/映射属性）
	 * @param value 新值
	 * @throws InvalidPropertyException 不存在该属性或属性不可写
	 * @throws PropertyAccessException 属性合法但访问器方法执行失败，或发生类型不匹配
	 */
	void setPropertyValue(String propertyName, @Nullable Object value) throws BeansException;

	/**
	 * 将指定值设置为当前属性值。
	 * @param pv 包含新属性值的对象
	 * @throws InvalidPropertyException 不存在该属性或属性不可写
	 * @throws PropertyAccessException 属性合法但访问器方法执行失败，或发生类型不匹配
	 */
	void setPropertyValue(PropertyValue pv) throws BeansException;

	/**
	 * 基于 Map 执行批量更新。
	 * <p>基于 {@link PropertyValues} 的批量更新能力更强：本方法仅为便捷提供。
	 * 行为与 {@link #setPropertyValues(PropertyValues)} 完全相同。
	 * @param map 提供属性的 Map；以属性名为键，属性值为值
	 * @throws InvalidPropertyException 不存在该属性或属性不可写
	 * @throws PropertyBatchUpdateException 批量更新中一个或多个特定属性发生
	 * {@link PropertyAccessException}。该异常会打包所有单独的
	 * {@code PropertyAccessException}；其余属性应已成功更新。
	 */
	void setPropertyValues(Map<?, ?> map) throws BeansException;

	/**
	 * 执行批量更新的首选方式。
	 * <p>注意：批量更新与单次更新不同——本接口实现在遇到
	 * <b>可恢复</b>错误（例如类型不匹配，但<b>不包括</b>无效字段名等）时，
	 * 会继续更新其余属性，并抛出包含全部个别错误的
	 * {@link PropertyBatchUpdateException}。之后可检查该异常以查看所有绑定错误。
	 * 已成功更新的属性会保持变更后的状态。
	 * <p>不允许未知字段或无效字段。
	 * @param pvs 要设置到目标对象上的 {@link PropertyValues}
	 * @throws InvalidPropertyException 不存在该属性或属性不可写
	 * @throws PropertyBatchUpdateException 批量更新中一个或多个特定属性发生
	 * {@link PropertyAccessException}。该异常会打包所有单独的
	 * {@code PropertyAccessException}；其余属性应已成功更新。
	 * @see #setPropertyValues(PropertyValues, boolean, boolean)
	 */
	void setPropertyValues(PropertyValues pvs) throws BeansException;

	/**
	 * 以更多行为控制执行批量更新。
	 * <p>注意：批量更新与单次更新不同——本接口实现在遇到
	 * <b>可恢复</b>错误（例如类型不匹配，但<b>不包括</b>无效字段名等）时，
	 * 会继续更新其余属性，并抛出包含全部个别错误的
	 * {@link PropertyBatchUpdateException}。之后可检查该异常以查看所有绑定错误。
	 * 已成功更新的属性会保持变更后的状态。
	 * @param pvs 要设置到目标对象上的 {@link PropertyValues}
	 * @param ignoreUnknown 是否忽略未知属性（在 bean 中找不到）
	 * @throws InvalidPropertyException 不存在该属性或属性不可写
	 * @throws PropertyBatchUpdateException 批量更新中一个或多个特定属性发生
	 * {@link PropertyAccessException}。该异常会打包所有单独的
	 * {@code PropertyAccessException}；其余属性应已成功更新。
	 * @see #setPropertyValues(PropertyValues, boolean, boolean)
	 */
	void setPropertyValues(PropertyValues pvs, boolean ignoreUnknown)
			throws BeansException;

	/**
	 * 以完全行为控制执行批量更新。
	 * <p>注意：批量更新与单次更新不同——本接口实现在遇到
	 * <b>可恢复</b>错误（例如类型不匹配，但<b>不包括</b>无效字段名等）时，
	 * 会继续更新其余属性，并抛出包含全部个别错误的
	 * {@link PropertyBatchUpdateException}。之后可检查该异常以查看所有绑定错误。
	 * 已成功更新的属性会保持变更后的状态。
	 * @param pvs 要设置到目标对象上的 {@link PropertyValues}
	 * @param ignoreUnknown 是否忽略未知属性（在 bean 中找不到）
	 * @param ignoreInvalid 是否忽略无效属性（找到但不可访问）
	 * @throws InvalidPropertyException 不存在该属性或属性不可写
	 * @throws PropertyBatchUpdateException 批量更新中一个或多个特定属性发生
	 * {@link PropertyAccessException}。该异常会打包所有单独的
	 * {@code PropertyAccessException}；其余属性应已成功更新。
	 */
	void setPropertyValues(PropertyValues pvs, boolean ignoreUnknown, boolean ignoreInvalid)
			throws BeansException;

}
