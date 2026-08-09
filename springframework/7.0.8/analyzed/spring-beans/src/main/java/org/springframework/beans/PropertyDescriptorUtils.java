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

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.jspecify.annotations.Nullable;

import org.springframework.core.ResolvableType;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * Spring 内部 {@link PropertyDescriptor} 实现所共用的委托方法。
 *
 * @author Chris Beams
 * @author Juergen Hoeller
 * @author Sam Brannen
 */
abstract class PropertyDescriptorUtils {

	/** 空的 PropertyDescriptor 数组常量。 */
	public static final PropertyDescriptor[] EMPTY_PROPERTY_DESCRIPTOR_ARRAY = {};


	/**
	 * 针对基本 set/get/is 访问器方法的简易内省算法，
	 * 并为它们构建对应的 JavaBeans 属性描述符。
	 * <p>仅支持基本 JavaBeans 约定，不支持索引属性、自定义器，
	 * 也不包含其他 BeanInfo 元数据。
	 * 若需标准 JavaBeans 内省，请使用 JavaBeans {@code Introspector}。
	 * @param beanClass 要内省的目标类
	 * @return 属性描述符集合
	 * @throws IntrospectionException 内省给定 bean 类时抛出
	 * @since 5.3.24
	 * @see SimpleBeanInfoFactory
	 * @see java.beans.Introspector#getBeanInfo(Class)
	 */
	public static Collection<? extends PropertyDescriptor> determineBasicProperties(Class<?> beanClass)
			throws IntrospectionException {

		Map<String, BasicPropertyDescriptor> pdMap = new TreeMap<>();

		for (Method method : beanClass.getMethods()) {
			String methodName = method.getName();

			boolean setter;
			int nameIndex;
			if (methodName.startsWith("set") && method.getParameterCount() == 1) {
				setter = true;
				nameIndex = 3;
			}
			else if (methodName.startsWith("get") && method.getParameterCount() == 0 && method.getReturnType() != void.class) {
				setter = false;
				nameIndex = 3;
			}
			else if (methodName.startsWith("is") && method.getParameterCount() == 0 && method.getReturnType() == boolean.class) {
				setter = false;
				nameIndex = 2;
			}
			else {
				continue;
			}

			String propertyName = StringUtils.uncapitalizeAsProperty(methodName.substring(nameIndex));
			if (propertyName.isEmpty()) {
				continue;
			}

			BasicPropertyDescriptor pd = pdMap.get(propertyName);
			if (pd != null) {
				if (setter) {
					pd.addWriteMethod(method);
				}
				else {
					Method readMethod = pd.getReadMethod();
					if (readMethod == null || readMethod.getReturnType().isAssignableFrom(method.getReturnType())) {
						pd.setReadMethod(method);
					}
				}
			}
			else {
				pd = new BasicPropertyDescriptor(propertyName, beanClass, (!setter ? method : null), (setter ? method : null));
				pdMap.put(propertyName, pd);
			}
		}

		return pdMap.values();
	}

	/**
	 * 复制与读/写方法无关的属性特征。
	 * 参见 {@link java.beans.FeatureDescriptor}。
	 * @param source 源属性描述符
	 * @param target 目标属性描述符
	 */
	public static void copyNonMethodProperties(PropertyDescriptor source, PropertyDescriptor target) {
		target.setExpert(source.isExpert());
		target.setHidden(source.isHidden());
		target.setPreferred(source.isPreferred());
		target.setName(source.getName());
		target.setShortDescription(source.getShortDescription());
		target.setDisplayName(source.getDisplayName());

		// 复制全部属性（模拟私有方法 FeatureDescriptor#addTable 的行为）
		Enumeration<String> keys = source.attributeNames();
		while (keys.hasMoreElements()) {
			String key = keys.nextElement();
			target.setValue(key, source.getValue(key));
		}

		// 参见 java.beans.PropertyDescriptor#PropertyDescriptor(PropertyDescriptor)
		target.setPropertyEditorClass(source.getPropertyEditorClass());
		target.setBound(source.isBound());
		target.setConstrained(source.isConstrained());
	}

