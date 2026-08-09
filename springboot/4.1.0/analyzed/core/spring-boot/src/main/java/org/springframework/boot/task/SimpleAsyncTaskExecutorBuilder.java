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

import org.springframework.beans.BeanUtils;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskDecorator;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

/**
 * 用于配置并创建 {@link SimpleAsyncTaskExecutor} 的 Builder。
 * 提供便捷方法设置常用 {@link SimpleAsyncTaskExecutor} 参数并注册
 * {@link #taskDecorator(TaskDecorator)}。高级配置可使用 {@link SimpleAsyncTaskExecutorCustomizer}。
 * <p>
 * 在典型的 Spring Boot 自动配置应用中，此 Builder 作为 Bean 可用，
 * 需要 {@link SimpleAsyncTaskExecutor} 时可注入。
 *
 * @author Stephane Nicoll
 * @author Filip Hrisafov
 * @author Moritz Halbritter
 * @author Yanming Zhou
 * @since 3.2.0
 */
public class SimpleAsyncTaskExecutorBuilder {

	private final @Nullable Boolean virtualThreads;

	private final @Nullable String threadNamePrefix;

	private final boolean cancelRemainingTasksOnClose;

	private final boolean rejectTasksWhenLimitReached;

	private final @Nullable Integer concurrencyLimit;

	private final @Nullable TaskDecorator taskDecorator;

	private final @Nullable Set<SimpleAsyncTaskExecutorCustomizer> customizers;

	private final @Nullable Duration taskTerminationTimeout;

	public SimpleAsyncTaskExecutorBuilder() {
		this(null, null, false, false, null, null, null, null);
	}

	private SimpleAsyncTaskExecutorBuilder(@Nullable Boolean virtualThreads, @Nullable String threadNamePrefix,
			boolean cancelRemainingTasksOnClose, boolean rejectTasksWhenLimitReached,
			@Nullable Integer concurrencyLimit, @Nullable TaskDecorator taskDecorator,
			@Nullable Set<SimpleAsyncTaskExecutorCustomizer> customizers, @Nullable Duration taskTerminationTimeout) {
		this.virtualThreads = virtualThreads;
		this.threadNamePrefix = threadNamePrefix;
		this.cancelRemainingTasksOnClose = cancelRemainingTasksOnClose;
		this.rejectTasksWhenLimitReached = rejectTasksWhenLimitReached;
		this.concurrencyLimit = concurrencyLimit;
		this.taskDecorator = taskDecorator;
		this.customizers = customizers;
		this.taskTerminationTimeout = taskTerminationTimeout;
	}

	/**
	 * 设置新建线程名称的前缀。
	 *
	 * @param threadNamePrefix the thread name prefix to set 线程名前缀
	 * @return a new builder instance 新的 Builder 实例
	 */
	public SimpleAsyncTaskExecutorBuilder threadNamePrefix(@Nullable String threadNamePrefix) {
		return new SimpleAsyncTaskExecutorBuilder(this.virtualThreads, threadNamePrefix,
				this.cancelRemainingTasksOnClose, this.rejectTasksWhenLimitReached, this.concurrencyLimit,
				this.taskDecorator, this.customizers, this.taskTerminationTimeout);
	}

	/**
	 * 设置是否使用虚拟线程。
	 *
	 * @param virtualThreads whether to use virtual threads 是否使用虚拟线程
	 * @return a new builder instance 新的 Builder 实例
	 */
	public SimpleAsyncTaskExecutorBuilder virtualThreads(@Nullable Boolean virtualThreads) {
		return new SimpleAsyncTaskExecutorBuilder(virtualThreads, this.threadNamePrefix,
				this.cancelRemainingTasksOnClose, this.rejectTasksWhenLimitReached, this.concurrencyLimit,
				this.taskDecorator, this.customizers, this.taskTerminationTimeout);
	}

	/**
	 * 设置关闭时是否取消剩余任务。默认 {@code false}：不跟踪活动线程，
	 * 或在 {@link #taskTerminationTimeout(Duration) taskTerminationTimeout} 超时后中断未完成线程。
	 * 设为 {@code true} 则在关闭时立即中断，可配合或不配合终止超时。
	 *
	 * @param cancelRemainingTasksOnClose whether to cancel remaining tasks on close 关闭时是否取消剩余任务
	 * @return a new builder instance 新的 Builder 实例
	 * @since 4.0.0
	 */
	public SimpleAsyncTaskExecutorBuilder cancelRemainingTasksOnClose(boolean cancelRemainingTasksOnClose) {
		return new SimpleAsyncTaskExecutorBuilder(this.virtualThreads, this.threadNamePrefix,
				cancelRemainingTasksOnClose, this.rejectTasksWhenLimitReached, this.concurrencyLimit,
				this.taskDecorator, this.customizers, this.taskTerminationTimeout);
	}

