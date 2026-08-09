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

import java.security.KeyStore;

import org.jspecify.annotations.Nullable;

import org.springframework.core.style.ToStringCreator;

/**
 * 可用于建立 SSL 连接的密钥库与信任库 bundle。
 *
 * @author Scott Frederick
 * @since 3.1.0
 * @see SslBundle#getStores()
 */
public interface SslStoreBundle {

	/**
	 * 各方法均返回 {@code null} 的 {@link SslStoreBundle} 实例。
	 */
	SslStoreBundle NONE = of(null, null, null);

	/**
	 * 返回由密钥材料生成的密钥库，或 {@code null}。
	 * @return the key store 密钥库
	 */
	@Nullable KeyStore getKeyStore();

	/**
	 * 返回密钥库中密钥的密码，或 {@code null}。
	 * @return the key password 密钥密码
	 */
	@Nullable String getKeyStorePassword();

	/**
	 * 返回由信任材料生成的信任库，或 {@code null}。
	 * @return the trust store 信任库
	 */
	@Nullable KeyStore getTrustStore();

	/**
	 * 工厂方法：创建新的 {@link SslStoreBundle} 实例。
	 * @param keyStore the key store or {@code null} 密钥库或 {@code null}
	 * @param keyStorePassword the key store password or {@code null} 密钥库密码或 {@code null}
	 * @param trustStore the trust store or {@code null} 信任库或 {@code null}
	 * @return a new {@link SslStoreBundle} instance 新的 {@link SslStoreBundle} 实例
	 */
	static SslStoreBundle of(@Nullable KeyStore keyStore, @Nullable String keyStorePassword,
			@Nullable KeyStore trustStore) {
		return new SslStoreBundle() {

			@Override
			public @Nullable KeyStore getKeyStore() {
				return keyStore;
			}

			@Override
			public @Nullable KeyStore getTrustStore() {
				return trustStore;
			}

			@Override
			public @Nullable String getKeyStorePassword() {
				return keyStorePassword;
			}

			@Override
			public String toString() {
				ToStringCreator creator = new ToStringCreator(this);
				creator.append("keyStore.type", (keyStore != null) ? keyStore.getType() : "none");
				creator.append("keyStorePassword", (keyStorePassword != null) ? "******" : null);
				creator.append("trustStore.type", (trustStore != null) ? trustStore.getType() : "none");
				return creator.toString();
			}

		};
	}

}
