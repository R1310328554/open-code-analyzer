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

import java.time.Instant;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.jspecify.annotations.Nullable;

/**
 * 运行时定时任务的表示，用作调度方法的返回值。
 *
 * @author Juergen Hoeller
 * @author Brian Clozel
 * @since 4.3
 * @see ScheduledTaskRegistrar#scheduleCronTask(CronTask)
 * @see ScheduledTaskRegistrar#scheduleFixedRateTask(FixedRateTask)
 * @see ScheduledTaskRegistrar#scheduleFixedDelayTask(FixedDelayTask)
 * @see ScheduledFuture
 */
public final class ScheduledTask {

	private final Task task;

	volatile @Nullable ScheduledFuture<?> future;


	ScheduledTask(Task task) {
		this.task = task;
	}


	/**
	 * 返回底层任务（通常为 {@link CronTask}、
	 * {@link FixedRateTask} 或 {@link FixedDelayTask}）。
	 * @since 5.0.2
	 */
	public Task getTask() {
		return this.task;
	}

	/**
	 * 触发取消本定时任务。
	 * <p>若任务仍在运行，此变体将强制中断。
	 * @see #cancel(boolean)
	 */
	public void cancel() {
		cancel(true);
	}

	/**
	 * 触发取消本定时任务。
	 * @param mayInterruptIfRunning 若任务仍在运行是否强制中断
	 * （指定 {@code false} 允许任务完成）
	 * @since 5.3.18
	 * @see ScheduledFuture#cancel(boolean)
	 */
	public void cancel(boolean mayInterruptIfRunning) {
		ScheduledFuture<?> future = this.future;
		if (future != null) {
			future.cancel(mayInterruptIfRunning);
		}
	}

	/**
	 * 返回任务下次计划执行时间，若任务已取消或未安排新执行则返回 {@code null}。
	 * @since 6.2
	 */
	public @Nullable Instant nextExecution() {
		ScheduledFuture<?> future = this.future;
		if (future != null && !future.isCancelled()) {
			long delay = future.getDelay(TimeUnit.MILLISECONDS);
			if (delay > 0) {
				return Instant.now().plusMillis(delay);
			}
		}
		return null;
	}

	@Override
	public String toString() {
		return this.task.toString();
	}

}
