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

package org.springframework.boot.context.properties.bind;

import org.jspecify.annotations.Nullable;

import org.springframework.boot.context.properties.source.ConfigurationProperty;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;

/**
 * 供 {@link BindHandler BindHandler} 使用的上下文信息。
 *
 * @author Phillip Webb
 * @author Madhura Bhave
 * @since 2.0.0
 */
public interface BindContext {

	/**
	 * 返回正在执行绑定操作的源 Binder。
	 *
	 * @return 源 Binder
	 */
	Binder getBinder();

	/**
	 * 返回当前绑定深度。根绑定从 {@code 0} 开始，每绑定一层属性深度加 {@code 1}。
	 *
	 * @return 当前绑定深度
	 */
	int getDepth();

	/**
	 * 返回 {@link Binder} 正在使用的 {@link ConfigurationPropertySource 属性源} 集合。
	 *
	 * @return 属性源
	 */
	Iterable<ConfigurationPropertySource> getSources();

	/**
	 * 返回实际正在绑定的 {@link ConfigurationProperty}；属性尚未确定时返回 {@code null}。
	 *
	 * @return 配置属性（可能为 {@code null}）
	 */
	@Nullable ConfigurationProperty getConfigurationProperty();

}
