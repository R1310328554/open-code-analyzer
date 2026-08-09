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

package org.springframework.beans.factory;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.util.Objects;

import org.jspecify.annotations.Nullable;

import org.springframework.core.MethodParameter;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

/**
 * 注入点的简单描述符，指向方法/构造器参数或字段。
 *
 * <p>由 {@link UnsatisfiedDependencyException} 暴露。也可作为工厂方法的参数使用，
 * 以便根据请求方的注入点构建定制化的 Bean 实例。
 *
 * @author Juergen Hoeller
 * @since 4.3
 * @see UnsatisfiedDependencyException#getInjectionPoint()
 * @see org.springframework.beans.factory.config.DependencyDescriptor
 */
public class InjectionPoint {

	/** 所包装的方法/构造器参数；与 {@link #field} 二者之一可用 */
	protected @Nullable MethodParameter methodParameter;

	/** 所包装的字段；与 {@link #methodParameter} 二者之一可用 */
	protected @Nullable Field field;

	/** 字段注解的缓存，避免重复反射读取 */
	private volatile Annotation @Nullable [] fieldAnnotations;


	/**
	 * 为方法或构造器参数创建注入点描述符。
	 * @param methodParameter 要包装的 MethodParameter
	 */
	public InjectionPoint(MethodParameter methodParameter) {
		Assert.notNull(methodParameter, "MethodParameter must not be null");
		this.methodParameter = methodParameter;
	}

	/**
	 * 为字段创建注入点描述符。
	 * @param field 要包装的字段
	 */
	public InjectionPoint(Field field) {
		Assert.notNull(field, "Field must not be null");
		this.field = field;
	}

	/**
	 * 拷贝构造器。
	 * @param original 要复制的原始描述符
	 */
	protected InjectionPoint(InjectionPoint original) {
		this.methodParameter = (original.methodParameter != null ?
				new MethodParameter(original.methodParameter) : null);
		this.field = original.field;
		this.fieldAnnotations = original.fieldAnnotations;
	}

	/**
	 * 仅供子类序列化使用。
	 */
	protected InjectionPoint() {
	}


	/**
	 * 返回所包装的 MethodParameter（若有）。
	 * <p>注意：MethodParameter 与 Field 二者必有其一可用。
	 * @return MethodParameter；若无则为 {@code null}
	 */
	public @Nullable MethodParameter getMethodParameter() {
		return this.methodParameter;
	}

	/**
	 * 返回所包装的 Field（若有）。
	 * <p>注意：MethodParameter 与 Field 二者必有其一可用。
	 * @return Field；若无则为 {@code null}
	 */
	public @Nullable Field getField() {
		return this.field;
	}

	/**
	 * 返回所包装的 MethodParameter，假定其一定存在。
	 * @return MethodParameter（永不为 {@code null}）
	 * @throws IllegalStateException 若没有可用的 MethodParameter
	 * @since 5.0
	 */
	protected final MethodParameter obtainMethodParameter() {
		Assert.state(this.methodParameter != null, "MethodParameter is not available");
		return this.methodParameter;
	}

	/**
	 * 获取与所包装字段或方法/构造器参数关联的注解。
	 */
	public Annotation[] getAnnotations() {
		if (this.field != null) {
			Annotation[] fieldAnnotations = this.fieldAnnotations;
			if (fieldAnnotations == null) {
				fieldAnnotations = this.field.getAnnotations();
				this.fieldAnnotations = fieldAnnotations;
			}
			return fieldAnnotations;
		}
		else {
			return obtainMethodParameter().getParameterAnnotations();
		}
	}

	/**
	 * 检索给定类型的字段/参数注解（若有）。
	 * @param annotationType 要检索的注解类型
	 * @return 注解实例；未找到则为 {@code null}
	 * @since 4.3.9
	 */
	public <A extends Annotation> @Nullable A getAnnotation(Class<A> annotationType) {
		return (this.field != null ? this.field.getAnnotation(annotationType) :
				obtainMethodParameter().getParameterAnnotation(annotationType));
	}

	/**
	 * 返回底层字段或方法/构造器参数所声明的类型，即注入类型。
	 */
	public Class<?> getDeclaredType() {
		return (this.field != null ? this.field.getType() : obtainMethodParameter().getParameterType());
	}

	/**
	 * 返回包含该注入点的所包装成员。
	 * @return 作为 Member 的 Field / Method / Constructor
	 */
	public Member getMember() {
		return (this.field != null ? this.field : obtainMethodParameter().getMember());
	}

	/**
	 * 返回所包装的带注解元素。
	 * <p>注意：若为方法/构造器参数，这里暴露的是方法或构造器本身上声明的注解
	 * （即方法/构造器级别，而非参数级别）。
	 * 若要在该场景下获取参数级别注解，请使用 {@link #getAnnotations()}，
	 * 它会透明地对应到字段注解的获取方式。
	 * @return 作为 AnnotatedElement 的 Field / Method / Constructor
	 */
	public AnnotatedElement getAnnotatedElement() {
		return (this.field != null ? this.field : obtainMethodParameter().getAnnotatedElement());
	}


	@Override
	public boolean equals(@Nullable Object other) {
		if (this == other) {
			return true;
		}
		if (other == null || getClass() != other.getClass()) {
			return false;
		}
		InjectionPoint otherPoint = (InjectionPoint) other;
		return (ObjectUtils.nullSafeEquals(this.field, otherPoint.field) &&
				ObjectUtils.nullSafeEquals(this.methodParameter, otherPoint.methodParameter));
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.field, this.methodParameter);
	}

	@Override
	public String toString() {
		return (this.field != null ? "field '" + this.field.getName() + "'" : String.valueOf(this.methodParameter));
	}

}
