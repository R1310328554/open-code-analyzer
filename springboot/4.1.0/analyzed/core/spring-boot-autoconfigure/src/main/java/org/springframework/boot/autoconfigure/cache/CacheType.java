/*
 * Copyright 2012-present the original author or authors.
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

package org.springframework.boot.autoconfigure.cache;

/**
 * 支持的缓存类型（按优先级顺序定义）。
 *
 * @author Stephane Nicoll
 * @author Phillip Webb
 * @author Eddú Meléndez
 * @since 4.0.0
 */
public enum CacheType {

	/**
	 * 使用上下文中 {@code Cache} Bean 的通用缓存。
	 */
	GENERIC,

	/**
	 * 基于 JCache（JSR-107）的缓存。
	 */
	JCACHE,

	/**
	 * 基于 Hazelcast 的缓存。
	 */
	HAZELCAST,

	/**
	 * 基于 Couchbase 的缓存。
	 */
	COUCHBASE,

	/**
	 * 基于 Infinispan 的缓存。
	 */
	INFINISPAN,

	/**
	 * 基于 Redis 的缓存。
	 */
	REDIS,

	/**
	 * 基于 Cache2k 的缓存。
	 */
	CACHE2K,

	/**
	 * 基于 Caffeine 的缓存。
	 */
	CAFFEINE,

	/**
	 * 简单的内存缓存。
	 */
	SIMPLE,

	/**
	 * 不使用缓存。
	 */
	NONE

}