	/**
	 * 设置达到并发上限时是否拒绝任务。默认 {@code false} 阻塞调用方直至可接受提交；
	 * 设为 {@code true} 则立即拒绝。
	 *
	 * @param rejectTasksWhenLimitReached whether to reject tasks when the concurrency
	 * limit has been reached 达到并发上限时是否拒绝任务
	 * @return a new builder instance 新的 Builder 实例
	 * @since 3.5.0
	 */
	public SimpleAsyncTaskExecutorBuilder rejectTasksWhenLimitReached(boolean rejectTasksWhenLimitReached) {
		return new SimpleAsyncTaskExecutorBuilder(this.virtualThreads, this.threadNamePrefix,
				this.cancelRemainingTasksOnClose, rejectTasksWhenLimitReached, this.concurrencyLimit,
				this.taskDecorator, this.customizers, this.taskTerminationTimeout);
	}

	/**
	 * 设置并发上限。
	 *
	 * @param concurrencyLimit the concurrency limit 并发上限
	 * @return a new builder instance 新的 Builder 实例
	 */
	public SimpleAsyncTaskExecutorBuilder concurrencyLimit(@Nullable Integer concurrencyLimit) {
		return new SimpleAsyncTaskExecutorBuilder(this.virtualThreads, this.threadNamePrefix,
				this.cancelRemainingTasksOnClose, this.rejectTasksWhenLimitReached, concurrencyLimit,
				this.taskDecorator, this.customizers, this.taskTerminationTimeout);
	}

	/**
	 * 设置要使用的 {@link TaskDecorator}；{@code null} 表示不使用。
	 *
	 * @param taskDecorator the task decorator to use 任务装饰器
	 * @return a new builder instance 新的 Builder 实例
	 */
	public SimpleAsyncTaskExecutorBuilder taskDecorator(@Nullable TaskDecorator taskDecorator) {
		return new SimpleAsyncTaskExecutorBuilder(this.virtualThreads, this.threadNamePrefix,
				this.cancelRemainingTasksOnClose, this.rejectTasksWhenLimitReached, this.concurrencyLimit,
				taskDecorator, this.customizers, this.taskTerminationTimeout);
	}

	/**
	 * 设置任务终止超时时间。
	 *
	 * @param taskTerminationTimeout the task termination timeout 任务终止超时
	 * @return a new builder instance 新的 Builder 实例
	 * @since 3.2.1
	 */
	public SimpleAsyncTaskExecutorBuilder taskTerminationTimeout(@Nullable Duration taskTerminationTimeout) {
		return new SimpleAsyncTaskExecutorBuilder(this.virtualThreads, this.threadNamePrefix,
				this.cancelRemainingTasksOnClose, this.rejectTasksWhenLimitReached, this.concurrencyLimit,
				this.taskDecorator, this.customizers, taskTerminationTimeout);
	}

	/**
	 * 设置应应用于 {@link SimpleAsyncTaskExecutor} 的 {@link SimpleAsyncTaskExecutorCustomizer customizers}。
	 * 在 Builder 配置应用后按添加顺序执行；设置此值会替换先前配置的全部 customizer。
	 *
	 * @param customizers the customizers to set 要设置的 customizer
	 * @return a new builder instance 新的 Builder 实例
	 * @see #additionalCustomizers(SimpleAsyncTaskExecutorCustomizer...)
	 */
	public SimpleAsyncTaskExecutorBuilder customizers(SimpleAsyncTaskExecutorCustomizer... customizers) {
		Assert.notNull(customizers, "'customizers' must not be null");
		return customizers(Arrays.asList(customizers));
	}

	/**
	 * 设置应应用于 {@link SimpleAsyncTaskExecutor} 的 {@link SimpleAsyncTaskExecutorCustomizer customizers}。
	 * 在 Builder 配置应用后按添加顺序执行；设置此值会替换先前配置的全部 customizer。
	 *
	 * @param customizers the customizers to set 要设置的 customizer
	 * @return a new builder instance 新的 Builder 实例
	 * @see #additionalCustomizers(Iterable)
	 */
	public SimpleAsyncTaskExecutorBuilder customizers(
			Iterable<? extends SimpleAsyncTaskExecutorCustomizer> customizers) {
		Assert.notNull(customizers, "'customizers' must not be null");
		return new SimpleAsyncTaskExecutorBuilder(this.virtualThreads, this.threadNamePrefix,
				this.cancelRemainingTasksOnClose, this.rejectTasksWhenLimitReached, this.concurrencyLimit,
				this.taskDecorator, append(null, customizers), this.taskTerminationTimeout);
	}

