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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.scheduling.support.DelegatingErrorHandlingRunnable;
import org.springframework.scheduling.support.TaskUtils;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

/**
 * 配置 {@link java.util.concurrent.ScheduledExecutorService}
 *（默认 {@link java.util.concurrent.ScheduledThreadPoolExecutor}）
 * 并暴露为 Bean 引用的 {@link org.springframework.beans.factory.FactoryBean}。
 *
 * <p>允许注册 {@link ScheduledExecutorTask ScheduledExecutorTasks}，
 * 初始化时自动启动 {@link ScheduledExecutorService}，
 * 上下文销毁时取消。若仅需启动时静态注册任务，
 * 应用代码完全无需访问 {@link ScheduledExecutorService} 实例；
 * 此时 {@code ScheduledExecutorFactoryBean} 仅用于生命周期集成。
 *
 * <p>也可通过构造器注入直接配置 {@link ScheduledThreadPoolExecutor}，
 * 或使用指向 {@link java.util.concurrent.Executors} 的工厂方法定义。
 * <b>配置类中常见 {@code @Bean} 方法尤其推荐后者，
 * 因本 {@code FactoryBean} 变体会强制返回 {@code FactoryBean} 类型
 * 而非 {@code ScheduledExecutorService}。</b>
 *
 * <p>注意 {@link java.util.concurrent.ScheduledExecutorService}
 * 在重复执行间共享同一 {@link Runnable} 实例，
 * 与 Quartz 每次执行创建新 Job 不同。
 *
 * <p><b>警告：</b>通过原生 {@link java.util.concurrent.ScheduledExecutorService}
 * 提交的 {@link Runnable Runnable} 一旦抛异常即从调度中移除。
 * 若希望异常后继续执行，请将本 FactoryBean 的
 * {@link #setContinueScheduledExecutionAfterException "continueScheduledExecutionAfterException"}
 * 属性设为 "true"。
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see #setPoolSize
 * @see #setRemoveOnCancelPolicy
 * @see #setThreadFactory
 * @see ScheduledExecutorTask
 * @see java.util.concurrent.ScheduledExecutorService
 * @see java.util.concurrent.ScheduledThreadPoolExecutor
 */
