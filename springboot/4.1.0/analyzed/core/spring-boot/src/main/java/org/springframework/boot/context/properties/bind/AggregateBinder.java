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

import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;

import org.springframework.boot.context.properties.bind.Binder.Context;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;

/**
 * {@link Binder} 用于绑定聚合类型（Map、List、数组）的内部策略。
 *
 * @param <T> 被绑定的类型
 * @author Phillip Webb
 * @author Madhura Bhave
 */
abstract class AggregateBinder<T> {

	private final Context context;

	AggregateBinder(Context context) {
		this.context = context;
	}

	/**
	 * 判断是否支持递归绑定。
	 *
	 * @param source 配置属性源，或 {@code null} 表示所有源
	 * @return 是否支持递归绑定
	 */
	protected abstract boolean isAllowRecursiveBinding(@Nullable ConfigurationPropertySource source);

	/**
	 * 执行聚合类型的绑定。
	 *
	 * @param name 要绑定的配置属性名
	 * @param target 绑定目标
	 * @param elementBinder 元素绑定器
	 * @return 绑定后的聚合对象，或 null
	 */
	@SuppressWarnings("unchecked")
	final @Nullable Object bind(ConfigurationPropertyName name, Bindable<?> target,
			AggregateElementBinder elementBinder) {
		Object result = bindAggregate(name, target, elementBinder);
		Supplier<?> value = target.getValue();
		if (result == null || value == null) {
			return result;
		}
		return merge((Supplier<T>) value, (T) result);
	}

	/**
	 * 执行实际的聚合绑定。
	 *
	 * @param name 要绑定的配置属性名
	 * @param target 绑定目标
	 * @param elementBinder 元素绑定器
	 * @return 绑定结果
	 */
	protected abstract @Nullable Object bindAggregate(ConfigurationPropertyName name, Bindable<?> target,
			AggregateElementBinder elementBinder);

	/**
	 * 将额外元素合并到现有聚合中。
	 *
	 * @param existing 现有值的供应器
	 * @param additional 要合并的额外元素
	 * @return 合并后的结果
	 */
	protected abstract T merge(Supplier<T> existing, T additional);

	/**
	 * 返回此绑定器使用的上下文。
	 *
	 * @return 上下文
	 */
	protected final Context getContext() {
		return this.context;
	}

	/**
	 * 用于提供聚合对象并缓存值的内部类。
	 *
	 * @param <T> 聚合类型
	 */
	protected static class AggregateSupplier<T> {

		private final Supplier<T> supplier;

		private @Nullable T supplied;

		public AggregateSupplier(Supplier<T> supplier) {
			this.supplier = supplier;
		}

		public T get() {
			if (this.supplied == null) {
				this.supplied = this.supplier.get();
			}
			return this.supplied;
		}

		public boolean wasSupplied() {
			return this.supplied != null;
		}

	}

}
