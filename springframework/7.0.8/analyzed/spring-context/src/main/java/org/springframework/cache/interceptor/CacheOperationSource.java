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
import java.util.Collection;

import org.jspecify.annotations.Nullable;

import org.springframework.util.CollectionUtils;

/**
 * 供 {@link CacheInterceptor} 使用的接口。实现类负责提供缓存操作属性，
 * 来源可以是配置、源码级元数据注解或其他途径。
 *
 * @author Costin Leau
 * @author Juergen Hoeller
 * @since 3.1
 */
public interface CacheOperationSource {

	/**
	 * 判断给定类是否为本 {@code CacheOperationSource} 元数据格式下的
	 * 缓存操作候选类。
	 * <p>若返回 {@code false}，则不会遍历该类的方法去执行
	 * {@link #getCacheOperations} 内省，从而跳过无关类。
	 * 返回 {@code true} 表示需要对该类的每个方法逐一完整内省。
	 * @param targetClass 待内省的类
	 * @return 若已知该类在类级或方法级均无缓存操作元数据则返回 {@code false}，
	 * 否则返回 {@code true}。默认实现返回 {@code true}，即执行常规内省
	 * @since 5.2
	 * @see #hasCacheOperations
	 */
	default boolean isCandidateClass(Class<?> targetClass) {
		return true;
	}

	/**
	 * 判断给定方法是否存在缓存操作。
	 * @param method 待内省的方法
	 * @param targetClass 目标类（可为 {@code null}，此时使用方法声明类）
	 * @since 6.2
	 * @see #getCacheOperations
	 */
	default boolean hasCacheOperations(Method method, @Nullable Class<?> targetClass) {
		return !CollectionUtils.isEmpty(getCacheOperations(method, targetClass));
	}

	/**
	 * 返回该方法对应的缓存操作集合；
	 * 若方法不含任何<em>可缓存</em>注解则返回 {@code null}。
	 * @param method 待内省的方法
	 * @param targetClass 目标类（可为 {@code null}，此时使用方法声明类）
	 * @return 该方法的全部缓存操作，未找到时返回 {@code null}
	 */
	@Nullable Collection<CacheOperation> getCacheOperations(Method method, @Nullable Class<?> targetClass);

}
