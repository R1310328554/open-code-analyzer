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
 * 任务调度的配置属性。
 *
 * @author Stephane Nicoll
 * @since 2.1.0
 */
@ConfigurationProperties("spring.task.scheduling")
public class TaskSchedulingProperties {

	private final Pool pool = new Pool();

	private final Simple simple = new Simple();

	private final Shutdown shutdown = new Shutdown();

	/**
	 * 新创建线程名称使用的前缀。
	 */
	private String threadNamePrefix = "scheduling-";

	public Pool getPool() {
		return this.pool;
	}

	public Simple getSimple() {
		return this.simple;
	}

	public Shutdown getShutdown() {
		return this.shutdown;
	}

	public String getThreadNamePrefix() {
		return this.threadNamePrefix;
	}

	public void setThreadNamePrefix(String threadNamePrefix) {
		this.threadNamePrefix = threadNamePrefix;
	}

	public static class Pool {

		/**
		 * 允许的最大线程数。启用虚拟线程时无效。
		 */
		private int size = 1;

		public int getSize() {
			return this.size;
		}

		public void setSize(int size) {
			this.size = size;
		}

	}

	public static class Simple {

		/**
		 * 设置允许的最大并行访问数。{@code -1} 表示完全不限制并发。
		 */
		private @Nullable Integer concurrencyLimit;

		public @Nullable Integer getConcurrencyLimit() {
			return this.concurrencyLimit;
		}

		public void setConcurrencyLimit(@Nullable Integer concurrencyLimit) {
			this.concurrencyLimit = concurrencyLimit;
		}

	}

	public static class Shutdown {

		/**
		 * 关闭时执行器是否应等待已调度任务完成。
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

}
