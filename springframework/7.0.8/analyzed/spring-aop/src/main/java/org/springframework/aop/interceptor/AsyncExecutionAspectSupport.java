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
 * 异步方法执行方面的基类，例如 {@code
 * org.springframework.scheduling.annotation.AnnotationAsyncExecutionInterceptor} 或 {@code
 * org.springframework.scheduling.aspectj.AnnotationAsyncExecutionAspect}。
 * <p> 逐个方法地为 <i> 执行者资格认证 </i> 提供支持。 {@code AsyncExecutionAspectSupport} 对象必须使用默认的 {@code
 * Executor} 构造，但每个单独的方法可以进一步限定执行时要使用的特定 {@code Executor} bean，例如通过注释属性。
 * @author Chris Beams
 * @author Juergen Hoeller
 * @author Stephane Nicoll
 * @author He Bo
 * @author Sebastien Deleuze
 * @since 3.1.2
 */
public abstract class AsyncExecutionAspectSupport implements BeanFactoryAware {

	/**
	 * 要选取的 {@link TaskExecutor} bean 的默认名称：“taskExecutor”。 <p>注意，初始查找是按类型进行的；这只是在上下文中发现多个执行器 b
	 * ean 的情况下的后备措施。
	 * @since 4.2.6
	 */
	public static final String DEFAULT_TASK_EXECUTOR_BEAN_NAME = "taskExecutor";


	/**
	 * 获取 Log（`Log`）。
	 */
	protected final Log logger = LogFactory.getLog(getClass());

	/** 执行器相关状态（`defaultExecutor`）。 */
	private SingletonSupplier<Executor> defaultExecutor;

	/** 处理器相关状态（`exceptionHandler`）。 */
	private SingletonSupplier<AsyncUncaughtExceptionHandler> exceptionHandler;

	/** 底层 BeanFactory 引用。 */
	private @Nullable BeanFactory beanFactory;

	/** 值相关状态（`embeddedValueResolver`）。 */
	private @Nullable StringValueResolver embeddedValueResolver;

	private final Map<Method, AsyncTaskExecutor> executors = new ConcurrentHashMap<>(16);


	/**
	 * 使用默认 {@link AsyncUncaughtExceptionHandler} 创建一个新实例。
	 * @param defaultExecutor 要委托的 {@code Executor}（通常是 Spring {@code AsyncTaskExecutor} 或 {@link java.util.concurrent.ExecutorService}），除非通过异步方法上的限定符请求了更具体的执行器，在这种情况下，将在调用时针对封闭的 bean 工厂查找执行器
	 */
	public AsyncExecutionAspectSupport(@Nullable Executor defaultExecutor) {
		this.defaultExecutor = new SingletonSupplier<>(defaultExecutor, () -> getDefaultExecutor(this.beanFactory));
		this.exceptionHandler = SingletonSupplier.of(SimpleAsyncUncaughtExceptionHandler::new);
	}

	/**
	 * 使用给定的异常处理程序创建一个新的 {@link AsyncExecutionAspectSupport}。
	 * @param defaultExecutor 要委托的 {@code Executor}（通常是 Spring {@code AsyncTaskExecutor} 或 {@link java.util.concurrent.ExecutorService}），除非通过异步方法上的限定符请求了更具体的执行器，在这种情况下，将在调用时针对封闭的 bean 工厂查找执行器
	 * @param exceptionHandler 要使用的 {@link AsyncUncaughtExceptionHandler}
	 */
	public AsyncExecutionAspectSupport(@Nullable Executor defaultExecutor, AsyncUncaughtExceptionHandler exceptionHandler) {
		this.defaultExecutor = new SingletonSupplier<>(defaultExecutor, () -> getDefaultExecutor(this.beanFactory));
		this.exceptionHandler = SingletonSupplier.of(exceptionHandler);
	}


	/**
	 * 使用给定的执行程序和异常处理程序供应商配置此方面，如果供应商不可解析，则应用相应的默认值。
	 * @since 5.1
	 */
	public void configure(@Nullable Supplier<? extends @Nullable Executor> defaultExecutor,
			@Nullable Supplier<? extends @Nullable AsyncUncaughtExceptionHandler> exceptionHandler) {

		this.defaultExecutor = new SingletonSupplier<>(defaultExecutor, () -> getDefaultExecutor(this.beanFactory));
		this.exceptionHandler = new SingletonSupplier<>(exceptionHandler, SimpleAsyncUncaughtExceptionHandler::new);
	}

