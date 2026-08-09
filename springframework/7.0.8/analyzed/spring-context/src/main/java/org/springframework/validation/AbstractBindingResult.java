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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.PropertyEditorRegistry;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * {@link BindingResult} 接口及其超接口 {@link Errors} 的抽象实现。
 * 封装 {@link ObjectError ObjectErrors} 与 {@link FieldError FieldErrors} 的通用管理。
 *
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @since 2.0
 * @see Errors
 */
@SuppressWarnings("serial")
public abstract class AbstractBindingResult extends AbstractErrors implements BindingResult, Serializable {

	private final String objectName;

	private MessageCodesResolver messageCodesResolver = new DefaultMessageCodesResolver();

	private final List<ObjectError> errors = new ArrayList<>();

	private final Map<String, Class<?>> fieldTypes = new HashMap<>();

	private final Map<String, Object> fieldValues = new HashMap<>();

	private final Set<String> suppressedFields = new HashSet<>();


	/**
	 * 创建新的 AbstractBindingResult 实例。
	 * @param objectName 目标对象名称
	 * @see DefaultMessageCodesResolver
	 */
	protected AbstractBindingResult(String objectName) {
		this.objectName = objectName;
	}


	/**
	 * 设置将错误解析为消息代码的策略。
	 * 默认为 DefaultMessageCodesResolver。
	 * @see DefaultMessageCodesResolver
	 */
	public void setMessageCodesResolver(MessageCodesResolver messageCodesResolver) {
		Assert.notNull(messageCodesResolver, "MessageCodesResolver must not be null");
		this.messageCodesResolver = messageCodesResolver;
	}

	/**
	 * 返回将错误解析为消息代码的策略。
	 */
	public MessageCodesResolver getMessageCodesResolver() {
		return this.messageCodesResolver;
	}


	//---------------------------------------------------------------------
	// Implementation of the Errors interface
	//---------------------------------------------------------------------

	@Override
	public String getObjectName() {
		return this.objectName;
	}

	@Override
	public void reject(String errorCode, Object @Nullable [] errorArgs, @Nullable String defaultMessage) {
		addError(new ObjectError(getObjectName(), resolveMessageCodes(errorCode), errorArgs, defaultMessage));
	}

	@Override
	public void rejectValue(@Nullable String field, String errorCode,
			Object @Nullable [] errorArgs, @Nullable String defaultMessage) {

		if (!StringUtils.hasLength(getNestedPath()) && !StringUtils.hasLength(field)) {
			// We're at the top of the nested object hierarchy,
			// so the present level is not a field but rather the top object.
			// The best we can do is register a global error here...
			reject(errorCode, errorArgs, defaultMessage);
			return;
		}

		String fixedField = fixedField(field);
		Object newVal = getActualFieldValue(fixedField);
		FieldError fe = new FieldError(getObjectName(), fixedField, newVal, false,
				resolveMessageCodes(errorCode, field), errorArgs, defaultMessage);
		addError(fe);
	}

	@Override
	public void addAllErrors(Errors errors) {
		if (!errors.getObjectName().equals(getObjectName())) {
			throw new IllegalArgumentException("Errors object needs to have same object name");
		}
		this.errors.addAll(errors.getAllErrors());
	}

	@Override
	public boolean hasErrors() {
		return !this.errors.isEmpty();
	}

	@Override
	public int getErrorCount() {
		return this.errors.size();
	}

	@Override
	public List<ObjectError> getAllErrors() {
		return Collections.unmodifiableList(this.errors);
	}

	@Override
	public List<ObjectError> getGlobalErrors() {
		List<ObjectError> result = new ArrayList<>();
		for (ObjectError objectError : this.errors) {
			if (!(objectError instanceof FieldError)) {
				result.add(objectError);
			}
		}
		return Collections.unmodifiableList(result);
	}

	@Override
	public @Nullable ObjectError getGlobalError() {
		for (ObjectError objectError : this.errors) {
			if (!(objectError instanceof FieldError)) {
				return objectError;
			}
		}
		return null;
	}

	@Override
	public List<FieldError> getFieldErrors() {
		List<FieldError> result = new ArrayList<>();
		for (ObjectError objectError : this.errors) {
			if (objectError instanceof FieldError fieldError) {
				result.add(fieldError);
			}
		}
		return Collections.unmodifiableList(result);
	}

	@Override
	public @Nullable FieldError getFieldError() {
		for (ObjectError objectError : this.errors) {
			if (objectError instanceof FieldError fieldError) {
				return fieldError;
			}
		}
		return null;
	}

	@Override
	public List<FieldError> getFieldErrors(String field) {
		List<FieldError> result = new ArrayList<>();
		String fixedField = fixedField(field);
		for (ObjectError objectError : this.errors) {
			if (objectError instanceof FieldError fieldError && isMatchingFieldError(fixedField, fieldError)) {
				result.add(fieldError);
			}
		}
		return Collections.unmodifiableList(result);
	}

	@Override
	public @Nullable FieldError getFieldError(String field) {
		String fixedField = fixedField(field);
		for (ObjectError objectError : this.errors) {
			if (objectError instanceof FieldError fieldError && isMatchingFieldError(fixedField, fieldError)) {
				return fieldError;
			}
		}
		return null;
	}

