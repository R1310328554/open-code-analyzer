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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.jspecify.annotations.Nullable;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

/**
 * 适用于禁用缓存的基本无操作 {@link CacheManager} 实现，
 * 通常用于支撑无实际底层存储的缓存声明。
 *
 * <p>本实现接受任何放入缓存的项，但不实际存储。
 *
 * @author Costin Leau
 * @author Stephane Nicoll
 * @author Juergen Hoeller
 * @since 3.1
 * @see NoOpCache
 */
public class NoOpCacheManager implements CacheManager {

	/** 缓存名称到 NoOpCache 实例的映射。 */
	private final ConcurrentMap<String, Cache> cacheMap = new ConcurrentHashMap<>(16);


	@Override
	public @Nullable Cache getCache(String name) {
		return this.cacheMap.computeIfAbsent(name, NoOpCache::new);
	}

	@Override
	public Collection<String> getCacheNames() {
		return Collections.unmodifiableSet(this.cacheMap.keySet());
	}

	@Override
	public void resetCaches() {
		this.cacheMap.clear();
	}

}
