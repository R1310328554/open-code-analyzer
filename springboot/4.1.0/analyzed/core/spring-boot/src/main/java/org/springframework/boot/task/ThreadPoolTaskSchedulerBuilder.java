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
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

/**
 * 用于配置并创建 {@link ThreadPoolTaskScheduler} 的 Builder。
 * 提供便捷方法设置常用 {@link ThreadPoolTaskScheduler} 参数。
 * 高级配置可使用 {@link ThreadPoolTaskSchedulerCustomizer}。
 * <p>
 * 在典型的 Spring Boot 自动配置应用中，此 Builder 作为 Bean 可用，
 * 需要 {@link ThreadPoolTaskScheduler} 时可注入。
 *
 * @author Stephane Nicoll
 * @since 3.2.0
 */
public class ThreadPoolTaskSchedulerBuilder {

	private final @Nullable Integer poolSize;

	private final @Nullable Boolean awaitTermination;

	private final @Nullable Duration awaitTerminationPeriod;

	private final @Nullable String threadNamePrefix;

	private final @Nullable TaskDecorator taskDecorator;

	private final @Nullable Set<ThreadPoolTaskSchedulerCustomizer> customizers;

	public ThreadPoolTaskSchedulerBuilder() {
		this(null, null, null, null, null, null);
	}

	private ThreadPoolTaskSchedulerBuilder(@Nullable Integer poolSize, @Nullable Boolean awaitTermination,
			@Nullable Duration awaitTerminationPeriod, @Nullable String threadNamePrefix,
			@Nullable TaskDecorator taskDecorator,
			@Nullable Set<ThreadPoolTaskSchedulerCustomizer> taskSchedulerCustomizers) {
		this.poolSize = poolSize;
		this.awaitTermination = awaitTermination;
		this.awaitTerminationPeriod = awaitTerminationPeriod;
		this.threadNamePrefix = threadNamePrefix;
		this.taskDecorator = taskDecorator;
		this.customizers = taskSchedulerCustomizers;
	}

