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

package org.springframework.cache;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;

/**
 * 定义通用缓存操作的接口。
 *
 * <p>主要作为 Spring 基于注解的缓存模型（{@link org.springframework.cache.annotation.Cacheable} 等）
 * 的 SPI，其次也可在应用中直接作为 API 使用。
 *
 * <p><b>注意：</b>鉴于缓存的通用用途，建议实现允许存储 {@code null} 值
 *（例如缓存返回 {@code null} 的方法）。
 *
 * @author Costin Leau
 * @author Juergen Hoeller
 * @author Stephane Nicoll
 * @since 3.1
 * @see CacheManager
 * @see org.springframework.cache.annotation.Cacheable
 */
public interface Cache {

	/**
	 * 返回缓存名称。
	 */
	String getName();

	/**
	 * 返回底层原生缓存提供者。
	 */
	Object getNativeCache();

	/**
	 * 返回此缓存将指定键映射到的值。
	 * <p>若缓存不包含该键的映射则返回 {@code null}；
	 * 否则，缓存的值（其本身可能为 {@code null}）将封装在 {@link ValueWrapper} 中返回。
	 * @param key 要返回其关联值的键
	 * @return 此缓存将指定键映射到的值，封装在 {@link ValueWrapper} 中，
	 * 其中也可能持有缓存的 {@code null} 值。直接返回 {@code null} 表示
	 * 缓存不包含该键的映射。
	 * @see #get(Object, Class)
	 * @see #get(Object, Callable)
	 */
	@Nullable ValueWrapper get(Object key);

	/**
	 * 返回此缓存将指定键映射到的值，并泛式指定返回值的类型。
	 * <p>注意：此 {@code get} 变体无法区分缓存的 {@code null} 值与完全无缓存条目。
	 * 为此目的请使用标准 {@link #get(Object)} 变体。
	 * @param key 要返回其关联值的键
	 * @param type 返回值的所需类型（可为 {@code null} 以跳过类型检查；
	 * 若在缓存中发现 {@code null} 值，指定类型无关紧要）
	 * @return 此缓存将指定键映射到的值（其本身可能为 {@code null}），
	 * 或若缓存不包含该键的映射则也为 {@code null}
	 * @throws IllegalStateException 若找到缓存条目但无法匹配指定类型
	 * @since 4.0
	 * @see #get(Object)
	 */
	<T> @Nullable T get(Object key, @Nullable Class<T> type);

	/**
	 * 返回此缓存将指定键映射到的值，必要时从 {@code valueLoader} 获取。
	 * 此方法为常见的「若已缓存则返回，否则创建、缓存并返回」模式提供简单替代。
	 * <p>若可能，实现应确保加载操作同步，以便在并发访问同一键时
	 * 指定的 {@code valueLoader} 仅被调用一次。
	 * <p>若 {@code valueLoader} 抛出异常，将封装为 {@link ValueRetrievalException}。
	 * @param key 要返回其关联值的键
	 * @return 此缓存将指定键映射到的值
	 * @throws ValueRetrievalException 若 {@code valueLoader} 抛出异常
	 * @since 4.3
	 * @see #get(Object)
	 */
	<T> @Nullable T get(Object key, Callable<T> valueLoader);

