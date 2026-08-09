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

package org.springframework.scheduling.support;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.TimeZone;

import org.jspecify.annotations.Nullable;

import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;
import org.springframework.util.Assert;

/**
 * cron 表达式的 {@link Trigger} 实现，包装按常见 crontab 约定解析的
 * {@link CronExpression}。
 *
 * <p>支持带 L/# 表达式的 Quartz 日/周字段。其余方面遵循常见 cron 约定，
 * 包括 SUN-SAT 使用 0-6（SUN 也可用 7）。
 * 注意 Quartz 在 cron 中对星期几使用 1-7 表示 SUN-SAT，
 * 而 Spring 即使结合可选 Quartz 专有 L/# 表达式也严格遵循 cron 约定。
 *
 * @author Juergen Hoeller
 * @author Arjen Poutsma
 * @since 3.0
 * @see CronExpression
 */
public class CronTrigger implements Trigger {

	private final CronExpression expression;

	private final @Nullable ZoneId zoneId;


	/**
	 * 使用默认时区中的模式构建 {@code CronTrigger}。
	 * <p>等价于 {@link CronTrigger#forLenientExecution} 工厂方法。
	 * 若前一任务仍在运行，可能跳过原定触发；若不希望如此，请考虑 {@link CronTrigger#forFixedExecution}。
	 * @param expression 遵循 cron 表达式约定的空格分隔时间字段列表
	 * @see CronTrigger#forLenientExecution
	 * @see CronTrigger#forFixedExecution
	 */
	public CronTrigger(String expression) {
		this.expression = CronExpression.parse(expression);
		this.zoneId = null;
	}

	/**
	 * 使用给定 TimeZone 中的模式构建 {@code CronTrigger}，
	 * 执行策略与 {@link CronTrigger#CronTrigger(String)} 相同（宽松模式）。
	 * <p>通常无需显式自定义时区，应使用 {@link org.springframework.scheduling.TaskScheduler#getClock()}。
	 * @param expression 遵循 cron 表达式约定的空格分隔时间字段列表
	 * @param timeZone 生成触发时间的时区
	 */
	public CronTrigger(String expression, TimeZone timeZone) {
		this.expression = CronExpression.parse(expression);
		Assert.notNull(timeZone, "TimeZone must not be null");
		this.zoneId = timeZone.toZoneId();
	}

	/**
	 * 使用给定 ZoneId 中的模式构建 {@code CronTrigger}，
	 * 执行策略与 {@link CronTrigger#CronTrigger(String)} 相同（宽松模式）。
	 * <p>通常无需显式自定义时区，应使用 {@link org.springframework.scheduling.TaskScheduler#getClock()}。
	 * @param expression 遵循 cron 表达式约定的空格分隔时间字段列表
	 * @param zoneId 生成触发时间的时区
	 * @since 5.3
	 * @see CronExpression#parse(String)
	 */
	public CronTrigger(String expression, ZoneId zoneId) {
		this.expression = CronExpression.parse(expression);
		Assert.notNull(zoneId, "ZoneId must not be null");
		this.zoneId = zoneId;
	}


	/**
	 * 返回构建本触发器时使用的 cron 模式。
	 */
	public String getExpression() {
		return this.expression.toString();
	}


	/**
	 * 根据给定触发器上下文确定下次执行时间。
	 * <p>下次执行时间基于上次执行的
	 * {@linkplain TriggerContext#lastCompletion 完成时间}计算，因此不会重叠执行。
	 */
	@Override
	public @Nullable Instant nextExecution(TriggerContext triggerContext) {
		Instant timestamp = determineLatestTimestamp(triggerContext);
		ZoneId zone = (this.zoneId != null ? this.zoneId : triggerContext.getClock().getZone());
		ZonedDateTime zonedTimestamp = timestamp.atZone(zone);
		ZonedDateTime nextTimestamp = this.expression.next(zonedTimestamp);
		return (nextTimestamp != null ? nextTimestamp.toInstant() : null);
	}

	Instant determineLatestTimestamp(TriggerContext triggerContext) {
		Instant timestamp = triggerContext.lastCompletion();
		if (timestamp != null) {
			Instant scheduled = triggerContext.lastScheduledExecution();
			if (scheduled != null && timestamp.isBefore(scheduled)) {
				// Previous task apparently executed too early...
				// Let's simply use the last calculated execution time then,
				// in order to prevent accidental re-fires in the same second.
				timestamp = scheduled;
			}
		}
		else {
			timestamp = determineInitialTimestamp(triggerContext);
		}
		return timestamp;
	}

