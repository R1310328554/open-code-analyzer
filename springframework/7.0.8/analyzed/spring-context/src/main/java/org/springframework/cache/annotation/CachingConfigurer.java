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

package org.springframework.cache.annotation;

import org.jspecify.annotations.Nullable;

import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.cache.interceptor.KeyGenerator;

/**
 * 为基于注解的缓存管理显式指定如何解析缓存及如何生成键的接口。
 *
 * <p>通常由带有 @{@link org.springframework.context.annotation.Configuration
 * Configuration} 注解并实现 @{@link EnableCaching} 的配置类实现。
 * 一般示例和上下文参见 @{@link EnableCaching}；详细说明参见
 * {@link #cacheManager()}、{@link #cacheResolver()}、{@link #keyGenerator()}
 * 和 {@link #errorHandler()}。
 *
 * <p><b>注意：{@code CachingConfigurer} 将较早初始化。</b>
 * 不要直接向自动装配字段注入常见依赖；可考虑为这些依赖声明惰性
 * {@link org.springframework.beans.factory.ObjectProvider}。
 *
 * @author Chris Beams
 * @author Stephane Nicoll
 * @since 3.1
 * @see EnableCaching
 */
public interface CachingConfigurer {

	/**
	 * 返回用于基于注解的缓存管理的缓存管理器 Bean。
	 * 将在此缓存管理器背后初始化默认 {@link CacheResolver}。
	 * 若要更精细地管理缓存解析，可考虑直接设置 {@link CacheResolver}。
	 * <p>实现必须显式声明
	 * {@link org.springframework.context.annotation.Bean @Bean}，以便缓存管理器
	 * 参与上下文生命周期，例如：
	 * <pre class="code">
	 * &#064;Configuration
	 * &#064;EnableCaching
	 * class AppConfig implements CachingConfigurer {
	 *     &#064;Bean // important!
	 *     &#064;Override
	 *     CacheManager cacheManager() {
	 *         // configure and return CacheManager instance
	 *     }
	 *     // ...
	 * }
	 * </pre>
	 * 更完整示例参见 @{@link EnableCaching}。
	 */
	default @Nullable CacheManager cacheManager() {
		return null;
	}

	/**
	 * 返回用于为基于注解的缓存管理解析常规缓存的 {@link CacheResolver} Bean。
	 * 这是指定所用 {@link CacheManager} 的替代且更强大的选项。
	 * <p>若同时设置了 {@link #cacheManager()} 和 {@code cacheResolver()}，则忽略缓存管理器。
	 * <p>实现必须显式声明
	 * {@link org.springframework.context.annotation.Bean @Bean}，以便缓存解析器
	 * 参与上下文生命周期，例如：
	 * <pre class="code">
	 * &#064;Configuration
	 * &#064;EnableCaching
	 * class AppConfig implements CachingConfigurer {
	 *     &#064;Bean // important!
	 *     &#064;Override
	 *     CacheResolver cacheResolver() {
	 *         // configure and return CacheResolver instance
	 *     }
	 *     // ...
	 * }
	 * </pre>
	 * 更完整示例参见 {@link EnableCaching}。
	 */
	default @Nullable CacheResolver cacheResolver() {
		return null;
	}

	/**
	 * 返回用于基于注解的缓存管理的键生成器 Bean。
	 * <p>默认使用 {@link org.springframework.cache.interceptor.SimpleKeyGenerator}。
	 * 更完整示例参见 @{@link EnableCaching}。
	 */
	default @Nullable KeyGenerator keyGenerator() {
		return null;
	}

	/**
	 * 返回用于处理缓存相关错误的 {@link CacheErrorHandler}。
	 * <p>默认使用 {@link org.springframework.cache.interceptor.SimpleCacheErrorHandler}，
	 * 将异常抛回客户端。
	 * 更完整示例参见 @{@link EnableCaching}。
	 */
	default @Nullable CacheErrorHandler errorHandler() {
		return null;
	}

}
