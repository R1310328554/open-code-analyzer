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

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;

import org.springframework.core.ResolvableType;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

/**
 * 以安全方式调用 lambda 的工具类。
 * 主要用于支持泛型回调：因类型擦除导致的 {@link ClassCastException 类型转换异常}
 * 会被识别并优雅处理，而非直接抛出。
 *
 * @author Phillip Webb
 * @since 2.0.0
 */
public final class LambdaSafe {

	private static final @Nullable Method CLASS_GET_MODULE;

	private static final @Nullable Method MODULE_GET_NAME;

	static {
		CLASS_GET_MODULE = ReflectionUtils.findMethod(Class.class, "getModule");
		MODULE_GET_NAME = (CLASS_GET_MODULE != null)
				? ReflectionUtils.findMethod(CLASS_GET_MODULE.getReturnType(), "getName") : null;
	}

	private LambdaSafe() {
	}

	/**
	 * 发起对单个回调实例的调用，处理常见的泛型类型匹配与异常。
	 *
	 * @param callbackType the callback type (a {@link FunctionalInterface functional
	 * interface}) 回调类型（{@link FunctionalInterface 函数式接口}）
	 * @param callbackInstance the callback instance (may be a lambda) 回调实例（可为 lambda）
	 * @param argument the primary argument passed to the callback 传给回调的主参数
	 * @param additionalArguments any additional arguments passed to the callback 传给回调的附加参数
	 * @param <C> the callback type 回调类型
	 * @param <A> the primary argument type 主参数类型
	 * @return a {@link Callback} instance that can be invoked 可调用的 {@link Callback} 实例
	 */
	public static <C, A> Callback<C, A> callback(Class<C> callbackType, C callbackInstance, A argument,
			@Nullable Object @Nullable ... additionalArguments) {
		Assert.notNull(callbackType, "'callbackType' must not be null");
		Assert.notNull(callbackInstance, "'callbackInstance' must not be null");
		return new Callback<>(callbackType, callbackInstance, argument, additionalArguments);
	}

	/**
	 * 发起对多个回调实例的调用，处理常见的泛型类型匹配与异常。
	 *
	 * @param callbackType the callback type (a {@link FunctionalInterface functional
	 * interface}) 回调类型（{@link FunctionalInterface 函数式接口}）
	 * @param callbackInstances the callback instances (elements may be lambdas) 回调实例集合（元素可为 lambda）
	 * @param argument the primary argument passed to the callbacks 传给各回调的主参数
	 * @param additionalArguments any additional arguments passed to the callbacks 传给各回调的附加参数
	 * @param <C> the callback type 回调类型
	 * @param <A> the primary argument type 主参数类型
	 * @return a {@link Callbacks} instance that can be invoked 可调用的 {@link Callbacks} 实例
	 */
	public static <C, A> Callbacks<C, A> callbacks(Class<C> callbackType, Collection<? extends C> callbackInstances,
			A argument, Object... additionalArguments) {
		Assert.notNull(callbackType, "'callbackType' must not be null");
		Assert.notNull(callbackInstances, "'callbackInstances' must not be null");
		return new Callbacks<>(callbackType, callbackInstances, argument, additionalArguments);
	}

	/**
	 * lambda 安全回调的抽象基类。
	 *
	 * @param <C> the callback type 回调类型
	 * @param <A> the primary argument type 主参数类型
	 * @param <SELF> the self class reference 自引用类型
	 */
	protected abstract static class LambdaSafeCallback<C, A, SELF extends LambdaSafeCallback<C, A, SELF>> {

		private final Class<C> callbackType;

		private final A argument;

		private final @Nullable Object @Nullable [] additionalArguments;

		private Log logger;

		private Filter<C, A> filter = new GenericTypeFilter<>();

