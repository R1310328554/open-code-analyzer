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
 * 用于自定义处理异步方法调用时使用的 {@link Executor} 实例，
 * 或处理 {@code void} 返回类型异步方法所抛出异常的
 * {@link AsyncUncaughtExceptionHandler} 实例的接口。
 *
 * <p>通常由标注 @{@link EnableAsync} 的
 * @{@link org.springframework.context.annotation.Configuration Configuration} 类实现。
 * 用法示例见 @{@link EnableAsync} 的 javadoc。
 *
 * <p><b>注意：{@code AsyncConfigurer} 会较早初始化。</b>
 * 请勿直接向自动装配字段注入常见依赖；
 * 可考虑为这些依赖声明惰性 {@link org.springframework.beans.factory.ObjectProvider}。
 *
 * @author Chris Beams
 * @author Stephane Nicoll
 * @since 3.1
 * @see AbstractAsyncConfiguration
 * @see EnableAsync
 */
public interface AsyncConfigurer {

	/**
	 * 处理异步方法调用时使用的 {@link Executor} 实例。
	 */
	default @Nullable Executor getAsyncExecutor() {
		return null;
	}

	/**
	 * 在 {@code void} 返回类型的异步方法执行抛出异常时使用的
	 * {@link AsyncUncaughtExceptionHandler} 实例。
	 */
	default @Nullable AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
		return null;
	}

}
