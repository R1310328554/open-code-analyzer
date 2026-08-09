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

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnThreading;
import org.springframework.boot.task.SimpleAsyncTaskSchedulerBuilder;
import org.springframework.boot.task.ThreadPoolTaskSchedulerBuilder;
import org.springframework.boot.thread.Threading;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.SimpleAsyncTaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.TaskSchedulerRouter;

/**
 * 在用户未显式启用任务调度时，可导入以暴露标准 {@link TaskScheduler} 的配置。
 * <p>
 * 若通过 {@code spring.threads.virtual.enabled=true} 启用虚拟线程，
 * 则暴露 {@link SimpleAsyncTaskScheduler}；否则暴露 {@link ThreadPoolTaskScheduler}。
 * <p>
 * 导入此配置的类应排在 {@link TaskSchedulingAutoConfiguration} 之后。
 *
 * @author Phillip Webb
 * @since 4.1.0
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnMissingBean(name = DefaultTaskSchedulerConfiguration.DEFAULT_TASK_SCHEDULER_BEAN_NAME)
public class DefaultTaskSchedulerConfiguration {

	/**
	 * 默认任务调度器的 Bean 名称。
	 */
	public static final String DEFAULT_TASK_SCHEDULER_BEAN_NAME = TaskSchedulerRouter.DEFAULT_TASK_SCHEDULER_BEAN_NAME;

	@Bean(name = DEFAULT_TASK_SCHEDULER_BEAN_NAME)
	@ConditionalOnBean(ThreadPoolTaskSchedulerBuilder.class)
	@ConditionalOnThreading(Threading.PLATFORM)
	ThreadPoolTaskScheduler taskScheduler(ThreadPoolTaskSchedulerBuilder threadPoolTaskSchedulerBuilder) {
		return threadPoolTaskSchedulerBuilder.build();
	}

	@Bean(name = DEFAULT_TASK_SCHEDULER_BEAN_NAME)
	@ConditionalOnBean(SimpleAsyncTaskSchedulerBuilder.class)
	@ConditionalOnThreading(Threading.VIRTUAL)
	SimpleAsyncTaskScheduler taskSchedulerVirtualThreads(
			SimpleAsyncTaskSchedulerBuilder simpleAsyncTaskSchedulerBuilder) {
		return simpleAsyncTaskSchedulerBuilder.build();
	}

}
