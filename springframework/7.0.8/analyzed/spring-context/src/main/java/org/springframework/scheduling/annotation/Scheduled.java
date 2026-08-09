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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

import org.springframework.aot.hint.annotation.Reflective;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

/**
 * 标记方法为定时任务的注解。对于周期性任务，须指定 {@link #cron}、
 * {@link #fixedDelay} 或 {@link #fixedRate} 之一，并可额外指定 {@link #initialDelay}。
 * 对于一次性任务，仅指定 {@link #initialDelay} 即可。
 *
 * <p>标注方法不得接受参数。通常返回类型为 {@code void}；
 * 否则通过调度器调用时将忽略返回值。
 *
 * <p>支持返回响应式 {@code Publisher} 或可经默认 {@code ReactiveAdapterRegistry}
 * 适配为 {@code Publisher} 类型的方法。{@code Publisher} 须支持多次后续订阅。
 * 返回的 {@code Publisher} 仅产生一次，调度基础设施随后按配置周期性订阅。
 * 发布者发出的值被忽略。错误以 {@code WARN} 级别记录，不阻止后续迭代。
 * 若配置固定延迟，订阅将被阻塞以遵守固定延迟语义。
 *
 * <p>也支持 Kotlin 挂起函数，前提是运行时存在协程-reactor 桥接
 * （{@code kotlinx.coroutine.reactor}）。该桥接将挂起函数适配为 {@code Publisher}，
 * 处理方式与响应式方法情况相同（见上文）。
 *
 * <p>{@code @Scheduled} 注解的处理通过注册 {@link ScheduledAnnotationBeanPostProcessor} 完成。
 * 可手动注册，或更方便地通过 {@code <task:annotation-driven/>} XML 元素
 * 或 {@link EnableScheduling @EnableScheduling} 注解。
 *
 * <p>本注解可作为<em>{@linkplain Repeatable 可重复}</em>注解使用。
 * 若同一方法上存在多个定时声明，将独立处理，各自触发独立触发器。
 * 因此此类共存调度可能重叠，并行或连续多次执行。
 *
 * <p>本注解可作为<em>元注解</em>创建带属性覆盖的自定义<em>组合注解</em>。
 *
 * @author Mark Fisher
 * @author Juergen Hoeller
 * @author Dave Syer
 * @author Chris Beams
 * @author Victor Brown
 * @author Sam Brannen
 * @since 3.0
 * @see EnableScheduling
 * @see ScheduledAnnotationBeanPostProcessor
 * @see Schedules
 */
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Repeatable(Schedules.class)
@Reflective
public @interface Scheduled {

	/**
	 * 表示禁用触发器的特殊 cron 表达式值：{@value}。
	 * <p>主要用于 <code>${...}</code> 占位符，
	 * 允许从外部禁用对应定时方法。
	 * @since 5.1
	 * @see ScheduledTaskRegistrar#CRON_DISABLED
	 */
	String CRON_DISABLED = ScheduledTaskRegistrar.CRON_DISABLED;


	/**
	 * 类 cron 表达式，扩展常规 UN*X 定义，包含秒、分、时、日、月、周触发。
	 * <p>例如 {@code "0 * * * * MON-FRI"} 表示工作日每分钟一次（整分第 0 秒）。
	 * <p>从左到右各字段含义如下。
	 * <ul>
	 * <li>second</li>
	 * <li>minute</li>
	 * <li>hour</li>
	 * <li>day of month</li>
	 * <li>month</li>
	 * <li>day of week</li>
	 * </ul>
	 * <p>特殊值 {@link #CRON_DISABLED "-"} 表示禁用的 cron 触发器，
	 * 主要用于由 <code>${...}</code> 占位符解析的外部指定值。
	 * @return 可解析为 cron 调度的表达式
	 * @see org.springframework.scheduling.support.CronExpression#parse(String)
	 */
	String cron() default "";

	/**
	 * cron 表达式解析使用的时区。默认为空字符串（即使用调度器时区）。
	 * @return {@link java.util.TimeZone#getTimeZone(String)} 接受的区域 ID，
	 * 或空字符串表示调度器默认时区
	 * @since 4.0
	 * @see org.springframework.scheduling.support.CronTrigger#CronTrigger(String, java.util.TimeZone)
	 * @see java.util.TimeZone
	 */
	String zone() default "";

	/**
	 * 以固定周期执行标注方法（两次调用间隔固定）。
	 * <p>时间单位默认为毫秒，可通过 {@link #timeUnit} 覆盖。
	 * @return 周期
	 */
	long fixedRate() default -1;

	/**
	 * 以固定周期执行标注方法。
	 * <p>持续时间字符串可为多种格式：
	 * <ul>
	 * <li>a plain integer &mdash; which is interpreted to represent a duration in
	 * milliseconds by default unless overridden via {@link #timeUnit()} (prefer
	 * using {@link #fixedDelay()} in that case)</li>
	 * <li>any of the known {@link org.springframework.format.annotation.DurationFormat.Style
	 * DurationFormat.Style}: the {@link org.springframework.format.annotation.DurationFormat.Style#ISO8601 ISO8601}
	 * style or the {@link org.springframework.format.annotation.DurationFormat.Style#SIMPLE SIMPLE} style
	 * &mdash; using the {@link #timeUnit()} as fallback if the string doesn't contain an explicit unit</li>
	 * <li>one of the above, with Spring-style "${...}" placeholders as well as SpEL expressions</li>
	 * </ul>
	 * @return 周期字符串值 &mdash; 例如占位符、
	 * {@link org.springframework.format.annotation.DurationFormat.Style#ISO8601 java.time.Duration} 兼容值
	 * 或 {@link org.springframework.format.annotation.DurationFormat.Style#SIMPLE 简单格式}兼容值
	 * @since 3.2.2
	 * @see #fixedRate()
	 */
	String fixedRateString() default "";