	/**
	 * 根据读方法与写方法推断属性类型。
	 * 参见 {@link java.beans.PropertyDescriptor#findPropertyType}。
	 * @param readMethod 读方法，可为 {@code null}
	 * @param writeMethod 写方法，可为 {@code null}
	 * @return 推断出的属性类型；两者皆无时返回 {@code null}
	 * @throws IntrospectionException 参数个数或类型不合法时抛出
	 */
	public static @Nullable Class<?> findPropertyType(@Nullable Method readMethod, @Nullable Method writeMethod)
			throws IntrospectionException {

		Class<?> propertyType = null;

		if (readMethod != null) {
			if (readMethod.getParameterCount() != 0) {
				throw new IntrospectionException("Bad read method arg count: " + readMethod);
			}
			propertyType = readMethod.getReturnType();
			if (propertyType == void.class) {
				throw new IntrospectionException("Read method returns void: " + readMethod);
			}
		}

		if (writeMethod != null) {
			Class<?>[] params = writeMethod.getParameterTypes();
			if (params.length != 1) {
				throw new IntrospectionException("Bad write method arg count: " + writeMethod);
			}
			if (propertyType != null) {
				if (propertyType.isAssignableFrom(params[0])) {
					// 写方法的属性类型可能更具体
					propertyType = params[0];
				}
				else if (params[0].isAssignableFrom(propertyType)) {
					// 继续沿用读方法的属性类型
				}
				else {
					throw new IntrospectionException(
							"Type mismatch between read and write methods: " + readMethod + " - " + writeMethod);
				}
			}
			else {
				propertyType = params[0];
			}
		}

		return propertyType;
	}

	/**
	 * 根据索引读/写方法推断索引属性类型，并与非索引属性类型做一致性校验。
	 * 参见 {@link java.beans.IndexedPropertyDescriptor#findIndexedPropertyType}。
	 * @param name 属性名
	 * @param propertyType 非索引属性类型，可为 {@code null}
	 * @param indexedReadMethod 索引读方法，可为 {@code null}
	 * @param indexedWriteMethod 索引写方法，可为 {@code null}
	 * @return 推断出的索引属性类型
	 * @throws IntrospectionException 参数个数、索引类型或读写类型不一致时抛出
	 */
	public static @Nullable Class<?> findIndexedPropertyType(String name, @Nullable Class<?> propertyType,
			@Nullable Method indexedReadMethod, @Nullable Method indexedWriteMethod) throws IntrospectionException {

		Class<?> indexedPropertyType = null;

		if (indexedReadMethod != null) {
			Class<?>[] params = indexedReadMethod.getParameterTypes();
			if (params.length != 1) {
				throw new IntrospectionException("Bad indexed read method arg count: " + indexedReadMethod);
			}
			if (params[0] != int.class) {
				throw new IntrospectionException("Non int index to indexed read method: " + indexedReadMethod);
			}
			indexedPropertyType = indexedReadMethod.getReturnType();
			if (indexedPropertyType == void.class) {
				throw new IntrospectionException("Indexed read method returns void: " + indexedReadMethod);
			}
		}

		if (indexedWriteMethod != null) {
			Class<?>[] params = indexedWriteMethod.getParameterTypes();
			if (params.length != 2) {
				throw new IntrospectionException("Bad indexed write method arg count: " + indexedWriteMethod);
			}
			if (params[0] != int.class) {
				throw new IntrospectionException("Non int index to indexed write method: " + indexedWriteMethod);
			}
			if (indexedPropertyType != null) {
				if (indexedPropertyType.isAssignableFrom(params[1])) {
					// 写方法的属性类型可能更具体
					indexedPropertyType = params[1];
				}
				else if (params[1].isAssignableFrom(indexedPropertyType)) {
					// 继续沿用读方法的属性类型
				}
				else {
					throw new IntrospectionException("Type mismatch between indexed read and write methods: " +
							indexedReadMethod + " - " + indexedWriteMethod);
				}
			}
			else {
				indexedPropertyType = params[1];
			}
		}

		if (propertyType != null && (!propertyType.isArray() ||
				propertyType.componentType() != indexedPropertyType)) {
			throw new IntrospectionException("Type mismatch between indexed and non-indexed methods: " +
					indexedReadMethod + " - " + indexedWriteMethod);
		}

		return indexedPropertyType;
	}

	/**
	 * 比较给定的两个 {@code PropertyDescriptor}，若它们等价则返回 {@code true}，
	 * 即读方法、写方法、属性类型、属性编辑器以及相关标志均等价。
	 * @param pd 第一个属性描述符
	 * @param otherPd 第二个属性描述符
	 * @return 两者是否等价
	 * @see java.beans.PropertyDescriptor#equals(Object)
	 */
	public static boolean equals(PropertyDescriptor pd, PropertyDescriptor otherPd) {
		return (ObjectUtils.nullSafeEquals(pd.getReadMethod(), otherPd.getReadMethod()) &&
				ObjectUtils.nullSafeEquals(pd.getWriteMethod(), otherPd.getWriteMethod()) &&
				ObjectUtils.nullSafeEquals(pd.getPropertyType(), otherPd.getPropertyType()) &&
				ObjectUtils.nullSafeEquals(pd.getPropertyEditorClass(), otherPd.getPropertyEditorClass()) &&
				pd.isBound() == otherPd.isBound() && pd.isConstrained() == otherPd.isConstrained());
	}


