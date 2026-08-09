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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.core.serializer.support.SerializationDelegate;

/**
 * 为每次 {@link #getCache} 请求惰性创建 {@link ConcurrentMapCache} 的
 * {@link CacheManager} 实现。也支持通过 {@link #setCacheNames} 预定义缓存名称的
 * 「静态」模式，运行时不再动态创建新缓存区域。
 *
 * <p>通过基本的 {@code CompletableFuture} 适配支持异步的
 * {@link Cache#retrieve(Object)} 与 {@link Cache#retrieve(Object, Supplier)} 操作，
 * 可提前判定缓存未命中。
 *
 * <p>注意：这并非功能完备的 CacheManager，不提供丰富的缓存配置选项。
 * 适用于测试或简单场景；高级本地缓存需求请考虑
 * {@link org.springframework.cache.caffeine.CaffeineCacheManager} 或
 * {@link org.springframework.cache.jcache.JCacheCacheManager}。
 *
 * @author Juergen Hoeller
 * @since 3.1
 * @see ConcurrentMapCache
 */
public class ConcurrentMapCacheManager implements CacheManager, BeanClassLoaderAware {

	/** 缓存名称 → {@link Cache} 实例的注册表。 */
	private final ConcurrentMap<String, Cache> cacheMap = new ConcurrentHashMap<>(16);

	/** {@code true} 时允许按名称动态创建缓存；{@code false} 为静态模式。 */
	private volatile boolean dynamic = true;

	/** 是否为所有缓存接受并转换 {@code null} 值。 */
	private boolean allowNullValues = true;

	/** 是否按值存储（序列化副本）而非存引用。 */
	private boolean storeByValue = false;

	/** 按值存储时使用的序列化委托；依赖 {@link #setBeanClassLoader} 注入类加载器。 */
	private @Nullable SerializationDelegate serialization;


	/**
	 * 构造动态 ConcurrentMapCacheManager，按需惰性创建缓存实例。
	 */
	public ConcurrentMapCacheManager() {
	}

	/**
	 * 构造静态 ConcurrentMapCacheManager，仅管理指定名称的缓存。
	 */
	public ConcurrentMapCacheManager(String... cacheNames) {
		setCacheNames(Arrays.asList(cacheNames));
	}


	/**
	 * 指定本 CacheManager 在「静态」模式下管理的缓存名称集合。
	 * <p>调用后缓存数量与名称固定，运行时不再创建新区域。
	 * <p>注意：此方法会替换同名已有缓存，并阻止后续动态创建——但不会删除无关的已有缓存。
	 * 若需完全重置，可先调用 {@link #resetCaches()}。
	 * <p>传入 {@code null} 可恢复为「动态」模式，允许再次动态创建缓存。
	 * @see #resetCaches()
	 */
	public void setCacheNames(@Nullable Collection<String> cacheNames) {
		if (cacheNames != null) {
			for (String name : cacheNames) {
				this.cacheMap.put(name, createConcurrentMapCache(name));
			}
			this.dynamic = false;
		}
		else {
			this.dynamic = true;
		}
	}

	/**
	 * 设置是否为所有缓存接受并转换 {@code null} 值。
	 * <p>默认为 {@code true}，尽管 ConcurrentHashMap 本身不支持 {@code null}；
	 * 内部会使用占位对象存储用户级 {@code null}。
	 * <p>注意：更改此设置会重建所有已有缓存实例。
	 */
	public void setAllowNullValues(boolean allowNullValues) {
		if (allowNullValues != this.allowNullValues) {
			this.allowNullValues = allowNullValues;
			// 需要以新的 null 值策略重建所有 Cache 实例
			recreateCaches();
		}
	}

	/**
	 * 返回本缓存管理器是否为所有缓存接受并转换 {@code null} 值。
	 */
	public boolean isAllowNullValues() {
		return this.allowNullValues;
	}

	/**
	 * 设置是否为所有缓存按值存储（{@code true}，序列化副本）还是存引用（{@code false}）。
	 * <p>默认为 {@code false}，即直接存储值本身，不要求可序列化。
	 * <p>注意：更改此设置会重建所有已有缓存实例。
	 * @since 4.3
	 */
	public void setStoreByValue(boolean storeByValue) {
		if (storeByValue != this.storeByValue) {
			this.storeByValue = storeByValue;
			// 需要以新的按值存储策略重建所有 Cache 实例
			recreateCaches();
		}
	}

	/**
	 * 返回是否为所有缓存按值存储。按值存储时，每个条目必须可序列化。
	 * @since 4.3
	 */
	public boolean isStoreByValue() {
		return this.storeByValue;
	}

	@Override
	public void setBeanClassLoader(ClassLoader classLoader) {
		this.serialization = new SerializationDelegate(classLoader);
		// 按值存储模式下，需要用新 ClassLoader 重建所有 Cache 实例
		if (isStoreByValue()) {
			recreateCaches();
		}
	}


	@Override
	public @Nullable Cache getCache(String name) {
		Cache cache = this.cacheMap.get(name);
		if (cache == null && this.dynamic) {
			cache = this.cacheMap.computeIfAbsent(name, this::createConcurrentMapCache);
		}
		return cache;
	}

	@Override
	public Collection<String> getCacheNames() {
		return Collections.unmodifiableSet(this.cacheMap.keySet());
	}

	/**
	 * 重置本缓存管理器的所有缓存：动态模式下完全移除以便按需重建，
	 * 静态模式下仅清空条目。
	 * @since 6.2.14
	 */
	@Override
	public void resetCaches() {
		this.cacheMap.values().forEach(Cache::clear);
		if (this.dynamic) {
			this.cacheMap.clear();
		}
	}

	/**
	 * 从本缓存管理器中移除指定名称的缓存。
	 * @param name the name of the cache
	 * @since 6.1.15
	 */
	public void removeCache(String name) {
		this.cacheMap.remove(name);
	}

	/** 以当前配置重建 {@code cacheMap} 中所有缓存实例。 */
	private void recreateCaches() {
		for (Map.Entry<String, Cache> entry : this.cacheMap.entrySet()) {
			entry.setValue(createConcurrentMapCache(entry.getKey()));
		}
	}

	/**
	 * 为指定缓存名称创建新的 ConcurrentMapCache 实例。
	 * @param name the name of the cache
	 * @return the ConcurrentMapCache (or a decorator thereof)
	 */
	protected Cache createConcurrentMapCache(String name) {
		SerializationDelegate actualSerialization = (isStoreByValue() ? this.serialization : null);
		return new ConcurrentMapCache(name, new ConcurrentHashMap<>(256), isAllowNullValues(), actualSerialization);
	}

}
