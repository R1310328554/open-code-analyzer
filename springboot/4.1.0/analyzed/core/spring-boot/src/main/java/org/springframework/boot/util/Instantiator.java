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

package org.springframework.boot.util;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.jspecify.annotations.Nullable;

import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

/**
 * 通过注入可用构造参数实例化对象的简单工厂。
 * 按参数数量从多到少尝试构造函数，支持类名或 {@link Class} 列表批量实例化。
 *
 * @param <T> the type to instantiate 要实例化的类型
 * @author Phillip Webb
 * @author Scott Frederick
 * @since 2.4.0
 */
public class Instantiator<T> {

	private static final Comparator<Constructor<?>> CONSTRUCTOR_COMPARATOR = Comparator
		.<Constructor<?>>comparingInt(Constructor::getParameterCount)
		.reversed();

	private static final FailureHandler throwingFailureHandler = (type, implementationName, failure) -> {
		throw new IllegalArgumentException("Unable to instantiate " + implementationName + " [" + type.getName() + "]",
				failure);
	};

	private final Class<?> type;

	private final Map<Class<?>, Function<Class<?>, @Nullable Object>> availableParameters;

	private final FailureHandler failureHandler;

	/**
	 * 为给定类型创建新的 {@link Instantiator} 实例。
	 *
	 * @param type the type to instantiate 要实例化的类型
	 * @param availableParameters consumer used to register available parameters 注册可用参数的回调
	 */
	public Instantiator(Class<?> type, Consumer<AvailableParameters> availableParameters) {
		this(type, availableParameters, throwingFailureHandler);
	}

	/**
	 * 为给定类型创建新的 {@link Instantiator} 实例。
	 *
	 * @param type the type to instantiate 要实例化的类型
	 * @param availableParameters consumer used to register available parameters 注册可用参数的回调
	 * @param failureHandler a {@link FailureHandler} that will be called in case of
	 * failure when instantiating objects 实例化失败时调用的处理器
	 * @since 2.7.0
	 */
	public Instantiator(Class<?> type, Consumer<AvailableParameters> availableParameters,
			FailureHandler failureHandler) {
		this.type = type;
		this.availableParameters = getAvailableParameters(availableParameters);
		this.failureHandler = failureHandler;
	}

	private Map<Class<?>, Function<Class<?>, @Nullable Object>> getAvailableParameters(
			Consumer<AvailableParameters> availableParameters) {
		Map<Class<?>, Function<Class<?>, @Nullable Object>> result = new LinkedHashMap<>();
		availableParameters.accept(new AvailableParameters() {

			@Override
			public void add(Class<?> type, @Nullable Object instance) {
				result.put(type, (factoryType) -> instance);
			}

			@Override
			public void add(Class<?> type, Function<Class<?>, @Nullable Object> factory) {
				result.put(type, factory);
			}

		});
		return Collections.unmodifiableMap(result);
	}

	/**
	 * 实例化给定类名集合，必要时注入构造参数。
	 *
	 * @param names the class names to instantiate 要实例化的类名
	 * @return a list of instantiated instances 实例列表
	 */
	public List<T> instantiate(Collection<String> names) {
		return instantiate(null, names);
	}

	/**
	 * 实例化给定类名集合，必要时注入构造参数。
	 *
	 * @param classLoader the source classloader 源类加载器
	 * @param names the class names to instantiate 要实例化的类名
	 * @return a list of instantiated instances 实例列表
	 * @since 2.4.8
	 */
	public List<T> instantiate(@Nullable ClassLoader classLoader, Collection<String> names) {
		Assert.notNull(names, "'names' must not be null");
		return instantiate(names.stream().map((name) -> TypeSupplier.forName(classLoader, name)));
	}

	/**
	 * 实例化给定类名，必要时注入构造参数。
	 *
	 * @param name the class name to instantiate 要实例化的类名
	 * @return an instantiated instance 实例
	 * @since 3.4.0
	 */
	public @Nullable T instantiate(String name) {
		return instantiate(null, name);
	}

	/**
	 * 实例化给定类名，必要时注入构造参数。
	 *
	 * @param classLoader the source classloader 源类加载器
	 * @param name the class name to instantiate 要实例化的类名
	 * @return an instantiated instance 实例
	 * @since 3.4.0
	 */
	public @Nullable T instantiate(@Nullable ClassLoader classLoader, String name) {
		return instantiate(TypeSupplier.forName(classLoader, name));
	}

	/**
	 * 实例化给定类，必要时注入构造参数。
	 *
	 * @param type the type to instantiate 要实例化的类型
	 * @return an instantiated instance 实例
	 * @since 3.4.0
	 */
	public @Nullable T instantiateType(Class<?> type) {
		Assert.notNull(type, "'type' must not be null");
		return instantiate(TypeSupplier.forType(type));
	}

