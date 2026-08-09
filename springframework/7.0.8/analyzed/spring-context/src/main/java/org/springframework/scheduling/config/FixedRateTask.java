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

/**
 * 用于固定速率语义的 {@link IntervalTask} 特化。
 *
 * @author Juergen Hoeller
 * @author Arjen Poutsma
 * @since 5.0.2
 * @see org.springframework.scheduling.annotation.Scheduled#fixedRate()
 * @see ScheduledTaskRegistrar#addFixedRateTask(IntervalTask)
 */
public class FixedRateTask extends IntervalTask {

	/**
	 * 创建新的 {@code FixedRateTask}。
	 * @param runnable 要执行的底层任务
	 * @param interval 任务执行间隔（毫秒）
	 * @param initialDelay 任务首次执行前的初始延迟
	 * @deprecated 自 6.0 起，请改用 {@link #FixedRateTask(Runnable, Duration, Duration)}
	 */
	@Deprecated(since = "6.0")
	public FixedRateTask(Runnable runnable, long interval, long initialDelay) {
		super(runnable, interval, initialDelay);
	}

	/**
	 * 创建新的 {@code FixedRateTask}。
	 * @param runnable 要执行的底层任务
	 * @param interval 任务执行间隔
	 * @param initialDelay 任务首次执行前的初始延迟
	 * @since 6.0
	 */
	public FixedRateTask(Runnable runnable, Duration interval, Duration initialDelay) {
		super(runnable, interval, initialDelay);
	}

	FixedRateTask(IntervalTask task) {
		super(task);
	}

}
