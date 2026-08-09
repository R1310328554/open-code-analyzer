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

package org.springframework.boot.context.properties;

import java.util.NoSuchElementException;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.util.function.SingletonSupplier;

/**
 * 将给定源中的值映射到目标对象的工具类。
 * 主要用于从 {@link ConfigurationProperties @ConfigurationProperties} 映射到第三方类。
 * <p>
 * 可按谓词过滤值，并在需要时适配值。例如：
 * <pre class="code">
 * PropertyMapper map = PropertyMapper.get();
 * map.from(source::getName)
 *   .to(destination::setName);
 * map.from(source::getTimeout)
 *   .when(this::thisYear)
 *   .asInt(Duration::getSeconds)
 *   .to(destination::setTimeoutSecs);
 * map.from(source::isEnabled)
 *   .whenFalse().
 *   .toCall(destination::disable);
 * </pre>
 * <p>
 * 映射最终可应用到 {@link Source#to(Consumer) setter}、触发
 * {@link Source#toCall(Runnable) 方法调用}，或创建
 * {@link Source#toInstance(Function) 新实例}。
 * <p>
 * 默认会过滤 {@code null} 值以及 supplier 抛出的 {@link NullPointerException}，
 * 不会将其传递给 consumer。若需应用 null，可使用 {@link Source#always()}。
 *
 * @author Phillip Webb
 * @author Artsiom Yudovin
 * @author Chris Bono
 * @author Moritz Halbritter
 * @since 2.0.0
 */
public final class PropertyMapper {

	private static final PropertyMapper INSTANCE = new PropertyMapper(null, null);

	private final @Nullable PropertyMapper parent;

	private final @Nullable SourceOperator sourceOperator;

	private PropertyMapper(@Nullable PropertyMapper parent, @Nullable SourceOperator sourceOperator) {
		this.parent = parent;
		this.sourceOperator = sourceOperator;
	}

	/**
	 * 返回新的 {@link PropertyMapper} 实例，对每个 {@link Source} 应用给定 {@link SourceOperator}。
	 *
	 * @param operator the source operator to apply 要应用的源操作
	 * @return a new property mapper instance 新的 PropertyMapper 实例
	 */
	public PropertyMapper alwaysApplying(SourceOperator operator) {
		Assert.notNull(operator, "'operator' must not be null");
		return new PropertyMapper(this, operator);
	}

	/**
	 * 从指定值创建新的 {@link Source}，用于完成映射。
	 *
	 * @param <T> the source type 源类型
	 * @param value the value 源值
	 * @return a {@link Source} that can be used to complete the mapping 可用于完成映射的 {@link Source}
	 */
	public <T> Source<T> from(@Nullable T value) {
		return from(() -> value);
	}

	/**
	 * 从指定值 supplier 创建新的 {@link Source}，用于完成映射。
	 *
	 * @param <T> the source type 源类型
	 * @param supplier the value supplier 值 supplier
	 * @return a {@link Source} that can be used to complete the mapping 可用于完成映射的 {@link Source}
	 * @see #from(Object)
	 */
	public <T> Source<T> from(Supplier<? extends @Nullable T> supplier) {
		Assert.notNull(supplier, "'supplier' must not be null");
		Source<T> source = getSource(supplier);
		if (this.sourceOperator != null) {
			source = this.sourceOperator.apply(source);
		}
		return source;
	}

	private <T> Source<T> getSource(Supplier<? extends @Nullable T> supplier) {
		if (this.parent != null) {
			return this.parent.from(supplier);
		}
		return new Source<>(SingletonSupplier.of(supplier), (value) -> true);
	}

	/**
	 * 返回 PropertyMapper 单例。
	 *
	 * @return the property mapper PropertyMapper 实例
	 */
	public static PropertyMapper get() {
		return INSTANCE;
	}

	/**
	 * 可应用于 {@link Source} 的操作。
	 */
	@FunctionalInterface
	public interface SourceOperator {

		/**
		 * 对给定 {@link Source} 应用操作。
		 *
		 * @param <T> the source type 源类型
		 * @param source the source to operate on 待操作的源
		 * @return the updated source 更新后的源
		 */
		<T> Source<T> apply(Source<T> source);

	}

	/**
	 * 正在映射过程中的源。
	 *
	 * @param <T> the source type 源类型
	 */
	public static final class Source<T> {

