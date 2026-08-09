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

package org.springframework.boot.autoconfigure.service.connection;

import org.jspecify.annotations.Nullable;

/**
 * 从给定 {@code source} 创建 {@link ConnectionDetails} 的工厂。
 * 实现类应在 {@code META-INF/spring.factories} 中注册。
 *
 * @param <S> 工厂接受的源类型；实现类应提供有效的 {@code toString}
 * @param <D> 工厂产生的 {@link ConnectionDetails} 类型
 * @author Moritz Halbritter
 * @author Andy Wilkinson
 * @author Phillip Webb
 * @since 3.1.0
 */
public interface ConnectionDetailsFactory<S, D extends ConnectionDetails> {

	/**
	 * 从给定 {@code source} 获取 {@link ConnectionDetails}。若无法创建详情则可能返回 {@code null}。
	 * @param source 源对象
	 * @return 连接详情，或 {@code null}
	 */
	@Nullable D getConnectionDetails(S source);

}
