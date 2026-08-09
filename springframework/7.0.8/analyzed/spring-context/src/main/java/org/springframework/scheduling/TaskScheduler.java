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
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.concurrent.ScheduledFuture;

import org.jspecify.annotations.Nullable;

/**
 * 基于不同触发器调度 {@link Runnable Runnables} 的任务调度器接口。
 *
 * <p>本接口与 {@link SchedulingTaskExecutor} 分离，因为通常代表不同后端，
 * 即具有不同特征与能力的线程池。若实现可处理两类执行特征，可同时实现两个接口。
 *
 * <p>"默认" 实现为
 * {@link org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler}，
 * 包装原生 {@link java.util.concurrent.ScheduledExecutorService}
 * 并添加扩展触发器能力。
 *
 * <p>本接口大致等价于 Jakarta EE 环境中支持的 JSR-236
 * {@code ManagedScheduledExecutorService}，但与 Spring {@code TaskExecutor} 模型对齐。
 *
 * @author Juergen Hoeller
 * @since 3.0
 * @see org.springframework.core.task.TaskExecutor
 * @see java.util.concurrent.ScheduledExecutorService
 * @see org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
 */
public interface TaskScheduler {

	/**
	 * 返回用于调度目的的时钟。
	 * @since 5.3
	 * @see Clock#systemDefaultZone()
	 */
	default Clock getClock() {
		return Clock.systemDefaultZone();
	}

	/**
	 * 调度给定 {@link Runnable}，在触发器指示下次执行时间时调用。
	 * <p>调度器关闭或返回的 {@link ScheduledFuture} 被取消后，执行将结束。
	 * @param task 触发器触发时要执行的 Runnable
	 * @param trigger {@link Trigger} 接口的实现，
	 * 例如包装 cron 表达式的 {@link org.springframework.scheduling.support.CronTrigger} 对象
	 * @return 表示任务待执行状态的 {@link ScheduledFuture}，
	 * 若给定 Trigger 不再触发（即从 {@link Trigger#nextExecution} 返回 {@code null}）则返回 {@code null}
	 * @throws org.springframework.core.task.TaskRejectedException 因内部原因（如池过载策略或池正在关闭）未接受任务时
	 * @see org.springframework.scheduling.support.CronTrigger
	 */
	@Nullable ScheduledFuture<?> schedule(Runnable task, Trigger trigger);

	/**
	 * 调度给定 {@link Runnable}，在指定执行时间调用。
	 * <p>调度器关闭或返回的 {@link ScheduledFuture} 被取消后，执行将结束。
	 * @param task 要执行的 Runnable
	 * @param startTime 任务期望执行时间
	 *（若已过期，任务将立即执行，即尽快执行）
	 * @return 表示任务待执行状态的 {@link ScheduledFuture}
	 * @throws org.springframework.core.task.TaskRejectedException 因内部原因未接受任务时
	 * @since 5.0
	 */
	ScheduledFuture<?> schedule(Runnable task, Instant startTime);

	/**
	 * 调度给定 {@link Runnable}，在指定执行时间调用。
	 * <p>调度器关闭或返回的 {@link ScheduledFuture} 被取消后，执行将结束。
	 * @param task 要执行的 Runnable
	 * @param startTime 任务期望执行时间
	 *（若已过期，任务将立即执行）
	 * @return 表示任务待执行状态的 {@link ScheduledFuture}
	 * @throws org.springframework.core.task.TaskRejectedException 因内部原因未接受任务时
	 * @deprecated 自 6.0 起，请改用 {@link #schedule(Runnable, Instant)}
	 */
	@Deprecated(since = "6.0")
	default ScheduledFuture<?> schedule(Runnable task, Date startTime) {
		return schedule(task, startTime.toInstant());
	}

	/**
	 * 调度给定 {@link Runnable}，在指定执行时间首次调用，随后按给定周期重复。
	 * <p>调度器关闭或返回的 {@link ScheduledFuture} 被取消后，执行将结束。
	 * @param task 要执行的 Runnable
	 * @param startTime 任务期望首次执行时间
	 *（若已过期，任务将立即执行）
	 * @param period 连续执行之间的间隔
	 * @return 表示任务待执行状态的 {@link ScheduledFuture}
	 * @throws org.springframework.core.task.TaskRejectedException 因内部原因未接受任务时
	 * @since 5.0
	 */
	ScheduledFuture<?> scheduleAtFixedRate(Runnable task, Instant startTime, Duration period);

	/**
	 * 调度给定 {@link Runnable}，在指定执行时间首次调用，随后按给定周期（毫秒）重复。
	 * <p>调度器关闭或返回的 {@link ScheduledFuture} 被取消后，执行将结束。
	 * @param task 要执行的 Runnable
	 * @param startTime 任务期望首次执行时间
	 * @param period 连续执行之间的间隔（毫秒）
	 * @return 表示任务待执行状态的 {@link ScheduledFuture}
	 * @throws org.springframework.core.task.TaskRejectedException 因内部原因未接受任务时
	 * @deprecated 自 6.0 起，请改用 {@link #scheduleAtFixedRate(Runnable, Instant, Duration)}
	 */
	@Deprecated(since = "6.0")
	default ScheduledFuture<?> scheduleAtFixedRate(Runnable task, Date startTime, long period) {
		return scheduleAtFixedRate(task, startTime.toInstant(), Duration.ofMillis(period));
	}

