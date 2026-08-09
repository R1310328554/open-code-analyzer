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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

/**
 * 组合式 {@link CacheManager} 实现，遍历给定的委托 {@link CacheManager} 实例集合。
 *
 * <p>允许在列表末尾自动添加 {@link NoOpCacheManager}，以处理无底层存储的缓存声明。
 * 否则，任何自定义 {@link CacheManager} 也可作为最后一个委托，按需惰性创建任意请求名称的缓存区域。
 *
 * <p>注意：本组合管理器委托的常规 CacheManager 若不知道指定缓存名称，
 * 需要从其 {@link #getCache(String)} 返回 {@code null}，以便继续迭代下一个委托。
 * 但大多数 {@link CacheManager} 实现会在请求后回退到惰性创建命名缓存；
 * 若有可用的「静态」模式（固定缓存名称），请查看具体配置细节。
 *
 * @author Costin Leau
 * @author Juergen Hoeller
 * @since 3.1
 * @see #setFallbackToNoOpCache
 * @see org.springframework.cache.concurrent.ConcurrentMapCacheManager#setCacheNames
 */
public class CompositeCacheManager implements CacheManager, InitializingBean {

	/** 委托的 CacheManager 列表。 */
	private final List<CacheManager> cacheManagers = new ArrayList<>();

	/** 是否在末尾回退到 NoOpCacheManager。 */
	private boolean fallbackToNoOpCache = false;


	/**
	 * 构造空的 CompositeCacheManager，委托 CacheManager 通过
	 * {@link #setCacheManagers "cacheManagers"} 属性添加。
	 */
	public CompositeCacheManager() {
	}

	/**
	 * 根据给定委托 CacheManager 构造 CompositeCacheManager。
	 * @param cacheManagers 要委托的 CacheManager
	 */
	public CompositeCacheManager(CacheManager... cacheManagers) {
		setCacheManagers(Arrays.asList(cacheManagers));
	}


	/**
	 * 指定要委托的 CacheManager。
	 */
	public void setCacheManagers(Collection<CacheManager> cacheManagers) {
		this.cacheManagers.addAll(cacheManagers);
	}

	/**
	 * 指示是否在委托列表末尾添加 {@link NoOpCacheManager}。
	 * 此时，已配置 CacheManager 未处理的任何 {@code getCache} 请求
	 * 将自动由 {@link NoOpCacheManager} 处理（因此永不返回 {@code null}）。
	 */
	public void setFallbackToNoOpCache(boolean fallbackToNoOpCache) {
		this.fallbackToNoOpCache = fallbackToNoOpCache;
	}

	@Override
	public void afterPropertiesSet() {
		if (this.fallbackToNoOpCache) {
			this.cacheManagers.add(new NoOpCacheManager());
		}
	}


	@Override
	public @Nullable Cache getCache(String name) {
		for (CacheManager cacheManager : this.cacheManagers) {
			Cache cache = cacheManager.getCache(name);
			if (cache != null) {
				return cache;
			}
		}
		return null;
	}

	@Override
	public Collection<String> getCacheNames() {
		Set<String> names = new LinkedHashSet<>();
		for (CacheManager manager : this.cacheManagers) {
			names.addAll(manager.getCacheNames());
		}
		return Collections.unmodifiableSet(names);
	}

	@Override
	public void resetCaches() {
		for (CacheManager manager : this.cacheManagers) {
			manager.resetCaches();
		}
	}

}
