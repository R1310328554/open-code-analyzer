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
 * 描述缓存「写入」（put）操作的类。
 *
 * @author Costin Leau
 * @author Phillip Webb
 * @author Marcin Kamionowski
 * @since 3.1
 */
public class CachePutOperation extends CacheOperation {

	/** 满足条件时不写入缓存的 SpEL 表达式（unless）。 */
	private final @Nullable String unless;


	/**
	 * 根据给定构建器创建新的 {@link CachePutOperation} 实例。
	 * @since 4.3
	 */
	public CachePutOperation(CachePutOperation.Builder b) {
		super(b);
		this.unless = b.unless;
	}


	/** 返回 unless 条件表达式。 */
	public @Nullable String getUnless() {
		return this.unless;
	}


	/**
	 * 用于创建 {@link CachePutOperation} 的构建器。
	 * @since 4.3
	 */
	public static class Builder extends CacheOperation.Builder {

		/** unless 条件表达式。 */
		private @Nullable String unless;

		/** 设置 unless 条件表达式。 */
		public void setUnless(String unless) {
			this.unless = unless;
		}

		@Override
		protected StringBuilder getOperationDescription() {
			StringBuilder sb = super.getOperationDescription();
			sb.append(" | unless='");
			sb.append(this.unless);
			sb.append('\'');
			return sb;
		}

		@Override
		public CachePutOperation build() {
			return new CachePutOperation(this);
		}
	}

}
