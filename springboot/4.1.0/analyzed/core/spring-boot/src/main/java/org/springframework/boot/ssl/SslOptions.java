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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.net.ssl.SSLEngine;

import org.jspecify.annotations.Nullable;

import org.springframework.core.style.ToStringCreator;

/**
 * 建立 SSL 连接时应应用的配置选项。
 *
 * @author Scott Frederick
 * @since 3.1.0
 * @see SslBundle#getOptions()
 */
public interface SslOptions {

	/**
	 * 各方法均返回 {@code null} 的 {@link SslOptions} 实例。
	 */
	SslOptions NONE = of(null, (Set<String>) null);

	/**
	 * 判断是否已指定任何 SSL 选项。
	 * @return {@code true} if SSL options have been specified 已指定 SSL 选项时返回 {@code true}
	 */
	default boolean isSpecified() {
		return (getCiphers() != null) || (getEnabledProtocols() != null);
	}

	/**
	 * 返回可用的密码套件，或 {@code null}。
	 * 名称应与 {@link SSLEngine#getSupportedCipherSuites()} 支持的套件兼容。
	 * @return the ciphers that can be used or {@code null} 可用密码套件或 {@code null}
	 */
	String @Nullable [] getCiphers();

	/**
	 * 返回应启用的协议，或 {@code null}。
	 * 名称应与 {@link SSLEngine#getSupportedProtocols()} 支持的协议兼容。
	 * @return the protocols to enable or {@code null} 要启用的协议或 {@code null}
	 */
	String @Nullable [] getEnabledProtocols();

	/**
	 * 工厂方法：创建新的 {@link SslOptions} 实例。
	 * @param ciphers the ciphers 密码套件
	 * @param enabledProtocols the enabled protocols 启用的协议
	 * @return a new {@link SslOptions} instance 新的 {@link SslOptions} 实例
	 */
	static SslOptions of(String @Nullable [] ciphers, String @Nullable [] enabledProtocols) {
		return new SslOptions() {

			@Override
			public String @Nullable [] getCiphers() {
				return ciphers;
			}

			@Override
			public String @Nullable [] getEnabledProtocols() {
				return enabledProtocols;
			}

			@Override
			public String toString() {
				ToStringCreator creator = new ToStringCreator(this);
				creator.append("ciphers", ciphers);
				creator.append("enabledProtocols", enabledProtocols);
				return creator.toString();
			}

		};
	}

	/**
	 * Factory method to create a new {@link SslOptions} instance.
	 * @param ciphers the ciphers
	 * @param enabledProtocols the enabled protocols
	 * @return a new {@link SslOptions} instance
	 */
	static SslOptions of(@Nullable Set<String> ciphers, @Nullable Set<String> enabledProtocols) {
		return of(toArray(ciphers), toArray(enabledProtocols));
	}

	/**
	 * 辅助方法：以 null 安全方式将 {@code String[]} 转为 {@link Collection}，供客户端库使用。
	 * @param array the array to convert 要转换的数组
	 * @return a collection or {@code null} 集合或 {@code null}
	 */
	static @Nullable Set<String> asSet(String @Nullable [] array) {
		return (array != null) ? Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(array))) : null;
	}

	private static String @Nullable [] toArray(@Nullable Collection<String> collection) {
		return (collection != null) ? collection.toArray(String[]::new) : null;
	}

}
