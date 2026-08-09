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

import java.util.concurrent.Callable;

import org.jspecify.annotations.Nullable;

import org.springframework.cache.Cache;

/**
 * 缓存相关异常的处理策略。多数情况下，缓存提供者抛出的异常应直接向上抛出；
 * 但在某些场景下，基础设施需要以不同方式消化这些异常。
 *
 * <p>典型用法：按 key 从缓存读取失败时，可不向上抛出异常，而是透明地当作缓存未命中处理。
 *
 * @author Stephane Nicoll
 * @since 4.1
 */
public interface CacheErrorHandler {

	/**
	 * 处理缓存提供者在按 {@code key} 读取条目时抛出的运行时异常，可选择重新抛出为致命异常。
	 * <p>对于默认的 {@code @Cacheable} 配置，初次缓存访问失败后会调用此方法；
	 * 随后的 put 步骤可能独立失败并由 {@link #handleCachePutError} 处理。
	 * 对于 {@code @Cacheable(sync=true)}，仅有合并的 get 步骤，失败时只调用
	 * {@code handleCacheGetError}，初次访问失败后不再单独尝试 put。
	 * @param exception the exception thrown by the cache provider
	 * @param cache the cache
	 * @param key the key used to get the item
	 * @see Cache#get(Object)
	 * @see Cache#get(Object, Callable)
	 */
	void handleCacheGetError(RuntimeException exception, Cache cache, Object key);

	/**
	 * 处理缓存提供者在按 {@code key} 和 {@code value} 更新条目时抛出的运行时异常。
	 * @param exception the exception thrown by the cache provider
	 * @param cache the cache
	 * @param key the key used to update the item
	 * @param value the value to associate with the key
	 * @see Cache#put(Object, Object)
	 */
	void handleCachePutError(RuntimeException exception, Cache cache, Object key, @Nullable Object value);

	/**
	 * 处理缓存提供者在按 {@code key} 清除条目时抛出的运行时异常。
	 * @param exception the exception thrown by the cache provider
	 * @param cache the cache
	 * @param key the key used to clear the item
	 */
	void handleCacheEvictError(RuntimeException exception, Cache cache, Object key);

	/**
	 * 处理缓存提供者在清空整个 {@link Cache} 时抛出的运行时异常。
	 * @param exception the exception thrown by the cache provider
	 * @param cache the cache to clear
	 */
	void handleCacheClearError(RuntimeException exception, Cache cache);

}
