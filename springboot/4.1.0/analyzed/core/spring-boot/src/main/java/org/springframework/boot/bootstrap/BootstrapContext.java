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

package org.springframework.boot.bootstrap;

import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;

import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

/**
 * 在启动及 {@link Environment} 后处理阶段、直至 {@link ApplicationContext} 准备完成
 * 之前可用的简单引导上下文。
 * <p>
 * 提供对可能创建成本较高、或需在 {@link ApplicationContext} 可用前共享的单例的
 * 延迟访问。
 * <p>
 * 实例按类型注册。若某类型已注册但未实际提供值，上下文可能返回 {@code null}。
 *
 * @author Phillip Webb
 * @since 4.0.0
 * @since 2.4.0
 * @see BootstrapRegistry
 */
public interface BootstrapContext {

	/**
	 * 若类型已注册则返回上下文中的实例；若此前未访问过则创建实例。
	 * @param <T> 实例类型
	 * @param type 实例类型
	 * @return 上下文管理的实例，可能为 {@code null}
	 * @throws IllegalStateException 若类型尚未注册
	 */
	<T> @Nullable T get(Class<T> type) throws IllegalStateException;

	/**
	 * Return an instance from the context if the type has been registered. The instance
	 * will be created if it hasn't been accessed previously.
	 * @param <T> the instance type
	 * @param type the instance type
	 * @param other 类型未注册时使用的实例
	 * @return 实例，可能为 {@code null}
	 */
	<T> @Nullable T getOrElse(Class<T> type, @Nullable T other);

	/**
	 * Return an instance from the context if the type has been registered. The instance
	 * will be created if it hasn't been accessed previously.
	 * @param <T> the instance type
	 * @param type the instance type
	 * @param other 类型未注册时提供实例的 Supplier
	 * @return the instance, which may be {@code null}
	 */
	<T> @Nullable T getOrElseSupply(Class<T> type, Supplier<@Nullable T> other);

	/**
	 * Return an instance from the context if the type has been registered. The instance
	 * will be created if it hasn't been accessed previously.
	 * @param <T> the instance type
	 * @param <X> 类型未注册时抛出的异常类型
	 * @param type the instance type
	 * @param exceptionSupplier 提供待抛出异常的 Supplier
	 * @return the instance managed by the context, which may be {@code null}
	 * @throws X 若类型尚未注册
	 */
	<T, X extends Throwable> @Nullable T getOrElseThrow(Class<T> type, Supplier<? extends X> exceptionSupplier)
			throws X;

	/**
	 * 返回给定类型是否已有注册。
	 * @param <T> the instance type
	 * @param type the instance type
	 * @return 若类型已注册则为 {@code true}
	 */
	<T> boolean isRegistered(Class<T> type);

}