	/**
	 * 提供执行异步方法时要使用的执行器。
	 * @param defaultExecutor 要委托的 {@code Executor}（通常是 Spring {@code AsyncTaskExecutor} 或 {@link java.util.concurrent.ExecutorService}），除非通过异步方法上的限定符请求了更具体的执行器，在这种情况下，将在调用时针对封闭的 bean 工厂查找执行器
	 * @see #getExecutorQualifier(Method)
	 * @see #setBeanFactory(BeanFactory)
	 * @see #getDefaultExecutor(BeanFactory)
	 */
	public void setExecutor(Executor defaultExecutor) {
		this.defaultExecutor = SingletonSupplier.of(defaultExecutor);
	}

	/**
	 * 提供 {@link AsyncUncaughtExceptionHandler} 以用于处理通过调用具有 {@code void} 返回类型的异步方法引发的异常。
	 */
	public void setExceptionHandler(AsyncUncaughtExceptionHandler exceptionHandler) {
		this.exceptionHandler = SingletonSupplier.of(exceptionHandler);
	}

	/**
	 * 设置在通过限定符查找执行程序或依赖默认执行程序查找算法时使用的 {@link BeanFactory}。
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
	 * 确定执行给定方法时要使用的特定执行器。
	 * @return 要使用的执行器（或 {@code null}，但前提是没有可用的默认执行器）
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
	 * 返回执行给定异步方法时要使用的执行器的限定符或 bean 名称，通常以注释属性的形式指定。 <p>返回空字符串或{@code null}表示未指定特定执行器，应使用{@link
	 * plain #setExecutor(Executor) default executor}。
	 * @param method 检查执行器限定符元数据的方法
	 * @return 限定符（如果指定），否则为空字符串或 {@code null}
	 * @see #determineAsyncExecutor(Method)
	 * @see #findQualifiedExecutor(BeanFactory, String)
	 */
	protected abstract @Nullable String getExecutorQualifier(Method method);

	/**
	 * 检索给定限定符的目标执行器。
	 * @param qualifier 要解决的限定符
	 * @return 目标执行器，或 {@code null}（如果没有可用的）
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
	 * 检索或构建此建议实例的默认执行器。 <p>从这里返回的执行器将被缓存以供进一步使用。 <p> 默认实现在上下文中搜索唯一的 {@link TaskExecutor} bean，
	 * 或者搜索名为“taskExecutor”的 {@link Executor} bean。如果两者都无法解析，则此实现将返回 {@code null}。
	 * @param beanFactory 用于默认执行器查找的 BeanFactory
	 * @return 默认执行器，如果没有可用则为 {@code null}
	 * @since 4.2.6
	 * @see #findQualifiedExecutor(BeanFactory, String)
	 * @see #DEFAULT_TASK_EXECUTOR_BEAN_NAME
	 */
	protected @Nullable Executor getDefaultExecutor(@Nullable BeanFactory beanFactory) {
		if (beanFactory != null) {
			try {
				// 搜索 TaskExecutor bean...不是普通的 Executor，因为那样会
				// 也与 ScheduledExecutorService 匹配，但它无法用于
				// 我们在这里的目的。 TaskExecutor 更明确地是为此设计的。
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
				// 放弃 -> 要么使用本地默认执行器，要么根本不使用......
			}
		}
		return null;
	}


	/**
	 * 使用所选执行器实际执行给定任务的委托。
	 * @param task 要执行的任务
	 * @param executor 选定的执行人
	 * @param returnType 声明的返回类型（可能是 {@link Future} 变体）
	 * @return 执行结果（可能是相应的 {@link Future} 句柄）
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
	 * 处理异步调用指定 {@link Method} 时引发的致命错误。 <p>如果方法的返回类型是{@link Future}对象，则只需将其抛出到更高级别即可传播原始异常。但是，
	 * 对于所有其他情况，异常将不会传输回客户端。在后一种情况下，当前的 {@link AsyncUncaughtExceptionHandler} 将用于管理此类异常。
	 * @param ex 要处理的异常
	 * @param method 被调用的方法
	 * @param params 用于调用该方法的参数
	 */
	protected void handleError(Throwable ex, Method method, @Nullable Object... params) throws Exception {
		if (Future.class.isAssignableFrom(method.getReturnType())) {
			ReflectionUtils.rethrowException(ex);
		}
		else {
			// 无法使用默认执行程序将异常传输给调用者
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
