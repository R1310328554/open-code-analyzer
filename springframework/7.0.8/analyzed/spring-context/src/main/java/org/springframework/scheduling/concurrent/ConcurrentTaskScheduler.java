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
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import jakarta.enterprise.concurrent.LastExecution;
import jakarta.enterprise.concurrent.ManagedScheduledExecutorService;
import org.jspecify.annotations.Nullable;

import org.springframework.core.task.TaskRejectedException;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.support.TaskUtils;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ErrorHandler;

/**
 * 接受 {@code java.util.concurrent.ScheduledExecutorService} 并为其暴露
 * Spring {@link org.springframework.scheduling.TaskScheduler} 的适配器。
 * 扩展 {@link ConcurrentTaskExecutor} 以同时实现
 * {@link org.springframework.scheduling.SchedulingTaskExecutor} 接口。
 *
 * <p>自动检测 JSR-236 {@link jakarta.enterprise.concurrent.ManagedScheduledExecutorService}，
 * 若可能则用于基于 Trigger 的调度，而非 Spring 本地 Trigger 管理
 * （最终委托给 {@code java.util.concurrent.ScheduledExecutorService} API 的常规延迟调度）。
 * 在 Jakarta EE 环境中进行 JSR-236 风格查找，请考虑 {@link DefaultManagedTaskScheduler}。
 *
 * <p>注意存在预构建的 {@link ThreadPoolTaskScheduler}，
 * 允许以 Bean 风格定义 {@link java.util.concurrent.ScheduledThreadPoolExecutor}，
 * 直接暴露为 Spring {@link org.springframework.scheduling.TaskScheduler}。
 * 这比原始 ScheduledThreadPoolExecutor 定义加单独本适配器类定义更方便。
 *
 * @author Juergen Hoeller
 * @author Mark Fisher
 * @author Arjen Poutsma
 * @since 3.0
 * @see java.util.concurrent.ScheduledExecutorService
 * @see java.util.concurrent.ScheduledThreadPoolExecutor
 * @see java.util.concurrent.Executors
 * @see DefaultManagedTaskScheduler
 * @see ThreadPoolTaskScheduler
 */
public class ConcurrentTaskScheduler extends ConcurrentTaskExecutor implements TaskScheduler {

	private static final TimeUnit NANO = TimeUnit.NANOSECONDS;


	private static @Nullable Class<?> managedScheduledExecutorServiceClass;

	static {
		try {
			managedScheduledExecutorServiceClass = ClassUtils.forName(
					"jakarta.enterprise.concurrent.ManagedScheduledExecutorService",
					ConcurrentTaskScheduler.class.getClassLoader());
		}
		catch (ClassNotFoundException ex) {
			// JSR-236 API not available...
			managedScheduledExecutorServiceClass = null;
		}
	}


	private @Nullable ScheduledExecutorService scheduledExecutor;

	private boolean enterpriseConcurrentScheduler = false;

	private @Nullable ErrorHandler errorHandler;

	private Clock clock = Clock.systemDefaultZone();


	/**
	 * 创建新的 ConcurrentTaskScheduler，默认使用单线程执行器。
	 * @see java.util.concurrent.Executors#newSingleThreadScheduledExecutor()
	 * @deprecated 请使用带外部提供 Executor 的
	 * {@link #ConcurrentTaskScheduler(ScheduledExecutorService)}
	 */
	@Deprecated(since = "6.1")
	public ConcurrentTaskScheduler() {
		super();
		this.scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
		this.enterpriseConcurrentScheduler = false;
	}

	/**
	 * 使用给定 {@link java.util.concurrent.ScheduledExecutorService} 作为共享委托
	 * 创建新的 ConcurrentTaskScheduler。
	 * <p>自动检测 JSR-236 {@link jakarta.enterprise.concurrent.ManagedScheduledExecutorService}，
	 * 若可能则用于基于 Trigger 的调度，而非 Spring 本地 Trigger 管理。
	 * @param scheduledExecutor 委托的 {@link java.util.concurrent.ScheduledExecutorService}，
	 * 用于 {@link org.springframework.scheduling.SchedulingTaskExecutor} 及 {@link TaskScheduler} 调用
	 */
	public ConcurrentTaskScheduler(@Nullable ScheduledExecutorService scheduledExecutor) {
		super(scheduledExecutor);
		if (scheduledExecutor != null) {
			initScheduledExecutor(scheduledExecutor);
		}
	}

