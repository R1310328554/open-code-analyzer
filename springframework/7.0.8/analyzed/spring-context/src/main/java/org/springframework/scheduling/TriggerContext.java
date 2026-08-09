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

package org.springframework.scheduling;

import java.time.Clock;
import java.time.Instant;
import java.util.Date;

import org.jspecify.annotations.Nullable;

/**
 * 封装给定任务上次执行时间与上次完成时间的上下文对象。
 *
 * @author Juergen Hoeller
 * @author Arjen Poutsma
 * @since 3.0
 */
public interface TriggerContext {

	/**
	 * 返回用于触发器计算的时钟。
	 * <p>默认为 {@link Clock#systemDefaultZone()}。
	 * @since 5.3
	 * @see TaskScheduler#getClock()
	 */
	default Clock getClock() {
		return Clock.systemDefaultZone();
	}

	/**
	 * 返回任务上次<i>计划</i>执行时间，
	 * 若此前未调度则返回 {@code null}。
	 * <p>默认实现委托给 {@link #lastScheduledExecution()}。
	 * @deprecated 自 6.0 起，请改用 {@link #lastScheduledExecution()}
	 */
	@Deprecated(since = "6.0")
	default @Nullable Date lastScheduledExecutionTime() {
		Instant instant = lastScheduledExecution();
		return (instant != null ? Date.from(instant) : null);
	}

	/**
	 * 返回任务上次<i>计划</i>执行时间，
	 * 若此前未调度则返回 {@code null}。
	 * @since 6.0
	 */
	@Nullable Instant lastScheduledExecution();

	/**
	 * 返回任务上次<i>实际</i>执行时间，
	 * 若此前未调度则返回 {@code null}。
	 * <p>默认实现委托给 {@link #lastActualExecution()}。
	 * @deprecated 自 6.0 起，请改用 {@link #lastActualExecution()}
	 */
	@Deprecated(since = "6.0")
	default @Nullable Date lastActualExecutionTime() {
		Instant instant = lastActualExecution();
		return (instant != null ? Date.from(instant) : null);
	}

	/**
	 * 返回任务上次<i>实际</i>执行时间，
	 * 若此前未调度则返回 {@code null}。
	 * @since 6.0
	 */
	@Nullable Instant lastActualExecution();

	/**
	 * 返回任务上次完成时间，
	 * 若此前未调度则返回 {@code null}。
	 * <p>默认实现委托给 {@link #lastCompletion()}。
	 * @deprecated 自 6.0 起，请改用 {@link #lastCompletion()}
	 */
	@Deprecated(since = "6.0")
	default @Nullable Date lastCompletionTime() {
		Instant instant = lastCompletion();
		return (instant != null ? Date.from(instant) : null);
	}

	/**
	 * 返回任务上次完成时间，
	 * 若此前未调度则返回 {@code null}。
	 * @since 6.0
	 */
	@Nullable Instant lastCompletion();

}
