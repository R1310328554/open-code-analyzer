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

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Callable;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.RunnableScheduledFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.jspecify.annotations.Nullable;

import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.TaskDecorator;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.scheduling.SchedulingTaskExecutor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.support.TaskUtils;
import org.springframework.util.Assert;
import org.springframework.util.ErrorHandler;

/**
 * Spring {@link TaskScheduler} 接口的标准实现，
 * 包装原生 {@link java.util.concurrent.ScheduledThreadPoolExecutor} 并为其提供
 * 所有适用的配置选项。默认调度线程数为 1；
 * 可通过 {@link #setPoolSize} 配置更高数量。
 *
 * <p>这是 Spring 的传统调度器变体，尽可能贴近
 * {@link java.util.concurrent.ScheduledExecutorService} 语义。
 * 任务执行发生在调度线程上而非独立执行线程。
 * 因此 {@link ScheduledFuture} 句柄（例如来自 {@link #schedule(Runnable, Instant)}）
 * 表示所提供任务（或一系列重复任务）的实际完成。
 *
 * @author Juergen Hoeller
 * @author Mark Fisher
 * @since 3.0
 * @see #setPoolSize
 * @see #setRemoveOnCancelPolicy
 * @see #setContinueExistingPeriodicTasksAfterShutdownPolicy
 * @see #setExecuteExistingDelayedTasksAfterShutdownPolicy
 * @see #setThreadFactory
 * @see #setErrorHandler
 * @see ThreadPoolTaskExecutor
 * @see SimpleAsyncTaskScheduler
 */
