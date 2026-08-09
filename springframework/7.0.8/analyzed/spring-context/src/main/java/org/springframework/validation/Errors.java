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

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.PropertyAccessor;

/**
 * 存储并暴露特定对象的数据绑定与校验错误信息。
 *
 * <p>字段名通常是目标对象的属性（例如绑定到 customer 对象时为 "name"）。
 * 实现也可支持嵌套对象的嵌套字段（例如 "address.street"），
 * 配合 {@link #setNestedPath} 进行子树导航：
 * 例如 {@code AddressValidator} 可校验 "address"，
 * 而无需知晓它是顶层 customer 对象的嵌套对象。
 *
 * <p>注意：{@code Errors} 对象非线程安全。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see Validator
 * @see ValidationUtils
 * @see SimpleErrors
 * @see BindingResult
 */
public interface Errors {

	/**
	 * 嵌套路径中路径元素之间的分隔符，
	 * 例如 "customer.name" 或 "customer.address.street"。
	 * <p>"." 与 beans 包中
	 * {@link org.springframework.beans.PropertyAccessor#NESTED_PROPERTY_SEPARATOR 嵌套属性分隔符} 相同。
	 */
	String NESTED_PATH_SEPARATOR = PropertyAccessor.NESTED_PROPERTY_SEPARATOR;


	/**
	 * 返回被绑定根对象的名称。
	 */
	String getObjectName();

	/**
	 * 允许更改上下文，使标准校验器可校验子树。
	 * reject 调用会将给定路径前缀到字段名。
	 * <p>例如地址校验器可校验 customer 对象的 "address" 子对象。
	 * <p>默认实现抛出 {@code UnsupportedOperationException}，
	 * 因并非所有 {@code Errors} 实现都支持嵌套路径。
	 * @param nestedPath 本对象内的嵌套路径，
	 * 例如 "address"（默认为 ""，{@code null} 也可接受）。
	 * 可以点结尾："address" 与 "address." 均有效。
	 * @see #getNestedPath()
	 */
	default void setNestedPath(String nestedPath) {
		throw new UnsupportedOperationException(getClass().getSimpleName() + " does not support nested paths");
	}

	/**
	 * 返回本 {@link Errors} 对象的当前嵌套路径。
	 * <p>返回带点号的嵌套路径，即 "address."，便于拼接路径。默认为空字符串。
	 * @see #setNestedPath(String)
	 */
	default String getNestedPath() {
		return "";
	}

	/**
	 * 将给定子路径压入嵌套路径栈。
	 * <p>调用 {@link #popNestedPath()} 会恢复对应 {@code pushNestedPath(String)} 调用前的原始嵌套路径。
	 * <p>使用嵌套路径栈可为子对象设置临时嵌套路径，无需额外临时路径持有者。
	 * <p>例如：当前路径 "spouse."，pushNestedPath("child") &rarr;
	 * 结果路径 "spouse.child."；popNestedPath() &rarr; 恢复为 "spouse."。
	 * <p>默认实现抛出 {@code UnsupportedOperationException}，
	 * 因并非所有 {@code Errors} 实现都支持嵌套路径。
	 * @param subPath 要压入嵌套路径栈的子路径
	 * @see #popNestedPath()
	 */
	default void pushNestedPath(String subPath) {
		throw new UnsupportedOperationException(getClass().getSimpleName() + " does not support nested paths");
	}

	/**
	 * 从嵌套路径栈弹出先前的嵌套路径。
	 * @throws IllegalStateException 若栈上无先前的嵌套路径
	 * @see #pushNestedPath(String)
	 */
	default void popNestedPath() throws IllegalStateException {
		throw new IllegalStateException("Cannot pop nested path: no nested path on stack");
	}

	/**
	 * 使用给定错误描述为整个目标对象注册全局错误。
	 * @param errorCode 错误码，可解释为消息键
	 * @see #reject(String, Object[], String)
	 */
	default void reject(String errorCode) {
		reject(errorCode, null, null);
	}

	/**
	 * 使用给定错误描述为整个目标对象注册全局错误。
	 * @param errorCode 错误码，可解释为消息键
	 * @param defaultMessage 后备默认消息
	 * @see #reject(String, Object[], String)
	 */
	default void reject(String errorCode, String defaultMessage) {
		reject(errorCode, null, defaultMessage);
	}

