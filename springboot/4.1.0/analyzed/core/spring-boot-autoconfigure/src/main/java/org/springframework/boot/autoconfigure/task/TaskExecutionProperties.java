/*
 * Copyright 2012-present the original author or authors.
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

package org.springframework.boot.autoconfigure.task;

import java.time.Duration;

import org.jspecify.annotations.Nullable;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 任务执行（Task Execution）的配置属性。
 * <p>
 * 绑定 {@code spring.task.execution.*}，控制线程池、简单异步执行器及关闭行为。
 *
 * @author Stephane Nicoll
 * @author Filip Hrisafov
 * @author Yanming Zhou
 * @since 2.1.0
 */
@ConfigurationProperties("spring.task.execution")
public class TaskExecutionProperties {

	private final Pool pool = new Pool();

	private final Simple simple = new Simple();

	private final Shutdown shutdown = new Shutdown();

	/**
	 * 决定何时创建任务执行器。
	 */
	private Mode mode = Mode.AUTO;

	/**
	 * 是否将当前上下文传播到任务执行中。
	 */
	private boolean propagateContext;

	/**
	 * 新创建线程名称的前缀。
	 */
	private String threadNamePrefix = "task-";

	public Simple getSimple() {
		return this.simple;
	}

	public Pool getPool() {
		return this.pool;
	}

	public Shutdown getShutdown() {
		return this.shutdown;
	}

	public Mode getMode() {
		return this.mode;
	}

	public void setMode(Mode mode) {
		this.mode = mode;
	}

	public boolean getPropagateContext() {
		return this.propagateContext;
	}

	public void setPropagateContext(boolean propagateContext) {
		this.propagateContext = propagateContext;
	}

	public String getThreadNamePrefix() {
		return this.threadNamePrefix;
	}

	public void setThreadNamePrefix(String threadNamePrefix) {
		this.threadNamePrefix = threadNamePrefix;
	}

	public static class Simple {

		/**
		 * 关闭时是否取消剩余任务。仅在线程可能长期阻塞时建议使用。
		 */
		private boolean cancelRemainingTasksOnClose;

		/**
		 * 达到并发上限时是否拒绝新任务。
		 */
		private boolean rejectTasksWhenLimitReached;

		/**
		 * 允许的最大并行访问数；{@code -1} 表示无并发限制。
		 */
		private @Nullable Integer concurrencyLimit;

		public boolean isCancelRemainingTasksOnClose() {
			return this.cancelRemainingTasksOnClose;
		}

		public void setCancelRemainingTasksOnClose(boolean cancelRemainingTasksOnClose) {
			this.cancelRemainingTasksOnClose = cancelRemainingTasksOnClose;
		}

		public boolean isRejectTasksWhenLimitReached() {
			return this.rejectTasksWhenLimitReached;
		}

		public void setRejectTasksWhenLimitReached(boolean rejectTasksWhenLimitReached) {
			this.rejectTasksWhenLimitReached = rejectTasksWhenLimitReached;
		}

		public @Nullable Integer getConcurrencyLimit() {
			return this.concurrencyLimit;
		}

		public void setConcurrencyLimit(@Nullable Integer concurrencyLimit) {
			this.concurrencyLimit = concurrencyLimit;
		}

	}

	public static class Pool {

		/**
		 * 队列容量。无界队列不会扩展线程池，因此忽略 {@code max-size}。
		 * 启用虚拟线程时无效。
		 */
		private int queueCapacity = Integer.MAX_VALUE;

		/**
		 * 核心线程数。启用虚拟线程时无效。
		 */
		private int coreSize = 8;

		/**
		 * 允许的最大线程数。队列积压时线程池可扩展至此规模。
		 * 无界队列时忽略；启用虚拟线程时无效。
		 */
		private int maxSize = Integer.MAX_VALUE;

		/**
		 * 是否允许核心线程超时退出，以实现线程池动态伸缩。
		 * 启用虚拟线程时无效。
		 */
		private boolean allowCoreThreadTimeout = true;

		/**
		 * 线程空闲后被终止前的最长等待时间。
		 * 启用虚拟线程时无效。
		 */
		private Duration keepAlive = Duration.ofSeconds(60);

		private final Shutdown shutdown = new Shutdown();

		public int getQueueCapacity() {
			return this.queueCapacity;
		}

		public void setQueueCapacity(int queueCapacity) {
			this.queueCapacity = queueCapacity;
		}

		public int getCoreSize() {
			return this.coreSize;
		}

		public void setCoreSize(int coreSize) {
			this.coreSize = coreSize;
		}

		public int getMaxSize() {
			return this.maxSize;
		}

		public void setMaxSize(int maxSize) {
			this.maxSize = maxSize;
		}

		public boolean isAllowCoreThreadTimeout() {
			return this.allowCoreThreadTimeout;
		}

		public void setAllowCoreThreadTimeout(boolean allowCoreThreadTimeout) {
			this.allowCoreThreadTimeout = allowCoreThreadTimeout;
		}

		public Duration getKeepAlive() {
			return this.keepAlive;
		}

		public void setKeepAlive(Duration keepAlive) {
			this.keepAlive = keepAlive;
		}

		public Shutdown getShutdown() {
			return this.shutdown;
		}

		public static class Shutdown {

			/**
			 * 应用上下文关闭阶段开始后是否仍接受新任务。
			 */
			private boolean acceptTasksAfterContextClose;

			public boolean isAcceptTasksAfterContextClose() {
				return this.acceptTasksAfterContextClose;
			}

			public void setAcceptTasksAfterContextClose(boolean acceptTasksAfterContextClose) {
				this.acceptTasksAfterContextClose = acceptTasksAfterContextClose;
			}

		}

	}

	public static class Shutdown {

		/**
		 * 关闭时执行器是否等待已调度任务完成。
		 */
		private boolean awaitTermination;

		/**
		 * 执行器等待剩余任务完成的最长时间。
		 */
		private @Nullable Duration awaitTerminationPeriod;

		public boolean isAwaitTermination() {
			return this.awaitTermination;
		}

		public void setAwaitTermination(boolean awaitTermination) {
			this.awaitTermination = awaitTermination;
		}

		public @Nullable Duration getAwaitTerminationPeriod() {
			return this.awaitTerminationPeriod;
		}

		public void setAwaitTerminationPeriod(@Nullable Duration awaitTerminationPeriod) {
			this.awaitTerminationPeriod = awaitTerminationPeriod;
		}

	}

	/**
	 * 决定何时创建任务执行器。
	 *
	 * @since 3.5.0
	 */
	public enum Mode {

		/**
		 * 若不存在用户自定义执行器则创建任务执行器。
		 */
		AUTO,

		/**
		 * 即使存在用户自定义执行器也创建任务执行器。
		 */
		FORCE

	}

}
