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

import org.jspecify.annotations.Nullable;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * {@link SpringApplication} 启动失败时发布的事件。
 *
 * @author Dave Syer
 * @since 1.0.0
 * @see ApplicationReadyEvent
 */
@SuppressWarnings("serial")
public class ApplicationFailedEvent extends SpringApplicationEvent {

	private final @Nullable ConfigurableApplicationContext context;

	private final Throwable exception;

	/**
	 * 创建新的 {@link ApplicationFailedEvent} 实例。
	 *
	 * @param application 当前应用
	 * @param args 应用运行参数
	 * @param context 正在创建的上下文（可能为 null）
	 * @param exception 导致失败的异常
	 */
	public ApplicationFailedEvent(SpringApplication application, String[] args,
			@Nullable ConfigurableApplicationContext context, Throwable exception) {
		super(application, args);
		this.context = context;
		this.exception = exception;
	}

	/**
	 * 返回应用上下文。
	 *
	 * @return 上下文，或 {@code null}
	 */
	public @Nullable ConfigurableApplicationContext getApplicationContext() {
		return this.context;
	}

	/**
	 * 返回导致失败的异常。
	 *
	 * @return 异常
	 */
	public Throwable getException() {
		return this.exception;
	}

}
