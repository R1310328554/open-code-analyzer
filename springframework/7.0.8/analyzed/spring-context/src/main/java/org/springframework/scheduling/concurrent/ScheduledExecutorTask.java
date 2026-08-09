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

package org.springframework.scheduling.concurrent;

import java.util.concurrent.TimeUnit;

import org.jspecify.annotations.Nullable;

import org.springframework.util.Assert;

/**
 * 描述定时执行器任务的 JavaBean，包含 {@link Runnable} 以及延迟与周期。
 * 必须指定周期；为其设置默认值没有意义。
 *
 * <p>{@link java.util.concurrent.ScheduledExecutorService} 不提供
 * cron 表达式等更复杂的调度选项。
 * 此类需求请考虑 {@link ThreadPoolTaskScheduler}。
 *
 * <p>注意 {@link java.util.concurrent.ScheduledExecutorService} 机制
 * 在重复执行间共享同一 {@link Runnable} 实例，
 * 与 Quartz 每次执行创建新 Job 实例不同。
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see java.util.concurrent.ScheduledExecutorService#scheduleWithFixedDelay(java.lang.Runnable, long, long, java.util.concurrent.TimeUnit)
 * @see java.util.concurrent.ScheduledExecutorService#scheduleAtFixedRate(java.lang.Runnable, long, long, java.util.concurrent.TimeUnit)
 */
public class ScheduledExecutorTask {

	private @Nullable Runnable runnable;

	private long delay = 0;

	private long period = -1;

	private TimeUnit timeUnit = TimeUnit.MILLISECONDS;

	private boolean fixedRate = false;


	/**
	 * 创建新的 ScheduledExecutorTask，
	 * 通过 Bean 属性填充。
	 * @see #setDelay
	 * @see #setPeriod
	 * @see #setFixedRate
	 */
	public ScheduledExecutorTask() {
	}

	/**
	 * 创建新的 ScheduledExecutorTask，
	 * 默认无延迟的一次性执行。
	 * @param executorTask 要调度的 Runnable
	 */
	public ScheduledExecutorTask(Runnable executorTask) {
		this.runnable = executorTask;
	}

	/**
	 * 创建新的 ScheduledExecutorTask，
	 * 默认带给定延迟的一次性执行。
	 * @param executorTask 要调度的 Runnable
	 * @param delay 首次启动任务前的延迟（毫秒）
	 */
	public ScheduledExecutorTask(Runnable executorTask, long delay) {
		this.runnable = executorTask;
		this.delay = delay;
	}

	/**
	 * 创建新的 ScheduledExecutorTask。
	 * @param executorTask 要调度的 Runnable
	 * @param delay 首次启动任务前的延迟（毫秒）
	 * @param period 重复任务执行间隔（毫秒）
	 * @param fixedRate 是否按固定速率调度
	 */
	public ScheduledExecutorTask(Runnable executorTask, long delay, long period, boolean fixedRate) {
		this.runnable = executorTask;
		this.delay = delay;
		this.period = period;
		this.fixedRate = fixedRate;
	}


	/**
	 * 设置作为执行器任务调度的 Runnable。
	 */
	public void setRunnable(Runnable executorTask) {
		this.runnable = executorTask;
	}

	/**
	 * 返回作为执行器任务调度的 Runnable。
	 */
	public Runnable getRunnable() {
		Assert.state(this.runnable != null, "No Runnable set");
		return this.runnable;
	}

	/**
	 * 设置首次启动任务前的延迟（毫秒）。
	 * 默认为 0，调度成功后立即启动任务。
	 */
	public void setDelay(long delay) {
		this.delay = delay;
	}

	/**
	 * 返回首次启动任务前的延迟。
	 */
	public long getDelay() {
		return this.delay;
	}

	/**
	 * 设置重复任务执行间隔（毫秒）。
	 * <p>默认为 -1，表示一次性执行。若为正值，
	 * 任务将按给定间隔重复执行。
	 * <p>注意 period 值在固定速率与固定延迟执行中语义不同。
	 * <p><b>注意：</b>period 为 0（例如固定延迟）<i>不</i>受支持，
	 * 因为 {@code java.util.concurrent.ScheduledExecutorService} 本身不支持。
	 * 因此 0 将被视为一次性执行；
	 * 但根本不应显式指定该值！
	 * @see #setFixedRate
	 * @see #isOneTimeTask()
	 * @see java.util.concurrent.ScheduledExecutorService#scheduleWithFixedDelay(Runnable, long, long, TimeUnit)
	 */
	public void setPeriod(long period) {
		this.period = period;
	}

	/**
	 * 返回重复任务执行间隔。
	 */
	public long getPeriod() {
		return this.period;
	}

	/**
	 * 本任务是否仅执行一次？
	 * @return 若本任务仅执行一次则返回 {@code true}
	 * @see #getPeriod()
	 */
	public boolean isOneTimeTask() {
		return (this.period <= 0);
	}

	/**
	 * 指定 delay 与 period 值的时间单位。
	 * 默认为毫秒 ({@code TimeUnit.MILLISECONDS})。
	 * @see java.util.concurrent.TimeUnit#MILLISECONDS
	 * @see java.util.concurrent.TimeUnit#SECONDS
	 */
	public void setTimeUnit(@Nullable TimeUnit timeUnit) {
		this.timeUnit = (timeUnit != null ? timeUnit : TimeUnit.MILLISECONDS);
	}

	/**
	 * 返回 delay 与 period 值的时间单位。
	 */
	public TimeUnit getTimeUnit() {
		return this.timeUnit;
	}

	/**
	 * 设置是否按固定速率而非固定延迟调度。
	 * 默认为 "false"，即固定延迟。
	 * <p>执行模式详情见 ScheduledExecutorService Javadoc。
	 * @see java.util.concurrent.ScheduledExecutorService#scheduleWithFixedDelay(java.lang.Runnable, long, long, java.util.concurrent.TimeUnit)
	 * @see java.util.concurrent.ScheduledExecutorService#scheduleAtFixedRate(java.lang.Runnable, long, long, java.util.concurrent.TimeUnit)
	 */
	public void setFixedRate(boolean fixedRate) {
		this.fixedRate = fixedRate;
	}

	/**
	 * 返回是否按固定速率调度。
	 */
	public boolean isFixedRate() {
		return this.fixedRate;
	}

}
