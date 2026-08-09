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

import org.springframework.boot.ssl.jks.JksSslStoreBundle;

/**
 * Java 密钥库（JKS 等）格式的 {@link SslBundleProperties}。
 *
 * @author Scott Frederick
 * @author Phillip Webb
 * @since 3.1.0
 * @see JksSslStoreBundle
 */
public class JksSslBundleProperties extends SslBundleProperties {

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
		 * 存储的 Security Provider。
		 */
		private @Nullable String provider;

		/**
		 * 包含存储内容的资源位置。
		 */
		private @Nullable String location;

		/**
		 * 访问存储所用的密码。
		 */
		private @Nullable String password;

		public @Nullable String getType() {
			return this.type;
		}

		public void setType(@Nullable String type) {
			this.type = type;
		}

		public @Nullable String getProvider() {
			return this.provider;
		}

		public void setProvider(@Nullable String provider) {
			this.provider = provider;
		}

		public @Nullable String getLocation() {
			return this.location;
		}

		public void setLocation(@Nullable String location) {
			this.location = location;
		}

		public @Nullable String getPassword() {
			return this.password;
		}

		public void setPassword(@Nullable String password) {
			this.password = password;
		}

	}

}
