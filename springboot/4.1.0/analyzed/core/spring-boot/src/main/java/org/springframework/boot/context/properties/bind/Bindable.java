/*
 * Copyright 2012-present the original author or authors.
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

package org.springframework.boot.context.properties.bind;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;

import org.springframework.boot.context.properties.source.ConfigurationProperty;
import org.springframework.core.ResolvableType;
import org.springframework.core.style.ToStringCreator;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

/**
 * 可由 {@link Binder} 绑定的源对象。
 *
 * @param <T> 源类型
 * @author Phillip Webb
 * @author Madhura Bhave
 * @since 2.0.0
 * @see Bindable#of(Class)
 * @see Bindable#of(ResolvableType)
 */
public final class Bindable<T> {

	private static final Annotation[] NO_ANNOTATIONS = {};

	private static final EnumSet<BindRestriction> NO_BIND_RESTRICTIONS = EnumSet.noneOf(BindRestriction.class);

	private final ResolvableType type;

	private final ResolvableType boxedType;

	private final @Nullable Supplier<T> value;

	private final Annotation[] annotations;

	private final EnumSet<BindRestriction> bindRestrictions;

	private final @Nullable BindMethod bindMethod;

	private Bindable(ResolvableType type, ResolvableType boxedType, @Nullable Supplier<T> value,
			Annotation[] annotations, EnumSet<BindRestriction> bindRestrictions, @Nullable BindMethod bindMethod) {
		this.type = type;
		this.boxedType = boxedType;
		this.value = value;
		this.annotations = annotations;
		this.bindRestrictions = bindRestrictions;
		this.bindMethod = bindMethod;
	}

	/**
	 * 返回待绑定项的类型。
	 *
	 * @return 正在绑定的类型
	 */
	public ResolvableType getType() {
		return this.type;
	}

	/**
	 * 返回待绑定项的装箱类型。
	 *
	 * @return 待绑定项的装箱类型
	 */
	public ResolvableType getBoxedType() {
		return this.boxedType;
	}

	/**
	 * 返回提供对象值的 supplier，或 {@code null}。
	 *
	 * @return 值或 {@code null}
	 */
	public @Nullable Supplier<T> getValue() {
		return this.value;
	}

	/**
	 * 返回可能影响绑定的关联注解。
	 *
	 * @return 关联注解
	 */
	public Annotation[] getAnnotations() {
		return this.annotations;
	}

	/**
	 * 返回单个可能影响绑定的关联注解。
	 *
	 * @param <A> 注解类型
	 * @param type 注解类型
	 * @return 关联注解或 {@code null}
	 */
	@SuppressWarnings("unchecked")
	public <A extends Annotation> @Nullable A getAnnotation(Class<A> type) {
		for (Annotation annotation : this.annotations) {
			if (type.isInstance(annotation)) {
				return (A) annotation;
			}
		}
		return null;
	}

	/**
	 * 若已添加指定的绑定限制则返回 {@code true}。
	 *
	 * @param bindRestriction 待检查的绑定限制
	 * @return 是否已添加该绑定限制
	 * @since 2.5.0
	 */
	public boolean hasBindRestriction(BindRestriction bindRestriction) {
		return this.bindRestrictions.contains(bindRestriction);
	}

	/**
	 * 返回用于绑定此 bindable 的 {@link BindMethod 方法}；若无需特定绑定方式则为 {@code null}。
	 *
	 * @return 绑定方法或 {@code null}
	 * @since 3.0.8
	 */
	public @Nullable BindMethod getBindMethod() {
		return this.bindMethod;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		Bindable<?> other = (Bindable<?>) obj;
		boolean result = true;
		result = result && nullSafeEquals(this.type.resolve(), other.type.resolve());
		result = result && nullSafeEquals(this.annotations, other.annotations);
		result = result && nullSafeEquals(this.bindRestrictions, other.bindRestrictions);
		result = result && nullSafeEquals(this.bindMethod, other.bindMethod);
		return result;
	}

