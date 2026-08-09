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

package org.springframework.boot.logging;

import org.jspecify.annotations.Nullable;

import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;

/**
 * 日志系统初始化期间传递给 {@link LoggingSystem} 的上下文。
 *
 * @author Phillip Webb
 * @since 1.3.0
 */
public class LoggingInitializationContext {

	private final @Nullable ConfigurableEnvironment environment;

	/**
	 * 创建新的 {@link LoggingInitializationContext} 实例。
	 *
	 * @param environment Spring 环境
	 */
	public LoggingInitializationContext(@Nullable ConfigurableEnvironment environment) {
		this.environment = environment;
	}

	/**
	 * 若可用则返回 Spring 环境。
	 *
	 * @return the {@link Environment} or {@code null} 环境或 {@code null}
	 */
	public @Nullable Environment getEnvironment() {
		return this.environment;
	}

}
