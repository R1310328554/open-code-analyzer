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

package org.springframework.boot.task;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.jspecify.annotations.Nullable;

import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.concurrent.SimpleAsyncTaskScheduler;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

/**
 * 用于配置并创建 {@link SimpleAsyncTaskScheduler} 的 Builder。
 * 提供便捷方法设置常用 {@link SimpleAsyncTaskScheduler} 参数。
 * 高级配置可使用 {@link SimpleAsyncTaskSchedulerCustomizer}。
 * <p>
 * 在典型的 Spring Boot 自动配置应用中，此 Builder 作为 Bean 可用，
 * 需要 {@link SimpleAsyncTaskScheduler} 时可注入。
 *
 * @author Stephane Nicoll
 * @author Moritz Halbritter
 * @since 3.2.0
 */
public class SimpleAsyncTaskSchedulerBuilder {

	private final @Nullable String threadNamePrefix;

	private final @Nullable Integer concurrencyLimit;

	private final @Nullable Boolean virtualThreads;

	private final @Nullable Duration taskTerminationTimeout;

	private final @Nullable TaskDecorator taskDecorator;

	private final @Nullable Set<SimpleAsyncTaskSchedulerCustomizer> customizers;

	public SimpleAsyncTaskSchedulerBuilder() {
		this(null, null, null, null, null, null);
	}

	private SimpleAsyncTaskSchedulerBuilder(@Nullable String threadNamePrefix, @Nullable Integer concurrencyLimit,
			@Nullable Boolean virtualThreads, @Nullable Duration taskTerminationTimeout,
			@Nullable TaskDecorator taskDecorator,
			@Nullable Set<SimpleAsyncTaskSchedulerCustomizer> taskSchedulerCustomizers) {
		this.threadNamePrefix = threadNamePrefix;
		this.concurrencyLimit = concurrencyLimit;
		this.virtualThreads = virtualThreads;
		this.customizers = taskSchedulerCustomizers;
		this.taskDecorator = taskDecorator;
		this.taskTerminationTimeout = taskTerminationTimeout;
	}

	/**
	 * 设置新建线程名称的前缀。
	 *
	 * @param threadNamePrefix the thread name prefix to set 线程名前缀
	 * @return a new builder instance 新的 Builder 实例
	 */
	public SimpleAsyncTaskSchedulerBuilder threadNamePrefix(@Nullable String threadNamePrefix) {
		return new SimpleAsyncTaskSchedulerBuilder(threadNamePrefix, this.concurrencyLimit, this.virtualThreads,
				this.taskTerminationTimeout, this.taskDecorator, this.customizers);
	}

	/**
	 * 设置并发上限。
	 *
	 * @param concurrencyLimit the concurrency limit 并发上限
	 * @return a new builder instance 新的 Builder 实例
	 */
	public SimpleAsyncTaskSchedulerBuilder concurrencyLimit(@Nullable Integer concurrencyLimit) {
		return new SimpleAsyncTaskSchedulerBuilder(this.threadNamePrefix, concurrencyLimit, this.virtualThreads,
				this.taskTerminationTimeout, this.taskDecorator, this.customizers);
	}

	/**
	 * 设置是否使用虚拟线程。
	 *
	 * @param virtualThreads whether to use virtual threads 是否使用虚拟线程
	 * @return a new builder instance 新的 Builder 实例
	 */
	public SimpleAsyncTaskSchedulerBuilder virtualThreads(@Nullable Boolean virtualThreads) {
		return new SimpleAsyncTaskSchedulerBuilder(this.threadNamePrefix, this.concurrencyLimit, virtualThreads,
				this.taskTerminationTimeout, this.taskDecorator, this.customizers);
	}

	/**
	 * 设置任务终止超时时间。
	 *
	 * @param taskTerminationTimeout the task termination timeout 任务终止超时
	 * @return a new builder instance 新的 Builder 实例
	 * @since 3.2.1
	 */
	public SimpleAsyncTaskSchedulerBuilder taskTerminationTimeout(@Nullable Duration taskTerminationTimeout) {
		return new SimpleAsyncTaskSchedulerBuilder(this.threadNamePrefix, this.concurrencyLimit, this.virtualThreads,
				taskTerminationTimeout, this.taskDecorator, this.customizers);
	}

	/**
	 * 设置 {@link SimpleAsyncTaskScheduler} 使用的任务装饰器。
	 *
	 * @param taskDecorator the task decorator to set 任务装饰器
	 * @return a new builder instance 新的 Builder 实例
	 * @since 3.5.0
	 */
	public SimpleAsyncTaskSchedulerBuilder taskDecorator(@Nullable TaskDecorator taskDecorator) {
		return new SimpleAsyncTaskSchedulerBuilder(this.threadNamePrefix, this.concurrencyLimit, this.virtualThreads,
				this.taskTerminationTimeout, taskDecorator, this.customizers);
	}