	@Override
	public int hashCode() {
		return ObjectUtils.nullSafeHash(this.type, this.annotations, this.bindRestrictions, this.bindMethod);
	}

	@Override
	public String toString() {
		ToStringCreator creator = new ToStringCreator(this);
		creator.append("type", this.type);
		creator.append("value", (this.value != null) ? "provided" : "none");
		creator.append("annotations", this.annotations);
		creator.append("bindMethod", this.bindMethod);
		return creator.toString();
	}

	private boolean nullSafeEquals(@Nullable Object o1, @Nullable Object o2) {
		return ObjectUtils.nullSafeEquals(o1, o2);
	}

	/**
	 * 创建带有指定注解的更新后 {@link Bindable} 实例。
	 *
	 * @param annotations 注解
	 * @return 更新后的 {@link Bindable}
	 */
	public Bindable<T> withAnnotations(Annotation @Nullable ... annotations) {
		return new Bindable<>(this.type, this.boxedType, this.value,
				(annotations != null) ? annotations : NO_ANNOTATIONS, NO_BIND_RESTRICTIONS, this.bindMethod);
	}

	/**
	 * 创建带有已有值的更新后 {@link Bindable} 实例。隐含将使用 Java Bean 绑定。
	 *
	 * @param existingValue 已有值
	 * @return 更新后的 {@link Bindable}
	 */
	public Bindable<T> withExistingValue(@Nullable T existingValue) {
		Assert.isTrue(existingValue == null || this.type.isArray() || boxedTypeIsInstanceOf(existingValue),
				() -> "'existingValue' must be an instance of " + this.type);
		Assert.state(this.bindMethod != BindMethod.VALUE_OBJECT,
				() -> "An existing value cannot be provided when binding as a value object");
		Supplier<T> value = (existingValue != null) ? () -> existingValue : null;
		return new Bindable<>(this.type, this.boxedType, value, this.annotations, this.bindRestrictions,
				BindMethod.JAVA_BEAN);
	}

	private boolean boxedTypeIsInstanceOf(T existingValue) {
		Class<?> resolved = this.boxedType.resolve();
		return resolved != null && resolved.isInstance(existingValue);
	}

	/**
	 * 创建带有值 supplier 的更新后 {@link Bindable} 实例。
	 *
	 * @param suppliedValue 值的 supplier
	 * @return 更新后的 {@link Bindable}
	 */
	public Bindable<T> withSuppliedValue(@Nullable Supplier<T> suppliedValue) {
		return new Bindable<>(this.type, this.boxedType, suppliedValue, this.annotations, this.bindRestrictions,
				this.bindMethod);
	}

	/**
	 * 创建带有额外绑定限制的更新后 {@link Bindable} 实例。
	 *
	 * @param additionalRestrictions 要应用的额外限制
	 * @return 更新后的 {@link Bindable}
	 * @since 2.5.0
	 */
	public Bindable<T> withBindRestrictions(BindRestriction... additionalRestrictions) {
		EnumSet<BindRestriction> bindRestrictions = EnumSet.copyOf(this.bindRestrictions);
		bindRestrictions.addAll(Arrays.asList(additionalRestrictions));
		return new Bindable<>(this.type, this.boxedType, this.value, this.annotations, bindRestrictions,
				this.bindMethod);
	}

	/**
	 * 创建带有特定绑定方法的更新后 {@link Bindable} 实例。要使用
	 * {@link BindMethod#VALUE_OBJECT 值对象绑定}，当前实例不得有已有值或 supplier 提供的值。
	 *
	 * @param bindMethod 用于绑定 bindable 的方法
	 * @return 更新后的 {@link Bindable}
	 * @since 3.0.8
	 */
	public Bindable<T> withBindMethod(@Nullable BindMethod bindMethod) {
		Assert.state(bindMethod != BindMethod.VALUE_OBJECT || this.value == null,
				() -> "Value object binding cannot be used with an existing or supplied value");
		return new Bindable<>(this.type, this.boxedType, this.value, this.annotations, this.bindRestrictions,
				bindMethod);
	}

