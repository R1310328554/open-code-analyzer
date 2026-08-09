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

import org.springframework.util.Assert;

/**
 * {@link Task} 执行的结果。
 *
 * @author Brian Clozel
 * @since 6.2
 * @param executionTime 任务开始执行的瞬间，未开始则为 {@code null}
 * @param status 执行结果的 {@link Status}
 * @param throwable 任务执行抛出的异常（若有）
 */
public record TaskExecutionOutcome(@Nullable Instant executionTime, Status status, @Nullable Throwable throwable) {

	TaskExecutionOutcome start(Instant executionTime) {
		return new TaskExecutionOutcome(executionTime, Status.STARTED, null);
	}

	TaskExecutionOutcome success() {
		Assert.state(this.executionTime != null, "Task has not been started yet");
		return new TaskExecutionOutcome(this.executionTime, Status.SUCCESS, null);
	}

	TaskExecutionOutcome failure(Throwable throwable) {
		Assert.state(this.executionTime != null, "Task has not been started yet");
		return new TaskExecutionOutcome(this.executionTime, Status.ERROR, throwable);
	}

	static TaskExecutionOutcome create() {
		return new TaskExecutionOutcome(null, Status.NONE, null);
	}


	/**
	 * 任务执行结果的状态。
	 */
	public enum Status {

		/**
		 * 任务尚未执行。
		 */
		NONE,

		/**
		 * 任务执行已开始且进行中。
		 */
		STARTED,

		/**
		 * 任务执行已成功完成。
		 */
		SUCCESS,

		/**
		 * 任务执行以错误结束。
		 */
		ERROR

	}

}
