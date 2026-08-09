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
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;

import org.springframework.core.BridgeMethodResolver;
import org.springframework.core.MethodParameter;
import org.springframework.core.ResolvableType;
import org.springframework.core.convert.Property;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

/**
 * 对标准 JavaBeans {@link PropertyDescriptor} 的扩展：
 * 重写 {@code getPropertyType()}，使泛型声明的类型变量
 * 能够针对所属 bean 类进行解析。
 *
 * @author Juergen Hoeller
 * @since 2.5.2
 */
final class GenericTypeAwarePropertyDescriptor extends PropertyDescriptor {

	/** 所属 bean 的类型 */
	private final Class<?> beanClass;

	/** 读方法（getter），可能为 {@code null} */
	private final @Nullable Method readMethod;

	/** 写方法（setter），可能为 {@code null} */
	private final @Nullable Method writeMethod;

	/** 与当前写方法同名、参数个数相同的其他候选写方法（存在歧义时） */
	private @Nullable Set<Method> ambiguousWriteMethods;

	/** 是否已针对写方法歧义打过调试日志 */
	private volatile boolean ambiguousWriteMethodsLogged;

	/** 写方法的第一个参数（绑定 containing class 后的 {@link MethodParameter}） */
	private @Nullable MethodParameter writeMethodParameter;

	/** 写方法参数的可解析类型（惰性缓存） */
	private volatile @Nullable ResolvableType writeMethodType;

	/** 读方法返回值的可解析类型 */
	private @Nullable ResolvableType readMethodType;

	/** 该属性的类型描述符（惰性缓存） */
	private volatile @Nullable TypeDescriptor typeDescriptor;

	/** 解析后的属性类型 */
	private @Nullable Class<?> propertyType;

	/** 专用 PropertyEditor 类型（若有） */
	private final @Nullable Class<?> propertyEditorClass;


	/**
	 * 创建感知泛型的属性描述符。
	 * @param beanClass 所属 bean 类型
	 * @param propertyName 属性名
	 * @param readMethod 读方法（可为 {@code null}）
	 * @param writeMethod 写方法（可为 {@code null}）
	 * @param propertyEditorClass 专用 PropertyEditor 类型（可为 {@code null}）
	 * @throws IntrospectionException 内省失败时抛出
	 */
	public GenericTypeAwarePropertyDescriptor(Class<?> beanClass, String propertyName,
			@Nullable Method readMethod, @Nullable Method writeMethod,
			@Nullable Class<?> propertyEditorClass) throws IntrospectionException {

		super(propertyName, null, null);
		this.beanClass = beanClass;

		Method readMethodToUse = (readMethod != null ? BridgeMethodResolver.findBridgedMethod(readMethod) : null);
		Method writeMethodToUse = (writeMethod != null ? BridgeMethodResolver.findBridgedMethod(writeMethod) : null);
		if (writeMethodToUse == null && readMethodToUse != null) {
			// 回退：原始 JavaBeans 内省可能因未解析桥接方法而找不到匹配的 setter，
			// 典型场景是 getter 使用协变返回类型，而 setter 按具体属性类型声明。
			Method candidate = ClassUtils.getMethodIfAvailable(
					this.beanClass, "set" + StringUtils.capitalize(getName()), (Class<?>[]) null);
			if (candidate != null && candidate.getParameterCount() == 1) {
				writeMethodToUse = candidate;
			}
		}
		this.readMethod = readMethodToUse;
		this.writeMethod = writeMethodToUse;

		if (this.writeMethod != null) {
			if (this.readMethod == null) {
				// 写方法未能与读方法配对：可能存在多个重载变体，
				// JDK 的 JavaBeans Introspector 会任意选定其中一个“胜出者”……
				Set<Method> ambiguousCandidates = new HashSet<>();
				for (Method method : beanClass.getMethods()) {
					if (method.getName().equals(this.writeMethod.getName()) &&
							!method.equals(this.writeMethod) && !method.isBridge() &&
							method.getParameterCount() == this.writeMethod.getParameterCount()) {
						ambiguousCandidates.add(method);
					}
				}
				if (!ambiguousCandidates.isEmpty()) {
					this.ambiguousWriteMethods = ambiguousCandidates;
				}
			}
			this.writeMethodParameter = new MethodParameter(this.writeMethod, 0).withContainingClass(this.beanClass);
		}

		if (this.readMethod != null) {
			this.readMethodType = ResolvableType.forMethodReturnType(this.readMethod, this.beanClass);
			this.propertyType = this.readMethodType.resolve(this.readMethod.getReturnType());
		}
		else if (this.writeMethodParameter != null) {
			this.propertyType = this.writeMethodParameter.getParameterType();
		}

		this.propertyEditorClass = propertyEditorClass;
	}


