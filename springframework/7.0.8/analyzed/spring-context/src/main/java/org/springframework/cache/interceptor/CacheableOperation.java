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

import org.jspecify.annotations.Nullable;

/**
 * 描述缓存「可读」（cacheable）操作的类。
 *
 * @author Costin Leau
 * @author Phillip Webb
 * @author Marcin Kamionowski
 * @since 3.1
 */
public class CacheableOperation extends CacheOperation {

	/** 满足条件时不使用缓存结果的 SpEL 表达式（unless）。 */
	private final @Nullable String unless;

	/** 是否以同步方式加载缓存（防止缓存击穿）。 */
	private final boolean sync;


	/**
	 * 根据给定构建器创建新的 {@link CacheableOperation} 实例。
	 * @since 4.3
	 */
	public CacheableOperation(CacheableOperation.Builder b) {
		super(b);
		this.unless = b.unless;
		this.sync = b.sync;
	}


	/** 返回 unless 条件表达式。 */
	public @Nullable String getUnless() {
		return this.unless;
	}

	/** 返回是否启用同步缓存加载。 */
	public boolean isSync() {
		return this.sync;
	}


	/**
	 * 用于创建 {@link CacheableOperation} 的构建器。
	 * @since 4.3
	 */
	public static class Builder extends CacheOperation.Builder {

		/** unless 条件表达式。 */
		private @Nullable String unless;

		/** 是否同步加载。 */
		private boolean sync;

		/** 设置 unless 条件表达式。 */
		public void setUnless(String unless) {
			this.unless = unless;
		}

		/** 设置是否以同步方式加载缓存。 */
		public void setSync(boolean sync) {
			this.sync = sync;
		}

		@Override
		protected StringBuilder getOperationDescription() {
			StringBuilder sb = super.getOperationDescription();
			sb.append(" | unless='");
			sb.append(this.unless);
			sb.append('\'');
			sb.append(" | sync='");
			sb.append(this.sync);
			sb.append('\'');
			return sb;
		}

		@Override
		public CacheableOperation build() {
			return new CacheableOperation(this);
		}
	}

}