	/**
	 * 在上次调用结束与下次调用开始之间以固定延迟执行标注方法。
	 * <p>时间单位默认为毫秒，可通过 {@link #timeUnit} 覆盖。
	 * <p><b>注意：使用虚拟线程时，推荐固定速率与 cron 触发器而非固定延迟。</b>
	 * 固定延迟任务在 {@link org.springframework.scheduling.concurrent.SimpleAsyncTaskScheduler}
	 * 的单调度器线程上运行。
	 * @return 延迟
	 */
	long fixedDelay() default -1;

	/**
	 * Execute the annotated method with a fixed period between the end of the
	 * last invocation and the start of the next.
	 * <p>The duration String can be in several formats:
	 * <ul>
	 * <li>a plain integer &mdash; which is interpreted to represent a duration in
	 * milliseconds by default unless overridden via {@link #timeUnit()} (prefer
	 * using {@link #fixedDelay()} in that case)</li>
	 * <li>any of the known {@link org.springframework.format.annotation.DurationFormat.Style
	 * DurationFormat.Style}: the {@link org.springframework.format.annotation.DurationFormat.Style#ISO8601 ISO8601}
	 * style or the {@link org.springframework.format.annotation.DurationFormat.Style#SIMPLE SIMPLE} style
	 * &mdash; using the {@link #timeUnit()} as fallback if the string doesn't contain an explicit unit</li>
	 * </ul>
	 * <p><b>NOTE: With virtual threads, fixed rates and cron triggers are recommended
	 * over fixed delays.</b> Fixed-delay tasks operate on a single scheduler thread
	 * with {@link org.springframework.scheduling.concurrent.SimpleAsyncTaskScheduler}.
	 * @return 延迟字符串值 &mdash; 例如占位符、
	 * {@link org.springframework.format.annotation.DurationFormat.Style#ISO8601 java.time.Duration} 兼容值
	 * 或 {@link org.springframework.format.annotation.DurationFormat.Style#SIMPLE 简单格式}兼容值
	 * @since 3.2.2
	 * @see #fixedDelay()
	 */
	String fixedDelayString() default "";

	/**
	 * {@link #fixedRate} 或 {@link #fixedDelay} 任务首次执行前的延迟时间单位数。
	 * <p>时间单位默认为毫秒，可通过 {@link #timeUnit} 覆盖。
	 * @return 初始延迟
	 * @since 3.2
	 */
	long initialDelay() default -1;

	/**
	 * Number of units of time to delay before the first execution of a
	 * {@link #fixedRate} or {@link #fixedDelay} task.
	 * <p>The duration String can be in several formats:
	 * <ul>
	 * <li>a plain integer &mdash; which is interpreted to represent a duration in
	 * milliseconds by default unless overridden via {@link #timeUnit()} (prefer
	 * using {@link #fixedDelay()} in that case)</li>
	 * <li>any of the known {@link org.springframework.format.annotation.DurationFormat.Style
	 * DurationFormat.Style}: the {@link org.springframework.format.annotation.DurationFormat.Style#ISO8601 ISO8601}
	 * style or the {@link org.springframework.format.annotation.DurationFormat.Style#SIMPLE SIMPLE} style
	 * &mdash; using the {@link #timeUnit()} as fallback if the string doesn't contain an explicit unit</li>
	 * <li>one of the above, with Spring-style "${...}" placeholders as well as SpEL expressions</li>
	 * </ul>
	 * @return 初始延迟字符串值 &mdash; 例如占位符、
	 * {@link org.springframework.format.annotation.DurationFormat.Style#ISO8601 java.time.Duration} 兼容值
	 * 或 {@link org.springframework.format.annotation.DurationFormat.Style#SIMPLE 简单格式}兼容值
	 * @since 3.2.2
	 * @see #initialDelay()
	 */
	String initialDelayString() default "";

	/**
	 * {@link #fixedDelay}、{@link #fixedDelayString}、{@link #fixedRate}、
	 * {@link #fixedRateString}、{@link #initialDelay} 及 {@link #initialDelayString}
	 * 使用的 {@link TimeUnit}。
	 * <p>默认为 {@link TimeUnit#MILLISECONDS}。
	 * <p>对 {@linkplain #cron() cron 表达式}及通过 {@link #fixedDelayString}、
	 * {@link #fixedRateString} 或 {@link #initialDelayString} 提供的
	 * {@link java.time.Duration} 值，本属性被忽略。
	 * @return 使用的 {@code TimeUnit}
	 * @since 5.3.10
	 */
	TimeUnit timeUnit() default TimeUnit.MILLISECONDS;

	/**
	 * 确定运行此定时方法的调度器的限定符。
	 * <p>默认为空字符串，表示默认调度器。
	 * <p>可用于确定目标调度器，
	 * 匹配特定 {@link org.springframework.scheduling.TaskScheduler} 或
	 * {@link java.util.concurrent.ScheduledExecutorService} Bean 定义的限定符值（或 Bean 名称）。
	 * @since 6.1
	 * @see org.springframework.scheduling.SchedulingAwareRunnable#getQualifier()
	 */
	String scheduler() default "";

}
