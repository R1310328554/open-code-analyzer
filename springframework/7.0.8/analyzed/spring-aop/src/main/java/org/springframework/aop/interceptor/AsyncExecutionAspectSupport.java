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
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.function.Supplier;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.beans.factory.annotation.BeanFactoryAnnotationUtils;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.EmbeddedValueResolver;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.support.TaskExecutorAdapter;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.util.StringValueResolver;
import org.springframework.util.function.SingletonSupplier;

/**
 * 异步方法执行切面的基类，例如
 * {@code org.springframework.scheduling.annotation.AnnotationAsyncExecutionInterceptor}
 * 或 {@code org.springframework.scheduling.aspectj.AnnotationAsyncExecutionAspect}。
 *
 * <p>支持按方法逐方法进行<i>执行器限定</i>。
 * {@code AsyncExecutionAspectSupport} 对象必须以默认 {@code Executor} 构造，
 * 但每个方法可进一步限定执行时使用的特定 {@code Executor} Bean，
 * 例如通过注解属性。
 *
 * @author Chris Beams
 * @author Juergen Hoeller
 * @author Stephane Nicoll
 * @author He Bo
 * @author Sebastien Deleuze
 * @since 3.1.2
 */
public abstract class AsyncExecutionAspectSupport implements BeanFactoryAware {

	/**
	 * 要选取的 {@link TaskExecutor} Bean 的默认名称："taskExecutor"。
	 * <p>注意初始查找按类型进行；这只是在上下文中找到多个执行器 Bean 时的回退。
	 * @since 4.2.6
	 */
	public static final String DEFAULT_TASK_EXECUTOR_BEAN_NAME = "taskExecutor";


	protected final Log logger = LogFactory.getLog(getClass());

	private SingletonSupplier<Executor> defaultExecutor;

	private SingletonSupplier<AsyncUncaughtExceptionHandler> exceptionHandler;

	private @Nullable BeanFactory beanFactory;

	private @Nullable StringValueResolver embeddedValueResolver;

	private final Map<Method, AsyncTaskExecutor> executors = new ConcurrentHashMap<>(16);


	/**
	 * 使用默认 {@link AsyncUncaughtExceptionHandler} 创建新实例。
	 * @param defaultExecutor 要委托的 {@code Executor}（通常是 Spring {@code AsyncTaskExecutor}
	 * 或 {@link java.util.concurrent.ExecutorService}），除非异步方法上的限定符
	 * 请求了更具体的执行器，此时将在调用时从 enclosing BeanFactory 查找
	 */
	public AsyncExecutionAspectSupport(@Nullable Executor defaultExecutor) {
		this.defaultExecutor = new SingletonSupplier<>(defaultExecutor, () -> getDefaultExecutor(this.beanFactory));
		this.exceptionHandler = SingletonSupplier.of(SimpleAsyncUncaughtExceptionHandler::new);
	}

	/**
	 * 使用给定异常处理器创建新的 {@link AsyncExecutionAspectSupport}。
	 * @param defaultExecutor 要委托的 {@code Executor}（通常是 Spring {@code AsyncTaskExecutor}
	 * 或 {@link java.util.concurrent.ExecutorService}），除非异步方法上的限定符
	 * 请求了更具体的执行器，此时将在调用时从 enclosing BeanFactory 查找
	 * @param exceptionHandler 要使用的 {@link AsyncUncaughtExceptionHandler}
	 */
	public AsyncExecutionAspectSupport(@Nullable Executor defaultExecutor, AsyncUncaughtExceptionHandler exceptionHandler) {
		this.defaultExecutor = new SingletonSupplier<>(defaultExecutor, () -> getDefaultExecutor(this.beanFactory));
		this.exceptionHandler = SingletonSupplier.of(exceptionHandler);
	}


	/**
	 * 使用给定执行器和异常处理器 Supplier 配置本切面，
	 * 若 Supplier 不可解析则应用对应默认值。
	 * @since 5.1
	 */
	public void configure(@Nullable Supplier<? extends @Nullable Executor> defaultExecutor,
			@Nullable Supplier<? extends @Nullable AsyncUncaughtExceptionHandler> exceptionHandler) {

		this.defaultExecutor = new SingletonSupplier<>(defaultExecutor, () -> getDefaultExecutor(this.beanFactory));
		this.exceptionHandler = new SingletonSupplier<>(exceptionHandler, SimpleAsyncUncaughtExceptionHandler::new);
	}

