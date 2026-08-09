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
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

/**
 * 用于配置并创建 {@link ThreadPoolTaskExecutor} 的 Builder。
 * 提供便捷方法设置常用 {@link ThreadPoolTaskExecutor} 参数并注册
 * {@link #taskDecorator(TaskDecorator)}。高级配置可使用 {@link ThreadPoolTaskExecutorCustomizer}。
 * <p>
 * 在典型的 Spring Boot 自动配置应用中，此 Builder 作为 Bean 可用，
 * 需要 {@link ThreadPoolTaskExecutor} 时可注入。
 *
 * @author Stephane Nicoll
 * @author Filip Hrisafov
 * @author Yanming Zhou
 * @since 3.2.0
 */
public class ThreadPoolTaskExecutorBuilder {

	private final @Nullable Integer queueCapacity;

	private final @Nullable Integer corePoolSize;

	private final @Nullable Integer maxPoolSize;

	private final @Nullable Boolean allowCoreThreadTimeOut;

	private final @Nullable Duration keepAlive;

	private final @Nullable Boolean acceptTasksAfterContextClose;

	private final @Nullable Boolean awaitTermination;

	private final @Nullable Duration awaitTerminationPeriod;

	private final @Nullable String threadNamePrefix;

	private final @Nullable TaskDecorator taskDecorator;

	private final @Nullable Set<ThreadPoolTaskExecutorCustomizer> customizers;

	public ThreadPoolTaskExecutorBuilder() {
		this.queueCapacity = null;
		this.corePoolSize = null;
		this.maxPoolSize = null;
		this.allowCoreThreadTimeOut = null;
		this.keepAlive = null;
		this.acceptTasksAfterContextClose = null;
		this.awaitTermination = null;
		this.awaitTerminationPeriod = null;
		this.threadNamePrefix = null;
		this.taskDecorator = null;
		this.customizers = null;
	}

	private ThreadPoolTaskExecutorBuilder(@Nullable Integer queueCapacity, @Nullable Integer corePoolSize,
			@Nullable Integer maxPoolSize, @Nullable Boolean allowCoreThreadTimeOut, @Nullable Duration keepAlive,
			@Nullable Boolean acceptTasksAfterContextClose, @Nullable Boolean awaitTermination,
			@Nullable Duration awaitTerminationPeriod, @Nullable String threadNamePrefix,
			@Nullable TaskDecorator taskDecorator, @Nullable Set<ThreadPoolTaskExecutorCustomizer> customizers) {
		this.queueCapacity = queueCapacity;
		this.corePoolSize = corePoolSize;
		this.maxPoolSize = maxPoolSize;
		this.allowCoreThreadTimeOut = allowCoreThreadTimeOut;
		this.keepAlive = keepAlive;
		this.acceptTasksAfterContextClose = acceptTasksAfterContextClose;
		this.awaitTermination = awaitTermination;
		this.awaitTerminationPeriod = awaitTerminationPeriod;
		this.threadNamePrefix = threadNamePrefix;
		this.taskDecorator = taskDecorator;
		this.customizers = customizers;
	}

	/**
	 * 设置队列容量。无界队列不会扩展线程池，因此忽略 {@link #maxPoolSize(int) maxPoolSize}。
	 *
	 * @param queueCapacity the queue capacity to set 队列容量
	 * @return a new builder instance 新的 Builder 实例
	 */
	public ThreadPoolTaskExecutorBuilder queueCapacity(int queueCapacity) {
		return new ThreadPoolTaskExecutorBuilder(queueCapacity, this.corePoolSize, this.maxPoolSize,
				this.allowCoreThreadTimeOut, this.keepAlive, this.acceptTasksAfterContextClose, this.awaitTermination,
				this.awaitTerminationPeriod, this.threadNamePrefix, this.taskDecorator, this.customizers);
	}

	/**
	 * 设置核心线程数。在队列未满时，这实际上也是最大线程数。
	 * <p>
	 * 若启用 {@link #allowCoreThreadTimeOut(boolean)}，核心线程可动态增减。
	 *
	 * @param corePoolSize the core pool size to set 核心池大小
	 * @return a new builder instance 新的 Builder 实例
	 */
	public ThreadPoolTaskExecutorBuilder corePoolSize(int corePoolSize) {
		return new ThreadPoolTaskExecutorBuilder(this.queueCapacity, corePoolSize, this.maxPoolSize,
				this.allowCoreThreadTimeOut, this.keepAlive, this.acceptTasksAfterContextClose, this.awaitTermination,
				this.awaitTerminationPeriod, this.threadNamePrefix, this.taskDecorator, this.customizers);
	}

