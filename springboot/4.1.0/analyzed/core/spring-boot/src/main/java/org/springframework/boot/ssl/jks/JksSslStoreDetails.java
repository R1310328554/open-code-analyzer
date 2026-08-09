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

package org.springframework.boot.ssl.jks;

import java.security.KeyStore;

import org.jspecify.annotations.Nullable;

import org.springframework.util.StringUtils;

/**
 * {@link JksSslStoreBundle} 中单个信任库或密钥库的详情。
 *
 * @param type the key store type, for example {@code JKS} or {@code PKCS11}. A
 * {@code null} value will use {@link KeyStore#getDefaultType()} 密钥库类型，例如 {@code JKS} 或 {@code PKCS11}；{@code null} 时使用 {@link KeyStore#getDefaultType()}
 * @param provider the name of the key store provider 密钥库 Provider 名称
 * @param location the location of the key store file or {@code null} if using a
 * {@code PKCS11} hardware store 密钥库文件位置；使用 {@code PKCS11} 硬件库时为 {@code null}
 * @param password the password used to unlock the store or {@code null} 解锁存储的密码或 {@code null}
 * @author Scott Frederick
 * @author Phillip Webb
 * @since 3.1.0
 */
public record JksSslStoreDetails(@Nullable String type, @Nullable String provider, @Nullable String location,
		@Nullable String password) {

	/**
	 * 返回使用新密码的 {@link JksSslStoreDetails} 实例。
	 * @param password the new password 新密码
	 * @return a new {@link JksSslStoreDetails} instance 新的 {@link JksSslStoreDetails} 实例
	 */
	public JksSslStoreDetails withPassword(String password) {
		return new JksSslStoreDetails(this.type, this.provider, this.location, password);
	}

	boolean isEmpty() {
		return isEmpty(this.type) && isEmpty(this.provider) && isEmpty(this.location);
	}

	private boolean isEmpty(@Nullable String value) {
		return !StringUtils.hasText(value);
	}

	/**
	 * 工厂方法：为给定位置创建新的 {@link JksSslStoreDetails} 实例。
	 * @param location the location 资源位置
	 * @return a new {@link JksSslStoreDetails} instance 新的 {@link JksSslStoreDetails} 实例
	 */
	public static JksSslStoreDetails forLocation(@Nullable String location) {
		return new JksSslStoreDetails(null, null, location, null);
	}

}
