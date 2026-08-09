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

import org.springframework.boot.origin.OriginProvider;

/**
 * 找不到 {@link ConfigData} 时抛出的 {@link ConfigDataNotFoundException}。
 *
 * @author Phillip Webb
 * @since 2.4.0
 */
public abstract class ConfigDataNotFoundException extends ConfigDataException implements OriginProvider {

	/**
	 * 创建新的 {@link ConfigDataNotFoundException} 实例。
	 *
	 * @param message 异常消息
	 * @param cause 异常原因
	 */
	ConfigDataNotFoundException(String message, @Nullable Throwable cause) {
		super(message, cause);
	}

	/**
	 * 返回找不到的实际引用项的描述。
	 *
	 * @return 引用项描述
	 */
	public abstract String getReferenceDescription();

}
