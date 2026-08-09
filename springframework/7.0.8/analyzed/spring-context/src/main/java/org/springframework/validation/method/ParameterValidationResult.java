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

package org.springframework.validation.method;

import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;

import org.jspecify.annotations.Nullable;

import org.springframework.context.MessageSourceResolvable;
import org.springframework.core.MethodParameter;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

/**
 * 存储并暴露方法参数的方法校验结果。
 * <ul>
 * <li>方法参数值上的直接校验错误以 {@link MessageSourceResolvable} 列表暴露。
 * <li>Object 方法参数上的嵌套校验错误由子类 {@link ParameterErrors}
 * 以 {@link org.springframework.validation.Errors} 形式暴露。
 * </ul>
 *
 * <p>当方法参数为 {@link List}、数组或 {@link java.util.Map} 等容器时，
 * 为每个有错误的元素创建单独的 {@link ParameterValidationResult}。
 * 此时 {@link #getContainer() container}、{@link #getContainerIndex() containerIndex}
 * 和 {@link #getContainerKey() containerKey} 属性提供额外上下文。
 *
 * @author Rossen Stoyanchev
 * @since 6.1
 */
public class ParameterValidationResult {

	private final MethodParameter methodParameter;

	private final @Nullable Object argument;

	private final List<MessageSourceResolvable> resolvableErrors;

	private final @Nullable Object container;

	private final @Nullable Integer containerIndex;

	private final @Nullable Object containerKey;

	private final BiFunction<MessageSourceResolvable, Class<?>, Object> sourceLookup;


	/**
	 * 创建 {@code ParameterValidationResult}。
	 */
	public ParameterValidationResult(
			MethodParameter param, @Nullable Object arg, Collection<? extends MessageSourceResolvable> errors,
			@Nullable Object container, @Nullable Integer index, @Nullable Object key,
			BiFunction<MessageSourceResolvable, Class<?>, Object> sourceLookup) {

		Assert.notNull(param, "MethodParameter is required");
		Assert.notEmpty(errors, "`resolvableErrors` must not be empty");
		this.methodParameter = param;
		this.argument = arg;
		this.resolvableErrors = List.copyOf(errors);
		this.container = container;
		this.containerIndex = index;
		this.containerKey = key;
		this.sourceLookup = sourceLookup;
	}


	/**
	 * 校验结果所属的方法参数。
	 */
	public MethodParameter getMethodParameter() {
		return this.methodParameter;
	}

	/**
	 * 已校验的方法参数值。
	 */
	public @Nullable Object getArgument() {
		return this.argument;
	}

	/**
	 * 从校验库的校验错误适配而来的 {@link MessageSourceResolvable} 表示列表。
	 * <ul>
	 * <li>对于方法参数上的直接约束，错误代码基于约束注解名、对象、方法、
	 * 参数及参数类型，例如
	 * {@code ["Max.myObject#myMethod.myParameter", "Max.myParameter", "Max.int", "Max"]}.
	 * Arguments include the parameter itself as a {@link MessageSourceResolvable}, for example,
	 * {@code ["myObject#myMethod.myParameter", "myParameter"]}, followed by actual
	 * constraint annotation attributes (i.e. excluding "message", "groups" and
	 * "payload") in alphabetical order of attribute names.
	 * <li>对于 Bean 方法参数上通过 {@link jakarta.validation.Validator @Valid}
	 * 的级联约束，本方法返回 {@link org.springframework.validation.FieldError 字段错误}，
	 * 也可通过 {@link ParameterErrors} 子类的方法更方便地访问。
	 * </ul>
	 */
	public List<MessageSourceResolvable> getResolvableErrors() {
		return this.resolvableErrors;
	}

	/**
	 * 当在 {@link java.util.Collection}、{@link java.util.Map}、
	 * {@link java.util.Optional} 等元素容器上声明 {@code @Valid} 时，
	 * 本方法返回已校验 {@link #getArgument() argument} 的容器，
	 * 而 {@link #getContainerIndex()} 和 {@link #getContainerKey()} 提供索引或键信息（若适用）。
	 */
	public @Nullable Object getContainer() {
		return this.container;
	}

	/**
	 * 当在 {@link List} 或数组等索引元素容器上声明 {@code @Valid} 时，
	 * 本方法返回已校验 {@link #getArgument() argument} 的索引。
	 */
	public @Nullable Integer getContainerIndex() {
		return this.containerIndex;
	}

	/**
	 * 当在 {@link java.util.Map} 等按键引用元素的容器上声明 {@code @Valid} 时，
	 * 本方法返回已校验 {@link #getArgument() argument} 的键。
	 */
	public @Nullable Object getContainerKey() {
		return this.containerKey;
	}

	/**
	 * 解包给定错误背后的源对象。对于 Jakarta Bean Validation，
	 * 源对象是 {@link jakarta.validation.ConstraintViolation}。
	 * @param sourceType 预期的源类型
	 * @return 给定类型的源对象
	 * @since 6.2
	 */
	@SuppressWarnings("unchecked")
	public <T> T unwrap(MessageSourceResolvable error, Class<T> sourceType) {
		return (T) this.sourceLookup.apply(error, sourceType);
	}

	@Override
	public boolean equals(@Nullable Object other) {
		if (this == other) {
			return true;
		}
		if (!super.equals(other)) {
			return false;
		}
		return (other instanceof ParameterValidationResult otherResult &&
				getMethodParameter().equals(otherResult.getMethodParameter()) &&
				ObjectUtils.nullSafeEquals(getArgument(), otherResult.getArgument()) &&
				ObjectUtils.nullSafeEquals(getContainerIndex(), otherResult.getContainerIndex()) &&
				ObjectUtils.nullSafeEquals(getContainerKey(), otherResult.getContainerKey()));
	}

	@Override
	public int hashCode() {
		int hashCode = super.hashCode();
		hashCode = 29 * hashCode + getMethodParameter().hashCode();
		hashCode = 29 * hashCode + ObjectUtils.nullSafeHashCode(getArgument());
		hashCode = 29 * hashCode + ObjectUtils.nullSafeHashCode(getContainerIndex());
		hashCode = 29 * hashCode + ObjectUtils.nullSafeHashCode(getContainerKey());
		return hashCode;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " for " + this.methodParameter +
				", argument value '" + ObjectUtils.nullSafeConciseToString(this.argument) + "'," +
				(this.containerIndex != null ? "containerIndex[" + this.containerIndex + "]," : "") +
				(this.containerKey != null ? "containerKey['" + this.containerKey + "']," : "") +
				" errors: " + getResolvableErrors();
	}

}
