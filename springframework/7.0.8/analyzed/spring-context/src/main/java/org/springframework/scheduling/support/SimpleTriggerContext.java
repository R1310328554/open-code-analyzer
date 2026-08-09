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

import java.time.Clock;
import java.time.Instant;
import java.util.Date;

import org.jspecify.annotations.Nullable;

import org.springframework.scheduling.TriggerContext;

/**
 * {@link TriggerContext} 接口的简单数据持有者实现。
 *
 * @author Juergen Hoeller
 * @since 3.0
 */
public class SimpleTriggerContext implements TriggerContext {

	private final Clock clock;

	private volatile @Nullable Instant lastScheduledExecution;

	private volatile @Nullable Instant lastActualExecution;

	private volatile @Nullable Instant lastCompletion;


	/**
	 * 创建所有时间值均为 {@code null} 的 SimpleTriggerContext，
	 * 使用默认时区的系统时钟。
	 */
	public SimpleTriggerContext() {
		this.clock = Clock.systemDefaultZone();
	}

	/**
	 * 以给定时间值创建 SimpleTriggerContext，使用默认时区的系统时钟。
	 * @param lastScheduledExecutionTime 上次<i>计划</i>执行时间
	 * @param lastActualExecutionTime 上次<i>实际</i>执行时间
	 * @param lastCompletionTime 上次完成时间
	 * @deprecated 自 6.0 起，请改用 {@link #SimpleTriggerContext(Instant, Instant, Instant)}
	 */
	@Deprecated(since = "6.0")
	public SimpleTriggerContext(@Nullable Date lastScheduledExecutionTime, @Nullable Date lastActualExecutionTime,
			@Nullable Date lastCompletionTime) {

		this(toInstant(lastScheduledExecutionTime), toInstant(lastActualExecutionTime), toInstant(lastCompletionTime));
	}

	private static @Nullable Instant toInstant(@Nullable Date date) {
		return (date != null ? date.toInstant() : null);
	}

	/**
	 * 以给定时间值创建 SimpleTriggerContext，使用默认时区的系统时钟。
	 * @param lastScheduledExecution 上次<i>计划</i>执行时间
	 * @param lastActualExecution 上次<i>实际</i>执行时间
	 * @param lastCompletion 上次完成时间
	 */
	public SimpleTriggerContext(@Nullable Instant lastScheduledExecution, @Nullable Instant lastActualExecution,
			@Nullable Instant lastCompletion) {

		this();
		this.lastScheduledExecution = lastScheduledExecution;
		this.lastActualExecution = lastActualExecution;
		this.lastCompletion = lastCompletion;
	}

	/**
	 * 创建所有时间值均为 {@code null} 的 SimpleTriggerContext，使用给定时钟。
	 * @param clock 用于触发器计算的时钟
	 * @since 5.3
	 * @see #update(Instant, Instant, Instant)
	 */
	public SimpleTriggerContext(Clock clock) {
		this.clock = clock;
	}


	/**
	 * 以最新时间值更新本持有者的状态。
 	 * @param lastScheduledExecutionTime 上次<i>计划</i>执行时间
	 * @param lastActualExecutionTime 上次<i>实际</i>执行时间
	 * @param lastCompletionTime 上次完成时间
	 * @deprecated 自 6.0 起，请改用 {@link #update(Instant, Instant, Instant)}
	 */
	@Deprecated(since = "6.0")
	public void update(@Nullable Date lastScheduledExecutionTime, @Nullable Date lastActualExecutionTime,
			@Nullable Date lastCompletionTime) {

		update(toInstant(lastScheduledExecutionTime), toInstant(lastActualExecutionTime), toInstant(lastCompletionTime));
	}

	/**
	 * 以最新时间值更新本持有者的状态。
 	 * @param lastScheduledExecution 上次<i>计划</i>执行时间
	 * @param lastActualExecution 上次<i>实际</i>执行时间
	 * @param lastCompletion 上次完成时间
	 */
	public void update(@Nullable Instant lastScheduledExecution, @Nullable Instant lastActualExecution,
			@Nullable Instant lastCompletion) {

		this.lastScheduledExecution = lastScheduledExecution;
		this.lastActualExecution = lastActualExecution;
		this.lastCompletion = lastCompletion;
	}


	@Override
	public Clock getClock() {
		return this.clock;
	}

	@Override
	public @Nullable Instant lastScheduledExecution() {
		return this.lastScheduledExecution;
	}

	@Override
	public @Nullable Instant lastActualExecution() {
		return this.lastActualExecution;
	}

	@Override
	public @Nullable Instant lastCompletion() {
		return this.lastCompletion;
	}

}
