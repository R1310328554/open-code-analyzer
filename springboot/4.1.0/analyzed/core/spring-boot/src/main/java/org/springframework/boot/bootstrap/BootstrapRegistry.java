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
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;

/**
 * 在启动及 {@link Environment} 后处理阶段、直至 {@link ApplicationContext} 准备完成
 * 之前可用的简单对象注册表。
 * <p>
 * 可用于注册创建成本较高、或需在 {@link ApplicationContext} 可用前共享的实例。
 * <p>
 * 注册表以 {@link Class} 为键，即每种类型只能存储一个实例。
 * <p>
 * 可通过 {@link #addCloseListener(ApplicationListener)} 添加监听器，在
 * {@link BootstrapContext} 关闭且 {@link ApplicationContext} 完全准备就绪后执行操作。
 * 例如，某实例可注册为常规 Spring Bean 供应用使用。
 *
 * @author Phillip Webb
 * @since 4.0.0
 * @see BootstrapContext
 * @see ConfigurableBootstrapContext
 */
public interface BootstrapRegistry {

	/**
	 * 向注册表注册指定类型。若该类型已注册且尚未作为 {@link Scope#SINGLETON 单例}
	 * 获取，则会被替换。
	 * @param <T> 实例类型
	 * @param type 实例类型
	 * @param instanceSupplier 实例供应器
	 */
	<T> void register(Class<T> type, InstanceSupplier<T> instanceSupplier);

	/**
	 * 若尚未存在则向注册表注册指定类型。
	 * @param <T> the instance type
	 * @param type the instance type
	 * @param instanceSupplier the instance supplier
	 */
	<T> void registerIfAbsent(Class<T> type, InstanceSupplier<T> instanceSupplier);

	/**
	 * Return if a registration exists for the given type.
	 * @param <T> the instance type
	 * @param type the instance type
	 * @return 若类型已注册则为 {@code true}
	 */
	<T> boolean isRegistered(Class<T> type);

	/**
	 * 返回给定类型已注册的 {@link InstanceSupplier}。
	 * @param <T> the instance type
	 * @param type the instance type
	 * @return 已注册的 {@link InstanceSupplier}，或 {@code null}
	 */
	<T> @Nullable InstanceSupplier<T> getRegisteredInstanceSupplier(Class<T> type);

	/**
	 * 添加 {@link ApplicationListener}，在 {@link BootstrapContext} 关闭且
	 * {@link ApplicationContext} 准备完成后以 {@link BootstrapContextClosedEvent} 调用。
	 * @param listener 要添加的监听器
	 */
	void addCloseListener(ApplicationListener<BootstrapContextClosedEvent> listener);

	/**
	 * 在需要时提供实际实例的供应器。
	 *
	 * @param <T> the instance type
	 * @see Scope
	 */
	@FunctionalInterface
	interface InstanceSupplier<T> {

		/**
		 * 在需要时创建实例的工厂方法。
		 * @param context 可用于获取其他引导实例的 {@link BootstrapContext}
		 * @return 实例，或 {@code null}
		 */
		@Nullable T get(BootstrapContext context);

		/**
		 * 返回所供应实例的作用域。
		 * @return 作用域
		 */
		default Scope getScope() {
			return Scope.SINGLETON;
		}

		/**
		 * 返回具有更新后 {@link Scope} 的新 {@link InstanceSupplier}。
		 * @param scope 新作用域
		 * @return 具有新作用域的新 {@link InstanceSupplier} 实例
		 */
		default InstanceSupplier<T> withScope(Scope scope) {
			Assert.notNull(scope, "'scope' must not be null");
			InstanceSupplier<T> parent = this;
			return new InstanceSupplier<>() {

				@Override
				public @Nullable T get(BootstrapContext context) {
					return parent.get(context);
				}

				@Override
				public Scope getScope() {
					return scope;
				}

			};
		}

		/**
		 * 为给定实例创建 {@link InstanceSupplier} 的工厂方法。
		 * @param <T> the instance type
		 * @param instance 实例
		 * @return 新的 {@link InstanceSupplier}
		 */
		static <T> InstanceSupplier<T> of(@Nullable T instance) {
			return (registry) -> instance;
		}

		/**
		 * 从 {@link Supplier} 创建 {@link InstanceSupplier} 的工厂方法。
		 * @param <T> the instance type
		 * @param supplier 提供实例的 Supplier
		 * @return a new {@link InstanceSupplier}
		 */
		static <T> InstanceSupplier<T> from(@Nullable Supplier<T> supplier) {
			return (registry) -> (supplier != null) ? supplier.get() : null;
		}

	}

	/**
	 * 实例的作用域。
	 */
	enum Scope {

		/**
		 * 单例实例。{@link InstanceSupplier} 仅调用一次，每次返回同一实例。
		 */
		SINGLETON,

		/**
		 * 原型实例。每次需要实例时都会调用 {@link InstanceSupplier}。
		 */
		PROTOTYPE

	}

}