	/**
	 * 设置允许的最大线程数。
	 *
	 * @param poolSize the pool size to set 池大小
	 * @return a new builder instance 新的 Builder 实例
	 */
	public ThreadPoolTaskSchedulerBuilder poolSize(int poolSize) {
		return new ThreadPoolTaskSchedulerBuilder(poolSize, this.awaitTermination, this.awaitTerminationPeriod,
				this.threadNamePrefix, this.taskDecorator, this.customizers);
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
	public ThreadPoolTaskSchedulerBuilder awaitTermination(boolean awaitTermination) {
		return new ThreadPoolTaskSchedulerBuilder(this.poolSize, awaitTermination, this.awaitTerminationPeriod,
				this.threadNamePrefix, this.taskDecorator, this.customizers);
	}

	/**
	 * 设置关闭时执行器最长阻塞等待时间。设置后，执行器在关闭时会阻塞，
	 * 等待剩余任务完成后再继续容器关闭流程。
	 * 当剩余任务可能依赖容器管理的其他资源时尤其有用。
	 *
	 * @param awaitTerminationPeriod the await termination period to set 关闭等待时长
	 * @return a new builder instance 新的 Builder 实例
	 */
	public ThreadPoolTaskSchedulerBuilder awaitTerminationPeriod(@Nullable Duration awaitTerminationPeriod) {
		return new ThreadPoolTaskSchedulerBuilder(this.poolSize, this.awaitTermination, awaitTerminationPeriod,
				this.threadNamePrefix, this.taskDecorator, this.customizers);
	}

	/**
	 * 设置新建线程名称的前缀。
	 *
	 * @param threadNamePrefix the thread name prefix to set 线程名前缀
	 * @return a new builder instance 新的 Builder 实例
	 */
	public ThreadPoolTaskSchedulerBuilder threadNamePrefix(@Nullable String threadNamePrefix) {
		return new ThreadPoolTaskSchedulerBuilder(this.poolSize, this.awaitTermination, this.awaitTerminationPeriod,
				threadNamePrefix, this.taskDecorator, this.customizers);
	}

	/**
	 * 设置应用于 {@link ThreadPoolTaskScheduler} 的 {@link TaskDecorator}。
	 *
	 * @param taskDecorator the task decorator to set 任务装饰器
	 * @return a new builder instance 新的 Builder 实例
	 * @since 3.5.0
	 */
	public ThreadPoolTaskSchedulerBuilder taskDecorator(@Nullable TaskDecorator taskDecorator) {
		return new ThreadPoolTaskSchedulerBuilder(this.poolSize, this.awaitTermination, this.awaitTerminationPeriod,
				this.threadNamePrefix, taskDecorator, this.customizers);
	}

	/**
	 * 设置应应用于 {@link ThreadPoolTaskScheduler} 的 {@link ThreadPoolTaskSchedulerCustomizer threadPoolTaskSchedulerCustomizers}。
	 * 在 Builder 配置应用后按添加顺序执行；设置此值会替换先前配置的全部 customizer。
	 *
	 * @param customizers the customizers to set 要设置的 customizer
	 * @return a new builder instance 新的 Builder 实例
	 * @see #additionalCustomizers(ThreadPoolTaskSchedulerCustomizer...)
	 */
	public ThreadPoolTaskSchedulerBuilder customizers(ThreadPoolTaskSchedulerCustomizer... customizers) {
		Assert.notNull(customizers, "'customizers' must not be null");
		return customizers(Arrays.asList(customizers));
	}

	/**
	 * 设置应应用于 {@link ThreadPoolTaskScheduler} 的 {@link ThreadPoolTaskSchedulerCustomizer threadPoolTaskSchedulerCustomizers}。
	 * 在 Builder 配置应用后按添加顺序执行；设置此值会替换先前配置的全部 customizer。
	 *
	 * @param customizers the customizers to set 要设置的 customizer
	 * @return a new builder instance 新的 Builder 实例
	 * @see #additionalCustomizers(ThreadPoolTaskSchedulerCustomizer...)
	 */
	public ThreadPoolTaskSchedulerBuilder customizers(
			Iterable<? extends ThreadPoolTaskSchedulerCustomizer> customizers) {
		Assert.notNull(customizers, "'customizers' must not be null");
		return new ThreadPoolTaskSchedulerBuilder(this.poolSize, this.awaitTermination, this.awaitTerminationPeriod,
				this.threadNamePrefix, this.taskDecorator, append(null, customizers));
	}

	/**
	 * 追加应应用于 {@link ThreadPoolTaskScheduler} 的 {@link ThreadPoolTaskSchedulerCustomizer threadPoolTaskSchedulerCustomizers}。
	 * 在 Builder 配置应用后按添加顺序执行。
	 *
	 * @param customizers the customizers to add 要追加的 customizer
	 * @return a new builder instance 新的 Builder 实例
	 * @see #customizers(ThreadPoolTaskSchedulerCustomizer...)
	 */
	public ThreadPoolTaskSchedulerBuilder additionalCustomizers(ThreadPoolTaskSchedulerCustomizer... customizers) {
		Assert.notNull(customizers, "'customizers' must not be null");
		return additionalCustomizers(Arrays.asList(customizers));
	}

	/**
	 * 追加应应用于 {@link ThreadPoolTaskScheduler} 的 {@link ThreadPoolTaskSchedulerCustomizer threadPoolTaskSchedulerCustomizers}。
	 * 在 Builder 配置应用后按添加顺序执行。
	 *
	 * @param customizers the customizers to add 要追加的 customizer
	 * @return a new builder instance 新的 Builder 实例
	 * @see #customizers(ThreadPoolTaskSchedulerCustomizer...)
	 */
	public ThreadPoolTaskSchedulerBuilder additionalCustomizers(
			Iterable<? extends ThreadPoolTaskSchedulerCustomizer> customizers) {
		Assert.notNull(customizers, "'customizers' must not be null");
		return new ThreadPoolTaskSchedulerBuilder(this.poolSize, this.awaitTermination, this.awaitTerminationPeriod,
				this.threadNamePrefix, this.taskDecorator, append(this.customizers, customizers));
	}

	/**
	 * 构建新的 {@link ThreadPoolTaskScheduler} 实例并用本 Builder 配置。
	 *
	 * @return a configured {@link ThreadPoolTaskScheduler} instance. 已配置的实例
	 * @see #configure(ThreadPoolTaskScheduler)
	 */
	public ThreadPoolTaskScheduler build() {
		return configure(new ThreadPoolTaskScheduler());
	}

	/**
	 * 使用本 Builder 配置给定的 {@link ThreadPoolTaskScheduler} 实例。
	 *
	 * @param <T> the type of task scheduler 任务调度器类型
	 * @param taskScheduler the {@link ThreadPoolTaskScheduler} to configure 待配置的调度器
	 * @return the task scheduler instance 任务调度器实例
	 * @see #build()
	 */
	public <T extends ThreadPoolTaskScheduler> T configure(T taskScheduler) {
		PropertyMapper map = PropertyMapper.get();
		map.from(this.poolSize).to(taskScheduler::setPoolSize);
		map.from(this.awaitTermination).to(taskScheduler::setWaitForTasksToCompleteOnShutdown);
		map.from(this.awaitTerminationPeriod).as(Duration::toMillis).to(taskScheduler::setAwaitTerminationMillis);
		map.from(this.threadNamePrefix).to(taskScheduler::setThreadNamePrefix);
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
