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

import java.lang.annotation.Annotation;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;

import org.springframework.aop.framework.autoproxy.AbstractBeanFactoryAwareAdvisingPostProcessor;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.task.TaskExecutor;
import org.springframework.util.Assert;
import org.springframework.util.function.SingletonSupplier;

/**
 * Bean 后处理器，为类或方法级携带 {@link Async} 注解的 Bean
 * 自动应用异步调用行为，向暴露的代理（现有 AOP 代理或新生成、
 * 实现目标全部接口的代理）添加对应的 {@link AsyncAnnotationAdvisor}。
 *
 * <p>可提供负责异步执行的 {@link TaskExecutor}，
 * 以及指示方法应异步调用的注解类型。
 * 若未指定注解类型，本后处理器将检测 Spring {@link Async @Async} 注解
 * 及 EJB 3.1 {@code jakarta.ejb.Asynchronous} 注解。
 *
 * <p>对于 {@code void} 返回类型的方法，异步调用期间抛出的异常
 * 无法被调用方访问。可指定 {@link AsyncUncaughtExceptionHandler} 处理此类情况。
 *
 * <p>注意：底层异步 Advisor 默认在现有 Advisor 之前应用，
 * 以便在调用链中尽早切换到异步执行。
 *
 * @author Mark Fisher
 * @author Juergen Hoeller
 * @author Stephane Nicoll
 * @since 3.0
 * @see Async
 * @see AsyncAnnotationAdvisor
 * @see #setBeforeExistingAdvisors
 * @see ScheduledAnnotationBeanPostProcessor
 */
@SuppressWarnings("serial")
public class AsyncAnnotationBeanPostProcessor extends AbstractBeanFactoryAwareAdvisingPostProcessor {

	/**
	 * 要选取的 {@link TaskExecutor} Bean 默认名称："taskExecutor"。
	 * <p>初始查找按类型进行；此名称仅作为上下文中存在多个执行器 Bean 时的回退。
	 * @since 4.2
	 * @see AnnotationAsyncExecutionInterceptor#DEFAULT_TASK_EXECUTOR_BEAN_NAME
	 */
	public static final String DEFAULT_TASK_EXECUTOR_BEAN_NAME =
			AnnotationAsyncExecutionInterceptor.DEFAULT_TASK_EXECUTOR_BEAN_NAME;


	protected final Log logger = LogFactory.getLog(getClass());

	private @Nullable Supplier<? extends @Nullable Executor> executor;

	private @Nullable Supplier<? extends @Nullable AsyncUncaughtExceptionHandler> exceptionHandler;

	private @Nullable Class<? extends Annotation> asyncAnnotationType;


	public AsyncAnnotationBeanPostProcessor() {
		setBeforeExistingAdvisors(true);
	}


	/**
	 * 使用给定执行器与异常处理器 Supplier 配置本后处理器，
	 * 若 Supplier 不可解析则应用对应默认值。
	 * @since 5.1
	 */
	public void configure(@Nullable Supplier<? extends @Nullable Executor> executor,
			@Nullable Supplier<? extends @Nullable AsyncUncaughtExceptionHandler> exceptionHandler) {

		this.executor = executor;
		this.exceptionHandler = exceptionHandler;
	}

	/**
	 * 设置异步调用方法时使用的 {@link Executor}。
	 * <p>若未指定，将应用默认执行器解析：在上下文中查找唯一 {@link TaskExecutor} Bean，
	 * 否则查找名为 "taskExecutor" 的 {@link Executor} Bean。
	 * 若两者均不可解析，将在拦截器内创建本地默认执行器。
	 * @see AnnotationAsyncExecutionInterceptor#getDefaultExecutor(BeanFactory)
	 * @see #DEFAULT_TASK_EXECUTOR_BEAN_NAME
	 */
	public void setExecutor(Executor executor) {
		this.executor = SingletonSupplier.of(executor);
	}

	/**
	 * 设置用于处理异步方法执行未捕获异常的
	 * {@link AsyncUncaughtExceptionHandler}。
	 * @since 4.1
	 */
	public void setExceptionHandler(AsyncUncaughtExceptionHandler exceptionHandler) {
		this.exceptionHandler = SingletonSupplier.of(exceptionHandler);
	}

	/**
	 * 设置在类或方法级别检测的“异步”注解类型。
	 * 默认检测 {@link Async} 注解及 EJB 3.1 {@code jakarta.ejb.Asynchronous} 注解。
	 * <p>此 setter 供开发者提供自定义（非 Spring 专有）注解类型，
	 * 以指示方法（或给定类的全部方法）应异步调用。
	 * @param asyncAnnotationType 所需的注解类型
	 */
	public void setAsyncAnnotationType(Class<? extends Annotation> asyncAnnotationType) {
		Assert.notNull(asyncAnnotationType, "'asyncAnnotationType' must not be null");
		this.asyncAnnotationType = asyncAnnotationType;
	}


	@Override
	public void setBeanFactory(BeanFactory beanFactory) {
		super.setBeanFactory(beanFactory);

		AsyncAnnotationAdvisor advisor = new AsyncAnnotationAdvisor(this.executor, this.exceptionHandler);
		if (this.asyncAnnotationType != null) {
			advisor.setAsyncAnnotationType(this.asyncAnnotationType);
		}
		advisor.setBeanFactory(beanFactory);
		this.advisor = advisor;
	}

}
