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

package org.springframework.validation;

import java.beans.PropertyEditor;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.PropertyEditorRegistry;

/**
 * 表示绑定结果的通用接口。扩展 {@link Errors} 接口以支持错误注册，
 * 便于应用 {@link Validator}，并增加绑定相关的分析与模型构建能力。
 *
 * <p>作为 {@link DataBinder} 的结果持有者，
 * 通过 {@link DataBinder#getBindingResult()} 获取。
 * BindingResult 实现也可直接使用，例如对其调用 {@link Validator}（如单元测试中）。
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see DataBinder
 * @see Errors
 * @see Validator
 * @see BeanPropertyBindingResult
 * @see DirectFieldBindingResult
 * @see MapBindingResult
 */
public interface BindingResult extends Errors {

	/**
	 * 模型中 BindingResult 实例名称的前缀，后接对象名。
	 */
	String MODEL_KEY_PREFIX = BindingResult.class.getName() + ".";


	/**
	 * 返回被包装的目标对象，可能是 bean、含 public 字段的对象或 Map，
	 * 取决于具体绑定策略。
	 */
	@Nullable Object getTarget();

	/**
	 * 返回当前状态的 model Map，以
	 * '{@link #MODEL_KEY_PREFIX MODEL_KEY_PREFIX} + objectName' 暴露 BindingResult 实例，
	 * 以 'objectName' 暴露对象本身。
	 * <p>注意：每次调用本方法都会重新构造 Map；
	 * 向 Map 添加内容后再调用本方法不会生效。
	 * <p>本方法返回的 model Map 属性通常包含在
	 * {@link org.springframework.web.servlet.ModelAndView} 中，
	 * 供 JSP 中使用 Spring {@code bind} 标签的表单视图访问 BindingResult。
	 * Spring 预置表单控制器在渲染表单视图时会自动处理；
	 * 自行构建 ModelAndView 时需包含本方法返回 model Map 中的属性。
	 * @see #getObjectName()
	 * @see #MODEL_KEY_PREFIX
	 * @see org.springframework.web.servlet.ModelAndView
	 * @see org.springframework.web.servlet.tags.BindTag
	 */
	Map<String, Object> getModel();

	/**
	 * 提取给定字段的原始字段值，通常用于比较。
	 * @param field 要检查的字段
	 * @return 字段当前原始值，未知时返回 {@code null}
	 */
	@Nullable Object getRawFieldValue(String field);

	/**
	 * 查找给定类型与属性的自定义属性编辑器。
	 * @param field 属性路径（名称或嵌套路径），
	 * 若查找给定类型所有属性的编辑器则为 {@code null}
	 * @param valueType 属性类型（若已给定属性可为 {@code null}，
	 * 但为一致性检查仍应指定）
	 * @return 已注册的编辑器，无则返回 {@code null}
	 */
	@Nullable PropertyEditor findEditor(@Nullable String field, @Nullable Class<?> valueType);

	/**
	 * 返回底层 PropertyEditorRegistry。
	 * @return PropertyEditorRegistry，本 BindingResult 无可用实例时返回 {@code null}
	 */
	@Nullable PropertyEditorRegistry getPropertyEditorRegistry();

	/**
	 * 将给定错误码解析为消息码。
	 * <p>以适当参数调用已配置的 {@link MessageCodesResolver}。
	 * @param errorCode 要解析为消息码的错误码
	 * @return 解析后的消息码
	 */
	String[] resolveMessageCodes(String errorCode);

	/**
	 * 将给定错误码解析为指定字段的消息码。
	 * <p>以适当参数调用已配置的 {@link MessageCodesResolver}。
	 * @param errorCode 要解析为消息码的错误码
	 * @param field 要解析消息码的字段
	 * @return 解析后的消息码
	 */
	String[] resolveMessageCodes(String errorCode, String field);

	/**
	 * 向错误列表添加自定义 {@link ObjectError} 或 {@link FieldError}。
	 * <p>供 {@link BindingErrorProcessor} 等协作策略使用。
	 * @see ObjectError
	 * @see FieldError
	 * @see BindingErrorProcessor
	 */
	void addError(ObjectError error);

	/**
	 * 记录指定字段的给定值。
	 * <p>在无法构造目标对象时使用，使原始字段值可通过 {@link #getFieldValue} 获取。
	 * 若已注册错误，每个受影响字段将暴露被拒绝的值。
	 * @param field 要记录值的字段
	 * @param type 字段类型
	 * @param value 原始值
	 * @since 5.0.4
	 */
	default void recordFieldValue(String field, Class<?> type, @Nullable Object value) {
	}

	/**
	 * 将指定的不允许字段标记为已抑制。
	 * <p>数据绑定器对每个检测到指向不允许字段的字段值调用本方法。
	 * @see DataBinder#setAllowedFields
	 */
	default void recordSuppressedField(String field) {
	}

	/**
	 * 返回绑定过程中被抑制的字段列表。
	 * <p>可用于判断是否有字段值指向了不允许的字段。
	 * @see DataBinder#setAllowedFields
	 */
	default String[] getSuppressedFields() {
		return new String[0];
	}

}
