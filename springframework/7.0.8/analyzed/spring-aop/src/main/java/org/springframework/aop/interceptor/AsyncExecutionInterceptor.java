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
 * AOP Alliance {@code MethodInterceptor}，使用给定
 * {@link org.springframework.core.task.AsyncTaskExecutor} 异步处理方法调用。
 * 通常与 {@link org.springframework.scheduling.annotation.Async} 注解配合使用。
 *
 * <p>就目标方法签名而言，支持任意参数类型。
 * 但返回类型限制为 {@code void} 或 {@code java.util.concurrent.Future}。
 * 后者情况下，代理返回的 Future 句柄是可用于跟踪异步方法执行结果的真实异步 Future。
 * 但由于目标方法需实现相同签名，它必须返回仅传递返回值的临时 Future 句柄
 * （如 Spring 的 {@link org.springframework.scheduling.annotation.AsyncResult}
 * 或 EJB 的 {@code jakarta.ejb.AsyncResult}）。
 *
 * <p>当返回类型为 {@code java.util.concurrent.Future} 时，
 * 执行期间抛出的任何异常可由调用者访问和管理。
 * 但 {@code void} 返回类型时此类异常无法传回。
 * 此时可注册 {@link AsyncUncaughtExceptionHandler} 处理此类异常。
 *
 * <p>注意：{@code AnnotationAsyncExecutionInterceptor} 子类更优，
 * 因其支持与 Spring {@code @Async} 注解配合的执行器限定。
 *
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
	 * 使用默认 {@link AsyncUncaughtExceptionHandler} 创建新实例。
	 * @param defaultExecutor 要委托的 {@link Executor}（通常是 Spring {@link AsyncTaskExecutor}
	 * 或 {@link java.util.concurrent.ExecutorService}）；否则将为本拦截器构建本地执行器
	 */
	public AsyncExecutionInterceptor(@Nullable Executor defaultExecutor) {
		super(defaultExecutor);
	}

	/**
	 * 创建新的 {@code AsyncExecutionInterceptor}。
	 * @param defaultExecutor 要委托的 {@link Executor}（通常是 Spring {@link AsyncTaskExecutor}
	 * 或 {@link java.util.concurrent.ExecutorService}）；否则将为本拦截器构建本地执行器
	 * @param exceptionHandler 要使用的 {@link AsyncUncaughtExceptionHandler}
	 */
	public AsyncExecutionInterceptor(@Nullable Executor defaultExecutor, AsyncUncaughtExceptionHandler exceptionHandler) {
		super(defaultExecutor, exceptionHandler);
	}


	/**
	 * 拦截给定方法调用，将方法的实际调用提交给正确的任务执行器并立即返回调用者。
	 * @param invocation 要拦截并异步化的方法
	 * @return 若原方法返回 {@code Future} 则为 {@link Future}；否则 {@code null}
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
	 * 获取执行给定方法时要使用的特定执行器的限定符。
	 * <p>本方法的默认实现实际上为空操作。
	 * <p>子类可覆盖以支持提取限定符信息——
	 * 例如通过给定方法上的注解。
	 * @return 始终 {@code null}
	 * @since 3.1.2
	 * @see #determineAsyncExecutor(Method)
	 */
	@Override
	protected @Nullable String getExecutorQualifier(Method method) {
		return null;
	}

	/**
	 * 本实现搜索上下文中唯一的 {@link org.springframework.core.task.TaskExecutor} Bean，
	 * 否则查找名为 "taskExecutor" 的 {@link Executor} Bean。
	 * 若两者均不可解析（例如完全未配置 {@code BeanFactory}），
	 * 且找不到默认执行器，则回退到新创建的 {@link SimpleAsyncTaskExecutor} 实例供本地使用。
	 * @see #DEFAULT_TASK_EXECUTOR_BEAN_NAME
	 */
	@Override
	protected @Nullable Executor getDefaultExecutor(@Nullable BeanFactory beanFactory) {
		Executor defaultExecutor = super.getDefaultExecutor(beanFactory);
		return (defaultExecutor != null ? defaultExecutor : new SimpleAsyncTaskExecutor());
	}

	@Override
	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE;
	}

}
