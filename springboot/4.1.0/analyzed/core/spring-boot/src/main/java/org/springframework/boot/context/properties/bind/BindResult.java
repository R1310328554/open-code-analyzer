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

import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;

import org.springframework.lang.Contract;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

/**
 * 用于返回 {@link Binder} 绑定操作结果的容器对象。可能包含成功绑定的对象，也可能为空结果。
 *
 * @param <T> 结果类型
 * @author Phillip Webb
 * @author Madhura Bhave
 * @since 2.0.0
 */
public final class BindResult<T> {

	private static final BindResult<?> UNBOUND = new BindResult<>(null);

	private final @Nullable T value;

	private BindResult(@Nullable T value) {
		this.value = value;
	}

	/**
	 * 返回已绑定的对象；若未绑定任何值则抛出 {@link NoSuchElementException}。
	 *
	 * @return 绑定的值（永不为 {@code null}）
	 * @throws NoSuchElementException 未绑定任何值时抛出
	 * @see #isBound()
	 */
	public T get() throws NoSuchElementException {
		if (this.value == null) {
			throw new NoSuchElementException("No value bound");
		}
		return this.value;
	}

	/**
	 * 若已绑定结果则返回 {@code true}。
	 *
	 * @return 是否已绑定结果
	 */
	public boolean isBound() {
		return (this.value != null);
	}

	/**
	 * 使用绑定的值调用指定 consumer；若未绑定任何值则不执行任何操作。
	 *
	 * @param consumer 已绑定值时执行的代码块
	 */
	public void ifBound(Consumer<? super T> consumer) {
		Assert.notNull(consumer, "'consumer' must not be null");
		if (this.value != null) {
			consumer.accept(this.value);
		}
	}

	/**
	 * 对绑定的值应用提供的映射函数；若未绑定任何值则返回更新后的未绑定结果。
	 *
	 * @param <U> 映射函数的结果类型
	 * @param mapper 应用于绑定值的映射函数；未绑定任何值时不会调用
	 * @return 描述对此 {@code BindResult} 的值应用映射函数后的 {@code BindResult}
	 */
	public <U> BindResult<U> map(Function<? super T, ? extends U> mapper) {
		Assert.notNull(mapper, "'mapper' must not be null");
		return of((this.value != null) ? mapper.apply(this.value) : null);
	}

	/**
	 * 返回已绑定的对象；若未绑定任何值则返回 {@code other}。
	 *
	 * @param other 无绑定值时返回的值（可为 {@code null}）
	 * @return 已绑定则返回值，否则返回 {@code other}
	 */
	@Contract("!null -> !null")
	public @Nullable T orElse(@Nullable T other) {
		return (this.value != null) ? this.value : other;
	}

	/**
	 * 返回已绑定的对象；若未绑定任何值则调用 {@code other} 并返回其结果。
	 *
	 * @param other 无绑定值时提供值的 {@link Supplier}
	 * @return 已绑定则返回值，否则返回 {@code other} 提供的值
	 */
	public T orElseGet(Supplier<? extends T> other) {
		return (this.value != null) ? this.value : other.get();
	}

	/**
	 * 返回已绑定的对象；若未绑定任何值则通过提供的 supplier 创建并抛出异常。
	 *
	 * @param <X> 要抛出的异常类型
	 * @param exceptionSupplier 提供待抛出异常的 supplier
	 * @return 当前值
	 * @throws X 无值时抛出
	 */
	public <X extends Throwable> T orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
		if (this.value == null) {
			throw exceptionSupplier.get();
		}
		return this.value;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		return ObjectUtils.nullSafeEquals(this.value, ((BindResult<?>) obj).value);
	}

	@Override
	public int hashCode() {
		return ObjectUtils.nullSafeHashCode(this.value);
	}

	@SuppressWarnings("unchecked")
	static <T> BindResult<T> of(@Nullable T value) {
		if (value == null) {
			return (BindResult<T>) UNBOUND;
		}
		return new BindResult<>(value);
	}

}