	/**
	 * 调度给定 {@link Runnable}，尽快启动并按给定周期重复调用。
	 * <p>调度器关闭或返回的 {@link ScheduledFuture} 被取消后，执行将结束。
	 * @param task 要执行的 Runnable
	 * @param period 连续执行之间的间隔
	 * @return 表示任务待执行状态的 {@link ScheduledFuture}
	 * @throws org.springframework.core.task.TaskRejectedException 因内部原因未接受任务时
	 * @since 5.0
	 */
	ScheduledFuture<?> scheduleAtFixedRate(Runnable task, Duration period);

	/**
	 * 调度给定 {@link Runnable}，尽快启动并按给定周期（毫秒）重复调用。
	 * <p>调度器关闭或返回的 {@link ScheduledFuture} 被取消后，执行将结束。
	 * @param task 要执行的 Runnable
	 * @param period 连续执行之间的间隔（毫秒）
	 * @return 表示任务待执行状态的 {@link ScheduledFuture}
	 * @throws org.springframework.core.task.TaskRejectedException 因内部原因未接受任务时
	 * @deprecated 自 6.0 起，请改用 {@link #scheduleAtFixedRate(Runnable, Duration)}
	 */
	@Deprecated(since = "6.0")
	default ScheduledFuture<?> scheduleAtFixedRate(Runnable task, long period) {
		return scheduleAtFixedRate(task, Duration.ofMillis(period));
	}

	/**
	 * 调度给定 {@link Runnable}，在指定时间首次执行，
	 * 随后在前次执行完成与下次开始之间保持给定延迟。
	 * <p>调度器关闭或返回的 {@link ScheduledFuture} 被取消后，执行将结束。
	 * @param task 要执行的 Runnable
	 * @param startTime 任务期望首次执行时间
	 * @param delay 前次执行完成与下次开始之间的延迟
	 * @return 表示任务待执行状态的 {@link ScheduledFuture}
	 * @throws org.springframework.core.task.TaskRejectedException 因内部原因未接受任务时
	 * @since 5.0
	 */
	ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, Instant startTime, Duration delay);

	/**
	 * 调度给定 {@link Runnable}，在指定时间首次执行，
	 * 随后在前次执行完成与下次开始之间保持给定延迟（毫秒）。
	 * <p>调度器关闭或返回的 {@link ScheduledFuture} 被取消后，执行将结束。
	 * @param task 要执行的 Runnable
	 * @param startTime 任务期望首次执行时间
	 * @param delay 前次执行完成与下次开始之间的延迟（毫秒）
	 * @return 表示任务待执行状态的 {@link ScheduledFuture}
	 * @throws org.springframework.core.task.TaskRejectedException 因内部原因未接受任务时
	 * @deprecated 自 6.0 起，请改用 {@link #scheduleWithFixedDelay(Runnable, Instant, Duration)}
	 */
	@Deprecated(since = "6.0")
	default ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, Date startTime, long delay) {
		return scheduleWithFixedDelay(task, startTime.toInstant(), Duration.ofMillis(delay));
	}

	/**
	 * 调度给定 {@link Runnable}，尽快启动，
	 * 并在前次执行完成与下次开始之间保持给定延迟。
	 * <p>调度器关闭或返回的 {@link ScheduledFuture} 被取消后，执行将结束。
	 * @param task 要执行的 Runnable
	 * @param delay 前次执行完成与下次开始之间的延迟
	 * @return 表示任务待执行状态的 {@link ScheduledFuture}
	 * @throws org.springframework.core.task.TaskRejectedException 因内部原因未接受任务时
	 * @since 5.0
	 */
	ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, Duration delay);

	/**
	 * 调度给定 {@link Runnable}，尽快启动，
	 * 并在前次执行完成与下次开始之间保持给定延迟（毫秒）。
	 * <p>调度器关闭或返回的 {@link ScheduledFuture} 被取消后，执行将结束。
	 * @param task 要执行的 Runnable
	 * @param delay 前次执行完成与下次开始之间的延迟（毫秒）
	 * @return 表示任务待执行状态的 {@link ScheduledFuture}
	 * @throws org.springframework.core.task.TaskRejectedException 因内部原因未接受任务时
	 * @deprecated 自 6.0 起，请改用 {@link #scheduleWithFixedDelay(Runnable, Duration)}
	 */
	@Deprecated(since = "6.0")
	default ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, long delay) {
		return scheduleWithFixedDelay(task, Duration.ofMillis(delay));
	}

}
