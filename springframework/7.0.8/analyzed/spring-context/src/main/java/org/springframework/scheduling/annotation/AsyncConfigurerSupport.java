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

package org.springframework.scheduling.annotation;

import java.util.concurrent.Executor;

import org.jspecify.annotations.Nullable;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;

/**
 * 便捷 {@link AsyncConfigurer}，实现全部方法以使用默认值。
 * 提供直接实现 {@link AsyncConfigurer} 的向后兼容替代方案。
 *
 * @author Stephane Nicoll
 * @since 4.1
 * @deprecated 自 6.0 起，请直接实现 {@link AsyncConfigurer}
 */
@Deprecated(since = "6.0")
public class AsyncConfigurerSupport implements AsyncConfigurer {

	@Override
	public @Nullable Executor getAsyncExecutor() {
		return null;
	}

	@Override
	public @Nullable AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
		return null;
	}

}
