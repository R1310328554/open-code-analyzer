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

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.jspecify.annotations.Nullable;

import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.TaskDecorator;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.scheduling.SchedulingTaskExecutor;
import org.springframework.util.Assert;
import org.springframework.util.ConcurrentReferenceHashMap;

/**
 * 允许以 Bean 风格（通过 "corePoolSize"、"maxPoolSize"、"keepAliveSeconds"、
 * "queueCapacity" 属性）配置 {@link java.util.concurrent.ThreadPoolExecutor}，
 * 并将其作为 Spring {@link org.springframework.core.task.TaskExecutor} 暴露的 JavaBean。
 * 本类也适合管理与监控（例如通过 JMX），
 * 提供若干有用属性："corePoolSize"、"maxPoolSize"、"keepAliveSeconds"
 *（均支持运行时更新）；"poolSize"、"activeCount"（仅用于自省）。
 *
 * <p>默认配置为核心池大小 1、无界最大池大小与无界队列容量。
 * 大致等价于 {@link java.util.concurrent.Executors#newSingleThreadExecutor()}，
 * 所有任务共享单线程。将 {@link #setQueueCapacity "queueCapacity"} 设为 0
 * 可模拟 {@link java.util.concurrent.Executors#newCachedThreadPool()}，
 * 池中线程可立即扩展至可能很高的数量。此时建议同时设置
 * {@link #setMaxPoolSize "maxPoolSize"}，以及可能更高的
 * {@link #setCorePoolSize "corePoolSize"}（另见
 * {@link #setAllowCoreThreadTimeOut "allowCoreThreadTimeOut"} 扩展模式）。
 *
 * <p><b>注意：</b>本类实现 Spring 的
 * {@link org.springframework.core.task.TaskExecutor} 接口以及
 * {@link java.util.concurrent.Executor} 接口，以前者为主接口，
 * 后者仅为辅助便利。因此异常处理遵循 TaskExecutor 契约而非 Executor 契约，
 * 尤其涉及 {@link org.springframework.core.task.TaskRejectedException}。
 *
 * <p>也可通过构造器注入直接配置 ThreadPoolExecutor，
 * 或使用指向 {@link java.util.concurrent.Executors} 的工厂方法定义。
 * 若要将此类原始 Executor 作为 Spring {@link org.springframework.core.task.TaskExecutor} 暴露，
 * 只需用 {@link org.springframework.scheduling.concurrent.ConcurrentTaskExecutor} 适配器包装即可。
 *
 * @author Juergen Hoeller
 * @author Rémy Guihard
 * @author Sam Brannen
 * @since 2.0
 * @see org.springframework.core.task.TaskExecutor
 * @see java.util.concurrent.ThreadPoolExecutor
 * @see ThreadPoolExecutorFactoryBean
 * @see ConcurrentTaskExecutor
 */
