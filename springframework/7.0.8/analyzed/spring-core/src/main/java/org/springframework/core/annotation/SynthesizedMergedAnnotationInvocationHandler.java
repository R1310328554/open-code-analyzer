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

package org.springframework.core.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;

import org.jspecify.annotations.Nullable;

import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;

/* ===== [OCA 中文解析] =====
class SynthesizedMergedAnnotationInvocationHandler — 意图说明

class `SynthesizedMergedAnnotationInvocationHandler`：请结合所属模块与调用方理解其在整体架构中的职责。；源文件: `spring-core/src/main/java/org/springframework/core/annotation/SynthesizedMergedAnnotationInvocationHandler.java`

（本注释由 open-code-analyzer 生成，置于原有文档注释之前）
===== [OCA 中文解析结束] ===== */
/**
 * {@link InvocationHandler} for an {@link Annotation} that Spring has
 * <em>synthesized</em> (i.e. wrapped in a dynamic proxy) with additional
 * functionality such as attribute alias handling.
 *
 * @author Sam Brannen
 * @author Phillip Webb
 * @since 5.2
 * @param <A> the annotation type
 * @see Annotation
 * @see AnnotationUtils#synthesizeAnnotation(Annotation, AnnotatedElement)
 */
final class SynthesizedMergedAnnotationInvocationHandler<A extends Annotation> implements InvocationHandler {

	private final MergedAnnotation<?> annotation;

	// [OCA] 字段 `type`：类成员状态。
	private final Class<A> type;

	// [OCA] 字段 `attributes`：类成员状态。
	private final AttributeMethods attributes;

	// [OCA] 字段 `valueCache`：类成员状态。
	private final Map<String, Object> valueCache = new ConcurrentHashMap<>(8);

	private volatile @Nullable Integer hashCode;

	private volatile @Nullable String string;


	private SynthesizedMergedAnnotationInvocationHandler(MergedAnnotation<A> annotation, Class<A> type) {
		Assert.notNull(annotation, "MergedAnnotation must not be null");
		Assert.notNull(type, "Type must not be null");
		Assert.isTrue(type.isAnnotation(), "Type must be an annotation");
		this.annotation = annotation;
		this.type = type;
		this.attributes = AttributeMethods.forAnnotationType(type);
	}


	@Override
	public Object invoke(Object proxy, Method method, Object[] args) {
		if (this.attributes.indexOf(method.getName()) != -1) {
			return getAttributeValue(method);
		}
		if (method.getParameterCount() == 0) {
			switch (method.getName()) {
				case "annotationType": return this.type;
				case "hashCode": return annotationHashCode();
				case "toString": return annotationToString();
			}
		}
		if (ReflectionUtils.isEqualsMethod(method)) {
			return annotationEquals(args[0]);
		}
		throw new AnnotationConfigurationException(String.format(
				"Method [%s] is unsupported for synthesized annotation type [%s]", method, this.type));
	}

