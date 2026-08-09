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
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.support.DelegatingErrorHandlingRunnable;
import org.springframework.scheduling.support.TaskUtils;
import org.springframework.util.Assert;
import org.springframework.util.ErrorHandler;

/**
 * Spring {@link TaskScheduler} 接口的简单实现，
 * 使用单个调度线程并在各自独立线程中执行每个定时任务。
 * 在 JDK 21 虚拟线程场景下是颇具吸引力的选择，
 * 预期常见用法为 {@link #setVirtualThreads setVirtualThreads(true)}。
 *
 * <p><b>注意：固定延迟调度强制在单个调度线程上执行，
 * 以提供传统固定延迟语义！</b>
 * 更推荐使用固定速率或 cron 触发器，
 * 它们更适合这种每任务一线程的调度器变体。
 *
 * <p>通过 {@link #setTaskTerminationTimeout} 支持优雅关闭，
 * 代价是运行时每个执行线程的任务跟踪开销。
 * 通过 {@link #setConcurrencyLimit} 支持限制并发线程数。
 * 默认并发任务执行数无限制。
 * 这允许定时任务执行的动态并发，
 * 与需要固定池大小的 {@link ThreadPoolTaskScheduler} 形成对比。
 *
 * <p><b>注意：本实现不重用线程！</b>请考虑基于线程池的 TaskScheduler 实现，
 * 尤其用于调度大量短生命周期任务。或在 JDK 21 上
 * 考虑将 {@link #setVirtualThreads} 设为 {@code true}。
 *
 * <p>继承 {@link SimpleAsyncTaskExecutor}，可完全替代它，
 * 例如作为同时充当 {@link org.springframework.core.task.TaskExecutor}
 * 与 {@link TaskScheduler} 的单一共享实例。
 * 其他执行器/调度器实现通常对调度线程池有特定约束，
 * 实践中往往需要单独线程池用于一般执行目的，本类一般并非如此。
 *
 * <p><b>注意：本调度器变体不跟踪任务的实际完成，
 * 而仅跟踪移交给执行线程。</b>因此
 * {@link ScheduledFuture} 句柄（例如来自 {@link #schedule(Runnable, Instant)}）
 * 表示该移交而非所提供任务（或一系列重复任务）的实际完成。
 * 此外，本调度器仅有限参与生命周期管理，
 * 停止触发器触发与固定延迟任务执行，但不停止已移交任务的执行。
 *
 * <p>作为内置每任务一线程能力的替代，
 * 本调度器也可通过 {@link #setTargetTaskExecutor} 配置
 * 用于定时任务执行的独立目标执行器：例如指向共享
 * {@link ThreadPoolTaskExecutor} Bean。这与
 * {@link ThreadPoolTaskScheduler} 配置仍相当不同，
 * 因其始终使用单个调度线程，
 * 同时动态分派到可能具有动态 core/max 池大小范围、
 * 参与共享并发限制的目标线程池。
 *
 * @author Juergen Hoeller
 * @since 6.1
 * @see #setVirtualThreads
 * @see #setTaskTerminationTimeout
 * @see #setConcurrencyLimit
 * @see SimpleAsyncTaskExecutor
 * @see ThreadPoolTaskScheduler
 */
