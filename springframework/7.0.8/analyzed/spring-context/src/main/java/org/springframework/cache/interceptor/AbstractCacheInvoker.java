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
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;

import org.springframework.cache.Cache;
import org.springframework.util.function.SingletonSupplier;

/**
 * 调用 {@link Cache} 操作的基础组件，发生异常时使用可配置的
 * {@link CacheErrorHandler} 处理。
 *
 * @author Stephane Nicoll
 * @author Juergen Hoeller
 * @author Simon Baslé
 * @since 4.1
 * @see org.springframework.cache.interceptor.CacheErrorHandler
 */
public abstract class AbstractCacheInvoker {

	/** 缓存异常处理器；默认使用 {@link SimpleCacheErrorHandler}（直接抛出异常）。 */
	protected SingletonSupplier<CacheErrorHandler> errorHandler;


	protected AbstractCacheInvoker() {
		this.errorHandler = SingletonSupplier.of(SimpleCacheErrorHandler::new);
	}

	protected AbstractCacheInvoker(CacheErrorHandler errorHandler) {
		this.errorHandler = SingletonSupplier.of(errorHandler);
	}


	/**
	 * 设置用于处理缓存提供者异常的 {@link CacheErrorHandler} 实例。
	 * 默认使用 {@link SimpleCacheErrorHandler}，即原样抛出异常。
	 */
	public void setErrorHandler(CacheErrorHandler errorHandler) {
		this.errorHandler = SingletonSupplier.of(errorHandler);
	}

	/**
	 * 返回当前使用的 {@link CacheErrorHandler}。
	 */
	public CacheErrorHandler getErrorHandler() {
		return this.errorHandler.obtain();
	}


	/**
	 * 在指定 {@link Cache} 上执行 {@link Cache#get(Object)}；
	 * 异常时委托错误处理器，若处理器未重新抛出则返回 {@code null} 模拟缓存未命中。
	 * @see Cache#get(Object)
	 */
	protected Cache.@Nullable ValueWrapper doGet(Cache cache, Object key) {
		try {
			return cache.get(key);
		}
		catch (RuntimeException ex) {
			getErrorHandler().handleCacheGetError(ex, cache, key);
			return null;  // 异常被处理后，当作缓存未命中
		}
	}

	/**
	 * 在指定 {@link Cache} 上执行 {@link Cache#get(Object, Callable)}；
	 * 异常时委托错误处理器，若处理器未重新抛出则调用 {@code valueLoader} 模拟 read-through。
	 * @since 6.2
	 * @see Cache#get(Object, Callable)
	 */
	protected <T> @Nullable T doGet(Cache cache, Object key, Callable<T> valueLoader) {
		try {
			return cache.get(key, valueLoader);
		}
		catch (Cache.ValueRetrievalException ex) {
			throw ex;
		}
		catch (RuntimeException ex) {
			getErrorHandler().handleCacheGetError(ex, cache, key);
			try {
				return valueLoader.call();
			}
			catch (Exception ex2) {
				throw new Cache.ValueRetrievalException(key, valueLoader, ex);
			}
		}
	}


	/**
	 * 在指定 {@link Cache} 上执行 {@link Cache#retrieve(Object)}；
	 * 异常时委托错误处理器，若处理器未重新抛出则返回 {@code null} 模拟缓存未命中。
	 * @since 6.2
	 * @see Cache#retrieve(Object)
	 */
	protected @Nullable CompletableFuture<?> doRetrieve(Cache cache, Object key) {
		try {
			return cache.retrieve(key);
		}
		catch (RuntimeException ex) {
			getErrorHandler().handleCacheGetError(ex, cache, key);
			return null;
		}
	}

	/**
	 * 在指定 {@link Cache} 上执行 {@link Cache#retrieve(Object, Supplier)}；
	 * 异常时委托错误处理器，若处理器未重新抛出则调用 {@code valueLoader} 模拟 read-through。
	 * @since 6.2
	 * @see Cache#retrieve(Object, Supplier)
	 */
	protected <T> CompletableFuture<T> doRetrieve(Cache cache, Object key, Supplier<CompletableFuture<T>> valueLoader) {
		try {
			return cache.retrieve(key, valueLoader);
		}
		catch (RuntimeException ex) {
			getErrorHandler().handleCacheGetError(ex, cache, key);
			return valueLoader.get();
		}
	}

	/**
	 * 在指定 {@link Cache} 上执行 {@link Cache#put(Object, Object)}，异常时委托错误处理器。
	 */
	protected void doPut(Cache cache, Object key, @Nullable Object value) {
		try {
			cache.put(key, value);
		}
		catch (RuntimeException ex) {
			getErrorHandler().handleCachePutError(ex, cache, key, value);
		}
	}

	/**
	 * 在指定 {@link Cache} 上执行 {@link Cache#evict(Object)} 或
	 * {@link Cache#evictIfPresent(Object)}（由 {@code immediate} 决定），异常时委托错误处理器。
	 */
	protected void doEvict(Cache cache, Object key, boolean immediate) {
		try {
			if (immediate) {
				cache.evictIfPresent(key);
			}
			else {
				cache.evict(key);
			}
		}
		catch (RuntimeException ex) {
			getErrorHandler().handleCacheEvictError(ex, cache, key);
		}
	}

	/**
	 * 在指定 {@link Cache} 上执行 {@link Cache#clear()} 或 {@link Cache#invalidate()}，
	 * 由 {@code immediate} 决定，异常时委托错误处理器。
	 */
	protected void doClear(Cache cache, boolean immediate) {
		try {
			if (immediate) {
				cache.invalidate();
			}
			else {
				cache.clear();
			}
		}
		catch (RuntimeException ex) {
			getErrorHandler().handleCacheClearError(ex, cache);
		}
	}

}