		LambdaSafeCallback(Class<C> callbackType, A argument, @Nullable Object @Nullable [] additionalArguments) {
			this.callbackType = callbackType;
			this.argument = argument;
			this.additionalArguments = additionalArguments;
			this.logger = LogFactory.getLog(callbackType);
		}

		/**
		 * 使用指定 logger 源报告 lambda 调用失败。
		 *
		 * @param loggerSource the logger source to use 用作 logger 源的类
		 * @return this instance 当前实例
		 */
		public SELF withLogger(Class<?> loggerSource) {
			return withLogger(LogFactory.getLog(loggerSource));
		}

		/**
		 * 使用指定 logger 报告 lambda 调用失败。
		 *
		 * @param logger the logger to use 待使用的 logger
		 * @return this instance 当前实例
		 */
		public SELF withLogger(Log logger) {
			Assert.notNull(logger, "'logger' must not be null");
			this.logger = logger;
			return self();
		}

		/**
		 * 使用指定过滤器决定回调何时适用。
		 * 若未显式设置过滤器，将尝试根据回调类型的泛型参数进行匹配。
		 *
		 * @param filter the filter to use 待使用的过滤器
		 * @return this instance 当前实例
		 * @since 3.4.8
		 */
		public SELF withFilter(Filter<C, A> filter) {
			Assert.notNull(filter, "'filter' must not be null");
			this.filter = filter;
			return self();
		}

		protected final <R> InvocationResult<R> invoke(C callbackInstance, Supplier<@Nullable R> supplier) {
			if (this.filter.match(this.callbackType, callbackInstance, this.argument, this.additionalArguments)) {
				try {
					return InvocationResult.of(supplier.get());
				}
				catch (ClassCastException ex) {
					if (!isLambdaGenericProblem(ex)) {
						throw ex;
					}
					logNonMatchingType(callbackInstance, ex);
				}
			}
			return InvocationResult.noResult();
		}

		private boolean isLambdaGenericProblem(ClassCastException ex) {
			return (ex.getMessage() == null || startsWithArgumentClassName(ex.getMessage()));
		}

		private boolean startsWithArgumentClassName(String message) {
			Predicate<@Nullable Object> startsWith = (argument) -> startsWithArgumentClassName(message, argument);
			return startsWith.test(this.argument) || additionalArgumentsStartsWith(startsWith);
		}

		private boolean additionalArgumentsStartsWith(Predicate<@Nullable Object> startsWith) {
			if (this.additionalArguments == null) {
				return false;
			}
			return Stream.of(this.additionalArguments).anyMatch(startsWith);
		}

		private boolean startsWithArgumentClassName(String message, @Nullable Object argument) {
			if (argument == null) {
				return false;
			}
			Class<?> argumentType = argument.getClass();
			// Java 8 上消息以类名开头："java.lang.String cannot be cast..."
			if (message.startsWith(argumentType.getName())) {
				return true;
			}
			// Java 11 上消息以 "class ..." 开头，即 Class.toString() 的形式
			if (message.startsWith(argumentType.toString())) {
				return true;
			}
			// Java 9 上消息曾包含模块名：
			// "java.base/java.lang.String cannot be cast..."
			int moduleSeparatorIndex = message.indexOf('/');
			if (moduleSeparatorIndex != -1 && message.startsWith(argumentType.getName(), moduleSeparatorIndex + 1)) {
				return true;
			}
			if (CLASS_GET_MODULE != null && MODULE_GET_NAME != null) {
				Object module = ReflectionUtils.invokeMethod(CLASS_GET_MODULE, argumentType);
				Object moduleName = ReflectionUtils.invokeMethod(MODULE_GET_NAME, module);
				return message.startsWith(moduleName + "/" + argumentType.getName());
			}
			return false;
		}