@SuppressWarnings("serial")
public class SimpleAsyncTaskScheduler extends SimpleAsyncTaskExecutor implements TaskScheduler,
		ApplicationContextAware, SmartLifecycle, ApplicationListener<ContextClosedEvent> {

	/**
	 * 执行器 {@link SmartLifecycle} 的默认阶段：{@code Integer.MAX_VALUE / 2}。
	 * @since 6.2
	 * @see #getPhase()
	 * @see ExecutorConfigurationSupport#DEFAULT_PHASE
	 */
	public static final int DEFAULT_PHASE = ExecutorConfigurationSupport.DEFAULT_PHASE;

	private static final TimeUnit NANO = TimeUnit.NANOSECONDS;


	private final ScheduledExecutorService triggerExecutor = createScheduledExecutor();

	private final ExecutorLifecycleDelegate triggerLifecycle = new ExecutorLifecycleDelegate(this.triggerExecutor);

	private final ScheduledExecutorService fixedDelayExecutor = createFixedDelayExecutor();

	private final ExecutorLifecycleDelegate fixedDelayLifecycle = new ExecutorLifecycleDelegate(this.fixedDelayExecutor);

	private @Nullable ErrorHandler errorHandler;

	private Clock clock = Clock.systemDefaultZone();

	private int phase = DEFAULT_PHASE;

	private @Nullable Executor targetTaskExecutor;

	private @Nullable ApplicationContext applicationContext;


	/**
	 * 提供 {@link ErrorHandler} 策略。
	 * @since 6.2
	 */
	public void setErrorHandler(ErrorHandler errorHandler) {
		Assert.notNull(errorHandler, "ErrorHandler must not be null");
		this.errorHandler = errorHandler;
	}

	/**
	 * 设置用于调度目的的时钟。
	 * <p>默认时钟为默认时区的系统时钟。
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

	/**
	 * 指定暂停与恢复本执行器的生命周期阶段。
	 * 默认为 {@link #DEFAULT_PHASE}。
	 * @see SmartLifecycle#getPhase()
	 */
	public void setPhase(int phase) {
		this.phase = phase;
	}

	/**
	 * 返回暂停与恢复本执行器的生命周期阶段。
	 * @see #setPhase
	 */
	@Override
	public int getPhase() {
		return this.phase;
	}

	/**
	 * 指定用于定时任务各自执行时委托的自定义目标 {@link Executor}。
	 * 例如可设为执行定时任务的独立线程池，
	 * 而本调度器仍使用其单个调度线程。
	 * <p>若未设置，则启用常规 {@link SimpleAsyncTaskExecutor}
	 * 安排，每个任务新建线程。
	 */
	public void setTargetTaskExecutor(Executor targetTaskExecutor) {
		this.targetTaskExecutor = (targetTaskExecutor == this ? null : targetTaskExecutor);
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}


	private ScheduledExecutorService createScheduledExecutor() {
		return new ScheduledThreadPoolExecutor(1, this::newThread) {
			@Override
			protected void beforeExecute(Thread thread, Runnable task) {
				triggerLifecycle.beforeExecute(thread);
			}
			@Override
			protected void afterExecute(Runnable task, Throwable ex) {
				triggerLifecycle.afterExecute();
			}
		};
	}

	private ScheduledExecutorService createFixedDelayExecutor() {
		return new ScheduledThreadPoolExecutor(1, this::newThread) {
			@Override
			protected void beforeExecute(Thread thread, Runnable task) {
				fixedDelayLifecycle.beforeExecute(thread);
			}
			@Override
			protected void afterExecute(Runnable task, Throwable ex) {
				fixedDelayLifecycle.afterExecute();
			}
		};
	}

	@Override
	protected void doExecute(Runnable task) {
		if (this.targetTaskExecutor != null) {
			this.targetTaskExecutor.execute(task);
		}
		else {
			super.doExecute(task);
		}
	}

	private Runnable taskOnSchedulerThread(Runnable task) {
		return new DelegatingErrorHandlingRunnable(task,
				(this.errorHandler != null ? this.errorHandler : TaskUtils.getDefaultErrorHandler(true)));
	}

	private Runnable scheduledTask(Runnable task) {
		return () -> execute(new DelegatingErrorHandlingRunnable(task, this::shutdownAwareErrorHandler));
	}

	private void shutdownAwareErrorHandler(Throwable ex) {
		if (this.errorHandler != null) {
			this.errorHandler.handleError(ex);
		}
		else if (this.triggerExecutor.isShutdown()) {
			LogFactory.getLog(getClass()).debug("Ignoring scheduled task exception after shutdown", ex);
		}
		else {
			TaskUtils.getDefaultErrorHandler(true).handleError(ex);
		}
	}


	@Override
	public void execute(Runnable task) {
		super.execute(TaskUtils.decorateTaskWithErrorHandler(task, this.errorHandler, false));
	}

	@Override
	public Future<?> submit(Runnable task) {
		return super.submit(TaskUtils.decorateTaskWithErrorHandler(task, this.errorHandler, false));
	}

	@Override
	public <T> Future<T> submit(Callable<T> task) {
		return super.submit(new DelegatingErrorHandlingCallable<>(task, this.errorHandler));
	}

	@Override
	public @Nullable ScheduledFuture<?> schedule(Runnable task, Trigger trigger) {
		try {
			Runnable delegate = scheduledTask(task);
			ErrorHandler errorHandler =
					(this.errorHandler != null ? this.errorHandler : TaskUtils.getDefaultErrorHandler(true));
			return new ReschedulingRunnable(
					delegate, trigger, this.clock, this.triggerExecutor, errorHandler).schedule();
		}
		catch (RejectedExecutionException ex) {
			throw new TaskRejectedException(this.triggerExecutor, task, ex);
		}
	}

	@Override
	public ScheduledFuture<?> schedule(Runnable task, Instant startTime) {
		Duration delay = Duration.between(this.clock.instant(), startTime);
		try {
			return this.triggerExecutor.schedule(scheduledTask(task), NANO.convert(delay), NANO);
		}
		catch (RejectedExecutionException ex) {
			throw new TaskRejectedException(this.triggerExecutor, task, ex);
		}
	}

	@Override
	public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, Instant startTime, Duration period) {
		Duration initialDelay = Duration.between(this.clock.instant(), startTime);
		try {
			return this.triggerExecutor.scheduleAtFixedRate(scheduledTask(task),
					NANO.convert(initialDelay), NANO.convert(period), NANO);
		}
		catch (RejectedExecutionException ex) {
			throw new TaskRejectedException(this.triggerExecutor, task, ex);
		}
	}

	@Override
	public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, Duration period) {
		try {
			return this.triggerExecutor.scheduleAtFixedRate(scheduledTask(task),
					0, NANO.convert(period), NANO);
		}
		catch (RejectedExecutionException ex) {
			throw new TaskRejectedException(this.triggerExecutor, task, ex);
		}
	}

	@Override
	public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, Instant startTime, Duration delay) {
		Duration initialDelay = Duration.between(this.clock.instant(), startTime);
		try {
			// Blocking task on scheduler thread for fixed delay semantics
			return this.fixedDelayExecutor.scheduleWithFixedDelay(taskOnSchedulerThread(task),
					NANO.convert(initialDelay), NANO.convert(delay), NANO);
		}
		catch (RejectedExecutionException ex) {
			throw new TaskRejectedException(this.fixedDelayExecutor, task, ex);
		}
	}

	@Override
	public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, Duration delay) {
		try {
			// Blocking task on scheduler thread for fixed delay semantics
			return this.fixedDelayExecutor.scheduleWithFixedDelay(taskOnSchedulerThread(task),
					0, NANO.convert(delay), NANO);
		}
		catch (RejectedExecutionException ex) {
			throw new TaskRejectedException(this.fixedDelayExecutor, task, ex);
		}
	}


	@Override
	public void start() {
		this.triggerLifecycle.start();
		this.fixedDelayLifecycle.start();
	}

	@Override
	public void stop() {
		this.triggerLifecycle.stop();
		this.fixedDelayLifecycle.stop();
	}

	@Override
	public void stop(Runnable callback) {
		this.triggerLifecycle.stop();  // no callback necessary since it's just triggers with hand-offs
		this.fixedDelayLifecycle.stop(callback);  // callback for currently executing fixed-delay tasks
	}

	@Override
	public boolean isRunning() {
		return (this.triggerLifecycle.isRunning() || this.fixedDelayLifecycle.isRunning());
	}

	@Override
	public void onApplicationEvent(ContextClosedEvent event) {
		if (event.getApplicationContext() == this.applicationContext) {
			this.triggerExecutor.shutdown();
			this.fixedDelayExecutor.shutdown();
		}
	}

	@Override
	public void close() {
		for (Runnable remainingTask : this.triggerExecutor.shutdownNow()) {
			if (remainingTask instanceof Future<?> future) {
				future.cancel(true);
			}
		}
		for (Runnable remainingTask : this.fixedDelayExecutor.shutdownNow()) {
			if (remainingTask instanceof Future<?> future) {
				future.cancel(true);
			}
		}
		super.close();
	}

}