	/**
	 * 返回此缓存将指定键映射到的值，封装在 {@link CompletableFuture} 中。
	 * 此操作不得阻塞，但允许在对应值立即可用时返回已完成的 {@link CompletableFuture}。
	 * <p>若缓存可立即确定不包含该键的映射（例如通过内存键映射），可返回 {@code null}。
	 * 否则，缓存值将在 {@link CompletableFuture} 中返回，{@code null} 表示迟确定的缓存未命中。
	 * 嵌套的 {@link ValueWrapper} 可能表示可空的缓存值；若不支持 {@code null} 值，
	 * 缓存值也可能以普通元素表示。调用代码需能处理此方法返回结果的所有变体。
	 * @param key 要返回其关联值的键
	 * @return 此缓存将指定键映射到的值，封装在 {@link CompletableFuture} 中，
	 * 迟确定缓存未命中时也可能为空。直接返回 {@code null} 表示缓存立即确定
	 * 不包含该键的映射。{@code CompletableFuture} 中包含的 {@link ValueWrapper}
	 * 表示可能为 {@code null} 的缓存值；在迟确定场景中，普通 CompletableFuture
	 * 中的 {@code null} 表示缓存未命中。若实现不支持实际缓存 {@code null} 值，
	 * 缓存也可能返回普通值以避免额外的值包装层。Spring 的缓存处理可应对所有这些实现策略。
	 * @since 6.1
	 * @see #retrieve(Object, Supplier)
	 */
	default @Nullable CompletableFuture<?> retrieve(Object key) {
		throw new UnsupportedOperationException(
				getClass().getName() + " does not support CompletableFuture-based retrieval");
	}

	/**
	 * 返回此缓存将指定键映射到的值，必要时从 {@code valueLoader} 获取。
	 * 此方法基于 {@link CompletableFuture} 为常见的「若已缓存则返回，否则创建、缓存并返回」
	 * 模式提供简单替代。此操作不得阻塞。
	 * <p>若可能，实现应确保加载操作同步，以便在并发访问同一键时
	 * 指定的 {@code valueLoader} 仅被调用一次。
	 * <p>使用此方法时，{@code null} 值始终表示用户级的 {@code null} 值。
	 * 提供的 {@link CompletableFuture} 句柄产生值或抛出异常。若 {@code valueLoader}
	 * 抛出异常，将传播到返回的 {@code CompletableFuture} 句柄。
	 * @param key 要返回其关联值的键
	 * @return 此缓存将指定键映射到的值，封装在永不为 {@code null} 的
	 * {@link CompletableFuture} 中。提供的 future 应产生值或抛出异常。
	 * @since 6.1
	 * @see #retrieve(Object)
	 * @see #get(Object, Callable)
	 */
	default <T> CompletableFuture<T> retrieve(Object key, Supplier<CompletableFuture<T>> valueLoader) {
		throw new UnsupportedOperationException(
				getClass().getName() + " does not support CompletableFuture-based retrieval");
	}

	/**
	 * 在此缓存中将指定值与指定键关联。
	 * <p>若缓存先前包含该键的映射，旧值将被指定值替换。
	 * <p>实际注册可能异步或延迟执行，后续查找可能尚看不到条目。
	 * 例如事务性缓存装饰器可能出现此情况。使用 {@link #putIfAbsent} 可保证立即注册。
	 * <p>若缓存需与 {@link CompletableFuture} 及响应式交互兼容，
	 * put 操作需实质上非阻塞，任何后端写穿异步发生。这与支持
	 * {@link #retrieve(Object)} 和 {@link #retrieve(Object, Supplier)} 的
	 * 缓存实现及配置一致。
	 * @param key 要与指定值关联的键
	 * @param value 要与指定键关联的值
	 * @see #putIfAbsent(Object, Object)
	 */
	void put(Object key, @Nullable Object value);

	/**
	 * 若尚未设置，则原子地将指定值与指定键在此缓存中关联。
	 * <p>等价于：
	 * <pre><code>
	 * ValueWrapper existingValue = cache.get(key);
	 * if (existingValue == null) {
	 *     cache.put(key, value);
	 * }
	 * return existingValue;
	 * </code></pre>
	 * 但操作以原子方式执行。虽然所有开箱即用的 {@link CacheManager} 实现
	 * 都能原子执行 put，操作也可能分两步非原子实现（例如先检查存在再 put）。
	 * 有关更多细节，请查阅所用原生缓存实现的文档。
	 * <p>默认实现沿上述代码片段思路委托给 {@link #get(Object)} 和 {@link #put(Object, Object)}。
	 * @param key 要与指定值关联的键
	 * @param value 要与指定键关联的值
	 * @return 此缓存将指定键映射到的值（其本身可能为 {@code null}），
	 * 或若调用前缓存不包含该键的任何映射则也为 {@code null}。
	 * 因此返回 {@code null} 表示给定 {@code value} 已与键关联。
	 * @since 4.1
	 * @see #put(Object, Object)
	 */
	default @Nullable ValueWrapper putIfAbsent(Object key, @Nullable Object value) {
		// 先尝试获取已有值
		ValueWrapper existingValue = get(key);
		// 不存在时才写入
		if (existingValue == null) {
			put(key, value);
		}
		return existingValue;
	}

