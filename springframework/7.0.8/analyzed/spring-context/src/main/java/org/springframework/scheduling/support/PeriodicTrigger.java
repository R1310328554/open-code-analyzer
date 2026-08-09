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

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

import org.jspecify.annotations.Nullable;

import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

/**
 * 用于周期性任务执行的触发器。周期可按固定速率或固定延迟应用，
 * 也可配置初始延迟。默认初始延迟为 0，默认行为为固定延迟
 *（即连续两次执行之间的间隔从每次<i>完成</i>时刻起算）。
 * 若要从各次调度的<i>开始</i>时刻起算间隔，
 * 将 {@code fixedRate} 属性设为 {@code true}。
 *
 * <p>注意：{@link TaskScheduler} 接口已定义固定速率与固定延迟的调度方法，
 * 且均支持可选的初始延迟。应尽可能直接使用这些方法。
 * 本 {@link Trigger} 实现的价值在于可在依赖 Trigger 抽象层的组件中使用，
 * 例如便于周期触发器、cron 触发器乃至自定义 Trigger 实现互换使用。
 *
 * @author Mark Fisher
 * @since 3.0
 */
public class PeriodicTrigger implements Trigger {

	private final Duration period;

	private final @Nullable ChronoUnit chronoUnit;

	private volatile @Nullable Duration initialDelay;

	private volatile boolean fixedRate;


	/**
	 * 以给定毫秒周期创建触发器。
	 * @deprecated 自 6.0 起，请改用 {@link #PeriodicTrigger(Duration)}
	 */
	@Deprecated(since = "6.0")
	public PeriodicTrigger(long period) {
		this(period, null);
	}

	/**
	 * 以给定周期与时间单位创建触发器。该时间单位不仅作用于周期，
	 * 也作用于后续通过 {@link #setInitialDelay(long)} 配置的 initialDelay。
	 * @deprecated 自 6.0 起，请改用 {@link #PeriodicTrigger(Duration)}
	 */
	@Deprecated(since = "6.0")
	public PeriodicTrigger(long period, @Nullable TimeUnit timeUnit) {
		this(toDuration(period, timeUnit), timeUnit);
	}

	private static Duration toDuration(long amount, @Nullable TimeUnit timeUnit) {
		if (timeUnit != null) {
			return Duration.of(amount, timeUnit.toChronoUnit());
		}
		else {
			return Duration.ofMillis(amount);
		}
	}

	/**
	 * 以给定 {@link Duration} 周期创建触发器。
	 * @since 6.0
	 */
	public PeriodicTrigger(Duration period) {
		this(period, null);
	}

	private PeriodicTrigger(Duration period, @Nullable TimeUnit timeUnit) {
		Assert.notNull(period, "Period must not be null");
		Assert.isTrue(!period.isNegative(), "Period must not be negative");
		this.period = period;
		if (timeUnit != null) {
			this.chronoUnit = timeUnit.toChronoUnit();
		}
		else {
			this.chronoUnit = null;
		}
	}


	/**
	 * 返回本触发器的周期。
	 * @since 5.0.2
	 * @deprecated 自 6.0 起，请改用 {@link #getPeriodDuration()}
	 */
	@Deprecated(since = "6.0")
	public long getPeriod() {
		if (this.chronoUnit != null) {
			return this.period.get(this.chronoUnit);
		}
		else {
			return this.period.toMillis();
		}
	}

	/**
	 * 返回本触发器的周期。
	 * @since 6.0
	 */
	public Duration getPeriodDuration() {
		return this.period;
	}

	/**
	 * 返回本触发器的时间单位（默认为毫秒）。
	 * @since 5.0.2
	 * @deprecated 自 6.0 起，无直接替代方法
	 */
	@Deprecated(since = "6.0")
	public TimeUnit getTimeUnit() {
		if (this.chronoUnit != null) {
			return TimeUnit.of(this.chronoUnit);
		}
		else {
			return TimeUnit.MILLISECONDS;
		}
	}

	/**
	 * 指定首次执行的延迟，按本触发器的 {@link TimeUnit} 解释。
	 * 若实例化时未显式提供时间单位，则默认为毫秒。
	 * @deprecated 自 6.0 起，请改用 {@link #setInitialDelay(Duration)}
	 */
	@Deprecated(since = "6.0")
	public void setInitialDelay(long initialDelay) {
		if (this.chronoUnit != null) {
			this.initialDelay = Duration.of(initialDelay, this.chronoUnit);
		}
		else {
			this.initialDelay = Duration.ofMillis(initialDelay);
		}
	}

	/**
	 * 指定首次执行的延迟。
	 * @since 6.0
	 */
	public void setInitialDelay(Duration initialDelay) {
		this.initialDelay = initialDelay;
	}

	/**
	 * 返回初始延迟；若无则返回 0。
	 * @since 5.0.2
	 * @deprecated 自 6.0 起，请改用 {@link #getInitialDelayDuration()}
	 */
	@Deprecated(since = "6.0")
	public long getInitialDelay() {
		Duration initialDelay = this.initialDelay;
		if (initialDelay != null) {
			if (this.chronoUnit != null) {
				return initialDelay.get(this.chronoUnit);
			}
			else {
				return initialDelay.toMillis();
			}
		}
		else {
			return 0;
		}
	}

	/**
	 * 返回初始延迟；若无则返回 {@code null}。
	 * @since 6.0
	 */
	public @Nullable Duration getInitialDelayDuration() {
		return this.initialDelay;
	}

	/**
	 * 指定周期间隔是否按各次调度的开始时间（而非实际完成时间）计量。
	 * 后者即“固定延迟”行为，为默认方式。
	 */
	public void setFixedRate(boolean fixedRate) {
		this.fixedRate = fixedRate;
	}

	/**
	 * 返回本触发器是否使用固定速率（{@code true}）或固定延迟（{@code false}）。
	 * @since 5.0.2
	 */
	public boolean isFixedRate() {
		return this.fixedRate;
	}


	/**
	 * 返回任务下次应执行的时间。
	 */
	@Override
	public Instant nextExecution(TriggerContext triggerContext) {
		Instant lastExecution = triggerContext.lastScheduledExecution();
		Instant lastCompletion = triggerContext.lastCompletion();
		if (lastExecution == null || lastCompletion == null) {
			Instant instant = triggerContext.getClock().instant();
			Duration initialDelay = this.initialDelay;
			if (initialDelay == null) {
				return instant;
			}
			else {
				return instant.plus(initialDelay);
			}
		}
		if (this.fixedRate) {
			return lastExecution.plus(this.period);
		}
		return lastCompletion.plus(this.period);
	}


	@Override
	public boolean equals(@Nullable Object other) {
		return (this == other || (other instanceof PeriodicTrigger that &&
				this.fixedRate == that.fixedRate &&
				this.period.equals(that.period) &&
				ObjectUtils.nullSafeEquals(this.initialDelay, that.initialDelay)));
	}

	@Override
	public int hashCode() {
		return this.period.hashCode();
	}

}
