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

package org.springframework.cache.interceptor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.util.Assert;

/**
 * {@link CacheResolver} 的基础实现：要求子类根据调用上下文提供缓存名称集合，
 * 本类负责通过 {@link CacheManager} 将名称解析为 {@link Cache} 实例。
 *
 * @author Stephane Nicoll
 * @author Juergen Hoeller
 * @since 4.1
 */
public abstract class AbstractCacheResolver implements CacheResolver, InitializingBean {

	/** 用于按名称查找 {@link Cache} 的缓存管理器。 */
	private @Nullable CacheManager cacheManager;


	/**
	 * 构造新的 {@code AbstractCacheResolver}。
	 * @see #setCacheManager
	 */
	protected AbstractCacheResolver() {
	}

	/**
	 * 使用给定 {@link CacheManager} 构造 {@code AbstractCacheResolver}。
	 * @param cacheManager the CacheManager to use
	 */
	protected AbstractCacheResolver(CacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}


	/**
	 * 设置本实例使用的 {@link CacheManager}。
	 */
	public void setCacheManager(CacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}

	/**
	 * 返回本实例使用的 {@link CacheManager}。
	 */
	public CacheManager getCacheManager() {
		Assert.state(this.cacheManager != null, "No CacheManager set");
		return this.cacheManager;
	}

	@Override
	public void afterPropertiesSet() {
		Assert.notNull(this.cacheManager, "CacheManager is required");
	}


	@Override
	public Collection<? extends Cache> resolveCaches(CacheOperationInvocationContext<?> context) {
		Collection<String> cacheNames = getCacheNames(context);
		if (cacheNames == null) {
			return Collections.emptyList();
		}
		Collection<Cache> result = new ArrayList<>(cacheNames.size());
		for (String cacheName : cacheNames) {
			Cache cache = getCacheManager().getCache(cacheName);
			if (cache == null) {
				throw new IllegalArgumentException("Cannot find cache named '" +
						cacheName + "' for " + context.getOperation());
			}
			result.add(cache);
		}
		return result;
	}

	/**
	 * 根据当前缓存管理器，提供本次调用应使用的缓存名称。
	 * <p>可返回 {@code null} 表示本次调用无需解析任何缓存。
	 * @param context the context of the particular invocation
	 * @return the cache name(s) to resolve, or {@code null} if no cache should be resolved
	 */
	protected abstract @Nullable Collection<String> getCacheNames(CacheOperationInvocationContext<?> context);

}
