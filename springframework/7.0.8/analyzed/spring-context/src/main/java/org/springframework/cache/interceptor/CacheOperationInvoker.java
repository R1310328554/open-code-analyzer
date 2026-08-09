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
 * 抽象一次缓存操作所对应的方法调用。
 *
 * <p>不提供传播受检异常的途径，但提供专用的 {@link ThrowableWrapper}
 * 用于包装底层调用抛出的任何异常。调用方应专门处理该异常类型。
 *
 * @author Stephane Nicoll
 * @since 4.1
 */
@FunctionalInterface
public interface CacheOperationInvoker {

	/**
	 * 执行本实例所定义的缓存操作。将调用期间抛出的任何异常
	 * 包装为 {@link ThrowableWrapper}。
	 * @return 操作执行结果
	 * @throws ThrowableWrapper 调用操作过程中发生错误时抛出
	 */
	@Nullable Object invoke() throws ThrowableWrapper;


	/**
	 * 包装调用 {@link #invoke()} 期间抛出的任何异常。
	 */
	@SuppressWarnings("serial")
	class ThrowableWrapper extends RuntimeException {

		/** 原始异常。 */
		private final Throwable original;

		/** 使用原始异常构造包装器。 */
		public ThrowableWrapper(Throwable original) {
			super(original.getMessage(), original);
			this.original = original;
		}

		/** 返回被包装的原始异常。 */
		public Throwable getOriginal() {
			return this.original;
		}
	}

}
