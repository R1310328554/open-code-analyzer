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

package org.springframework.scheduling;

import org.springframework.core.NestedRuntimeException;

/**
 * 调度失败时抛出的通用异常，
 * 例如调度器已关闭。
 * 为 unchecked 异常，因为调度失败通常不可恢复。
 *
 * @author Juergen Hoeller
 * @since 2.0
 */
@SuppressWarnings("serial")
public class SchedulingException extends NestedRuntimeException {

	/**
	 * {@code SchedulingException} 构造器。
	 * @param msg 详细消息
	 */
	public SchedulingException(String msg) {
		super(msg);
	}

	/**
	 * {@code SchedulingException} 构造器。
	 * @param msg 详细消息
	 * @param cause 根本原因（通常来自底层调度 API，如 Quartz）
	 */
	public SchedulingException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
