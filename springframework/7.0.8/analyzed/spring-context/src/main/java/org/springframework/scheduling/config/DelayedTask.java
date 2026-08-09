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
 * 定义带初始延迟的 {@code Runnable} 的 {@link Task} 实现。
 *
 * @author Juergen Hoeller
 * @since 6.1
 */
public class DelayedTask extends Task {

	private final Duration initialDelay;


	/**
	 * 创建新的 {@code DelayedTask}。
	 * @param runnable 要执行的底层任务
	 * @param initialDelay 任务执行前的初始延迟
	 */
	public DelayedTask(Runnable runnable, Duration initialDelay) {
		super(runnable);
		Assert.notNull(initialDelay, "InitialDelay must not be null");
		this.initialDelay = initialDelay;
	}

	/**
	 * 拷贝构造函数。
	 */
	DelayedTask(DelayedTask task) {
		super(task.getRunnable());
		Assert.notNull(task, "DelayedTask must not be null");
		this.initialDelay = task.getInitialDelayDuration();
	}


	/**
	 * 返回任务首次执行前的初始延迟。
	 */
	public Duration getInitialDelayDuration() {
		return this.initialDelay;
	}

}