	Instant determineInitialTimestamp(TriggerContext triggerContext) {
		return triggerContext.getClock().instant();
	}


	@Override
	public boolean equals(@Nullable Object other) {
		return (this == other || (other instanceof CronTrigger that &&
				this.expression.equals(that.expression) &&
				Objects.equals(this.zoneId, that.zoneId)));
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.expression, this.zoneId);
	}

	@Override
	public String toString() {
		return this.expression.toString();
	}


	/**
	 * 创建宽松执行的 {@link CronTrigger}，每次任务完成后按完成时间重新调度。
	 * <p>若关联任务耗时过长，本变体不会补发错过的触发。
	 * 因此若前一任务仍在运行，可能跳过原定触发。
	 * <p>等价于常规 {@link CronTrigger} 构造函数。
	 * 注意宽松执行依赖调度器：线程池上长任务可能跳过触发，
	 * 而每任务新线程时可能接近 {@link #forFixedExecution} 精度。
	 * @param expression 遵循 cron 表达式约定的空格分隔时间字段列表
	 * @since 6.1.3
	 * @see #resumeLenientExecution
	 */
	public static CronTrigger forLenientExecution(String expression) {
		return new CronTrigger(expression);
	}

	/**
	 * 创建宽松执行的 {@link CronTrigger}，每次任务完成后按完成时间重新调度。
	 * <p>若关联任务耗时过长，本变体不会补发错过的触发。
	 * 因此若前一任务仍在运行，可能跳过原定触发。
	 * @param expression 遵循 cron 表达式约定的空格分隔时间字段列表
	 * @param resumptionTimestamp 恢复起点时间戳（上次已知完成时间），
	 * 从此计算新触发并可能立即触发（仅一次，后续计算均从该首次恢复触发的完成时间开始）
	 * @since 6.1.3
	 * @see #forLenientExecution
	 */
	public static CronTrigger resumeLenientExecution(String expression, Instant resumptionTimestamp) {
		return new CronTrigger(expression) {
			@Override
			Instant determineInitialTimestamp(TriggerContext triggerContext) {
				return resumptionTimestamp;
			}
		};
	}

	/**
	 * 创建固定执行的 {@link CronTrigger}，每次任务完成后按上次计划时间重新调度。
	 * <p>若关联任务耗时过长，本变体会补发错过的触发，为每个原定触发安排任务。
	 * 后续任务可能延迟执行但绝不会被跳过。
	 * <p>长任务情况下立即或延迟执行可能依赖调度器，但不跳过任务的保证可移植。
	 * @param expression 遵循 cron 表达式约定的空格分隔时间字段列表
	 * @since 6.1.3
	 * @see #resumeFixedExecution
	 */
	public static CronTrigger forFixedExecution(String expression) {
		return new CronTrigger(expression) {
			@Override
			protected Instant determineLatestTimestamp(TriggerContext triggerContext) {
				Instant scheduled = triggerContext.lastScheduledExecution();
				return (scheduled != null ? scheduled : super.determineInitialTimestamp(triggerContext));
			}
		};
	}

	/**
	 * 创建固定执行的 {@link CronTrigger}，每次任务完成后按上次计划时间重新调度。
	 * <p>若关联任务耗时过长，本变体会补发错过的触发，为每个原定触发安排任务。
	 * 后续任务可能延迟执行但绝不会被跳过。
	 * @param expression 遵循 cron 表达式约定的空格分隔时间字段列表
	 * @param resumptionTimestamp 恢复起点时间戳（上次已知计划时间），
	 * 其间每个触发立即补发，以弥补期间本应发生的每次执行
	 * @since 6.1.3
	 * @see #forFixedExecution
	 */
	public static CronTrigger resumeFixedExecution(String expression, Instant resumptionTimestamp) {
		return new CronTrigger(expression) {
			@Override
			protected Instant determineLatestTimestamp(TriggerContext triggerContext) {
				Instant scheduled = triggerContext.lastScheduledExecution();
				return (scheduled != null ? scheduled : super.determineLatestTimestamp(triggerContext));
			}
			@Override
			Instant determineInitialTimestamp(TriggerContext triggerContext) {
				return resumptionTimestamp;
			}
		};
	}

}
