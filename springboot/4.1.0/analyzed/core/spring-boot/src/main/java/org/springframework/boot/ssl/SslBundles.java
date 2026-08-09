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

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * 可按名称检索的受管 {@link SslBundle} 实例集合。
 *
 * @author Scott Frederick
 * @author Moritz Halbritter
 * @author Jonatan Ivanov
 * @since 3.1.0
 */
public interface SslBundles {

	/**
	 * 返回指定名称的 {@link SslBundle}。
	 *
	 * @param name the bundle name 束名称
	 * @return the bundle SSL 束
	 * @throws NoSuchSslBundleException if a bundle with the provided name does not exist 若不存在则抛出
	 */
	SslBundle getBundle(String name) throws NoSuchSslBundleException;

	/**
	 * 添加在指定束每次更新时调用的处理器。
	 *
	 * @param name the bundle name 束名称
	 * @param updateHandler the handler that should be called 更新处理器
	 * @throws NoSuchSslBundleException if a bundle with the provided name does not exist 若不存在则抛出
	 * @since 3.2.0
	 */
	void addBundleUpdateHandler(String name, Consumer<SslBundle> updateHandler) throws NoSuchSslBundleException;

	/**
	 * 添加在每次注册束时调用的处理器。
	 * 处理器接收束名称与束实例作为参数。
	 *
	 * @param registerHandler the handler that should be called 注册处理器
	 * @since 3.5.0
	 */
	void addBundleRegisterHandler(BiConsumer<String, SslBundle> registerHandler);

	/**
	 * 返回此实例管理的全部束名称。
	 *
	 * @return the bundle names 束名称列表
	 * @since 3.4.0
	 */
	List<String> getBundleNames();

}
