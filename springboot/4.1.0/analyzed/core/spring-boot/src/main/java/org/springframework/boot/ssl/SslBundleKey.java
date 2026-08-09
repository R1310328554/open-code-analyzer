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

import org.jspecify.annotations.Nullable;

import org.springframework.core.style.ToStringCreator;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * 通过 {@link SslBundle} 获取的单个密钥引用。
 * 包含密钥密码与别名信息。
 *
 * @author Phillip Webb
 * @since 3.1.0
 */
public interface SslBundleKey {

	/**
	 * 不返回任何值的 {@link SslBundleKey} 常量。
	 */
	SslBundleKey NONE = of(null, null);

	/**
	 * 返回访问密钥应使用的密码；若无需密码则返回 {@code null}。
	 *
	 * @return the key password 密钥密码
	 */
	@Nullable String getPassword();

	/**
	 * 返回密钥别名；若密钥无别名则返回 {@code null}。
	 *
	 * @return the key alias 密钥别名
	 */
	@Nullable String getAlias();

	/**
	 * 断言给定密钥库包含此别名。
	 *
	 * @param keyStore the keystore to check 待检查的密钥库
	 */
	default void assertContainsAlias(@Nullable KeyStore keyStore) {
		String alias = getAlias();
		if (StringUtils.hasLength(alias) && keyStore != null) {
			try {
				Assert.state(keyStore.containsAlias(alias),
						() -> String.format("Keystore does not contain alias '%s'", alias));
			}
			catch (KeyStoreException ex) {
				throw new IllegalStateException(
						String.format("Could not determine if keystore contains alias '%s'", alias), ex);
			}
		}
	}

	/**
	 * 工厂方法：创建新的 {@link SslBundleKey} 实例。
	 *
	 * @param password the password used to access the key 访问密钥的密码
	 * @return a new {@link SslBundleKey} instance 新的 {@link SslBundleKey} 实例
	 */
	static SslBundleKey of(String password) {
		return of(password, null);
	}

	/**
	 * 工厂方法：创建新的 {@link SslBundleKey} 实例。
	 *
	 * @param password the password used to access the key 访问密钥的密码
	 * @param alias the alias of the key 密钥别名
	 * @return a new {@link SslBundleKey} instance 新的 {@link SslBundleKey} 实例
	 */
	static SslBundleKey of(@Nullable String password, @Nullable String alias) {
		return new SslBundleKey() {

			@Override
			public @Nullable String getPassword() {
				return password;
			}

			@Override
			public @Nullable String getAlias() {
				return alias;
			}

			@Override
			public String toString() {
				ToStringCreator creator = new ToStringCreator(this);
				creator.append("alias", alias);
				creator.append("password", (password != null) ? "******" : null);
				return creator.toString();
			}

		};
	}

}
