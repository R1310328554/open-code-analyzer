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

package org.springframework.boot;

import org.springframework.context.ApplicationEvent;

/**
 * 当从 {@link ExitCodeGenerator} 确定应用退出码时发布的事件。
 *
 * @author Phillip Webb
 * @since 1.3.2
 */
public class ExitCodeEvent extends ApplicationEvent {

	private final int exitCode;

	/**
	 * 创建新的 {@link ExitCodeEvent} 实例。
	 *
	 * @param source 事件源
	 * @param exitCode 退出码
	 */
	public ExitCodeEvent(Object source, int exitCode) {
		super(source);
		this.exitCode = exitCode;
	}

	/**
	 * 返回将用于退出 JVM 的退出码。
	 *
	 * @return 退出码
	 */
	public int getExitCode() {
		return this.exitCode;
	}

}