@SuppressWarnings("serial")
public class ScheduledExecutorFactoryBean extends ExecutorConfigurationSupport
		implements FactoryBean<ScheduledExecutorService> {

	private int poolSize = 1;

	private ScheduledExecutorTask @Nullable [] scheduledExecutorTasks;

	private boolean removeOnCancelPolicy = false;

	private boolean continueScheduledExecutionAfterException = false;

	private boolean exposeUnconfigurableExecutor = false;

	private @Nullable ScheduledExecutorService exposedExecutor;


	/**
	 * 设置 ScheduledExecutorService 的池大小。
	 * 默认为 1。
	 */
	public void setPoolSize(int poolSize) {
		Assert.isTrue(poolSize > 0, "'poolSize' must be 1 or higher");
		this.poolSize = poolSize;
	}

	/**
	 * 向本 FactoryBean 创建的 ScheduledExecutorService 注册 ScheduledExecutorTask 列表。
	 * 根据各 ScheduledExecutorTask 的设置，
	 * 通过 ScheduledExecutorService 的 schedule 方法之一注册。
	 * @see java.util.concurrent.ScheduledExecutorService#schedule(java.lang.Runnable, long, java.util.concurrent.TimeUnit)
	 * @see java.util.concurrent.ScheduledExecutorService#scheduleWithFixedDelay(java.lang.Runnable, long, long, java.util.concurrent.TimeUnit)
	 * @see java.util.concurrent.ScheduledExecutorService#scheduleAtFixedRate(java.lang.Runnable, long, long, java.util.concurrent.TimeUnit)
	 */
	public void setScheduledExecutorTasks(ScheduledExecutorTask... scheduledExecutorTasks) {
		this.scheduledExecutorTasks = scheduledExecutorTasks;
	}

	/**
	 * 在 {@link ScheduledThreadPoolExecutor} 上设置 cancel 时移除模式。
	 * <p>默认为 {@code false}。设为 {@code true} 时，
	 * 目标执行器将切换为 remove-on-cancel 模式（若可能，否则软回退）。
	 */
	public void setRemoveOnCancelPolicy(boolean removeOnCancelPolicy) {
		this.removeOnCancelPolicy = removeOnCancelPolicy;
	}

	/**
	 * 指定定时任务抛异常后是否继续执行。
	 * <p>默认为 "false"，与 {@link java.util.concurrent.ScheduledExecutorService}
	 * 原生行为一致。设为 "true" 可保证各任务异常后继续调度执行，
	 * 如同成功执行一样。
	 * @see java.util.concurrent.ScheduledExecutorService#scheduleAtFixedRate
	 */
	public void setContinueScheduledExecutionAfterException(boolean continueScheduledExecutionAfterException) {
		this.continueScheduledExecutionAfterException = continueScheduledExecutionAfterException;
	}

	/**
	 * 指定本 FactoryBean 是否应为创建的执行器暴露不可配置装饰器。
	 * <p>默认为 "false"，将原始执行器作为 Bean 引用暴露。
	 * 设为 "true" 可严格禁止客户端修改执行器配置。
	 * @see java.util.concurrent.Executors#unconfigurableScheduledExecutorService
	 */
	public void setExposeUnconfigurableExecutor(boolean exposeUnconfigurableExecutor) {
		this.exposeUnconfigurableExecutor = exposeUnconfigurableExecutor;
	}


	@Override
	protected ExecutorService initializeExecutor(
			ThreadFactory threadFactory, RejectedExecutionHandler rejectedExecutionHandler) {

		ScheduledExecutorService executor =
				createExecutor(this.poolSize, threadFactory, rejectedExecutionHandler);

		if (this.removeOnCancelPolicy) {
			if (executor instanceof ScheduledThreadPoolExecutor threadPoolExecutor) {
				threadPoolExecutor.setRemoveOnCancelPolicy(true);
			}
			else {
				logger.debug("Could not apply remove-on-cancel policy - not a ScheduledThreadPoolExecutor");
			}
		}

		// Register specified ScheduledExecutorTasks, if necessary.
		if (!ObjectUtils.isEmpty(this.scheduledExecutorTasks)) {
			registerTasks(this.scheduledExecutorTasks, executor);
		}

		// Wrap executor with an unconfigurable decorator.
		this.exposedExecutor = (this.exposeUnconfigurableExecutor ?
				Executors.unconfigurableScheduledExecutorService(executor) : executor);

		return executor;
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
				ScheduledExecutorFactoryBean.this.beforeExecute(thread, task);
			}
			@Override
			protected void afterExecute(Runnable task, Throwable ex) {
				ScheduledExecutorFactoryBean.this.afterExecute(task, ex);
			}
		};
	}

	/**
	 * 在指定 {@link ScheduledExecutorService} 上注册
	 * {@link ScheduledExecutorTask ScheduledExecutorTasks}。
	 * @param tasks 指定的 ScheduledExecutorTasks（永不为空）
	 * @param executor 注册任务的 ScheduledExecutorService
	 */
	protected void registerTasks(ScheduledExecutorTask[] tasks, ScheduledExecutorService executor) {
		for (ScheduledExecutorTask task : tasks) {
			Runnable runnable = getRunnableToSchedule(task);
			if (task.isOneTimeTask()) {
				executor.schedule(runnable, task.getDelay(), task.getTimeUnit());
			}
			else {
				if (task.isFixedRate()) {
					executor.scheduleAtFixedRate(runnable, task.getDelay(), task.getPeriod(), task.getTimeUnit());
				}
				else {
					executor.scheduleWithFixedDelay(runnable, task.getDelay(), task.getPeriod(), task.getTimeUnit());
				}
			}
		}
	}

	/**
	 * 确定给定任务实际要调度的 Runnable。
	 * <p>将任务的 Runnable 包装为
	 * {@link org.springframework.scheduling.support.DelegatingErrorHandlingRunnable}，
	 * 捕获并记录异常。必要时根据
	 * {@link #setContinueScheduledExecutionAfterException "continueScheduledExecutionAfterException"}
	 * 标志抑制异常。
	 * @param task 待调度的 ScheduledExecutorTask
	 * @return 实际要调度的 Runnable（可能是装饰器）
	 */
	protected Runnable getRunnableToSchedule(ScheduledExecutorTask task) {
		return (this.continueScheduledExecutionAfterException ?
				new DelegatingErrorHandlingRunnable(task.getRunnable(), TaskUtils.LOG_AND_SUPPRESS_ERROR_HANDLER) :
				new DelegatingErrorHandlingRunnable(task.getRunnable(), TaskUtils.LOG_AND_PROPAGATE_ERROR_HANDLER));
	}


	@Override
	public @Nullable ScheduledExecutorService getObject() {
		return this.exposedExecutor;
	}

	@Override
	public Class<? extends ScheduledExecutorService> getObjectType() {
		return (this.exposedExecutor != null ? this.exposedExecutor.getClass() : ScheduledExecutorService.class);
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

}
