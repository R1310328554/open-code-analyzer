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
import java.util.concurrent.TimeUnit;

import org.springframework.aot.hint.annotation.Reflective;
import org.springframework.core.annotation.AliasFor;
import org.springframework.resilience.retry.MethodRetryPredicate;

/**
 * 为单个方法指定重试特征的通用注解；
 * 若在类型级别标注，则对给定类层次结构中所有经代理调用的方法生效。
 *
 * <p>与 {@link org.springframework.core.retry.RetryTemplate}
 * 及 Reactor 重试支持对齐，可重新调用命令式目标方法，
 * 或相应地装饰返回的响应式 Publisher。
 *
 * <p>若要跟踪方法级重试处理中遇到的异常，
 * 可考虑 {@link org.springframework.resilience.retry.MethodRetryEvent} 监听器。
 *
 * <p>受 <a href="https://github.com/spring-projects/spring-retry">Spring Retry</a>
 * 项目启发，但在 Spring Framework 中重新设计为最小核心重试特性。
 *
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 7.0
 * @see EnableResilientMethods
 * @see RetryAnnotationBeanPostProcessor
 * @see org.springframework.core.retry.RetryPolicy
 * @see org.springframework.core.retry.RetryTemplate
 * @see reactor.core.publisher.Mono#retryWhen
 * @see reactor.core.publisher.Flux#retryWhen
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Reflective
public @interface Retryable {

	/**
	 * {@link #includes()} 的便捷默认属性，
	 * 通常与单个需重试的异常类型配合使用。
	 */
	@AliasFor("includes")
	Class<? extends Throwable>[] value() default {};

	/**
	 * 应尝试重试的适用异常类型。本属性便于指定可赋值的异常类型。
	 * <p>所提供的异常类型将与失败调用抛出的异常及嵌套的
	 * {@linkplain Throwable#getCause() 原因} 进行匹配。
	 * <p>可与 {@link #excludes() excludes} 或自定义 {@link #predicate() predicate} 组合使用。
	 * <p>默认为空，表示对任意异常尝试重试。
	 * @see #excludes()
	 * @see #predicate()
	 */
	@AliasFor("value")
	Class<? extends Throwable>[] includes() default {};

	/**
	 * 应避免重试的不适用异常类型。本属性便于指定可赋值的异常类型。
	 * <p>所提供的异常类型将与失败调用抛出的异常及嵌套的
	 * {@linkplain Throwable#getCause() 原因} 进行匹配。
	 * <p>可与 {@link #includes() includes} 或自定义 {@link #predicate() predicate} 组合使用。
	 * <p>默认为空，表示对任意异常尝试重试。
	 * @see #includes()
	 * @see #predicate()
	 */
	Class<? extends Throwable>[] excludes() default {};

	/**
	 * 过滤可重试调用的适用异常的谓词。
	 * <p>指定的 {@link MethodRetryPredicate} 实现将按方法实例化。
	 * 若需访问其他 Bean 或设施，可在构造器级别或通过自动装配注解使用依赖注入。
	 * <p>可与 {@link #includes() includes} 或 {@link #excludes() excludes} 组合使用。
	 * <p>默认为对任意异常尝试重试。
	 * @see #includes()
	 * @see #excludes()
	 */
	Class<? extends MethodRetryPredicate> predicate() default MethodRetryPredicate.class;

	/**
	 * 最大重试次数。
	 * <p>注意：{@code 总尝试次数 = 1 次初始尝试 + maxRetries 次重试}。
	 * 因此若 {@code maxRetries} 设为 4，带注解方法至少调用 1 次、至多 5 次。
	 * <p>默认为 3。
	 */
	long maxRetries() default 3;

	/**
	 * 可配置字符串形式的最大重试次数。
	 * <p>此处指定非空值将覆盖 {@link #maxRetries()} 属性。
	 * <p>支持 Spring 风格 "${...}" 占位符及 SpEL 表达式。
	 * @see #maxRetries()
	 */
	String maxRetriesString() default "";

	/**
	 * 初始调用及后续重试（含延迟）允许的最大耗时。
	 * <p>默认为 {@code 0}，表示不应用超时。
	 * <p>时间单位默认为毫秒，可通过 {@link #timeUnit} 覆盖。
	 * <p>必须大于等于零。
	 * @since 7.0.2
	 */
	long timeout() default 0;

	/**
	 * 字符串形式的超时时间。
	 * <p>此处指定非空值将覆盖 {@link #timeout()} 属性。
	 * <p>持续时间字符串可为多种形式：
	 * <ul>
	 * <li>纯整数 &mdash; 默认解释为毫秒，除非通过 {@link #timeUnit()} 覆盖（此情况建议使用 {@link #delay()}）</li>
	 * <li>已知 {@link org.springframework.format.annotation.DurationFormat.Style
	 * DurationFormat.Style} 之一：{@link org.springframework.format.annotation.DurationFormat.Style#ISO8601 ISO8601}
	 * 或 {@link org.springframework.format.annotation.DurationFormat.Style#SIMPLE SIMPLE} 风格
	 * &mdash; 若字符串未含显式单位则回退使用 {@link #timeUnit()}</li>
	 * <li>上述任一种，并支持 Spring 风格 "${...}" 占位符及 SpEL 表达式</li>
	 * </ul>
	 * @return 字符串形式的超时值 &mdash; 例如占位符、
	 * 符合 {@link org.springframework.format.annotation.DurationFormat.Style#ISO8601 java.time.Duration} 的值，
	 * 或符合 {@link org.springframework.format.annotation.DurationFormat.Style#SIMPLE 简单格式} 的值
	 * @since 7.0.2
	 * @see #timeout()
	 */
	String timeoutString() default "";

	/**
	 * 初始调用后的基础延迟。若指定乘数，则作为乘法的初始延迟。
	 * <p>时间单位默认为毫秒，可通过 {@link #timeUnit} 覆盖。
	 * <p>必须大于等于零。默认为 1000。
	 * @see #jitter()
	 * @see #multiplier()
	 * @see #maxDelay()
	 */
	long delay() default 1000;

	/**
	 * 字符串形式的初始调用后基础延迟。
	 * <p>此处指定非空值将覆盖 {@link #delay()} 属性。
	 * <p>持续时间字符串格式参见 {@link #timeoutString()}。
	 * @return 字符串形式的初始延迟 &mdash; 例如占位符，
	 * 或符合 {@link org.springframework.format.annotation.DurationFormat.Style#ISO8601 java.time.Duration} 的值
	 * 或符合 {@link org.springframework.format.annotation.DurationFormat.Style#SIMPLE 简单格式} 的值
	 * @see #delay()
	 */
	String delayString() default "";

	/**
	 * 基础重试尝试的抖动值，随机加减于计算延迟，
	 * 结果介于 {@code delay - jitter} 与 {@code delay + jitter} 之间，
	 * 但不低于基础 {@link #delay()} 或高于 {@link #maxDelay()}。
	 * 若指定乘数，亦应用于抖动值。
	 * <p>时间单位默认为毫秒，可通过 {@link #timeUnit} 覆盖。
	 * <p>默认为 0（无抖动）。
	 * @see #delay()
	 * @see #multiplier()
	 * @see #maxDelay()
	 */
	long jitter() default 0;

	/**
	 * 字符串形式的基础重试抖动值。
	 * <p>此处指定非空值将覆盖 {@link #jitter()} 属性。
	 * <p>持续时间字符串格式参见 {@link #timeoutString()}。
	 * @return 字符串形式的抖动值
	 * @see #jitter()
	 */
	String jitterString() default "";

	/**
	 * 下次重试延迟的乘数，应用于前次延迟（从 {@link #delay()} 开始）
	 * 以及每次尝试适用的 {@link #jitter()}。
	 * <p>默认为 1.0，即固定延迟。
	 * @see #delay()
	 * @see #jitter()
	 * @see #maxDelay()
	 */
	double multiplier() default 1.0;

	/**
	 * 可配置字符串形式的下次重试延迟乘数。
	 * <p>此处指定非空值将覆盖 {@link #multiplier()} 属性。
	 * <p>支持 Spring 风格 "${...}" 占位符及 SpEL 表达式。
	 * @see #multiplier()
	 */
	String multiplierString() default "";

	/**
	 * 任意重试尝试的最大延迟，限制 {@link #jitter()} 与 {@link #multiplier()}
	 * 可将 {@linkplain #delay() 延迟} 增大的幅度。
	 * <p>时间单位默认为毫秒，可通过 {@link #timeUnit} 覆盖。
	 * <p>默认为无限制。
	 * @see #delay()
	 * @see #jitter()
	 * @see #multiplier()
	 */
	long maxDelay() default Long.MAX_VALUE;

	/**
	 * 字符串形式的任意重试最大延迟。
	 * <p>此处指定非空值将覆盖 {@link #maxDelay()} 属性。
	 * <p>持续时间字符串格式参见 {@link #timeoutString()}。
	 * @return 字符串形式的最大延迟值
	 * @see #maxDelay()
	 */
	String maxDelayString() default "";

	/**
	 * 用于 {@link #delay}、{@link #delayString}、{@link #jitter}、{@link #jitterString}、
	 * {@link #maxDelay} 与 {@link #maxDelayString} 的 {@link TimeUnit}。
	 * <p>默认为 {@link TimeUnit#MILLISECONDS}。
	 * <p>通过 {@link #delayString}、{@link #jitterString} 或 {@link #maxDelayString}
	 * 提供的 {@link java.time.Duration} 值将忽略本属性。
	 * @return 要使用的 {@code TimeUnit}
	 */
	TimeUnit timeUnit() default TimeUnit.MILLISECONDS;

}
