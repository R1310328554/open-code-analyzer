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

package org.springframework.boot.context.config;

import org.jspecify.annotations.Nullable;

/**
 * 配置数据异常的抽象基类。
 *
 * @author Phillip Webb
 * @author Madhura Bhave
 * @since 2.4.0
 */
public abstract class ConfigDataException extends RuntimeException {

	/**
	 * 创建新的 {@link ConfigDataException} 实例。
	 *
	 * @param message 异常消息
	 * @param cause 异常原因
	 */
	protected ConfigDataException(String message, @Nullable Throwable cause) {
		super(message, cause);
	}

}