	/**
	 * 提供执行异步方法时使用的执行器。
	 * @param defaultExecutor 要委托的 {@code Executor}（通常是 Spring {@code AsyncTaskExecutor}
	 * 或 {@link java.util.concurrent.ExecutorService}），除非异步方法上的限定符
	 * 请求了更具体的执行器，此时将在调用时从 enclosing BeanFactory 查找
	 * @see #getExecutorQualifier(Method)
	 * @see #setBeanFactory(BeanFactory)
	 * @see #getDefaultExecutor(BeanFactory)
	 */
	public void setExecutor(Executor defaultExecutor) {
		this.defaultExecutor = SingletonSupplier.of(defaultExecutor);
	}

	/**
	 * 提供用于处理调用 {@code void} 返回类型异步方法时
	 * 抛出异常的 {@link AsyncUncaughtExceptionHandler}。
	 */
	public void setExceptionHandler(AsyncUncaughtExceptionHandler exceptionHandler) {
		this.exceptionHandler = SingletonSupplier.of(exceptionHandler);
	}

	/**
	 * 设置按限定符查找执行器或依赖默认执行器查找算法时
	 * 使用的 {@link BeanFactory}。
	 * @see #findQualifiedExecutor(BeanFactory, String)
	 * @see #getDefaultExecutor(BeanFactory)
	 */
	@Override
	public void setBeanFactory(BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
		if (beanFactory instanceof ConfigurableBeanFactory configurableBeanFactory) {
			this.embeddedValueResolver = new EmbeddedValueResolver(configurableBeanFactory);
		}
		this.executors.clear();
	}


	/**
	 * 确定执行给定方法时使用的特定执行器。
	 * @return 要使用的执行器（或 {@code null}，但仅在没有默认执行器时）
	 */
	protected @Nullable AsyncTaskExecutor determineAsyncExecutor(Method method) {
		AsyncTaskExecutor executor = this.executors.get(method);
		if (executor == null) {
			Executor targetExecutor;
			String qualifier = getExecutorQualifier(method);
			if (this.embeddedValueResolver != null && StringUtils.hasLength(qualifier)) {
				qualifier = this.embeddedValueResolver.resolveStringValue(qualifier);
			}
			if (StringUtils.hasLength(qualifier)) {
				targetExecutor = findQualifiedExecutor(this.beanFactory, qualifier);
			}
			else {
				targetExecutor = this.defaultExecutor.get();
			}
			if (targetExecutor == null) {
				return null;
			}
			executor = (targetExecutor instanceof AsyncTaskExecutor asyncTaskExecutor ?
					asyncTaskExecutor : new TaskExecutorAdapter(targetExecutor));
			this.executors.put(method, executor);
		}
		return executor;
	}

	/**
	 * 返回执行给定异步方法时要使用的执行器限定符或 Bean 名称，
	 * 通常以注解属性形式指定。
	 * <p>返回空字符串或 {@code null} 表示未指定特定执行器，
	 * 应使用 {@linkplain #setExecutor(Executor) 默认执行器}。
	 * @param method 要检查执行器限定符元数据的方法
	 * @return 若指定则返回限定符，否则空 String 或 {@code null}
	 * @see #determineAsyncExecutor(Method)
	 * @see #findQualifiedExecutor(BeanFactory, String)
	 */
	protected abstract @Nullable String getExecutorQualifier(Method method);

	/**
	 * 检索给定限定符的目标执行器。
	 * @param qualifier 要解析的限定符
	 * @return 目标执行器，若无则 {@code null}
	 * @since 4.2.6
	 * @see #getExecutorQualifier(Method)
	 */
	protected @Nullable Executor findQualifiedExecutor(@Nullable BeanFactory beanFactory, String qualifier) {
		if (beanFactory == null) {
			throw new IllegalStateException("BeanFactory must be set on " + getClass().getSimpleName() +
					" to access qualified executor '" + qualifier + "'");
		}
		return BeanFactoryAnnotationUtils.qualifiedBeanOfType(beanFactory, Executor.class, qualifier);
	}