		private void logNonMatchingType(C callback, ClassCastException ex) {
			if (this.logger.isDebugEnabled()) {
				Class<?> expectedType = ResolvableType.forClass(this.callbackType).resolveGeneric();
				String expectedTypeName = (expectedType != null) ? ClassUtils.getShortName(expectedType) + " type"
						: "type";
				String message = "Non-matching " + expectedTypeName + " for callback "
						+ ClassUtils.getShortName(this.callbackType) + ": " + callback;
				this.logger.debug(message, ex);
			}
		}

		@SuppressWarnings("unchecked")
		private SELF self() {
			return (SELF) this;
		}

	}

	/**
	 * 表示单个可以 lambda 安全方式调用的回调。
	 *
	 * @param <C> the callback type 回调类型
	 * @param <A> the primary argument type 主参数类型
	 */
	public static final class Callback<C, A> extends LambdaSafeCallback<C, A, Callback<C, A>> {

		private final C callbackInstance;

		private Callback(Class<C> callbackType, C callbackInstance, A argument,
				@Nullable Object @Nullable [] additionalArguments) {
			super(callbackType, argument, additionalArguments);
			this.callbackInstance = callbackInstance;
		}

		/**
		 * 调用返回 void 的回调实例。
		 *
		 * @param invoker the invoker used to invoke the callback 用于调用回调的 invoker
		 */
		public void invoke(Consumer<C> invoker) {
			Supplier<@Nullable Void> supplier = () -> {
				invoker.accept(this.callbackInstance);
				return null;
			};
			invoke(this.callbackInstance, supplier);
		}

		/**
		 * 调用返回结果的回调实例。
		 *
		 * @param invoker the invoker used to invoke the callback 用于调用回调的 invoker
		 * @param <R> the result type 结果类型
		 * @return the result of the invocation (may be {@link InvocationResult#noResult}
		 * if the callback was not invoked) 调用结果（若回调未执行则可能为 {@link InvocationResult#noResult}）
		 */
		public <R> InvocationResult<R> invokeAnd(Function<C, @Nullable R> invoker) {
			Supplier<@Nullable R> supplier = () -> invoker.apply(this.callbackInstance);
			return invoke(this.callbackInstance, supplier);
		}

	}

	/**
	 * 表示一组可以 lambda 安全方式调用的回调。
	 *
	 * @param <C> the callback type 回调类型
	 * @param <A> the primary argument type 主参数类型
	 */
	public static final class Callbacks<C, A> extends LambdaSafeCallback<C, A, Callbacks<C, A>> {

		private final Collection<? extends C> callbackInstances;

		private Callbacks(Class<C> callbackType, Collection<? extends C> callbackInstances, A argument,
				Object[] additionalArguments) {
			super(callbackType, argument, additionalArguments);
			this.callbackInstances = callbackInstances;
		}

		/**
		 * 调用返回 void 的各回调实例。
		 *
		 * @param invoker the invoker used to invoke the callback 用于调用回调的 invoker
		 */
		public void invoke(Consumer<C> invoker) {
			this.callbackInstances.forEach((callbackInstance) -> {
				Supplier<@Nullable Void> supplier = () -> {
					invoker.accept(callbackInstance);
					return null;
				};
				invoke(callbackInstance, supplier);
			});
		}

		/**
		 * 调用返回结果的各回调实例。
		 *
		 * @param invoker the invoker used to invoke the callback 用于调用回调的 invoker
		 * @param <R> the result type 结果类型
		 * @return the results of the invocation (may be an empty stream if no callbacks
		 * could be called) 调用结果流（若无回调被调用则可能为空）
		 */
		public <R> Stream<R> invokeAnd(Function<C, @Nullable R> invoker) {
			Function<C, InvocationResult<R>> mapper = (callbackInstance) -> {
				Supplier<@Nullable R> supplier = () -> invoker.apply(callbackInstance);
				return invoke(callbackInstance, supplier);
			};
			return this.callbackInstances.stream()
				.map(mapper)
				.filter(InvocationResult::hasResult)
				.map(InvocationResult::get);
		}

	}

