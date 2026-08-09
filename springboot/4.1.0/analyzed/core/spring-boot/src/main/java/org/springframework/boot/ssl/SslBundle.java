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
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.jspecify.annotations.Nullable;

import org.springframework.core.style.ToStringCreator;
import org.springframework.util.StringUtils;

/**
 * 可用于建立 SSL 连接的信任材料束。
 * 聚合密钥库、密钥引用、SSL 选项与管理器工厂。
 *
 * @author Scott Frederick
 * @author Moritz Halbritter
 * @since 3.1.0
 */
public interface SslBundle {

	/**
	 * 默认使用的 SSL 协议。
	 */
	String DEFAULT_PROTOCOL = "TLS";

	/**
	 * 返回用于访问此束密钥库与信任库的 {@link SslStoreBundle}。
	 *
	 * @return the {@code SslStoreBundle} instance for this bundle 此束的 {@code SslStoreBundle} 实例
	 */
	SslStoreBundle getStores();

	/**
	 * 返回此束应使用的密钥引用，或 {@link SslBundleKey#NONE}。
	 *
	 * @return a reference to the SSL key that should be used 应使用的 SSL 密钥引用
	 */
	SslBundleKey getKey();

	/**
	 * 返回建立 SSL 连接时应应用的 {@link SslOptions}。
	 *
	 * @return the options that should be applied 应应用的选项
	 */
	SslOptions getOptions();

	/**
	 * 返回建立连接时使用的协议。取值应被 {@link SSLContext#getInstance(String)} 支持。
	 *
	 * @return the SSL protocol SSL 协议
	 * @see SSLContext#getInstance(String)
	 */
	String getProtocol();

	/**
	 * 返回用于访问此束 {@link KeyManager 密钥} 与 {@link TrustManager 信任} 管理器的
	 * {@link SslManagerBundle}。
	 *
	 * @return the {@code SslManagerBundle} instance for this bundle 此束的 {@code SslManagerBundle} 实例
	 */
	SslManagerBundle getManagers();

	/**
	 * 工厂方法：为此束创建新的 {@link SSLContext}。
	 *
	 * @return a new {@link SSLContext} instance 新的 {@link SSLContext} 实例
	 */
	default SSLContext createSslContext() {
		return getManagers().createSslContext(getProtocol());
	}

	/**
	 * 工厂方法：创建新的 {@link SslBundle} 实例。
	 *
	 * @param stores the stores or {@code null} 存储束或 {@code null}
	 * @return a new {@link SslBundle} instance 新的 {@link SslBundle} 实例
	 */
	static SslBundle of(@Nullable SslStoreBundle stores) {
		return of(stores, null, null);
	}

	/**
	 * 工厂方法：创建新的 {@link SslBundle} 实例。
	 *
	 * @param stores the stores or {@code null} 存储束或 {@code null}
	 * @param key the key or {@code null} 密钥或 {@code null}
	 * @return a new {@link SslBundle} instance 新的 {@link SslBundle} 实例
	 */
	static SslBundle of(@Nullable SslStoreBundle stores, @Nullable SslBundleKey key) {
		return of(stores, key, null);
	}

	/**
	 * 工厂方法：创建新的 {@link SslBundle} 实例。
	 *
	 * @param stores the stores or {@code null} 存储束或 {@code null}
	 * @param key the key or {@code null} 密钥或 {@code null}
	 * @param options the options or {@code null} 选项或 {@code null}
	 * @return a new {@link SslBundle} instance 新的 {@link SslBundle} 实例
	 */
	static SslBundle of(@Nullable SslStoreBundle stores, @Nullable SslBundleKey key, @Nullable SslOptions options) {
		return of(stores, key, options, null);
	}

	/**
	 * 工厂方法：创建新的 {@link SslBundle} 实例。
	 *
	 * @param stores the stores or {@code null} 存储束或 {@code null}
	 * @param key the key or {@code null} 密钥或 {@code null}
	 * @param options the options or {@code null} 选项或 {@code null}
	 * @param protocol the protocol or {@code null} 协议或 {@code null}
	 * @return a new {@link SslBundle} instance 新的 {@link SslBundle} 实例
	 */
	static SslBundle of(@Nullable SslStoreBundle stores, @Nullable SslBundleKey key, @Nullable SslOptions options,
			@Nullable String protocol) {
		return of(stores, key, options, protocol, null);
	}

	/**
	 * 工厂方法：创建新的 {@link SslBundle} 实例。
	 *
	 * @param stores the stores or {@code null} 存储束或 {@code null}
	 * @param key the key or {@code null} 密钥或 {@code null}
	 * @param options the options or {@code null} 选项或 {@code null}
	 * @param protocol the protocol or {@code null} 协议或 {@code null}
	 * @param managers the managers or {@code null} 管理器束或 {@code null}
	 * @return a new {@link SslBundle} instance 新的 {@link SslBundle} 实例
	 */
	static SslBundle of(@Nullable SslStoreBundle stores, @Nullable SslBundleKey key, @Nullable SslOptions options,
			@Nullable String protocol, @Nullable SslManagerBundle managers) {
		SslManagerBundle managersToUse = (managers != null) ? managers : SslManagerBundle.from(stores, key);
		return new SslBundle() {

			@Override
			public SslStoreBundle getStores() {
				return (stores != null) ? stores : SslStoreBundle.NONE;
			}

			@Override
			public SslBundleKey getKey() {
				return (key != null) ? key : SslBundleKey.NONE;
			}

			@Override
			public SslOptions getOptions() {
				return (options != null) ? options : SslOptions.NONE;
			}

			@Override
			public String getProtocol() {
				return (!StringUtils.hasText(protocol)) ? DEFAULT_PROTOCOL : protocol;
			}

			@Override
			public SslManagerBundle getManagers() {
				return managersToUse;
			}

			@Override
			public String toString() {
				ToStringCreator creator = new ToStringCreator(this);
				creator.append("key", getKey());
				creator.append("options", getOptions());
				creator.append("protocol", getProtocol());
				creator.append("stores", getStores());
				return creator.toString();
			}

		};
	}

	/**
	 * 工厂方法：创建使用系统默认 SSL 配置的 {@link SslBundle}。
	 *
	 * @return a new {@link SslBundle} instance 新的 {@link SslBundle} 实例
	 * @since 3.5.0
	 */
	static SslBundle systemDefault() {
		try {
			KeyManagerFactory keyManagerFactory = KeyManagerFactory
				.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			keyManagerFactory.init(null, null);
			TrustManagerFactory trustManagerFactory = TrustManagerFactory
				.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			trustManagerFactory.init((KeyStore) null);
			SSLContext sslContext = SSLContext.getDefault();
			return of(null, null, null, null, new SslManagerBundle() {
				@Override
				public KeyManagerFactory getKeyManagerFactory() {
					return keyManagerFactory;
				}

				@Override
				public TrustManagerFactory getTrustManagerFactory() {
					return trustManagerFactory;
				}

				@Override
				public SSLContext createSslContext(String protocol) {
					return sslContext;
				}
			});
		}
		catch (NoSuchAlgorithmException | KeyStoreException | UnrecoverableKeyException ex) {
			throw new IllegalStateException("Could not initialize system default SslBundle: " + ex.getMessage(), ex);
		}
	}

}