	/**
	 * 实例化给定类集合，必要时注入构造参数。
	 *
	 * @param types the types to instantiate 要实例化的类型
	 * @return a list of instantiated instances 实例列表
	 * @since 2.4.8
	 */
	public List<T> instantiateTypes(Collection<Class<?>> types) {
		Assert.notNull(types, "'types' must not be null");
		return instantiate(types.stream().map(TypeSupplier::forType));
	}

	/**
	 * 获取给定类型的可注入参数实例。可在不使用反射的手动实例化场景中使用。
	 *
	 * @param <A> the argument type 参数类型
	 * @param type the argument type 参数类型
	 * @return the argument to inject or {@code null} 要注入的参数或 {@code null}
	 * @since 3.4.0
	 */
	@SuppressWarnings("unchecked")
	public <A> @Nullable A getArg(Class<A> type) {
		Assert.notNull(type, "'type' must not be null");
		Function<Class<?>, Object> parameter = getAvailableParameter(type);
		Assert.state(parameter != null, "Unknown argument type " + type.getName());
		return (A) parameter.apply(this.type);
	}

	private List<T> instantiate(Stream<TypeSupplier> typeSuppliers) {
		return typeSuppliers.map(this::instantiate).sorted(AnnotationAwareOrderComparator.INSTANCE).toList();
	}

	private @Nullable T instantiate(TypeSupplier typeSupplier) {
		try {
			Class<?> type = typeSupplier.get();
			Assert.state(this.type.isAssignableFrom(type), () -> type + " is not assignable to " + this.type);
			return instantiate(type);
		}
		catch (Throwable ex) {
			this.failureHandler.handleFailure(this.type, typeSupplier.getName(), ex);
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	private T instantiate(Class<?> type) throws Exception {
		Constructor<?>[] constructors = type.getDeclaredConstructors();
		Arrays.sort(constructors, CONSTRUCTOR_COMPARATOR);
		for (Constructor<?> constructor : constructors) {
			Object[] args = getArgs(constructor.getParameterTypes());
			if (args != null) {
				ReflectionUtils.makeAccessible(constructor);
				return (T) constructor.newInstance(args);
			}
		}
		throw new IllegalAccessException("Class [" + type.getName() + "] has no suitable constructor");
	}

	private Object @Nullable [] getArgs(Class<?>[] parameterTypes) {
		Object[] args = new Object[parameterTypes.length];
		for (int i = 0; i < parameterTypes.length; i++) {
			Function<Class<?>, Object> parameter = getAvailableParameter(parameterTypes[i]);
			if (parameter == null) {
				return null;
			}
			args[i] = parameter.apply(this.type);
		}
		return args;
	}

	private @Nullable Function<Class<?>, Object> getAvailableParameter(Class<?> parameterType) {
		for (Map.Entry<Class<?>, Function<Class<?>, Object>> entry : this.availableParameters.entrySet()) {
			if (entry.getKey().isAssignableFrom(parameterType)) {
				return entry.getValue();
			}
		}
		return null;
	}

	/**
	 * 用于注册可用构造参数的回调接口。
	 */
	public interface AvailableParameters {

		/**
		 * 添加带实例值的参数。
		 *
		 * @param type the parameter type 参数类型
		 * @param instance the instance that should be injected 要注入的实例
		 */
		void add(Class<?> type, @Nullable Object instance);

		/**
		 * 添加工厂方式提供的参数。
		 *
		 * @param type the parameter type 参数类型
		 * @param factory the factory used to create the instance that should be injected 创建实例的工厂
		 */
		void add(Class<?> type, Function<Class<?>, @Nullable Object> factory);

	}

	/**
	 * 提供 {@link Class} 类型的 {@link Supplier}。
	 */
	private interface TypeSupplier {

		String getName();

		Class<?> get() throws ClassNotFoundException;

		static TypeSupplier forName(@Nullable ClassLoader classLoader, String name) {
			return new TypeSupplier() {

				@Override
				public String getName() {
					return name;
				}

				@Override
				public Class<?> get() throws ClassNotFoundException {
					return ClassUtils.forName(name, classLoader);
				}

			};
		}

		static TypeSupplier forType(Class<?> type) {
			return new TypeSupplier() {

				@Override
				public String getName() {
					return type.getName();
				}

				@Override
				public Class<?> get() {
					return type;
				}

			};
		}

	}

	/**
	 * 实例化类型失败时的处理策略。
	 *
	 * @since 2.7.0
	 */
	public interface FailureHandler {

		/**
		 * 处理实例化 {@code type} 时发生的 {@code failure}。
		 *
		 * @param type the type 目标类型
		 * @param implementationName the name of the implementation type 实现类名
		 * @param failure the failure that occurred 发生的异常
		 */
		void handleFailure(Class<?> type, String implementationName, Throwable failure);

	}

}