	/**
	 * 用于限制回调何时被使用的过滤器。
	 *
	 * @param <C> the callback type 回调类型
	 * @param <A> the primary argument type 主参数类型
	 * @since 3.4.8
	 */
	@FunctionalInterface
	public interface Filter<C, A> {

		/**
		 * 判断给定回调是否匹配并应被调用。
		 *
		 * @param callbackType the callback type (the functional interface) 回调类型（函数式接口）
		 * @param callbackInstance the callback instance (the implementation) 回调实例（实现）
		 * @param argument the primary argument 主参数
		 * @param additionalArguments any additional arguments 附加参数
		 * @return if the callback matches and should be invoked 是否匹配且应调用
		 */
		boolean match(Class<C> callbackType, C callbackInstance, A argument,
				@Nullable Object @Nullable [] additionalArguments);

		/**
		 * 返回允许所有回调被调用的 {@link Filter}。
		 *
		 * @param <C> the callback type 回调类型
		 * @param <A> the primary argument type 主参数类型
		 * @return an "allow all" filter "允许全部" 过滤器
		 */
		static <C, A> Filter<C, A> allowAll() {
			return (callbackType, callbackInstance, argument, additionalArguments) -> true;
		}

	}

	/**
	 * 当回调具有单个泛型参数且主参数为该泛型实例时匹配的 {@link Filter}。
	 */
	private static final class GenericTypeFilter<C, A> implements Filter<C, A> {

		@Override
		public boolean match(Class<C> callbackType, C callbackInstance, A argument,
				@Nullable Object @Nullable [] additionalArguments) {
			ResolvableType type = ResolvableType.forClass(callbackType, callbackInstance.getClass());
			if (type.getGenerics().length != 1) {
				return true;
			}
			Class<?> generic = type.resolveGeneric();
			if (generic != null) {
				return generic.isInstance(argument);
			}
			return true;
		}

	}

	/**
	 * 回调调用结果：可能是值、{@code null}，或在回调不适用时完全缺失。
	 * 设计类似 {@link Optional}，但允许 {@code null} 作为有效值。
	 *
	 * @param <R> the result type 结果类型
	 */
	public static final class InvocationResult<R> {

		private static final InvocationResult<?> NONE = new InvocationResult<>(null);

		private final @Nullable R value;

		private InvocationResult(@Nullable R value) {
			this.value = value;
		}

		/**
		 * 若存在结果则返回 {@code true}。
		 *
		 * @return if a result is present 是否存在结果
		 */
		public boolean hasResult() {
			return this != NONE;
		}

		/**
		 * 返回调用结果；若回调不适用则返回 {@code null}。
		 *
		 * @return the result of the invocation or {@code null} 调用结果或 {@code null}
		 */
		public @Nullable R get() {
			return this.value;
		}

		/**
		 * 返回调用结果；若回调不适用则返回给定回退值。
		 *
		 * @param fallback the fallback to use when there is no result 无结果时使用的回退值
		 * @return the result of the invocation or the fallback 调用结果或回退值
		 */
		public @Nullable R get(@Nullable R fallback) {
			return (this != NONE) ? this.value : fallback;
		}

		/**
		 * 使用指定值创建新的 {@link InvocationResult} 实例。
		 *
		 * @param value the value (may be {@code null}) 值（可为 {@code null}）
		 * @param <R> the result type 结果类型
		 * @return an {@link InvocationResult} {@link InvocationResult} 实例
		 */
		public static <R> InvocationResult<R> of(@Nullable R value) {
			return new InvocationResult<>(value);
		}

		/**
		 * 返回表示无结果的 {@link InvocationResult} 实例。
		 *
		 * @param <R> the result type 结果类型
		 * @return an {@link InvocationResult} {@link InvocationResult} 实例
		 */
		@SuppressWarnings("unchecked")
		public static <R> InvocationResult<R> noResult() {
			return (InvocationResult<R>) NONE;
		}

	}

}
