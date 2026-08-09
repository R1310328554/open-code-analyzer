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

/**
 * 可为指定名称注册 {@link SslBundle} 的接口。
 *
 * @author Scott Frederick
 * @author Moritz Halbritter
 * @since 3.1.0
 */
public interface SslBundleRegistry {

	/**
	 * 注册具名 {@link SslBundle}。
	 *
	 * @param name the bundle name 束名称
	 * @param bundle the bundle SSL 束
	 */
	void registerBundle(String name, SslBundle bundle);

	/**
	 * 更新 {@link SslBundle}。
	 *
	 * @param name the bundle name 束名称
	 * @param updatedBundle the updated bundle 更新后的 SSL 束
	 * @throws NoSuchSslBundleException if the bundle cannot be found 若找不到束则抛出
	 * @since 3.2.0
	 */
	void updateBundle(String name, SslBundle updatedBundle) throws NoSuchSslBundleException;

}
