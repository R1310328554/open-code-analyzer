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

package org.springframework.resilience.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.aot.hint.annotation.Reflective;
import org.springframework.core.annotation.AliasFor;

/**
 * 为单个方法指定并发上限的通用注解；
 * 若在类型级别标注，则对给定类层次结构中所有经代理调用的方法生效。
 * 默认行为是在达到上限时阻塞后续方法调用。
 * 也可通过将 {@link #policy()} 配置为 {@code policy = REJECT} 来拒绝后续调用。
 *
 * <p>类型级别场景下，从类型继承并发上限的所有方法共享同一并发节流器，
 * 任意此类方法调用均计入共享并发上限。
 * 而局部标注的方法则仅对该方法调用应用具有指定上限的局部节流器。
 *
 * <p>在虚拟线程场景下尤其有用，因为通常不存在线程池上限。
 * 异步任务可在 {@link org.springframework.core.task.SimpleAsyncTaskExecutor} 上约束。
 * 同步调用时，本注解通过
 * {@link org.springframework.aop.interceptor.ConcurrencyThrottleInterceptor} 提供等价行为。
 * 编程式用法也可考虑 {@link org.springframework.core.task.SyncTaskExecutor}
 * 及其继承的并发节流支持（7.0 新增）。
 *
 * @author Juergen Hoeller
 * @author Hyunsang Han
 * @author Sam Brannen
 * @since 7.0
 * @see EnableResilientMethods
 * @see ConcurrencyLimitBeanPostProcessor
 * @see org.springframework.aop.interceptor.ConcurrencyThrottleInterceptor
 * @see org.springframework.core.task.SyncTaskExecutor#setConcurrencyLimit
 * @see org.springframework.core.task.SimpleAsyncTaskExecutor#setConcurrencyLimit
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Reflective
public @interface ConcurrencyLimit {

	/**
	 * {@link #limit()} 的别名。
	 * <p>在无需其他属性时使用 &mdash; 例如 {@code @ConcurrencyLimit(5)}。
	 * @see #limitString()
	 */
	@AliasFor("limit")
	int value() default Integer.MIN_VALUE;

	/**
	 * 并发上限。
	 * <p>指定 {@code 1} 可有效地在每次方法调用时锁定目标实例。
	 * <p>指定大于 {@code 1} 的上限可实现类池化节流，
	 * 限制并发调用数，类似线程池上限。
	 * <p>指定 {@code -1} 表示无界并发。
	 * @see #value()
	 * @see #limitString()
	 * @see org.springframework.util.ConcurrencyThrottleSupport#UNBOUNDED_CONCURRENCY
	 */
	@AliasFor("value")
	int limit() default Integer.MIN_VALUE;

	/**
	 * 可配置字符串形式的并发上限。
	 * <p>此处指定非空值将覆盖 {@link #limit()} 与 {@link #value()} 属性。
	 * <p>支持 Spring 风格 "${...}" 占位符及 SpEL 表达式。
	 * <p>支持的值详见 {@link #limit()} 的 Javadoc。
	 * @see #limit()
	 * @see org.springframework.util.ConcurrencyThrottleSupport#UNBOUNDED_CONCURRENCY
	 */
	String limitString() default "";

	/**
	 * 达到并发上限时对方法调用施加节流的策略。
	 * <p>默认行为是在达到指定上限后阻塞后续并发调用：{@link ThrottlePolicy#BLOCK}。
	 * <p>将策略切换为 {@code REJECT} 可改为拒绝后续调用，
	 * 在进一步并发调用尝试时抛出 {@link org.springframework.resilience.InvocationRejectedException}
	 *（继承通用 {@link java.util.concurrent.RejectedExecutionException}）：
	 * {@link ThrottlePolicy#REJECT}。
	 * @since 7.0.3
	 */
	ThrottlePolicy policy() default ThrottlePolicy.BLOCK;


	/**
	 * 达到并发上限时对方法调用施加节流所应用的策略。
	 * @since 7.0.3
	 */
	enum ThrottlePolicy {

		/**
		 * 默认策略：阻塞直至能在配置上限内调用方法。
		 */
		BLOCK,

		/**
		 * 备选策略：达到上限后拒绝后续方法调用。
		 * @see org.springframework.resilience.InvocationRejectedException
		 */
		REJECT
	}

}
