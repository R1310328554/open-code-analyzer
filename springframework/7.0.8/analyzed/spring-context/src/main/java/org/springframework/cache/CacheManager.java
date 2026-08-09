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

package org.springframework.cache;

import java.util.Collection;

import org.jspecify.annotations.Nullable;

/**
 * Spring 中央缓存管理器 SPI。
 *
 * <p>允许按名称获取 {@link Cache} 区域。
 *
 * @author Costin Leau
 * @author Sam Brannen
 * @author Juergen Hoeller
 * @since 3.1
 */
public interface CacheManager {

	/**
	 * 获取与给定名称关联的缓存。
	 * <p>注意，若原生提供者支持，缓存可能在运行时惰性创建。
	 * @param name 缓存标识符（不得为 {@code null}）
	 * @return 关联的缓存，或若不存在或无法创建则返回 {@code null}
	 */
	@Nullable Cache getCache(String name);

	/**
	 * 获取本管理器已知的缓存名称集合。
	 * @return 缓存管理器已知的所有缓存名称
	 */
	Collection<String> getCacheNames();

	/**
	 * 若可能，从本缓存管理器移除所有已注册缓存，并按需重新创建。
	 * 调用后，{@link #getCacheNames()} 可能为空，缓存提供者将丢弃所有缓存管理状态。
	 * <p>或者，实现可对固定现有缓存区域执行等效重置而不实际丢弃缓存。
	 * 此行为表现为 {@link #getCacheNames()} 仍暴露非空名称集，
	 * 而对应缓存区域不再包含缓存条目。
	 * <p>默认实现对所有已注册缓存调用 {@link Cache#clear}，保留所有已注册缓存，
	 * 满足上述替代实现路径。自定义实现可丢弃实际缓存（按需重新创建），
	 * 或在实际缓存提供者级别执行更彻底的重置。
	 * @since 7.0.2
	 * @see Cache#clear()
	 */
	default void resetCaches() {
		// 遍历所有已知缓存名称
		for (String cacheName : getCacheNames()) {
			Cache cache = getCache(cacheName);
			// 对每个缓存执行清空
			if (cache != null) {
				cache.clear();
			}
		}
	}

}
