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

package org.springframework.scheduling.concurrent;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import jakarta.enterprise.concurrent.ManagedExecutors;
import jakarta.enterprise.concurrent.ManagedTask;
import org.jspecify.annotations.Nullable;

import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.TaskDecorator;
import org.springframework.core.task.support.TaskExecutorAdapter;
import org.springframework.scheduling.SchedulingAwareRunnable;
import org.springframework.scheduling.SchedulingTaskExecutor;
import org.springframework.util.ClassUtils;

/**
 * 接受 {@code java.util.concurrent.Executor} 并为其暴露
 * Spring {@link org.springframework.core.task.TaskExecutor} 的适配器。
 * 也检测扩展的 {@code java.util.concurrent.ExecutorService}，
 * 相应适配 {@link org.springframework.core.task.AsyncTaskExecutor} 接口。
 *
 * <p>自动检测 JSR-236 {@link jakarta.enterprise.concurrent.ManagedExecutorService}，
 * 为其暴露 {@link jakarta.enterprise.concurrent.ManagedTask} 适配器，
 * 基于 {@link SchedulingAwareRunnable} 提供 long-running 提示，
 * 基于给定 Runnable/Callable 的 {@code toString()} 提供 identity 名称。
 * 在 Jakarta EE 环境中进行 JSR-236 风格查找，请考虑 {@link DefaultManagedTaskExecutor}。
 *
 * <p>注意存在预构建的 {@link ThreadPoolTaskExecutor}，
 * 允许以 Bean 风格定义 {@link java.util.concurrent.ThreadPoolExecutor}，
 * 直接暴露为 Spring {@link org.springframework.core.task.TaskExecutor}。
 * 这比原始 ThreadPoolExecutor 定义加单独本适配器类定义更方便。
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see java.util.concurrent.Executor
 * @see java.util.concurrent.ExecutorService
 * @see java.util.concurrent.ThreadPoolExecutor
 * @see java.util.concurrent.Executors
 * @see DefaultManagedTaskExecutor
 * @see ThreadPoolTaskExecutor
 */
@SuppressWarnings("deprecation")
public class ConcurrentTaskExecutor implements AsyncTaskExecutor, SchedulingTaskExecutor {

	private static final Executor STUB_EXECUTOR = (task -> {
		throw new IllegalStateException("Executor not configured");
	});

	private static @Nullable Class<?> managedExecutorServiceClass;

	static {
		try {
			managedExecutorServiceClass = ClassUtils.forName(
					"jakarta.enterprise.concurrent.ManagedExecutorService",
					ConcurrentTaskScheduler.class.getClassLoader());
		}
		catch (ClassNotFoundException ex) {
			// JSR-236 API not available...
			managedExecutorServiceClass = null;
		}
	}


	private Executor concurrentExecutor = STUB_EXECUTOR;

	private TaskExecutorAdapter adaptedExecutor = new TaskExecutorAdapter(STUB_EXECUTOR);

	private @Nullable TaskDecorator taskDecorator;


	/**
	 * 创建新的 ConcurrentTaskExecutor，默认使用单线程执行器。
	 * @see java.util.concurrent.Executors#newSingleThreadExecutor()
	 * @deprecated 请使用带外部提供 Executor 的 {@link #ConcurrentTaskExecutor(Executor)}
	 */
	@Deprecated(since = "6.1")
	public ConcurrentTaskExecutor() {
		this.concurrentExecutor = Executors.newSingleThreadExecutor();
		this.adaptedExecutor = new TaskExecutorAdapter(this.concurrentExecutor);
	}

	/**
	 * 使用给定 {@link java.util.concurrent.Executor} 创建新的 ConcurrentTaskExecutor。
	 * <p>自动检测 JSR-236 {@link jakarta.enterprise.concurrent.ManagedExecutorService}，
	 * 为其暴露 {@link jakarta.enterprise.concurrent.ManagedTask} 适配器。
	 * @param executor 委托的 {@link java.util.concurrent.Executor}
	 */
	public ConcurrentTaskExecutor(@Nullable Executor executor) {
		if (executor != null) {
			setConcurrentExecutor(executor);
		}
	}


