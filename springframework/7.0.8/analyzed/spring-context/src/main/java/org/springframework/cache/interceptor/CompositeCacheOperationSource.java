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

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;

import org.jspecify.annotations.Nullable;

import org.springframework.util.Assert;

/**
 * 组合式 {@link CacheOperationSource} 实现，遍历给定的
 * {@code CacheOperationSource} 实例数组并聚合结果。
 *
 * @author Costin Leau
 * @author Juergen Hoeller
 * @since 3.1
 */
@SuppressWarnings("serial")
public class CompositeCacheOperationSource implements CacheOperationSource, Serializable {

	/** 被组合的缓存操作来源数组。 */
	private final CacheOperationSource[] cacheOperationSources;


	/**
	 * 根据给定来源创建新的 CompositeCacheOperationSource。
	 * @param cacheOperationSources 要组合的 CacheOperationSource 实例
	 */
	public CompositeCacheOperationSource(CacheOperationSource... cacheOperationSources) {
		Assert.notEmpty(cacheOperationSources, "CacheOperationSource array must not be empty");
		this.cacheOperationSources = cacheOperationSources;
	}

	/**
	 * 返回本 {@code CompositeCacheOperationSource} 所组合的
	 * {@code CacheOperationSource} 实例。
	 */
	public final CacheOperationSource[] getCacheOperationSources() {
		return this.cacheOperationSources;
	}


	@Override
	public boolean isCandidateClass(Class<?> targetClass) {
		// 任一子来源认为候选则视为候选
		for (CacheOperationSource source : this.cacheOperationSources) {
			if (source.isCandidateClass(targetClass)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean hasCacheOperations(Method method, @Nullable Class<?> targetClass) {
		for (CacheOperationSource source : this.cacheOperationSources) {
			if (source.hasCacheOperations(method, targetClass)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public @Nullable Collection<CacheOperation> getCacheOperations(Method method, @Nullable Class<?> targetClass) {
		Collection<CacheOperation> ops = null;
		for (CacheOperationSource source : this.cacheOperationSources) {
			Collection<CacheOperation> cacheOperations = source.getCacheOperations(method, targetClass);
			if (cacheOperations != null) {
				if (ops == null) {
					ops = new ArrayList<>();
				}
				ops.addAll(cacheOperations);
			}
		}
		return ops;
	}

}