	/**
	 * 返回所属 bean 的类型。
	 */
	public Class<?> getBeanClass() {
		return this.beanClass;
	}

	@Override
	public @Nullable Method getReadMethod() {
		return this.readMethod;
	}

	@Override
	public @Nullable Method getWriteMethod() {
		return this.writeMethod;
	}

	/**
	 * 返回实际用于访问的写方法；若存在歧义候选，首次调用时打调试日志。
	 * @return 写方法（永不为 {@code null}）
	 */
	public Method getWriteMethodForActualAccess() {
		Assert.state(this.writeMethod != null, "No write method available");
		if (this.ambiguousWriteMethods != null && !this.ambiguousWriteMethodsLogged) {
			this.ambiguousWriteMethodsLogged = true;
			LogFactory.getLog(GenericTypeAwarePropertyDescriptor.class).debug("Non-unique JavaBean property '" +
					getName() + "' being accessed! Ambiguous write methods found next to actually used [" +
					this.writeMethod + "]: " + this.ambiguousWriteMethods);
		}
		return this.writeMethod;
	}

	/**
	 * 在存在歧义写方法时，按给定值类型挑选一个可用的回退写方法。
	 * @param valueType 待写入值的类型（可为 {@code null}）
	 * @return 匹配的回退写方法；无合适候选时返回 {@code null}
	 */
	public @Nullable Method getWriteMethodFallback(@Nullable Class<?> valueType) {
		if (this.ambiguousWriteMethods != null) {
			for (Method method : this.ambiguousWriteMethods) {
				Class<?> paramType = method.getParameterTypes()[0];
				if (valueType != null ? paramType.isAssignableFrom(valueType) : !paramType.isPrimitive()) {
					return method;
				}
			}
		}
		return null;
	}

	/**
	 * 当且仅当恰好存在一个歧义写方法候选时，返回该唯一回退写方法。
	 * @return 唯一候选；否则 {@code null}
	 */
	public @Nullable Method getUniqueWriteMethodFallback() {
		if (this.ambiguousWriteMethods != null && this.ambiguousWriteMethods.size() == 1) {
			return this.ambiguousWriteMethods.iterator().next();
		}
		return null;
	}

	/**
	 * 是否拥有唯一、无歧义的写方法。
	 */
	public boolean hasUniqueWriteMethod() {
		return (this.writeMethod != null && this.ambiguousWriteMethods == null);
	}

	/**
	 * 返回写方法的参数描述（第一个参数）。
	 */
	public MethodParameter getWriteMethodParameter() {
		Assert.state(this.writeMethodParameter != null, "No write method available");
		return this.writeMethodParameter;
	}

	/**
	 * 返回写方法参数的 {@link ResolvableType}（惰性解析并缓存）。
	 */
	public ResolvableType getWriteMethodType() {
		ResolvableType writeMethodType = this.writeMethodType;
		if (writeMethodType == null) {
			writeMethodType = ResolvableType.forMethodParameter(getWriteMethodParameter());
			this.writeMethodType = writeMethodType;
		}
		return writeMethodType;
	}

	/**
	 * 返回读方法返回值的 {@link ResolvableType}。
	 */
	public ResolvableType getReadMethodType() {
		Assert.state(this.readMethodType != null, "No read method available");
		return this.readMethodType;
	}

	/**
	 * 返回该属性的 {@link TypeDescriptor}（惰性构建并缓存）。
	 */
	public TypeDescriptor getTypeDescriptor() {
		TypeDescriptor typeDescriptor = this.typeDescriptor;
		if (typeDescriptor == null) {
			Property property = new Property(getBeanClass(), getReadMethod(), getWriteMethod(), getName());
			typeDescriptor = new TypeDescriptor(property);
			this.typeDescriptor = typeDescriptor;
		}
		return typeDescriptor;
	}

	@Override
	public @Nullable Class<?> getPropertyType() {
		return this.propertyType;
	}

	@Override
	public @Nullable Class<?> getPropertyEditorClass() {
		return this.propertyEditorClass;
	}


	@Override
	public boolean equals(@Nullable Object other) {
		return (this == other || (other instanceof GenericTypeAwarePropertyDescriptor that &&
				getBeanClass().equals(that.getBeanClass()) &&
				PropertyDescriptorUtils.equals(this, that)));
	}

	@Override
	public int hashCode() {
		return Objects.hash(getBeanClass(), getReadMethod(), getWriteMethod());
	}

}
