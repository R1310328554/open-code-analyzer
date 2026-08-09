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

package org.springframework.scheduling.config;

import java.time.Duration;

import org.springframework.util.Assert;

/**
 * 定义在指定毫秒间隔执行的 {@code Runnable} 的 {@link Task} 实现，
 * 根据上下文可视为固定速率或固定延迟。
 *
 * @author Chris Beams
 * @author Arjen Poutsma
 * @since 3.2
 * @see ScheduledTaskRegistrar#addFixedRateTask(IntervalTask)
 * @see ScheduledTaskRegistrar#addFixedDelayTask(IntervalTask)
 */
public class IntervalTask extends DelayedTask {

	private final Duration interval;


	/**
	 * 创建新的 {@code IntervalTask}。
	 * @param runnable 要执行的底层任务
	 * @param interval 任务执行间隔（毫秒）
	 * @param initialDelay 任务首次执行前的初始延迟
	 * @deprecated 自 6.0 起，请改用 {@link #IntervalTask(Runnable, Duration, Duration)}
	 */
	@Deprecated(since = "6.0")
	public IntervalTask(Runnable runnable, long interval, long initialDelay) {
		this(runnable, Duration.ofMillis(interval), Duration.ofMillis(initialDelay));
	}

	/**
	 * 创建无初始延迟的新 {@code IntervalTask}。
	 * @param runnable 要执行的底层任务
	 * @param interval 任务执行间隔（毫秒）
	 * @deprecated 自 6.0 起，请改用 {@link #IntervalTask(Runnable, Duration)}
	 */
	@Deprecated(since = "6.0")
	public IntervalTask(Runnable runnable, long interval) {
		this(runnable, Duration.ofMillis(interval), Duration.ZERO);
	}

	/**
	 * 创建无初始延迟的新 {@code IntervalTask}。
	 * @param runnable 要执行的底层任务
	 * @param interval 任务执行间隔
	 * @since 6.0
	 */
	public IntervalTask(Runnable runnable, Duration interval) {
		this(runnable, interval, Duration.ZERO);
	}

	/**
	 * 创建新的 {@code IntervalTask}。
	 * @param runnable 要执行的底层任务
	 * @param interval 任务执行间隔
	 * @param initialDelay 任务首次执行前的初始延迟
	 * @since 6.0
	 */
	public IntervalTask(Runnable runnable, Duration interval, Duration initialDelay) {
		super(runnable, initialDelay);
		Assert.notNull(interval, "Interval must not be null");
		this.interval = interval;
	}

	/**
	 * 拷贝构造函数。
	 */
	IntervalTask(IntervalTask task) {
		super(task);
		this.interval = task.getIntervalDuration();
	}


	/**
	 * 返回任务执行间隔（毫秒）。
	 * @deprecated 自 6.0 起，请改用 {@link #getIntervalDuration()}
	 */
	@Deprecated(since = "6.0")
	public long getInterval() {
		return this.interval.toMillis();
	}

	/**
	 * 返回任务执行间隔。
	 * @since 6.0
	 */
	public Duration getIntervalDuration() {
		return this.interval;
	}

	/**
	 * 返回任务首次执行前的初始延迟。
	 * @deprecated 自 6.0 起，请改用 {@link #getInitialDelayDuration()}
	 */
	@Deprecated(since = "6.0")
	public long getInitialDelay() {
		return getInitialDelayDuration().toMillis();
	}

	/**
	 * 返回任务首次执行前的初始延迟。
	 * @since 6.0
	 */
	@Override
	public Duration getInitialDelayDuration() {
		return super.getInitialDelayDuration();
	}

}
