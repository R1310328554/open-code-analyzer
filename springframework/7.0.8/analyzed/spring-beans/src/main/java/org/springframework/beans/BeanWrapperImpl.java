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

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;

import org.springframework.core.MethodParameter;
import org.springframework.core.ResolvableType;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

/**
 * 默认的 {@link BeanWrapper} 实现，对常见用法通常已足够。
 * 会缓存内省结果以提高效率。
 *
 * <p>说明：会自动注册 {@code org.springframework.beans.propertyeditors} 包中的
 * 默认属性编辑器，它们与 JDK 标准 PropertyEditor 一并生效。应用可调用
 * {@link #registerCustomEditor(Class, java.beans.PropertyEditor)}
 * 为某一实例注册编辑器（即不在整个应用中共享）。细节见基类
 * {@link PropertyEditorRegistrySupport}。
 *
 * <p><b>注意：自 Spring 2.5 起，就几乎所有用途而言，本类都是内部类。</b>
 * 之所以保持 public，只是为了让其他框架包能够访问。应用侧标准访问请改用
 * {@link PropertyAccessorFactory#forBeanPropertyAccess} 工厂方法。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @author Stephane Nicoll
 * @since 15 April 2001
 * @see #registerCustomEditor
 * @see #setPropertyValues
 * @see #setPropertyValue
 * @see #getPropertyValue
 * @see #getPropertyType
 * @see BeanWrapper
 * @see PropertyEditorRegistrySupport
 */
public class BeanWrapperImpl extends AbstractNestablePropertyAccessor implements BeanWrapper {

	/**
	 * 本对象的缓存内省结果，避免每次都付出 JavaBeans 内省的开销。
	 */
	private @Nullable CachedIntrospectionResults cachedIntrospectionResults;


	/**
	 * 创建一个空的 BeanWrapperImpl。随后需设置被包装实例。
	 * 会注册默认编辑器。
	 * @see #setWrappedInstance
	 */
	public BeanWrapperImpl() {
		this(true);
	}

	/**
	 * 创建一个空的 BeanWrapperImpl。随后需设置被包装实例。
	 * @param registerDefaultEditors 是否注册默认编辑器
	 * （若 BeanWrapper 完全不需要类型转换，可设为 false 以跳过）
	 * @see #setWrappedInstance
	 */
	public BeanWrapperImpl(boolean registerDefaultEditors) {
		super(registerDefaultEditors);
	}

	/**
	 * 为给定对象创建 BeanWrapperImpl。
	 * @param object 由本 BeanWrapper 包装的对象
	 */
	public BeanWrapperImpl(Object object) {
		super(object);
	}

	/**
	 * 创建 BeanWrapperImpl，并包装指定类的一个新实例。
	 * @param clazz 要实例化并包装的类
	 */
	public BeanWrapperImpl(Class<?> clazz) {
		super(clazz);
	}

	/**
	 * 为给定对象创建 BeanWrapperImpl，并登记该对象所处的嵌套路径。
	 * @param object 由本 BeanWrapper 包装的对象
	 * @param nestedPath 该对象的嵌套路径
	 * @param rootObject 路径顶端的根对象
	 */
	public BeanWrapperImpl(Object object, String nestedPath, Object rootObject) {
		super(object, nestedPath, rootObject);
	}

	/**
	 * 为给定对象创建 BeanWrapperImpl，并登记该对象所处的嵌套路径。
	 * @param object 由本 BeanWrapper 包装的对象
	 * @param nestedPath 该对象的嵌套路径
	 * @param parent 包含本包装器的父 BeanWrapper（不得为 {@code null}）
	 */
	private BeanWrapperImpl(Object object, String nestedPath, BeanWrapperImpl parent) {
		super(object, nestedPath, parent);
	}


	/**
	 * 设置要持有的 bean 实例，不对 {@link java.util.Optional} 做任何解包。
	 * @param object 实际目标对象
	 * @since 4.3
	 * @see #setWrappedInstance(Object)
	 */
	public void setBeanInstance(Object object) {
		this.wrappedObject = object;
		this.rootObject = object;
		this.typeConverterDelegate = new TypeConverterDelegate(this, this.wrappedObject);
		setIntrospectionClass(object.getClass());
	}

