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

package org.springframework.boot.ssl.pem;

import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.List;

import org.jspecify.annotations.Nullable;

import org.springframework.boot.io.ApplicationResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.Assert;

/**
 * 从 PEM 内容加载的单个信任库或密钥库。
 *
 * @author Phillip Webb
 * @since 3.2.0
 * @see PemSslStoreDetails
 * @see PemContent
 */
public interface PemSslStore {

	/**
	 * 密钥库类型，例如 {@code JKS} 或 {@code PKCS11}；{@code null} 时使用 {@link KeyStore#getDefaultType()}。
	 * @return the key store type 密钥库类型
	 */
	@Nullable String type();

	/**
	 * 在 {@link KeyStore} 中设置条目时使用的别名。
	 * @return the alias 别名
	 */
	@Nullable String alias();

	/**
	 * 在 {@link KeyStore} 中
	 * {@link KeyStore#setKeyEntry(String, java.security.Key, char[], java.security.cert.Certificate[])
	 * 设置密钥条目} 时使用的密码。
	 * @return the password 密码
	 */
	@Nullable String password();

	/**
	 * 此存储的证书。存在 {@link #privateKey() 私钥} 时视为证书链；
	 * 否则视为应全部注册的证书列表。
	 * @return the X509 certificates X509 证书列表
	 */
	@Nullable List<X509Certificate> certificates();

	/**
	 * 此存储的私钥，或 {@code null}。
	 * @return the private key 私钥
	 */
	@Nullable PrivateKey privateKey();

	/**
	 * 返回使用新别名的新 {@link PemSslStore} 实例。
	 * @param alias the new alias 新别名
	 * @return a new {@link PemSslStore} instance 新的 {@link PemSslStore} 实例
	 */
	default PemSslStore withAlias(@Nullable String alias) {
		List<X509Certificate> certificates = certificates();
		Assert.notNull(certificates, "'certificates' must not be null");
		return of(type(), alias, password(), certificates, privateKey());
	}

	/**
	 * 返回使用新密码的新 {@link PemSslStore} 实例。
	 * @param password the new password 新密码
	 * @return a new {@link PemSslStore} instance 新的 {@link PemSslStore} 实例
	 */
	default PemSslStore withPassword(@Nullable String password) {
		List<X509Certificate> certificates = certificates();
		Assert.notNull(certificates, "'certificates' must not be null");
		return of(type(), alias(), password, certificates, privateKey());
	}

	/**
	 * 使用给定 {@link PemSslStoreDetails} 加载并返回 {@link PemSslStore} 实例。
	 * @param details the PEM store details PEM 存储详情
	 * @return a loaded {@link PemSslStore} or {@code null} 已加载的 {@link PemSslStore} 或 {@code null}
	 */
	static @Nullable PemSslStore load(@Nullable PemSslStoreDetails details) {
		return load(details, ApplicationResourceLoader.get());
	}

	/**
	 * 使用给定 {@link PemSslStoreDetails} 与资源加载器加载 {@link PemSslStore}。
	 * @param details the PEM store details PEM 存储详情
	 * @param resourceLoader the resource loader used to load content 用于加载内容的资源加载器
	 * @return a loaded {@link PemSslStore} or {@code null} 已加载的 {@link PemSslStore} 或 {@code null}
	 * @since 3.3.5
	 */
	static @Nullable PemSslStore load(@Nullable PemSslStoreDetails details, ResourceLoader resourceLoader) {
		if (details == null || details.isEmpty()) {
			return null;
		}
		return new LoadedPemSslStore(details, resourceLoader);
	}

	/**
	 * 工厂方法：使用给定值创建新的 {@link PemSslStore}。
	 * @param type the key store type 密钥库类型
	 * @param certificates the certificates for this store 此存储的证书
	 * @param privateKey the private key 私钥
	 * @return a new {@link PemSslStore} instance 新的 {@link PemSslStore} 实例
	 */
	static PemSslStore of(@Nullable String type, List<X509Certificate> certificates, @Nullable PrivateKey privateKey) {
		return of(type, null, null, certificates, privateKey);
	}

	/**
	 * 工厂方法：使用给定证书与私钥创建新的 {@link PemSslStore}。
	 * @param certificates the certificates for this store 此存储的证书
	 * @param privateKey the private key 私钥
	 * @return a new {@link PemSslStore} instance 新的 {@link PemSslStore} 实例
	 */
	static PemSslStore of(List<X509Certificate> certificates, @Nullable PrivateKey privateKey) {
		return of(null, null, null, certificates, privateKey);
	}

	/**
	 * 工厂方法：使用给定值创建新的 {@link PemSslStore}。
	 * @param type the key store type 密钥库类型
	 * @param alias the alias used when setting entries in the {@link KeyStore} 在 {@link KeyStore} 中设置条目时使用的别名
	 * @param password the password used
	 * {@link KeyStore#setKeyEntry(String, java.security.Key, char[], java.security.cert.Certificate[])
	 * setting key entries} in the {@link KeyStore} 设置密钥条目时使用的密码
	 * @param certificates the certificates for this store 此存储的证书
	 * @param privateKey the private key 私钥
	 * @return a new {@link PemSslStore} instance 新的 {@link PemSslStore} 实例
	 */
	static PemSslStore of(@Nullable String type, @Nullable String alias, @Nullable String password,
			List<X509Certificate> certificates, @Nullable PrivateKey privateKey) {
		Assert.notEmpty(certificates, "'certificates' must not be empty");
		return new PemSslStore() {

			@Override
			public @Nullable String type() {
				return type;
			}

			@Override
			public @Nullable String alias() {
				return alias;
			}

			@Override
			public @Nullable String password() {
				return password;
			}

			@Override
			public List<X509Certificate> certificates() {
				return certificates;
			}

			@Override
			public @Nullable PrivateKey privateKey() {
				return privateKey;
			}

		};
	}

}