	/**
	 * 设置允许的最大线程数。当 {@link #queueCapacity(int) queue} 已满时，
	 * 线程池可扩展至此规模以应对负载。
	 * <p>
	 * 若 {@link #queueCapacity(int) queue capacity} 无界，则忽略此设置。
	 *
	 * @param maxPoolSize the max pool size to set 最大池大小
	 * @return a new builder instance 新的 Builder 实例
	 */
	public ThreadPoolTaskExecutorBuilder maxPoolSize(int maxPoolSize) {
		return new ThreadPoolTaskExecutorBuilder(this.queueCapacity, this.corePoolSize, maxPoolSize,
				this.allowCoreThreadTimeOut, this.keepAlive, this.acceptTasksAfterContextClose, this.awaitTermination,
				this.awaitTerminationPeriod, this.threadNamePrefix, this.taskDecorator, this.customizers);
	}

	/**
	 * 设置是否允许核心线程超时。启用后线程池可动态扩缩。
	 *
	 * @param allowCoreThreadTimeOut if core threads are allowed to time out 是否允许核心线程超时
	 * @return a new builder instance 新的 Builder 实例
	 */
	public ThreadPoolTaskExecutorBuilder allowCoreThreadTimeOut(boolean allowCoreThreadTimeOut) {
		return new ThreadPoolTaskExecutorBuilder(this.queueCapacity, this.corePoolSize, this.maxPoolSize,
				allowCoreThreadTimeOut, this.keepAlive, this.acceptTasksAfterContextClose, this.awaitTermination,
				this.awaitTerminationPeriod, this.threadNamePrefix, this.taskDecorator, this.customizers);
	}

	/**
	 * 设置线程在终止前可保持空闲的最长时间。
	 *
	 * @param keepAlive the keep alive to set 保活时间
	 * @return a new builder instance 新的 Builder 实例
	 */
	public ThreadPoolTaskExecutorBuilder keepAlive(@Nullable Duration keepAlive) {
		return new ThreadPoolTaskExecutorBuilder(this.queueCapacity, this.corePoolSize, this.maxPoolSize,
				this.allowCoreThreadTimeOut, keepAlive, this.acceptTasksAfterContextClose, this.awaitTermination,
				this.awaitTerminationPeriod, this.threadNamePrefix, this.taskDecorator, this.customizers);
	}

	/**
	 * 设置应用上下文关闭阶段开始后是否仍接受新任务。
	 *
	 * @param acceptTasksAfterContextClose whether to accept further tasks after the
	 * application context close phase has begun 关闭阶段开始后是否接受任务
	 * @return a new builder instance 新的 Builder 实例
	 * @since 3.3.0
	 */
	public ThreadPoolTaskExecutorBuilder acceptTasksAfterContextClose(boolean acceptTasksAfterContextClose) {
		return new ThreadPoolTaskExecutorBuilder(this.queueCapacity, this.corePoolSize, this.maxPoolSize,
				this.allowCoreThreadTimeOut, this.keepAlive, acceptTasksAfterContextClose, this.awaitTermination,
				this.awaitTerminationPeriod, this.threadNamePrefix, this.taskDecorator, this.customizers);
	}

	/**
	 * 设置关闭时执行器是否等待已调度任务完成，
	 * 不中断运行中任务并执行队列中全部任务。
	 *
	 * @param awaitTermination whether the executor needs to wait for the tasks to
	 * complete on shutdown 关闭时是否等待任务完成
	 * @return a new builder instance 新的 Builder 实例
	 * @see #awaitTerminationPeriod(Duration)
	 */
	public ThreadPoolTaskExecutorBuilder awaitTermination(boolean awaitTermination) {
		return new ThreadPoolTaskExecutorBuilder(this.queueCapacity, this.corePoolSize, this.maxPoolSize,
				this.allowCoreThreadTimeOut, this.keepAlive, this.acceptTasksAfterContextClose, awaitTermination,
				this.awaitTerminationPeriod, this.threadNamePrefix, this.taskDecorator, this.customizers);
	}