	/**
	 * 追加应应用于 {@link SimpleAsyncTaskExecutor} 的 {@link SimpleAsyncTaskExecutorCustomizer customizers}。
	 * 在 Builder 配置应用后按添加顺序执行。
	 *
	 * @param customizers the customizers to add 要追加的 customizer
	 * @return a new builder instance 新的 Builder 实例
	 * @see #customizers(SimpleAsyncTaskExecutorCustomizer...)
	 */
	public SimpleAsyncTaskExecutorBuilder additionalCustomizers(SimpleAsyncTaskExecutorCustomizer... customizers) {
		Assert.notNull(customizers, "'customizers' must not be null");
		return additionalCustomizers(Arrays.asList(customizers));
	}

	/**
	 * 追加应应用于 {@link SimpleAsyncTaskExecutor} 的 {@link SimpleAsyncTaskExecutorCustomizer customizers}。
	 * 在 Builder 配置应用后按添加顺序执行。
	 *
	 * @param customizers the customizers to add 要追加的 customizer
	 * @return a new builder instance 新的 Builder 实例
	 * @see #customizers(Iterable)
	 */
	public SimpleAsyncTaskExecutorBuilder additionalCustomizers(
			Iterable<? extends SimpleAsyncTaskExecutorCustomizer> customizers) {
		Assert.notNull(customizers, "'customizers' must not be null");
		return new SimpleAsyncTaskExecutorBuilder(this.virtualThreads, this.threadNamePrefix,
				this.cancelRemainingTasksOnClose, this.rejectTasksWhenLimitReached, this.concurrencyLimit,
				this.taskDecorator, append(this.customizers, customizers), this.taskTerminationTimeout);
	}

	/**
	 * 构建新的 {@link SimpleAsyncTaskExecutor} 实例并用本 Builder 配置。
	 *
	 * @return a configured {@link SimpleAsyncTaskExecutor} instance. 已配置的实例
	 * @see #build(Class)
	 * @see #configure(SimpleAsyncTaskExecutor)
	 */
	public SimpleAsyncTaskExecutor build() {
		return configure(new SimpleAsyncTaskExecutor());
	}

	/**
	 * 构建指定类型的 {@link SimpleAsyncTaskExecutor} 实例并用本 Builder 配置。
	 *
	 * @param <T> the type of task executor 任务执行器类型
	 * @param taskExecutorClass the template type to create 要实例化的类型
	 * @return a configured {@link SimpleAsyncTaskExecutor} instance. 已配置的实例
	 * @see #build()
	 * @see #configure(SimpleAsyncTaskExecutor)
	 */
	public <T extends SimpleAsyncTaskExecutor> T build(Class<T> taskExecutorClass) {
		return configure(BeanUtils.instantiateClass(taskExecutorClass));
	}

	/**
	 * 使用本 Builder 配置给定的 {@link SimpleAsyncTaskExecutor} 实例。
	 *
	 * @param <T> the type of task executor 任务执行器类型
	 * @param taskExecutor the {@link SimpleAsyncTaskExecutor} to configure 待配置的执行器
	 * @return the task executor instance 任务执行器实例
	 * @see #build()
	 * @see #build(Class)
	 */
	public <T extends SimpleAsyncTaskExecutor> T configure(T taskExecutor) {
		PropertyMapper map = PropertyMapper.get();
		map.from(this.virtualThreads).to(taskExecutor::setVirtualThreads);
		map.from(this.threadNamePrefix).whenHasText().to(taskExecutor::setThreadNamePrefix);
		map.from(this.cancelRemainingTasksOnClose).to(taskExecutor::setCancelRemainingTasksOnClose);
		map.from(this.rejectTasksWhenLimitReached).to(taskExecutor::setRejectTasksWhenLimitReached);
		map.from(this.concurrencyLimit).to(taskExecutor::setConcurrencyLimit);
		map.from(this.taskDecorator).to(taskExecutor::setTaskDecorator);
		map.from(this.taskTerminationTimeout).as(Duration::toMillis).to(taskExecutor::setTaskTerminationTimeout);
		if (!CollectionUtils.isEmpty(this.customizers)) {
			this.customizers.forEach((customizer) -> customizer.customize(taskExecutor));
		}
		return taskExecutor;
	}

	private <T> Set<T> append(@Nullable Set<T> set, Iterable<? extends T> additions) {
		Set<T> result = new LinkedHashSet<>((set != null) ? set : Collections.emptySet());
		additions.forEach(result::add);
		return Collections.unmodifiableSet(result);
	}

}
