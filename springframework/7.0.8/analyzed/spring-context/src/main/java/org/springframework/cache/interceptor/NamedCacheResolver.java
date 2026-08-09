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

import java.util.Collection;
import java.util.List;

import org.jspecify.annotations.Nullable;

import org.springframework.cache.CacheManager;

/**
 * 强制将缓存解析为给定 {@link CacheManager} 上可配置名称集合的 {@link CacheResolver}。
 *
 * @author Stephane Nicoll
 * @since 4.1
 */
public class NamedCacheResolver extends AbstractCacheResolver {

	/** 本解析器使用的缓存名称集合。 */
	private @Nullable Collection<String> cacheNames;


	public NamedCacheResolver() {
	}

	public NamedCacheResolver(CacheManager cacheManager, String... cacheNames) {
		super(cacheManager);
		this.cacheNames = List.of(cacheNames);
	}


	/**
	 * 设置本解析器应使用的缓存名称。
	 */
	public void setCacheNames(Collection<String> cacheNames) {
		this.cacheNames = cacheNames;
	}

	@Override
	protected @Nullable Collection<String> getCacheNames(CacheOperationInvocationContext<?> context) {
		return this.cacheNames;
	}

}