	/**
	 * 设置关闭时执行器最长阻塞等待时间。设置后，执行器在关闭时会阻塞，
	 * 等待剩余任务完成后再继续容器关闭流程。
	 * 当剩余任务可能依赖容器管理的其他资源时尤其有用。
	 *
	 * @param awaitTerminationPeriod the await termination period to set 关闭等待时长
	 * @return a new builder instance 新的 Builder 实例
	 */
	public ThreadPoolTaskExecutorBuilder awaitTerminationPeriod(@Nullable Duration awaitTerminationPeriod) {
		return new ThreadPoolTaskExecutorBuilder(this.queueCapacity, this.corePoolSize, this.maxPoolSize,
				this.allowCoreThreadTimeOut, this.keepAlive, this.acceptTasksAfterContextClose, this.awaitTermination,
				awaitTerminationPeriod, this.threadNamePrefix, this.taskDecorator, this.customizers);
	}

	/**
	 * 设置新建线程名称的前缀。
	 *
	 * @param threadNamePrefix the thread name prefix to set 线程名前缀
	 * @return a new builder instance 新的 Builder 实例
	 */
	public ThreadPoolTaskExecutorBuilder threadNamePrefix(@Nullable String threadNamePrefix) {
		return new ThreadPoolTaskExecutorBuilder(this.queueCapacity, this.corePoolSize, this.maxPoolSize,
				this.allowCoreThreadTimeOut, this.keepAlive, this.acceptTasksAfterContextClose, this.awaitTermination,
				this.awaitTerminationPeriod, threadNamePrefix, this.taskDecorator, this.customizers);
	}

	/**
	 * 设置要使用的 {@link TaskDecorator}；{@code null} 表示不使用。
	 *
	 * @param taskDecorator the task decorator to use 任务装饰器
	 * @return a new builder instance 新的 Builder 实例
	 */
	public ThreadPoolTaskExecutorBuilder taskDecorator(@Nullable TaskDecorator taskDecorator) {
		return new ThreadPoolTaskExecutorBuilder(this.queueCapacity, this.corePoolSize, this.maxPoolSize,
				this.allowCoreThreadTimeOut, this.keepAlive, this.acceptTasksAfterContextClose, this.awaitTermination,
				this.awaitTerminationPeriod, this.threadNamePrefix, taskDecorator, this.customizers);
	}

	/**
	 * 设置应应用于 {@link ThreadPoolTaskExecutor} 的 {@link ThreadPoolTaskExecutorCustomizer ThreadPoolTaskExecutorCustomizers}。
	 * 在 Builder 配置应用后按添加顺序执行；设置此值会替换先前配置的全部 customizer。
	 *
	 * @param customizers the customizers to set 要设置的 customizer
	 * @return a new builder instance 新的 Builder 实例
	 * @see #additionalCustomizers(ThreadPoolTaskExecutorCustomizer...)
	 */
	public ThreadPoolTaskExecutorBuilder customizers(ThreadPoolTaskExecutorCustomizer... customizers) {
		Assert.notNull(customizers, "'customizers' must not be null");
		return customizers(Arrays.asList(customizers));
	}

	/**
	 * Set the {@link ThreadPoolTaskExecutorCustomizer ThreadPoolTaskExecutorCustomizers}
	 * that should be applied to the {@link ThreadPoolTaskExecutor}. Customizers are
	 * applied in the order that they were added after builder configuration has been
	 * applied. Setting this value will replace any previously configured customizers.
	 * @param customizers the customizers to set
	 * @return a new builder instance
	 * @see #additionalCustomizers(ThreadPoolTaskExecutorCustomizer...)
	 */
	public ThreadPoolTaskExecutorBuilder customizers(Iterable<? extends ThreadPoolTaskExecutorCustomizer> customizers) {
		Assert.notNull(customizers, "'customizers' must not be null");
		return new ThreadPoolTaskExecutorBuilder(this.queueCapacity, this.corePoolSize, this.maxPoolSize,
				this.allowCoreThreadTimeOut, this.keepAlive, this.acceptTasksAfterContextClose, this.awaitTermination,
				this.awaitTerminationPeriod, this.threadNamePrefix, this.taskDecorator, append(null, customizers));
	}