		private final Supplier<? extends @Nullable T> supplier;

		private final Predicate<T> predicate;

		private Source(Supplier<? extends @Nullable T> supplier, Predicate<T> predicate) {
			Assert.notNull(predicate, "'predicate' must not be null");
			this.supplier = supplier;
			this.predicate = predicate;
		}

		/**
		 * 返回在值为 {@code null} 时使用给定 supplier 获取回退值的新 {@link Source}。
		 *
		 * @param fallback the fallback supplier 回退值 supplier
		 * @return a new {@link Source} instance 新的 {@link Source} 实例
		 * @since 4.0.0
		 */
		public Source<T> orFrom(Supplier<? extends @Nullable T> fallback) {
			Assert.notNull(fallback, "'fallback' must not be null");
			Supplier<@Nullable T> supplier = () -> {
				T value = getValue();
				return (value != null) ? value : fallback.get();
			};
			return new Source<>(supplier, this.predicate);
		}

		/**
		 * 返回适配为 {@link Integer} 类型的源。
		 *
		 * @param <R> the resulting type 结果数值类型
		 * @param adapter an adapter to convert the current value to a number 将当前值转为数值的适配器
		 * @return a new adapted source instance 新的适配源实例
		 */
		public <R extends Number> Source<Integer> asInt(Adapter<? super T, ? extends R> adapter) {
			return as(adapter).as(Number::intValue);
		}

		/**
		 * 通过给定适配函数返回转换后的源。
		 *
		 * @param <R> the resulting type 结果类型
		 * @param adapter the adapter to apply 要应用的适配器
		 * @return a new adapted source instance 新的适配源实例
		 */
		public <R> Source<R> as(Adapter<? super T, ? extends R> adapter) {
			Assert.notNull(adapter, "'adapter' must not be null");
			Supplier<@Nullable R> supplier = () -> {
				T value = getValue();
				return (value != null && this.predicate.test(value)) ? adapter.adapt(value) : null;
			};
			Predicate<R> predicate = (adaptedValue) -> {
				T value = getValue();
				return value != null && this.predicate.test(value);
			};
			return new Source<>(supplier, predicate);
		}

		/**
		 * 返回仅映射 {@code true} 值的过滤源。
		 *
		 * @return a new filtered source instance 新的过滤源实例
		 */
		public Source<T> whenTrue() {
			return when(Boolean.TRUE::equals);
		}

		/**
		 * 返回仅映射 {@code false} 值的过滤源。
		 *
		 * @return a new filtered source instance 新的过滤源实例
		 */
		public Source<T> whenFalse() {
			return when(Boolean.FALSE::equals);
		}

		/**
		 * 返回仅映射 {@code toString()} 含实际文本的值的过滤源。
		 *
		 * @return a new filtered source instance 新的过滤源实例
		 */
		public Source<T> whenHasText() {
			return when((value) -> StringUtils.hasText(value.toString()));
		}

		/**
		 * 返回仅映射与指定 {@code object} 相等的值的过滤源。
		 *
		 * @param object the object to match 要匹配的对象
		 * @return a new filtered source instance 新的过滤源实例
		 */
		public Source<T> whenEqualTo(@Nullable Object object) {
			return when((value) -> value.equals(object));
		}

		/**
		 * 返回仅映射为给定类型实例的值的过滤源。
		 *
		 * @param <R> the target type 目标类型
		 * @param target the target type to match 要匹配的目标类型
		 * @return a new filtered source instance 新的过滤源实例
		 */
		public <R extends T> Source<R> whenInstanceOf(Class<R> target) {
			Assert.notNull(target, "'target' must not be null");
			return when(target::isInstance).as(target::cast);
		}

		/**
		 * 返回不映射满足给定谓词的值的过滤源。
		 *
		 * @param predicate the predicate used to filter values 用于过滤值的谓词
		 * @return a new filtered source instance 新的过滤源实例
		 */
		public Source<T> whenNot(Predicate<T> predicate) {
			Assert.notNull(predicate, "'predicate' must not be null");
			return when(predicate.negate());
		}

