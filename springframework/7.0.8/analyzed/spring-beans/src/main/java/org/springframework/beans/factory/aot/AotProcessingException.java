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

package org.springframework.beans.factory.aot;

import org.jspecify.annotations.Nullable;

/**
 * AOT 处理器执行失败时抛出。
 *
 * @author Stephane Nicoll
 * @since 6.2
 */
@SuppressWarnings("serial")
public class AotProcessingException extends AotException {

	/**
	 * 使用详细消息和可选的根原因创建新实例。
	 * @param msg 详细消息
	 * @param cause 根原因（若有）
	 */
	public AotProcessingException(String msg, @Nullable Throwable cause) {
		super(msg, cause);
	}

}
