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

package org.springframework.scheduling.annotation;

import org.springframework.scheduling.config.ScheduledTaskRegistrar;

/**
 * 由标注 {@link EnableScheduling @EnableScheduling} 的
 * {@link org.springframework.context.annotation.Configuration @Configuration} 类
 * 实现的可选接口。通常用于设置执行定时任务时使用的特定
 * {@link org.springframework.scheduling.TaskScheduler TaskScheduler} Bean，
 * 或以<em>编程</em>方式注册定时任务，而非使用 {@link Scheduled @Scheduled} 注解的
 * <em>声明式</em>方式。例如实现 {@link org.springframework.scheduling.Trigger Trigger}
 * 任务时可能需要，{@code @Scheduled} 不支持此类任务。
 *
 * <p>详细用法示例见 {@link EnableScheduling @EnableScheduling}。
 *
 * @author Chris Beams
 * @since 3.1
 * @see EnableScheduling
 * @see ScheduledTaskRegistrar
 */
@FunctionalInterface
public interface SchedulingConfigurer {

	/**
	 * 回调，允许向给定 {@link ScheduledTaskRegistrar} 注册
	 * {@link org.springframework.scheduling.TaskScheduler} 及特定
	 * {@link org.springframework.scheduling.config.Task} 实例。
	 * @param taskRegistrar 待配置的任务注册器
	 */
	void configureTasks(ScheduledTaskRegistrar taskRegistrar);

}