	/**
	 * 若存在则从本缓存中驱逐该键的映射。
	 * <p>实际驱逐可能异步或延迟执行，后续查找可能仍能看到条目。
	 * 例如事务性缓存装饰器可能出现此情况。使用 {@link #evictIfPresent} 可保证立即移除。
	 * <p>若缓存需与 {@link CompletableFuture} 及响应式交互兼容，
	 * evict 操作需实质上非阻塞，任何后端写穿异步发生。这与支持
	 * {@link #retrieve(Object)} 和 {@link #retrieve(Object, Supplier)} 的
	 * 缓存实现及配置一致。
	 * @param key 要从缓存中移除其映射的键
	 * @see #evictIfPresent(Object)
	 */
	void evict(Object key);

	/**
	 * 若存在则从本缓存中驱逐该键的映射，并期望该键对后续查找立即不可见。
	 * <p>默认实现委托给 {@link #evict(Object)}，对先前键存在性不确定时返回 {@code false}。
	 * 缓存提供者及尤其是缓存装饰器，若可能应执行立即驱逐（例如在事务中通常延迟缓存操作时），
	 * 并可靠地确定给定键先前是否存在。
	 * @param key 要从缓存中移除其映射的键
	 * @return 若此前已知缓存包含该键的映射则为 {@code true}，
	 * 若不存在或无法确定先前存在则为 {@code false}
	 * @since 5.2
	 * @see #evict(Object)
	 */
	default boolean evictIfPresent(Object key) {
		evict(key);
		return false;
	}

	/**
	 * 通过移除所有映射来清空缓存。
	 * <p>实际清空可能异步或延迟执行，后续查找可能仍能看到条目。
	 * 例如事务性缓存装饰器可能出现此情况。使用 {@link #invalidate()} 可保证立即移除条目。
	 * <p>若缓存需与 {@link CompletableFuture} 及响应式交互兼容，
	 * clear 操作需实质上非阻塞，任何后端写穿异步发生。这与支持
	 * {@link #retrieve(Object)} 和 {@link #retrieve(Object, Supplier)} 的
	 * 缓存实现及配置一致。
	 * @see #invalidate()
	 */
	void clear();

	/**
	 * 通过移除所有映射使缓存失效，并期望所有条目对后续查找立即不可见。
	 * @return 若此前已知缓存包含映射则为 {@code true}，
	 * 若不存在或无法确定先前存在则为 {@code false}
	 * @since 5.2
	 * @see #clear()
	 */
	default boolean invalidate() {
		clear();
		return false;
	}


	/**
	 * 表示缓存值的（包装）对象。
	 */
	@FunctionalInterface
	interface ValueWrapper {

		/**
		 * 返回缓存中的实际值。
		 */
		@Nullable Object get();
	}


	/**
	 * 当值加载器回调失败时，从 {@link #get(Object, Callable)} 抛出的包装异常。
	 * @since 4.3
	 */
	@SuppressWarnings("serial")
	class ValueRetrievalException extends RuntimeException {

		private final @Nullable Object key;

		public ValueRetrievalException(@Nullable Object key, Callable<?> loader, @Nullable Throwable ex) {
			super(String.format("Value for key '%s' could not be loaded using '%s'", key, loader), ex);
			this.key = key;
		}

		public @Nullable Object getKey() {
			return this.key;
		}
	}

}