	/**
	 * 设置被包装实例，并同步更新内省所用的目标类。
	 */
	@Override
	public void setWrappedInstance(Object object, @Nullable String nestedPath, @Nullable Object rootObject) {
		super.setWrappedInstance(object, nestedPath, rootObject);
		setIntrospectionClass(getWrappedClass());
	}

	/**
	 * 设置要进行内省的类。
	 * 目标对象变更时需要调用。
	 * @param clazz 要内省的类
	 */
	protected void setIntrospectionClass(Class<?> clazz) {
		if (this.cachedIntrospectionResults != null && this.cachedIntrospectionResults.getBeanClass() != clazz) {
			this.cachedIntrospectionResults = null;
		}
	}

	/**
	 * 获取针对被包装对象的、惰性初始化的 CachedIntrospectionResults。
	 */
	private CachedIntrospectionResults getCachedIntrospectionResults() {
		if (this.cachedIntrospectionResults == null) {
			this.cachedIntrospectionResults = CachedIntrospectionResults.forClass(getWrappedClass());
		}
		return this.cachedIntrospectionResults;
	}


	/**
	 * 将给定值转换为指定属性所需的类型。
	 * <p>本方法仅供 BeanFactory 内部优化使用。
	 * 程序化转换请使用 {@code convertIfNecessary} 系列方法。
	 * @param value 要转换的值
	 * @param propertyName 目标属性
	 * （此处不支持嵌套或索引属性）
	 * @return 新值，可能已经过类型转换
	 * @throws TypeMismatchException 若类型转换失败
	 */
	public @Nullable Object convertForProperty(@Nullable Object value, String propertyName) throws TypeMismatchException {
		CachedIntrospectionResults cachedIntrospectionResults = getCachedIntrospectionResults();
		PropertyDescriptor pd = cachedIntrospectionResults.getPropertyDescriptor(propertyName);
		if (pd == null) {
			throw new InvalidPropertyException(getRootClass(), getNestedPath() + propertyName,
					"No property '" + propertyName + "' found");
		}
		TypeDescriptor td = ((GenericTypeAwarePropertyDescriptor) pd).getTypeDescriptor();
		return convertForProperty(propertyName, null, value, td);
	}

	/**
	 * 返回当前包装类上指定属性名对应的本地 PropertyHandler。
	 */
	@Override
	protected @Nullable PropertyHandler getLocalPropertyHandler(String propertyName) {
		PropertyDescriptor pd = getCachedIntrospectionResults().getPropertyDescriptor(propertyName);
		return (pd != null ? new BeanPropertyHandler((GenericTypeAwarePropertyDescriptor) pd) : null);
	}

	/**
	 * 为嵌套对象创建新的 BeanWrapperImpl。
	 */
	@Override
	protected BeanWrapperImpl newNestedPropertyAccessor(Object object, String nestedPath) {
		return new BeanWrapperImpl(object, nestedPath, this);
	}

	/**
	 * 创建“属性不可写”异常，并附带可能的拼写相近属性名提示。
	 */
	@Override
	protected NotWritablePropertyException createNotWritablePropertyException(String propertyName) {
		PropertyMatches matches = PropertyMatches.forProperty(propertyName, getRootClass());
		throw new NotWritablePropertyException(getRootClass(), getNestedPath() + propertyName,
				matches.buildErrorMessage(), matches.getPossibleMatches());
	}

	/**
	 * 返回被包装对象的全部 PropertyDescriptor。
	 */
	@Override
	public PropertyDescriptor[] getPropertyDescriptors() {
		return getCachedIntrospectionResults().getPropertyDescriptors();
	}

	/**
	 * 返回被包装对象上指定属性的 PropertyDescriptor（支持嵌套路径）。
	 */
	@Override
	public PropertyDescriptor getPropertyDescriptor(String propertyName) throws InvalidPropertyException {
		BeanWrapperImpl nestedBw = (BeanWrapperImpl) getPropertyAccessorForPropertyPath(propertyName);
		String finalPath = getFinalPath(nestedBw, propertyName);
		PropertyDescriptor pd = nestedBw.getCachedIntrospectionResults().getPropertyDescriptor(finalPath);
		if (pd == null) {
			throw new InvalidPropertyException(getRootClass(), getNestedPath() + propertyName,
					"No property '" + propertyName + "' found");
		}
		return pd;
	}