	/**
	 * See {@link Annotation#equals(Object)} for a definition of the required algorithm.
	 * @param other the other object to compare against
	 */
	private boolean annotationEquals(Object other) {
		if (this == other) {
			return true;
		}
		if (!this.type.isInstance(other)) {
			return false;
		}
		for (int i = 0; i < this.attributes.size(); i++) {
			Method attribute = this.attributes.get(i);
			Object thisValue = getAttributeValue(attribute);
			Object otherValue = AnnotationUtils.invokeAnnotationMethod(attribute, other);
			if (!ObjectUtils.nullSafeEquals(thisValue, otherValue)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * See {@link Annotation#hashCode()} for a definition of the required algorithm.
	 */
	private int annotationHashCode() {
		Integer hashCode = this.hashCode;
		if (hashCode == null) {
			hashCode = computeHashCode();
			this.hashCode = hashCode;
		}
		return hashCode;
	}

	private Integer computeHashCode() {
		int hashCode = 0;
		for (int i = 0; i < this.attributes.size(); i++) {
			Method attribute = this.attributes.get(i);
			Object value = getAttributeValue(attribute);
			hashCode += (127 * attribute.getName().hashCode()) ^ ObjectUtils.nullSafeHashCode(value);
		}
		return hashCode;
	}

	private String annotationToString() {
		String string = this.string;
		if (string == null) {
			StringBuilder builder = new StringBuilder("@").append(getName(this.type)).append('(');
			if (this.attributes.size() == 1 && this.attributes.get(0).getName().equals(MergedAnnotation.VALUE)) {
				// Don't prepend "value=" for an annotation that only declares a "value" attribute.
				builder.append(toString(getAttributeValue(this.attributes.get(0))));
			}
			else {
				for (int i = 0; i < this.attributes.size(); i++) {
					Method attribute = this.attributes.get(i);
					if (i > 0) {
						builder.append(", ");
					}
					builder.append(attribute.getName());
					builder.append('=');
					builder.append(toString(getAttributeValue(attribute)));
				}
			}
			builder.append(')');
			string = builder.toString();
			this.string = string;
		}
		return string;
	}

	/* ===== [OCA 中文解析] =====
方法 toString — 意图与阅读要点

方法 `toString` 复杂度较高（CCN≈12, NLOC≈40）。阅读时建议先抓住主路径，再看分支/异常/缓存等旁路逻辑；关注它在调用链中上下游的契约（入参约束、返回值语义、抛出的异常）。
	===== [OCA 中文解析结束] ===== */
	/**
	 * This method currently does not address the following issues which we may
	 * choose to address at a later point in time.
	 *
	 * <ul>
	 * <li>non-ASCII, non-visible, and non-printable characters within a character
	 * or String literal are not escaped.</li>
	 * <li>formatting for float and double values does not take into account whether
	 * a value is not a number (NaN) or infinite.</li>
	 * </ul>
	 * @param value the attribute value to format
	 * @return the formatted string representation
	 */
	private String toString(Object value) {
		Class<?> type = value.getClass();
		if (type.isArray()) {
			StringBuilder builder = new StringBuilder("{");
			int arrayLength = Array.getLength(value);
			for (int i = 0; i < arrayLength; i++) {
				if (i > 0) {
					builder.append(", ");
				}
				builder.append(toString(Array.get(value, i)));
			}
			builder.append('}');
			return builder.toString();
		}
		if (type == String.class) {
			return '"' + ((String) value) + '"';
		}
		if (type == Character.class) {
			return '\'' + value.toString() + '\'';
		}
		if (type == Byte.class) {
			return String.format("(byte)0x%02x", value);
		}
		if (type == Long.class) {
			return Long.toString((Long) value) + 'L';
		}
		if (type == Float.class) {
			return Float.toString((Float) value) + 'f';
		}
		if (type == Double.class) {
			return Double.toString((Double) value);
		}
		if (value instanceof Enum<?> e) {
			return e.name();
		}
		if (type == Class.class) {
			return getName((Class<?>) value) + ".class";
		}
		return String.valueOf(value);
	}

	private Object getAttributeValue(Method method) {
		Object value = this.valueCache.computeIfAbsent(method.getName(), attributeName -> {
			Class<?> type = ClassUtils.resolvePrimitiveIfNecessary(method.getReturnType());
			return this.annotation.getValue(attributeName, type).orElseThrow(
					() -> new NoSuchElementException("No value found for attribute named '" + attributeName +
							"' in merged annotation " + getName(this.annotation.getType())));
		});

		// Clone non-empty arrays so that users cannot alter the contents of values in our cache.
		if (value.getClass().isArray() && Array.getLength(value) > 0) {
			value = cloneArray(value);
		}

		return value;
	}

	/* ===== [OCA 中文解析] =====
方法 cloneArray — 意图与阅读要点

方法 `cloneArray` 复杂度较高（CCN≈9, NLOC≈28）。阅读时建议先抓住主路径，再看分支/异常/缓存等旁路逻辑；关注它在调用链中上下游的契约（入参约束、返回值语义、抛出的异常）。
	===== [OCA 中文解析结束] ===== */
	/**
	 * Clone the provided array, ensuring that the original component type is retained.
	 * @param array the array to clone
	 */
	private Object cloneArray(Object array) {
		Class<?> type = array.getClass();
		if (type == boolean[].class) {
			return ((boolean[]) array).clone();
		}
		if (type == byte[].class) {
			return ((byte[]) array).clone();
		}
		if (type == char[].class) {
			return ((char[]) array).clone();
		}
		if (type == double[].class) {
			return ((double[]) array).clone();
		}
		if (type == float[].class) {
			return ((float[]) array).clone();
		}
		if (type == int[].class) {
			return ((int[]) array).clone();
		}
		if (type == long[].class) {
			return ((long[]) array).clone();
		}
		if (type == short[].class) {
			return ((short[]) array).clone();
		}

		// else
		return ((Object[]) array).clone();
	}

	@SuppressWarnings("unchecked")
	static <A extends Annotation> A createProxy(MergedAnnotation<A> annotation, Class<A> type) {
		ClassLoader classLoader = type.getClassLoader();
		Class<?>[] interfaces = new Class<?>[] {type};
		InvocationHandler handler = new SynthesizedMergedAnnotationInvocationHandler<>(annotation, type);
		return (A) Proxy.newProxyInstance(classLoader, interfaces, handler);
	}

	private static String getName(Class<?> clazz) {
		String canonicalName = clazz.getCanonicalName();
		return (canonicalName != null ? canonicalName : clazz.getName());
	}

}