	/**
	 * 设置应应用于 {@link SimpleAsyncTaskScheduler} 的 {@link SimpleAsyncTaskSchedulerCustomizer customizers}。
	 * 在 Builder 配置应用后按添加顺序执行；设置此值会替换先前配置的全部 customizer。
	 *
	 * @param customizers the customizers to set 要设置的 customizer
	 * @return a new builder instance 新的 Builder 实例
	 * @see #additionalCustomizers(SimpleAsyncTaskSchedulerCustomizer...)
	 */
	public SimpleAsyncTaskSchedulerBuilder customizers(SimpleAsyncTaskSchedulerCustomizer... customizers) {
		Assert.notNull(customizers, "'customizers' must not be null");
		return customizers(Arrays.asList(customizers));
	}

	/**
	 * 设置应应用于 {@link SimpleAsyncTaskScheduler} 的 {@link SimpleAsyncTaskSchedulerCustomizer customizers}。
	 * 在 Builder 配置应用后按添加顺序执行；设置此值会替换先前配置的全部 customizer。
	 *
	 * @param customizers the customizers to set 要设置的 customizer
	 * @return a new builder instance 新的 Builder 实例
	 * @see #additionalCustomizers(Iterable)
	 */
	public SimpleAsyncTaskSchedulerBuilder customizers(
			Iterable<? extends SimpleAsyncTaskSchedulerCustomizer> customizers) {
		Assert.notNull(customizers, "'customizers' must not be null");
		return new SimpleAsyncTaskSchedulerBuilder(this.threadNamePrefix, this.concurrencyLimit, this.virtualThreads,
				this.taskTerminationTimeout, this.taskDecorator, append(null, customizers));
	}

	/**
	 * 追加应应用于 {@link SimpleAsyncTaskScheduler} 的 {@link SimpleAsyncTaskSchedulerCustomizer customizers}。
	 * 在 Builder 配置应用后按添加顺序执行。
	 *
	 * @param customizers the customizers to add 要追加的 customizer
	 * @return a new builder instance 新的 Builder 实例
	 * @see #customizers(SimpleAsyncTaskSchedulerCustomizer...)
	 */
	public SimpleAsyncTaskSchedulerBuilder additionalCustomizers(SimpleAsyncTaskSchedulerCustomizer... customizers) {
		Assert.notNull(customizers, "'customizers' must not be null");
		return additionalCustomizers(Arrays.asList(customizers));
	}

	/**
	 * 追加应应用于 {@link SimpleAsyncTaskScheduler} 的 {@link SimpleAsyncTaskSchedulerCustomizer customizers}。
	 * 在 Builder 配置应用后按添加顺序执行。
	 *
	 * @param customizers the customizers to add 要追加的 customizer
	 * @return a new builder instance 新的 Builder 实例
	 * @see #customizers(Iterable)
	 */
	public SimpleAsyncTaskSchedulerBuilder additionalCustomizers(
			Iterable<? extends SimpleAsyncTaskSchedulerCustomizer> customizers) {
		Assert.notNull(customizers, "'customizers' must not be null");
		return new SimpleAsyncTaskSchedulerBuilder(this.threadNamePrefix, this.concurrencyLimit, this.virtualThreads,
				this.taskTerminationTimeout, this.taskDecorator, append(this.customizers, customizers));
	}

	/**
	 * 构建新的 {@link SimpleAsyncTaskScheduler} 实例并用本 Builder 配置。
	 *
	 * @return a configured {@link SimpleAsyncTaskScheduler} instance. 已配置的实例
	 * @see #configure(SimpleAsyncTaskScheduler)
	 */
	public SimpleAsyncTaskScheduler build() {
		return configure(new SimpleAsyncTaskScheduler());
	}

	/**
	 * 使用本 Builder 配置给定的 {@link SimpleAsyncTaskScheduler} 实例。
	 *
	 * @param <T> the type of task scheduler 任务调度器类型
	 * @param taskScheduler the {@link SimpleAsyncTaskScheduler} to configure 待配置的调度器
	 * @return the task scheduler instance 任务调度器实例
	 * @see #build()
	 */
	public <T extends SimpleAsyncTaskScheduler> T configure(T taskScheduler) {
		PropertyMapper map = PropertyMapper.get();
		map.from(this.threadNamePrefix).to(taskScheduler::setThreadNamePrefix);
		map.from(this.concurrencyLimit).to(taskScheduler::setConcurrencyLimit);
		map.from(this.virtualThreads).to(taskScheduler::setVirtualThreads);
		map.from(this.taskTerminationTimeout).as(Duration::toMillis).to(taskScheduler::setTaskTerminationTimeout);
		map.from(this.taskDecorator).to(taskScheduler::setTaskDecorator);
		if (!CollectionUtils.isEmpty(this.customizers)) {
			this.customizers.forEach((customizer) -> customizer.customize(taskScheduler));
		}
		return taskScheduler;
	}

	private <T> Set<T> append(@Nullable Set<T> set, Iterable<? extends T> additions) {
		Set<T> result = new LinkedHashSet<>((set != null) ? set : Collections.emptySet());
		additions.forEach(result::add);
		return Collections.unmodifiableSet(result);
	}

}
