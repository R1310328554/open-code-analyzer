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

package org.springframework.cache.support;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.util.CollectionUtils;

/**
 * 实现 {@link CacheManager} 通用方法的抽象基类。
 * 适用于底层缓存不变的「静态」环境。
 *
 * @author Costin Leau
 * @author Juergen Hoeller
 * @author Stephane Nicoll
 * @since 3.1
 */
public abstract class AbstractCacheManager implements CacheManager, InitializingBean {

	/** 缓存名称到 Cache 实例的映射。 */
	private final ConcurrentMap<String, Cache> cacheMap = new ConcurrentHashMap<>(16);

	/** 对外暴露的缓存名称集合。 */
	private volatile Set<String> cacheNames = Collections.emptySet();


	// 启动时提前初始化缓存

	@Override
	public void afterPropertiesSet() {
		initializeCaches();
	}

	/**
	 * 初始化缓存的静态配置。
	 * <p>启动时通过 {@link #afterPropertiesSet()} 触发；
	 * 也可在运行时调用以重新初始化。
	 * @since 4.2.2
	 * @see #loadCaches()
	 */
	public void initializeCaches() {
		Collection<? extends Cache> caches = loadCaches();

		synchronized (this.cacheMap) {
			this.cacheNames = Collections.emptySet();
			this.cacheMap.clear();
			Set<String> cacheNames = CollectionUtils.newLinkedHashSet(caches.size());
			for (Cache cache : caches) {
				String name = cache.getName();
				this.cacheMap.put(name, decorateCache(cache));
				cacheNames.add(name);
			}
			this.cacheNames = Collections.unmodifiableSet(cacheNames);
		}
	}

	/**
	 * 加载本缓存管理器的初始缓存。
	 * <p>启动时由 {@link #afterPropertiesSet()} 调用。
	 * 返回的集合可以为空，但不得为 {@code null}。
	 */
	protected abstract Collection<? extends Cache> loadCaches();


	// 访问时惰性初始化缓存

	@Override
	public @Nullable Cache getCache(String name) {
		// 快速检查已有缓存...
		Cache cache = this.cacheMap.get(name);
		if (cache != null) {
			return cache;
		}

		// 提供者可能支持按需创建缓存...
		Cache missingCache = getMissingCache(name);
		if (missingCache != null) {
			// 缺失缓存注册时完全同步
			synchronized (this.cacheMap) {
				cache = this.cacheMap.get(name);
				if (cache == null) {
					cache = decorateCache(missingCache);
					this.cacheMap.put(name, cache);
					updateCacheNames(name);
				}
			}
		}
		return cache;
	}

	@Override
	public Collection<String> getCacheNames() {
		return this.cacheNames;
	}

	@Override
	public void resetCaches() {
		synchronized (this.cacheMap) {
			this.cacheMap.values().forEach(Cache::clear);
		}
	}


	// 供子类使用的通用缓存初始化委托

	/**
	 * 检查是否已注册给定名称的缓存。
	 * 与 {@link #getCache(String)} 不同，本方法不会通过
	 * {@link #getMissingCache(String)} 触发缺失缓存的惰性创建。
	 * @param name 缓存标识符（不得为 {@code null}）
	 * @return 关联的 Cache 实例，未找到则为 {@code null}
	 * @since 4.1
	 * @see #getCache(String)
	 * @see #getMissingCache(String)
	 */
	protected final @Nullable Cache lookupCache(String name) {
		return this.cacheMap.get(name);
	}

	/**
	 * 用给定名称更新对外暴露的 {@link #cacheNames} 集合。
	 * <p>始终在完整的 {@link #cacheMap} 锁内调用，行为类似保留顺序的
	 * {@code CopyOnWriteArraySet}，但对外暴露为不可变引用。
	 * @param name 要添加的缓存名称
	 */
	private void updateCacheNames(String name) {
		Set<String> cacheNames = new LinkedHashSet<>(this.cacheNames);
		cacheNames.add(name);
		this.cacheNames = Collections.unmodifiableSet(cacheNames);
	}


	// 可覆盖的缓存初始化模板方法

	/**
	 * 如有必要，装饰给定 Cache 对象。
	 * @param cache 要添加到本 CacheManager 的 Cache 对象
	 * @return 要使用的装饰后 Cache 对象，默认直接返回传入的 Cache 对象
	 */
	protected Cache decorateCache(Cache cache) {
		return cache;
	}

	/**
	 * 返回具有指定 {@code name} 的缺失缓存，若不存在或无法按需创建则返回 {@code null}。
	 * <p>若原生提供者支持，缓存可在运行时惰性创建。若按名称查找无结果，
	 * {@code AbstractCacheManager} 子类有机会在运行时注册该缓存。返回的缓存
	 * 将自动添加到本缓存管理器。
	 * @param name 要检索的缓存名称
	 * @return 缺失的缓存，若不存在或无法按需创建则为 {@code null}
	 * @since 4.1
	 * @see #getCache(String)
	 */
	protected @Nullable Cache getMissingCache(String name) {
		return null;
	}

}
