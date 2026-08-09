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
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

import org.aopalliance.aop.Advice;
import org.jspecify.annotations.Nullable;

import org.springframework.aop.Pointcut;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.aop.support.AbstractPointcutAdvisor;
import org.springframework.aop.support.ComposablePointcut;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.function.SingletonSupplier;

/**
 * 通过 {@link Async} 注解激活异步方法执行的 Advisor。
 * 该注解可用于实现类及服务接口的方法级和类型级。
 *
 * <p>本 Advisor 也会检测 EJB 3.1 {@code jakarta.ejb.Asynchronous} 注解，
 * 与 Spring 自有 {@code Async} 同等处理。
 * 此外可通过 {@link #setAsyncAnnotationType "asyncAnnotationType"} 属性
 * 指定自定义异步注解类型。
 *
 * @author Juergen Hoeller
 * @since 3.0
 * @see Async
 * @see AnnotationAsyncExecutionInterceptor
 */
@SuppressWarnings("serial")
public class AsyncAnnotationAdvisor extends AbstractPointcutAdvisor implements BeanFactoryAware {

	private final Advice advice;

	private Pointcut pointcut;


	/**
	 * 为 Bean 风格配置创建新的 {@code AsyncAnnotationAdvisor}。
	 */
	public AsyncAnnotationAdvisor() {
		this((Supplier<? extends @Nullable Executor>) null, (Supplier<? extends @Nullable AsyncUncaughtExceptionHandler>) null);
	}

	/**
	 * 为给定任务执行器创建新的 {@code AsyncAnnotationAdvisor}。
	 * @param executor 异步方法使用的任务执行器
	 * （可为 {@code null} 以触发默认执行器解析）
	 * @param exceptionHandler 处理异步方法执行意外异常的
	 * {@link AsyncUncaughtExceptionHandler}
	 * @see AnnotationAsyncExecutionInterceptor#getDefaultExecutor(BeanFactory)
	 */
	public AsyncAnnotationAdvisor(
			@Nullable Executor executor, @Nullable AsyncUncaughtExceptionHandler exceptionHandler) {

		this(SingletonSupplier.ofNullable(executor), SingletonSupplier.ofNullable(exceptionHandler));
	}

	/**
	 * 为给定任务执行器创建新的 {@code AsyncAnnotationAdvisor}。
	 * @param executor 异步方法使用的任务执行器
	 * （可为 {@code null} 以触发默认执行器解析）
	 * @param exceptionHandler 处理异步方法执行意外异常的
	 * {@link AsyncUncaughtExceptionHandler}
	 * @since 5.1
	 * @see AnnotationAsyncExecutionInterceptor#getDefaultExecutor(BeanFactory)
	 */
	@SuppressWarnings("unchecked")
	public AsyncAnnotationAdvisor(
			@Nullable Supplier<? extends @Nullable Executor> executor, @Nullable Supplier<? extends @Nullable AsyncUncaughtExceptionHandler> exceptionHandler) {

		Set<Class<? extends Annotation>> asyncAnnotationTypes = CollectionUtils.newLinkedHashSet(2);
		asyncAnnotationTypes.add(Async.class);

		ClassLoader classLoader = AsyncAnnotationAdvisor.class.getClassLoader();
		try {
			asyncAnnotationTypes.add((Class<? extends Annotation>)
					ClassUtils.forName("jakarta.ejb.Asynchronous", classLoader));
		}
		catch (ClassNotFoundException ex) {
			// If EJB API not present, simply ignore.
		}
		try {
			asyncAnnotationTypes.add((Class<? extends Annotation>)
					ClassUtils.forName("jakarta.enterprise.concurrent.Asynchronous", classLoader));
		}
		catch (ClassNotFoundException ex) {
			// If Jakarta Concurrent API not present, simply ignore.
		}

		this.advice = buildAdvice(executor, exceptionHandler);
		this.pointcut = buildPointcut(asyncAnnotationTypes);
	}


	/**
	 * 设置“异步”注解类型。
	 * <p>默认异步注解类型为 {@link Async} 注解，
	 * 以及 EJB 3.1 {@code jakarta.ejb.Asynchronous} 注解（若存在）。
	 * <p>此 setter 供开发者提供自定义（非 Spring 专有）注解类型，
	 * 以指示方法应异步执行。
	 * @param asyncAnnotationType 所需的注解类型
	 */
	public void setAsyncAnnotationType(Class<? extends Annotation> asyncAnnotationType) {
		Assert.notNull(asyncAnnotationType, "'asyncAnnotationType' must not be null");
		Set<Class<? extends Annotation>> asyncAnnotationTypes = new HashSet<>();
		asyncAnnotationTypes.add(asyncAnnotationType);
		this.pointcut = buildPointcut(asyncAnnotationTypes);
	}

	/**
	 * 设置按限定符查找执行器时使用的 {@code BeanFactory}。
	 */
	@Override
	public void setBeanFactory(BeanFactory beanFactory) {
		if (this.advice instanceof BeanFactoryAware beanFactoryAware) {
			beanFactoryAware.setBeanFactory(beanFactory);
		}
	}


	@Override
	public Advice getAdvice() {
		return this.advice;
	}

	@Override
	public Pointcut getPointcut() {
		return this.pointcut;
	}


	protected Advice buildAdvice(
			@Nullable Supplier<? extends @Nullable Executor> executor, @Nullable Supplier<? extends @Nullable AsyncUncaughtExceptionHandler> exceptionHandler) {

		AnnotationAsyncExecutionInterceptor interceptor = new AnnotationAsyncExecutionInterceptor(null);
		interceptor.configure(executor, exceptionHandler);
		return interceptor;
	}

	/**
	 * 为给定异步注解类型（若有）计算切点。
	 * @param asyncAnnotationTypes 待内省的异步注解类型
	 * @return 适用的 Pointcut 对象，无则返回 {@code null}
	 */
	protected Pointcut buildPointcut(Set<Class<? extends Annotation>> asyncAnnotationTypes) {
		ComposablePointcut result = null;
		for (Class<? extends Annotation> asyncAnnotationType : asyncAnnotationTypes) {
			Pointcut cpc = new AnnotationMatchingPointcut(asyncAnnotationType, true);
			Pointcut mpc = new AnnotationMatchingPointcut(null, asyncAnnotationType, true);
			if (result == null) {
				result = new ComposablePointcut(cpc);
			}
			else {
				result.union(cpc);
			}
			result = result.union(mpc);
		}
		return (result != null ? result : Pointcut.TRUE);
	}

}
