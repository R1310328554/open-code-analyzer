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

package org.springframework.aop.interceptor;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.jspecify.annotations.Nullable;

import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.core.Ordered;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

/**
 * AOP 联盟 {@code MethodInterceptor}，使用给定的 {@link
 * org.springframework.core.task.AsyncTaskExecutor} 异步处理方法调用。通常与 {@link
 * org.springframework.scheduling.annotation.Async} 注释一起使用。
 * <p>在目标方法签名方面，支持任何参数类型。但是，返回类型仅限于 {@code void} 或 {@code java.util.concurrent.Future}。在后一种
 * 情况下，从代理返回的 Future 句柄将是一个实际的异步 Future，可用于跟踪异步方法执行的结果。然而，由于目标方法需要实现相同的签名，因此它必须返回一个临时的 Futu
 * re 句柄，该句柄仅传递返回值（如 Spring 的 {@link org.springframework.scheduling.annotation.AsyncResult}
 *  或 EJB 的 {@code jakarta.ejb.AsyncResult}）。
 * <p>当返回类型为{@code java.util.concurrent.Future}时，执行过程中抛出的任何异常都可以被调用者访问和管理。然而，对于 {@code
 * void} 返回类型，此类异常无法传回。在这种情况下，可以注册 {@link AsyncUncaughtExceptionHandler} 来处理此类异常。
 * <p>注意：{@code AnnotationAsyncExecutionInterceptor} 子类是首选，因为它支持与 Spring 的 {@code @Async}
 * 注释结合使用的执行器资格。
 * @author Juergen Hoeller
 * @author Chris Beams
 * @author Stephane Nicoll
 * @since 3.0
 * @see org.springframework.scheduling.annotation.Async
 * @see org.springframework.scheduling.annotation.AsyncAnnotationAdvisor
 * @see org.springframework.scheduling.annotation.AnnotationAsyncExecutionInterceptor
 */
public class AsyncExecutionInterceptor extends AsyncExecutionAspectSupport implements MethodInterceptor, Ordered {

	/**
	 * 使用默认 {@link AsyncUncaughtExceptionHandler} 创建一个新实例。
	 * @param defaultExecutor 要委托给的 {@link Executor}（通常是 Spring {@link AsyncTaskExecutor} 或 {@link java.util.concurrent.ExecutorService}）；否则将构建该拦截器的本地执行器
	 */
	public AsyncExecutionInterceptor(@Nullable Executor defaultExecutor) {
		super(defaultExecutor);
	}

	/**
	 * 创建一个新的 {@code AsyncExecutionInterceptor}。
	 * @param defaultExecutor 要委托给的 {@link Executor}（通常是 Spring {@link AsyncTaskExecutor} 或 {@link java.util.concurrent.ExecutorService}）；否则将构建该拦截器的本地执行器
	 * @param exceptionHandler 要使用的 {@link AsyncUncaughtExceptionHandler}
	 */
	public AsyncExecutionInterceptor(@Nullable Executor defaultExecutor, AsyncUncaughtExceptionHandler exceptionHandler) {
		super(defaultExecutor, exceptionHandler);
	}


	/**
	 * 拦截给定的方法调用，将方法的实际调用提交给正确的任务执行器并立即返回给调用者。
	 * @param invocation 拦截并异步的方法
	 * @return Future} 如果原始方法返回{@code Future}；否则为 {@code null}。
	 */
	@Override
	public @Nullable Object invoke(final MethodInvocation invocation) throws Throwable {
		Class<?> targetClass = (invocation.getThis() != null ? AopUtils.getTargetClass(invocation.getThis()) : null);
		final Method userMethod = BridgeMethodResolver.getMostSpecificMethod(invocation.getMethod(), targetClass);

		AsyncTaskExecutor executor = determineAsyncExecutor(userMethod);
		if (executor == null) {
			throw new IllegalStateException(
					"No executor specified and no default executor set on AsyncExecutionInterceptor either");
		}

		Callable<Object> task = () -> {
			try {
				Object result = invocation.proceed();
				if (result instanceof Future<?> future) {
					return future.get();
				}
			}
			catch (ExecutionException ex) {
				Throwable cause = ex.getCause();
				handleError(cause == null ? ex : cause, userMethod, invocation.getArguments());
			}
			catch (Throwable ex) {
				handleError(ex, userMethod, invocation.getArguments());
			}
			return null;
		};

		return doSubmit(task, executor, userMethod.getReturnType());
	}

	/**
	 * 获取执行给定方法时要使用的特定执行器的限定符。 <p> 此方法的默认实现实际上是无操作。 <p>子类可以重写此方法以提供对提取限定符信息的支持——例如，通过给定方法的注释。
	 * @return {@code null}
	 * @since 3.1.2
	 * @see #determineAsyncExecutor(Method)
	 */
	@Override
	protected @Nullable String getExecutorQualifier(Method method) {
		return null;
	}

	/**
	 * 此实现在上下文中搜索唯一的 {@link org.springframework.core.task.TaskExecutor}
	 * bean，或者搜索名为“taskExecutor”的 {@link Executor} bean。如果两者都无法解析（例如，如果根本没有配置 {@code
	 * BeanFactory}），则如果找不到默认值，则此实现将回退到新创建的 {@link SimpleAsyncTaskExecutor} 实例以供本地使用。
	 * @see #DEFAULT_TASK_EXECUTOR_BEAN_NAME
	 */
	@Override
	protected @Nullable Executor getDefaultExecutor(@Nullable BeanFactory beanFactory) {
		Executor defaultExecutor = super.getDefaultExecutor(beanFactory);
		return (defaultExecutor != null ? defaultExecutor : new SimpleAsyncTaskExecutor());
	}

	/**
	 * 获取 Order（`Order`）。
	 */
	@Override
	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE;
	}

}
