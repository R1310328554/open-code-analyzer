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

import org.jspecify.annotations.Nullable;

import org.springframework.util.StringUtils;

/**
 * {@link PemSslStoreBundle} 中单个信任库或密钥库的详情。
 *
 * @param type the key store type, for example {@code JKS} or {@code PKCS11}. A
 * {@code null} value will use {@link KeyStore#getDefaultType()} 密钥库类型；{@code null} 时使用 {@link KeyStore#getDefaultType()}
 * @param alias the alias used when setting entries in the {@link KeyStore} 在 {@link KeyStore} 中设置条目时使用的别名
 * @param password the password used
 * {@link KeyStore#setKeyEntry(String, java.security.Key, char[], java.security.cert.Certificate[])
 * setting key entries} in the {@link KeyStore} 设置密钥条目时使用的密码
 * @param certificates the certificates content (either the PEM content itself or a
 * reference to the resource to load). When a {@link #privateKey() private key} is present
 * this value is treated as a certificate chain, otherwise it is treated a list of
 * certificates that should all be registered 证书内容（PEM 文本或资源引用）；有私钥时视为证书链，否则视为待注册证书列表
 * @param privateKey the private key content (either the PEM content itself or a reference
 * to the resource to load) 私钥内容（PEM 文本或资源引用）
 * @param privateKeyPassword a password used to decrypt an encrypted private key 解密加密私钥的密码
 * @author Scott Frederick
 * @author Phillip Webb
 * @since 3.1.0
 * @see PemSslStore#load(PemSslStoreDetails)
 */
public record PemSslStoreDetails(@Nullable String type, @Nullable String alias, @Nullable String password,
		@Nullable String certificates, @Nullable String privateKey, @Nullable String privateKeyPassword) {

	/**
	 * 创建新的 {@link PemSslStoreDetails} 实例。
	 * @param type the key store type, for example {@code JKS} or {@code PKCS11}. A
	 * {@code null} value will use {@link KeyStore#getDefaultType()} 密钥库类型
	 * @param alias the alias used when setting entries in the {@link KeyStore} 别名
	 * @param password the password used
	 * {@link KeyStore#setKeyEntry(String, java.security.Key, char[], java.security.cert.Certificate[])
	 * setting key entries} in the {@link KeyStore} 密钥条目密码
	 * @param certificates the certificate content (either the PEM content itself or a
	 * reference to the resource to load) 证书内容
	 * @param privateKey the private key content (either the PEM content itself or a
	 * reference to the resource to load) 私钥内容
	 * @param privateKeyPassword a password used to decrypt an encrypted private key 私钥解密密码
	 * @since 3.2.0
	 */
	public PemSslStoreDetails {
	}

	/**
	 * 创建新的 {@link PemSslStoreDetails} 实例。
	 * @param type the key store type, for example {@code JKS} or {@code PKCS11}. A
	 * {@code null} value will use {@link KeyStore#getDefaultType()} 密钥库类型
	 * @param certificate the certificate content (either the PEM content itself or a
	 * reference to the resource to load) 证书内容
	 * @param privateKey the private key content (either the PEM content itself or a
	 * reference to the resource to load) 私钥内容
	 * @param privateKeyPassword a password used to decrypt an encrypted private key 私钥解密密码
	 */
	public PemSslStoreDetails(@Nullable String type, @Nullable String certificate, @Nullable String privateKey,
			@Nullable String privateKeyPassword) {
		this(type, null, null, certificate, privateKey, privateKeyPassword);
	}

	/**
	 * 创建新的 {@link PemSslStoreDetails} 实例。
	 * @param type the key store type, for example {@code JKS} or {@code PKCS11}. A
	 * {@code null} value will use {@link KeyStore#getDefaultType()} 密钥库类型
	 * @param certificate the certificate content (either the PEM content itself or a
	 * reference to the resource to load) 证书内容
	 * @param privateKey the private key content (either the PEM content itself or a
	 * reference to the resource to load) 私钥内容
	 */
	public PemSslStoreDetails(@Nullable String type, @Nullable String certificate, @Nullable String privateKey) {
		this(type, certificate, privateKey, null);
	}

	/**
	 * 返回使用新别名的新 {@link PemSslStoreDetails} 实例。
	 * @param alias the new alias 新别名
	 * @return a new {@link PemSslStoreDetails} instance 新的 {@link PemSslStoreDetails} 实例
	 * @since 3.2.0
	 */
	public PemSslStoreDetails withAlias(@Nullable String alias) {
		return new PemSslStoreDetails(this.type, alias, this.password, this.certificates, this.privateKey,
				this.privateKeyPassword);
	}

	/**
	 * 返回使用新密码的新 {@link PemSslStoreDetails} 实例。
	 * @param password the new password 新密码
	 * @return a new {@link PemSslStoreDetails} instance 新的 {@link PemSslStoreDetails} 实例
	 * @since 3.2.0
	 */
	public PemSslStoreDetails withPassword(@Nullable String password) {
		return new PemSslStoreDetails(this.type, this.alias, password, this.certificates, this.privateKey,
				this.privateKeyPassword);
	}

	/**
	 * 返回使用新私钥的新 {@link PemSslStoreDetails} 实例。
	 * @param privateKey the new private key 新私钥
	 * @return a new {@link PemSslStoreDetails} instance 新的 {@link PemSslStoreDetails} 实例
	 */
	public PemSslStoreDetails withPrivateKey(@Nullable String privateKey) {
		return new PemSslStoreDetails(this.type, this.alias, this.password, this.certificates, privateKey,
				this.privateKeyPassword);
	}

	/**
	 * 返回使用新私钥密码的新 {@link PemSslStoreDetails} 实例。
	 * @param privateKeyPassword the new private key password 新私钥密码
	 * @return a new {@link PemSslStoreDetails} instance 新的 {@link PemSslStoreDetails} 实例
	 */
	public PemSslStoreDetails withPrivateKeyPassword(@Nullable String privateKeyPassword) {
		return new PemSslStoreDetails(this.type, this.alias, this.password, this.certificates, this.privateKey,
				privateKeyPassword);
	}

	boolean isEmpty() {
		return isEmpty(this.type) && isEmpty(this.certificates) && isEmpty(this.privateKey);
	}

	private boolean isEmpty(@Nullable String value) {
		return !StringUtils.hasText(value);
	}

	/**
	 * 工厂方法：为给定证书创建新的 {@link PemSslStoreDetails} 实例。
	 * <b>注意：</b> 此方法并不验证值是否仅含单个证书，
	 * 功能上等价于 {@link #forCertificates(String)}。
	 * @param certificate the certificate content (either the PEM content itself or a
	 * reference to the resource to load) 证书内容
	 * @return a new {@link PemSslStoreDetails} instance 新的 {@link PemSslStoreDetails} 实例
	 */
	public static PemSslStoreDetails forCertificate(@Nullable String certificate) {
		return forCertificates(certificate);
	}

	/**
	 * 工厂方法：为给定证书创建新的 {@link PemSslStoreDetails} 实例。
	 * @param certificates the certificates content (either the PEM content itself or a
	 * reference to the resource to load) 证书内容
	 * @return a new {@link PemSslStoreDetails} instance 新的 {@link PemSslStoreDetails} 实例
	 * @since 3.2.0
	 */
	public static PemSslStoreDetails forCertificates(@Nullable String certificates) {
		return new PemSslStoreDetails(null, certificates, null);
	}

}
