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

import org.jspecify.annotations.Nullable;

/**
 * {@link Runnable} 接口的扩展，为长时间运行操作添加特殊回调。
 *
 * <p>建议具备调度能力的 TaskExecutor 检查提交的 Runnable，
 * 检测是否实现本接口并尽可能作出相应处理。
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see org.springframework.core.task.TaskExecutor
 * @see SchedulingTaskExecutor
 */
public interface SchedulingAwareRunnable extends Runnable {

	/**
	 * 返回 Runnable 的操作是否为长生命周期（{@code true}）而非短生命周期（{@code false}）。
	 * <p>前者情况下，任务不会从线程池（若有）分配线程，
	 * 而是视为长时间运行的后台线程。
	 * <p>这应视为提示。TaskExecutor 实现当然可忽略此标志及 SchedulingAwareRunnable 接口本身。
	 * <p>默认实现返回 {@code false}（自 6.1 起）。
	 */
	default boolean isLongLived() {
		return false;
	}

	/**
	 * 返回与此 Runnable 关联的限定符。
	 * <p>默认实现返回 {@code null}。
	 * <p>可根据调度器实现用于自定义目的。
	 * {@link org.springframework.scheduling.config.TaskSchedulerRouter} 内省此限定符，
	 * 以确定给定 Runnable 应使用的目标调度器，
	 * 匹配特定 {@link org.springframework.scheduling.TaskScheduler} 或
	 * {@link java.util.concurrent.ScheduledExecutorService} Bean 定义的限定符值（或 Bean 名称）。
	 * @since 6.1
	 * @see org.springframework.scheduling.annotation.Scheduled#scheduler()
	 */
	default @Nullable String getQualifier() {
		return null;
	}

}