	/**
	 * 使用给定错误描述为整个目标对象注册全局错误。
	 * @param errorCode 错误码，可解释为消息键
	 * @param errorArgs 错误参数，用于 MessageFormat 参数绑定（可为 {@code null}）
	 * @param defaultMessage 后备默认消息
	 * @see #rejectValue(String, String, Object[], String)
	 */
	void reject(String errorCode, Object @Nullable [] errorArgs, @Nullable String defaultMessage);

	/**
	 * 使用给定错误描述为当前对象（若有则尊重当前嵌套路径）的指定字段注册字段错误。
	 * <p>字段名可为 {@code null} 或空字符串，表示当前对象本身而非其字段。
	 * 这可能在嵌套对象图中产生对应字段错误，
	 * 若当前对象为顶层对象则产生全局错误。
	 * @param field 字段名（可为 {@code null} 或空字符串）
	 * @param errorCode 错误码，可解释为消息键
	 * @see #rejectValue(String, String, Object[], String)
	 */
	default void rejectValue(@Nullable String field, String errorCode) {
		rejectValue(field, errorCode, null, null);
	}

	/**
	 * 使用给定错误描述为当前对象（若有则尊重当前嵌套路径）的指定字段注册字段错误。
	 * <p>字段名可为 {@code null} 或空字符串，表示当前对象本身而非其字段。
	 * 这可能在嵌套对象图中产生对应字段错误，
	 * 若当前对象为顶层对象则产生全局错误。
	 * @param field 字段名（可为 {@code null} 或空字符串）
	 * @param errorCode 错误码，可解释为消息键
	 * @param defaultMessage 后备默认消息
	 * @see #rejectValue(String, String, Object[], String)
	 */
	default void rejectValue(@Nullable String field, String errorCode, String defaultMessage) {
		rejectValue(field, errorCode, null, defaultMessage);
	}

	/**
	 * 使用给定错误描述为当前对象（若有则尊重当前嵌套路径）的指定字段注册字段错误。
	 * <p>字段名可为 {@code null} 或空字符串，表示当前对象本身而非其字段。
	 * 这可能在嵌套对象图中产生对应字段错误，
	 * 若当前对象为顶层对象则产生全局错误。
	 * @param field 字段名（可为 {@code null} 或空字符串）
	 * @param errorCode 错误码，可解释为消息键
	 * @param errorArgs 错误参数，用于 MessageFormat 参数绑定（可为 {@code null}）
	 * @param defaultMessage 后备默认消息
	 * @see #reject(String, Object[], String)
	 */
	void rejectValue(@Nullable String field, String errorCode,
			Object @Nullable [] errorArgs, @Nullable String defaultMessage);

	/**
	 * 将给定 {@code Errors} 实例的所有错误添加到本 {@code Errors} 实例。
	 * <p>便捷方法，避免为合并 Errors 实例而重复调用 {@code reject(..)}。
	 * <p>注意：传入的 {@code Errors} 实例应指向同一目标对象，
	 * 或至少包含适用于本 Errors 实例目标对象的兼容错误。
	 * <p>默认实现抛出 {@code UnsupportedOperationException}，
	 * 因并非所有 {@code Errors} 实现都支持 {@code #addAllErrors}。
	 * @param errors 要合并的 {@code Errors} 实例
	 * @see #getAllErrors()
	 */
	default void addAllErrors(Errors errors) {
		throw new UnsupportedOperationException(getClass().getSimpleName() + " does not support addAllErrors");
	}

	/**
	 * 抛出映射异常，消息汇总已记录的错误。
	 * @param messageToException 将消息映射为异常的函数，
	 * 例如 {@code IllegalArgumentException::new} 或 {@code IllegalStateException::new}
	 * @param <T> 要抛出的异常类型
	 * @since 6.1
	 * @see #toString()
	 */
	default <T extends Throwable> void failOnError(Function<String, T> messageToException) throws T {
		if (hasErrors()) {
			throw messageToException.apply(toString());
		}
	}

	/**
	 * 判断是否存在任何错误。
	 * @see #hasGlobalErrors()
	 * @see #hasFieldErrors()
	 */
	default boolean hasErrors() {
		return (!getGlobalErrors().isEmpty() || !getFieldErrors().isEmpty());
	}

	/**
	 * 确定错误总数。
	 * @see #getGlobalErrorCount()
	 * @see #getFieldErrorCount()
	 */
	default int getErrorCount() {
		return (getGlobalErrors().size() + getFieldErrors().size());
	}

