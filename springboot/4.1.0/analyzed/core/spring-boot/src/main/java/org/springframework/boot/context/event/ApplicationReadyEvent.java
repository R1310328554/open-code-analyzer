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

import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * 尽可能晚地发布、表示应用已准备好处理请求的事件。
 * 事件源为 {@link SpringApplication} 本身，但此时所有初始化步骤均已完成，
 * 应避免修改其内部状态。
 *
 * @author Stephane Nicoll
 * @author Chris Bono
 * @since 1.3.0
 * @see ApplicationFailedEvent
 */
@SuppressWarnings("serial")
public class ApplicationReadyEvent extends SpringApplicationEvent {

	private final ConfigurableApplicationContext context;

	private final @Nullable Duration timeTaken;

	/**
	 * 创建新的 {@link ApplicationReadyEvent} 实例。
	 *
	 * @param application 当前应用
	 * @param args 应用运行参数
	 * @param context 正在创建的上下文
	 * @param timeTaken 应用就绪所耗时间
	 * @since 2.6.0
	 */
	public ApplicationReadyEvent(SpringApplication application, String[] args, ConfigurableApplicationContext context,
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
	 * 返回应用就绪所耗时间；未知时为 {@code null}。
	 *
	 * @return 就绪所耗时间
	 * @since 2.6.0
	 */
	public @Nullable Duration getTimeTaken() {
		return this.timeTaken;
	}

}
