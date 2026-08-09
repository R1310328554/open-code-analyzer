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

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.jspecify.annotations.Nullable;

import org.springframework.cache.interceptor.AbstractFallbackCacheOperationSource;
import org.springframework.cache.interceptor.CacheOperation;
import org.springframework.util.Assert;

/**
 * 用于处理注解格式缓存元数据的
 * {@link org.springframework.cache.interceptor.CacheOperationSource CacheOperationSource} 接口实现。
 *
 * <p>本类读取 Spring 的 {@link Cacheable}、{@link CachePut} 和 {@link CacheEvict} 注解，
 * 并向 Spring 缓存基础设施暴露对应的缓存操作定义。也可作为自定义
 * {@code CacheOperationSource} 的基类。
 *
 * @author Costin Leau
 * @author Juergen Hoeller
 * @author Stephane Nicoll
 * @since 3.1
 */
@SuppressWarnings("serial")
public class AnnotationCacheOperationSource extends AbstractFallbackCacheOperationSource implements Serializable {

	private final Set<CacheAnnotationParser> annotationParsers;

	/** 是否仅处理 public 方法上的缓存注解。 */
	private boolean publicMethodsOnly = true;


	/**
	 * 创建默认的 AnnotationCacheOperationSource，支持携带 {@code Cacheable}
	 * 和 {@code CacheEvict} 注解的 public 方法。
	 */
	public AnnotationCacheOperationSource() {
		this.annotationParsers = Collections.singleton(new SpringCacheAnnotationParser());
	}

	/**
	 * 创建默认的 {@code AnnotationCacheOperationSource}，支持携带 {@code Cacheable}
	 * 和 {@code CacheEvict} 注解的 public 方法。
	 * @param publicMethodsOnly 是否仅支持带注解的 public 方法（通常用于基于代理的 AOP），
	 * 或同时支持 protected/private 方法（通常用于 AspectJ 类织入）
	 * @see #setPublicMethodsOnly
	 */
	public AnnotationCacheOperationSource(boolean publicMethodsOnly) {
		this();
		this.publicMethodsOnly = publicMethodsOnly;
	}

	/**
	 * 创建自定义 AnnotationCacheOperationSource。
	 * @param annotationParser 要使用的 CacheAnnotationParser
	 */
	public AnnotationCacheOperationSource(CacheAnnotationParser annotationParser) {
		Assert.notNull(annotationParser, "CacheAnnotationParser must not be null");
		this.annotationParsers = Collections.singleton(annotationParser);
	}

	/**
	 * 创建自定义 AnnotationCacheOperationSource。
	 * @param annotationParsers 要使用的 CacheAnnotationParser
	 */
	public AnnotationCacheOperationSource(CacheAnnotationParser... annotationParsers) {
		Assert.notEmpty(annotationParsers, "At least one CacheAnnotationParser needs to be specified");
		this.annotationParsers = Set.of(annotationParsers);
	}

	/**
	 * 创建自定义 AnnotationCacheOperationSource。
	 * @param annotationParsers 要使用的 CacheAnnotationParser
	 */
	public AnnotationCacheOperationSource(Set<CacheAnnotationParser> annotationParsers) {
		Assert.notEmpty(annotationParsers, "At least one CacheAnnotationParser needs to be specified");
		this.annotationParsers = annotationParsers;
	}


	/**
	 * 设置可缓存方法是否应为 public。
	 * <p>默认为 {@code true}。
	 * @since 6.2
	 */
	public void setPublicMethodsOnly(boolean publicMethodsOnly) {
		this.publicMethodsOnly = publicMethodsOnly;
	}


	@Override
	public boolean isCandidateClass(Class<?> targetClass) {
		for (CacheAnnotationParser parser : this.annotationParsers) {
			if (parser.isCandidateClass(targetClass)) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected @Nullable Collection<CacheOperation> findCacheOperations(Class<?> clazz) {
		return determineCacheOperations(parser -> parser.parseCacheAnnotations(clazz));
	}

	@Override
	protected @Nullable Collection<CacheOperation> findCacheOperations(Method method) {
		return determineCacheOperations(parser -> parser.parseCacheAnnotations(method));
	}

	/**
	 * 为给定 {@link CacheOperationProvider} 确定缓存操作。
	 * <p>本实现委托给已配置的 {@link CacheAnnotationParser CacheAnnotationParsers}，
	 * 将已知注解解析为 Spring 的元数据属性类。
	 * <p>可覆盖以支持携带缓存元数据的自定义注解。
	 * @param provider 要使用的缓存操作提供者
	 * @return 已配置的缓存操作，或若未找到则为 {@code null}
	 */
	protected @Nullable Collection<CacheOperation> determineCacheOperations(CacheOperationProvider provider) {
		Collection<CacheOperation> ops = null;
		// 遍历所有注解解析器并合并结果
		for (CacheAnnotationParser parser : this.annotationParsers) {
			Collection<CacheOperation> annOps = provider.getCacheOperations(parser);
			if (annOps != null) {
				if (ops == null) {
					ops = annOps;
				}
				else {
					// 合并多个解析器的结果
					Collection<CacheOperation> combined = new ArrayList<>(ops.size() + annOps.size());
					combined.addAll(ops);
					combined.addAll(annOps);
					ops = combined;
				}
			}
		}
		return ops;
	}

	/**
	 * 默认情况下，仅 public 方法可设为可缓存。
	 * @see #setPublicMethodsOnly
	 */
	@Override
	protected boolean allowPublicMethodsOnly() {
		return this.publicMethodsOnly;
	}


	@Override
	public boolean equals(@Nullable Object other) {
		return (this == other || (other instanceof AnnotationCacheOperationSource otherCos &&
				this.annotationParsers.equals(otherCos.annotationParsers) &&
				this.publicMethodsOnly == otherCos.publicMethodsOnly));
	}

	@Override
	public int hashCode() {
		return this.annotationParsers.hashCode();
	}


	/**
	 * 回调接口，基于给定 {@link CacheAnnotationParser} 提供 {@link CacheOperation} 实例。
	 */
	@FunctionalInterface
	protected interface CacheOperationProvider {

		/**
		 * 返回指定解析器提供的 {@link CacheOperation} 实例。
		 * @param parser 要使用的解析器
		 * @return 缓存操作，或若未找到则为 {@code null}
		 */
		@Nullable Collection<CacheOperation> getCacheOperations(CacheAnnotationParser parser);
	}

}