	/**
	 * 获取所有错误，包括全局错误与字段错误。
	 * @return {@link ObjectError}/{@link FieldError} 实例列表
	 * @see #getGlobalErrors()
	 * @see #getFieldErrors()
	 */
	default List<ObjectError> getAllErrors() {
		return Stream.concat(getGlobalErrors().stream(), getFieldErrors().stream()).toList();
	}

	/**
	 * 判断是否存在全局错误。
	 * @see #hasFieldErrors()
	 */
	default boolean hasGlobalErrors() {
		return !getGlobalErrors().isEmpty();
	}

	/**
	 * 确定全局错误数量。
	 * @see #getFieldErrorCount()
	 */
	default int getGlobalErrorCount() {
		return getGlobalErrors().size();
	}

	/**
	 * 获取所有全局错误。
	 * @return {@link ObjectError} 实例列表
	 * @see #getFieldErrors()
	 */
	List<ObjectError> getGlobalErrors();

	/**
	 * 获取<i>第一个</i>全局错误（若有）。
	 * @return 全局错误，或 {@code null}
	 * @see #getFieldError()
	 */
	default @Nullable ObjectError getGlobalError() {
		return getGlobalErrors().stream().findFirst().orElse(null);
	}

	/**
	 * 判断是否存在与字段相关的错误。
	 * @see #hasGlobalErrors()
	 */
	default boolean hasFieldErrors() {
		return !getFieldErrors().isEmpty();
	}

	/**
	 * 确定与字段相关的错误数量。
	 * @see #getGlobalErrorCount()
	 */
	default int getFieldErrorCount() {
		return getFieldErrors().size();
	}

	/**
	 * 获取与字段相关的所有错误。
	 * @return {@link FieldError} 实例列表
	 * @see #getGlobalErrors()
	 */
	List<FieldError> getFieldErrors();

	/**
	 * 获取与字段相关的<i>第一个</i>错误（若有）。
	 * @return 字段特定错误，或 {@code null}
	 * @see #getGlobalError()
	 */
	default @Nullable FieldError getFieldError() {
		return getFieldErrors().stream().findFirst().orElse(null);
	}

	/**
	 * 判断给定字段是否存在相关错误。
	 * @param field 字段名
	 * @see #hasFieldErrors()
	 */
	default boolean hasFieldErrors(String field) {
		return (getFieldError(field) != null);
	}

	/**
	 * 确定给定字段相关错误的数量。
	 * @param field 字段名
	 * @see #getFieldErrorCount()
	 */
	default int getFieldErrorCount(String field) {
		return getFieldErrors(field).size();
	}

	/**
	 * 获取与给定字段相关的所有错误。
	 * <p>实现可支持完整字段名（如 "address.street"）
	 * 以及模式匹配（如 "address.*"）。
	 * @param field 字段名
	 * @return {@link FieldError} 实例列表
	 * @see #getFieldErrors()
	 */
	default List<FieldError> getFieldErrors(String field) {
		return getFieldErrors().stream().filter(error -> field.equals(error.getField())).toList();
	}

	/**
	 * 获取与给定字段相关的第一个错误（若有）。
	 * @param field 字段名
	 * @return 字段特定错误，或 {@code null}
	 * @see #getFieldError()
	 */
	default @Nullable FieldError getFieldError(String field) {
		return getFieldErrors().stream().filter(error -> field.equals(error.getField())).findFirst().orElse(null);
	}

	/**
	 * 返回给定字段的当前值，可能是当前 bean 属性值或上次绑定中被拒绝的更新值。
	 * <p>便于访问用户指定的字段值，即使存在类型不匹配。
	 * @param field 字段名
	 * @return 给定字段的当前值
	 * @see #getFieldType(String)
	 */
	@Nullable Object getFieldValue(String field);

	/**
	 * 尽可能确定给定字段的类型。
	 * <p>实现应能在字段值为 {@code null} 时仍确定类型，
	 * 例如通过关联描述符。
	 * @param field 字段名
	 * @return 字段类型，无法确定时返回 {@code null}
	 * @see #getFieldValue(String)
	 */
	default @Nullable Class<?> getFieldType(String field) {
		return Optional.ofNullable(getFieldValue(field)).map(Object::getClass).orElse(null);
	}

	/**
	 * 返回已记录错误的摘要，例如用于异常消息。
	 * @see #failOnError(Function)
	 */
	@Override
	String toString();

}
