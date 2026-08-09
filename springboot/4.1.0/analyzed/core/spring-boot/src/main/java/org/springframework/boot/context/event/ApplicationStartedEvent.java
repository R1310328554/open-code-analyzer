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

package org.springframework.boot.context.event;

import java.time.Duration;

import org.jspecify.annotations.Nullable;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * 应用上下文刷新后、任何 {@link ApplicationRunner 应用} 与 {@link CommandLineRunner 命令行}
 * Runner 调用前发布的事件。
 *
 * @author Andy Wilkinson
 * @since 2.0.0
 */
@SuppressWarnings("serial")
public class ApplicationStartedEvent extends SpringApplicationEvent {

	private final ConfigurableApplicationContext context;

	private final @Nullable Duration timeTaken;

	/**
	 * 创建新的 {@link ApplicationStartedEvent} 实例。
	 *
	 * @param application 当前应用
	 * @param args 应用运行参数
	 * @param context 正在创建的上下文
	 * @param timeTaken 应用启动所耗时间
	 * @since 2.6.0
	 */
	public ApplicationStartedEvent(SpringApplication application, String[] args, ConfigurableApplicationContext context,
			@Nullable Duration timeTaken) {
		super(application, args);
		this.context = context;
		this.timeTaken = timeTaken;
	}

	/**
	 * 返回应用上下文。
	 *
	 * @return 上下文
	 */
	public ConfigurableApplicationContext getApplicationContext() {
		return this.context;
	}

	/**
	 * 返回应用启动所耗时间；未知时为 {@code null}。
	 *
	 * @return 启动耗时
	 * @since 2.6.0
	 */
	public @Nullable Duration getTimeTaken() {
		return this.timeTaken;
	}

}