		/**
		 * 返回仅映射满足给定谓词的值的过滤源。
		 *
		 * @param predicate the predicate used to filter values 用于过滤值的谓词
		 * @return a new filtered source instance 新的过滤源实例
		 */
		public Source<T> when(Predicate<T> predicate) {
			Assert.notNull(predicate, "'predicate' must not be null");
			return new Source<>(this.supplier, this.predicate.and(predicate));
		}

		/**
		 * 将未过滤的值传递给指定 consumer 以完成映射。
		 * 适用于可变对象。
		 *
		 * @param consumer the consumer that should accept the value if it's not been
		 * filtered 接收未过滤值的 consumer
		 */
		public void to(Consumer<? super T> consumer) {
			Assert.notNull(consumer, "'consumer' must not be null");
			T value = getValue();
			if (value != null && test(value)) {
				consumer.accept(value);
			}
		}

		/**
		 * 对未过滤值将给定函数应用于现有实例并返回新实例以完成映射。
		 * 值被过滤时原样返回 {@code instance}。适用于不可变对象。
		 *
		 * @param <R> the result type 结果类型
		 * @param instance the current instance 当前实例
		 * @param mapper the mapping function 映射函数
		 * @return a new mapped instance or the original instance 新映射实例或原实例
		 * @since 3.0.0
		 */
		public <R> R to(R instance, BiFunction<R, ? super T, R> mapper) {
			Assert.notNull(instance, "'instance' must not be null");
			Assert.notNull(mapper, "'mapper' must not be null");
			T value = getValue();
			if (value != null && test(value)) {
				return mapper.apply(instance, value);
			}
			return instance;
		}

		/**
		 * 从未过滤值创建新实例以完成映射。
		 *
		 * @param <R> the resulting type 结果类型
		 * @param factory the factory used to create the instance 创建实例的工厂
		 * @return the instance 新实例
		 * @throws NoSuchElementException if the value has been filtered 值已被过滤时
		 */
		public <R> R toInstance(Function<? super T, R> factory) {
			Assert.notNull(factory, "'factory' must not be null");
			T value = getValue();
			if (value != null && test(value)) {
				return factory.apply(value);
			}
			throw new NoSuchElementException("No value present");
		}

		/**
		 * 值未被过滤时调用指定方法以完成映射。
		 *
		 * @param runnable the method to call if the value has not been filtered 未过滤时要调用的方法
		 */
		public void toCall(Runnable runnable) {
			Assert.notNull(runnable, "'runnable' must not be null");
			T value = getValue();
			if (value != null && test(value)) {
				runnable.run();
			}
		}

		/**
		 * 返回即使值为 {@code null} 也能完成映射的源版本。
		 *
		 * @return a new {@link Always} instance 新的 {@link Always} 实例
		 * @since 4.0.0
		 */
		public Always<T> always() {
			Supplier<@Nullable T> getValue = this::getValue;
			return new Always<>(getValue, this::test);
		}

		private @Nullable T getValue() {
			try {
				return this.supplier.get();
			}
			catch (NullPointerException ex) {
				return null;
			}
		}

		private boolean test(T value) {
			Assert.state(value != null, "'value' must not be null");
			return this.predicate.test(value);
		}

		/**
		 * 适配值并可能返回 {@code null} 的适配器。
		 *
		 * @param <T> the source type 源类型
		 * @param <R> the result type 结果类型
		 * @since 4.0.0
		 */
		@FunctionalInterface
		public interface Adapter<T, R> {

			/**
			 * 适配给定值。
			 *
			 * @param value the value to adapt 待适配的值
			 * @return an adapted value or {@code null} 适配后的值或 {@code null}
			 */
			@Nullable R adapt(T value);

		}

		/**
		 * 允许使用可接受 null 的方法完成源映射。
		 *
		 * @param <T> the source type 源类型
		 * @since 4.0.0
		 */
		public static class Always<T> {

			private final Supplier<@Nullable T> supplier;

			private final Predicate<T> predicate;

			Always(Supplier<@Nullable T> supplier, Predicate<T> predicate) {
				this.supplier = supplier;
				this.predicate = predicate;
			}

