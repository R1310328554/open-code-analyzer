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

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * 构建并暴露预配置 {@link ForkJoinPool} 的 Spring {@link FactoryBean}。
 *
 * @author Juergen Hoeller
 * @since 3.1
 */
public class ForkJoinPoolFactoryBean implements FactoryBean<ForkJoinPool>, InitializingBean, DisposableBean {

	private boolean commonPool = false;

	private int parallelism = Runtime.getRuntime().availableProcessors();

	private ForkJoinPool.ForkJoinWorkerThreadFactory threadFactory = ForkJoinPool.defaultForkJoinWorkerThreadFactory;

	private Thread.@Nullable UncaughtExceptionHandler uncaughtExceptionHandler;

	private boolean asyncMode = false;

	private int awaitTerminationSeconds = 0;

	private @Nullable ForkJoinPool forkJoinPool;


	/**
	 * 设置是否暴露 Java 的 'common' {@link ForkJoinPool}。
	 * <p>默认为 {@code false}，基于本 FactoryBean 的
	 * {@link #setParallelism parallelism}、
	 * {@link #setThreadFactory threadFactory}、
	 * {@link #setUncaughtExceptionHandler uncaughtExceptionHandler} 与
	 * {@link #setAsyncMode asyncMode} 属性创建本地 {@link ForkJoinPool} 实例。
	 * <p><b>注意：</b>将此标志设为 {@code true} 将有效忽略本 FactoryBean 的所有其他属性，
	 * 改为复用共享的 JDK common {@link ForkJoinPool}。
	 * 这是合理选择，但会移除应用自定义 ForkJoinPool 行为的能力，
	 * 尤其无法使用自定义线程。
	 * @since 3.2
	 * @see java.util.concurrent.ForkJoinPool#commonPool()
	 */
	public void setCommonPool(boolean commonPool) {
		this.commonPool = commonPool;
	}

	/**
	 * 指定并行级别。默认为 {@link Runtime#availableProcessors()}。
	 */
	public void setParallelism(int parallelism) {
		this.parallelism = parallelism;
	}

	/**
	 * 设置创建新 ForkJoinWorkerThread 的工厂。
	 * 默认为 {@link ForkJoinPool#defaultForkJoinWorkerThreadFactory}。
	 */
	public void setThreadFactory(ForkJoinPool.ForkJoinWorkerThreadFactory threadFactory) {
		this.threadFactory = threadFactory;
	}

	/**
	 * 设置因执行任务时遇到不可恢复错误而终止的内部工作线程的处理程序。默认为无。
	 */
	public void setUncaughtExceptionHandler(Thread.UncaughtExceptionHandler uncaughtExceptionHandler) {
		this.uncaughtExceptionHandler = uncaughtExceptionHandler;
	}

	/**
	 * 指定是否为永不 join 的分叉任务建立本地先进先出调度模式。
	 * 在工作线程仅处理事件式异步任务的应用中，
	 * 此模式 (asyncMode = {@code true}) 可能比默认本地栈模式更合适。默认为 {@code false}。
	 */
	public void setAsyncMode(boolean asyncMode) {
		this.asyncMode = asyncMode;
	}

	/**
	 * 设置本 ForkJoinPool 在 shutdown 时最多阻塞的秒数，
	 * 以等待剩余任务完成执行，然后容器其余部分继续关闭。
	 * 若剩余任务可能需要访问容器管理的其他资源，这尤其有用。
	 * <p>默认情况下，本 ForkJoinPool 完全不等待任务终止。
	 * 它将与容器其余部分并行关闭，
	 * 继续完全执行所有进行中任务及队列中剩余任务。
	 * 相反，若通过本属性指定 await-termination 周期，
	 * 本执行器将最多等待给定时间以待任务终止。
	 * <p>注意此特性对 {@link #setCommonPool "commonPool"} 模式同样有效。
	 * 此时底层 ForkJoinPool 不会真正终止，但会等待所有任务终止。
	 * @see java.util.concurrent.ForkJoinPool#shutdown()
	 * @see java.util.concurrent.ForkJoinPool#awaitTermination
	 */
	public void setAwaitTerminationSeconds(int awaitTerminationSeconds) {
		this.awaitTerminationSeconds = awaitTerminationSeconds;
	}

	@Override
	public void afterPropertiesSet() {
		this.forkJoinPool = (this.commonPool ? ForkJoinPool.commonPool() :
				new ForkJoinPool(this.parallelism, this.threadFactory, this.uncaughtExceptionHandler, this.asyncMode));
	}


	@Override
	public @Nullable ForkJoinPool getObject() {
		return this.forkJoinPool;
	}

	@Override
	public Class<?> getObjectType() {
		return ForkJoinPool.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}


	@Override
	public void destroy() {
		if (this.forkJoinPool != null) {
			// Ignored for the common pool.
			this.forkJoinPool.shutdown();

			// Wait for all tasks to terminate - works for the common pool as well.
			if (this.awaitTerminationSeconds > 0) {
				try {
					this.forkJoinPool.awaitTermination(this.awaitTerminationSeconds, TimeUnit.SECONDS);
				}
				catch (InterruptedException ex) {
					Thread.currentThread().interrupt();
				}
			}
		}
	}

}