	/**
	 * 指定委托的 {@link java.util.concurrent.Executor}。
	 * <p>自动检测 JSR-236 {@link jakarta.enterprise.concurrent.ManagedExecutorService}，
	 * 为其暴露 {@link jakarta.enterprise.concurrent.ManagedTask} 适配器。
	 */
	public final void setConcurrentExecutor(Executor executor) {
		this.concurrentExecutor = executor;
		this.adaptedExecutor = getAdaptedExecutor(this.concurrentExecutor);
	}

	/**
	 * 返回本适配器委托的 {@link java.util.concurrent.Executor}。
	 */
	public final Executor getConcurrentExecutor() {
		return this.concurrentExecutor;
	}

	/**
	 * 指定应用于即将执行的 {@link Runnable} 的自定义 {@link TaskDecorator}。
	 * <p>注意装饰器不一定应用于用户提供的 {@code Runnable}/{@code Callable}，
	 * 而是实际执行回调（可能是用户任务的包装）。
	 * <p>主要用例是在任务调用周围设置执行上下文，或提供任务执行监控/统计。
	 * @since 4.3
	 */
	public final void setTaskDecorator(TaskDecorator taskDecorator) {
		this.taskDecorator = taskDecorator;
		this.adaptedExecutor.setTaskDecorator(taskDecorator);
	}


	@Override
	public void execute(Runnable task) {
		this.adaptedExecutor.execute(task);
	}

	@Deprecated(since = "5.3.16")
	@Override
	public void execute(Runnable task, long startTimeout) {
		this.adaptedExecutor.execute(task, startTimeout);
	}

	@Override
	public Future<?> submit(Runnable task) {
		return this.adaptedExecutor.submit(task);
	}

	@Override
	public <T> Future<T> submit(Callable<T> task) {
		return this.adaptedExecutor.submit(task);
	}


	private TaskExecutorAdapter getAdaptedExecutor(Executor originalExecutor) {
		TaskExecutorAdapter adapter =
				(managedExecutorServiceClass != null && managedExecutorServiceClass.isInstance(originalExecutor) ?
						new ManagedTaskExecutorAdapter(originalExecutor) : new TaskExecutorAdapter(originalExecutor));
		if (this.taskDecorator != null) {
			adapter.setTaskDecorator(this.taskDecorator);
		}
		return adapter;
	}

	Runnable decorateTaskIfNecessary(Runnable task) {
		return (this.taskDecorator != null ? this.taskDecorator.decorate(task) : task);
	}


	/**
	 * TaskExecutorAdapter 子类，将所有提供的 Runnable 与 Callable
	 * 包装为 JSR-236 ManagedTask，基于 {@link SchedulingAwareRunnable} 提供 long-running 提示，
	 * 基于任务 {@code toString()} 表示提供 identity 名称。
	 */
	private static class ManagedTaskExecutorAdapter extends TaskExecutorAdapter {

		public ManagedTaskExecutorAdapter(Executor concurrentExecutor) {
			super(concurrentExecutor);
		}

		@Override
		public void execute(Runnable task) {
			super.execute(ManagedTaskBuilder.buildManagedTask(task, task.toString()));
		}

		@Override
		public Future<?> submit(Runnable task) {
			return super.submit(ManagedTaskBuilder.buildManagedTask(task, task.toString()));
		}

		@Override
		public <T> Future<T> submit(Callable<T> task) {
			return super.submit(ManagedTaskBuilder.buildManagedTask(task, task.toString()));
		}
	}


	/**
	 * 将给定 Runnable/Callable 包装为 JSR-236 ManagedTask 的委托，
	 * 基于 {@link SchedulingAwareRunnable} 提供 long-running 提示及给定 identity 名称。
	 */
	protected static class ManagedTaskBuilder {

		public static Runnable buildManagedTask(Runnable task, String identityName) {
			Map<String, String> properties;
			if (task instanceof SchedulingAwareRunnable schedulingAwareRunnable) {
				properties = new HashMap<>(4);
				properties.put(ManagedTask.LONGRUNNING_HINT,
						Boolean.toString(schedulingAwareRunnable.isLongLived()));
			}
			else {
				properties = new HashMap<>(2);
			}
			properties.put(ManagedTask.IDENTITY_NAME, identityName);
			return ManagedExecutors.managedTask(task, properties, null);
		}

		public static <T> Callable<T> buildManagedTask(Callable<T> task, String identityName) {
			Map<String, String> properties = new HashMap<>(2);
			properties.put(ManagedTask.IDENTITY_NAME, identityName);
			return ManagedExecutors.managedTask(task, properties, null);
		}
	}

}
