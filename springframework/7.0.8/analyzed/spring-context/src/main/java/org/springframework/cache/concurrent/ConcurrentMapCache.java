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

package org.springframework.cache.concurrent;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;

import org.springframework.cache.support.AbstractValueAdaptingCache;
import org.springframework.core.serializer.support.SerializationDelegate;
import org.springframework.util.Assert;

/**
 * 基于 JDK {@code java.util.concurrent} 包的简单 {@link org.springframework.cache.Cache} 实现。
 *
 * <p>适用于测试或简单缓存场景，通常与
 * {@link org.springframework.cache.support.SimpleCacheManager} 配合使用，
 * 或通过 {@link ConcurrentMapCacheManager} 动态创建。
 *
 * <p>以尽力而为的方式支持 {@link #retrieve(Object)} 与
 * {@link #retrieve(Object, Supplier)}，依赖默认的 {@link CompletableFuture}
 * 执行（通常在 JVM 的 {@link ForkJoinPool#commonPool()} 中）。
 *
 * <p><b>注意：</b>默认底层存储 {@link ConcurrentHashMap} 不允许 {@code null} 值，
 * 本类会将用户传入的 {@code null} 替换为预定义的内部占位对象。
 * 可通过 {@link #ConcurrentMapCache(String, ConcurrentMap, boolean)} 构造器改变此行为。
 *
 * @author Costin Leau
 * @author Juergen Hoeller
 * @author Stephane Nicoll
 * @since 3.1
 * @see ConcurrentMapCacheManager
 */
public class ConcurrentMapCache extends AbstractValueAdaptingCache {

	/** 缓存逻辑名称。 */
	private final String name;

	/** 底层并发 Map 存储。 */
	private final ConcurrentMap<Object, Object> store;

	/** 非 {@code null} 时启用按值存储（序列化副本）；否则存引用。 */
	private final @Nullable SerializationDelegate serialization;


	/**
	 * 创建指定名称的 ConcurrentMapCache。
	 * @param name the name of the cache
	 */
	public ConcurrentMapCache(String name) {
		this(name, new ConcurrentHashMap<>(256), true);
	}

	/**
	 * 创建指定名称的 ConcurrentMapCache。
	 * @param name the name of the cache
	 * @param allowNullValues whether to accept and convert {@code null}
	 * values for this cache
	 */
	public ConcurrentMapCache(String name, boolean allowNullValues) {
		this(name, new ConcurrentHashMap<>(256), allowNullValues);
	}

	/**
	 * 使用指定名称和内部 {@link ConcurrentMap} 创建 ConcurrentMapCache。
	 * @param name the name of the cache
	 * @param store the ConcurrentMap to use as an internal store
	 * @param allowNullValues whether to allow {@code null} values
	 * (adapting them to an internal null holder value)
	 */
	public ConcurrentMapCache(String name, ConcurrentMap<Object, Object> store, boolean allowNullValues) {
		this(name, store, allowNullValues, null);
	}

	/**
	 * 使用指定名称、内部 {@link ConcurrentMap} 和序列化委托创建 ConcurrentMapCache。
	 * 若指定了 {@link SerializationDelegate}，则启用 {@link #isStoreByValue() 按值存储}。
	 * @param name the name of the cache
	 * @param store the ConcurrentMap to use as an internal store
	 * @param allowNullValues whether to allow {@code null} values
	 * (adapting them to an internal null holder value)
	 * @param serialization the {@link SerializationDelegate} to use
	 * to serialize cache entry or {@code null} to store the reference
	 * @since 4.3
	 */
	protected ConcurrentMapCache(String name, ConcurrentMap<Object, Object> store,
			boolean allowNullValues, @Nullable SerializationDelegate serialization) {

		super(allowNullValues);
		Assert.notNull(name, "Name must not be null");
		Assert.notNull(store, "Store must not be null");
		this.name = name;
		this.store = store;
		this.serialization = serialization;
	}


	/**
	 * 返回是否按值存储（{@code true}，序列化副本）还是存引用（{@code false}，默认）。
	 * 按值存储时，每个条目必须可序列化。
	 * @since 4.3
	 */
	public final boolean isStoreByValue() {
		return (this.serialization != null);
	}

	@Override
	public final String getName() {
		return this.name;
	}

	@Override
	public final ConcurrentMap<Object, Object> getNativeCache() {
		return this.store;
	}

	@Override
	protected @Nullable Object lookup(Object key) {
		return this.store.get(key);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> @Nullable T get(Object key, Callable<T> valueLoader) {
		// computeIfAbsent 保证同一 key 只加载一次
		return (T) fromStoreValue(this.store.computeIfAbsent(key, k -> {
			try {
				return toStoreValue(valueLoader.call());
			}
			catch (Throwable ex) {
				throw new ValueRetrievalException(key, valueLoader, ex);
			}
		}));
	}

	@Override
	public @Nullable CompletableFuture<?> retrieve(Object key) {
		Object value = lookup(key);
		return (value != null ? CompletableFuture.completedFuture(
				isAllowNullValues() ? toValueWrapper(value) : fromStoreValue(value)) : null);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> CompletableFuture<T> retrieve(Object key, Supplier<CompletableFuture<T>> valueLoader) {
		return CompletableFuture.supplyAsync(() ->
				(T) fromStoreValue(this.store.computeIfAbsent(key, k -> toStoreValue(valueLoader.get().join()))));
	}

	@Override
	public void put(Object key, @Nullable Object value) {
		this.store.put(key, toStoreValue(value));
	}

	@Override
	public @Nullable ValueWrapper putIfAbsent(Object key, @Nullable Object value) {
		Object existing = this.store.putIfAbsent(key, toStoreValue(value));
		return toValueWrapper(existing);
	}

	@Override
	public void evict(Object key) {
		this.store.remove(key);
	}

	@Override
	public boolean evictIfPresent(Object key) {
		return (this.store.remove(key) != null);
	}

	@Override
	public void clear() {
		this.store.clear();
	}

	@Override
	public boolean invalidate() {
		boolean notEmpty = !this.store.isEmpty();
		this.store.clear();
		return notEmpty;
	}

	@Override
	protected Object toStoreValue(@Nullable Object userValue) {
		Object storeValue = super.toStoreValue(userValue);
		if (this.serialization != null) {
			try {
				return this.serialization.serializeToByteArray(storeValue);
			}
			catch (Throwable ex) {
				throw new IllegalArgumentException("Failed to serialize cache value '" + userValue +
						"'. Does it implement Serializable?", ex);
			}
		}
		else {
			return storeValue;
		}
	}

	@Override
	protected @Nullable Object fromStoreValue(@Nullable Object storeValue) {
		if (storeValue != null && this.serialization != null) {
			try {
				return super.fromStoreValue(this.serialization.deserializeFromByteArray((byte[]) storeValue));
			}
			catch (Throwable ex) {
				throw new IllegalArgumentException("Failed to deserialize cache value '" + storeValue + "'", ex);
			}
		}
		else {
			return super.fromStoreValue(storeValue);
		}
	}

}
