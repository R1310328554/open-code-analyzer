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

import java.lang.reflect.Field;
import java.lang.reflect.InaccessibleObjectException;
import java.util.HashMap;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import org.springframework.core.ResolvableType;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.util.ReflectionUtils;

/**
 * 直接访问实例字段的 {@link ConfigurablePropertyAccessor} 实现。
 * 允许绕过 JavaBean setter，直接绑定到字段。
 *
 * <p>自 Spring 4.2 起，绝大多数 {@link BeanWrapper} 能力已合并到
 * {@link AbstractPropertyAccessor}，因此本类同样支持属性遍历以及对
 * 集合与 Map 的访问。
 *
 * <p>DirectFieldAccessor 上 {@code extractOldValueForEditor} 的默认值为
 * {@code true}，因为字段总是可以无副作用地读取。
 *
 * @author Juergen Hoeller
 * @author Stephane Nicoll
 * @since 2.0
 * @see #setExtractOldValueForEditor
 * @see BeanWrapper
 * @see org.springframework.validation.DirectFieldBindingResult
 * @see org.springframework.validation.DataBinder#initDirectFieldAccess()
 */
public class DirectFieldAccessor extends AbstractNestablePropertyAccessor {

	/** 属性名到字段处理器的缓存。 */
	private final Map<String, FieldPropertyHandler> fieldMap = new HashMap<>();


	/**
	 * 为给定对象创建 DirectFieldAccessor。
	 * @param object 由本 DirectFieldAccessor 包装的对象
	 */
	public DirectFieldAccessor(Object object) {
		super(object);
	}

	/**
	 * 为给定对象创建 DirectFieldAccessor，并登记该对象所处的嵌套路径。
	 * @param object 由本 DirectFieldAccessor 包装的对象
	 * @param nestedPath 该对象的嵌套路径
	 * @param parent 包含本访问器的父 DirectFieldAccessor（不得为 {@code null}）
	 */
	protected DirectFieldAccessor(Object object, String nestedPath, DirectFieldAccessor parent) {
		super(object, nestedPath, parent);
	}


	/**
	 * 按属性名查找本地字段处理器；首次命中时会解析字段并缓存。
	 */
	@Override
	protected @Nullable PropertyHandler getLocalPropertyHandler(String propertyName) {
		FieldPropertyHandler propertyHandler = this.fieldMap.get(propertyName);
		if (propertyHandler == null) {
			Field field = ReflectionUtils.findField(getWrappedClass(), propertyName);
			if (field != null) {
				propertyHandler = new FieldPropertyHandler(field);
				this.fieldMap.put(propertyName, propertyHandler);
			}
		}
		return propertyHandler;
	}

	/**
	 * 为嵌套对象创建新的 DirectFieldAccessor。
	 */
	@Override
	protected DirectFieldAccessor newNestedPropertyAccessor(Object object, String nestedPath) {
		return new DirectFieldAccessor(object, nestedPath, this);
	}

	/**
	 * 创建“属性不可写”异常，并附带可能的拼写相近字段名提示。
	 */
	@Override
	protected NotWritablePropertyException createNotWritablePropertyException(String propertyName) {
		PropertyMatches matches = PropertyMatches.forField(propertyName, getRootClass());
		throw new NotWritablePropertyException(getRootClass(), getNestedPath() + propertyName,
				matches.buildErrorMessage(), matches.getPossibleMatches());
	}


	/**
	 * 基于反射 Field 的属性处理器，负责字段读写与类型描述。
	 */
	private class FieldPropertyHandler extends PropertyHandler {

		/** 对应的字段。 */
		private final Field field;

		/** 该字段的可解析类型。 */
		private final ResolvableType resolvableType;

		/**
		 * 使用给定字段创建处理器（字段视为既可读又可写）。
		 */
		public FieldPropertyHandler(Field field) {
			super(field.getType(), true, true);
			this.field = field;
			this.resolvableType = ResolvableType.forField(this.field);
		}

		/**
		 * 返回该字段的 TypeDescriptor。
		 */
		@Override
		public TypeDescriptor toTypeDescriptor() {
			return new TypeDescriptor(this.resolvableType, this.field.getType(), this.field.getAnnotations());
		}

		/**
		 * 返回该字段的 ResolvableType。
		 */
		@Override
		public ResolvableType getResolvableType() {
			return this.resolvableType;
		}

		/**
		 * 返回嵌套层级上 Map 值类型的 TypeDescriptor。
		 */
		@Override
		public TypeDescriptor getMapValueType(int nestingLevel) {
			return new TypeDescriptor(this.resolvableType.getNested(nestingLevel).asMap().getGeneric(1),
					null, this.field.getAnnotations());
		}

		/**
		 * 返回嵌套层级上集合元素类型的 TypeDescriptor。
		 */
		@Override
		public TypeDescriptor getCollectionType(int nestingLevel) {
			return new TypeDescriptor(this.resolvableType.getNested(nestingLevel).asCollection().getGeneric(),
					null, this.field.getAnnotations());
		}

		/**
		 * 返回指定嵌套层级的 TypeDescriptor。
		 */
		@Override
		public @Nullable TypeDescriptor nested(int level) {
			return TypeDescriptor.nested(this.field, level);
		}

		/**
		 * 通过反射读取字段值。
		 */
		@Override
		public @Nullable Object getValue() throws Exception {
			try {
				ReflectionUtils.makeAccessible(this.field);
				return this.field.get(getWrappedInstance());
			}
			catch (IllegalAccessException | InaccessibleObjectException ex) {
				throw new InvalidPropertyException(getWrappedClass(),
						this.field.getName(), "Field is not accessible", ex);
			}
		}

		/**
		 * 通过反射写入字段值。
		 */
		@Override
		public void setValue(@Nullable Object value) throws Exception {
			try {
				ReflectionUtils.makeAccessible(this.field);
				this.field.set(getWrappedInstance(), value);
			}
			catch (IllegalAccessException | InaccessibleObjectException ex) {
				throw new InvalidPropertyException(getWrappedClass(), this.field.getName(),
						"Field is not accessible", ex);
			}
		}
	}

}