	/**
	 * 创建与指定实例类型相同、且已有值等于该实例的新 {@link Bindable}。
	 *
	 * @param <T> 源类型
	 * @param instance 实例（不得为 {@code null}）
	 * @return {@link Bindable} 实例
	 * @see #of(ResolvableType)
	 * @see #withExistingValue(Object)
	 */
	@SuppressWarnings("unchecked")
	public static <T> Bindable<T> ofInstance(T instance) {
		Assert.notNull(instance, "'instance' must not be null");
		Class<T> type = (Class<T>) instance.getClass();
		return of(type).withExistingValue(instance);
	}

	/**
	 * 创建指定类型的新 {@link Bindable}。
	 *
	 * @param <T> 源类型
	 * @param type 类型（不得为 {@code null}）
	 * @return {@link Bindable} 实例
	 * @see #of(ResolvableType)
	 */
	public static <T> Bindable<T> of(Class<T> type) {
		Assert.notNull(type, "'type' must not be null");
		return of(ResolvableType.forClass(type));
	}

	/**
	 * 创建指定元素类型的新 {@link Bindable} {@link List}。
	 *
	 * @param <E> 元素类型
	 * @param elementType 列表元素类型
	 * @return {@link Bindable} 实例
	 */
	public static <E> Bindable<List<E>> listOf(Class<E> elementType) {
		return of(ResolvableType.forClassWithGenerics(List.class, elementType));
	}

	/**
	 * 创建指定元素类型的新 {@link Bindable} {@link Set}。
	 *
	 * @param <E> 元素类型
	 * @param elementType 集合元素类型
	 * @return {@link Bindable} 实例
	 */
	public static <E> Bindable<Set<E>> setOf(Class<E> elementType) {
		return of(ResolvableType.forClassWithGenerics(Set.class, elementType));
	}

	/**
	 * 创建指定键值类型的新 {@link Bindable} {@link Map}。
	 *
	 * @param <K> 键类型
	 * @param <V> 值类型
	 * @param keyType Map 键类型
	 * @param valueType Map 值类型
	 * @return {@link Bindable} 实例
	 */
	public static <K, V> Bindable<Map<K, V>> mapOf(Class<K> keyType, Class<V> valueType) {
		return of(ResolvableType.forClassWithGenerics(Map.class, keyType, valueType));
	}

	/**
	 * 创建指定类型的新 {@link Bindable}。
	 *
	 * @param <T> 源类型
	 * @param type 类型（不得为 {@code null}）
	 * @return {@link Bindable} 实例
	 * @see #of(Class)
	 */
	public static <T> Bindable<T> of(ResolvableType type) {
		Assert.notNull(type, "'type' must not be null");
		ResolvableType boxedType = box(type);
		return new Bindable<>(type, boxedType, null, NO_ANNOTATIONS, NO_BIND_RESTRICTIONS, null);
	}

	private static ResolvableType box(ResolvableType type) {
		Class<?> resolved = type.resolve();
		if (resolved != null && resolved.isPrimitive()) {
			Object array = Array.newInstance(resolved, 1);
			Class<?> wrapperType = Array.get(array, 0).getClass();
			return ResolvableType.forClass(wrapperType);
		}
		if (resolved != null && resolved.isArray()) {
			return ResolvableType.forArrayComponent(box(type.getComponentType()));
		}
		return type;
	}

	/**
	 * 绑定值时可应用的限制。
	 *
	 * @since 2.5.0
	 */
	public enum BindRestriction {

		/**
		 * 不绑定直接的 {@link ConfigurationProperty} 匹配项。
		 */
		NO_DIRECT_PROPERTY

	}

}
