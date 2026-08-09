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

import org.springframework.core.task.AsyncTaskExecutor;

/**
 * 暴露与潜在任务提交者相关的调度特征的
 * {@link org.springframework.core.task.TaskExecutor} 扩展。
 *
 * <p>建议调度客户端提交与所用 {@code TaskExecutor} 实现
 * 所暴露偏好相匹配的 {@link Runnable Runnables}。
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see SchedulingAwareRunnable
 * @see org.springframework.core.task.TaskExecutor
 */
public interface SchedulingTaskExecutor extends AsyncTaskExecutor {

	/**
	 * 此 {@code TaskExecutor} 是否偏好短生命周期任务而非长生命周期任务？
	 * <p>{@code SchedulingTaskExecutor} 实现可指示是否偏好提交的任务
	 * 在单次任务执行中尽可能少做工作。例如，提交的任务可将重复循环拆分为
	 * 独立子任务，随后再提交后续任务（若可行）。
	 * <p>这应视为提示。{@code TaskExecutor} 客户端当然可忽略此标志及
	 * {@code SchedulingTaskExecutor} 接口本身。不过线程池通常会表明
	 * 偏好短生命周期任务，以实现更细粒度的调度。
	 * @return 若此执行器偏好短生命周期任务（默认）则返回 {@code true}，
	 * 否则返回 {@code false}（按常规 {@code TaskExecutor} 处理）
	 */
	default boolean prefersShortLivedTasks() {
		return true;
	}

}
