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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.FactoryBean;

/**
 * 允许以 Bean 风格（通过 "corePoolSize"、"maxPoolSize"、"keepAliveSeconds"、
 * "queueCapacity" 属性）配置 {@link java.util.concurrent.ThreadPoolExecutor}，
 * 并将其原生 {@link java.util.concurrent.ExecutorService} 类型暴露为 Bean 引用的 JavaBean。
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
 * <p>也可通过构造器注入直接配置 {@link ThreadPoolExecutor}，
 * 或使用指向 {@link java.util.concurrent.Executors} 的工厂方法定义。
 * <b>配置类中常见 {@code @Bean} 方法尤其推荐后者，
 * 因本 {@code FactoryBean} 变体会强制返回 {@code FactoryBean} 类型
 * 而非实际 {@code Executor} 类型。</b>
 *
 * <p>若需要基于时间的 {@link java.util.concurrent.ScheduledExecutorService}，
 * 请考虑 {@link ScheduledExecutorFactoryBean}。

 * @author Juergen Hoeller
 * @since 3.0
 * @see java.util.concurrent.ExecutorService
 * @see java.util.concurrent.Executors
 * @see java.util.concurrent.ThreadPoolExecutor
 */
@SuppressWarnings("serial")
public class ThreadPoolExecutorFactoryBean extends ExecutorConfigurationSupport
		implements FactoryBean<ExecutorService> {

	private int corePoolSize = 1;

	private int maxPoolSize = Integer.MAX_VALUE;

	private int keepAliveSeconds = 60;

	private int queueCapacity = Integer.MAX_VALUE;

	private boolean allowCoreThreadTimeOut = false;

	private boolean prestartAllCoreThreads = false;

	private boolean strictEarlyShutdown = false;

	private boolean exposeUnconfigurableExecutor = false;

	private @Nullable ExecutorService exposedExecutor;


	/**
	 * 设置 ThreadPoolExecutor 的核心池大小。
	 * 默认为 1。
	 */
	public void setCorePoolSize(int corePoolSize) {
		this.corePoolSize = corePoolSize;
	}

	/**
	 * 设置 ThreadPoolExecutor 的最大池大小。
	 * 默认为 {@code Integer.MAX_VALUE}。
	 */
	public void setMaxPoolSize(int maxPoolSize) {
		this.maxPoolSize = maxPoolSize;
	}

	/**
	 * 设置 ThreadPoolExecutor 的 keep-alive 秒数。
	 * 默认为 60。
	 */
	public void setKeepAliveSeconds(int keepAliveSeconds) {
		this.keepAliveSeconds = keepAliveSeconds;
	}

	/**
	 * 设置 ThreadPoolExecutor 的 BlockingQueue 容量。
	 * 默认为 {@code Integer.MAX_VALUE}。
	 * <p>任意正值将创建 LinkedBlockingQueue 实例；
	 * 其他值将创建 SynchronousQueue 实例。
	 * @see java.util.concurrent.LinkedBlockingQueue
	 * @see java.util.concurrent.SynchronousQueue
	 */
	public void setQueueCapacity(int queueCapacity) {
		this.queueCapacity = queueCapacity;
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
	 * <p>默认为 "false"。
	 * @since 5.3.14
	 * @see java.util.concurrent.ThreadPoolExecutor#prestartAllCoreThreads
	 */
	public void setPrestartAllCoreThreads(boolean prestartAllCoreThreads) {
		this.prestartAllCoreThreads = prestartAllCoreThreads;
	}

	/**
	 * 指定上下文关闭时是否发出提前关闭信号，
	 * 释放所有空闲线程并拒绝后续任务提交。
	 * <p>默认为 "false"。
	 * 详情见 {@link ThreadPoolTaskExecutor#setStrictEarlyShutdown}。
	 * @since 6.1.4
	 * @see #initiateShutdown()
	 */
	public void setStrictEarlyShutdown(boolean defaultEarlyShutdown) {
		this.strictEarlyShutdown = defaultEarlyShutdown;
	}

	/**
	 * 指定本 FactoryBean 是否应为创建的执行器暴露不可配置装饰器。
	 * <p>默认为 "false"，将原始执行器作为 Bean 引用暴露。
	 * 设为 "true" 可严格禁止客户端修改执行器配置。
	 * @see java.util.concurrent.Executors#unconfigurableExecutorService
	 */
	public void setExposeUnconfigurableExecutor(boolean exposeUnconfigurableExecutor) {
		this.exposeUnconfigurableExecutor = exposeUnconfigurableExecutor;
	}


	@Override
	protected ExecutorService initializeExecutor(
			ThreadFactory threadFactory, RejectedExecutionHandler rejectedExecutionHandler) {

		BlockingQueue<Runnable> queue = createQueue(this.queueCapacity);
		ThreadPoolExecutor executor = createExecutor(this.corePoolSize, this.maxPoolSize,
				this.keepAliveSeconds, queue, threadFactory, rejectedExecutionHandler);
		if (this.allowCoreThreadTimeOut) {
			executor.allowCoreThreadTimeOut(true);
		}
		if (this.prestartAllCoreThreads) {
			executor.prestartAllCoreThreads();
		}

		// Wrap executor with an unconfigurable decorator.
		this.exposedExecutor = (this.exposeUnconfigurableExecutor ?
				Executors.unconfigurableExecutorService(executor) : executor);

		return executor;
	}

	/**
	 * 创建新的 {@link ThreadPoolExecutor} 或其子类实例。
	 * <p>默认实现创建标准 {@link ThreadPoolExecutor}。
	 * 可覆盖以提供自定义 {@link ThreadPoolExecutor} 子类。
	 * @param corePoolSize 指定核心池大小
	 * @param maxPoolSize 指定最大池大小
	 * @param keepAliveSeconds 指定 keep-alive 时间（秒）
	 * @param queue 使用的 BlockingQueue
	 * @param threadFactory 使用的 ThreadFactory
	 * @param rejectedExecutionHandler 使用的 RejectedExecutionHandler
	 * @return 新的 ThreadPoolExecutor 实例
	 * @see #afterPropertiesSet()
	 */
	protected ThreadPoolExecutor createExecutor(
			int corePoolSize, int maxPoolSize, int keepAliveSeconds, BlockingQueue<Runnable> queue,
			ThreadFactory threadFactory, RejectedExecutionHandler rejectedExecutionHandler) {

		return new ThreadPoolExecutor(corePoolSize, maxPoolSize,
				keepAliveSeconds, TimeUnit.SECONDS, queue, threadFactory, rejectedExecutionHandler) {
			@Override
			protected void beforeExecute(Thread thread, Runnable task) {
				ThreadPoolExecutorFactoryBean.this.beforeExecute(thread, task);
			}
			@Override
			protected void afterExecute(Runnable task, Throwable ex) {
				ThreadPoolExecutorFactoryBean.this.afterExecute(task, ex);
			}
		};
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

	@Override
	protected void initiateEarlyShutdown() {
		if (this.strictEarlyShutdown) {
			super.initiateEarlyShutdown();
		}
	}


	@Override
	public @Nullable ExecutorService getObject() {
		return this.exposedExecutor;
	}

	@Override
	public Class<? extends ExecutorService> getObjectType() {
		return (this.exposedExecutor != null ? this.exposedExecutor.getClass() : ExecutorService.class);
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

}