	/**
	 * 供 {@link #determineBasicProperties(Class)} 使用的 {@code PropertyDescriptor}，
	 * 在 {@link #setReadMethod}/{@link #setWriteMethod} 时不做提前类型判定。
	 * @since 5.3.24
	 */
	private static class BasicPropertyDescriptor extends PropertyDescriptor {

		/** 所属 bean 类型，用于解析泛型读写方法的具体类型。 */
		private final Class<?> beanClass;

		/** 当前选用的读方法。 */
		private @Nullable Method readMethod;

		/** 当前选用的写方法；在有多个候选写方法时可能延迟解析。 */
		private @Nullable Method writeMethod;

		/** 候选写方法列表，供延迟选择最匹配者。 */
		private final List<Method> candidateWriteMethods = new ArrayList<>();

		/**
		 * 创建基本属性描述符。
		 * @param propertyName 属性名
		 * @param beanClass 所属 bean 类型
		 * @param readMethod 读方法，可为 {@code null}
		 * @param writeMethod 写方法，可为 {@code null}
		 * @throws IntrospectionException 父类构造过程中类型不合法时抛出
		 */
		public BasicPropertyDescriptor(String propertyName, Class<?> beanClass, @Nullable Method readMethod, @Nullable Method writeMethod)
				throws IntrospectionException {

			super(propertyName, readMethod, writeMethod);
			this.beanClass = beanClass;
		}

		/**
		 * 设置读方法（仅缓存引用，不做父类那套提前类型校验）。
		 */
		@Override
		public void setReadMethod(@Nullable Method readMethod) {
			this.readMethod = readMethod;
		}

		/**
		 * 返回当前读方法。
		 */
		@Override
		public @Nullable Method getReadMethod() {
			return this.readMethod;
		}

		/**
		 * 设置写方法（仅缓存引用，不做父类那套提前类型校验）。
		 */
		@Override
		public void setWriteMethod(@Nullable Method writeMethod) {
			this.writeMethod = writeMethod;
		}

		/**
		 * 追加一个候选写方法。
		 * <p>由于 {@code setWriteMethod()} 会从
		 * {@code PropertyDescriptor(String, Method, Method)} 构造函数中被调用，
		 * {@code this.writeMethod} 此时可能非空。
		 * @param writeMethod 候选写方法
		 */
		void addWriteMethod(Method writeMethod) {
			// 由于 setWriteMethod() 会从 PropertyDescriptor(String, Method, Method)
			// 构造函数中被调用，this.writeMethod 此时可能非空。
			if (this.writeMethod != null) {
				this.candidateWriteMethods.add(this.writeMethod);
				this.writeMethod = null;
			}
			this.candidateWriteMethods.add(writeMethod);
		}

		/**
		 * 返回写方法；若尚未选定且存在多个候选，则按读方法返回类型挑选最匹配者。
		 */
		@Override
		public @Nullable Method getWriteMethod() {
			if (this.writeMethod == null && !this.candidateWriteMethods.isEmpty()) {
				if (this.readMethod == null || this.candidateWriteMethods.size() == 1) {
					this.writeMethod = this.candidateWriteMethods.get(0);
				}
				else {
					Class<?> resolvedReadType =
							ResolvableType.forMethodReturnType(this.readMethod, this.beanClass).toClass();
					for (Method method : this.candidateWriteMethods) {
						// 1) 与解析后的类型做精确匹配检查。
						Class<?> resolvedWriteType =
								ResolvableType.forMethodParameter(method, 0, this.beanClass).toClass();
						if (resolvedReadType.equals(resolvedWriteType)) {
							this.writeMethod = method;
							break;
						}

						// 2) 检查候选写方法的参数类型是否与读方法返回类型兼容。
						Class<?> parameterType = method.getParameterTypes()[0];
						if (this.readMethod.getReturnType().isAssignableFrom(parameterType)) {
							// 若尚未找到兼容的写方法，或当前候选的参数类型是先前候选
							// 参数类型的子类型，则将当前候选记为写方法。
							if (this.writeMethod == null ||
									this.writeMethod.getParameterTypes()[0].isAssignableFrom(parameterType)) {
								this.writeMethod = method;
								// 此处不 break，还需与剩余候选继续比较。
							}
						}
					}
				}
			}
			return this.writeMethod;
		}
	}

}
