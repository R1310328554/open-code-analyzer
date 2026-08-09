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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.core.annotation.AliasFor;

/**
 * {@code @CacheConfig} 提供在类级别共享通用缓存相关设置的机制。
 *
 * <p>当此注解出现在给定类上时，它为该类中定义的任何缓存操作提供一组默认设置。
 *
 * @author Stephane Nicoll
 * @author Sam Brannen
 * @since 4.1
 * @see Cacheable
 * @see CachePut
 * @see CacheEvict
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CacheConfig {

	/**
	 * {@link #cacheNames} 的别名。
	 * <p>适用于无需其他属性时，例如：{@code @CacheConfig("books")}。
	 * @since 6.2.9
	 */
	@AliasFor("cacheNames")
	String[] value() default {};

	/**
	 * 注解类中定义的缓存操作要考虑的默认缓存名称。
	 * <p>若操作级别未设置，则使用这些名称而非默认值。
	 * <p>名称可用于确定目标缓存，通过配置的 {@link #cacheResolver()} 解析，
	 * 通常委托给 {@link org.springframework.cache.CacheManager#getCache}。
	 * 更多细节参见 {@link Cacheable#cacheNames()}。
	 * @see #value
	 */
	@AliasFor("value")
	String[] cacheNames() default {};

	/**
	 * 用于该类的默认 {@link org.springframework.cache.interceptor.KeyGenerator} 的 Bean 名称。
	 * <p>若操作级别未设置，则使用此生成器而非默认值。
	 * <p>键生成器与自定义键互斥。当为操作定义了此类键时，忽略此键生成器的值。
	 */
	String keyGenerator() default "";

	/**
	 * 用于创建默认 {@link org.springframework.cache.interceptor.CacheResolver} 的
	 * 自定义 {@link org.springframework.cache.CacheManager} 的 Bean 名称（若尚未设置）。
	 * <p>若操作级别未设置解析器和缓存管理器，且未通过 {@link #cacheResolver} 设置缓存解析器，
	 * 则使用此管理器而非默认值。
	 * @see org.springframework.cache.interceptor.SimpleCacheResolver
	 */
	String cacheManager() default "";

	/**
	 * 要使用的自定义 {@link org.springframework.cache.interceptor.CacheResolver} 的 Bean 名称。
	 * <p>若操作级别未设置解析器和缓存管理器，则使用此解析器而非默认值。
	 */
	String cacheResolver() default "";

}
