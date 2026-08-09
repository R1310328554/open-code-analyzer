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

package org.springframework.boot.autoconfigure.ssl;

import org.jspecify.annotations.Nullable;

import org.springframework.boot.ssl.pem.PemSslStoreBundle;

/**
 * PEM 编码证书与私钥的 {@link SslBundleProperties}。
 *
 * @author Scott Frederick
 * @author Phillip Webb
 * @author Moritz Halbritter
 * @since 3.1.0
 * @see PemSslStoreBundle
 */
public class PemSslBundleProperties extends SslBundleProperties {

	/**
	 * 密钥库（keystore）属性。
	 */
	private final Store keystore = new Store();

	/**
	 * 信任库（truststore）属性。
	 */
	private final Store truststore = new Store();

	public Store getKeystore() {
		return this.keystore;
	}

	public Store getTruststore() {
		return this.truststore;
	}

	/**
	 * 存储（store）属性。
	 */
	public static class Store {

		/**
		 * 要创建的存储类型，例如 JKS。
		 */
		private @Nullable String type;

		/**
		 * PEM 格式证书或证书链的位置或内联内容。
		 */
		private @Nullable String certificate;

		/**
		 * PEM 格式私钥的位置或内联内容。
		 */
		private @Nullable String privateKey;

		/**
		 * 解密加密私钥所用的密码。
		 */
		private @Nullable String privateKeyPassword;

		/**
		 * 是否验证私钥与公钥是否匹配。
		 */
		private boolean verifyKeys;

		public @Nullable String getType() {
			return this.type;
		}

		public void setType(@Nullable String type) {
			this.type = type;
		}

		public @Nullable String getCertificate() {
			return this.certificate;
		}

		public void setCertificate(@Nullable String certificate) {
			this.certificate = certificate;
		}

		public @Nullable String getPrivateKey() {
			return this.privateKey;
		}

		public void setPrivateKey(@Nullable String privateKey) {
			this.privateKey = privateKey;
		}

		public @Nullable String getPrivateKeyPassword() {
			return this.privateKeyPassword;
		}

		public void setPrivateKeyPassword(@Nullable String privateKeyPassword) {
			this.privateKeyPassword = privateKeyPassword;
		}

		public boolean isVerifyKeys() {
			return this.verifyKeys;
		}

		public void setVerifyKeys(boolean verifyKeys) {
			this.verifyKeys = verifyKeys;
		}

	}

}