	@Override
	public @Nullable Object getFieldValue(String field) {
		FieldError fieldError = getFieldError(field);
		// Use rejected value in case of error, current field value otherwise.
		if (fieldError != null) {
			Object value = fieldError.getRejectedValue();
			// Do not apply formatting on binding failures like type mismatches.
			return (fieldError.isBindingFailure() || getTarget() == null ? value : formatFieldValue(field, value));
		}
		else if (getTarget() != null) {
			Object value = getActualFieldValue(fixedField(field));
			return formatFieldValue(field, value);
		}
		else {
			return this.fieldValues.get(field);
		}
	}

	/**
	 * 本默认实现基于实际字段值（若有）确定类型。
	 * 子类应覆盖此方法，从描述符确定类型，即使值为 {@code null}。
	 * @see #getActualFieldValue
	 */
	@Override
	public @Nullable Class<?> getFieldType(@Nullable String field) {
		if (getTarget() != null) {
			Object value = getActualFieldValue(fixedField(field));
			if (value != null) {
				return value.getClass();
			}
		}
		return this.fieldTypes.get(field);
	}


	//---------------------------------------------------------------------
	// Implementation of BindingResult interface
	//---------------------------------------------------------------------

	/**
	 * 返回所获状态的模型 Map，将 Errors 实例暴露为
	 * '{@link #MODEL_KEY_PREFIX MODEL_KEY_PREFIX} + objectName' 以及对象本身。
	 * <p>注意，每次调用本方法都会构造 Map。
	 * 向 Map 添加内容后再重新调用本方法无效。
	 * <p>本方法返回的模型 Map 中的属性通常包含在使用 Spring bind 标签的
	 * 表单视图的 ModelAndView 中，该标签需要访问 Errors 实例。
	 * @see #getObjectName
	 * @see #MODEL_KEY_PREFIX
	 */
	@Override
	public Map<String, Object> getModel() {
		Map<String, Object> model = new LinkedHashMap<>(2);
		// Mapping from name to target object.
		model.put(getObjectName(), getTarget());
		// Errors instance, even if no errors.
		model.put(MODEL_KEY_PREFIX + getObjectName(), this);
		return model;
	}

	@Override
	public @Nullable Object getRawFieldValue(String field) {
		return (getTarget() != null ? getActualFieldValue(fixedField(field)) : null);
	}

	/**
	 * 本实现委托 {@link #getPropertyEditorRegistry() PropertyEditorRegistry}
	 * 的编辑器查找功能（若可用）。
	 */
	@Override
	public @Nullable PropertyEditor findEditor(@Nullable String field, @Nullable Class<?> valueType) {
		PropertyEditorRegistry editorRegistry = getPropertyEditorRegistry();
		if (editorRegistry != null) {
			Class<?> valueTypeToUse = valueType;
			if (valueTypeToUse == null) {
				valueTypeToUse = getFieldType(field);
			}
			return editorRegistry.findCustomEditor(valueTypeToUse, fixedField(field));
		}
		else {
			return null;
		}
	}

	/**
	 * 本实现返回 {@code null}。
	 */
	@Override
	public @Nullable PropertyEditorRegistry getPropertyEditorRegistry() {
		return null;
	}

	@Override
	public String[] resolveMessageCodes(String errorCode) {
		return getMessageCodesResolver().resolveMessageCodes(errorCode, getObjectName());
	}

	@Override
	public String[] resolveMessageCodes(String errorCode, @Nullable String field) {
		return getMessageCodesResolver().resolveMessageCodes(
				errorCode, getObjectName(), fixedField(field), getFieldType(field));
	}

	@Override
	public void addError(ObjectError error) {
		this.errors.add(error);
	}

	@Override
	public void recordFieldValue(String field, Class<?> type, @Nullable Object value) {
		this.fieldTypes.put(field, type);
		this.fieldValues.put(field, value);
	}

	/**
	 * 将指定不允许的字段标记为已抑制。
	 * <p>数据绑定器对每个检测到指向不允许字段的字段值调用此方法。
	 * @see DataBinder#setAllowedFields
	 */
	@Override
	public void recordSuppressedField(String field) {
		this.suppressedFields.add(field);
	}

	/**
	 * 返回绑定过程中被抑制的字段列表。
	 * <p>可用于判断是否有字段值指向不允许的字段。
	 * @see DataBinder#setAllowedFields
	 */
	@Override
	public String[] getSuppressedFields() {
		return StringUtils.toStringArray(this.suppressedFields);
	}


	@Override
	public boolean equals(@Nullable Object other) {
		return (this == other || (other instanceof BindingResult that &&
				getObjectName().equals(that.getObjectName()) &&
				ObjectUtils.nullSafeEquals(getTarget(), that.getTarget()) &&
				getAllErrors().equals(that.getAllErrors())));
	}

	@Override
	public int hashCode() {
		return getObjectName().hashCode();
	}


	//---------------------------------------------------------------------
	// Template methods to be implemented/overridden by subclasses
	//---------------------------------------------------------------------

	/**
	 * 返回包装的目标对象。
	 */
	@Override
	public abstract @Nullable Object getTarget();

	/**
	 * 提取给定字段的实际字段值。
	 * @param field 要检查的字段
	 * @return 字段的当前值
	 */
	protected abstract @Nullable Object getActualFieldValue(String field);

	/**
	 * 格式化给定字段的给定值。
	 * <p>默认实现直接返回字段值。
	 * @param field 要检查的字段
	 * @param value 字段值（非绑定错误的拒绝值或实际字段值）
	 * @return 格式化后的值
	 */
	protected @Nullable Object formatFieldValue(String field, @Nullable Object value) {
		return value;
	}

}
