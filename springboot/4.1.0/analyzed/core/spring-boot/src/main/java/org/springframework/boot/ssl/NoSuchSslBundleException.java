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

package org.springframework.boot.ssl;

import org.jspecify.annotations.Nullable;

/**
 * 表示引用了不存在于注册表中的 {@link SslBundle} 名称时抛出的异常。
 *
 * @author Scott Frederick
 * @since 3.1.0
 */
public class NoSuchSslBundleException extends RuntimeException {

	private final String bundleName;

	/**
	 * 创建新的 {@code NoSuchSslBundleException} 实例。
	 *
	 * @param bundleName the name of the bundle that could not be found 未找到的束名称
	 * @param message the exception message 异常消息
	 */
	public NoSuchSslBundleException(String bundleName, String message) {
		this(bundleName, message, null);
	}

	/**
	 * 创建新的 {@code NoSuchSslBundleException} 实例。
	 *
	 * @param bundleName the name of the bundle that could not be found 未找到的束名称
	 * @param message the exception message 异常消息
	 * @param cause the exception cause 异常原因
	 */
	public NoSuchSslBundleException(String bundleName, String message, @Nullable Throwable cause) {
		super(message, cause);
		this.bundleName = bundleName;
	}

	/**
	 * 返回未找到的 SSL 束名称。
	 *
	 * @return the bundle name SSL 束名称
	 */
	public String getBundleName() {
		return this.bundleName;
	}

}
