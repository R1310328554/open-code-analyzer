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

package org.springframework.beans.factory.support;

import java.lang.reflect.Method;
import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;

import org.springframework.util.Assert;
import org.springframework.util.function.ThrowingBiFunction;
import org.springframework.util.function.ThrowingSupplier;

/**
 * 专用 {@link Supplier}，当需要 {@link RegisteredBean 已注册 Bean} 的详细信息
 * 来提供实例时，可设置到
 * {@link AbstractBeanDefinition#setInstanceSupplier(Supplier) BeanDefinition} 上。
 *
 * @author Phillip Webb
 * @author Stephane Nicoll
 * @since 6.0
 * @param <T> 本供应商提供的实例类型
 * @see RegisteredBean
 * @see org.springframework.beans.factory.aot.BeanInstanceSupplier
 */
@FunctionalInterface
public interface InstanceSupplier<T> extends ThrowingSupplier<T> {

	@Override
	default T getWithException() {
		throw new IllegalStateException("No RegisteredBean parameter provided");
	}

	/**
	 * 获取提供的实例。
	 * @param registeredBean 请求实例的已注册 Bean
	 * @return 提供的实例
	 * @throws Exception 发生错误时
	 */
	T get(RegisteredBean registeredBean) throws Exception;

	/**
	 * 返回本供应商用于创建实例的工厂方法，
	 * 若未知或使用其他方式则为 {@code null}。
	 * @return 用于创建实例的工厂方法，或 {@code null}
	 */
	default @Nullable Method getFactoryMethod() {
		return null;
	}

	/**
	 * 返回组合实例供应商：先从本供应商获取实例，
	 * 再应用 {@code after} 函数得到结果。
	 * @param <V> {@code after} 函数的输出类型，也是组合函数的结果类型
	 * @param after 获取实例后要应用的函数
	 * @return 组合后的实例供应商
	 */
	default <V> InstanceSupplier<V> andThen(
			ThrowingBiFunction<RegisteredBean, ? super T, ? extends V> after) {

		Assert.notNull(after, "'after' function must not be null");
		return new InstanceSupplier<>() {
			@Override
			public V get(RegisteredBean registeredBean) throws Exception {
				return after.applyWithException(registeredBean, InstanceSupplier.this.get(registeredBean));
			}
			@Override
			public @Nullable Method getFactoryMethod() {
				return InstanceSupplier.this.getFactoryMethod();
			}
		};
	}

	/**
	 * 从 {@link ThrowingSupplier} 创建 {@link InstanceSupplier} 的工厂方法。
	 * @param <T> 本供应商提供的实例类型
	 * @param supplier 源供应商
	 * @return 新的 {@link InstanceSupplier}
	 */
	static <T> InstanceSupplier<T> using(ThrowingSupplier<T> supplier) {
		Assert.notNull(supplier, "Supplier must not be null");
		if (supplier instanceof InstanceSupplier<T> instanceSupplier) {
			return instanceSupplier;
		}
		return registeredBean -> supplier.getWithException();
	}

	/**
	 * 从 {@link ThrowingSupplier} 创建 {@link InstanceSupplier} 的工厂方法。
	 * @param <T> 本供应商提供的实例类型
	 * @param factoryMethod 使用的工厂方法
	 * @param supplier 源供应商
	 * @return 新的 {@link InstanceSupplier}
	 */
	static <T> InstanceSupplier<T> using(@Nullable Method factoryMethod, ThrowingSupplier<T> supplier) {
		Assert.notNull(supplier, "Supplier must not be null");

		if (supplier instanceof InstanceSupplier<T> instanceSupplier &&
				instanceSupplier.getFactoryMethod() == factoryMethod) {
			return instanceSupplier;
		}

		return new InstanceSupplier<>() {
			@Override
			public T get(RegisteredBean registeredBean) throws Exception {
				return supplier.getWithException();
			}
			@Override
			public @Nullable Method getFactoryMethod() {
				return factoryMethod;
			}
		};
	}

	/**
	 * Lambda 友好方法，可在单次调用中创建 {@link InstanceSupplier} 并添加后处理器。
	 * 例如：{@code InstanceSupplier.of(registeredBean -> ...).andThen(...)}。
	 * @param <T> 本供应商提供的实例类型
	 * @param instanceSupplier 源实例供应商
	 * @return 新的 {@link InstanceSupplier}
	 */
	static <T> InstanceSupplier<T> of(InstanceSupplier<T> instanceSupplier) {
		Assert.notNull(instanceSupplier, "InstanceSupplier must not be null");
		return instanceSupplier;
	}

}