	/**
	 * 追加应应用于 {@link ThreadPoolTaskExecutor} 的 {@link ThreadPoolTaskExecutorCustomizer ThreadPoolTaskExecutorCustomizers}。
	 * 在 Builder 配置应用后按添加顺序执行。
	 *
	 * @param customizers the customizers to add 要追加的 customizer
	 * @return a new builder instance 新的 Builder 实例
	 * @see #customizers(ThreadPoolTaskExecutorCustomizer...)
	 */
	public ThreadPoolTaskExecutorBuilder additionalCustomizers(ThreadPoolTaskExecutorCustomizer... customizers) {
		Assert.notNull(customizers, "'customizers' must not be null");
		return additionalCustomizers(Arrays.asList(customizers));
	}

	/**
	 * 追加应应用于 {@link ThreadPoolTaskExecutor} 的 {@link ThreadPoolTaskExecutorCustomizer ThreadPoolTaskExecutorCustomizers}。
	 * 在 Builder 配置应用后按添加顺序执行。
	 *
	 * @param customizers the customizers to add 要追加的 customizer
	 * @return a new builder instance 新的 Builder 实例
	 * @see #customizers(ThreadPoolTaskExecutorCustomizer...)
	 */
	public ThreadPoolTaskExecutorBuilder additionalCustomizers(
			Iterable<? extends ThreadPoolTaskExecutorCustomizer> customizers) {
		Assert.notNull(customizers, "'customizers' must not be null");
		return new ThreadPoolTaskExecutorBuilder(this.queueCapacity, this.corePoolSize, this.maxPoolSize,
				this.allowCoreThreadTimeOut, this.keepAlive, this.acceptTasksAfterContextClose, this.awaitTermination,
				this.awaitTerminationPeriod, this.threadNamePrefix, this.taskDecorator,
				append(this.customizers, customizers));
	}

	/**
	 * 构建新的 {@link ThreadPoolTaskExecutor} 实例并用本 Builder 配置。
	 *
	 * @return a configured {@link ThreadPoolTaskExecutor} instance. 已配置的实例
	 * @see #build(Class)
	 * @see #configure(ThreadPoolTaskExecutor)
	 */
	public ThreadPoolTaskExecutor build() {
		return configure(new ThreadPoolTaskExecutor());
	}

	/**
	 * 构建指定类型的 {@link ThreadPoolTaskExecutor} 实例并用本 Builder 配置。
	 *
	 * @param <T> the type of task executor 任务执行器类型
	 * @param taskExecutorClass the template type to create 要实例化的类型
	 * @return a configured {@link ThreadPoolTaskExecutor} instance. 已配置的实例
	 * @see #build()
	 * @see #configure(ThreadPoolTaskExecutor)
	 */
	public <T extends ThreadPoolTaskExecutor> T build(Class<T> taskExecutorClass) {
		return configure(BeanUtils.instantiateClass(taskExecutorClass));
	}

	/**
	 * 使用本 Builder 配置给定的 {@link ThreadPoolTaskExecutor} 实例。
	 *
	 * @param <T> the type of task executor 任务执行器类型
	 * @param taskExecutor the {@link ThreadPoolTaskExecutor} to configure 待配置的执行器
	 * @return the task executor instance 任务执行器实例
	 * @see #build()
	 * @see #build(Class)
	 */
	public <T extends ThreadPoolTaskExecutor> T configure(T taskExecutor) {
		PropertyMapper map = PropertyMapper.get();
		map.from(this.queueCapacity).to(taskExecutor::setQueueCapacity);
		map.from(this.corePoolSize).to(taskExecutor::setCorePoolSize);
		map.from(this.maxPoolSize).to(taskExecutor::setMaxPoolSize);
		map.from(this.keepAlive).asInt(Duration::getSeconds).to(taskExecutor::setKeepAliveSeconds);
		map.from(this.allowCoreThreadTimeOut).to(taskExecutor::setAllowCoreThreadTimeOut);
		map.from(this.acceptTasksAfterContextClose).to(taskExecutor::setAcceptTasksAfterContextClose);
		map.from(this.awaitTermination).to(taskExecutor::setWaitForTasksToCompleteOnShutdown);
		map.from(this.awaitTerminationPeriod).as(Duration::toMillis).to(taskExecutor::setAwaitTerminationMillis);
		map.from(this.threadNamePrefix).whenHasText().to(taskExecutor::setThreadNamePrefix);
		map.from(this.taskDecorator).to(taskExecutor::setTaskDecorator);
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
