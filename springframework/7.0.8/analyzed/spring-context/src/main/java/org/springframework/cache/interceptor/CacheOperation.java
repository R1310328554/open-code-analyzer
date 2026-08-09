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

import java.util.Collections;
import java.util.Set;

import org.jspecify.annotations.Nullable;

import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

/**
 * 缓存操作的基类。
 *
 * @author Costin Leau
 * @author Stephane Nicoll
 * @author Marcin Kamionowski
 * @since 3.1
 */
public abstract class CacheOperation implements BasicOperation {

	/** 操作名称（通常为注解所在元素描述）。 */
	private final String name;

	/** 目标缓存名称集合。 */
	private final Set<String> cacheNames;

	/** 缓存 key 的 SpEL 表达式。 */
	private final String key;

	/** 键生成器 Bean 名称。 */
	private final String keyGenerator;

	/** 缓存管理器 Bean 名称。 */
	private final String cacheManager;

	/** 缓存解析器 Bean 名称。 */
	private final String cacheResolver;

	/** 执行条件的 SpEL 表达式。 */
	private final String condition;

	/** 缓存的 {@code toString()} 结果，用于 {@link #equals} 与 {@link #hashCode}。 */
	private final String toString;


	/**
	 * 根据给定构建器创建新的 {@link CacheOperation} 实例。
	 * @since 4.3
	 */
	protected CacheOperation(Builder b) {
		this.name = b.name;
		this.cacheNames = b.cacheNames;
		this.key = b.key;
		this.keyGenerator = b.keyGenerator;
		this.cacheManager = b.cacheManager;
		this.cacheResolver = b.cacheResolver;
		this.condition = b.condition;
		this.toString = b.getOperationDescription().toString();
	}


	/** 返回操作名称。 */
	public String getName() {
		return this.name;
	}

	@Override
	public Set<String> getCacheNames() {
		return this.cacheNames;
	}

	/** 返回缓存 key 的 SpEL 表达式。 */
	public String getKey() {
		return this.key;
	}

	/** 返回键生成器 Bean 名称。 */
	public String getKeyGenerator() {
		return this.keyGenerator;
	}

	/** 返回缓存管理器 Bean 名称。 */
	public String getCacheManager() {
		return this.cacheManager;
	}

	/** 返回缓存解析器 Bean 名称。 */
	public String getCacheResolver() {
		return this.cacheResolver;
	}

	/** 返回执行条件的 SpEL 表达式。 */
	public String getCondition() {
		return this.condition;
	}


	/**
	 * 通过比较 {@code toString()} 结果判断相等性。
	 * @see #toString()
	 */
	@Override
	public boolean equals(@Nullable Object other) {
		return (other instanceof CacheOperation && toString().equals(other.toString()));
	}

	/**
	 * 返回 {@code toString()} 的哈希码。
	 * @see #toString()
	 */
	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	/**
	 * 返回用于标识本缓存操作的描述字符串。
	 * <p>该值在对象构造时通过调用 {@link Builder#getOperationDescription()} 生成，
	 * 并用于 {@link #hashCode} 与 {@link #equals}。
	 * @see Builder#getOperationDescription()
	 */
	@Override
	public final String toString() {
		return this.toString;
	}


	/**
	 * 用于创建 {@link CacheOperation} 的构建器基类。
	 * @since 4.3
	 */
	public abstract static class Builder {

		/** 操作名称。 */
		private String name = "";

		/** 缓存名称集合。 */
		private Set<String> cacheNames = Collections.emptySet();

		/** 缓存 key 表达式。 */
		private String key = "";

		/** 键生成器名称。 */
		private String keyGenerator = "";

		/** 缓存管理器名称。 */
		private String cacheManager = "";

		/** 缓存解析器名称。 */
		private String cacheResolver = "";

		/** 执行条件表达式。 */
		private String condition = "";

		/** 设置操作名称。 */
		public void setName(String name) {
			Assert.hasText(name, "Name must not be empty");
			this.name = name;
		}

		/** 设置单个缓存名称。 */
		public void setCacheName(String cacheName) {
			Assert.hasText(cacheName, "Cache name must not be empty");
			this.cacheNames = Collections.singleton(cacheName);
		}

		/** 设置多个缓存名称。 */
		public void setCacheNames(String... cacheNames) {
			this.cacheNames = CollectionUtils.newLinkedHashSet(cacheNames.length);
			for (String cacheName : cacheNames) {
				Assert.hasText(cacheName, "Cache name must be non-empty if specified");
				this.cacheNames.add(cacheName);
			}
		}

		/** 返回已配置的缓存名称集合。 */
		public Set<String> getCacheNames() {
			return this.cacheNames;
		}

		/** 设置缓存 key 的 SpEL 表达式。 */
		public void setKey(String key) {
			Assert.notNull(key, "Key must not be null");
			this.key = key;
		}

		/** 返回缓存 key 表达式。 */
		public String getKey() {
			return this.key;
		}

		/** 返回键生成器名称。 */
		public String getKeyGenerator() {
			return this.keyGenerator;
		}

		/** 返回缓存管理器名称。 */
		public String getCacheManager() {
			return this.cacheManager;
		}

		/** 返回缓存解析器名称。 */
		public String getCacheResolver() {
			return this.cacheResolver;
		}

		/** 设置键生成器 Bean 名称。 */
		public void setKeyGenerator(String keyGenerator) {
			Assert.notNull(keyGenerator, "KeyGenerator name must not be null");
			this.keyGenerator = keyGenerator;
		}

		/** 设置缓存管理器 Bean 名称。 */
		public void setCacheManager(String cacheManager) {
			Assert.notNull(cacheManager, "CacheManager name must not be null");
			this.cacheManager = cacheManager;
		}

		/** 设置缓存解析器 Bean 名称。 */
		public void setCacheResolver(String cacheResolver) {
			Assert.notNull(cacheResolver, "CacheResolver name must not be null");
			this.cacheResolver = cacheResolver;
		}

		/** 设置执行条件的 SpEL 表达式。 */
		public void setCondition(String condition) {
			Assert.notNull(condition, "Condition must not be null");
			this.condition = condition;
		}

		/**
		 * 返回用于标识本缓存操作的描述字符串。
		 * <p>供子类在 {@code toString()} 中复用。
		 */
		protected StringBuilder getOperationDescription() {
			StringBuilder result = new StringBuilder(getClass().getSimpleName());
			result.append('[').append(this.name);
			result.append("] caches=").append(this.cacheNames);
			result.append(" | key='").append(this.key);
			result.append("' | keyGenerator='").append(this.keyGenerator);
			result.append("' | cacheManager='").append(this.cacheManager);
			result.append("' | cacheResolver='").append(this.cacheResolver);
			result.append("' | condition='").append(this.condition).append('\'');
			return result;
		}

		/** 构建具体的 {@link CacheOperation} 实例。 */
		public abstract CacheOperation build();
	}

}
