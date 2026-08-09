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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;

import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.core.MethodClassKey;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;

/**
 * {@link CacheOperationSource} 的抽象实现：缓存已解析的方法级操作元数据，
 * 并按以下回退策略查找：1. 目标类上的具体方法；2. 目标类；3. 声明方法；4. 声明类/接口。
 *
 * <p>若目标方法未关联缓存操作，则回退到目标类上声明的元数据。
 * 方法级声明会完全覆盖类级声明。
 * 若目标类上未找到，还会检查 JDK 代理场景下调用所经过的接口。
 *
 * @author Costin Leau
 * @author Juergen Hoeller
 * @since 3.1
 */
public abstract class AbstractFallbackCacheOperationSource implements CacheOperationSource {

	/**
	 * 缓存中的哨兵值：表示该方法无缓存操作，后续无需再查找。
	 */
	private static final Collection<CacheOperation> NULL_CACHING_MARKER = Collections.emptyList();


	/**
	 * 子类可用的日志记录器。
	 * <p>本基类未实现 {@link java.io.Serializable}，反序列化后日志会重建
	 * （前提是具体子类可序列化）。
	 */
	protected final Log logger = LogFactory.getLog(getClass());

	/**
	 * 已解析的 {@link CacheOperation} 缓存，键为特定目标类上的方法。
	 * <p>本基类未实现 {@link java.io.Serializable}，反序列化后缓存会重建。
	 */
	private final Map<Object, Collection<CacheOperation>> operationCache = new ConcurrentHashMap<>(1024);


	@Override
	public boolean hasCacheOperations(Method method, @Nullable Class<?> targetClass) {
		return !CollectionUtils.isEmpty(getCacheOperations(method, targetClass, false));
	}

	@Override
	public @Nullable Collection<CacheOperation> getCacheOperations(Method method, @Nullable Class<?> targetClass) {
		return getCacheOperations(method, targetClass, true);
	}

	/**
	 * 确定本次方法调用的缓存操作。
	 * <p>方法级元数据缺失时，回退到类级声明的元数据。
	 * @param method the method for the current invocation (never {@code null})
	 * @param targetClass the target class for this invocation (can be {@code null})
	 * @param cacheNull whether {@code null} results should be cached as well
	 * @return {@link CacheOperation} for this method, or {@code null} if the method
	 * is not cacheable
	 */
	private @Nullable Collection<CacheOperation> getCacheOperations(
			Method method, @Nullable Class<?> targetClass, boolean cacheNull) {

		if (ReflectionUtils.isObjectMethod(method)) {
			return null;
		}

		Object cacheKey = getCacheKey(method, targetClass);
		Collection<CacheOperation> cached = this.operationCache.get(cacheKey);

		if (cached != null) {
			return (cached != NULL_CACHING_MARKER ? cached : null);
		}
		else {
			Collection<CacheOperation> cacheOps = computeCacheOperations(method, targetClass);
			if (cacheOps != null) {
				if (logger.isTraceEnabled()) {
					logger.trace("Adding cacheable method '" + method.getName() + "' with operations: " + cacheOps);
				}
				this.operationCache.put(cacheKey, cacheOps);
			}
			else if (cacheNull) {
				// 缓存「无操作」结果，避免重复解析
				this.operationCache.put(cacheKey, NULL_CACHING_MARKER);
			}
			return cacheOps;
		}
	}

	/**
	 * 为给定方法和目标类生成缓存键。
	 * <p>重载方法不得产生相同键；同一方法的不同实例必须产生相同键。
	 * @param method the method (never {@code null})
	 * @param targetClass the target class (may be {@code null})
	 * @return the cache key (never {@code null})
	 */
	protected Object getCacheKey(Method method, @Nullable Class<?> targetClass) {
		return new MethodClassKey(method, targetClass);
	}

	private @Nullable Collection<CacheOperation> computeCacheOperations(Method method, @Nullable Class<?> targetClass) {
		// 若配置为仅允许 public 方法，则跳过非 public
		if (allowPublicMethodsOnly() && !Modifier.isPublic(method.getModifiers())) {
			return null;
		}
		// 跳过 BeanFactoryAware 的 setBeanFactory 方法
		if (method.getDeclaringClass() == BeanFactoryAware.class) {
			return null;
		}

		// 方法可能声明在接口上，但元数据需从目标类获取
		Method specificMethod = AopUtils.getMostSpecificMethod(method, targetClass);

		// 第一优先：目标类上的具体方法
		Collection<CacheOperation> opDef = findCacheOperations(specificMethod);
		if (opDef != null) {
			return opDef;
		}

		// 第二优先：目标类上的类级缓存操作
		opDef = findCacheOperations(specificMethod.getDeclaringClass());
		if (opDef != null && ClassUtils.isUserLevelMethod(method)) {
			return opDef;
		}

		if (specificMethod != method) {
			// 回退：原始方法上的操作
			opDef = findCacheOperations(method);
			if (opDef != null) {
				return opDef;
			}
			// 最后回退：原始方法声明类上的操作
			opDef = findCacheOperations(method.getDeclaringClass());
			if (opDef != null && ClassUtils.isUserLevelMethod(method)) {
				return opDef;
			}
		}

		return null;
	}


	/**
	 * 子类实现：返回给定类关联的缓存操作（如有）。
	 * @param clazz the class to retrieve the cache operations for
	 * @return all cache operations associated with this class, or {@code null} if none
	 */
	protected abstract @Nullable Collection<CacheOperation> findCacheOperations(Class<?> clazz);

	/**
	 * 子类实现：返回给定方法关联的缓存操作（如有）。
	 * @param method the method to retrieve the cache operations for
	 * @return all cache operations associated with this method, or {@code null} if none
	 */
	protected abstract @Nullable Collection<CacheOperation> findCacheOperations(Method method);

	/**
	 * 是否仅允许 public 方法具有缓存语义？
	 * <p>默认实现返回 {@code false}。
	 */
	protected boolean allowPublicMethodsOnly() {
		return false;
	}

}