	/**
	 * 使用给定 {@link java.util.concurrent.Executor} 与
	 * {@link java.util.concurrent.ScheduledExecutorService} 作为委托创建新的 ConcurrentTaskScheduler。
	 * <p>自动检测 JSR-236 {@link jakarta.enterprise.concurrent.ManagedScheduledExecutorService}，
	 * 若可能则用于基于 Trigger 的调度。
	 * @param concurrentExecutor 委托的 {@link java.util.concurrent.Executor}，
	 * 用于 {@link org.springframework.scheduling.SchedulingTaskExecutor} 调用
	 * @param scheduledExecutor 委托的 {@link java.util.concurrent.ScheduledExecutorService}，
	 * 用于 {@link TaskScheduler} 调用
	 */
	public ConcurrentTaskScheduler(Executor concurrentExecutor, ScheduledExecutorService scheduledExecutor) {
		super(concurrentExecutor);
		initScheduledExecutor(scheduledExecutor);
	}


	private void initScheduledExecutor(ScheduledExecutorService scheduledExecutor) {
		this.scheduledExecutor = scheduledExecutor;
		this.enterpriseConcurrentScheduler = (managedScheduledExecutorServiceClass != null &&
				managedScheduledExecutorServiceClass.isInstance(scheduledExecutor));
	}

	/**
	 * 指定委托的 {@link java.util.concurrent.ScheduledExecutorService}。
	 * <p>自动检测 JSR-236 {@link jakarta.enterprise.concurrent.ManagedScheduledExecutorService}，
	 * 若可能则用于基于 Trigger 的调度。
	 * <p>注意：这仅适用于 {@link TaskScheduler} 调用。
	 * 若希望给定执行器也应用于 {@link org.springframework.scheduling.SchedulingTaskExecutor} 调用，
	 * 请将同一执行器引用传给 {@link #setConcurrentExecutor}。
	 * @see #setConcurrentExecutor
	 */
	public void setScheduledExecutor(ScheduledExecutorService scheduledExecutor) {
		initScheduledExecutor(scheduledExecutor);
	}

	private ScheduledExecutorService getScheduledExecutor() {
		if (this.scheduledExecutor == null) {
			throw new IllegalStateException("No ScheduledExecutor is configured");
		}
		return this.scheduledExecutor;
	}

	/**
	 * 提供 {@link ErrorHandler} 策略。
	 */
	public void setErrorHandler(ErrorHandler errorHandler) {
		Assert.notNull(errorHandler, "ErrorHandler must not be null");
		this.errorHandler = errorHandler;
	}

