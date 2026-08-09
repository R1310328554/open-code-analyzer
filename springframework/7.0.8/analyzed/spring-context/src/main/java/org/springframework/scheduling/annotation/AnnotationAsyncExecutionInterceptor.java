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

import java.lang.reflect.Method;
import java.util.concurrent.Executor;

import org.jspecify.annotations.Nullable;

import org.springframework.aop.interceptor.AsyncExecutionInterceptor;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.core.annotation.AnnotatedElementUtils;

/**
 * {@link AsyncExecutionInterceptor} 的特化实现，根据 {@link Async} 注解
 * 将方法执行委托给 {@code Executor}。
 *
 * <p>专门支持 {@link Async#value()} 执行器限定符机制。
 *
 * <p>支持在方法或声明类级别通过 {@code @Async} 检测限定符元数据。
 * 详见 {@link #getExecutorQualifier(Method)}。
 *
 * @author Chris Beams
 * @author Stephane Nicoll
 * @since 3.1.2
 * @see org.springframework.scheduling.annotation.Async
 * @see org.springframework.scheduling.annotation.AsyncAnnotationAdvisor
 */
public class AnnotationAsyncExecutionInterceptor extends AsyncExecutionInterceptor {

	/**
	 * 使用给定执行器及简单 {@link AsyncUncaughtExceptionHandler} 创建新的
	 * {@code AnnotationAsyncExecutionInterceptor}。
	 * @param defaultExecutor 当方法级 {@link Async#value()} 未指定更具体执行器时使用的默认执行器；
	 * 否则将为本拦截器构建本地执行器
	 */
	public AnnotationAsyncExecutionInterceptor(@Nullable Executor defaultExecutor) {
		super(defaultExecutor);
	}

	/**
	 * 使用给定执行器创建新的 {@code AnnotationAsyncExecutionInterceptor}。
	 * @param defaultExecutor 当方法级 {@link Async#value()} 未指定更具体执行器时使用的默认执行器；
	 * 否则将为本拦截器构建本地执行器
	 * @param exceptionHandler 用于处理 {@code void} 返回类型异步方法执行
	 * 所抛出异常的 {@link AsyncUncaughtExceptionHandler}
	 */
	public AnnotationAsyncExecutionInterceptor(@Nullable Executor defaultExecutor, AsyncUncaughtExceptionHandler exceptionHandler) {
		super(defaultExecutor, exceptionHandler);
	}


	/**
	 * 返回执行给定方法时使用的执行器限定符或 Bean 名称，
	 * 由方法或声明类级别的 {@link Async#value} 指定。
	 * 若方法与类级别均标注 {@code @Async}，方法上的 {@code value} 优先
	 * （即使为空字符串，也表示优先使用默认执行器）。
	 * @param method 待检查执行器限定符元数据的方法
	 * @return 已指定时返回限定符，否则返回空字符串表示应使用
	 * {@linkplain #setExecutor(Executor) 默认执行器}
	 * @see #determineAsyncExecutor(Method)
	 */
	@Override
	protected @Nullable String getExecutorQualifier(Method method) {
		// Maintainer's note: changes made here should also be made in
		// AnnotationAsyncExecutionAspect#getExecutorQualifier
		Async async = AnnotatedElementUtils.findMergedAnnotation(method, Async.class);
		if (async == null) {
			async = AnnotatedElementUtils.findMergedAnnotation(method.getDeclaringClass(), Async.class);
		}
		return (async != null ? async.value() : null);
	}

}
