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

import org.springframework.boot.ssl.SslBundle;
import org.springframework.boot.ssl.SslBundleRegistry;

/**
 * 向 {@link SslBundleRegistry} 注册 {@link SslBundle} 实例的类型应实现的接口。
 *
 * @author Scott Frederick
 * @since 3.1.0
 */
@FunctionalInterface
public interface SslBundleRegistrar {

	/**
	 * 向 {@link SslBundleRegistry} 注册 {@link SslBundle} 的回调方法。
	 * @param registry 接受 {@code SslBundle} 的注册表
	 */
	void registerBundles(SslBundleRegistry registry);

}
