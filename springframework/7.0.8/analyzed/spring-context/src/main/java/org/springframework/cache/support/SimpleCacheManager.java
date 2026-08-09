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

import org.springframework.cache.Cache;

/**
 * 基于给定 {@link Cache} 集合工作的简单缓存管理器。
 * 适用于测试或简单的缓存声明场景。
 *
 * <p>若直接使用本实现（而非通过常规 Bean 注册），在
 * {@linkplain #setCaches(Collection) 提供缓存集合}后应调用
 * {@link #initializeCaches()} 以初始化内部状态。
 *
 * @author Costin Leau
 * @since 3.1
 * @see NoOpCache
 * @see org.springframework.cache.concurrent.ConcurrentMapCache
 */
public class SimpleCacheManager extends AbstractCacheManager {

	/** 本 CacheManager 使用的 Cache 实例集合。 */
	private Collection<? extends Cache> caches = Collections.emptySet();


	/**
	 * 指定本 CacheManager 使用的 Cache 实例集合。
	 * @see #initializeCaches()
	 */
	public void setCaches(Collection<? extends Cache> caches) {
		this.caches = caches;
	}

	@Override
	protected Collection<? extends Cache> loadCaches() {
		return this.caches;
	}

}