@SuppressWarnings({"serial", "deprecation"})
public class ThreadPoolTaskScheduler extends ExecutorConfigurationSupport
		implements AsyncTaskExecutor, SchedulingTaskExecutor, TaskScheduler {

	private static final TimeUnit NANO = TimeUnit.NANOSECONDS;


	private volatile int poolSize = 1;

	private volatile boolean removeOnCancelPolicy;

	private volatile boolean continueExistingPeriodicTasksAfterShutdownPolicy;

	private volatile boolean executeExistingDelayedTasksAfterShutdownPolicy = true;

	private @Nullable TaskDecorator taskDecorator;

	private volatile @Nullable ErrorHandler errorHandler;

	private Clock clock = Clock.systemDefaultZone();

	private @Nullable ScheduledExecutorService scheduledExecutor;


	/**
	 * 设置 ScheduledExecutorService 的池大小。
	 * 默认为 1。
	 * <p><b>此设置可在运行时修改，例如通过 JMX。</b>
	 */
	public void setPoolSize(int poolSize) {
		Assert.isTrue(poolSize > 0, "'poolSize' must be 1 or higher");
		if (this.scheduledExecutor instanceof ScheduledThreadPoolExecutor threadPoolExecutor) {
			threadPoolExecutor.setCorePoolSize(poolSize);
		}
		this.poolSize = poolSize;
	}

	/**
	 * 在 {@link ScheduledThreadPoolExecutor} 上设置 cancel 时移除模式。
	 * <p>默认为 {@code false}。设为 {@code true} 时，
	 * 目标执行器将切换为 remove-on-cancel 模式（若可能）。
	 * <p><b>此设置可在运行时修改，例如通过 JMX。</b>
	 * @see ScheduledThreadPoolExecutor#setRemoveOnCancelPolicy
	 */
	public void setRemoveOnCancelPolicy(boolean flag) {
		if (this.scheduledExecutor instanceof ScheduledThreadPoolExecutor threadPoolExecutor) {
			threadPoolExecutor.setRemoveOnCancelPolicy(flag);
		}
		this.removeOnCancelPolicy = flag;
	}

	/**
	 * 设置执行器 shutdown 后是否继续现有周期性任务。
	 * <p>默认为 {@code false}。设为 {@code true} 时，
	 * 目标执行器将切换为继续周期性任务（若可能）。
	 * <p><b>此设置可在运行时修改，例如通过 JMX。</b>
	 * @since 5.3.9
	 * @see ScheduledThreadPoolExecutor#setContinueExistingPeriodicTasksAfterShutdownPolicy
	 */
	public void setContinueExistingPeriodicTasksAfterShutdownPolicy(boolean flag) {
		if (this.scheduledExecutor instanceof ScheduledThreadPoolExecutor threadPoolExecutor) {
			threadPoolExecutor.setContinueExistingPeriodicTasksAfterShutdownPolicy(flag);
		}
		this.continueExistingPeriodicTasksAfterShutdownPolicy = flag;
	}

	/**
	 * 设置执行器 shutdown 后是否执行现有延迟任务。
	 * <p>默认为 {@code true}。设为 {@code false} 时，
	 * 目标执行器将切换为丢弃剩余任务（若可能）。
	 * <p><b>此设置可在运行时修改，例如通过 JMX。</b>
	 * @since 5.3.9
	 * @see ScheduledThreadPoolExecutor#setExecuteExistingDelayedTasksAfterShutdownPolicy
	 */
	public void setExecuteExistingDelayedTasksAfterShutdownPolicy(boolean flag) {
		if (this.scheduledExecutor instanceof ScheduledThreadPoolExecutor threadPoolExecutor) {
			threadPoolExecutor.setExecuteExistingDelayedTasksAfterShutdownPolicy(flag);
		}
		this.executeExistingDelayedTasksAfterShutdownPolicy = flag;
	}

	/**
	 * 指定应用于即将执行的任意 {@link Runnable} 的自定义 {@link TaskDecorator}。
	 * <p>注意此类装饰器不应用于用户提供的 {@code Runnable}/{@code Callable}，
	 * 而是应用于调度执行回调（用户任务的包装）。
	 * <p>主要用例是在任务调用周围设置执行上下文，
	 * 或为任务执行提供监控/统计。
	 * @since 6.2
	 */
	public void setTaskDecorator(TaskDecorator taskDecorator) {
		this.taskDecorator = taskDecorator;
	}

	/**
	 * 设置自定义 {@link ErrorHandler} 策略。
	 */
	public void setErrorHandler(ErrorHandler errorHandler) {
		this.errorHandler = errorHandler;
	}

	/**
	 * 设置用于调度目的的时钟。
	 * <p>默认时钟为默认时区的系统时钟。
	 * @since 5.3
	 * @see Clock#systemDefaultZone()
	 */
	public void setClock(Clock clock) {
		Assert.notNull(clock, "Clock must not be null");
		this.clock = clock;
	}

	@Override
	public Clock getClock() {
		return this.clock;
	}


	@Override
	protected ExecutorService initializeExecutor(
			ThreadFactory threadFactory, RejectedExecutionHandler rejectedExecutionHandler) {

		this.scheduledExecutor = createExecutor(this.poolSize, threadFactory, rejectedExecutionHandler);

		if (this.scheduledExecutor instanceof ScheduledThreadPoolExecutor threadPoolExecutor) {
			if (this.removeOnCancelPolicy) {
				threadPoolExecutor.setRemoveOnCancelPolicy(true);
			}
			if (this.continueExistingPeriodicTasksAfterShutdownPolicy) {
				threadPoolExecutor.setContinueExistingPeriodicTasksAfterShutdownPolicy(true);
			}
			if (!this.executeExistingDelayedTasksAfterShutdownPolicy) {
				threadPoolExecutor.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
			}
		}

		return this.scheduledExecutor;
	}

	/**
	 * 创建新的 {@link ScheduledExecutorService} 实例。
	 * <p>默认实现创建 {@link ScheduledThreadPoolExecutor}。
	 * 子类可覆盖以提供自定义 {@link ScheduledExecutorService} 实例。
	 * @param poolSize 指定池大小
	 * @param threadFactory 使用的 ThreadFactory
	 * @param rejectedExecutionHandler 使用的 RejectedExecutionHandler
	 * @return 新的 ScheduledExecutorService 实例
	 * @see #afterPropertiesSet()
	 * @see java.util.concurrent.ScheduledThreadPoolExecutor
	 */
	protected ScheduledExecutorService createExecutor(
			int poolSize, ThreadFactory threadFactory, RejectedExecutionHandler rejectedExecutionHandler) {

		return new ScheduledThreadPoolExecutor(poolSize, threadFactory, rejectedExecutionHandler) {
			@Override
			protected void beforeExecute(Thread thread, Runnable task) {
				ThreadPoolTaskScheduler.this.beforeExecute(thread, task);
			}
			@Override
			protected void afterExecute(Runnable task, Throwable ex) {
				ThreadPoolTaskScheduler.this.afterExecute(task, ex);
			}
			@Override
			protected <V> RunnableScheduledFuture<V> decorateTask(Runnable runnable, RunnableScheduledFuture<V> task) {
				return decorateTaskIfNecessary(task);
			}
			@Override
			protected <V> RunnableScheduledFuture<V> decorateTask(Callable<V> callable, RunnableScheduledFuture<V> task) {
				return decorateTaskIfNecessary(task);
			}
		};
	}

	/**
	 * 返回底层 ScheduledExecutorService 以供原生访问。
	 * @return 底层 ScheduledExecutorService（永不为 {@code null}）
	 * @throws IllegalStateException 若 ThreadPoolTaskScheduler 尚未初始化
	 */
	public ScheduledExecutorService getScheduledExecutor() throws IllegalStateException {
		Assert.state(this.scheduledExecutor != null, "ThreadPoolTaskScheduler not initialized");
		return this.scheduledExecutor;
	}

	/**
	 * 返回底层 ScheduledThreadPoolExecutor（若可用）。
	 * @return 底层 ScheduledExecutorService（永不为 {@code null}）
	 * @throws IllegalStateException 若 ThreadPoolTaskScheduler 尚未初始化，
	 * 或底层 ScheduledExecutorService 不是 ScheduledThreadPoolExecutor
	 * @see #getScheduledExecutor()
	 */
	public ScheduledThreadPoolExecutor getScheduledThreadPoolExecutor() throws IllegalStateException {
		Assert.state(this.scheduledExecutor instanceof ScheduledThreadPoolExecutor,
				"No ScheduledThreadPoolExecutor available");
		return (ScheduledThreadPoolExecutor) this.scheduledExecutor;
	}

	/**
	 * 返回当前池大小。
	 * <p>需要底层 {@link ScheduledThreadPoolExecutor}。
	 * @see #getScheduledThreadPoolExecutor()
	 * @see java.util.concurrent.ScheduledThreadPoolExecutor#getPoolSize()
	 */
	public int getPoolSize() {
		if (this.scheduledExecutor == null) {
			// Not initialized yet: assume initial pool size.
			return this.poolSize;
		}
		return getScheduledThreadPoolExecutor().getPoolSize();
	}

	/**
	 * 返回当前活动线程数。
	 * <p>需要底层 {@link ScheduledThreadPoolExecutor}。
	 * @see #getScheduledThreadPoolExecutor()
	 * @see java.util.concurrent.ScheduledThreadPoolExecutor#getActiveCount()
	 */
	public int getActiveCount() {
		if (this.scheduledExecutor == null) {
			// Not initialized yet: assume no active threads.
			return 0;
		}
		return getScheduledThreadPoolExecutor().getActiveCount();
	}

	/**
	 * 返回 cancel 时移除模式的当前设置。
	 * <p>需要底层 {@link ScheduledThreadPoolExecutor}。
	 * @deprecated 请直接使用 {@link #getScheduledThreadPoolExecutor()} 访问
	 */
	@Deprecated(since = "5.3.9")
	public boolean isRemoveOnCancelPolicy() {
		if (this.scheduledExecutor == null) {
			// Not initialized yet: return our setting for the time being.
			return this.removeOnCancelPolicy;
		}
		return getScheduledThreadPoolExecutor().getRemoveOnCancelPolicy();
	}


	// SchedulingTaskExecutor implementation

	@Override
	public void execute(Runnable task) {
		Executor executor = getScheduledExecutor();
		try {
			executor.execute(errorHandlingTask(task, false));
		}
		catch (RejectedExecutionException ex) {
			throw new TaskRejectedException(executor, task, ex);
		}
	}

	@Override
	public Future<?> submit(Runnable task) {
		ExecutorService executor = getScheduledExecutor();
		try {
			return executor.submit(errorHandlingTask(task, false));
		}
		catch (RejectedExecutionException ex) {
			throw new TaskRejectedException(executor, task, ex);
		}
	}

	@Override
	public <T> Future<T> submit(Callable<T> task) {
		ExecutorService executor = getScheduledExecutor();
		try {
			return executor.submit(new DelegatingErrorHandlingCallable<>(task, this.errorHandler));
		}
		catch (RejectedExecutionException ex) {
			throw new TaskRejectedException(executor, task, ex);
		}
	}


	// TaskScheduler implementation

	@Override
	public @Nullable ScheduledFuture<?> schedule(Runnable task, Trigger trigger) {
		ScheduledExecutorService executor = getScheduledExecutor();
		try {
			ErrorHandler errorHandler = this.errorHandler;
			if (errorHandler == null) {
				errorHandler = TaskUtils.getDefaultErrorHandler(true);
			}
			return new ReschedulingRunnable(task, trigger, this.clock, executor, errorHandler).schedule();
		}
		catch (RejectedExecutionException ex) {
			throw new TaskRejectedException(executor, task, ex);
		}
	}

	@Override
	public ScheduledFuture<?> schedule(Runnable task, Instant startTime) {
		ScheduledExecutorService executor = getScheduledExecutor();
		Duration delay = Duration.between(this.clock.instant(), startTime);
		try {
			return executor.schedule(errorHandlingTask(task, false), NANO.convert(delay), NANO);
		}
		catch (RejectedExecutionException ex) {
			throw new TaskRejectedException(executor, task, ex);
		}
	}

	@Override
	public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, Instant startTime, Duration period) {
		ScheduledExecutorService executor = getScheduledExecutor();
		Duration initialDelay = Duration.between(this.clock.instant(), startTime);
		try {
			return executor.scheduleAtFixedRate(errorHandlingTask(task, true),
					NANO.convert(initialDelay), NANO.convert(period), NANO);
		}
		catch (RejectedExecutionException ex) {
			throw new TaskRejectedException(executor, task, ex);
		}
	}

	@Override
	public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, Duration period) {
		ScheduledExecutorService executor = getScheduledExecutor();
		try {
			return executor.scheduleAtFixedRate(errorHandlingTask(task, true),
					0, NANO.convert(period), NANO);
		}
		catch (RejectedExecutionException ex) {
			throw new TaskRejectedException(executor, task, ex);
		}
	}

	@Override
	public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, Instant startTime, Duration delay) {
		ScheduledExecutorService executor = getScheduledExecutor();
		Duration initialDelay = Duration.between(this.clock.instant(), startTime);
		try {
			return executor.scheduleWithFixedDelay(errorHandlingTask(task, true),
					NANO.convert(initialDelay), NANO.convert(delay), NANO);
		}
		catch (RejectedExecutionException ex) {
			throw new TaskRejectedException(executor, task, ex);
		}
	}

	@Override
	public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, Duration delay) {
		ScheduledExecutorService executor = getScheduledExecutor();
		try {
			return executor.scheduleWithFixedDelay(errorHandlingTask(task, true),
					0, NANO.convert(delay), NANO);
		}
		catch (RejectedExecutionException ex) {
			throw new TaskRejectedException(executor, task, ex);
		}
	}


	private <V> RunnableScheduledFuture<V> decorateTaskIfNecessary(RunnableScheduledFuture<V> future) {
		return (this.taskDecorator != null ? new DelegatingRunnableScheduledFuture<>(future, this.taskDecorator) :
				future);
	}

	private Runnable errorHandlingTask(Runnable task, boolean isRepeatingTask) {
		return TaskUtils.decorateTaskWithErrorHandler(task, this.errorHandler, isRepeatingTask);
	}


	private static class DelegatingRunnableScheduledFuture<V> implements RunnableScheduledFuture<V> {

		private final RunnableScheduledFuture<V> future;

		private final Runnable decoratedRunnable;

		public DelegatingRunnableScheduledFuture(RunnableScheduledFuture<V> future, TaskDecorator taskDecorator) {
			this.future = future;
			this.decoratedRunnable = taskDecorator.decorate(this.future);
		}

		@Override
		public void run() {
			this.decoratedRunnable.run();
		}

		@Override
		public boolean cancel(boolean mayInterruptIfRunning) {
			return this.future.cancel(mayInterruptIfRunning);
		}

		@Override
		public boolean isCancelled() {
			return this.future.isCancelled();
		}

		@Override
		public boolean isDone() {
			return this.future.isDone();
		}

		@Override
		public V get() throws InterruptedException, ExecutionException {
			return this.future.get();
		}

		@Override
		public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
			return this.future.get(timeout, unit);
		}

		@Override
		public boolean isPeriodic() {
			return this.future.isPeriodic();
		}

		@Override
		public long getDelay(TimeUnit unit) {
			return this.future.getDelay(unit);
		}

		@Override
		public int compareTo(Delayed o) {
			return this.future.compareTo(o);
		}
	}

}