			/**
			 * 通过给定适配函数返回转换后的源。
			 *
			 * @param <R> the resulting type 结果类型
			 * @param adapter the adapter to apply 要应用的适配器
			 * @return a new adapted source instance 新的适配源实例
			 */
			public <R> Always<R> as(Adapter<? super T, ? extends R> adapter) {
				Assert.notNull(adapter, "'adapter' must not be null");
				Supplier<@Nullable R> supplier = () -> {
					T value = getValue();
					return (value == null || test(value)) ? adapter.adapt(value) : null;
				};
				Predicate<R> predicate = (adaptedValue) -> {
					T value = getValue();
					return value == null || test(value);
				};
				return new Always<>(supplier, predicate);
			}

			/**
			 * 将未过滤的值传递给指定 consumer 以完成映射。
			 * 适用于可变对象。
			 *
			 * @param consumer the consumer that should accept the value if it's not been
			 * filtered 接收未过滤值的 consumer
			 */
			public void to(Consumer<@Nullable ? super T> consumer) {
				Assert.notNull(consumer, "'consumer' must not be null");
				T value = getValue();
				if (value == null || test(value)) {
					consumer.accept(value);
				}
			}

			/**
			 * 对未过滤值将给定函数应用于现有实例并返回新实例以完成映射。
			 * 值被过滤时原样返回 {@code instance}。适用于不可变对象。
			 *
			 * @param <R> the result type 结果类型
			 * @param instance the current instance 当前实例
			 * @param mapper the mapping function 映射函数
			 * @return a new mapped instance or the original instance 新映射实例或原实例
			 */
			public <R> R to(R instance, Mapper<R, ? super T> mapper) {
				Assert.notNull(instance, "'instance' must not be null");
				Assert.notNull(mapper, "'mapper' must not be null");
				T value = getValue();
				if (value == null || test(value)) {
					return mapper.map(instance, value);
				}
				return instance;
			}

			/**
			 * 从未过滤值创建新实例以完成映射。
			 *
			 * @param <R> the resulting type 结果类型
			 * @param factory the factory used to create the instance 创建实例的工厂
			 * @return the instance 新实例
			 * @throws NoSuchElementException if the value has been filtered 值已被过滤时
			 */
			public <R> R toInstance(Factory<? super T, ? extends R> factory) {
				Assert.notNull(factory, "'factory' must not be null");
				T value = getValue();
				if (value == null || test(value)) {
					return factory.create(value);
				}
				throw new NoSuchElementException("No value present");
			}

			/**
			 * 值未被过滤时调用指定方法以完成映射。
			 *
			 * @param runnable the method to call if the value has not been filtered 未过滤时要调用的方法
			 */
			public void toCall(Runnable runnable) {
				Assert.notNull(runnable, "'runnable' must not be null");
				T value = getValue();
				if (value == null || test(value)) {
					runnable.run();
				}
			}

			private @Nullable T getValue() {
				return this.supplier.get();
			}

			private boolean test(T value) {
				Assert.state(value != null, "'value' must not be null");
				return this.predicate.test(value);
			}

			/**
			 * 支持 nullable 值的适配器。
			 *
			 * @param <T> the source type 源类型
			 * @param <R> the result type 结果类型
			 */
			@FunctionalInterface
			public interface Adapter<T, R> {

				/**
				 * Adapt the given value.
				 * @param value the value to adapt
				 * @return an adapted value or {@code null}
				 */
				@Nullable R adapt(@Nullable T value);

			}

			/**
			 * 支持 nullable 值的工厂。
			 *
			 * @param <T> the source type 源类型
			 * @param <R> the result type 结果类型
			 */
			@FunctionalInterface
			public interface Factory<T, R extends @Nullable Object> {

				/**
				 * 为给定 nullable 值创建新实例。
				 *
				 * @param value the value used to create the instance (may be
				 * {@code null}) 用于创建实例的值（可为 {@code null}）
				 * @return the resulting instance 结果实例
				 */
				R create(@Nullable T value);

			}

			/**
			 * 支持 nullable 值的映射器。
			 *
			 * @param <T> the source type 源类型
			 * @param <R> the result type 结果类型
			 */
			@FunctionalInterface
			public interface Mapper<R extends @Nullable Object, T> {

				/**
				 * 为给定 nullable 值映射现有实例。
				 *
				 * @param instance the existing instance 现有实例
				 * @param value the value to map (may be {@code null}) 待映射的值（可为 {@code null}）
				 * @return the resulting mapped instance 映射后的实例
				 */
				R map(R instance, @Nullable T value);

			}

		}

	}

}
