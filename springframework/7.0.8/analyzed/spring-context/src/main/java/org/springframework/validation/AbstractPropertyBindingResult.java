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

import org.jspecify.annotations.Nullable;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.ConfigurablePropertyAccessor;
import org.springframework.beans.PropertyAccessorUtils;
import org.springframework.beans.PropertyEditorRegistry;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.support.ConvertingPropertyEditorAdapter;
import org.springframework.util.Assert;

/**
 * 基于 Spring {@link org.springframework.beans.PropertyAccessor} 机制的
 * {@link BindingResult} 实现抽象基类。
 * 通过委托对应 PropertyAccessor 方法预实现字段访问。
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see #getPropertyAccessor()
 * @see org.springframework.beans.PropertyAccessor
 * @see org.springframework.beans.ConfigurablePropertyAccessor
 */
@SuppressWarnings("serial")
public abstract class AbstractPropertyBindingResult extends AbstractBindingResult {

	private transient @Nullable ConversionService conversionService;


	/**
	 * 创建新的 AbstractPropertyBindingResult 实例。
	 * @param objectName 目标对象名称
	 * @see DefaultMessageCodesResolver
	 */
	protected AbstractPropertyBindingResult(String objectName) {
		super(objectName);
	}


	public void initConversion(ConversionService conversionService) {
		Assert.notNull(conversionService, "ConversionService must not be null");
		this.conversionService = conversionService;
		if (getTarget() != null) {
			getPropertyAccessor().setConversionService(conversionService);
		}
	}

	/**
	 * 返回底层 PropertyAccessor。
	 * @see #getPropertyAccessor()
	 */
	@Override
	public @Nullable PropertyEditorRegistry getPropertyEditorRegistry() {
		return (getTarget() != null ? getPropertyAccessor() : null);
	}

	/**
	 * 返回规范属性名。
	 * @see org.springframework.beans.PropertyAccessorUtils#canonicalPropertyName
	 */
	@Override
	protected String canonicalFieldName(String field) {
		return PropertyAccessorUtils.canonicalPropertyName(field);
	}

	/**
	 * 根据属性类型确定字段类型。
	 * @see #getPropertyAccessor()
	 */
	@Override
	public @Nullable Class<?> getFieldType(@Nullable String field) {
		return (getTarget() != null ? getPropertyAccessor().getPropertyType(fixedField(field)) :
				super.getFieldType(field));
	}

	/**
	 * 从 PropertyAccessor 获取字段值。
	 * @see #getPropertyAccessor()
	 */
	@Override
	protected @Nullable Object getActualFieldValue(String field) {
		return getPropertyAccessor().getPropertyValue(field);
	}

	/**
	 * 基于已注册的 PropertyEditor 格式化字段值。
	 * @see #getCustomEditor
	 */
	@Override
	protected @Nullable Object formatFieldValue(String field, @Nullable Object value) {
		String fixedField = fixedField(field);
		// Try custom editor...
		PropertyEditor customEditor = getCustomEditor(fixedField);
		if (customEditor != null) {
			customEditor.setValue(value);
			String textValue = customEditor.getAsText();
			// If the PropertyEditor returned null, there is no appropriate
			// text representation for this value: only use it if non-null.
			if (textValue != null) {
				return textValue;
			}
		}
		if (this.conversionService != null) {
			// Try custom converter...
			TypeDescriptor fieldDesc = getPropertyAccessor().getPropertyTypeDescriptor(fixedField);
			TypeDescriptor strDesc = TypeDescriptor.valueOf(String.class);
			if (fieldDesc != null && this.conversionService.canConvert(fieldDesc, strDesc)) {
				return this.conversionService.convert(value, fieldDesc, strDesc);
			}
		}
		return value;
	}

	/**
	 * 获取给定字段的自定义 PropertyEditor（若有）。
	 * @param fixedField 完全限定字段名
	 * @return 自定义 PropertyEditor，或 {@code null}
	 */
	protected @Nullable PropertyEditor getCustomEditor(String fixedField) {
		Class<?> targetType = getPropertyAccessor().getPropertyType(fixedField);
		PropertyEditor editor = getPropertyAccessor().findCustomEditor(targetType, fixedField);
		if (editor == null) {
			editor = BeanUtils.findEditorByConvention(targetType);
		}
		return editor;
	}

	/**
	 * 本实现会在适用时暴露 Formatter 对应的 PropertyEditor 适配器。
	 */
	@Override
	public @Nullable PropertyEditor findEditor(@Nullable String field, @Nullable Class<?> valueType) {
		Class<?> valueTypeForLookup = valueType;
		if (valueTypeForLookup == null) {
			valueTypeForLookup = getFieldType(field);
		}
		PropertyEditor editor = super.findEditor(field, valueTypeForLookup);
		if (editor == null && this.conversionService != null) {
			TypeDescriptor td = null;
			if (field != null && getTarget() != null) {
				TypeDescriptor ptd = getPropertyAccessor().getPropertyTypeDescriptor(fixedField(field));
				if (ptd != null && (valueType == null || valueType.isAssignableFrom(ptd.getType()))) {
					td = ptd;
				}
			}
			if (td == null) {
				td = TypeDescriptor.valueOf(valueTypeForLookup);
			}
			if (this.conversionService.canConvert(TypeDescriptor.valueOf(String.class), td)) {
				editor = new ConvertingPropertyEditorAdapter(this.conversionService, td);
			}
		}
		return editor;
	}


	/**
	 * 按具体访问策略提供要使用的 PropertyAccessor。
	 * <p>注意：BindingResult 使用的 PropertyAccessor 默认应将
	 * {@code extractOldValueForEditor} 标志设为 {@code true}，
	 * 因为作为数据绑定目标的模型对象通常可无副作用地提取旧值。
	 * @see ConfigurablePropertyAccessor#setExtractOldValueForEditor
	 */
	public abstract ConfigurablePropertyAccessor getPropertyAccessor();

}