	/**
	 * 基于 JavaBeans PropertyDescriptor 的属性处理器，负责读写与类型描述。
	 */
	private class BeanPropertyHandler extends PropertyHandler {

		/** 对应的泛型感知属性描述符。 */
		private final GenericTypeAwarePropertyDescriptor pd;

		/**
		 * 使用给定属性描述符创建处理器。
		 */
		public BeanPropertyHandler(GenericTypeAwarePropertyDescriptor pd) {
			super(pd.getPropertyType(), pd.getReadMethod() != null, pd.getWriteMethod() != null);
			this.pd = pd;
		}

		/**
		 * 返回该属性的 TypeDescriptor。
		 */
		@Override
		public TypeDescriptor toTypeDescriptor() {
			return this.pd.getTypeDescriptor();
		}

		/**
		 * 返回读方法对应的 ResolvableType。
		 */
		@Override
		public ResolvableType getResolvableType() {
			return this.pd.getReadMethodType();
		}

		/**
		 * 返回嵌套层级上 Map 值类型的 TypeDescriptor。
		 */
		@Override
		public TypeDescriptor getMapValueType(int nestingLevel) {
			return new TypeDescriptor(
					this.pd.getReadMethodType().getNested(nestingLevel).asMap().getGeneric(1),
					null, this.pd.getTypeDescriptor().getAnnotations());
		}

		/**
		 * 返回嵌套层级上集合元素类型的 TypeDescriptor。
		 */
		@Override
		public TypeDescriptor getCollectionType(int nestingLevel) {
			return new TypeDescriptor(
					this.pd.getReadMethodType().getNested(nestingLevel).asCollection().getGeneric(),
					null, this.pd.getTypeDescriptor().getAnnotations());
		}

		/**
		 * 返回指定嵌套层级的 TypeDescriptor。
		 */
		@Override
		public @Nullable TypeDescriptor nested(int level) {
			return this.pd.getTypeDescriptor().nested(level);
		}

		/**
		 * 通过读方法获取属性值。
		 */
		@Override
		public @Nullable Object getValue() throws Exception {
			Method readMethod = this.pd.getReadMethod();
			Assert.state(readMethod != null, "No read method available");
			ReflectionUtils.makeAccessible(readMethod);
			return readMethod.invoke(getWrappedInstance(), (Object[]) null);
		}

		/**
		 * 通过写方法设置属性值。
		 */
		@Override
		public void setValue(@Nullable Object value) throws Exception {
			Method writeMethod = this.pd.getWriteMethodForActualAccess();
			ReflectionUtils.makeAccessible(writeMethod);
			writeMethod.invoke(getWrappedInstance(), value);
		}

		/**
		 * 在首选写方法不可用时，尝试回退写方法（必要时先做类型转换）。
		 * @return 若成功写入则返回 {@code true}
		 */
		@Override
		public boolean setValueFallbackIfPossible(@Nullable Object value) {
			try {
				Method writeMethod = this.pd.getWriteMethodFallback(value != null ? value.getClass() : null);
				if (writeMethod == null) {
					writeMethod = this.pd.getUniqueWriteMethodFallback();
					if (writeMethod != null) {
						// 这里需要转换：否则上面按类型匹配的 getWriteMethodFallback 本就会直接返回该方法
						value = convertForProperty(this.pd.getName(), null, value,
								new TypeDescriptor(new MethodParameter(writeMethod, 0)));
					}
				}
				if (writeMethod != null) {
					ReflectionUtils.makeAccessible(writeMethod);
					writeMethod.invoke(getWrappedInstance(), value);
					return true;
				}
			}
			catch (Exception ex) {
				LogFactory.getLog(BeanPropertyHandler.class).debug("Write method fallback failed", ex);
			}
			return false;
		}
	}

}