@SuppressWarnings({"serial", "deprecation"})
public class ThreadPoolTaskExecutor extends ExecutorConfigurationSupport
		implements AsyncTaskExecutor, SchedulingTaskExecutor {

	private final Object poolSizeMonitor = new Object();

	private int corePoolSize = 1;

	private int maxPoolSize = Integer.MAX_VALUE;

	private int keepAliveSeconds = 60;

	private int queueCapacity = Integer.MAX_VALUE;

	private boolean allowCoreThreadTimeOut = false;

	private boolean prestartAllCoreThreads = false;

	private boolean strictEarlyShutdown = false;

	private @Nullable TaskDecorator taskDecorator;

	private @Nullable ThreadPoolExecutor threadPoolExecutor;

	// Runnable decorator to user-level FutureTask, if different
	private final Map<Runnable, Object> decoratedTaskMap =
			new ConcurrentReferenceHashMap<>(16, ConcurrentReferenceHashMap.ReferenceType.WEAK);


	/**
	 * 设置 ThreadPoolExecutor 的核心池大小。
	 * 默认为 1。
	 * <p><b>此设置可在运行时修改，例如通过 JMX。</b>
	 */
	public void setCorePoolSize(int corePoolSize) {
		synchronized (this.poolSizeMonitor) {
			if (this.threadPoolExecutor != null) {
				this.threadPoolExecutor.setCorePoolSize(corePoolSize);
			}
			this.corePoolSize = corePoolSize;
		}
	}

	/**
	 * 返回 ThreadPoolExecutor 的核心池大小。
	 */
	public int getCorePoolSize() {
		synchronized (this.poolSizeMonitor) {
			return this.corePoolSize;
		}
	}

	/**
	 * 设置 ThreadPoolExecutor 的最大池大小。
	 * 默认为 {@code Integer.MAX_VALUE}。
	 * <p><b>此设置可在运行时修改，例如通过 JMX。</b>
	 */
	public void setMaxPoolSize(int maxPoolSize) {
		synchronized (this.poolSizeMonitor) {
			if (this.threadPoolExecutor != null) {
				this.threadPoolExecutor.setMaximumPoolSize(maxPoolSize);
			}
			this.maxPoolSize = maxPoolSize;
		}
	}

	/**
	 * 返回 ThreadPoolExecutor 的最大池大小。
	 */
	public int getMaxPoolSize() {
		synchronized (this.poolSizeMonitor) {
			return this.maxPoolSize;
		}
	}

	/**
	 * 设置 ThreadPoolExecutor 的 keep-alive 秒数。
	 * <p>默认为 60。
	 * <p><b>此设置可在运行时修改，例如通过 JMX。</b>
	 */
	public void setKeepAliveSeconds(int keepAliveSeconds) {
		synchronized (this.poolSizeMonitor) {
			if (this.threadPoolExecutor != null) {
				this.threadPoolExecutor.setKeepAliveTime(keepAliveSeconds, TimeUnit.SECONDS);
			}
			this.keepAliveSeconds = keepAliveSeconds;
		}
	}

	/**
	 * 返回 ThreadPoolExecutor 的 keep-alive 秒数。
	 */
	public int getKeepAliveSeconds() {
		synchronized (this.poolSizeMonitor) {
			return this.keepAliveSeconds;
		}
	}

	/**
	 * 设置 ThreadPoolExecutor 的 BlockingQueue 容量。
	 * <p>默认为 {@code Integer.MAX_VALUE}。
	 * <p>任意正值将创建 LinkedBlockingQueue 实例；
	 * 其他值将创建 SynchronousQueue 实例。
	 * @see java.util.concurrent.LinkedBlockingQueue
	 * @see java.util.concurrent.SynchronousQueue
	 */
	public void setQueueCapacity(int queueCapacity) {
		this.queueCapacity = queueCapacity;
	}

	/**
	 * 返回 ThreadPoolExecutor 的 BlockingQueue 容量。
	 * @since 5.3.21
	 * @see #setQueueCapacity(int)
	 */
	public int getQueueCapacity() {
		return this.queueCapacity;
	}

	/**
	 * 指定是否允许核心线程超时。即使队列非空也可动态扩缩
	 *（最大池大小仅在队列满后才增长）。
	 * <p>默认为 "false"。
	 * @see java.util.concurrent.ThreadPoolExecutor#allowCoreThreadTimeOut(boolean)
	 */
	public void setAllowCoreThreadTimeOut(boolean allowCoreThreadTimeOut) {
		this.allowCoreThreadTimeOut = allowCoreThreadTimeOut;
	}

	/**
	 * 指定是否启动所有核心线程，使其空闲等待任务。
	 * <p>默认为 "false"，按需启动线程并加入池。
	 * @since 5.3.14
	 * @see java.util.concurrent.ThreadPoolExecutor#prestartAllCoreThreads
	 */
	public void setPrestartAllCoreThreads(boolean prestartAllCoreThreads) {
		this.prestartAllCoreThreads = prestartAllCoreThreads;
	}

	/**
	 * 指定上下文关闭时是否发出提前关闭信号，
	 * 释放所有空闲线程并拒绝后续任务提交。
	 * <p>默认情况下，现有任务仍可在协调的生命周期停止阶段内完成。
	 * 本设置仅控制上下文关闭时是否触发显式 {@link ThreadPoolExecutor#shutdown()} 调用，
	 * 此后拒绝任务提交。
	 * <p>自 6.1.4 起，默认为 "false"，宽松允许上下文关闭后仍有迟到的任务到达，
	 * 仍参与生命周期停止阶段。注意这与 {@link #setAcceptTasksAfterContextClose} 不同，
	 * 后者完全绕过协调的生命周期停止阶段，根本不显式等待现有任务完成。
	 * <p>设为 "true" 可获得与 {@link ThreadPoolTaskScheduler}
	 * 6.1 起默认行为类似的严格提前关闭信号。
	 * 注意相关标志 {@link #setAcceptTasksAfterContextClose} 与
	 * {@link #setWaitForTasksToCompleteOnShutdown} 将覆盖本设置，
	 * 导致无协调生命周期停止阶段的延迟关闭。
	 * @since 6.1.4
	 * @see #initiateShutdown()
	 */
	public void setStrictEarlyShutdown(boolean strictEarlyShutdown) {
		this.strictEarlyShutdown = strictEarlyShutdown;
	}

	/**
	 * 指定应用于即将执行的任意 {@link Runnable} 的自定义 {@link TaskDecorator}。
	 * <p>注意此类装饰器未必应用于用户提供的 {@code Runnable}/{@code Callable}，
	 * 而是应用于实际执行回调（可能是用户任务的包装）。
	 * <p>主要用例是在任务调用周围设置执行上下文，
	 * 或为任务执行提供监控/统计。
	 * <p><b>注意：</b>{@code TaskDecorator} 实现中的异常处理
	 * 限于通过 {@code execute} 调用的普通 {@code Runnable} 执行。
	 * 对于 {@code #submit} 调用，暴露的 {@code Runnable} 将是
	 * 不传播任何异常的 {@code FutureTask}；
	 * 可能需要强制转换并调用 {@code Future#get} 以评估异常。
	 * 此类 {@code Future} 场景下如何访问异常，
	 * 见 {@code ThreadPoolExecutor#afterExecute} Javadoc 示例。
	 * @since 4.3
	 */
	public void setTaskDecorator(TaskDecorator taskDecorator) {
		this.taskDecorator = taskDecorator;
	}


	/**
	 * 注意：本方法向基类暴露 {@link ExecutorService}，
	 * 但内部保存实际 {@link ThreadPoolExecutor} 句柄。
	 * 不要为替换执行器而覆盖本方法，仅用于装饰其 {@code ExecutorService} 句柄或保存自定义状态。
	 */
	@Override
	protected ExecutorService initializeExecutor(
			ThreadFactory threadFactory, RejectedExecutionHandler rejectedExecutionHandler) {

		BlockingQueue<Runnable> queue = createQueue(this.queueCapacity);

		ThreadPoolExecutor executor = new ThreadPoolExecutor(
					this.corePoolSize, this.maxPoolSize, this.keepAliveSeconds, TimeUnit.SECONDS,
					queue, threadFactory, rejectedExecutionHandler) {
			@Override
			public void execute(Runnable command) {
				Runnable decorated = command;
				if (taskDecorator != null) {
					decorated = taskDecorator.decorate(command);
					if (decorated != command) {
						decoratedTaskMap.put(decorated, command);
					}
				}
				super.execute(decorated);
			}
			@Override
			protected void beforeExecute(Thread thread, Runnable task) {
				ThreadPoolTaskExecutor.this.beforeExecute(thread, task);
			}
			@Override
			protected void afterExecute(Runnable task, Throwable ex) {
				ThreadPoolTaskExecutor.this.afterExecute(task, ex);
			}
		};

		if (this.allowCoreThreadTimeOut) {
			executor.allowCoreThreadTimeOut(true);
		}
		if (this.prestartAllCoreThreads) {
			executor.prestartAllCoreThreads();
		}

		this.threadPoolExecutor = executor;
		return executor;
	}

	/**
	 * 创建 ThreadPoolExecutor 使用的 BlockingQueue。
	 * <p>容量为正值时创建 LinkedBlockingQueue 实例；否则创建 SynchronousQueue。
	 * @param queueCapacity 指定队列容量
	 * @return BlockingQueue 实例
	 * @see java.util.concurrent.LinkedBlockingQueue
	 * @see java.util.concurrent.SynchronousQueue
	 */
	protected BlockingQueue<Runnable> createQueue(int queueCapacity) {
		if (queueCapacity > 0) {
			return new LinkedBlockingQueue<>(queueCapacity);
		}
		else {
			return new SynchronousQueue<>();
		}
	}

	/**
	 * 返回底层 ThreadPoolExecutor 以供原生访问。
	 * @return 底层 ThreadPoolExecutor（永不为 {@code null}）
	 * @throws IllegalStateException 若 ThreadPoolTaskExecutor 尚未初始化
	 */
	public ThreadPoolExecutor getThreadPoolExecutor() throws IllegalStateException {
		Assert.state(this.threadPoolExecutor != null, "ThreadPoolTaskExecutor not initialized");
		return this.threadPoolExecutor;
	}

	/**
	 * 返回当前池大小。
	 * @see java.util.concurrent.ThreadPoolExecutor#getPoolSize()
	 */
	public int getPoolSize() {
		if (this.threadPoolExecutor == null) {
			// Not initialized yet: assume core pool size.
			return this.corePoolSize;
		}
		return this.threadPoolExecutor.getPoolSize();
	}

	/**
	 * 返回当前队列大小。
	 * @since 5.3.21
	 * @see java.util.concurrent.ThreadPoolExecutor#getQueue()
	 */
	public int getQueueSize() {
		if (this.threadPoolExecutor == null) {
			// Not initialized yet: assume no queued tasks.
			return 0;
		}
		return this.threadPoolExecutor.getQueue().size();
	}

	/**
	 * 返回当前活动线程数。
	 * @see java.util.concurrent.ThreadPoolExecutor#getActiveCount()
	 */
	public int getActiveCount() {
		if (this.threadPoolExecutor == null) {
			// Not initialized yet: assume no active threads.
			return 0;
		}
		return this.threadPoolExecutor.getActiveCount();
	}


	@Override
	public void execute(Runnable task) {
		Executor executor = getThreadPoolExecutor();
		try {
			executor.execute(task);
		}
		catch (RejectedExecutionException ex) {
			throw new TaskRejectedException(executor, task, ex);
		}
	}

	@Override
	public Future<?> submit(Runnable task) {
		ExecutorService executor = getThreadPoolExecutor();
		try {
			return executor.submit(task);
		}
		catch (RejectedExecutionException ex) {
			throw new TaskRejectedException(executor, task, ex);
		}
	}

	@Override
	public <T> Future<T> submit(Callable<T> task) {
		ExecutorService executor = getThreadPoolExecutor();
		try {
			return executor.submit(task);
		}
		catch (RejectedExecutionException ex) {
			throw new TaskRejectedException(executor, task, ex);
		}
	}

	@Override
	protected void cancelRemainingTask(Runnable task) {
		super.cancelRemainingTask(task);
		// Cancel associated user-level Future handle as well
		Object original = this.decoratedTaskMap.get(task);
		if (original instanceof Future<?> future) {
			future.cancel(true);
		}
	}

	@Override
	protected void initiateEarlyShutdown() {
		if (this.strictEarlyShutdown) {
			super.initiateEarlyShutdown();
		}
	}

}
