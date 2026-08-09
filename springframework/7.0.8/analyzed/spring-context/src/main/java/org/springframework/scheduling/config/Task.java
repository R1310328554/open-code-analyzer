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

import org.jspecify.annotations.Nullable;

import org.springframework.scheduling.SchedulingAwareRunnable;
import org.springframework.util.Assert;

/**
 * 定义作为任务执行的 {@code Runnable} 的持有者类，
 * 通常在计划时间或间隔执行。各种调度方式见子类层次结构。
 *
 * @author Chris Beams
 * @author Juergen Hoeller
 * @author Brian Clozel
 * @since 3.2
 */
public class Task {

	private final Runnable runnable;

	private TaskExecutionOutcome lastExecutionOutcome;


	/**
	 * 创建新的 {@code Task}。
	 * @param runnable 要执行的底层任务
	 */
	public Task(Runnable runnable) {
		Assert.notNull(runnable, "Runnable must not be null");
		this.runnable = new OutcomeTrackingRunnable(runnable);
		this.lastExecutionOutcome = TaskExecutionOutcome.create();
	}


	/**
	 * 返回执行底层任务的 {@link Runnable}。
	 * <p>注意，未必返回 {@link Task#Task(Runnable) 原始 runnable}，
	 * 框架可能为其包装以提供额外支持。
	 */
	public Runnable getRunnable() {
		return this.runnable;
	}

	/**
	 * 返回上次任务执行的结果。
	 * @since 6.2
	 */
	public TaskExecutionOutcome getLastExecutionOutcome() {
		return this.lastExecutionOutcome;
	}

	@Override
	public String toString() {
		return this.runnable.toString();
	}


	private class OutcomeTrackingRunnable implements SchedulingAwareRunnable {

		private final Runnable runnable;

		public OutcomeTrackingRunnable(Runnable runnable) {
			this.runnable = runnable;
		}

		@Override
		public void run() {
			try {
				Task.this.lastExecutionOutcome = Task.this.lastExecutionOutcome.start(Instant.now());
				this.runnable.run();
				Task.this.lastExecutionOutcome = Task.this.lastExecutionOutcome.success();
			}
			catch (Throwable exc) {
				Task.this.lastExecutionOutcome = Task.this.lastExecutionOutcome.failure(exc);
				throw exc;
			}
		}

		@Override
		public boolean isLongLived() {
			if (this.runnable instanceof SchedulingAwareRunnable sar) {
				return sar.isLongLived();
			}
			return SchedulingAwareRunnable.super.isLongLived();
		}

		@Override
		public @Nullable String getQualifier() {
			if (this.runnable instanceof SchedulingAwareRunnable sar) {
				return sar.getQualifier();
			}
			return SchedulingAwareRunnable.super.getQualifier();
		}

		@Override
		public String toString() {
			return this.runnable.toString();
		}
	}

}
