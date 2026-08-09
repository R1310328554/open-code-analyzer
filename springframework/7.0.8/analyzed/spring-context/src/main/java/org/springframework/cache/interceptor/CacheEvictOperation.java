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

/**
 * 描述缓存「驱逐」（evict）操作的类。
 *
 * @author Costin Leau
 * @author Marcin Kamionowski
 * @since 3.1
 */
public class CacheEvictOperation extends CacheOperation {

	/** 是否清空整个缓存（而非仅按 key 驱逐）。 */
	private final boolean cacheWide;

	/** 是否在方法调用之前执行驱逐。 */
	private final boolean beforeInvocation;


	/**
	 * 根据给定构建器创建新的 {@link CacheEvictOperation} 实例。
	 * @since 4.3
	 */
	public CacheEvictOperation(CacheEvictOperation.Builder b) {
		super(b);
		this.cacheWide = b.cacheWide;
		this.beforeInvocation = b.beforeInvocation;
	}


	/** 返回是否对整个缓存执行清空操作。 */
	public boolean isCacheWide() {
		return this.cacheWide;
	}

	/** 返回驱逐是否在方法调用之前执行。 */
	public boolean isBeforeInvocation() {
		return this.beforeInvocation;
	}


	/**
	 * 用于创建 {@link CacheEvictOperation} 的构建器。
	 * @since 4.3
	 */
	public static class Builder extends CacheOperation.Builder {

		/** 是否清空整个缓存。 */
		private boolean cacheWide = false;

		/** 是否在调用前驱逐。 */
		private boolean beforeInvocation = false;

		/** 设置是否清空整个缓存。 */
		public void setCacheWide(boolean cacheWide) {
			this.cacheWide = cacheWide;
		}

		/** 设置是否在方法调用前执行驱逐。 */
		public void setBeforeInvocation(boolean beforeInvocation) {
			this.beforeInvocation = beforeInvocation;
		}

		@Override
		protected StringBuilder getOperationDescription() {
			StringBuilder sb = super.getOperationDescription();
			sb.append(',');
			sb.append(this.cacheWide);
			sb.append(',');
			sb.append(this.beforeInvocation);
			return sb;
		}

		@Override
		public CacheEvictOperation build() {
			return new CacheEvictOperation(this);
		}
	}

}