	/**
	 * 设置调度使用的时钟。
	 * <p>默认为默认时区的系统时钟。
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
		ScheduledExecutorService scheduleExecutorToUse = getScheduledExecutor();
		try {
			if (this.enterpriseConcurrentScheduler) {
				return new EnterpriseConcurrentTriggerScheduler().schedule(decorateTask(task, true), trigger);
			}
			else {
				ErrorHandler errorHandler =
						(this.errorHandler != null ? this.errorHandler : TaskUtils.getDefaultErrorHandler(true));
				return new ReschedulingRunnable(
						decorateTaskIfNecessary(task), trigger, this.clock, scheduleExecutorToUse, errorHandler)
						.schedule();
			}
		}
		catch (RejectedExecutionException ex) {
			throw new TaskRejectedException(scheduleExecutorToUse, task, ex);
		}
	}

	@Override
	public ScheduledFuture<?> schedule(Runnable task, Instant startTime) {
		ScheduledExecutorService scheduleExecutorToUse = getScheduledExecutor();
		Duration delay = Duration.between(this.clock.instant(), startTime);
		try {
			return scheduleExecutorToUse.schedule(decorateTask(task, false), NANO.convert(delay), NANO);
		}
		catch (RejectedExecutionException ex) {
			throw new TaskRejectedException(scheduleExecutorToUse, task, ex);
		}
	}

	@Override
	public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, Instant startTime, Duration period) {
		ScheduledExecutorService scheduleExecutorToUse = getScheduledExecutor();
		Duration initialDelay = Duration.between(this.clock.instant(), startTime);
		try {
			return scheduleExecutorToUse.scheduleAtFixedRate(decorateTask(task, true),
					NANO.convert(initialDelay), NANO.convert(period), NANO);
		}
		catch (RejectedExecutionException ex) {
			throw new TaskRejectedException(scheduleExecutorToUse, task, ex);
		}
	}

	@Override
	public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, Duration period) {
		ScheduledExecutorService scheduleExecutorToUse = getScheduledExecutor();
		try {
			return scheduleExecutorToUse.scheduleAtFixedRate(decorateTask(task, true),
					0, NANO.convert(period), NANO);
		}
		catch (RejectedExecutionException ex) {
			throw new TaskRejectedException(scheduleExecutorToUse, task, ex);
		}
	}

	@Override
	public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, Instant startTime, Duration delay) {
		ScheduledExecutorService scheduleExecutorToUse = getScheduledExecutor();
		Duration initialDelay = Duration.between(this.clock.instant(), startTime);
		try {
			return scheduleExecutorToUse.scheduleWithFixedDelay(decorateTask(task, true),
					NANO.convert(initialDelay), NANO.convert(delay), NANO);
		}
		catch (RejectedExecutionException ex) {
			throw new TaskRejectedException(scheduleExecutorToUse, task, ex);
		}
	}

	@Override
	public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, Duration delay) {
		ScheduledExecutorService scheduleExecutorToUse = getScheduledExecutor();
		try {
			return scheduleExecutorToUse.scheduleWithFixedDelay(decorateTask(task, true),
					0, NANO.convert(delay), NANO);
		}
		catch (RejectedExecutionException ex) {
			throw new TaskRejectedException(scheduleExecutorToUse, task, ex);
		}
	}

	private Runnable decorateTask(Runnable task, boolean isRepeatingTask) {
		Runnable result = TaskUtils.decorateTaskWithErrorHandler(task, this.errorHandler, isRepeatingTask);
		result = decorateTaskIfNecessary(result);
		if (this.enterpriseConcurrentScheduler) {
			result = ManagedTaskBuilder.buildManagedTask(result, task.toString());
		}
		return result;
	}


	/**
	 * 将 Spring Trigger 适配为 JSR-236 Trigger 的委托。
	 * 分离为内部类以避免对 JSR-236 API 的硬依赖。
	 */
	private class EnterpriseConcurrentTriggerScheduler {

		public ScheduledFuture<?> schedule(Runnable task, Trigger trigger) {
			ManagedScheduledExecutorService executor = (ManagedScheduledExecutorService) getScheduledExecutor();
			return executor.schedule(task, new TriggerAdapter(trigger));
		}


		private static class TriggerAdapter implements jakarta.enterprise.concurrent.Trigger {

			private final Trigger adaptee;

			public TriggerAdapter(Trigger adaptee) {
				this.adaptee = adaptee;
			}

			@Override
			public @Nullable Date getNextRunTime(@Nullable LastExecution le, Date taskScheduledTime) {
				Instant instant = this.adaptee.nextExecution(new LastExecutionAdapter(le));
				return (instant != null ? Date.from(instant) : null);
			}

			@Override
			public boolean skipRun(LastExecution lastExecutionInfo, Date scheduledRunTime) {
				return false;
			}


			private static class LastExecutionAdapter implements TriggerContext {

				private final @Nullable LastExecution le;

				public LastExecutionAdapter(@Nullable LastExecution le) {
					this.le = le;
				}

				@Override
				public @Nullable Instant lastScheduledExecution() {
					return (this.le != null ? toInstant(this.le.getScheduledStart()) : null);
				}

				@Override
				public @Nullable Instant lastActualExecution() {
					return (this.le != null ? toInstant(this.le.getRunStart()) : null);
				}

				@Override
				public @Nullable Instant lastCompletion() {
					return (this.le != null ? toInstant(this.le.getRunEnd()) : null);
				}

				private static @Nullable Instant toInstant(@Nullable Date date) {
					return (date != null ? date.toInstant() : null);
				}
			}
		}
	}

}
