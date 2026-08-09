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

import org.springframework.util.Assert;

/**
 * 可用于建立 SSL 连接的密钥管理器与信任管理器 bundle。
 * 实例通常 {@link #from(SslStoreBundle, SslBundleKey) 由}
 * {@link SslStoreBundle} 创建。
 *
 * @author Scott Frederick
 * @author Moritz Halbritter
 * @since 3.1.0
 * @see SslStoreBundle
 * @see SslBundle#getManagers()
 */
public interface SslManagerBundle {

	/**
	 * 返回用于建立身份的 {@code KeyManager} 实例。
	 * @return the key managers 密钥管理器数组
	 */
	default KeyManager[] getKeyManagers() {
		return getKeyManagerFactory().getKeyManagers();
	}

	/**
	 * 返回用于建立身份的 {@code KeyManagerFactory}。
	 * @return the key manager factory 密钥管理器工厂
	 */
	KeyManagerFactory getKeyManagerFactory();

	/**
	 * 返回用于建立信任的 {@link TrustManager} 实例。
	 * @return the trust managers 信任管理器数组
	 */
	default TrustManager[] getTrustManagers() {
		return getTrustManagerFactory().getTrustManagers();
	}

	/**
	 * 返回用于建立信任的 {@link TrustManagerFactory}。
	 * @return the trust manager factory 信任管理器工厂
	 */
	TrustManagerFactory getTrustManagerFactory();

	/**
	 * 工厂方法：为本实例管理的 {@link #getKeyManagers() 密钥管理器}
	 * 与 {@link #getTrustManagers() 信任管理器} 创建新的 {@link SSLContext}。
	 * @param protocol the standard name of the SSL protocol SSL 协议标准名称，参见
	 * {@link SSLContext#getInstance(String)}
	 * @return a new {@link SSLContext} instance 新的 {@link SSLContext} 实例
	 */
	default SSLContext createSslContext(String protocol) {
		try {
			SSLContext sslContext = SSLContext.getInstance(protocol);
			sslContext.init(getKeyManagers(), getTrustManagers(), null);
			return sslContext;
		}
		catch (Exception ex) {
			throw new IllegalStateException("Could not load SSL context: " + ex.getMessage(), ex);
		}
	}

	/**
	 * 工厂方法：创建新的 {@link SslManagerBundle} 实例。
	 * @param keyManagerFactory the key manager factory 密钥管理器工厂
	 * @param trustManagerFactory the trust manager factory 信任管理器工厂
	 * @return a new {@link SslManagerBundle} instance 新的 {@link SslManagerBundle} 实例
	 */
	static SslManagerBundle of(KeyManagerFactory keyManagerFactory, TrustManagerFactory trustManagerFactory) {
		Assert.notNull(keyManagerFactory, "'keyManagerFactory' must not be null");
		Assert.notNull(trustManagerFactory, "'trustManagerFactory' must not be null");
		return new SslManagerBundle() {

			@Override
			public KeyManagerFactory getKeyManagerFactory() {
				return keyManagerFactory;
			}

			@Override
			public TrustManagerFactory getTrustManagerFactory() {
				return trustManagerFactory;
			}

		};
	}

	/**
	 * 工厂方法：基于给定 {@link SslStoreBundle} 与 {@link SslBundleKey}
	 * 创建新的 {@link SslManagerBundle}。
	 * @param storeBundle the SSL store bundle SSL 存储 bundle
	 * @param key the key reference 密钥引用
	 * @return a new {@link SslManagerBundle} instance 新的 {@link SslManagerBundle} 实例
	 */
	static SslManagerBundle from(@Nullable SslStoreBundle storeBundle, @Nullable SslBundleKey key) {
		return new DefaultSslManagerBundle(storeBundle, key);
	}

	/**
	 * 工厂方法：使用给定 {@link TrustManagerFactory} 与默认 {@link KeyManagerFactory}
	 * 创建新的 {@link SslManagerBundle}。
	 * @param trustManagerFactory the trust manager factory 信任管理器工厂
	 * @return a new {@link SslManagerBundle} instance 新的 {@link SslManagerBundle} 实例
	 * @since 3.5.0
	 */
	static SslManagerBundle from(TrustManagerFactory trustManagerFactory) {
		Assert.notNull(trustManagerFactory, "'trustManagerFactory' must not be null");
		KeyManagerFactory defaultKeyManagerFactory = createDefaultKeyManagerFactory();
		return of(defaultKeyManagerFactory, trustManagerFactory);
	}

	/**
	 * 工厂方法：使用给定 {@link TrustManager 信任管理器} 与默认 {@link KeyManagerFactory}
	 * 创建新的 {@link SslManagerBundle}。
	 * @param trustManagers the trust managers to use 要使用的信任管理器
	 * @return a new {@link SslManagerBundle} instance 新的 {@link SslManagerBundle} 实例
	 * @since 3.5.0
	 */
	static SslManagerBundle from(TrustManager... trustManagers) {
		Assert.notNull(trustManagers, "'trustManagers' must not be null");
		KeyManagerFactory defaultKeyManagerFactory = createDefaultKeyManagerFactory();
		TrustManagerFactory defaultTrustManagerFactory = createDefaultTrustManagerFactory();
		return of(defaultKeyManagerFactory, FixedTrustManagerFactory.of(defaultTrustManagerFactory, trustManagers));
	}

	private static TrustManagerFactory createDefaultTrustManagerFactory() {
		String defaultAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
		TrustManagerFactory trustManagerFactory;
		try {
			trustManagerFactory = TrustManagerFactory.getInstance(defaultAlgorithm);
			trustManagerFactory.init((KeyStore) null);
		}
		catch (NoSuchAlgorithmException | KeyStoreException ex) {
			throw new IllegalStateException(
					"Unable to create TrustManagerFactory for default '%s' algorithm".formatted(defaultAlgorithm), ex);
		}
		return trustManagerFactory;
	}

	private static KeyManagerFactory createDefaultKeyManagerFactory() {
		String defaultAlgorithm = KeyManagerFactory.getDefaultAlgorithm();
		KeyManagerFactory keyManagerFactory;
		try {
			keyManagerFactory = KeyManagerFactory.getInstance(defaultAlgorithm);
			keyManagerFactory.init(null, null);
		}
		catch (NoSuchAlgorithmException | KeyStoreException | UnrecoverableKeyException ex) {
			throw new IllegalStateException(
					"Unable to create KeyManagerFactory for default '%s' algorithm".formatted(defaultAlgorithm), ex);
		}
		return keyManagerFactory;
	}

}
