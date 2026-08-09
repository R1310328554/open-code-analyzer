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

import java.lang.reflect.Method;
import java.util.Collection;

import org.jspecify.annotations.Nullable;

import org.springframework.cache.interceptor.CacheOperation;

/**
 * 解析已知缓存注解类型的策略接口。
 * {@link AnnotationCacheOperationSource} 委托此类解析器以支持特定注解类型，
 * 例如 Spring 自身的 {@link Cacheable}、{@link CachePut} 和 {@link CacheEvict}。
 *
 * @author Costin Leau
 * @author Stephane Nicoll
 * @author Juergen Hoeller
 * @since 3.1
 * @see AnnotationCacheOperationSource
 * @see SpringCacheAnnotationParser
 */
public interface CacheAnnotationParser {

	/**
	 * 判断给定类是否为本 {@code CacheAnnotationParser} 注解格式下
	 * 缓存操作的候选类。
	 * <p>若返回 {@code false}，给定类上的方法将不会为 {@code #parseCacheAnnotations}
	 * 自省而遍历。因此 {@code false} 是对不受影响类的优化；
	 * {@code true} 表示需对给定类上每个方法单独完整自省。
	 * @param targetClass 要自省的类
	 * @return 若已知类在类或方法级别无缓存操作注解则为 {@code false}；
	 * 否则为 {@code true}。默认实现返回 {@code true}，即进行常规自省。
	 * @since 5.2
	 */
	default boolean isCandidateClass(Class<?> targetClass) {
		return true;
	}

	/**
	 * 基于本解析器理解的注解类型，解析给定类的缓存定义。
	 * <p>实质上将已知缓存注解解析为 Spring 的元数据属性类。
	 * 若类不可缓存则返回 {@code null}。
	 * @param type 带注解的类
	 * @return 已配置的缓存操作，或若未找到则为 {@code null}
	 * @see AnnotationCacheOperationSource#findCacheOperations(Class)
	 */
	@Nullable Collection<CacheOperation> parseCacheAnnotations(Class<?> type);

	/**
	 * 基于本解析器理解的注解类型，解析给定方法的缓存定义。
	 * <p>实质上将已知缓存注解解析为 Spring 的元数据属性类。
	 * 若方法不可缓存则返回 {@code null}。
	 * @param method 带注解的方法
	 * @return 已配置的缓存操作，或若未找到则为 {@code null}
	 * @see AnnotationCacheOperationSource#findCacheOperations(Method)
	 */
	@Nullable Collection<CacheOperation> parseCacheAnnotations(Method method);

}