	/**
	 * 检索或构建本 advice 实例的默认执行器。
	 * <p>从此处返回的执行器将被缓存以供后续使用。
	 * <p>默认实现搜索上下文中唯一的 {@link TaskExecutor} Bean，
	 * 否则查找名为 "taskExecutor" 的 {@link Executor} Bean。
	 * 若两者均不可解析，本实现返回 {@code null}。
	 * @param beanFactory 用于默认执行器查找的 BeanFactory
	 * @return 默认执行器，若无则 {@code null}
	 * @since 4.2.6
	 * @see #findQualifiedExecutor(BeanFactory, String)
	 * @see #DEFAULT_TASK_EXECUTOR_BEAN_NAME
	 */
	protected @Nullable Executor getDefaultExecutor(@Nullable BeanFactory beanFactory) {
		if (beanFactory != null) {
			try {
				// 搜索 TaskExecutor Bean... 而非普通 Executor，
				// 因为后者也会匹配 ScheduledExecutorService，
				// 对我们此处用途不可用。TaskExecutor 设计更明确。
				return beanFactory.getBean(TaskExecutor.class);
			}
			catch (NoUniqueBeanDefinitionException ex) {
				logger.debug("Could not find unique TaskExecutor bean. " +
						"Continuing search for an Executor bean named 'taskExecutor'", ex);
				try {
					return beanFactory.getBean(DEFAULT_TASK_EXECUTOR_BEAN_NAME, Executor.class);
				}
				catch (NoSuchBeanDefinitionException ex2) {
					if (logger.isInfoEnabled()) {
						logger.info("More than one TaskExecutor bean found within the context, and none is named " +
								"'taskExecutor'. Mark one of them as primary or name it 'taskExecutor' (possibly " +
								"as an alias) in order to use it for async processing: " + ex.getBeanNamesFound());
					}
				}
			}
			catch (NoSuchBeanDefinitionException ex) {
				logger.debug("Could not find default TaskExecutor bean. " +
						"Continuing search for an Executor bean named 'taskExecutor'", ex);
				try {
					return beanFactory.getBean(DEFAULT_TASK_EXECUTOR_BEAN_NAME, Executor.class);
				}
				catch (NoSuchBeanDefinitionException ex2) {
					logger.info("No task executor bean found for async processing: " +
							"no bean of type TaskExecutor and no bean named 'taskExecutor' either");
				}
				// 放弃 -> 要么使用本地默认执行器，要么完全没有...
			}
		}
		return null;
	}


	/**
	 * 实际使用所选执行器执行给定任务的委托。
	 * @param task 要执行的任务
	 * @param executor 所选执行器
	 * @param returnType 声明的返回类型（可能是 {@link Future} 变体）
	 * @return 执行结果（可能是对应的 {@link Future} 句柄）
	 */
	protected @Nullable Object doSubmit(Callable<Object> task, AsyncTaskExecutor executor, Class<?> returnType) {
		if (CompletableFuture.class.isAssignableFrom(returnType)) {
			return executor.submitCompletable(task);
		}
		else if (Future.class.isAssignableFrom(returnType)) {
			return executor.submit(task);
		}
		else if (void.class == returnType || "kotlin.Unit".equals(returnType.getName())) {
			executor.submit(task);
			return null;
		}
		else {
			throw new IllegalArgumentException(
					"Invalid return type for async method (only Future and void supported): " + returnType);
		}
	}

	/**
	 * 处理异步调用指定 {@link Method} 时抛出的致命错误。
	 * <p>若方法返回类型为 {@link Future} 对象，
	 * 可通过在更高层直接抛出原始异常来传播。
	 * 但在其他情况下，异常不会传回客户端。
	 * 后一种情况下，当前 {@link AsyncUncaughtExceptionHandler} 将用于处理此类异常。
	 * @param ex 要处理的异常
	 * @param method 被调用的方法
	 * @param params 用于调用方法的参数
	 */
	protected void handleError(Throwable ex, Method method, @Nullable Object... params) throws Exception {
		if (Future.class.isAssignableFrom(method.getReturnType())) {
			ReflectionUtils.rethrowException(ex);
		}
		else {
			// 无法使用默认执行器将异常传递给调用者
			try {
				this.exceptionHandler.obtain().handleUncaughtException(ex, method, params);
			}
			catch (Throwable ex2) {
				logger.warn("Exception handler for async method '" + method.toGenericString() +
						"' threw unexpected exception itself", ex2);
			}
		}
	}

}
